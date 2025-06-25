package com.habittracker.service;

import com.habittracker.dto.progress.*;
import com.habittracker.entity.Habit;
import com.habittracker.entity.Progress;
import com.habittracker.exception.BusinessException;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final HabitRepository habitRepository;
    private final UserSecurityService userSecurityService;

    /**
     * Trouve une progression par son ID
     */
    public ProgressResponse findById(Long id) {
        ValidationUtils.validateId(id, "progression");

        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progression", id));

        return enrichProgressResponse(ProgressResponse.fromEntity(progress));
    }

    /**
     * Récupère les progressions d'une habitude
     */
    public Page<ProgressResponse> findHabitProgress(Long habitId, Pageable pageable) {
        ValidationUtils.validateId(habitId, "habitude");

        Page<Progress> progressPage = progressRepository.findByHabitId(habitId, pageable);
        return progressPage.map(this::toEnrichedResponse);
    }

    /**
     * Récupère les progressions d'une habitude sur une période
     */
    public List<ProgressResponse> findHabitProgressByPeriod(Long habitId, LocalDate startDate, LocalDate endDate) {
        ValidationUtils.validateId(habitId, "habitude");
        validateDateRange(startDate, endDate);

        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(
                habitId, startDate, endDate);

        return progressList.stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    /**
     * Récupère les progressions d'un utilisateur
     */
    public Page<ProgressResponse> findUserProgress(Long userId, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");

        Page<Progress> progressPage = progressRepository.findByUserId(userId, pageable);
        return progressPage.map(this::toEnrichedResponse);
    }

    /**
     * Récupère les progressions du jour pour un utilisateur
     */
    public List<ProgressResponse> findTodayProgress(Long userId) {
        ValidationUtils.validateId(userId, "utilisateur");

        List<Progress> todayProgress = progressRepository.findByUserIdAndDate(userId, LocalDate.now());
        return todayProgress.stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    /**
     * Crée une nouvelle progression
     */
    @Transactional
    public ProgressResponse createProgress(Long habitId, ProgressCreateRequest request, Authentication authentication) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validateNotNull(request, "Données de progression");

        // Vérifier que l'habitude existe et appartient à l'utilisateur
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitude", habitId));

        userSecurityService.validateUserAccess(habit.getUserId(), authentication);

        // Vérifier qu'il n'y a pas déjà une progression pour cette date
        if (progressRepository.existsByUserIdAndHabitIdAndDate(
                habit.getUserId(), habitId, request.getDate())) {
            throw new BusinessException("Une progression existe déjà pour cette date");
        }

        // Créer la progression
        Progress progress = new Progress(habit.getUserId(), habitId, request.getDate(), request.getValue());
        progress.setNote(request.getNote());
        progress = progressRepository.save(progress);

        log.info("Progression créée: habitude {}, date {}, valeur {}",
                habitId, request.getDate(), request.getValue());

        return enrichProgressResponse(ProgressResponse.fromEntity(progress));
    }

    /**
     * Met à jour une progression
     */
    @Transactional
    public ProgressResponse updateProgress(Long id, ProgressUpdateRequest request, Authentication authentication) {
        ValidationUtils.validateId(id, "progression");
        ValidationUtils.validateNotNull(request, "Données de mise à jour");

        if (!request.hasChanges()) {
            throw new BusinessException("Aucune modification fournie");
        }

        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progression", id));

        userSecurityService.validateUserAccess(progress.getUserId(), authentication);

        // Mettre à jour les champs
        if (request.getDate() != null) {
            progress.setDate(request.getDate());
        }
        if (request.getValue() != null) {
            progress.setValue(request.getValue());
        }
        if (request.getNote() != null) {
            progress.setNote(request.getNote());
        }

        progress = progressRepository.save(progress);

        log.info("Progression mise à jour: ID {}", id);

        return enrichProgressResponse(ProgressResponse.fromEntity(progress));
    }

    /**
     * Supprime une progression
     */
    @Transactional
    public void deleteProgress(Long id, Authentication authentication) {
        ValidationUtils.validateId(id, "progression");

        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progression", id));

        userSecurityService.validateUserAccess(progress.getUserId(), authentication);

        progressRepository.delete(progress);

        log.info("Progression supprimée: ID {}", id);
    }

    /**
     * Récupère les statistiques d'une habitude
     */
    public ProgressStatsResponse getHabitStatistics(Long habitId, int days) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "Nombre de jours");

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitude", habitId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(
                habitId, startDate, endDate);

        return buildStatsResponse(habit, progressList, startDate, endDate, days);
    }

    /**
     * Récupère les données pour les graphiques
     */
    public Map<String, Object> getChartData(Long habitId, int days, String chartType) {
        ValidationUtils.validateId(habitId, "habitude");
        ValidationUtils.validatePositive(days, "Nombre de jours");

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitude", habitId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Progress> progressList = progressRepository.findByHabitIdAndDateBetween(
                habitId, startDate, endDate);

        return buildChartData(habit, progressList, chartType);
    }

    /**
     * Récupère un résumé des progressions d'un utilisateur
     */
    public Map<String, Object> getProgressSummary(Long userId, int days) {
        ValidationUtils.validateId(userId, "utilisateur");
        ValidationUtils.validatePositive(days, "Nombre de jours");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Progress> progressList = progressRepository.findByUserIdAndDateBetween(
                userId, startDate, endDate);

        return buildProgressSummary(progressList, days);
    }

    // === MÉTHODES PRIVÉES ===

    private ProgressResponse toEnrichedResponse(Progress progress) {
        return enrichProgressResponse(ProgressResponse.fromEntity(progress));
    }

    private ProgressResponse enrichProgressResponse(ProgressResponse response) {
        // Récupérer les informations de l'habitude
        Habit habit = habitRepository.findById(response.getHabitId()).orElse(null);

        if (habit != null) {
            return response.withHabitInfo(habit.getTitle(), habit.getUnit(), habit.getTargetValue());
        }

        return response;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("La date de début doit être antérieure à la date de fin");
        }
        if (startDate.isBefore(LocalDate.now().minusYears(1))) {
            throw new BusinessException("La période ne peut pas dépasser un an");
        }
    }

    private ProgressStatsResponse buildStatsResponse(Habit habit, List<Progress> progressList,
                                                     LocalDate startDate, LocalDate endDate, int totalDays) {

        // Calculs statistiques simples
        int totalEntries = progressList.size();
        double completionRate = totalDays > 0 ? (double) totalEntries / totalDays * 100 : 0;

        double totalValue = progressList.stream().mapToDouble(Progress::getValue).sum();
        double averageValue = totalEntries > 0 ? totalValue / totalEntries : 0;
        double maxValue = progressList.stream().mapToDouble(Progress::getValue).max().orElse(0);
        double minValue = progressList.stream().mapToDouble(Progress::getValue).min().orElse(0);

        Progress lastProgress = progressList.stream()
                .max((p1, p2) -> p1.getDate().compareTo(p2.getDate()))
                .orElse(null);

        double lastValue = lastProgress != null ? lastProgress.getValue() : 0;
        LocalDate lastEntryDate = lastProgress != null ? lastProgress.getDate() : null;

        // Calcul des objectifs atteints
        int daysTargetReached = 0;
        if (habit.getTargetValue() != null && habit.getTargetValue() > 0) {
            daysTargetReached = (int) progressList.stream()
                    .filter(p -> p.getValue() >= habit.getTargetValue())
                    .count();
        }
        double targetReachRate = totalEntries > 0 ? (double) daysTargetReached / totalEntries * 100 : 0;

        // Calculs simples
        int consecutiveDays = calculateConsecutiveDays(progressList);
        double consistencyScore = completionRate; // Simplification
        double progressTrend = calculateSimpleTrend(progressList);
        boolean improvementDetected = progressTrend > 0;

        int daysSinceLastEntry = lastEntryDate != null ?
                (int) java.time.temporal.ChronoUnit.DAYS.between(lastEntryDate, LocalDate.now()) : -1;

        return ProgressStatsResponse.builder()
                .habitId(habit.getId())
                .habitTitle(habit.getTitle())
                .habitUnit(habit.getUnit())
                .habitTarget(habit.getTargetValue())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .totalEntries(totalEntries)
                .consecutiveDays(consecutiveDays)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .totalValue(Math.round(totalValue * 100.0) / 100.0)
                .averageValue(Math.round(averageValue * 100.0) / 100.0)
                .maxValue(maxValue)
                .minValue(minValue)
                .lastValue(lastValue)
                .progressTrend(progressTrend)
                .improvementDetected(improvementDetected)
                .daysTargetReached(daysTargetReached)
                .targetReachRate(Math.round(targetReachRate * 100.0) / 100.0)
                .consistencyScore(consistencyScore)
                .lastEntryDate(lastEntryDate)
                .daysSinceLastEntry(daysSinceLastEntry)
                .build();
    }

    private Map<String, Object> buildChartData(Habit habit, List<Progress> progressList, String chartType) {
        Map<String, Object> chartData = new HashMap<>();

        chartData.put("chartType", chartType);
        chartData.put("habitTitle", habit.getTitle());
        chartData.put("habitUnit", habit.getUnit());
        chartData.put("habitTarget", habit.getTargetValue());

        List<Map<String, Object>> dataPoints = progressList.stream()
                .map(progress -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", progress.getDate().toString());
                    point.put("value", progress.getValue());
                    point.put("note", progress.getNote());
                    if (habit.getTargetValue() != null) {
                        point.put("targetReached", progress.getValue() >= habit.getTargetValue());
                    }
                    return point;
                })
                .toList();

        chartData.put("data", dataPoints);

        return chartData;
    }

    private Map<String, Object> buildProgressSummary(List<Progress> progressList, int days) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalEntries", progressList.size());
        summary.put("averageEntriesPerDay", days > 0 ? (double) progressList.size() / days : 0);

        // Grouper par date
        Map<LocalDate, Integer> dailyProgress = new HashMap<>();
        progressList.forEach(progress -> {
            dailyProgress.merge(progress.getDate(), 1, Integer::sum);
        });

        summary.put("activeDays", dailyProgress.size());
        summary.put("dailyProgress", dailyProgress);

        return summary;
    }

    private int calculateConsecutiveDays(List<Progress> progressList) {
        if (progressList.isEmpty()) return 0;

        List<LocalDate> dates = progressList.stream()
                .map(Progress::getDate)
                .distinct()
                .sorted(java.util.Collections.reverseOrder())
                .toList();

        int consecutive = 0;
        LocalDate currentDate = LocalDate.now();

        for (LocalDate date : dates) {
            if (date.equals(currentDate)) {
                consecutive++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return consecutive;
    }

    private double calculateSimpleTrend(List<Progress> progressList) {
        if (progressList.size() < 2) return 0;

        List<Progress> sortedProgress = progressList.stream()
                .sorted((p1, p2) -> p1.getDate().compareTo(p2.getDate()))
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

        return secondHalfAvg - firstHalfAvg;
    }
}