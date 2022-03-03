package io.setl.iobc.besu.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.json.CJObject;

/**
 * Get the result of a transaction.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Service
@Slf4j
public class GetTransactionResultImpl implements io.setl.iobc.model.tx.GetTransactionResult {

  /**
   * Get the result of a transaction for the BESU server.
   *
   * @param web3j  Web3 client
   * @param txHash the transaction's hash
   *
   * @return the result of the transaction, which may still be PENDING
   */
  public static TransactionResult getResult(Web3j web3j, String txHash) {
    CJObject data = new CJObject();
    TransactionResult.TransactionResultBuilder builder = TransactionResult.builder().transactionId(txHash).additionalData(data);

    EthGetTransactionReceipt transactionReceipt;
    try {
      transactionReceipt = web3j.ethGetTransactionReceipt(txHash).send();
    } catch (IOException exception) {
      log.error("Failed to fetch transaction details {}", txHash, exception);
      data.put("error", "IO_EXCEPTION");
      data.put("message", exception.toString());
      return builder.txStatus(TxStatus.UNKNOWN).build();
    }

    // Check the JSON-RPC call was OK
    if (transactionReceipt.hasError()) {
      Response.Error error = transactionReceipt.getError();
      log.error("Remote server reports JSON-RPC error code {}:\nMessage: {}\nData: {}",
          error.getCode(), error.getMessage(), error.getData()
      );

      data.put("error", "JSON-RPC FAILURE");
      data.put("code", error.getCode());
      data.put("message", error.getMessage());
      data.put("data", error.getData());
      return builder.txStatus(TxStatus.UNKNOWN).build();
    }

    // We get this *before* getting the transaction receipt to ensure that it is not higher than the current block when we get the receipt.
    BigInteger blockNumber = GetBlockNumberImpl.getRecentBlock(web3j);

    // Did we get a receipt?
    Optional<? extends TransactionReceipt> receiptOptional = transactionReceipt.getTransactionReceipt();
    if (receiptOptional.isPresent()) {
      log.info("Received transaction receipt for transaction {}", txHash);

      // we have a receipt so create response
      TransactionReceipt receipt = receiptOptional.get();
      CommonTransactionReceiptHandler.decodeRevertReason(receipt);

      builder.blockNumber(receipt.getBlockNumber());

      // copy useful data, if available
      notNull(data, "blockNumber", receipt.getBlockNumberRaw());
      notNull(data, "blockHash", receipt.getBlockHash());
      notNull(data, "contractAddress", receipt.getContractAddress());
      notNull(data, "from", receipt.getFrom());
      notNull(data, "revertReason", receipt.getRevertReason());
      notNull(data, "status", receipt.getStatus());
      notNull(data, "to", receipt.getTo());
      notNull(data, "transactionHash", receipt.getTransactionHash());
      notNull(data, "transactionIndex", receipt.getTransactionIndexRaw());
      notNull(data, "type", receipt.getType());

      if (receipt.isStatusOK()) {
        builder.txStatus(TxStatus.SUCCESS);
      } else {
        builder.txStatus(TxStatus.FAILURE);
      }
    } else {
      builder.blockNumber(blockNumber).txStatus(TxStatus.PENDING);
    }

    return builder.build();
  }


  private static void notNull(CJObject cjObject, String key, String value) {
    if (value != null) {
      cjObject.put(key, value);
    }
  }


  @Override
  public CompletableFuture<TransactionResult> apply(ChainConfiguration configuration, Input input) {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;
    return CompletableFuture.completedFuture(getResult(besu.getWeb3j(), input.getTransactionId()));
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
