package com.elsevier.cardiac_user_profile_service.controller;

import com.elsevier.cardiac_user_profile_service.dto.GetProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.dto.ProfileResponseDto;
import com.elsevier.cardiac_user_profile_service.dto.UpdateProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;


// Identity: the API Gateway verifies the JWT and forwards identity downstream
// via the X-User-Id / X-User-Email headers - this service never sees or
// decodes a token itself, it just trusts those headers' presence and value.
@RestController
@RequestMapping("/profile")
@Tag(
        name = "Profile",
        description = "Read and update the authenticated user's profile. Both endpoints "
                + "require identity to be forwarded by the API Gateway via the X-User-Id / "
                + "X-User-Email headers."
)
public class ProfileController {

    private final ProfileService profileService;


    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @Operation(
            summary = "Get the authenticated user's profile",
            description = "Looks up the profile for the caller identified by the X-User-Id / "
                    + "X-User-Email headers forwarded by the Gateway."
    )
            @ApiResponse(responseCode = "200", description = "Profile found",
                    content = @Content(schema = @Schema(implementation = ProfileResponseDto.class)))
            @ApiResponse(responseCode = "404", description = "No profile for this user", content = @Content)
    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(

            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Email") String email
    ) {

        GetProfileRequestDto requestDto =
                new GetProfileRequestDto();

        requestDto.setUserId(userId);
        requestDto.setEmail(email);


        ProfileResponseDto responseDto =
                profileService.getProfile(requestDto);


        return ResponseEntity.ok(responseDto);
    }


    @Operation(
            summary = "Update the authenticated user's profile",
            description = "Updates the profile for the caller identified by the X-User-Id / "
                    + "X-User-Email headers forwarded by the Gateway."
    )
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = ProfileResponseDto.class)))
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
            @ApiResponse(responseCode = "404", description = "No profile for this user", content = @Content)
    @PutMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(

            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Email") String email,

            @Valid @RequestBody UpdateProfileRequestDto requestDto
    ) {

        ProfileResponseDto responseDto =
                profileService.updateProfile(
                        userId,
                        email,
                        requestDto
                );


        return ResponseEntity.ok(responseDto);
    }
}
