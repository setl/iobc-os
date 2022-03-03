package io.setl.iobc.rest;

import javax.json.JsonObject;

import org.springframework.http.HttpStatus;

import io.setl.common.ParameterisedException;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Translate throwables to a ParameterisedWebExceptions.
 *
 * @author Simon Greatrix on 22/11/2021.
 */
public class WebExceptionTranslator {

  /**
   * Convert throwable to web exception.
   *
   * @param thrown the throwable
   *
   * @return the web exception
   */
  public static ParameterisedWebException convert(Throwable thrown) {
    if (thrown instanceof ParameterisedWebException) {
      return (ParameterisedWebException) thrown;
    }

    ParameterisedException parameterisedException = ExceptionTranslator.convert(thrown);
    JsonObject params = parameterisedException.getParameters();
    int code = params.getInt("httpStatus", 400);
    HttpStatus httpStatus = HttpStatus.resolve(code);
    if (httpStatus == null) {
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    return new ParameterisedWebException(
        httpStatus,
        parameterisedException.getTemplate(),
        parameterisedException.getParameters(),
        parameterisedException.getCause()
    );
  }

}
