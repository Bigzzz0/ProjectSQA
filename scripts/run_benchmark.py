#!/usr/bin/env python3
"""
Universal Benchmark Runner for ProjectSQA
Designed by Member 4 (Infrastructure & Data Analysis Lead)

Automates:
1. Dynamic project & bug discovery via Defects4J (pids & bids)
2. Test deployment & compilation for all 4 techniques (IPO, MIO, Claude, Gemini)
3. Target Modified Class coverage extraction (Line & Branch coverage via Cobertura)
4. Empirical Fault Detection Rate verification (comparing Buggy vs Fixed)
5. State persistence & Resumption (--resume) via progress.json
6. CSV & JSON result generation for reporting
"""

import os
import sys
import time
import json
import csv
import glob
import shutil
import argparse
from typing import List, Dict, Optional, Any, Tuple

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

# Add scripts directory to path for d4j_meta import
SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
sys.path.insert(0, SCRIPTS_DIR)

import d4j_meta

PROGRESS_FILE = os.path.join(PROJECT_ROOT, "progress.json")
RESULTS_DIR = os.path.join(PROJECT_ROOT, "results")
DEFAULT_CSV = os.path.join(RESULTS_DIR, "benchmark_results.csv")
WORK_BASE = "/tmp/d4j_eval"

# Representative 17 projects with default selected bugs (Active and clear modified classes)
REPRESENTATIVE_17 = [
    ("Chart", 1),
    ("Cli", 1),
    ("Closure", 1),
    ("Codec", 1),
    ("Collections", 25),
    ("Compress", 1),
    ("Csv", 1),
    ("Gson", 1),
    ("JacksonCore", 1),
    ("JacksonDatabind", 1),
    ("JacksonXml", 1),
    ("Jsoup", 1),
    ("JxPath", 1),
    ("Lang", 1),
    ("Math", 2),
    ("Mockito", 1),
    ("Time", 1)
]

TECHNIQUE_DIRS = {
    "ipo": os.path.join(PROJECT_ROOT, "Combinatorial_IPO", "TestCode"),
    "mio": os.path.join(PROJECT_ROOT, "MIO_Algorithm", "TestCode"),
    "claude": os.path.join(PROJECT_ROOT, "Claude-sonnet_5", "TestCode"),
    "gemini": os.path.join(PROJECT_ROOT, "Gemini-3_8_flash", "TestCode")
}

TECHNIQUE_NAMES = {
    "ipo": "IPO (Microsoft PICT)",
    "mio": "MIO (EvoSuite SBST)",
    "claude": "Claude Sonnet 5",
    "gemini": "Gemini 3.8 Flash"
}

def load_progress() -> Dict[str, Any]:
    if os.path.exists(PROGRESS_FILE):
        try:
            with open(PROGRESS_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return {}
    return {}

def save_progress(progress: Dict[str, Any]):
    os.makedirs(os.path.dirname(PROGRESS_FILE), exist_ok=True)
    with open(PROGRESS_FILE, "w", encoding="utf-8") as f:
        json.dump(progress, f, indent=2, ensure_ascii=False)

def init_csv(csv_path: str):
    os.makedirs(os.path.dirname(csv_path), exist_ok=True)
    if not os.path.exists(csv_path):
        with open(csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow([
                "Project", "Bug_ID", "Technique", "Target_Classes",
                "Test_File", "Line_Coverage_%", "Branch_Coverage_%",
                "Fault_Detected", "Execution_Status", "Timestamp"
            ])

def append_csv_result(csv_path: str, row: List[Any]):
    with open(csv_path, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(row)

def find_test_files_for_target(technique: str, project: str, bug_id: int, modified_classes: List[str]) -> List[str]:
    """Find generated test .java files matching target modified classes."""
    base_dir = TECHNIQUE_DIRS.get(technique)
    if not base_dir or not os.path.exists(base_dir):
        return []
    
    found = []
    # Search all .java files in test folder
    for f in glob.glob(os.path.join(base_dir, "**", "*Test*.java"), recursive=True):
        if "scaffolding" in f:
            continue
        fname = os.path.basename(f)
        # Check if file corresponds to any target class
        for tc in modified_classes:
            short_name = tc.split(".")[-1]
            if short_name.lower() in fname.lower():
                found.append(f)
                break
    
    # Fallback: if only 1 test file exists in folder, use it
    if not found:
        all_tests = [f for f in glob.glob(os.path.join(base_dir, "*Test*.java")) if "scaffolding" not in f]
        if len(all_tests) == 1:
            found = all_tests
            
    return found

def get_class_package(java_file_path: str) -> str:
    """Extract package declaration from a Java file."""
    with open(java_file_path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if line.startswith("package ") and line.endswith(";"):
                return line[8:-1].strip()
    return ""

import tarfile

def create_test_suite_archive(test_files: List[str], archive_path: str, technique: str) -> str:
    """
    Packages generated test file(s) into a Defects4J-compliant .tar.bz2 archive.
    Inside the tar, files are arranged by package directory hierarchy (e.g. org/apache/...).
    """
    os.makedirs(os.path.dirname(os.path.abspath(archive_path)), exist_ok=True)
    temp_staging = archive_path + "_staging"
    if os.path.exists(temp_staging):
        shutil.rmtree(temp_staging, ignore_errors=True)
    os.makedirs(temp_staging, exist_ok=True)

    top_dirs = set()
    for tf in test_files:
        pkg = get_class_package(tf)
        pkg_path = pkg.replace(".", os.sep) if pkg else ""
        dest_dir = os.path.join(temp_staging, pkg_path)
        os.makedirs(dest_dir, exist_ok=True)
        shutil.copy2(tf, dest_dir)
        
        # Scaffolding if MIO
        if technique == "mio":
            scaffold = tf.replace(".java", "_scaffolding.java")
            if os.path.exists(scaffold):
                shutil.copy2(scaffold, dest_dir)
                
        if pkg:
            top_dir = pkg.split(".")[0]
            top_dirs.add(top_dir)

    with tarfile.open(archive_path, "w:bz2") as tar:
        if top_dirs:
            for td in top_dirs:
                full_td = os.path.join(temp_staging, td)
                if os.path.exists(full_td):
                    tar.add(full_td, arcname=td)
        else:
            for f in os.listdir(temp_staging):
                tar.add(os.path.join(temp_staging, f), arcname=f)

    shutil.rmtree(temp_staging, ignore_errors=True)
    return archive_path

def parse_d4j_coverage_summary(summary_csv_path: str) -> Dict[str, float]:
    """
    Read summary.csv written automatically by defects4j coverage:
    LinesTotal,LinesCovered,ConditionsTotal,ConditionsCovered
    """
    if not os.path.exists(summary_csv_path):
        return {"line_coverage": 0.0, "branch_coverage": 0.0}
    try:
        with open(summary_csv_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                lt = int(row.get("LinesTotal", 0))
                lc = int(row.get("LinesCovered", 0))
                ct = int(row.get("ConditionsTotal", 0))
                cc = int(row.get("ConditionsCovered", 0))
                l_rate = round(lc / lt * 100.0, 2) if lt > 0 else 0.0
                b_rate = round(cc / ct * 100.0, 2) if ct > 0 else 0.0
                return {
                    "line_coverage": l_rate,
                    "branch_coverage": b_rate,
                    "lines_total": lt,
                    "lines_covered": lc,
                    "branches_total": ct,
                    "branches_covered": cc
                }
    except Exception as e:
        print(f"Warning: Failed to parse summary.csv: {e}")
    return {"line_coverage": 0.0, "branch_coverage": 0.0}

def count_failing_tests(work_dir: str) -> Tuple[int, List[str]]:
    """Count failing tests listed in $WORK_DIR/failing_tests."""
    fail_file = os.path.join(work_dir, "failing_tests")
    if not os.path.exists(fail_file):
        return 0, []
    failures = []
    with open(fail_file, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if line.startswith("--- "):
                failures.append(line[4:].strip())
    return len(failures), failures

def evaluate_technique_on_bug(
    project: str,
    bug_id: int,
    technique: str,
    csv_path: str,
    clean_tmp: bool = True
) -> Dict[str, Any]:
    """
    Evaluate a single technique on a specific bug using Defects4J native external test suite workflow (-s).
    """
    state_key = f"{project}-{bug_id}-{technique}"
    print(f"\n[{state_key}] Starting evaluation...")
    
    work_buggy = os.path.join(WORK_BASE, f"{project}_{bug_id}b")
    work_fixed = os.path.join(WORK_BASE, f"{project}_{bug_id}f")
    
    # 1. Checkout Buggy version
    print(f"[{state_key}] Checking out {project}-{bug_id}b...")
    if not d4j_meta.checkout_project(project, bug_id, is_buggy=True, work_dir=work_buggy):
        return {"status": "CHECKOUT_ERROR", "fault_detected": "NO", "line_cov": 0.0, "branch_cov": 0.0}
    
    # 2. Extract Metadata
    modified_classes = d4j_meta.get_modified_classes(work_buggy)
    print(f"[{state_key}] Target modified classes: {modified_classes}")
    
    # 3. Locate Test Files
    test_files = find_test_files_for_target(technique, project, bug_id, modified_classes)
    if not test_files:
        print(f"[{state_key}] ⏳ No test files found yet in {TECHNIQUE_DIRS[technique]}. Skipping...")
        return {
            "status": "WAITING_FOR_TESTS",
            "fault_detected": "-",
            "line_cov": "-",
            "branch_cov": "-",
            "test_file": "-"
        }
    
    test_file = test_files[0]
    test_fname = os.path.basename(test_file)
    print(f"[{state_key}] Packaging test files: {[os.path.basename(tf) for tf in test_files]}")
    
    # 4. Package External Test Suite into .tar.bz2
    archive_buggy = os.path.join(WORK_BASE, f"{project}-{bug_id}b-{technique}.1.tar.bz2")
    archive_fixed = os.path.join(WORK_BASE, f"{project}-{bug_id}f-{technique}.1.tar.bz2")
    create_test_suite_archive(test_files, archive_buggy, technique)
    create_test_suite_archive(test_files, archive_fixed, technique)
    
    # 5. Measure Code Coverage on Buggy using Defects4J native coverage -s
    print(f"[{state_key}] Measuring code coverage via defects4j coverage -s...")
    cov_code, cov_out, cov_err = d4j_meta.run_cmd(["defects4j", "coverage", "-w", work_buggy, "-s", archive_buggy], timeout=240)
    
    # Check for compile error or timeout during coverage
    if "timed out" in cov_err.lower():
        print(f"[{state_key}] ⏱️ Test execution TIMED OUT during coverage run!")
        return {"status": "TIMEOUT", "fault_detected": "TIMEOUT", "line_cov": 0.0, "branch_cov": 0.0, "test_file": test_fname}
    if "cannot compile" in (cov_out + cov_err).lower() or cov_code != 0 and not os.path.exists(os.path.join(work_buggy, "summary.csv")):
        print(f"[{state_key}] ❌ COMPILE ERROR: Generated test suite failed to compile!")
        return {"status": "COMPILE_ERROR", "fault_detected": "COMPILE_ERROR", "line_cov": 0.0, "branch_cov": 0.0, "test_file": test_fname}
        
    summary_csv = os.path.join(work_buggy, "summary.csv")
    cov_metrics = parse_d4j_coverage_summary(summary_csv)
    line_cov = cov_metrics.get("line_coverage", 0.0)
    branch_cov = cov_metrics.get("branch_coverage", 0.0)
    print(f"[{state_key}] Target Modified Class Coverage -> Line: {line_cov}% | Branch: {branch_cov}%")
    
    # 6. Test on Buggy version
    print(f"[{state_key}] Running test on Buggy version to verify failure exposure...")
    test_b_code, test_b_out, test_b_err = d4j_meta.run_cmd(["defects4j", "test", "-w", work_buggy, "-s", archive_buggy], timeout=240)
    if "timed out" in test_b_err.lower():
        return {"status": "TIMEOUT", "fault_detected": "TIMEOUT", "line_cov": line_cov, "branch_cov": branch_cov, "test_file": test_fname}
    fail_b_count, fail_b_list = count_failing_tests(work_buggy)
    print(f"[{state_key}] Buggy Failures ({fail_b_count}): {fail_b_list[:2]}")
    
    # 7. Checkout & Test on Fixed version
    print(f"[{state_key}] Checking out Fixed version ({project}-{bug_id}f)...")
    d4j_meta.checkout_project(project, bug_id, is_buggy=False, work_dir=work_fixed)
    print(f"[{state_key}] Running test on Fixed version to verify fix passing...")
    test_f_code, test_f_out, test_f_err = d4j_meta.run_cmd(["defects4j", "test", "-w", work_fixed, "-s", archive_fixed], timeout=240)
    if "timed out" in test_f_err.lower():
        return {"status": "TIMEOUT", "fault_detected": "TIMEOUT", "line_cov": line_cov, "branch_cov": branch_cov, "test_file": test_fname}
    fail_f_count, fail_f_list = count_failing_tests(work_fixed)
    print(f"[{state_key}] Fixed Failures ({fail_f_count}): {fail_f_list[:2]}")
    
    # 8. Classify Fault Detection with Academic Rigor
    if fail_b_count > 0 and fail_f_count == 0:
        fault_detected = f"BUG_DETECTED ({fail_b_count} Triggered)"
    elif fail_b_count > 0 and fail_f_count > 0:
        fault_detected = f"FLAKY_OR_REGRESSION ({fail_b_count} b-fail, {fail_f_count} f-fail)"
    else:
        fault_detected = "NOT_DETECTED (All passed)"
        
    print(f"[{state_key}] [RESULT] Status: DONE | Line Cov: {line_cov}% | Branch Cov: {branch_cov}% | Fault: {fault_detected}")
    
    res_dict = {
        "status": "DONE",
        "fault_detected": fault_detected,
        "line_cov": line_cov,
        "branch_cov": branch_cov,
        "test_file": test_fname,
        "target_classes": ";".join(modified_classes),
        "buggy_failures": fail_b_list,
        "fixed_failures": fail_f_list
    }
    
    # Save individual JSON
    bug_res_dir = os.path.join(RESULTS_DIR, project, str(bug_id))
    os.makedirs(bug_res_dir, exist_ok=True)
    with open(os.path.join(bug_res_dir, f"{technique}.json"), "w", encoding="utf-8") as jf:
        json.dump(res_dict, jf, indent=2)
    bug_res_dir = os.path.join(RESULTS_DIR, project, str(bug_id))
    os.makedirs(bug_res_dir, exist_ok=True)
    with open(os.path.join(bug_res_dir, f"{technique}.json"), "w", encoding="utf-8") as jf:
        json.dump(res_dict, jf, indent=2)
        
    # Append CSV
    append_csv_result(csv_path, [
        project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
        ";".join(modified_classes), test_fname, line_cov, branch_cov,
        fault_detected, "DONE", time.strftime("%Y-%m-%d %H:%M:%S")
    ])
    
    # Cleanup work dirs if requested
    if clean_tmp:
        shutil.rmtree(work_buggy, ignore_errors=True)
        shutil.rmtree(work_fixed, ignore_errors=True)
        
    return res_dict

def main():
    parser = argparse.ArgumentParser(description="Universal Benchmark Runner for Defects4J")
    parser.add_argument("--project", type=str, help="Run specific project (e.g. Lang)")
    parser.add_argument("--bug", type=int, help="Run specific bug ID (e.g. 1)")
    parser.add_argument("--sample-17", action="store_true", help="Run 17 Representative Projects Benchmark")
    parser.add_argument("--all-bugs", action="store_true", help="Run Exhaustive Benchmark on all active bugs")
    parser.add_argument("--techniques", type=str, default="ipo,mio,claude,gemini", help="Comma-separated techniques")
    parser.add_argument("--resume", action="store_true", help="Resume from progress.json")
    parser.add_argument("--csv", type=str, default=DEFAULT_CSV, help="Output CSV path")
    parser.add_argument("--no-clean", action="store_true", help="Do not delete /tmp folders after evaluation")
    args = parser.parse_args()

    init_csv(args.csv)
    progress = load_progress() if args.resume else {}
    techniques = [t.strip().lower() for t in args.techniques.split(",") if t.strip()]

    # Build evaluation queue
    queue: List[Tuple[str, int]] = []
    
    if args.project and args.bug:
        queue = [(args.project, args.bug)]
    elif args.sample_17 or (not args.all_bugs and not args.project):
        queue = REPRESENTATIVE_17
    elif args.all_bugs:
        projects = [args.project] if args.project else d4j_meta.get_all_projects()
        print(f"Discovering all active bugs for {len(projects)} projects...")
        for p in projects:
            bugs = d4j_meta.get_active_bugs(p)
            print(f"  -> {p}: {len(bugs)} active bugs")
            for b in bugs:
                queue.append((p, b))
    elif args.project:
        bugs = d4j_meta.get_active_bugs(args.project)
        queue = [(args.project, b) for b in bugs]

    print("=" * 65)
    print(f"🚀 ProjectSQA Universal Benchmark Runner")
    print(f"📊 Total Target Queue: {len(queue)} Bugs")
    print(f"🔬 Techniques: {', '.join([TECHNIQUE_NAMES.get(t, t) for t in techniques])}")
    print(f"💾 Results CSV: {args.csv}")
    print(f"🔄 Resume Mode: {'ENABLED' if args.resume else 'DISABLED'}")
    print("=" * 65)

    for proj, bid in queue:
        for tech in techniques:
            key = f"{proj}-{bid}-{tech}"
            if args.resume and progress.get(key, {}).get("status") == "DONE":
                print(f"⏩ [SKIP] {key} already completed in progress.json")
                continue
                
            try:
                res = evaluate_technique_on_bug(
                    proj, bid, tech, args.csv, clean_tmp=(not args.no_clean)
                )
                progress[key] = res
                save_progress(progress)
            except Exception as e:
                print(f"❌ Error evaluating {key}: {e}")
                progress[key] = {"status": "UNHANDLED_ERROR", "error": str(e)}
                save_progress(progress)

    print("\n" + "=" * 65)
    print(f"🎉 Benchmark Execution Completed!")
    print(f"📄 Summary CSV available at: {args.csv}")
    print("=" * 65)

if __name__ == "__main__":
    main()
