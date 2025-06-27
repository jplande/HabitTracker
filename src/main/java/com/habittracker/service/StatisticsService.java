package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.Progress;
import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour les calculs de statistiques et tendances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ProgressRepository progressRepository;
    private final HabitRepository habitRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Calcule les statistiques globales d'un utilisateur
     */
    public Map<String, Object> calculateUserStatistics(Long userId, int days) {
        ValidationUtils.validateId(userId, "utilisateur");
        ValidationUtils.validatePositive(days, "nombre de jours");

        Map<String, Object> stats = new HashMap<>();

        // Statistiques de base
        long totalHabits = habitRepository.countByUserId(userId);
        long activeHabits = habitRepository.countByUserIdAndIsActive(userId, true);
        long totalProgress = progressRepository.countByUserId(userId);
        long totalAchievements = achievementRepository.countByUserId(userId);

        stats.put("totalHabits", totalHabits);
        stats.put("activeHabits", activeHabits);
        stats.put("totalProgress", totalProgress);
        stats.put("totalAchievements", totalAchievements);

        // Statistiques de période
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Progress> periodProgress = progressRepository.findByUserIdAndDateBetween(userId, startDate, LocalDate.now());

        stats.put("periodProgress", periodProgress.size());
        stats.put("averageProgressPerDay", calculateAverageProgressPerDay(periodProgress, days));
        stats.put("currentStreak", calculateUserCurrentStreak(userId));
        stats.put("bestWeek", findBestWeek(userId));
        stats.put("consistency", calculateConsistencyScore(userId, days));

        log.debug("📊 Statistiques calculées pour utilisateur {}", userId);
        return stats;
    }

    /**
     * Calcule les statistiques détaillées d'une habitude
     */
    public Map<String, Object> calculateHabitStatistics(Long habitId, int days) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "nombre de jours");

        Habit habit = getHabit(habitId);
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(habitId, startDate, LocalDate.now());

        Map<String, Object> stats = new HashMap<>();

        // Informations de base
        stats.put("habitId", habitId);
        stats.put("habitTitle", habit.getTitle());
        stats.put("habitUnit", habit.getUnit());
        stats.put("habitTarget", habit.getTargetValue());

        // Statistiques de progression
        stats.put("totalEntries", progressList.size());
        stats.put("completionRate", calculateCompletionRate(progressList, days));
        stats.put("currentStreak", calculateHabitCurrentStreak(habitId));
        stats.put("longestStreak", calculateLongestStreak(habitId));

        // Statistiques de valeurs
        if (!progressList.isEmpty()) {
            List<Double> values = progressList.stream().map(Progress::getValue).toList();
            stats.put("totalValue", calculateSum(values));
            stats.put("averageValue", calculateAverage(values));
            stats.put("maxValue", calculateMax(values));
            stats.put("minValue", calculateMin(values));
            stats.put("medianValue", calculateMedian(values));
        } else {
            stats.put("totalValue", 0.0);
            stats.put("averageValue", 0.0);
            stats.put("maxValue", 0.0);
            stats.put("minValue", 0.0);
            stats.put("medianValue", 0.0);
        }

        // Tendances
        stats.put("trend", calculateTrend(progressList));
        stats.put("improvement", calculateImprovement(progressList));
        stats.put("targetReachRate", calculateTargetReachRate(progressList, habit.getTargetValue()));

        // Progression récente
        stats.put("lastProgressDate", getLastProgressDate(progressList));
        stats.put("daysSinceLastProgress", calculateDaysSinceLastProgress(habitId));

        log.debug("📊 Statistiques calculées pour habitude {}", habitId);
        return stats;
    }

    /**
     * Calcule les tendances mensuelles
     */
    public Map<String, Object> calculateMonthlyTrends(Long userId) {
        ValidationUtils.validateId(userId, "utilisateur");

        Map<String, Object> trends = new HashMap<>();

        // Récupérer les données des 6 derniers mois
        LocalDate startDate = LocalDate.now().minusMonths(6);
        List<Progress> progressList = progressRepository.findByUserIdAndDateBetween(userId, startDate, LocalDate.now());

        Map<String, Integer> monthlyProgress = groupProgressByMonth(progressList);
        Map<String, Double> monthlyAverages = calculateMonthlyAverages(userId, 6);

        trends.put("monthlyProgress", monthlyProgress);
        trends.put("monthlyAverages", monthlyAverages);
        trends.put("overallTrend", calculateOverallTrend(monthlyProgress));
        trends.put("mostActiveMonth", findMostActiveMonth(monthlyProgress));
        trends.put("growthRate", calculateGrowthRate(monthlyProgress));

        log.debug("📈 Tendances mensuelles calculées pour utilisateur {}", userId);
        return trends;
    }

    /**
     * Compare les performances entre habitudes
     */
    public Map<String, Object> compareHabits(Long userId) {
        ValidationUtils.validateId(userId, "utilisateur");

        List<Habit> userHabits = habitRepository.findByUserIdAndIsActive(userId, true);
        Map<String, Object> comparison = new HashMap<>();

        List<Map<String, Object>> habitStats = new ArrayList<>();

        for (Habit habit : userHabits) {
            Map<String, Object> habitData = new HashMap<>();

            long progressCount = progressRepository.countByHabitId(habit.getId());
            double consistency = calculateHabitConsistency(habit.getId(), 30);

            habitData.put("id", habit.getId());
            habitData.put("title", habit.getTitle());
            habitData.put("category", habit.getCategory());
            habitData.put("progressCount", progressCount);
            habitData.put("consistency", consistency);
            habitData.put("currentStreak", calculateHabitCurrentStreak(habit.getId()));

            habitStats.add(habitData);
        }

        // Trier par consistance
        habitStats.sort((h1, h2) -> Double.compare(
                (Double) h2.get("consistency"),
                (Double) h1.get("consistency")
        ));

        comparison.put("habits", habitStats);
        comparison.put("bestHabit", habitStats.isEmpty() ? null : habitStats.get(0));
        comparison.put("totalHabits", habitStats.size());
        comparison.put("averageConsistency", calculateAverageConsistency(habitStats));

        log.debug("🔄 Comparaison habitudes calculée pour utilisateur {}", userId);
        return comparison;
    }

    // === MÉTHODES PRIVÉES DE CALCUL ===

    private Habit getHabit(Long habitId) {
        return habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habitude non trouvée: " + habitId));
    }

    /**
     * Calcule la moyenne de progression par jour
     */
    private double calculateAverageProgressPerDay(List<Progress> progressList, int days) {
        return days > 0 ? (double) progressList.size() / days : 0.0;
    }

    /**
     * Calcule le taux de completion (% de jours avec progression)
     */
    private double calculateCompletionRate(List<Progress> progressList, int days) {
        Set<LocalDate> uniqueDates = progressList.stream()
                .map(Progress::getDate)
                .collect(Collectors.toSet());

        return days > 0 ? (double) uniqueDates.size() / days * 100 : 0.0;
    }

    /**
     * Calcule la série actuelle d'un utilisateur
     */
    private int calculateUserCurrentStreak(Long userId) {
        LocalDate currentDate = LocalDate.now();
        int streak = 0;

        // Vérifier jour par jour en remontant
        for (int i = 0; i < 365; i++) { // Maximum 1 an
            List<Progress> dayProgress = progressRepository.findByUserIdAndDate(userId, currentDate);

            if (!dayProgress.isEmpty()) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else if (i == 0) {
                // Pas de progression aujourd'hui = pas de série
                break;
            } else {
                // Première interruption trouvée
                break;
            }
        }

        return streak;
    }

    /**
     * Calcule la série actuelle d'une habitude
     */
    private int calculateHabitCurrentStreak(Long habitId) {
        LocalDate currentDate = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 365; i++) {
            boolean hasProgress = progressRepository.existsByHabitIdAndDate(habitId, currentDate);

            if (hasProgress) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else if (i == 0) {
                break;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Calcule la plus longue série d'une habitude
     */
    private int calculateLongestStreak(Long habitId) {
        List<Progress> allProgress = progressRepository.findTop30ByHabitIdOrderByDateDesc(habitId);

        if (allProgress.isEmpty()) return 0;

        List<LocalDate> dates = allProgress.stream()
                .map(Progress::getDate)
                .sorted()
                .toList();

        int maxStreak = 0;
        int currentStreak = 1;

        for (int i = 1; i < dates.size(); i++) {
            LocalDate prevDate = dates.get(i - 1);
            LocalDate currentDate = dates.get(i);

            if (ChronoUnit.DAYS.between(prevDate, currentDate) == 1) {
                currentStreak++;
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = 1;
            }
        }

        return Math.max(maxStreak, currentStreak);
    }

    /**
     * Calcule la tendance (positive/négative/stable)
     */
    private String calculateTrend(List<Progress> progressList) {
        if (progressList.size() < 2) return "insufficient_data";

        List<Progress> sortedProgress = progressList.stream()
                .sorted(Comparator.comparing(Progress::getDate))
                .toList();

        int midPoint = sortedProgress.size() / 2;

        double firstHalfAvg = sortedProgress.subList(0, midPoint).stream()
                .mapToDouble(Progress::getValue)
                .average()
                .orElse(0);

        double secondHalfAvg = sortedProgress.subList(midPoint, sortedProgress.size()).stream()
                .mapToDouble(Progress::getValue)
                .average()
                .orElse(0);

        double difference = secondHalfAvg - firstHalfAvg;

        if (difference > 0.1) return "positive";
        if (difference < -0.1) return "negative";
        return "stable";
    }

    /**
     * Calcule le pourcentage d'amélioration
     */
    private double calculateImprovement(List<Progress> progressList) {
        if (progressList.size() < 2) return 0.0;

        List<Progress> sortedProgress = progressList.stream()
                .sorted(Comparator.comparing(Progress::getDate))
                .toList();

        double firstValue = sortedProgress.get(0).getValue();
        double lastValue = sortedProgress.get(sortedProgress.size() - 1).getValue();

        if (firstValue == 0) return 0.0;

        return ((lastValue - firstValue) / firstValue) * 100;
    }

    /**
     * Calcule le taux d'atteinte de l'objectif
     */
    private double calculateTargetReachRate(List<Progress> progressList, Double targetValue) {
        if (targetValue == null || targetValue <= 0 || progressList.isEmpty()) {
            return 0.0;
        }

        long targetReached = progressList.stream()
                .mapToLong(p -> p.getValue() >= targetValue ? 1 : 0)
                .sum();

        return ((double) targetReached / progressList.size()) * 100;
    }

    /**
     * Calcule la consistance d'une habitude
     */
    private double calculateHabitConsistency(Long habitId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(habitId, startDate, LocalDate.now());

        Set<LocalDate> uniqueDates = progressList.stream()
                .map(Progress::getDate)
                .collect(Collectors.toSet());

        return days > 0 ? ((double) uniqueDates.size() / days) * 100 : 0.0;
    }

    /**
     * Calcule le score de consistance global
     */
    private double calculateConsistencyScore(Long userId, int days) {
        List<Habit> activeHabits = habitRepository.findByUserIdAndIsActive(userId, true);

        if (activeHabits.isEmpty()) return 0.0;

        double totalConsistency = activeHabits.stream()
                .mapToDouble(habit -> calculateHabitConsistency(habit.getId(), days))
                .sum();

        return totalConsistency / activeHabits.size();
    }

    /**
     * Trouve la meilleure semaine
     */
    private String findBestWeek(Long userId) {
        // Implémentation simplifiée
        LocalDate startDate = LocalDate.now().minusWeeks(8);
        List<Progress> progressList = progressRepository.findByUserIdAndDateBetween(userId, startDate, LocalDate.now());

        Map<String, Integer> weeklyCount = new HashMap<>();

        for (Progress progress : progressList) {
            LocalDate date = progress.getDate();
            int year = date.getYear();
            int weekOfYear = date.getDayOfYear() / 7 + 1;
            String weekKey = String.format("%d-S%02d", year, weekOfYear);

            weeklyCount.merge(weekKey, 1, Integer::sum);
        }

        return weeklyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("aucune");
    }

    /**
     * Groupe les progressions par mois
     */
    private Map<String, Integer> groupProgressByMonth(List<Progress> progressList) {
        Map<String, Integer> monthlyProgress = new HashMap<>();

        for (Progress progress : progressList) {
            String monthKey = progress.getDate().getYear() + "-" +
                    String.format("%02d", progress.getDate().getMonthValue());
            monthlyProgress.merge(monthKey, 1, Integer::sum);
        }

        return monthlyProgress;
    }

    /**
     * Calcule les moyennes mensuelles
     */
    private Map<String, Double> calculateMonthlyAverages(Long userId, int months) {
        Map<String, Double> monthlyAverages = new HashMap<>();

        for (int i = 0; i < months; i++) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            List<Progress> monthProgress = progressRepository.findByUserIdAndDateBetween(userId, monthStart, monthEnd);

            String monthKey = monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue());

            if (!monthProgress.isEmpty()) {
                double average = monthProgress.stream()
                        .mapToDouble(Progress::getValue)
                        .average()
                        .orElse(0.0);
                monthlyAverages.put(monthKey, Math.round(average * 100.0) / 100.0);
            } else {
                monthlyAverages.put(monthKey, 0.0);
            }
        }

        return monthlyAverages;
    }

    /**
     * Calcule la tendance globale
     */
    private String calculateOverallTrend(Map<String, Integer> monthlyProgress) {
        if (monthlyProgress.size() < 2) return "insufficient_data";

        List<Map.Entry<String, Integer>> sorted = monthlyProgress.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        int firstHalf = sorted.subList(0, sorted.size() / 2).stream()
                .mapToInt(Map.Entry::getValue)
                .sum();

        int secondHalf = sorted.subList(sorted.size() / 2, sorted.size()).stream()
                .mapToInt(Map.Entry::getValue)
                .sum();

        if (secondHalf > firstHalf * 1.1) return "croissante";
        if (secondHalf < firstHalf * 0.9) return "décroissante";
        return "stable";
    }

    /**
     * Trouve le mois le plus actif
     */
    private String findMostActiveMonth(Map<String, Integer> monthlyProgress) {
        return monthlyProgress.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("aucun");
    }

    /**
     * Calcule le taux de croissance
     */
    private double calculateGrowthRate(Map<String, Integer> monthlyProgress) {
        if (monthlyProgress.size() < 2) return 0.0;

        List<Integer> values = monthlyProgress.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();

        int firstValue = values.get(0);
        int lastValue = values.get(values.size() - 1);

        if (firstValue == 0) return 0.0;

        return ((double) (lastValue - firstValue) / firstValue) * 100;
    }

    /**
     * Calcule la consistance moyenne
     */
    private double calculateAverageConsistency(List<Map<String, Object>> habitStats) {
        if (habitStats.isEmpty()) return 0.0;

        double totalConsistency = habitStats.stream()
                .mapToDouble(habit -> (Double) habit.get("consistency"))
                .sum();

        return Math.round((totalConsistency / habitStats.size()) * 100.0) / 100.0;
    }

    /**
     * Récupère la date de dernière progression
     */
    private LocalDate getLastProgressDate(List<Progress> progressList) {
        return progressList.stream()
                .map(Progress::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * Calcule les jours depuis la dernière progression
     */
    private int calculateDaysSinceLastProgress(Long habitId) {
        Optional<Progress> lastProgress = progressRepository.findTop1ByHabitIdOrderByDateDesc(habitId);

        return lastProgress.map(progress -> (int) ChronoUnit.DAYS.between(progress.getDate(), LocalDate.now())).orElse(-1);

    }

    // === MÉTHODES UTILITAIRES MATHÉMATIQUES ===

    private double calculateSum(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }

    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double calculateMax(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private double calculateMin(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private double calculateMedian(List<Double> values) {
        List<Double> sorted = values.stream().sorted().toList();
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }
}