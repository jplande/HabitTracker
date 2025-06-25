package com.habittracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // === CACHE SIMPLE AVEC STRING REDIS TEMPLATE ===

    /**
     * Met en cache une valeur avec TTL (JSON String)
     */
    public void cache(String key, Object value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("✅ Cached: {} (TTL: {})", key, ttl);
        } catch (Exception e) {
            log.warn("❌ Cache error: {}", e.getMessage());
        }
    }

    /**
     * Récupère une valeur du cache
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Map<String, Object>> get(String key, Class<T> type) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                T value = objectMapper.readValue(jsonValue, type);
                log.debug("🎯 Cache hit: {}", key);
                return (Optional<Map<String, Object>>) Optional.of(value);
            }
            log.debug("⚡ Cache miss: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("❌ Cache read error: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Supprime une clé du cache
     */
    public void evict(String key) {
        try {
            stringRedisTemplate.delete(key);
            log.debug("🗑️ Evicted: {}", key);
        } catch (Exception e) {
            log.warn("❌ Cache evict error: {}", e.getMessage());
        }
    }

    // === MÉTHODES SPÉCIALISÉES ===

    /**
     * Cache les statistiques d'un utilisateur (1h)
     */
    @Cacheable(value = "user-stats", key = "#userId")
    public void cacheUserStats(Long userId, Map<String, Object> stats) {
        cache("user:stats:" + userId, stats, Duration.ofHours(1));
    }

    /**
     * Récupère les stats utilisateur
     */
    public Optional<Map<String, Object>> getUserStats(Long userId) {
        return get("user:stats:" + userId, Map.class);
    }

    /**
     * Cache les stats d'une habitude (30min)
     */
    public void cacheHabitStats(Long habitId, Object stats) {
        cache("habit:stats:" + habitId, stats, Duration.ofMinutes(30));
    }

    /**
     * Récupère les stats d'habitude
     */
    public <T> Optional<T> getHabitStats(Long habitId, Class<T> type) {
        return (Optional<T>) get("habit:stats:" + habitId, type);
    }

    /**
     * Cache les données de graphique (15min)
     */
    public void cacheChartData(Long habitId, String chartType, Object data) {
        cache("chart:" + habitId + ":" + chartType, data, Duration.ofMinutes(15));
    }

    /**
     * Récupère les données de graphique
     */
    public Optional<Map<String, Object>> getChartData(Long habitId, String chartType) {
        return get("chart:" + habitId + ":" + chartType, Map.class);
    }

    // === INVALIDATION SIMPLE ===

    /**
     * Invalide tous les caches d'un utilisateur
     */
    @CacheEvict(value = "user-stats", key = "#userId")
    public void evictUserCaches(Long userId) {
        evict("user:stats:" + userId);
        log.info("🧹 User {} caches evicted", userId);
    }

    /**
     * Invalide tous les caches d'une habitude
     */
    public void evictHabitCaches(Long habitId) {
        evict("habit:stats:" + habitId);
        // Éviter les patterns complexes, supprimer manuellement les types de graphiques courants
        evict("chart:" + habitId + ":line");
        evict("chart:" + habitId + ":bar");
        log.info("🧹 Habit {} caches evicted", habitId);
    }

    /**
     * Invalide après modification de progression
     */
    public void evictProgressCaches(Long habitId, Long userId) {
        evictHabitCaches(habitId);
        evictUserCaches(userId);
        log.info("📈 Progress caches evicted (habit: {}, user: {})", habitId, userId);
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si Redis est disponible
     */
    public boolean isRedisAvailable() {
        try {
            stringRedisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(1));
            String result = stringRedisTemplate.opsForValue().get("health:check");
            return "ok".equals(result);
        } catch (Exception e) {
            log.warn("❌ Redis not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test simple du cache
     */
    public Map<String, Object> testCache() {
        try {
            String testKey = "test:simple:" + System.currentTimeMillis();
            Map<String, Object> testData = Map.of(
                    "message", "Hello Cache!",
                    "timestamp", System.currentTimeMillis(),
                    "value", 42
            );

            // Mise en cache
            cache(testKey, testData, Duration.ofMinutes(1));

            // Lecture
            Optional<Map<String, Object>> cached = get(testKey, Map.class);

            if (cached.isPresent()) {
                log.info("🎯 CACHE HIT - Test réussi !");
                return Map.of(
                        "status", "SUCCESS",
                        "message", "Cache fonctionne !",
                        "cached_data", cached.get()
                );
            } else {
                log.error("❌ CACHE MISS - Test échoué !");
                return Map.of(
                        "status", "ERROR",
                        "message", "Cache ne fonctionne pas !"
                );
            }
        } catch (Exception e) {
            log.error("❌ Erreur test cache: {}", e.getMessage());
            return Map.of(
                    "status", "ERROR",
                    "message", "Erreur: " + e.getMessage()
            );
        }
    }
}