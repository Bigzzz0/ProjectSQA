import json
import tempfile
import unittest
from pathlib import Path

from generator.scenario_junit_generator import synthesize_scenario_suite
from runner.catalog import CatalogTarget
from scenario.spec import load_target_plan


class ScenarioSpecTests(unittest.TestCase):
    def setUp(self):
        self.catalog = CatalogTarget(
            project="Demo",
            bug_id=1,
            directory="Demo_1b",
            target_class="demo.Target",
            simple_name="Target",
            modified_sources=("demo.Target",),
            trigger_tests=("demo.TargetTest::testBug",),
        )

    def _raw(self):
        return {
            "schema_version": 1,
            "status": "APPROVED",
            "project": "Demo",
            "bug_id": 1,
            "suite": {"package": "demo", "class_name": "Demo_1b_IPOTest"},
            "scenarios": [
                {
                    "id": "bug_path",
                    "target_classes": ["demo.Target"],
                    "entrypoint": "Target.run(String,boolean)",
                    "invocation_kind": "static",
                    "factors": {
                        "input": {"empty": '""', "text": '"x"'},
                        "mode": {"on": "true", "off": "false"},
                    },
                    "constraints": [],
                    "mandatory_seeds": [{"input": "text", "mode": "on"}],
                    "body": "assertNotNull(Target.run({{input}}, {{mode}}));",
                    "oracle": {"mode": "invariant", "description": "result exists"},
                    "evidence": {
                        "patch_file": "Demo/patches/1.src.patch",
                        "trigger_tests": ["demo.TargetTest::testBug"],
                        "rationale": "The patch changes Target.run and the trigger calls it.",
                    },
                }
            ],
        }

    def _load(self, raw):
        with tempfile.TemporaryDirectory() as name:
            path = Path(name) / "Demo_1b.json"
            path.write_text(json.dumps(raw), encoding="utf-8")
            return load_target_plan(path, self.catalog)

    def test_load_and_render_scenario(self):
        plan = self._load(self._raw())
        scenario = plan.scenarios[0]
        source = synthesize_scenario_suite(
            plan, [(scenario, [{"input": "text", "mode": "on"}])]
        )
        self.assertIn("@Test(timeout = 4000)", source)
        self.assertIn('Target.run("x", true)', source)

    def test_unapproved_plan_is_rejected(self):
        raw = self._raw()
        raw["status"] = "DRAFT"
        with self.assertRaisesRegex(ValueError, "APPROVED"):
            self._load(raw)

    def test_body_must_use_every_factor(self):
        raw = self._raw()
        raw["scenarios"][0]["body"] = "assertTrue({{mode}});"
        with self.assertRaisesRegex(ValueError, "body tokens"):
            self._load(raw)

    def test_trigger_must_come_from_catalog(self):
        raw = self._raw()
        raw["scenarios"][0]["evidence"]["trigger_tests"] = ["Other::test"]
        with self.assertRaisesRegex(ValueError, "outside the catalog"):
            self._load(raw)


if __name__ == "__main__":
    unittest.main()
