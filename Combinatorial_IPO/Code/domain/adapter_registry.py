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
    scalar = {
        "byte", "short", "int", "long", "float", "double", "boolean", "char",
        "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "Character",
        "String", "java.lang.String", "BigInteger", "java.math.BigInteger",
        "BigDecimal", "java.math.BigDecimal",
    }
    return "scalar_string_value" if normalized in scalar else None


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
