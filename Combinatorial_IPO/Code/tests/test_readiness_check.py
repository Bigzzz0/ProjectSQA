"""Tests for the catalog-loop readiness decision."""

from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from runner.experiment import build_experiment_manifest
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

    def _write_selected_experiment(self, root: Path):
        catalog = root / "catalog.json"
        catalog.write_text(
            json.dumps(
                [
                    {
                        "project": "Lang",
                        "bug_id": bug_id,
                        "dir": "Lang_{}b".format(bug_id),
                        "target_class": "example.Target{}".format(bug_id),
                        "simple_name": "Target{}".format(bug_id),
                        "modified_sources": ["example.Target{}".format(bug_id)],
                        "trigger_tests": [
                            "example.Target{}Test::trigger".format(bug_id)
                        ],
                    }
                    for bug_id in (1, 2)
                ]
            ),
            encoding="utf-8",
        )
        scenarios = root / "scenarios"
        scenarios.mkdir()
        (scenarios / "Lang_1b.json").write_text(
            json.dumps(
                {
                    "schema_version": 1,
                    "status": "APPROVED",
                    "project": "Lang",
                    "bug_id": 1,
                    "suite": {
                        "package": "example",
                        "class_name": "Lang_1b_IPOTest",
                    },
                    "scenarios": [
                        {
                            "id": "boundary",
                            "target_classes": ["example.Target1"],
                            "entrypoint": "Target1.check(int,int)",
                            "invocation_kind": "static",
                            "factors": {
                                "left": {"zero": "0", "one": "1"},
                                "right": {"zero": "0", "one": "1"},
                            },
                            "constraints": [],
                            "mandatory_seeds": [],
                            "body": "assertEquals({{left}}, {{right}});",
                            "oracle": {"mode": "invariant"},
                            "evidence": {
                                "patch_file": "Lang/patches/1.src.patch",
                                "trigger_tests": ["example.Target1Test::trigger"],
                                "rationale": "Synthetic readiness fixture.",
                            },
                        }
                    ],
                }
            ),
            encoding="utf-8",
        )
        experiment = root / "experiment.json"
        experiment.write_text(
            json.dumps(
                build_experiment_manifest(
                    "lang-one-only", ["Lang_1b"], catalog, scenarios
                )
            ),
            encoding="utf-8",
        )
        return catalog, scenarios, experiment

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

    def test_readiness_ignores_unselected_feasibility_records(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            representatives = root / "representatives.json"
            representatives.write_text("[]", encoding="utf-8")
            feasibility = root / "feasibility.json"
            feasibility.write_text(
                json.dumps(
                    {
                        "generation_performed": False,
                        "target_count": 2,
                        "bug_target_count": 2,
                        "source_target_count": 2,
                        "target_status_counts": {"AUDITED": 2},
                        "targets": [
                            {"project": "Lang", "bug_id": 1, "status": "AUDITED"},
                            {"project": "Lang", "bug_id": 2, "status": "AUDITED"},
                        ],
                    }
                ),
                encoding="utf-8",
            )
            catalog, scenarios, experiment = self._write_selected_experiment(root)

            report = check_readiness(
                root,
                representatives,
                feasibility,
                catalog_path=catalog,
                scenario_root=scenarios,
                experiment_path=experiment,
            )

            self.assertTrue(report["ready_to_start_loop"])
            self.assertEqual(1, report["experiment_target_count"])
            self.assertEqual([], report["catalog_issues"])


if __name__ == "__main__":
    unittest.main()
