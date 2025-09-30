package com.healthdata.health.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HealthData 도메인 테스트")
class HealthDataTest {

    @Test
    @DisplayName("유효한 정보로 건강 데이터를 생성할 수 있다")
    void createHealthData() {
        // given
        String recordKey = "test-record-key";
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 10, 10);
        int steps = 100;
        float calories = 10.5f;
        float distance = 0.08f;
        
        // when
        HealthData healthData = HealthData.create(
                recordKey, from, to, steps, calories, "kcal", distance, "km"
        );
        
        // then
        assertThat(healthData.getRecordKey().getValue()).isEqualTo(recordKey);
        assertThat(healthData.getPeriod().getFromTime()).isEqualTo(from);
        assertThat(healthData.getPeriod().getToTime()).isEqualTo(to);
        assertThat(healthData.getSteps().getValue()).isEqualTo(steps);
        assertThat(healthData.getCalories().getValue()).isEqualTo(calories);
        assertThat(healthData.getDistance().getValue()).isEqualTo(distance);
    }
    
    @Test
    @DisplayName("from이 to보다 늦은 시간일 때 예외가 발생한다")
    void createHealthDataWithInvalidPeriod() {
        // given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 10, 10);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        // when & then
        assertThatThrownBy(() -> 
            HealthData.create("test", from, to, 100, 10.5f, "kcal", 0.08f, "km")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("From time must be before to time");
    }
    
    @Test
    @DisplayName("음수 걸음수로 건강 데이터를 생성할 수 없다")
    void createHealthDataWithNegativeSteps() {
        // given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 10, 10);
        
        // when & then
        assertThatThrownBy(() -> 
            HealthData.create("test", from, to, -10, 10.5f, "kcal", 0.08f, "km")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Steps cannot be negative");
    }
}