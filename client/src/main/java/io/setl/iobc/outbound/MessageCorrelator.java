package io.setl.iobc.outbound;

import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.Response.InReplyTo;

/**
 * A correlator of inbound and outbound messages.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Slf4j
public class MessageCorrelator {

  private static final ConcurrentHashMap<InReplyTo, TypedFuture<?>> callbacks = new ConcurrentHashMap<>();


  /**
   * Notify listeners of a successful handling.
   *
   * @param inReplyTo the message this is in reply to
   * @param result    the result of the handling
   */
  public static void notify(InReplyTo inReplyTo, Object result) {
    TypedFuture<?> future = callbacks.remove(inReplyTo);
    if (future != null) {
      future.set(result);
      return;
    }

    // We see everything on the Kafka topic, so we see replies to other systems to that have nothing to do with us.
    log.trace("Received irrelevant message in reply to: {}", inReplyTo);
  }


  /**
   * Notify listeners that the message was not handled successfully.
   *
   * @param inReplyTo    the message this is in reply to
   * @param errorDetails the details of the error.
   */
  public static void notify(InReplyTo inReplyTo, ErrorDetails errorDetails) {
    TypedFuture<?> future = callbacks.remove(inReplyTo);
    if (future != null) {
      future.fail(new RemoteIobcException(errorDetails));
      return;
    }

    // We see everything on the Kafka topic, so we see replies to other systems to that have nothing to do with us.
    log.debug("An irrelevant message {} failed: {}", inReplyTo, errorDetails);
  }


  /**
   * Notify listeners that some sort of exception was encountered in processing the reply.
   *
   * @param inReplyTo the message it was in-reply-to
   * @param thrown    the exception thrown
   */
  public static void notify(InReplyTo inReplyTo, Throwable thrown) {
    TypedFuture<?> future = callbacks.remove(inReplyTo);
    if (future != null) {
      future.fail(thrown);
      return;
    }

    // If it is not our message, where did the exception come from?
    log.warn("An irrelevant message failed: {}", inReplyTo, thrown);
  }


  /**
   * Register a future to receive a reply. Futures should be registered <strong>before</strong> the message is sent.
   *
   * @param inReplyTo the expected in-reply-to
   * @param future    the future
   */
  public static void register(InReplyTo inReplyTo, TypedFuture<?> future) {
    callbacks.put(inReplyTo, future);
  }

}
