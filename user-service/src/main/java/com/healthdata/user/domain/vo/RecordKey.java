package com.healthdata.user.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    public static RecordKey generate() {
        return new RecordKey(UUID.randomUUID().toString());
    }

    public static RecordKey of(String value) {
        return new RecordKey(value);
    }
}