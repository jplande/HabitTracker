package com.habittracker.service;

import com.habittracker.config.JwtConfig;
import com.habittracker.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtConfig jwtConfig;

    // Cache simple pour les refresh tokens
    private final Set<String> validRefreshTokens = ConcurrentHashMap.newKeySet();

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtConfig.getAccessTokenExpiration(), "access");
    }

    public String generateRefreshToken(Authentication authentication) {
        String refreshToken = generateToken(authentication, jwtConfig.getRefreshTokenExpiration(), "refresh");
        validRefreshTokens.add(refreshToken);
        return refreshToken;
    }

    /**
     * ✅ MÉTHODE PRINCIPALE FIXÉE pour génération token User
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtConfig.getAccessTokenExpiration(), ChronoUnit.SECONDS);

        // ✅ Claims standardisées et compatibles
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("habit-tracker")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getUsername())

                // Claims custom
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())

                // ✅ FIX: Authorities au bon format
                .claim("authorities", "ROLE_" + user.getRole().name())
                .claim("role", user.getRole().name())

                .claim("type", "access")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        log.info("🔑 Token généré pour {} (ID: {}, Role: {})",
                user.getUsername(), user.getId(), user.getRole());

        return token;
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String tokenType = jwt.getClaimAsString("type");
            return "refresh".equals(tokenType) &&
                    validRefreshTokens.contains(token) &&
                    jwt.getExpiresAt().isAfter(Instant.now());
        } catch (Exception e) {
            log.debug("Token de refresh invalide: {}", e.getMessage());
            return false;
        }
    }

    public void invalidateRefreshToken(String token) {
        validRefreshTokens.remove(token);
        log.debug("🗑️ Refresh token invalidé");
    }

    public String getUsernameFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            log.debug("Impossible d'extraire l'username du token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Méthode pour extraire l'userId du token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaim("userId");
        } catch (Exception e) {
            log.debug("Impossible d'extraire l'userId du token: {}", e.getMessage());
            return null;
        }
    }

    public void invalidateAllUserTokens(String username) {
        validRefreshTokens.removeIf(token -> {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                return username.equals(jwt.getSubject());
            } catch (Exception e) {
                return false;
            }
        });
        log.info("🗑️ Tous les tokens de {} invalidés", username);
    }

    /**
     * ✅ Génération token depuis Authentication (pour compatibility)
     */
    private String generateToken(Authentication authentication, long expiration, String type) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiration, ChronoUnit.SECONDS);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("habit-tracker")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(authentication.getName())

                // ✅ Authorities au bon format
                .claim("authorities", authorities)
                .claim("scope", authorities) // Pour compatibilité

                .claim("type", type)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * ✅ Méthode de debug pour vérifier un token
     */
    public void debugToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            log.info("🔍 TOKEN DEBUG:");
            log.info("  Subject: {}", jwt.getSubject());
            log.info("  UserId: {}", (Object) jwt.getClaim("userId"));
            log.info("  Authorities: {}", (Object) jwt.getClaim("authorities"));
            log.info("  Role: {}", (Object) jwt.getClaim("role"));
            log.info("  Expires: {}", jwt.getExpiresAt());
            log.info("  Type: {}", (Object) jwt.getClaim("type"));
        } catch (Exception e) {
            log.error(" Token invalide: {}", e.getMessage());
        }
    }
}