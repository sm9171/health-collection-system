package com.healthdata.health.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_health_data_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthDataSummaryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "total_calories", nullable = false)
    private float totalCalories;

    @Column(name = "total_distance", nullable = false)
    private float totalDistance;

    @Column(name = "entry_count", nullable = false)
    private int entryCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HealthDataSummaryJpaEntity(String recordKey, LocalDate summaryDate, int totalSteps,
                                      float totalCalories, float totalDistance, int entryCount,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.recordKey = recordKey;
        this.summaryDate = summaryDate;
        this.totalSteps = totalSteps;
        this.totalCalories = totalCalories;
        this.totalDistance = totalDistance;
        this.entryCount = entryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
