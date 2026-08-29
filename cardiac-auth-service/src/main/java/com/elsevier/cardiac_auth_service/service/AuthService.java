package com.elsevier.cardiac_auth_service.service;

import com.elsevier.cardiac_auth_service.dto.LoginRequest;
import com.elsevier.cardiac_auth_service.dto.RegisterRequest;
import com.elsevier.cardiac_auth_service.entity.User;
import com.elsevier.cardiac_auth_service.exception.EmailAlreadyExistsException;
import com.elsevier.cardiac_auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        userRepository.save(user);

    }

    public User authenticate(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }
}