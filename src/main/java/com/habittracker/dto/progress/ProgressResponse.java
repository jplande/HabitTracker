package com.habittracker.dto.progress;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.habittracker.entity.Progress;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressResponse {
    Long id;
    Long userId;
    Long habitId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    Double value;
    String note;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;

    // Informations enrichies
    String habitTitle;
    String habitUnit;
    Double habitTarget;
    Double completionPercentage;
    Boolean targetReached;

    public static ProgressResponse fromEntity(Progress progress) {
        return ProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .habitId(progress.getHabitId())
                .date(progress.getDate())
                .value(progress.getValue())
                .note(progress.getNote())
                .createdAt(progress.getCreatedAt())
                .build();
    }

    public ProgressResponse withHabitInfo(String habitTitle, String habitUnit, Double habitTarget) {
        Double completion = null;
        Boolean targetReached = null;

        if (habitTarget != null && habitTarget > 0) {
            completion = Math.round((value / habitTarget) * 100 * 100.0) / 100.0;
            targetReached = value >= habitTarget;
        }

        return ProgressResponse.builder()
                .id(this.id)
                .userId(this.userId)
                .habitId(this.habitId)
                .date(this.date)
                .value(this.value)
                .note(this.note)
                .createdAt(this.createdAt)
                .habitTitle(habitTitle)
                .habitUnit(habitUnit)
                .habitTarget(habitTarget)
                .completionPercentage(completion)
                .targetReached(targetReached)
                .build();
    }
}