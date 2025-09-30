package com.healthdata.health.domain.model;

import com.healthdata.health.domain.vo.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthData {
    private HealthDataId id;
    private RecordKey recordKey;
    private Period period;
    private Steps steps;
    private Calories calories;
    private Distance distance;
    private LocalDateTime collectedAt;

    private HealthData(RecordKey recordKey, Period period, Steps steps, Calories calories, Distance distance) {
        this.recordKey = recordKey;
        this.period = period;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.collectedAt = LocalDateTime.now();
    }

    public static HealthData create(String recordKey, 
                                   LocalDateTime from, 
                                   LocalDateTime to,
                                   int steps, 
                                   float caloriesValue,
                                   String caloriesUnit,
                                   float distanceValue,
                                   String distanceUnit) {
        return new HealthData(
                RecordKey.of(recordKey),
                Period.of(from, to),
                Steps.of(steps),
                Calories.of(caloriesValue, caloriesUnit),
                Distance.of(distanceValue, distanceUnit)
        );
    }

    public static HealthData fromPersistence(Long id, String recordKey, 
                                           LocalDateTime fromTime, LocalDateTime toTime,
                                           int steps, 
                                           float caloriesValue, String caloriesUnit,
                                           float distanceValue, String distanceUnit,
                                           LocalDateTime collectedAt) {
        HealthData healthData = new HealthData();
        healthData.id = HealthDataId.of(id);
        healthData.recordKey = RecordKey.of(recordKey);
        healthData.period = Period.of(fromTime, toTime);
        healthData.steps = Steps.of(steps);
        healthData.calories = Calories.of(caloriesValue, caloriesUnit);
        healthData.distance = Distance.of(distanceValue, distanceUnit);
        healthData.collectedAt = collectedAt;
        return healthData;
    }
}