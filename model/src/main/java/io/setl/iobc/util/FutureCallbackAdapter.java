package io.setl.iobc.util;

import java.util.function.Consumer;

import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * An adapter that allows easy use of lambdas.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
public class FutureCallbackAdapter<V> implements ListenableFutureCallback<V> {

  private final Consumer<Throwable> failureHandler;

  private final Consumer<V> successHandler;


  public FutureCallbackAdapter(Consumer<V> successHandler, Consumer<Throwable> failureHandler) {
    this.failureHandler = failureHandler;
    this.successHandler = successHandler;
  }


  @Override
  public void onFailure(Throwable ex) {
    failureHandler.accept(ex);
  }


  @Override
  public void onSuccess(V result) {
    successHandler.accept(result);
  }

}
