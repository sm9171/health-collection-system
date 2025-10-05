-- Rename health_data_summary to daily_health_data_summary
ALTER TABLE health_data_summary RENAME TO daily_health_data_summary;

-- Create monthly_health_data_summary table
CREATE TABLE monthly_health_data_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_key VARCHAR(255) NOT NULL,
    summary_year INT NOT NULL,
    summary_month INT NOT NULL,
    total_steps INT NOT NULL DEFAULT 0,
    total_calories FLOAT NOT NULL DEFAULT 0,
    total_distance FLOAT NOT NULL DEFAULT 0,
    entry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_monthly_summary (record_key, summary_year, summary_month),
    INDEX idx_monthly_record_key (record_key),
    INDEX idx_monthly_summary_year_month (summary_year, summary_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
