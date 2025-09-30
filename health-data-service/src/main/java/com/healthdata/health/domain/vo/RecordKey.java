package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordKey {
    private String value;

    public RecordKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Record key cannot be empty");
        }
        this.value = value;
    }

    public static RecordKey of(String value) {
        return new RecordKey(value);
    }
}