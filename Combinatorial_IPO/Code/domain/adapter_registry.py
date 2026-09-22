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


def _semantic_domain(java_type: str) -> Optional[AdapterDomain]:
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
        return AdapterDomain(
            "collection",
            ("java.util.Collections.emptyList()", "java.util.Collections.singletonList({})".format(value)),
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
    if simple in {"Node", "org.jsoup.nodes.Node", "Element", "org.jsoup.nodes.Element"}:
        return AdapterDomain(
            "jsoup_node",
            ('new org.jsoup.nodes.Element("p")', 'new org.jsoup.nodes.Element("div")'),
        )
    if compact in {"List", "java.util.List", "Collection", "java.util.Collection", "List<?>", "Collection<?>", "List<Object>", "Collection<Object>"}:
        return AdapterDomain(
            "collection_generic",
            ("java.util.Collections.emptyList()", 'java.util.Arrays.asList("a", "b")'),
        )
    if compact in {"Set", "java.util.Set", "Set<?>", "Set<String>", "Set<Object>"}:
        return AdapterDomain(
            "set_generic",
            ("java.util.Collections.emptySet()", 'java.util.Collections.singleton("a")'),
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
    return None



def resolve_adapter_domain(java_type: str) -> AdapterDomain:
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
    semantic = _semantic_domain(normalized)
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
    }
    if normalized in scalar:
        return "scalar_string_value"
    simple = normalized.rsplit(".", 1)[-1]
    if simple in {"List", "Collection", "Set", "ArrayList", "HashSet"}:
        return "collection_size"
    if simple in {"Map", "HashMap"}:
        return "map_size"
    if simple in {"Complex", "Fraction"}:
        return "deterministic_to_string"
    if simple in {"Date"}:
        return "date_time_millis"
    return None




def domains_for_parameters(parameters: List[Mapping[str, object]]) -> Tuple[Dict[str, List[str]], Dict[str, str], bool]:
    domains: Dict[str, List[str]] = {}
    adapters: Dict[str, str] = {}
    cleanup_required = False
    for parameter in parameters:
        name = str(parameter["name"])
        domain = resolve_adapter_domain(str(parameter["type"]))
        domains[name] = list(domain.values)
        adapters[name] = domain.adapter
        cleanup_required = cleanup_required or domain.cleanup_required
    return domains, adapters, cleanup_required
