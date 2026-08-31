package com.elsevier.cardiac_user_profile_service.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String userId) {
        super("Profile not found for user ID: " + userId);
    }
}