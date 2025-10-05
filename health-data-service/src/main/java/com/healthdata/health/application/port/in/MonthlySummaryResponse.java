package com.healthdata.health.application.port.in;

import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryResponse(
    String recordKey,
    List<MonthlySummary> summaries
) {

    public record MonthlySummary(
        YearMonth yearMonth,
        int totalSteps,
        float totalCalories,
        float totalDistance
    ) {}

    public static MonthlySummaryResponse from(String recordKey, List<MonthlySummary> summaries) {
        return new MonthlySummaryResponse(recordKey, summaries);
    }
}
