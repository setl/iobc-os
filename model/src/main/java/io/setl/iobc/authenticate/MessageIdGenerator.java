package io.setl.iobc.authenticate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.function.UnaryOperator;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import prng.SecureRandomBuilder;
import prng.SecureRandomBuilder.Hash;

import io.setl.util.LRUCache;

/**
 * A unique Message ID.
 *
 * @author Simon Greatrix on 14/11/2021.
 */
public class MessageIdGenerator implements UnaryOperator<String> {

  public static final MessageIdGenerator INSTANCE = new MessageIdGenerator();

  /** Generators for creating unique message IDs. */
  private static final LRUCache<String, Factory> CACHE = new LRUCache<>(1000, Factory::new);

  private static final Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();



  /**
   * Factory for creating unique IDs for a specific user.
   *
   * <p>This factory creates unpredictable unique IDs by combining a 32-bit counter, a 32-bit random value, and the millisecond epoch time which are passed
   * through an AES permutation to ensure the IDs are well distributed.</p>
   */
  private static class Factory {

    /** Cipher used to permute the deterministic source. */
    private final Cipher cipher;

    private final byte[] output = new byte[17];

    private final ByteBuffer outputArea;

    /** Random generator for creating unpredictable values. */
    private final SecureRandom random = SecureRandomBuilder.hash().hash(Hash.SHA512).laziness(1000).build();

    private final ByteBuffer workingArea = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder());

    /** Counter for generating unique IDs. */
    private int counter;

    /** Padding bits. */
    private long padding;


    // I know what I'm doing with ECB
    @SuppressWarnings("java:S5542")
    Factory(String userId) {
      outputArea = ByteBuffer.wrap(output).limit(16).slice();
      counter = random.nextInt();
      padding = random.nextLong();

      // Create an AES permutation using the user ID as a seed for the private key.
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(userId.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec spec = new SecretKeySpec(hash, 0, 16, "AES");
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
      } catch (GeneralSecurityException e) {
        throw new InternalError("Cryptographic failure", e);
      }
    }


    synchronized String create() {
      counter++;
      workingArea.clear();
      outputArea.clear();
      workingArea.putInt(0, counter);
      workingArea.putInt(4, random.nextInt());
      workingArea.putLong(8, System.currentTimeMillis());
      try {
        cipher.doFinal(workingArea, outputArea);
      } catch (GeneralSecurityException e) {
        // these errors should never happen
        throw new InternalError("Cryptographic failure", e);
      }

      // We are going to encode in Base-64. Fifteen bytes takes 20 characters. Sixteen bytes needs 22, but the last 1 only holds two bits. We use a padding
      // byte to use the full variety in the last character.
      output[16] = (byte) padding;
      padding >>>= 8;
      if (padding == 0) {
        padding = random.nextLong();
      }

      return ENCODER.encodeToString(output).substring(0, 22);
    }

  }


  @Override
  public String apply(String senderId) {
    return CACHE.get(senderId).create();
  }

}
