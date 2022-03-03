package io.setl.iobc.besu.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.response.EmptyTransactionReceipt;
import org.web3j.tx.response.TransactionReceiptProcessor;

import io.setl.common.MutableInt;

/**
 * Transaction handler that invokes a callback when the transaction receipt becomes available.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Slf4j
public class CommonTransactionReceiptHandler extends TransactionReceiptProcessor {

  /** The prefix used to mark revert reasons in Besu. This is first 32 bits of the Keccak-256 of "Error(string)". */
  private static final String REVERT_ERROR_METHOD_ID = "0x08c379a0";

  /** The prefix used to mark panic reasons in Besu. This is first 32 bits of the Keccak-256 of "Panic(uint256)". */
  private static final String REVERT_PANIC_METHOD_ID = "0x4e487b71";

  private static final List<TypeReference<Type>> REVERT_PANIC_TYPES;

  private static final List<TypeReference<Type>> REVERT_REASON_TYPES;



  private static class MyEmptyReceipt extends EmptyTransactionReceipt {

    public MyEmptyReceipt(String transactionHash) {
      super(transactionHash);
    }


    /**
     * Workaround for contract creation requiring a non-null address.
     *
     * @return ""
     */
    @Override
    public String getContractAddress() {
      return "";
    }


    /**
     * Workaround for contract creation checking immediately for success.
     *
     * @return null
     */
    @Override
    public String getStatus() {
      return null;
    }

  }


  /**
   * Attempt to decode the information provided as a "revert reason" from the Ethereum block-chain. Updates the receipt if a revert reason is present and can be
   * decoded.
   *
   * @param receipt the transaction receipt which is updated
   */
  public static void decodeRevertReason(TransactionReceipt receipt) {
    String reason = receipt.getRevertReason();
    if (reason == null || reason.isEmpty()) {
      return;
    }

    if (reason.startsWith(REVERT_ERROR_METHOD_ID)) {
      String encodedRevertReason = reason.substring(REVERT_ERROR_METHOD_ID.length());
      List<Type> decoded = FunctionReturnDecoder.decode(encodedRevertReason, REVERT_REASON_TYPES);
      Utf8String decodedRevertReason = (Utf8String) decoded.get(0);
      receipt.setRevertReason(decodedRevertReason.getValue());
    }

    if (reason.startsWith(REVERT_PANIC_METHOD_ID)) {
      String encodedRevertReason = reason.substring(REVERT_PANIC_METHOD_ID.length());
      List<Type> decoded = FunctionReturnDecoder.decode(encodedRevertReason, REVERT_PANIC_TYPES);
      Uint256 decodedRevertReason = (Uint256) decoded.get(0);
      BigInteger reasonCode = decodedRevertReason.getValue();
      if (reasonCode.bitLength() < 32) {
        switch (reasonCode.intValue()) {
          case 0x01:
            receipt.setRevertReason("PANIC: Explicit assert invoked (0x01)");
            break;
          case 0x11:
            receipt.setRevertReason("PANIC: arithmetic overflow or underflow (0x11)");
            break;
          case 0x12:
            receipt.setRevertReason("PANIC: division by zero (or modulo zero) (0x12)");
            break;
          case 0x22:
            receipt.setRevertReason("PANIC: access attempted of incorrect encoded storage byte array (0x22)");
            break;
          case 0x31:
            receipt.setRevertReason("PANIC: '.pop()' called on an empty array (0x31)");
            break;
          case 0x32:
            receipt.setRevertReason("PANIC: array index out of bounds. (0x32)");
            break;
          case 0x41:
            receipt.setRevertReason("PANIC: Too much memory allocated, or array too large (0x41)");
            break;
          case 0x51:
            receipt.setRevertReason("PANIC: called an zero-initialized variable of internal function type (0x51)");
            break;
          default:
            receipt.setRevertReason("PANIC: 0x" + decodedRevertReason.getValue().toString(16));
            break;
        }
      } else {
        receipt.setRevertReason("PANIC: 0x" + decodedRevertReason.getValue().toString(16));
      }
    }
  }


  static {
    try {
      REVERT_REASON_TYPES = List.of(TypeReference.makeTypeReference("string"));
      REVERT_PANIC_TYPES = List.of(TypeReference.makeTypeReference("uint256"));
    } catch (ClassNotFoundException e) {
      throw new InternalError("ABI type 'string' was not recognised");
    }
  }

  private final Map<String, Consumer<TransactionReceipt>> callbacks = new ConcurrentHashMap<>();

  private final AtomicBoolean isRunning = new AtomicBoolean(false);

  private final Object lock = new Object();

  private final Map<String, MutableInt> pending = new ConcurrentHashMap<>();

  private final ExecutorService service;

  private final Map<String, TransactionReceipt> unclaimed = new ConcurrentHashMap<>();

  private final Web3j web3j;


  /**
   * New instance.
   */
  public CommonTransactionReceiptHandler(Web3j web3j, ExecutorService service) {
    super(web3j);
    this.web3j = web3j;
    this.service = service;
  }


  /**
   * Register a callback to handle the receipt of a transaction.
   *
   * @param transactionHash the transaction's hash
   * @param callback        the callback to invoke.
   */
  public void register(String transactionHash, ReceiptHandler callback) {
    if (callback.getResult().isDone()) {
      return;
    }

    log.info("Waiting for response on transaction {}", transactionHash);
    synchronized (lock) {
      TransactionReceipt receipt = unclaimed.remove(transactionHash);
      if (receipt != null) {
        service.submit(() -> callback.accept(receipt));
        return;
      }

      callbacks.put(transactionHash, callback);
    }
  }


  /**
   * Scan periodically to check if transactions have completed.
   */
  public void scan() {
    if (!isRunning.compareAndSet(false, true)) {
      return;
    }
    try {
      for (var e : pending.entrySet()) {
        String txHash = e.getKey();
        int count = e.getValue().increment();
        log.info("Checking transaction {}. Attempt {}", txHash, count);

        EthGetTransactionReceipt transactionReceipt;
        try {
          transactionReceipt = web3j.ethGetTransactionReceipt(txHash).send();
        } catch (IOException exception) {
          log.error("Failed to fetch transaction details {}", txHash, exception);
          continue;
        }

        // Check the JSON-RPC call was OK
        if (transactionReceipt.hasError()) {
          Response.Error error = transactionReceipt.getError();
          log.error("Remote server reports JSON-RPC error code {}:\nMessage: {}\nData: {}",
              error.getCode(), error.getMessage(), error.getData()
          );
          continue;
        }

        // Did we get a receipt?
        Optional<? extends TransactionReceipt> optionalReceipt = transactionReceipt.getTransactionReceipt();
        if (optionalReceipt.isPresent()) {
          log.info("Received transaction receipt for transaction {}", txHash);

          // we have a receipt so invoke callback
          pending.remove(txHash);
          TransactionReceipt receipt = optionalReceipt.get();
          decodeRevertReason(receipt);
          synchronized (lock) {
            Consumer<TransactionReceipt> callback = callbacks.remove(txHash);
            if (callback != null) {
              service.submit(() -> callback.accept(receipt));
            } else {
              unclaimed.put(txHash, receipt);
            }
          }
        }

        // no receipt, so keep trying
      }
    } finally {
      isRunning.set(false);
    }
  }


  @Override
  public TransactionReceipt waitForTransactionReceipt(String transactionHash) {
    pending.put(transactionHash, new MutableInt(0));
    return new MyEmptyReceipt(transactionHash);
  }

}
