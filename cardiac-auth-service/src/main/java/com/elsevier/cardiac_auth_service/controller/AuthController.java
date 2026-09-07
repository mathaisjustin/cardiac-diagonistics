package com.elsevier.cardiac_auth_service.controller;

import org.springframework.security.core.Authentication;
import com.elsevier.cardiac_auth_service.dto.*;
import com.elsevier.cardiac_auth_service.security.RefreshTokenService;
import com.elsevier.cardiac_auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Auth",
        description = "Registration, login, token refresh, logout, and password change."
)
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Returns 201 with no body on success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(
            summary = "Log in",
            description = "Authenticates with email/password and issues a new access + refresh "
                    + "token pair."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.authenticate(request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Refresh an access token",
            description = "Exchanges a still-valid refresh token for a new access + refresh "
                    + "token pair."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New token pair issued",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid, expired, or revoked",
                    content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response = authService.refreshAccessToken(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Log out",
            description = "Revokes the given refresh token so it can no longer be used to "
                    + "obtain new access tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request) {

        refreshTokenService.revokeRefreshToken(
                request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change password",
            description = "Changes the authenticated caller's password. Requires a valid "
                    + "access token (the caller's identity is taken from the current "
                    + "Authentication)."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated, or old password incorrect",
                    content = @Content)
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userId = (String) authentication.getPrincipal();

        authService.changePassword(userId, request);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }


}
