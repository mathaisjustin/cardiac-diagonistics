package com.elsevier.cardiac_user_profile_service.consumer;

import com.elsevier.cardiac_user_profile_service.entity.Profile;
import com.elsevier.cardiac_user_profile_service.exception.InvalidUserProfileEventException;
import com.elsevier.cardiac_user_profile_service.exception.ProfilePersistenceException;
import com.elsevier.cardiac_user_profile_service.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileConsumerTest {

    @Mock
    private ProfileRepository profileRepository;

    private UserProfileConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserProfileConsumer(profileRepository, JsonMapper.builder().build());
    }

    @Test
    void createsAProfileFromAWellFormedEvent() {
        String message = """
                {
                  "userId": "user-123",
                  "email": "jane@example.com",
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "contactNumber": "555-0100",
                  "department": "Cardiology"
                }
                """;

        when(profileRepository.existsById("user-123")).thenReturn(false);

        consumer.consumeUserProfile(message);

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(captor.capture());

        Profile saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.getFirstName()).isEqualTo("Jane");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getContact()).isEqualTo("555-0100");
        assertThat(saved.getDepartment()).isEqualTo("Cardiology");
    }

    @Test
    void isIdempotentForAUserThatAlreadyHasAProfile() {
        String message = """
                {"userId": "user-123", "email": "jane@example.com",
                 "firstName": "Jane", "lastName": "Doe",
                 "contactNumber": "555-0100", "department": "Cardiology"}
                """;

        when(profileRepository.existsById("user-123")).thenReturn(true);

        consumer.consumeUserProfile(message);

        verify(profileRepository, never()).save(any());
    }

    @Test
    void throwsOnMalformedJsonWithoutTouchingTheRepository() {
        assertThatThrownBy(() -> consumer.consumeUserProfile("not valid json"))
                .isInstanceOf(InvalidUserProfileEventException.class);

        verify(profileRepository, never()).existsById(any());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void wrapsADatabaseFailureAsAProfilePersistenceException() {
        String message = """
                {"userId": "user-123", "email": "jane@example.com",
                 "firstName": "Jane", "lastName": "Doe",
                 "contactNumber": "555-0100", "department": "Cardiology"}
                """;

        when(profileRepository.existsById("user-123")).thenReturn(false);
        when(profileRepository.save(any(Profile.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> consumer.consumeUserProfile(message))
                .isInstanceOf(ProfilePersistenceException.class);
    }
}
