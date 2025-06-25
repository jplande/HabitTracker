package com.habittracker.dto.achievement;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AchievementCheckResponse {
    Long userId;
    int totalChecked;
    int newAchievementsUnlocked;
    List<AchievementResponse> newAchievements;
    String message;

    public static AchievementCheckResponse noNewAchievements(Long userId) {
        return AchievementCheckResponse.builder()
                .userId(userId)
                .totalChecked(1)
                .newAchievementsUnlocked(0)
                .newAchievements(List.of())
                .message("Aucun nouveau badge débloqué")
                .build();
    }

    public static AchievementCheckResponse withNewAchievements(Long userId, List<AchievementResponse> achievements) {
        return AchievementCheckResponse.builder()
                .userId(userId)
                .totalChecked(achievements.size())
                .newAchievementsUnlocked(achievements.size())
                .newAchievements(achievements)
                .message(String.format("🎉 %d nouveau(x) badge(s) débloqué(s) !", achievements.size()))
                .build();
    }
}