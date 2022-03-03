package io.setl.iobc.model.tokens;

import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.tokens.GetName.Output;

/**
 * Get a token balance.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
public interface GetName extends IobcDelegate<TokenId, Output> {

  String NAME = "TOKEN.GET_NAME";



  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(description = "The name of a token")
  class Output implements MessageContent {

    /** The token's name. */
    @NotEmpty
    @Schema(description = "The name of a token")
    String name;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
