package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.domain.model.HealthDataSummary;
import org.springframework.stereotype.Component;

@Component
public class HealthDataSummaryMapper {

    public HealthDataSummaryJpaEntity toEntity(HealthDataSummary summary) {
        return new HealthDataSummaryJpaEntity(
                summary.getRecordKey(),
                summary.getSummaryDate(),
                summary.getTotalSteps(),
                summary.getTotalCalories(),
                summary.getTotalDistance(),
                summary.getEntryCount(),
                summary.getCreatedAt(),
                summary.getUpdatedAt()
        );
    }

    public HealthDataSummary toDomain(HealthDataSummaryJpaEntity entity) {
        return HealthDataSummary.fromPersistence(
                entity.getId(),
                entity.getRecordKey(),
                entity.getSummaryDate(),
                entity.getTotalSteps(),
                entity.getTotalCalories(),
                entity.getTotalDistance(),
                entity.getEntryCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
