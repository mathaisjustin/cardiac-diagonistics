package com.elsevier.cardiac_api_gateway;

import com.elsevier.cardiac_api_gateway.security.IdentitySigner;
import com.elsevier.cardiac_api_gateway.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    // Eureka app names (spring.application.name, uppercased) - resolved to a live instance
    // per-request by the lb(serviceId) filter (spring-cloud-starter-loadbalancer on the
    // classpath), not a static address.
    private static final String AUTH_SERVICE_ID = "CARDIAC-AUTH-SERVICE";
    private static final String PROFILE_SERVICE_ID = "CARDIAC-USER-PROFILE-SERVICE";
    private static final String DIAGNOSIS_SERVICE_ID = "CARDIAC-DIAGNOSIS-SERVICE";
    private static final String BOOKMARK_SERVICE_ID = "CARDIAC-BOOKMARK-SERVICE";

    private final JwtService jwtService;
    private final IdentitySigner identitySigner;

    public GatewayRoutesConfig(JwtService jwtService, IdentitySigner identitySigner) {
        this.jwtService = jwtService;
        this.identitySigner = identitySigner;
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> authRoute =
                route("auth-service")
                        .route(path("/api/auth/**"), http())
                        .filter(lb(AUTH_SERVICE_ID))
                        .build();

        RouterFunction<ServerResponse> profileRoute =
                route("user-profile-service")
                        .route(path("/api/profile/**"), http())
                        .before(stripPrefix(1))
                        .filter(lb(PROFILE_SERVICE_ID))
                        .filter(profileAuthenticationFilter())
                        .build();

        RouterFunction<ServerResponse> diagnosisRoute =
                route("diagnosis-service")
                        .route(path("/api/diagnosis/**"), http())
                        .before(stripPrefix(1))
                        .filter(lb(DIAGNOSIS_SERVICE_ID))
                        .filter(optionalIdentityFilter())
                        .build();

        RouterFunction<ServerResponse> bookmarkRoute =
                route("bookmark-service")
                        .route(path("/api/bookmarks/**"), http())
                        .before(stripPrefix(1))
                        .filter(lb(BOOKMARK_SERVICE_ID))
                        .filter(bookmarkAuthenticationFilter())
                        .build();

        return authRoute
                .and(profileRoute)
                .and(diagnosisRoute)
                .and(bookmarkRoute);
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse>
    profileAuthenticationFilter() {

        return (request, next) -> {

            String token = extractToken(request);

            if (token == null || !jwtService.isTokenValid(token)) {
                return unauthorized();
            }

            try {
                String userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);
                String signature = identitySigner.sign(userId, email);

                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .headers(GatewayRoutesConfig::stripIdentityHeaders)
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-Identity-Signature", signature)
                        .build();

                return next.handle(modifiedRequest);

            } catch (Exception exception) {
                return unauthorized();
            }
        };
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse>
    bookmarkAuthenticationFilter() {

        return (request, next) -> {

            String token = extractToken(request);

            if (token == null || !jwtService.isTokenValid(token)) {
                return unauthorized();
            }

            try {
                String userId = jwtService.extractUserId(token);
                String signature = identitySigner.sign(userId, "");

                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .headers(GatewayRoutesConfig::stripIdentityHeaders)
                        .header("X-User-Id", userId)
                        .header("X-Identity-Signature", signature)
                        .build();

                return next.handle(modifiedRequest);

            } catch (Exception exception) {
                return unauthorized();
            }
        };
    }

    /**
     * Diagnosis Service's own routes decide public vs protected per-route (some are fully
     * public, some require X-User-Id, GET /diagnosis/{id} changes response shape based on
     * whether it's present). So this filter never rejects a request - it only attaches a
     * signed X-User-Id if a valid token happens to be present, and passes the request through
     * unmodified (no identity headers) otherwise. Any client-supplied identity headers are
     * still stripped either way, so they're never trusted from outside the Gateway.
     */
    private HandlerFilterFunction<ServerResponse, ServerResponse>
    optionalIdentityFilter() {

        return (request, next) -> {

            String token = extractToken(request);

            if (token == null || !jwtService.isTokenValid(token)) {
                ServerRequest strippedRequest = ServerRequest.from(request)
                        .headers(GatewayRoutesConfig::stripIdentityHeaders)
                        .build();

                return next.handle(strippedRequest);
            }

            try {
                String userId = jwtService.extractUserId(token);
                String signature = identitySigner.sign(userId, "");

                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .headers(GatewayRoutesConfig::stripIdentityHeaders)
                        .header("X-User-Id", userId)
                        .header("X-Identity-Signature", signature)
                        .build();

                return next.handle(modifiedRequest);

            } catch (Exception exception) {
                ServerRequest strippedRequest = ServerRequest.from(request)
                        .headers(GatewayRoutesConfig::stripIdentityHeaders)
                        .build();

                return next.handle(strippedRequest);
            }
        };
    }

    private static void stripIdentityHeaders(org.springframework.http.HttpHeaders headers) {
        headers.remove("X-User-Id");
        headers.remove("X-User-Email");
        headers.remove("X-Identity-Signature");
    }

    private String extractToken(ServerRequest request) {

        String authorization =
                request.headers().firstHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }

    private ServerResponse unauthorized() {

        return ServerResponse
                .status(401)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Unauthorized\"}");
    }
}