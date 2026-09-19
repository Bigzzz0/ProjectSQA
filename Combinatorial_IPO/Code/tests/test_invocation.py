from __future__ import annotations

import unittest

from domain.invocation import render_invocation
from generator.junit_generator import synthesize_junit_suite
from oracle.fixed_version_oracle import generate_collector_source, parse_oracle_output


class InvocationTests(unittest.TestCase):
    def test_factorized_receiver_is_used_by_oracle_and_junit(self) -> None:
        method = {
            "name": "add",
            "return_type": "int",
            "static": False,
            "parameters": [{"name": "value", "type": "int"}],
            "factor_names": ["receiver__seed", "value"],
            "receiver_strategy": {
                "expression_template": "new Sample({receiver__seed})"
            },
        }
        row = {"receiver__seed": "1", "value": "2"}
        expected = "(new Sample(1)).add(2)"
        self.assertEqual(expected, render_invocation("Sample", method, row))
        self.assertIn(expected, generate_collector_source("example", "Sample", method, [row]))
        suite = synthesize_junit_suite(
            "example",
            "Sample",
            [(method, [row], [{"id": 1, "outcome": "RETURN", "type": "java.lang.Integer", "value_or_message": "3"}])],
        )
        self.assertIn(expected, suite)
        self.assertIn("@Test(timeout = 4000)", suite)

    def test_collector_emits_real_tsv_separators(self) -> None:
        source = generate_collector_source(
            "", "Sample",
            {"name": "value", "return_type": "int", "static": True, "parameters": []},
            [{}],
        )
        line = next(item for item in source.splitlines() if "RETURN" in item)
        self.assertIn("\tRETURN\t", line)
        self.assertNotIn(r"\\tRETURN\\t", line)

    def test_empty_oracle_message_survives_output_trimming(self) -> None:
        outcomes = parse_oracle_output(
            "1\tTHROW\tjava.lang.NullPointerException\t=", 1
        )
        self.assertEqual("", outcomes[0]["value_or_message"])


if __name__ == "__main__":
    unittest.main()
