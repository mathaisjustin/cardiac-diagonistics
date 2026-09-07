package com.elsevier.cardiac_bookmark_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookmarkResponseDto implements Serializable {

    @Schema(description = "Bookmark id")
    private String id;
    @Schema(description = "Id of the bookmarked diagnosis record", example = "1")
    private String diagnosisId;
    @Schema(description = "Gender from the diagnosis record", example = "Male")
    private String gender;
    @Schema(description = "Age from the diagnosis record", example = "54")
    private Integer age;
    @Schema(description = "Blood pressure from the diagnosis record", example = "130")
    private String bp;
    @Schema(description = "Pain type from the diagnosis record", example = "Typical Angina")
    private String painType;
    @Schema(description = "Treatment from the diagnosis record")
    private String treatment;
    @Schema(description = "When the bookmark was saved")
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
