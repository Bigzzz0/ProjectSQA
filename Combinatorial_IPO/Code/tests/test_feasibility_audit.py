"""Tests for non-generating catalog feasibility audits."""

from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from runner.feasibility_audit import audit_catalog


class FeasibilityAuditTests(unittest.TestCase):
    def test_audit_classifies_methods_without_generating_artifacts(self) -> None:
        source = """package example;

public class Sample {
    public static int min(int a, int b) { return Math.min(a, b); }
    public static Object repeat(Object value, int count) { return value; }
    public int instanceValue(int left, int right) { return left + right; }
}
"""
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            target_root = root / "targets"
            target_directory = target_root / "Demo_1b"
            target_directory.mkdir(parents=True)
            (target_directory / "Sample.java").write_text(source, encoding="utf-8")
            catalog = root / "catalog.json"
            catalog.write_text(
                json.dumps(
                    [
                        {
                            "project": "Demo",
                            "bug_id": 1,
                            "dir": "Demo_1b",
                            "target_class": "example.Sample",
                            "simple_name": "Sample",
                        }
                    ]
                ),
                encoding="utf-8",
            )

            report = audit_catalog(catalog, target_root)

            self.assertFalse(report["generation_performed"])
            self.assertEqual(1, report["target_count"])
            self.assertEqual(1, report["bug_target_count"])
            self.assertEqual(1, report["source_target_count"])
            self.assertEqual(
                {
                    "CANDIDATE": 1,
                    "NEEDS_SEMANTIC_MODEL": 1,
                    "UNSUPPORTED": 1,
                },
                report["method_status_counts"],
            )
            self.assertFalse((root / "Models").exists())
            self.assertFalse((root / "Result_Round1").exists())
            self.assertFalse((root / "TestCode").exists())
