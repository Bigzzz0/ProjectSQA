"""Load and validate the explicit Defects4J target catalog safely."""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple


@dataclass(frozen=True)
class CatalogTarget:
    project: str
    bug_id: int
    directory: str
    target_class: str
    simple_name: str

    @property
    def target_key(self) -> str:
        return "{}_{}b".format(self.project, self.bug_id)


@dataclass(frozen=True)
class ResolvedCatalogTarget:
    target: CatalogTarget
    target_directory: Path
    source_file: Path


def load_catalog(catalog_path: Path) -> List[CatalogTarget]:
    """Read a catalog and reject malformed or duplicate target entries."""
    raw_entries = json.loads(catalog_path.read_text(encoding="utf-8"))
    if not isinstance(raw_entries, list):
        raise ValueError("Catalog root must be a list")

    targets: List[CatalogTarget] = []
    seen = set()
    for index, entry in enumerate(raw_entries, start=1):
        if not isinstance(entry, dict):
            raise ValueError("Catalog entry {} must be an object".format(index))
        required = ("project", "bug_id", "dir", "target_class", "simple_name")
        missing = [key for key in required if key not in entry]
        if missing:
            raise ValueError(
                "Catalog entry {} is missing {}".format(index, ", ".join(missing))
            )
        if not isinstance(entry["project"], str) or not entry["project"]:
            raise ValueError("Catalog entry {} has an invalid project".format(index))
        if not isinstance(entry["bug_id"], int) or entry["bug_id"] < 1:
            raise ValueError("Catalog entry {} has an invalid bug_id".format(index))
        for key in ("dir", "target_class", "simple_name"):
            if not isinstance(entry[key], str) or not entry[key]:
                raise ValueError(
                    "Catalog entry {} has an invalid {}".format(index, key)
                )

        target = CatalogTarget(
            project=entry["project"],
            bug_id=entry["bug_id"],
            directory=entry["dir"],
            target_class=entry["target_class"],
            simple_name=entry["simple_name"],
        )
        identity = (target.project, target.bug_id)
        if identity in seen:
            raise ValueError(
                "Duplicate catalog target: {}-{}".format(*identity)
            )
        seen.add(identity)
        targets.append(target)
    return targets


def resolve_catalog_targets(
    catalog_path: Path, target_root: Path
) -> Tuple[List[ResolvedCatalogTarget], List[dict]]:
    """Resolve exact catalog sources and report mismatches without guessing."""
    root = target_root.resolve()
    resolved: List[ResolvedCatalogTarget] = []
    issues: List[dict] = []

    for target in load_catalog(catalog_path):
        directory = (root / target.directory).resolve()
        issue_base = {
            "project": target.project,
            "bug_id": target.bug_id,
            "target_class": target.target_class,
            "source_directory": target.directory,
            "status": "CATALOG_MISMATCH",
        }
        if directory.parent != root:
            raise ValueError(
                "Catalog directory must be a direct child of target root: {}".format(
                    target.directory
                )
            )
        if not directory.is_dir():
            issues.append(dict(issue_base, reason="Target directory does not exist"))
            continue

        source_file = directory / "{}.java".format(target.simple_name)
        if not source_file.is_file():
            available = sorted(path.name for path in directory.glob("*.java"))
            issues.append(
                dict(
                    issue_base,
                    reason="Catalog source file does not exist: {}".format(
                        source_file.name
                    ),
                    available_sources=available,
                )
            )
            continue
        resolved.append(ResolvedCatalogTarget(target, directory, source_file))

    return resolved, issues
