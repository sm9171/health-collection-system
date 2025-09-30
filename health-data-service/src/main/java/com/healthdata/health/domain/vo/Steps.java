package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Steps {
    private int value;

    public Steps(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Steps cannot be negative");
        }
        this.value = value;
    }

    public static Steps of(int value) {
        return new Steps(value);
    }
}