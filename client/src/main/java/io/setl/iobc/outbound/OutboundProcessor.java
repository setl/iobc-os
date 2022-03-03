package io.setl.iobc.outbound;

import java.util.concurrent.atomic.AtomicInteger;
import javax.json.JsonValue;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.transaction.annotation.Transactional;

import io.setl.http.signatures.KeySpecification;
import io.setl.iobc.IobcListeners;
import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.model.ErrorDetails;
import io.setl.iobc.model.Response;

/**
 * Process messages received on the IOBC "Outbound" topic, which is outbound from the IOBC server and inbound to this.
 *
 * @author Simon Greatrix on 16/11/2021.
 */
@Slf4j
public class OutboundProcessor implements Processor<String, AuthenticatedMessage> {

  private static final AtomicInteger inputCount = new AtomicInteger();

  private final KeyProvider keyProvider;

  private final IobcListeners listeners;


  public OutboundProcessor(
      IobcListeners listeners,
      KeyProvider keyProvider
  ) {
    this.listeners = listeners;
    this.keyProvider = keyProvider;
  }


  @Override
  public void close() {
    // nothing to do
  }


  @Override
  public void init(ProcessorContext context) {
    // nothing to do
  }


  protected boolean isSignatureInvalid(AuthenticatedMessage message) {
    KeySpecification keySpecification = keyProvider.getVerifyingKey(message.getUserId());
    if (keySpecification == null) {
      log.error("No verification key available for user {}", message.getUserId());
      return true;
    }
    return !message.isValidSignature(keySpecification);
  }


  @Override
  @Transactional
  public void process(String key, AuthenticatedMessage message) {
    log.debug("Received message from server: {}", message);
    inputCount.incrementAndGet();
    if (isSignatureInvalid(message)) {
      log.error("Received message does not contain a valid signature: {}", message);
      return;
    }

    if (!(message.getContent() instanceof Response)) {
      log.error("Received message does not contain a response: {}", message);
      return;
    }

    // notify listeners
    listeners.notifyListeners(message);

    // Messages on the outbound channel always have "Response" as their content.
    Response response = (Response) message.getContent();
    if (response.isPass()) {
      // Correctly handled, but that does not mean (for example) that a transaction succeeded.
      MessageCorrelator.notify(response.getInReplyTo(), response.getResult());
      return;
    }

    // The operation was not successfully handled.
    ErrorDetails errorDetails = response.getErrorDetails();
    if (errorDetails == null) {
      errorDetails = ErrorDetails.builder().message("N/A").code("iobc/unknown").parameters(JsonValue.EMPTY_JSON_OBJECT).build();
    }
    MessageCorrelator.notify(response.getInReplyTo(), errorDetails);
  }

}
