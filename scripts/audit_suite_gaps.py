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
    "Target_Classes", "Candidate_Files", "Candidate_Packages", "Target_Match_Files",
    "Candidate_SHA256",
]


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


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


def main():
    with open(CATALOG, encoding="utf-8") as stream:
        catalog = json.load(stream)
    targets = {
        (row["project"], int(row["bug_id"])): row.get("target_classes") or row.get("modified_classes") or []
        for row in catalog
    }
    with open(MASTER, newline="", encoding="utf-8-sig") as stream:
        rows = list(csv.DictReader(stream))

    gaps = [row for row in rows if row.get("Execution_Status") == "NO_SUITE"]
    output = []
    for row in gaps:
        project, bug_id = row["Project"], int(row["Bug_ID"])
        technique = next(key for key, label in KEY_TO_TECHNIQUE.items() if label == row["Technique"])
        classes = targets[(project, bug_id)]
        files = candidate_files(technique, project, bug_id)
        matching = [path for path in files if test_file_matches_targets(path, classes)]
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


if __name__ == "__main__":
    main()
