package com.habittracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long habitId;

    @NotNull
    private LocalDate date;

    @NotNull
    private Double value;

    private String note;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Progress(Long userId, Long habitId, LocalDate date, Double value) {
        this.userId = userId;
        this.habitId = habitId;
        this.date = date;
        this.value = value;
    }
}