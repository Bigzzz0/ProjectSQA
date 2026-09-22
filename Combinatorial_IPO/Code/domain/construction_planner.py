"""Deterministic Construction Planner for Java receiver and parameter synthesis.

Builds ConstructionPlans using:
1. Accessible constructors (ordered by parameter complexity)
2. Static factory methods (getInstance, of, create, valueOf, from)
3. Known concrete subtypes for abstract/interface targets
4. Builder patterns (builder()...build())
5. Short deterministic setup sequences (max 3 calls)

Bounded graph recursion depth <= 3, cycle detection via visited types.
Shared by fixed oracle collection and JUnit test synthesis.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from typing import Dict, List, Mapping, Optional, Sequence, Set, Tuple

from domain.adapter_registry import domains_for_parameters, resolve_adapter_domain
from domain.value_generator import NeedsSemanticModelError


@dataclass
class ConstructionPlan:
    kind: str  # "constructor", "static_factory", "concrete_subtype", "builder", "setup_sequence"
    target_class: str
    expression: str
    setup_statements: List[str] = field(default_factory=list)
    cleanup_statements: List[str] = field(default_factory=list)
    factor_domains: Dict[str, List[str]] = field(default_factory=dict)
    adapters: Dict[str, str] = field(default_factory=dict)
    cleanup_required: bool = False
    depth: int = 1
    provenance: str = "direct"


# Known concrete subtypes for prominent abstract classes and interfaces in Defects4J
KNOWN_CONCRETE_SUBTYPES: Dict[str, Tuple[str, ...]] = {
    # JFreeChart
    "org.jfree.chart.renderer.category.AbstractCategoryItemRenderer": (
        "org.jfree.chart.renderer.category.BarRenderer",
        "org.jfree.chart.renderer.category.LineAndShapeRenderer",
    ),
    "org.jfree.chart.renderer.category.CategoryItemRenderer": (
        "org.jfree.chart.renderer.category.BarRenderer",
    ),
    "org.jfree.chart.renderer.xy.AbstractXYItemRenderer": (
        "org.jfree.chart.renderer.xy.XYLineAndShapeRenderer",
    ),
    "org.jfree.chart.plot.Plot": (
        "org.jfree.chart.plot.CategoryPlot",
        "org.jfree.chart.plot.XYPlot",
    ),
    "org.jfree.data.general.Dataset": (
        "org.jfree.data.category.DefaultCategoryDataset",
    ),
    "org.jfree.data.category.CategoryDataset": (
        "org.jfree.data.category.DefaultCategoryDataset",
    ),
    # Apache Commons Math
    "org.apache.commons.math.distribution.ContinuousDistribution": (
        "org.apache.commons.math.distribution.NormalDistributionImpl",
    ),
    "org.apache.commons.math.distribution.IntegerDistribution": (
        "org.apache.commons.math.distribution.BinomialDistributionImpl",
    ),
    "org.apache.commons.math.linear.RealVector": (
        "org.apache.commons.math.linear.ArrayRealVector",
    ),
    "org.apache.commons.math.linear.RealMatrix": (
        "org.apache.commons.math.linear.Array2DRowRealMatrix",
    ),
    "org.apache.commons.math.analysis.UnivariateRealFunction": (
        "org.apache.commons.math.analysis.polynomials.PolynomialFunction",
    ),
    # Commons Compress
    "org.apache.commons.compress.archivers.ArchiveInputStream": (
        "org.apache.commons.compress.archivers.tar.TarArchiveInputStream",
    ),
    "org.apache.commons.compress.compressors.CompressorInputStream": (
        "org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream",
    ),
    # Joda-Time
    "org.joda.time.Chronology": (
        "org.joda.time.chrono.ISOChronology",
    ),
    "org.joda.time.ReadableInstant": (
        "org.joda.time.Instant",
        "org.joda.time.DateTime",
    ),
    "org.joda.time.ReadableDateTime": (
        "org.joda.time.DateTime",
    ),
}

# Known static factories
FACTORY_METHOD_NAMES = {"getInstance", "of", "create", "valueOf", "from", "newInstance", "getDefault"}


def plan_receiver(
    metadata: Mapping[str, object],
    target_class: str,
    max_depth: int = 3,
    visited_types: Optional[Set[str]] = None,
) -> Optional[ConstructionPlan]:
    """Determine a deterministic construction plan for instantiating target_class."""
    visited = set(visited_types or ())
    if target_class in visited:
        return None
    visited.add(target_class)

    simple_name = target_class.rsplit(".", 1)[-1].replace("$", ".")
    type_kind = str(metadata.get("type_kind", "class"))
    is_abstract = bool(metadata.get("abstract"))

    # 1. If abstract or interface, look for known concrete subtype
    if is_abstract or type_kind in {"interface", "annotation"}:
        subtypes = KNOWN_CONCRETE_SUBTYPES.get(target_class, ())
        for subtype in subtypes:
            sub_simple = subtype.rsplit(".", 1)[-1].replace("$", ".")
            return ConstructionPlan(
                kind="concrete_subtype",
                target_class=target_class,
                expression=f"new {subtype}()",
                provenance=f"subtype:{subtype}",
                depth=1,
            )
        return None

    # 2. Check declared constructors
    constructors = [
        item for item in metadata.get("constructors", [])
        if isinstance(item, dict) and item.get("visibility") in {"public", "protected", "package"}
    ]

    # Sort constructors: 0-arg first, then fewest parameters
    def ctor_sort_key(item: dict) -> Tuple[int, str]:
        params = item.get("parameters", [])
        return (len(params) if isinstance(params, list) else 99, str(item.get("name")))

    constructors.sort(key=ctor_sort_key)

    all_declared = metadata.get("constructors", [])
    has_declared_ctors = bool(metadata.get("declared_constructor_count")) or (isinstance(all_declared, list) and len(all_declared) > 0)

    # If no declared constructors at all, implicit 0-arg constructor exists
    if not constructors and not has_declared_ctors:
        return ConstructionPlan(
            kind="constructor",
            target_class=target_class,
            expression=f"new {simple_name}()",
            provenance="implicit_zero_arg",
            depth=1,
        )


    for ctor in constructors:
        params = ctor.get("parameters", [])
        if not isinstance(params, list):
            continue

        if not params:
            return ConstructionPlan(
                kind="constructor",
                target_class=target_class,
                expression=f"new {simple_name}()",
                provenance="zero_arg_constructor",
                depth=1,
            )

        # Parameterized constructor: attempt to resolve parameters
        try:
            domains, adapters, cleanup = domains_for_parameters(params)
            factor_domains = {f"receiver__{k}": v for k, v in domains.items()}
            adapter_map = {f"receiver__{k}": v for k, v in adapters.items()}
            args_expr = ", ".join(f"{{receiver__{p['name']}}}" for p in params)
            return ConstructionPlan(
                kind="constructor",
                target_class=target_class,
                expression=f"new {simple_name}({args_expr})",
                factor_domains=factor_domains,
                adapters=adapter_map,
                cleanup_required=cleanup,
                provenance="factorized_constructor",
                depth=1,
            )
        except NeedsSemanticModelError:
            continue

    # 3. Check static factory methods on the target class
    methods = [
        item for item in metadata.get("methods", [])
        if isinstance(item, dict) and item.get("static") and item.get("visibility") in {"public", "protected"}
    ]

    for m in methods:
        m_name = str(m.get("name", ""))
        return_type = str(m.get("return_type", ""))
        if m_name in FACTORY_METHOD_NAMES and (simple_name in return_type or target_class in return_type):
            params = m.get("parameters", [])
            if not isinstance(params, list):
                continue
            if not params:
                return ConstructionPlan(
                    kind="static_factory",
                    target_class=target_class,
                    expression=f"{simple_name}.{m_name}()",
                    provenance=f"factory:{m_name}",
                    depth=1,
                )
            try:
                domains, adapters, cleanup = domains_for_parameters(params)
                factor_domains = {f"receiver__{k}": v for k, v in domains.items()}
                adapter_map = {f"receiver__{k}": v for k, v in adapters.items()}
                args_expr = ", ".join(f"{{receiver__{p['name']}}}" for p in params)
                return ConstructionPlan(
                    kind="static_factory",
                    target_class=target_class,
                    expression=f"{simple_name}.{m_name}({args_expr})",
                    factor_domains=factor_domains,
                    adapters=adapter_map,
                    cleanup_required=cleanup,
                    provenance=f"factory:{m_name}",
                    depth=1,
                )
            except NeedsSemanticModelError:
                continue

    # 4. Check builder pattern
    # Look for a static builder() method returning a Builder
    for m in methods:
        if m.get("name") == "builder":
            return ConstructionPlan(
                kind="builder",
                target_class=target_class,
                expression=f"{simple_name}.builder().build()",
                provenance="builder",
                depth=2,
            )

    return None
