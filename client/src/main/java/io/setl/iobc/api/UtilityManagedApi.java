package io.setl.iobc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.model.tx.GetBlockForTime;
import io.setl.iobc.model.tx.GetBlockNumber;
import io.setl.iobc.model.tx.GetTransactionResult;
import io.setl.iobc.model.tx.VerifyCreateToken;

/**
 * Access the IOBC utility API, managing correlation of responses in the caller.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Component
public class UtilityManagedApi {

  private final InboundProducer producer;


  @Autowired
  public UtilityManagedApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Get the current block number.
   *
   * @param userId the requesting user
   *
   * @return the message ID
   */
  public InReplyTo getBlockNumber(String userId) {
    return producer.sendMessage(userId, GetBlockNumber.NAME, null);
  }


  /**
   * Get a block number which is the closest block to the specified point in time at which a token can be queried.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getBlockNumber(String userId, GetBlockForTime.Input input) {
    return producer.sendMessage(userId, GetBlockForTime.NAME, input);
  }


  /**
   * Get the result of a transaction.
   *
   * @param userId the requesting user
   * @param input  the transaction's identifier
   *
   * @return the messageID
   */
  public InReplyTo getTransactionResult(String userId, GetTransactionResult.Input input) {
    return producer.sendMessage(userId, GetTransactionResult.NAME, input);
  }


  /**
   * Get the result of a "create token transaction". IOBC has to process the transaction result of the Create Token transaction itself. If there was an
   * internal failure, this call will re-trigger that processing.
   *
   * @param userId the requesting user
   * @param input  the transaction's identifier
   *
   * @return the transaction result
   */
  public InReplyTo verifyCreateToken(String userId, GetTransactionResult.Input input) {
    return producer.sendMessage(userId, VerifyCreateToken.NAME, input);
  }

}
