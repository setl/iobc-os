package io.setl.iobc.model;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipals;
import io.setl.iobc.config.ChainConfiguration;

/**
 * Ping handler for simple connectivity tests.
 *
 * @author Simon Greatrix on 16/11/2021.
 */
@Service
public class PingDelegate implements IobcDelegate<PingDelegate.Input, PingDelegate.Output> {

  /**
   * Input to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  public static class Input implements MessageInput {

    /** The message to echo back. */
    String message;


    @Override
    public MessagePrincipals resolvePrincipal() {
      return MessagePrincipals.forPublic();
    }

  }



  /**
   * Output to the operation.
   */
  @Builder
  @Value
  @Jacksonized
  public static class Output implements MessageContent {

    /** The message copied from the input. */
    String message;

    /** The time the ping was processed. */
    Instant time;

  }


  @Override
  public CompletableFuture<Output> apply(ChainConfiguration configuration, Input input) {
    return CompletableFuture.completedFuture(Output.builder().message(input.message).time(Instant.now()).build());
  }


  @Override
  public ChainBrand getBrandSupported() {
    return ChainBrand.NONE;
  }


  @Override
  public String getType() {
    return "PING";
  }

}
