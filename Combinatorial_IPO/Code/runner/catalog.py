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
    modified_sources: Tuple[str, ...]
    trigger_tests: Tuple[str, ...]

    @property
    def target_key(self) -> str:
        return "{}_{}b".format(self.project, self.bug_id)


@dataclass(frozen=True)
class ResolvedCatalogTarget:
    target: CatalogTarget
    target_class: str
    target_directory: Path
    source_file: Path


def _non_empty_strings(entry: dict, key: str, index: int) -> Tuple[str, ...]:
    value = entry.get(key)
    if not isinstance(value, list) or not value:
        raise ValueError(
            "Catalog entry {} has an invalid {}".format(index, key)
        )
    if any(not isinstance(item, str) or not item for item in value):
        raise ValueError(
            "Catalog entry {} has an invalid {}".format(index, key)
        )
    if len(set(value)) != len(value):
        raise ValueError(
            "Catalog entry {} has duplicate {}".format(index, key)
        )
    return tuple(value)


def load_catalog(catalog_path: Path) -> List[CatalogTarget]:
    """Read a catalog and reject malformed or duplicate bug targets."""
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

        # Legacy catalogs named one class. New catalogs retain those fields for
        # other team tools while providing the complete Defects4J source/test lists.
        modified_sources = (
            _non_empty_strings(entry, "modified_sources", index)
            if "modified_sources" in entry
            else (entry["target_class"],)
        )
        trigger_tests = (
            _non_empty_strings(entry, "trigger_tests", index)
            if "trigger_tests" in entry
            else ((entry["trigger_test"],) if entry.get("trigger_test") else ())
        )
        if entry["target_class"] not in modified_sources:
            raise ValueError(
                "Catalog entry {} target_class is not in modified_sources".format(
                    index
                )
            )
        if entry["simple_name"] != entry["target_class"].rsplit(".", 1)[-1]:
            raise ValueError(
                "Catalog entry {} simple_name does not match target_class".format(
                    index
                )
            )

        target = CatalogTarget(
            project=entry["project"],
            bug_id=entry["bug_id"],
            directory=entry["dir"],
            target_class=entry["target_class"],
            simple_name=entry["simple_name"],
            modified_sources=modified_sources,
            trigger_tests=trigger_tests,
        )
        identity = (target.project, target.bug_id)
        if identity in seen:
            raise ValueError("Duplicate catalog target: {}-{}".format(*identity))
        seen.add(identity)
        targets.append(target)
    return targets


def resolve_catalog_targets(
    catalog_path: Path, target_root: Path
) -> Tuple[List[ResolvedCatalogTarget], List[dict]]:
    """Resolve every Defects4J-modified source without guessing replacements."""
    root = target_root.resolve()
    resolved: List[ResolvedCatalogTarget] = []
    issues: List[dict] = []

    for target in load_catalog(catalog_path):
        directory = (root / target.directory).resolve()
        if directory.parent != root:
            raise ValueError(
                "Catalog directory must be a direct child of target root: {}".format(
                    target.directory
                )
            )
        if not directory.is_dir():
            issues.append(
                {
                    "project": target.project,
                    "bug_id": target.bug_id,
                    "source_directory": target.directory,
                    "status": "CATALOG_MISMATCH",
                    "reason": "Target directory does not exist",
                }
            )
            continue

        for target_class in target.modified_sources:
            simple_name = target_class.rsplit(".", 1)[-1]
            source_file = directory / "{}.java".format(simple_name)
            issue_base = {
                "project": target.project,
                "bug_id": target.bug_id,
                "target_class": target_class,
                "source_directory": target.directory,
                "status": "CATALOG_MISMATCH",
            }
            if not source_file.is_file():
                available = sorted(path.name for path in directory.glob("*.java"))
                issues.append(
                    dict(
                        issue_base,
                        reason="Modified source file does not exist: {}".format(
                            source_file.name
                        ),
                        available_sources=available,
                    )
                )
                continue
            resolved.append(
                ResolvedCatalogTarget(target, target_class, directory, source_file)
            )

    return resolved, issues
