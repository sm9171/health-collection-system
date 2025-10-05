package com.healthdata.health.adapter.in.batch;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * BatchScheduler 수동 실행 테스트
 *
 * 실제 환경(MySQL, Redis, Kafka)에서 특정 날짜의 배치를 실행하는 테스트입니다.
 *
 * 사용법:
 * 1. 인프라 서비스 시작: docker-compose -f infra/docker/docker-compose.yml up -d
 * 2. TEST_DATE를 원하는 날짜로 변경
 * 3. 테스트 실행: ./gradlew :health-data-service:test --tests "BatchSchedulerManualTest.runDailyBatchForTargetDay"
 */
@Disabled
@SpringBootTest(properties = {
        "spring.kafka.streams.auto-startup=false"
})
@DisplayName("BatchScheduler 수동 실행 테스트")
class BatchSchedulerManualTest {

    @Autowired
    private BatchScheduler batchScheduler;

    @Test
    @DisplayName("특정 일자의 일별 데이터를 집계한다")
    void runDailyBatchForTargetDay() throws Exception {
        LocalDate targetDay = LocalDate.of(2025, 10, 5);

        System.out.println("=".repeat(60));
        System.out.println("특정 일자 데이터 일별 배치 시작: " + targetDay);
        System.out.println("=".repeat(60));

        JobExecution execution = batchScheduler.runAggregationForDate(targetDay);

        System.out.println("\n배치 완료 - 상태: " + execution.getStatus());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("특정 달의 월별 데이터를 집계한다")
    void runMonthlyBatchForTargetMonth() throws Exception {
        YearMonth targetMonth = YearMonth.of(2025, 10);

        System.out.println("=".repeat(60));
        System.out.println("특정 달 데이터 월별 배치 시작: " + targetMonth);
        System.out.println("=".repeat(60));

        JobExecution execution = batchScheduler.runMonthlyAggregationForMonth(targetMonth);

        System.out.println("\n배치 완료 - 상태: " + execution.getStatus());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
