"""Tests for backend-independent 2-way coverage verification."""

from __future__ import annotations

import csv
import unittest
from pathlib import Path

from domain.semantic_overrides import NUMBER_UTILS_FACTORS
from verification.pair_coverage import (
    InteractionPair,
    expected_pairs,
    verify_pair_coverage,
)


class PairCoverageTests(unittest.TestCase):
    def test_two_factor_cartesian_rows_have_complete_coverage(self) -> None:
        domains = {"left": ["0", "1"], "right": ["A", "B"]}
        rows = [
            {"left": left, "right": right}
            for left in domains["left"]
            for right in domains["right"]
        ]

        report = verify_pair_coverage(domains, rows)

        self.assertTrue(report.complete)
        self.assertEqual(4, report.expected_pair_count)
        self.assertEqual(4, report.covered_pair_count)
        self.assertEqual(100.0, report.coverage_percent)

    def test_four_rows_cover_all_pairs_for_three_binary_factors(self) -> None:
        domains = {"a": ["0", "1"], "b": ["0", "1"], "c": ["0", "1"]}
        rows = [
            {"a": "0", "b": "0", "c": "0"},
            {"a": "0", "b": "1", "c": "1"},
            {"a": "1", "b": "0", "c": "1"},
            {"a": "1", "b": "1", "c": "0"},
        ]

        report = verify_pair_coverage(domains, rows)

        self.assertTrue(report.complete)
        self.assertEqual(12, report.expected_pair_count)
        self.assertEqual(12, report.covered_pair_count)

    def test_incomplete_rows_report_the_exact_missing_pairs(self) -> None:
        domains = {"left": ["0", "1"], "right": ["A", "B"]}
        report = verify_pair_coverage(
            domains,
            [{"left": "0", "right": "A"}],
        )

        self.assertFalse(report.complete)
        self.assertEqual(1, report.covered_pair_count)
        self.assertEqual(25.0, report.coverage_percent)
        self.assertIn(
            InteractionPair("left", "1", "right", "B"),
            report.missing_pairs,
        )

    def test_row_with_missing_factor_is_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "must contain exactly factors"):
            verify_pair_coverage(
                {"left": ["0"], "right": ["A"]},
                [{"left": "0"}],
            )

    def test_row_with_value_outside_domain_is_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "outside domain"):
            verify_pair_coverage(
                {"left": ["0"], "right": ["A"]},
                [{"left": "1", "right": "A"}],
            )

    def test_duplicate_domain_values_are_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "duplicate values"):
            expected_pairs({"left": ["0", "0"], "right": ["A"]})

    def test_one_factor_has_complete_vacuous_pair_coverage(self) -> None:
        report = verify_pair_coverage(
            {"only": ["A", "B"]},
            [{"only": "A"}, {"only": "B"}],
        )

        self.assertTrue(report.complete)
        self.assertEqual(0, report.expected_pair_count)
        self.assertEqual(100.0, report.coverage_percent)

    def test_lang1_pict_pilot_has_complete_pair_coverage(self) -> None:
        ipo_root = Path(__file__).resolve().parents[1]
        combinations_path = (
            ipo_root
            / "baselines"
            / "pict"
            / "Lang_1b"
            / "Result_Round1"
            / "NumberUtils"
            / "createNumber__String_combinations.tsv"
        )
        with combinations_path.open("r", encoding="utf-8", newline="") as stream:
            rows = list(csv.DictReader(stream, delimiter="\t"))

        report = verify_pair_coverage(NUMBER_UTILS_FACTORS, rows)

        self.assertEqual(48, len(rows))
        self.assertTrue(report.complete)
        self.assertEqual(194, report.expected_pair_count)
        self.assertEqual(194, report.covered_pair_count)
        self.assertEqual(0, len(report.missing_pairs))


if __name__ == "__main__":
    unittest.main()
