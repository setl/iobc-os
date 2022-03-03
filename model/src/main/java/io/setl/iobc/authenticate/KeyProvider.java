package io.setl.iobc.authenticate;

import io.setl.http.signatures.KeySpecification;

/**
 * A mechanism to provide key specifications for signing and verifying messages.
 *
 * @author Simon Greatrix on 02/12/2021.
 */
public interface KeyProvider {

  KeySpecification getSigningKey(String userId);


  KeySpecification getVerifyingKey(String userId);

}
