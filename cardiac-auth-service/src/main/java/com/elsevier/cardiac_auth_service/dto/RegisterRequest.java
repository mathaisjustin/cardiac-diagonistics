package com.elsevier.cardiac_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

        @Schema(description = "Unique account email", example = "jane.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Plaintext password - hashed on the server before storage")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "First name", example = "Jane")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Contact phone number", example = "+1-555-0100")
        @NotBlank(message = "Contact number is required")
        String contactNumber,

        @Schema(description = "Department/team", example = "Cardiology")
        @NotBlank(message = "Department is required")
        String department
) {
}