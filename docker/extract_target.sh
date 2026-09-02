#!/bin/bash
# ==============================================================================
# Script: extract_target.sh
# Purpose: Automatically checkout Defects4J project, extract target class source,
#          and generate bug info for team members.
# Usage: ./extract_target.sh [Project] [Bug_ID]
# Example: ./extract_target.sh Lang 1
# ==============================================================================

set -e

PROJECT=${1:-Lang}
BUG_ID=${2:-1}
WORK_DIR="/tmp/${PROJECT}_${BUG_ID}_buggy"
OUTPUT_DIR="/workspace/target_benchmark/${PROJECT}_${BUG_ID}b"

echo "=========================================================="
echo " [Member 4 Tool] Defects4J Target Extraction Pipeline"
echo " Project: $PROJECT | Bug ID: $BUG_ID (Buggy Version: ${BUG_ID}b)"
echo "=========================================================="

# 1. สร้างโฟลเดอร์ปลายทาง
mkdir -p "$OUTPUT_DIR"

# 2. ทำการ Checkout โปรเจกต์ Defects4J หากยังไม่มี
if [ -d "$WORK_DIR" ]; then
    echo ">> Found existing directory at $WORK_DIR. Skipping checkout..."
else
    echo ">> Checking out $PROJECT-${BUG_ID}b to $WORK_DIR..."
    defects4j checkout -p "$PROJECT" -v "${BUG_ID}b" -w "$WORK_DIR"
fi

cd "$WORK_DIR"

# 3. คอมไพล์โปรเจกต์
echo ">> Compiling project..."
defects4j compile

# 4. สกัดข้อมูลบั๊ก Ground Truth เก็บเป็นไฟล์ข้อความ
echo ">> Extracting bug ground truth info..."
defects4j info -p "$PROJECT" -b "$BUG_ID" > "$OUTPUT_DIR/defects4j_info.txt"

# 5. ค้นหาและคัดลอก Modified Classes ออกมาไว้ที่ Host Workspace
echo ">> Locating and copying target class source files..."
# ดึงรายชื่อ modified classes จาก defects4j info
MODIFIED_CLASSES=$(defects4j info -p "$PROJECT" -b "$BUG_ID" | grep -A 20 "List of modified sources" | grep " - " | awk '{print $2}')

if [ -n "$MODIFIED_CLASSES" ]; then
    for CLASS in $MODIFIED_CLASSES; do
        CLASS_PATH=$(echo "$CLASS" | tr '.' '/')
        # ค้นหาไฟล์ .java ใน src
        SOURCE_FILE=$(find "$WORK_DIR" -type f -name "$(basename $CLASS_PATH).java" | grep -v "/test/" | head -n 1)
        if [ -f "$SOURCE_FILE" ]; then
            echo "   -> Copying $(basename $SOURCE_FILE) to $OUTPUT_DIR"
            cp "$SOURCE_FILE" "$OUTPUT_DIR/"
        fi
    done
else
    # กรณีหาไม่พบ ให้ fallback หาคลาสทั่วไปของ Lang-1
    if [ "$PROJECT" = "Lang" ] && [ "$BUG_ID" = "1" ]; then
        cp "$WORK_DIR/src/main/java/org/apache/commons/lang3/math/NumberUtils.java" "$OUTPUT_DIR/" 2>/dev/null || true
    fi
fi

echo "=========================================================="
echo " Extraction Completed Successfully!"
echo " Source code & info saved to: $OUTPUT_DIR"
echo " Member 1, 2, 3 can now access files directly from host workspace."
echo "=========================================================="
