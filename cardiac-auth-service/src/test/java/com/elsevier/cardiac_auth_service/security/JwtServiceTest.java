package com.elsevier.cardiac_auth_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long-for-hmac";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 900000L);
    }

    @Test
    void generatesATokenThatIsValid() {
        String token = jwtService.generateAccessToken("user-123", "jane@example.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractsTheUserIdFromTheSubjectClaim() {
        String token = jwtService.generateAccessToken("user-123", "jane@example.com");

        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
    }

    @Test
    void extractsTheEmailFromTheCustomClaim() {
        String token = jwtService.generateAccessToken("user-123", "jane@example.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("jane@example.com");
    }

    @Test
    void rejectsAGarbageToken() {
        assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();
    }

    @Test
    void rejectsATokenSignedWithADifferentSecret() {
        JwtService otherService = new JwtService(
                "a-completely-different-secret-key-32-bytes-plus", 900000L);

        String token = otherService.generateAccessToken("user-123", "jane@example.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void rejectsAnExpiredToken() {
        JwtService alreadyExpired = new JwtService(SECRET, -1000L);

        String token = alreadyExpired.generateAccessToken("user-123", "jane@example.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
