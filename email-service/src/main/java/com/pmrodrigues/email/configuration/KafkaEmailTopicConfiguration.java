package com.pmrodrigues.email.configuration;

import com.pmrodrigues.commons.dtos.Email;
import lombok.NonNull;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaEmailTopicConfiguration {

    @Value("${spring.kafka.bootstrapAddress:localhost:29092}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic email() {
        return TopicBuilder
                .name("email")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public ProducerFactory<String, Email> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, "email:com.pmrodrigues.commons.dtos.Email");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory<String, Email> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "send");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "email:com.pmrodrigues.commons.dtos.Email");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(Email.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Email> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Email> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Email> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public DefaultErrorHandler errorHandler(@NonNull final ConsumerRecordRecoverer recoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer);
        handler.addNotRetryableExceptions(MessagingException.class);
        handler.addRetryableExceptions(MailException.class);
        return handler;
    }
}
