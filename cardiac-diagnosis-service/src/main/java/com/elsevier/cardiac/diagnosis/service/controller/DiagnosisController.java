package com.elsevier.cardiac.diagnosis.service.controller;

import com.elsevier.cardiac.diagnosis.service.dto.AdvancedSearchRequest;
import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResponse;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisListItem;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisPublicDetail;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisSearchRequest;
import com.elsevier.cardiac.diagnosis.service.exception.UnauthorizedException;
import com.elsevier.cardiac.diagnosis.service.exception.ValidationException;
import com.elsevier.cardiac.diagnosis.service.security.JwtPayloadReader;
import com.elsevier.cardiac.diagnosis.service.service.DiagnosisService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    private static final Set<String> VALID_GENDERS = Set.of("Male", "Female");

    private static final Set<String> VALID_PAIN_TYPES = Set.of(
            "Typical Angina", "Atypical Angina", "Non-Anginal Pain", "Asymptomatic"
    );

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

    // POST /diagnosis
    @PostMapping
    public Diagnosis createDiagnosis(
            @RequestBody Diagnosis diagnosis) {

        return diagnosisService.createDiagnosis(diagnosis);
    }

    // GET /diagnosis/search?gender=Male&painType=Typical%20Angina&ageMin=40&ageMax=60&bpMin=120&bpMax=150
    // Registered users only.
    @GetMapping("/search")
    public List<Diagnosis> search(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String painType,
            @RequestParam(required = false) Integer ageMin,
            @RequestParam(required = false) Integer ageMax,
            @RequestParam(required = false) Integer bpMin,
            @RequestParam(required = false) Integer bpMax) {

        if (!JwtPayloadReader.isAuthenticated(authorization)) {
            throw new UnauthorizedException("Advanced search requires you to be logged in");
        }

        if (gender == null && painType == null && ageMin == null
                && ageMax == null && bpMin == null && bpMax == null) {
            throw new ValidationException("At least one search filter is required");
        }

        if (gender != null && !VALID_GENDERS.contains(gender)) {
            throw new ValidationException("gender must be one of: " + VALID_GENDERS);
        }

        if (painType != null && !VALID_PAIN_TYPES.contains(painType)) {
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

    // GET /diagnosis/analysis/treatment?gender=Male&age=67&bp=140&painType=Typical%20Angina
    @GetMapping("/analysis/treatment")
    public AnalysisResponse analyzeTreatment(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Integer bp,
            @RequestParam(required = false) String painType) {

        DiagnosisSearchRequest request =
                new DiagnosisSearchRequest();

        request.setGender(gender);
        request.setAge(age);
        request.setBp(bp);
        request.setPainType(painType);

        return diagnosisService.analyzeTreatment(request);
    }

    // GET /diagnosis/validate/{id}
    @GetMapping("/validate/{id}")
    public Map<String, Boolean> validateDiagnosis(
            @PathVariable String id) {

        boolean exists =
                diagnosisService.diagnosisExists(id);

        return Map.of("exists", exists);
    }

    // GET /diagnosis/{id}
    @GetMapping("/{id}")
    public Object getDiagnosisById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Diagnosis diagnosis = diagnosisService.getDiagnosisById(id);

        if (JwtPayloadReader.isAuthenticated(authorization)) {
            return diagnosis;
        }

        return new DiagnosisPublicDetail(diagnosis);
    }
}