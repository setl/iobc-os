package io.setl.iobc.model.tokens;

import java.math.BigInteger;
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
import io.setl.iobc.model.tokens.BurnToken.Input;

/**
 * Destroy an amount of tokens in the block-chain. Must be invoked by the token owner.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface BurnToken extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.BURN";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The amount of tokens to destroy. */
    @NotNull
    @Min(0)
    BigInteger amount;

    /** The address that loses the tokens. */
    @NotEmpty
    String from;

    /** If specified and true, burn from locked tokens. */
    Boolean fromLocked;

    /** The symbol of the token to destroy. */
    @NotEmpty
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forToken(symbol);
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
