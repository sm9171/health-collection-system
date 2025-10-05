package com.healthdata.health.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_health_data_summary", indexes = {
        @Index(name = "idx_monthly_record_key", columnList = "recordKey"),
        @Index(name = "idx_monthly_summary_year_month", columnList = "summaryYear, summaryMonth")
}, uniqueConstraints = {
        @UniqueConstraint(name = "unique_monthly_summary", columnNames = {"recordKey", "summaryYear", "summaryMonth"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyHealthDataSummaryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @Column(name = "summary_year", nullable = false)
    private int summaryYear;

    @Column(name = "summary_month", nullable = false)
    private int summaryMonth;

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

    public MonthlyHealthDataSummaryJpaEntity(String recordKey, int summaryYear, int summaryMonth,
                                             int totalSteps, float totalCalories, float totalDistance,
                                             int entryCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.recordKey = recordKey;
        this.summaryYear = summaryYear;
        this.summaryMonth = summaryMonth;
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
