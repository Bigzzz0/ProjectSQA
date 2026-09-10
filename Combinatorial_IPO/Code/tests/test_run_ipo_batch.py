"""Focused tests for overload selection in the IPO batch runner."""

from __future__ import annotations

import csv
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from analyzer.java_parser import parse_java_file
from runner.run_ipo_batch import (
    _matches_method_filter,
    _method_signature,
    run_batch,
)
from oracle.fixed_version_oracle import OracleCollectionError


INT_MIN = {
    "name": "min",
    "return_type": "int",
    "static": True,
    "parameters": [
        {"name": "a", "type": "int"},
        {"name": "b", "type": "int"},
        {"name": "c", "type": "int"},
    ],
}

LONG_MIN = {
    "name": "min",
    "return_type": "long",
    "static": True,
    "parameters": [
        {"name": "a", "type": "long"},
        {"name": "b", "type": "long"},
        {"name": "c", "type": "long"},
    ],
}


class MethodFilterTests(unittest.TestCase):
    def test_method_signature_includes_parameter_types(self) -> None:
        self.assertEqual("min(int,int,int)", _method_signature(INT_MIN))

    def test_signature_filter_selects_only_the_requested_overload(self) -> None:
        self.assertTrue(
            _matches_method_filter(INT_MIN, None, "min(int, int, int)")
        )
        self.assertFalse(
            _matches_method_filter(LONG_MIN, None, "min(int, int, int)")
        )

    def test_legacy_method_filter_still_selects_all_overloads(self) -> None:
        self.assertTrue(_matches_method_filter(INT_MIN, "min", None))
        self.assertTrue(_matches_method_filter(LONG_MIN, "min", None))
        self.assertFalse(_matches_method_filter(INT_MIN, "max", None))

    def test_number_utils_int_min_signature_has_one_match(self) -> None:
        project_root = Path(__file__).resolve().parents[3]
        source_file = project_root / "target_benchmark" / "Lang_1b" / "NumberUtils.java"
        methods = parse_java_file(str(source_file))["methods"]

        matches = [
            method
            for method in methods
            if _matches_method_filter(method, None, "min(int,int,int)")
        ]

        self.assertEqual(1, len(matches))
        self.assertEqual("int", matches[0]["return_type"])


class NativeIpoBatchTests(unittest.TestCase):
    def test_catalog_run_processes_only_the_named_source(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Demo_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "Sample.java").write_text(
                """package example;
public class Sample {
    public static int min(int left, int right) { return Math.min(left, right); }
}
""",
                encoding="utf-8",
            )
            (target_directory / "Other.java").write_text(
                """package example;
public class Other {
    public static int max(int left, int right) { return Math.max(left, right); }
}
""",
                encoding="utf-8",
            )
            catalog = temporary_root / "catalog.json"
            catalog.write_text(
                json.dumps(
                    [
                        {
                            "project": "Demo",
                            "bug_id": 1,
                            "dir": "Demo_1b",
                            "target_class": "example.Sample",
                            "simple_name": "Sample",
                        }
                    ]
                ),
                encoding="utf-8",
            )

            output_root = temporary_root / "output"
            manifest = run_batch(
                target_root=target_root,
                output_root=output_root,
                catalog_path=catalog,
            )

            self.assertEqual(str(catalog), manifest["catalog"])
            self.assertEqual("Result_Round2", manifest["result_directory"])
            self.assertEqual(["Sample"], [record["class"] for record in manifest["records"]])
            self.assertFalse((output_root / "Models" / "Demo_1b" / "Other").exists())
            loop_state = json.loads(
                (output_root / "Result_Round2" / "catalog_loop_state.json").read_text(
                    encoding="utf-8"
                )
            )
            self.assertTrue(loop_state["started"])
            self.assertEqual("COMPLETED", loop_state["status"])
            self.assertFalse((output_root / "Result_Round1").exists())

    def test_catalog_run_cannot_write_result_round1(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            catalog = root / "catalog.json"
            catalog.write_text("[]", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "must use Result_Round2"):
                run_batch(
                    target_root=root,
                    output_root=root / "output",
                    catalog_path=catalog,
                    result_directory="Result_Round1",
                )

    def test_suite_generation_failure_is_isolated_per_method(self) -> None:
        source = """package example;

public class SuiteSample {
    public static int first(int left, int right) { return left; }
    public static int second(int left, int right) { return right; }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Suite_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "SuiteSample.java").write_text(
                source, encoding="utf-8"
            )

            def fake_collector(**kwargs):
                return [
                    {
                        "id": index,
                        "outcome": "RETURN",
                        "type": "java.lang.Integer",
                        "value_or_message": "0",
                    }
                    for index, _ in enumerate(kwargs["combinations"], start=1)
                ]

            def fake_synthesizer(package_name, class_name, method_cases, test_class_name=None):
                if method_cases[0][0]["name"] == "first":
                    raise ValueError("simulated suite failure")
                return "package {};\npublic class {} {{}}\n".format(
                    package_name, test_class_name
                )

            with patch(
                "runner.run_ipo_batch.collect_fixed_oracle",
                side_effect=fake_collector,
            ), patch(
                "runner.run_ipo_batch.synthesize_junit_suite",
                side_effect=fake_synthesizer,
            ):
                manifest = run_batch(
                    target_root=target_root,
                    output_root=temporary_root / "output",
                    collect_oracles=True,
                )

            records = {record["method"]: record for record in manifest["records"]}
            self.assertEqual("SUITE_ERROR", records["first"]["status"])
            self.assertEqual("GENERATED", records["second"]["status"])
            self.assertEqual(1, manifest["generated_suite_count"])

    def test_suite_is_published_only_after_fixed_verification(self) -> None:
        source = """package example;

public class VerifiedSample {
    public static int min(int left, int right) { return Math.min(left, right); }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Verified_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "VerifiedSample.java").write_text(
                source, encoding="utf-8"
            )

            def fake_collector(**kwargs):
                return [
                    {
                        "id": index,
                        "outcome": "RETURN",
                        "type": "java.lang.Integer",
                        "value_or_message": "0",
                    }
                    for index, _ in enumerate(kwargs["combinations"], start=1)
                ]

            output_root = temporary_root / "output"
            with patch(
                "runner.run_ipo_batch.collect_fixed_oracle",
                side_effect=fake_collector,
            ), patch(
                "runner.run_ipo_batch.verify_fixed_suite",
                return_value="JUnit version 4\nOK (25 tests)",
            ):
                manifest = run_batch(
                    target_root=target_root,
                    output_root=output_root,
                    collect_oracles=True,
                    verify_suites=True,
                )

            record = manifest["records"][0]
            self.assertEqual("VERIFIED", record["suite_status"])
            self.assertEqual("OK (25 tests)", record["verification_result"])
            self.assertEqual(1, manifest["generated_suite_count"])
            self.assertTrue((output_root / record["test_suite"]).is_file())

    def test_failed_fixed_verification_does_not_publish_suite(self) -> None:
        source = """package example;

public class FailedSample {
    public static int min(int left, int right) { return Math.min(left, right); }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Failed_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "FailedSample.java").write_text(
                source, encoding="utf-8"
            )

            def fake_collector(**kwargs):
                return [
                    {
                        "id": index,
                        "outcome": "RETURN",
                        "type": "java.lang.Integer",
                        "value_or_message": "0",
                    }
                    for index, _ in enumerate(kwargs["combinations"], start=1)
                ]

            output_root = temporary_root / "output"
            with patch(
                "runner.run_ipo_batch.collect_fixed_oracle",
                side_effect=fake_collector,
            ), patch(
                "runner.run_ipo_batch.verify_fixed_suite",
                side_effect=OracleCollectionError("simulated JUnit failure"),
            ):
                manifest = run_batch(
                    target_root=target_root,
                    output_root=output_root,
                    collect_oracles=True,
                    verify_suites=True,
                )

            record = manifest["records"][0]
            self.assertEqual("SUITE_VERIFY_ERROR", record["status"])
            self.assertEqual("FAILED", record["suite_status"])
            self.assertEqual(0, manifest["generated_suite_count"])
            self.assertFalse((output_root / "TestCode").exists())

    def test_oracle_failure_is_isolated_per_method(self) -> None:
        source = """package example;

public class OracleSample {
    public static int first(int left, int right) { return left; }
    public static int second(int left, int right) { return right; }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Oracle_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "OracleSample.java").write_text(
                source, encoding="utf-8"
            )

            def fake_collector(**kwargs):
                if kwargs["method"]["name"] == "first":
                    raise OracleCollectionError("simulated oracle failure")
                return [
                    {
                        "id": index,
                        "outcome": "RETURN",
                        "type": "java.lang.Integer",
                        "value_or_message": "0",
                    }
                    for index, _ in enumerate(kwargs["combinations"], start=1)
                ]

            with patch(
                "runner.run_ipo_batch.collect_fixed_oracle",
                side_effect=fake_collector,
            ):
                manifest = run_batch(
                    target_root=target_root,
                    output_root=temporary_root / "output",
                    collect_oracles=True,
                )

            records = {record["method"]: record for record in manifest["records"]}
            self.assertEqual("ORACLE_ERROR", records["first"]["status"])
            self.assertEqual("ERROR", records["first"]["oracle_status"])
            self.assertEqual("GENERATED", records["second"]["status"])
            self.assertEqual("COLLECTED", records["second"]["oracle_status"])
            self.assertEqual(1, manifest["generated_suite_count"])

    def test_unsupported_method_does_not_block_supported_method(self) -> None:
        source = """package example;

public class MixedSample {
    public static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static Object repeat(Object value, int count) {
        return value;
    }

    public int instanceValue(int left, int right) {
        return left + right;
    }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Mixed_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "MixedSample.java").write_text(
                source, encoding="utf-8"
            )

            manifest = run_batch(
                target_root=target_root,
                output_root=temporary_root / "output",
            )

            statuses = {
                record["method_signature"]: record["status"]
                for record in manifest["records"]
            }
            self.assertEqual("GENERATED", statuses["min(int,int,int)"])
            self.assertEqual(
                "NEEDS_SEMANTIC_MODEL", statuses["repeat(Object,int)"]
            )
            self.assertEqual("UNSUPPORTED", statuses["instanceValue(int,int)"])

    def test_batch_uses_native_ipo_and_waits_for_oracle_before_junit(self) -> None:
        source = """package example;

public class Sample {
    public static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            temporary_root = Path(temporary_directory)
            target_root = temporary_root / "targets"
            target_directory = target_root / "Demo_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "Sample.java").write_text(
                source, encoding="utf-8"
            )
            output_root = temporary_root / "output"

            manifest = run_batch(
                target_root=target_root,
                output_root=output_root,
                project_filter="Demo",
                bug_filter=1,
                signature_filter="min(int,int,int)",
            )

            self.assertEqual("ipo", manifest["generation_backend"])
            self.assertEqual(2, manifest["strength"])
            self.assertEqual(1, manifest["generated_method_count"])
            self.assertEqual(0, manifest["generated_suite_count"])
            record = manifest["records"][0]
            self.assertEqual("min(int,int,int)", record["method_signature"])
            self.assertEqual("ipo", record["generation_backend"])
            self.assertEqual(125, record["cartesian_count"])
            self.assertEqual(28, record["pairwise_count"])
            self.assertEqual(75, record["expected_pair_count"])
            self.assertEqual(75, record["covered_pair_count"])
            self.assertEqual(100.0, record["pair_coverage_percent"])
            self.assertEqual(0, record["duplicate_concrete_input_count"])
            self.assertEqual("MISSING", record["oracle_status"])
            self.assertFalse((output_root / "TestCode").exists())

            domains_path = output_root / record["domains"]
            self.assertEqual(
                {
                    "a": ["0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"],
                    "b": ["0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"],
                    "c": ["0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"],
                },
                json.loads(domains_path.read_text(encoding="utf-8")),
            )

            oracle_path = (
                output_root
                / "Result_Round1"
                / "Demo_1b"
                / "Sample"
                / "min__int_int_int_oracle.json"
            )
            oracle_path.write_text("[]", encoding="utf-8")
            stale_manifest = run_batch(
                target_root=target_root,
                output_root=output_root,
                signature_filter="min(int,int,int)",
            )

            self.assertEqual(
                "STALE", stale_manifest["records"][0]["oracle_status"]
            )
            self.assertEqual(0, stale_manifest["generated_suite_count"])

            inputs_path = output_root / record["inputs"]
            with inputs_path.open("r", encoding="utf-8", newline="") as stream:
                inputs = list(csv.DictReader(stream, delimiter="\t"))
            matching_oracle = [
                {
                    "id": index,
                    "arguments": arguments,
                    "outcome": "RETURN",
                    "type": "java.lang.Integer",
                    "value_or_message": "0",
                }
                for index, arguments in enumerate(inputs, start=1)
            ]
            oracle_path.write_text(
                json.dumps(matching_oracle), encoding="utf-8"
            )
            ready_manifest = run_batch(
                target_root=target_root,
                output_root=output_root,
                signature_filter="min(int,int,int)",
            )

            self.assertEqual(
                "REUSED", ready_manifest["records"][0]["oracle_status"]
            )
            self.assertEqual(1, ready_manifest["generated_suite_count"])
            self.assertTrue(
                (
                    output_root
                    / "TestCode"
                    / "Demo_1b"
                    / "Sample_min__int_int_int_IPOTest.java"
                ).is_file()
            )
            suite_source = (
                output_root
                / "TestCode"
                / "Demo_1b"
                / "Sample_min__int_int_int_IPOTest.java"
            ).read_text(encoding="utf-8")
            self.assertIn(
                "public class Sample_min__int_int_int_IPOTest", suite_source
            )
            self.assertTrue((output_root / record["record_manifest"]).is_file())

    def test_lang1_native_ipo_generation_uses_temporary_output(self) -> None:
        project_root = Path(__file__).resolve().parents[3]
        target_root = project_root / "target_benchmark"
        with tempfile.TemporaryDirectory() as temporary_directory:
            output_root = Path(temporary_directory) / "output"

            manifest = run_batch(
                target_root=target_root,
                output_root=output_root,
                project_filter="Lang",
                bug_filter=1,
                signature_filter="createNumber(String)",
            )

            self.assertEqual(1, manifest["generated_method_count"])
            self.assertEqual(0, manifest["generated_suite_count"])
            record = manifest["records"][0]
            self.assertEqual("ipo", record["generation_backend"])
            self.assertEqual("createNumber(String)", record["method_signature"])
            self.assertEqual("number_utils_numeric_string", record["strategy"])
            self.assertEqual(960, record["cartesian_count"])
            self.assertEqual(48, record["pairwise_count"])
            self.assertEqual(194, record["expected_pair_count"])
            self.assertEqual(194, record["covered_pair_count"])
            self.assertEqual(0, record["missing_pair_count"])
            self.assertEqual(100.0, record["pair_coverage_percent"])
            self.assertEqual("MISSING", record["oracle_status"])
            self.assertFalse((output_root / "TestCode").exists())


if __name__ == "__main__":
    unittest.main()
