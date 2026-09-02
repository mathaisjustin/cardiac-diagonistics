package com.elsevier.cardiac_bookmark_service.producer;

import com.elsevier.cardiac_bookmark_service.event.BookmarkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Test-only mechanism: lets us publish a sample {@code bookmark.created} event onto Kafka
 * (via {@link com.elsevier.cardiac_bookmark_service.controller.MockBookmarkController}) to
 * verify {@link com.elsevier.cardiac_bookmark_service.consumer.BookmarkConsumer} end-to-end,
 * before the real producer (Diagnosis Service / frontend flow) exists. Not part of the
 * documented Bookmark Service responsibilities.
 */
@Component
public class MockBookmarkProducer {

    private static final Logger log = LoggerFactory.getLogger(MockBookmarkProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;


    public MockBookmarkProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${bookmark.kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }


    public void publish(BookmarkEvent event) {

        String message = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(topic, event.getUserId(), message);

        log.info("Published mock bookmark event to topic '{}': {}", topic, message);
    }
}
