package com.elsevier.cardiac_bookmark_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Every route in this service requires X-User-Id, so this filter runs before the controller on
 * every request: if X-User-Id is present, it must carry a valid X-Identity-Signature (see
 * IdentitySignatureVerifier) or the request is rejected outright - a request with no X-User-Id
 * at all is left alone, since the controller itself already returns 400 for that case.
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
