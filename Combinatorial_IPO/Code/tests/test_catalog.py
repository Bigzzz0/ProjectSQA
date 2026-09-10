"""Tests for safe explicit target-catalog resolution."""

from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from runner.catalog import load_catalog, resolve_catalog_targets


def _entry(**overrides):
    entry = {
        "project": "Demo",
        "bug_id": 1,
        "dir": "Demo_1b",
        "target_class": "example.Sample",
        "simple_name": "Sample",
    }
    entry.update(overrides)
    return entry


class CatalogTests(unittest.TestCase):
    def test_catalog_resolves_only_the_named_source(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            target_directory = root / "targets" / "Demo_1b"
            target_directory.mkdir(parents=True)
            expected = target_directory / "Sample.java"
            expected.write_text("public class Sample {}", encoding="utf-8")
            (target_directory / "Other.java").write_text(
                "public class Other {}", encoding="utf-8"
            )
            catalog = root / "catalog.json"
            catalog.write_text(json.dumps([_entry()]), encoding="utf-8")

            resolved, issues = resolve_catalog_targets(catalog, root / "targets")

            self.assertEqual([], issues)
            self.assertEqual([expected], [item.source_file for item in resolved])

    def test_missing_catalog_source_is_reported_without_guessing(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            target_directory = root / "targets" / "Demo_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "Other.java").write_text(
                "public class Other {}", encoding="utf-8"
            )
            catalog = root / "catalog.json"
            catalog.write_text(json.dumps([_entry()]), encoding="utf-8")

            resolved, issues = resolve_catalog_targets(catalog, root / "targets")

            self.assertEqual([], resolved)
            self.assertEqual("CATALOG_MISMATCH", issues[0]["status"])
            self.assertEqual(["Other.java"], issues[0]["available_sources"])

    def test_duplicate_project_bug_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            catalog = Path(temporary_directory) / "catalog.json"
            catalog.write_text(
                json.dumps([_entry(), _entry(simple_name="Other")]),
                encoding="utf-8",
            )

            with self.assertRaisesRegex(ValueError, "Duplicate"):
                load_catalog(catalog)
