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
    r"^\s*(?:public\s+)?(?:(?:abstract|final|strictfp)\s+)*class\s+(\w+)",
    re.MULTILINE,
)
METHOD_PATTERN = re.compile(
    r"\bpublic\s+"
    r"(?P<modifiers>(?:(?:static|final|synchronized|native|abstract|strictfp|default)\s+)*)"
    r"(?P<return_type>[\w.$<>?,\[\]\s]+?)\s+"
    r"(?P<name>\w+)\s*"
    r"\((?P<parameters>[^()]*)\)\s*"
    r"(?:throws\s+[^\{;]+)?[\{;]",
    re.MULTILINE,
)


def _remove_comments(source: str) -> str:
    """Remove comments while preserving Java string and character literals."""
    pattern = re.compile(
        r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\')|//[^\r\n]*|/\*.*?\*/',
        re.DOTALL,
    )
    return pattern.sub(lambda match: match.group(1) or "", source)


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
    """Return package, class, and public method metadata from a Java file."""
    path = Path(filepath)
    source = _remove_comments(path.read_text(encoding="utf-8", errors="ignore"))

    package_match = PACKAGE_PATTERN.search(source)
    class_match = CLASS_PATTERN.search(source)
    if not class_match:
        raise ValueError("No public class declaration found in {}".format(path))

    methods: List[Dict[str, object]] = []
    for match in METHOD_PATTERN.finditer(source):
        raw_parameters = match.group("parameters").strip()
        try:
            parameters = [
                _parse_parameter(item) for item in _split_parameters(raw_parameters)
            ]
        except ValueError:
            continue

        modifiers = match.group("modifiers").split()
        methods.append(
            {
                "name": match.group("name"),
                "return_type": " ".join(match.group("return_type").split()),
                "static": "static" in modifiers,
                "parameters": parameters,
            }
        )

    return {
        "source_file": str(path),
        "package": package_match.group(1) if package_match else "",
        "class": class_match.group(1),
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
