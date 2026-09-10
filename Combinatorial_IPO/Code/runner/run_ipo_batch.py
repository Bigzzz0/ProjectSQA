"""Run the automated IPO pipeline across extracted Defects4J targets."""

from __future__ import annotations

import argparse
import csv
import json
import math
import re
import sys
import tempfile
from pathlib import Path
from time import perf_counter
from typing import Dict, List, Mapping, Optional, Sequence, Tuple


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from algorithm.ipo import generate_pairwise  # noqa: E402
from analyzer.java_parser import method_signature, parse_java_file  # noqa: E402
from domain.semantic_overrides import find_semantic_override  # noqa: E402
from domain.value_generator import (  # noqa: E402
    NeedsSemanticModelError,
    UnsupportedTypeError,
    get_domain_for_type,
)
from generator.junit_generator import synthesize_junit_suite  # noqa: E402
from oracle.fixed_version_oracle import (  # noqa: E402
    OracleCollectionError,
    collect_fixed_oracle,
)
from oracle.verify_suite import verify_suite as verify_fixed_suite  # noqa: E402
from runner.catalog import resolve_catalog_targets  # noqa: E402
from verification.pair_coverage import verify_pair_coverage  # noqa: E402


TARGET_DIRECTORY_PATTERN = re.compile(r"^(?P<project>.+)_(?P<bug_id>\d+)b$")
RESULT_DIRECTORIES = {"Result_Round1", "Result_Round2"}


class UnsupportedMethodError(ValueError):
    """Raised when a parsed method cannot form a meaningful pairwise model."""


def discover_targets(target_root: Path) -> List[Tuple[str, int, Path]]:
    """Discover all extracted ``<Project>_<BugID>b`` directories."""
    targets: List[Tuple[str, int, Path]] = []
    if not target_root.is_dir():
        raise ValueError("Target root does not exist: {}".format(target_root))

    for directory in sorted(path for path in target_root.iterdir() if path.is_dir()):
        match = TARGET_DIRECTORY_PATTERN.fullmatch(directory.name)
        if match:
            targets.append(
                (match.group("project"), int(match.group("bug_id")), directory)
            )
    return targets


def _method_id(method: Mapping[str, object]) -> str:
    parameters = method.get("parameters", [])
    parameter_types = [
        str(parameter.get("type", "unknown"))
        for parameter in parameters
        if isinstance(parameter, dict)
    ]
    signature = "_".join(parameter_types) if parameter_types else "no_args"
    raw = "{}__{}".format(method.get("name", "method"), signature)
    return re.sub(r"[^A-Za-z0-9_.-]+", "_", raw).strip("_")


def _method_signature(method: Mapping[str, object]) -> str:
    """Return a stable Java-style signature used to select overloads."""
    return method_signature(method)


def _matches_method_filter(
    method: Mapping[str, object],
    method_filter: Optional[str],
    signature_filter: Optional[str],
) -> bool:
    """Match either the legacy method name or one exact overload signature."""
    if signature_filter:
        return _method_signature(method) == re.sub(r"\s+", "", signature_filter)
    return not method_filter or method.get("name") == method_filter


def _domains_for_method(method: Mapping[str, object]) -> Dict[str, List[str]]:
    domains: Dict[str, List[str]] = {}
    for parameter in method.get("parameters", []):
        if not isinstance(parameter, dict):
            raise ValueError("Malformed method parameter metadata")
        name = parameter.get("name")
        parameter_type = parameter.get("type")
        if not isinstance(name, str) or not isinstance(parameter_type, str):
            raise ValueError("Parameter name and type are required")
        domains[name] = get_domain_for_type(parameter_type)
    return domains


def _write_combinations(
    path: Path,
    parameter_names: Sequence[str],
    combinations: Sequence[Mapping[str, str]],
) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=parameter_names, delimiter="\t")
        writer.writeheader()
        writer.writerows(combinations)


def run_batch(
    target_root: Path,
    output_root: Path,
    project_filter: Optional[str] = None,
    bug_filter: Optional[int] = None,
    method_filter: Optional[str] = None,
    signature_filter: Optional[str] = None,
    catalog_path: Optional[Path] = None,
    collect_oracles: bool = False,
    verify_suites: bool = False,
    defects4j_executable: str = "defects4j",
    result_directory: Optional[str] = None,
) -> Dict[str, object]:
    """Generate models, combinations, and JUnit suites for discovered targets."""
    selected_result_directory = result_directory or (
        "Result_Round2" if catalog_path is not None else "Result_Round1"
    )
    if selected_result_directory not in RESULT_DIRECTORIES:
        raise ValueError(
            "result_directory must be Result_Round1 or Result_Round2"
        )
    if catalog_path is not None and selected_result_directory != "Result_Round2":
        raise ValueError(
            "Catalog-loop results must use Result_Round2 to preserve readiness evidence"
        )
    result_root = output_root / selected_result_directory
    records: List[Dict[str, object]] = []
    generated_suites: List[str] = []

    catalog_issues: List[Dict[str, object]] = []
    catalog_loop_state_path: Optional[Path] = None
    if catalog_path is not None:
        resolved_targets, catalog_issues = resolve_catalog_targets(
            catalog_path, target_root
        )
        catalog_loop_state_path = (
            result_root / "catalog_loop_state.json"
        )
        catalog_loop_state_path.parent.mkdir(parents=True, exist_ok=True)
        catalog_loop_state_path.write_text(
            json.dumps(
                {
                    "started": True,
                    "status": "STARTED",
                    "catalog": str(catalog_path),
                },
                indent=2,
            ),
            encoding="utf-8",
        )
        target_sources = [
            (
                item.target.project,
                item.target.bug_id,
                item.target_directory,
                [item.source_file],
                item.target_class,
            )
            for item in resolved_targets
        ]
    else:
        target_sources = [
            (project, bug_id, directory, sorted(directory.glob("*.java")), None)
            for project, bug_id, directory in discover_targets(target_root)
        ]
    records.extend(catalog_issues)

    for project, bug_id, target_directory, java_files, expected_class in target_sources:
        if project_filter and project != project_filter:
            continue
        if bug_filter is not None and bug_id != bug_filter:
            continue

        target_key = "{}_{}b".format(project, bug_id)
        for java_file in java_files:
            try:
                metadata = parse_java_file(str(java_file))
            except ValueError as exc:
                records.append(
                    {
                        "project": project,
                        "bug_id": bug_id,
                        "source": str(java_file),
                        "status": "ANALYZE_ERROR",
                        "error": str(exc),
                    }
                )
                continue

            class_name = str(metadata["class"])
            package_name = str(metadata["package"])
            fully_qualified_class = (
                "{}.{}".format(package_name, class_name)
                if package_name
                else class_name
            )
            if expected_class and fully_qualified_class != expected_class:
                records.append(
                    {
                        "project": project,
                        "bug_id": bug_id,
                        "source": str(java_file),
                        "target_class": expected_class,
                        "actual_class": fully_qualified_class,
                        "status": "CATALOG_MISMATCH",
                        "reason": "Parsed class does not match catalog target_class",
                    }
                )
                continue
            ready_method_cases = []
            methods = metadata.get("methods", [])
            for method in methods if isinstance(methods, list) else []:
                if not isinstance(method, dict):
                    continue
                if not _matches_method_filter(
                    method, method_filter, signature_filter
                ):
                    continue
                method_id = _method_id(method)
                method_record = {
                    "project": project,
                    "bug_id": bug_id,
                    "class": class_name,
                    "method": method.get("name"),
                    "method_signature": _method_signature(method),
                    "method_id": method_id,
                }
                if method.get("static") is not True:
                    records.append(
                        dict(
                            method_record,
                            status="UNSUPPORTED",
                            reason="Only public static methods are supported",
                        )
                    )
                    continue
                if not method.get("parameters"):
                    records.append(
                        dict(
                            method_record,
                            status="UNSUPPORTED",
                            reason="A method without parameters has no pairwise factors",
                        )
                    )
                    continue

                model_dir = output_root / "Models" / target_key / class_name
                result_dir = result_root / target_key / class_name
                try:
                    semantic_override = find_semantic_override(
                        fully_qualified_class, method
                    )
                    factor_domains = (
                        semantic_override.factor_domains
                        if semantic_override
                        else _domains_for_method(method)
                    )
                    strategy = (
                        semantic_override.name
                        if semantic_override
                        else "generic_type_domains"
                    )
                    if len(factor_domains) < 2:
                        raise UnsupportedMethodError(
                            "Pairwise generation requires at least two factors"
                        )
                    generation_started = perf_counter()
                    factor_combinations = generate_pairwise(
                        factor_domains,
                        seed_combinations=(
                            semantic_override.seed_combinations
                            if semantic_override
                            else ()
                        ),
                    )
                    generation_seconds = perf_counter() - generation_started
                    coverage_report = verify_pair_coverage(
                        factor_domains, factor_combinations
                    )
                    if not coverage_report.complete:
                        raise ValueError(
                            "Native IPO output is missing {} required pairs".format(
                                len(coverage_report.missing_pairs)
                            )
                        )
                    concrete_combinations = (
                        [
                            semantic_override.materialize(combination)
                            for combination in factor_combinations
                        ]
                        if semantic_override
                        else factor_combinations
                    )

                    domains_path = model_dir / "{}_domains.json".format(method_id)
                    combinations_path = result_dir / "{}_combinations.tsv".format(
                        method_id
                    )
                    inputs_path = result_dir / "{}_inputs.tsv".format(method_id)
                    oracle_path = result_dir / "{}_oracle.json".format(method_id)
                    domains_path.parent.mkdir(parents=True, exist_ok=True)
                    domains_path.write_text(
                        json.dumps(factor_domains, indent=2), encoding="utf-8"
                    )
                    _write_combinations(
                        combinations_path,
                        list(factor_domains),
                        factor_combinations,
                    )
                    parameter_names = [
                        str(parameter["name"])
                        for parameter in method["parameters"]
                    ]
                    _write_combinations(
                        inputs_path,
                        parameter_names,
                        concrete_combinations,
                    )

                    cartesian_count = math.prod(
                        len(values) for values in factor_domains.values()
                    )
                    pairwise_count = len(factor_combinations)
                    unique_concrete_input_count = len(
                        {
                            tuple(combination.items())
                            for combination in concrete_combinations
                        }
                    )
                    reduction_percent = (
                        (1.0 - pairwise_count / cartesian_count) * 100.0
                        if cartesian_count
                        else 0.0
                    )
                    record = {
                        "project": project,
                        "bug_id": bug_id,
                        "class": class_name,
                        "method": method.get("name"),
                        "method_signature": _method_signature(method),
                        "method_id": method_id,
                        "status": "GENERATED",
                        "generation_backend": "ipo",
                        "strength": 2,
                        "strategy": strategy,
                        "seed_count": (
                            len(semantic_override.seed_combinations)
                            if semantic_override
                            else 0
                        ),
                        "factor_count": len(factor_domains),
                        "cartesian_count": cartesian_count,
                        "pairwise_count": pairwise_count,
                        "expected_pair_count": coverage_report.expected_pair_count,
                        "covered_pair_count": coverage_report.covered_pair_count,
                        "missing_pair_count": len(coverage_report.missing_pairs),
                        "pair_coverage_percent": coverage_report.coverage_percent,
                        "generation_seconds": round(generation_seconds, 6),
                        "unique_concrete_input_count": unique_concrete_input_count,
                        "duplicate_concrete_input_count": (
                            pairwise_count - unique_concrete_input_count
                        ),
                        "reduction_percent": round(reduction_percent, 4),
                        "domains": str(domains_path.relative_to(output_root)),
                        "combinations": str(combinations_path.relative_to(output_root)),
                        "inputs": str(inputs_path.relative_to(output_root)),
                    }
                    method_case = None
                    oracle_outcomes = None
                    oracle_matches = False
                    if oracle_path.exists():
                        oracle_outcomes = json.loads(
                            oracle_path.read_text(encoding="utf-8")
                        )
                        if not isinstance(oracle_outcomes, list):
                            raise ValueError("Oracle file must contain a list")
                        record["oracle"] = str(oracle_path.relative_to(output_root))
                        oracle_matches = len(oracle_outcomes) == len(
                            concrete_combinations
                        ) and all(
                            isinstance(outcome, dict)
                            and outcome.get("id") == index
                            and outcome.get("arguments") == dict(combination)
                            for index, (outcome, combination) in enumerate(
                                zip(oracle_outcomes, concrete_combinations), start=1
                            )
                        )
                        if oracle_matches:
                            record["oracle_status"] = "REUSED"
                            method_case = (
                                method,
                                concrete_combinations,
                                oracle_outcomes,
                            )
                        else:
                            record["oracle_status"] = "STALE"
                    else:
                        record["oracle_status"] = "MISSING"

                    if not oracle_matches and collect_oracles:
                        try:
                            oracle_outcomes = collect_fixed_oracle(
                                project=project,
                                bug_id=bug_id,
                                package_name=package_name,
                                target_class=class_name,
                                method=method,
                                combinations=concrete_combinations,
                                defects4j_executable=defects4j_executable,
                            )
                        except OracleCollectionError as exc:
                            record["status"] = "ORACLE_ERROR"
                            record["oracle_status"] = "ERROR"
                            record["error"] = str(exc)
                            records.append(record)
                            continue
                        for outcome, combination in zip(
                            oracle_outcomes, concrete_combinations
                        ):
                            outcome["arguments"] = dict(combination)
                        oracle_path.write_text(
                            json.dumps(oracle_outcomes, indent=2), encoding="utf-8"
                        )
                        record["oracle"] = str(oracle_path.relative_to(output_root))
                        record["oracle_status"] = "COLLECTED"
                        method_case = (
                            method,
                            concrete_combinations,
                            oracle_outcomes,
                        )
                    records.append(record)
                    if method_case is not None:
                        ready_method_cases.append((record, method_id, method_case))
                except NeedsSemanticModelError as exc:
                    records.append(
                        dict(
                            method_record,
                            status="NEEDS_SEMANTIC_MODEL",
                            reason=str(exc),
                        )
                    )
                except (UnsupportedTypeError, UnsupportedMethodError) as exc:
                    records.append(
                        dict(
                            method_record,
                            status="UNSUPPORTED",
                            reason=str(exc),
                        )
                    )
                except ValueError as exc:
                    records.append(
                        dict(
                            method_record,
                            status="GENERATION_ERROR",
                            error=str(exc),
                        )
                    )

            for record, method_id, method_case in ready_method_cases:
                test_class_name = "{}_{}_IPOTest".format(class_name, method_id)
                suite_path = (
                    output_root
                    / "TestCode"
                    / target_key
                    / "{}.java".format(test_class_name)
                )
                try:
                    suite = synthesize_junit_suite(
                        package_name,
                        class_name,
                        [method_case],
                        test_class_name=test_class_name,
                    )
                except ValueError as exc:
                    record["status"] = "SUITE_ERROR"
                    record["suite_status"] = "ERROR"
                    record["error"] = str(exc)
                    continue

                fully_qualified_test_class = (
                    "{}.{}".format(package_name, test_class_name)
                    if package_name
                    else test_class_name
                )
                if verify_suites:
                    try:
                        with tempfile.TemporaryDirectory(
                            prefix="ipo_suite_candidate_"
                        ) as temporary_directory:
                            candidate_path = Path(temporary_directory) / suite_path.name
                            candidate_path.write_text(suite, encoding="utf-8")
                            verification_output = verify_fixed_suite(
                                project=project,
                                version="{}f".format(bug_id),
                                suite_path=candidate_path,
                                test_class=fully_qualified_test_class,
                                defects4j_executable=defects4j_executable,
                            )
                    except (OracleCollectionError, OSError) as exc:
                        record["status"] = "SUITE_VERIFY_ERROR"
                        record["suite_status"] = "FAILED"
                        record["error"] = str(exc)
                        continue
                    suite_path.parent.mkdir(parents=True, exist_ok=True)
                    suite_path.write_text(suite, encoding="utf-8")
                    record["suite_status"] = "VERIFIED"
                    output_lines = [
                        line.strip()
                        for line in verification_output.splitlines()
                        if line.strip()
                    ]
                    record["verification_result"] = (
                        output_lines[-1] if output_lines else "PASSED"
                    )
                elif output_root.resolve() == IPO_ROOT.resolve():
                    record["suite_status"] = "VERIFICATION_REQUIRED"
                    continue
                else:
                    suite_path.parent.mkdir(parents=True, exist_ok=True)
                    suite_path.write_text(suite, encoding="utf-8")
                    record["suite_status"] = "GENERATED_UNVERIFIED"
                relative_suite = str(suite_path.relative_to(output_root))
                generated_suites.append(relative_suite)
                record["test_suite"] = relative_suite

    for record in records:
        required_identity = ("project", "bug_id", "class", "method_id")
        if not all(key in record for key in required_identity):
            continue
        if record.get("duplicate_concrete_input_count", 0):
            record["duplicate_explanation"] = (
                "Distinct abstract factor rows may materialize to the same concrete "
                "arguments; rows are retained to preserve verified abstract pair coverage"
            )
        record_path = (
            result_root
            / "{}_{}b".format(record["project"], record["bug_id"])
            / str(record["class"])
            / "{}_record.json".format(record["method_id"])
        )
        record["record_manifest"] = str(record_path.relative_to(output_root))
        record_path.parent.mkdir(parents=True, exist_ok=True)
        record_path.write_text(json.dumps(record, indent=2), encoding="utf-8")

    manifest = {
        "target_root": str(target_root),
        "catalog": str(catalog_path) if catalog_path else None,
        "result_directory": selected_result_directory,
        "collect_oracles": collect_oracles,
        "verify_suites": verify_suites,
        "generation_backend": "ipo",
        "strength": 2,
        "generated_method_count": sum(
            record.get("status") == "GENERATED" for record in records
        ),
        "generated_suite_count": len(generated_suites),
        "generated_suites": generated_suites,
        "records": records,
    }
    manifest_path = result_root / "batch_manifest.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    if catalog_loop_state_path is not None:
        catalog_loop_state_path.write_text(
            json.dumps(
                {
                    "started": True,
                    "status": "COMPLETED",
                    "catalog": str(catalog_path),
                    "generated_method_count": manifest["generated_method_count"],
                    "generated_suite_count": manifest["generated_suite_count"],
                },
                indent=2,
            ),
            encoding="utf-8",
        )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate IPO suites for Defects4J targets")
    parser.add_argument("--target-root", type=Path, default=PROJECT_ROOT / "target_benchmark")
    parser.add_argument("--output-root", type=Path, default=IPO_ROOT)
    parser.add_argument("--catalog", type=Path, help="Use an explicit target catalog")
    parser.add_argument(
        "--result-directory",
        choices=sorted(RESULT_DIRECTORIES),
        help="Result folder (catalog loops are always stored in Result_Round2)",
    )
    parser.add_argument(
        "--collect-oracles",
        action="store_true",
        help="Collect fixed-version outcomes and generate verified-input suites",
    )
    parser.add_argument(
        "--verify-suites",
        action="store_true",
        help="Run each suite on the fixed version before publishing TestCode",
    )
    parser.add_argument("--defects4j", default="defects4j")
    parser.add_argument("--project", help="Generate only one project")
    parser.add_argument("--bug", type=int, help="Generate only one bug ID")
    method_selection = parser.add_mutually_exclusive_group()
    method_selection.add_argument(
        "--method", help="Generate all overloads with this method name"
    )
    method_selection.add_argument(
        "--signature",
        help='Generate one exact overload, for example "min(int,int,int)"',
    )
    args = parser.parse_args()

    manifest = run_batch(
        target_root=args.target_root,
        output_root=args.output_root,
        project_filter=args.project,
        bug_filter=args.bug,
        method_filter=args.method,
        signature_filter=args.signature,
        catalog_path=args.catalog,
        collect_oracles=args.collect_oracles,
        verify_suites=args.verify_suites,
        defects4j_executable=args.defects4j,
        result_directory=args.result_directory,
    )
    print(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
