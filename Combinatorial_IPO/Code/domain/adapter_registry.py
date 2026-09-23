"""Reusable semantic adapters used by the all-class feasibility planner.

Adapters describe safe, reviewable Java expressions.  They do not silently
fall back to ``null`` for an unknown reference type.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Dict, List, Mapping, Optional, Tuple

from domain.value_generator import (
    NeedsSemanticModelError,
    UnsupportedTypeError,
    get_domain_for_type,
    normalize_java_type,
)


@dataclass(frozen=True)
class AdapterDomain:
    adapter: str
    values: Tuple[str, ...]
    cleanup_required: bool = False


def _array_domain(java_type: str) -> Optional[AdapterDomain]:
    if java_type.endswith("[][]"):
        base = java_type[:-4].strip()
        if re.fullmatch(r"(?:byte|short|int|long|float|double|boolean|char|String)", base):
            sample = '"val"' if base == "String" else ("true" if base == "boolean" else "1")
            return AdapterDomain(
                "2d_array",
                ("new {}[][] {{}}".format(base), "new {}[][] {{{{}}}}".format(base, sample)),
            )
    if java_type in {"Object[]", "java.lang.Object[]"}:
        return AdapterDomain(
            "object_array",
            ("new Object[] {}", 'new Object[] {"a", Integer.valueOf(1)}'),
        )
    if not java_type.endswith("[]"):
        return None
    component = java_type[:-2].strip()
    if not re.fullmatch(r"(?:byte|short|int|long|float|double|boolean|char|String)", component):
        return None
    sample = '"value"' if component == "String" else ("true" if component == "boolean" else "1")
    return AdapterDomain(
        "array",
        ("new {}[] {{}}".format(component), "new {}[] {{{}}}".format(component, sample)),
    )


def _semantic_domain(java_type: str, target_class: str = "") -> Optional[AdapterDomain]:
    compact = re.sub(r"\s+", "", java_type)
    simple = compact.rsplit(".", 1)[-1]
    if simple in {"Reader", "StringReader"}:
        return AdapterDomain(
            "reader",
            (
                'new java.io.StringReader("")',
                'new java.io.StringReader("a")',
                'new java.io.StringReader("a\\nb")',
            ),
            cleanup_required=True,
        )
    if simple in {"InputStream", "ByteArrayInputStream"}:
        return AdapterDomain(
            "input_stream",
            (
                "new java.io.ByteArrayInputStream(new byte[] {})",
                "new java.io.ByteArrayInputStream(new byte[] {1})",
            ),
            cleanup_required=True,
        )
    if simple in {"Charset", "java.nio.charset.Charset"}:
        return AdapterDomain(
            "charset",
            ("java.nio.charset.StandardCharsets.UTF_8", "java.nio.charset.StandardCharsets.US_ASCII"),
        )
    if simple in {"Locale", "java.util.Locale"}:
        return AdapterDomain(
            "locale",
            ("java.util.Locale.ROOT", "java.util.Locale.US", "java.util.Locale.JAPAN"),
        )
    if simple in {"Pattern", "java.util.regex.Pattern"}:
        return AdapterDomain(
            "pattern",
            (
                'java.util.regex.Pattern.compile("")',
                'java.util.regex.Pattern.compile("a")',
                'java.util.regex.Pattern.compile(".*")',
            ),
        )
    if simple in {"BigInteger", "java.math.BigInteger"}:
        return AdapterDomain(
            "big_integer",
            ("java.math.BigInteger.ZERO", "java.math.BigInteger.ONE", "java.math.BigInteger.ONE.negate()"),
        )
    if simple in {"BigDecimal", "java.math.BigDecimal"}:
        return AdapterDomain(
            "big_decimal",
            ("java.math.BigDecimal.ZERO", "java.math.BigDecimal.ONE", "java.math.BigDecimal.ONE.negate()"),
        )
    collection_match = re.match(r"(?:java\.util\.)?(?:List|Collection)<(String|Integer)>", compact)
    if collection_match:
        value = '"value"' if collection_match.group(1) == "String" else "1"
        elem_t = collection_match.group(1)
        return AdapterDomain(
            "collection",
            ("java.util.Collections.<{}>emptyList()".format(elem_t), "java.util.Collections.singletonList({})".format(value)),
        )
    if simple in {"OutputStream", "ByteArrayOutputStream"}:
        return AdapterDomain(
            "output_stream",
            (
                "new java.io.ByteArrayOutputStream()",
                "new java.io.ByteArrayOutputStream(32)",
            ),
            cleanup_required=True,
        )
    if simple in {"Writer", "StringWriter"}:
        return AdapterDomain(
            "writer",
            (
                'new java.io.StringWriter()',
                'new java.io.StringWriter(16)',
            ),
            cleanup_required=True,
        )
    if simple in {"CharSequence"}:
        return AdapterDomain(
            "char_sequence",
            ('""', '"a"', '"test"'),
        )
    if simple in {"Date", "java.util.Date"}:
        return AdapterDomain(
            "date",
            ("new java.util.Date(0L)", "new java.util.Date(1000000000000L)"),
        )
    if simple in {"TimeZone", "java.util.TimeZone"}:
        return AdapterDomain(
            "timezone",
            ('java.util.TimeZone.getTimeZone("UTC")', 'java.util.TimeZone.getTimeZone("GMT")'),
        )
    if simple in {"Calendar", "java.util.Calendar"}:
        return AdapterDomain(
            "calendar",
            ('java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))',),
        )
    if simple in {"File", "java.io.File"}:
        return AdapterDomain(
            "file",
            ('new java.io.File("temp.txt")', 'new java.io.File(".")'),
        )
    if simple in {"Object", "java.lang.Object"}:
        return AdapterDomain(
            "object",
            ('new Object()', '"sample_str"', 'Integer.valueOf(1)'),
        )
    if simple.startswith("Class") or compact.startswith("Class<"):
        return AdapterDomain(
            "class_literal",
            ("String.class", "Object.class", "Integer.class"),
        )
    if simple in {"DateTimeZone", "org.joda.time.DateTimeZone"}:
        return AdapterDomain(
            "date_time_zone",
            ("org.joda.time.DateTimeZone.UTC",),
        )
    if simple in {"Complex", "org.apache.commons.math.complex.Complex"}:
        return AdapterDomain(
            "complex",
            ("new org.apache.commons.math.complex.Complex(1.0, 2.0)", "org.apache.commons.math.complex.Complex.ZERO"),
        )
    if simple in {"Fraction", "org.apache.commons.math.fraction.Fraction"}:
        return AdapterDomain(
            "fraction",
            ("new org.apache.commons.math.fraction.Fraction(1, 2)", "org.apache.commons.math.fraction.Fraction.ONE"),
        )
    if simple in {"Attributes", "org.jsoup.nodes.Attributes"}:
        return AdapterDomain(
            "attributes",
            ("new org.jsoup.nodes.Attributes()",),
        )
    if simple in {"Tag", "org.jsoup.parser.Tag"}:
        return AdapterDomain(
            "tag",
            ('org.jsoup.parser.Tag.valueOf("p")', 'org.jsoup.parser.Tag.valueOf("div")'),
        )
    if compact in {"org.jsoup.nodes.Node", "org.jsoup.nodes.Element"} or (
        simple in {"Node", "Element"} and target_class.startswith("org.jsoup")
    ):
        return AdapterDomain(
            "jsoup_node",
            ('new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("p"), "")', 'new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf("div"), "")'),
        )
    if compact in {"List", "java.util.List", "Collection", "java.util.Collection", "List<?>", "Collection<?>", "List<String>", "Collection<String>"}:
        return AdapterDomain(
            "collection_generic",
            ("java.util.Collections.<String>emptyList()", 'java.util.Arrays.asList("a", "b")'),
        )
    if compact in {"List<Object>", "Collection<Object>"}:
        return AdapterDomain(
            "collection_generic",
            ("java.util.Collections.<Object>emptyList()", 'java.util.Arrays.<Object>asList("a", "b")'),
        )
    if compact in {"Set", "java.util.Set", "Set<?>", "Set<String>"}:
        return AdapterDomain(
            "set_generic",
            ("java.util.Collections.<String>emptySet()", 'java.util.Collections.singleton("a")'),
        )
    if compact in {"Set<Object>"}:
        return AdapterDomain(
            "set_generic",
            ("java.util.Collections.<Object>emptySet()", 'java.util.Collections.<Object>singleton("a")'),
        )
    if compact in {"Map", "java.util.Map", "Map<?,?>", "Map<String,String>", "Map<String,Object>", "Map<Object,Object>"}:
        return AdapterDomain(
            "map_generic",
            ("java.util.Collections.emptyMap()", 'java.util.Collections.singletonMap("key", "val")'),
        )
    iterator_match = re.match(r"(?:java\.util\.)?Iterator<(String|Integer)>", compact)
    if iterator_match:
        value = '"value"' if iterator_match.group(1) == "String" else "1"
        return AdapterDomain(
            "iterator",
            (
                "java.util.Collections.emptyList().iterator()",
                "java.util.Collections.singletonList({}).iterator()".format(value),
            ),
        )
    if simple in {"StringBuffer"}:
        return AdapterDomain("string_buffer", ('new java.lang.StringBuffer("")', 'new java.lang.StringBuffer("test")'))
    if simple in {"StringBuilder"}:
        return AdapterDomain("string_builder", ('new java.lang.StringBuilder("")', 'new java.lang.StringBuilder("test")'))
    if simple in {"Comparable"}:
        return AdapterDomain("comparable", ('"sample_str"', 'Integer.valueOf(1)'))
    if simple in {"Number", "java.lang.Number"}:
        return AdapterDomain("number", ('Integer.valueOf(1)', 'Double.valueOf(2.5)', 'Long.valueOf(100L)'))
    if simple in {"Throwable", "Exception", "RuntimeException"}:
        return AdapterDomain("throwable", ('new java.lang.Exception("test")', 'new java.lang.IllegalArgumentException("invalid")'))
    if simple in {"Base64Variant", "com.fasterxml.jackson.core.Base64Variant"}:
        return AdapterDomain("base64_variant", ('com.fasterxml.jackson.core.Base64Variants.MIME', 'com.fasterxml.jackson.core.Base64Variants.PEM'))
    if simple in {"RealVector", "org.apache.commons.math.linear.RealVector", "ArrayRealVector", "org.apache.commons.math.linear.ArrayRealVector"}:
        return AdapterDomain("real_vector", ('new org.apache.commons.math.linear.ArrayRealVector(new double[] {1.0, 2.0})', 'new org.apache.commons.math.linear.ArrayRealVector(new double[] {0.0, 0.0})'))
    if simple in {"RealMatrix", "org.apache.commons.math.linear.RealMatrix", "Array2DRowRealMatrix", "org.apache.commons.math.linear.Array2DRowRealMatrix"}:
        return AdapterDomain("real_matrix", ('new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})',))
    if simple in {"BigFraction", "org.apache.commons.math.fraction.BigFraction"}:
        return AdapterDomain("big_fraction", ('new org.apache.commons.math.fraction.BigFraction(1, 2)', 'org.apache.commons.math.fraction.BigFraction.ONE'))
    if simple in {"Paint", "java.awt.Paint", "Color", "java.awt.Color"}:
        return AdapterDomain("paint", ('java.awt.Color.RED', 'java.awt.Color.BLUE'))
    if simple in {"Stroke", "java.awt.Stroke", "BasicStroke", "java.awt.BasicStroke"}:
        return AdapterDomain("stroke", ('new java.awt.BasicStroke(1.0f)', 'new java.awt.BasicStroke(2.0f)'))
    if simple in {"Shape", "java.awt.Shape", "Rectangle2D", "java.awt.geom.Rectangle2D"}:
        return AdapterDomain("shape", ('new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 10.0, 10.0)', 'new java.awt.geom.Rectangle2D.Double(1.0, 1.0, 50.0, 50.0)'))
    if simple in {"CategoryDataset", "org.jfree.data.category.CategoryDataset", "DefaultCategoryDataset", "org.jfree.data.category.DefaultCategoryDataset"}:
        return AdapterDomain("category_dataset", ('new org.jfree.data.category.DefaultCategoryDataset()',))
    if simple in {"ValueAxis", "org.jfree.chart.axis.ValueAxis", "NumberAxis", "org.jfree.chart.axis.NumberAxis"}:
        return AdapterDomain("value_axis", ('new org.jfree.chart.axis.NumberAxis()', 'new org.jfree.chart.axis.NumberAxis("Label")'))
    if simple in {"Marker", "org.jfree.chart.plot.Marker", "ValueMarker", "org.jfree.chart.plot.ValueMarker"}:
        return AdapterDomain("marker", ('new org.jfree.chart.plot.ValueMarker(0.0)', 'new org.jfree.chart.plot.ValueMarker(1.0)'))
    if simple in {"Layer", "org.jfree.chart.util.Layer"}:
        return AdapterDomain("layer", ('org.jfree.chart.util.Layer.FOREGROUND', 'org.jfree.chart.util.Layer.BACKGROUND'))
    if simple in {"Chronology", "org.joda.time.Chronology"}:
        return AdapterDomain("chronology", ('org.joda.time.chrono.ISOChronology.getInstanceUTC()', 'org.joda.time.chrono.GJChronology.getInstanceUTC()'))
    if simple in {"ReadableInstant", "org.joda.time.ReadableInstant", "Instant", "org.joda.time.Instant"}:
        return AdapterDomain("readable_instant", ('new org.joda.time.Instant(0L)', 'new org.joda.time.Instant(1000000000000L)'))
    if simple in {"ReadableDuration", "org.joda.time.ReadableDuration", "Duration", "org.joda.time.Duration"}:
        return AdapterDomain("readable_duration", ('new org.joda.time.Duration(0L)', 'new org.joda.time.Duration(1000L)'))
    if simple in {"ReadablePeriod", "org.joda.time.ReadablePeriod", "Period", "org.joda.time.Period"}:
        return AdapterDomain("readable_period", ('org.joda.time.Period.ZERO', 'org.joda.time.Period.days(1)'))
    if simple in {"ReadablePartial", "org.joda.time.ReadablePartial", "LocalDate", "org.joda.time.LocalDate"}:
        return AdapterDomain("readable_partial", ('new org.joda.time.LocalDate(2000, 1, 1)', 'new org.joda.time.LocalDate(2020, 6, 15)'))
    if simple in {"LocalDateTime", "org.joda.time.LocalDateTime"}:
        return AdapterDomain("local_date_time", ('new org.joda.time.LocalDateTime(2000, 1, 1, 12, 0)',))
    if simple in {"LocalTime", "org.joda.time.LocalTime"}:
        return AdapterDomain("local_time", ('new org.joda.time.LocalTime(12, 0)', 'new org.joda.time.LocalTime(0, 0)'))
    if simple in {"DurationFieldType", "org.joda.time.DurationFieldType"}:
        return AdapterDomain("duration_field_type", ('org.joda.time.DurationFieldType.days()', 'org.joda.time.DurationFieldType.hours()'))
    if simple in {"DateTimeFieldType", "org.joda.time.DateTimeFieldType"}:
        return AdapterDomain("date_time_field_type", ('org.joda.time.DateTimeFieldType.year()', 'org.joda.time.DateTimeFieldType.monthOfYear()'))
    if simple in {"PeriodType", "org.joda.time.PeriodType"}:
        return AdapterDomain("period_type", ('org.joda.time.PeriodType.standard()', 'org.joda.time.PeriodType.days()'))
    if simple in {"Option", "org.apache.commons.cli.Option"}:
        if target_class.startswith("org.apache.commons.cli2") or "cli2" in target_class:
            return None
        return AdapterDomain("cli_option", ('new org.apache.commons.cli.Option("o", "opt")', 'new org.apache.commons.cli.Option("f", "file", true, "desc")'))
    if simple in {"URI", "java.net.URI"}:
        return AdapterDomain("uri", ('java.net.URI.create("http://localhost")', 'java.net.URI.create("urn:test")'))
    if simple in {"URL", "java.net.URL"}:
        return AdapterDomain("url", ('new java.net.URL("http://localhost")',))
    if simple in {"AbstractCompiler", "Compiler", "com.google.javascript.jscomp.AbstractCompiler", "com.google.javascript.jscomp.Compiler"}:
        return AdapterDomain("abstract_compiler", ('new com.google.javascript.jscomp.Compiler()',))
    if simple in {"StrBuilder", "org.apache.commons.lang.text.StrBuilder"}:
        return AdapterDomain("str_builder", ('new org.apache.commons.lang.text.StrBuilder("")', 'new org.apache.commons.lang.text.StrBuilder("test")'))
    if simple in {"StrMatcher", "org.apache.commons.lang.text.StrMatcher"}:
        return AdapterDomain("str_matcher", ('org.apache.commons.lang.text.StrMatcher.commaMatcher()', 'org.apache.commons.lang.text.StrMatcher.spaceMatcher()'))
    if simple in {"JavaType", "com.fasterxml.jackson.databind.JavaType"}:
        return AdapterDomain("java_type", ('com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)',))
    if simple in {"Type", "java.lang.reflect.Type"}:
        return AdapterDomain("type", ('String.class', 'Object.class'))
    if simple in {"JsonParser", "com.fasterxml.jackson.core.JsonParser"}:
        return AdapterDomain("json_parser", ('new com.fasterxml.jackson.core.JsonFactory().createParser("{}")',), cleanup_required=True)
    if simple in {"JsonGenerator", "com.fasterxml.jackson.core.JsonGenerator"}:
        return AdapterDomain("json_generator", ('new com.fasterxml.jackson.core.JsonFactory().createGenerator(new java.io.ByteArrayOutputStream())',), cleanup_required=True)
    if simple in {"DeserializationContext", "com.fasterxml.jackson.databind.DeserializationContext"}:
        return AdapterDomain("deser_ctx", ('new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationContext()',))
    if simple in {"DeserializationConfig", "com.fasterxml.jackson.databind.DeserializationConfig"}:
        return AdapterDomain("deser_cfg", ('new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()',))
    if simple in {"SerializerProvider", "com.fasterxml.jackson.databind.SerializerProvider"}:
        return AdapterDomain("ser_provider", ('new com.fasterxml.jackson.databind.ObjectMapper().getSerializerProvider()',))
    if simple in {"T", "K", "V", "E"}:
        return AdapterDomain("type_variable", ('"sample"', 'Integer.valueOf(1)'))
    return None



def resolve_adapter_domain(java_type: str, target_class: str = "") -> AdapterDomain:
    """Resolve a reusable domain or raise a reason suitable for the backlog."""
    normalized = normalize_java_type(java_type)
    try:
        values = tuple(value for value in get_domain_for_type(normalized) if value != "null")
        if values:
            return AdapterDomain("generic", values)
    except (NeedsSemanticModelError, UnsupportedTypeError):
        pass
    array = _array_domain(normalized)
    if array:
        return array
    semantic = _semantic_domain(normalized, target_class=target_class)
    if semantic:
        return semantic
    raise NeedsSemanticModelError(
        "No registered adapter for reference type: {}".format(java_type)
    )


def stable_observer_for_type(java_type: object) -> Optional[str]:
    """Return a deterministic observer name, never an arbitrary object toString."""
    if not isinstance(java_type, str):
        return None
    normalized = normalize_java_type(java_type)
    if normalized.endswith("[]"):
        return "array_string_value"
    scalar = {
        "byte", "short", "int", "long", "float", "double", "boolean", "char",
        "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "Character",
        "String", "java.lang.String", "CharSequence", "BigInteger", "java.math.BigInteger",
        "BigDecimal", "java.math.BigDecimal", "Number", "java.lang.Number",
        "StringBuffer", "StringBuilder",
    }
    if normalized in scalar:
        return "scalar_string_value"
    simple = normalized.rsplit(".", 1)[-1]
    if simple in {"List", "Collection", "Set", "ArrayList", "HashSet", "Elements"}:
        return "collection_size"
    if simple in {"Map", "HashMap"}:
        return "map_size"
    if simple in {
        "Complex", "Fraction", "BigFraction", "RealVector", "RealMatrix",
        "OpenMapRealVector", "ArrayRealVector", "Array2DRowRealMatrix",
        "Date", "StrBuilder", "CSVFormat", "JsonToken", "DateTimeZone",
        "Period", "LocalDate", "LocalDateTime", "LocalTime", "Duration", "Instant",
        "URI", "URL", "File", "Path", "Element", "Node", "Document",
    }:
        return "deterministic_to_string"
    return None




def domains_for_parameters(
    parameters: List[Mapping[str, object]], target_class: str = ""
) -> Tuple[Dict[str, List[str]], Dict[str, str], bool]:
    domains: Dict[str, List[str]] = {}
    adapters: Dict[str, str] = {}
    cleanup_required = False
    for parameter in parameters:
        name = str(parameter["name"])
        domain = resolve_adapter_domain(str(parameter["type"]), target_class=target_class)
        domains[name] = list(domain.values)
        adapters[name] = domain.adapter
        cleanup_required = cleanup_required or domain.cleanup_required
    return domains, adapters, cleanup_required
