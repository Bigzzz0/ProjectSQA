#!/usr/bin/env python3
"""
Batch Bug Metadata & Catalog Extractor for Defects4J
Designed by Member 4 (Infra & Data Lead) for ProjectSQA

Extracts metadata, modified classes, triggering tests, and root cause errors
for ALL active bugs in Defects4J, enabling the entire team to inspect, target,
and trigger defects for all 856 active bugs across 17 projects.
"""

import os
import sys
import json
import argparse
import subprocess
from typing import List, Dict, Any

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "target_benchmark")

sys.path.insert(0, SCRIPTS_DIR)
import d4j_meta

def get_bug_info_summary(project: str, bug_id: int) -> Dict[str, Any]:
    """Extract ground truth info for a specific bug via defects4j info."""
    code, out, err = d4j_meta.run_cmd(["defects4j", "info", "-p", project, "-b", str(bug_id)], timeout=60)
    
    modified_classes = []
    trigger_tests = []
    root_cause = "N/A"
    bug_report_id = "N/A"
    
    lines = out.splitlines()
    in_root_cause = False
    in_modified = False
    
    for line in lines:
        line_str = line.strip()
        if line_str.startswith("Bug report id:"):
            pass
        elif "Root cause in triggering tests:" in line_str:
            in_root_cause = True
            in_modified = False
            continue
        elif "List of modified sources:" in line_str:
            in_root_cause = False
            in_modified = True
            continue
        elif line_str.startswith("---"):
            in_root_cause = False
            in_modified = False
            continue
            
        if in_root_cause and line_str:
            if root_cause == "N/A":
                root_cause = line_str
            else:
                root_cause += " " + line_str
        elif in_modified and line_str.startswith("-"):
            cls_name = line_str[1:].strip()
            if cls_name:
                modified_classes.append(cls_name)
                
    return {
        "project": project,
        "bug_id": bug_id,
        "modified_classes": modified_classes,
        "root_cause": root_cause,
        "raw_info": out
    }

def main():
    parser = argparse.ArgumentParser(description="Defects4J All-Bugs Catalog & Batch Extractor")
    parser.add_argument("--project", type=str, default=None, help="Target project (e.g. Lang, Math) or all")
    parser.add_argument("--list-summary", action="store_true", help="List bug count across all 17 projects")
    parser.add_argument("--catalog", action="store_true", help="Generate all-bugs catalog JSON/MD")
    parser.add_argument("--max-per-project", type=int, default=None, help="Max bugs to index per project")
    parser.add_argument("--extract-code", action="store_true", help="Extract Java sources to target_benchmark/")
    args = parser.parse_args()

    all_projects = d4j_meta.get_all_projects()
    
    if args.list_summary:
        print("=" * 65)
        print("📊 Defects4J Projects & Active Bugs Overview")
        print("=" * 65)
        total_bugs = 0
        for p in all_projects:
            bids = d4j_meta.get_active_bugs(p)
            count = len(bids)
            total_bugs += count
            print(f" • {p:<18}: {count:>3} bugs (Bugs: {min(bids) if bids else 0} to {max(bids) if bids else 0})")
        print("=" * 65)
        print(f"🌟 Total Active Bugs in Defects4J: {total_bugs} Bugs across {len(all_projects)} Projects")
        print("=" * 65)
        return

    projects_to_process = [args.project] if args.project else all_projects
    catalog = []
    
    print(f"🚀 Starting bug extraction for projects: {projects_to_process}")
    for p in projects_to_process:
        bids = d4j_meta.get_active_bugs(p)
        if args.max_per_project:
            bids = bids[:args.max_per_project]
        print(f"\n>> Processing {p} ({len(bids)} bugs)...")
        for bid in bids:
            info = get_bug_info_summary(p, bid)
            catalog.append(info)
            print(f"   [{p}-{bid}] Modified: {info['modified_classes']} | Cause: {info['root_cause'][:50]}...")
            
            if args.extract_code:
                out_bug_dir = os.path.join(OUTPUT_DIR, f"{p}_{bid}b")
                os.makedirs(out_bug_dir, exist_ok=True)
                with open(os.path.join(out_bug_dir, "defects4j_info.txt"), "w", encoding="utf-8") as f:
                    f.write(info["raw_info"])
                    
    # Save catalog
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    json_path = os.path.join(OUTPUT_DIR, "all_bugs_catalog.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(catalog, f, indent=2, ensure_ascii=False)
    print(f"\n✅ Catalog saved to: {json_path} (Total indexed: {len(catalog)} bugs)")

if __name__ == "__main__":
    main()
