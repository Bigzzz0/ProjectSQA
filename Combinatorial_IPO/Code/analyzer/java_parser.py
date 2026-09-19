"""Extract public Java method signatures for the IPO pipeline.

This is intentionally a lightweight source analyzer.  It does not try to be a
complete Java compiler; unsupported signatures are skipped instead of guessed.
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Dict, List, Mapping


PACKAGE_PATTERN = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
CLASS_PATTERN = re.compile(
    r"^[ \t]*(?:(?:public|protected|private)[ \t]+)?"
    r"(?P<modifiers>(?:(?:abstract|final|strictfp|static)[ \t]+)*)"
    r"(?P<kind>class|interface|enum)[ \t]+(?P<name>\w+)",
    re.MULTILINE,
)
METHOD_PATTERN = re.compile(
    r"^[ \t]*(?:(?P<visibility>public|protected|private)[ \t]+)?"
    r"(?P<modifiers>(?:(?:static|final|synchronized|native|abstract|strictfp|default)[ \t]+)*)"
    r"(?P<return_type>[\w.$<>?,\[\] \t]+?)[ \t]+"
    r"(?P<name>\w+)[ \t]*"
    r"\((?P<parameters>[^()]*)\)[ \t]*"
    r"(?:throws[ \t]+[^\{;]+)?[\{;]",
    re.MULTILINE,
)


def _remove_comments(source: str) -> str:
    """Blank comments while preserving offsets, lines, and Java literals."""
    pattern = re.compile(
        r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\')|//[^\r\n]*|/\*.*?\*/',
        re.DOTALL,
    )
    def replace(match: re.Match[str]) -> str:
        if match.group(1):
            return match.group(1)
        return "".join("\n" if char == "\n" else "\r" if char == "\r" else " " for char in match.group(0))

    return pattern.sub(replace, source)


def _declaration_span(source: str, start: int, declaration_end: int) -> Dict[str, int]:
    """Return stable offsets/lines for a declaration, including its brace body."""
    end = declaration_end
    opening = source.rfind("{", start, declaration_end)
    semicolon = source.rfind(";", start, declaration_end)
    if opening >= 0 and opening > semicolon:
        depth = 0
        quote = ""
        escaped = False
        for index in range(opening, len(source)):
            character = source[index]
            if quote:
                if escaped:
                    escaped = False
                elif character == "\\":
                    escaped = True
                elif character == quote:
                    quote = ""
                continue
            if character in {'"', "'"}:
                quote = character
                continue
            if character == "{":
                depth += 1
            elif character == "}":
                depth -= 1
                if depth == 0:
                    end = index + 1
                    break
    return {
        "start_offset": start,
        "end_offset": end,
        "start_line": source.count("\n", 0, start) + 1,
        "end_line": source.count("\n", 0, end) + 1,
    }


def _split_parameters(parameters: str) -> List[str]:
    """Split a parameter list without breaking commas inside generic types."""
    parts: List[str] = []
    start = 0
    generic_depth = 0

    for index, character in enumerate(parameters):
        if character == "<":
            generic_depth += 1
        elif character == ">" and generic_depth:
            generic_depth -= 1
        elif character == "," and generic_depth == 0:
            parts.append(parameters[start:index].strip())
            start = index + 1

    final_part = parameters[start:].strip()
    if final_part:
        parts.append(final_part)
    return parts


def _brace_depths(source: str) -> List[int]:
    """Return the Java brace depth at every offset in one linear pass."""
    depths: List[int] = [0] * (len(source) + 1)
    depth = 0
    quote = ""
    escaped = False
    for offset, character in enumerate(source):
        depths[offset] = depth
        if quote:
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == quote:
                quote = ""
            continue
        if character in {'"', "'"}:
            quote = character
        elif character == "{":
            depth += 1
        elif character == "}":
            depth -= 1
    depths[len(source)] = depth
    return depths


def _parse_parameter(parameter: str) -> Dict[str, str]:
    """Convert one Java parameter declaration into its type and name."""
    without_annotations = re.sub(r"@\w+(?:\([^)]*\))?\s*", "", parameter)
    tokens = [token for token in without_annotations.split() if token != "final"]
    if len(tokens) < 2:
        raise ValueError("Unsupported Java parameter declaration: {!r}".format(parameter))

    name = tokens[-1]
    parameter_type = " ".join(tokens[:-1]).replace("...", "[]")
    if name.endswith("[]"):
        name = name[:-2]
        parameter_type += "[]"
    return {"name": name, "type": parameter_type}


def parse_java_file(filepath: str) -> Dict[str, object]:
    """Return top-level type, constructors, and callable method metadata."""
    path = Path(filepath)
    source = _remove_comments(path.read_text(encoding="utf-8", errors="ignore"))

    package_match = PACKAGE_PATTERN.search(source)
    class_match = CLASS_PATTERN.search(source)
    if not class_match:
        raise ValueError("No public class declaration found in {}".format(path))

    class_name = class_match.group("name")
    brace_depths = _brace_depths(source)
    methods: List[Dict[str, object]] = []
    for match in METHOD_PATTERN.finditer(source):
        if brace_depths[match.start()] != 1:
            continue
        if match.group("name") == class_name:
            # Constructors are parsed separately so they cannot masquerade as
            # methods with an empty return type.
            continue
        raw_parameters = match.group("parameters").strip()
        try:
            parameters = [
                _parse_parameter(item) for item in _split_parameters(raw_parameters)
            ]
        except ValueError:
            continue

        modifiers = match.group("modifiers").split()
        record = {
                "name": match.group("name"),
                "return_type": " ".join(match.group("return_type").split()),
                "static": "static" in modifiers,
                "visibility": match.group("visibility") or "package",
                "kind": "method",
                "parameters": parameters,
            }
        record.update(_declaration_span(source, match.start(), match.end()))
        methods.append(record)

    constructor_pattern = re.compile(
        r"^[ \t]*(?:(?P<visibility>public|protected|private)[ \t]+)?"
        r"(?P<modifiers>(?:(?:final|synchronized|strictfp)[ \t]+)*)"
        + re.escape(class_name)
        + r"[ \t]*\((?P<parameters>[^()]*)\)[ \t]*(?:throws[ \t]+[^\{;]+)?[\{;]",
        re.MULTILINE,
    )
    constructors: List[Dict[str, object]] = []
    for match in constructor_pattern.finditer(source):
        if brace_depths[match.start()] != 1:
            continue
        try:
            parameters = [
                _parse_parameter(item)
                for item in _split_parameters(match.group("parameters").strip())
            ]
        except ValueError:
            continue
        record = {
                "name": class_name,
                "return_type": None,
                "static": False,
                "visibility": match.group("visibility") or "package",
                "kind": "constructor",
                "parameters": parameters,
            }
        record.update(_declaration_span(source, match.start(), match.end()))
        constructors.append(record)

    broad_constructor_pattern = re.compile(
        r"^[ \t]*(?:(?:public|protected|private)[ \t]+)?"
        + re.escape(class_name)
        + r"[ \t\r\n]*\(",
        re.MULTILINE,
    )
    declared_constructor_count = sum(
        1
        for match in broad_constructor_pattern.finditer(source)
        if brace_depths[match.start()] == 1
    )

    return {
        "source_file": str(path),
        "package": package_match.group(1) if package_match else "",
        "class": class_name,
        "type_kind": class_match.group("kind"),
        "abstract": "abstract" in class_match.group("modifiers").split(),
        "constructors": constructors,
        "declared_constructor_count": declared_constructor_count,
        "methods": methods,
    }


def method_signature(method: Mapping[str, object]) -> str:
    """Return a normalized Java-style signature for one parsed method."""
    name = method.get("name")
    parameters = method.get("parameters")
    if not isinstance(name, str) or not isinstance(parameters, list):
        raise ValueError("Method name and parameters are required")

    parameter_types: List[str] = []
    for parameter in parameters:
        if not isinstance(parameter, dict) or not isinstance(
            parameter.get("type"), str
        ):
            raise ValueError("Every method parameter must have a type")
        parameter_types.append(re.sub(r"\s+", "", parameter["type"]))
    return "{}({})".format(name, ",".join(parameter_types))


def main() -> None:
    parser = argparse.ArgumentParser(description="Extract public Java method metadata")
    parser.add_argument("java_file", help="Path to a Java source file")
    args = parser.parse_args()
    print(json.dumps(parse_java_file(args.java_file), indent=2))


if __name__ == "__main__":
    main()
