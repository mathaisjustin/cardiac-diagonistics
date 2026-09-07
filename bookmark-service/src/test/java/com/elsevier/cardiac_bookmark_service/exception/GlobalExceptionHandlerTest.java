package com.elsevier.cardiac_bookmark_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBookmarkNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBookmarkNotFound(new BookmarkNotFoundException("b1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("No bookmark found with id: b1");
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
