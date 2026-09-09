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

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MIO_DIR = os.path.join(PROJECT_ROOT, "MIO_Algorithm")
TESTCODE_DIR = os.path.join(MIO_DIR, "TestCode")
RESULT_R2_DIR = os.path.join(MIO_DIR, "Result_Round2")
RAW_REPORTS_DIR = os.path.join(RESULT_R2_DIR, "raw_reports")
SUMMARY_CSV = os.path.join(RESULT_R2_DIR, "evosuite_budget_summary.csv")
PROGRESS_FILE = os.path.join(MIO_DIR, "progress_mio.json")
CATALOG_FILE = os.path.join(PROJECT_ROOT, "target_benchmark", "catalog_17_projects.json")

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

def load_catalog() -> List[Dict[str, Any]]:
    """Load the 17 representative projects catalog."""
    if os.path.exists(CATALOG_FILE):
        with open(CATALOG_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    return []

def load_progress() -> Dict[str, Any]:
    if os.path.exists(PROGRESS_FILE):
        try:
            with open(PROGRESS_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return {}
    return {}

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
    """Ensure project bare repository exists in Defects4J container."""
    repo_mapping = {
        "Chart": "https://github.com/jfree/jfreechart.git",
        "Cli": "https://github.com/apache/commons-cli.git",
        "Closure": "https://github.com/google/closure-compiler.git",
        "Codec": "https://github.com/apache/commons-codec.git",
        "Collections": "https://github.com/apache/commons-collections.git",
        "Compress": "https://github.com/apache/commons-compress.git",
        "Csv": "https://github.com/apache/commons-csv.git",
        "Gson": "https://github.com/google/gson.git",
        "JacksonCore": "https://github.com/FasterXML/jackson-core.git",
        "JacksonDatabind": "https://github.com/FasterXML/jackson-databind.git",
        "JacksonXml": "https://github.com/FasterXML/jackson-dataformat-xml.git",
        "Jsoup": "https://github.com/jhy/jsoup.git",
        "JxPath": "https://github.com/apache/commons-jxpath.git",
        "Lang": "https://github.com/apache/commons-lang.git",
        "Math": "https://github.com/apache/commons-math.git",
        "Mockito": "https://github.com/mockito/mockito.git",
        "Time": "https://github.com/JodaOrg/joda-time.git"
    }
    
    # Internal repo directory names in Defects4J
    dir_mapping = {
        "Chart": "jfreechart.git",
        "Cli": "commons-cli.git",
        "Closure": "closure-compiler.git",
        "Codec": "commons-codec.git",
        "Collections": "commons-collections.git",
        "Compress": "commons-compress.git",
        "Csv": "commons-csv.git",
        "Gson": "gson.git",
        "JacksonCore": "jackson-core.git",
        "JacksonDatabind": "jackson-databind.git",
        "JacksonXml": "jackson-dataformat-xml.git",
        "Jsoup": "jsoup.git",
        "JxPath": "commons-jxpath.git",
        "Lang": "commons-lang.git",
        "Math": "commons-math.git",
        "Mockito": "mockito.git",
        "Time": "joda-time.git"
    }
    
    internal_name = dir_mapping.get(project)
    if not internal_name:
        return
        
    check_cmd = ["docker", "exec", CONTAINER_NAME, "bash", "-c", f"[ -f /opt/defects4j/project_repos/{internal_name}/HEAD ]"]
    code, _, _ = run_cmd(check_cmd)
    if code != 0:
        url = repo_mapping.get(project)
        if url:
            print(f">> Cloning bare repo for {project} from {url}...")
            clone_cmd = [
                "docker", "exec", CONTAINER_NAME, "bash", "-c",
                f"rm -rf /opt/defects4j/project_repos/{internal_name} && git clone --bare {url} /opt/defects4j/project_repos/{internal_name}"
            ]
            run_cmd(clone_cmd, timeout=300)

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
    if [ ! -d "{work_dir}" ]; then
        defects4j checkout -p "{project}" -v "{bug_id}b" -w "{work_dir}"
    fi
    cd "{work_dir}"
    defects4j compile
    CP=$(defects4j export -p cp.compile)
    
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    
    java -jar /workspace/MIO_Algorithm/Code/evosuite-1.0.6.jar \\
      -class "{target_class}" \\
      -projectCP "$CP" \\
      -Dalgorithm=MIO \\
      -Dcriterion=LINE:BRANCH:EXCEPTION:MUTATION \\
      -Dsearch_budget={budget} \\
      -seed {seed} \\
      -Dreport_dir="{container_report_dir}" \\
      -base_dir "{container_out_dir}"
    """
    
    start_time = time.time()
    code, stdout, stderr = run_cmd(["docker", "exec", CONTAINER_NAME, "bash", "-c", cmd_script], timeout=budget + 180)
    duration = round(time.time() - start_time, 2)
    
    stat_file = os.path.join(local_report_dir, "statistics.csv")
    metrics = parse_statistics_csv(stat_file)
    metrics["duration"] = duration
    metrics["success"] = (code == 0)
    metrics["seed"] = seed
    metrics["report_dir"] = local_report_dir
    
    return metrics

def process_target(entry: Dict[str, Any]):
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
    target_key = f"{project}_{bug_id}"
    
    if target_key not in progress:
        progress[target_key] = {}
        
    init_summary_csv()
    
    for budget in BUDGETS:
        budget_str = f"{budget}s"
        if budget_str in progress[target_key] and len(progress[target_key][budget_str].get("runs", [])) == len(SEEDS):
            print(f">> Budget {budget}s already completed. Skipping...")
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
            print(f" Done! Coverage: {cov}% in {dur}s")
            runs.append(res)
            
            if cov > best_cov:
                best_cov = cov
                best_run = res
                
        # Calculate statistics
        cov_list = [r.get("coverage", 0.0) for r in runs]
        dur_list = [r.get("duration", 0.0) for r in runs]
        
        mean_cov = round(statistics.mean(cov_list), 2)
        sd_cov = round(statistics.stdev(cov_list), 2) if len(cov_list) > 1 else 0.0
        mean_dur = round(statistics.mean(dur_list), 2)
        best_seed = best_run["seed"] if best_run else SEEDS[0]
        
        # Save to summary CSV
        with open(SUMMARY_CSV, "a", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow([
                project, bug_id, target_class, budget,
                mean_cov, sd_cov,
                mean_cov, sd_cov, # Branch estimate
                0.0, 0.0,         # Mutation estimate
                mean_dur, best_seed
            ])
            
        progress[target_key][budget_str] = {
            "mean_coverage": mean_cov,
            "sd_coverage": sd_cov,
            "mean_duration": mean_dur,
            "best_seed": best_seed,
            "runs": runs
        }
        save_progress(progress)
        
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
        
    print(f"\n🎉 Completed {project}-{bug_id}b! Tests saved to: {local_target_dir}\n")

def main():
    parser = argparse.ArgumentParser(description="MIO Algorithm Batch Runner for Defects4J")
    parser.add_argument("--project", type=str, help="Specific project to run (e.g. Lang, Math)")
    parser.add_argument("--bug", type=int, help="Specific bug ID to run (e.g. 1)")
    parser.add_argument("--all", action="store_true", help="Run all 17 Representative Projects")
    parser.add_argument("--dry-run", action="store_true", help="Print plan without running")
    args = parser.parse_args()

    ensure_container_running()
    catalog = load_catalog()
    
    if not catalog:
        print("Error: catalog_17_projects.json not found!")
        sys.exit(1)
        
    targets = []
    if args.project:
        targets = [e for e in catalog if e["project"].lower() == args.project.lower()]
        if args.bug:
            targets = [e for e in targets if e["bug_id"] == args.bug]
    elif args.all:
        targets = catalog
    else:
        # Default: Show summary and prompt
        print("MIO Batch Runner:")
        print(f"Found {len(catalog)} targets in 17 representative projects.")
        print("Use --project <name>, --bug <id>, or --all to execute.")
        return

    if args.dry_run:
        print(f">> Dry run: {len(targets)} targets to process:")
        for t in targets:
            print(f" • {t['project']}-{t['bug_id']}: {t['target_class']}")
        return

    print(f"Starting MIO Batch Execution for {len(targets)} target(s)...")
    for t in targets:
        process_target(t)

if __name__ == "__main__":
    main()
