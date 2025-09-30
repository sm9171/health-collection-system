package com.healthdata.health.application.port.in;

public interface CollectHealthDataUseCase {
    void collect(CollectHealthDataCommand command);
}