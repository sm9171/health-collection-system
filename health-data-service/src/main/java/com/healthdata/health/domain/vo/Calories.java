package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calories {
    private float value;
    private String unit;

    private Calories(float value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public static Calories of(float value, String unit) {
        if (value < 0) {
            throw new IllegalArgumentException("Calories cannot be negative");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
        return new Calories(value, unit);
    }
}