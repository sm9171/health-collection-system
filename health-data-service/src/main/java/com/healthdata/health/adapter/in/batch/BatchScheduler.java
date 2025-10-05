package com.healthdata.health.adapter.in.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("aggregateHealthDataJob")
    private final Job aggregateHealthDataJob;

    @Qualifier("aggregateMonthlyHealthDataJob")
    private final Job aggregateMonthlyHealthDataJob;

    /**
     * 매일 새벽 2시에 전날 데이터 집계
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyAggregation() {
        try {
            log.info("Starting daily health data aggregation batch job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("runTime", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(aggregateHealthDataJob, jobParameters);

            log.info("Daily health data aggregation batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to run daily health data aggregation batch job", e);
        }
    }

    /**
     * 테스트용: 수동으로 배치 실행 (필요시 활성화)
     */
    // @Scheduled(fixedDelay = 60000) // 1분마다 실행 (테스트용)
    public void runTestAggregation() {
        try {
            log.info("Starting test health data aggregation batch job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("runTime", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(aggregateHealthDataJob, jobParameters);

            log.info("Test health data aggregation batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to run test health data aggregation batch job", e);
        }
    }

    /**
     * 매월 1일 새벽 3시에 전달 데이터 집계 (일별 배치 완료 후 실행)
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void runMonthlyAggregation() {
        try {
            log.info("Starting monthly health data aggregation batch job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("runTime", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(aggregateMonthlyHealthDataJob, jobParameters);

            log.info("Monthly health data aggregation batch job completed successfully");
        } catch (Exception e) {
            log.error("Failed to run monthly health data aggregation batch job", e);
        }
    }

    /**
     * 특정 날짜의 데이터를 집계하는 배치 실행 (수동 실행용)
     * @param targetDate 집계할 날짜
     * @return JobExecution 배치 실행 결과
     */
    public JobExecution runAggregationForDate(LocalDate targetDate) throws Exception {
        log.info("Starting health data aggregation batch job for date: {}", targetDate);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("runTime", LocalDateTime.now())
                .addString("targetDate", targetDate.toString())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(aggregateHealthDataJob, jobParameters);

        log.info("Health data aggregation batch job for date {} completed with status: {}",
                targetDate, execution.getStatus());

        return execution;
    }

    /**
     * 특정 월의 데이터를 집계하는 배치 실행 (수동 실행용)
     * @param targetMonth 집계할 월 (null이면 지난달)
     * @return JobExecution 배치 실행 결과
     */
    public JobExecution runMonthlyAggregationForMonth(YearMonth targetMonth) throws Exception {
        YearMonth month = (targetMonth != null) ? targetMonth : YearMonth.now().minusMonths(1);
        log.info("Starting monthly health data aggregation batch job for month: {}", month);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("runTime", LocalDateTime.now())
                .addString("targetMonth", month.toString())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(aggregateMonthlyHealthDataJob, jobParameters);

        log.info("Monthly health data aggregation batch job for month {} completed with status: {}",
                month, execution.getStatus());

        return execution;
    }
}
