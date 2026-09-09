"""Focused tests for overload selection in the IPO batch runner."""

from __future__ import annotations

import csv
import json
import tempfile
import unittest
from pathlib import Path

from analyzer.java_parser import parse_java_file
from runner.run_ipo_batch import (
    _matches_method_filter,
    _method_signature,
    run_batch,
)


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
        project_root = Path(__file__).resolve().parents[2]
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
                    / "Sample_IPOTest.java"
                ).is_file()
            )

    def test_lang1_native_ipo_generation_uses_temporary_output(self) -> None:
        project_root = Path(__file__).resolve().parents[2]
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
