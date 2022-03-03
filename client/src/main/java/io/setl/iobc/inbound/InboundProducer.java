package io.setl.iobc.inbound;

import java.security.InvalidKeyException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.UnsupportedAlgorithmException;
import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.config.TopicConfiguration;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.outbound.MessageCorrelator;
import io.setl.iobc.outbound.TypedFuture;

/**
 * Produce messages that are inbound to the IOBC server (not inbound to this client).
 *
 * @author Simon Greatrix on 17/11/2021.
 */
@Service
@Slf4j
public class InboundProducer {

  private final KeyProvider keyProvider;

  private final KafkaTemplate<String, AuthenticatedMessage> template;


  @Autowired
  public InboundProducer(
      @Qualifier("iobcInboundTemplate") KafkaTemplate<String, AuthenticatedMessage> kafkaTemplate,
      KeyProvider keyProvider
  ) {
    template = kafkaTemplate;
    this.keyProvider = keyProvider;
  }


  /**
   * Send a message to the IOBC server.
   *
   * @param userId    the user ID.
   * @param type      the message type
   * @param content   the message content
   * @param replyType the expected reply type
   * @param <T>       the expected reply type
   *
   * @return a future that will complete when a reply is received.
   */
  public <T> CompletableFuture<T> send(String userId, String type, MessageContent content, Class<T> replyType) {
    AuthenticatedMessage message = new AuthenticatedMessage(userId, type, content);
    InReplyTo inReplyTo = message.buildInReplyTo();
    sign(userId, message);
    TypedFuture<T> typedFuture = new TypedFuture<>(replyType);
    MessageCorrelator.register(inReplyTo, typedFuture);
    template.executeInTransaction(operations -> operations.send(TopicConfiguration.INBOUND, message));
    return typedFuture.getFuture();
  }


  /**
   * Send a message to the IOBC server.
   *
   * @param userId  the user ID.
   * @param type    the message type
   * @param content the message content
   *
   * @return the message ID.
   */
  public InReplyTo sendMessage(String userId, String type, MessageContent content) {
    AuthenticatedMessage message = new AuthenticatedMessage(userId, type, content);
    InReplyTo inReplyTo = message.buildInReplyTo();
    sign(userId, message);
    template.executeInTransaction(operations -> operations.send(TopicConfiguration.INBOUND, message));
    return inReplyTo;
  }


  protected void sign(String userId, AuthenticatedMessage message) {
    KeySpecification keySpecification = keyProvider.getSigningKey(userId);
    Objects.requireNonNull(keySpecification, "No signing key available for " + userId);
    try {
      message.sign(keySpecification);
    } catch (UnsupportedAlgorithmException | InvalidKeyException e) {
      throw new IllegalArgumentException("Invalid key specification: " + keySpecification);
    }
  }

}
