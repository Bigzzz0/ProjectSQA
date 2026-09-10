import pathlib
import tempfile
import unittest

from runner.scenario_catalog import load_scenario_catalog, run_scenario_catalog


IPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
PROJECT_ROOT = IPO_ROOT.parent


class ScenarioCatalogTests(unittest.TestCase):
    def test_repository_has_one_approved_plan_for_every_target(self):
        plans = load_scenario_catalog(
            PROJECT_ROOT / "target_benchmark" / "catalog_17_projects.json",
            IPO_ROOT / "Configuration" / "targets",
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


if __name__ == "__main__":
    unittest.main()
