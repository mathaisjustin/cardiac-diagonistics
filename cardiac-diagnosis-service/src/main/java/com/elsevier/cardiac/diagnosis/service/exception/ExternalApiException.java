package com.elsevier.cardiac.diagnosis.service.exception;

public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }
}