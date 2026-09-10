"""Compile and run one generated JUnit suite in a temporary Defects4J checkout."""

from __future__ import annotations

import argparse
import os
import shutil
import tempfile
from pathlib import Path

if __package__:
    from .fixed_version_oracle import OracleCollectionError, _run
else:
    from fixed_version_oracle import OracleCollectionError, _run


def verify_suite(
    project: str,
    version: str,
    suite_path: Path,
    test_class: str,
    defects4j_executable: str = "defects4j",
    java_executable: str = "java",
) -> str:
    """Return JUnitCore output when the generated suite passes completely."""
    with tempfile.TemporaryDirectory(prefix="ipo_suite_verify_") as temp_name:
        temp_root = Path(temp_name)
        checkout = temp_root / "checkout"
        _run(
            [
                defects4j_executable,
                "checkout",
                "-p",
                project,
                "-v",
                version,
                "-w",
                str(checkout),
            ],
            temp_root,
        )
        test_source_directory = _run(
            [defects4j_executable, "export", "-p", "dir.src.tests"], checkout
        )
        package_name = test_class.rsplit(".", 1)[0] if "." in test_class else ""
        destination_directory = checkout / test_source_directory
        if package_name:
            destination_directory /= Path(*package_name.split("."))
        destination_directory.mkdir(parents=True, exist_ok=True)
        shutil.copy2(suite_path, destination_directory / suite_path.name)

        _run([defects4j_executable, "compile"], checkout)
        test_classpath = _run(
            [defects4j_executable, "export", "-p", "cp.test"], checkout
        )
        executable_path = shutil.which(defects4j_executable)
        framework_junit = None
        if executable_path:
            framework_root = Path(executable_path).resolve().parents[1]
            candidate = framework_root / "projects" / "lib" / "junit-4.12-hamcrest-1.3.jar"
            if candidate.is_file():
                framework_junit = str(candidate)
        class_binary_directory = _run(
            [defects4j_executable, "export", "-p", "dir.bin.classes"], checkout
        )
        test_binary_directory = _run(
            [defects4j_executable, "export", "-p", "dir.bin.tests"], checkout
        )
        runtime_entries = [
                str(checkout / test_binary_directory),
                str(checkout / class_binary_directory),
        ]
        # Some early Defects4J projects export JUnit 3 only even though the
        # infrastructure compiles generated JUnit 4 tests.  Prefer the bundled
        # framework JUnit 4 jar so JUnitCore and @Test(timeout=...) are present.
        if framework_junit:
            runtime_entries.append(framework_junit)
        runtime_entries.append(test_classpath)
        runtime_classpath = os.pathsep.join(runtime_entries)
        return _run(
            [
                java_executable,
                "-cp",
                runtime_classpath,
                "org.junit.runner.JUnitCore",
                test_class,
            ],
            checkout,
        )


def main() -> None:
    parser = argparse.ArgumentParser(description="Verify a generated IPO JUnit suite")
    parser.add_argument("--project", required=True)
    parser.add_argument("--version", required=True)
    parser.add_argument("--suite", required=True, type=Path)
    parser.add_argument("--test-class", required=True)
    parser.add_argument("--defects4j", default="defects4j")
    args = parser.parse_args()

    try:
        output = verify_suite(
            project=args.project,
            version=args.version,
            suite_path=args.suite,
            test_class=args.test_class,
            defects4j_executable=args.defects4j,
        )
    except OracleCollectionError as exc:
        raise SystemExit(str(exc))
    print(output)


if __name__ == "__main__":
    main()
