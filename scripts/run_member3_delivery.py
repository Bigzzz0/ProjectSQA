#!/usr/bin/env python3
"""Run the unique Member 3 delivery keys through the central benchmark runner."""

import argparse
import csv
import os
import subprocess
import sys
from collections import Counter
from pathlib import Path

from run_benchmark import PROJECT_ROOT, TECHNIQUE_NAMES, test_file_matches_targets


RESULTS = Path(PROJECT_ROOT) / "results"
DELIVERY = RESULTS / "delivery_report_m3.csv"
CATALOG = Path(PROJECT_ROOT) / "target_benchmark" / "all_bugs_catalog.json"
BENCHMARK = RESULTS / "benchmark_results.csv"
LOG_DIR = RESULTS / "member3_delivery_logs"
TECHNIQUES = {
    "DeepSeek V4 Flash": "deepseek",
    "Gemini 3.8 Flash": "gemini",
}


def stable_delivery_sha(path):
    """Match the Windows CRLF-normalized hashes recorded in the handoff CSV."""
    content = path.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
    return __import__("hashlib").sha256(content.replace(b"\n", b"\r\n")).hexdigest()


def load_queue():
    import json

    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    targets = {
        (item["project"], int(item["bug_id"])):
        item.get("target_classes") or item.get("modified_classes") or []
        for item in catalog
    }
    queue = {}
    records = list(csv.DictReader(DELIVERY.open(newline="", encoding="utf-8-sig")))
    for row in records:
        project, separator, bug_text = row["bug"].rpartition("-")
        if not separator or not bug_text.isdigit():
            raise ValueError(f"Invalid delivery bug key: {row['bug']!r}")
        bug_id = int(bug_text)
        technique = TECHNIQUES.get(row["tech"])
        if not technique:
            raise ValueError(f"Unexpected delivery technique: {row['tech']!r}")
        key = (project, bug_id, technique)
        classes = targets.get((project, bug_id), [])
        if row["target_cls"] not in classes:
            raise ValueError(f"Target is absent from the catalog for {project}-{bug_id}: {row['target_cls']}")
        rel_path = Path(row["file_rel"])
        path = (Path(PROJECT_ROOT) / rel_path).resolve()
        if os.path.commonpath([str(Path(PROJECT_ROOT).resolve()), str(path)]) != str(Path(PROJECT_ROOT).resolve()):
            raise ValueError(f"Delivery path escapes the repository: {row['file_rel']}")
        if not path.is_file():
            raise ValueError(f"Delivered suite is missing: {row['file_rel']}")
        if stable_delivery_sha(path) != row["sha256"]:
            raise ValueError(f"Delivered suite hash mismatch: {row['file_rel']}")
        if not test_file_matches_targets(str(path), classes):
            raise ValueError(f"Suite does not match its catalog target: {row['file_rel']}")
        queue.setdefault(key, []).append(path)

    if len(records) != 31 or len(queue) != 29:
        raise ValueError(f"Expected 31 delivered files for 29 benchmark keys; found {len(records)} files and {len(queue)} keys")
    if sum(1 for key in queue if key[2] == "deepseek") != 17:
        raise ValueError("Expected 17 unique DeepSeek benchmark keys")
    if sum(1 for key in queue if key[2] == "gemini") != 12:
        raise ValueError("Expected 12 unique Gemini benchmark keys")
    return records, sorted(queue)


def latest_outcome(key):
    project, bug_id, technique = key
    if not BENCHMARK.is_file():
        return {}
    latest = {}
    with BENCHMARK.open(newline="", encoding="utf-8-sig", errors="replace") as stream:
        for row in csv.DictReader(stream):
            if (row.get("Project"), row.get("Bug_ID"), row.get("Technique")) == (
                project, str(bug_id), TECHNIQUE_NAMES[technique]
            ):
                latest = row
    return latest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true", help="Validate handoff and list keys without evaluating")
    args = parser.parse_args()
    _, queue = load_queue()
    print(f"Validated 31 delivery files for {len(queue)} unique benchmark keys.", flush=True)
    if args.dry_run:
        for project, bug_id, technique in queue:
            print(f"{project}-{bug_id} {technique}")
        return 0

    LOG_DIR.mkdir(parents=True, exist_ok=True)
    failures = 0
    for index, key in enumerate(queue, start=1):
        project, bug_id, technique = key
        label = f"{project}-{bug_id}-{technique}"
        command = [
            sys.executable,
            str(Path(PROJECT_ROOT) / "scripts" / "run_benchmark.py"),
            "--project", project,
            "--bug", str(bug_id),
            "--techniques", technique,
            "--resume",
        ]
        log_path = LOG_DIR / f"{label}.log"
        print(f"[{index}/{len(queue)}] {label}", flush=True)
        with log_path.open("w", encoding="utf-8") as log:
            log.write("Command: " + " ".join(command) + "\n")
            process = subprocess.Popen(
                command,
                cwd=PROJECT_ROOT,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                encoding="utf-8",
                errors="replace",
                bufsize=1,
            )
            assert process.stdout is not None
            for line in process.stdout:
                print(line, end="", flush=True)
                log.write(line)
            return_code = process.wait()
            outcome = latest_outcome(key)
            status = outcome.get("Execution_Status", "NO_RESULT")
            log.write(f"\nProcess_Exit_Code: {return_code}\nExecution_Status: {status}\n")
            if outcome.get("Run_ID"):
                log.write(f"Run_ID: {outcome['Run_ID']}\nSuite_SHA256: {outcome.get('Suite_SHA256', '')}\n")
            if outcome.get("Error_Detail"):
                log.write("Error_Detail:\n" + outcome["Error_Detail"] + "\n")
        if return_code != 0 or status in {"RUN_ERROR", "CHECKOUT_ERROR", "INVALID_SUITE", "STALE_RESULT", "NOT_RUN"}:
            failures += 1
        print(f"[{index}/{len(queue)}] outcome={status}", flush=True)

    outcomes = Counter()
    for key in queue:
        outcomes[latest_outcome(key).get("Execution_Status", "NO_RESULT")] += 1
    print("Final targeted outcomes:")
    for status, count in sorted(outcomes.items()):
        print(f"{status}: {count}")
    print(f"Per-key logs: {LOG_DIR}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
