package io.setl.iobc.authenticate;

import java.io.UncheckedIOException;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import io.setl.http.signatures.KeySpecification;
import io.setl.http.signatures.error.UnsupportedAlgorithmException;
import io.setl.http.signatures.sign.Sign;
import io.setl.http.signatures.sign.SignFactory;
import io.setl.http.signatures.verify.Verify;
import io.setl.http.signatures.verify.VerifyFactory;
import io.setl.iobc.model.Response.InReplyTo;
import io.setl.iobc.util.SerdeSupport;

/**
 * A Message that can be signed and verified. Although it would be more elegant to store some parts of this in a message header that would require dependencies
 * on the transport mechanism.
 *
 * @author Simon Greatrix on 14/11/2021.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Jacksonized
@Slf4j
public class AuthenticatedMessage {

  private static final Decoder SIGNATURE_DECODE = Base64.getUrlDecoder();

  private static final Encoder SIGNATURE_ENCODE = Base64.getUrlEncoder().withoutPadding();

  private static final ObjectWriter SIGNATURE_WRITER;



  private interface ContentTypeSpecifier {

    @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
    AuthenticatedMessageBuilder content(MessageContent content);

  }



  /**
   * Base class for the builder implementation created by Lombok, ensuring that the Builder has the appropriate Jackson annotations.
   */
  public static class AuthenticatedMessageBuilder implements ContentTypeSpecifier {

  }



  static {
    SIGNATURE_WRITER = SerdeSupport.getObjectMapper().writerFor(AuthenticatedMessage.class);
  }

  /** The content of this message. */
  @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
  private final MessageContent content;

  /** This message's creation time. */
  private final long createTime;

  /** This message's ID (must be unique, to prevent replays). */
  private final String messageId;

  /** The message's type. */
  private final String type;

  /** The user ID. */
  private final String userId;

  /** This message's signature in Base-64 URL with no padding. */
  private String signature;


  /**
   * Create a message with no content.
   *
   * @param userId the sending user
   */
  public AuthenticatedMessage(String userId, String type) {
    this.userId = userId;
    this.type = type;
    messageId = MessageIdGenerator.INSTANCE.apply(userId);
    createTime = System.currentTimeMillis();
    content = null;
    signature = "";
  }


  /**
   * Create a message.
   *
   * @param userId  the sending user
   * @param content the content
   */
  public AuthenticatedMessage(String userId, String type, MessageContent content) {
    this.userId = userId;
    this.type = type;
    messageId = MessageIdGenerator.INSTANCE.apply(userId);
    createTime = System.currentTimeMillis();
    this.content = content;
    signature = "";
  }


  public InReplyTo buildInReplyTo() {
    return InReplyTo.builder().userId(userId).messageId(messageId).build();
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AuthenticatedMessage)) {
      return false;
    }
    AuthenticatedMessage that = (AuthenticatedMessage) o;
    return createTime == that.createTime && content.equals(that.content) && messageId.equals(that.messageId) && userId.equals(that.userId) && signature.equals(
        that.signature);
  }


  @Valid
  @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
  public MessageContent getContent() {
    return content;
  }


  public long getCreateTime() {
    return createTime;
  }


  @NotEmpty
  public String getMessageId() {
    return messageId;
  }


  @NotEmpty
  public String getSignature() {
    return signature;
  }


  @NotEmpty
  public String getType() {
    return type;
  }


  @NotEmpty
  public String getUserId() {
    return userId;
  }


  @Override
  public int hashCode() {
    return Objects.hash(messageId);
  }


  /**
   * Verify if this message has a valid signature.
   *
   * @param publicKey the public key
   *
   * @return true if valid
   */
  public boolean isValidSignature(KeySpecification publicKey) {
    if (signature.isEmpty() || publicKey == null) {
      return false;
    }
    try {
      Verify verify = VerifyFactory.create(publicKey);
      return verify.verify(SIGNATURE_DECODE.decode(signature), toBytes());
    } catch (InvalidKeyException | UnsupportedAlgorithmException e) {
      log.error("Invalid key provided for signature validation: {}", publicKey, e);
      return false;
    }
  }


  public void sign(KeySpecification privateKey) throws UnsupportedAlgorithmException, InvalidKeyException {
    Sign signer = SignFactory.create(privateKey);
    signature = SIGNATURE_ENCODE.encodeToString(signer.sign(toBytes()));
  }


  /**
   * Convert this to bytes to check the signature.
   *
   * @return the bytes to sign
   */
  private byte[] toBytes() {
    String oldSignature = signature;
    signature = "";
    try {
      return SIGNATURE_WRITER.writeValueAsBytes(this);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    } finally {
      signature = oldSignature;
    }
  }


  @Override
  public String toString() {
    return String.format(
        "AuthenticatedMessage(messageId=%s, content=%s, createTime=%s, signature=%s)",
        messageId, content, createTime, signature
    );
  }

}
