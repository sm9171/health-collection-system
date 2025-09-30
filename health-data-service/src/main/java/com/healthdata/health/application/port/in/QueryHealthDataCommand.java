package com.healthdata.health.application.port.in;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class QueryHealthDataCommand {
    String recordKey;
    LocalDateTime from;
    LocalDateTime to;
}