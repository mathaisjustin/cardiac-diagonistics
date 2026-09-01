package com.elsevier.cardiac_bookmark_service.event;

/**
 * JSON structure carried on the {@code bookmark.created} Kafka topic. Mirrors the
 * bookmark-creation request a client would otherwise make synchronously, but delivered
 * asynchronously: the diagnosis record has already been resolved by the time this event
 * is published, so the payload is a snapshot rather than an ID to re-fetch.
 */
public class BookmarkEvent {

    private String userId;
    private String diagnosisId;
    private DiagnosisPayload payload;


    public BookmarkEvent() {
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }


    public DiagnosisPayload getPayload() {
        return payload;
    }

    public void setPayload(DiagnosisPayload payload) {
        this.payload = payload;
    }
}
