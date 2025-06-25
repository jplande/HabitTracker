package com.habittracker.dto.progress;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class ProgressStatsResponse {
    Long habitId;
    String habitTitle;
    String habitUnit;
    Double habitTarget;

    // Période d'analyse
    LocalDate startDate;
    LocalDate endDate;
    int totalDays;

    // Statistiques globales
    int totalEntries;
    int consecutiveDays;
    double completionRate;

    // Valeurs
    Double totalValue;
    Double averageValue;
    Double maxValue;
    Double minValue;
    Double lastValue;

    // Progression
    Double progressTrend;
    Boolean improvementDetected;

    // Objectifs
    int daysTargetReached;
    double targetReachRate;

    // Constance
    double consistencyScore;
    LocalDate lastEntryDate;
    int daysSinceLastEntry;
}