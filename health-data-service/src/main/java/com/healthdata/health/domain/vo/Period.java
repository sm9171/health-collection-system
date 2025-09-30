package com.healthdata.health.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Period {
    private LocalDateTime fromTime;
    private LocalDateTime toTime;

    private Period(LocalDateTime fromTime, LocalDateTime toTime) {
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    public static Period of(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to time cannot be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From time must be before to time");
        }
        return new Period(from, to);
    }

    public boolean overlaps(Period other) {
        return this.fromTime.isBefore(other.toTime) 
            && this.toTime.isAfter(other.fromTime);
    }
}