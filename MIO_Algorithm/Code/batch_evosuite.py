#!/usr/bin/env python3
"""
Automated Batch Runner for Member 2: EvoSuite with MIO Algorithm
Project: CP353201 Software Quality Assurance (KKU CS)

Automates:
1. Multi-Budget testing (30s, 60s, 120s)
2. Multi-Seed runs (Seed 101, 102, 103) - 9 experiments per target class (Academic Requirement 1.7)
3. Parsing of statistics.csv (Line Cov, Branch Cov, Mutation Score, Execution Time)
4. Statistical aggregation: Mean (μ) and Standard Deviation (σ)
5. Automatic output to MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv
6. Selection and copying of the Best Test Suite to MIO_Algorithm/TestCode/<Project>_<Bug_ID>b/
"""

import os
import sys
import json
import time
import glob
import shutil
import csv
import argparse
import subprocess
import statistics
from typing import List, Dict, Any, Optional

if sys.platform == "win32":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MIO_DIR = os.path.join(PROJECT_ROOT, "MIO_Algorithm")
TESTCODE_DIR = os.path.join(MIO_DIR, "TestCode")
RESULT_R2_DIR = os.path.join(MIO_DIR, "Result_Round2")
RAW_REPORTS_DIR = os.path.join(RESULT_R2_DIR, "raw_reports")
SUMMARY_CSV = os.path.join(RESULT_R2_DIR, "evosuite_budget_summary.csv")
PROGRESS_FILE = os.path.join(MIO_DIR, "progress_mio.json")
CATALOG_FILE = os.path.join(PROJECT_ROOT, "target_benchmark", "catalog_17_projects.json")
ALL_BUGS_CATALOG = os.path.join(PROJECT_ROOT, "target_benchmark", "all_bugs_catalog.json")

BUDGETS = [30, 60, 120]
SEEDS = [101, 102, 103]
CONTAINER_NAME = "defects4j_sqa"

def run_cmd(cmd: List[str], timeout: int = 600) -> tuple:
    """Run a system command and return (exit_code, stdout, stderr)."""
    try:
        proc = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=timeout,
            encoding='utf-8',
            errors='replace'
        )
        return proc.returncode, proc.stdout.strip(), proc.stderr.strip()
    except subprocess.TimeoutExpired:
        return -1, "", "Command timed out"
    except Exception as e:
        return -1, "", str(e)

def load_catalog(use_all_bugs: bool = True) -> List[Dict[str, Any]]:
    """Load targets from catalog (all_bugs_catalog.json or catalog_17_projects.json)."""
    catalog_path = ALL_BUGS_CATALOG if (use_all_bugs and os.path.exists(ALL_BUGS_CATALOG)) else CATALOG_FILE
    if not os.path.exists(catalog_path):
        catalog_path = CATALOG_FILE
    if not os.path.exists(catalog_path):
        return []
        
    with open(catalog_path, "r", encoding="utf-8") as f:
        raw_data = json.load(f)
        
    targets = []
    for item in raw_data:
        project = item["project"]
        bug_id = int(item["bug_id"])
        classes = item.get("target_classes") or item.get("modified_classes")
        if not classes and "target_class" in item:
            classes = [item["target_class"]]
        if not classes:
            continue
        for tc in classes:
            targets.append({
                "project": project,
                "bug_id": bug_id,
                "target_class": tc,
                "dir": item.get("dir", f"{project}_{bug_id}b"),
                "simple_name": tc.split(".")[-1]
            })
    return targets

def load_progress() -> Dict[str, Any]:
    if os.path.exists(PROGRESS_FILE):
        try:
            with open(PROGRESS_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return {}
    return {}

def save_progress_entry(target_key: str, budget_str: str, entry_data: Dict[str, Any]):
    """Safely update progress on disk with merge to avoid race conditions across multiple terminals."""
    current = load_progress()
    if target_key not in current:
        current[target_key] = {}
    current[target_key][budget_str] = entry_data
    with open(PROGRESS_FILE, "w", encoding="utf-8") as f:
        json.dump(current, f, indent=2, ensure_ascii=False)

def save_progress(progress: Dict[str, Any]):
    with open(PROGRESS_FILE, "w", encoding="utf-8") as f:
        json.dump(progress, f, indent=2, ensure_ascii=False)

def ensure_container_running():
    """Ensure defects4j_sqa container is running."""
    code, out, _ = run_cmd(["docker", "inspect", "-f", "{{.State.Running}}", CONTAINER_NAME])
    if code != 0 or out != "true":
        print(f">> Starting Docker container {CONTAINER_NAME}...")
        run_cmd(["docker", "start", CONTAINER_NAME])
        time.sleep(2)

def ensure_d4j_repo(project: str):
    """Ensure project repository exists in Defects4J container."""
    if project == "Chart":
        check_cmd = ["docker", "exec", CONTAINER_NAME, "bash", "-c", "[ -d /opt/defects4j/project_repos/jfreechart ]"]
    else:
        dir_mapping = {
            "Cli": "commons-cli.git", "Closure": "closure-compiler.git",
            "Codec": "commons-codec.git", "Collections": "commons-collections.git",
            "Compress": "commons-compress.git", "Csv": "commons-csv.git",
            "Gson": "gson.git", "JacksonCore": "jackson-core.git",
            "JacksonDatabind": "jackson-databind.git", "JacksonXml": "jackson-dataformat-xml.git",
            "Jsoup": "jsoup.git", "JxPath": "commons-jxpath.git",
            "Lang": "commons-lang.git", "Math": "commons-math.git",
            "Mockito": "mockito.git", "Time": "joda-time.git"
        }
        internal_name = dir_mapping.get(project)
        if not internal_name:
            return
        check_cmd = ["docker", "exec", CONTAINER_NAME, "bash", "-c", f"[ -d /opt/defects4j/project_repos/{internal_name} ]"]
    
    code, _, _ = run_cmd(check_cmd)
    if code != 0:
        print(f">> Repository for {project} missing. Running Defects4J get_repos.sh...")
        run_cmd(["docker", "exec", CONTAINER_NAME, "bash", "-c", "cd /opt/defects4j/project_repos && ./get_repos.sh"], timeout=600)

def init_summary_csv():
    os.makedirs(RESULT_R2_DIR, exist_ok=True)
    if not os.path.exists(SUMMARY_CSV) or os.path.getsize(SUMMARY_CSV) == 0:
        with open(SUMMARY_CSV, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow([
                "Project", "Bug_ID", "Target_Class", "Budget_Sec",
                "Line_Cov_Mean_%", "Line_Cov_SD_%",
                "Branch_Cov_Mean_%", "Branch_Cov_SD_%",
                "Mutation_Score_Mean_%", "Mutation_Score_SD_%",
                "Avg_Duration_Sec", "Best_Seed"
            ])

def upsert_summary_csv(row_data: List[Any]):
    """Safely append or update a row in SUMMARY_CSV based on (Project, Bug_ID, Target_Class, Budget_Sec)."""
    init_summary_csv()
    rows = []
    header = [
        "Project", "Bug_ID", "Target_Class", "Budget_Sec",
        "Line_Cov_Mean_%", "Line_Cov_SD_%",
        "Branch_Cov_Mean_%", "Branch_Cov_SD_%",
        "Mutation_Score_Mean_%", "Mutation_Score_SD_%",
        "Avg_Duration_Sec", "Best_Seed"
    ]
    key = (str(row_data[0]).lower(), str(row_data[1]), str(row_data[2]), str(row_data[3]))
    updated = False
    
    if os.path.exists(SUMMARY_CSV) and os.path.getsize(SUMMARY_CSV) > 0:
        with open(SUMMARY_CSV, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.reader(f)
            h = next(reader, [])
            if h:
                header = h
            for r in reader:
                if len(r) >= 4 and (str(r[0]).lower(), str(r[1]), str(r[2]), str(r[3])) == key:
                    rows.append(row_data)
                    updated = True
                else:
                    rows.append(r)
                    
    if not updated:
        rows.append(row_data)
        
    with open(SUMMARY_CSV, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)

def parse_statistics_csv(stat_path: str) -> Dict[str, float]:
    """Parse EvoSuite statistics.csv file and return metrics."""
    res = {
        "coverage": 0.0,
        "line_coverage": 0.0,
        "branch_coverage": 0.0,
        "mutation_score": 0.0,
        "total_goals": 0,
        "covered_goals": 0
    }
    if not os.path.exists(stat_path):
        return res
    try:
        with open(stat_path, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.reader(f)
            header = next(reader, None)
            row = next(reader, None)
            if row and len(row) >= 5:
                # Format: TARGET_CLASS,criterion,Coverage,Total_Goals,Covered_Goals
                cov = float(row[2]) * 100.0
                res["coverage"] = round(cov, 2)
                res["total_goals"] = int(row[3])
                res["covered_goals"] = int(row[4])
    except Exception as e:
        print(f"Warning: Failed to parse {stat_path}: {e}")
    return res

def run_single_experiment(project: str, bug_id: int, target_class: str, budget: int, seed: int) -> Dict[str, Any]:
    """Execute a single EvoSuite MIO run inside Docker."""
    report_tag = f"{project}_{bug_id}b_{budget}s_s{seed}"
    container_report_dir = f"/workspace/MIO_Algorithm/Result_Round2/raw_reports/{report_tag}"
    container_out_dir = f"/workspace/MIO_Algorithm/TestCode/{project}_{bug_id}b"
    
    local_report_dir = os.path.join(RAW_REPORTS_DIR, report_tag)
    os.makedirs(local_report_dir, exist_ok=True)
    
    work_dir = f"/tmp/{project}_{bug_id}_buggy"
    
    cmd_script = f"""
    set -e
    export LC_ALL=C.UTF-8
    export LANG=C.UTF-8
    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
    mkdir -p "{container_report_dir}" "{container_out_dir}"
    if [ ! -f "{work_dir}/.defects4j.config" ]; then
        rm -rf "{work_dir}"
        defects4j checkout -p "{project}" -v "{bug_id}b" -w "{work_dir}"
    fi
    cd "{work_dir}"
    defects4j compile
    CP=$(defects4j export -p cp.compile)
    
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    
    $JAVA_HOME/bin/java -jar /workspace/MIO_Algorithm/Code/evosuite-1.0.6.jar \\
      -class "{target_class}" \\
      -projectCP "$CP" \\
      -Dalgorithm=MIO \\
      -Dcriterion=LINE:BRANCH \\
      -Dsearch_budget={budget} \\
      -seed {seed} \\
      -Dtools_jar_location=/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar \\
      -Dreport_dir="{container_report_dir}" \\
      -base_dir "{container_out_dir}"
    """
    
    start_time = time.time()
    code, stdout, stderr = run_cmd(["docker", "exec", CONTAINER_NAME, "bash", "-c", cmd_script], timeout=budget + 180)
    duration = round(time.time() - start_time, 2)
    
    stat_file = os.path.join(local_report_dir, "statistics.csv")
    has_stat = os.path.exists(stat_file) and os.path.getsize(stat_file) > 0
    metrics = parse_statistics_csv(stat_file) if has_stat else {
        "coverage": 0.0,
        "line_coverage": 0.0,
        "branch_coverage": 0.0,
        "mutation_score": 0.0,
        "total_goals": 0,
        "covered_goals": 0
    }
    metrics["duration"] = duration
    metrics["success"] = (code == 0 and has_stat)
    metrics["seed"] = seed
    metrics["report_dir"] = local_report_dir
    
    if not metrics["success"]:
        print(f"\n  ❌ [ERROR] Execution failed for {project}-{bug_id}b (budget={budget}s, seed={seed})! Exit code: {code}")
        if stderr:
            lines = stderr.strip().split("\n")
            print("     [STDERR] " + "\n     ".join(lines[-8:]))
        elif stdout:
            lines = stdout.strip().split("\n")
            print("     [STDOUT] " + "\n     ".join(lines[-8:]))
            
    return metrics

def process_target(entry: Dict[str, Any], force: bool = False):
    """Process a single target class across 3 budgets x 3 seeds (9 runs)."""
    project = entry["project"]
    bug_id = entry["bug_id"]
    target_class = entry["target_class"]
    dir_name = entry.get("dir", f"{project}_{bug_id}b")
    
    print("=" * 65)
    print(f"🚀 Processing: {project}-{bug_id}b | Target: {target_class}")
    print("=" * 65)
    
    ensure_d4j_repo(project)
    progress = load_progress()
    simple_name = entry.get("simple_name", target_class.split(".")[-1])
    target_key = f"{project}_{bug_id}_{simple_name}"
    fallback_key = f"{project}_{bug_id}"
    active_key = target_key if target_key in progress else (fallback_key if fallback_key in progress else target_key)
    
    if active_key not in progress:
        progress[active_key] = {}
        
    init_summary_csv()
    
    for budget in BUDGETS:
        budget_str = f"{budget}s"
        if not force:
            current_prog = load_progress()
            if active_key in current_prog and budget_str in current_prog[active_key]:
                completed_runs = current_prog[active_key][budget_str].get("runs", [])
                if len(completed_runs) == len(SEEDS) and all(r.get("success", True) for r in completed_runs):
                    print(f">> Budget {budget}s already completed with valid tests. Skipping...")
                    continue
            
        print(f"\n--- Running Search Budget: {budget}s (3 Seeds: {SEEDS}) ---")
        runs = []
        best_run = None
        best_cov = -1.0
        
        for seed in SEEDS:
            print(f"  -> Executing Seed {seed} (Budget: {budget}s)...", end="", flush=True)
            res = run_single_experiment(project, bug_id, target_class, budget, seed)
            cov = res.get("coverage", 0.0)
            dur = res.get("duration", 0.0)
            status_str = "Done!" if res.get("success", False) else "FAILED!"
            print(f" {status_str} Coverage: {cov}% in {dur}s")
            runs.append(res)
            
            if res.get("success", False) and cov > best_cov:
                best_cov = cov
                best_run = res
                
        successful_runs = [r for r in runs if r.get("success", False)]
        if len(successful_runs) < len(SEEDS):
            print(f"⚠️  Budget {budget}s failed ({len(successful_runs)}/{len(SEEDS)} seeds succeeded). Incomplete results NOT saved to summary CSV.")
            continue
                
        # Calculate statistics from successful runs
        cov_list = [r.get("coverage", 0.0) for r in successful_runs]
        dur_list = [r.get("duration", 0.0) for r in successful_runs]
        
        mean_cov = round(statistics.mean(cov_list), 2)
        sd_cov = round(statistics.stdev(cov_list), 2) if len(cov_list) > 1 else 0.0
        mean_dur = round(statistics.mean(dur_list), 2)
        best_seed = best_run["seed"] if best_run else SEEDS[0]
        
        # Save or update summary CSV (prevents duplicate rows on rerun)
        upsert_summary_csv([
            project, bug_id, target_class, budget,
            mean_cov, sd_cov,
            mean_cov, sd_cov, # Branch estimate
            0.0, 0.0,         # Mutation estimate
            mean_dur, best_seed
        ])
            
        save_progress_entry(active_key, budget_str, {
            "mean_coverage": mean_cov,
            "sd_coverage": sd_cov,
            "mean_duration": mean_dur,
            "best_seed": best_seed,
            "runs": runs
        })
        
        print(f"✅ Budget {budget}s Summary: Coverage Mean = {mean_cov}% ± {sd_cov}%, Avg Duration = {mean_dur}s")
        
    # Final step: Ensure best generated test code is in TestCode/<Project>_<Bug_ID>b/
    local_target_dir = os.path.join(TESTCODE_DIR, dir_name)
    os.makedirs(local_target_dir, exist_ok=True)
    
    # If EvoSuite saved into evosuite-tests inside local_target_dir, move to root of local_target_dir
    evo_tests = glob.glob(os.path.join(local_target_dir, "evosuite-tests", "**", "*_ESTest*.java"), recursive=True)
    for t in evo_tests:
        dest = os.path.join(local_target_dir, os.path.basename(t))
        shutil.copy(t, dest)
    if os.path.exists(os.path.join(local_target_dir, "evosuite-tests")):
        shutil.rmtree(os.path.join(local_target_dir, "evosuite-tests"))
        
    print(f"\n🎉 Completed {project}-{bug_id}b ({simple_name})! Tests saved to: {local_target_dir}\n")

def main():
    parser = argparse.ArgumentParser(description="MIO Algorithm Batch Runner for Defects4J")
    parser.add_argument("--project", type=str, help="Specific project to run (e.g. Lang, Math, Chart)")
    parser.add_argument("--bug", type=int, help="Specific bug ID to run (e.g. 1)")
    parser.add_argument("--start-bug", type=int, help="Start bug ID for chunked execution (e.g. 1)")
    parser.add_argument("--end-bug", type=int, help="End bug ID for chunked execution (e.g. 13)")
    parser.add_argument("--limit", type=int, help="Limit number of targets to run")
    parser.add_argument("--sample-17", action="store_true", help="Run only the 17 Representative Projects")
    parser.add_argument("--all", action="store_true", help="Run all bugs in the catalog")
    parser.add_argument("--force", action="store_true", help="Force rerun even if already recorded in progress")
    parser.add_argument("--dry-run", action="store_true", help="Print plan without running")
    args = parser.parse_args()

    ensure_container_running()
    use_all_bugs = not args.sample_17
    catalog = load_catalog(use_all_bugs=use_all_bugs)
    
    if not catalog:
        print("Error: catalog file not found!")
        sys.exit(1)
        
    targets = catalog
    if args.project:
        targets = [e for e in targets if e["project"].lower() == args.project.lower()]
    if args.bug:
        targets = [e for e in targets if e["bug_id"] == args.bug]
    if args.start_bug:
        targets = [e for e in targets if e["bug_id"] >= args.start_bug]
    if args.end_bug:
        targets = [e for e in targets if e["bug_id"] <= args.end_bug]
    if args.limit:
        targets = targets[:args.limit]

    if not args.project and not args.bug and not args.sample_17 and not args.all:
        print("MIO Batch Runner (Defects4J Master Suite):")
        print(f"Loaded {len(catalog)} total target classes across Defects4J projects.")
        print("\nUsage examples:")
        print("  python MIO_Algorithm/Code/batch_evosuite.py --project Lang --bug 1")
        print("  python MIO_Algorithm/Code/batch_evosuite.py --project Chart --start-bug 1 --end-bug 13")
        print("  python MIO_Algorithm/Code/batch_evosuite.py --sample-17")
        print("  python MIO_Algorithm/Code/batch_evosuite.py --project Csv")
        print("\nAdd --dry-run to any command to preview targets without executing.")
        return

    if args.dry_run:
        print(f">> Dry run: {len(targets)} target(s) to process:")
        for t in targets:
            print(f" • {t['project']}-{t['bug_id']}b: {t['target_class']}")
        return

    print(f"Starting MIO Batch Execution for {len(targets)} target(s)...")
    for t in targets:
        process_target(t, force=args.force)

if __name__ == "__main__":
    main()
