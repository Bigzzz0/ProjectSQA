"""Generate the experiment catalog from checked-in Defects4J metadata."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Dict, List, Optional


TARGET_DIRECTORY_PATTERN = re.compile(r"^(?P<project>.+)_(?P<bug_id>\d+)b$")
SEPARATOR_PATTERN = re.compile(r"^-{10,}\s*$")


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
