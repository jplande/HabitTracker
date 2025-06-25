package com.habittracker.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.habittracker.entity.User;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    Long id;
    String username;
    String email;
    String firstName;
    String lastName;
    User.Role role;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Statistiques optionnelles
    Long totalHabits;
    Long activeHabits;
    Long totalProgress;
    Long totalAchievements;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserResponse publicView(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Méthodes pour créer des versions avec statistiques
    public UserResponse withStatistics(Long totalHabits, Long activeHabits,
                                       Long totalProgress, Long totalAchievements) {
        return UserResponse.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .role(this.role)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .totalHabits(totalHabits)
                .activeHabits(activeHabits)
                .totalProgress(totalProgress)
                .totalAchievements(totalAchievements)
                .build();
    }
}