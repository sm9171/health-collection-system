package com.healthdata.health.application.service;

import com.healthdata.health.application.port.in.HealthDataListResponse;
import com.healthdata.health.application.port.in.QueryHealthDataCommand;
import com.healthdata.health.application.port.in.QueryHealthDataUseCase;
import com.healthdata.health.application.port.out.HealthDataRepository;
import com.healthdata.health.domain.model.HealthData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryHealthDataService implements QueryHealthDataUseCase {

    private final HealthDataRepository healthDataRepository;

    @Override
    public HealthDataListResponse query(QueryHealthDataCommand command) {
        List<HealthData> healthDataList = healthDataRepository.findByRecordKeyAndPeriod(
                command.getRecordKey(), 
                command.getFrom(), 
                command.getTo()
        );

        return HealthDataListResponse.from(command.getRecordKey(), healthDataList);
    }
}