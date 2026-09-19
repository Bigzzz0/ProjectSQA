from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from oracle.fixed_version_oracle import OracleCollectionError
from runner.all_class_pipeline import (
    audit_class,
    generate_class,
    select_canary,
    validate_verified_manifest,
)
from runner.catalog import CatalogTarget


class AllClassPipelineTests(unittest.TestCase):
    def _target(self) -> CatalogTarget:
        return CatalogTarget(
            project="Demo", bug_id=1, directory="Demo_1b",
            target_class="example.Sample", simple_name="Sample",
            modified_sources=("example.Sample",),
            trigger_tests=("example.SampleTest::testChanged",),
        )

    def _plan(self):
        return {
            "schema_version": 2, "project": "Demo", "bug_id": 1,
            "target_class": "example.Sample", "source_presence": "PRESENT_IN_BOTH",
            "status": "AUTO_READY", "audit_hash": "audit-1",
            "callables": [
                {
                    "kind": "method", "name": "changed", "signature": "changed(int,int)",
                    "return_type": "int", "static": True, "visibility": "public",
                    "parameters": [{"name": "a", "type": "int"}, {"name": "b", "type": "int"}],
                    "status": "AUTO_READY", "factor_names": ["a", "b"],
                    "factor_domains": {"a": ["0", "1"], "b": ["0", "1"]},
                    "adapters": {"a": "generic", "b": "generic"},
                }
            ],
        }

    def test_audit_uses_diff_evidence_and_does_not_select_untouched_method(self) -> None:
        buggy = """package example;
public class Sample {
  public static int changed(int a, int b) { return a; }
  public static int untouched(int a, int b) { return b; }
}
"""
        fixed = buggy.replace("return a", "return Math.min(a, b)")
        plan = audit_class(
            self._target(), "example.Sample",
            {
                "classes": {"example.Sample": {
                    "buggy_source": buggy, "fixed_source": fixed,
                    "source_presence": "PRESENT_IN_BOTH",
                }},
                "trigger_sources": [],
            },
        )
        callables = {item["signature"]: item for item in plan["callables"]}
        self.assertEqual("AUTO_READY", callables["changed(int,int)"]["status"])
        self.assertEqual("NEEDS_ENTRY_POINT", callables["untouched(int,int)"]["status"])

    def test_canary_is_deterministic_union(self) -> None:
        records = [
            {**self._plan(), "project": "B", "source_presence": "ADDED_IN_FIXED"},
            {**self._plan(), "project": "A", "target_class": "example.A"},
            {**self._plan(), "project": "A", "target_class": "example.Z"},
        ]
        selected = select_canary(records)
        self.assertEqual(
            [("A", "example.A"), ("B", "example.Sample")],
            [(item["project"], item["target_class"]) for item in selected],
        )

    def test_class_is_published_only_after_fixed_verification(self) -> None:
        def oracle(**kwargs):
            return [
                {"id": index, "outcome": "RETURN", "type": "java.lang.Integer", "value_or_message": "0"}
                for index, _ in enumerate(kwargs["combinations"], start=1)
            ]

        with tempfile.TemporaryDirectory() as name:
            output = Path(name)
            with patch("runner.all_class_pipeline.collect_fixed_oracle", side_effect=oracle), patch(
                "runner.all_class_pipeline.verify_suite", return_value="JUnit version 4\nOK (4 tests)"
            ):
                record = generate_class(self._plan(), output, "experiment", "defects4j")
            self.assertEqual("FIXED_VERIFIED", record["status"])
            self.assertTrue((output / record["suite_path"]).is_file())
            self.assertEqual(100.0, record["methods"][0]["pair_coverage_percent"])

    def test_failed_verification_never_publishes(self) -> None:
        def oracle(**kwargs):
            return [
                {"id": index, "outcome": "RETURN", "type": "java.lang.Integer", "value_or_message": "0"}
                for index, _ in enumerate(kwargs["combinations"], start=1)
            ]

        with tempfile.TemporaryDirectory() as name:
            output = Path(name)
            with patch("runner.all_class_pipeline.collect_fixed_oracle", side_effect=oracle), patch(
                "runner.all_class_pipeline.verify_suite", side_effect=OracleCollectionError("failed")
            ):
                record = generate_class(self._plan(), output, "experiment", "defects4j")
            self.assertEqual("GENERATION_OR_VERIFICATION_ERROR", record["status"])
            self.assertFalse((output / "TestCode").exists())

    def test_resume_reuses_hash_valid_oracle_after_verification_failure(self) -> None:
        def oracle(**kwargs):
            return [
                {"id": index, "outcome": "RETURN", "type": "java.lang.Integer", "value_or_message": "0"}
                for index, _ in enumerate(kwargs["combinations"], start=1)
            ]

        with tempfile.TemporaryDirectory() as name:
            output = Path(name)
            with patch("runner.all_class_pipeline.collect_fixed_oracle", side_effect=oracle), patch(
                "runner.all_class_pipeline.verify_suite", side_effect=OracleCollectionError("failed")
            ):
                generate_class(self._plan(), output, "experiment", "defects4j")
            with patch(
                "runner.all_class_pipeline.collect_fixed_oracle",
                side_effect=AssertionError("oracle must be reused"),
            ), patch("runner.all_class_pipeline.verify_suite", return_value="OK (4 tests)"):
                record = generate_class(
                    self._plan(), output, "experiment", "defects4j", resume=True
                )
            self.assertEqual("FIXED_VERIFIED", record["status"])
            self.assertTrue(record["methods"][0]["reused_oracle"])

    def test_verified_manifest_rejects_changed_suite(self) -> None:
        with tempfile.TemporaryDirectory() as name:
            output = Path(name)
            suite = output / "TestCode" / "all-modified-classes" / "Demo_1b" / "Sample_IPOTest.java"
            suite.parent.mkdir(parents=True)
            suite.write_text("original", encoding="utf-8")
            manifest = suite.parent.parent / "verified_suites_manifest.json"
            manifest.write_text(
                json.dumps({"records": [{
                    "project": "Demo", "bug_id": 1, "target_class": "example.Sample",
                    "status": "FIXED_VERIFIED", "generation_backend": "native_ipo",
                    "suite_path": str(suite.relative_to(output)), "suite_sha256": "wrong",
                    "methods": [{"pair_coverage_percent": 100.0}],
                }]}),
                encoding="utf-8",
            )
            result = validate_verified_manifest(output)
            self.assertFalse(result["valid"])


if __name__ == "__main__":
    unittest.main()
