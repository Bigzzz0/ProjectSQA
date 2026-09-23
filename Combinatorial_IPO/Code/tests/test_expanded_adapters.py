"""Fixture tests for expanded semantic adapters, observers, and construction planning."""

from __future__ import annotations

import unittest

from domain.adapter_registry import (
    resolve_adapter_domain,
    stable_observer_for_type,
)
from domain.construction_planner import KNOWN_CONCRETE_SUBTYPES, plan_receiver


class ExpandedAdaptersTests(unittest.TestCase):
    def test_2d_primitive_array_domains(self) -> None:
        double_2d = resolve_adapter_domain("double[][]")
        self.assertEqual("2d_array", double_2d.adapter)
        self.assertIn("new double[][] {}", double_2d.values)

        int_2d = resolve_adapter_domain("int[][]")
        self.assertEqual("2d_array", int_2d.adapter)
        self.assertIn("new int[][] {}", int_2d.values)

    def test_object_array_domain(self) -> None:
        obj_arr = resolve_adapter_domain("Object[]")
        self.assertEqual("object_array", obj_arr.adapter)
        self.assertIn("new Object[] {}", obj_arr.values)

    def test_semantic_adapters_resolvable(self) -> None:
        types_to_check = [
            ("StringBuffer", "string_buffer"),
            ("StringBuilder", "string_builder"),
            ("Comparable", "comparable"),
            ("Number", "number"),
            ("Throwable", "throwable"),
            ("Base64Variant", "base64_variant"),
            ("RealVector", "real_vector"),
            ("RealMatrix", "real_matrix"),
            ("BigFraction", "big_fraction"),
            ("Paint", "paint"),
            ("Stroke", "stroke"),
            ("Shape", "shape"),
            ("CategoryDataset", "category_dataset"),
            ("ValueAxis", "value_axis"),
            ("Marker", "marker"),
            ("Layer", "layer"),
            ("Chronology", "chronology"),
            ("ReadableInstant", "readable_instant"),
            ("ReadableDuration", "readable_duration"),
            ("ReadablePeriod", "readable_period"),
            ("ReadablePartial", "readable_partial"),
            ("LocalDateTime", "local_date_time"),
            ("LocalTime", "local_time"),
            ("DurationFieldType", "duration_field_type"),
            ("DateTimeFieldType", "date_time_field_type"),
            ("PeriodType", "period_type"),
            ("Option", "cli_option"),
            ("URI", "uri"),
            ("URL", "url"),
            ("AbstractCompiler", "abstract_compiler"),
            ("StrBuilder", "str_builder"),
            ("StrMatcher", "str_matcher"),
            ("JavaType", "java_type"),
            ("JsonParser", "json_parser"),
            ("JsonGenerator", "json_generator"),
            ("DeserializationContext", "deser_ctx"),
            ("DeserializationConfig", "deser_cfg"),
            ("SerializerProvider", "ser_provider"),
            ("T", "type_variable"),
        ]
        for type_name, expected_adapter in types_to_check:
            with self.subTest(type_name=type_name):
                domain = resolve_adapter_domain(type_name)
                self.assertEqual(expected_adapter, domain.adapter)
                self.assertTrue(len(domain.values) > 0)

    def test_expanded_stable_observers(self) -> None:
        types_with_deterministic_observers = [
            "StringBuffer",
            "StringBuilder",
            "Elements",
            "BigFraction",
            "RealVector",
            "RealMatrix",
            "StrBuilder",
            "CSVFormat",
            "JsonToken",
            "DateTimeZone",
            "Period",
            "LocalDate",
            "LocalDateTime",
            "LocalTime",
            "Duration",
            "Instant",
            "URI",
            "URL",
            "File",
            "Path",
            "Element",
            "Node",
            "Document",
        ]
        for type_name in types_with_deterministic_observers:
            with self.subTest(type_name=type_name):
                obs = stable_observer_for_type(type_name)
                self.assertIsNotNone(obs, f"Observer for {type_name} should not be None")

    def test_concrete_subtypes_registered(self) -> None:
        expected_subtypes = [
            "java.lang.Comparable",
            "java.lang.CharSequence",
            "org.jfree.chart.axis.ValueAxis",
            "org.jfree.chart.plot.Marker",
            "org.joda.time.ReadablePeriod",
            "org.joda.time.ReadableDuration",
            "org.joda.time.ReadablePartial",
            "com.google.javascript.jscomp.AbstractCompiler",
        ]
        for type_name in expected_subtypes:
            with self.subTest(type_name=type_name):
                self.assertIn(type_name, KNOWN_CONCRETE_SUBTYPES)

    def test_construction_planning_with_comparable_constructor(self) -> None:
        # Simulate a class with constructor Taking Comparable (like TimeSeries)
        metadata = {
            "type_kind": "class",
            "abstract": False,
            "declared_constructor_count": 1,
            "constructors": [
                {
                    "name": "TimeSeries",
                    "visibility": "public",
                    "parameters": [{"name": "name", "type": "Comparable"}],
                }
            ],
            "methods": [],
        }
        plan = plan_receiver(metadata, "org.jfree.data.time.TimeSeries")
        self.assertIsNotNone(plan)
        self.assertEqual("constructor", plan.kind)
        self.assertIn("receiver__name", plan.factor_domains)


if __name__ == "__main__":
    unittest.main()
