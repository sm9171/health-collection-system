package com.healthdata.health.adapter.in.batch;

import com.healthdata.health.adapter.out.cache.HealthDataCacheRepository;
import com.healthdata.health.adapter.out.persistence.*;
import com.healthdata.health.domain.model.HealthDataSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DailyHealthDataAggregationJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final HealthDataSummaryJpaRepository summaryRepository;
    private final HealthDataSummaryMapper summaryMapper;
    private final HealthDataCacheRepository cacheRepository;

    @Bean
    public Job aggregateHealthDataJob() {
        return new JobBuilder("aggregateHealthDataJob", jobRepository)
                .start(aggregateStep())
                .build();
    }

    @Bean
    public Step aggregateStep() {
        return new StepBuilder("aggregateStep", jobRepository)
                .<Map<String, Object>, HealthDataSummary>chunk(100, transactionManager)
                .reader(healthDataReader(null))
                .processor(healthDataProcessor())
                .writer(healthDataWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Map<String, Object>> healthDataReader(
            @Value("#{jobParameters['targetDate']}") String targetDateStr) {

        // JobParameter에서 날짜 파싱, 없으면 어제 날짜 사용
        LocalDate date;
        if (targetDateStr != null && !targetDateStr.isEmpty()) {
            date = LocalDate.parse(targetDateStr);
        } else {
            date = LocalDate.now().minusDays(1);
        }

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23, 59, 59);

        String query = "SELECT new map(" +
                       "h.recordKey as recordKey, " +
                       "CAST(h.fromTime AS LocalDate) as summaryDate, " +
                       "SUM(h.steps) as totalSteps, " +
                       "SUM(h.calories) as totalCalories, " +
                       "SUM(h.distance) as totalDistance, " +
                       "COUNT(h) as entryCount) " +
                       "FROM HealthDataJpaEntity h " +
                       "WHERE h.fromTime >= :from AND h.fromTime < :to " +
                       "GROUP BY h.recordKey, CAST(h.fromTime AS LocalDate)";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("from", from);
        parameters.put("to", to);

        log.info("Creating healthDataReader for date: {}", date);

        return new JpaPagingItemReaderBuilder<Map<String, Object>>()
                .name("healthDataReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(query)
                .parameterValues(parameters)
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Map<String, Object>, HealthDataSummary> healthDataProcessor() {
        return item -> {
            String recordKey = (String) item.get("recordKey");
            LocalDate summaryDate = (LocalDate) item.get("summaryDate");
            Long totalSteps = (Long) item.get("totalSteps");
            Double totalCalories = (Double) item.get("totalCalories");
            Double totalDistance = (Double) item.get("totalDistance");

            HealthDataSummary summary = HealthDataSummary.create(recordKey, summaryDate);
            summary.addData(
                    totalSteps.intValue(),
                    totalCalories.floatValue(),
                    totalDistance.floatValue()
            );

            log.info("Processing summary: recordKey={}, date={}, steps={}, calories={}, distance={}",
                    recordKey, summaryDate, totalSteps, totalCalories, totalDistance);

            return summary;
        };
    }

    @Bean
    public ItemWriter<HealthDataSummary> healthDataWriter() {
        return items -> {
            for (HealthDataSummary summary : items) {
                // 1. 일별 합계를 MySQL에 저장
                HealthDataSummaryJpaEntity entity = summaryMapper.toEntity(summary);
                summaryRepository.save(entity);

                // 2. Redis에 Warm Data로 캐싱 (7일간 보관)
                cacheRepository.saveWarmData(
                        summary.getRecordKey(),
                        summary.getSummaryDate(),
                        summary,
                        Duration.ofDays(7)
                );

                log.info("Saved daily summary: recordKey={}, date={}, steps={}, calories={}, distance={}",
                        summary.getRecordKey(), summary.getSummaryDate(),
                        summary.getTotalSteps(), summary.getTotalCalories(), summary.getTotalDistance());
            }
        };
    }
}
