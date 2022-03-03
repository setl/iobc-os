package io.setl.iobc.rest.intercept;

import javax.annotation.Nullable;

import io.setl.http.signatures.HttpSignature;
import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.AuthenticationException;
import io.setl.http.signatures.error.UnknownKeyIdException;
import io.setl.http.signatures.server.SignatureAuthentication;
import io.setl.http.signatures.server.SignatureFilterBase;
import io.setl.http.signatures.server.SimpleSignatureAuthentication;
import io.setl.iobc.Server;
import io.setl.iobc.authenticate.KeyProvider;


/**
 * Checks the header in the HTTP request, verifies the HTTP signature.
 */
public class SignatureFilter extends SignatureFilterBase {

  /** The signing key for server-to-server replies. */
  private final KeyProvider keyProvider;


  /**
   * Create a new filter.
   */
  public SignatureFilter(
      KeyProvider keyProvider
  ) {
    HttpSignature.setMaxSkewSeconds(10);
    this.keyProvider = keyProvider;
  }


  @Nullable
  @Override
  protected SignatureAuthentication getAuthentication(String keyId) throws AuthenticationException {
    KeySpecification signingKey = keyProvider.getSigningKey(Server.NAME);
    KeySpecification verifyKey = keyProvider.getSigningKey(keyId);
    if (verifyKey != null) {
      return new SimpleSignatureAuthentication(verifyKey, signingKey, () -> new UserAuthentication(keyId));
    }

    throw new UnknownKeyIdException("Unrecognised key ID: \"" + keyId + "\"");
  }


}
