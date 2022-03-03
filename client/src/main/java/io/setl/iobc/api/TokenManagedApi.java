package io.setl.iobc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.setl.iobc.inbound.InboundProducer;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.TransactionInput;
import io.setl.iobc.model.TransactionInput.TxProcessingMode;
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

/**
 * Entry points for the Token Api.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class TokenManagedApi {

  private final InboundProducer producer;

  private TxProcessingMode processingMode = TxProcessingMode.RETURN_ID;


  @Autowired
  public TokenManagedApi(InboundProducer producer) {
    this.producer = producer;
  }


  /**
   * Perform an ERC-20 style transfer approval.
   *
   * @param userId the requesting user
   * @param input  the details of the approval
   *
   * @return the message ID
   */
  public InReplyTo approveTransfer(String userId, ApproveTransfer.Input input) {
    return producer.sendMessage(userId, ApproveTransfer.NAME, prepare(input));
  }


  /**
   * Burn an amount of tokens.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo burnToken(String userId, BurnToken.Input input) {
    return producer.sendMessage(userId, BurnToken.NAME, prepare(input));
  }


  /**
   * Perform a controller transfer of assets.
   *
   * @param userId the requesting user
   * @param input  the details of the transfer
   *
   * @return the message ID
   */
  public InReplyTo controllerTransfer(String userId, ControllerTransfer.Input input) {
    return producer.sendMessage(userId, ControllerTransfer.NAME, prepare(input));
  }


  /**
   * Create a new token.
   *
   * @param userId the requesting user
   * @param input  the details of the new token
   *
   * @return the message ID
   */
  public InReplyTo createToken(String userId, CreateToken.Input input) {
    return producer.sendMessage(userId, CreateToken.NAME, prepare(input));
  }


  /**
   * Delete a token record from state permanently.
   *
   * @param userId the user
   * @param input  the token to delete
   *
   * @return the result of the transaction
   */
  public InReplyTo deleteToken(String userId, DeleteToken.Input input) {
    return producer.sendMessage(userId, DeleteToken.NAME, prepare(input));
  }


  /**
   * Cancel a DVP contract.
   *
   * @param userId the user
   * @param input  details of the trade to cancel
   *
   * @return the result of the transaction
   */
  public InReplyTo dvpCancel(String userId, DvpCancel.Input input) {
    return producer.sendMessage(userId, DvpCancel.NAME, prepare(input));
  }


  /**
   * Create a DVP trade as the controller of one of the tokens involved.
   *
   * @param userId the requesting user.
   * @param input  the input
   *
   * @return the transaction result.
   */
  public InReplyTo dvpControllerCommit(String userId, DvpControllerCommit.Input input) {
    return producer.sendMessage(userId, DvpControllerCommit.NAME, prepare(input));
  }


  /**
   * Create a DVP trade as the controller of one of the tokens involved.
   *
   * @param userId the requesting user.
   * @param input  the input
   *
   * @return the transaction result.
   */
  public InReplyTo dvpControllerCreate(String userId, DvpControllerCreate.Input input) {
    return producer.sendMessage(userId, DvpControllerCreate.NAME, prepare(input));
  }


  /**
   * Get all the tokens known to IOBC.
   *
   * @param userId the requesting user
   *
   * @return the message ID
   */
  public InReplyTo getAllTokens(String userId) {
    return producer.sendMessage(userId, GetAllTokens.NAME, null);
  }


  /**
   * Get an ERC-20 approved transfer allowance. Note: the allowance is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getAllowance(String userId, GetAllowance.Input input) {
    return producer.sendMessage(userId, GetAllowance.NAME, input);
  }


  /**
   * Get an address's token balance. Note: the balance is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getBalance(String userId, GetBalance.Input input) {
    return producer.sendMessage(userId, GetBalance.NAME, input);
  }


  /**
   * Get all the holdings for a token.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getHoldings(String userId, GetHoldings.Input input) {
    return producer.sendMessage(userId, GetHoldings.NAME, input);
  }


  /**
   * Get the number of token's the controller has locked on an address.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getLockedAmount(String userId, GetLocked.Input input) {
    return producer.sendMessage(userId, GetLocked.NAME, input);
  }


  /**
   * Get a token's name.
   *
   * @param userId the requesting user
   * @param input  the token's symbol
   *
   * @return the message ID
   */
  public InReplyTo getName(String userId, TokenId input) {
    return producer.sendMessage(userId, GetName.NAME, input);
  }


  public TxProcessingMode getProcessingMode() {
    return processingMode;
  }


  /**
   * Get the total amount of tokens currently in existence. Note: the value is correct at the time it was retrieved, but that was in the past.
   *
   * @param userId the requesting user
   * @param input  the details of the request
   *
   * @return the message ID
   */
  public InReplyTo getTotalSupply(String userId, TokenId input) {
    return producer.sendMessage(userId, GetTotalSupply.NAME, input);
  }


  /**
   * Lock a number of tokens to prevent them being transferred. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo lockTokens(String userId, LockToken.Input input) {
    return producer.sendMessage(userId, LockToken.NAME, prepare(input));
  }


  /**
   * Mint an amount of tokens. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo mintTokens(String userId, MintToken.Input input) {
    return producer.sendMessage(userId, MintToken.NAME, prepare(input));
  }


  @SuppressWarnings("unchecked")
  protected <T extends TransactionInput> T prepare(T input) {
    return (T) input.withTxProcessingMode(processingMode);
  }


  public void setProcessingMode(TxProcessingMode processingMode) {
    this.processingMode = processingMode;
  }


  /**
   * Terminate a token contract, burning all the associated tokens and preventing future transactions.
   *
   * @param userId the user
   * @param input  the token to terminate
   *
   * @return the result of the transaction
   */
  public InReplyTo terminate(String userId, TerminateToken.Input input) {
    return producer.sendMessage(userId, TerminateToken.NAME, prepare(input));
  }


  /**
   * Transfer using a pre-approval (ERC-20).
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo transferFrom(String userId, TransferFrom.Input input) {
    return producer.sendMessage(userId, TransferFrom.NAME, prepare(input));
  }


  /**
   * Transfer tokens to another address.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo transferTokens(String userId, TransferToken.Input input) {
    return producer.sendMessage(userId, TransferToken.NAME, prepare(input));
  }


  /**
   * Unlock an amount of tokens so that they can be transferred. Note: only the token owner can do this.
   *
   * @param userId the requesting user
   * @param input  the details of the burn
   *
   * @return the message ID
   */
  public InReplyTo unlockTokens(String userId, UnlockToken.Input input) {
    return producer.sendMessage(userId, UnlockToken.NAME, prepare(input));
  }

}
