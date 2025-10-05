package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.domain.model.MonthlyHealthDataSummary;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class MonthlyHealthDataSummaryMapper {

    public MonthlyHealthDataSummaryJpaEntity toEntity(MonthlyHealthDataSummary domain) {
        return new MonthlyHealthDataSummaryJpaEntity(
                domain.getRecordKey(),
                domain.getSummaryMonth().getYear(),
                domain.getSummaryMonth().getMonthValue(),
                domain.getTotalSteps(),
                domain.getTotalCalories(),
                domain.getTotalDistance(),
                domain.getEntryCount(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public MonthlyHealthDataSummary toDomain(MonthlyHealthDataSummaryJpaEntity entity) {
        return MonthlyHealthDataSummary.fromPersistence(
                entity.getId(),
                entity.getRecordKey(),
                YearMonth.of(entity.getSummaryYear(), entity.getSummaryMonth()),
                entity.getTotalSteps(),
                entity.getTotalCalories(),
                entity.getTotalDistance(),
                entity.getEntryCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
