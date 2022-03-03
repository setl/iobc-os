package io.setl.iobc.model.tokens;

import javax.json.JsonObject;
import javax.json.JsonValue;
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
import io.setl.iobc.model.tokens.CreateToken.Input;

/**
 * Create a new token in the block-chain.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface CreateToken extends IobcDelegate<Input, TransactionResult> {

  String NAME = "TOKEN.CREATE";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements TransactionInput {

    /** The address that will control this token. */
    @NotEmpty
    String controller;

    /** The name of the token. */
    String name;

    /** Parameters to the token creation. */
    @Default
    @NotNull
    JsonObject parameters = JsonValue.EMPTY_JSON_OBJECT;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;

    /** The required processing mode for the transaction. */
    @NotNull
    @Default
    @With
    TransactionInput.TxProcessingMode txProcessingMode = TxProcessingMode.RETURN_RESULT;

    /** The type of the token to create. */
    @NotEmpty
    String type;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forAddress(controller);
    }

  }


  @Override
  default String getType() {
    return NAME;
  }

}
