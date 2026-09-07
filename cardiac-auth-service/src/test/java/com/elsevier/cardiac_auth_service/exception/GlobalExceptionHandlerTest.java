package com.elsevier.cardiac_auth_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailAlreadyExists_returns409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleEmailAlreadyExists(new EmailAlreadyExistsException("taken"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "taken");
    }

    @Test
    void handleInvalidCredentials_returns401WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("bad creds"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "bad creds");
    }

    @Test
    void handleInvalidRefreshToken_returns401WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidRefreshToken(new InvalidRefreshTokenException("expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "expired");
    }

    @Test
    void handleInvalidRequestBody_returns400WithFixedMessage() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<Map<String, String>> response = handler.handleInvalidRequestBody(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Request body is required");
    }

    @Test
    void handleValidationException_returns400WithFieldErrors() {
        BindException bindingResult = new BindException(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Validation failed");
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors =
                (Map<String, String>) response.getBody().get("validationErrors");
        assertThat(validationErrors).containsEntry("email", "must not be blank");
    }

    @Test
    void handleKafkaPublishException_returns503WithFixedMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleKafkaPublishException(
                new KafkaPublishException("boom", new RuntimeException("cause")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody())
                .containsEntry("message", "Registration failed because the messaging service is unavailable");
    }

    @Test
    void handleInvalidPassword_returns400WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidPassword(new InvalidPasswordException("too short"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "too short");
    }

    @Test
    void handleUserNotFound_returns404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserNotFound(new UserNotFoundException("no such user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "no such user");
    }

    @Test
    void handleTypeMismatch_returns400WithParameterName() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "age", null, null);

        ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).contains("age");
    }

    @Test
    void handleDatabaseException_returns500WithFixedMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleDatabaseException(
                new DataAccessResourceFailureException("db down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "A database error occurred");
    }

    @Test
    void handleGenericException_returns500WithFixedMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGenericException(new RuntimeException("unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
