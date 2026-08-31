package com.elsevier.cardiac_auth_service.config;

import com.elsevier.cardiac_auth_service.dto.UserRegisteredEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, UserRegisteredEvent> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        config.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );

        config.put(
                ProducerConfig.RETRIES_CONFIG,
                1
        );

        config.put(
                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                10000
        );

        config.put(
                ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                5000
        );

        config.put(
                ProducerConfig.MAX_BLOCK_MS_CONFIG,
                10000
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate(
            ProducerFactory<String, UserRegisteredEvent> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }
}