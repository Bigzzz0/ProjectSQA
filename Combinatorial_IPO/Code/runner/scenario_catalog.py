"""Run approved defect-focused scenarios without scanning arbitrary methods."""

from __future__ import annotations

import argparse
import csv
import json
import math
import shutil
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Optional


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from algorithm.ipo import generate_pairwise  # noqa: E402
from generator.scenario_junit_generator import synthesize_scenario_suite  # noqa: E402
from oracle.fixed_version_oracle import OracleCollectionError  # noqa: E402
from oracle.verify_suite import verify_suite  # noqa: E402
from runner.experiment import resolve_experiment  # noqa: E402
from scenario.spec import TargetScenarioPlan, load_target_plan  # noqa: E402
from verification.pair_coverage import verify_pair_coverage  # noqa: E402


def load_scenario_catalog(
    catalog_path: Path, scenario_root: Path, experiment_path: Path
) -> List[TargetScenarioPlan]:
    resolved = resolve_experiment(experiment_path, catalog_path, scenario_root)
    return [
        load_target_plan(resolved.scenario_paths[target.target_key], target)
        for target in resolved.targets
    ]


def _write_tsv(path: Path, fields: List[str], rows: List[Dict[str, str]]) -> None:
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, delimiter="\t")
        writer.writeheader()
        writer.writerows(rows)


def _generate_target(plan: TargetScenarioPlan, target_dir: Path) -> Dict[str, object]:
    scenario_rows = []
    scenario_records = []
    for scenario in plan.scenarios:
        valid = scenario.valid_rows()
        rows = generate_pairwise(
            scenario.factors,
            seed_combinations=scenario.seeds,
            valid_combinations=valid,
        )
        coverage = verify_pair_coverage(
            scenario.factors, rows, valid_combinations=valid
        )
        if not coverage.complete:
            raise ValueError(
                "{} misses {} valid pairs".format(
                    scenario.scenario_id, len(coverage.missing_pairs)
                )
            )
        scenario_dir = target_dir / scenario.scenario_id
        scenario_dir.mkdir(parents=True, exist_ok=True)
        (scenario_dir / "domains.json").write_text(
            json.dumps(scenario.factors, indent=2), encoding="utf-8"
        )
        (scenario_dir / "constraints.json").write_text(
            json.dumps(list(scenario.constraints), indent=2), encoding="utf-8"
        )
        _write_tsv(scenario_dir / "combinations.tsv", list(scenario.factors), rows)
        materialized = [scenario.materialize(row) for row in rows]
        (scenario_dir / "materialized_inputs.json").write_text(
            json.dumps(materialized, indent=2), encoding="utf-8"
        )
        oracle_rows = [
            {
                "id": index,
                "combination": row,
                "mode": scenario.oracle["mode"],
                "description": scenario.oracle.get("description", ""),
            }
            for index, row in enumerate(rows, start=1)
        ]
        (scenario_dir / "oracle.json").write_text(
            json.dumps(oracle_rows, indent=2), encoding="utf-8"
        )
        scenario_records.append(
            {
                "id": scenario.scenario_id,
                "entrypoint": scenario.entrypoint,
                "invocation_kind": scenario.invocation_kind,
                "factor_count": len(scenario.factors),
                "cartesian_count": math.prod(len(values) for values in scenario.factors.values()),
                "valid_combination_count": len(valid),
                "pairwise_count": len(rows),
                "expected_pair_count": coverage.expected_pair_count,
                "covered_pair_count": coverage.covered_pair_count,
                "pair_coverage_percent": coverage.coverage_percent,
                "mandatory_seed_count": len(scenario.seeds),
                "evidence": scenario.evidence,
            }
        )
        scenario_rows.append((scenario, rows))

    suite = synthesize_scenario_suite(plan, scenario_rows)
    suite_path = target_dir / "{}.java".format(plan.suite_class)
    suite_path.write_text(suite, encoding="utf-8")
    shutil.copy2(plan.source_path, target_dir / "scenario_spec.json")
    return {
        "project": plan.project,
        "bug_id": plan.bug_id,
        "target": plan.target_key,
        "status": "GENERATED",
        "generation_backend": "native_ipo",
        "strength": 2,
        "suite_package": plan.suite_package,
        "suite_class": plan.suite_class,
        "suite_path": str(suite_path),
        "scenarios": scenario_records,
    }


def run_scenario_catalog(
    catalog_path: Path,
    scenario_root: Path,
    experiment_path: Path,
    output_root: Path,
    defects4j_executable: str = "defects4j",
    verify_fixed: bool = True,
    run_id: Optional[str] = None,
) -> Dict[str, object]:
    resolved = resolve_experiment(experiment_path, catalog_path, scenario_root)
    plans = load_scenario_catalog(catalog_path, scenario_root, experiment_path)
    expected_target_count = resolved.spec.expected_target_count
    run_id = run_id or datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    result_root = output_root / "Result_Round2"
    run_dir = result_root / run_id
    if run_dir.exists():
        raise ValueError("Run directory already exists: {}".format(run_dir))
    run_dir.mkdir(parents=True)
    shutil.copy2(experiment_path, run_dir / "experiment_manifest.json")
    state_path = result_root / "catalog_loop_state.json"
    state_path.write_text(
        json.dumps(
            {
                "started": True,
                "status": "PREFLIGHT",
                "run_id": run_id,
                "experiment_id": resolved.spec.experiment_id,
                "target_count": expected_target_count,
            },
            indent=2,
        ),
        encoding="utf-8",
    )

    records: List[Dict[str, object]] = []
    suite_candidates: Dict[str, Path] = {}
    for plan in plans:
        target_dir = run_dir / plan.target_key
        try:
            record = _generate_target(plan, target_dir)
            suite_path = Path(str(record.pop("suite_path")))
            suite_candidates[plan.target_key] = suite_path
            if verify_fixed:
                output = verify_suite(
                    project=plan.project,
                    version="{}f".format(plan.bug_id),
                    suite_path=suite_path,
                    test_class="{}.{}".format(plan.suite_package, plan.suite_class),
                    defects4j_executable=defects4j_executable,
                )
                lines = [line.strip() for line in output.splitlines() if line.strip()]
                record["verification_result"] = lines[-1] if lines else "PASSED"
                record["status"] = "VERIFIED"
            else:
                record["status"] = "GENERATED_UNVERIFIED"
            record["test_suite"] = str(suite_path.relative_to(output_root))
            records.append(record)
        except (ValueError, OracleCollectionError, OSError) as exc:
            records.append(
                {
                    "project": plan.project,
                    "bug_id": plan.bug_id,
                    "target": plan.target_key,
                    "status": "FAILED",
                    "error": str(exc),
                }
            )

    success = verify_fixed and len(records) == expected_target_count and all(
        record["status"] == "VERIFIED" for record in records
    )
    manifest = {
        "run_id": run_id,
        "experiment_id": resolved.spec.experiment_id,
        "experiment": str(experiment_path),
        "catalog": str(catalog_path),
        "scenario_root": str(scenario_root),
        "input_fingerprints": {
            "selected_catalog_sha256": resolved.selected_catalog_sha256,
            "scenario_set_sha256": resolved.scenario_set_sha256,
        },
        "generation_backend": "native_ipo",
        "strength": resolved.spec.strength,
        "target_count": len(records),
        "verified_target_count": sum(record["status"] == "VERIFIED" for record in records),
        "published_suite_count": 0,
        "status": "VERIFIED" if success else "FAILED",
        "records": records,
    }

    if success:
        publish_root = output_root / "TestCode"
        staged = Path(tempfile.mkdtemp(prefix="ipo_publish_", dir=str(output_root)))
        try:
            for record in records:
                destination = staged / record["target"]
                destination.mkdir(parents=True)
                source_suite = suite_candidates[str(record["target"])]
                shutil.copy2(source_suite, destination / source_suite.name)
            for source_dir in staged.iterdir():
                destination = publish_root / source_dir.name
                destination.mkdir(parents=True, exist_ok=True)
                for source in source_dir.iterdir():
                    source.replace(destination / source.name)
            manifest["published_suite_count"] = len(records)
        finally:
            shutil.rmtree(staged, ignore_errors=True)

    (run_dir / "batch_manifest.json").write_text(
        json.dumps(manifest, indent=2), encoding="utf-8"
    )
    state_path.write_text(
        json.dumps(
            {
                "started": True,
                "status": manifest["status"],
                "run_id": run_id,
                "experiment_id": resolved.spec.experiment_id,
                "target_count": len(records),
                "verified_target_count": manifest["verified_target_count"],
                "published_suite_count": manifest["published_suite_count"],
            },
            indent=2,
        ),
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Run approved native IPO scenarios")
    parser.add_argument("--catalog", type=Path, default=PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json")
    parser.add_argument("--scenarios", type=Path, default=IPO_ROOT / "Configuration" / "targets")
    parser.add_argument(
        "--experiment",
        type=Path,
        default=IPO_ROOT / "Configuration" / "experiments" / "round2-17-targets.json",
    )
    parser.add_argument("--output-root", type=Path, default=IPO_ROOT)
    parser.add_argument("--defects4j", default="defects4j")
    parser.add_argument("--no-verify", action="store_true")
    parser.add_argument("--run-id")
    args = parser.parse_args()
    manifest = run_scenario_catalog(
        args.catalog,
        args.scenarios,
        args.experiment,
        args.output_root,
        defects4j_executable=args.defects4j,
        verify_fixed=not args.no_verify,
        run_id=args.run_id,
    )
    print(json.dumps(manifest, indent=2))
    if manifest["status"] != "VERIFIED":
        raise SystemExit(1)


if __name__ == "__main__":
    main()
