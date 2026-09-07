package com.elsevier.cardiac.diagnosis.service.config;

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
 * existing @RestController/@GetMapping/etc. annotations already on
 * DiagnosisController; this bean just supplies the descriptive header
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
 * It's "/api" rather than "/api/diagnosis" because DiagnosisController is
 * already @RequestMapping("/diagnosis") - that's already baked into every
 * documented operation, and the Gateway strips exactly one segment
 * ("/api") off /api/diagnosis/** before forwarding here. A server prefix
 * of "/api/diagnosis" would double up with the operation path (e.g.
 * /api/diagnosis/diagnosis/stats).
 *
 * Note: most of this service's routes are public or best-effort identity
 * (see per-endpoint descriptions) - the Authorize button is offered for the
 * routes that do require it (/search, /analysis, /{id}/bookmark).
 *
 * Docs are served at:
 *   /v3/api-docs        - raw OpenAPI JSON
 *   /swagger-ui/index.html - interactive Swagger UI
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI diagnosisServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cardiac Diagnosis Service API")
                        .description(
                                "Stateless service that proxies, filters, and analyzes cardiac "
                                        + "diagnosis records sourced from the external Diagnosis API. "
                                        + "In production this service sits behind the API Gateway, which "
                                        + "authenticates the caller and forwards their identity via the "
                                        + "X-User-Id / X-User-Email / X-Identity-Signature headers "
                                        + "documented on each endpoint below. Calling this service "
                                        + "directly (bypassing the Gateway) without those headers is "
                                        + "equivalent to an anonymous/guest request."
                        )
                        .version("v1")
                        .contact(new Contact().name("Cardiac Diagnostics Team")))
                .servers(List.of(new Server().url("/api")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by Auth Service's "
                                        + "/login here - the Gateway verifies it and forwards your "
                                        + "identity to this service.")));
    }
}
