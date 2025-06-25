package com.habittracker.service;

import com.habittracker.security.CustomUserDetailsService.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return userId.equals(principal.getId());
        } catch (ClassCastException e) {
            log.warn("Type de principal inattendu: {}", authentication.getPrincipal().getClass());
            return false;
        }
    }
}