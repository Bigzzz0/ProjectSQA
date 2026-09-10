"""Tests for the project's native deterministic 2-way IPO implementation."""

from __future__ import annotations

import copy
import unittest

from algorithm.ipo import (
    generate_pairwise,
    horizontal_growth,
    initial_construction,
    vertical_growth,
)
from domain.semantic_overrides import NUMBER_UTILS_FACTORS
from verification.pair_coverage import verify_pair_coverage


class IpoTests(unittest.TestCase):
    def test_constraint_aware_generation_contains_only_valid_rows(self):
        domains = {
            "population": ["10", "20"],
            "successes": ["5", "15"],
            "sample": ["5", "15"],
        }
        valid = [
            {"population": p, "successes": s, "sample": n}
            for p in domains["population"]
            for s in domains["successes"]
            for n in domains["sample"]
            if int(s) <= int(p) and int(n) <= int(p)
        ]

        rows = generate_pairwise(domains, valid_combinations=valid)

        self.assertTrue(rows)
        self.assertTrue(all(row in valid for row in rows))
        report = verify_pair_coverage(domains, rows, valid_combinations=valid)
        self.assertTrue(report.complete)

    def test_constraint_violating_seed_is_rejected(self):
        domains = {"mode": ["safe", "fast"], "input": ["small", "large"]}
        valid = [
            {"mode": "safe", "input": "small"},
            {"mode": "safe", "input": "large"},
            {"mode": "fast", "input": "small"},
        ]
        with self.assertRaisesRegex(ValueError, "violates scenario constraints"):
            generate_pairwise(
                domains,
                seed_combinations=[{"mode": "fast", "input": "large"}],
                valid_combinations=valid,
            )
    def test_initial_construction_is_first_two_factor_cartesian_product(self) -> None:
        domains = {"a": ["0", "1"], "b": ["X", "Y"], "c": ["T", "F"]}

        self.assertEqual(
            [
                {"a": "0", "b": "X"},
                {"a": "0", "b": "Y"},
                {"a": "1", "b": "X"},
                {"a": "1", "b": "Y"},
            ],
            initial_construction(domains),
        )

    def test_horizontal_growth_greedily_covers_binary_third_factor(self) -> None:
        domains = {"a": ["0", "1"], "b": ["0", "1"], "c": ["0", "1"]}
        initial_rows = initial_construction(domains)

        rows, uncovered = horizontal_growth(initial_rows, domains, "c")

        self.assertEqual(
            [
                {"a": "0", "b": "0", "c": "0"},
                {"a": "0", "b": "1", "c": "1"},
                {"a": "1", "b": "0", "c": "1"},
                {"a": "1", "b": "1", "c": "0"},
            ],
            rows,
        )
        self.assertEqual(set(), uncovered)

    def test_vertical_growth_adds_rows_for_pairs_horizontal_growth_misses(self) -> None:
        domains = {
            "a": ["0", "1"],
            "b": ["0", "1"],
            "c": ["0", "1", "2"],
        }
        horizontal_rows, uncovered = horizontal_growth(
            initial_construction(domains), domains, "c"
        )

        self.assertEqual(4, len(uncovered))
        completed_rows = vertical_growth(
            horizontal_rows, domains, "c", uncovered
        )
        report = verify_pair_coverage(domains, completed_rows)

        self.assertEqual(6, len(completed_rows))
        self.assertTrue(report.complete)

    def test_heterogeneous_four_factor_domains_have_complete_coverage(self) -> None:
        domains = {
            "a": ["0", "1"],
            "b": ["A", "B", "C"],
            "c": ["true", "false"],
            "d": ["x", "y", "z", "w"],
        }

        rows = generate_pairwise(domains)

        self.assertTrue(verify_pair_coverage(domains, rows).complete)

    def test_generation_is_deterministic_and_does_not_mutate_domains(self) -> None:
        domains = {
            "a": ["0", "1"],
            "b": ["A", "B", "C"],
            "c": ["true", "false"],
        }
        original = copy.deepcopy(domains)

        first = generate_pairwise(domains)
        second = generate_pairwise(domains)

        self.assertEqual(first, second)
        self.assertEqual(original, domains)

    def test_varied_domain_shapes_are_complete_and_have_unique_rows(self) -> None:
        shapes = [
            [2, 3],
            [2, 3, 4],
            [1, 2, 3, 4],
            [2, 3, 2, 4, 3],
            [3, 2, 3, 2, 4, 2],
        ]
        for shape in shapes:
            with self.subTest(shape=shape):
                domains = {
                    "p{}".format(index): [
                        "v{}".format(value) for value in range(size)
                    ]
                    for index, size in enumerate(shape)
                }

                rows = generate_pairwise(domains)

                self.assertTrue(verify_pair_coverage(domains, rows).complete)
                row_keys = {
                    tuple(row[factor] for factor in domains) for row in rows
                }
                self.assertEqual(len(rows), len(row_keys))

    def test_lang1_semantic_model_is_complete_and_keeps_mandatory_seed(self) -> None:
        seed = {
            "Prefix": "ZeroX",
            "ValueType": "HexDigits",
            "Suffix": "None",
            "Length": "Normal",
        }

        rows = generate_pairwise(NUMBER_UTILS_FACTORS, [seed])
        report = verify_pair_coverage(NUMBER_UTILS_FACTORS, rows)

        self.assertTrue(report.complete)
        self.assertEqual(194, report.expected_pair_count)
        self.assertIn(seed, rows)
        row_keys = {
            tuple(row[factor] for factor in NUMBER_UTILS_FACTORS) for row in rows
        }
        self.assertEqual(len(rows), len(row_keys))

    def test_missing_mandatory_seed_is_appended_once(self) -> None:
        domains = {"a": ["0", "1"], "b": ["0", "1"], "c": ["0", "1"]}
        seed = {"a": "0", "b": "0", "c": "1"}

        rows = generate_pairwise(domains, [seed, seed])

        self.assertEqual(seed, rows[-1])
        self.assertEqual(1, rows.count(seed))
        self.assertTrue(verify_pair_coverage(domains, rows).complete)

    def test_seed_value_outside_domain_is_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "outside domain"):
            generate_pairwise(
                {"a": ["0"], "b": ["1"]},
                [{"a": "invalid", "b": "1"}],
            )

    def test_single_factor_covers_every_value(self) -> None:
        self.assertEqual(
            [{"only": "A"}, {"only": "B"}],
            generate_pairwise({"only": ["A", "B"]}),
        )

    def test_empty_domains_are_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "At least one factor"):
            generate_pairwise({})


if __name__ == "__main__":
    unittest.main()
