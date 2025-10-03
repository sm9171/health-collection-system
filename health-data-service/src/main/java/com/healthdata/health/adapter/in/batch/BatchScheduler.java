package com.healthdata.health.adapter.in.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job aggregateHealthDataJob;

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
}
