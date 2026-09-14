#!/usr/bin/env python3
"""
Defects4J Complete All-Bugs Catalog Extractor (All 835+ Active Bugs across 17 Projects)
Designed by Member 4 (Infrastructure & Data Lead) for ProjectSQA

Extracts metadata, modified classes, triggering tests, and issue tracker IDs
for ALL active bugs in Defects4J, enabling the team to target, test, and benchmark
all classes and bugs across all 17 projects.
"""

import os
import sys
import json
import csv
import time
import argparse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Dict, Any, Optional

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "target_benchmark")
CATALOG_JSON = os.path.join(OUTPUT_DIR, "all_bugs_catalog.json")
CATALOG_MD = os.path.join(OUTPUT_DIR, "all_bugs_catalog.md")

ALL_PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections",
    "Compress", "Csv", "Gson", "JacksonCore", "JacksonDatabind",
    "JacksonXml", "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
]

D4J_LOCAL_PATH = "/opt/defects4j/framework/projects"
RAW_BASE_URL = "https://raw.githubusercontent.com/rjust/defects4j/master/framework/projects"

def fetch_url_text(url: str, timeout: int = 15) -> str:
    """Fetch text from raw URL with timeout and User-Agent."""
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "ProjectSQA-Extractor"})
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.read().decode("utf-8", errors="replace")
    except Exception:
        return ""

def get_project_active_bugs(project: str) -> List[Dict[str, Any]]:
    """Get list of active bug records for a project."""
    # 1. Try local Defects4J framework folder if in container
    local_csv = os.path.join(D4J_LOCAL_PATH, project, "active-bugs.csv")
    if os.path.exists(local_csv):
        with open(local_csv, "r", encoding="utf-8", errors="replace") as f:
            reader = csv.DictReader(f)
            return list(reader)
            
    # 2. Fetch from GitHub raw
    url = f"{RAW_BASE_URL}/{project}/active-bugs.csv"
    text = fetch_url_text(url)
    if text:
        reader = csv.DictReader(text.splitlines())
        return list(reader)
    return []

def get_bug_details(project: str, bug_id: str, row: Dict[str, Any]) -> Dict[str, Any]:
    """Extract modified classes and trigger tests for a specific bug."""
    modified_classes = []
    trigger_tests = []
    
    # 1. Local path check
    local_mod = os.path.join(D4J_LOCAL_PATH, project, "modified_classes", f"{bug_id}.src")
    local_trig = os.path.join(D4J_LOCAL_PATH, project, "trigger_tests", str(bug_id))
    
    if os.path.exists(local_mod):
        with open(local_mod, "r", encoding="utf-8", errors="replace") as f:
            modified_classes = [line.strip() for line in f if line.strip()]
    else:
        mod_url = f"{RAW_BASE_URL}/{project}/modified_classes/{bug_id}.src"
        mod_text = fetch_url_text(mod_url)
        if mod_text:
            modified_classes = [line.strip() for line in mod_text.splitlines() if line.strip()]
            
    if os.path.exists(local_trig):
        with open(local_trig, "r", encoding="utf-8", errors="replace") as f:
            trigger_tests = [line.strip() for line in f if line.strip()]
    else:
        trig_url = f"{RAW_BASE_URL}/{project}/trigger_tests/{bug_id}"
        trig_text = fetch_url_text(trig_url)
        if trig_text:
            trigger_tests = [line.strip() for line in trig_text.splitlines() if line.strip()]

    # Extract simple class names
    simple_names = [cls.split(".")[-1] for cls in modified_classes]

    return {
        "project": project,
        "bug_id": int(bug_id),
        "target_classes": modified_classes,
        "simple_names": simple_names,
        "trigger_tests": trigger_tests,
        "report_id": row.get("report.id", "N/A"),
        "report_url": row.get("report.url", "N/A"),
        "revision_buggy": row.get("revision.id.buggy", "N/A"),
        "revision_fixed": row.get("revision.id.fixed", "N/A")
    }

def extract_all_bugs_catalog(projects: Optional[List[str]] = None, max_workers: int = 16) -> List[Dict[str, Any]]:
    """Extract catalog entries for all active bugs across specified projects."""
    target_projects = projects if projects else ALL_PROJECTS
    print(f"🚀 Extracting Complete All-Bugs Catalog for {len(target_projects)} Projects...")
    
    all_tasks = []
    catalog = []
    
    # Collect all (project, bug_id, row) tasks
    for p in target_projects:
        bugs = get_project_active_bugs(p)
        print(f"  • {p:<16}: {len(bugs):>3} active bugs found")
        for b in bugs:
            bid = b.get("bug.id")
            if bid:
                all_tasks.append((p, bid, b))
                
    total_expected = len(all_tasks)
    print(f"\n📦 Fetching detailed metadata for {total_expected} bugs using {max_workers} concurrent workers...")
    
    t0 = time.time()
    completed = 0
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_to_bug = {
            executor.submit(get_bug_details, p, bid, row): (p, bid)
            for p, bid, row in all_tasks
        }
        for future in as_completed(future_to_bug):
            p, bid = future_to_bug[future]
            try:
                res = future.result()
                catalog.append(res)
                completed += 1
                if completed % 100 == 0 or completed == total_expected:
                    print(f"   [{completed}/{total_expected}] Processed ({int(completed/total_expected*100)}%)...")
            except Exception as e:
                print(f"   [!] Error processing {p}-{bid}: {e}")
                
    catalog.sort(key=lambda x: (x["project"], x["bug_id"]))
    elapsed = round(time.time() - t0, 2)
    print(f"\n✅ All-Bugs metadata extracted in {elapsed}s! Total entries: {len(catalog)}")
    return catalog

def save_catalog_files(catalog: List[Dict[str, Any]]):
    """Save catalog to JSON and Markdown format."""
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # 1. JSON Catalog
    with open(CATALOG_JSON, "w", encoding="utf-8") as f:
        json.dump(catalog, f, indent=2, ensure_ascii=False)
    print(f"📄 JSON Catalog saved: {CATALOG_JSON}")
    
    # 2. Markdown Catalog
    with open(CATALOG_MD, "w", encoding="utf-8") as f:
        f.write("# 📚 Defects4J All-Bugs & All-Classes Master Catalog\n\n")
        f.write(f"**Total Active Bugs:** {len(catalog)} Bugs across 17 Projects  \n")
        f.write(f"**Dataset Scope:** Full Defects4J Dataset (All Modified Classes Under Test)  \n")
        f.write(f"**Last Updated:** {time.strftime('%Y-%m-%d %H:%M:%S')}  \n\n")
        f.write("---\n\n")
        f.write("| # | Project | Bug ID | Target Modified Classes | Primary Trigger Test | Report ID |\n")
        f.write("| :-: | :--- | :-: | :--- | :--- | :--- |\n")
        for i, item in enumerate(catalog, 1):
            classes_str = "<br>".join([f"`{c}`" for c in item["target_classes"]]) if item["target_classes"] else "-"
            trig_str = f"`{item['trigger_tests'][0]}`" if item["trigger_tests"] else "-"
            f.write(f"| {i} | **{item['project']}** | `{item['bug_id']}b` | {classes_str} | {trig_str} | {item['report_id']} |\n")
    print(f"📄 Markdown Catalog saved: {CATALOG_MD}")

def main():
    parser = argparse.ArgumentParser(description="Defects4J Complete All-Bugs Catalog Extractor")
    parser.add_argument("--project", type=str, default=None, help="Specific project (e.g. Lang, Math)")
    parser.add_argument("--workers", type=int, default=16, help="Concurrent workers")
    args = parser.parse_args()

    target_projects = [args.project] if args.project else None
    catalog = extract_all_bugs_catalog(projects=target_projects, max_workers=args.workers)
    save_catalog_files(catalog)

if __name__ == "__main__":
    main()

