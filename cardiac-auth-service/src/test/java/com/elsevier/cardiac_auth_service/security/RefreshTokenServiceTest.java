package com.elsevier.cardiac_auth_service.security;

import com.elsevier.cardiac_auth_service.entity.RefreshToken;
import com.elsevier.cardiac_auth_service.exception.InvalidRefreshTokenException;
import com.elsevier.cardiac_auth_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long EXPIRATION_MS = 604800000L; // 7 days

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, EXPIRATION_MS);
    }

    @Test
    void createRefreshTokenDeletesAnyExistingTokenForTheUserFirst() {
        refreshTokenService.createRefreshToken("user-123");

        verify(refreshTokenRepository).deleteByUserId("user-123");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshTokenNeverStoresThePlaintextToken() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        String rawToken = refreshTokenService.createRefreshToken("user-123");

        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(saved.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.isRevoked()).isFalse();
    }

    @Test
    void validateRefreshTokenSucceedsForAFreshToken() {
        RefreshToken stored = new RefreshToken();
        stored.setUserId("user-123");
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

        RefreshToken result = refreshTokenService.validateRefreshToken("some-raw-token");

        assertThat(result).isSameAs(stored);
    }

    @Test
    void validateRefreshTokenRejectsAnUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("unknown"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void validateRefreshTokenRejectsARevokedToken() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(true);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("revoked"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token has been revoked");
    }

    @Test
    void validateRefreshTokenRejectsAnExpiredToken() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().minusSeconds(1));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token has expired");
    }

    @Test
    void revokeByUserIdMarksTheExistingTokenRevoked() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);

        when(refreshTokenRepository.findByUserId("user-123"))
                .thenReturn(Optional.of(stored));

        refreshTokenService.revokeByUserId("user-123");

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void revokeByUserIdIsANoOpWhenTheUserHasNoToken() {
        when(refreshTokenRepository.findByUserId("user-123"))
                .thenReturn(Optional.empty());

        refreshTokenService.revokeByUserId("user-123");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeRefreshTokenMarksTheMatchingTokenRevoked() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(stored));

        refreshTokenService.revokeRefreshToken("raw-token");

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(stored);
    }

    @Test
    void revokeRefreshTokenRejectsAnUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken("unknown"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
