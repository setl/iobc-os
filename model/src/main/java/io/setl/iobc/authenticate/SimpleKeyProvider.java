package io.setl.iobc.authenticate;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import io.setl.http.signatures.KeySpecification;
import io.setl.util.CopyOnWriteMap;

/**
 * Simple provider which stores the keys in a map and must be explicitly loaded.
 *
 * @author Simon Greatrix on 02/12/2021.
 */
public class SimpleKeyProvider implements KeyProvider {

  private final CopyOnWriteMap<String, KeySpecification> signingKeys = new CopyOnWriteMap<>();

  private final CopyOnWriteMap<String, KeySpecification> verifyKeys = new CopyOnWriteMap<>();


  @Override
  public KeySpecification getSigningKey(String userId) {
    return signingKeys.get(userId);
  }


  @Override
  public KeySpecification getVerifyingKey(String userId) {
    return verifyKeys.get(userId);
  }


  /**
   * Set the signing key for a specific user.
   *
   * @param userId the user's ID
   * @param key    the key specification
   */
  public void setSigningKey(String userId, KeySpecification key) {
    Objects.requireNonNull(userId, "The user's ID must be specified.");
    if (key != null) {
      signingKeys.put(userId, key);
    } else {
      signingKeys.remove(userId);
    }
  }


  /**
   * Set a shared symmetric secret to use with HMAC-SHA256.
   *
   * @param userId the user's ID
   * @param secret the shared secret
   */
  public void setSymmetricSharedSecret(String userId, String secret) {
    Key secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "RAW");
    KeySpecification keySpecification = new KeySpecification(userId, secretKey, "HmacSHA256");
    setSigningKey(userId, keySpecification);
    setVerifyKey(userId, keySpecification);
  }


  /**
   * Set the verify key for a specific user.
   *
   * @param userId the user's ID
   * @param key    the key specification
   */
  public void setVerifyKey(String userId, KeySpecification key) {
    Objects.requireNonNull(userId, "The user's ID must be specified.");
    if (key != null) {
      verifyKeys.put(userId, key);
    } else {
      verifyKeys.remove(userId);
    }
  }

}
