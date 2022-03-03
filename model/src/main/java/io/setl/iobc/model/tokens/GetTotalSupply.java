package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;

/**
 * Get the total supply for a token.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface GetTotalSupply extends IobcDelegate<GetTotalSupply.Input, GetTotalSupply.Output> {

  String NAME = "TOKEN.TOTAL_SUPPLY";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    /** The block number to query. */
    @Min(-1)
    @Default
    long block = -1;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forToken(symbol);
    }

  }



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "GetTotalSupply_Output",
      description = "The total number of tokens in existence"
  )
  class Output implements MessageContent {

    /** The total supply. */
    @Schema(description = "The total number of tokens in existence", required = true)
    @Min(0)
    @NotNull
    BigInteger amount;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
