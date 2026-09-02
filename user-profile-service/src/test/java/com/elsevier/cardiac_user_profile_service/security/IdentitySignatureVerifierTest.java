package com.elsevier.cardiac_user_profile_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class IdentitySignatureVerifierTest {

    private static final String SECRET = "test-shared-secret-for-hmac-signing-tests";

    private IdentitySignatureVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new IdentitySignatureVerifier(SECRET);
    }

    @Test
    void acceptsASignatureComputedTheSameWayTheGatewayWould() throws Exception {
        String signature = sign("user-123", "jane@example.com");

        assertThat(verifier.isValid("user-123", "jane@example.com", signature)).isTrue();
    }

    @Test
    void rejectsAMissingSignature() {
        assertThat(verifier.isValid("user-123", "jane@example.com", null)).isFalse();
    }

    @Test
    void rejectsAMissingUserId() {
        assertThat(verifier.isValid(null, "jane@example.com", "anything")).isFalse();
    }

    @Test
    void rejectsATamperedUserId() throws Exception {
        String signature = sign("user-123", "jane@example.com");

        // signature was computed for user-123, but the header now claims a different user
        assertThat(verifier.isValid("someone-else", "jane@example.com", signature)).isFalse();
    }

    @Test
    void rejectsASignatureComputedWithADifferentSecret() throws Exception {
        IdentitySignatureVerifier otherVerifier = new IdentitySignatureVerifier(SECRET);
        String signature = signWithSecret("user-123", "jane@example.com", "a-totally-different-secret");

        assertThat(otherVerifier.isValid("user-123", "jane@example.com", signature)).isFalse();
    }

    @Test
    void treatsAMissingEmailAsAnEmptyStringInTheCanonicalForm() throws Exception {
        String signature = sign("user-123", null);

        assertThat(verifier.isValid("user-123", null, signature)).isTrue();
    }

    private String sign(String userId, String email) throws Exception {
        return signWithSecret(userId, email, SECRET);
    }

    private String signWithSecret(String userId, String email, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        String canonical = userId + "|" + (email == null ? "" : email);
        byte[] result = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
    }
}
