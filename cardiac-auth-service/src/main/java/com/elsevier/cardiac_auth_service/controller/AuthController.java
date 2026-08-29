package com.elsevier.cardiac_auth_service.controller;

import com.elsevier.cardiac_auth_service.dto.LoginRequest;
import com.elsevier.cardiac_auth_service.dto.LoginResponse;
import com.elsevier.cardiac_auth_service.dto.RegisterRequest;
import com.elsevier.cardiac_auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(
//            @Valid @RequestBody LoginRequest request) {
//
//        authService.authenticate(request);
//
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request) {

        authService.authenticate(request);

        return ResponseEntity.ok().build();
    }
}