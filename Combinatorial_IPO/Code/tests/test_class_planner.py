from __future__ import annotations

import unittest

from analyzer.class_planner import build_class_plan


class ClassPlannerTests(unittest.TestCase):
    def test_instance_method_with_implicit_receiver_is_auto_ready(self) -> None:
        metadata = {
            "type_kind": "class",
            "constructors": [],
            "methods": [
                {
                    "kind": "method",
                    "name": "add",
                    "return_type": "int",
                    "static": False,
                    "visibility": "public",
                    "parameters": [
                        {"name": "left", "type": "int"},
                        {"name": "right", "type": "int"},
                    ],
                }
            ],
        }
        plan = build_class_plan(metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH")
        self.assertEqual("AUTO_READY", plan["status"])
        self.assertEqual("instance", plan["callables"][0]["invocation_kind"])
        self.assertEqual(2, plan["schema_version"])

    def test_unknown_reference_type_is_adapter_backlog(self) -> None:
        metadata = {
            "type_kind": "class",
            "constructors": [],
            "methods": [
                {
                    "kind": "method",
                    "name": "merge",
                    "return_type": "Object",
                    "static": True,
                    "visibility": "public",
                    "parameters": [
                        {"name": "left", "type": "ProjectNode"},
                        {"name": "right", "type": "ProjectNode"},
                    ],
                }
            ],
        }
        plan = build_class_plan(metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH")
        self.assertEqual("NEEDS_ADAPTER", plan["status"])
        self.assertEqual(["ProjectNode"], plan["missing_adapters"])

    def test_single_factor_is_not_pairwise_applicable(self) -> None:
        metadata = {
            "type_kind": "class",
            "constructors": [],
            "methods": [
                {
                    "kind": "method",
                    "name": "parse",
                    "return_type": "int",
                    "static": True,
                    "visibility": "public",
                    "parameters": [{"name": "value", "type": "String"}],
                }
            ],
        }
        plan = build_class_plan(metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH")
        self.assertEqual("NOT_PAIRWISE_APPLICABLE", plan["status"])

    def test_evidence_is_required_for_all_class_plans(self) -> None:
        metadata = {
            "type_kind": "class",
            "constructors": [],
            "methods": [
                {
                    "kind": "method", "name": "min", "return_type": "int",
                    "static": True, "visibility": "public",
                    "parameters": [{"name": "a", "type": "int"}, {"name": "b", "type": "int"}],
                }
            ],
        }
        plan = build_class_plan(
            metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH",
            callable_evidence={}, require_evidence=True,
        )
        self.assertEqual("NEEDS_ENTRY_POINT", plan["status"])

    def test_factorized_constructor_adds_receiver_factors(self) -> None:
        metadata = {
            "type_kind": "class",
            "constructors": [
                {
                    "kind": "constructor", "name": "Sample", "return_type": None,
                    "static": False, "visibility": "public",
                    "parameters": [{"name": "seed", "type": "int"}],
                }
            ],
            "methods": [
                {
                    "kind": "method", "name": "add", "return_type": "int",
                    "static": False, "visibility": "public",
                    "parameters": [{"name": "value", "type": "int"}],
                }
            ],
        }
        plan = build_class_plan(metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH")
        method = next(item for item in plan["callables"] if item["kind"] == "method")
        self.assertEqual("AUTO_READY", method["status"])
        self.assertEqual(["receiver__seed", "value"], method["factor_names"])
        self.assertEqual("factorized_constructor", method["receiver_strategy"]["kind"])

    def test_object_return_needs_stable_observer(self) -> None:
        metadata = {
            "type_kind": "class", "constructors": [],
            "methods": [{
                "kind": "method", "name": "merge", "return_type": "Object",
                "static": True, "visibility": "public",
                "parameters": [{"name": "a", "type": "int"}, {"name": "b", "type": "int"}],
            }],
        }
        plan = build_class_plan(metadata, "Demo", 1, "example.Sample", [], "PRESENT_IN_BOTH")
        self.assertEqual("NEEDS_ADAPTER", plan["status"])
        self.assertEqual(["object_observer:Object"], plan["missing_adapters"])


if __name__ == "__main__":
    unittest.main()
