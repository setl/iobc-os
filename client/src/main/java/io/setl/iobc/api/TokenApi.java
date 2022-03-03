package io.setl.iobc.api;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.ApproveTransfer;
import io.setl.iobc.model.tokens.BurnToken;
import io.setl.iobc.model.tokens.ControllerTransfer;
import io.setl.iobc.model.tokens.CreateToken;
import io.setl.iobc.model.tokens.DeleteToken;
import io.setl.iobc.model.tokens.GetAllTokens;
import io.setl.iobc.model.tokens.GetAllowance;
import io.setl.iobc.model.tokens.GetBalance;
import io.setl.iobc.model.tokens.GetHoldings;
import io.setl.iobc.model.tokens.GetLocked;
import io.setl.iobc.model.tokens.GetName;
import io.setl.iobc.model.tokens.GetTotalSupply;
import io.setl.iobc.model.tokens.LockToken;
import io.setl.iobc.model.tokens.MintToken;
import io.setl.iobc.model.tokens.TerminateToken;
import io.setl.iobc.model.tokens.TransferFrom;
import io.setl.iobc.model.tokens.TransferToken;
import io.setl.iobc.model.tokens.UnlockToken;
import io.setl.iobc.model.tokens.dvp.DvpCancel;
import io.setl.iobc.model.tokens.dvp.DvpControllerCommit;
import io.setl.iobc.model.tokens.dvp.DvpControllerCreate;
import io.setl.iobc.model.tokens.dvp.DvpId;
import io.setl.iobc.model.tokens.dvp.GetDvpTrade;

/**
 * Entry points for the Token Api.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class TokenApi {

  private final InboundProducer producer;


  @Autowired
  public TokenApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Perform an ERC-20 style transfer approval.
   *
   * @param userId the requesting user
   * @param input  the details of the approval
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> approveTransfer(String userId, ApproveTransfer.Input input) {
    return producer.send(userId, ApproveTransfer.NAME, input, TransactionResult.class);
  }


  /**
   * Burn an amount of tokens.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> burnToken(String userId, BurnToken.Input input) {
    return producer.send(userId, BurnToken.NAME, input, TransactionResult.class);
  }


  /**
   * Perform a controller transfer of assets.
   *
   * @param userId the requesting user
   * @param input  the details of the transfer
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> controllerTransfer(String userId, ControllerTransfer.Input input) {
    return producer.send(userId, ControllerTransfer.NAME, input, TransactionResult.class);
  }


  /**
   * Create a new token.
   *
   * @param userId the requesting user
   * @param input  the details of the new token
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> createToken(String userId, CreateToken.Input input) {
    return producer.send(userId, CreateToken.NAME, input, TransactionResult.class);
  }


  /**
   * Delete a token record from state permanently.
   *
   * @param userId the user
   * @param input  the token to delete
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> deleteToken(String userId, DeleteToken.Input input) {
    return producer.send(userId, DeleteToken.NAME, input, TransactionResult.class);
  }


  /**
   * Cancel a DVP contract.
   *
   * @param userId the user
   * @param input  details of the trade to cancel
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> dvpCancel(String userId, DvpCancel.Input input) {
    return producer.send(userId, DvpCancel.NAME, input, TransactionResult.class);
  }


  /**
   * Create a DVP trade as the controller of one of the tokens involved.
   *
   * @param userId the requesting user.
   * @param input  the input
   *
   * @return the transaction result.
   */
  public CompletableFuture<TransactionResult> dvpControllerCommit(String userId, DvpControllerCommit.Input input) {
    return producer.send(userId, DvpControllerCommit.NAME, input, TransactionResult.class);
  }


  /**
   * Create a DVP trade as the controller of one of the tokens involved.
   *
   * @param userId the requesting user.
   * @param input  the input
   *
   * @return the transaction result.
   */
  public CompletableFuture<TransactionResult> dvpControllerCreate(String userId, DvpControllerCreate.Input input) {
    return producer.send(userId, DvpControllerCreate.NAME, input, TransactionResult.class);
  }


  /**
   * Get all the tokens known to IOBC.
   *
   * @param userId the requesting user
   *
   * @return the details of all the tokens
   */
  public CompletableFuture<GetAllTokens.Output> getAllTokens(String userId) {
    return producer.send(userId, GetAllTokens.NAME, null, GetAllTokens.Output.class);
  }


  /**
   * Get an ERC-20 approved transfer allowance. Note: the allowance is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the available allowance
   */
  public CompletableFuture<GetAllowance.Output> getAllowance(String userId, GetAllowance.Input input) {
    return producer.send(userId, GetAllowance.NAME, input, GetAllowance.Output.class);
  }


  /**
   * Get an address's token balance. Note: the balance is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the result of the transaction
   */
  public CompletableFuture<GetBalance.Output> getBalance(String userId, GetBalance.Input input) {
    return producer.send(userId, GetBalance.NAME, input, GetBalance.Output.class);
  }


  /**
   * Get the details of a DVP trade.
   *
   * @param userId the requesting user
   * @param dvpId  the ID of the trade
   *
   * @return the trade details
   */
  public CompletableFuture<GetDvpTrade.Output> getDvpTrade(String userId, DvpId dvpId) {
    return producer.send(userId, GetDvpTrade.NAME, dvpId, GetDvpTrade.Output.class);
  }


  /**
   * Get all the holdings for a token.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public CompletableFuture<GetHoldings.Output> getHoldings(String userId, GetHoldings.Input input) {
    return producer.send(userId, GetHoldings.NAME, input, GetHoldings.Output.class);
  }


  /**
   * Get the number of token's the controller has locked on an address.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the number of tokens locked
   */
  public CompletableFuture<GetLocked.Output> getLockedAmount(String userId, GetLocked.Input input) {
    return producer.send(userId, GetLocked.NAME, input, GetLocked.Output.class);
  }


  /**
   * Get a token's name.
   *
   * @param userId the requesting user
   * @param input  the token's symbol
   *
   * @return the token's name
   */
  public CompletableFuture<GetName.Output> getName(String userId, TokenId input) {
    return producer.send(userId, GetName.NAME, input, GetName.Output.class);
  }


  /**
   * Get the total amount of tokens currently in existence. Note: the value is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the number of tokens existing
   */
  public CompletableFuture<GetTotalSupply.Output> getTotalSupply(String userId, TokenId input) {
    return producer.send(userId, GetTotalSupply.NAME, input, GetTotalSupply.Output.class);
  }


  /**
   * Lock a number of tokens to prevent them being transferred. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> lockTokens(String userId, LockToken.Input input) {
    return producer.send(userId, LockToken.NAME, input, TransactionResult.class);
  }


  /**
   * Mint an amount of tokens. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> mintTokens(String userId, MintToken.Input input) {
    return producer.send(userId, MintToken.NAME, input, TransactionResult.class);
  }


  /**
   * Terminate a token contract, burning all the associated tokens and preventing future transactions.
   *
   * @param userId the user
   * @param input  the token to terminate
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> terminate(String userId, TerminateToken.Input input) {
    return producer.send(userId, TerminateToken.NAME, input, TransactionResult.class);
  }


  /**
   * Transfer using a pre-approval (ERC-20).
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> transferFrom(String userId, TransferFrom.Input input) {
    return producer.send(userId, TransferFrom.NAME, input, TransactionResult.class);
  }


  /**
   * Transfer tokens to another address.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> transferTokens(String userId, TransferToken.Input input) {
    return producer.send(userId, TransferToken.NAME, input, TransactionResult.class);
  }


  /**
   * Unlock an amount of tokens so that they can be transferred. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the result of the transaction
   */
  public CompletableFuture<TransactionResult> unlockTokens(String userId, UnlockToken.Input input) {
    return producer.send(userId, UnlockToken.NAME, input, TransactionResult.class);
  }

}
