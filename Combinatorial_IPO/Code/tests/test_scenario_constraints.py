import unittest

from scenario.constraints import satisfies, valid_combinations


class ScenarioConstraintTests(unittest.TestCase):
    def test_forbid_require_and_compare_rules(self):
        domains = {
            "population": ["10", "20"],
            "successes": ["5", "15"],
            "mode": ["normal", "strict"],
        }
        constraints = [
            {"type": "compare", "left": "successes", "op": "le", "right": "population"},
            {"type": "forbid", "when": {"population": "10", "mode": "strict"}},
            {"type": "require", "if": {"successes": "15"}, "then": {"mode": "normal"}},
        ]
        rows = valid_combinations(domains, constraints)
        self.assertTrue(rows)
        self.assertTrue(all(satisfies(row, constraints) for row in rows))
        self.assertNotIn(
            {"population": "10", "successes": "15", "mode": "normal"}, rows
        )

    def test_unknown_constraint_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "Unsupported constraint type"):
            valid_combinations({"a": ["x"], "b": ["y"]}, [{"type": "mystery"}])


if __name__ == "__main__":
    unittest.main()
