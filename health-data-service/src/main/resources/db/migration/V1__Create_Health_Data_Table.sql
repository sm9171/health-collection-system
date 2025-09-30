CREATE TABLE health_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_key VARCHAR(50) NOT NULL,
    from_time TIMESTAMP NOT NULL,
    to_time TIMESTAMP NOT NULL,
    steps INT NOT NULL,
    calories FLOAT NOT NULL,
    calories_unit VARCHAR(10) NOT NULL DEFAULT 'kcal',
    distance FLOAT NOT NULL,
    distance_unit VARCHAR(10) NOT NULL DEFAULT 'km',
    collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_record_key (record_key),
    INDEX idx_time_range (from_time, to_time),
    INDEX idx_collected_at (collected_at),
    UNIQUE KEY unique_data (record_key, from_time, to_time)
);