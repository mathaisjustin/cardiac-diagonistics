package com.elsevier.cardiac_bookmark_service.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig config = new KafkaConsumerConfig();

    @Test
    void kafkaErrorHandler_returnsAConfiguredHandler() {
        DefaultErrorHandler handler = config.kafkaErrorHandler();

        assertThat(handler).isNotNull();
    }

    @Test
    void logGivingUp_doesNotThrowForAFailedRecord() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("bookmark.created", 0, 5L, "key", "value");

        assertThatCode(() -> config.logGivingUp(record, new RuntimeException("boom")))
                .doesNotThrowAnyException();
    }
}
