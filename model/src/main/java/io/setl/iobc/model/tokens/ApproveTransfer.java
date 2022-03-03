package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.TransactionInput;
import io.setl.iobc.model.TransactionResult;
import io.setl.iobc.model.tokens.ApproveTransfer.Input;

/**
 * Create a SETL style encumbrance, or an ERC-20 approval. Both of these allow another party to transfer some of the owner's tokens, but does not guarantee that
 * the tokens exist.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface ApproveTransfer extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.APPROVE";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    @Min(0)
    @Nonnull
    BigInteger amount;

    @Min(0)
    BigInteger expected;

    /** The address that owns the allowed token amount. */
    @NotEmpty
    String owner;

    /** The address that can spend the allowed token amount. */
    @NotEmpty
    String spender;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forAddress(owner);
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
