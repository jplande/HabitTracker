package com.habittracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String icon;

    @Enumerated(EnumType.STRING)
    private AchievementType achievementType;

    private LocalDateTime unlockedAt = LocalDateTime.now();

    public enum AchievementType {
        CONSISTENCY, MILESTONE, STREAK, DEDICATION, OVERACHIEVER, VARIETY, EARLY_BIRD, PERSEVERANCE
    }

    public Achievement(Long userId, String name, String description, String icon, AchievementType achievementType) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.achievementType = achievementType;
        this.unlockedAt = LocalDateTime.now();
    }
}