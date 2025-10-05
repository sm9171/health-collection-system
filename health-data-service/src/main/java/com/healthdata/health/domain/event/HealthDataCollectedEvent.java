package com.healthdata.health.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthDataCollectedEvent {
    private String recordKey;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private int steps;
    private float caloriesValue;
    private String caloriesUnit;
    private float distanceValue;
    private String distanceUnit;
    private LocalDateTime collectedAt;
}