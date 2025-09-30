package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthDataId {
    private Long value;

    public HealthDataId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Health Data ID must be positive");
        }
        this.value = value;
    }

    public static HealthDataId of(Long value) {
        return new HealthDataId(value);
    }
}