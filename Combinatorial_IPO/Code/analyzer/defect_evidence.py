"""Select defect-related Java callables from source diffs and triggering tests."""

from __future__ import annotations

import difflib
import re
from collections import Counter
from typing import Dict, Iterable, List, Mapping, Sequence, Set, Tuple

from analyzer.java_parser import method_signature


def changed_lines(buggy_source: str, fixed_source: str) -> Tuple[Set[int], Set[int]]:
    """Return one-based changed line numbers for buggy and fixed source text."""
    buggy_lines = buggy_source.splitlines()
    fixed_lines = fixed_source.splitlines()
    buggy_changed: Set[int] = set()
    fixed_changed: Set[int] = set()
    # Java sources contain many repeated braces and blank lines. Disabling
    # autojunk makes SequenceMatcher quadratic on large Defects4J classes.
    matcher = difflib.SequenceMatcher(a=buggy_lines, b=fixed_lines, autojunk=True)
    for tag, a_start, a_end, b_start, b_end in matcher.get_opcodes():
        if tag == "equal":
            continue
        buggy_changed.update(range(a_start + 1, a_end + 1))
        fixed_changed.update(range(b_start + 1, b_end + 1))
        # Insertions/deletions have an empty range on one side. Anchor them to
        # the closest surviving line so enclosing declarations can be selected.
        if a_start == a_end and buggy_lines:
            buggy_changed.add(min(a_start + 1, len(buggy_lines)))
        if b_start == b_end and fixed_lines:
            fixed_changed.add(min(b_start + 1, len(fixed_lines)))
    return buggy_changed, fixed_changed


def _overlaps(callable_metadata: Mapping[str, object], lines: Set[int]) -> bool:
    start = callable_metadata.get("start_line")
    end = callable_metadata.get("end_line")
    return isinstance(start, int) and isinstance(end, int) and any(
        start <= line <= end for line in lines
    )


def _trigger_mentions(name: str, sources: Iterable[str]) -> bool:
    pattern = re.compile(r"(?<![\w$])(?:[A-Za-z_$][\w$]*\s*\.\s*)?" + re.escape(name) + r"\s*\(")
    return any(pattern.search(source) for source in sources)


def select_callable_evidence(
    fixed_metadata: Mapping[str, object],
    fixed_source: str,
    buggy_metadata: Mapping[str, object] | None = None,
    buggy_source: str = "",
    trigger_test_sources: Sequence[str] = (),
) -> Dict[str, List[Dict[str, object]]]:
    """Return evidence by exact signature without broad public-method fallback."""
    buggy_changed, fixed_changed = changed_lines(buggy_source, fixed_source)
    evidence: Dict[str, List[Dict[str, object]]] = {}
    fixed_callables = [
        item
        for key in ("constructors", "methods")
        for item in fixed_metadata.get(key, [])
        if isinstance(item, dict)
    ]
    buggy_callables = [
        item
        for key in ("constructors", "methods")
        for item in (buggy_metadata or {}).get(key, [])
        if isinstance(item, dict)
    ]
    buggy_by_signature = {method_signature(item): item for item in buggy_callables}
    name_counts = Counter(str(item.get("name")) for item in fixed_callables)

    for item in fixed_callables:
        signature = method_signature(item)
        records: List[Dict[str, object]] = []
        if _overlaps(item, fixed_changed) or _overlaps(
            buggy_by_signature.get(signature, {}), buggy_changed
        ):
            records.append({"kind": "SOURCE_DIFF", "confidence": "HIGH"})
        name = str(item.get("name", ""))
        if name and _trigger_mentions(name, trigger_test_sources):
            records.append(
                {
                    "kind": "TRIGGER_TEST_CALL",
                    "confidence": "HIGH" if name_counts[name] == 1 else "AMBIGUOUS",
                }
            )
        if records:
            evidence[signature] = records
    return evidence
