package io.setl.iobc;

import java.util.Map;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;

import io.setl.iobc.authenticate.AuthenticatedMessage;
import io.setl.iobc.authenticate.KeyProvider;
import io.setl.iobc.authenticate.SimpleKeyProvider;
import io.setl.iobc.config.TopicConfiguration;
import io.setl.iobc.util.SerdeSupport;
import io.setl.json.jackson.JsonModule;

/**
 * Common spring configuration.
 *
 * @author Simon Greatrix on 12/11/2021.
 */
@ComponentScan(basePackages = "io.setl.iobc")
@PropertySource({
    "classpath:/io/setl/iobc/iobc-secrets.properties",
})
@Configuration
@Import({TopicConfiguration.class})
class SpringConfiguration {

  @Autowired
  public SpringConfiguration() {
  }


  @Bean
  public KStream<String, AuthenticatedMessage> inboundStream(
      StreamsBuilder builder,
      InboundProcessorSupplier supplier
  ) {
    KStream<String, AuthenticatedMessage> stream = builder.stream(
        TopicConfiguration.INBOUND,
        Consumed.with(Serdes.String(), SerdeSupport.newSerde(AuthenticatedMessage.class))
    );
    stream.process(supplier);
    return stream;
  }


  @Bean
  public KeyProvider keyProvider(@Value("#{${setl.iobc.security.sharedSecrets}}") Map<String, String> secrets) {
    SimpleKeyProvider keyProvider = new SimpleKeyProvider();
    secrets.forEach(keyProvider::setSymmetricSharedSecret);
    return keyProvider;
  }


  @Bean
  @Order(1) // Execute after order=0 has run so we over-ride Spring Boot
  public Jackson2ObjectMapperBuilderCustomizer objectMapperCustomizer() {
    return builder -> {
      // case in-sensitive and default view
      builder.featuresToEnable(
          MapperFeature.DEFAULT_VIEW_INCLUSION,
          MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
          MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
      );

      // standard modules
      builder.modulesToInstall(
          new JsonModule(),
          new Jdk8Module(),
          new JavaTimeModule(),
          new ParameterNamesModule()
      );
    };
  }


  @Bean
  public KafkaTemplate<String, AuthenticatedMessage> outboundTemplate(KafkaProperties kafkaProperties) {
    Serializer<AuthenticatedMessage> serializer = SerdeSupport.newSerializer(AuthenticatedMessage.class);
    return TopicConfiguration.makeTemplate(kafkaProperties, TopicConfiguration.OUTBOUND, new StringSerializer(), serializer);
  }

}
