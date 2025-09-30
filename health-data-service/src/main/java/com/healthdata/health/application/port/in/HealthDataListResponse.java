package com.healthdata.health.application.port.in;

import com.healthdata.health.domain.model.HealthData;

import java.time.LocalDateTime;
import java.util.List;

public record HealthDataListResponse(
    String recordKey,
    int totalCount,
    Summary summary,
    List<HealthDataResponse> data
) {

    public record Summary(
        int totalSteps,
        float totalCalories,
        float totalDistance
    ) {}

    public record HealthDataResponse(
        Long id,
        PeriodResponse period,
        int steps,
        CaloriesResponse calories,
        DistanceResponse distance,
        LocalDateTime collectedAt
    ) {

        public record PeriodResponse(
            LocalDateTime from,
            LocalDateTime to
        ) {}

        public record CaloriesResponse(
            float value,
            String unit
        ) {}

        public record DistanceResponse(
            float value,
            String unit
        ) {}
    }

    public static HealthDataListResponse from(String recordKey, List<HealthData> healthDataList) {
        List<HealthDataResponse> responses = healthDataList.stream()
                .map(data -> new HealthDataResponse(
                        data.getId() != null ? data.getId().getValue() : null,
                        new HealthDataResponse.PeriodResponse(
                                data.getPeriod().getFromTime(),
                                data.getPeriod().getToTime()
                        ),
                        data.getSteps().getValue(),
                        new HealthDataResponse.CaloriesResponse(
                                data.getCalories().getValue(),
                                data.getCalories().getUnit()
                        ),
                        new HealthDataResponse.DistanceResponse(
                                data.getDistance().getValue(),
                                data.getDistance().getUnit()
                        ),
                        data.getCollectedAt()
                ))
                .toList();

        Summary summary = new Summary(
                healthDataList.stream().mapToInt(data -> data.getSteps().getValue()).sum(),
                (float) healthDataList.stream().mapToDouble(data -> data.getCalories().getValue()).sum(),
                (float) healthDataList.stream().mapToDouble(data -> data.getDistance().getValue()).sum()
        );

        return new HealthDataListResponse(
                recordKey,
                healthDataList.size(),
                summary,
                responses
        );
    }
}