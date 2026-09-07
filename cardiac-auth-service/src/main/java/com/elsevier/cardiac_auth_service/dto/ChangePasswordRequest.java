package com.elsevier.cardiac_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChangePasswordRequest(
        @Schema(description = "Current password") String oldPassword,
        @Schema(description = "New password to set") String newPassword
) {
}