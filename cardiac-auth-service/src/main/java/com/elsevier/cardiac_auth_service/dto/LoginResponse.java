package com.elsevier.cardiac_auth_service.dto;

public record LoginResponse(
        String accessToken,String refreshToken
) {
}