package com.healthdata.health.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyHealthDataSummary {
    private Long id;
    private String recordKey;
    private YearMonth summaryMonth;
    private int totalSteps;
    private float totalCalories;
    private float totalDistance;
    private int entryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MonthlyHealthDataSummary(String recordKey, YearMonth summaryMonth) {
        this.recordKey = recordKey;
        this.summaryMonth = summaryMonth;
        this.totalSteps = 0;
        this.totalCalories = 0.0f;
        this.totalDistance = 0.0f;
        this.entryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static MonthlyHealthDataSummary create(String recordKey, YearMonth summaryMonth) {
        return new MonthlyHealthDataSummary(recordKey, summaryMonth);
    }

    public static MonthlyHealthDataSummary fromPersistence(Long id, String recordKey, YearMonth summaryMonth,
                                                             int totalSteps, float totalCalories, float totalDistance,
                                                             int entryCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        MonthlyHealthDataSummary summary = new MonthlyHealthDataSummary();
        summary.id = id;
        summary.recordKey = recordKey;
        summary.summaryMonth = summaryMonth;
        summary.totalSteps = totalSteps;
        summary.totalCalories = totalCalories;
        summary.totalDistance = totalDistance;
        summary.entryCount = entryCount;
        summary.createdAt = createdAt;
        summary.updatedAt = updatedAt;
        return summary;
    }

    public void setData(int steps, float calories, float distance, int count) {
        this.totalSteps = steps;
        this.totalCalories = calories;
        this.totalDistance = distance;
        this.entryCount = count;
        this.updatedAt = LocalDateTime.now();
    }

    public void addData(int steps, float calories, float distance) {
        this.totalSteps += steps;
        this.totalCalories += calories;
        this.totalDistance += distance;
        this.entryCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
