package io.setl.iobc.api;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tx.GetBlockForTime;
import io.setl.iobc.model.tx.GetBlockNumber;
import io.setl.iobc.model.tx.GetTransactionResult;
import io.setl.iobc.model.tx.VerifyCreateToken;

/**
 * Access the IOBC utility API.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Component
public class UtilityApi {

  private final InboundProducer producer;


  @Autowired
  public UtilityApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Get the current block number.
   *
   * @param userId the requesting user
   *
   * @return the block number
   */
  public CompletableFuture<GetBlockNumber.Output> getBlockNumber(String userId) {
    return producer.send(userId, GetBlockNumber.NAME, null, GetBlockNumber.Output.class);
  }


  /**
   * Get a block number which is the closest block to the specified point in time at which a token can be queried.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the block number
   */
  public CompletableFuture<GetBlockForTime.Output> getBlockNumber(String userId, GetBlockForTime.Input input) {
    return producer.send(userId, GetBlockForTime.NAME, input, GetBlockForTime.Output.class);
  }


  /**
   * Get the result of a transaction.
   *
   * @param userId the requesting user
   * @param input  the transaction's identifier
   *
   * @return the transaction result
   */
  public CompletableFuture<TransactionResult> getTransactionResult(String userId, GetTransactionResult.Input input) {
    return producer.send(userId, GetTransactionResult.NAME, input, TransactionResult.class);
  }


  /**
   * Get the result of a "create token" transaction. IOBC has to process the transaction result of the Create Token transaction itself. If there was an
   * internal failure, this call will re-trigger that processing.
   *
   * @param userId the requesting user
   * @param input  the transaction's identifier
   *
   * @return the transaction result
   */
  public CompletableFuture<TransactionResult> verifyCreateToken(String userId, GetTransactionResult.Input input) {
    return producer.send(userId, VerifyCreateToken.NAME, input, TransactionResult.class);
  }

}
