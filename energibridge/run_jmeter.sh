#!/usr/bin/env bash

# This script runs a JMeter load test and appends test metadata
# to the metadata.txt file created by measure_energy.sh.
# Usage:
#   ./run_jmeter.sh <test.jmx> <energibridge-output-dir>

# Check JMX file
if [ -z "$1" ]; then
  echo "Error: You must specify the JMX file."
  echo "Usage: ./run_jmeter.sh <test.jmx> <energibridge-output-dir>"
  exit 1
fi
JMX_FILE="$1"

# Check output directory
if [ -z "$2" ]; then
  echo "Error: You must specify the EnergiBridge output directory."
  echo "Usage: ./run_jmeter.sh <test.jmx> <energibridge-output-dir>"
  exit 1
fi
OUTPUT_DIR="$2"

META_FILE="$OUTPUT_DIR/metadata.txt"

# Check metadata file
if [ ! -f "$META_FILE" ]; then
  echo "Error: metadata.txt not found in:"
  echo "  $META_FILE"
  exit 1
fi

# Extract ThreadGroup parameters
THREADS=$(grep -oP '(?<=<intProp name="ThreadGroup.num_threads">)\d+' "$JMX_FILE")
RAMP_UP=$(grep -oP '(?<=<intProp name="ThreadGroup.ramp_time">)\d+' "$JMX_FILE")
DURATION=$(grep -oP '(?<=<longProp name="ThreadGroup.duration">)\d+' "$JMX_FILE")
DELAY=$(grep -oP '(?<=<longProp name="ThreadGroup.delay">)\d+' "$JMX_FILE")

# Extract endpoints
ENDPOINTS=$(grep -oP '(?<=<stringProp name="HTTPSampler.path">).*(?=</stringProp>)' "$JMX_FILE")

# Run JMeter
START_TIME=$(date +"%Y-%m-%d %H:%M:%S")
jmeter -n -t "$JMX_FILE"
END_TIME=$(date +"%Y-%m-%d %H:%M:%S")

# Append metadata
{
  echo ""
  echo "JMeter Load Test Metadata"
  echo "-------------------------"
  echo "Start time:  $START_TIME"
  echo "End time:    $END_TIME"
  echo "JMX file:    $JMX_FILE"
  echo ""
  echo "Thread count: $THREADS"
  echo "Ramp-up:      $RAMP_UP"
  echo "Duration:     $DURATION"
  echo "Delay:        $DELAY"
  echo ""
  echo "Endpoints used:"
  echo "$ENDPOINTS"
} >> "$META_FILE"

echo "JMeter metadata appended to: $META_FILE"

