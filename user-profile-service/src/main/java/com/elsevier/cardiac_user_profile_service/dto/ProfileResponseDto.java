package com.elsevier.cardiac_user_profile_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class ProfileResponseDto {

    @Schema(description = "Account email", example = "jane.doe@example.com")
    private String email;
    @Schema(description = "First name", example = "Jane")
    private String firstName;
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    @Schema(description = "Contact phone number", example = "+1-555-0100")
    private String contact;
    @Schema(description = "Department/team", example = "Cardiology")
    private String department;
    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;


    // Required by Jackson for deserialization.
    public ProfileResponseDto() {
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}