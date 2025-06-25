package com.habittracker.controller;

import com.habittracker.dto.*;
import com.habittracker.dto.auth.AuthResponse;
import com.habittracker.dto.auth.LoginRequest;
import com.habittracker.dto.auth.RefreshTokenRequest;
import com.habittracker.dto.user.UserInfoResponse;
import com.habittracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentative de connexion pour: {}", request.getUsername());

        AuthResponse response = authService.login(request);

        log.info("Connexion réussie pour: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Tentative d'inscription pour: {}", request.getUsername());

        AuthResponse response = authService.register(request);

        log.info("Inscription réussie pour: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Demande de rafraîchissement de token");

        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            Authentication authentication) {

        log.info("Déconnexion pour: {}", authentication.getName());

        authService.logout(request.getRefreshToken(), authentication.getName());

        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        UserInfoResponse userInfo = authService.getCurrentUserInfo(authentication);
        return ResponseEntity.ok(userInfo);
    }
}