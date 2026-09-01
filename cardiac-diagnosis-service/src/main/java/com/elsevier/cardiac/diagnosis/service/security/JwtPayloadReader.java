package com.elsevier.cardiac.diagnosis.service.security;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Reads JWT claims without verifying the signature - the Gateway (or whoever
 * forwarded the request) is trusted to have already validated the token.
 * This service only needs to know who's calling, not re-authenticate them.
 */
public final class JwtPayloadReader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtPayloadReader() {
    }

    public static Optional<Map<String, Object>> readClaims(String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(7).trim();
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(pad(parts[1]));

            Map<String, Object> claims = OBJECT_MAPPER.readValue(
                    new String(decoded, StandardCharsets.UTF_8),
                    Map.class
            );

            return Optional.of(claims);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static boolean isAuthenticated(String authorizationHeader) {
        return readClaims(authorizationHeader).isPresent();
    }

    private static String pad(String base64Url) {
        int remainder = base64Url.length() % 4;
        if (remainder == 0) {
            return base64Url;
        }
        return base64Url + "====".substring(remainder);
    }
}
