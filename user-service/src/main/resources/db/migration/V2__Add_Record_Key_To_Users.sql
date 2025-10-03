ALTER TABLE users
ADD COLUMN record_key VARCHAR(255) NOT NULL UNIQUE AFTER password;

CREATE INDEX idx_record_key ON users(record_key);