"""Tests for safe generic Java value-domain generation."""

from __future__ import annotations

import unittest

from domain.value_generator import (
    NeedsSemanticModelError,
    UnsupportedTypeError,
    get_domain_for_type,
)


class ValueGeneratorTests(unittest.TestCase):
    def test_supported_type_returns_boundary_values(self) -> None:
        self.assertEqual(
            ["0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"],
            get_domain_for_type("int"),
        )

    def test_string_domain_contains_valid_and_boundary_numeric_values(self) -> None:
        domain = get_domain_for_type("String")

        self.assertIn('"0"', domain)
        self.assertIn('"1.5"', domain)
        self.assertIn('"9223372036854775807"', domain)
        self.assertIn('"9223372036854775808"', domain)

    def test_unknown_object_never_falls_back_to_null(self) -> None:
        with self.assertRaisesRegex(NeedsSemanticModelError, "Widget"):
            get_domain_for_type("Widget")

    def test_unknown_primitive_is_unsupported(self) -> None:
        with self.assertRaisesRegex(UnsupportedTypeError, "unsigned"):
            get_domain_for_type("unsigned")
