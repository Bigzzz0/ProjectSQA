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
import hashlib
import tempfile
from functools import lru_cache
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
IPO_MANIFEST = os.path.join(PROJECT_ROOT, "Combinatorial_IPO", "Results", "verified_suites_manifest.json")

CSV_FIELDS = [
    "Project", "Bug_ID", "Technique", "Target_Classes", "Test_Files",
    "Line_Coverage_%", "Branch_Coverage_%", "Fault_Detection_Status",
    "Failures_Count", "Execution_Status", "Timestamp", "Suite_SHA256",
    "Run_ID", "Evaluation_Duration_Sec", "Error_Detail"
]

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
    "deepseek": os.path.join(PROJECT_ROOT, "Deepseek-v4_flash", "TestCode"),
    "claude": os.path.join(PROJECT_ROOT, "Deepseek-v4_flash", "TestCode"),
    "gemini": os.path.join(PROJECT_ROOT, "Gemini-3_8_flash", "TestCode")
}

TECHNIQUE_NAMES = {
    "ipo": "IPO (Native IPO)",
    "mio": "MIO (EvoSuite SBST)",
    "deepseek": "DeepSeek V4 Flash",
    "claude": "DeepSeek V4 Flash",
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
    fd, temp_path = tempfile.mkstemp(prefix="progress_", suffix=".json", dir=os.path.dirname(PROGRESS_FILE))
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as f:
            json.dump(progress, f, indent=2, ensure_ascii=False)
            f.flush()
            os.fsync(f.fileno())
        os.replace(temp_path, PROGRESS_FILE)
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

def init_csv(csv_path: str):
    os.makedirs(os.path.dirname(csv_path), exist_ok=True)
    if not os.path.exists(csv_path):
        with open(csv_path, "w", newline="", encoding="utf-8") as f:
            csv.DictWriter(f, fieldnames=CSV_FIELDS).writeheader()

def append_csv_result(
    csv_path: str,
    row: List[Any],
    suite_sha256: str = "",
    run_id: str = "",
    duration_sec: Optional[float] = None,
    error_detail: str = "",
):
    """Upsert one measured outcome per project/bug/technique; preserve older columns."""
    os.makedirs(os.path.dirname(csv_path), exist_ok=True)
    if isinstance(row, dict):
        new_row = {field: row.get(field, "") for field in CSV_FIELDS}
    else:
        new_row = dict(zip(CSV_FIELDS[:11], row))
        new_row.update({
            "Suite_SHA256": suite_sha256,
            "Run_ID": run_id,
            "Evaluation_Duration_Sec": "" if duration_sec is None else round(duration_sec, 3),
            "Error_Detail": error_detail,
        })

    prior_rows = {}
    if os.path.exists(csv_path):
        with open(csv_path, "r", newline="", encoding="utf-8", errors="replace") as f:
            for old in csv.DictReader(f):
                migrated = {field: old.get(field, "") for field in CSV_FIELDS}
                key = (migrated["Project"], migrated["Bug_ID"], migrated["Technique"])
                if not all(key):
                    continue
                previous = prior_rows.get(key)
                # CSV is append ordered historically; timestamps break ties after migration.
                if previous is None or migrated["Timestamp"] >= previous["Timestamp"]:
                    prior_rows[key] = migrated

    key = (str(new_row.get("Project", "")), str(new_row.get("Bug_ID", "")), str(new_row.get("Technique", "")))
    prior_rows[key] = {field: "" if new_row.get(field) is None else new_row.get(field, "") for field in CSV_FIELDS}
    fd, temp_path = tempfile.mkstemp(prefix="benchmark_", suffix=".csv", dir=os.path.dirname(os.path.abspath(csv_path)))
    try:
        with os.fdopen(fd, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=CSV_FIELDS)
            writer.writeheader()
            for item in sorted(prior_rows.values(), key=lambda r: (r["Project"], int(r["Bug_ID"] or 0), r["Technique"])):
                writer.writerow(item)
        os.replace(temp_path, csv_path)
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)


def sha256_file(path: str) -> str:
    # The checked-in manifest records Git's LF-normalized source digest;
    # Windows checkout may expand those line endings to CRLF.
    with open(path, "rb") as f:
        contents = f.read().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
    return hashlib.sha256(contents).hexdigest()


def suite_fingerprint(test_files: List[str]) -> str:
    digest = hashlib.sha256()
    for path in sorted(test_files):
        digest.update(os.path.basename(path).encode("utf-8"))
        digest.update(b"\0")
        with open(path, "rb") as f:
            for chunk in iter(lambda: f.read(1024 * 1024), b""):
                digest.update(chunk)
        scaffold = path.replace(".java", "_scaffolding.java")
        if os.path.exists(scaffold):
            digest.update(os.path.basename(scaffold).encode("utf-8"))
            with open(scaffold, "rb") as f:
                for chunk in iter(lambda: f.read(1024 * 1024), b""):
                    digest.update(chunk)
    return digest.hexdigest()


def progress_result_is_current(
    result: Dict[str, Any], project: Optional[str] = None,
    bug_id: Optional[int] = None, technique: Optional[str] = None,
) -> bool:
    """A saved outcome can be reused only when its measured suite is unchanged."""
    status = result.get("status")
    if status == "NO_SUITE" and project and bug_id is not None and technique:
        classes = load_catalog_targets().get((project, int(bug_id)), [])
        if not classes:
            return False
        try:
            return not find_test_files_for_target(technique, project, int(bug_id), classes)
        except (OSError, ValueError, json.JSONDecodeError):
            return False
    if status not in {"DONE", "COMPILE_ERROR"}:
        return False
    paths = result.get("test_paths")
    expected = result.get("suite_sha256")
    if not paths or not expected or not all(os.path.isfile(path) for path in paths):
        return False
    try:
        return suite_fingerprint(paths) == expected
    except OSError:
        return False


def load_csv_run_ids(csv_path: str) -> set:
    """Index persisted measurement runs so resume cannot skip another output file."""
    runs = set()
    if not os.path.isfile(csv_path):
        return runs
    with open(csv_path, "r", newline="", encoding="utf-8", errors="replace") as f:
        for row in csv.DictReader(f):
            runs.add((
                row.get("Project", ""),
                str(row.get("Bug_ID", "")),
                row.get("Technique", ""),
                row.get("Suite_SHA256", ""),
                row.get("Run_ID", ""),
            ))
    return runs


@lru_cache(maxsize=1)
def load_ipo_manifest() -> Dict[str, Any]:
    if not os.path.isfile(IPO_MANIFEST):
        return {}
    with open(IPO_MANIFEST, "r", encoding="utf-8") as f:
        return json.load(f)


@lru_cache(maxsize=1)
def load_catalog_targets() -> Dict[Tuple[str, int], List[str]]:
    catalog_path = os.path.join(PROJECT_ROOT, "target_benchmark", "all_bugs_catalog.json")
    if not os.path.isfile(catalog_path):
        return {}
    with open(catalog_path, "r", encoding="utf-8") as f:
        catalog = json.load(f)
    targets = {}
    for item in catalog:
        classes = item.get("target_classes") or item.get("modified_classes") or []
        targets[(item["project"], int(item["bug_id"]))] = list(classes)
    return targets


def find_verified_ipo_suites(project: str, bug_id: int, modified_classes: List[str]) -> List[str]:
    """Return only manifest-listed Native IPO suites whose content hash is intact."""
    manifest = load_ipo_manifest()
    suite_root = os.path.realpath(os.path.join(PROJECT_ROOT, "Combinatorial_IPO"))
    modified = set(modified_classes)
    found = []
    invalid = []
    for record in manifest.get("records", []):
        if record.get("project") != project or int(record.get("bug_id", -1)) != int(bug_id):
            continue
        target_class = record.get("target_class", "")
        if target_class not in modified:
            continue
        if record.get("status") != "FIXED_VERIFIED" or record.get("generation_backend") != "native_ipo":
            invalid.append(f"{target_class}: manifest status/backend is not verified Native IPO")
            continue
        rel_path = record.get("suite_path", "")
        suite_path = os.path.realpath(os.path.join(suite_root, rel_path))
        if os.path.commonpath([suite_root, suite_path]) != suite_root or not os.path.isfile(suite_path):
            invalid.append(f"{target_class}: missing or unsafe suite_path {rel_path!r}")
            continue
        expected_hash = record.get("suite_sha256", "")
        if not expected_hash or sha256_file(suite_path) != expected_hash:
            invalid.append(f"{target_class}: suite SHA-256 mismatch")
            continue
        found.append(suite_path)
    if invalid:
        raise ValueError("Invalid Native IPO manifest record(s): " + "; ".join(invalid))
    return sorted(set(found))

def find_test_files_for_target(technique: str, project: str, bug_id: int, modified_classes: List[str]) -> List[str]:
    """
    Find generated test .java files matching target modified classes across Defects4J.
    Supports multi-class bugs, specific bug subdirectories, and root test directories.
    Strictly verifies class name match to prevent cross-project test leakage.
    """
    if technique == "ipo":
        return find_verified_ipo_suites(project, bug_id, modified_classes)

    base_dir = TECHNIQUE_DIRS.get(technique)
    if not base_dir or not os.path.exists(base_dir):
        return []
    
    # Potential directories where tests for this bug might reside
    candidate_dirs = [
        os.path.join(base_dir, f"{project}_{bug_id}b"),
        os.path.join(base_dir, f"{project}-{bug_id}"),
        os.path.join(base_dir, f"{project}_{bug_id}"),
        os.path.join(base_dir, project, str(bug_id)),
        base_dir
    ]
    
    found = []
    
    for c_dir in candidate_dirs:
        if not os.path.exists(c_dir):
            continue
        c_dir_base = os.path.basename(c_dir).lower()
        is_bug_specific_dir = (
            c_dir_base in [f"{project.lower()}_{bug_id}b", f"{project.lower()}-{bug_id}", f"{project.lower()}_{bug_id}"]
            or f"{project.lower()}_{bug_id}b" in c_dir.lower()
            or f"{project.lower()}-{bug_id}" in c_dir.lower()
        )
        # When checking base_dir fallback, do NOT recurse into other bug subfolders
        pattern = os.path.join(c_dir, "**", "*Test*.java") if is_bug_specific_dir else os.path.join(c_dir, "*Test*.java")
        for f in glob.glob(pattern, recursive=is_bug_specific_dir):
            if "scaffolding" in f:
                continue
            if test_file_matches_targets(f, modified_classes) and f not in found:
                found.append(f)
                            
        # If tests were found in the dedicated bug-specific directory, stop searching
        if is_bug_specific_dir and found:
            break
            
    # Return found test files. (Never fall back to grabbing unrelated tests)
    return found

def get_class_package(java_file_path: str) -> str:
    """Extract package declaration from a Java file."""
    try:
        with open(java_file_path, "r", encoding="utf-8", errors="replace") as f:
            for line in f:
                line = line.strip()
                if line.startswith("package ") and line.endswith(";"):
                    return line[8:-1].strip()
    except Exception:
        pass
    return ""


def test_file_matches_targets(java_file_path: str, modified_classes: List[str]) -> bool:
    """Match generated tests by exact target package and target-name prefix."""
    filename = os.path.basename(java_file_path).lower()
    package = get_class_package(java_file_path)
    for target_class in modified_classes:
        short_name = target_class.rsplit(".", 1)[-1].lower()
        target_package = target_class.rsplit(".", 1)[0] if "." in target_class else ""
        if package != target_package or not filename.startswith(short_name):
            continue
        suffix = filename[len(short_name):]
        if suffix.startswith(("_", "-", "deepseek", "gemini")):
            return True
    return False

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
    started_clock = time.monotonic()
    started_at = time.strftime("%Y-%m-%d %H:%M:%S")
    run_id = f"{state_key}-{int(time.time())}"
    print(f"\n[{state_key}] Starting evaluation...")
    
    work_buggy = os.path.join(WORK_BASE, f"{project}_{bug_id}b")
    work_fixed = os.path.join(WORK_BASE, f"{project}_{bug_id}f")
    # Never trust leftovers from an interrupted prior evaluation.
    shutil.rmtree(work_buggy, ignore_errors=True)
    shutil.rmtree(work_fixed, ignore_errors=True)

    def finish(result: Dict[str, Any]) -> Dict[str, Any]:
        if clean_tmp:
            shutil.rmtree(work_buggy, ignore_errors=True)
            shutil.rmtree(work_fixed, ignore_errors=True)
        return result
    
    # Resolve targets from the checked-in catalog so missing suites do not trigger
    # expensive project checkouts. Every suite that will run is checked against D4J.
    modified_classes = load_catalog_targets().get((project, int(bug_id)), [])
    if not modified_classes:
        print(f"[{state_key}] Checking out {project}-{bug_id}b to read target metadata...")
        if not d4j_meta.checkout_project(project, bug_id, is_buggy=True, work_dir=work_buggy):
            append_csv_result(csv_path, [
                project, bug_id, TECHNIQUE_NAMES.get(technique, technique), "", "", "", "",
                "CHECKOUT_ERROR", 0, "CHECKOUT_ERROR", started_at
            ], run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail="Buggy checkout failed")
            return finish({"status": "CHECKOUT_ERROR", "fault_detected": "NOT_EVALUATED", "line_cov": None, "branch_cov": None, "run_id": run_id})
        modified_classes = d4j_meta.get_modified_classes(work_buggy)
    print(f"[{state_key}] Target modified classes: {modified_classes}")

    # Locate suite before checkout; absent suites get a durable NO_SUITE outcome.
    try:
        test_files = find_test_files_for_target(technique, project, bug_id, modified_classes)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        detail = str(exc)
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), "", "", "",
            "INVALID_SUITE", 0, "INVALID_SUITE", started_at
        ], run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "INVALID_SUITE", "fault_detected": "NOT_EVALUATED",
            "target_classes": ";".join(modified_classes), "error": detail
        })

    if not test_files:
        print(f"[{state_key}] ⏳ No test files found for this bug. Recording NO_SUITE without checkout.")
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), "", "", "",
            "NO_SUITE", 0, "NO_SUITE", started_at
        ], run_id=run_id, duration_sec=time.monotonic() - started_clock,
            error_detail="No matching test suite was available")
        return finish({
            "status": "NO_SUITE", "fault_detected": "NOT_EVALUATED", "line_cov": None,
            "branch_cov": None, "target_classes": ";".join(modified_classes), "test_file": "",
            "test_paths": [], "suite_sha256": "", "run_id": run_id,
        })

    if not os.path.isdir(work_buggy):
        print(f"[{state_key}] Checking out {project}-{bug_id}b...")
        if not d4j_meta.checkout_project(project, bug_id, is_buggy=True, work_dir=work_buggy):
            append_csv_result(csv_path, [
                project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), "", "", "",
                "CHECKOUT_ERROR", 0, "CHECKOUT_ERROR", started_at
            ], run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail="Buggy checkout failed")
            return finish({"status": "CHECKOUT_ERROR", "fault_detected": "NOT_EVALUATED", "line_cov": None, "branch_cov": None, "run_id": run_id})

    live_classes = d4j_meta.get_modified_classes(work_buggy)
    if not live_classes or set(live_classes) != set(modified_classes):
        detail = f"Catalog/D4J target class mismatch: catalog={modified_classes}; defects4j={live_classes}"
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes),
            ";".join(os.path.basename(path) for path in test_files), "", "", "INVALID_SUITE", 0,
            "INVALID_SUITE", started_at
        ], suite_sha256=suite_fingerprint(test_files), run_id=run_id,
            duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({"status": "INVALID_SUITE", "fault_detected": "NOT_EVALUATED", "error": detail,
                       "suite_sha256": suite_fingerprint(test_files), "test_paths": test_files, "run_id": run_id})
    modified_classes = live_classes
    test_fname = ";".join([os.path.basename(tf) for tf in test_files])
    test_sha256 = suite_fingerprint(test_files)
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
        res_dict = {
            "status": "TIMEOUT",
            "fault_detected": "TIMEOUT",
            "line_cov": None,
            "branch_cov": None,
            "test_file": test_fname,
            "target_classes": ";".join(modified_classes),
            "buggy_failures": [],
            "fixed_failures": []
        }
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
            ";".join(modified_classes), test_fname, "", "",
            "TIMEOUT", 0, "TIMEOUT", time.strftime("%Y-%m-%d %H:%M:%S")
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=cov_err[-2000:])
        res_dict.update({"suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id})
        return finish(res_dict)

    if "cannot compile" in (cov_out + cov_err).lower() or (cov_code != 0 and not os.path.exists(os.path.join(work_buggy, "summary.csv"))):
        print(f"[{state_key}] ❌ COMPILE ERROR: Generated test suite failed to compile!")
        res_dict = {
            "status": "COMPILE_ERROR",
            "fault_detected": "COMPILE_ERROR",
            "line_cov": None,
            "branch_cov": None,
            "test_file": test_fname,
            "target_classes": ";".join(modified_classes),
            "buggy_failures": [],
            "fixed_failures": []
        }
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
            ";".join(modified_classes), test_fname, "", "",
            "COMPILE_ERROR", 0, "COMPILE_ERROR", time.strftime("%Y-%m-%d %H:%M:%S")
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=(cov_out + "\n" + cov_err)[-2000:])
        res_dict.update({"suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id})
        return finish(res_dict)
        
    summary_csv = os.path.join(work_buggy, "summary.csv")
    cov_metrics = parse_d4j_coverage_summary(summary_csv)
    line_cov = cov_metrics.get("line_coverage", 0.0)
    branch_cov = cov_metrics.get("branch_coverage", 0.0)
    print(f"[{state_key}] Target Modified Class Coverage -> Line: {line_cov}% | Branch: {branch_cov}%")
    
    # 6. Test on Buggy version
    print(f"[{state_key}] Running test on Buggy version to verify failure exposure...")
    test_b_code, test_b_out, test_b_err = d4j_meta.run_cmd(["defects4j", "test", "-w", work_buggy, "-s", archive_buggy], timeout=240)
    if "timed out" in test_b_err.lower():
        res_dict = {
            "status": "TIMEOUT",
            "fault_detected": "TIMEOUT",
            "line_cov": line_cov,
            "branch_cov": branch_cov,
            "test_file": test_fname,
            "target_classes": ";".join(modified_classes),
            "buggy_failures": [],
            "fixed_failures": []
        }
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
            ";".join(modified_classes), test_fname, line_cov, branch_cov,
            "TIMEOUT", 0, "TIMEOUT", time.strftime("%Y-%m-%d %H:%M:%S")
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=test_b_err[-2000:])
        res_dict.update({"suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id})
        return finish(res_dict)

    fail_b_count, fail_b_list = count_failing_tests(work_buggy)
    if "cannot compile" in (test_b_out + test_b_err).lower():
        detail = (test_b_out + "\n" + test_b_err)[-2000:]
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), test_fname,
            line_cov, branch_cov, "COMPILE_ERROR", 0, "COMPILE_ERROR", started_at
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "COMPILE_ERROR", "fault_detected": "COMPILE_ERROR", "line_cov": None,
            "branch_cov": None, "test_file": test_fname, "target_classes": ";".join(modified_classes),
            "suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id, "error": detail
        })
    if test_b_code != 0 and fail_b_count == 0:
        detail = (test_b_out + "\n" + test_b_err)[-2000:]
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), test_fname,
            line_cov, branch_cov, "RUN_ERROR", 0, "RUN_ERROR", started_at
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "RUN_ERROR", "fault_detected": "NOT_EVALUATED", "line_cov": None,
            "branch_cov": None, "test_file": test_fname, "target_classes": ";".join(modified_classes),
            "suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id, "error": detail
        })
    print(f"[{state_key}] Buggy Failures ({fail_b_count}): {fail_b_list[:2]}")
    
    # 7. Checkout & Test on Fixed version
    print(f"[{state_key}] Checking out Fixed version ({project}-{bug_id}f)...")
    if not d4j_meta.checkout_project(project, bug_id, is_buggy=False, work_dir=work_fixed):
        detail = "Fixed checkout failed"
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), test_fname,
            line_cov, branch_cov, "CHECKOUT_ERROR", 0, "CHECKOUT_ERROR", started_at
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "CHECKOUT_ERROR", "fault_detected": "NOT_EVALUATED", "line_cov": None,
            "branch_cov": None, "test_file": test_fname, "target_classes": ";".join(modified_classes),
            "suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id, "error": detail
        })
    print(f"[{state_key}] Running test on Fixed version to verify fix passing...")
    test_f_code, test_f_out, test_f_err = d4j_meta.run_cmd(["defects4j", "test", "-w", work_fixed, "-s", archive_fixed], timeout=240)
    if "timed out" in test_f_err.lower():
        res_dict = {
            "status": "TIMEOUT",
            "fault_detected": "TIMEOUT",
            "line_cov": line_cov,
            "branch_cov": branch_cov,
            "test_file": test_fname,
            "target_classes": ";".join(modified_classes),
            "buggy_failures": fail_b_list,
            "fixed_failures": []
        }
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
            ";".join(modified_classes), test_fname, line_cov, branch_cov,
            "TIMEOUT", 0, "TIMEOUT", time.strftime("%Y-%m-%d %H:%M:%S")
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=test_f_err[-2000:])
        res_dict.update({"suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id})
        return finish(res_dict)

    fail_f_count, fail_f_list = count_failing_tests(work_fixed)
    if "cannot compile" in (test_f_out + test_f_err).lower():
        detail = (test_f_out + "\n" + test_f_err)[-2000:]
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), test_fname,
            line_cov, branch_cov, "COMPILE_ERROR", 0, "COMPILE_ERROR", started_at
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "COMPILE_ERROR", "fault_detected": "COMPILE_ERROR", "line_cov": None,
            "branch_cov": None, "test_file": test_fname, "target_classes": ";".join(modified_classes),
            "suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id, "error": detail
        })
    if test_f_code != 0 and fail_f_count == 0:
        detail = (test_f_out + "\n" + test_f_err)[-2000:]
        append_csv_result(csv_path, [
            project, bug_id, TECHNIQUE_NAMES.get(technique, technique), ";".join(modified_classes), test_fname,
            line_cov, branch_cov, "RUN_ERROR", 0, "RUN_ERROR", started_at
        ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock, error_detail=detail)
        return finish({
            "status": "RUN_ERROR", "fault_detected": "NOT_EVALUATED", "line_cov": None,
            "branch_cov": None, "test_file": test_fname, "target_classes": ";".join(modified_classes),
            "suite_sha256": test_sha256, "test_paths": test_files, "run_id": run_id, "error": detail
        })
    print(f"[{state_key}] Fixed Failures ({fail_f_count}): {fail_f_list[:2]}")
    
    # 8. Classify Fault Detection with Academic Rigor (5 Standard Levels)
    failures_count = 0
    if fail_b_count > 0 and fail_f_count == 0:
        fault_detected = "BUG_DETECTED"
        failures_count = fail_b_count
    elif fail_b_count > 0 and fail_f_count > 0:
        fault_detected = "FLAKY_OR_REGRESSION"
        failures_count = fail_b_count
    else:
        fault_detected = "NOT_DETECTED"
        failures_count = 0
        
    print(f"[{state_key}] [RESULT] Status: DONE | Line Cov: {line_cov}% | Branch Cov: {branch_cov}% | Fault: {fault_detected} ({failures_count} Failures)")
    
    res_dict = {
        "status": "DONE",
        "fault_detected": fault_detected,
        "failures_count": failures_count,
        "line_cov": line_cov,
        "branch_cov": branch_cov,
        "test_file": test_fname,
        "target_classes": ";".join(modified_classes),
        "buggy_failures": fail_b_list,
        "fixed_failures": fail_f_list,
        "suite_sha256": test_sha256,
        "test_paths": test_files,
        "run_id": run_id
    }
    
    # Save individual JSON
    bug_res_dir = os.path.join(RESULTS_DIR, project, str(bug_id))
    os.makedirs(bug_res_dir, exist_ok=True)
    with open(os.path.join(bug_res_dir, f"{technique}.json"), "w", encoding="utf-8") as jf:
        json.dump(res_dict, jf, indent=2)
        
    # Append CSV
    append_csv_result(csv_path, [
        project, bug_id, TECHNIQUE_NAMES.get(technique, technique),
        ";".join(modified_classes), test_fname, line_cov, branch_cov,
        fault_detected, failures_count, "DONE", time.strftime("%Y-%m-%d %H:%M:%S")
    ], suite_sha256=test_sha256, run_id=run_id, duration_sec=time.monotonic() - started_clock)
    
    # Save individual JSON with the exact suite provenance used for this result.
    with open(os.path.join(bug_res_dir, f"{technique}.json"), "w", encoding="utf-8") as jf:
        json.dump(res_dict, jf, indent=2, ensure_ascii=False)
        
    return finish(res_dict)

def main():
    parser = argparse.ArgumentParser(description="Universal Benchmark Runner for Defects4J (All-Bugs & All-Classes)")
    parser.add_argument("--project", type=str, help="Run specific project (e.g. Lang)")
    parser.add_argument("--bug", type=int, help="Run specific bug ID (e.g. 1)")
    parser.add_argument("--sample-17", action="store_true", help="Run 17 Representative Projects Benchmark")
    parser.add_argument("--all-bugs", action="store_true", help="Run Exhaustive Benchmark on all active bugs in Defects4J")
    parser.add_argument("--techniques", type=str, default="ipo,mio,deepseek,gemini", help="Comma-separated techniques")
    parser.add_argument("--resume", action="store_true", help="Resume from progress.json")
    parser.add_argument("--csv", type=str, default=DEFAULT_CSV, help="Output CSV path")
    parser.add_argument("--no-clean", action="store_true", help="Do not delete /tmp folders after evaluation")
    args = parser.parse_args()

    init_csv(args.csv)
    progress = load_progress() if args.resume else {}
    persisted_runs = load_csv_run_ids(args.csv)
    techniques = [t.strip().lower() for t in args.techniques.split(",") if t.strip()]
    techniques = list(dict.fromkeys("deepseek" if t == "claude" else t for t in techniques))
    unknown = sorted(set(techniques) - set(TECHNIQUE_NAMES))
    if unknown:
        parser.error(f"Unknown technique(s): {', '.join(unknown)}")

    # Build evaluation queue
    queue: List[Tuple[str, int]] = []
    
    if args.project and args.bug:
        queue = [(args.project, args.bug)]
    elif args.sample_17:
        queue = REPRESENTATIVE_17
    elif args.project:
        bugs = d4j_meta.get_active_bugs(args.project)
        queue = [(args.project, b) for b in bugs]
    else:
        # Default: All active bugs across all projects (Defects4J All-Bugs / All-Classes)
        projects = [args.project] if args.project else d4j_meta.get_all_projects()
        print(f"Discovering all active bugs across {len(projects)} projects in Defects4J...")
        for p in projects:
            bugs = d4j_meta.get_active_bugs(p)
            print(f"  -> {p}: {len(bugs)} active bugs")
            for b in bugs:
                queue.append((p, b))

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
            prior = progress.get(key, {})
            output_key = (
                proj,
                str(bid),
                TECHNIQUE_NAMES.get(tech, tech),
                prior.get("suite_sha256", ""),
                prior.get("run_id", ""),
            )
            if args.resume and progress_result_is_current(prior, proj, bid, tech) and output_key in persisted_runs:
                print(f"⏩ [SKIP] {key} already completed with the same suite hash")
                continue
                
            try:
                res = evaluate_technique_on_bug(
                    proj, bid, tech, args.csv, clean_tmp=(not args.no_clean)
                )
                progress[key] = res
                save_progress(progress)
            except Exception as e:
                print(f"❌ Error evaluating {key}: {e}")
                detail = str(e)
                progress[key] = {"status": "RUN_ERROR", "error": detail}
                append_csv_result(args.csv, [
                    proj, bid, TECHNIQUE_NAMES.get(tech, tech), "", "", "", "",
                    "RUN_ERROR", 0, "RUN_ERROR", time.strftime("%Y-%m-%d %H:%M:%S")
                ], run_id=f"{key}-{int(time.time())}", error_detail=detail[-2000:])
                save_progress(progress)

    print("\n" + "=" * 65)
    print(f"🎉 Benchmark Execution Completed!")
    print(f"📄 Summary CSV available at: {args.csv}")
    print("=" * 65)

if __name__ == "__main__":
    main()
