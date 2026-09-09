#!/bin/bash
# ==============================================================================
# Script: extract_representative_17.sh
# Purpose: Automatically checkout and extract target class sources + metadata
#          for all 17 representative projects in Defects4J.
# Designed by: Member 4 (Infrastructure & Benchmark Manager)
# Usage (inside container): bash /workspace/scripts/extract_representative_17.sh
# ==============================================================================

set -e

echo "=========================================================="
echo " 🚀 Defects4J 17-Project Representative Extractor"
echo " Extracting curated target classes for all 17 projects..."
echo "=========================================================="

TARGET_BUGS=(
  "Chart:1"
  "Cli:1"
  "Closure:1"
  "Codec:1"
  "Collections:25"
  "Compress:1"
  "Csv:1"
  "Gson:1"
  "JacksonCore:1"
  "JacksonDatabind:1"
  "JacksonXml:1"
  "Jsoup:1"
  "JxPath:1"
  "Lang:1"
  "Math:2"
  "Mockito:1"
  "Time:1"
)

TOTAL=${#TARGET_BUGS[@]}
COUNT=1

for ITEM in "${TARGET_BUGS[@]}"; do
    IFS=":" read -r PROJECT BUG_ID <<< "$ITEM"
    echo ""
    echo "[$COUNT/$TOTAL] Processing Project: $PROJECT (Bug ID: $BUG_ID)..."
    
    WORK_DIR="/tmp/${PROJECT}_${BUG_ID}_buggy"
    OUTPUT_DIR="/workspace/target_benchmark/${PROJECT}_${BUG_ID}b"
    mkdir -p "$OUTPUT_DIR"
    
    # 1. Checkout
    if [ -d "$WORK_DIR" ]; then
        echo "   -> Found existing workdir at $WORK_DIR. Skipping checkout..."
    else
        echo "   -> Checking out $PROJECT-${BUG_ID}b..."
        defects4j checkout -p "$PROJECT" -v "${BUG_ID}b" -w "$WORK_DIR" || {
            echo "   ⚠️ Warning: Checkout failed for $PROJECT-$BUG_ID, continuing..."
            continue
        }
    fi
    
    cd "$WORK_DIR"
    
    # 2. Compile & Metadata
    echo "   -> Extracting ground truth metadata..."
    defects4j info -p "$PROJECT" -b "$BUG_ID" > "$OUTPUT_DIR/defects4j_info.txt" 2>/dev/null || true
    
    # 3. Copy target source classes
    MODIFIED_CLASSES=$(defects4j export -p classes.modified 2>/dev/null || true)
    SRC_DIR=$(defects4j export -p dir.src.classes 2>/dev/null || true)
    
    if [ -n "$MODIFIED_CLASSES" ]; then
        for CLASS in $MODIFIED_CLASSES; do
            CLASS_PATH=$(echo "$CLASS" | tr '.' '/')
            SOURCE_FILE="$WORK_DIR/$SRC_DIR/${CLASS_PATH}.java"
            if [ -f "$SOURCE_FILE" ]; then
                echo "   -> Copying $(basename "$SOURCE_FILE") to $OUTPUT_DIR"
                cp "$SOURCE_FILE" "$OUTPUT_DIR/"
            else
                ALT_FILE=$(find "$WORK_DIR" -type f -name "$(basename "$CLASS_PATH").java" | grep -v "/test/" | head -n 1)
                if [ -f "$ALT_FILE" ]; then
                    echo "   -> Copying $(basename "$ALT_FILE") to $OUTPUT_DIR (found via search)"
                    cp "$ALT_FILE" "$OUTPUT_DIR/"
                fi
            fi
        done
    fi
    
    COUNT=$((COUNT + 1))
done

echo ""
echo "=========================================================="
echo " 🎉 Successfully Extracted 17 Representative Projects!"
echo " All source files and info saved to /workspace/target_benchmark/"
echo "=========================================================="
