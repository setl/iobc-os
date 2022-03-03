package io.setl.iobc;

import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.valid.ObjectValidator;

/**
 * Supplier of new inbound processor instances.
 *
 * @author Simon Greatrix on 19/11/2021.
 */
@Component
public class InboundProcessorSupplier implements ProcessorSupplier<String, AuthenticatedMessage> {

  /** Map of input message type to delegate. */
  private final DelegateMatcher delegates;

  private final KeyProvider keyProvider;

  private final KafkaTemplate<String, AuthenticatedMessage> template;

  private final ObjectValidator validator;


  /** New instance. */
  @Autowired
  public InboundProcessorSupplier(
      DelegateMatcher delegates,
      @Qualifier("outboundTemplate") KafkaTemplate<String, AuthenticatedMessage> template,
      KeyProvider keyProvider,
      ObjectValidator validator
  ) {
    this.template = template;
    this.validator = validator;
    this.keyProvider = keyProvider;
    this.delegates = delegates;
  }


  @Override
  public InboundProcessor get() {
    return new InboundProcessor(delegates, template, validator, keyProvider);
  }

}
