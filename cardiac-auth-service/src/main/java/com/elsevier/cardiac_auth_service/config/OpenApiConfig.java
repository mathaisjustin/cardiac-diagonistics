package com.elsevier.cardiac_auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI metadata only - no routes, filters, or business behavior are
 * touched here. springdoc-openapi auto-generates the spec and UI from the
 * existing @RestController/@PostMapping/etc. annotations already on
 * AuthController; this bean just supplies the descriptive header
 * (title/version/description), a fixed gateway-relative server URL, and a
 * shared JWT Authorize scheme (see docsRoutes() in the Gateway's
 * GatewayRoutesConfig - all 4 services' docs use the same "bearerAuth"
 * scheme name so the Authorize flow looks and works identically no matter
 * which service's tab you're on in the aggregated Swagger UI). Only
 * change-password actually requires it here (see its
 * @SecurityRequirement("bearerAuth") on AuthController) - register/login/
 * refresh/logout are public, so no blanket requirement is set on the spec
 * as a whole.
 *
 * The explicit server URL below is required: without it, springdoc infers
 * the server address from whatever request context it sees, which - since
 * the Gateway fetches this doc server-side over the Docker network - ends
 * up being this container's internal IP (unreachable from your browser).
 * A relative URL is resolved by Swagger UI against the page's own origin
 * (the Gateway), so "Try it out" calls route back through the Gateway.
 *
 * Docs are served at:
 *   /v3/api-docs        - raw OpenAPI JSON
 *   /swagger-ui/index.html - interactive Swagger UI
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cardiac Auth Service API")
                        .description(
                                "Handles user registration, login, and JWT/refresh-token issuance "
                                        + "for the Cardiac Diagnostics System. In production this service "
                                        + "sits behind the API Gateway, which forwards client requests to "
                                        + "it directly on /api/auth/**."
                        )
                        .version("v1")
                        .contact(new Contact().name("Cardiac Diagnostics Team")))
                .servers(List.of(new Server().url("/api/auth")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by /login here.")));
    }
}
