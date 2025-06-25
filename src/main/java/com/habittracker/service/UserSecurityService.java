package com.habittracker.service;

import com.habittracker.security.CustomUserDetailsService.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserSecurityService {

    public void validateUserAccess(Long userId, Authentication authentication) {
        if (!canAccessUser(userId, authentication)) {
            log.warn("Accès refusé pour l'utilisateur ID: {}", userId);
            throw new AccessDeniedException("Accès non autorisé à cette ressource");
        }
    }

    public boolean canAccessUser(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return isAdmin(authentication) || isOwner(userId, authentication);
    }

    public boolean canModifyUser(Long userId, Authentication authentication) {
        return canAccessUser(userId, authentication);
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

        log.warn("Type de principal inattendu: {}", authentication.getPrincipal().getClass());
        return false;
    }
}