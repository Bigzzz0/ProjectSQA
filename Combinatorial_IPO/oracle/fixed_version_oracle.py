"""Collect deterministic outcomes from a Defects4J fixed version."""

from __future__ import annotations

import argparse
import base64
import csv
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Dict, List, Mapping, Sequence


IPO_ROOT = Path(__file__).resolve().parents[1]
if str(IPO_ROOT) not in sys.path:
    sys.path.insert(0, str(IPO_ROOT))

from analyzer.java_parser import parse_java_file  # noqa: E402


JAVA_IDENTIFIER = re.compile(r"^[A-Za-z_$][A-Za-z0-9_$]*$")


class OracleCollectionError(RuntimeError):
    """Raised when a fixed-version oracle cannot be collected reliably."""


def _run(
    command: Sequence[str],
    cwd: Path,
    timeout_seconds: int = 300,
) -> str:
    try:
        completed = subprocess.run(
            list(command),
            cwd=str(cwd),
            capture_output=True,
            text=True,
            timeout=timeout_seconds,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        raise OracleCollectionError(
            "Command could not run: {}".format(" ".join(command))
        ) from exc
    if completed.returncode != 0:
        detail = completed.stderr.strip() or completed.stdout.strip()
        raise OracleCollectionError(
            "Command failed with code {}: {}\n{}".format(
                completed.returncode, " ".join(command), detail
            )
        )
    return completed.stdout.strip()


def collector_class_name(target_class: str) -> str:
    if not JAVA_IDENTIFIER.fullmatch(target_class):
        raise ValueError("Invalid target class name: {!r}".format(target_class))
    return "{}_IPOOracleCollector".format(target_class)


def generate_collector_source(
    package_name: str,
    target_class: str,
    method: Mapping[str, object],
    combinations: Sequence[Mapping[str, str]],
) -> str:
    """Generate a Java main class that emits one TSV outcome per input."""
    method_name = method.get("name")
    parameters = method.get("parameters")
    return_type = method.get("return_type")
    if not isinstance(method_name, str) or not JAVA_IDENTIFIER.fullmatch(method_name):
        raise ValueError("A valid method name is required")
    if method.get("static") is not True:
        raise ValueError("Oracle collection currently supports static methods only")
    if not isinstance(parameters, list) or not isinstance(return_type, str):
        raise ValueError("Method parameters and return type are required")

    parameter_names = []
    for parameter in parameters:
        if not isinstance(parameter, dict) or not isinstance(parameter.get("name"), str):
            raise ValueError("Malformed method parameter metadata")
        parameter_names.append(parameter["name"])

    calls: List[str] = []
    for index, combination in enumerate(combinations, start=1):
        if set(combination) != set(parameter_names):
            raise ValueError("Oracle combination does not match method parameters")
        arguments = ", ".join(combination[name] for name in parameter_names)
        invocation = "{}.{}({})".format(target_class, method_name, arguments)
        if return_type == "void":
            body = "{};\n            emitReturn({}, null, \"\");".format(
                invocation, index
            )
        else:
            body = "Object result = {};\n            emitReturn({}, result, String.valueOf(result));".format(
                invocation, index
            )
        calls.append(
            """        try {{
            {body}
        }} catch (Throwable error) {{
            emitThrow({index}, error);
        }}""".format(body=body, index=index)
        )

    package_line = "package {};\n\n".format(package_name) if package_name else ""
    class_name = collector_class_name(target_class)
    return """{package_line}import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class {collector_class} {{
    private static String encode(String value) {{
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }}

    private static void emitReturn(int id, Object value, String text) {{
        String type = value == null ? "null" : value.getClass().getName();
        System.out.println(id + "\\tRETURN\\t" + type + "\\t" + encode(text));
    }}

    private static void emitThrow(int id, Throwable error) {{
        String message = error.getMessage() == null ? "" : error.getMessage();
        System.out.println(id + "\\tTHROW\\t" + error.getClass().getName() + "\\t" + encode(message));
    }}

    public static void main(String[] args) {{
{calls}
    }}
}}
""".format(
        package_line=package_line,
        collector_class=class_name,
        calls="\n".join(calls),
    )


def parse_oracle_output(output: str, expected_count: int) -> List[Dict[str, object]]:
    """Parse and validate collector TSV output."""
    outcomes: List[Dict[str, object]] = []
    for line in output.splitlines():
        parts = line.rstrip("\r").split("\t")
        if len(parts) != 4 or parts[1] not in {"RETURN", "THROW"}:
            raise OracleCollectionError("Malformed oracle output line: {!r}".format(line))
        try:
            row_id = int(parts[0])
            text = base64.b64decode(parts[3]).decode("utf-8")
        except (ValueError, UnicodeError) as exc:
            raise OracleCollectionError("Invalid oracle output encoding") from exc
        outcomes.append(
            {
                "id": row_id,
                "outcome": parts[1],
                "type": parts[2],
                "value_or_message": text,
            }
        )

    expected_ids = list(range(1, expected_count + 1))
    if [outcome["id"] for outcome in outcomes] != expected_ids:
        raise OracleCollectionError(
            "Oracle IDs are incomplete or out of order; expected {}, got {}".format(
                expected_ids, [outcome["id"] for outcome in outcomes]
            )
        )
    return outcomes


def collect_fixed_oracle(
    project: str,
    bug_id: int,
    package_name: str,
    target_class: str,
    method: Mapping[str, object],
    combinations: Sequence[Mapping[str, str]],
    defects4j_executable: str = "defects4j",
    javac_executable: str = "javac",
    java_executable: str = "java",
) -> List[Dict[str, object]]:
    """Checkout ``<bug_id>f``, run a generated collector, and return outcomes."""
    source = generate_collector_source(
        package_name, target_class, method, combinations
    )
    with tempfile.TemporaryDirectory(prefix="ipo_fixed_oracle_") as temp_name:
        temp_root = Path(temp_name)
        checkout = temp_root / "checkout"
        _run(
            [
                defects4j_executable,
                "checkout",
                "-p",
                project,
                "-v",
                "{}f".format(bug_id),
                "-w",
                str(checkout),
            ],
            temp_root,
        )
        _run([defects4j_executable, "compile"], checkout)
        compile_classpath = _run(
            [defects4j_executable, "export", "-p", "cp.compile"], checkout
        )
        binary_directory = _run(
            [defects4j_executable, "export", "-p", "dir.bin.classes"], checkout
        )
        binary_path = checkout / binary_directory
        collector_classes = temp_root / "collector_classes"
        collector_classes.mkdir()
        source_path = temp_root / "{}.java".format(
            collector_class_name(target_class)
        )
        source_path.write_text(source, encoding="utf-8")

        classpath_entries = [str(binary_path)]
        if compile_classpath:
            classpath_entries.append(compile_classpath)
        compile_cp = os.pathsep.join(classpath_entries)
        _run(
            [
                javac_executable,
                "-cp",
                compile_cp,
                "-d",
                str(collector_classes),
                str(source_path),
            ],
            temp_root,
        )

        runtime_cp = os.pathsep.join([str(collector_classes), compile_cp])
        fully_qualified_collector = (
            "{}.{}".format(package_name, collector_class_name(target_class))
            if package_name
            else collector_class_name(target_class)
        )
        output = _run(
            [java_executable, "-cp", runtime_cp, fully_qualified_collector],
            temp_root,
        )
        return parse_oracle_output(output, len(combinations))


def main() -> None:
    parser = argparse.ArgumentParser(description="Collect outcomes from a Defects4J fixed version")
    parser.add_argument("--project", required=True)
    parser.add_argument("--bug", required=True, type=int)
    parser.add_argument("--source-file", required=True, type=Path)
    parser.add_argument("--method", required=True)
    parser.add_argument("--inputs", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--defects4j", default="defects4j")
    args = parser.parse_args()

    metadata = parse_java_file(str(args.source_file))
    matches = [
        method
        for method in metadata["methods"]
        if method.get("name") == args.method
    ]
    if len(matches) != 1:
        raise OracleCollectionError(
            "Expected one method named {!r}; found {}".format(args.method, len(matches))
        )

    with args.inputs.open("r", encoding="utf-8", newline="") as input_file:
        combinations = list(csv.DictReader(input_file, delimiter="\t"))
    outcomes = collect_fixed_oracle(
        project=args.project,
        bug_id=args.bug,
        package_name=str(metadata["package"]),
        target_class=str(metadata["class"]),
        method=matches[0],
        combinations=combinations,
        defects4j_executable=args.defects4j,
    )
    for outcome, combination in zip(outcomes, combinations):
        outcome["arguments"] = combination
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(outcomes, indent=2), encoding="utf-8")
    print("Collected {} fixed-version outcomes".format(len(outcomes)))


if __name__ == "__main__":
    main()
