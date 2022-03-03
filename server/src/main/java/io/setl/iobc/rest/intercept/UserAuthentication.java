package io.setl.iobc.rest.intercept;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A spring security authentication token for an Open CSD user.
 */
public class UserAuthentication extends AbstractAuthenticationToken {

  private static final GrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");

  /** User connections have the "USER" role. */
  private static final Collection<GrantedAuthority> ROLE_USER = Collections.singleton(USER);

  private final String userId;


  /**
   * New instance.
   *
   * @param userId the user's ID
   */
  public UserAuthentication(String userId) {
    super(ROLE_USER);
    this.userId = userId;
    setAuthenticated(true);
  }


  @Override
  public Object getCredentials() {
    return null;
  }


  @Override
  public String getName() {
    return userId;
  }


  @Override
  public Object getPrincipal() {
    return (AuthenticatedPrincipal) () -> userId;
  }

}
