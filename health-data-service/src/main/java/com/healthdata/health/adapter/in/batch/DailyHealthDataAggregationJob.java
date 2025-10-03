package com.healthdata.health.adapter.in.batch;

import com.healthdata.health.adapter.out.cache.HealthDataCacheRepository;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaEntity;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaRepository;
import com.healthdata.health.adapter.out.persistence.HealthDataSummaryMapper;
import com.healthdata.health.domain.model.HealthDataSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

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
                .reader(healthDataReader())
                .processor(healthDataProcessor())
                .writer(healthDataWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Map<String, Object>> healthDataReader() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime from = yesterday.atStartOfDay();
        LocalDateTime to = yesterday.atTime(23, 59, 59);

        String query = "SELECT h.recordKey as recordKey, " +
                       "DATE(h.fromTime) as summaryDate, " +
                       "SUM(h.steps) as totalSteps, " +
                       "SUM(h.calories.value) as totalCalories, " +
                       "SUM(h.distance.value) as totalDistance, " +
                       "COUNT(h) as entryCount " +
                       "FROM HealthDataJpaEntity h " +
                       "WHERE h.fromTime >= :from AND h.fromTime < :to " +
                       "GROUP BY h.recordKey, DATE(h.fromTime)";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("from", from);
        parameters.put("to", to);

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
            Long entryCount = (Long) item.get("entryCount");

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
                // MySQL에 저장
                HealthDataSummaryJpaEntity entity = summaryMapper.toEntity(summary);
                summaryRepository.save(entity);

                // Redis에 Warm Data로 캐싱 (7일간 보관)
                cacheRepository.saveWarmData(
                        summary.getRecordKey(),
                        summary.getSummaryDate(),
                        summary,
                        Duration.ofDays(7)
                );

                log.info("Saved summary: recordKey={}, date={}, steps={}, calories={}, distance={}",
                        summary.getRecordKey(), summary.getSummaryDate(),
                        summary.getTotalSteps(), summary.getTotalCalories(), summary.getTotalDistance());
            }
        };
    }
}
