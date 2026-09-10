import json
import pathlib
import tempfile
import unittest

from runner.catalog import load_catalog
from runner.experiment import (
    build_experiment_manifest,
    resolve_experiment,
    scenario_set_fingerprint,
    selected_catalog_fingerprint,
)


IPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
PROJECT_ROOT = IPO_ROOT.parent


class ExperimentTests(unittest.TestCase):
    def _write_fixture(self, root: pathlib.Path):
        catalog_path = root / "catalog.json"
        catalog_path.write_text(
            json.dumps(
                [
                    {
                        "project": "Lang",
                        "bug_id": 1,
                        "dir": "Lang_1b",
                        "target_class": "example.Target",
                        "simple_name": "Target",
                        "modified_sources": ["example.Target"],
                        "trigger_tests": ["example.TargetTest::trigger"],
                    },
                    {
                        "project": "Lang",
                        "bug_id": 2,
                        "dir": "Lang_2b",
                        "target_class": "example.Other",
                        "simple_name": "Other",
                        "modified_sources": ["example.Other"],
                        "trigger_tests": ["example.OtherTest::trigger"],
                    },
                ]
            ),
            encoding="utf-8",
        )
        scenario_root = root / "scenarios"
        scenario_root.mkdir()
        selected_scenario = scenario_root / "Lang_1b.json"
        selected_scenario.write_text(
            json.dumps({"schema_version": 1, "status": "APPROVED"}),
            encoding="utf-8",
        )
        # An unselected scenario may coexist without changing this experiment.
        (scenario_root / "Lang_2b.json").write_text(
            json.dumps({"schema_version": 1, "status": "DRAFT"}),
            encoding="utf-8",
        )
        selected = [load_catalog(catalog_path)[0]]
        scenario_paths = {"Lang_1b": selected_scenario}
        experiment_path = root / "experiment.json"
        experiment_path.write_text(
            json.dumps(
                {
                    "schema_version": 1,
                    "experiment_id": "lang-one-only",
                    "strength": 2,
                    "targets": ["Lang_1b"],
                    "expected_summary": {
                        "target_count": 1,
                        "modified_source_count": 1,
                        "triggering_test_count": 1,
                    },
                    "input_fingerprints": {
                        "selected_catalog_sha256": selected_catalog_fingerprint(
                            selected
                        ),
                        "scenario_set_sha256": scenario_set_fingerprint(
                            ["Lang_1b"], scenario_paths
                        ),
                    },
                }
            ),
            encoding="utf-8",
        )
        return experiment_path, catalog_path, scenario_root

    def test_experiment_ignores_unselected_catalog_and_scenario_targets(self):
        with tempfile.TemporaryDirectory() as name:
            paths = self._write_fixture(pathlib.Path(name))

            resolved = resolve_experiment(*paths)

            self.assertEqual(["Lang_1b"], [t.target_key for t in resolved.targets])

    def test_selected_scenario_change_breaks_reproducibility_lock(self):
        with tempfile.TemporaryDirectory() as name:
            root = pathlib.Path(name)
            experiment, catalog, scenarios = self._write_fixture(root)
            (scenarios / "Lang_1b.json").write_text(
                json.dumps({"schema_version": 1, "status": "DRAFT"}),
                encoding="utf-8",
            )

            with self.assertRaisesRegex(ValueError, "fingerprint"):
                resolve_experiment(experiment, catalog, scenarios)

    def test_manifest_builder_derives_counts_instead_of_hard_coding_them(self):
        with tempfile.TemporaryDirectory() as name:
            root = pathlib.Path(name)
            _, catalog, scenarios = self._write_fixture(root)

            manifest = build_experiment_manifest(
                "expanded", ["Lang_1b", "Lang_2b"], catalog, scenarios
            )

            self.assertEqual(2, manifest["expected_summary"]["target_count"])
            self.assertEqual(
                2, manifest["expected_summary"]["modified_source_count"]
            )
            self.assertEqual(
                2, manifest["expected_summary"]["triggering_test_count"]
            )

    def test_repository_experiment_can_be_rebuilt_exactly(self):
        experiment_path = (
            IPO_ROOT
            / "Configuration"
            / "experiments"
            / "round2-17-targets.json"
        )
        saved = json.loads(experiment_path.read_text(encoding="utf-8"))

        rebuilt = build_experiment_manifest(
            saved["experiment_id"],
            saved["targets"],
            PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json",
            IPO_ROOT / "Configuration" / "targets",
        )

        self.assertEqual(saved, rebuilt)


if __name__ == "__main__":
    unittest.main()
