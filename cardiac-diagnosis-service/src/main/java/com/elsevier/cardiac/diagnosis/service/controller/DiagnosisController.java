package com.elsevier.cardiac.diagnosis.service.controller;

import com.elsevier.cardiac.diagnosis.service.dto.AdvancedSearchRequest;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResult;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisListItem;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisPublicDetail;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisStats;
import com.elsevier.cardiac.diagnosis.service.exception.UnauthorizedException;
import com.elsevier.cardiac.diagnosis.service.exception.ValidationException;
import com.elsevier.cardiac.diagnosis.service.service.DiagnosisService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Identity: the API Gateway verifies the JWT, parses it, and forwards identity
// downstream via the X-User-Id header - this service never sees or decodes a
// token itself, it just trusts that header's presence (and value).
@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    private static final Set<String> VALID_GENDERS = Set.of("Male", "Female");

    // Matches the external Diagnosis API's real casing ("Non-anginal Pain", lowercase "a") -
    // validated case-insensitively below so this can't drift out of sync again.
    private static final Set<String> VALID_PAIN_TYPES = Set.of(
            "Typical Angina", "Atypical Angina", "Non-anginal Pain", "Asymptomatic"
    );

    private static final Set<String> VALID_CHARACTERISTICS = Set.of("age", "gender", "painType");

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    // GET /diagnosis
    @GetMapping
    public List<DiagnosisListItem> getAllDiagnoses() {

        return diagnosisService.getAllDiagnoses()
                .stream()
                .map(DiagnosisListItem::new)
                .collect(Collectors.toList());
    }

    // GET /diagnosis/stats
    // Public - landing page summary. Total records, mean age, surgery share,
    // and a fresh random 3-record sample (gender/age/painType only) each call.
    @GetMapping("/stats")
    public DiagnosisStats stats() {
        return diagnosisService.getPublicStats();
    }

    // GET /diagnosis/search?gender=Male&painType=Typical%20Angina&ageMin=40&ageMax=60&bpMin=120&bpMax=150
    // Registered users only.
    @GetMapping("/search")
    public List<Diagnosis> search(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String painType,
            @RequestParam(required = false) Integer ageMin,
            @RequestParam(required = false) Integer ageMax,
            @RequestParam(required = false) Integer bpMin,
            @RequestParam(required = false) Integer bpMax) {

        if (userId == null) {
            throw new UnauthorizedException("Advanced search requires you to be logged in");
        }

        if (gender == null && painType == null && ageMin == null
                && ageMax == null && bpMin == null && bpMax == null) {
            throw new ValidationException("At least one search filter is required");
        }

        if (gender != null && !containsIgnoreCase(VALID_GENDERS, gender)) {
            throw new ValidationException("gender must be one of: " + VALID_GENDERS);
        }

        if (painType != null && !containsIgnoreCase(VALID_PAIN_TYPES, painType)) {
            throw new ValidationException("painType must be one of: " + VALID_PAIN_TYPES);
        }

        if (ageMin != null && ageMax != null && ageMin > ageMax) {
            throw new ValidationException("ageMin cannot be greater than ageMax");
        }

        if (bpMin != null && bpMax != null && bpMin > bpMax) {
            throw new ValidationException("bpMin cannot be greater than bpMax");
        }

        AdvancedSearchRequest request = new AdvancedSearchRequest();

        request.setGender(gender);
        request.setPainType(painType);
        request.setAgeMin(ageMin);
        request.setAgeMax(ageMax);
        request.setBpMin(bpMin);
        request.setBpMax(bpMax);

        return diagnosisService.advancedSearch(request);
    }

    // GET /diagnosis/analysis?by=age|gender|painType
    // Registered users only. Always runs against the full dataset -
    // not affected by any search filters.
    @GetMapping("/analysis")
    public AnalysisResult analyze(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String by) {

        if (userId == null) {
            throw new UnauthorizedException("Treatment analysis requires you to be logged in");
        }

        if (by == null || !VALID_CHARACTERISTICS.contains(by)) {
            throw new ValidationException("by must be one of: " + VALID_CHARACTERISTICS);
        }

        return diagnosisService.analyzeByCharacteristic(by);
    }

    // POST /diagnosis/{id}/bookmark
    // Registered users only. Publishes a BookmarkEvent to Kafka for Bookmark
    // Service to consume and save - this route doesn't touch a database itself.
    @PostMapping("/{id}/bookmark")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> bookmark(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            throw new UnauthorizedException("Bookmarking requires you to be logged in");
        }

        diagnosisService.bookmarkDiagnosis(id, userId);

        return Map.of(
                "message", "Bookmark request submitted",
                "diagnosisId", id
        );
    }

    // GET /diagnosis/{id}
    @GetMapping("/{id}")
    public Object getDiagnosisById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Diagnosis diagnosis = diagnosisService.getDiagnosisById(id);

        if (userId != null) {
            return diagnosis;
        }

        return new DiagnosisPublicDetail(diagnosis);
    }

    private boolean containsIgnoreCase(Set<String> values, String candidate) {

        return values.stream()
                .anyMatch(value -> value.equalsIgnoreCase(candidate));
    }
}
