package com.healthdata.health.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthDataJpaRepository extends JpaRepository<HealthDataJpaEntity, Long> {

    @Query("SELECT h FROM HealthDataJpaEntity h WHERE h.recordKey = :recordKey " +
           "AND h.fromTime >= :from AND h.toTime <= :to " +
           "ORDER BY h.fromTime ASC")
    List<HealthDataJpaEntity> findByRecordKeyAndTimeRange(
            @Param("recordKey") String recordKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(h) > 0 FROM HealthDataJpaEntity h WHERE h.recordKey = :recordKey " +
           "AND h.fromTime = :from AND h.toTime = :to")
    boolean existsByRecordKeyAndPeriod(
            @Param("recordKey") String recordKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT h FROM HealthDataJpaEntity h WHERE h.recordKey = :recordKey " +
           "AND FUNCTION('DATE_FORMAT', h.fromTime, '%Y%m%d') = :date " +
           "ORDER BY h.fromTime ASC")
    List<HealthDataJpaEntity> findByRecordKeyAndDate(
            @Param("recordKey") String recordKey,
            @Param("date") String date);

    @Query("SELECT h FROM HealthDataJpaEntity h WHERE h.recordKey = :recordKey " +
           "AND DATE(h.fromTime) = CURRENT_DATE " +
           "ORDER BY h.fromTime ASC")
    List<HealthDataJpaEntity> findByRecordKeyAndToday(@Param("recordKey") String recordKey);

    @Query("SELECT h FROM HealthDataJpaEntity h WHERE h.recordKey = :recordKey " +
           "AND FUNCTION('DATE_FORMAT', h.fromTime, '%Y%m') = :yearMonth " +
           "ORDER BY h.fromTime ASC")
    List<HealthDataJpaEntity> findByRecordKeyAndYearMonth(
            @Param("recordKey") String recordKey,
            @Param("yearMonth") String yearMonth);

    @Query("SELECT COALESCE(SUM(h.steps), 0) FROM HealthDataJpaEntity h " +
           "WHERE h.recordKey = :recordKey AND DATE(h.fromTime) = CURRENT_DATE")
    Integer sumTodaySteps(@Param("recordKey") String recordKey);

    @Query("SELECT COALESCE(SUM(h.calories), 0.0) FROM HealthDataJpaEntity h " +
           "WHERE h.recordKey = :recordKey AND DATE(h.fromTime) = CURRENT_DATE")
    Float sumTodayCalories(@Param("recordKey") String recordKey);

    @Query("SELECT COALESCE(SUM(h.distance), 0.0) FROM HealthDataJpaEntity h " +
           "WHERE h.recordKey = :recordKey AND DATE(h.fromTime) = CURRENT_DATE")
    Float sumTodayDistance(@Param("recordKey") String recordKey);
}