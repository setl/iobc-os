package io.setl.iobc.besu;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for working with a BESU block-chain.
 *
 * @author Simon Greatrix on 18/11/2021.
 */
@Configuration
@EnableScheduling
public class BesuConfiguration {

  @Bean
  public ExecutorService besuExecutorService() {
    return new ThreadPoolExecutor(2, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
  }

}
