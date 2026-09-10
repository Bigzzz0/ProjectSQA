"""Tests for the catalog-loop readiness decision."""

from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from runner.readiness_check import check_readiness


class ReadinessCheckTests(unittest.TestCase):
    def _write_minimum_inputs(self, root: Path) -> tuple[Path, Path]:
        representatives = root / "representatives.json"
        representatives.write_text("[]", encoding="utf-8")
        feasibility = root / "feasibility.json"
        feasibility.write_text(
            json.dumps(
                {
                    "generation_performed": False,
                    "target_count": 17,
                    "bug_target_count": 17,
                    "source_target_count": 17,
                    "target_status_counts": {"AUDITED": 17},
                }
            ),
            encoding="utf-8",
        )
        return representatives, feasibility

    def test_ready_before_catalog_loop_starts(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            representatives, feasibility = self._write_minimum_inputs(root)

            report = check_readiness(root, representatives, feasibility)

            self.assertTrue(report["ready_to_start_loop"])
            self.assertFalse(report["loop_started"])
            self.assertIsNone(report["loop_state"])

    def test_not_ready_after_catalog_loop_start_is_recorded(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            representatives, feasibility = self._write_minimum_inputs(root)
            state_path = root / "Result_Round2" / "catalog_loop_state.json"
            state_path.parent.mkdir(parents=True)
            state_path.write_text(
                json.dumps({"started": True, "status": "STARTED"}),
                encoding="utf-8",
            )

            report = check_readiness(root, representatives, feasibility)

            self.assertFalse(report["ready_to_start_loop"])
            self.assertTrue(report["loop_started"])
            self.assertTrue(report["loop_in_progress"])
            self.assertEqual("STARTED", report["loop_state"]["status"])

    def test_verified_previous_run_does_not_block_reproduction(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            representatives, feasibility = self._write_minimum_inputs(root)
            state_path = root / "Result_Round2" / "catalog_loop_state.json"
            state_path.parent.mkdir(parents=True)
            state_path.write_text(
                json.dumps({"started": True, "status": "VERIFIED"}),
                encoding="utf-8",
            )

            report = check_readiness(root, representatives, feasibility)

            self.assertTrue(report["ready_to_start_loop"])
            self.assertTrue(report["loop_started"])
            self.assertFalse(report["loop_in_progress"])


if __name__ == "__main__":
    unittest.main()
