package io.setl.iobc.model.tokens;

import java.util.Map;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.TokenSpecification;
import io.setl.iobc.model.tokens.GetAllTokens.Output;

/**
 * Delegate to handle requests to retrieve all known tokens.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public interface GetAllTokens extends IobcDelegate<MessageInput, Output> {

  String NAME = "TOKENS.GET_ALL";



  /**
   * Output from the request.
   */
  @Builder
  @Value
  @Jacksonized
  @Schema(name = "GetAllTokens_Output", description = "The result of a query of all known tokens")
  class Output implements MessageContent {

    @NotEmpty
    @Schema(description = "A map of token symbols to their internal specifications", required = true)
    Map<String, TokenSpecification> tokens;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
