package com.elsevier.cardiac_auth_service.dto;

public record UserRegisteredEvent(
        Long userId,
//        String email,
        String firstName,
        String lastName,
        String contactNumber,
        String department
) {
}