"""Unit tests for the deterministic construction planner."""

from __future__ import annotations

import unittest

from domain.construction_planner import ConstructionPlan, plan_receiver


class ConstructionPlannerTests(unittest.TestCase):
    def test_plan_receiver_with_zero_arg_constructor(self) -> None:
        metadata = {
            "type_kind": "class",
            "abstract": False,
            "constructors": [
                {"name": "Simple", "visibility": "public", "parameters": []}
            ],
            "methods": [],
        }
        plan = plan_receiver(metadata, "com.example.Simple")
        self.assertIsNotNone(plan)
        self.assertEqual("constructor", plan.kind)
        self.assertEqual("new Simple()", plan.expression)

    def test_plan_receiver_with_factorized_constructor(self) -> None:
        metadata = {
            "type_kind": "class",
            "abstract": False,
            "constructors": [
                {
                    "name": "Point",
                    "visibility": "public",
                    "parameters": [{"name": "x", "type": "int"}, {"name": "y", "type": "int"}],
                }
            ],
            "methods": [],
        }
        plan = plan_receiver(metadata, "com.example.Point")
        self.assertIsNotNone(plan)
        self.assertEqual("constructor", plan.kind)
        self.assertIn("new Point({receiver__x}, {receiver__y})", plan.expression)
        self.assertIn("receiver__x", plan.factor_domains)
        self.assertIn("receiver__y", plan.factor_domains)

    def test_plan_receiver_for_abstract_class_uses_known_subtype(self) -> None:
        metadata = {
            "type_kind": "class",
            "abstract": True,
            "constructors": [{"name": "AbstractCategoryItemRenderer", "visibility": "protected", "parameters": []}],
            "methods": [],
        }
        target = "org.jfree.chart.renderer.category.AbstractCategoryItemRenderer"
        plan = plan_receiver(metadata, target)
        self.assertIsNotNone(plan)
        self.assertEqual("concrete_subtype", plan.kind)
        self.assertTrue(plan.expression.startswith("new org.jfree.chart.renderer.category.BarRenderer"))

    def test_plan_receiver_with_static_factory(self) -> None:
        metadata = {
            "type_kind": "class",
            "abstract": False,
            "constructors": [{"name": "Singleton", "visibility": "private", "parameters": []}],
            "methods": [
                {
                    "name": "getInstance",
                    "visibility": "public",
                    "static": True,
                    "return_type": "Singleton",
                    "parameters": [],
                }
            ],
        }
        plan = plan_receiver(metadata, "com.example.Singleton")
        self.assertIsNotNone(plan)
        self.assertEqual("static_factory", plan.kind)
        self.assertEqual("Singleton.getInstance()", plan.expression)

    def test_plan_receiver_with_builder(self) -> None:
        metadata = {
            "type_kind": "class",
            "abstract": False,
            "constructors": [{"name": "Config", "visibility": "private", "parameters": []}],
            "methods": [
                {
                    "name": "builder",
                    "visibility": "public",
                    "static": True,
                    "return_type": "ConfigBuilder",
                    "parameters": [],
                }
            ],
        }
        plan = plan_receiver(metadata, "com.example.Config")
        self.assertIsNotNone(plan)
        self.assertEqual("builder", plan.kind)
        self.assertEqual("Config.builder().build()", plan.expression)


if __name__ == "__main__":
    unittest.main()
