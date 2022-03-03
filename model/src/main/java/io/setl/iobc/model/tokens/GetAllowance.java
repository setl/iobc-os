package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipal;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.tokens.GetAllowance.Input;
import io.setl.iobc.model.tokens.GetAllowance.Output;

/**
 * Get the "allowance" an address has for a token on behalf of another address. An allowance is like a SETL encumbrance, or an ERC-20 allowance.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface GetAllowance extends IobcDelegate<Input, Output> {

  String NAME = "TOKEN.GET_ALLOWANCE";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    /** The address that owns the allowed token amount. */
    @NotEmpty
    String owner;

    /** The address that can spend the allowed token amount. */
    @NotEmpty
    String spender;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return new MessagePrincipals(
          MessagePrincipal.forAddress(owner),
          MessagePrincipal.forAddress(spender),
          MessagePrincipal.forToken(symbol)
      );
    }

  }



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "GetAllowance_Output",
      description = "The result of a query of a transfer allowance"
  )
  class Output implements MessageContent {

    /** The amount locked. */
    @Schema(description = "The amount that can currently be transferred by the spender on behalf of the owner", required = true)
    BigInteger amount;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
