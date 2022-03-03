package io.setl.iobc.model.tx;

import java.math.BigInteger;
import java.time.Instant;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;

/**
 * Get the block number associated with a given point in time.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface GetBlockForTime extends IobcDelegate<GetBlockForTime.Input, GetBlockForTime.Output> {

  String NAME = "UTILITY.GET_BLOCK_NUMBER_FOR_TIME";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  class Input implements MessageInput {

    /** The chain to look for the block on. */
    String chainId;

    /** The time to check as a date-time. */
    Instant dateTime;

    /** The time to check as an epoch-second value. */
    Long epochSecond;

    /** The token symbol. This is used to establish the earliest possible block co-incident with the token's creation. */
    @NotEmpty
    String symbol;


    /**
     * Get the effective epoch second.
     *
     * @return the epoch second
     */
    @JsonIgnore
    public long getEffectiveEpochSecond() {
      if (epochSecond != null) {
        return epochSecond;
      }
      return dateTime.getEpochSecond();
    }


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forPublic();
    }


    /**
     * Uses bean validation to ensure that either a date-time or an epoch second is specified, but not both.
     *
     * @return true if exactly one specification is given
     */
    @JsonIgnore
    @AssertTrue
    public boolean specifiesDateTimeXorEpochSecond() {
      return (dateTime != null) ^ (epochSecond != null);
    }

  }



  /**
   * Output from the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(name = "GetBlockForTime_Output", description = "Get the highest block number prior to a given time")
  class Output implements MessageContent {

    /** The amount held. */
    @Schema(description = "The block number", required = true)
    BigInteger blockNumber;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
