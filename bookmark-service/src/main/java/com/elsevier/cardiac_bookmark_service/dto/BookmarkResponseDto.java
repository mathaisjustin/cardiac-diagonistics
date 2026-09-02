package com.elsevier.cardiac_bookmark_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookmarkResponseDto implements Serializable {

    private String id;
    private String diagnosisId;
    private String gender;
    private Integer age;
    private String bp;
    private String painType;
    private String treatment;
    private LocalDateTime createdAt;


    public BookmarkResponseDto() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }


    public String getPainType() {
        return painType;
    }

    public void setPainType(String painType) {
        this.painType = painType;
    }


    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
