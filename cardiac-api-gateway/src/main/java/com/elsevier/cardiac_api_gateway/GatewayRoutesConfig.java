package com.elsevier.cardiac_api_gateway;

import com.elsevier.cardiac_api_gateway.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    private final JwtService jwtService;

    public GatewayRoutesConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> authRoute =
                route("auth-service")
                        .route(path("/api/auth/**"), http())
                        .before(uri("http://localhost:8081"))
                        .build();

        RouterFunction<ServerResponse> profileRoute =
                route("user-profile-service")
                        .route(path("/api/profile/**"), http())
                        .before(uri("http://localhost:8080"))
                        .before(stripPrefix(1))
                        .filter(profileAuthenticationFilter())
                        .build();

        RouterFunction<ServerResponse> diagnosisRoute =
                route("diagnosis-service")
                        .route(path("/api/diagnosis/**"), http())
                        .before(uri("http://localhost:8083"))
                        .before(stripPrefix(1))
                        .build();

        RouterFunction<ServerResponse> bookmarkRoute =
                route("bookmark-service")
                        .route(path("/api/bookmarks/**"), http())
                        .before(uri("http://localhost:8082"))
                        .before(stripPrefix(1))
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
                Long userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);

                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .headers(headers -> {
                            headers.remove("X-User-Id");
                            headers.remove("X-User-Email");
                        })
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Email", email)
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
                Long userId = jwtService.extractUserId(token);

                ServerRequest modifiedRequest = ServerRequest.from(request)
                        .headers(headers -> {
                            headers.remove("X-User-Id");
                            headers.remove("X-User-Email");
                        })
                        .header("X-User-Id", String.valueOf(userId))
                        .build();

                return next.handle(modifiedRequest);

            } catch (Exception exception) {
                return unauthorized();
            }
        };
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