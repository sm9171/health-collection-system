package com.healthdata.health.application.service;

import com.healthdata.health.application.port.in.CollectHealthDataCommand;
import com.healthdata.health.application.port.in.CollectHealthDataUseCase;
import com.healthdata.health.application.port.out.HealthDataRepository;
import com.healthdata.health.domain.model.HealthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectHealthDataService implements CollectHealthDataUseCase {

    private final HealthDataRepository healthDataRepository;

    @Override
    public void collect(CollectHealthDataCommand command) {
        List<HealthData> healthDataList = command.getEntries().stream()
                .map(entry -> {
                    // 중복 데이터 체크
                    if (healthDataRepository.existsByRecordKeyAndPeriod(
                            command.getRecordKey(), 
                            entry.getFrom(), 
                            entry.getTo())) {
                        throw new DataDuplicationException("이미 수집된 데이터입니다: " + 
                                command.getRecordKey() + ", " + entry.getFrom() + " ~ " + entry.getTo());
                    }
                    
                    return HealthData.create(
                            command.getRecordKey(),
                            entry.getFrom(),
                            entry.getTo(),
                            entry.getSteps(),
                            entry.getCaloriesValue(),
                            entry.getCaloriesUnit(),
                            entry.getDistanceValue(),
                            entry.getDistanceUnit()
                    );
                })
                .toList();

        healthDataRepository.saveAll(healthDataList);
    }
}