package io.setl.iobc;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Raw chain configuration from the application YAML file.
 *
 * @author Simon Greatrix on 03/02/2022.
 */
@Configuration
@ConfigurationProperties("setl.iobc")
@Getter
@Setter
public class RawChainConfiguration {

  private Map<String, Object> chain;

}
