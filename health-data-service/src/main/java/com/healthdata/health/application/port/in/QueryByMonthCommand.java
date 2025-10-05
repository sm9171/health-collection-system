package com.healthdata.health.application.port.in;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueryByMonthCommand {
    String recordKey;
    String yearMonth; // yyyyMM format (e.g., "202506")
}
