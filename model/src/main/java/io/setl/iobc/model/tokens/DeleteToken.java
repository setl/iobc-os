package io.setl.iobc.model.tokens;

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

/**
 * Terminate a token contract, preventing all future trades on it.
 *
 * @author Simon Greatrix on 07/12/2021.
 */
public interface DeleteToken extends IobcDelegate<DeleteToken.Input, TransactionResult> {

  String NAME = "TOKEN.TERMINATE";



  /**
   * Specify just a token symbol as the input to an operation.
   *
   * @author Simon Greatrix on 24/11/2021.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

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
      return MessagePrincipals.forToken(symbol);
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
