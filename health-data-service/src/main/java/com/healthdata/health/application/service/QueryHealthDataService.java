package com.healthdata.health.application.service;

import com.healthdata.health.adapter.out.persistence.HealthDataJpaRepository;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaRepository;
import com.healthdata.health.adapter.out.persistence.MonthlyHealthDataSummaryJpaRepository;
import com.healthdata.health.application.port.in.*;
import com.healthdata.health.application.port.out.HealthDataRepository;
import com.healthdata.health.domain.model.HealthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryHealthDataService implements QueryHealthDataUseCase {

    private final HealthDataRepository healthDataRepository;
    private final HealthDataJpaRepository healthDataJpaRepository;
    private final HealthDataSummaryJpaRepository dailySummaryRepository;
    private final MonthlyHealthDataSummaryJpaRepository monthlySummaryRepository;

    @Override
    public HealthDataListResponse query(QueryHealthDataCommand command) {
        List<HealthData> healthDataList = healthDataRepository.findByRecordKeyAndPeriod(
                command.getRecordKey(),
                command.getFrom(),
                command.getTo()
        );

        return HealthDataListResponse.from(command.getRecordKey(), healthDataList);
    }

    @Override
    public DailySummaryResponse queryDailySummaries(String recordKey) {
        List<DailySummaryResponse.DailySummary> summaries = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. 배치로 집계된 과거 데이터 조회 (daily_health_data_summary)
        List<DailySummaryResponse.DailySummary> batchSummaries = dailySummaryRepository
                .findByRecordKeyOrderBySummaryDateDesc(recordKey)
                .stream()
                .map(entity -> new DailySummaryResponse.DailySummary(
                        entity.getSummaryDate(),
                        entity.getTotalSteps(),
                        entity.getTotalCalories(),
                        entity.getTotalDistance()
                ))
                .toList();

        // 2. 오늘 날짜 데이터가 배치 데이터에 포함되어 있는지 확인
        boolean todayExistsInBatch = batchSummaries.stream()
                .anyMatch(summary -> summary.date().equals(today));

        summaries.addAll(batchSummaries);

        // 3. 오늘 데이터가 배치에 없으면 health_data 테이블에서 실시간 집계
        if (!todayExistsInBatch) {
            Integer todaySteps = healthDataJpaRepository.sumTodaySteps(recordKey);
            Float todayCalories = healthDataJpaRepository.sumTodayCalories(recordKey);
            Float todayDistance = healthDataJpaRepository.sumTodayDistance(recordKey);

            // 오늘 데이터가 있으면 추가 (null 체크 포함)
            if (todaySteps != null && todayCalories != null && todayDistance != null &&
                (todaySteps > 0 || todayCalories > 0 || todayDistance > 0)) {
                summaries.add(0, new DailySummaryResponse.DailySummary(
                        today,
                        todaySteps,
                        todayCalories,
                        todayDistance
                ));
            }
        }

        return DailySummaryResponse.from(recordKey, summaries);
    }

    @Override
    public MonthlySummaryResponse queryMonthlySummaries(String recordKey) {
        List<MonthlySummaryResponse.MonthlySummary> summaries = monthlySummaryRepository
                .findByRecordKeyOrderByYearMonthDesc(recordKey)
                .stream()
                .map(entity -> new MonthlySummaryResponse.MonthlySummary(
                        YearMonth.of(entity.getSummaryYear(), entity.getSummaryMonth()),
                        entity.getTotalSteps(),
                        entity.getTotalCalories(),
                        entity.getTotalDistance()
                ))
                .toList();

        return MonthlySummaryResponse.from(recordKey, summaries);
    }
}