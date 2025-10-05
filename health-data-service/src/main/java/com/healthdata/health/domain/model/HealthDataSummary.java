package com.healthdata.health.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthDataSummary {
    private Long id;
    private String recordKey;
    private LocalDate summaryDate;
    private int totalSteps;
    private float totalCalories;
    private float totalDistance;
    private int entryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private HealthDataSummary(String recordKey, LocalDate summaryDate) {
        this.recordKey = recordKey;
        this.summaryDate = summaryDate;
        this.totalSteps = 0;
        this.totalCalories = 0.0f;
        this.totalDistance = 0.0f;
        this.entryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static HealthDataSummary create(String recordKey, LocalDate summaryDate) {
        return new HealthDataSummary(recordKey, summaryDate);
    }

    public static HealthDataSummary fromPersistence(Long id, String recordKey, LocalDate summaryDate,
                                                     int totalSteps, float totalCalories, float totalDistance,
                                                     int entryCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        HealthDataSummary summary = new HealthDataSummary();
        summary.id = id;
        summary.recordKey = recordKey;
        summary.summaryDate = summaryDate;
        summary.totalSteps = totalSteps;
        summary.totalCalories = totalCalories;
        summary.totalDistance = totalDistance;
        summary.entryCount = entryCount;
        summary.createdAt = createdAt;
        summary.updatedAt = updatedAt;
        return summary;
    }

    public void addData(int steps, float calories, float distance) {
        this.totalSteps += steps;
        this.totalCalories += calories;
        this.totalDistance += distance;
        this.entryCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
