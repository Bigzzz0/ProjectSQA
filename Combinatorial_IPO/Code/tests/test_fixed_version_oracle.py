"""Tests for selecting overloaded methods during fixed-version oracle collection."""

from __future__ import annotations

import unittest

from oracle.fixed_version_oracle import OracleCollectionError, select_method


INT_MIN = {
    "name": "min",
    "parameters": [
        {"name": "a", "type": "int"},
        {"name": "b", "type": "int"},
        {"name": "c", "type": "int"},
    ],
}

LONG_MIN = {
    "name": "min",
    "parameters": [
        {"name": "a", "type": "long"},
        {"name": "b", "type": "long"},
        {"name": "c", "type": "long"},
    ],
}


class OracleMethodSelectionTests(unittest.TestCase):
    def test_exact_signature_selects_one_overload(self) -> None:
        selected = select_method(
            [INT_MIN, LONG_MIN], signature="min(int, int, int)"
        )

        self.assertIs(INT_MIN, selected)

    def test_ambiguous_legacy_method_name_is_rejected(self) -> None:
        with self.assertRaisesRegex(OracleCollectionError, "found 2"):
            select_method([INT_MIN, LONG_MIN], method_name="min")

    def test_unknown_signature_is_rejected(self) -> None:
        with self.assertRaisesRegex(OracleCollectionError, "found 0"):
            select_method([INT_MIN, LONG_MIN], signature="min(short,short,short)")


if __name__ == "__main__":
    unittest.main()
