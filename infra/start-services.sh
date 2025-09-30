#!/bin/bash

echo "Starting Health Data Collection System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is ready${NC}"
            return 0
        fi
        
        echo "Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 5
        ((attempt++))
    done
    
    echo -e "${RED}✗ $service_name failed to start within timeout${NC}"
    return 1
}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi

# Start infrastructure services
echo -e "${YELLOW}Starting infrastructure services...${NC}"

# Start MySQL, Redis, Kafka
docker-compose -f infra/docker/docker-compose.yml up -d

echo "Waiting for MySQL to be ready..."
sleep 10

# Verify MySQL is ready
if ! docker exec health-mysql mysqladmin -uroot -proot ping --silent; then
    echo -e "${RED}MySQL failed to start!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Infrastructure services started${NC}"

# Build all services
echo -e "${YELLOW}Building all services...${NC}"
if ! ./gradlew clean bootJar -x test; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Build completed successfully${NC}"

# Start microservices in order
echo -e "${YELLOW}Starting microservices...${NC}"

# 1. Config Server
echo "Starting Config Server..."
nohup java -jar config-server/build/libs/config-server-0.0.1-SNAPSHOT.jar > logs/config-server.log 2>&1 &
CONFIG_PID=$!
echo $CONFIG_PID > pids/config-server.pid

if ! wait_for_service "Config Server" "http://localhost:8888/actuator/health"; then
    kill $CONFIG_PID 2>/dev/null
    exit 1
fi

# 2. Discovery Server
echo "Starting Discovery Server..."
nohup java -jar discovery-server/build/libs/discovery-server-0.0.1-SNAPSHOT.jar > logs/discovery-server.log 2>&1 &
DISCOVERY_PID=$!
echo $DISCOVERY_PID > pids/discovery-server.pid

if ! wait_for_service "Discovery Server" "http://localhost:8761/actuator/health"; then
    kill $CONFIG_PID $DISCOVERY_PID 2>/dev/null
    exit 1
fi

# 3. Gateway Server
echo "Starting Gateway Server..."
nohup java -jar gateway-server/build/libs/gateway-server-0.0.1-SNAPSHOT.jar > logs/gateway-server.log 2>&1 &
GATEWAY_PID=$!
echo $GATEWAY_PID > pids/gateway-server.pid

if ! wait_for_service "Gateway Server" "http://localhost:8080/actuator/health"; then
    kill $CONFIG_PID $DISCOVERY_PID $GATEWAY_PID 2>/dev/null
    exit 1
fi

# 4. User Service
echo "Starting User Service..."
nohup java -jar user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar > logs/user-service.log 2>&1 &
USER_PID=$!
echo $USER_PID > pids/user-service.pid

if ! wait_for_service "User Service" "http://localhost:8081/actuator/health"; then
    kill $CONFIG_PID $DISCOVERY_PID $GATEWAY_PID $USER_PID 2>/dev/null
    exit 1
fi

# 5. Health Data Service
echo "Starting Health Data Service..."
nohup java -jar health-data-service/build/libs/health-data-service-0.0.1-SNAPSHOT.jar > logs/health-data-service.log 2>&1 &
HEALTH_PID=$!
echo $HEALTH_PID > pids/health-data-service.pid

if ! wait_for_service "Health Data Service" "http://localhost:8082/actuator/health"; then
    kill $CONFIG_PID $DISCOVERY_PID $GATEWAY_PID $USER_PID $HEALTH_PID 2>/dev/null
    exit 1
fi

echo -e "${GREEN}"
echo "=========================================="
echo "🎉 All services started successfully!"
echo "=========================================="
echo "Service URLs:"
echo "• Eureka Dashboard: http://localhost:8761"
echo "• API Gateway: http://localhost:8080"
echo "• User Service: http://localhost:8081"
echo "• Health Data Service: http://localhost:8082"
echo ""
echo "API Documentation:"
echo "• User API: http://localhost:8081/swagger-ui.html"
echo "• Health Data API: http://localhost:8082/swagger-ui.html"
echo ""
echo "To stop all services, run: ./infra/stop-services.sh"
echo -e "${NC}"