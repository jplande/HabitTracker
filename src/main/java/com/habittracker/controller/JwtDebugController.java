package com.habittracker.controller;

import com.habittracker.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour débugger les tokens JWT
 */
@RestController
@RequestMapping("/api/jwt-debug")
@RequiredArgsConstructor
@Slf4j
public class JwtDebugController {

    private final JwtService jwtService;

    /**
     * Endpoint pour analyser l'auth actuelle
     */
    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication) {

        Map<String, Object> info = new HashMap<>();

        // Infos header
        info.put("authHeaderPresent", authHeader != null);
        if (authHeader != null) {
            info.put("authHeaderFormat", authHeader.startsWith("Bearer ") ? "Correct" : "Incorrect");
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                info.put("tokenLength", token.length());
                info.put("tokenStart", token.substring(0, Math.min(20, token.length())) + "...");

                // Debug du token
                jwtService.debugToken(token);
            }
        }

        // Infos authentication
        info.put("authenticationPresent", authentication != null);
        if (authentication != null) {
            info.put("authenticationName", authentication.getName());
            info.put("authenticationClass", authentication.getClass().getSimpleName());
            info.put("authorities", authentication.getAuthorities());
            info.put("isAuthenticated", authentication.isAuthenticated());

            // Détails principal
            Object principal = authentication.getPrincipal();
            info.put("principalClass", principal.getClass().getSimpleName());

            if (principal instanceof Jwt jwt) {
                info.put("jwtSubject", jwt.getSubject());
                info.put("jwtClaims", jwt.getClaims().keySet());
                info.put("jwtUserId", jwt.getClaim("userId"));
                info.put("jwtAuthorities", jwt.getClaim("authorities"));
            }
        }

        log.info("🔍 JWT Debug - Auth: {}, Header: {}",
                authentication != null ? "Present" : "Missing",
                authHeader != null ? "Present" : "Missing");

        return ResponseEntity.ok(info);
    }

    /**
     * Test avec token en paramètre
     */
    @PostMapping("/test-token")
    public ResponseEntity<Map<String, Object>> testToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isEmpty()) {
            result.put("error", "Token manquant");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // Enlever "Bearer " si présent
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            jwtService.debugToken(token);

            String username = jwtService.getUsernameFromToken(token);
            Long userId = jwtService.getUserIdFromToken(token);

            result.put("valid", true);
            result.put("username", username);
            result.put("userId", userId);
            result.put("tokenLength", token.length());

        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint protégé pour test
     */
    @GetMapping("/protected-test")
    public ResponseEntity<Map<String, Object>> protectedTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("success", true);
            response.put("message", "✅ Authentification JWT réussie !");
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());

            log.info("✅ Test protégé réussi pour: {}", authentication.getName());
        } else {
            response.put("success", false);
            response.put("message", "❌ Pas d'authentification");
        }

        return ResponseEntity.ok(response);
    }
}