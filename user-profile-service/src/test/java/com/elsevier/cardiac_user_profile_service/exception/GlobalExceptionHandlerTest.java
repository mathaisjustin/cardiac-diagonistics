package com.elsevier.cardiac_user_profile_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleProfileNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleProfileNotFound(new ProfileNotFoundException("u1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Profile not found for user ID: u1");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleMissingRequestHeader_returns400() throws NoSuchMethodException {
        Method method = SampleController.class.getMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MissingRequestHeaderException exception =
                new MissingRequestHeaderException("X-User-Id", parameter);

        ResponseEntity<ErrorResponse> response = handler.handleMissingRequestHeader(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("X-User-Id");
    }

    @Test
    void handleInvalidRequestBody_returns400() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequestBody(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid request body");
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
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "age", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("age");
    }

    @Test
    void handleDatabaseException_returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDatabaseException(new DataAccessResourceFailureException("db down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("A database error occurred");
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    private static class SampleController {
        public void sample(String userId) {
        }
    }
}
