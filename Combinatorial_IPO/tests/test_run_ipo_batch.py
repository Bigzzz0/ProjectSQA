"""Focused tests for overload selection in the IPO batch runner."""

from __future__ import annotations

import unittest
from pathlib import Path

from analyzer.java_parser import parse_java_file
from runner.run_ipo_batch import _matches_method_filter, _method_signature


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


if __name__ == "__main__":
    unittest.main()
