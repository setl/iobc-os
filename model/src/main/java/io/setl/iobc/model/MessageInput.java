package io.setl.iobc.model;

import io.setl.iobc.authenticate.MessageContent;
import io.setl.iobc.authenticate.MessagePrincipals;

/**
 * Standard methods for input to a delegate.
 *
 * @author Simon Greatrix on 27/01/2022.
 */
public interface MessageInput extends MessageContent {

  MessagePrincipals resolvePrincipal();

}
