package com.elsevier.cardiac_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Short-lived JWT access token") String accessToken,
        @Schema(description = "Long-lived opaque refresh token") String refreshToken
) {
}