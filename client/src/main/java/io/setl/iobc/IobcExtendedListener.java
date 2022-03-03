package io.setl.iobc;

import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.Response;
import io.setl.iobc.model.Response.InReplyTo;

/**
 * An extended listener which splits processing between success and failure paths.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface IobcExtendedListener extends IobcListener {

  /**
   * Direct the message down one of three paths: success, failure, or ignored.
   *
   * @param message the message
   */
  default void accept(AuthenticatedMessage message) {
    String type = message.getType();
    MessageContent content = message.getContent();

    // Content should always be a response instance, but we verify that.
    if (content instanceof Response) {
      if (shouldIgnore(message)) {
        return;
      }
      Response response = (Response) content;
      InReplyTo inReplyTo = response.getInReplyTo();
      if (response.isPass()) {
        acceptSuccess(inReplyTo, type, response.getResult());
      } else {
        acceptFailure(inReplyTo, type, response.getErrorDetails());
      }
    }
  }


  void acceptFailure(InReplyTo inReplyTo, String type, ErrorDetails errorDetails);


  void acceptSuccess(InReplyTo inReplyTo, String type, MessageContent content);


  /**
   * Should the message be ignored?.
   *
   * @param message the message, with a <code>Response</code> content
   *
   * @return true if it should be ignored
   */
  default boolean shouldIgnore(AuthenticatedMessage message) {
    return false;
  }

}
