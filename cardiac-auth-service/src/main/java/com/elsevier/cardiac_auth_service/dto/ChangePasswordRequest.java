package com.elsevier.cardiac_auth_service.dto;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {
}