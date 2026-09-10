"""Load a reproducible experiment scope independently from the IPO engine."""

from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Mapping, Sequence, Tuple

from runner.catalog import CatalogTarget, load_catalog


SHA256 = re.compile(r"^[0-9a-f]{64}$")


@dataclass(frozen=True)
class ExperimentSpec:
    experiment_id: str
    strength: int
    target_keys: Tuple[str, ...]
    expected_target_count: int
    expected_modified_source_count: int
    expected_triggering_test_count: int
    selected_catalog_sha256: str
    scenario_set_sha256: str
    source_path: Path


@dataclass(frozen=True)
class ResolvedExperiment:
    spec: ExperimentSpec
    targets: Tuple[CatalogTarget, ...]
    scenario_paths: Mapping[str, Path]
    selected_catalog_sha256: str
    scenario_set_sha256: str


def _canonical_json(value: object) -> bytes:
    return json.dumps(
        value, ensure_ascii=False, sort_keys=True, separators=(",", ":")
    ).encode("utf-8")


def selected_catalog_fingerprint(targets: Sequence[CatalogTarget]) -> str:
    """Hash only metadata selected by the experiment, not unrelated targets."""
    payload = [
        {
            "project": target.project,
            "bug_id": target.bug_id,
            "directory": target.directory,
            "target_class": target.target_class,
            "simple_name": target.simple_name,
            "modified_sources": list(target.modified_sources),
            "trigger_tests": list(target.trigger_tests),
        }
        for target in targets
    ]
    return hashlib.sha256(_canonical_json(payload)).hexdigest()


def scenario_set_fingerprint(
    target_keys: Sequence[str], scenario_paths: Mapping[str, Path]
) -> str:
    """Hash the semantic JSON content of every selected scenario spec."""
    payload = [
        {
            "target": key,
            "scenario": json.loads(scenario_paths[key].read_text(encoding="utf-8")),
        }
        for key in target_keys
    ]
    return hashlib.sha256(_canonical_json(payload)).hexdigest()


def _positive_integer(raw: Mapping[str, object], key: str) -> int:
    value = raw.get(key)
    if not isinstance(value, int) or isinstance(value, bool) or value < 1:
        raise ValueError("Experiment {} must be a positive integer".format(key))
    return value


def _fingerprint(raw: Mapping[str, object], key: str) -> str:
    value = raw.get(key)
    if not isinstance(value, str) or not SHA256.fullmatch(value):
        raise ValueError("Experiment {} must be a SHA-256 digest".format(key))
    return value


def load_experiment(path: Path) -> ExperimentSpec:
    """Read the explicit target list and its reproducibility locks."""
    raw = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(raw, dict) or raw.get("schema_version") != 1:
        raise ValueError("Experiment manifest must use schema_version 1")
    experiment_id = raw.get("experiment_id")
    if not isinstance(experiment_id, str) or not experiment_id.strip():
        raise ValueError("Experiment experiment_id must be a non-empty string")
    strength = _positive_integer(raw, "strength")
    if strength != 2:
        raise ValueError("This runner currently supports only 2-way experiments")
    targets = raw.get("targets")
    if (
        not isinstance(targets, list)
        or not targets
        or any(not isinstance(key, str) or not key for key in targets)
    ):
        raise ValueError("Experiment targets must be a non-empty string list")
    if len(set(targets)) != len(targets):
        raise ValueError("Experiment targets must be unique")
    expected = raw.get("expected_summary")
    fingerprints = raw.get("input_fingerprints")
    if not isinstance(expected, dict) or not isinstance(fingerprints, dict):
        raise ValueError("Experiment requires expected_summary and input_fingerprints")
    expected_target_count = _positive_integer(expected, "target_count")
    if expected_target_count != len(targets):
        raise ValueError("Experiment target_count does not match targets")
    return ExperimentSpec(
        experiment_id=experiment_id,
        strength=strength,
        target_keys=tuple(targets),
        expected_target_count=expected_target_count,
        expected_modified_source_count=_positive_integer(
            expected, "modified_source_count"
        ),
        expected_triggering_test_count=_positive_integer(
            expected, "triggering_test_count"
        ),
        selected_catalog_sha256=_fingerprint(
            fingerprints, "selected_catalog_sha256"
        ),
        scenario_set_sha256=_fingerprint(fingerprints, "scenario_set_sha256"),
        source_path=path,
    )


def resolve_experiment(
    experiment_path: Path, catalog_path: Path, scenario_root: Path
) -> ResolvedExperiment:
    """Select and validate only the targets named by an experiment manifest."""
    spec = load_experiment(experiment_path)
    catalog_by_key: Dict[str, CatalogTarget] = {
        target.target_key: target for target in load_catalog(catalog_path)
    }
    missing_targets = [key for key in spec.target_keys if key not in catalog_by_key]
    if missing_targets:
        raise ValueError(
            "Experiment targets missing from catalog: {}".format(missing_targets)
        )
    targets = tuple(catalog_by_key[key] for key in spec.target_keys)
    modified_source_count = sum(len(target.modified_sources) for target in targets)
    triggering_test_count = sum(len(target.trigger_tests) for target in targets)
    if modified_source_count != spec.expected_modified_source_count:
        raise ValueError(
            "Experiment modified source count changed: expected {}, got {}".format(
                spec.expected_modified_source_count, modified_source_count
            )
        )
    if triggering_test_count != spec.expected_triggering_test_count:
        raise ValueError(
            "Experiment triggering test count changed: expected {}, got {}".format(
                spec.expected_triggering_test_count, triggering_test_count
            )
        )

    available = {path.stem: path for path in scenario_root.glob("*.json")}
    missing_scenarios = [key for key in spec.target_keys if key not in available]
    if missing_scenarios:
        raise ValueError(
            "Experiment scenarios missing: {}".format(missing_scenarios)
        )
    scenario_paths = {key: available[key] for key in spec.target_keys}
    catalog_digest = selected_catalog_fingerprint(targets)
    scenario_digest = scenario_set_fingerprint(spec.target_keys, scenario_paths)
    if catalog_digest != spec.selected_catalog_sha256:
        raise ValueError("Selected catalog fingerprint does not match experiment")
    if scenario_digest != spec.scenario_set_sha256:
        raise ValueError("Scenario set fingerprint does not match experiment")
    return ResolvedExperiment(
        spec=spec,
        targets=targets,
        scenario_paths=scenario_paths,
        selected_catalog_sha256=catalog_digest,
        scenario_set_sha256=scenario_digest,
    )


def build_experiment_manifest(
    experiment_id: str,
    target_keys: Sequence[str],
    catalog_path: Path,
    scenario_root: Path,
) -> dict:
    """Build the reviewable lock file for a new approved experiment scope."""
    if not experiment_id.strip():
        raise ValueError("Experiment ID must be non-empty")
    if not target_keys or len(set(target_keys)) != len(target_keys):
        raise ValueError("Target keys must be non-empty and unique")
    catalog_by_key = {
        target.target_key: target for target in load_catalog(catalog_path)
    }
    missing_targets = [key for key in target_keys if key not in catalog_by_key]
    if missing_targets:
        raise ValueError(
            "Experiment targets missing from catalog: {}".format(missing_targets)
        )
    scenario_paths = {
        key: scenario_root / "{}.json".format(key) for key in target_keys
    }
    missing_scenarios = [
        key for key, path in scenario_paths.items() if not path.is_file()
    ]
    if missing_scenarios:
        raise ValueError(
            "Experiment scenarios missing: {}".format(missing_scenarios)
        )
    targets = [catalog_by_key[key] for key in target_keys]
    return {
        "schema_version": 1,
        "experiment_id": experiment_id,
        "strength": 2,
        "targets": list(target_keys),
        "expected_summary": {
            "target_count": len(targets),
            "modified_source_count": sum(
                len(target.modified_sources) for target in targets
            ),
            "triggering_test_count": sum(
                len(target.trigger_tests) for target in targets
            ),
        },
        "input_fingerprints": {
            "selected_catalog_sha256": selected_catalog_fingerprint(targets),
            "scenario_set_sha256": scenario_set_fingerprint(
                target_keys, scenario_paths
            ),
        },
    }
