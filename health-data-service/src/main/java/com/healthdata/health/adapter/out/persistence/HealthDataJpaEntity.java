package com.healthdata.health.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_data", indexes = {
        @Index(name = "idx_record_key", columnList = "recordKey"),
        @Index(name = "idx_time_range", columnList = "fromTime, toTime"),
        @Index(name = "idx_collected_at", columnList = "collectedAt")
}, uniqueConstraints = {
        @UniqueConstraint(name = "unique_data", columnNames = {"recordKey", "fromTime", "toTime"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthDataJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_key", nullable = false, length = 50)
    private String recordKey;

    @Column(name = "from_time", nullable = false)
    private LocalDateTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalDateTime toTime;

    @Column(nullable = false)
    private int steps;

    @Column(nullable = false)
    private float calories;

    @Column(name = "calories_unit", nullable = false, length = 10)
    private String caloriesUnit;

    @Column(nullable = false)
    private float distance;

    @Column(name = "distance_unit", nullable = false, length = 10)
    private String distanceUnit;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public HealthDataJpaEntity(String recordKey, LocalDateTime fromTime, LocalDateTime toTime,
                               int steps, float calories, String caloriesUnit,
                               float distance, String distanceUnit, LocalDateTime collectedAt) {
        this.recordKey = recordKey;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.steps = steps;
        this.calories = calories;
        this.caloriesUnit = caloriesUnit;
        this.distance = distance;
        this.distanceUnit = distanceUnit;
        this.collectedAt = collectedAt;
        this.createdAt = LocalDateTime.now();
    }
}