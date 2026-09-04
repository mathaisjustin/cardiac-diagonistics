package com.elsevier.cardiac_auth_service.service;

import com.elsevier.cardiac_auth_service.dto.ChangePasswordRequest;
import com.elsevier.cardiac_auth_service.dto.LoginRequest;
import com.elsevier.cardiac_auth_service.dto.LoginResponse;
import com.elsevier.cardiac_auth_service.dto.RefreshTokenRequest;
import com.elsevier.cardiac_auth_service.dto.RegisterRequest;
import com.elsevier.cardiac_auth_service.dto.UserRegisteredEvent;
import com.elsevier.cardiac_auth_service.entity.RefreshToken;
import com.elsevier.cardiac_auth_service.entity.User;
import com.elsevier.cardiac_auth_service.exception.EmailAlreadyExistsException;
import com.elsevier.cardiac_auth_service.exception.InvalidCredentialsException;
import com.elsevier.cardiac_auth_service.exception.InvalidPasswordException;
import com.elsevier.cardiac_auth_service.exception.InvalidRefreshTokenException;
import com.elsevier.cardiac_auth_service.exception.UserNotFoundException;
import com.elsevier.cardiac_auth_service.kafka.UserRegistrationProducer;
import com.elsevier.cardiac_auth_service.repository.UserRepository;
import com.elsevier.cardiac_auth_service.security.JwtService;
import com.elsevier.cardiac_auth_service.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRegistrationProducer userRegistrationProducer;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, passwordEncoder, jwtService,
                refreshTokenService, userRegistrationProducer);
    }

    // ---- register ----

    @Test
    void registerRejectsAnAlreadyUsedEmail() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                "jane@example.com", "Passw0rd1", "Jane", "Doe", "555-0100", "Cardiology");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(userRegistrationProducer, never()).publish(any());
    }

    @Test
    void registerHashesThePasswordSavesTheUserAndPublishesTheEvent() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd1")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("generated-id");
            return user;
        });

        RegisterRequest request = new RegisterRequest(
                "jane@example.com", "Passw0rd1", "Jane", "Doe", "555-0100", "Cardiology");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("jane@example.com");

        ArgumentCaptor<UserRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(userRegistrationProducer).publish(eventCaptor.capture());

        UserRegisteredEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo("generated-id");
        assertThat(event.firstName()).isEqualTo("Jane");
        assertThat(event.lastName()).isEqualTo("Doe");
        assertThat(event.contactNumber()).isEqualTo("555-0100");
        assertThat(event.department()).isEqualTo("Cardiology");
    }

    // ---- authenticate ----

    @Test
    void authenticateRejectsAnUnknownEmail() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("jane@example.com", "Passw0rd1");

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void authenticateRejectsAWrongPassword() {
        User user = existingUser();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void authenticateIssuesTokensOnAMatchingPassword() {
        User user = existingUser();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rd1", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getId(), user.getEmail()))
                .thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user.getId()))
                .thenReturn("refresh-token");

        LoginRequest request = new LoginRequest("jane@example.com", "Passw0rd1");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    // ---- refreshAccessToken ----

    @Test
    void refreshAccessTokenRotatesTheTokenPairForAValidRefreshToken() {
        User user = existingUser();

        RefreshToken oldToken = new RefreshToken();
        oldToken.setUserId(user.getId());

        when(refreshTokenService.validateRefreshToken("raw-refresh-token"))
                .thenReturn(oldToken);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user.getId(), user.getEmail()))
                .thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user.getId()))
                .thenReturn("new-refresh-token");

        LoginResponse response = authService.refreshAccessToken(
                new RefreshTokenRequest("raw-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refreshAccessTokenFailsWhenTheTokenBelongsToADeletedUser() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setUserId("gone-user");

        when(refreshTokenService.validateRefreshToken("raw-refresh-token"))
                .thenReturn(oldToken);
        when(userRepository.findById("gone-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(
                new RefreshTokenRequest("raw-refresh-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    // ---- changePassword ----

    @Test
    void changePasswordFailsWhenTheUserNoLongerExists() {
        when(userRepository.findById("user-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(
                "user-123", new ChangePasswordRequest("old", "new")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changePasswordRejectsAWrongOldPassword() {
        User user = existingUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-old", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                user.getId(), new ChangePasswordRequest("wrong-old", "new-pass")))
                .isInstanceOf(InvalidPasswordException.class);

        verify(userRepository, never()).save(any());
        verify(refreshTokenService, never()).revokeByUserId(any());
    }

    @Test
    void changePasswordUpdatesTheHashAndRevokesTheRefreshToken() {
        User user = existingUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rd1", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("NewPass1")).thenReturn("new-hashed-password");

        authService.changePassword(user.getId(), new ChangePasswordRequest("Passw0rd1", "NewPass1"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeByUserId(user.getId());
    }

    private User existingUser() {
        User user = new User();
        user.setId("user-123");
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed-Passw0rd1");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
