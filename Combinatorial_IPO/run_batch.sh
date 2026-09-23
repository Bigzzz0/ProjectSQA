#!/usr/bin/env bash
# Combinatorial IPO - Unified End-to-End Pipeline Runner (Bash/Linux/Docker)
# Stages: Audit -> Canary Gate -> Queue Generation -> Summary & Validate
# Full autonomy, fast resume (<1ms skip), and automatic failure isolation.

set -e
export PYTHONPATH="/workspace/Combinatorial_IPO/Code"

echo "================================================================="
echo "   Native IPO 2-Way All-Class Unified Pipeline Runner"
echo "   Stage 1: Feasibility Audit & Inventory Refresh (1,070 targets)"
echo "   Stage 2: Deterministic Canary Gate"
echo "   Stage 3: Full Queue Generation (Fast Resume & Isolated Logs)"
echo "   Stage 4: Summary & Integrity Validation"
echo "================================================================="

echo ""
echo "[Stage 1/4] Running Feasibility Audit across 1,070 targets..."
python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode audit --resume

echo ""
echo "[Stage 2/4] Running Canary Gate..."
python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode canary --resume

echo ""
echo "[Stage 3/4] Running Full Queue Generation..."
python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume

echo ""
echo "[Stage 4/4] Final Validation & Summary..."
python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary
python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate

echo ""
echo "================================================================="
echo "   Pipeline Finished! See Results/logs/failures.log if any errors."
echo "================================================================="
