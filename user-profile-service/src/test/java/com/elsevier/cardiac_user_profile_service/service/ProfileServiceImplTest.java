package com.elsevier.cardiac_user_profile_service.service;

import com.elsevier.cardiac_user_profile_service.dto.GetProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.dto.ProfileResponseDto;
import com.elsevier.cardiac_user_profile_service.dto.UpdateProfileRequestDto;
import com.elsevier.cardiac_user_profile_service.entity.Profile;
import com.elsevier.cardiac_user_profile_service.exception.ProfileNotFoundException;
import com.elsevier.cardiac_user_profile_service.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileServiceImpl(profileRepository);
    }

    @Test
    void getProfileReturnsTheStoredFieldsPlusTheEmailFromTheRequest() {
        Profile profile = existingProfile();
        when(profileRepository.findById("user-123")).thenReturn(Optional.of(profile));

        GetProfileRequestDto request = new GetProfileRequestDto();
        request.setUserId("user-123");
        request.setEmail("jane@example.com");

        ProfileResponseDto response = profileService.getProfile(request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getContact()).isEqualTo("555-0100");
        assertThat(response.getDepartment()).isEqualTo("Cardiology");
    }

    @Test
    void getProfileThrowsWhenNoProfileExistsYet() {
        when(profileRepository.findById("user-123")).thenReturn(Optional.empty());

        GetProfileRequestDto request = new GetProfileRequestDto();
        request.setUserId("user-123");
        request.setEmail("jane@example.com");

        assertThatThrownBy(() -> profileService.getProfile(request))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void updateProfileOverwritesTheEditableFieldsAndReturnsTheUpdatedValues() {
        Profile profile = existingProfile();
        when(profileRepository.findById("user-123")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequestDto request = new UpdateProfileRequestDto();
        request.setFirstName("Janet");
        request.setLastName("Doe");
        request.setContact("555-0101");
        request.setDepartment("Radiology");

        ProfileResponseDto response =
                profileService.updateProfile("user-123", "jane@example.com", request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getFirstName()).isEqualTo("Janet");
        assertThat(response.getContact()).isEqualTo("555-0101");
        assertThat(response.getDepartment()).isEqualTo("Radiology");

        // the same managed entity was mutated in place
        assertThat(profile.getFirstName()).isEqualTo("Janet");
    }

    @Test
    void updateProfileThrowsWhenNoProfileExistsYet() {
        when(profileRepository.findById("user-123")).thenReturn(Optional.empty());

        UpdateProfileRequestDto request = new UpdateProfileRequestDto();
        request.setFirstName("Janet");
        request.setLastName("Doe");
        request.setContact("555-0101");
        request.setDepartment("Radiology");

        assertThatThrownBy(() ->
                profileService.updateProfile("user-123", "jane@example.com", request))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    private Profile existingProfile() {
        Profile profile = new Profile();
        profile.setUserId("user-123");
        profile.setFirstName("Jane");
        profile.setLastName("Doe");
        profile.setContact("555-0100");
        profile.setDepartment("Cardiology");
        return profile;
    }
}
