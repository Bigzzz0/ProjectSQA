"""Characterization tests for the Microsoft PICT adapter behavior."""

from __future__ import annotations

import unittest

from backends.pict_backend import (
    PictGenerationError,
    build_pict_model,
    build_pict_seed,
    generate_pairwise,
    parse_pict_output,
)


class PictBackendTests(unittest.TestCase):
    def test_build_model_uses_stable_tokens_for_java_expressions(self) -> None:
        model, lookup = build_pict_model(
            {
                "left": ["0", "Integer.MAX_VALUE"],
                "right": ['"a"', '"b"'],
            }
        )

        self.assertEqual(
            "left: P000V000, P000V001\n"
            "right: P001V000, P001V001\n",
            model,
        )
        self.assertEqual("Integer.MAX_VALUE", lookup["left"]["P000V001"])
        self.assertEqual('"a"', lookup["right"]["P001V000"])

    def test_parse_output_decodes_tokens_in_parameter_order(self) -> None:
        _, lookup = build_pict_model(
            {"left": ["0", "1"], "right": ["true", "false"]}
        )
        output = (
            "left\tright\n"
            "P000V000\tP001V001\n"
            "P000V001\tP001V000\n"
        )

        self.assertEqual(
            [
                {"left": "0", "right": "false"},
                {"left": "1", "right": "true"},
            ],
            parse_pict_output(output, lookup),
        )

    def test_parse_output_rejects_unknown_tokens(self) -> None:
        _, lookup = build_pict_model({"left": ["0"], "right": ["true"]})

        with self.assertRaisesRegex(PictGenerationError, "Unknown token"):
            parse_pict_output(
                "left\tright\nP000V999\tP001V000\n",
                lookup,
            )

    def test_seed_uses_the_same_tokens_as_the_model(self) -> None:
        _, lookup = build_pict_model({"left": ["0", "1"], "right": ["true"]})

        seed = build_pict_seed(
            [{"left": "1", "right": "true"}],
            lookup,
        )

        self.assertEqual(
            "left\tright\nP000V001\tP001V000\n",
            seed,
        )

    def test_duplicate_domain_values_are_rejected(self) -> None:
        with self.assertRaisesRegex(ValueError, "duplicate values"):
            build_pict_model({"left": ["0", "0"]})

    def test_single_factor_covers_every_value_without_running_pict(self) -> None:
        self.assertEqual(
            [{"only": "A"}, {"only": "B"}],
            generate_pairwise(
                {"only": ["A", "B"]},
                pict_executable="executable-that-does-not-exist",
            ),
        )


if __name__ == "__main__":
    unittest.main()
