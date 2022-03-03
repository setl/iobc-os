package io.setl.iobc.model;

import static org.junit.Assert.assertEquals;

import java.security.InvalidKeyException;
import java.security.KeyPair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import io.setl.crypto.KeyGen.Type;
import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.UnsupportedAlgorithmException;
import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.json.jackson.JsonModule;

/**
 * @author Simon Greatrix on 16/11/2021.
 */
public class TransactionResponseTest {

  @Test
  public void testFail() throws JsonProcessingException, UnsupportedAlgorithmException, InvalidKeyException {
    Response response = Response.builder().pass(true).inReplyTo(InReplyTo.builder().userId("user").messageId("a message").build()).errorDetails(
        ErrorDetails.builder().code("code").message("message").build()
    ).build();

    KeyPair keyPair = Type.EC_NIST_P256.generate();
    AuthenticatedMessage message = new AuthenticatedMessage("user1337", "TEST", response);
    message.sign(new KeySpecification("test", keyPair.getPrivate(), "EC"));

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JsonModule());
    String json = objectMapper.writeValueAsString(message);

    AuthenticatedMessage message2 = objectMapper.readValue(json, AuthenticatedMessage.class);
    assertEquals(message, message2);
    assertEquals(message.hashCode(), message2.hashCode());
  }


  @Test
  public void testPass() throws JsonProcessingException, UnsupportedAlgorithmException, InvalidKeyException {
    Response response = Response.builder().pass(true).inReplyTo(InReplyTo.builder().userId("user").messageId("a message").build()).result(
        TransactionResult.builder().transactionId("txid").build()
    ).build();

    KeyPair keyPair = Type.EC_NIST_P256.generate();
    AuthenticatedMessage message = new AuthenticatedMessage("user1337", "TEST", response);
    message.sign(new KeySpecification("test", keyPair.getPrivate(), "EC"));

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JsonModule());
    String json = objectMapper.writeValueAsString(message);

    AuthenticatedMessage message2 = objectMapper.readValue(json, AuthenticatedMessage.class);
    assertEquals(message, message2);
    assertEquals(message.hashCode(), message2.hashCode());
  }

}