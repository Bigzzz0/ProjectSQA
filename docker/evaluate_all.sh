#!/bin/bash
# ==============================================================================
# Script: evaluate_all.sh
# Purpose: Benchmark evaluator for all 4 test generation tools:
#          1. Combinatorial IPO (PICT)
#          2. MIO Algorithm (EvoSuite)
#          3. Claude Sonnet 5
#          4. Gemini 3.8 Flash
# Usage: ./evaluate_all.sh [Project] [Bug_ID]
# Example: ./evaluate_all.sh Lang 1
# ==============================================================================

set -e

PROJECT=${1:-Lang}
BUG_ID=${2:-1}

echo "=========================================================="
echo " [Member 4 Tool] Defects4J Automated Benchmark Evaluator"
echo " Project: $PROJECT | Bug ID: $BUG_ID"
echo " Calling Universal Python Runner for exact metric analysis..."
echo "=========================================================="

# เรียกใช้งาน Universal Runner ของ Member 4
python3 /workspace/scripts/run_benchmark.py --project "$PROJECT" --bug "$BUG_ID"

echo "=========================================================="
echo " [✓] Evaluation Finished!"
echo " Results saved to: /workspace/results/benchmark_results.csv"
echo " Detailed JSON:    /workspace/results/$PROJECT/$BUG_ID/"
echo "=========================================================="
