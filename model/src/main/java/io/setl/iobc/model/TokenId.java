package io.setl.iobc.model;

import javax.validation.constraints.NotEmpty;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.setl.iobc.authenticate.MessagePrincipals;

/**
 * Specify just a token symbol as the input to an operation.
 *
 * @author Simon Greatrix on 24/11/2021.
 */
@Builder
@Value
@Jacksonized
public class TokenId implements MessageInput {

  /** The symbol and primary identifier of the token. */
  @NotEmpty
  String symbol;

  @Override
  public MessagePrincipals resolvePrincipal() {
    return MessagePrincipals.forPublic();
  }
}
