package io.setl.iobc;

import io.setl.iobc.authenticate.AuthenticatedMessage;

/**
 * A listener for IOBC responses.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
public interface IobcListener {

  void accept(AuthenticatedMessage message);

}
