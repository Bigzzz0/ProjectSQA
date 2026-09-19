from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from runner.generate_catalog import normalize_all_bugs_catalog
from runner.catalog import load_catalog
from runner.catalog_snapshot import build_snapshot, write_snapshot
from runner.all_class_experiment import build_all_class_manifest


class CatalogNormalizationTests(unittest.TestCase):
    def test_resources_and_stack_traces_are_separated(self) -> None:
        catalog = normalize_all_bugs_catalog(
            [
                {
                    "project": "Codec",
                    "bug_id": 14,
                    "target_classes": [
                        "example.Parser",
                        "src.main.resources.example.rules.txt",
                    ],
                    "trigger_tests": [
                        "--- example.ParserTest::testRule",
                        "java.lang.AssertionError",
                        "at example.ParserTest.testRule(ParserTest.java:10)",
                    ],
                }
            ]
        )
        self.assertEqual(["example.Parser"], catalog[0]["modified_sources"])
        self.assertEqual(
            ["src.main.resources.example.rules.txt"],
            catalog[0]["modified_resources"],
        )
        self.assertEqual(["example.ParserTest::testRule"], catalog[0]["trigger_tests"])

    def test_snapshot_is_deterministic_and_does_not_modify_master(self) -> None:
        raw = [
            {
                "project": "Demo",
                "bug_id": 1,
                "target_classes": ["example.Sample", "src.main.resources.rules.xml"],
                "trigger_tests": ["--- example.SampleTest::testValue", "at stack.Trace"],
            }
        ]
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            master = root / "all_bugs_catalog.json"
            master.write_text(json.dumps(raw), encoding="utf-8")
            original = master.read_bytes()

            first = build_snapshot(master)
            second = build_snapshot(master)
            output = root / "snapshot.json"
            write_snapshot(first, output)

            self.assertEqual(first, second)
            self.assertEqual(original, master.read_bytes())
            self.assertEqual(1, first["summary"]["modified_java_source_count"])
            self.assertEqual(1, first["summary"]["modified_resource_count"])
            self.assertEqual("example.Sample", load_catalog(output)[0].target_class)
            experiment = build_all_class_manifest(output, "fixture")
            self.assertEqual(1, experiment["expected_summary"]["bug_target_count"])
            self.assertEqual(1, experiment["expected_summary"]["modified_java_source_count"])


if __name__ == "__main__":
    unittest.main()
