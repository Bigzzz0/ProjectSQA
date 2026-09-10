import json
import pathlib
import tempfile
import unittest

from runner.catalog import load_catalog
from runner.experiment import (
    scenario_set_fingerprint,
    selected_catalog_fingerprint,
)
from runner.scenario_catalog import load_scenario_catalog, run_scenario_catalog


IPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
PROJECT_ROOT = IPO_ROOT.parent
EXPERIMENT = IPO_ROOT / "Configuration" / "experiments" / "round2-17-targets.json"


def _write_experiment_fixture(root: pathlib.Path, target_count: int):
    catalog_path = root / "catalog.json"
    scenario_root = root / "scenarios"
    scenario_root.mkdir()
    entries = []
    keys = []
    for index in range(1, target_count + 1):
        project = "Project{}".format(index)
        key = "{}_1b".format(project)
        target_class = "example.Target{}".format(index)
        trigger = "example.Target{}Test::trigger".format(index)
        keys.append(key)
        entries.append(
            {
                "project": project,
                "bug_id": 1,
                "dir": key,
                "target_class": target_class,
                "simple_name": "Target{}".format(index),
                "modified_sources": [target_class],
                "trigger_tests": [trigger],
            }
        )
        scenario = {
            "schema_version": 1,
            "status": "APPROVED",
            "project": project,
            "bug_id": 1,
            "suite": {
                "package": "example",
                "class_name": "{}_IPOTest".format(key),
            },
            "scenarios": [
                {
                    "id": "boundary",
                    "target_classes": [target_class],
                    "entrypoint": "Target{}.check(int,int)".format(index),
                    "invocation_kind": "static",
                    "factors": {
                        "left": {"zero": "0", "one": "1"},
                        "right": {"zero": "0", "one": "1"},
                    },
                    "constraints": [],
                    "mandatory_seeds": [{"left": "zero", "right": "zero"}],
                    "body": "int actual = {{left}} + {{right}};\nassertTrue(actual >= 0);",
                    "oracle": {"mode": "invariant"},
                    "evidence": {
                        "patch_file": "{}/patches/1.src.patch".format(project),
                        "trigger_tests": [trigger],
                        "rationale": "Synthetic extensibility fixture.",
                    },
                }
            ],
        }
        (scenario_root / "{}.json".format(key)).write_text(
            json.dumps(scenario), encoding="utf-8"
        )
    catalog_path.write_text(json.dumps(entries), encoding="utf-8")
    targets = load_catalog(catalog_path)
    scenario_paths = {
        key: scenario_root / "{}.json".format(key) for key in keys
    }
    experiment_path = root / "experiment.json"
    experiment_path.write_text(
        json.dumps(
            {
                "schema_version": 1,
                "experiment_id": "synthetic-{}-targets".format(target_count),
                "strength": 2,
                "targets": keys,
                "expected_summary": {
                    "target_count": target_count,
                    "modified_source_count": target_count,
                    "triggering_test_count": target_count,
                },
                "input_fingerprints": {
                    "selected_catalog_sha256": selected_catalog_fingerprint(targets),
                    "scenario_set_sha256": scenario_set_fingerprint(
                        keys, scenario_paths
                    ),
                },
            }
        ),
        encoding="utf-8",
    )
    return catalog_path, scenario_root, experiment_path


class ScenarioCatalogTests(unittest.TestCase):
    def test_repository_has_one_approved_plan_for_every_target(self):
        plans = load_scenario_catalog(
            PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json",
            IPO_ROOT / "Configuration" / "targets",
            EXPERIMENT,
        )
        self.assertEqual(17, len(plans))
        self.assertTrue(all(len(plan.scenarios) >= 1 for plan in plans))
        self.assertTrue(all(len(scenario.factors) >= 2 for plan in plans for scenario in plan.scenarios))

    def test_unverified_dry_run_never_publishes_testcode(self):
        with tempfile.TemporaryDirectory() as name:
            root = pathlib.Path(name)
            manifest = run_scenario_catalog(
                PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json",
                IPO_ROOT / "Configuration" / "targets",
                EXPERIMENT,
                root,
                verify_fixed=False,
                run_id="dry",
            )
            self.assertEqual(17, manifest["target_count"])
            self.assertEqual(0, manifest["published_suite_count"])
            self.assertFalse((root / "TestCode").exists())
            self.assertTrue(
                all(
                    scenario["pair_coverage_percent"] == 100.0
                    for record in manifest["records"]
                    for scenario in record["scenarios"]
                )
            )

    def test_eighteen_target_experiment_is_not_hard_coded(self):
        with tempfile.TemporaryDirectory() as name:
            root = pathlib.Path(name)
            catalog, scenarios, experiment = _write_experiment_fixture(root, 18)

            manifest = run_scenario_catalog(
                catalog,
                scenarios,
                experiment,
                root,
                verify_fixed=False,
                run_id="expanded",
            )

            self.assertEqual("synthetic-18-targets", manifest["experiment_id"])
            self.assertEqual(18, manifest["target_count"])
            self.assertEqual(18, len(manifest["records"]))
            self.assertEqual(0, manifest["published_suite_count"])
            self.assertFalse((root / "TestCode").exists())
            self.assertTrue(
                all(
                    scenario["pair_coverage_percent"] == 100.0
                    for record in manifest["records"]
                    for scenario in record["scenarios"]
                )
            )


if __name__ == "__main__":
    unittest.main()
