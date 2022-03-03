package io.setl.iobc.model;

import java.util.concurrent.CompletableFuture;

import io.setl.common.ParameterisedException;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.config.ChainConfiguration;

/**
 * A simple specification of a delegate for a single operation.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
@SuppressWarnings("java:S119") // Allow properly named type parameters
public interface IobcDelegate<InputType extends MessageInput, OutputType extends MessageContent> {

  CompletableFuture<OutputType> apply(ChainConfiguration chain, InputType input) throws ParameterisedException;


  /**
   * Get the chain brand supported by this delegate.
   *
   * @return the supported brand.
   */
  ChainBrand getBrandSupported();


  /**
   * The type name on the input message.
   *
   * @return the type name
   */
  String getType();

}
