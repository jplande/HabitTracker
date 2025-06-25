package com.habittracker.dto.achievement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.habittracker.entity.Achievement;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AchievementResponse {
    Long id;
    Long userId;
    String name;
    String description;
    String icon;
    Achievement.AchievementType achievementType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime unlockedAt;

    // Informations enrichies
    Boolean isNew;
    Integer rarity; // Sur 100 (pourcentage d'utilisateurs qui ont ce badge)
    String category;

    public static AchievementResponse fromEntity(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .userId(achievement.getUserId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .icon(achievement.getIcon())
                .achievementType(achievement.getAchievementType())
                .unlockedAt(achievement.getUnlockedAt())
                .build();
    }

    public AchievementResponse withEnrichment(Boolean isNew, Integer rarity, String category) {
        return AchievementResponse.builder()
                .id(this.id)
                .userId(this.userId)
                .name(this.name)
                .description(this.description)
                .icon(this.icon)
                .achievementType(this.achievementType)
                .unlockedAt(this.unlockedAt)
                .isNew(isNew)
                .rarity(rarity)
                .category(category)
                .build();
    }
}