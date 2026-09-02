package com.elsevier.cardiac_bookmark_service.config;

import com.elsevier.cardiac_bookmark_service.exception.InvalidBookmarkEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Centralized Kafka listener error handling, mirroring GlobalExceptionHandler's
 * role for the REST layer: consumer exceptions land here instead of being
 * swallowed inside each @KafkaListener method.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "Giving up on Kafka record from topic '{}' partition {} offset {}: {}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        exception.getMessage(),
                        exception
                ),
                new FixedBackOff(1000L, 2)
        );

        // A malformed payload will never succeed on retry, so fail fast.
        errorHandler.addNotRetryableExceptions(InvalidBookmarkEventException.class);

        return errorHandler;
    }
}
