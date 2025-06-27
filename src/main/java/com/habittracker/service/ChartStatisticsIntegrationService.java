package com.habittracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'intégration pour combiner graphiques et statistiques avec cache
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartStatisticsIntegrationService {

    private final ChartService chartService;
    private final StatisticsService statisticsService;
    private final CacheService cacheService;

    /**
     * Récupère toutes les données d'une habitude (stats + graphiques)
     */
    @Cacheable(value = "habit-complete-data", key = "#habitId + '_' + #days")
    public Map<String, Object> getCompleteHabitData(Long habitId, int days) {
        log.info("📊 Génération données complètes pour habitude {} ({} jours)", habitId, days);

        Map<String, Object> completeData = new HashMap<>();

        try {
            // Statistiques
            Map<String, Object> stats = statisticsService.calculateHabitStatistics(habitId, days);
            completeData.put("statistics", stats);

            // Graphiques
            Map<String, Object> charts = new HashMap<>();
            charts.put("line", chartService.generateLineChartData(habitId, days));
            charts.put("bar", chartService.generateBarChartData(habitId, days));
            charts.put("weekly", chartService.generateWeeklyProgressChart(habitId));
            charts.put("heatmap", chartService.generateHeatmapData(habitId, Math.min(days, 90)));

            completeData.put("charts", charts);
            completeData.put("generatedAt", System.currentTimeMillis());

            log.info("✅ Données complètes générées pour habitude {}", habitId);

        } catch (Exception e) {
            log.error("❌ Erreur génération données habitude {}: {}", habitId, e.getMessage());
            completeData.put("error", "Erreur lors de la génération des données");
        }

        return completeData;
    }

    /**
     * Récupère un dashboard utilisateur complet avec cache
     */
    @Cacheable(value = "user-dashboard", key = "#userId + '_' + #days")
    public Map<String, Object> getCompleteUserDashboard(Long userId, int days) {
        log.info("📊 Génération dashboard complet pour utilisateur {} ({} jours)", userId, days);

        Map<String, Object> dashboard = new HashMap<>();

        try {
            // Statistiques globales
            dashboard.put("statistics", statisticsService.calculateUserStatistics(userId, days));

            // Tendances
            dashboard.put("trends", statisticsService.calculateMonthlyTrends(userId));

            // Comparaison habitudes
            dashboard.put("comparison", statisticsService.compareHabits(userId));

            // Métadonnées
            dashboard.put("userId", userId);
            dashboard.put("period", days);
            dashboard.put("generatedAt", System.currentTimeMillis());

            log.info("✅ Dashboard complet généré pour utilisateur {}", userId);

        } catch (Exception e) {
            log.error("❌ Erreur génération dashboard utilisateur {}: {}", userId, e.getMessage());
            dashboard.put("error", "Erreur lors de la génération du dashboard");
        }

        return dashboard;
    }

    /**
     * Récupère des données optimisées pour mobile
     */
    public Map<String, Object> getMobileOptimizedData(Long habitId, Long userId) {
        log.info("📱 Génération données mobile pour habitude {} / utilisateur {}", habitId, userId);

        Map<String, Object> mobileData = new HashMap<>();

        try {
            // Statistiques essentielles seulement
            Map<String, Object> stats = statisticsService.calculateHabitStatistics(habitId, 7); // 7 jours seulement
            Map<String, Object> essentialStats = new HashMap<>();
            essentialStats.put("currentStreak", stats.get("currentStreak"));
            essentialStats.put("completionRate", stats.get("completionRate"));
            essentialStats.put("averageValue", stats.get("averageValue"));
            essentialStats.put("trend", stats.get("trend"));

            mobileData.put("statistics", essentialStats);

            // Graphique simplifié (7 jours)
            mobileData.put("weeklyChart", chartService.generateLineChartData(habitId, 7));

            // Heatmap réduite (30 jours)
            mobileData.put("heatmap", chartService.generateHeatmapData(habitId, 30));

            mobileData.put("optimizedFor", "mobile");
            mobileData.put("generatedAt", System.currentTimeMillis());

            log.info("✅ Données mobile générées pour habitude {}", habitId);

        } catch (Exception e) {
            log.error("❌ Erreur génération mobile habitude {}: {}", habitId, e.getMessage());
            mobileData.put("error", "Erreur lors de la génération mobile");
        }

        return mobileData;
    }

    /**
     * Récupère des données pour export (CSV, PDF, etc.)
     */
    public Map<String, Object> getExportData(Long habitId, int days) {
        log.info("📄 Génération données export pour habitude {} ({} jours)", habitId, days);

        Map<String, Object> exportData = new HashMap<>();

        try {
            // Statistiques complètes
            exportData.put("statistics", statisticsService.calculateHabitStatistics(habitId, days));

            // Données brutes pour CSV
            Map<String, Object> rawData = new HashMap<>();
            // Ici on pourrait ajouter les données de progression brutes
            // rawData.put("progressEntries", progressService.getRawData(habitId, days));

            exportData.put("rawData", rawData);
            exportData.put("exportFormat", "complete");
            exportData.put("generatedAt", System.currentTimeMillis());

            log.info("✅ Données export générées pour habitude {}", habitId);

        } catch (Exception e) {
            log.error("❌ Erreur génération export habitude {}: {}", habitId, e.getMessage());
            exportData.put("error", "Erreur lors de la génération export");
        }

        return exportData;
    }

    /**
     * Invalide le cache pour une habitude
     */
    public void invalidateHabitCache(Long habitId, Long userId) {
        cacheService.evictHabitCaches(habitId);
        cacheService.evictUserCaches(userId);
        log.info("🧹 Cache invalidé pour habitude {} / utilisateur {}", habitId, userId);
    }

    /**
     * Test de performance des calculs
     */
    public Map<String, Object> performanceTest(Long habitId) {
        log.info("⚡ Test performance pour habitude {}", habitId);

        Map<String, Object> results = new HashMap<>();

        long startTime = System.currentTimeMillis();

        // Test statistiques
        long statsStart = System.currentTimeMillis();
        statisticsService.calculateHabitStatistics(habitId, 30);
        long statsTime = System.currentTimeMillis() - statsStart;

        // Test graphique ligne
        long lineStart = System.currentTimeMillis();
        chartService.generateLineChartData(habitId, 30);
        long lineTime = System.currentTimeMillis() - lineStart;

        // Test graphique barres
        long barStart = System.currentTimeMillis();
        chartService.generateBarChartData(habitId, 30);
        long barTime = System.currentTimeMillis() - barStart;

        long totalTime = System.currentTimeMillis() - startTime;

        results.put("statisticsTime", statsTime + "ms");
        results.put("lineChartTime", lineTime + "ms");
        results.put("barChartTime", barTime + "ms");
        results.put("totalTime", totalTime + "ms");
        results.put("habitId", habitId);

        log.info("⚡ Test terminé en {}ms pour habitude {}", totalTime, habitId);
        return results;
    }
}