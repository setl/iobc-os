package io.setl.iobc.outbound;

import java.util.concurrent.CompletableFuture;

/**
 * A type safe future.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
public class TypedFuture<T> {

  /** The future. */
  private final CompletableFuture<T> future = new CompletableFuture<>();

  /** The required type. */
  private final Class<T> type;


  /**
   * New instance.
   *
   * @param type the type
   */
  public TypedFuture(Class<T> type) {
    this.type = type;
  }


  /**
   * Fail with an exception.
   *
   * @param thrown the exception
   */
  public void fail(Throwable thrown) {
    future.completeExceptionally(thrown);
  }


  public CompletableFuture<T> getFuture() {
    return future;
  }


  /**
   * Succeed with a value.
   *
   * @param result the result
   */
  public void set(Object result) {
    if (result == null) {
      if (Void.class.equals(type)) {
        future.complete(null);
      } else {
        future.completeExceptionally(new NullPointerException("Result was null"));
      }
      return;
    }
    try {
      future.complete(type.cast(result));
    } catch (ClassCastException e) {
      future.completeExceptionally(e);
    }
  }

}
