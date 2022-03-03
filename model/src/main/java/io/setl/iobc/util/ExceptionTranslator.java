package io.setl.iobc.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import io.setl.common.ParameterisedException;
import io.setl.json.CJObject;

/**
 * Convert exceptions to ParameterisedExceptions.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public class ExceptionTranslator {


  /**
   * Convert the throwable to a ParameterisedException.
   *
   * @param thrown the throwable
   *
   * @return the equivalent ParameterisedException
   */
  public static ParameterisedException convert(Throwable thrown) {
    if (thrown instanceof ParameterisedException) {
      return (ParameterisedException) thrown;
    }

    String stackTrace;
    try (
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter)
    ) {
      thrown.printStackTrace(printWriter);
      printWriter.flush();
      stringWriter.flush();
      stackTrace = stringWriter.toString();
    } catch (IOException ioException) {
      // should not happen as no I/O
      throw new UncheckedIOException(ioException);
    }

    CJObject cjObject = new CJObject();
    cjObject.put("class", thrown.getClass().getName());
    cjObject.put("message", thrown.getMessage());
    cjObject.put("description", thrown.toString());
    cjObject.put("stackTrace", stackTrace);

    return new ParameterisedException(thrown.toString(), "iobc/internal_error", cjObject, thrown);
  }

}
