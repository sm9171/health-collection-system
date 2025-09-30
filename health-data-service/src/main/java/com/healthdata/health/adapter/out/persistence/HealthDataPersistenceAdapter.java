package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.application.port.out.HealthDataRepository;
import com.healthdata.health.domain.model.HealthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HealthDataPersistenceAdapter implements HealthDataRepository {

    private final HealthDataJpaRepository jpaRepository;
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
}