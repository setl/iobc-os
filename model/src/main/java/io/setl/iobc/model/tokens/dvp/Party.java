package io.setl.iobc.model.tokens.dvp;

import java.math.BigInteger;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * One of the parties to a DVP trade.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
@Builder
@Value
@Jacksonized
public class Party {

  /** The address of the party. */
  @NotEmpty
  String address;

  /** The amount of the token to trade. */
  @NotNull
  @Min(0)
  BigInteger amount;

  /** The token to be supplied by this party. */
  @NotEmpty
  String symbol;

}
