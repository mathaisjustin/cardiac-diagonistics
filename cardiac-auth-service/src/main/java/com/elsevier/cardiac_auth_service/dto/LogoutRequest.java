package com.elsevier.cardiac_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

        @Schema(description = "Refresh token to revoke")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}