package com.healthdata.health.application.port.out;

import com.healthdata.health.domain.model.HealthData;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthDataRepository {
    List<HealthData> saveAll(List<HealthData> healthDataList);
    List<HealthData> findByRecordKeyAndPeriod(String recordKey, LocalDateTime from, LocalDateTime to);
    boolean existsByRecordKeyAndPeriod(String recordKey, LocalDateTime from, LocalDateTime to);
}