package com.habittracker.controller;

import com.habittracker.service.CachedServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur d'administration du cache Redis
 */
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CacheAdminController {

    private final CachedServices cachedServices;

    /**
     * Invalide tous les caches d'un utilisateur
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> invalidateUserCaches(@PathVariable Long userId) {
        log.info("🧹 Admin: Invalidation caches utilisateur {}", userId);
        cachedServices.invalidateUserCaches(userId);
        return ResponseEntity.ok("Caches utilisateur " + userId + " invalidés");
    }

    /**
     * Invalide tous les caches d'une habitude
     */
    @DeleteMapping("/habits/{habitId}/user/{userId}")
    public ResponseEntity<String> invalidateHabitCaches(
            @PathVariable Long habitId,
            @PathVariable Long userId) {
        log.info("🧹 Admin: Invalidation caches habitude {} pour utilisateur {}", habitId, userId);
        cachedServices.invalidateHabitCaches(habitId, userId);
        return ResponseEntity.ok("Caches habitude " + habitId + " invalidés");
    }
}