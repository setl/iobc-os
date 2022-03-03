package io.setl.iobc.model;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.setl.iobc.authenticate.MessageContent;

/**
 * Something that can be continued in the future.
 *
 * @author Simon Greatrix on 25/11/2021.
 */
public interface ContinuedMessage<T extends ContinuedMessage<T>> extends MessageContent {

  /**
   * Get the continuation, if any.
   *
   * @return the continuation, or null
   */
  @JsonIgnore
  CompletableFuture<T> getContinuation();

}
