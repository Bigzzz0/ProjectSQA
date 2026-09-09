"""Run the automated IPO pipeline across extracted Defects4J targets."""

from __future__ import annotations

import argparse
import csv
import json
import math
import re
import sys
from pathlib import Path
from time import perf_counter
from typing import Dict, List, Mapping, Optional, Sequence, Tuple


IPO_ROOT = Path(__file__).resolve().parents[1]
PROJECT_ROOT = IPO_ROOT.parent
if str(IPO_ROOT) not in sys.path:
    sys.path.insert(0, str(IPO_ROOT))

from algorithm.ipo import generate_pairwise  # noqa: E402
from analyzer.java_parser import parse_java_file  # noqa: E402
from domain.semantic_overrides import find_semantic_override  # noqa: E402
from domain.value_generator import get_domain_for_type  # noqa: E402
from generator.junit_generator import synthesize_junit_suite  # noqa: E402
from verification.pair_coverage import verify_pair_coverage  # noqa: E402


TARGET_DIRECTORY_PATTERN = re.compile(r"^(?P<project>.+)_(?P<bug_id>\d+)b$")


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
    name = method.get("name")
    parameters = method.get("parameters")
    if not isinstance(name, str) or not isinstance(parameters, list):
        raise ValueError("Method name and parameters are required")

    parameter_types: List[str] = []
    for parameter in parameters:
        if not isinstance(parameter, dict) or not isinstance(
            parameter.get("type"), str
        ):
            raise ValueError("Every method parameter must have a type")
        parameter_types.append(re.sub(r"\s+", "", parameter["type"]))
    return "{}({})".format(name, ",".join(parameter_types))


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
) -> Dict[str, object]:
    """Generate models, combinations, and JUnit suites for discovered targets."""
    records: List[Dict[str, object]] = []
    generated_suites: List[str] = []

    for project, bug_id, target_directory in discover_targets(target_root):
        if project_filter and project != project_filter:
            continue
        if bug_filter is not None and bug_id != bug_filter:
            continue

        target_key = "{}_{}b".format(project, bug_id)
        for java_file in sorted(target_directory.glob("*.java")):
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
            method_cases = []
            class_records: List[Dict[str, object]] = []
            methods = metadata.get("methods", [])
            for method in methods if isinstance(methods, list) else []:
                if not isinstance(method, dict):
                    continue
                if not _matches_method_filter(
                    method, method_filter, signature_filter
                ):
                    continue
                if method.get("static") is not True or not method.get("parameters"):
                    continue

                method_id = _method_id(method)
                model_dir = output_root / "Models" / target_key / class_name
                result_dir = output_root / "Result_Round1" / target_key / class_name
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
                        "unique_concrete_input_count": len(
                            {
                                tuple(combination.items())
                                for combination in concrete_combinations
                            }
                        ),
                        "reduction_percent": round(reduction_percent, 4),
                        "domains": str(domains_path.relative_to(output_root)),
                        "combinations": str(combinations_path.relative_to(output_root)),
                        "inputs": str(inputs_path.relative_to(output_root)),
                    }
                    method_case = None
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
                    records.append(record)
                    if method_case is not None:
                        class_records.append(record)
                        method_cases.append(method_case)
                except ValueError as exc:
                    records.append(
                        {
                            "project": project,
                            "bug_id": bug_id,
                            "class": class_name,
                            "method": method.get("name"),
                            "method_id": method_id,
                            "status": "GENERATION_ERROR",
                            "error": str(exc),
                        }
                    )

            if method_cases:
                suite = synthesize_junit_suite(
                    package_name, class_name, method_cases
                )
                suite_path = (
                    output_root
                    / "TestCode"
                    / target_key
                    / "{}_IPOTest.java".format(class_name)
                )
                suite_path.parent.mkdir(parents=True, exist_ok=True)
                suite_path.write_text(suite, encoding="utf-8")
                relative_suite = str(suite_path.relative_to(output_root))
                generated_suites.append(relative_suite)
                for record in class_records:
                    record["test_suite"] = relative_suite

    manifest = {
        "target_root": str(target_root),
        "generation_backend": "ipo",
        "strength": 2,
        "generated_method_count": sum(
            record.get("status") == "GENERATED" for record in records
        ),
        "generated_suite_count": len(generated_suites),
        "generated_suites": generated_suites,
        "records": records,
    }
    manifest_path = output_root / "Result_Round1" / "batch_manifest.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate IPO suites for Defects4J targets")
    parser.add_argument("--target-root", type=Path, default=PROJECT_ROOT / "target_benchmark")
    parser.add_argument("--output-root", type=Path, default=IPO_ROOT)
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
    )
    print(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
