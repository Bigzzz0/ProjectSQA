"""Generate the experiment catalog from checked-in Defects4J metadata."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path
from typing import Dict, List, Optional


TARGET_DIRECTORY_PATTERN = re.compile(r"^(?P<project>.+)_(?P<bug_id>\d+)b$")
SEPARATOR_PATTERN = re.compile(r"^-{10,}\s*$")
TRIGGER_HEADER_PATTERN = re.compile(r"^(?:---\s*)?([\w.$]+::[\w$<>]+)\s*$")
RESOURCE_MARKERS = (".resources.", "src.main.resources.", "src/test/resources/")


def normalize_trigger_tests(lines: List[str]) -> List[str]:
    """Keep stable ``Class::method`` identifiers and discard stack traces."""
    triggers: List[str] = []
    seen = set()
    for line in lines:
        match = TRIGGER_HEADER_PATTERN.match(str(line).strip())
        if not match:
            continue
        trigger = match.group(1)
        if trigger not in seen:
            seen.add(trigger)
            triggers.append(trigger)
    return triggers


def classify_modified_entry(value: str) -> str:
    """Classify Defects4J modified entries without pretending resources are Java."""
    normalized = value.replace("\\", "/")
    if normalized.endswith((".java", ".class")):
        return "JAVA"
    if normalized.endswith((".txt", ".xml", ".properties", ".json")):
        return "RESOURCE"
    if any(marker in normalized for marker in RESOURCE_MARKERS):
        return "RESOURCE"
    return "JAVA"


def normalize_all_bugs_entry(entry: Dict[str, object]) -> Dict[str, object]:
    """Convert legacy all-bugs metadata into the catalog contract used by IPO."""
    project = str(entry["project"])
    bug_id = int(entry["bug_id"])
    raw_modified = entry.get("modified_sources", entry.get("target_classes", []))
    if not isinstance(raw_modified, list):
        raise ValueError("Modified entries must be a list for {}-{}".format(project, bug_id))
    java_sources = [str(item) for item in raw_modified if classify_modified_entry(str(item)) == "JAVA"]
    resources = [str(item) for item in raw_modified if classify_modified_entry(str(item)) == "RESOURCE"]
    if not java_sources:
        raise ValueError("Bug target has no modified Java sources: {}-{}".format(project, bug_id))
    raw_triggers = entry.get("trigger_tests", [])
    triggers = normalize_trigger_tests(raw_triggers if isinstance(raw_triggers, list) else [])
    primary = java_sources[0]
    normalized: Dict[str, object] = {
        "project": project,
        "bug_id": bug_id,
        "dir": "{}_{}b".format(project, bug_id),
        "target_class": primary,
        "simple_name": primary.rsplit(".", 1)[-1],
        "modified_sources": java_sources,
        "modified_resources": resources,
        "trigger_tests": triggers,
        "trigger_test": triggers[0] if triggers else None,
        "bug_report_id": entry.get("bug_report_id", entry.get("report_id", "N/A")),
        "bug_report_url": entry.get("bug_report_url", entry.get("report_url", "N/A")),
        "revision_buggy": entry.get("revision_buggy", "N/A"),
        "revision_fixed": entry.get("revision_fixed", "N/A"),
        "generated_from": "defects4j_metadata",
    }
    return normalized


def normalize_all_bugs_catalog(entries: List[Dict[str, object]]) -> List[Dict[str, object]]:
    """Normalize, sort, and reject duplicate Defects4J bug targets."""
    normalized = [normalize_all_bugs_entry(entry) for entry in entries]
    normalized.sort(key=lambda item: (str(item["project"]), int(item["bug_id"])))
    identities = [(item["project"], item["bug_id"]) for item in normalized]
    if len(identities) != len(set(identities)):
        raise ValueError("All-bugs catalog contains duplicate project/bug targets")
    return normalized


def catalog_fingerprint(entries: List[Dict[str, object]]) -> str:
    canonical = json.dumps(
        entries, ensure_ascii=False, sort_keys=True, separators=(",", ":")
    ).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def _required_match(pattern: str, text: str, label: str) -> str:
    match = re.search(pattern, text, flags=re.MULTILINE)
    if not match:
        raise ValueError("Missing {} in defects4j_info.txt".format(label))
    return match.group(1).strip()


def _bullet_section(text: str, heading: str) -> List[str]:
    lines = text.splitlines()
    try:
        start = next(
            index for index, line in enumerate(lines) if line.strip() == heading
        )
    except StopIteration:
        raise ValueError("Missing section: {}".format(heading))

    values: List[str] = []
    for line in lines[start + 1 :]:
        if SEPARATOR_PATTERN.fullmatch(line.strip()):
            break
        match = re.match(r"^\s+-\s+(.+?)\s*$", line)
        if match:
            values.append(match.group(1))
    if not values:
        raise ValueError("Section has no entries: {}".format(heading))
    return values


def parse_defects4j_info(text: str, directory_name: str) -> Dict[str, object]:
    """Parse one ``defects4j info`` snapshot into one reproducible bug target."""
    project = _required_match(r"^\s*Project ID:\s*(\S+)\s*$", text, "Project ID")
    summary = re.search(r"^Summary for Bug:\s*(\S+)-(\d+)\s*$", text, re.MULTILINE)
    if not summary:
        raise ValueError("Missing Summary for Bug in defects4j_info.txt")
    summary_project, bug_text = summary.groups()
    bug_id = int(bug_text)
    expected_directory = "{}_{}b".format(project, bug_id)
    if summary_project != project:
        raise ValueError("Project ID and Summary for Bug disagree")
    if directory_name != expected_directory:
        raise ValueError(
            "Directory {} does not match {}".format(directory_name, expected_directory)
        )

    trigger_tests = _bullet_section(text, "Root cause in triggering tests:")
    modified_sources = _bullet_section(text, "List of modified sources:")
    primary_class = modified_sources[0]
    return {
        "project": project,
        "bug_id": bug_id,
        "dir": directory_name,
        # Singular fields remain for existing Member 3/4 consumers.
        "target_class": primary_class,
        "simple_name": primary_class.rsplit(".", 1)[-1],
        "modified_sources": modified_sources,
        "trigger_test": trigger_tests[0],
        "trigger_tests": trigger_tests,
        "bug_report_id": _required_match(
            r"^Bug report id:\s*\r?\n([^\r\n]+)", text, "Bug report id"
        ),
        "bug_report_url": _required_match(
            r"^Bug report url:\s*\r?\n([^\r\n]+)", text, "Bug report url"
        ),
        "generated_from": "defects4j_info.txt",
    }


def generate_catalog(
    target_root: Path, existing_catalog: Optional[Path] = None
) -> List[Dict[str, object]]:
    """Build a deterministic catalog and verify every declared source exists."""
    preserved_domains = {}
    if existing_catalog and existing_catalog.is_file():
        previous = json.loads(existing_catalog.read_text(encoding="utf-8"))
        if isinstance(previous, list):
            preserved_domains = {
                (item.get("project"), item.get("bug_id")): item.get("domain")
                for item in previous
                if isinstance(item, dict) and item.get("domain")
            }

    catalog: List[Dict[str, object]] = []
    for directory in sorted(path for path in target_root.iterdir() if path.is_dir()):
        if not TARGET_DIRECTORY_PATTERN.fullmatch(directory.name):
            continue
        info_path = directory / "defects4j_info.txt"
        if not info_path.is_file():
            raise ValueError("Missing metadata: {}".format(info_path))
        entry = parse_defects4j_info(
            info_path.read_text(encoding="utf-8"), directory.name
        )
        for target_class in entry["modified_sources"]:
            source = directory / "{}.java".format(target_class.rsplit(".", 1)[-1])
            if not source.is_file():
                raise ValueError("Metadata source is not extracted: {}".format(source))
        domain = preserved_domains.get((entry["project"], entry["bug_id"]))
        if domain:
            entry["domain"] = domain
        catalog.append(entry)
    return catalog


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate the target catalog from defects4j_info.txt files"
    )
    parser.add_argument("--target-root", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument(
        "--preserve-domains-from",
        type=Path,
        help="Optional previous catalog used only for project-domain labels",
    )
    args = parser.parse_args()

    catalog = generate_catalog(args.target_root, args.preserve_domains_from)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(catalog, indent=2) + "\n", encoding="utf-8")
    print(
        json.dumps(
            {
                "bug_target_count": len(catalog),
                "modified_source_count": sum(
                    len(entry["modified_sources"]) for entry in catalog
                ),
                "output": str(args.output),
            },
            indent=2,
        )
    )


if __name__ == "__main__":
    main()
