"""Tests for metadata-driven Defects4J catalog generation."""

from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from runner.generate_catalog import generate_catalog, parse_defects4j_info


INFO = """Summary of configuration for Project: Demo
    Project ID: Demo

Summary for Bug: Demo-7
--------------------------------------------------------------------------------
Bug report id:
DEMO-42
--------------------------------------------------------------------------------
Bug report url:
https://example.test/DEMO-42
--------------------------------------------------------------------------------
Root cause in triggering tests:
 - example.SampleTest::testOne
   --> java.lang.AssertionError
 - example.OtherTest::testTwo
   --> java.lang.AssertionError
--------------------------------------------------------------------------------
List of modified sources:
 - example.Sample
 - example.Other
--------------------------------------------------------------------------------
"""


class GenerateCatalogTests(unittest.TestCase):
    def test_parser_keeps_all_sources_and_triggering_tests(self) -> None:
        entry = parse_defects4j_info(INFO, "Demo_7b")

        self.assertEqual(7, entry["bug_id"])
        self.assertEqual("example.Sample", entry["target_class"])
        self.assertEqual(
            ["example.Sample", "example.Other"], entry["modified_sources"]
        )
        self.assertEqual(
            ["example.SampleTest::testOne", "example.OtherTest::testTwo"],
            entry["trigger_tests"],
        )

    def test_generation_requires_every_metadata_source_to_exist(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            target = root / "Demo_7b"
            target.mkdir()
            (target / "defects4j_info.txt").write_text(INFO, encoding="utf-8")
            (target / "Sample.java").write_text(
                "package example; public class Sample {}", encoding="utf-8"
            )

            with self.assertRaisesRegex(ValueError, "Other.java"):
                generate_catalog(root)

    def test_generation_is_derived_from_metadata(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            target = root / "Demo_7b"
            target.mkdir()
            (target / "defects4j_info.txt").write_text(INFO, encoding="utf-8")
            for name in ("Sample.java", "Other.java"):
                (target / name).write_text(
                    "package example; public class {} {{}}".format(Path(name).stem),
                    encoding="utf-8",
                )

            catalog = generate_catalog(root)

            self.assertEqual(1, len(catalog))
            self.assertEqual("defects4j_info.txt", catalog[0]["generated_from"])


if __name__ == "__main__":
    unittest.main()
