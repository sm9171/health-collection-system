#!/bin/bash

echo "Building all modules..."

modules=(
    "config-server"
    "discovery-server" 
    "gateway-server"
    "user-service"
    "health-data-service"
)

for module in "${modules[@]}"
do
    echo "Building $module..."
    cd ../$module
    ./gradlew clean bootJar -x test
    echo "$module build completed"
done

echo "All modules built successfully!"