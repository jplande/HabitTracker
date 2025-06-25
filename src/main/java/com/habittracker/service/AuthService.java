package com.habittracker.service;

import com.habittracker.config.JwtConfig;
import com.habittracker.dto.AuthResponse;
import com.habittracker.dto.LoginRequest;
import com.habittracker.dto.RegisterRequest;
import com.habittracker.dto.UserInfoResponse;
import com.habittracker.entity.User;
import com.habittracker.exception.BusinessException;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Récupérer l'utilisateur
            User user = findUserByUsernameOrEmail(request.getUsername());

            // Générer les tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            return AuthResponse.of(
                    accessToken,
                    refreshToken,
                    jwtConfig.getAccessTokenExpiration(),
                    user
            );

        } catch (AuthenticationException e) {
            log.warn("Échec de connexion pour: {}", request.getUsername());
            throw new BusinessException("Identifiants invalides");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier l'unicité
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà utilisé");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.USER);
        user.setIsActive(true);

        user = userRepository.save(user);

        // Authentifier automatiquement après inscription
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                request.getPassword()
        );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtConfig.getAccessTokenExpiration(),
                user
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = findUserByUsernameOrEmail(username);

        // Invalider l'ancien refresh token
        jwtService.invalidateRefreshToken(refreshToken);

        // Générer de nouveaux tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                List.of(() -> "ROLE_" + user.getRole().name())
        );

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(authentication);

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtConfig.getAccessTokenExpiration(),
                user
        );
    }

    public void logout(String refreshToken, String username) {
        // Invalider le refresh token spécifique
        if (refreshToken != null) {
            jwtService.invalidateRefreshToken(refreshToken);
        }

        // Optionnel : invalider tous les tokens de l'utilisateur
        // jwtService.invalidateAllUserTokens(username);
    }

    public UserInfoResponse getCurrentUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = findUserByUsernameOrEmail(username);
        return UserInfoResponse.fromUser(user);
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }
}