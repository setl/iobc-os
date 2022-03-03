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
import io.setl.iobc.model.tokens.GetLocked.Input;
import io.setl.iobc.model.tokens.GetLocked.Output;

/**
 * Get a token balance.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface GetLocked extends IobcDelegate<Input, Output> {

  String NAME = "TOKEN.GET_LOCKED";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    /** The address to check. */
    @NotEmpty
    String address;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return new MessagePrincipals(
          MessagePrincipal.forAddress(address),
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
  @Schema(name = "GetLocked_Output", description = "The number of locked tokens held by the address")
  class Output implements MessageContent {

    /** The amount locked. */
    @Schema(description = "The number of locked tokens", required = true)
    BigInteger amount;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
