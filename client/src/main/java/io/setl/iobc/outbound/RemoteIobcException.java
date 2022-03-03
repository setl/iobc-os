package io.setl.iobc.outbound;

import io.setl.iobc.model.ErrorDetails;

/**
 * An exception to throw when an IOBC request returns a failure.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public class RemoteIobcException extends Exception {

  private static String getMessage(ErrorDetails details) {
    String e = details.getMessage();
    return (e != null) ? e : details.getCode();
  }


  private final ErrorDetails errorDetails;


  public RemoteIobcException(ErrorDetails errorDetails) {
    super(getMessage(errorDetails));
    this.errorDetails = errorDetails;
  }


  public ErrorDetails getErrorDetails() {
    return errorDetails;
  }

}
