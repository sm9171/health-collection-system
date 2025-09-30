#!/bin/bash

echo "Stopping Health Data Collection System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to stop service by PID file
stop_service() {
    local service_name=$1
    local pid_file="pids/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $pid)..."
            kill $pid
            
            # Wait for graceful shutdown
            local attempt=1
            while [ $attempt -le 10 ] && ps -p $pid > /dev/null 2>&1; do
                sleep 1
                ((attempt++))
            done
            
            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                echo "Force killing $service_name..."
                kill -9 $pid
            fi
            
            echo -e "${GREEN}✓ $service_name stopped${NC}"
        else
            echo "$service_name was not running"
        fi
        rm -f "$pid_file"
    else
        echo "No PID file found for $service_name"
    fi
}

# Create directories if they don't exist
mkdir -p pids

# Stop services in reverse order
echo -e "${YELLOW}Stopping services...${NC}"

stop_service "health-data-service"
stop_service "user-service" 
stop_service "gateway-server"
stop_service "discovery-server"
stop_service "config-server"

# Clean up any remaining processes
echo "Cleaning up any remaining processes..."
pkill -f "config-server.*\.jar" 2>/dev/null
pkill -f "discovery-server.*\.jar" 2>/dev/null
pkill -f "gateway-server.*\.jar" 2>/dev/null
pkill -f "user-service.*\.jar" 2>/dev/null
pkill -f "health-data-service.*\.jar" 2>/dev/null

# Stop infrastructure services
echo -e "${YELLOW}Stopping infrastructure services...${NC}"
docker-compose -f infra/docker/docker-compose.yml down

echo -e "${GREEN}"
echo "=========================================="
echo "🛑 All services stopped successfully!"
echo "=========================================="
echo -e "${NC}"