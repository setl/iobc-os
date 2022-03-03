package io.setl.iobc.rest;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import io.setl.pychain.rest.WebSecurityPathConfigurer;


/**
 * Specify which paths to secure.
 *
 * @author Simon Greatrix on 06/07/2020.
 */
@Configuration
public class RestSecurityConfig implements WebSecurityPathConfigurer {

  @Override
  public Collection<String> getOpenPaths() {
    return List.of("/api/**");
  }


  @Override
  public Collection<String> getSecurePaths() {
    return List.of();
  }

}
