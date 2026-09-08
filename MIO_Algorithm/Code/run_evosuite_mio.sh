#!/bin/bash
# ==============================================================================
# Script: run_evosuite_mio.sh
# Purpose: Run EvoSuite with MIO Algorithm on a Defects4J Target Class.
# Usage: ./run_evosuite_mio.sh [Project] [Bug_ID] [Target_Class] [Budget_Seconds]
# Example: ./run_evosuite_mio.sh Lang 1 org.apache.commons.lang3.math.NumberUtils 60
# ==============================================================================

set -e

PROJECT=${1:-Lang}
BUG_ID=${2:-1}
TARGET_CLASS=${3:-org.apache.commons.lang3.math.NumberUtils}
BUDGET=${4:-60}

WORK_DIR="/tmp/${PROJECT}_${BUG_ID}_buggy"
OUTPUT_DIR="/workspace/MIO_Algorithm/TestCode"
REPORT_DIR="/workspace/MIO_Algorithm/Result_Round1"
EVOSUITE_JAR="/opt/evosuite/evosuite-1.0.6.jar"

echo "=========================================================="
echo " [Member 2 Tool] EvoSuite MIO Automated Test Generation"
echo " Project: $PROJECT | Bug ID: $BUG_ID"
echo " Target Class: $TARGET_CLASS"
echo " Search Budget: $BUDGET seconds | Algorithm: MIO"
echo "=========================================================="

mkdir -p "$OUTPUT_DIR"
mkdir -p "$REPORT_DIR"

# 1. Checkout โปรเจกต์หากยังไม่มี
if [ ! -d "$WORK_DIR" ]; then
    echo ">> Checking out $PROJECT-${BUG_ID}b to $WORK_DIR..."
    defects4j checkout -p "$PROJECT" -v "${BUG_ID}b" -w "$WORK_DIR"
fi

cd "$WORK_DIR"

# 2. คอมไพล์โปรเจกต์
echo ">> Compiling project..."
defects4j compile

# 3. ดึง Classpath ของโปรเจกต์ออกมา
echo ">> Exporting compilation classpath..."
CP=$(defects4j export -p cp.compile)

# 4. สั่งรัน EvoSuite MIO (ใช้ Java 8 เพื่อรองรับ tools.jar)
JAVA_BIN="java"
if [ -f "/usr/lib/jvm/java-8-openjdk-amd64/bin/java" ]; then
    JAVA_BIN="/usr/lib/jvm/java-8-openjdk-amd64/bin/java"
fi

echo ">> Starting EvoSuite MIO Search (Budget: ${BUDGET}s) using $JAVA_BIN..."
"$JAVA_BIN" -jar "$EVOSUITE_JAR" \
  -class "$TARGET_CLASS" \
  -projectCP "$CP" \
  -Dalgorithm=MIO \
  -Dcriterion=LINE:BRANCH:EXCEPTION:MUTATION \
  -Dsearch_budget="$BUDGET" \
  -Dreport_dir="$REPORT_DIR" \
  -base_dir "$OUTPUT_DIR"

echo "=========================================================="
echo " [✓] EvoSuite MIO Generation Completed!"
echo " Tests saved to: $OUTPUT_DIR"
echo " Statistics saved to: $REPORT_DIR"
echo " Member 4 can now evaluate these tests in the runner."
echo "=========================================================="
