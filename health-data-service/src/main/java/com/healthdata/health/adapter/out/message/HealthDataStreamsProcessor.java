package com.healthdata.health.adapter.out.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthdata.health.domain.event.HealthDataCollectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class HealthDataStreamsProcessor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${kafka.topic.health-data-collected:health-data-collected}")
    private String sourceTopic;

    @Bean
    public KStream<String, HealthDataCollectedEvent> healthDataStream(StreamsBuilder streamsBuilder) {
        JsonSerde<HealthDataCollectedEvent> eventSerde = new JsonSerde<>(HealthDataCollectedEvent.class);

        KStream<String, HealthDataCollectedEvent> stream = streamsBuilder
                .stream(sourceTopic, Consumed.with(Serdes.String(), eventSerde));

        // 실시간 집계: recordKey + 날짜별로 그룹화
        stream
                .groupBy(
                        (key, event) -> event.getRecordKey() + ":" + toDateString(event.getFromTime()),
                        Grouped.with(Serdes.String(), eventSerde)
                )
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(10)))
                .aggregate(
                        DailyAggregation::new,
                        (key, event, aggregate) -> {
                            aggregate.addSteps(event.getSteps());
                            aggregate.addCalories(event.getCaloriesValue());
                            aggregate.addDistance(event.getDistanceValue());
                            aggregate.incrementCount();
                            return aggregate;
                        },
                        Materialized.with(Serdes.String(), new JsonSerde<>(DailyAggregation.class))
                )
                .toStream()
                .foreach((windowedKey, aggregation) -> {
                    String redisKey = "health:hot:" + windowedKey.key();
                    try {
                        redisTemplate.opsForValue().set(redisKey, aggregation, Duration.ofHours(24));
                        log.info("Updated hot data in Redis: key={}, steps={}, calories={}, distance={}",
                                redisKey, aggregation.getTotalSteps(), aggregation.getTotalCalories(), aggregation.getTotalDistance());
                    } catch (Exception e) {
                        log.error("Failed to update Redis: key={}", redisKey, e);
                    }
                });

        return stream;
    }

    private String toDateString(LocalDateTime dateTime) {
        return dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static class DailyAggregation {
        private int totalSteps = 0;
        private float totalCalories = 0.0f;
        private float totalDistance = 0.0f;
        private int count = 0;

        public void addSteps(int steps) {
            this.totalSteps += steps;
        }

        public void addCalories(float calories) {
            this.totalCalories += calories;
        }

        public void addDistance(float distance) {
            this.totalDistance += distance;
        }

        public void incrementCount() {
            this.count++;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public float getTotalCalories() {
            return totalCalories;
        }

        public float getTotalDistance() {
            return totalDistance;
        }

        public int getCount() {
            return count;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public void setTotalCalories(float totalCalories) {
            this.totalCalories = totalCalories;
        }

        public void setTotalDistance(float totalDistance) {
            this.totalDistance = totalDistance;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
