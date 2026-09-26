#!/usr/bin/env python3
"""Build a provenance-checked, bug-level master benchmark dataset."""

import csv
import json
import os
import statistics
import sys
import tempfile
from collections import Counter

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
sys.path.insert(0, SCRIPTS_DIR)

from run_benchmark import (  # noqa: E402
    TECHNIQUE_NAMES,
    find_test_files_for_target,
    suite_fingerprint,
)

RESULTS_DIR = os.path.join(PROJECT_ROOT, "results")
BENCHMARK_CSV = os.path.join(RESULTS_DIR, "benchmark_results.csv")
CATALOG_JSON = os.path.join(PROJECT_ROOT, "target_benchmark", "all_bugs_catalog.json")
MASTER_SUMMARY_CSV = os.path.join(RESULTS_DIR, "master_benchmark_summary.csv")
MASTER_STATS_JSON = os.path.join(RESULTS_DIR, "master_descriptive_stats.json")
SUITE_INVENTORY_CSV = os.path.join(RESULTS_DIR, "suite_inventory.csv")

TECHNIQUES = ("ipo", "mio", "deepseek", "gemini")
TECHNIQUE_LABELS = {key: TECHNIQUE_NAMES[key] for key in TECHNIQUES}
MEASURED_ATTEMPT_STATUSES = {"DONE", "COMPILE_ERROR", "TIMEOUT"}
UNRESOLVED_STATUSES = {"NOT_RUN", "STALE_RESULT", "CHECKOUT_ERROR", "INVALID_SUITE", "RUN_ERROR"}
RESULT_FIELDS = [
    "Project", "Bug_ID", "Technique", "Target_Classes", "Suite_Available",
    "Test_Files", "Line_Coverage_%", "Branch_Coverage_%",
    "Fault_Detection_Status", "Execution_Status", "Failures_Count",
    "Evaluation_Duration_Sec", "Suite_SHA256", "Run_ID", "Timestamp",
    "Run_Log", "Error_Detail", "Source",
]


def load_catalog():
    with open(CATALOG_JSON, "r", encoding="utf-8") as f:
        catalog = json.load(f)
    if not isinstance(catalog, list) or not catalog:
        raise ValueError("Defects4J catalog is empty or has an unsupported format")
    keys = [(item["project"], int(item["bug_id"])) for item in catalog]
    if len(keys) != len(set(keys)):
        raise ValueError("Defects4J catalog contains duplicate project/bug keys")
    return catalog


def load_latest_measured_rows():
    """Use only runner rows with a run id and a fingerprint of the tested suite."""
    latest = {}
    legacy_rows = 0
    if not os.path.isfile(BENCHMARK_CSV):
        return latest, legacy_rows
    with open(BENCHMARK_CSV, "r", newline="", encoding="utf-8", errors="replace") as f:
        for row in csv.DictReader(f):
            tech = (row.get("Technique") or "").strip()
            execution_status = row.get("Execution_Status", "")
            has_run_id = bool(row.get("Run_ID"))
            has_suite_hash = bool(row.get("Suite_SHA256"))
            if not has_run_id or (not has_suite_hash and execution_status not in {"NO_SUITE", "CHECKOUT_ERROR", "INVALID_SUITE"}):
                legacy_rows += 1
                continue
            if tech not in TECHNIQUE_LABELS.values():
                # Keep historical PICT measurements separate from manifest-backed
                # Native IPO results; the two suite sources are not interchangeable.
                legacy_rows += 1
                continue
            try:
                bug_id = int(row["Bug_ID"])
            except (KeyError, TypeError, ValueError):
                continue
            key = (row["Project"].strip(), bug_id, tech)
            previous = latest.get(key)
            timestamp = row.get("Timestamp", "")
            if previous is None or (timestamp, row.get("Run_ID", "")) >= (
                previous.get("Timestamp", ""), previous.get("Run_ID", "")
            ):
                latest[key] = row
    return latest, legacy_rows


def discover_suites(project, bug_id, classes):
    result = {}
    for tech in TECHNIQUES:
        try:
            paths = find_test_files_for_target(tech, project, bug_id, classes)
            result[tech] = {
                "paths": paths,
                "sha256": suite_fingerprint(paths) if paths else "",
                "error": "",
            }
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            result[tech] = {"paths": [], "sha256": "", "error": str(exc)}
    return result


def as_float(value):
    try:
        if value is None or value == "":
            return ""
        return float(value)
    except (TypeError, ValueError):
        return ""


def as_int(value):
    try:
        return int(value) if value not in (None, "") else ""
    except (TypeError, ValueError):
        return ""


def write_csv_atomic(path, rows, fields):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    fd, temp_path = tempfile.mkstemp(prefix="master_", suffix=".csv", dir=os.path.dirname(path))
    try:
        with os.fdopen(fd, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=fields)
            writer.writeheader()
            writer.writerows(rows)
        os.replace(temp_path, path)
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)


def write_run_log(project, bug_id, technique, measured, master_row):
    """Persist a compact result log with provenance and the detailed outcome."""
    run_id = measured.get("Run_ID", "")
    if not run_id:
        return ""
    log_dir = os.path.join(RESULTS_DIR, "run_logs")
    os.makedirs(log_dir, exist_ok=True)
    safe_name = "".join(ch if ch.isalnum() or ch in "-_." else "_" for ch in run_id)
    log_path = os.path.join(log_dir, f"{safe_name}.json")
    detail = {
        "project": project,
        "bug_id": int(bug_id),
        "technique": technique,
        "run_id": run_id,
        "suite_sha256": measured.get("Suite_SHA256", ""),
        "test_files": master_row.get("Test_Files", ""),
        "target_classes": master_row.get("Target_Classes", ""),
        "timestamp": measured.get("Timestamp", ""),
        "evaluation_duration_sec": as_float(measured.get("Evaluation_Duration_Sec")),
        "execution_status": measured.get("Execution_Status", ""),
        "fault_detection_status": measured.get("Fault_Detection_Status", ""),
        "line_coverage_percent": as_float(measured.get("Line_Coverage_%")),
        "branch_coverage_percent": as_float(measured.get("Branch_Coverage_%")),
        "failures_count": as_int(measured.get("Failures_Count")),
        "error_detail": measured.get("Error_Detail", ""),
    }
    # The runner writes detailed buggy/fixed failing-test lists for successful runs.
    technique_key = next((key for key, label in TECHNIQUE_LABELS.items() if label == technique), technique)
    individual_json = os.path.join(RESULTS_DIR, project, str(bug_id), f"{technique_key}.json")
    if os.path.isfile(individual_json):
        try:
            with open(individual_json, "r", encoding="utf-8") as stream:
                generated = json.load(stream)
            if generated.get("run_id") == run_id:
                detail["buggy_failures"] = generated.get("buggy_failures", [])
                detail["fixed_failures"] = generated.get("fixed_failures", [])
        except (OSError, json.JSONDecodeError):
            pass
    fd, temp_path = tempfile.mkstemp(prefix="run_log_", suffix=".json", dir=log_dir)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as stream:
            json.dump(detail, stream, indent=2, ensure_ascii=False)
        os.replace(temp_path, log_path)
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)
    return os.path.relpath(log_path, PROJECT_ROOT)


def mean_sd(values):
    if not values:
        return {"mean": None, "sd": None}
    return {
        "mean": round(statistics.mean(values), 2),
        "sd": round(statistics.stdev(values), 2) if len(values) > 1 else 0.0,
    }


def consolidate():
    catalog = load_catalog()
    latest, legacy_rows = load_latest_measured_rows()
    results = []
    inventory = []

    for item in catalog:
        project = item["project"]
        bug_id = int(item["bug_id"])
        classes = item.get("target_classes") or item.get("modified_classes") or []
        suites = discover_suites(project, bug_id, classes)

        for tech in TECHNIQUES:
            suite = suites[tech]
            label = TECHNIQUE_LABELS[tech]
            key = (project, bug_id, label)
            measured = latest.get(key)
            suite_available = bool(suite["paths"])

            if suite["error"]:
                execution_status = "INVALID_SUITE"
                fault_status = "NOT_EVALUATED"
                error_detail = suite["error"]
            elif (
                measured
                and suite_available
                and measured.get("Suite_SHA256") == suite["sha256"]
            ):
                execution_status = measured.get("Execution_Status", "")
                fault_status = measured.get("Fault_Detection_Status", "NOT_EVALUATED")
                error_detail = measured.get("Error_Detail", "")
            elif measured and (
                measured.get("Execution_Status") in {"CHECKOUT_ERROR", "INVALID_SUITE"}
                or (measured.get("Execution_Status") == "NO_SUITE" and not suite_available)
            ):
                # These states are meaningful even without a suite hash; keep them
                # visible while withholding benchmark metrics.
                execution_status = measured.get("Execution_Status")
                fault_status = "NOT_EVALUATED"
                error_detail = measured.get("Error_Detail", "")
            elif measured:
                execution_status = "STALE_RESULT"
                fault_status = "NOT_EVALUATED"
                error_detail = "Saved measurement does not match the current suite fingerprint"
            elif suite_available:
                execution_status = "NOT_RUN"
                fault_status = "NOT_EVALUATED"
                error_detail = "A test suite exists but has no provenance-checked benchmark result"
            else:
                execution_status = "NO_SUITE"
                fault_status = "NOT_EVALUATED"
                error_detail = "No matching test suite is available"

            row = {
                "Project": project,
                "Bug_ID": bug_id,
                "Technique": label,
                "Target_Classes": ";".join(classes),
                "Suite_Available": "YES" if suite_available else "NO",
                "Test_Files": ";".join(os.path.basename(p) for p in suite["paths"]),
                "Line_Coverage_%": "",
                "Branch_Coverage_%": "",
                "Fault_Detection_Status": fault_status,
                "Execution_Status": execution_status,
                "Failures_Count": "",
                "Evaluation_Duration_Sec": "",
                "Suite_SHA256": suite["sha256"],
                "Run_ID": "",
                "Timestamp": "",
                "Run_Log": "",
                "Error_Detail": error_detail,
                "Source": "",
            }
            if measured and execution_status == measured.get("Execution_Status"):
                row.update({
                    "Failures_Count": as_int(measured.get("Failures_Count")),
                    "Evaluation_Duration_Sec": as_float(measured.get("Evaluation_Duration_Sec")),
                    "Run_ID": measured.get("Run_ID", ""),
                    "Timestamp": measured.get("Timestamp", ""),
                    "Run_Log": write_run_log(project, bug_id, label, measured, row),
                    "Source": "benchmark_results.csv",
                })
                # Coverage is a completed measurement only. In particular, the
                # runner's legacy 0 placeholders on compile failures are not data.
                if execution_status == "DONE":
                    row.update({
                        "Line_Coverage_%": as_float(measured.get("Line_Coverage_%")),
                        "Branch_Coverage_%": as_float(measured.get("Branch_Coverage_%")),
                    })
            results.append(row)
            inventory.append({
                "Project": project,
                "Bug_ID": bug_id,
                "Technique": label,
                "Target_Classes": ";".join(classes),
                "Suite_Available": row["Suite_Available"],
                "Test_Files": row["Test_Files"],
                "Suite_SHA256": suite["sha256"],
                "Inventory_Status": "INVALID_SUITE" if suite["error"] else ("READY" if suite_available else "NO_SUITE"),
                "Error_Detail": suite["error"],
            })

    expected_rows = len(catalog) * len(TECHNIQUES)
    if len(results) != expected_rows:
        raise AssertionError(f"Expected {expected_rows} bug/technique rows, got {len(results)}")
    keys = [(r["Project"], r["Bug_ID"], r["Technique"]) for r in results]
    if len(keys) != len(set(keys)):
        raise AssertionError("Master output contains duplicate bug/technique keys")

    write_csv_atomic(MASTER_SUMMARY_CSV, results, RESULT_FIELDS)
    write_csv_atomic(SUITE_INVENTORY_CSV, inventory, [
        "Project", "Bug_ID", "Technique", "Target_Classes", "Suite_Available",
        "Test_Files", "Suite_SHA256", "Inventory_Status", "Error_Detail",
    ])

    stats_by_technique = {}
    for label in TECHNIQUE_LABELS.values():
        group = [r for r in results if r["Technique"] == label]
        attempted = [r for r in group if r["Execution_Status"] in MEASURED_ATTEMPT_STATUSES]
        covered = [r for r in group if r["Execution_Status"] == "DONE"]
        detected = sum(r["Fault_Detection_Status"] == "BUG_DETECTED" for r in attempted)
        line_values = [float(r["Line_Coverage_%"]) for r in covered if r["Line_Coverage_%"] != ""]
        branch_values = [float(r["Branch_Coverage_%"]) for r in covered if r["Branch_Coverage_%"] != ""]
        distribution = Counter(r["Fault_Detection_Status"] for r in attempted)
        stats_by_technique[label] = {
            "catalog_bugs": len(catalog),
            "suite_available_bugs": sum(r["Suite_Available"] == "YES" for r in group),
            "attempted_bugs": len(attempted),
            "successful_benchmark_bugs": len(covered),
            "coverage_valid_bugs": min(len(line_values), len(branch_values)),
            "detected_bugs": detected,
            "fdr_evaluated_percent": round(detected / len(attempted) * 100, 2) if attempted else None,
            "fdr_full_catalog_percent": round(detected / len(catalog) * 100, 2) if catalog else None,
            "line_coverage": mean_sd(line_values),
            "branch_coverage": mean_sd(branch_values),
            "status_distribution": dict(distribution),
            "suite_inventory_distribution": dict(Counter(r["Execution_Status"] for r in group)),
        }

    final_stats = {
        "dataset": {
            "catalog_bugs": len(catalog),
            "projects": len({item["project"] for item in catalog}),
            "expected_bug_technique_rows": expected_rows,
            "legacy_or_unsupported_rows_excluded": legacy_rows,
            "results_are_complete": not any(row["Execution_Status"] in UNRESOLVED_STATUSES for row in results),
            "available_suite_evaluations_complete": all(
                row["Execution_Status"] in MEASURED_ATTEMPT_STATUSES
                for row in results if row["Suite_Available"] == "YES"
            ),
            "rows_with_no_suite": sum(row["Execution_Status"] == "NO_SUITE" for row in results),
            "unresolved_rows": sum(row["Execution_Status"] in UNRESOLVED_STATUSES for row in results),
        },
        "techniques": stats_by_technique,
    }
    with open(MASTER_STATS_JSON, "w", encoding="utf-8") as f:
        json.dump(final_stats, f, indent=2, ensure_ascii=False)

    print(f"Master dataset: {len(results)} bug/technique rows ({len(catalog)} bugs x {len(TECHNIQUES)} techniques)")
    print(f"Legacy or unsupported benchmark rows excluded: {legacy_rows}")
    for label, stats in stats_by_technique.items():
        print(
            f"{label}: suites={stats['suite_available_bugs']}, attempted={stats['attempted_bugs']}, "
            f"detected={stats['detected_bugs']}, evaluated FDR={stats['fdr_evaluated_percent']}%"
        )
    print(f"Wrote {MASTER_SUMMARY_CSV}, {SUITE_INVENTORY_CSV}, and {MASTER_STATS_JSON}")


if __name__ == "__main__":
    consolidate()
