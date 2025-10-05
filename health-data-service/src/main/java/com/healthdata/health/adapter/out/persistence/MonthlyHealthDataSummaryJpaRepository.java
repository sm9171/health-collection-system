package com.healthdata.health.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyHealthDataSummaryJpaRepository extends JpaRepository<MonthlyHealthDataSummaryJpaEntity, Long> {

    @Query("SELECT m FROM MonthlyHealthDataSummaryJpaEntity m " +
           "WHERE m.recordKey = :recordKey AND m.summaryYear = :year AND m.summaryMonth = :month")
    Optional<MonthlyHealthDataSummaryJpaEntity> findByRecordKeyAndYearMonth(
            @Param("recordKey") String recordKey,
            @Param("year") int year,
            @Param("month") int month);

    @Query("SELECT m FROM MonthlyHealthDataSummaryJpaEntity m " +
           "WHERE m.recordKey = :recordKey " +
           "ORDER BY m.summaryYear DESC, m.summaryMonth DESC")
    List<MonthlyHealthDataSummaryJpaEntity> findByRecordKeyOrderByYearMonthDesc(@Param("recordKey") String recordKey);

    @Query("SELECT m FROM MonthlyHealthDataSummaryJpaEntity m " +
           "WHERE m.recordKey = :recordKey AND m.summaryYear = :year")
    List<MonthlyHealthDataSummaryJpaEntity> findByRecordKeyAndYear(
            @Param("recordKey") String recordKey,
            @Param("year") int year);
}
