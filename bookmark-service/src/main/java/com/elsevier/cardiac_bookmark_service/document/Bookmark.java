package com.elsevier.cardiac_bookmark_service.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "bookmarks")
@CompoundIndex(name = "user_diagnosis_unique", def = "{'userId': 1, 'diagnosisId': 1}", unique = true)
public class Bookmark {

    @Id
    private String id;

    private String userId;
    private String diagnosisId;

    private String gender;
    private Integer age;
    private String bp;
    private String painType;
    private String treatment;

    private LocalDateTime createdAt;


    public Bookmark() {
    }


    public static Bookmark newBookmark(String userId, String diagnosisId) {

        Bookmark bookmark = new Bookmark();

        bookmark.id = UUID.randomUUID().toString();
        bookmark.userId = userId;
        bookmark.diagnosisId = diagnosisId;
        bookmark.createdAt = LocalDateTime.now();

        return bookmark;
    }


    public String getId() {
        return id;
    }


    public String getUserId() {
        return userId;
    }


    public String getDiagnosisId() {
        return diagnosisId;
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
}
