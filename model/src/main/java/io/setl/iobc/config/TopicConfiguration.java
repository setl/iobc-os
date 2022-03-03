package io.setl.iobc.config;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.LoggingProducerListener;

/**
 * Configuration for Kafka topics.
 *
 * @author Simon Greatrix on 17/11/2021.
 */
@Configuration
@EnableKafka
@EnableKafkaStreams
public class TopicConfiguration {

  public static final String INBOUND = "setl-iobc.inbound";

  public static final String OUTBOUND = "setl-iobc.outbound";


  /**
   * Create a template with the required properties. Create only one template per topic.
   *
   * @param kafkaProperties the configuration
   * @param topic           the topic to attach the template to
   * @param keySerializer   the topic's key serializer
   * @param valueSerializer the topic's value serializer
   * @param <K>             the key type
   * @param <V>             the value type
   *
   * @return a new template
   */
  public static <K, V> KafkaTemplate<K, V> makeTemplate(
      KafkaProperties kafkaProperties,
      String topic,
      Serializer<K> keySerializer,
      Serializer<V> valueSerializer
  ) {
    Map<String, Object> map = kafkaProperties.buildProducerProperties();
    // This must be unique within the kafka ecosystem.
    String producerId = Objects.requireNonNull(kafkaProperties.getProducer().getClientId()) + "/" + topic;
    map.put(ProducerConfig.CLIENT_ID_CONFIG, producerId);

    // Surprisingly, Spring does not include this property in its built properties.
    map.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerId + "/");

    DefaultKafkaProducerFactory<K, V> factory = new DefaultKafkaProducerFactory<>(map, keySerializer, valueSerializer);
    KafkaTemplate<K, V> myTemplate = new KafkaTemplate<>(factory);
    myTemplate.setProducerListener(new LoggingProducerListener<>());
    myTemplate.setDefaultTopic(topic);
    return myTemplate;
  }


  /**
   * Topic on which IOBC receives messages.
   *
   * @param partitions the number of partitions for the topic. A negative number indicates to use the server default.
   * @param retention  the duration for which topic messages should be retained. A zero duration indicates to use the server default.
   *
   * @return new topic instance.
   */
  @Bean
  public NewTopic inboundTopic(@Value("${setl.iobc.topic.partitions:-1}") int partitions, @Value("${setl.iobc.topic.retention:P0D}") Duration retention) {
    return newTopic(INBOUND, partitions, retention);
  }


  /** Kafka shutdown helper. */
  @Bean
  public KafkaStreamsShutdown kafkaStreamsShutdown() {
    return new KafkaStreamsShutdown();
  }


  /**
   * Create a new topic specification.
   *
   * @param name       the topics name
   * @param partitions the number of partitions for the topic. A negative number indicates to use the server default.
   * @param retention  the duration for which topic messages should be retained. A zero duration indicates to use the server default.
   *
   * @return the new topic
   */
  private NewTopic newTopic(String name, int partitions, Duration retention) {
    TopicBuilder builder = TopicBuilder.name(name);
    // Spring forwards all non-negative numbers to Kafka. Not sure what zero partitions could mean. Spring will not attempt to decrease the number of partitions.
    if (partitions >= 0) {
      builder = builder.partitions(partitions);
    }

    // Retention time. Spring only applies this on topic creation, so changing it does nothing.
    if (!retention.isZero()) {
      builder = builder.config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retention.toMillis()));
    }
    return builder.build();
  }


  /**
   * Topic on which IOBC sends messages.
   *
   * @param partitions the number of partitions for the topic. A negative number indicates to use the server default.
   * @param retention  the duration for which topic messages should be retained. A zero duration indicates to use the server default.
   *
   * @return new topic instance.
   */
  @Bean
  public NewTopic outboundTopic(@Value("${setl.iobc.topic.partitions:-1}") int partitions, @Value("${setl.iobc.topic.retention:P0D}") Duration retention) {
    return newTopic(OUTBOUND, partitions, retention);
  }

}
