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
import io.setl.iobc.model.tokens.UnlockToken.Input;

/**
 * An asset controller can lock an amount of an asset held by another party.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface UnlockToken extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.UNLOCK";



  /** Input to the operation specifying how many tokens to unlock. */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    @NotEmpty
    String address;

    @Min(0)
    BigInteger amount;

    @NotEmpty
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;


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
