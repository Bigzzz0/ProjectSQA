#!/usr/bin/env python3
"""Reclassify completed runs from their recorded buggy/fixed failure lists.

Use --apply to correct historical DONE rows where the fixed version failed.
The default is a read-only preview. Rebuild the master dataset after applying.
"""

import argparse
import csv
import json
import os
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MASTER = ROOT / "results" / "master_benchmark_summary.csv"
RAW = ROOT / "results" / "benchmark_results.csv"
PROGRESS = ROOT / "progress.json"
TECHNIQUE_KEYS = {
    "IPO (Native IPO)": "ipo",
    "MIO (EvoSuite SBST)": "mio",
    "DeepSeek V4 Flash": "deepseek",
    "Gemini 3.8 Flash": "gemini",
}


def classify(buggy_failures, fixed_failures):
    if fixed_failures:
        return "FLAKY_OR_REGRESSION"
    if buggy_failures:
        return "BUG_DETECTED"
    return "NOT_DETECTED"


def read_csv(path):
    with path.open("r", newline="", encoding="utf-8-sig") as stream:
        reader = csv.DictReader(stream)
        return reader.fieldnames, list(reader)


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, temp_name = tempfile.mkstemp(prefix=path.stem + "_", suffix=".json", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            json.dump(value, stream, indent=2, ensure_ascii=False)
            stream.write("\n")
        os.replace(temp_name, path)
    finally:
        if os.path.exists(temp_name):
            os.remove(temp_name)


def atomic_csv(path, fieldnames, rows):
    fd, temp_name = tempfile.mkstemp(prefix=path.stem + "_", suffix=".csv", dir=path.parent)
    try:
        with os.fdopen(fd, "w", newline="", encoding="utf-8") as stream:
            writer = csv.DictWriter(stream, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(rows)
        os.replace(temp_name, path)
    finally:
        if os.path.exists(temp_name):
            os.remove(temp_name)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="write corrected statuses")
    args = parser.parse_args()

    _, master_rows = read_csv(MASTER)
    corrections = []
    for row in master_rows:
        if row.get("Execution_Status") != "DONE":
            continue
        run_log = ROOT / row["Run_Log"].replace("\\", os.sep)
        if not run_log.is_file():
            raise FileNotFoundError(f"Run log missing for {row['Run_ID']}: {row['Run_Log']}")
        with run_log.open("r", encoding="utf-8") as stream:
            detail = json.load(stream)
        if detail.get("run_id") != row["Run_ID"] or detail.get("suite_sha256") != row["Suite_SHA256"]:
            raise ValueError(f"Run provenance mismatch for {row['Run_ID']}")
        if "buggy_failures" not in detail or "fixed_failures" not in detail:
            raise ValueError(f"Failure evidence is incomplete for {row['Run_ID']}")
        expected = classify(detail["buggy_failures"], detail["fixed_failures"])
        if expected != row["Fault_Detection_Status"]:
            corrections.append((row, detail, run_log, expected))

    if not corrections:
        print("All completed runs already match their buggy/fixed failure evidence.")
        return

    print(f"Statuses to correct: {len(corrections)}")
    for row, _, _, expected in corrections:
        print(f"{row['Project']}-{row['Bug_ID']} | {row['Technique']} | {row['Fault_Detection_Status']} -> {expected}")
    if not args.apply:
        print("Preview only; rerun with --apply to update the raw CSV, logs, per-bug JSON, and progress state.")
        return

    raw_fields, raw_rows = read_csv(RAW)
    corrections_by_run = {row["Run_ID"]: (row, detail, log_path, expected)
                          for row, detail, log_path, expected in corrections}
    matched_raw = set()
    for raw in raw_rows:
        change = corrections_by_run.get(raw.get("Run_ID", ""))
        if change:
            row, _, _, expected = change
            if (raw["Project"], str(int(raw["Bug_ID"])), raw["Technique"]) != (
                row["Project"], str(int(row["Bug_ID"])), row["Technique"]
            ):
                raise ValueError(f"Raw CSV key mismatch for {row['Run_ID']}")
            raw["Fault_Detection_Status"] = expected
            matched_raw.add(row["Run_ID"])
    missing_raw = set(corrections_by_run) - matched_raw
    if missing_raw:
        raise ValueError(f"Corrected run IDs missing from raw benchmark CSV: {sorted(missing_raw)}")

    json_updates = []
    for row, detail, log_path, expected in corrections:
        detail["fault_detection_status"] = expected
        technique_key = TECHNIQUE_KEYS[row["Technique"]]
        result_path = ROOT / "results" / row["Project"] / str(int(row["Bug_ID"])) / f"{technique_key}.json"
        with result_path.open("r", encoding="utf-8") as stream:
            result = json.load(stream)
        if result.get("run_id") != row["Run_ID"]:
            raise ValueError(f"Per-bug JSON run ID mismatch for {row['Run_ID']}")
        result["fault_detected"] = expected
        json_updates.append((log_path, detail))
        json_updates.append((result_path, result))

    progress = json.loads(PROGRESS.read_text(encoding="utf-8"))
    corrected_by_run = {row["Run_ID"]: expected for row, _, _, expected in corrections}
    for value in progress.values():
        if isinstance(value, dict) and value.get("run_id") in corrected_by_run:
            value["fault_detected"] = corrected_by_run[value["run_id"]]

    for path, value in json_updates:
        atomic_json(path, value)
    atomic_csv(RAW, raw_fields, raw_rows)
    atomic_json(PROGRESS, progress)
    print(f"Applied corrections to {len(corrections)} completed runs.")


if __name__ == "__main__":
    main()
