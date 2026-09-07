package com.elsevier.cardiac_user_profile_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI metadata only - no routes, filters, or business behavior are
 * touched here. springdoc-openapi auto-generates the spec and UI from the
 * existing @RestController/@GetMapping/etc. annotations already on
 * ProfileController; this bean just supplies the descriptive header
 * (title/version/description), a fixed gateway-relative server URL, and a
 * shared JWT Authorize scheme (same "bearerAuth" name used by every
 * service's docs, so the Authorize flow looks and works identically across
 * tabs in the aggregated Swagger UI).
 *
 * The explicit server URL below is required: without it, springdoc infers
 * the server address from whatever request context it sees, which - since
 * the Gateway fetches this doc server-side over the Docker network - ends
 * up being this container's internal IP (unreachable from your browser).
 * A relative URL is resolved by Swagger UI against the page's own origin
 * (the Gateway), so "Try it out" calls route back through the Gateway.
 *
 * Note: this service itself expects identity via X-User-Id/X-User-Email/
 * X-Identity-Signature (set by the Gateway, not a raw JWT) - the Authorize
 * button here documents the JWT you'd present to the Gateway, which is
 * what actually produces those headers for you.
 *
 * Docs are served at:
 *   /v3/api-docs        - raw OpenAPI JSON
 *   /swagger-ui/index.html - interactive Swagger UI
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI userProfileServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cardiac User Profile Service API")
                        .description(
                                "Manages user profile data for the Cardiac Diagnostics System. "
                                        + "In production this service sits behind the API Gateway, which "
                                        + "authenticates the caller and forwards their identity via the "
                                        + "X-User-Id / X-User-Email / X-Identity-Signature headers "
                                        + "documented on each endpoint below. Calling this service "
                                        + "directly (bypassing the Gateway) without those headers is "
                                        + "equivalent to an anonymous/guest request."
                        )
                        .version("v1")
                        .contact(new Contact().name("Cardiac Diagnostics Team")))
                .servers(List.of(new Server().url("/api/profile")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by Auth Service's "
                                        + "/login here - the Gateway verifies it and forwards your "
                                        + "identity to this service.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
