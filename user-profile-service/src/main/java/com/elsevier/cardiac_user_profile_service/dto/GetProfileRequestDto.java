package com.elsevier.cardiac_user_profile_service.dto;

public class GetProfileRequestDto {

    private String userId;
    private String email;


    public GetProfileRequestDto() {
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}