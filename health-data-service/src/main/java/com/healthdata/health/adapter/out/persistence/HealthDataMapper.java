package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.domain.model.HealthData;
import org.springframework.stereotype.Component;

@Component
public class HealthDataMapper {

    public HealthDataJpaEntity toEntity(HealthData healthData) {
        return new HealthDataJpaEntity(
                healthData.getRecordKey().getValue(),
                healthData.getPeriod().getFromTime(),
                healthData.getPeriod().getToTime(),
                healthData.getSteps().getValue(),
                healthData.getCalories().getValue(),
                healthData.getCalories().getUnit(),
                healthData.getDistance().getValue(),
                healthData.getDistance().getUnit(),
                healthData.getCollectedAt()
        );
    }

    public HealthData toDomain(HealthDataJpaEntity entity) {
        return HealthData.fromPersistence(
                entity.getId(),
                entity.getRecordKey(),
                entity.getFromTime(),
                entity.getToTime(),
                entity.getSteps(),
                entity.getCalories(),
                entity.getCaloriesUnit(),
                entity.getDistance(),
                entity.getDistanceUnit(),
                entity.getCollectedAt()
        );
    }
}