package com.elsevier.cardiac_auth_service.kafka;

import com.elsevier.cardiac_auth_service.dto.UserRegisteredEvent;
import com.elsevier.cardiac_auth_service.exception.KafkaPublishException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationProducer {

    private static final String TOPIC = "user.registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    public UserRegistrationProducer(
            KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UserRegisteredEvent event) {

        try {
            kafkaTemplate.send(
                    TOPIC,
                    event.userId().toString(),
                    event
            ).get();

        } catch (Exception e) {
            throw new KafkaPublishException(
                    "Failed to publish user registration event",
                    e
            );
        }
    }
}