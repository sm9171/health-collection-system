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
}