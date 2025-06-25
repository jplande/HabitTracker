package com.habittracker.dto.achievement;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;

@Value
@Builder
@Jacksonized
public class AchievementCheckRequest {

    @NotNull(message = "L'ID utilisateur est obligatoire")
    Long userId;

    // Optionnel : vérifier pour une habitude spécifique
    Long habitId;

    // Optionnel : type de vérification
    String triggerType; // "PROGRESS_ADDED", "HABIT_CREATED", "STREAK_UPDATED", etc.
}