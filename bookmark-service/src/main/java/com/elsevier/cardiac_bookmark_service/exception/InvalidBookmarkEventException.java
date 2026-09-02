package com.elsevier.cardiac_bookmark_service.exception;

public class InvalidBookmarkEventException extends RuntimeException {

    public InvalidBookmarkEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
