package com.healthdata.health.application.port.in;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CollectHealthDataCommand {
    String recordKey;
    List<HealthDataEntry> entries;

    @Value
    @Builder
    public static class HealthDataEntry {
        LocalDateTime from;
        LocalDateTime to;
        int steps;
        float caloriesValue;
        String caloriesUnit;
        float distanceValue;
        String distanceUnit;
    }
}