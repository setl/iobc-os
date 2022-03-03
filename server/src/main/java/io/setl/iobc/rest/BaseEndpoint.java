package io.setl.iobc.rest;

import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import io.setl.common.StringUtils;
import io.setl.iobc.DelegateMatcher;
import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.model.MessageInput;
import io.setl.iobc.util.SerdeSupport;
import io.setl.pychain.rest.ParameterisedWebException;

/**
 * Base endpoint for classes.
 *
 * @author Simon Greatrix on 27/01/2022.
 */
@Slf4j
public abstract class BaseEndpoint {

  protected DelegateMatcher delegateMatcher;


  @SuppressWarnings("unchecked")
  protected <T extends MessageContent> T handle(String operation, MessageInput input) throws ParameterisedWebException, InterruptedException {
    try {
      return (T) delegateMatcher.invoke(operation, input).get();
    } catch (ExecutionException e) {
      log.error("Error during execution of delegate for {} with input:{}", operation, StringUtils.logSafe(SerdeSupport.getPrettyJson(input)), e);
      Throwable throwable = e.getCause();
      throw WebExceptionTranslator.convert(throwable != null ? throwable : e);
    }
  }


  @Autowired
  public final void setDelegateMatcher(DelegateMatcher delegateMatcher) {
    this.delegateMatcher = delegateMatcher;
  }

}
