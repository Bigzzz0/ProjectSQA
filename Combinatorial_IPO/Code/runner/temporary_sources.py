"""Read buggy/fixed Defects4J sources from disposable checkouts only."""

from __future__ import annotations

import subprocess
import tempfile
import re
from contextlib import contextmanager
from pathlib import Path
from typing import Dict, Iterator, Mapping, Sequence


class SourceCheckoutError(RuntimeError):
    pass


def _run(command: Sequence[str], cwd: Path) -> str:
    result = subprocess.run(
        list(command), cwd=str(cwd), text=True, stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT, check=False,
    )
    if result.returncode:
        raise SourceCheckoutError(
            "Command failed ({}):\n{}".format(" ".join(command), result.stdout)
        )
    return result.stdout.strip()


def _export(defects4j: str, checkout: Path, property_name: str) -> Path:
    value = _run([defects4j, "export", "-p", property_name], checkout)
    if not value:
        raise SourceCheckoutError("Defects4J export returned no {}".format(property_name))
    return checkout / value.splitlines()[-1].strip()


def _class_path(fqcn: str) -> Path:
    return Path(*fqcn.split(".")).with_suffix(".java")


def _trigger_path(trigger: str) -> Path:
    class_name = trigger.split("::", 1)[0].split("$", 1)[0]
    return _class_path(class_name)


def _trigger_method_body(source: str, trigger: str) -> str:
    method_name = trigger.split("::", 1)[-1]
    match = re.search(
        r"\b" + re.escape(method_name) + r"\s*\([^)]*\)\s*(?:throws\s+[^\{]+)?\{",
        source,
    )
    if not match:
        return ""
    opening = source.find("{", match.start(), match.end())
    depth = 0
    for index in range(opening, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[opening + 1:index]
    return ""


@contextmanager
def checkout_bug_sources(
    project: str,
    bug_id: int,
    modified_sources: Sequence[str],
    trigger_tests: Sequence[str],
    defects4j_executable: str = "defects4j",
) -> Iterator[Mapping[str, object]]:
    """Yield source text from b/f checkouts and delete both when finished."""
    with tempfile.TemporaryDirectory(prefix="ipo_all_class_{}_{}_".format(project, bug_id)) as name:
        root = Path(name)
        checkouts: Dict[str, Path] = {}
        source_roots: Dict[str, Path] = {}
        test_roots: Dict[str, Path] = {}
        for version in ("b", "f"):
            checkout = root / version
            _run(
                [defects4j_executable, "checkout", "-p", project, "-v", "{}{}".format(bug_id, version), "-w", str(checkout)],
                root,
            )
            checkouts[version] = checkout
            source_roots[version] = _export(defects4j_executable, checkout, "dir.src.classes")
            test_roots[version] = _export(defects4j_executable, checkout, "dir.src.tests")

        classes: Dict[str, Dict[str, object]] = {}
        for fqcn in modified_sources:
            relative = _class_path(fqcn)
            buggy = source_roots["b"] / relative
            fixed = source_roots["f"] / relative
            classes[fqcn] = {
                "buggy_path": buggy if buggy.is_file() else None,
                "fixed_path": fixed if fixed.is_file() else None,
                "buggy_source": buggy.read_text(encoding="utf-8", errors="ignore") if buggy.is_file() else "",
                "fixed_source": fixed.read_text(encoding="utf-8", errors="ignore") if fixed.is_file() else "",
                "source_presence": (
                    "PRESENT_IN_BOTH" if buggy.is_file() and fixed.is_file()
                    else "ADDED_IN_FIXED" if fixed.is_file()
                    else "DELETED_IN_FIXED" if buggy.is_file()
                    else "EXTRACTION_ERROR"
                ),
            }
        trigger_sources = []
        for trigger in trigger_tests:
            relative = _trigger_path(trigger)
            candidates = (test_roots["f"] / relative, test_roots["b"] / relative)
            source = next((path for path in candidates if path.is_file()), None)
            if source is not None:
                trigger_sources.append(
                    _trigger_method_body(
                        source.read_text(encoding="utf-8", errors="ignore"), trigger
                    )
                )
        yield {"classes": classes, "trigger_sources": trigger_sources}
