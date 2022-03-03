package io.setl.iobc.model.tx;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.TokenId;
import io.setl.iobc.model.TransactionResult;

/**
 * Verify the creation status of a token.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface VerifyCreateToken extends IobcDelegate<TokenId, VerifyCreateToken.Output> {

  String NAME = "TX.GET_CREATE_TOKEN_RESULT";



  /** The current token status. */
  enum TokenStatus {
    /** The token exists and is ready for use. */
    SUCCESS,

    /** Creation of the token failed. */
    FAILED,

    /** The transaction that creates the token has not yet completed. */
    IN_PROGRESS,

    /** The token is not known to IOBC. */
    UNKNOWN,

    /** The token status could not be checked. The transaction result may have more information. */
    ERROR
  }



  /** Output from this delegate. */
  @Builder
  @Value
  @Jacksonized
  @Schema(
      name = "VerifyCreateToken_Output",
      description = "The current status of the token creation process for a token"
  )
  class Output implements MessageContent {

    /** The result of the transaction that created the token, if known. */
    @Schema(description = "The result of the transaction that created the token, if known")
    TransactionResult result;

    /** The status of the token. */
    @Schema(description = "The status of the token", required = true)
    @NotNull TokenStatus status;

    /** The token symbol that was checked. */
    @Schema(description = "The symbol that identifies the token", required = true)
    @NotEmpty String symbol;

  }


  @Override
  default String getType() {
    return NAME;
  }

}
