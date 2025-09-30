-- Create databases for microservices
CREATE DATABASE IF NOT EXISTS healthdb;
CREATE DATABASE IF NOT EXISTS userdb;

-- Grant permissions (optional, but recommended for production)
GRANT ALL PRIVILEGES ON healthdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON userdb.* TO 'root'@'%';
FLUSH PRIVILEGES;