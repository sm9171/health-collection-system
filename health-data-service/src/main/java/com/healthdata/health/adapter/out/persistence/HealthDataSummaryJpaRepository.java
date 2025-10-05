package com.healthdata.health.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthDataSummaryJpaRepository extends JpaRepository<HealthDataSummaryJpaEntity, Long> {

    Optional<HealthDataSummaryJpaEntity> findByRecordKeyAndSummaryDate(String recordKey, LocalDate summaryDate);

    List<HealthDataSummaryJpaEntity> findByRecordKeyOrderBySummaryDateDesc(String recordKey);

    @Query("SELECT COALESCE(SUM(e.totalSteps), 0) FROM HealthDataSummaryJpaEntity e " +
           "WHERE e.recordKey = :recordKey AND e.summaryDate = :date")
    int sumStepsByRecordKeyAndDate(@Param("recordKey") String recordKey, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.totalCalories), 0) FROM HealthDataSummaryJpaEntity e " +
           "WHERE e.recordKey = :recordKey AND e.summaryDate = :date")
    float sumCaloriesByRecordKeyAndDate(@Param("recordKey") String recordKey, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.totalDistance), 0) FROM HealthDataSummaryJpaEntity e " +
           "WHERE e.recordKey = :recordKey AND e.summaryDate = :date")
    float sumDistanceByRecordKeyAndDate(@Param("recordKey") String recordKey, @Param("date") LocalDate date);
}
