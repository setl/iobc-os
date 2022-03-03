package io.setl.iobc.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

/**
 * Shutdown handler for Kafka.
 *
 * @author Simon Greatrix on 26/11/2021.
 */
public class KafkaStreamsShutdown implements InitializingBean {

  private StreamsBuilderFactoryBean factoryBean;


  @Override
  public void afterPropertiesSet() {
    Runtime.getRuntime().addShutdownHook(new Thread(factoryBean::stop));
  }


  @Autowired
  public void setStreamsBuilderFactoryBean(StreamsBuilderFactoryBean factoryBean) {
    this.factoryBean = factoryBean;
  }

}
