package com.healthdata.health.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlyHealthDataSummary 도메인 테스트")
class MonthlyHealthDataSummaryTest {

    @Test
    @DisplayName("유효한 정보로 월별 건강 데이터 합계를 생성할 수 있다")
    void createMonthlySummary() {
        // given
        String recordKey = "test-record-key";
        YearMonth summaryMonth = YearMonth.of(2025, 6);

        // when
        MonthlyHealthDataSummary summary = MonthlyHealthDataSummary.create(recordKey, summaryMonth);

        // then
        assertThat(summary.getRecordKey()).isEqualTo(recordKey);
        assertThat(summary.getSummaryMonth()).isEqualTo(summaryMonth);
        assertThat(summary.getTotalSteps()).isEqualTo(0);
        assertThat(summary.getTotalCalories()).isEqualTo(0.0f);
        assertThat(summary.getTotalDistance()).isEqualTo(0.0f);
        assertThat(summary.getEntryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("setData로 월별 합계 데이터를 설정할 수 있다")
    void setData() {
        // given
        MonthlyHealthDataSummary summary = MonthlyHealthDataSummary.create(
                "test-record-key", YearMonth.of(2025, 6));

        // when
        summary.setData(10000, 500.5f, 8.5f, 30);

        // then
        assertThat(summary.getTotalSteps()).isEqualTo(10000);
        assertThat(summary.getTotalCalories()).isEqualTo(500.5f);
        assertThat(summary.getTotalDistance()).isEqualTo(8.5f);
        assertThat(summary.getEntryCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("addData로 월별 합계 데이터를 누적할 수 있다")
    void addData() {
        // given
        MonthlyHealthDataSummary summary = MonthlyHealthDataSummary.create(
                "test-record-key", YearMonth.of(2025, 6));
        summary.setData(5000, 250.0f, 4.0f, 15);

        // when
        summary.addData(1000, 50.5f, 0.8f);

        // then
        assertThat(summary.getTotalSteps()).isEqualTo(6000);
        assertThat(summary.getTotalCalories()).isEqualTo(300.5f);
        assertThat(summary.getTotalDistance()).isEqualTo(4.8f);
        assertThat(summary.getEntryCount()).isEqualTo(16);
    }

    @Test
    @DisplayName("fromPersistence로 영속화된 데이터를 도메인 객체로 변환할 수 있다")
    void fromPersistence() {
        // given
        Long id = 1L;
        String recordKey = "test-record-key";
        YearMonth summaryMonth = YearMonth.of(2025, 6);
        int totalSteps = 10000;
        float totalCalories = 500.5f;
        float totalDistance = 8.5f;
        int entryCount = 30;

        // when
        MonthlyHealthDataSummary summary = MonthlyHealthDataSummary.fromPersistence(
                id, recordKey, summaryMonth, totalSteps, totalCalories, totalDistance,
                entryCount, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        // then
        assertThat(summary.getId()).isEqualTo(id);
        assertThat(summary.getRecordKey()).isEqualTo(recordKey);
        assertThat(summary.getSummaryMonth()).isEqualTo(summaryMonth);
        assertThat(summary.getTotalSteps()).isEqualTo(totalSteps);
        assertThat(summary.getTotalCalories()).isEqualTo(totalCalories);
        assertThat(summary.getTotalDistance()).isEqualTo(totalDistance);
        assertThat(summary.getEntryCount()).isEqualTo(entryCount);
    }
}
