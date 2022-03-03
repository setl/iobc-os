package io.setl.iobc.besu.tx;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.TransactionResult.TxStatus;
import io.setl.iobc.util.SerdeSupport;
import io.setl.json.CJObject;

/**
 * Simple handler for basic transaction confirmations.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Slf4j
public class SimpleReceiptHandler implements ReceiptHandler {

  protected final CompletableFuture<TransactionResult> result = new CompletableFuture<>();


  @Override
  public void accept(TransactionReceipt transactionReceipt) {
    if (log.isInfoEnabled()) {
      String json = SerdeSupport.getObjectMapper().convertValue(transactionReceipt, CJObject.class).toPrettyString();
      log.info("Received transaction receipt:\n{}", json);
    }

    if (transactionReceipt.isStatusOK()) {
      handleSuccess(transactionReceipt);
    } else {
      handleFailure(transactionReceipt);
    }
  }


  public CompletableFuture<TransactionResult> getResult() {
    return result;
  }


  protected void handleFailure(TransactionReceipt transactionReceipt) {
    CJObject cjObject = makeFailureData(transactionReceipt);
    result.complete(TransactionResult.builder()
        .transactionId(transactionReceipt.getTransactionHash())
        .blockNumber(transactionReceipt.getBlockNumber())
        .txStatus(TxStatus.FAILURE)
        .additionalData(cjObject)
        .build());
  }


  protected void handleSuccess(TransactionReceipt transactionReceipt) {
    CJObject cjObject = makeSuccessData(transactionReceipt);
    result.complete(TransactionResult.builder()
        .transactionId(transactionReceipt.getTransactionHash())
        .blockNumber(transactionReceipt.getBlockNumber())
        .txStatus(TxStatus.SUCCESS)
        .additionalData(cjObject)
        .build());
  }


  protected CJObject makeFailureData(TransactionReceipt transactionReceipt) {
    CJObject cjObject = new CJObject();
    cjObject.put("revertReason", transactionReceipt.getRevertReason());
    cjObject.put("blockHash", transactionReceipt.getBlockHash());
    cjObject.put("status", transactionReceipt.getStatus());
    return cjObject;
  }


  protected CJObject makeSuccessData(TransactionReceipt transactionReceipt) {
    CJObject cjObject = new CJObject();
    cjObject.put("blockHash", transactionReceipt.getBlockHash());
    cjObject.put("blockNumber", transactionReceipt.getBlockNumber());
    cjObject.put("contractAddress", transactionReceipt.getContractAddress());
    return cjObject;
  }

}
