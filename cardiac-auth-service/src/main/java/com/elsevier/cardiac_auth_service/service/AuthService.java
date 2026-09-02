package com.elsevier.cardiac_auth_service.service;

import com.elsevier.cardiac_auth_service.dto.*;
import com.elsevier.cardiac_auth_service.entity.RefreshToken;
import com.elsevier.cardiac_auth_service.entity.User;
import com.elsevier.cardiac_auth_service.exception.*;
import com.elsevier.cardiac_auth_service.kafka.UserRegistrationProducer;
import com.elsevier.cardiac_auth_service.repository.UserRepository;
import com.elsevier.cardiac_auth_service.security.JwtService;
import com.elsevier.cardiac_auth_service.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRegistrationProducer userRegistrationProducer;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder , JwtService jwtService,RefreshTokenService refreshTokenService, UserRegistrationProducer userRegistrationProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRegistrationProducer = userRegistrationProducer;
    }
    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        //set Email
        User user = new User();
        user.setEmail(request.email());

        // hash password
        String hashedPassword = passwordEncoder.encode(request.password());
        user.setPasswordHash(hashedPassword);

        // date and time
        LocalDateTime now =  LocalDateTime.now();

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // save the data to db
        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                request.firstName(),
                request.lastName(),
                request.contactNumber(),
                request.department()
        );

        userRegistrationProducer.publish(event);

    }

    //login

    public LoginResponse authenticate(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail()
        );

        String refreshToken = refreshTokenService.createRefreshToken(
                user.getId()
        );

        return new LoginResponse(accessToken,refreshToken);
    }


    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {

        RefreshToken oldRefreshToken =
                refreshTokenService.validateRefreshToken(request.refreshToken());

        User user = userRepository.findById(oldRefreshToken.getUserId())
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Invalid refresh token"));

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail()
        );

        String newRefreshToken =
                refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse(
                accessToken,
                newRefreshToken
        );
    }


    @Transactional
    public void changePassword(
            String userId,
            ChangePasswordRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        boolean passwordMatches = passwordEncoder.matches(
                request.oldPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(user);

        refreshTokenService.revokeByUserId(userId);
    }
}