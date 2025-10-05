package com.healthdata.health.adapter.out.persistence;

import com.healthdata.health.domain.model.MonthlyHealthDataSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlyHealthDataSummaryMapper 테스트")
class MonthlyHealthDataSummaryMapperTest {

    private final MonthlyHealthDataSummaryMapper mapper = new MonthlyHealthDataSummaryMapper();

    @Test
    @DisplayName("도메인 모델을 JPA 엔티티로 변환할 수 있다")
    void toEntity() {
        // given
        MonthlyHealthDataSummary domain = MonthlyHealthDataSummary.create(
                "test-record-key", YearMonth.of(2025, 6));
        domain.setData(10000, 500.5f, 8.5f, 30);

        // when
        MonthlyHealthDataSummaryJpaEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getRecordKey()).isEqualTo("test-record-key");
        assertThat(entity.getSummaryYear()).isEqualTo(2025);
        assertThat(entity.getSummaryMonth()).isEqualTo(6);
        assertThat(entity.getTotalSteps()).isEqualTo(10000);
        assertThat(entity.getTotalCalories()).isEqualTo(500.5f);
        assertThat(entity.getTotalDistance()).isEqualTo(8.5f);
        assertThat(entity.getEntryCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("JPA 엔티티를 도메인 모델로 변환할 수 있다")
    void toDomain() {
        // given
        LocalDateTime now = LocalDateTime.now();
        MonthlyHealthDataSummaryJpaEntity entity = new MonthlyHealthDataSummaryJpaEntity(
                "test-record-key", 2025, 6, 10000, 500.5f, 8.5f, 30, now, now);

        // when
        MonthlyHealthDataSummary domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getRecordKey()).isEqualTo("test-record-key");
        assertThat(domain.getSummaryMonth()).isEqualTo(YearMonth.of(2025, 6));
        assertThat(domain.getTotalSteps()).isEqualTo(10000);
        assertThat(domain.getTotalCalories()).isEqualTo(500.5f);
        assertThat(domain.getTotalDistance()).isEqualTo(8.5f);
        assertThat(domain.getEntryCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("도메인 모델을 엔티티로 변환 후 다시 도메인으로 변환하면 동일한 데이터를 가진다")
    void roundTrip() {
        // given
        MonthlyHealthDataSummary original = MonthlyHealthDataSummary.create(
                "test-record-key", YearMonth.of(2025, 6));
        original.setData(10000, 500.5f, 8.5f, 30);

        // when
        MonthlyHealthDataSummaryJpaEntity entity = mapper.toEntity(original);
        MonthlyHealthDataSummary converted = mapper.toDomain(entity);

        // then
        assertThat(converted.getRecordKey()).isEqualTo(original.getRecordKey());
        assertThat(converted.getSummaryMonth()).isEqualTo(original.getSummaryMonth());
        assertThat(converted.getTotalSteps()).isEqualTo(original.getTotalSteps());
        assertThat(converted.getTotalCalories()).isEqualTo(original.getTotalCalories());
        assertThat(converted.getTotalDistance()).isEqualTo(original.getTotalDistance());
        assertThat(converted.getEntryCount()).isEqualTo(original.getEntryCount());
    }
}
