"""Audit catalog targets without generating combinations or running Defects4J."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Dict, List


CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from analyzer.class_planner import build_class_plan  # noqa: E402
from analyzer.java_parser import parse_java_file  # noqa: E402
from runner.catalog import load_catalog, resolve_catalog_targets  # noqa: E402


def audit_catalog(catalog_path: Path, target_root: Path) -> Dict[str, object]:
    """Return a non-generating feasibility report for every catalog entry."""
    catalog_targets = load_catalog(catalog_path)
    resolved_targets, issues = resolve_catalog_targets(catalog_path, target_root)
    targets: List[Dict[str, object]] = [
        dict(issue, status="DATA_ERROR") for issue in issues
    ]

    for resolved in resolved_targets:
        target = resolved.target
        try:
            metadata = parse_java_file(str(resolved.source_file))
        except ValueError as exc:
            targets.append(
                {
                    "project": target.project,
                    "bug_id": target.bug_id,
                    "target_class": resolved.target_class,
                    "source": str(resolved.source_file),
                    "status": "ANALYSIS_ERROR",
                    "reason": str(exc),
                }
            )
            continue

        actual_class = (
            "{}.{}".format(metadata["package"], metadata["class"])
            if metadata["package"]
            else str(metadata["class"])
        )
        if actual_class != resolved.target_class:
            targets.append(
                {
                    "project": target.project,
                    "bug_id": target.bug_id,
                    "target_class": resolved.target_class,
                    "actual_class": actual_class,
                    "source": str(resolved.source_file),
                    "status": "DATA_ERROR",
                    "reason": "Parsed class does not match catalog target_class",
                }
            )
            continue

        plan = build_class_plan(
            metadata=metadata,
            project=target.project,
            bug_id=target.bug_id,
            target_class=resolved.target_class,
            trigger_tests=target.trigger_tests,
            source_presence=resolved.source_presence,
            source_sha256=hashlib.sha256(resolved.source_file.read_bytes()).hexdigest(),
        )
        plan["source"] = str(resolved.source_file)
        targets.append(plan)

    target_counts = Counter(str(target["status"]) for target in targets)
    callable_counts: Counter = Counter()
    for target in targets:
        callable_counts.update(target.get("callable_status_counts", {}))
    expected_source_count = sum(len(target.modified_sources) for target in catalog_targets)
    return {
        "catalog": str(catalog_path),
        "target_root": str(target_root),
        "generation_performed": False,
        "target_count": expected_source_count,
        "bug_target_count": len(catalog_targets),
        "source_target_count": len(targets),
        "inventory_complete": len(targets) == expected_source_count,
        "target_status_counts": dict(sorted(target_counts.items())),
        "callable_status_counts": dict(sorted(callable_counts.items())),
        "method_status_counts": dict(sorted(callable_counts.items())),
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
                    "bug_target_count": report["bug_target_count"],
                    "source_target_count": report["source_target_count"],
                    "target_status_counts": report["target_status_counts"],
                    "callable_status_counts": report["callable_status_counts"],
                },
                indent=2,
            )
        )
    else:
        print(rendered)


if __name__ == "__main__":
    main()
