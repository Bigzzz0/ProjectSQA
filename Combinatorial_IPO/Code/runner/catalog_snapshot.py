"""Create a deterministic, IPO-owned snapshot of the shared Defects4J catalog."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Dict, List, Mapping

from runner.generate_catalog import catalog_fingerprint, normalize_all_bugs_catalog


SNAPSHOT_SCHEMA_VERSION = 1


def file_sha256(path: Path) -> str:
    content = path.read_bytes().replace(b"\r\n", b"\n")
    return hashlib.sha256(content).hexdigest()


def _normalizer_sha256() -> str:
    paths = (Path(__file__), Path(__file__).with_name("generate_catalog.py"))
    digest = hashlib.sha256()
    for path in paths:
        digest.update(path.name.encode("utf-8"))
        digest.update(path.read_bytes())
    return digest.hexdigest()


def build_snapshot(master_catalog: Path) -> Dict[str, object]:
    """Normalize a shared catalog without modifying it or embedding timestamps."""
    raw = json.loads(master_catalog.read_text(encoding="utf-8"))
    if not isinstance(raw, list) or any(not isinstance(item, dict) for item in raw):
        raise ValueError("Shared all-bugs catalog root must be a list of objects")
    entries: List[Dict[str, object]] = normalize_all_bugs_catalog(raw)
    summary = {
        "bug_target_count": len(entries),
        "modified_java_source_count": sum(
            len(item["modified_sources"]) for item in entries
        ),
        "modified_resource_count": sum(
            len(item["modified_resources"]) for item in entries
        ),
        "triggering_test_count": sum(len(item["trigger_tests"]) for item in entries),
    }
    return {
        "schema_version": SNAPSHOT_SCHEMA_VERSION,
        "kind": "ipo_all_modified_classes_catalog_snapshot",
        "source": {
            "path": "target_benchmark/all_bugs_catalog.json",
            "sha256": file_sha256(master_catalog),
        },
        "normalizer_sha256": _normalizer_sha256(),
        "catalog_sha256": catalog_fingerprint(entries),
        "summary": summary,
        "entries": entries,
    }


def write_snapshot(snapshot: Mapping[str, object], output: Path) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(snapshot, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    project_root = Path(__file__).resolve().parents[2]
    parser.add_argument(
        "--input",
        type=Path,
        default=project_root / "target_benchmark" / "all_bugs_catalog.json",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=(
            project_root
            / "Combinatorial_IPO"
            / "Configuration"
            / "catalogs"
            / "all-modified-classes.normalized.json"
        ),
    )
    args = parser.parse_args()
    before = file_sha256(args.input)
    snapshot = build_snapshot(args.input)
    if file_sha256(args.input) != before:
        raise RuntimeError("Shared catalog changed while building the IPO snapshot")
    write_snapshot(snapshot, args.output)
    print(json.dumps({**snapshot["summary"], "output": str(args.output)}, indent=2))


if __name__ == "__main__":
    main()
