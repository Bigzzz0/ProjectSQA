"""Audit catalog targets without generating combinations or running Defects4J."""

from __future__ import annotations

import argparse
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Dict, List, Mapping


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from analyzer.java_parser import method_signature, parse_java_file  # noqa: E402
from domain.semantic_overrides import find_semantic_override  # noqa: E402
from domain.value_generator import (  # noqa: E402
    NeedsSemanticModelError,
    UnsupportedTypeError,
    get_domain_for_type,
)
from runner.catalog import resolve_catalog_targets  # noqa: E402


def _audit_method(
    fully_qualified_class: str, method: Mapping[str, object]
) -> Dict[str, object]:
    record: Dict[str, object] = {
        "method": method.get("name"),
        "signature": method_signature(method),
    }
    if method.get("static") is not True:
        record.update(
            status="UNSUPPORTED",
            reason="Only public static methods are supported",
        )
        return record

    parameters = method.get("parameters")
    if not isinstance(parameters, list) or not parameters:
        record.update(
            status="UNSUPPORTED",
            reason="A method without parameters has no pairwise factors",
        )
        return record

    semantic_override = find_semantic_override(fully_qualified_class, method)
    try:
        domains = (
            semantic_override.factor_domains
            if semantic_override
            else {
                str(parameter["name"]): get_domain_for_type(
                    str(parameter["type"])
                )
                for parameter in parameters
            }
        )
    except NeedsSemanticModelError as exc:
        record.update(status="NEEDS_SEMANTIC_MODEL", reason=str(exc))
        return record
    except UnsupportedTypeError as exc:
        record.update(status="UNSUPPORTED", reason=str(exc))
        return record

    if len(domains) < 2:
        record.update(
            status="UNSUPPORTED",
            reason="Pairwise generation requires at least two factors",
        )
        return record

    record.update(
        status="CANDIDATE",
        strategy=(
            semantic_override.name if semantic_override else "generic_type_domains"
        ),
        factor_count=len(domains),
        parameter_types=[str(parameter["type"]) for parameter in parameters],
    )
    return record


def audit_catalog(catalog_path: Path, target_root: Path) -> Dict[str, object]:
    """Return a non-generating feasibility report for every catalog entry."""
    resolved_targets, issues = resolve_catalog_targets(catalog_path, target_root)
    targets: List[Dict[str, object]] = list(issues)

    for resolved in resolved_targets:
        target = resolved.target
        try:
            metadata = parse_java_file(str(resolved.source_file))
        except ValueError as exc:
            targets.append(
                {
                    "project": target.project,
                    "bug_id": target.bug_id,
                    "target_class": target.target_class,
                    "source": str(resolved.source_file),
                    "status": "ANALYZE_ERROR",
                    "reason": str(exc),
                }
            )
            continue

        actual_class = (
            "{}.{}".format(metadata["package"], metadata["class"])
            if metadata["package"]
            else str(metadata["class"])
        )
        if actual_class != target.target_class:
            targets.append(
                {
                    "project": target.project,
                    "bug_id": target.bug_id,
                    "target_class": target.target_class,
                    "actual_class": actual_class,
                    "source": str(resolved.source_file),
                    "status": "CATALOG_MISMATCH",
                    "reason": "Parsed class does not match catalog target_class",
                }
            )
            continue

        methods = [
            _audit_method(actual_class, method)
            for method in metadata.get("methods", [])
            if isinstance(method, dict)
        ]
        method_counts = Counter(str(method["status"]) for method in methods)
        targets.append(
            {
                "project": target.project,
                "bug_id": target.bug_id,
                "target_class": target.target_class,
                "source": str(resolved.source_file),
                "status": "AUDITED",
                "method_status_counts": dict(sorted(method_counts.items())),
                "methods": methods,
            }
        )

    target_counts = Counter(str(target["status"]) for target in targets)
    method_counts: Counter = Counter()
    for target in targets:
        method_counts.update(target.get("method_status_counts", {}))
    return {
        "catalog": str(catalog_path),
        "target_root": str(target_root),
        "generation_performed": False,
        "target_count": len(targets),
        "target_status_counts": dict(sorted(target_counts.items())),
        "method_status_counts": dict(sorted(method_counts.items())),
        "targets": targets,
    }


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Audit IPO target feasibility without generating tests"
    )
    parser.add_argument(
        "--catalog",
        required=True,
        type=Path,
        help="Explicit target catalog to audit",
    )
    parser.add_argument(
        "--target-root", type=Path, default=PROJECT_ROOT / "target_benchmark"
    )
    parser.add_argument("--output", type=Path, help="Optional JSON report path")
    parser.add_argument(
        "--summary-only",
        action="store_true",
        help="Print counts only while retaining the full optional output report",
    )
    args = parser.parse_args()

    report = audit_catalog(args.catalog, args.target_root)
    rendered = json.dumps(report, indent=2)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered, encoding="utf-8")
    if args.summary_only:
        print(
            json.dumps(
                {
                    "generation_performed": report["generation_performed"],
                    "target_count": report["target_count"],
                    "target_status_counts": report["target_status_counts"],
                    "method_status_counts": report["method_status_counts"],
                },
                indent=2,
            )
        )
    else:
        print(rendered)


if __name__ == "__main__":
    main()
