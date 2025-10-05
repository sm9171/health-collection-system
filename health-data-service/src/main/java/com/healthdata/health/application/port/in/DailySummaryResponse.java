package com.healthdata.health.application.port.in;

import java.time.LocalDate;
import java.util.List;

public record DailySummaryResponse(
    String recordKey,
    List<DailySummary> summaries
) {

    public record DailySummary(
        LocalDate date,
        int totalSteps,
        float totalCalories,
        float totalDistance
    ) {}

    public static DailySummaryResponse from(String recordKey, List<DailySummary> summaries) {
        return new DailySummaryResponse(recordKey, summaries);
    }
}
