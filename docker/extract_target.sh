#!/bin/bash
# ==============================================================================
# Script: extract_target.sh
# Purpose: Automatically checkout Defects4J project, extract target class source,
#          and generate bug info for team members (Member 1, 2, 3).
# Usage: ./extract_target.sh [Project] [Bug_ID]
# Example: ./extract_target.sh Lang 1
# ==============================================================================

set -e

PROJECT=${1:-Lang}
BUG_ID=${2:-1}
WORK_DIR="/tmp/${PROJECT}_${BUG_ID}_buggy"
OUTPUT_DIR="/workspace/target_benchmark/${PROJECT}_${BUG_ID}b"

echo "=========================================================="
echo " [Member 4 Tool] Defects4J Dynamic Target Extraction"
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

# 5. สกัดรายชื่อ Modified Classes แบบ Dynamic ด้วย defects4j export
echo ">> Locating and copying target class source files..."
MODIFIED_CLASSES=$(defects4j export -p classes.modified)
SRC_DIR=$(defects4j export -p dir.src.classes)

if [ -n "$MODIFIED_CLASSES" ]; then
    for CLASS in $MODIFIED_CLASSES; do
        CLASS_PATH=$(echo "$CLASS" | tr '.' '/')
        SOURCE_FILE="$WORK_DIR/$SRC_DIR/${CLASS_PATH}.java"
        if [ -f "$SOURCE_FILE" ]; then
            echo "   -> Copying $(basename "$SOURCE_FILE") to $OUTPUT_DIR"
            cp "$SOURCE_FILE" "$OUTPUT_DIR/"
        else
            # ค้นหาแบบ recursive กรณี path ไม่ตรงกับ src dir
            ALT_FILE=$(find "$WORK_DIR" -type f -name "$(basename "$CLASS_PATH").java" | grep -v "/test/" | head -n 1)
            if [ -f "$ALT_FILE" ]; then
                echo "   -> Copying $(basename "$ALT_FILE") to $OUTPUT_DIR (found via search)"
                cp "$ALT_FILE" "$OUTPUT_DIR/"
            fi
        fi
    done
fi

echo "=========================================================="
echo " Extraction Completed Successfully!"
echo " Source code & info saved to: $OUTPUT_DIR"
echo " Member 1, 2, 3 can now access files directly from host workspace."
echo "=========================================================="
