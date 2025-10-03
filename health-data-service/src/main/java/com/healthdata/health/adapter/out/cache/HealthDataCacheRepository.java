package com.healthdata.health.adapter.out.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HealthDataCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_DATA_PREFIX = "health:hot:";
    private static final String WARM_DATA_PREFIX = "health:warm:";

    /**
     * Hot Data: 실시간 집계 데이터 조회
     */
    public Optional<Object> getHotData(String recordKey, LocalDate date) {
        String key = HOT_DATA_PREFIX + recordKey + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                log.debug("Cache hit for hot data: key={}", key);
            }
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.error("Failed to get hot data from Redis: key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Warm Data: 배치 작업으로 생성된 요약 데이터 조회
     */
    public Optional<Object> getWarmData(String recordKey, LocalDate date) {
        String key = WARM_DATA_PREFIX + recordKey + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                log.debug("Cache hit for warm data: key={}", key);
            }
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.error("Failed to get warm data from Redis: key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Warm Data 저장 (배치 작업에서 사용)
     */
    public void saveWarmData(String recordKey, LocalDate date, Object data, Duration ttl) {
        String key = WARM_DATA_PREFIX + recordKey + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            redisTemplate.opsForValue().set(key, data, ttl);
            log.info("Saved warm data to Redis: key={}", key);
        } catch (Exception e) {
            log.error("Failed to save warm data to Redis: key={}", key, e);
        }
    }

    /**
     * 캐시 삭제
     */
    public void evict(String recordKey, LocalDate date) {
        String hotKey = HOT_DATA_PREFIX + recordKey + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String warmKey = WARM_DATA_PREFIX + recordKey + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            redisTemplate.delete(hotKey);
            redisTemplate.delete(warmKey);
            log.info("Evicted cache for recordKey={}, date={}", recordKey, date);
        } catch (Exception e) {
            log.error("Failed to evict cache: recordKey={}, date={}", recordKey, date, e);
        }
    }
}
