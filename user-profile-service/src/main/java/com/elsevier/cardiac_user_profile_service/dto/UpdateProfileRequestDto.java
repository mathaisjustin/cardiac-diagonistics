package com.elsevier.cardiac_user_profile_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequestDto {

    @Schema(description = "First name", example = "Jane")
    @NotBlank(message = "First name must not be empty")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank(message = "Last name must not be empty")
    private String lastName;

    @Schema(description = "Contact phone number", example = "+1-555-0100")
    @NotBlank(message = "Contact must not be empty")
    private String contact;

    @Schema(description = "Department/team", example = "Cardiology")
    @NotBlank(message = "Department must not be empty")
    private String department;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}