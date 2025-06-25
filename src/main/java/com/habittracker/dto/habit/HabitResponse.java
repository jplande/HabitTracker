package com.habittracker.dto.habit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.habittracker.entity.Habit;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HabitResponse {
    Long id;
    Long userId;
    String title;
    String description;
    Habit.Category category;
    String unit;
    Habit.Frequency frequency;
    Double targetValue;
    Boolean isActive;
    LocalDateTime createdAt;

    // Statistiques optionnelles
    Long totalProgress;
    Long currentStreak;
    Double averageCompletion;
    LocalDateTime lastProgressDate;

    public static HabitResponse fromEntity(Habit habit) {
        return HabitResponse.builder()
                .id(habit.getId())
                .userId(habit.getUserId())
                .title(habit.getTitle())
                .description(habit.getDescription())
                .category(habit.getCategory())
                .unit(habit.getUnit())
                .frequency(habit.getFrequency())
                .targetValue(habit.getTargetValue())
                .isActive(habit.getIsActive())
                .createdAt(habit.getCreatedAt())
                .build();
    }

    public HabitResponse withStatistics(Long totalProgress, Long currentStreak,
                                        Double averageCompletion, LocalDateTime lastProgressDate) {
        return HabitResponse.builder()
                .id(this.id)
                .userId(this.userId)
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .unit(this.unit)
                .frequency(this.frequency)
                .targetValue(this.targetValue)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .totalProgress(totalProgress)
                .currentStreak(currentStreak)
                .averageCompletion(averageCompletion)
                .lastProgressDate(lastProgressDate)
                .build();
    }
}
