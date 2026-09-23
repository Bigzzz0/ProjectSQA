"""Evidence-led, resumable Native IPO pipeline for all modified Java classes."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from collections import Counter
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Dict, Iterable, List, Mapping, MutableMapping, Sequence

CODE_ROOT = Path(__file__).resolve().parents[1]
IPO_ROOT = CODE_ROOT.parent
PROJECT_ROOT = IPO_ROOT.parent
if str(CODE_ROOT) not in sys.path:
    sys.path.insert(0, str(CODE_ROOT))

from algorithm.ipo import generate_pairwise  # noqa: E402
from analyzer.class_planner import build_class_plan  # noqa: E402
from analyzer.defect_evidence import select_callable_evidence  # noqa: E402
from analyzer.java_parser import parse_java_file, parse_java_file_ast_preferred  # noqa: E402
from generator.junit_generator import synthesize_junit_suite  # noqa: E402
from oracle.fixed_version_oracle import OracleCollectionError, collect_fixed_oracle  # noqa: E402
from oracle.verify_suite import verify_suite  # noqa: E402
from runner.all_class_experiment import build_all_class_manifest  # noqa: E402
from runner.catalog import CatalogTarget, load_catalog  # noqa: E402
from runner.catalog_snapshot import file_sha256  # noqa: E402
from runner.temporary_sources import SourceCheckoutError, checkout_bug_sources  # noqa: E402
from verification.pair_coverage import verify_pair_coverage  # noqa: E402


ROUTED_STATUSES = {"NEEDS_ADAPTER", "NEEDS_ENTRY_POINT", "NOT_PAIRWISE_APPLICABLE", "DATA_ERROR", "ANALYSIS_ERROR"}
PUBLISH_ROOT_NAME = ""


def _canonical_hash(value: object) -> str:
    return hashlib.sha256(
        json.dumps(value, sort_keys=True, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    ).hexdigest()


def _code_hash(paths: Sequence[Path]) -> str:
    digest = hashlib.sha256()
    for path in paths:
        digest.update(path.relative_to(CODE_ROOT).as_posix().encode("utf-8"))
        digest.update(path.read_bytes())
    return digest.hexdigest()


def _atomic_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(
        mode="w", encoding="utf-8", dir=str(path.parent), delete=False, suffix=".tmp"
    ) as stream:
        json.dump(value, stream, indent=2, ensure_ascii=False, sort_keys=True)
        stream.write("\n")
        temporary = Path(stream.name)
    os.replace(temporary, path)


def _parse_text(source: str, suffix: str, target_class: str = "") -> Mapping[str, object]:
    with tempfile.TemporaryDirectory(prefix="ipo_parse_") as name:
        path = Path(name) / suffix
        path.write_text(source, encoding="utf-8")
        return parse_java_file_ast_preferred(str(path), target_class=target_class)


def audit_class(
    target: CatalogTarget,
    target_class: str,
    bundle: Mapping[str, object],
    require_evidence: bool = False,
) -> Dict[str, object]:
    class_sources = bundle["classes"][target_class]
    presence = str(class_sources["source_presence"])
    if presence == "EXTRACTION_ERROR":
        return {
            "schema_version": 2, "project": target.project, "bug_id": target.bug_id,
            "target_class": target_class, "source_presence": presence,
            "status": "DATA_ERROR", "reason": "Modified source is absent from buggy and fixed checkouts",
            "audit_hash": _canonical_hash([target.project, target.bug_id, target_class, presence]),
        }
    selected_source = str(class_sources["fixed_source"] or class_sources["buggy_source"])
    try:
        selected = _parse_text(selected_source, target_class.rsplit(".", 1)[-1] + ".java", target_class=target_class)
        buggy = (
            _parse_text(str(class_sources["buggy_source"]), "Buggy.java", target_class=target_class)
            if class_sources["buggy_source"] else None
        )
    except (ValueError, Exception) as exc:
        return {
            "schema_version": 2, "project": target.project, "bug_id": target.bug_id,
            "target_class": target_class, "source_presence": presence,
            "status": "ANALYSIS_ERROR", "reason": str(exc),
            "audit_hash": _canonical_hash([target.project, target.bug_id, target_class, str(exc)]),
        }
    actual = selected.get("fqcn") or ("{}.{}".format(selected["package"], selected["class"]) if selected["package"] else selected["class"])
    if actual != target_class and actual.replace("$", ".") != target_class.replace("$", "."):
        return {
            "schema_version": 2, "project": target.project, "bug_id": target.bug_id,
            "target_class": target_class, "actual_class": actual,
            "source_presence": presence, "status": "DATA_ERROR",
            "reason": "Parsed class does not match catalog target_class",
            "audit_hash": _canonical_hash([target.project, target.bug_id, target_class, actual]),
        }
    evidence = select_callable_evidence(
        selected,
        str(class_sources["fixed_source"] or selected_source),
        buggy,
        str(class_sources["buggy_source"]),
        tuple(bundle.get("trigger_sources", [])),
    )
    plan = build_class_plan(
        selected, target.project, target.bug_id, target_class,
        target.trigger_tests, presence,
        hashlib.sha256(selected_source.encode("utf-8")).hexdigest(),
        evidence, require_evidence,
    )
    if presence == "DELETED_IN_FIXED":
        plan["status"] = "NOT_PAIRWISE_APPLICABLE"
        plan["reason"] = "Class is absent from the fixed version; fixed-oracle publication is impossible"
        plan["recommended_test_type"] = "integration_or_regression"
        plan["audit_hash"] = _canonical_hash({key: value for key, value in plan.items() if key not in {"audit_hash", "input_hash"}})
        plan["input_hash"] = plan["audit_hash"]
    return plan


def select_canary(records: Sequence[Mapping[str, object]], target_count: int | None = None) -> List[Mapping[str, object]]:
    """Select a deterministic union by project, source presence, and adapter family, optionally filling up to target_count."""
    ready = sorted(
        (record for record in records if record.get("status") == "AUTO_READY"),
        key=lambda item: (str(item["project"]), int(item["bug_id"]), str(item["target_class"])),
    )
    selected: Dict[tuple, Mapping[str, object]] = {}
    covered_projects = set()
    covered_presence = set()
    covered_adapters = set()
    covered_receivers = set()

    for record in ready:
        identity = (record["project"], record["bug_id"], record["target_class"])
        adapters = {
            adapter
            for callable_plan in record.get("callables", [])
            if callable_plan.get("status") == "AUTO_READY"
            for adapter in callable_plan.get("adapters", {}).values()
        }
        receiver_kind = str((record.get("receiver_strategy") or {}).get("kind", "static"))
        adds = (
            record["project"] not in covered_projects
            or record.get("source_presence") not in covered_presence
            or receiver_kind not in covered_receivers
            or bool(adapters - covered_adapters)
        )
        if adds:
            selected[identity] = record
            covered_projects.add(record["project"])
            covered_presence.add(record.get("source_presence"))
            covered_adapters.update(adapters)
            covered_receivers.add(receiver_kind)

    if target_count is not None and len(selected) < target_count and len(selected) < len(ready):
        by_project: Dict[str, List[Mapping[str, object]]] = {}
        for record in ready:
            identity = (record["project"], record["bug_id"], record["target_class"])
            if identity not in selected:
                by_project.setdefault(str(record["project"]), []).append(record)
        projects = sorted(by_project.keys())
        idx = 0
        while len(selected) < target_count and any(by_project.values()):
            proj = projects[idx % len(projects)]
            if by_project[proj]:
                rec = by_project[proj].pop(0)
                selected[(rec["project"], rec["bug_id"], rec["target_class"])] = rec
            idx += 1

    chosen = sorted(
        selected.values(),
        key=lambda item: (str(item["project"]), int(item["bug_id"]), str(item["target_class"])),
    )
    if target_count is not None:
        return chosen[:target_count]
    return chosen


def _write_tsv(path: Path, factors: Sequence[str], rows: Sequence[Mapping[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=list(factors), delimiter="\t", lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def _stage_hashes(plan: Mapping[str, object], callable_plan: Mapping[str, object], rows: Sequence[Mapping[str, str]]) -> Dict[str, str]:
    model_hash = _canonical_hash(
        {
            "audit_hash": plan["audit_hash"], "signature": callable_plan["signature"],
            "domains": callable_plan["factor_domains"], "strength": 2,
            "ipo_code": _code_hash((CODE_ROOT / "algorithm" / "ipo.py", CODE_ROOT / "verification" / "pair_coverage.py")),
        }
    )
    oracle_hash = _canonical_hash(
        {
            "model_hash": model_hash, "rows": rows,
            "fixed_version": "{}f".format(plan["bug_id"]),
            "oracle_code": _code_hash((CODE_ROOT / "oracle" / "fixed_version_oracle.py",)),
        }
    )
    suite_hash = _canonical_hash(
        {
            "oracle_hash": oracle_hash,
            "code": _code_hash((CODE_ROOT / "generator" / "junit_generator.py", CODE_ROOT / "oracle" / "verify_suite.py")),
        }
    )
    return {"model_hash": model_hash, "oracle_hash": oracle_hash, "suite_hash": suite_hash}


def _generation_toolchain_hash() -> str:
    return _code_hash(
        (
            CODE_ROOT / "algorithm" / "ipo.py",
            CODE_ROOT / "verification" / "pair_coverage.py",
            CODE_ROOT / "domain" / "invocation.py",
            CODE_ROOT / "oracle" / "fixed_version_oracle.py",
            CODE_ROOT / "generator" / "junit_generator.py",
            CODE_ROOT / "oracle" / "verify_suite.py",
        )
    )


def _publish_paths(output_root: Path, plan: Mapping[str, object]) -> tuple[Path, str]:
    package_parts = str(plan["target_class"]).split(".")
    class_name = package_parts.pop()
    target_folder = "{}_{}b".format(plan["project"], plan["bug_id"])
    if PUBLISH_ROOT_NAME:
        relative = Path(PUBLISH_ROOT_NAME) / target_folder / Path(*package_parts) / "{}_IPOTest.java".format(class_name)
    else:
        relative = Path(target_folder) / Path(*package_parts) / "{}_IPOTest.java".format(class_name)
    return output_root / "TestCode" / relative, class_name


def generate_class(
    plan: Mapping[str, object],
    output_root: Path,
    experiment_id: str,
    defects4j_executable: str,
    resume: bool = False,
    verbose: bool = False,
) -> Dict[str, object]:
    ready = [item for item in plan.get("callables", []) if item.get("status") == "AUTO_READY"]
    identity = {key: plan[key] for key in ("project", "bug_id", "target_class", "audit_hash")}
    if not ready:
        return {**identity, "status": "SKIPPED_NOT_READY"}
    target_key = "{}_{}b".format(plan["project"], plan["bug_id"])
    class_name = str(plan["target_class"]).rsplit(".", 1)[-1]
    package_name = str(plan["target_class"]).rsplit(".", 1)[0] if "." in str(plan["target_class"]) else ""
    class_root = output_root / "Results" / "cache" / experiment_id / target_key / Path(*str(plan["target_class"]).split("."))
    method_cases = []
    method_records = []
    failed_methods = []
    suite_hash_inputs = []

    for c_idx, callable_plan in enumerate(ready, start=1):
        method_sig = str(callable_plan["signature"])
        method_id = re.sub(r"[^A-Za-z0-9_]+", "_", method_sig).strip("_")
        method_root = class_root / method_id
        if verbose:
            print(f"\n      [{c_idx}/{len(ready)}] {method_sig}", file=sys.stderr, flush=True, end="")
        try:
            domains = callable_plan["factor_domains"]
            rows = generate_pairwise(domains, allow_empty=True)
            coverage = verify_pair_coverage(domains, rows, allow_empty=True)
            if not coverage.complete:
                raise ValueError("Native IPO output misses {} required pairs".format(len(coverage.missing_pairs)))
            hashes = _stage_hashes(plan, callable_plan, rows)
            _atomic_json(method_root / "domains.json", domains)
            _write_tsv(method_root / "combinations.tsv", list(domains), rows)
            oracle_path = method_root / "oracle.json"
            method_record_path = method_root / "method_record.json"
            outcomes = None
            reused_oracle = False
            if resume and oracle_path.is_file() and method_record_path.is_file():
                previous_method = json.loads(method_record_path.read_text(encoding="utf-8"))
                if (
                    previous_method.get("oracle_hash") == hashes["oracle_hash"]
                    and previous_method.get("oracle_sha256") == file_sha256(oracle_path)
                ):
                    outcomes = json.loads(oracle_path.read_text(encoding="utf-8"))
                    reused_oracle = True
            if outcomes is None:
                if verbose:
                    print(" -> collecting oracle", file=sys.stderr, flush=True, end="")
                outcomes = collect_fixed_oracle(
                    project=str(plan["project"]), bug_id=int(plan["bug_id"]),
                    package_name=package_name, target_class=class_name,
                    method=callable_plan, combinations=rows,
                    defects4j_executable=defects4j_executable,
                )
                for outcome, arguments in zip(outcomes, rows):
                    outcome["arguments"] = dict(arguments)
                _atomic_json(oracle_path, outcomes)
            elif verbose:
                print(" -> cached oracle", file=sys.stderr, flush=True, end="")
            method_cases.append((callable_plan, rows, outcomes))
            method_record = {
                "signature": callable_plan["signature"], **hashes,
                "oracle_sha256": file_sha256(oracle_path), "reused_oracle": reused_oracle,
                "pairwise_count": len(rows),
                "expected_pair_count": coverage.expected_pair_count,
                "covered_pair_count": coverage.covered_pair_count,
                "pair_coverage_percent": coverage.coverage_percent,
            }
            method_records.append(method_record)
            _atomic_json(method_record_path, method_record)
            suite_hash_inputs.append(hashes["suite_hash"])
            if verbose:
                print(f" ({len(rows)} pairs OK)", file=sys.stderr, flush=True, end="")
        except (ValueError, OSError, OracleCollectionError, Exception) as exc:
            failed_methods.append({"signature": method_sig, "error": str(exc)})
            _atomic_json(method_root / "method_error.json", {"signature": method_sig, "error": str(exc)})
            if verbose:
                print(f" [SKIP: {str(exc)[:40]}]", file=sys.stderr, flush=True, end="")

    if not method_cases:
        record = {
            **identity,
            "status": "GENERATION_OR_VERIFICATION_ERROR",
            "error": "All {} candidate callables failed oracle collection".format(len(ready)),
            "methods": method_records,
            "failed_methods": failed_methods,
        }
        _atomic_json(class_root / "class_record.json", record)
        return record

    try:
        suite_hash = _canonical_hash(suite_hash_inputs)
        test_class = "{}_IPOTest".format(class_name)
        suite = synthesize_junit_suite(package_name, class_name, method_cases, test_class_name=test_class)
        destination, _ = _publish_paths(output_root, plan)
        with tempfile.TemporaryDirectory(prefix="ipo_all_class_suite_") as name:
            candidate = Path(name) / destination.name
            candidate.write_text(suite, encoding="utf-8")
            output = verify_suite(
                project=str(plan["project"]), version="{}f".format(plan["bug_id"]),
                suite_path=candidate,
                test_class="{}.{}".format(package_name, test_class) if package_name else test_class,
                defects4j_executable=defects4j_executable,
            )
            destination.parent.mkdir(parents=True, exist_ok=True)
            staged = destination.with_suffix(destination.suffix + ".tmp")
            staged.write_text(suite, encoding="utf-8")
            os.replace(staged, destination)
        record = {
            **identity, "status": "FIXED_VERIFIED", "generation_backend": "native_ipo",
            "strength": 2, "suite_hash": suite_hash,
            "generation_toolchain_hash": _generation_toolchain_hash(),
            "suite_path": str(destination.relative_to(output_root)).replace("\\", "/"),
            "suite_sha256": file_sha256(destination), "test_class": test_class,
            "methods": method_records,
            "failed_methods": failed_methods,
            "total_candidates": len(ready),
            "verified_method_count": len(method_cases),
            "verification_result": next((line.strip() for line in reversed(output.splitlines()) if line.strip()), "PASSED"),
        }
    except (ValueError, OSError, OracleCollectionError, Exception) as exc:
        record = {
            **identity,
            "status": "GENERATION_OR_VERIFICATION_ERROR",
            "error": str(exc),
            "methods": method_records,
            "failed_methods": failed_methods,
        }
    _atomic_json(class_root / "class_record.json", record)
    return record


def _routing_manifest(experiment_id: str, records: Sequence[Mapping[str, object]]) -> Dict[str, object]:
    routed = []
    class_backlog: Counter = Counter()
    callable_backlog: Counter = Counter()
    for record in records:
        if record.get("status") not in ROUTED_STATUSES:
            continue
        class_backlog.update(set(record.get("missing_adapters", [])))
        for callable_plan in record.get("callables", []):
            callable_backlog.update(set(callable_plan.get("missing_adapters", [])))
        reasons = sorted(
            {
                str(item.get("reason"))
                for item in record.get("callables", [])
                if item.get("reason")
            }
        )
        recommendations = sorted(
            {
                str(item.get("recommended_test_type"))
                for item in record.get("callables", [])
                if item.get("recommended_test_type")
            }
        )
        routed.append(
            {
                "project": record.get("project"), "bug_id": record.get("bug_id"),
                "target_class": record.get("target_class"), "status": record.get("status"),
                "reason": record.get("reason"), "callable_reasons": reasons,
                "missing_adapters": record.get("missing_adapters", []),
                "recommended_test_type": record.get("recommended_test_type") or (recommendations[0] if len(recommendations) == 1 else "manual_semantic_model"),
                "handoff_owner": "Member4_or_other_generator",
            }
        )
    backlog = [
        {
            "adapter": adapter,
            "unlockable_class_count": class_backlog[adapter],
            "unlockable_callable_count": callable_backlog[adapter],
        }
        for adapter in sorted(
            set(class_backlog) | set(callable_backlog),
            key=lambda item: (-class_backlog[item], -callable_backlog[item], item),
        )
    ]
    return {
        "schema_version": 1, "experiment_id": experiment_id,
        "adapter_backlog": backlog, "records": routed,
    }


def run_audit(
    catalog_path: Path, experiment_path: Path, output_root: Path,
    defects4j: str, project: str | None, bug: int | None, resume: bool,
    workers: int = 1, require_evidence: bool = False,
) -> Dict[str, object]:
    experiment = json.loads(experiment_path.read_text(encoding="utf-8"))
    current = build_all_class_manifest(catalog_path, str(experiment["experiment_id"]))
    if current != experiment:
        raise ValueError("Experiment manifest does not match catalog and planner inputs")
    root = output_root / "Results"
    plans_root = root / "plans" / str(experiment["experiment_id"])
    experiment_hash = _canonical_hash(current)
    inventory_path = root / "inventory.json"
    audit_path = root / "audit_manifest.json"
    previous_manifest_path = inventory_path if inventory_path.is_file() else audit_path
    previous_experiment_hash = None
    previous_records: Dict[tuple, Mapping[str, object]] = {}
    if previous_manifest_path.is_file():
        previous_manifest = json.loads(previous_manifest_path.read_text(encoding="utf-8"))
        previous_experiment_hash = previous_manifest.get("experiment_hash")
        previous_records = {
            (item["project"], item["bug_id"], item["target_class"]): item
            for item in previous_manifest.get("records", [])
        }
    records: List[Dict[str, object]] = []
    pending: List[CatalogTarget] = []
    for target in load_catalog(catalog_path):
        if project and target.project != project:
            continue
        if bug is not None and target.bug_id != bug:
            continue
        reusable = []
        if resume:
            for target_class in target.modified_sources:
                destination = plans_root / target.target_key / Path(*target_class.split(".")).with_suffix(".json")
                if destination.is_file():
                    reusable.append(json.loads(destination.read_text(encoding="utf-8")))
        if len(reusable) == len(target.modified_sources):
            records.extend(reusable)
            continue
        pending.append(target)

    if records:
        print(
            f"Audit: Fast-loaded {len(records)} existing class plans from disk cache.",
            file=sys.stderr, flush=True,
        )
    if pending:
        print(
            f"Audit: {len(pending)} bug targets require checkout & audit. Processing...",
            file=sys.stderr, flush=True,
        )

    def process_target(target: CatalogTarget) -> List[Dict[str, object]]:
        print(
            f"  -> Auditing {target.target_key} ({', '.join(target.modified_sources)})...",
            file=sys.stderr, flush=True,
        )
        target_records: List[Dict[str, object]] = []
        try:
            with checkout_bug_sources(
                target.project, target.bug_id, target.modified_sources,
                target.trigger_tests, defects4j,
            ) as bundle:
                for target_class in target.modified_sources:
                    destination = plans_root / target.target_key / Path(*target_class.split(".")).with_suffix(".json")
                    plan = audit_class(target, target_class, bundle, require_evidence=require_evidence)
                    _atomic_json(destination, plan)
                    target_records.append(plan)
        except SourceCheckoutError as exc:
            for target_class in target.modified_sources:
                target_records.append(
                    {
                        "schema_version": 2, "project": target.project, "bug_id": target.bug_id,
                        "target_class": target_class, "status": "DATA_ERROR", "reason": str(exc),
                        "audit_hash": _canonical_hash([target.project, target.bug_id, target_class, str(exc)]),
                    }
                )
        return target_records

    with ThreadPoolExecutor(max_workers=max(1, workers)) as executor:
        for target_records in executor.map(process_target, pending):
            records.extend(target_records)
    merged_records = dict(previous_records)
    for record in records:
        merged_records[(record["project"], record["bug_id"], record["target_class"])] = record
    records = sorted(
        merged_records.values(),
        key=lambda item: (str(item["project"]), int(item["bug_id"]), str(item["target_class"])),
    )
    counts = Counter(str(record["status"]) for record in records)
    expected_count = int(experiment["expected_summary"]["modified_java_source_count"])
    manifest = {
        "schema_version": 2, "experiment_id": experiment["experiment_id"], "mode": "AUDIT_ONLY",
        "experiment_hash": experiment_hash,
        "generation_performed": False, "filters": {"project": project, "bug_id": bug},
        "class_instance_count": len(records), "status_counts": dict(sorted(counts.items())),
        "expected_class_instance_count": expected_count,
        "inventory_complete": len(records) == expected_count == sum(counts.values()), "records": records,
    }
    _atomic_json(inventory_path, manifest)
    _atomic_json(audit_path, manifest)
    _atomic_json(root / "routing_manifest.json", _routing_manifest(str(experiment["experiment_id"]), records))
    return manifest


def _load_audit(output_root: Path, experiment_id: str) -> Dict[str, object]:
    candidates = [
        output_root / "Results" / "inventory.json",
        output_root / "Results" / "audit_manifest.json",
        output_root / "Result_Round1" / experiment_id / "audit_manifest.json",
    ]
    for path in candidates:
        if path.is_file():
            return json.loads(path.read_text(encoding="utf-8"))
    raise ValueError("Audit/inventory manifest is missing: searched {}".format([str(p) for p in candidates]))


def run_generation(
    output_root: Path, experiment_id: str, defects4j: str,
    mode: str, project: str | None, bug: int | None, resume: bool,
) -> Dict[str, object]:
    audit = _load_audit(output_root, experiment_id)
    records = [
        record for record in audit["records"]
        if (not project or record["project"] == project)
        and (bug is None or record["bug_id"] == bug)
    ]
    if mode == "canary":
        records = list(select_canary(records, target_count=40))
    publish_manifest_path = output_root / "Results" / "verified_suites_manifest.json"
    existing: MutableMapping[tuple, Mapping[str, object]] = {}
    if publish_manifest_path.is_file():
        previous = json.loads(publish_manifest_path.read_text(encoding="utf-8"))
        existing = {
            (item["project"], item["bug_id"], item["target_class"]): item
            for item in previous.get("records", [])
        }
    generation_manifest_path = output_root / "Results" / "generation_manifest.json"
    accumulated: MutableMapping[tuple, Mapping[str, object]] = {}
    if generation_manifest_path.is_file():
        previous_generation = json.loads(generation_manifest_path.read_text(encoding="utf-8"))
        accumulated = {
            (item["project"], item["bug_id"], item["target_class"]): item
            for item in previous_generation.get("records", [])
        }
    if mode == "canary":
        plan_hashes = {
            (item["project"], item["bug_id"], item["target_class"]): item.get("audit_hash")
            for item in records
        }
        accumulated = {
            key: item for key, item in accumulated.items()
            if key in plan_hashes and item.get("audit_hash") == plan_hashes[key]
        }
    total_records = len(records)
    print(
        f"\n=== Starting Native IPO Generation ({mode.upper()}) ===",
        file=sys.stderr, flush=True,
    )
    print(
        f"Total classes to consider: {total_records} | Already verified in manifest: {len(existing)}",
        file=sys.stderr, flush=True,
    )
    print("-" * 75, file=sys.stderr, flush=True)

    generated = []
    for idx, plan in enumerate(records, start=1):
        target_key = f"{plan['project']}_{plan['bug_id']}b"
        target_cls = str(plan["target_class"])
        pct = (idx / total_records) * 100.0

        key = (plan["project"], plan["bug_id"], plan["target_class"])
        previous = existing.get(key)
        if resume and previous and previous.get("status") == "FIXED_VERIFIED":
            suite = output_root / str(previous.get("suite_path", ""))
            if (
                previous.get("generation_toolchain_hash") == _generation_toolchain_hash()
                and previous.get("audit_hash") == plan.get("audit_hash")
                and suite.is_file()
                and file_sha256(suite) == previous.get("suite_sha256")
            ):
                print(
                    f"[{idx:>3}/{total_records}] ({pct:5.1f}%) {target_key:<14} {target_cls:<45} -> [SKIP (Already Verified)]",
                    file=sys.stderr, flush=True,
                )
                generated.append(previous)
                accumulated[key] = previous
                continue
            else:
                existing.pop(key, None)

        if plan.get("status") != "AUTO_READY":
            print(
                f"[{idx:>3}/{total_records}] ({pct:5.1f}%) {target_key:<14} {target_cls:<45} -> [SKIP ({plan.get('status')})]",
                file=sys.stderr, flush=True,
            )
            record = {k: plan[k] for k in ("project", "bug_id", "target_class", "audit_hash") if k in plan}
            record["status"] = "SKIPPED_NOT_READY"
            generated.append(record)
            accumulated[key] = record
            continue

        print(
            f"[{idx:>3}/{total_records}] ({pct:5.1f}%) {target_key:<14} {target_cls:<45} ...",
            file=sys.stderr, flush=True, end="",
        )
        record = generate_class(plan, output_root, experiment_id, defects4j, resume=resume, verbose=True)
        status = record.get("status", "UNKNOWN")
        v_count = record.get("verified_method_count", 0)
        tot_c = record.get("total_candidates", 0)

        generated.append(record)
        accumulated[key] = record

        if status == "FIXED_VERIFIED":
            existing[key] = record
            print(
                f"\n      => [OK] FIXED_VERIFIED ({v_count}/{tot_c} methods passed | Total verified: {len(existing)})",
                file=sys.stderr, flush=True,
            )
        else:
            existing.pop(key, None)
            err = record.get("error", "Generation or Verification Failed")
            print(
                f"\n      => [FAIL] {status} ({err[:60]})",
                file=sys.stderr, flush=True,
            )
            failures_log_path = output_root / "Results" / "logs" / "failures.log"
            try:
                failures_log_path.parent.mkdir(parents=True, exist_ok=True)
                with failures_log_path.open("a", encoding="utf-8") as f_log:
                    f_log.write(f"[{target_key}] {target_cls} -> {status}\n{err}\n{'-'*60}\n")
            except Exception:
                pass

        verified_manifest = {
            "schema_version": 1, "experiment_id": experiment_id,
            "generation_backend": "native_ipo", "strength": 2,
            "records": sorted(existing.values(), key=lambda item: (item["project"], item["bug_id"], item["target_class"])),
        }
        _atomic_json(publish_manifest_path, verified_manifest)

        all_records = sorted(
            accumulated.values(),
            key=lambda item: (str(item["project"]), int(item["bug_id"]), str(item["target_class"])),
        )
        counts = Counter(str(item["status"]) for item in all_records)
        manifest = {
            "schema_version": 1, "experiment_id": experiment_id, "mode": mode.upper(),
            "filters": {"project": project, "bug_id": bug},
            "class_instance_count": len(all_records), "processed_this_run": len(generated),
            "status_counts": dict(sorted(counts.items())),
            "records": all_records, "verified_manifest": str(publish_manifest_path.relative_to(output_root)).replace("\\", "/"),
        }
        _atomic_json(generation_manifest_path, manifest)

    all_records = sorted(
        accumulated.values(),
        key=lambda item: (str(item["project"]), int(item["bug_id"]), str(item["target_class"])),
    )
    counts = Counter(str(item["status"]) for item in all_records)
    manifest = {
        "schema_version": 1, "experiment_id": experiment_id, "mode": mode.upper(),
        "filters": {"project": project, "bug_id": bug},
        "class_instance_count": len(all_records), "processed_this_run": len(generated),
        "status_counts": dict(sorted(counts.items())),
        "records": all_records, "verified_manifest": str(publish_manifest_path.relative_to(output_root)).replace("\\", "/"),
    }
    if mode == "canary":
        canary_failures = [
            item for item in generated
            if item.get("status") != "FIXED_VERIFIED"
        ]
        manifest["canary_passed"] = len(canary_failures) == 0
        manifest["canary_failures"] = [
            {
                "project": item.get("project"),
                "bug_id": item.get("bug_id"),
                "target_class": item.get("target_class"),
                "status": item.get("status"),
                "error": item.get("error", ""),
            }
            for item in canary_failures
        ]
        if canary_failures:
            print(
                f"\n[ERROR] Canary gate failed: {len(canary_failures)} target(s) failed generation/verification.",
                file=sys.stderr, flush=True,
            )
            for fail in canary_failures:
                print(
                    f"  - {fail.get('project')}_{fail.get('bug_id')}b {fail.get('target_class')}: {fail.get('status')} ({str(fail.get('error', ''))[:80]})",
                    file=sys.stderr, flush=True,
                )
    _atomic_json(generation_manifest_path, manifest)
    return manifest


def validate_verified_manifest(output_root: Path) -> Dict[str, object]:
    candidates = [
        output_root / "Results" / "verified_suites_manifest.json",
        output_root / "TestCode" / "verified_suites_manifest.json",
        output_root / "TestCode" / "all-modified-classes" / "verified_suites_manifest.json",
    ]
    path = next((p for p in candidates if p.is_file()), None)
    if not path:
        return {"valid": False, "verified_suite_count": 0, "issues": ["verified_suites_manifest.json not found"]}
    manifest = json.loads(path.read_text(encoding="utf-8"))
    issues = []
    identities = set()
    for record in manifest.get("records", []):
        identity = (record.get("project"), record.get("bug_id"), record.get("target_class"))
        if identity in identities:
            issues.append("duplicate identity: {}".format(identity))
        identities.add(identity)
        suite = output_root / str(record.get("suite_path", ""))
        if record.get("status") != "FIXED_VERIFIED" or record.get("generation_backend") != "native_ipo":
            issues.append("unverified/non-native record: {}".format(identity))
        elif not suite.is_file() or file_sha256(suite) != record.get("suite_sha256"):
            issues.append("missing or changed suite: {}".format(identity))
        for method in record.get("methods", []):
            if method.get("pair_coverage_percent") != 100.0:
                issues.append("incomplete pair coverage: {}".format(identity))
    return {"valid": not issues, "verified_suite_count": len(identities), "issues": issues}


def show_summary(output_root: Path) -> Dict[str, object]:
    summary: Dict[str, object] = {}
    inv_path = output_root / "Results" / "inventory.json"
    if not inv_path.is_file():
        inv_path = output_root / "Results" / "audit_manifest.json"
    if inv_path.is_file():
        inv = json.loads(inv_path.read_text(encoding="utf-8"))
        summary["inventory"] = {
            "class_instance_count": inv.get("class_instance_count"),
            "status_counts": inv.get("status_counts"),
        }
    gen_path = output_root / "Results" / "generation_manifest.json"
    if gen_path.is_file():
        gen = json.loads(gen_path.read_text(encoding="utf-8"))
        summary["generation"] = {
            "class_instance_count": gen.get("class_instance_count"),
            "processed_this_run": gen.get("processed_this_run"),
            "status_counts": gen.get("status_counts"),
        }
    ver_path = output_root / "Results" / "verified_suites_manifest.json"
    if ver_path.is_file():
        ver = json.loads(ver_path.read_text(encoding="utf-8"))
        summary["verified_suites"] = {
            "verified_count": len(ver.get("records", [])),
        }
    val = validate_verified_manifest(output_root)
    summary["validation"] = val
    return summary


def preflight(catalog: Path, experiment: Path, defects4j: str) -> Dict[str, object]:
    checks = {
        "catalog_exists": catalog.is_file(), "experiment_exists": experiment.is_file(),
        "defects4j_available": shutil.which(defects4j) is not None,
    }
    if checks["defects4j_available"]:
        # Defects4J 2.x does not expose a ``version`` subcommand. ``pids`` is
        # read-only and exercises both the launcher and installed metadata.
        result = subprocess.run([defects4j, "pids"], text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
        checks["defects4j_metadata_ok"] = result.returncode == 0 and bool(result.stdout.strip())
    return {"ready": all(checks.values()), "checks": checks}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mode", required=True, choices=("preflight", "audit", "inventory", "plan", "canary", "generate", "validate", "summary"))
    parser.add_argument("--catalog", type=Path, default=IPO_ROOT / "Configuration" / "catalogs" / "all-modified-classes.normalized.json")
    parser.add_argument("--experiment", type=Path, default=IPO_ROOT / "Configuration" / "experiments" / "all-854-modified-classes.json")
    parser.add_argument("--output-root", type=Path, default=IPO_ROOT)
    parser.add_argument("--defects4j", default="defects4j")
    parser.add_argument("--project")
    parser.add_argument("--bug", type=int)
    parser.add_argument("--resume", action="store_true")
    parser.add_argument("--workers", type=int, default=1, help="Parallel audit checkouts (audit mode only)")
    parser.add_argument("--summary-only", action="store_true")
    args = parser.parse_args()
    experiment_id = "all-854-modified-classes"
    if args.experiment.is_file():
        experiment_id = str(json.loads(args.experiment.read_text(encoding="utf-8"))["experiment_id"])
    if args.mode == "preflight":
        result = preflight(args.catalog, args.experiment, args.defects4j)
    elif args.mode in {"audit", "inventory", "plan"}:
        result = run_audit(
            args.catalog, args.experiment, args.output_root, args.defects4j,
            args.project, args.bug, args.resume, args.workers,
        )
    elif args.mode in {"canary", "generate"}:
        result = run_generation(args.output_root, experiment_id, args.defects4j, args.mode, args.project, args.bug, args.resume)
    elif args.mode == "validate":
        result = validate_verified_manifest(args.output_root)
    elif args.mode == "summary":
        result = show_summary(args.output_root)
    else:
        raise ValueError("Unknown mode: {}".format(args.mode))
    if args.summary_only and "records" in result:
        result = {key: result[key] for key in result if key not in {"records"}}
    print(json.dumps(result, indent=2, ensure_ascii=False))
    if (
        result.get("ready") is False
        or result.get("valid") is False
        or (args.mode == "canary" and not result.get("canary_passed", False))
    ):
        raise SystemExit(1)


if __name__ == "__main__":
    main()
