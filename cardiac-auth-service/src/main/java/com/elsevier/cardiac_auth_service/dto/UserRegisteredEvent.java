package com.elsevier.cardiac_auth_service.dto;

public record UserRegisteredEvent(
        String userId,
        String firstName,
        String lastName,
        String contactNumber,
        String department
) {
}