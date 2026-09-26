#!/usr/bin/env python3
"""Explain why each NO_SUITE row has no currently accepted test suite."""

import csv
import hashlib
import json
import os
from collections import Counter

from run_benchmark import (
    PROJECT_ROOT,
    TECHNIQUE_DIRS,
    get_class_package,
    test_file_matches_targets,
)

MASTER = os.path.join(PROJECT_ROOT, "results", "master_benchmark_summary.csv")
CATALOG = os.path.join(PROJECT_ROOT, "target_benchmark", "all_bugs_catalog.json")
OUTPUT = os.path.join(PROJECT_ROOT, "results", "suite_gap_audit.csv")
AI_DELIVERY = os.path.join(PROJECT_ROOT, "results", "delivery_report_m3.csv")

KEY_TO_TECHNIQUE = {
    "ipo": "IPO (Native IPO)",
    "mio": "MIO (EvoSuite SBST)",
    "deepseek": "DeepSeek V4 Flash",
    "gemini": "Gemini 3.8 Flash",
}
OWNER = {
    "ipo": "Member 1 — IPO",
    "mio": "Member 2 — MIO",
    "deepseek": "Member 3 — AI",
    "gemini": "Member 3 — AI",
}
FIELDS = [
    "Project", "Bug_ID", "Technique", "Owner", "Master_Status", "Audit_Status",
    "Generation_Status", "Generation_Reason", "Generation_Evidence",
    "Target_Classes", "Candidate_Files", "Candidate_Packages", "Target_Match_Files",
    "Candidate_SHA256",
]

IPO_GENERATION = os.path.join(
    PROJECT_ROOT, "Combinatorial_IPO", "Results", "generation_manifest.json"
)
IPO_ROUTING = os.path.join(
    PROJECT_ROOT, "Combinatorial_IPO", "Results", "routing_manifest.json"
)
MIO_EVIDENCE = os.path.join(
    PROJECT_ROOT, "MIO_Algorithm", "MIO_FAILURE_ANALYSIS_REPORT.md"
)
IPO_EVIDENCE = "Combinatorial_IPO/Results/generation_manifest.json"
IPO_ROUTING_EVIDENCE = "Combinatorial_IPO/Results/routing_manifest.json"

MIO_FAILURES = {
    ("Mockito", bug): "Defects4J compile failure (Bintray/JCenter dependency endpoint)"
    for bug in [*range(1, 12), *range(18, 22)]
}
MIO_FAILURES.update({
    ("Math", bug): "EvoSuite 1.0.6 internal NullPointerException during MIO search"
    for bug in (13, 31)
})
MIO_FAILURES[("Gson", 3)] = "EvoSuite 1.0.6 internal NullPointerException during MIO search"
MIO_FAILURES[("Gson", 8)] = "EvoSuite client JVM native crash (SIGSEGV) involving sun.misc.Unsafe"
MIO_FAILURES[("JacksonDatabind", 24)] = "Defects4J compile failure caused by character encoding"


def sha256(path):
    # Member 3 recorded hashes from a Windows checkout. Canonicalize text files
    # to CRLF so delivery hashes stay comparable across Git autocrlf settings.
    with open(path, "rb") as stream:
        contents = stream.read().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
    contents = contents.replace(b"\n", b"\r\n")
    return hashlib.sha256(contents).hexdigest()


def candidate_files(technique, project, bug_id):
    base = TECHNIQUE_DIRS[technique]
    expected = f"{project.lower()}_{bug_id}b"
    dirs = [
        os.path.join(base, f"{project}_{bug_id}b"),
        os.path.join(base, f"{project}-{bug_id}"),
        os.path.join(base, f"{project}_{bug_id}"),
        os.path.join(base, project, str(bug_id)),
        base,
    ]
    found = []
    for directory in dirs:
        if not os.path.isdir(directory):
            continue
        leaf = os.path.basename(directory).lower()
        specific = (
            leaf in {expected, f"{project.lower()}-{bug_id}", f"{project.lower()}_{bug_id}"}
            or expected in directory.lower()
            or f"{project.lower()}-{bug_id}" in directory.lower()
        )
        pattern = "**/*.java" if specific else "*.java"
        for root, _, filenames in os.walk(directory) if specific else [(directory, [], os.listdir(directory))]:
            for filename in filenames:
                if filename.endswith(".java"):
                    path = os.path.join(root, filename)
                    if path not in found:
                        found.append(path)
        if specific and found:
            break
    return sorted(found)


def load_ipo_generation():
    with open(IPO_GENERATION, encoding="utf-8") as stream:
        manifest = json.load(stream)
    with open(IPO_ROUTING, encoding="utf-8") as stream:
        routing = json.load(stream)

    generation_by_bug = {}
    for record in manifest["records"]:
        key = (record["project"], int(record["bug_id"]))
        generation_by_bug.setdefault(key, []).append(record)

    routing_by_bug = {}
    for record in routing["records"]:
        key = (record["project"], int(record["bug_id"]))
        routing_by_bug.setdefault(key, []).append(record)
    return generation_by_bug, routing_by_bug


def load_ai_deliveries(targets):
    deliveries = {}
    with open(AI_DELIVERY, newline="", encoding="utf-8-sig") as stream:
        for record in csv.DictReader(stream):
            project, separator, bug_text = record["bug"].rpartition("-")
            if not separator or not bug_text.isdigit():
                raise ValueError(f"Unrecognized bug key in AI delivery report: {record['bug']!r}")
            bug_id = int(bug_text)
            tech = "deepseek" if record["tech"] == KEY_TO_TECHNIQUE["deepseek"] else (
                "gemini" if record["tech"] == KEY_TO_TECHNIQUE["gemini"] else ""
            )
            if not tech:
                raise ValueError(f"Unrecognized AI technique in delivery report: {record['tech']!r}")
            key = (project, bug_id, tech)
            path = os.path.join(PROJECT_ROOT, record["file_rel"].replace("/", os.sep))
            if not os.path.isfile(path):
                raise ValueError(f"Delivered suite file is missing: {record['file_rel']}")
            if sha256(path) != record["sha256"]:
                raise ValueError(f"Delivered suite hash mismatch: {record['file_rel']}")
            target = record["target_cls"]
            if target not in targets.get((project, bug_id), []):
                raise ValueError(
                    f"Delivered target is not in catalog for {project}-{bug_id}: {target}"
                )
            deliveries.setdefault(key, []).append(record)
    return deliveries


def generation_details(
    technique, project, bug_id, files, ipo_generation, ipo_routing, ai_deliveries
):
    if technique == "ipo":
        key = (project, bug_id)
        records = ipo_generation.get(key, [])
        statuses = {record.get("status") for record in records}
        if "GENERATION_OR_VERIFICATION_ERROR" in statuses:
            reason = (
                "Generation/verification error recorded; retained candidate is unverified"
                if files else
                "Generation/verification error recorded; no candidate Java file remains"
            )
            return (
                "GENERATION_OR_VERIFICATION_ERROR",
                reason,
                IPO_EVIDENCE,
            )

        route_statuses = sorted({
            record.get("status")
            for record in ipo_routing.get(key, [])
            if record.get("status")
        })
        detail = "; ".join(route_statuses) if route_statuses else "No routing record"
        return (
            "SKIPPED_NOT_READY",
            detail,
            f"{IPO_EVIDENCE}; {IPO_ROUTING_EVIDENCE}",
        )

    if technique == "mio":
        reason = MIO_FAILURES.get((project, bug_id))
        if reason:
            return "GENERATION_FAILURE", reason, "MIO_Algorithm/MIO_FAILURE_ANALYSIS_REPORT.md"
        return "UNCONFIRMED", "No owner generation outcome recorded", ""

    delivery_key = (project, bug_id, technique)
    if ai_deliveries.get(delivery_key):
        return (
            "CANDIDATE_DELIVERED_PENDING_BENCHMARK",
            "Owner delivery report lists the suite; central compilation and buggy/fixed evaluation are pending",
            "results/delivery_report_m3.csv; results/DELIVERY_REPORT_M3.md",
        )
    if files:
        return (
            "CANDIDATE_REQUIRES_TARGET_CORRECTION",
            "Candidate file is present but absent from the accepted delivery manifest and does not match the catalog target",
            "results/DELIVERY_REPORT_M3.md",
        )
    return "NO_CANDIDATE", "No AI suite candidate or delivery record is available", ""


def main():
    with open(CATALOG, encoding="utf-8") as stream:
        catalog = json.load(stream)
    targets = {
        (row["project"], int(row["bug_id"])): row.get("target_classes") or row.get("modified_classes") or []
        for row in catalog
    }
    with open(MASTER, newline="", encoding="utf-8-sig") as stream:
        rows = list(csv.DictReader(stream))

    ipo_generation, ipo_routing = load_ipo_generation()
    ai_deliveries = load_ai_deliveries(targets)

    gaps = [row for row in rows if row.get("Execution_Status") == "NO_SUITE"]
    output = []
    for row in gaps:
        project, bug_id = row["Project"], int(row["Bug_ID"])
        technique = next(key for key, label in KEY_TO_TECHNIQUE.items() if label == row["Technique"])
        classes = targets[(project, bug_id)]
        files = candidate_files(technique, project, bug_id)
        matching = [path for path in files if test_file_matches_targets(path, classes)]
        generation_status, generation_reason, generation_evidence = generation_details(
            technique, project, bug_id, files, ipo_generation, ipo_routing, ai_deliveries
        )
        if not files:
            status = "NO_JAVA_CANDIDATE"
        elif technique == "ipo":
            status = "UNVERIFIED_IPO_CANDIDATE"
        elif matching:
            status = "MATCHING_CANDIDATE_NOT_ADMITTED"
        else:
            status = "TARGET_MISMATCH_CANDIDATE"
        output.append({
            "Project": project,
            "Bug_ID": bug_id,
            "Technique": row["Technique"],
            "Owner": OWNER[technique],
            "Master_Status": row["Execution_Status"],
            "Audit_Status": status,
            "Generation_Status": generation_status,
            "Generation_Reason": generation_reason,
            "Generation_Evidence": generation_evidence,
            "Target_Classes": ";".join(classes),
            "Candidate_Files": ";".join(os.path.relpath(path, PROJECT_ROOT) for path in files),
            "Candidate_Packages": ";".join(sorted({get_class_package(path) for path in files})),
            "Target_Match_Files": ";".join(os.path.relpath(path, PROJECT_ROOT) for path in matching),
            "Candidate_SHA256": ";".join(sha256(path) for path in files),
        })

    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    with open(OUTPUT, "w", newline="", encoding="utf-8-sig") as stream:
        writer = csv.DictWriter(stream, fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows(output)
    print(f"Wrote {len(output)} NO_SUITE audit rows to {os.path.relpath(OUTPUT, PROJECT_ROOT)}")
    for status, count in sorted(Counter(row["Audit_Status"] for row in output).items()):
        print(f"{status}: {count}")
    print("Generation outcomes:")
    for status, count in sorted(Counter(row["Generation_Status"] for row in output).items()):
        print(f"{status}: {count}")


if __name__ == "__main__":
    main()
