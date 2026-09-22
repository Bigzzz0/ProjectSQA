"""Create a reproducible all-class experiment manifest."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Dict, List

CODE_ROOT = Path(__file__).resolve().parents[1]
PROJECT_ROOT = CODE_ROOT.parent.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from runner.catalog import CatalogTarget, load_catalog
AUDIT_INPUTS = (
    CODE_ROOT / "analyzer" / "java_parser.py",
    CODE_ROOT / "analyzer" / "defect_evidence.py",
    CODE_ROOT / "analyzer" / "class_planner.py",
    CODE_ROOT / "domain" / "construction_planner.py",
    CODE_ROOT / "domain" / "adapter_registry.py",
    CODE_ROOT / "runner" / "temporary_sources.py",
    CODE_ROOT / "runner" / "all_class_pipeline.py",
)
GENERATION_INPUTS = (
    CODE_ROOT / "algorithm" / "ipo.py",
    CODE_ROOT / "domain" / "invocation.py",
    CODE_ROOT / "generator" / "junit_generator.py",
    CODE_ROOT / "oracle" / "fixed_version_oracle.py",
    CODE_ROOT / "oracle" / "verify_suite.py",
    CODE_ROOT / "verification" / "pair_coverage.py",
)


def _files_fingerprint(paths: tuple[Path, ...]) -> str:
    digest = hashlib.sha256()
    for path in paths:
        digest.update(path.name.encode("utf-8"))
        digest.update(path.read_bytes().replace(b"\r\n", b"\n"))
    return digest.hexdigest()


def _catalog_fingerprint(targets: List[CatalogTarget]) -> str:
    payload = [
        {
            "project": target.project,
            "bug_id": target.bug_id,
            "directory": target.directory,
            "modified_sources": list(target.modified_sources),
            "modified_resources": list(target.modified_resources),
            "trigger_tests": list(target.trigger_tests),
        }
        for target in targets
    ]
    return hashlib.sha256(
        json.dumps(payload, sort_keys=True, separators=(",", ":")).encode("utf-8")
    ).hexdigest()


def build_all_class_manifest(catalog_path: Path, experiment_id: str) -> Dict[str, object]:
    targets: List[CatalogTarget] = load_catalog(catalog_path)
    target_keys = [target.target_key for target in targets]
    return {
        "schema_version": 2,
        "experiment_id": experiment_id,
        "strength": 2,
        "mode": "all_modified_java_classes",
        "targets": target_keys,
        "expected_summary": {
            "bug_target_count": len(targets),
            "modified_java_source_count": sum(len(target.modified_sources) for target in targets),
            "modified_resource_count": sum(len(target.modified_resources) for target in targets),
            "triggering_test_count": sum(len(target.trigger_tests) for target in targets),
        },
        "input_fingerprints": {
            "selected_catalog_sha256": _catalog_fingerprint(targets),
            "audit_toolchain_sha256": _files_fingerprint(AUDIT_INPUTS),
            "generation_toolchain_sha256": _files_fingerprint(GENERATION_INPUTS),
        },
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--catalog", type=Path, required=True)
    parser.add_argument("--experiment-id", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    manifest = build_all_class_manifest(args.catalog, args.experiment_id)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest["expected_summary"], indent=2))


if __name__ == "__main__":
    main()
