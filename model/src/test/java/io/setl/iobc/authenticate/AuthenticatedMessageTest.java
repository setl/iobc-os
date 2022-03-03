package io.setl.iobc.authenticate;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import io.setl.crypto.KeyGen.Type;
import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.UnsupportedAlgorithmException;
import io.setl.iobc.model.tokens.BurnToken;

/**
 * @author Simon Greatrix on 14/11/2021.
 */
public class AuthenticatedMessageTest {


  @Test
  public void serialize() throws IOException, UnsupportedAlgorithmException, InvalidKeyException {
    KeyPair keyPair = Type.EC_NIST_P256.generate();
    BurnToken.Input input = BurnToken.Input.builder().symbol("EURO").amount(BigInteger.TEN).from("myAddress").build();
    AuthenticatedMessage message = new AuthenticatedMessage("user1337", "TEST", input);
    message.sign(new KeySpecification("TEST", keyPair.getPrivate(), "EC"));

    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writeValueAsString(message);
    System.out.println(json);

    AuthenticatedMessage message2 = objectMapper.readValue(json, AuthenticatedMessage.class);
    assertEquals(message, message2);
    assertEquals(message.hashCode(), message2.hashCode());
  }


  @Test
  public void signVerify() throws UnsupportedAlgorithmException, InvalidKeyException {
    KeyPair keyPair = Type.EC_NIST_P256.generate();
    BurnToken.Input input = BurnToken.Input.builder().symbol("EURO").amount(BigInteger.TEN).from("myAddress").build();
    AuthenticatedMessage message = new AuthenticatedMessage("user1337", "TEST", input);
    message.sign(new KeySpecification("TEST", keyPair.getPrivate(), "EC"));
    assertTrue(message.isValidSignature(new KeySpecification("TEST", keyPair.getPublic(), "EC")));
  }

}