package com.habittracker.service;

import com.habittracker.entity.Habit;
import com.habittracker.entity.Progress;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service pour la génération de données de graphiques Chart.js
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartService {

    private final ProgressRepository progressRepository;
    private final HabitRepository habitRepository;

    /**
     * Génère les données de graphique en ligne pour une habitude
     */
    public Map<String, Object> generateLineChartData(Long habitId, int days) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "nombre de jours");

        Habit habit = getHabit(habitId);
        List<Progress> progressList = getProgressForPeriod(habitId, days);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("type", "line");
        chartData.put("data", buildLineDataset(progressList, habit, days));
        chartData.put("options", getLineChartOptions(habit));

        log.debug("📊 Données graphique ligne générées pour habitude {}", habitId);
        return chartData;
    }

    /**
     * Génère les données de graphique en barres pour une habitude
     */
    public Map<String, Object> generateBarChartData(Long habitId, int days) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "nombre de jours");

        Habit habit = getHabit(habitId);
        List<Progress> progressList = getProgressForPeriod(habitId, days);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("type", "bar");
        chartData.put("data", buildBarDataset(progressList, habit, days));
        chartData.put("options", getBarChartOptions(habit));

        log.debug("📊 Données graphique barres générées pour habitude {}", habitId);
        return chartData;
    }

    /**
     * Génère un graphique de progression hebdomadaire
     */
    public Map<String, Object> generateWeeklyProgressChart(Long habitId) {
        ValidationUtils.validateId(habitId, "habitude");

        Habit habit = getHabit(habitId);
        Map<String, Double> weeklyData = calculateWeeklyProgress(habitId);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("type", "bar");
        chartData.put("data", buildWeeklyDataset(weeklyData, habit));
        chartData.put("options", getWeeklyChartOptions(habit));

        log.debug("📊 Données progression hebdomadaire générées pour habitude {}", habitId);
        return chartData;
    }

    /**
     * Génère un graphique de heatmap pour la régularité
     */
    public Map<String, Object> generateHeatmapData(Long habitId, int days) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "nombre de jours");

        List<Progress> progressList = getProgressForPeriod(habitId, days);
        Map<String, Integer> heatmapData = buildHeatmapData(progressList, days);

        Map<String, Object> result = new HashMap<>();
        result.put("type", "heatmap");
        result.put("data", heatmapData);
        result.put("totalDays", days);
        result.put("activeDays", heatmapData.size());

        log.debug("🔥 Données heatmap générées pour habitude {}", habitId);
        return result;
    }

    // === MÉTHODES PRIVÉES ===

    private Habit getHabit(Long habitId) {
        return habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habitude non trouvée: " + habitId));
    }

    private List<Progress> getProgressForPeriod(Long habitId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return progressRepository.findByHabitIdAndDateBetween(habitId, startDate, endDate);
    }

    /**
     * Construit le dataset pour graphique ligne
     */
    private Map<String, Object> buildLineDataset(List<Progress> progressList, Habit habit, int days) {
        // Créer une carte date -> valeur
        Map<LocalDate, Double> progressMap = new HashMap<>();
        progressList.forEach(p -> progressMap.put(p.getDate(), p.getValue()));

        // Générer les labels (dates) et données
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        List<Double> targetLine = new ArrayList<>();

        LocalDate currentDate = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            labels.add(currentDate.format(DateTimeFormatter.ofPattern("dd/MM")));
            values.add(progressMap.getOrDefault(currentDate, 0.0));

            // Ligne d'objectif si défini
            if (habit.getTargetValue() != null) {
                targetLine.add(habit.getTargetValue());
            }

            currentDate = currentDate.plusDays(1);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);

        List<Map<String, Object>> datasets = new ArrayList<>();

        // Dataset principal
        Map<String, Object> mainDataset = new HashMap<>();
        mainDataset.put("label", habit.getTitle());
        mainDataset.put("data", values);
        mainDataset.put("borderColor", "#3B82F6");
        mainDataset.put("backgroundColor", "#3B82F6");
        mainDataset.put("tension", 0.3);
        datasets.add(mainDataset);

        // Dataset objectif si défini
        if (habit.getTargetValue() != null && !targetLine.isEmpty()) {
            Map<String, Object> targetDataset = new HashMap<>();
            targetDataset.put("label", "Objectif (" + habit.getTargetValue() + " " + habit.getUnit() + ")");
            targetDataset.put("data", targetLine);
            targetDataset.put("borderColor", "#EF4444");
            targetDataset.put("backgroundColor", "transparent");
            targetDataset.put("borderDash", Arrays.asList(5, 5));
            datasets.add(targetDataset);
        }

        data.put("datasets", datasets);
        return data;
    }

    /**
     * Construit le dataset pour graphique barres
     */
    private Map<String, Object> buildBarDataset(List<Progress> progressList, Habit habit, int days) {
        Map<LocalDate, Double> progressMap = new HashMap<>();
        progressList.forEach(p -> progressMap.put(p.getDate(), p.getValue()));

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        LocalDate currentDate = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            labels.add(currentDate.format(DateTimeFormatter.ofPattern("dd/MM")));
            double value = progressMap.getOrDefault(currentDate, 0.0);
            values.add(value);

            // Couleur selon l'atteinte de l'objectif
            if (habit.getTargetValue() != null) {
                colors.add(value >= habit.getTargetValue() ? "#10B981" : "#F59E0B");
            } else {
                colors.add("#3B82F6");
            }

            currentDate = currentDate.plusDays(1);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);

        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", habit.getTitle() + " (" + habit.getUnit() + ")");
        dataset.put("data", values);
        dataset.put("backgroundColor", colors);

        data.put("datasets", List.of(dataset));
        return data;
    }

    /**
     * Construit le dataset pour progression hebdomadaire
     */
    private Map<String, Object> buildWeeklyDataset(Map<String, Double> weeklyData, Habit habit) {
        List<String> labels = new ArrayList<>(weeklyData.keySet());
        Collections.sort(labels);

        List<Double> values = new ArrayList<>();
        labels.forEach(label -> values.add(weeklyData.get(label)));

        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);

        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Moyenne hebdomadaire");
        dataset.put("data", values);
        dataset.put("backgroundColor", "#8B5CF6");

        data.put("datasets", List.of(dataset));
        return data;
    }

    /**
     * Calcule la progression hebdomadaire
     */
    private Map<String, Double> calculateWeeklyProgress(Long habitId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(8); // 8 semaines

        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(habitId, startDate, endDate);

        Map<String, List<Double>> weeklyGroups = new HashMap<>();

        for (Progress progress : progressList) {
            // Calculer la semaine (format "2024-S01")
            LocalDate date = progress.getDate();
            int year = date.getYear();
            int weekOfYear = date.getDayOfYear() / 7 + 1;
            String weekKey = String.format("%d-S%02d", year, weekOfYear);

            weeklyGroups.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(progress.getValue());
        }

        // Calculer les moyennes
        Map<String, Double> weeklyAverages = new HashMap<>();
        weeklyGroups.forEach((week, values) -> {
            double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            weeklyAverages.put(week, Math.round(average * 100.0) / 100.0);
        });

        return weeklyAverages;
    }

    /**
     * Construit les données de heatmap
     */
    private Map<String, Integer> buildHeatmapData(List<Progress> progressList, int days) {
        Map<String, Integer> heatmapData = new HashMap<>();

        LocalDate currentDate = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            String dateKey = currentDate.toString();
            LocalDate finalCurrentDate = currentDate;
            boolean hasProgress = progressList.stream()
                    .anyMatch(p -> p.getDate().equals(finalCurrentDate));

            heatmapData.put(dateKey, hasProgress ? 1 : 0);
            currentDate = currentDate.plusDays(1);
        }

        return heatmapData;
    }

    /**
     * Options pour graphique ligne
     */
    private Map<String, Object> getLineChartOptions(Habit habit) {
        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("maintainAspectRatio", false);

        Map<String, Object> scales = new HashMap<>();

        Map<String, Object> y = new HashMap<>();
        y.put("beginAtZero", true);
        y.put("title", Map.of("display", true, "text", habit.getUnit()));
        scales.put("y", y);

        options.put("scales", scales);

        Map<String, Object> plugins = new HashMap<>();
        plugins.put("legend", Map.of("display", true));
        plugins.put("title", Map.of("display", true, "text", "Évolution - " + habit.getTitle()));
        options.put("plugins", plugins);

        return options;
    }

    /**
     * Options pour graphique barres
     */
    private Map<String, Object> getBarChartOptions(Habit habit) {
        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("maintainAspectRatio", false);

        Map<String, Object> scales = new HashMap<>();

        Map<String, Object> y = new HashMap<>();
        y.put("beginAtZero", true);
        y.put("title", Map.of("display", true, "text", habit.getUnit()));
        scales.put("y", y);

        options.put("scales", scales);

        Map<String, Object> plugins = new HashMap<>();
        plugins.put("legend", Map.of("display", false));
        plugins.put("title", Map.of("display", true, "text", "Progression quotidienne - " + habit.getTitle()));
        options.put("plugins", plugins);

        return options;
    }

    /**
     * Options pour graphique hebdomadaire
     */
    private Map<String, Object> getWeeklyChartOptions(Habit habit) {
        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("maintainAspectRatio", false);

        Map<String, Object> scales = new HashMap<>();

        Map<String, Object> y = new HashMap<>();
        y.put("beginAtZero", true);
        y.put("title", Map.of("display", true, "text", "Moyenne " + habit.getUnit()));
        scales.put("y", y);

        options.put("scales", scales);

        Map<String, Object> plugins = new HashMap<>();
        plugins.put("legend", Map.of("display", false));
        plugins.put("title", Map.of("display", true, "text", "Progression hebdomadaire - " + habit.getTitle()));
        options.put("plugins", plugins);

        return options;
    }
}