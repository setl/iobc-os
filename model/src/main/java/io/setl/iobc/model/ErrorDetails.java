package io.setl.iobc.model;

import javax.json.JsonObject;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Error details for an unsuccessful processing attempt.
 *
 * @author Simon Greatrix on 16/11/2021.
 */
@Data
@Builder
@Jacksonized
public class ErrorDetails {

  /** An error code that indicates the kind of failure. Null if the operation was a success. */
  private final String code;

  /** A human-readable error message. Null if the operation was a success. */
  private String message;

  /** Parameters associated with the error. Null if the operation was a success. */
  private JsonObject parameters;

}
