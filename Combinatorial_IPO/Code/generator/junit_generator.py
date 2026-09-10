"""Generate project-compliant JUnit 4 tests from concrete combinations."""

from __future__ import annotations

import argparse
import csv
import json
import re
import sys
from pathlib import Path
from typing import List, Mapping, Optional, Sequence, Tuple


IPO_ROOT = Path(__file__).resolve().parents[1]
if str(IPO_ROOT) not in sys.path:
    sys.path.insert(0, str(IPO_ROOT))

from analyzer.java_parser import parse_java_file  # noqa: E402


JAVA_IDENTIFIER = re.compile(r"^[A-Za-z_$][A-Za-z0-9_$]*$")


def _require_java_identifier(value: str, label: str) -> None:
    if not JAVA_IDENTIFIER.fullmatch(value):
        raise ValueError("Invalid {}: {!r}".format(label, value))


def _comment_value(value: str) -> str:
    """Keep generated combination comments on one safe Java-comment line."""
    return value.replace("*/", "* /").replace("\r", "\\r").replace("\n", "\\n")


def _java_string_literal(value: str) -> str:
    return json.dumps(value, ensure_ascii=True)


def _render_test_method(
    class_name: str,
    method_name: str,
    parameter_names: Sequence[str],
    combination: Mapping[str, str],
    index: int,
    oracle: Optional[Mapping[str, object]] = None,
) -> str:
    expected_keys = set(parameter_names)
    actual_keys = set(combination)
    if actual_keys != expected_keys:
        raise ValueError(
            "Combination keys do not match method parameters; expected {}, got {}".format(
                sorted(expected_keys), sorted(actual_keys)
            )
        )

    arguments = ", ".join(combination[name] for name in parameter_names)
    details = ", ".join(
        "{}={}".format(name, _comment_value(combination[name]))
        for name in parameter_names
    )
    invocation = "{}.{}({})".format(class_name, method_name, arguments)
    if oracle is None:
        body = "{};".format(invocation)
    else:
        outcome = oracle.get("outcome")
        outcome_type = oracle.get("type")
        value = oracle.get("value_or_message")
        if not isinstance(outcome_type, str) or not isinstance(value, str):
            raise ValueError("Oracle type and value_or_message must be strings")
        if outcome == "RETURN":
            if outcome_type == "null":
                body = "assertNull({});".format(invocation)
            else:
                body = (
                    "Object actual = {invocation};\n"
                    "assertNotNull(actual);\n"
                    "assertEquals({expected_type}, actual.getClass().getName());\n"
                    "assertEquals({expected_value}, String.valueOf(actual));"
                ).format(
                    invocation=invocation,
                    expected_type=_java_string_literal(outcome_type),
                    expected_value=_java_string_literal(value),
                )
        elif outcome == "THROW":
            if not re.fullmatch(r"[A-Za-z_$][A-Za-z0-9_.$]*", outcome_type):
                raise ValueError("Invalid oracle exception type: {!r}".format(outcome_type))
            body = (
                "try {{\n"
                "    {invocation};\n"
                "    fail(\"Expected {exception_type}\");\n"
                "}} catch ({exception_type} expected) {{\n"
                "    // Expected outcome recorded from the fixed version.\n"
                "}}"
            ).format(
                invocation=invocation,
                exception_type=outcome_type,
            )
        else:
            raise ValueError("Unsupported oracle outcome: {!r}".format(outcome))

    indented_body = "\n".join("        " + line for line in body.splitlines())
    return """    @Test(timeout = 4000)
    public void test_{method}_pairwise_{index:03d}() throws Exception {{
        // Combination: {details}
{body}
    }}
""".format(
        method=method_name,
        index=index,
        details=details,
        body=indented_body,
    )


def _validate_method(method: Mapping[str, object]) -> Tuple[str, List[str]]:
    method_name = method.get("name")
    if not isinstance(method_name, str):
        raise ValueError("Method name is required")
    _require_java_identifier(method_name, "method name")
    if method.get("static") is not True:
        raise ValueError("Instance methods require a constructor strategy")

    raw_parameters = method.get("parameters")
    if not isinstance(raw_parameters, list):
        raise ValueError("Method parameters must be a list")
    parameter_names: List[str] = []
    for parameter in raw_parameters:
        if not isinstance(parameter, dict) or not isinstance(parameter.get("name"), str):
            raise ValueError("Every method parameter must have a name")
        name = parameter["name"]
        _require_java_identifier(name, "parameter name")
        parameter_names.append(name)
    return method_name, parameter_names


def synthesize_junit_suite(
    package_name: str,
    class_name: str,
    method_cases: Sequence[Tuple],
    test_class_name: Optional[str] = None,
) -> str:
    """Create one JUnit 4 class containing cases for multiple static methods."""
    _require_java_identifier(class_name, "class name")
    if test_class_name is None:
        test_class_name = "{}_IPOTest".format(class_name)
    _require_java_identifier(test_class_name, "test class name")
    if not method_cases:
        raise ValueError("At least one method and its combinations are required")

    rendered_methods: List[str] = []
    next_test_index = 1
    for method_case in method_cases:
        if len(method_case) == 2:
            method, combinations = method_case
            outcomes = None
        elif len(method_case) == 3:
            method, combinations, outcomes = method_case
        else:
            raise ValueError("Method case must contain method, combinations, and optional oracle")
        method_name, parameter_names = _validate_method(method)
        if not combinations:
            raise ValueError(
                "At least one combination is required for {}".format(method_name)
            )
        if outcomes is not None and len(outcomes) != len(combinations):
            raise ValueError("Oracle count does not match combinations for {}".format(method_name))
        for local_index, combination in enumerate(combinations):
            oracle = outcomes[local_index] if outcomes is not None else None
            if oracle is not None and oracle.get("id") != local_index + 1:
                raise ValueError("Oracle IDs must match combination order")
            if (
                oracle is not None
                and "arguments" in oracle
                and oracle.get("arguments") != dict(combination)
            ):
                raise ValueError("Oracle arguments do not match combination order")
            rendered_methods.append(
                _render_test_method(
                    class_name,
                    method_name,
                    parameter_names,
                    combination,
                    next_test_index,
                    oracle,
                )
            )
            next_test_index += 1

    package_line = "package {};\n\n".format(package_name) if package_name else ""
    methods = "\n".join(rendered_methods)

    return """{package_line}import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for {class_name}.
 */
public class {test_class_name} {{
{methods}
}}
""".format(
        package_line=package_line,
        class_name=class_name,
        test_class_name=test_class_name,
        methods=methods,
    )


def synthesize_junit(
    package_name: str,
    class_name: str,
    method: Mapping[str, object],
    combinations: Sequence[Mapping[str, str]],
) -> str:
    """Backward-compatible helper for generating a suite for one method."""
    return synthesize_junit_suite(
        package_name, class_name, [(method, combinations)]
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate an oracle-backed JUnit 4 suite")
    parser.add_argument("--source-file", required=True, type=Path)
    parser.add_argument("--method", required=True)
    parser.add_argument("--inputs", required=True, type=Path)
    parser.add_argument("--oracle", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    metadata = parse_java_file(str(args.source_file))
    matches = [
        method
        for method in metadata["methods"]
        if method.get("name") == args.method
    ]
    if len(matches) != 1:
        raise ValueError(
            "Expected one method named {!r}; found {}".format(args.method, len(matches))
        )
    with args.inputs.open("r", encoding="utf-8", newline="") as input_file:
        combinations = list(csv.DictReader(input_file, delimiter="\t"))
    outcomes = json.loads(args.oracle.read_text(encoding="utf-8"))
    source = synthesize_junit_suite(
        str(metadata["package"]),
        str(metadata["class"]),
        [(matches[0], combinations, outcomes)],
    )
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(source, encoding="utf-8")
    print("Generated {} oracle-backed tests".format(len(combinations)))


if __name__ == "__main__":
    main()
