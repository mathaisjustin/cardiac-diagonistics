package com.elsevier.cardiac_user_profile_service.controller;

import com.elsevier.cardiac_user_profile_service.dto.GetProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.dto.ProfileResponseDto;
import com.elsevier.cardiac_user_profile_service.dto.UpdateProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.service.ProfileService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;


    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(

            @RequestHeader("X-User-Id") String userId,

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


    @PutMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(

            @RequestHeader("X-User-Id") String userId,

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