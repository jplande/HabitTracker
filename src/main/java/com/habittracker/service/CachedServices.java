package com.habittracker.service;

import com.habittracker.dto.habit.HabitCreateRequest;
import com.habittracker.dto.habit.HabitResponse;
import com.habittracker.dto.habit.HabitUpdateRequest;
import com.habittracker.dto.progress.ProgressCreateRequest;
import com.habittracker.dto.progress.ProgressResponse;
import com.habittracker.dto.progress.ProgressStatsResponse;
import com.habittracker.dto.progress.ProgressUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Extensions des services avec cache Redis simple
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CachedServices {

    private final HabitService habitService;
    private final ProgressService progressService;
    private final CacheService cacheService;

    // === HABIT SERVICE AVEC CACHE ===

    /**
     * Récupère les statistiques d'une habitude avec cache
     */
    public ProgressStatsResponse getHabitStatistics(Long habitId, int days) {
        // Vérifier le cache
        Optional<ProgressStatsResponse> cached = cacheService.getHabitStats(habitId, ProgressStatsResponse.class);
        if (cached.isPresent()) {
            log.debug("🎯 Habit stats cache hit: {}", habitId);
            return cached.get();
        }

        // Calculer et cacher
        ProgressStatsResponse stats = progressService.getHabitStatistics(habitId, days);
        cacheService.cacheHabitStats(habitId, stats);
        return stats;
    }

    /**
     * Récupère les données de graphique avec cache
     */
    public Map<String, Object> getChartData(Long habitId, int days, String chartType) {
        // Vérifier le cache
        Optional<Map<String, Object>> cached = cacheService.getChartData(habitId, chartType);
        if (cached.isPresent()) {
            log.debug("🎯 Chart data cache hit: {}/{}", habitId, chartType);
            return cached.get();
        }

        // Calculer et cacher
        Map<String, Object> data = progressService.getChartData(habitId, days, chartType);
        cacheService.cacheChartData(habitId, chartType, data);
        return data;
    }

    /**
     * Crée une habitude et invalide les caches
     */
    @Transactional
    @CacheEvict(value = "user-stats", key = "#userId")
    public HabitResponse createHabit(Long userId, HabitCreateRequest request) {
        HabitResponse habit = habitService.createHabit(userId, request);
        cacheService.evictUserCaches(userId);
        return habit;
    }

    /**
     * Met à jour une habitude et invalide les caches
     */
    @Transactional
    public HabitResponse updateHabit(Long id, Long userId, HabitUpdateRequest request) {
        HabitResponse habit = habitService.updateHabit(id, userId, request);
        cacheService.evictHabitCaches(id);
        cacheService.evictUserCaches(userId);
        return habit;
    }

    /**
     * Supprime une habitude et invalide les caches
     */
    @Transactional
    public void deleteHabit(Long id, Long userId) {
        habitService.deleteHabit(id, userId);
        cacheService.evictHabitCaches(id);
        cacheService.evictUserCaches(userId);
    }

    // === PROGRESS SERVICE AVEC CACHE ===

    /**
     * Crée une progression et invalide les caches
     */
    @Transactional
    public ProgressResponse createProgress(Long habitId, ProgressCreateRequest request, Authentication auth) {
        ProgressResponse progress = progressService.createProgress(habitId, request, auth);
        cacheService.evictProgressCaches(habitId, progress.getUserId());
        return progress;
    }

    /**
     * Met à jour une progression et invalide les caches
     */
    @Transactional
    public ProgressResponse updateProgress(Long id, ProgressUpdateRequest request, Authentication auth) {
        ProgressResponse oldProgress = progressService.findById(id);
        ProgressResponse progress = progressService.updateProgress(id, request, auth);
        cacheService.evictProgressCaches(oldProgress.getHabitId(), progress.getUserId());
        return progress;
    }

    /**
     * Supprime une progression et invalide les caches
     */
    @Transactional
    public void deleteProgress(Long id, Authentication auth) {
        ProgressResponse progress = progressService.findById(id);
        progressService.deleteProgress(id, auth);
        cacheService.evictProgressCaches(progress.getHabitId(), progress.getUserId());
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Invalide tous les caches d'un utilisateur
     */
    public void invalidateUserCaches(Long userId) {
        cacheService.evictUserCaches(userId);
    }

    /**
     * Invalide tous les caches d'une habitude
     */
    public void invalidateHabitCaches(Long habitId, Long userId) {
        cacheService.evictHabitCaches(habitId);
        cacheService.evictUserCaches(userId);
    }
}