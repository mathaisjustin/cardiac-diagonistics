package com.elsevier.cardiac.diagnosis.service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDiagnosisNotFound_returns404Body() {
        Map<String, Object> body = handler.handleDiagnosisNotFound(new DiagnosisNotFoundException("42"));

        assertThat(body).containsEntry("status", 404)
                .containsEntry("message", "Diagnosis not found with id: 42");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleExternalApiException_returns503Body() {
        Map<String, Object> body =
                handler.handleExternalApiException(new ExternalApiException("upstream down"));

        assertThat(body).containsEntry("status", 503)
                .containsEntry("message", "upstream down");
    }

    @Test
    void handleValidationException_returns400Body() {
        Map<String, Object> body = handler.handleValidationException(new ValidationException("bad filter"));

        assertThat(body).containsEntry("status", 400)
                .containsEntry("message", "bad filter");
    }

    @Test
    void handleTypeMismatch_returns400BodyWithParameterName() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "age", null, null);

        Map<String, Object> body = handler.handleTypeMismatch(exception);

        assertThat(body).containsEntry("status", 400);
        assertThat((String) body.get("message")).contains("age");
    }

    @Test
    void handleUnauthorizedException_returns401Body() {
        Map<String, Object> body =
                handler.handleUnauthorizedException(new UnauthorizedException("login required"));

        assertThat(body).containsEntry("status", 401)
                .containsEntry("message", "login required");
    }

    @Test
    void handleKafkaPublishException_returns503FixedBody() {
        Map<String, Object> body = handler.handleKafkaPublishException(
                new KafkaPublishException("boom", new RuntimeException("cause")));

        assertThat(body).containsEntry("status", 503)
                .containsEntry("message", "Bookmarking is temporarily unavailable, please try again");
    }

    @Test
    void handleGeneralException_returns500FixedBody() {
        Map<String, Object> body = handler.handleGeneralException(new RuntimeException("unexpected"));

        assertThat(body).containsEntry("status", 500)
                .containsEntry("message", "An unexpected error occurred");
    }
}
