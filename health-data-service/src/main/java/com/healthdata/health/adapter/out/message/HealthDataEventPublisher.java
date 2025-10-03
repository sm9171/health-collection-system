package com.healthdata.health.adapter.out.message;

import com.healthdata.health.domain.event.HealthDataCollectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthDataEventPublisher {

    private final KafkaTemplate<String, HealthDataCollectedEvent> kafkaTemplate;

    @Value("${kafka.topic.health-data-collected:health-data-collected}")
    private String topic;

    public void publish(HealthDataCollectedEvent event) {
        try {
            kafkaTemplate.send(topic, event.getRecordKey(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published health data event: recordKey={}, fromTime={}, toTime={}",
                                    event.getRecordKey(), event.getFromTime(), event.getToTime());
                        } else {
                            log.error("Failed to publish health data event: recordKey={}",
                                    event.getRecordKey(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing health data event: recordKey={}", event.getRecordKey(), e);
        }
    }
}