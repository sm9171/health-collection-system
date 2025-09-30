package com.healthdata.health.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record HealthDataCollectionRequest(
    @NotBlank(message = "Record key is required")
    String recordkey,
    
    @Valid
    @NotNull(message = "Data is required")
    Data data
) {
    
    public record Data(
        @Valid
        @NotEmpty(message = "Entries cannot be empty")
        List<Entry> entries
    ) {}
    
    public record Entry(
        @Valid
        @NotNull(message = "Period is required")
        Period period,
        
        @Valid
        @NotNull(message = "Distance is required")
        Distance distance,
        
        @Valid
        @NotNull(message = "Calories is required")
        Calories calories,
        
        @Min(value = 0, message = "Steps must be non-negative")
        int steps
    ) {}
    
    public record Period(
        @NotNull(message = "From time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime from,
        
        @NotNull(message = "To time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime to
    ) {}
    
    public record Distance(
        @NotBlank(message = "Distance unit is required")
        String unit,
        
        @Min(value = 0, message = "Distance value must be non-negative")
        float value
    ) {}
    
    public record Calories(
        @NotBlank(message = "Calories unit is required")
        String unit,
        
        @Min(value = 0, message = "Calories value must be non-negative")
        float value
    ) {}
}