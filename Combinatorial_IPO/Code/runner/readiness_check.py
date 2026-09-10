"""Verify persistent evidence required before starting the catalog loop."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Dict, List


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from verification.pair_coverage import verify_pair_coverage  # noqa: E402
from runner.experiment import resolve_experiment  # noqa: E402
from runner.scenario_catalog import load_scenario_catalog  # noqa: E402


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
    ipo_root: Path,
    representatives_path: Path,
    feasibility_path: Path,
    catalog_path: Path = None,
    scenario_root: Path = None,
    experiment_path: Path = None,
) -> dict:
    """Check representative artifacts and non-generating catalog audit evidence."""
    representatives = json.loads(representatives_path.read_text(encoding="utf-8"))
    method_results = [
        _verify_representative(ipo_root, expected) for expected in representatives
    ]

    scenario_issues: List[str] = []
    resolved_experiment = None
    supplied_experiment_inputs = (catalog_path, scenario_root, experiment_path)
    if any(item is not None for item in supplied_experiment_inputs):
        if not all(item is not None for item in supplied_experiment_inputs):
            scenario_issues.append(
                "Catalog, scenario root, and experiment manifest must be provided together"
            )
        else:
            try:
                resolved_experiment = resolve_experiment(
                    experiment_path, catalog_path, scenario_root
                )
            except (ValueError, OSError, json.JSONDecodeError) as exc:
                scenario_issues.append(str(exc))

    catalog_issues: List[str] = []
    audit_statuses = {}
    if not feasibility_path.is_file():
        catalog_issues.append("Missing feasibility audit")
        feasibility = {}
    else:
        feasibility = json.loads(feasibility_path.read_text(encoding="utf-8"))
        if feasibility.get("generation_performed") is not False:
            catalog_issues.append("Feasibility audit must not generate tests")
        if resolved_experiment is not None:
            audit_records = feasibility.get("targets")
            if not isinstance(audit_records, list):
                catalog_issues.append(
                    "Feasibility audit requires per-source target records"
                )
                selected_audit_records = []
            else:
                selected_identities = {
                    (target.project, target.bug_id)
                    for target in resolved_experiment.targets
                }
                selected_audit_records = [
                    record
                    for record in audit_records
                    if isinstance(record, dict)
                    and (record.get("project"), record.get("bug_id"))
                    in selected_identities
                ]
            audited_identities = {
                (record.get("project"), record.get("bug_id"))
                for record in selected_audit_records
            }
            if len(audited_identities) != resolved_experiment.spec.expected_target_count:
                catalog_issues.append(
                    "Feasibility audit target count does not match experiment"
                )
            if (
                len(selected_audit_records)
                != resolved_experiment.spec.expected_modified_source_count
            ):
                catalog_issues.append(
                    "Feasibility audit modified source count does not match experiment"
                )
            audit_statuses = dict(
                Counter(
                    str(record.get("status")) for record in selected_audit_records
                )
            )
            source_target_count = len(selected_audit_records)
        else:
            audit_statuses = feasibility.get("target_status_counts", {})
            source_target_count = feasibility.get("source_target_count")
        if (
            source_target_count is not None
            and sum(audit_statuses.values()) != source_target_count
        ):
            catalog_issues.append(
                "Feasibility source count does not match target status counts"
            )
        unexpected = set(audit_statuses) - {"AUDITED", "CATALOG_MISMATCH"}
        if unexpected:
            catalog_issues.append(
                "Unexpected feasibility statuses: {}".format(
                    ", ".join(sorted(unexpected))
                )
            )

    known_catalog_mismatches = int(
        audit_statuses.get("CATALOG_MISMATCH", 0)
    )
    scenario_target_count = 0
    scenario_count = 0
    if resolved_experiment is not None:
        try:
            plans = load_scenario_catalog(
                catalog_path, scenario_root, experiment_path
            )
            scenario_target_count = len(plans)
            scenario_count = sum(len(plan.scenarios) for plan in plans)
        except (ValueError, OSError, json.JSONDecodeError) as exc:
            scenario_issues.append(str(exc))
    loop_state_path = ipo_root / "Result_Round2" / "catalog_loop_state.json"
    loop_started = False
    loop_state = None
    if loop_state_path.is_file():
        loop_state = json.loads(loop_state_path.read_text(encoding="utf-8"))
        loop_started = loop_state.get("started") is True
    loop_in_progress = loop_started and loop_state.get("status") in {
        "STARTED",
        "PREFLIGHT",
    }

    ready = (
        all(result["passed"] for result in method_results)
        and not catalog_issues
        and not scenario_issues
        and not loop_in_progress
    )
    return {
        "ready_to_start_loop": ready,
        "loop_started": loop_started,
        "loop_in_progress": loop_in_progress,
        "loop_state": loop_state,
        "representative_count": len(method_results),
        "representatives_passed": sum(
            result["passed"] for result in method_results
        ),
        "known_catalog_mismatches": known_catalog_mismatches,
        "catalog_issues": catalog_issues,
        "experiment_id": (
            resolved_experiment.spec.experiment_id
            if resolved_experiment is not None
            else None
        ),
        "experiment_target_count": (
            resolved_experiment.spec.expected_target_count
            if resolved_experiment is not None
            else 0
        ),
        "scenario_target_count": scenario_target_count,
        "scenario_count": scenario_count,
        "scenario_issues": scenario_issues,
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
        "--catalog",
        type=Path,
        default=IPO_ROOT.parent / "target_benchmark" / "catalog_17_projects.json",
    )
    parser.add_argument(
        "--scenarios",
        type=Path,
        default=IPO_ROOT / "Configuration" / "targets",
    )
    parser.add_argument(
        "--experiment",
        type=Path,
        default=IPO_ROOT / "Configuration" / "experiments" / "round2-17-targets.json",
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

    report = check_readiness(
        args.ipo_root,
        args.representatives,
        args.feasibility,
        catalog_path=args.catalog,
        scenario_root=args.scenarios,
        experiment_path=args.experiment,
    )
    rendered = json.dumps(report, indent=2)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(rendered, encoding="utf-8")
    print(rendered)
    if not report["ready_to_start_loop"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
