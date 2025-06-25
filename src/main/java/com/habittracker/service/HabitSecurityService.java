package com.habittracker.service;

import com.habittracker.security.CustomUserDetailsService.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HabitSecurityService {

    public void validateHabitAccess(Long userId, Authentication authentication) {
        if (!canAccessUserHabits(userId, authentication)) {
            log.warn("Accès refusé aux habitudes de l'utilisateur ID: {}", userId);
            throw new AccessDeniedException("Accès non autorisé aux habitudes de cet utilisateur");
        }
    }

    public boolean canAccessUserHabits(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return isAdmin(authentication) || isOwner(userId, authentication);
    }

    public boolean canModifyUserHabits(Long userId, Authentication authentication) {
        return canAccessUserHabits(userId, authentication);
    }

    public Long getCurrentUserId(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Authentification requise");
        }

        // Gestion du JWT (OAuth2)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                return jwt.getClaim("userId");
            } catch (Exception e) {
                log.warn("Impossible d'extraire userId du JWT: {}", e.getMessage());
                throw new AccessDeniedException("Token JWT invalide");
            }
        }

        // Gestion du UserPrincipal (Basic Auth)
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }

        log.warn("Type de principal non supporté: {}", authentication.getPrincipal().getClass());
        throw new AccessDeniedException("Type d'authentification non supporté");
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(Long userId, Authentication authentication) {
        // Pour JWT
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                Long tokenUserId = jwt.getClaim("userId");
                return userId.equals(tokenUserId);
            } catch (Exception e) {
                log.warn("Erreur lors de l'extraction de userId du JWT: {}", e.getMessage());
                return false;
            }
        }

        // Pour UserPrincipal
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return userId.equals(principal.getId());
        }

        return false;
    }
}