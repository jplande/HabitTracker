package com.habittracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @NotBlank
    private String unit;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private Double targetValue;

    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum Category {
        SPORT, SANTE, EDUCATION, TRAVAIL, LIFESTYLE, SOCIAL, CREATIVITE, FINANCE, AUTRE
    }

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY
    }

    public Habit(Long userId, String title, Category category, String unit, Frequency frequency) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.unit = unit;
        this.frequency = frequency;
        this.isActive = true;
    }
}