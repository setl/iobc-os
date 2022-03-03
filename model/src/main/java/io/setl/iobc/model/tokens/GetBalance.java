package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipal;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.tokens.GetBalance.Input;
import io.setl.iobc.model.tokens.GetBalance.Output;

/**
 * Get a token balance.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface GetBalance extends IobcDelegate<Input, Output> {

  String NAME = "TOKEN.GET_BALANCE";



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

    /** The block number to query. */
    @Min(-1)
    @Default
    long block = -1;

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
  @Schema(name = "GetBalance_Output", description = "The result of a balance query")
  class Output implements MessageContent {

    /** The amount held. */
    @Schema(description = "The number of tokens held", required = true)
    BigInteger amount;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
