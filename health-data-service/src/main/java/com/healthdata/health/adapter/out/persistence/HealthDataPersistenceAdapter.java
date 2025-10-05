package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.application.port.out.HealthDataRepository;
import com.healthdata.health.domain.model.HealthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HealthDataPersistenceAdapter implements HealthDataRepository {

    private final HealthDataJpaRepository jpaRepository;
    private final HealthDataSummaryJpaRepository summaryRepository;
    private final HealthDataMapper mapper;

    @Override
    public List<HealthData> saveAll(List<HealthData> healthDataList) {
        List<HealthDataJpaEntity> entities = healthDataList.stream()
                .map(mapper::toEntity)
                .toList();

        List<HealthDataJpaEntity> saved = jpaRepository.saveAll(entities);

        return saved.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HealthData> findByRecordKeyAndPeriod(String recordKey, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByRecordKeyAndTimeRange(recordKey, from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByRecordKeyAndPeriod(String recordKey, LocalDateTime from, LocalDateTime to) {
        return jpaRepository.existsByRecordKeyAndPeriod(recordKey, from, to);
    }

    @Override
    public List<HealthData> findByRecordKeyAndDate(String recordKey, String date) {
        LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate today = LocalDate.now();

        // 오늘 데이터는 원본 테이블에서 조회
        if (targetDate.equals(today)) {
            return jpaRepository.findByRecordKeyAndToday(recordKey)
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
        }

        // 어제 이전 데이터는 일별 합계 테이블에서 조회
        // TODO: 합계 테이블 데이터를 HealthData 형식으로 변환 필요
        return jpaRepository.findByRecordKeyAndDate(recordKey, date)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HealthData> findByRecordKeyAndYearMonth(String recordKey, String yearMonth) {
        // 월별 조회는 원본 데이터 조회 (월별 합계는 별도 API로 제공)
        return jpaRepository.findByRecordKeyAndYearMonth(recordKey, yearMonth)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}