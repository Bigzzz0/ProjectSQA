# Combinatorial IPO - Unified End-to-End Pipeline Runner (PowerShell)
# Stages: Audit -> Canary Gate -> Queue Generation -> Summary & Validate
# Full autonomy, fast resume (<1ms skip), and automatic failure isolation.

$ErrorActionPreference = "Stop"

function Check-LastExitCode {
    param([string]$StageName)
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n[FATAL] $StageName failed with exit code $LASTEXITCODE. Halting pipeline execution immediately." -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "   Native IPO 2-Way All-Class Unified Pipeline Runner" -ForegroundColor Cyan
Write-Host "   Stage 1: Feasibility Audit & Inventory Refresh (1,070 targets)" -ForegroundColor Cyan
Write-Host "   Stage 2: Deterministic Canary Gate (Quality Gate)" -ForegroundColor Cyan
Write-Host "   Stage 3: Full Queue Generation (Fast Resume & Isolated Logs)" -ForegroundColor Cyan
Write-Host "   Stage 4: Summary & Integrity Validation" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Audit stage
Write-Host "`n[Stage 1/4] Running Feasibility Audit across 1,070 targets..." -ForegroundColor Yellow
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode audit --resume
Check-LastExitCode "[Stage 1/4] Feasibility Audit"

# 2. Canary Gate stage
Write-Host "`n[Stage 2/4] Running Canary Gate..." -ForegroundColor Yellow
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode canary --resume
Check-LastExitCode "[Stage 2/4] Canary Gate"

# 3. Full Generation stage
Write-Host "`n[Stage 3/4] Running Full Queue Generation..." -ForegroundColor Yellow
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume
Check-LastExitCode "[Stage 3/4] Full Queue Generation"

# 4. Summary & Validation stage
Write-Host "`n[Stage 4/4] Final Validation & Summary..." -ForegroundColor Green
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary
Check-LastExitCode "[Stage 4/4] Summary"

docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
Check-LastExitCode "[Stage 4/4] Validation"

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "   Pipeline Finished Successfully! See Results/logs/failures.log if any errors." -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
