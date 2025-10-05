package com.healthdata.health.adapter.in.batch;

import com.healthdata.health.adapter.out.persistence.HealthDataSummaryJpaEntity;
import com.healthdata.health.adapter.out.persistence.MonthlyHealthDataSummaryJpaEntity;
import com.healthdata.health.adapter.out.persistence.MonthlyHealthDataSummaryJpaRepository;
import com.healthdata.health.adapter.out.persistence.MonthlyHealthDataSummaryMapper;
import com.healthdata.health.domain.model.MonthlyHealthDataSummary;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MonthlyHealthDataAggregationJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MonthlyHealthDataSummaryJpaRepository monthlySummaryRepository;
    private final MonthlyHealthDataSummaryMapper monthlySummaryMapper;

    @Bean
    public Job aggregateMonthlyHealthDataJob() {
        return new JobBuilder("aggregateMonthlyHealthDataJob", jobRepository)
                .start(aggregateMonthlyStep())
                .build();
    }

    @Bean
    public Step aggregateMonthlyStep() {
        return new StepBuilder("aggregateMonthlyStep", jobRepository)
                .<Map<String, Object>, MonthlyHealthDataSummary>chunk(100, transactionManager)
                .reader(monthlyHealthDataReader(null))
                .processor(monthlyHealthDataProcessor(null))
                .writer(monthlyHealthDataWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Map<String, Object>> monthlyHealthDataReader(
            @Value("#{jobParameters['targetMonth']}") String targetMonthStr) {

        // JobParameter에서 월 파싱, 없으면 지난달 사용
        YearMonth month;
        if (targetMonthStr != null && !targetMonthStr.isEmpty()) {
            month = YearMonth.parse(targetMonthStr);
        } else {
            month = YearMonth.now().minusMonths(1);
        }

        // 지난달의 1일부터 마지막 날까지
        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();

        String query = "SELECT new map(" +
                       "s.recordKey as recordKey, " +
                       "SUM(s.totalSteps) as totalSteps, " +
                       "SUM(s.totalCalories) as totalCalories, " +
                       "SUM(s.totalDistance) as totalDistance, " +
                       "COUNT(s) as entryCount) " +
                       "FROM HealthDataSummaryJpaEntity s " +
                       "WHERE s.summaryDate >= :firstDay AND s.summaryDate <= :lastDay " +
                       "GROUP BY s.recordKey";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstDay", firstDay);
        parameters.put("lastDay", lastDay);

        log.info("Creating monthlyHealthDataReader for month: {}", month);

        return new JpaPagingItemReaderBuilder<Map<String, Object>>()
                .name("monthlyHealthDataReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(query)
                .parameterValues(parameters)
                .pageSize(100)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, MonthlyHealthDataSummary> monthlyHealthDataProcessor(
            @Value("#{jobParameters['targetMonth']}") String targetMonthStr) {

        // JobParameter에서 월 파싱, 없으면 지난달 사용
        YearMonth month;
        if (targetMonthStr != null && !targetMonthStr.isEmpty()) {
            month = YearMonth.parse(targetMonthStr);
        } else {
            month = YearMonth.now().minusMonths(1);
        }

        return item -> {
            String recordKey = (String) item.get("recordKey");
            Long totalSteps = (Long) item.get("totalSteps");
            Double totalCalories = (Double) item.get("totalCalories");
            Double totalDistance = (Double) item.get("totalDistance");
            Long entryCount = (Long) item.get("entryCount");

            MonthlyHealthDataSummary summary = MonthlyHealthDataSummary.create(recordKey, month);
            summary.setData(
                    totalSteps.intValue(),
                    totalCalories.floatValue(),
                    totalDistance.floatValue(),
                    entryCount.intValue()
            );

            log.info("Processing monthly summary: recordKey={}, month={}, steps={}, calories={}, distance={}",
                    recordKey, month, totalSteps, totalCalories, totalDistance);

            return summary;
        };
    }

    @Bean
    public ItemWriter<MonthlyHealthDataSummary> monthlyHealthDataWriter() {
        return items -> {
            for (MonthlyHealthDataSummary summary : items) {
                YearMonth targetMonth = summary.getSummaryMonth();

                // 기존 월별 합계 조회
                Optional<MonthlyHealthDataSummaryJpaEntity> existingEntity =
                        monthlySummaryRepository.findByRecordKeyAndYearMonth(
                                summary.getRecordKey(),
                                targetMonth.getYear(),
                                targetMonth.getMonthValue());

                MonthlyHealthDataSummaryJpaEntity entity;
                if (existingEntity.isPresent()) {
                    // 기존 데이터 업데이트
                    entity = existingEntity.get();
                    MonthlyHealthDataSummary existing = monthlySummaryMapper.toDomain(entity);
                    existing.setData(
                            summary.getTotalSteps(),
                            summary.getTotalCalories(),
                            summary.getTotalDistance(),
                            summary.getEntryCount()
                    );
                    entity = monthlySummaryMapper.toEntity(existing);
                } else {
                    // 새로운 데이터 생성
                    entity = monthlySummaryMapper.toEntity(summary);
                }

                monthlySummaryRepository.save(entity);

                log.info("Saved monthly summary: recordKey={}, month={}, steps={}, calories={}, distance={}",
                        summary.getRecordKey(), summary.getSummaryMonth(),
                        summary.getTotalSteps(), summary.getTotalCalories(), summary.getTotalDistance());
            }
        };
    }
}
