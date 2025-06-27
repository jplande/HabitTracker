package com.habittracker.controller;

import com.habittracker.service.ChartService;
import com.habittracker.service.StatisticsService;
import com.habittracker.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur avec sécurité COHÉRENTE
 * - Charts habitudes : PUBLICS (pas de validation)
 * - Stats utilisateur : PROTÉGÉES (avec validation JWT)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChartStatisticsController {

    private final ChartService chartService;
    private final StatisticsService statisticsService;
    private final UserSecurityService userSecurityService;

    // ========== GRAPHIQUES HABITUDES (PUBLICS) ==========

    /**
     * Graphique ligne - PUBLIC (pas de données privées)
     */
    @GetMapping("/habits/{habitId}/charts/line")
    public ResponseEntity<Map<String, Object>> getLineChart(
            @PathVariable Long habitId,
            @RequestParam(defaultValue = "30") int days) {

        log.debug("📊 Graphique ligne demandé pour habitude {} ({} jours)", habitId, days);
        Map<String, Object> chartData = chartService.generateLineChartData(habitId, days);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Graphique barres - PUBLIC
     */
    @GetMapping("/habits/{habitId}/charts/bar")
    public ResponseEntity<Map<String, Object>> getBarChart(
            @PathVariable Long habitId,
            @RequestParam(defaultValue = "30") int days) {

        log.debug("📊 Graphique barres demandé pour habitude {} ({} jours)", habitId, days);
        Map<String, Object> chartData = chartService.generateBarChartData(habitId, days);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Graphique hebdomadaire - PUBLIC
     */
    @GetMapping("/habits/{habitId}/charts/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyChart(@PathVariable Long habitId) {
        log.debug("📊 Graphique hebdomadaire demandé pour habitude {}", habitId);
        Map<String, Object> chartData = chartService.generateWeeklyProgressChart(habitId);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Heatmap - PUBLIC
     */
    @GetMapping("/habits/{habitId}/charts/heatmap")
    public ResponseEntity<Map<String, Object>> getHeatmap(
            @PathVariable Long habitId,
            @RequestParam(defaultValue = "90") int days) {

        log.debug("🔥 Heatmap demandée pour habitude {} ({} jours)", habitId, days);
        Map<String, Object> heatmapData = chartService.generateHeatmapData(habitId, days);
        return ResponseEntity.ok(heatmapData);
    }

    /**
     * Statistiques habitude - PUBLIC
     */
    @GetMapping("/habits/{habitId}/statistics")
    public ResponseEntity<Map<String, Object>> getHabitStatistics(
            @PathVariable Long habitId,
            @RequestParam(defaultValue = "30") int days) {

        log.debug("📈 Statistiques demandées pour habitude {} ({} jours)", habitId, days);
        Map<String, Object> stats = statisticsService.calculateHabitStatistics(habitId, days);
        return ResponseEntity.ok(stats);
    }

    /**
     * Graphique générique - PUBLIC
     */
    @GetMapping("/habits/{habitId}/charts/{type}")
    public ResponseEntity<Map<String, Object>> getChart(
            @PathVariable Long habitId,
            @PathVariable String type,
            @RequestParam(defaultValue = "30") int days) {

        log.debug("📊 Graphique {} demandé pour habitude {}", type, habitId);

        Map<String, Object> chartData = switch (type.toLowerCase()) {
            case "line" -> chartService.generateLineChartData(habitId, days);
            case "bar" -> chartService.generateBarChartData(habitId, days);
            case "weekly" -> chartService.generateWeeklyProgressChart(habitId);
            case "heatmap" -> chartService.generateHeatmapData(habitId, days);
            default -> throw new IllegalArgumentException("Type de graphique non supporté: " + type);
        };

        return ResponseEntity.ok(chartData);
    }

    // ========== STATISTIQUES UTILISATEUR (PROTÉGÉES) ==========

    /**
     * Statistiques utilisateur - PROTÉGÉ (avec validation JWT)
     */
    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {

        // ✅ Validation JWT obligatoire (endpoint protégé)
        userSecurityService.validateUserAccess(userId, auth);

        log.info("🔐 Statistiques utilisateur {} demandées (authentifié: {})",
                userId, auth.getName());

        Map<String, Object> stats = statisticsService.calculateUserStatistics(userId, days);
        return ResponseEntity.ok(stats);
    }

    /**
     * Tendances mensuelles - PROTÉGÉ
     */
    @GetMapping("/users/{userId}/trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends(
            @PathVariable Long userId,
            Authentication auth) {

        // ✅ Validation JWT obligatoire
        userSecurityService.validateUserAccess(userId, auth);

        log.info("🔐 Tendances utilisateur {} demandées (authentifié: {})",
                userId, auth.getName());

        Map<String, Object> trends = statisticsService.calculateMonthlyTrends(userId);
        return ResponseEntity.ok(trends);
    }

    /**
     * Comparaison habitudes - PROTÉGÉ
     */
    @GetMapping("/users/{userId}/habits/comparison")
    public ResponseEntity<Map<String, Object>> compareHabits(
            @PathVariable Long userId,
            Authentication auth) {

        // ✅ Validation JWT obligatoire
        userSecurityService.validateUserAccess(userId, auth);

        log.info("🔐 Comparaison habitudes utilisateur {} demandée (authentifié: {})",
                userId, auth.getName());

        Map<String, Object> comparison = statisticsService.compareHabits(userId);
        return ResponseEntity.ok(comparison);
    }

    /**
     * Dashboard complet - PROTÉGÉ
     */
    @GetMapping("/users/{userId}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {

        // ✅ Validation JWT obligatoire
        userSecurityService.validateUserAccess(userId, auth);

        log.info("🔐 Dashboard utilisateur {} demandé (authentifié: {})",
                userId, auth.getName());

        Map<String, Object> dashboard = Map.of(
                "statistics", statisticsService.calculateUserStatistics(userId, days),
                "trends", statisticsService.calculateMonthlyTrends(userId),
                "comparison", statisticsService.compareHabits(userId)
        );

        log.info("📊 Dashboard généré pour utilisateur {} (auth: {})", userId, auth.getName());
        return ResponseEntity.ok(dashboard);
    }
}