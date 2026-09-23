from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

from runner.all_class_pipeline import (
    _code_hash,
    _generation_toolchain_hash,
    main,
    run_generation,
)
from runner.catalog_snapshot import file_sha256


class BatchControllerGatesTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.output_root = Path(self.temp_dir.name)
        self.results_dir = self.output_root / "Results"
        self.results_dir.mkdir(parents=True)

    def tearDown(self) -> None:
        self.temp_dir.cleanup()

    def _create_mock_inventory(self, records: list[dict]) -> None:
        inventory = {
            "schema_version": 2,
            "experiment_id": "test-exp",
            "class_instance_count": len(records),
            "records": records,
        }
        (self.results_dir / "inventory.json").write_text(
            json.dumps(inventory), encoding="utf-8"
        )

    # -------------------------------------------------------------------------
    # Gate 1: Canary Gate Enforcement & Non-Zero Exit
    # -------------------------------------------------------------------------
    def test_canary_failure_sets_canary_passed_false(self) -> None:
        """When a canary target fails verification, manifest['canary_passed'] must be False."""
        plan = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "AUTO_READY",
            "audit_hash": "audit-hash-1",
            "source_presence": "PRESENT_IN_BOTH",
        }
        self._create_mock_inventory([plan])

        failed_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "GENERATION_OR_VERIFICATION_ERROR",
            "error": "Simulated verification failure",
        }

        with patch("runner.all_class_pipeline.select_canary", return_value=[plan]), patch(
            "runner.all_class_pipeline.generate_class", return_value=failed_record
        ):
            manifest = run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="canary", project=None, bug=None, resume=False,
            )

        self.assertFalse(manifest.get("canary_passed"))
        self.assertEqual(1, len(manifest.get("canary_failures", [])))
        self.assertEqual("example.Sample", manifest["canary_failures"][0]["target_class"])

    def test_canary_success_sets_canary_passed_true(self) -> None:
        """When all canary targets pass verification, manifest['canary_passed'] must be True."""
        plan = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "AUTO_READY",
            "audit_hash": "audit-hash-1",
            "source_presence": "PRESENT_IN_BOTH",
        }
        self._create_mock_inventory([plan])

        success_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "verified_method_count": 1,
            "total_candidates": 1,
            "audit_hash": "audit-hash-1",
            "generation_toolchain_hash": _generation_toolchain_hash(),
        }

        with patch("runner.all_class_pipeline.select_canary", return_value=[plan]), patch(
            "runner.all_class_pipeline.generate_class", return_value=success_record
        ):
            manifest = run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="canary", project=None, bug=None, resume=False,
            )

        self.assertTrue(manifest.get("canary_passed"))
        self.assertEqual(0, len(manifest.get("canary_failures", [])))

    def test_canary_failure_causes_main_to_exit_nonzero(self) -> None:
        """all_class_pipeline main() must raise SystemExit(1) if canary fails."""
        mock_result = {
            "mode": "CANARY",
            "canary_passed": False,
            "canary_failures": [{"project": "Demo", "bug_id": 1, "target_class": "Sample"}],
        }
        test_args = ["prog", "--mode", "canary", "--output-root", str(self.output_root)]

        with patch("sys.argv", test_args), patch(
            "runner.all_class_pipeline.run_generation", return_value=mock_result
        ):
            with self.assertRaises(SystemExit) as ctx:
                main()
            self.assertEqual(1, ctx.exception.code)

    def test_canary_success_causes_main_to_exit_zero(self) -> None:
        """all_class_pipeline main() must not raise SystemExit if canary succeeds."""
        mock_result = {
            "mode": "CANARY",
            "canary_passed": True,
            "canary_failures": [],
        }
        test_args = ["prog", "--mode", "canary", "--output-root", str(self.output_root)]

        with patch("sys.argv", test_args), patch(
            "runner.all_class_pipeline.run_generation", return_value=mock_result
        ):
            # Should exit cleanly without raising SystemExit
            try:
                main()
            except SystemExit as exc:
                self.assertEqual(0, exc.code)

    # -------------------------------------------------------------------------
    # Gate 2: Strict Resume Validation (audit_hash & generation_toolchain_hash)
    # -------------------------------------------------------------------------
    def test_resume_skips_when_suite_audit_and_toolchain_hashes_match(self) -> None:
        """Resume must SKIP generation only when suite, audit_hash, and toolchain_hash all match."""
        plan = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "AUTO_READY",
            "audit_hash": "audit-v1",
        }
        self._create_mock_inventory([plan])

        suite_file = self.output_root / "TestCode" / "Demo_1b" / "Sample_IPOTest.java"
        suite_file.parent.mkdir(parents=True)
        suite_file.write_text("// dummy test suite content", encoding="utf-8")
        current_suite_hash = file_sha256(suite_file)
        current_toolchain = _generation_toolchain_hash()

        verified_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "suite_path": str(suite_file.relative_to(self.output_root)).replace("\\", "/"),
            "suite_sha256": current_suite_hash,
            "audit_hash": "audit-v1",
            "generation_toolchain_hash": current_toolchain,
        }
        (self.results_dir / "verified_suites_manifest.json").write_text(
            json.dumps({"records": [verified_record]}), encoding="utf-8"
        )

        mock_generate = MagicMock()
        with patch("runner.all_class_pipeline.generate_class", mock_generate):
            manifest = run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="generate", project=None, bug=None, resume=True,
            )

        # generate_class should NOT have been called because everything matched
        mock_generate.assert_not_called()
        self.assertEqual(1, len(manifest["records"]))
        self.assertEqual("FIXED_VERIFIED", manifest["records"][0]["status"])

    def test_resume_regenerates_when_toolchain_hash_differs(self) -> None:
        """When toolchain hash differs (code change), target must NOT be skipped."""
        plan = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "AUTO_READY",
            "audit_hash": "audit-v1",
        }
        self._create_mock_inventory([plan])

        suite_file = self.output_root / "TestCode" / "Demo_1b" / "Sample_IPOTest.java"
        suite_file.parent.mkdir(parents=True)
        suite_file.write_text("// dummy test suite content", encoding="utf-8")
        current_suite_hash = file_sha256(suite_file)

        verified_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "suite_path": str(suite_file.relative_to(self.output_root)).replace("\\", "/"),
            "suite_sha256": current_suite_hash,
            "audit_hash": "audit-v1",
            "generation_toolchain_hash": "old-stale-toolchain-hash",
        }
        (self.results_dir / "verified_suites_manifest.json").write_text(
            json.dumps({"records": [verified_record]}), encoding="utf-8"
        )

        new_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "suite_path": str(suite_file.relative_to(self.output_root)).replace("\\", "/"),
            "suite_sha256": current_suite_hash,
            "audit_hash": "audit-v1",
            "generation_toolchain_hash": _generation_toolchain_hash(),
        }
        mock_generate = MagicMock(return_value=new_record)
        with patch("runner.all_class_pipeline.generate_class", mock_generate):
            run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="generate", project=None, bug=None, resume=True,
            )

        # Must have regenerated because toolchain changed
        mock_generate.assert_called_once()

    def test_resume_regenerates_when_audit_hash_differs(self) -> None:
        """When audit_hash differs (model/params changed), target must NOT be skipped."""
        plan = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "AUTO_READY",
            "audit_hash": "new-audit-v2",
        }
        self._create_mock_inventory([plan])

        suite_file = self.output_root / "TestCode" / "Demo_1b" / "Sample_IPOTest.java"
        suite_file.parent.mkdir(parents=True)
        suite_file.write_text("// dummy test suite content", encoding="utf-8")
        current_suite_hash = file_sha256(suite_file)
        current_toolchain = _generation_toolchain_hash()

        verified_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "suite_path": str(suite_file.relative_to(self.output_root)).replace("\\", "/"),
            "suite_sha256": current_suite_hash,
            "audit_hash": "old-audit-v1",  # mismatch with plan
            "generation_toolchain_hash": current_toolchain,
        }
        (self.results_dir / "verified_suites_manifest.json").write_text(
            json.dumps({"records": [verified_record]}), encoding="utf-8"
        )

        new_record = {
            "project": "Demo",
            "bug_id": 1,
            "target_class": "example.Sample",
            "status": "FIXED_VERIFIED",
            "suite_path": str(suite_file.relative_to(self.output_root)).replace("\\", "/"),
            "suite_sha256": current_suite_hash,
            "audit_hash": "new-audit-v2",
            "generation_toolchain_hash": current_toolchain,
        }
        mock_generate = MagicMock(return_value=new_record)
        with patch("runner.all_class_pipeline.generate_class", mock_generate):
            run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="generate", project=None, bug=None, resume=True,
            )

        # Must have regenerated because audit_hash changed
        mock_generate.assert_called_once()

    # -------------------------------------------------------------------------
    # Gate 3: Complete & Atomic Generation Accounting
    # -------------------------------------------------------------------------
    def test_all_generation_outcomes_are_recorded_in_manifests(self) -> None:
        """Every generated class (success, failure, skipped) must be recorded in generation_manifest.json."""
        plan1 = {
            "project": "Demo", "bug_id": 1, "target_class": "example.Success",
            "status": "AUTO_READY", "audit_hash": "hash-1",
        }
        plan2 = {
            "project": "Demo", "bug_id": 1, "target_class": "example.Failure",
            "status": "AUTO_READY", "audit_hash": "hash-2",
        }
        plan3 = {
            "project": "Demo", "bug_id": 1, "target_class": "example.NotReady",
            "status": "NEEDS_ADAPTER", "audit_hash": "hash-3",
        }
        self._create_mock_inventory([plan1, plan2, plan3])

        rec1 = {
            "project": "Demo", "bug_id": 1, "target_class": "example.Success",
            "status": "FIXED_VERIFIED", "verified_method_count": 1, "total_candidates": 1,
        }
        rec2 = {
            "project": "Demo", "bug_id": 1, "target_class": "example.Failure",
            "status": "GENERATION_OR_VERIFICATION_ERROR", "error": "Assertion error in test",
        }

        def side_effect(plan, *args, **kwargs):
            if plan["target_class"] == "example.Success":
                return rec1
            return rec2

        with patch("runner.all_class_pipeline.generate_class", side_effect=side_effect):
            manifest = run_generation(
                self.output_root, "test-exp", "defects4j",
                mode="generate", project=None, bug=None, resume=False,
            )

        # Verify generation_manifest.json on disk
        gen_file = self.results_dir / "generation_manifest.json"
        self.assertTrue(gen_file.is_file())
        disk_manifest = json.loads(gen_file.read_text(encoding="utf-8"))

        self.assertEqual(3, disk_manifest["class_instance_count"])
        self.assertEqual(3, disk_manifest["processed_this_run"])
        self.assertEqual(1, disk_manifest["status_counts"].get("FIXED_VERIFIED"))
        self.assertEqual(1, disk_manifest["status_counts"].get("GENERATION_OR_VERIFICATION_ERROR"))
        self.assertEqual(1, disk_manifest["status_counts"].get("SKIPPED_NOT_READY"))

        # Verify verified_suites_manifest only contains the FIXED_VERIFIED class
        ver_file = self.results_dir / "verified_suites_manifest.json"
        self.assertTrue(ver_file.is_file())
        ver_manifest = json.loads(ver_file.read_text(encoding="utf-8"))
        self.assertEqual(1, len(ver_manifest["records"]))
        self.assertEqual("example.Success", ver_manifest["records"][0]["target_class"])

        # Verify failures.log captured the failure
        failures_log = self.results_dir / "logs" / "failures.log"
        self.assertTrue(failures_log.is_file())
        log_content = failures_log.read_text(encoding="utf-8")
        self.assertIn("example.Failure", log_content)
        self.assertIn("Assertion error in test", log_content)


if __name__ == "__main__":
    unittest.main()
