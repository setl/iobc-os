package io.setl.iobc.model.tokens;

import java.math.BigInteger;
import java.util.List;
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
import io.setl.iobc.model.tokens.GetHoldings.Input;
import io.setl.iobc.model.tokens.GetHoldings.Output;

/**
 * Get the holdings of a token.
 *
 * @author Simon Greatrix on 28/11/2021.
 */
public interface GetHoldings extends IobcDelegate<Input, Output> {

  String NAME = "TOKEN.GET_HOLDINGS";



  /**
   * Tuple to specify a holding in the response.
   */
  @Builder
  @Value
  @Jacksonized
  class Holding {

    /** The address. */
    @NotEmpty
    String address;

    /** The holding. */
    @NotNull
    @Min(0)
    BigInteger amount;

  }



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

    /** The index of the last holding to return. */
    @Default
    @Min(0)
    int end = Integer.MAX_VALUE;

    /** The index of the first holding to return. */
    @Default
    @Min(0)
    int start = 0;

    /** The symbol and primary identifier of the token. */
    @NotEmpty
    String symbol;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forToken(symbol);
    }

  }



  /**
   * Output from the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "GetHoldings_Output",
      description = "The addresses holding a token"
  )
  class Output implements MessageContent {

    /** The address that owns the token. */
    @Schema(description = "The address that owns the token")
    @NotEmpty
    String controller;

    /** The index of the last holding returned. */
    @Schema(description = "The index of the last holding returned (exclusive)")
    @Min(0)
    int end;

    /** The address to check. */
    @Schema(description = "The holdings")
    @NotNull
    List<Holding> holdings;

    /** The total number of holdings. */
    @Min(0)
    @Schema(description = "The total number of addresses known to hold some quantity of the token.")
    int size;

    /** The index of the first holding returned. */
    @Schema(description = "The index of the first holding returned (inclusive)")
    @Min(0)
    int start;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
