package com.habittracker.service;

import com.habittracker.dto.achievement.*;
import com.habittracker.entity.Achievement;
import com.habittracker.entity.User;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.repository.*;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final ProgressRepository progressRepository;

    /**
     * Récupère tous les achievements d'un utilisateur
     */
    public Page<AchievementResponse> findUserAchievements(Long userId, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");

        Page<Achievement> achievementPage = achievementRepository.findByUserIdOrderByUnlockedAtDesc(userId, pageable);
        return achievementPage.map(this::toEnrichedResponse);
    }

    /**
     * Récupère les achievements par type
     */
    public List<AchievementResponse> findUserAchievementsByType(Long userId, Achievement.AchievementType type) {
        ValidationUtils.validateId(userId, "utilisateur");

        List<Achievement> achievements = achievementRepository.findByUserIdAndAchievementType(userId, type);
        return achievements.stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    /**
     * Récupère les achievements récents (7 derniers jours)
     */
    public List<AchievementResponse> findRecentAchievements(Long userId, int days) {
        ValidationUtils.validateId(userId, "utilisateur");

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Achievement> recentAchievements = achievementRepository.findByUserIdAndUnlockedAtAfter(userId, since);

        return recentAchievements.stream()
                .map(achievement -> toEnrichedResponse(achievement).withEnrichment(true, null, null))
                .toList();
    }

    /**
     * Vérifie et débloque de nouveaux achievements
     */
    @Transactional
    public AchievementCheckResponse checkAndUnlockAchievements(AchievementCheckRequest request) {
        ValidationUtils.validateId(request.getUserId(), "utilisateur");

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", request.getUserId()));

        List<Achievement> newAchievements = new ArrayList<>();

        // Vérifier les différents types d'achievements
        newAchievements.addAll(checkConsistencyAchievements(user));
        newAchievements.addAll(checkMilestoneAchievements(user));
        newAchievements.addAll(checkStreakAchievements(user));
        newAchievements.addAll(checkDedicationAchievements(user));
        newAchievements.addAll(checkVarietyAchievements(user));

        if (newAchievements.isEmpty()) {
            return AchievementCheckResponse.noNewAchievements(request.getUserId());
        }

        // Sauvegarder les nouveaux achievements
        List<Achievement> savedAchievements = achievementRepository.saveAll(newAchievements);
        List<AchievementResponse> achievementResponses = savedAchievements.stream()
                .map(this::toEnrichedResponse)
                .toList();

        log.info("🏆 {} nouveaux achievements débloqués pour l'utilisateur {}",
                newAchievements.size(), user.getUsername());

        return AchievementCheckResponse.withNewAchievements(request.getUserId(), achievementResponses);
    }

    /**
     * Récupère un résumé des achievements d'un utilisateur
     */
    public AchievementSummaryResponse getUserAchievementSummary(Long userId) {
        ValidationUtils.validateId(userId, "utilisateur");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        List<Achievement> userAchievements = achievementRepository.findByUserId(userId);

        // Calculs des statistiques
        int totalAchievements = userAchievements.size();
        int totalPossible = 50; // Nombre total d'achievements possibles
        double completionPercentage = totalPossible > 0 ? (double) totalAchievements / totalPossible * 100 : 0;

        // Grouper par type
        Map<String, Integer> achievementsByType = new HashMap<>();
        for (Achievement achievement : userAchievements) {
            String type = achievement.getAchievementType().name();
            achievementsByType.merge(type, 1, Integer::sum);
        }

        // Dernier achievement
        Achievement lastAchievement = userAchievements.stream()
                .max((a1, a2) -> a1.getUnlockedAt().compareTo(a2.getUnlockedAt()))
                .orElse(null);

        // Achievements récents
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        int achievementsThisWeek = (int) userAchievements.stream()
                .filter(a -> a.getUnlockedAt().isAfter(weekAgo))
                .count();

        int achievementsThisMonth = (int) userAchievements.stream()
                .filter(a -> a.getUnlockedAt().isAfter(monthAgo))
                .count();

        // Calcul de rareté (simplifié)
        int common = Math.min(totalAchievements, 10);
        int rare = Math.min(Math.max(0, totalAchievements - 10), 10);
        int epic = Math.min(Math.max(0, totalAchievements - 20), 10);
        int legendary = Math.max(0, totalAchievements - 30);

        return AchievementSummaryResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalAchievements(totalAchievements)
                .totalPossibleAchievements(totalPossible)
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .achievementsByType(achievementsByType)
                .lastAchievementDate(lastAchievement != null ? lastAchievement.getUnlockedAt() : null)
                .lastAchievementName(lastAchievement != null ? lastAchievement.getName() : null)
                .achievementsThisWeek(achievementsThisWeek)
                .achievementsThisMonth(achievementsThisMonth)
                .commonAchievements(common)
                .rareAchievements(rare)
                .epicAchievements(epic)
                .legendaryAchievements(legendary)
                .nextPossibleAchievement("Prochain objectif disponible")
                .progressToNextAchievement(calculateProgressToNext(userId))
                .build();
    }

    // === MÉTHODES PRIVÉES ===

    private AchievementResponse toEnrichedResponse(Achievement achievement) {
        // Calcul de la rareté (pourcentage d'utilisateurs qui ont ce badge)
        long totalUsers = userRepository.count();
        long usersWithThisAchievement = achievementRepository.countByUserIdAndAchievementType(
                achievement.getUserId(), achievement.getAchievementType());

        int rarity = totalUsers > 0 ? (int) ((double) usersWithThisAchievement / totalUsers * 100) : 0;

        // Vérifier si c'est récent (moins de 3 jours)
        boolean isNew = achievement.getUnlockedAt().isAfter(LocalDateTime.now().minusDays(3));

        String category = mapTypeToCategory(achievement.getAchievementType());

        return AchievementResponse.fromEntity(achievement)
                .withEnrichment(isNew, rarity, category);
    }

    private String mapTypeToCategory(Achievement.AchievementType type) {
        return switch (type) {
            case CONSISTENCY, STREAK -> "Régularité";
            case MILESTONE, DEDICATION -> "Progression";
            case OVERACHIEVER -> "Performance";
            case VARIETY -> "Diversité";
            case EARLY_BIRD -> "Timing";
            case PERSEVERANCE -> "Persévérance";
        };
    }

    // === VÉRIFICATIONS DES ACHIEVEMENTS ===

    private List<Achievement> checkConsistencyAchievements(User user) {
        List<Achievement> newAchievements = new ArrayList<>();

        // 7 jours consécutifs
        if (!achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Semaine parfaite", Achievement.AchievementType.CONSISTENCY)) {

            long consecutiveDays = calculateConsecutiveDays(user.getId());
            if (consecutiveDays >= 7) {
                newAchievements.add(new Achievement(user.getId(), "Semaine parfaite",
                        "7 jours consécutifs de progression", "🔥", Achievement.AchievementType.CONSISTENCY));
            }
        }

        return newAchievements;
    }

    private List<Achievement> checkMilestoneAchievements(User user) {
        List<Achievement> newAchievements = new ArrayList<>();

        long totalProgress = progressRepository.countByUserId(user.getId());

        // 10 progressions
        if (totalProgress >= 10 && !achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Première dizaine", Achievement.AchievementType.MILESTONE)) {
            newAchievements.add(new Achievement(user.getId(), "Première dizaine",
                    "10 progressions enregistrées", "🎯", Achievement.AchievementType.MILESTONE));
        }

        // 50 progressions
        if (totalProgress >= 50 && !achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Demi-siècle", Achievement.AchievementType.MILESTONE)) {
            newAchievements.add(new Achievement(user.getId(), "Demi-siècle",
                    "50 progressions enregistrées", "🏅", Achievement.AchievementType.MILESTONE));
        }

        return newAchievements;
    }

    private List<Achievement> checkStreakAchievements(User user) {
        List<Achievement> newAchievements = new ArrayList<>();

        // Série de 15 jours
        if (!achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Série impressionnante", Achievement.AchievementType.STREAK)) {

            long consecutiveDays = calculateConsecutiveDays(user.getId());
            if (consecutiveDays >= 15) {
                newAchievements.add(new Achievement(user.getId(), "Série impressionnante",
                        "15 jours consécutifs", "⚡", Achievement.AchievementType.STREAK));
            }
        }

        return newAchievements;
    }

    private List<Achievement> checkDedicationAchievements(User user) {
        List<Achievement> newAchievements = new ArrayList<>();

        long activeHabits = habitRepository.countByUserIdAndIsActive(user.getId(), true);

        // 5 habitudes actives
        if (activeHabits >= 5 && !achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Multi-tâches", Achievement.AchievementType.DEDICATION)) {
            newAchievements.add(new Achievement(user.getId(), "Multi-tâches",
                    "5 habitudes actives en même temps", "🎪", Achievement.AchievementType.DEDICATION));
        }

        return newAchievements;
    }

    private List<Achievement> checkVarietyAchievements(User user) {
        List<Achievement> newAchievements = new ArrayList<>();

        // Vérifier si l'utilisateur a des habitudes dans 3 catégories différentes
        // (Implementation simplifiée)
        if (!achievementRepository.existsByUserIdAndNameAndAchievementType(
                user.getId(), "Polyvalent", Achievement.AchievementType.VARIETY)) {

            long distinctCategories = habitRepository.countByUserId(user.getId()); // Simplification
            if (distinctCategories >= 3) {
                newAchievements.add(new Achievement(user.getId(), "Polyvalent",
                        "Habitudes dans 3 catégories différentes", "🌈", Achievement.AchievementType.VARIETY));
            }
        }

        return newAchievements;
    }

    private long calculateConsecutiveDays(Long userId) {
        // Implémentation simplifiée
        // Dans une vraie app, on calculerait les jours consécutifs avec progression
        return progressRepository.countByUserIdAndDateAfter(userId,
                java.time.LocalDate.now().minusDays(30));
    }

    private double calculateProgressToNext(Long userId) {
        // Calcul simplifié du progrès vers le prochain achievement
        long totalProgress = progressRepository.countByUserId(userId);
        long nextMilestone = ((totalProgress / 10) + 1) * 10; // Prochain multiple de 10
        return ((double) totalProgress / nextMilestone) * 100;
    }
}