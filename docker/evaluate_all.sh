#!/bin/bash
# ==============================================================================
# Script: evaluate_all.sh
# Purpose: Benchmark evaluator for all 4 test generation tools:
#          1. Combinatorial IPO (PICT)
#          2. MIO Algorithm (EvoSuite)
#          3. Claude Sonnet 4.6
#          4. Gemini 3.6 Flash
# Usage: ./evaluate_all.sh [Project] [Bug_ID]
# Example: ./evaluate_all.sh Lang 1
# ==============================================================================

PROJECT=${1:-Lang}
BUG_ID=${2:-1}

BUGGY_DIR="/tmp/${PROJECT}_${BUG_ID}_buggy"
FIXED_DIR="/tmp/${PROJECT}_${BUG_ID}_fixed"
RESULTS_FILE="/workspace/benchmark_results.md"

echo "=========================================================="
echo " [Member 4 Tool] Defects4J Automated Benchmark Evaluator"
echo " Project: $PROJECT | Bug ID: $BUG_ID"
echo "=========================================================="

# 1. ตรวจสอบและ Checkout ทั้งเวอร์ชัน Buggy (b) และ Fixed (f)
if [ ! -d "$BUGGY_DIR" ]; then
    echo ">> Checking out Buggy version ($PROJECT-${BUG_ID}b)..."
    defects4j checkout -p "$PROJECT" -v "${BUG_ID}b" -w "$BUGGY_DIR"
fi

if [ ! -d "$FIXED_DIR" ]; then
    echo ">> Checking out Fixed version ($PROJECT-${BUG_ID}f)..."
    defects4j checkout -p "$PROJECT" -v "${BUG_ID}f" -w "$FIXED_DIR"
fi

# ระบุไดเรกทอรี Test สำหรับ Lang
TEST_TARGET_SUBDIR="src/test/java/org/apache/commons/lang3/math"

# กำหนดรายชื่อเครื่องมือและโฟลเดอร์ TestCode
TOOLS=("IPO_Algorithm" "MIO_EvoSuite" "Claude_Sonnet_4_6" "Gemini_3_6_Flash")
TEST_PATHS=(
    "/workspace/Combinatorial_IPO/TestCode"
    "/workspace/MIO_Algorithm/TestCode"
    "/workspace/Claude-sonnet_4_6/TestCode"
    "/workspace/Gemini-3_6_flash/TestCode"
)

# เตรียมส่วนหัวของไฟล์ Markdown สรุปผล
cat << EOF > "$RESULTS_FILE"
# 📊 ผลการทดสอบและประเมินประสิทธิภาพ (Benchmark Evaluation Results)

**โครงการ:** Defects4J - $PROJECT (Bug ID: $BUG_ID)  
**วันที่ประเมินผล:** $(date '+%Y-%m-%d %H:%M:%S')  
**ผู้ประเมิน:** Member 4 (นายศิฆรินทร์ อุปจันทร์ - Infrastructure Lead)

---

## 📈 ตารางเปรียบเทียบผลลัพธ์ (Comparison Table)

| เครื่องมือ / อัลกอริทึม | สถานะการรัน | Line Coverage | Branch Coverage | Fault Detected? | หมายเหตุ |
| :--- | :---: | :---: | :---: | :---: | :--- |
EOF

for i in "${!TOOLS[@]}"; do
    TOOL_NAME="${TOOLS[$i]}"
    TEST_DIR="${TEST_PATHS[$i]}"

    echo "----------------------------------------------------------"
    echo ">> Evaluating: $TOOL_NAME"
    echo "   Checking test files in: $TEST_DIR"

    # หาไฟล์ .java ในโฟลเดอร์
    TEST_FILES=$(find "$TEST_DIR" -type f -name "*Test*.java" 2>/dev/null || true)

    if [ -z "$TEST_FILES" ]; then
        echo "   [!] No test files found yet in $TEST_DIR. Skipping..."
        echo "| **$TOOL_NAME** | ⏳ รอไฟล์ Test | - | - | - | ยังไม่มีไฟล์ใน TestCode/ |" >> "$RESULTS_FILE"
        continue
    fi

    for TEST_FILE in $TEST_FILES; do
        FILENAME=$(basename "$TEST_FILE")
        echo "   -> Processing $FILENAME ..."

        # 1. คัดลอกไฟล์ Test เข้าสู่โปรเจกต์ Buggy
        mkdir -p "$BUGGY_DIR/$TEST_TARGET_SUBDIR"
        cp "$TEST_FILE" "$BUGGY_DIR/$TEST_TARGET_SUBDIR/"

        # 2. ทดสอบคอมไพล์
        cd "$BUGGY_DIR"
        if ! defects4j compile > /dev/null 2>&1; then
            echo "   [X] Compilation Failed for $FILENAME"
            echo "| **$TOOL_NAME** | ❌ Compile Error | 0% | 0% | No | คอมไพล์ไม่ผ่านบน Java 8 |" >> "$RESULTS_FILE"
            rm -f "$BUGGY_DIR/$TEST_TARGET_SUBDIR/$FILENAME"
            continue
        fi

        # 3. สกัด Coverage
        echo "   -> Measuring code coverage..."
        defects4j coverage > /dev/null 2>&1 || true

        LINE_COV="N/A"
        BRANCH_COV="N/A"
        if [ -f "$BUGGY_DIR/coverage.xml" ]; then
            # ดึงค่า line-rate และ branch-rate จาก Cobertura XML
            LINE_RATE=$(grep -m 1 "line-rate=" "$BUGGY_DIR/coverage.xml" | sed -E 's/.*line-rate="([^"]+)".*/\1/' || echo "0")
            BRANCH_RATE=$(grep -m 1 "branch-rate=" "$BUGGY_DIR/coverage.xml" | sed -E 's/.*branch-rate="([^"]+)".*/\1/' || echo "0")
            LINE_COV=$(python3 -c "print(f'{float(\"$LINE_RATE\") * 100:.1f}%')" 2>/dev/null || echo "$LINE_RATE")
            BRANCH_COV=$(python3 -c "print(f'{float(\"$BRANCH_RATE\") * 100:.1f}%')" 2>/dev/null || echo "$BRANCH_RATE")
        fi

        # 4. ทดสอบตรวจจับบั๊ก (Fault Detection)
        # รันบน Buggy: ถ้า Fail แปลว่าทริกเกอร์บั๊ก
        echo "   -> Testing on Buggy version ($PROJECT-${BUG_ID}b)..."
        BUGGY_FAIL=0
        if defects4j test | grep -q "Failing tests:"; then
            BUGGY_FAIL=1
        fi

        # รันบน Fixed: คัดลอกเข้า Fixed แล้วเทส
        echo "   -> Verifying on Fixed version ($PROJECT-${BUG_ID}f)..."
        mkdir -p "$FIXED_DIR/$TEST_TARGET_SUBDIR"
        cp "$TEST_FILE" "$FIXED_DIR/$TEST_TARGET_SUBDIR/"
        cd "$FIXED_DIR"
        defects4j compile > /dev/null 2>&1 || true
        FIXED_PASS=0
        if ! defects4j test | grep -q "Failing tests:"; then
            FIXED_PASS=1
        fi

        FAULT_DETECTED="No"
        if [ "$BUGGY_FAIL" -eq 1 ] && [ "$FIXED_PASS" -eq 1 ]; then
            FAULT_DETECTED="✅ YES (Detected)"
        elif [ "$BUGGY_FAIL" -eq 1 ] && [ "$FIXED_PASS" -eq 0 ]; then
            FAULT_DETECTED="⚠️ Test Error (Fails on both)"
        fi

        echo "   [✓] Result: Line Cov: $LINE_COV | Branch Cov: $BRANCH_COV | Fault Detected: $FAULT_DETECTED"
        echo "| **$TOOL_NAME** | ✅ ผ่าน ($FILENAME) | $LINE_COV | $BRANCH_COV | $FAULT_DETECTED | ประเมินผลสำเร็จ |" >> "$RESULTS_FILE"

        # คลีนไฟล์ออกเพื่อไม่ให้กระทบเครื่องมือตัวถัดไป
        rm -f "$BUGGY_DIR/$TEST_TARGET_SUBDIR/$FILENAME"
        rm -f "$FIXED_DIR/$TEST_TARGET_SUBDIR/$FILENAME"
    done
done

echo "=========================================================="
echo " Evaluation Finished!"
echo " Summary report written to: $RESULTS_FILE"
echo "=========================================================="
cat "$RESULTS_FILE"
