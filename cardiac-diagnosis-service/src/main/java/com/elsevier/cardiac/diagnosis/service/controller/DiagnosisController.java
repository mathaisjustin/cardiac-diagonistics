package com.elsevier.cardiac.diagnosis.service.controller;

import com.elsevier.cardiac.diagnosis.service.dto.AnalysisResponse;
import com.elsevier.cardiac.diagnosis.service.dto.Diagnosis;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisListItem;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisPublicDetail;
import com.elsevier.cardiac.diagnosis.service.dto.DiagnosisSearchRequest;
import com.elsevier.cardiac.diagnosis.service.security.JwtPayloadReader;
import com.elsevier.cardiac.diagnosis.service.service.DiagnosisService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

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

    // GET /diagnosis/search?gender=Male&age=67&bp=140&painType=Typical%20Angina
    @GetMapping("/search")
    public List<Diagnosis> search(
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

        return diagnosisService.search(request);
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