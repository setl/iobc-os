package io.setl.iobc.model.tx;

import java.math.BigInteger;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;

/**
 * Utility delegate to get the latest block number.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface GetBlockNumber extends IobcDelegate<MessageInput, GetBlockNumber.Output> {

  String NAME = "UTILITY.GET_BLOCK_NUMBER";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(name = "GetBlockNumber_Output", description = "Get the current block number")
  class Output implements MessageContent {

    /** The amount held. */
    @Schema(description = "The current block number", required = true)
    @NotNull
    @Min(0)
    BigInteger blockNumber;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
