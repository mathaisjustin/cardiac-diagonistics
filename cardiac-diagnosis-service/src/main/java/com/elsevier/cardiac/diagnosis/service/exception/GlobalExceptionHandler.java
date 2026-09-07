package com.elsevier.cardiac.diagnosis.service.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";
    private static final String TIMESTAMP_KEY = "timestamp";

    @ExceptionHandler(DiagnosisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleDiagnosisNotFound(
            DiagnosisNotFoundException exception) {

        return Map.of(
                STATUS_KEY, 404,
                MESSAGE_KEY, exception.getMessage(),
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(ExternalApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleExternalApiException(
            ExternalApiException exception) {

        log.error("External diagnosis API call failed: {}", exception.getMessage(), exception);

        return Map.of(
                STATUS_KEY, 503,
                MESSAGE_KEY, exception.getMessage(),
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(
            ValidationException exception) {

        return Map.of(
                STATUS_KEY, 400,
                MESSAGE_KEY, exception.getMessage(),
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return Map.of(
                STATUS_KEY, 400,
                MESSAGE_KEY, "Invalid value for parameter: " + exception.getName(),
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleUnauthorizedException(
            UnauthorizedException exception) {

        return Map.of(
                STATUS_KEY, 401,
                MESSAGE_KEY, exception.getMessage(),
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(KafkaPublishException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleKafkaPublishException(
            KafkaPublishException exception) {

        log.error("Kafka publish failed: {}", exception.getMessage(), exception);

        return Map.of(
                STATUS_KEY, 503,
                MESSAGE_KEY, "Bookmarking is temporarily unavailable, please try again",
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneralException(
            Exception exception) {

        log.error("Unhandled exception: {}", exception.getMessage(), exception);

        return Map.of(
                STATUS_KEY, 500,
                MESSAGE_KEY, "An unexpected error occurred",
                TIMESTAMP_KEY, LocalDateTime.now(ZoneOffset.UTC)
        );
    }
}
