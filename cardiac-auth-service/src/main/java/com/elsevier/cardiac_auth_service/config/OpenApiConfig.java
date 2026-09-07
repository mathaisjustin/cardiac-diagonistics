package com.elsevier.cardiac_auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI metadata only - no routes, filters, or business behavior are
 * touched here. springdoc-openapi auto-generates the spec and UI from the
 * existing @RestController/@PostMapping/etc. annotations already on
 * AuthController; this bean just supplies the descriptive header
 * (title/version/description) shown at the top of that generated document.
 *
 * Docs are served at:
 *   /v3/api-docs        - raw OpenAPI JSON
 *   /swagger-ui/index.html - interactive Swagger UI
 */
@Configuration
public class OpenApiConfig {

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
                        .contact(new Contact().name("Cardiac Diagnostics Team")));
    }
}
