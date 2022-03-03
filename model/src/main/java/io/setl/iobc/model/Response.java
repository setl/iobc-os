package io.setl.iobc.model;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessageContent;

/**
 * A pass or fail response.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
@Value
@Builder
@Jacksonized
@EqualsAndHashCode
public class Response implements MessageContent {

  private interface ResultTypeSpecifier {

    /** Ensure that type information is passed on the result property. */
    @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
    ResponseBuilder result(MessageContent content);

  }



  /**
   * Record of what this response is in reply to.
   */
  @Value
  @Builder
  @Jacksonized
  @EqualsAndHashCode
  public static class InReplyTo {

    String messageId;

    String userId;


    @Override
    public String toString() {
      return "< " + userId + " | " + messageId + " >";
    }

  }



  /**
   * Builder for Response instances.
   */
  public static class ResponseBuilder implements ResultTypeSpecifier {
    // Lombok adds required code
  }



  /** The error details if the operation was not a success. */
  @JsonInclude(Include.NON_NULL)
  ErrorDetails errorDetails;

  /** The message ID of the message this is in reply to. */
  @NotEmpty
  InReplyTo inReplyTo;

  /** If true the operation was successful. */
  boolean pass;

  /** The operation result if it was a success. */
  @JsonInclude(Include.NON_NULL)
  @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
  MessageContent result;

}
