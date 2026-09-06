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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(
        name = "Diagnosis",
        description = "Browse, search, and analyze cardiac diagnosis records. Some routes are "
                + "public (guest) and some require the caller's identity to be forwarded by the "
                + "API Gateway via the X-User-Id header - each endpoint below states which."
)
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
    @Operation(
            summary = "List all diagnosis records",
            description = "Returns every diagnosis record in summary form (id, gender, age, "
                    + "pain type - treatment and full vitals are omitted here; see the "
                    + "single-record endpoint for those). Public - no identity header required."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of diagnosis summaries",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                    schema = @Schema(implementation = DiagnosisListItem.class)))
    )
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
    @Operation(
            summary = "Get public dataset summary",
            description = "Landing-page summary for anonymous visitors: total record count, "
                    + "mean age, share of cases resulting in surgery, and a fresh random "
                    + "3-record sample (gender/age/pain type only - no treatment or vitals) "
                    + "generated on every call. Public - no identity header required."
    )
    @ApiResponse(responseCode = "200", description = "Aggregate stats and a random sample",
            content = @Content(schema = @Schema(implementation = DiagnosisStats.class)))
    @GetMapping("/stats")
    public DiagnosisStats stats() {
        return diagnosisService.getPublicStats();
    }

    // GET /diagnosis/search?gender=Male&painType=Typical%20Angina&ageMin=40&ageMax=60&bpMin=120&bpMax=150
    // Registered users only.
    @Operation(
            summary = "Search diagnosis records by filter",
            description = "Filters the full dataset by any combination of gender, pain type, "
                    + "age range, and blood-pressure range. At least one filter is required. "
                    + "Requires the caller to be authenticated (X-User-Id forwarded by the "
                    + "Gateway) - returns 401 for anonymous callers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching diagnosis records",
                    content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                            schema = @Schema(implementation = Diagnosis.class)))),
            @ApiResponse(responseCode = "400", description = "No filter supplied, or an invalid "
                    + "gender/painType/range value", content = @Content),
            @ApiResponse(responseCode = "401", description = "X-User-Id header missing - login required",
                    content = @Content)
    })
    @GetMapping("/search")
    public List<Diagnosis> search(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Parameter(description = "Exact match, case-insensitive", example = "Male")
            @RequestParam(required = false) String gender,
            @Parameter(description = "Exact match, case-insensitive", example = "Typical Angina")
            @RequestParam(required = false) String painType,
            @Parameter(description = "Minimum age (inclusive)", example = "40")
            @RequestParam(required = false) Integer ageMin,
            @Parameter(description = "Maximum age (inclusive)", example = "60")
            @RequestParam(required = false) Integer ageMax,
            @Parameter(description = "Minimum blood pressure (inclusive)", example = "120")
            @RequestParam(required = false) Integer bpMin,
            @Parameter(description = "Maximum blood pressure (inclusive)", example = "150")
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
    @Operation(
            summary = "Treatment analysis by characteristic",
            description = "Groups the full dataset by the requested characteristic (age, "
                    + "gender, or pain type) and returns treatment counts/percentages per group, "
                    + "plus the dominant treatment overall. Always runs against the full "
                    + "dataset - unaffected by any /search filters. Requires the caller to be "
                    + "authenticated (X-User-Id forwarded by the Gateway) - returns 401 for "
                    + "anonymous callers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Treatment breakdown by group",
                    content = @Content(schema = @Schema(implementation = AnalysisResult.class))),
            @ApiResponse(responseCode = "400", description = "by must be one of: age, gender, painType",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "X-User-Id header missing - login required",
                    content = @Content)
    })
    @GetMapping("/analysis")
    public AnalysisResult analyze(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Parameter(description = "Characteristic to group by", example = "age",
                    schema = @Schema(allowableValues = {"age", "gender", "painType"}))
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
    @Operation(
            summary = "Bookmark a diagnosis record",
            description = "Publishes a bookmark-requested event to Kafka for Bookmark Service "
                    + "to consume and persist asynchronously - this endpoint does not write to "
                    + "a database itself, so a 202 response means the request was accepted, not "
                    + "that the bookmark is saved yet. Requires the caller to be authenticated "
                    + "(X-User-Id forwarded by the Gateway) - returns 401 for anonymous callers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Bookmark request accepted for async processing"),
            @ApiResponse(responseCode = "401", description = "X-User-Id header missing - login required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No diagnosis record with the given id",
                    content = @Content)
    })
    @PostMapping("/{id}/bookmark")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> bookmark(
            @Parameter(description = "Diagnosis record id", example = "1", required = true)
            @PathVariable String id,
            @Parameter(hidden = true)
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
    @Operation(
            summary = "Get a single diagnosis record",
            description = "Returns the full record (including treatment) if the caller is "
                    + "authenticated (X-User-Id forwarded by the Gateway); returns the public "
                    + "view (treatment omitted) for anonymous callers. Public - no identity "
                    + "header required, but the response shape differs based on it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnosis record - full detail when "
                    + "authenticated, public detail (no treatment) when anonymous",
                    content = @Content(schema = @Schema(oneOf = {Diagnosis.class, DiagnosisPublicDetail.class}))),
            @ApiResponse(responseCode = "404", description = "No diagnosis record with the given id",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public Object getDiagnosisById(
            @Parameter(description = "Diagnosis record id", example = "1", required = true)
            @PathVariable String id,
            @Parameter(hidden = true)
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
