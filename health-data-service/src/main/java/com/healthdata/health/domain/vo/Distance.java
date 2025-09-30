package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distance {
    private float value;
    private String unit;

    private Distance(float value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public static Distance of(float value, String unit) {
        if (value < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
        return new Distance(value, unit);
    }
}