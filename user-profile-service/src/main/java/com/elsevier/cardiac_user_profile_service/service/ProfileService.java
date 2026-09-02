package com.elsevier.cardiac_user_profile_service.service;

import com.elsevier.cardiac_user_profile_service.dto.GetProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.dto.ProfileResponseDto;
import com.elsevier.cardiac_user_profile_service.dto.UpdateProfileRequestDto;

public interface ProfileService {

    ProfileResponseDto getProfile(
            GetProfileRequestDto requestDto
    );

    ProfileResponseDto updateProfile(
            String userId,
            String email,
            UpdateProfileRequestDto requestDto
    );
}