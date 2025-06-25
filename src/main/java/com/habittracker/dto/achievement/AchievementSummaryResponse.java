package com.habittracker.dto.achievement;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class AchievementSummaryResponse {
    Long userId;
    String username;

    // Statistiques globales
    int totalAchievements;
    int totalPossibleAchievements;
    double completionPercentage;

    // Par type
    Map<String, Integer> achievementsByType;

    // Récents
    LocalDateTime lastAchievementDate;
    String lastAchievementName;
    int achievementsThisWeek;
    int achievementsThisMonth;

    // Raretés
    int commonAchievements;
    int rareAchievements;
    int epicAchievements;
    int legendaryAchievements;

    // Progression
    String nextPossibleAchievement;
    double progressToNextAchievement;
}