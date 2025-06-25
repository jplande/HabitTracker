package com.habittracker.dto.habit;

import com.habittracker.entity.Habit;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Value
@Builder
@Jacksonized
public class HabitCreateRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    String title;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    String description;

    @NotNull(message = "La catégorie est obligatoire")
    Habit.Category category;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 50, message = "L'unité ne peut pas dépasser 50 caractères")
    String unit;

    @NotNull(message = "La fréquence est obligatoire")
    Habit.Frequency frequency;

    @Positive(message = "La valeur cible doit être positive")
    Double targetValue;
}