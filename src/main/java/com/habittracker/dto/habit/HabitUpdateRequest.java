package com.habittracker.dto.habit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.habittracker.entity.Habit;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HabitUpdateRequest {

    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    String title;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    String description;

    Habit.Category category;

    @Size(max = 50, message = "L'unité ne peut pas dépasser 50 caractères")
    String unit;

    Habit.Frequency frequency;

    @Positive(message = "La valeur cible doit être positive")
    Double targetValue;

    Boolean isActive;

    public boolean isEmpty() {
        return title == null && description == null && category == null &&
                unit == null && frequency == null && targetValue == null && isActive == null;
    }

    public boolean hasChanges() {
        return !isEmpty();
    }

}