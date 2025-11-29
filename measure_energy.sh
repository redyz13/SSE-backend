#!/usr/bin/env bash

# This script measures the energy consumption of the Spring Boot backend.
# Usage:
#   ./measure-energy-backend.sh /path/to/EnergiBridge [seconds]

# Check argument
if [ -z "$1" ]; then
  echo "Error: You must specify the path to the EnergiBridge directory."
  echo "Usage: ./measure-energy-backend.sh /path/to/EnergiBridge [seconds]"
  exit 1
fi

ENERGIBRIDGE_DIR="$1"
ENERGIBRIDGE_BIN="$ENERGIBRIDGE_DIR/target/release/energibridge"

# Default measurement time is 15 seconds
MEASURE_TIME="${2:-15}"

# Check energibridge binary
if [ ! -f "$ENERGIBRIDGE_BIN" ]; then
  echo "Error: energibridge binary not found in:"
  echo "  $ENERGIBRIDGE_BIN"
  echo "Compile EnergiBridge with: cargo build -r"
  exit 1
fi

# Automatically set permissions for MSR registers (reset on reboot)
sudo chgrp -R wheel /dev/cpu/*/msr 2>/dev/null
sudo chmod g+r /dev/cpu/*/msr 2>/dev/null

# Apply raw I/O capability to energibridge binary
sudo setcap cap_sys_rawio=ep "$ENERGIBRIDGE_BIN"

# Check .env file
if [ ! -f ".env" ]; then
  echo "Error: .env file not found in the current directory."
  exit 1
fi

# Export .env variables
set -a
source .env
set +a

# Replace 'db' with 'localhost' inside SPRING_DATASOURCE_URL
SPRING_DATASOURCE_URL=$(echo "$SPRING_DATASOURCE_URL" | sed 's#mysql://db:#mysql://localhost:#')

# Detect JAR file
JAR_FILE=$(ls target/*.jar 2>/dev/null | head -n 1)
if [ -z "$JAR_FILE" ]; then
  echo "Error: No .jar file found in target/."
  exit 1
fi

# Create output directory
OUTPUT_DIR="energy-backend-results"
mkdir -p "$OUTPUT_DIR"

echo "---------------------------------------------"
echo "Using energibridge: $ENERGIBRIDGE_BIN"
echo "Using JAR:          $JAR_FILE"
echo "Execution time:     $MEASURE_TIME seconds"
echo "Output directory:   $OUTPUT_DIR"
echo "---------------------------------------------"
echo ""

# Generate timestamped CSV filename
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
OUTPUT_FILE="$OUTPUT_DIR/energy-backend-$TIMESTAMP.csv"

# Export patched env vars for Spring Boot
export SPRING_DATASOURCE_URL

# Measure energy consumption and save output to CSV
"$ENERGIBRIDGE_BIN" --summary -o "$OUTPUT_FILE" -m "$MEASURE_TIME" \
    bash -c "java -jar '$JAR_FILE'"

echo ""
echo "---------------------------------------------"
echo " Energy report saved: $OUTPUT_FILE"
echo "---------------------------------------------"

