"""Build deterministic, evidence-led class-level IPO feasibility plans."""

from __future__ import annotations

import hashlib
import json
from collections import Counter
from typing import Dict, List, Mapping, Optional, Sequence

from analyzer.java_parser import method_signature
from domain.adapter_registry import domains_for_parameters, stable_observer_for_type
from domain.value_generator import NeedsSemanticModelError


TERMINAL_STATUSES = {
    "AUTO_READY",
    "NEEDS_ADAPTER",
    "NEEDS_ENTRY_POINT",
    "NOT_PAIRWISE_APPLICABLE",
    "DATA_ERROR",
    "ANALYSIS_ERROR",
}


def _parameter_model(parameters: Sequence[Mapping[str, object]]) -> Dict[str, object]:
    try:
        domains, adapters, cleanup = domains_for_parameters(list(parameters))
        return {
            "domains": domains,
            "adapters": adapters,
            "cleanup_required": cleanup,
            "missing": [],
        }
    except NeedsSemanticModelError:
        missing: List[str] = []
        for parameter in parameters:
            try:
                domains_for_parameters([parameter])
            except NeedsSemanticModelError:
                value = str(parameter.get("type", ""))
                if value and value not in missing:
                    missing.append(value)
        return {"domains": {}, "adapters": {}, "cleanup_required": False, "missing": missing}


from domain.construction_planner import plan_receiver


def _receiver_strategy(
    metadata: Mapping[str, object], target_class: str
) -> Optional[Dict[str, object]]:
    plan = plan_receiver(metadata, target_class)
    if plan is None:
        return None
    return {
        "kind": plan.kind if plan.kind != "constructor" else (
            "implicit_zero_arg_constructor" if plan.provenance == "implicit_zero_arg"
            else ("zero_arg_constructor" if not plan.factor_domains else "factorized_constructor")
        ),
        "expression_template": plan.expression,
        "factor_domains": dict(plan.factor_domains),
        "adapters": dict(plan.adapters),
        "cleanup_required": plan.cleanup_required,
        "provenance": plan.provenance,
    }


def _has_unambiguous_evidence(records: Sequence[Mapping[str, object]]) -> bool:

    return any(record.get("confidence") != "AMBIGUOUS" for record in records)


def plan_callable(
    callable_metadata: Mapping[str, object],
    receiver_strategy: Optional[Mapping[str, object]],
    evidence: Sequence[Mapping[str, object]],
    require_evidence: bool,
) -> Dict[str, object]:
    parameters = callable_metadata.get("parameters", [])
    record: Dict[str, object] = {
        "kind": callable_metadata.get("kind", "method"),
        "name": callable_metadata.get("name"),
        "signature": method_signature(callable_metadata),
        "visibility": callable_metadata.get("visibility", "package"),
        "static": bool(callable_metadata.get("static")),
        "return_type": callable_metadata.get("return_type"),
        "parameters": parameters,
        "evidence": list(evidence),
    }
    if require_evidence and (not evidence or not _has_unambiguous_evidence(evidence)):
        record.update(
            status="NEEDS_ENTRY_POINT",
            reason="No unambiguous diff or triggering-test evidence selects this callable",
            missing_adapters=["entry_point_evidence"],
        )
        return record
    if record["visibility"] not in {"public", "protected"}:
        record.update(
            status="NEEDS_ENTRY_POINT",
            reason="Non-public changed callable requires a defect-related public entry point",
            missing_adapters=["public_entry_point"],
        )
        return record
    if not isinstance(parameters, list):
        record.update(status="ANALYSIS_ERROR", reason="Invalid parameter metadata")
        return record
    parameter_model = _parameter_model(parameters)
    if parameter_model["missing"]:
        record.update(
            status="NEEDS_ADAPTER",
            reason="One or more parameter types need semantic factories",
            missing_adapters=parameter_model["missing"],
        )
        return record
    if record["kind"] == "constructor":
        record.update(
            status="NEEDS_ADAPTER",
            reason="Constructor result needs a stable object observer or invariant",
            missing_adapters=["object_observer"],
        )
        return record
    if not record["static"] and receiver_strategy is None:
        record.update(
            status="NEEDS_ADAPTER",
            reason="Instance method needs a deterministic receiver factory",
            missing_adapters=["receiver_factory"],
        )
        return record
    if callable_metadata.get("return_type") == "void":
        record.update(
            status="NEEDS_ADAPTER",
            reason="Void/stateful action needs a deterministic state observer",
            missing_adapters=["state_observer"],
        )
        return record
    observer = stable_observer_for_type(callable_metadata.get("return_type"))
    if observer is None:
        record.update(
            status="NEEDS_ADAPTER",
            reason="Return value needs a stable observer instead of arbitrary toString",
            missing_adapters=["object_observer:{}".format(callable_metadata.get("return_type"))],
        )
        return record

    factor_domains = dict(parameter_model["domains"])
    adapters = dict(parameter_model["adapters"])
    cleanup_required = bool(parameter_model["cleanup_required"])
    if not record["static"] and receiver_strategy is not None:
        factor_domains = {**receiver_strategy.get("factor_domains", {}), **factor_domains}
        adapters = {**receiver_strategy.get("adapters", {}), **adapters}
        cleanup_required = cleanup_required or bool(receiver_strategy.get("cleanup_required"))
        record["receiver_strategy"] = dict(receiver_strategy)
    if len(factor_domains) < 2:
        record.update(
            status="NOT_PAIRWISE_APPLICABLE",
            reason="Pairwise IPO requires at least two meaningful factors",
            recommended_test_type="example_based",
        )
        return record
    record.update(
        status="AUTO_READY",
        invocation_kind="static" if record["static"] else "instance",
        factor_domains=factor_domains,
        factor_names=list(factor_domains),
        adapters=adapters,
        observer=observer,
        cleanup_required=cleanup_required,
    )
    return record


def build_class_plan(
    metadata: Mapping[str, object],
    project: str,
    bug_id: int,
    target_class: str,
    trigger_tests: Sequence[str],
    source_presence: str,
    source_sha256: str = "",
    callable_evidence: Optional[Mapping[str, Sequence[Mapping[str, object]]]] = None,
    require_evidence: bool = False,
) -> Dict[str, object]:
    receiver = _receiver_strategy(metadata, target_class)
    callables: List[Mapping[str, object]] = []
    for key in ("constructors", "methods"):
        values = metadata.get(key, [])
        if isinstance(values, list):
            callables.extend(item for item in values if isinstance(item, dict))
    evidence_map = callable_evidence or {}
    callable_plans = [
        plan_callable(
            item,
            receiver,
            evidence_map.get(method_signature(item), []),
            require_evidence,
        )
        for item in callables
    ]
    counts = Counter(str(item["status"]) for item in callable_plans)
    precedence = (
        "AUTO_READY",
        "NEEDS_ADAPTER",
        "NEEDS_ENTRY_POINT",
        "ANALYSIS_ERROR",
        "NOT_PAIRWISE_APPLICABLE",
    )
    status = next((candidate for candidate in precedence if counts[candidate]), "NOT_PAIRWISE_APPLICABLE")
    missing_adapters = sorted(
        {
            adapter
            for item in callable_plans
            for adapter in item.get("missing_adapters", [])
        }
    )
    plan: Dict[str, object] = {
        "schema_version": 2,
        "project": project,
        "bug_id": bug_id,
        "target_class": target_class,
        "source_presence": source_presence,
        "source_sha256": source_sha256,
        "status": status,
        "receiver_strategy": receiver,
        "trigger_tests": list(trigger_tests),
        "callable_status_counts": dict(sorted(counts.items())),
        "missing_adapters": missing_adapters,
        "callables": callable_plans,
    }
    digest_payload = json.dumps(
        plan, sort_keys=True, ensure_ascii=False, separators=(",", ":")
    ).encode("utf-8")
    plan["audit_hash"] = hashlib.sha256(digest_payload).hexdigest()
    plan["input_hash"] = plan["audit_hash"]
    return plan
