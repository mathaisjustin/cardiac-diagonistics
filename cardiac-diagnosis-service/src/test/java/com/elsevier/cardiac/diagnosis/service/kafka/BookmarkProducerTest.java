package com.elsevier.cardiac.diagnosis.service.kafka;

import com.elsevier.cardiac.diagnosis.service.event.BookmarkEvent;
import com.elsevier.cardiac.diagnosis.service.exception.KafkaPublishException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private final BookmarkEvent event = new BookmarkEvent("u1", "1", null);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void publish_executionException_wrapsInKafkaPublishException() throws Exception {
        when(objectMapper.writeValueAsString(event)).thenReturn("{}");
        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send("bookmark.created", "u1", "{}")).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("send failed", new RuntimeException()));

        BookmarkProducer producer = new BookmarkProducer(kafkaTemplate, objectMapper, "bookmark.created");

        assertThatThrownBy(() -> producer.publish(event))
                .isInstanceOf(KafkaPublishException.class)
                .hasMessageContaining("Failed to publish bookmark event");
    }

    @Test
    void publish_interruptedException_reinterruptsThreadAndWrapsException() throws Exception {
        when(objectMapper.writeValueAsString(event)).thenReturn("{}");
        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send("bookmark.created", "u1", "{}")).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("interrupted"));

        BookmarkProducer producer = new BookmarkProducer(kafkaTemplate, objectMapper, "bookmark.created");

        assertThatThrownBy(() -> producer.publish(event))
                .isInstanceOf(KafkaPublishException.class)
                .hasMessageContaining("Interrupted while publishing bookmark event");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
