package io.setl.iobc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import io.setl.http.signatures.DigestConfig;
import io.setl.http.signatures.server.DigestFilter;
import io.setl.http.signatures.server.RequestIdFilter;
import io.setl.http.signatures.server.RequestIdTracker;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.rest.intercept.SignatureFilter;
import io.setl.pychain.rest.WebSecurityPathConfigurer;

/**
 * Security for Spring Actuator health end-points.
 *
 * <p>The order of the security configurations:</p>
 *
 * <p>
 * HIGHEST : The /error end points<br />
 * 1 : Spring actuators<br />
 * 2 : Normal requests <br />
 * LOWEST : Forbid everything<br />
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@SuppressFBWarnings("SPRING_CSRF_PROTECTION_DISABLED")
public class WebSecurityConfig {

  /**
   * A filter that denies all access.
   */
  private static final Filter DENY_ALL = (servletRequest, servletResponse, chain) -> {
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    response.sendError(
        HttpStatus.FORBIDDEN.value(),
        "Invalid authorization or URL. Please use an an appropriate authorization method for the resource you are trying to access."
    );
  };



  /**
   * Security for the server health endpoints.
   */
  @Configuration(proxyBeanMethods = false)
  public static class ActuatorSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.requestMatcher(EndpointRequest.to(HealthEndpoint.class)).authorizeRequests((requests) ->
          requests.anyRequest().anonymous());
      http.httpBasic();
    }

  }



  /**
   * Receiving error messages is key to troubleshooting, so always allow them.
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Configuration
  public static class ErrorSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.requestMatcher(new OrRequestMatcher(
              new AntPathRequestMatcher("/error/**"),
              new AntPathRequestMatcher("/error")
          ))
          .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
          .rememberMe().disable()
          .csrf().disable();
    }

  }



  /**
   * If no other set of end-points matches, use this.
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  @Configuration
  public static class FinalSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      // We deny everything except for OPTIONS requests which are required for CORS preflight
      http.requestMatcher(new NotOptionsMatcher())
          .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
          .csrf().disable()
          .addFilterAfter(DENY_ALL, SecurityContextPersistenceFilter.class);
    }

  }



  /** Match anything but HTTP OPTIONS. */
  public static class NotOptionsMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
      String method = request.getMethod();
      return !method.equals("OPTIONS");
    }

  }



  /** Security for the utility end-point. */
  @Order(1)
  @Configuration
  public static class UtilitySecurityConfig extends WebSecurityConfigurerAdapter {

    private List<RequestMatcher> allAntPaths;

    private DigestConfig digestConfig;

    private boolean isEnabled;

    private KeyProvider keyProvider;

    private RequestIdTracker requestIdTracker;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
      if (isEnabled) {
        // Add the signature, request ID and digest filters.
        Filter digestFilter = new DigestFilter(digestConfig);
        Filter signatureFilter = new SignatureFilter(keyProvider);
        Filter requestIdFilter = new RequestIdFilter(requestIdTracker);

        http.requestMatcher(new OrRequestMatcher(allAntPaths))
            .addFilterAfter(signatureFilter, SecurityContextPersistenceFilter.class)
            .addFilterAfter(digestFilter, SignatureFilter.class)
            .addFilterAfter(requestIdFilter, DigestFilter.class)
            .csrf().disable()
            .rememberMe().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
      } else {
        // Allow access
        http.requestMatcher(new OrRequestMatcher(allAntPaths))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .rememberMe().disable()
            .csrf().disable()
            .anonymous(configuration -> configuration.authorities("ROLE_ANONYMOUS", "ROLE_USER"));
      }
    }


    /** Set the paths that need to be secured. */
    @Autowired
    public void setAntPaths(Collection<WebSecurityPathConfigurer> configurers) {
      ArrayList<RequestMatcher> paths = new ArrayList<>();
      for (WebSecurityPathConfigurer c : configurers) {
        for (String p : c.getOpenPaths()) {
          paths.add(new AntPathRequestMatcher(p));
        }
      }
      allAntPaths = paths;
    }


    @Autowired
    public void setDigestConfig(DigestConfig digestConfig) {
      this.digestConfig = digestConfig;
    }


    // Enabled by default
    @Value("${setl.http.bearer.enabled:true}")
    public void setEnabled(boolean enabled) {
      isEnabled = enabled;
    }


    @Autowired
    public void setKeyProvider(KeyProvider keyProvider) {
      this.keyProvider = keyProvider;
    }


    @Autowired
    public void setRequestIdTracker(RequestIdTracker requestIdTracker) {
      this.requestIdTracker = requestIdTracker;
    }

  }

}
