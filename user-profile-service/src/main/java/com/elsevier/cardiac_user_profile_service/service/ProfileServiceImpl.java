package com.elsevier.cardiac_user_profile_service.service;

import com.elsevier.cardiac_user_profile_service.dto.GetProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.dto.ProfileResponseDto;
import com.elsevier.cardiac_user_profile_service.dto.UpdateProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.entity.Profile;
import com.elsevier.cardiac_user_profile_service.exception.ProfileNotFoundException;
import com.elsevier.cardiac_user_profile_service.repository.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;


    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }


    @Override
    public ProfileResponseDto getProfile(
            GetProfileRequestDto requestDto
    ) {

        Profile profile = profileRepository
                .findById(requestDto.getUserId())
                .orElseThrow(() ->
                        new ProfileNotFoundException(
                                requestDto.getUserId()
                        )
                );


        ProfileResponseDto responseDto =
                new ProfileResponseDto();

        responseDto.setEmail(requestDto.getEmail());
        responseDto.setFirstName(profile.getFirstName());
        responseDto.setLastName(profile.getLastName());
        responseDto.setContact(profile.getContact());
        responseDto.setDepartment(profile.getDepartment());
        responseDto.setCreatedAt(profile.getCreatedAt());


        return responseDto;
    }


    @Override
    public ProfileResponseDto updateProfile(
            String userId,
            String email,
            UpdateProfileRequestDto requestDto
    ) {

        Profile profile = profileRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ProfileNotFoundException(userId)
                );


        profile.setFirstName(requestDto.getFirstName());
        profile.setLastName(requestDto.getLastName());
        profile.setContact(requestDto.getContact());
        profile.setDepartment(requestDto.getDepartment());


        Profile updatedProfile =
                profileRepository.save(profile);


        ProfileResponseDto responseDto =
                new ProfileResponseDto();

        responseDto.setEmail(email);
        responseDto.setFirstName(updatedProfile.getFirstName());
        responseDto.setLastName(updatedProfile.getLastName());
        responseDto.setContact(updatedProfile.getContact());
        responseDto.setDepartment(updatedProfile.getDepartment());
        responseDto.setCreatedAt(updatedProfile.getCreatedAt());


        return responseDto;
    }
}