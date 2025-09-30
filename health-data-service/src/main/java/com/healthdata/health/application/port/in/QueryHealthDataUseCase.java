package com.healthdata.health.application.port.in;

public interface QueryHealthDataUseCase {
    HealthDataListResponse query(QueryHealthDataCommand command);
}