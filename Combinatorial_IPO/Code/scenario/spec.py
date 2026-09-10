"""Load and validate trusted, defect-focused IPO scenario specifications."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Mapping, Sequence, Tuple

from runner.catalog import CatalogTarget
from scenario.constraints import valid_combinations


TOKEN = re.compile(r"\{\{([A-Za-z_][A-Za-z0-9_]*)\}\}")
INVOCATION_KINDS = {"static", "constructor", "instance", "stateful", "harness"}


@dataclass(frozen=True)
class ScenarioSpec:
    scenario_id: str
    target_classes: Tuple[str, ...]
    entrypoint: str
    invocation_kind: str
    factors: Dict[str, Tuple[str, ...]]
    bindings: Dict[str, Dict[str, str]]
    constraints: Tuple[Mapping[str, object], ...]
    seeds: Tuple[Mapping[str, str], ...]
    body: str
    oracle: Mapping[str, object]
    evidence: Mapping[str, object]

    def valid_rows(self) -> List[Dict[str, str]]:
        return valid_combinations(self.factors, self.constraints)

    def materialize(self, row: Mapping[str, str]) -> str:
        def replace(match: re.Match) -> str:
            factor = match.group(1)
            if factor not in row:
                raise ValueError("Unknown body token: {}".format(factor))
            return self.bindings[factor][row[factor]]

        return TOKEN.sub(replace, self.body)


@dataclass(frozen=True)
class TargetScenarioPlan:
    project: str
    bug_id: int
    suite_package: str
    suite_class: str
    base_class: str
    imports: Tuple[str, ...]
    helpers: str
    scenarios: Tuple[ScenarioSpec, ...]
    source_path: Path

    @property
    def target_key(self) -> str:
        return "{}_{}b".format(self.project, self.bug_id)


def _require_string(raw: Mapping[str, object], key: str) -> str:
    value = raw.get(key)
    if not isinstance(value, str) or not value.strip():
        raise ValueError("{} must be a non-empty string".format(key))
    return value


def _load_scenario(raw: Mapping[str, object], catalog: CatalogTarget) -> ScenarioSpec:
    scenario_id = _require_string(raw, "id")
    invocation_kind = _require_string(raw, "invocation_kind")
    if invocation_kind not in INVOCATION_KINDS:
        raise ValueError("{} has unsupported invocation_kind".format(scenario_id))
    target_classes = raw.get("target_classes")
    if not isinstance(target_classes, list) or not target_classes:
        raise ValueError("{} must name target_classes".format(scenario_id))
    if not set(target_classes) <= set(catalog.modified_sources):
        raise ValueError("{} names a class outside modified_sources".format(scenario_id))

    raw_factors = raw.get("factors")
    if not isinstance(raw_factors, dict) or len(raw_factors) < 2:
        raise ValueError("{} requires at least two factors".format(scenario_id))
    factors: Dict[str, Tuple[str, ...]] = {}
    bindings: Dict[str, Dict[str, str]] = {}
    for factor, levels in raw_factors.items():
        if not isinstance(factor, str) or not factor or not isinstance(levels, dict):
            raise ValueError("{} has malformed factors".format(scenario_id))
        if len(levels) < 2:
            raise ValueError("{} factor {} requires at least two levels".format(scenario_id, factor))
        if any(not isinstance(level, str) or not isinstance(java, str) for level, java in levels.items()):
            raise ValueError("{} factor {} levels must map to Java strings".format(scenario_id, factor))
        factors[factor] = tuple(levels)
        bindings[factor] = dict(levels)

    body = _require_string(raw, "body")
    tokens = set(TOKEN.findall(body))
    if tokens != set(factors):
        raise ValueError(
            "{} body tokens must match factors; expected {}, got {}".format(
                scenario_id, sorted(factors), sorted(tokens)
            )
        )
    if not re.search(r"\b(?:assert\w*|fail)\s*\(", body):
        raise ValueError("{} body must contain an assertion".format(scenario_id))

    evidence = raw.get("evidence")
    if not isinstance(evidence, dict):
        raise ValueError("{} requires evidence".format(scenario_id))
    triggers = evidence.get("trigger_tests")
    if not isinstance(triggers, list) or not triggers:
        raise ValueError("{} requires triggering-test evidence".format(scenario_id))
    if not set(triggers) <= set(catalog.trigger_tests):
        raise ValueError("{} cites a trigger outside the catalog".format(scenario_id))
    _require_string(evidence, "patch_file")
    _require_string(evidence, "rationale")

    constraints = raw.get("constraints", [])
    seeds = raw.get("mandatory_seeds", [])
    if not isinstance(constraints, list) or not isinstance(seeds, list):
        raise ValueError("{} constraints and seeds must be lists".format(scenario_id))
    oracle = raw.get("oracle")
    if not isinstance(oracle, dict) or oracle.get("mode") not in {"invariant", "fixed"}:
        raise ValueError("{} requires invariant or fixed oracle metadata".format(scenario_id))

    spec = ScenarioSpec(
        scenario_id=scenario_id,
        target_classes=tuple(target_classes),
        entrypoint=_require_string(raw, "entrypoint"),
        invocation_kind=invocation_kind,
        factors=factors,
        bindings=bindings,
        constraints=tuple(constraints),
        seeds=tuple(seeds),
        body=body,
        oracle=oracle,
        evidence=evidence,
    )
    valid = spec.valid_rows()
    for seed in spec.seeds:
        if dict(seed) not in valid:
            raise ValueError("{} has a mandatory seed outside valid combinations".format(scenario_id))
    return spec


def load_target_plan(path: Path, catalog: CatalogTarget) -> TargetScenarioPlan:
    raw = json.loads(path.read_text(encoding="utf-8"))
    if raw.get("schema_version") != 1 or raw.get("status") != "APPROVED":
        raise ValueError("{} must be schema_version 1 and APPROVED".format(path.name))
    if raw.get("project") != catalog.project or raw.get("bug_id") != catalog.bug_id:
        raise ValueError("{} identity does not match catalog".format(path.name))
    suite = raw.get("suite")
    if not isinstance(suite, dict):
        raise ValueError("{} requires suite metadata".format(path.name))
    scenarios = raw.get("scenarios")
    if not isinstance(scenarios, list) or not scenarios:
        raise ValueError("{} requires at least one scenario".format(path.name))
    parsed = tuple(_load_scenario(item, catalog) for item in scenarios)
    ids = [item.scenario_id for item in parsed]
    if len(ids) != len(set(ids)):
        raise ValueError("{} contains duplicate scenario IDs".format(path.name))
    imports = suite.get("imports", [])
    if not isinstance(imports, list) or any(not isinstance(item, str) for item in imports):
        raise ValueError("{} suite imports must be strings".format(path.name))
    return TargetScenarioPlan(
        project=catalog.project,
        bug_id=catalog.bug_id,
        suite_package=_require_string(suite, "package"),
        suite_class=_require_string(suite, "class_name"),
        base_class=str(suite.get("base_class", "")),
        imports=tuple(imports),
        helpers=str(suite.get("helpers", "")),
        scenarios=parsed,
        source_path=path,
    )
