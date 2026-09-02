package com.elsevier.cardiac.diagnosis.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Verifies the X-Identity-Signature header the API Gateway attaches alongside X-User-Id /
 * X-User-Email, so this service can trust those headers without re-parsing the caller's JWT
 * itself. The Gateway signs "<userId>|<email>" (HMAC-SHA256) with the same shared secret this
 * service is configured with - if a request's headers don't match a signature computed the
 * same way, they were not set by the Gateway and must not be trusted.
 */
@Component
public class IdentitySignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec signingKey;

    public IdentitySignatureVerifier(@Value("${jwt.secret}") String secret) {
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public boolean isValid(String userId, String email, String signature) {
        if (userId == null || signature == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);

            String canonical = userId + "|" + (email == null ? "" : email);
            byte[] expected = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(expected);

            return java.security.MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            return false;
        }
    }
}
