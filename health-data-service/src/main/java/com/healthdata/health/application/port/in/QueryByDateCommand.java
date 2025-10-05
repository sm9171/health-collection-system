package com.healthdata.health.application.port.in;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueryByDateCommand {
    String recordKey;
    String date; // yyyyMMdd format (e.g., "20250607")
}
