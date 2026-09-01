package com.elsevier.cardiac.diagnosis.service.kafka;

import com.elsevier.cardiac.diagnosis.service.event.BookmarkEvent;
import com.elsevier.cardiac.diagnosis.service.exception.KafkaPublishException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class BookmarkProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public BookmarkProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${bookmark.kafka.topic}") String topic) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(BookmarkEvent event) {

        String message;

        try {
            message = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new KafkaPublishException("Failed to serialize bookmark event", e);
        }

        try {
            kafkaTemplate.send(topic, event.getUserId(), message).get();
        } catch (Exception e) {
            throw new KafkaPublishException("Failed to publish bookmark event", e);
        }
    }
}
