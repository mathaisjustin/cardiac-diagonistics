package com.elsevier.cardiac_api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Signs the identity the Gateway forwards downstream, so a service can trust X-User-Id /
 * X-User-Email without re-parsing the JWT itself - it only needs to recompute this HMAC with
 * the same shared secret and compare. Without this, X-User-Id would be a plain, spoofable
 * header: anything that could reach a service directly (or forge the header before the
 * Gateway's own auth filter runs) could claim to be any user.
 *
 * Canonical string signed is always "<userId>|<email>", with an empty string for email when
 * the route doesn't forward one (e.g. the bookmark route) - callers on both sides must build
 * the same canonical string or verification will fail.
 */
@Service
public class IdentitySigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec signingKey;

    public IdentitySigner(@Value("${jwt.secret}") String secret) {
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String sign(String userId, String email) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);

            String canonical = userId + "|" + (email == null ? "" : email);
            byte[] signature = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign identity headers", exception);
        }
    }
}
