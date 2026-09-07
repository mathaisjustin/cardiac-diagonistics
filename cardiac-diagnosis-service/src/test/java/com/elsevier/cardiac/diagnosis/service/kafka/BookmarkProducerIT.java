package com.elsevier.cardiac.diagnosis.service.kafka;

import com.elsevier.cardiac.diagnosis.service.event.BookmarkEvent;
import com.elsevier.cardiac.diagnosis.service.event.DiagnosisPayload;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real Kafka integration test - publishes through BookmarkProducer against an actual broker
 * via Testcontainers, then reads the message back with a plain consumer to verify the topic,
 * key, and serialized payload. Requires a Docker daemon to run: `mvn verify` (or `mvn test`)
 * on a machine with Docker, or in CI - this will NOT run in an environment without Docker
 * available.
 */
@Testcontainers
class BookmarkProducerIT {

    private static final String TOPIC = "bookmark.created";

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaConsumer<String, String> consumer;
    private BookmarkProducer bookmarkProducer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        );
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
        bookmarkProducer = new BookmarkProducer(kafkaTemplate, new ObjectMapper(), TOPIC);

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, "bookmark-producer-it",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(TOPIC));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void publish_sendsSerializedEventToRealBroker() {
        DiagnosisPayload payload = new DiagnosisPayload("Male", 45, "130", "Typical Angina", "Medication");
        BookmarkEvent event = new BookmarkEvent("user-1", "1", payload);

        bookmarkProducer.publish(event);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        List<ConsumerRecord<String, String>> received = new java.util.ArrayList<>();
        records.forEach(received::add);

        assertThat(received).hasSize(1);
        ConsumerRecord<String, String> record = received.get(0);
        assertThat(record.key()).isEqualTo("user-1");
        assertThat(record.value())
                .contains("\"diagnosisId\":\"1\"")
                .contains("\"gender\":\"Male\"");
    }
}
