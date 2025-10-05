CREATE TABLE health_data_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_key VARCHAR(255) NOT NULL,
    summary_date DATE NOT NULL,
    total_steps INT NOT NULL DEFAULT 0,
    total_calories FLOAT NOT NULL DEFAULT 0,
    total_distance FLOAT NOT NULL DEFAULT 0,
    entry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_summary (record_key, summary_date),
    INDEX idx_record_key (record_key),
    INDEX idx_summary_date (summary_date),
    INDEX idx_record_key_date (record_key, summary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
