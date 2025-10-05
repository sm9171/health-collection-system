package com.healthdata.health.adapter.in.batch;

import com.healthdata.health.adapter.out.cache.HealthDataCacheRepository;
import com.healthdata.health.adapter.out.persistence.HealthDataJpaEntity;
import com.healthdata.health.adapter.out.persistence.HealthDataJpaRepository;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaEntity;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("통합 테스트 환경 설정 필요 - H2/Redis 설정 문제로 비활성화")
@DisplayName("BatchScheduler 통합 테스트 - 특정 날짜 배치 실행")
class BatchSchedulerIntegrationTest {

    @Autowired
    private BatchScheduler batchScheduler;

    @Autowired
    private HealthDataJpaRepository healthDataRepository;

    @Autowired
    private HealthDataSummaryJpaRepository summaryRepository;

    @MockitoBean
    private HealthDataCacheRepository cacheRepository;

    private final String TEST_RECORD_KEY = "test-record-key-001";
    private final LocalDate TEST_DATE = LocalDate.of(2025, 10, 3);

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        summaryRepository.deleteAll();
        healthDataRepository.deleteAll();
    }

    @Test
    @DisplayName("특정 날짜(2025-10-03)의 건강 데이터를 집계하여 일별 합계 테이블에 저장한다")
    void runAggregationForSpecificDate() throws Exception {
        // Given: 2025-10-03 날짜의 테스트 데이터 생성
        List<HealthDataJpaEntity> testData = createTestHealthData(TEST_RECORD_KEY, TEST_DATE);
        healthDataRepository.saveAll(testData);

        // 데이터 저장 확인
        LocalDateTime from = TEST_DATE.atStartOfDay();
        LocalDateTime to = TEST_DATE.atTime(23, 59, 59);
        List<HealthDataJpaEntity> savedData = healthDataRepository.findByRecordKeyAndTimeRange(
                TEST_RECORD_KEY, from, to);
        assertThat(savedData).hasSize(testData.size());

        // 합계 계산
        int expectedTotalSteps = testData.stream().mapToInt(HealthDataJpaEntity::getSteps).sum();
        float expectedTotalCalories = (float) testData.stream().mapToDouble(HealthDataJpaEntity::getCalories).sum();
        float expectedTotalDistance = (float) testData.stream().mapToDouble(HealthDataJpaEntity::getDistance).sum();

        // When: 2025-10-03 날짜로 배치 실행
        JobExecution execution = batchScheduler.runAggregationForDate(TEST_DATE);

        // Then: 배치 실행 성공 확인
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 일별 합계 테이블에 저장되었는지 확인
        Optional<HealthDataSummaryJpaEntity> summary =
                summaryRepository.findByRecordKeyAndSummaryDate(TEST_RECORD_KEY, TEST_DATE);

        assertThat(summary).isPresent();
        assertThat(summary.get().getRecordKey()).isEqualTo(TEST_RECORD_KEY);
        assertThat(summary.get().getSummaryDate()).isEqualTo(TEST_DATE);
        assertThat(summary.get().getTotalSteps()).isEqualTo(expectedTotalSteps);
        assertThat(summary.get().getTotalCalories()).isEqualTo(expectedTotalCalories);
        assertThat(summary.get().getTotalDistance()).isEqualTo(expectedTotalDistance);
        assertThat(summary.get().getEntryCount()).isEqualTo(testData.size());
    }

    @Test
    @DisplayName("특정 날짜에 데이터가 없으면 배치가 성공하지만 합계 테이블에 저장되지 않는다")
    void runAggregationForDateWithNoData() throws Exception {
        // Given: 데이터가 없는 날짜
        LocalDate emptyDate = LocalDate.of(2025, 10, 1);

        // When: 배치 실행
        JobExecution execution = batchScheduler.runAggregationForDate(emptyDate);

        // Then: 배치는 성공하지만 합계 데이터는 없음
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Optional<HealthDataSummaryJpaEntity> summary =
                summaryRepository.findByRecordKeyAndSummaryDate(TEST_RECORD_KEY, emptyDate);

        assertThat(summary).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자의 데이터를 동시에 집계한다")
    void runAggregationForMultipleUsers() throws Exception {
        // Given: 여러 사용자의 데이터 생성
        String recordKey1 = "user-001";
        String recordKey2 = "user-002";
        String recordKey3 = "user-003";

        List<HealthDataJpaEntity> allData = new ArrayList<>();
        allData.addAll(createTestHealthData(recordKey1, TEST_DATE));
        allData.addAll(createTestHealthData(recordKey2, TEST_DATE));
        allData.addAll(createTestHealthData(recordKey3, TEST_DATE));

        healthDataRepository.saveAll(allData);

        // When: 배치 실행
        JobExecution execution = batchScheduler.runAggregationForDate(TEST_DATE);

        // Then: 모든 사용자의 합계가 저장됨
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Optional<HealthDataSummaryJpaEntity> summary1 =
                summaryRepository.findByRecordKeyAndSummaryDate(recordKey1, TEST_DATE);
        Optional<HealthDataSummaryJpaEntity> summary2 =
                summaryRepository.findByRecordKeyAndSummaryDate(recordKey2, TEST_DATE);
        Optional<HealthDataSummaryJpaEntity> summary3 =
                summaryRepository.findByRecordKeyAndSummaryDate(recordKey3, TEST_DATE);

        assertThat(summary1).isPresent();
        assertThat(summary2).isPresent();
        assertThat(summary3).isPresent();

        // 각 사용자별로 데이터가 올바르게 집계되었는지 확인
        assertThat(summary1.get().getTotalSteps()).isGreaterThan(0);
        assertThat(summary2.get().getTotalSteps()).isGreaterThan(0);
        assertThat(summary3.get().getTotalSteps()).isGreaterThan(0);
    }

    /**
     * 테스트용 건강 데이터 생성
     */
    private List<HealthDataJpaEntity> createTestHealthData(String recordKey, LocalDate date) {
        List<HealthDataJpaEntity> data = new ArrayList<>();

        // 하루 동안 10분 간격으로 6개 데이터 생성
        for (int i = 0; i < 6; i++) {
            LocalDateTime fromTime = date.atTime(9, i * 10);
            LocalDateTime toTime = fromTime.plusMinutes(10);

            data.add(new HealthDataJpaEntity(
                    recordKey,
                    fromTime,
                    toTime,
                    100 + (i * 10),           // steps: 100, 110, 120, ...
                    5.0f + (i * 0.5f),        // calories: 5.0, 5.5, 6.0, ...
                    "kcal",
                    0.08f + (i * 0.01f),      // distance: 0.08, 0.09, 0.10, ...
                    "km",
                    LocalDateTime.now()
            ));
        }

        return data;
    }
}
