package io.setl.iobc.besu.tx;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

import io.setl.iobc.model.TransactionInput.TxProcessingMode;
import io.setl.iobc.model.TransactionResult;

/**
 * Create simple receipt handlers.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Slf4j
public class SimpleReceiptHandlerFactory implements ReceiptHandlerFactory {

  private final CommonTransactionReceiptHandler receiptHandler;


  public SimpleReceiptHandlerFactory(
      CommonTransactionReceiptHandler receiptHandler
  ) {
    this.receiptHandler = receiptHandler;
  }


  @Override
  public CompletableFuture<TransactionResult> prepare(
      TxProcessingMode mode, BigInteger recentBlock, String txHash
  ) {
    SimpleReceiptHandler handler = new SimpleReceiptHandler();
    receiptHandler.register(txHash, handler);
    final CompletableFuture<TransactionResult> result = handler.getResult();
    if (mode == TxProcessingMode.RETURN_RESULT) {
      return result;
    }

    CompletableFuture<TransactionResult> initial = ReceiptHandlerFactory.prepareIdOnly(recentBlock, txHash);
    return initial
        .thenApply(r -> {
          r.setContinuation(result);
          return r;
        });
  }


}
