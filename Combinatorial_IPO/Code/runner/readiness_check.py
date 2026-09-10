"""Verify persistent evidence required before starting the catalog loop."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from pathlib import Path
from typing import Dict, List


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from verification.pair_coverage import verify_pair_coverage  # noqa: E402


def _read_tsv(path: Path) -> List[Dict[str, str]]:
    with path.open("r", encoding="utf-8", newline="") as stream:
        return list(csv.DictReader(stream, delimiter="\t"))


def _verify_representative(ipo_root: Path, expected: dict) -> dict:
    target_key = "{}_{}b".format(expected["project"], expected["bug_id"])
    record_path = (
        ipo_root
        / "Result_Round1"
        / target_key
        / expected["class"]
        / "{}_record.json".format(expected["method_id"])
    )
    result = {
        "project": expected["project"],
        "bug_id": expected["bug_id"],
        "class": expected["class"],
        "signature": expected["signature"],
        "record": str(record_path.relative_to(ipo_root)),
        "passed": False,
        "issues": [],
    }
    issues: List[str] = result["issues"]
    if not record_path.is_file():
        issues.append("Missing per-method record")
        return result

    record = json.loads(record_path.read_text(encoding="utf-8"))
    checks = {
        "method signature": record.get("method_signature") == expected["signature"],
        "strategy": record.get("strategy") == expected["strategy"],
        "native IPO backend": record.get("generation_backend") == "ipo",
        "2-way strength": record.get("strength") == 2,
        "generated status": record.get("status") == "GENERATED",
        "complete pair coverage": record.get("pair_coverage_percent") == 100.0,
        "zero missing pairs": record.get("missing_pair_count") == 0,
        "oracle available": record.get("oracle_status") in {"COLLECTED", "REUSED"},
        "fixed suite verified": record.get("suite_status") == "VERIFIED",
    }
    issues.extend(label for label, passed in checks.items() if not passed)

    duplicate_count = int(record.get("duplicate_concrete_input_count", 0))
    if duplicate_count and not record.get("duplicate_explanation"):
        issues.append("Concrete duplicates are not explained")

    artifact_keys = ("domains", "combinations", "inputs", "oracle", "test_suite")
    artifacts: Dict[str, Path] = {}
    for key in artifact_keys:
        relative_path = record.get(key)
        if not isinstance(relative_path, str):
            issues.append("Missing {} path".format(key))
            continue
        artifact_path = ipo_root / relative_path
        artifacts[key] = artifact_path
        if not artifact_path.is_file():
            issues.append("Missing {} artifact".format(key))

    if all(key in artifacts and artifacts[key].is_file() for key in artifact_keys):
        domains = json.loads(artifacts["domains"].read_text(encoding="utf-8"))
        combinations = _read_tsv(artifacts["combinations"])
        inputs = _read_tsv(artifacts["inputs"])
        oracle = json.loads(artifacts["oracle"].read_text(encoding="utf-8"))
        coverage = verify_pair_coverage(domains, combinations)
        pairwise_count = int(record.get("pairwise_count", -1))
        if not coverage.complete or coverage.covered_pair_count != coverage.expected_pair_count:
            issues.append("Independent pair coverage verification failed")
        if len(combinations) != pairwise_count or len(inputs) != pairwise_count:
            issues.append("Combination or input row count differs from manifest")
        oracle_matches = isinstance(oracle, list) and len(oracle) == len(inputs) and all(
            isinstance(outcome, dict)
            and outcome.get("id") == index
            and outcome.get("arguments") == arguments
            for index, (outcome, arguments) in enumerate(zip(oracle, inputs), start=1)
        )
        if not oracle_matches:
            issues.append("Oracle IDs or arguments do not match inputs")

        suite_source = artifacts["test_suite"].read_text(encoding="utf-8")
        if suite_source.count("@Test(timeout = 4000)") != pairwise_count:
            issues.append("JUnit timeout/test count differs from manifest")
        if "public class {}".format(artifacts["test_suite"].stem) not in suite_source:
            issues.append("JUnit public class does not match filename")

    result["passed"] = not issues
    if result["passed"]:
        result.update(
            pairwise_count=record["pairwise_count"],
            expected_pair_count=record["expected_pair_count"],
            verification_result=record.get("verification_result"),
        )
    return result


def check_readiness(
    ipo_root: Path, representatives_path: Path, feasibility_path: Path
) -> dict:
    """Check representative artifacts and non-generating catalog audit evidence."""
    representatives = json.loads(representatives_path.read_text(encoding="utf-8"))
    method_results = [
        _verify_representative(ipo_root, expected) for expected in representatives
    ]

    catalog_issues: List[str] = []
    if not feasibility_path.is_file():
        catalog_issues.append("Missing feasibility audit")
        feasibility = {}
    else:
        feasibility = json.loads(feasibility_path.read_text(encoding="utf-8"))
        if feasibility.get("generation_performed") is not False:
            catalog_issues.append("Feasibility audit must not generate tests")
        if feasibility.get("target_count") != 17:
            catalog_issues.append("Feasibility audit does not contain 17 targets")
        statuses = feasibility.get("target_status_counts", {})
        unexpected = set(statuses) - {"AUDITED", "CATALOG_MISMATCH"}
        if unexpected:
            catalog_issues.append(
                "Unexpected feasibility statuses: {}".format(
                    ", ".join(sorted(unexpected))
                )
            )

    known_catalog_mismatches = int(
        feasibility.get("target_status_counts", {}).get("CATALOG_MISMATCH", 0)
    )
    loop_state_path = ipo_root / "Result_Round1" / "catalog_loop_state.json"
    loop_started = False
    loop_state = None
    if loop_state_path.is_file():
        loop_state = json.loads(loop_state_path.read_text(encoding="utf-8"))
        loop_started = loop_state.get("started") is True

    ready = (
        all(result["passed"] for result in method_results)
        and not catalog_issues
        and not loop_started
    )
    return {
        "ready_to_start_loop": ready,
        "loop_started": loop_started,
        "loop_state": loop_state,
        "representative_count": len(method_results),
        "representatives_passed": sum(
            result["passed"] for result in method_results
        ),
        "known_catalog_mismatches": known_catalog_mismatches,
        "catalog_issues": catalog_issues,
        "representatives": method_results,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="Check IPO catalog-loop readiness")
    parser.add_argument("--ipo-root", type=Path, default=IPO_ROOT)
    parser.add_argument(
        "--representatives",
        type=Path,
        default=IPO_ROOT / "Configuration" / "readiness_representatives.json",
    )
    parser.add_argument(
        "--feasibility",
        type=Path,
        default=IPO_ROOT / "Result_Round1" / "feasibility_audit.json",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=IPO_ROOT / "Result_Round1" / "readiness_report.json",
    )
    args = parser.parse_args()

    report = check_readiness(args.ipo_root, args.representatives, args.feasibility)
    rendered = json.dumps(report, indent=2)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(rendered, encoding="utf-8")
    print(rendered)
    if not report["ready_to_start_loop"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
