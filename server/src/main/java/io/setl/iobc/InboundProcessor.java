package io.setl.iobc;

import java.security.InvalidKeyException;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import io.setl.common.Pair;
import io.setl.common.ParameterisedException;
import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.UnsupportedAlgorithmException;
import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.config.TopicConfiguration;
import io.setl.iobc.model.ContinuedMessage;
import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.IobcDelegate;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.model.Response;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.util.ExceptionTranslator;
import io.setl.iobc.valid.ObjectValidator;

/**
 * Process Kafka messages on the "inbound" topic.
 *
 * @author Simon Greatrix on 16/11/2021.
 */
@Slf4j
public class InboundProcessor implements Processor<String, AuthenticatedMessage> {

  /** Map of input message type to delegate. */
  private final DelegateMatcher delegates;

  private final KeyProvider keyProvider;

  private final KafkaTemplate<String, AuthenticatedMessage> template;

  private final ObjectValidator validator;


  InboundProcessor(
      DelegateMatcher delegates,
      KafkaTemplate<String, AuthenticatedMessage> template,
      ObjectValidator validator,
      KeyProvider keyProvider
  ) {
    this.template = template;
    this.validator = validator;
    this.delegates = delegates;
    this.keyProvider = keyProvider;
  }


  @Override
  public void close() {
    // nothing to do
  }


  private void handleFailure(InReplyTo inReplyTo, String type, Throwable throwable) {
    log.error("Inbound operation of {} for {} failed", type, inReplyTo, throwable);
    ParameterisedException parameterised = ExceptionTranslator.convert(throwable);
    ErrorDetails details = ErrorDetails.builder()
        .message(parameterised.getMessage())
        .code(parameterised.getTemplate())
        .parameters(parameterised.getParameters())
        .build();

    Response response = Response.builder().inReplyTo(inReplyTo).pass(false).errorDetails(details).build();
    AuthenticatedMessage message = new AuthenticatedMessage(Server.NAME, type, response);
    sign(message);
    template.executeInTransaction(o -> o.send(TopicConfiguration.OUTBOUND, message));
  }


  private boolean handleResult(InReplyTo inReplyTo, String type, MessageContent messageContent, Throwable throwable) {
    if (messageContent != null) {
      handleSuccess(inReplyTo, type, messageContent);

      if (messageContent instanceof ContinuedMessage) {
        CompletableFuture<ContinuedMessage> continuation = ((ContinuedMessage) messageContent).getContinuation();
        if (continuation != null) {
          continuation.handle((m, t) -> handleResult(inReplyTo, type, m, t));
        }
      }
      return true;
    }

    handleFailure(inReplyTo, type, throwable);
    return false;
  }


  private void handleSuccess(InReplyTo inReplyTo, String type, MessageContent messageContent) {
    log.info("Inbound message of {} for {} handled OK", type, inReplyTo);
    Response response = Response.builder().inReplyTo(inReplyTo).pass(true).result(messageContent).build();
    AuthenticatedMessage message = new AuthenticatedMessage("iobc-server", type, response);
    sign(message);

    template.executeInTransaction(o -> o.send(TopicConfiguration.OUTBOUND, message));
  }


  @Override
  public void init(ProcessorContext context) {
    log.info("Inbound processor is initialised");
  }


  @Override
  @Transactional
  public void process(String key, AuthenticatedMessage value) {
    String messageType = value.getType();
    log.info("Received inbound message of type {}", messageType);

    if (validateMessage(value)) {
      return;
    }

    MessageInput content = (MessageInput) value.getContent();

    CompletableFuture<MessageContent> result = delegates.invoke(messageType, content);
    result.handle((m, t) -> handleResult(value.buildInReplyTo(), messageType, m, t));
  }


  private void sign(AuthenticatedMessage message) {
    KeySpecification keySpecification = keyProvider.getSigningKey(Server.NAME);
    if (keySpecification == null) {
      throw new IllegalStateException("Server does not know its own signing secret");
    }
    try {
      message.sign(keySpecification);
    } catch (UnsupportedAlgorithmException | InvalidKeyException e) {
      log.error("Unable to sign outgoing messages", e);
    }

  }


  private boolean validateMessage(AuthenticatedMessage message) {
    ErrorDetails errorDetails = validator.validate(message);

    KeySpecification keySpecification = keyProvider.getVerifyingKey(message.getUserId());
    if (errorDetails == null && !message.isValidSignature(keySpecification)) {
      errorDetails = ErrorDetails.builder()
          .code("iobc:invalid-message-signature")
          .message("Invalid message signature")
          .build();
    }

    if (errorDetails == null) {
      return false;
    }

    Response response = Response.builder()
        .inReplyTo(message.buildInReplyTo())
        .pass(false)
        .errorDetails(errorDetails)
        .build();

    AuthenticatedMessage reply = new AuthenticatedMessage("iobc-server", message.getType(), response);
    sign(reply);
    template.executeInTransaction(o -> o.send(TopicConfiguration.OUTBOUND, reply));

    return true;
  }

}
