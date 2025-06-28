package com.habittracker.controller.admin;

import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.repository.UserRepository;
import com.habittracker.service.StatisticsService;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur MVC pour l'interface d'administration
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final StatisticsService statisticsService;
    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final ProgressRepository progressRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Dashboard principal d'administration
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("🔧 Accès au dashboard admin");

        try {
            // Statistiques globales
            Map<String, Object> globalStats = buildGlobalStatistics();
            model.addAttribute("globalStats", globalStats);

            // Statistiques récentes (7 derniers jours)
            Map<String, Object> recentStats = buildRecentStatistics();
            model.addAttribute("recentStats", recentStats);

            // Données pour les graphiques
            Map<String, Object> chartData = buildDashboardChartData();
            model.addAttribute("chartData", chartData);

            model.addAttribute("pageTitle", "Dashboard Admin");
            model.addAttribute("currentPage", "dashboard");

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement du dashboard admin", e);
            model.addAttribute("error", "Erreur lors du chargement des données");
        }

        return "admin/dashboard";
    }

    /**
     * Gestion des utilisateurs
     */
    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Model model) {

        log.info("👥 Accès à la gestion des utilisateurs - page: {}", page);

        try {
            Pageable pageable = PageRequest.of(page, size);

            // Liste des utilisateurs avec pagination
            var usersPage = userService.findAllUsers(pageable);
            model.addAttribute("usersPage", usersPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("search", search);

            // Statistiques utilisateurs
            Map<String, Object> userStats = buildUserStatistics();
            model.addAttribute("userStats", userStats);

            model.addAttribute("pageTitle", "Gestion des Utilisateurs");
            model.addAttribute("currentPage", "users");

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement des utilisateurs", e);
            model.addAttribute("error", "Erreur lors du chargement des utilisateurs");
        }

        return "admin/users";
    }

    /**
     * Gestion des habitudes
     */
    @GetMapping("/habits")
    public String habits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            Model model) {

        log.info("🎯 Accès à la gestion des habitudes - page: {}", page);

        try {
            // Statistiques des habitudes
            Map<String, Object> habitStats = buildHabitStatistics();
            model.addAttribute("habitStats", habitStats);

            // Habitudes par catégorie
            Map<String, Long> habitsByCategory = buildHabitsByCategory();
            model.addAttribute("habitsByCategory", habitsByCategory);

            // Habitudes les plus populaires
            var popularHabits = buildPopularHabits();
            model.addAttribute("popularHabits", popularHabits);

            model.addAttribute("pageTitle", "Gestion des Habitudes");
            model.addAttribute("currentPage", "habits");
            model.addAttribute("currentPageNum", page);
            model.addAttribute("category", category);

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement des habitudes", e);
            model.addAttribute("error", "Erreur lors du chargement des habitudes");
        }

        return "admin/habits";
    }

    /**
     * Statistiques détaillées
     */
    @GetMapping("/statistics")
    public String statistics(
            @RequestParam(defaultValue = "30") int days,
            Model model) {

        log.info("📊 Accès aux statistiques détaillées - {} jours", days);

        try {
            // Statistiques globales détaillées
            Map<String, Object> detailedStats = buildDetailedStatistics(days);
            model.addAttribute("detailedStats", detailedStats);

            // Tendances et évolutions
            Map<String, Object> trends = buildTrendStatistics(days);
            model.addAttribute("trends", trends);

            // Données pour graphiques avancés
            Map<String, Object> advancedCharts = buildAdvancedChartData(days);
            model.addAttribute("advancedCharts", advancedCharts);

            model.addAttribute("pageTitle", "Statistiques Détaillées");
            model.addAttribute("currentPage", "statistics");
            model.addAttribute("selectedDays", days);

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement des statistiques", e);
            model.addAttribute("error", "Erreur lors du chargement des statistiques");
        }

        return "admin/statistics";
    }

    /**
     * Exportation de données (placeholder)
     */
    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("pageTitle", "Export de Données");
        model.addAttribute("currentPage", "export");
        return "admin/export";
    }

    /**
     * Paramètres d'administration
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Paramètres Admin");
        model.addAttribute("currentPage", "settings");
        return "admin/settings";
    }

    // === MÉTHODES PRIVÉES POUR CONSTRUCTION DES DONNÉES ===

    /**
     * Construit les statistiques globales
     */
    private Map<String, Object> buildGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long totalHabits = habitRepository.count();
        long totalProgress = progressRepository.count();
        long totalAchievements = achievementRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalHabits", totalHabits);
        stats.put("totalProgress", totalProgress);
        stats.put("totalAchievements", totalAchievements);

        // Taux d'utilisateurs actifs
        double activeUserRate = totalUsers > 0 ? ((double) activeUsers / totalUsers) * 100 : 0;
        stats.put("activeUserRate", Math.round(activeUserRate * 100.0) / 100.0);

        // Moyenne habitudes par utilisateur
        double avgHabitsPerUser = activeUsers > 0 ? (double) totalHabits / activeUsers : 0;
        stats.put("avgHabitsPerUser", Math.round(avgHabitsPerUser * 100.0) / 100.0);

        return stats;
    }

    /**
     * Construit les statistiques récentes
     */
    private Map<String, Object> buildRecentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Nouveaux utilisateurs cette semaine
        long newUsers = userRepository.findByCreatedAtAfter(weekAgo, PageRequest.of(0, 1000))
                .getTotalElements();

        // Progression cette semaine
        long recentProgress = progressRepository.countByUserIdAndDateAfter(null,
                weekAgo.toLocalDate()); // Simplifié

        stats.put("newUsersThisWeek", newUsers);
        stats.put("progressThisWeek", recentProgress);
        stats.put("weeklyGrowth", calculateWeeklyGrowth());

        return stats;
    }

    /**
     * Construit les données pour graphiques du dashboard
     */
    private Map<String, Object> buildDashboardChartData() {
        Map<String, Object> chartData = new HashMap<>();

        // Graphique évolution utilisateurs (simplifié)
        Map<String, Object> userGrowth = new HashMap<>();
        userGrowth.put("labels", new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"});
        userGrowth.put("data", new int[]{10, 25, 45, 78, 120, 150});
        chartData.put("userGrowth", userGrowth);

        // Graphique activité (simplifié)
        Map<String, Object> activityChart = new HashMap<>();
        activityChart.put("labels", new String[]{"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"});
        activityChart.put("data", new int[]{120, 150, 180, 170, 160, 140, 100});
        chartData.put("weeklyActivity", activityChart);

        return chartData;
    }

    /**
     * Construit les statistiques des utilisateurs
     */
    private Map<String, Object> buildUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = totalUsers - activeUsers;

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);

        return stats;
    }

    /**
     * Construit les statistiques des habitudes
     */
    private Map<String, Object> buildHabitStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalHabits = habitRepository.count();
        long activeHabits = habitRepository.countByIsActive(true);

        stats.put("totalHabits", totalHabits);
        stats.put("activeHabits", activeHabits);
        stats.put("inactiveHabits", totalHabits - activeHabits);

        return stats;
    }

    /**
     * Construit la répartition des habitudes par catégorie
     */
    private Map<String, Long> buildHabitsByCategory() {
        Map<String, Long> categories = new HashMap<>();

        // Simulation - dans un vrai projet, on ferait une requête groupée
        categories.put("SPORT", 45L);
        categories.put("SANTE", 38L);
        categories.put("EDUCATION", 32L);
        categories.put("TRAVAIL", 28L);
        categories.put("LIFESTYLE", 25L);
        categories.put("AUTRE", 15L);

        return categories;
    }

    /**
     * Construit la liste des habitudes populaires
     */
    private Map<String, Object> buildPopularHabits() {
        Map<String, Object> popular = new HashMap<>();

        // Simulation - données factices
        popular.put("mostCreated", "Course à pied");
        popular.put("mostProgressed", "Lecture quotidienne");
        popular.put("bestCompletion", "Boire de l'eau");

        return popular;
    }

    /**
     * Construit les statistiques détaillées
     */
    private Map<String, Object> buildDetailedStatistics(int days) {
        Map<String, Object> stats = new HashMap<>();

        // Activité moyenne par jour
        double avgDailyProgress = (double) progressRepository.count() / days;
        stats.put("avgDailyProgress", Math.round(avgDailyProgress * 100.0) / 100.0);

        // Taux de rétention (simplifié)
        stats.put("retentionRate", 78.5);

        // Engagement utilisateur
        stats.put("avgSessionTime", "12 min");
        stats.put("dailyActiveUsers", Math.round(userRepository.countByIsActive(true) * 0.65));

        return stats;
    }

    /**
     * Construit les tendances
     */
    private Map<String, Object> buildTrendStatistics(int days) {
        Map<String, Object> trends = new HashMap<>();

        trends.put("userGrowthTrend", "positive");
        trends.put("activityTrend", "stable");
        trends.put("engagementTrend", "positive");

        return trends;
    }

    /**
     * Construit les données pour graphiques avancés
     */
    private Map<String, Object> buildAdvancedChartData(int days) {
        Map<String, Object> charts = new HashMap<>();

        // Heatmap d'activité
        Map<String, Object> heatmap = new HashMap<>();
        heatmap.put("type", "heatmap");
        heatmap.put("period", days);
        charts.put("activityHeatmap", heatmap);

        // Funnel d'engagement
        Map<String, Object> funnel = new HashMap<>();
        funnel.put("registration", 100);
        funnel.put("firstHabit", 75);
        funnel.put("weeklyActive", 45);
        funnel.put("monthlyActive", 32);
        charts.put("engagementFunnel", funnel);

        return charts;
    }

    /**
     * Calcule la croissance hebdomadaire (simplifié)
     */
    private double calculateWeeklyGrowth() {
        // Simulation - dans un vrai projet, calcul basé sur données réelles
        return 12.5; // 12.5% de croissance
    }
}