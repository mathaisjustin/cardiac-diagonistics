package com.elsevier.cardiac.diagnosis.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI metadata only - no routes, filters, or business behavior are
 * touched here. springdoc-openapi auto-generates the spec and UI from the
 * existing @RestController/@GetMapping/etc. annotations already on
 * DiagnosisController; this bean just supplies the descriptive header
 * (title/version/description) shown at the top of that generated document.
 *
 * Docs are served at:
 *   /v3/api-docs        - raw OpenAPI JSON
 *   /swagger-ui/index.html - interactive Swagger UI
 */
@Configuration
public class OpenApiConfig {

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
                        .contact(new Contact().name("Cardiac Diagnostics Team")));
    }
}
