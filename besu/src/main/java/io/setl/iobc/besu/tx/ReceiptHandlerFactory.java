package io.setl.iobc.besu.tx;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import io.setl.iobc.model.TransactionInput.TxProcessingMode;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;

/**
 * A creator of receipt handlers.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface ReceiptHandlerFactory {

  /**
   * Create a simple result for when we want to return just the transaction hash.
   *
   * @param blockNumber optional block number.
   * @param txHash      the transaction hash.
   *
   * @return sample result
   */
  static CompletableFuture<TransactionResult> prepareIdOnly(BigInteger blockNumber, String txHash) {
    return prepareIdOnly(blockNumber, txHash, null);
  }


  /**
   * Create a simple result for when we want to return just the transaction hash.
   *
   * @param blockNumber optional block number.
   * @param txHash      the transaction hash.
   *
   * @return sample result
   */
  static CompletableFuture<TransactionResult> prepareIdOnly(BigInteger blockNumber, String txHash, CompletableFuture<TransactionResult> continues) {
    TransactionResult.TransactionResultBuilder builder = TransactionResult.builder()
        .transactionId(txHash)
        .blockNumber(blockNumber)
        .continuation(continues)
        .txStatus(TxStatus.PENDING);

    return CompletableFuture.completedFuture(builder.build());
  }


  /**
   * Prepare a receipt handler and return the future it manages.
   *
   * @param mode        the required processing mode for the transaction
   * @param recentBlock a recent block number, if known
   * @param txHash      the transaction's hash
   *
   * @return the future.
   */
  CompletableFuture<TransactionResult> prepare(TxProcessingMode mode, BigInteger recentBlock, String txHash);

}
