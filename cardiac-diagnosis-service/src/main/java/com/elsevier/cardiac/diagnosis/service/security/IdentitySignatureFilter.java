package com.elsevier.cardiac.diagnosis.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This service's routes are a mix of public, optional-identity, and identity-required - that
 * per-route decision is made by DiagnosisController itself (via @RequestHeader(required =
 * false)), not here. This filter only guards against a spoofed X-User-Id: if the header is
 * present, it must carry a valid X-Identity-Signature (see IdentitySignatureVerifier) or the
 * request is rejected outright before it ever reaches the controller. A request with no
 * X-User-Id at all is passed through untouched - the controller decides whether that's allowed
 * for the route being called.
 */
@Component
public class IdentitySignatureFilter extends OncePerRequestFilter {

    private final IdentitySignatureVerifier verifier;

    public IdentitySignatureFilter(IdentitySignatureVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        if (userId != null) {
            String email = request.getHeader("X-User-Email");
            String signature = request.getHeader("X-Identity-Signature");

            if (!verifier.isValid(userId, email, signature)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Invalid identity signature\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
