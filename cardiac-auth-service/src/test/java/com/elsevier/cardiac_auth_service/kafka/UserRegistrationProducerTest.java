package com.elsevier.cardiac_auth_service.kafka;

import com.elsevier.cardiac_auth_service.dto.UserRegisteredEvent;
import com.elsevier.cardiac_auth_service.exception.KafkaPublishException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationProducerTest {

    @Mock
    private KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    private final UserRegisteredEvent event =
            new UserRegisteredEvent("u1", "Jane", "Doe", "+1-555-0100", "Cardiology");

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void publish_executionException_wrapsInKafkaPublishException() throws Exception {
        CompletableFuture<SendResult<String, UserRegisteredEvent>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send("user.registered", "u1", event)).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("send failed", new RuntimeException()));

        UserRegistrationProducer producer = new UserRegistrationProducer(kafkaTemplate);

        assertThatThrownBy(() -> producer.publish(event))
                .isInstanceOf(KafkaPublishException.class)
                .hasMessageContaining("Failed to publish user registration event");
    }

    @Test
    void publish_interruptedException_reinterruptsThreadAndWrapsException() throws Exception {
        CompletableFuture<SendResult<String, UserRegisteredEvent>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send("user.registered", "u1", event)).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("interrupted"));

        UserRegistrationProducer producer = new UserRegistrationProducer(kafkaTemplate);

        assertThatThrownBy(() -> producer.publish(event))
                .isInstanceOf(KafkaPublishException.class)
                .hasMessageContaining("Interrupted while publishing user registration event");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
