package io.setl.iobc.besu.tx;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import io.setl.iobc.besu.BesuChainConfiguration;
import io.setl.iobc.config.ChainConfiguration;
import io.setl.iobc.model.ChainBrand;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.TransactionInput.TxProcessingMode;

/**
 * Get the latest block number.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Slf4j
@Service
public class GetBlockNumberImpl implements io.setl.iobc.model.tx.GetBlockNumber {

  private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

  /** The number of milliseconds for which a block number can be considered recent. */
  private static final long RECENT_DURATION = 10_000;

  /** Lock for working with the recent block number. */
  private static final Object RECENT_LOCK = new Object();

  /** The recent block number. */
  private static BigInteger recentBlock = MINUS_ONE;

  /** The time the recent block was fetched. */
  private static long recentTime = 0;


  /**
   * Get the current block number, or -1 if it is unavailable.
   *
   * @param web3j Web3J instance
   *
   * @return block number (or -1)
   */
  public static BigInteger get(Web3j web3j) {
    // Mode must be ID only. Get the minimum block number.
    Request<?, EthBlockNumber> request = web3j.ethBlockNumber();
    EthBlockNumber response;
    try {
      response = request.send();
    } catch (IOException ioException) {
      log.error("Failed to fetch block number", ioException);
      return MINUS_ONE;
    }

    // Check the JSON-RPC call was OK
    if (response.hasError()) {
      Response.Error error = response.getError();
      log.error("Remote server reports JSON-RPC error code {}:\nMessage: {}\nData: {}",
          error.getCode(), error.getMessage(), error.getData()
      );
      return MINUS_ONE;
    }

    return response.getBlockNumber();
  }


  /**
   * Get a recent block number, but only if the processing mode is RETURN_ID.
   *
   * @param mode  if RETURN_ID, get the block number so an ID-only return can supply it
   * @param web3j for fetching a block number if a recent one is not known
   *
   * @return a recent block number, or null
   */
  public static BigInteger getRecentBlock(Web3j web3j, TxProcessingMode mode) {
    if (mode != TxProcessingMode.RETURN_ID) {
      return null;
    }
    return getRecentBlock(web3j);
  }


  /**
   * Get a recent block number. This may not be the current block number.
   *
   * @param web3j for fetching a block number if a recent one is not known
   *
   * @return a recent block number
   */
  public static BigInteger getRecentBlock(Web3j web3j) {
    synchronized (RECENT_LOCK) {
      long now = System.currentTimeMillis();
      if ((now - recentTime) > RECENT_DURATION) {
        // need to update the recent block number
        BigInteger newBlock = get(web3j);
        if (!newBlock.equals(MINUS_ONE)) {
          recentBlock = newBlock;
        }
        recentTime = now;
      }

      return recentBlock;
    }
  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, MessageInput input) {
    BesuChainConfiguration besu = (BesuChainConfiguration) configuration;

    BigInteger blockNumber = get(besu.getWeb3j());
    return CompletableFuture.completedFuture(Output.builder().blockNumber(blockNumber).build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.BESU;
  }

}
