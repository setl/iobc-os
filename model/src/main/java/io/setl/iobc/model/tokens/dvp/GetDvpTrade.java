package io.setl.iobc.model.tokens.dvp;

import java.math.BigInteger;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.IobcDelegate;

/**
 * Get the details of a DVP trade.
 *
 * @author Simon Greatrix on 30/11/2021.
 */
public interface GetDvpTrade extends IobcDelegate<DvpId, GetDvpTrade.Output> {

  String NAME = "TOKENS.DVP.GET_TRADE";



  /** Input to the trade creation. */
  @Builder
  @Value
  @Jacksonized
  class Output implements MessageContent {

    /** The trade's ID. */
    String dvpId;

    /** Does the trade exist?. */
    boolean exists;

    /** First party to the trade. */
    @NotNull @Valid
    PartyDetails party1;

    /** Second party to the trade. */
    @NotNull @Valid
    PartyDetails party2;

  }



  /** Details of a party to a DVP contract. */
  @Builder
  @Value
  @Jacksonized
  class PartyDetails {

    /** The address of the party. */
    @NotEmpty
    String address;

    /** The amount of the token to trade. */
    @NotNull
    @Min(0)
    BigInteger amount;

    /** Has this party committed to the trade?. */
    boolean committed;

    /** The token to be supplied by this party. */
    @NotEmpty
    String symbol;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
