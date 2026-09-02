package com.elsevier.cardiac.diagnosis.service.exception;

import java.time.LocalDateTime;
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

    @ExceptionHandler(DiagnosisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleDiagnosisNotFound(
            DiagnosisNotFoundException exception) {

        return Map.of(
                "status", 404,
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(ExternalApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleExternalApiException(
            ExternalApiException exception) {

        log.error("External diagnosis API call failed: {}", exception.getMessage(), exception);

        return Map.of(
                "status", 503,
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(
            ValidationException exception) {

        return Map.of(
                "status", 400,
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return Map.of(
                "status", 400,
                "message", "Invalid value for parameter: " + exception.getName(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleUnauthorizedException(
            UnauthorizedException exception) {

        return Map.of(
                "status", 401,
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(KafkaPublishException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleKafkaPublishException(
            KafkaPublishException exception) {

        log.error("Kafka publish failed: {}", exception.getMessage(), exception);

        return Map.of(
                "status", 503,
                "message", "Bookmarking is temporarily unavailable, please try again",
                "timestamp", LocalDateTime.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneralException(
            Exception exception) {

        log.error("Unhandled exception: {}", exception.getMessage(), exception);

        return Map.of(
                "status", 500,
                "message", "An unexpected error occurred",
                "timestamp", LocalDateTime.now()
        );
    }
}
