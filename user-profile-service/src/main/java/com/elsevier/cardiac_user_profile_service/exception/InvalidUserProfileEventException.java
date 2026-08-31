package com.elsevier.cardiac_user_profile_service.exception;

public class InvalidUserProfileEventException extends RuntimeException {

    public InvalidUserProfileEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
