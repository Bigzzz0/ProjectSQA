"""Target-specific factor models that supplement generic type domains."""

from __future__ import annotations

import json
from dataclasses import dataclass
from typing import Callable, Dict, List, Mapping, Optional


@dataclass(frozen=True)
class SemanticOverride:
    """A factor model plus a function that creates real method arguments."""

    name: str
    factor_domains: Dict[str, List[str]]
    materialize: Callable[[Mapping[str, str]], Dict[str, str]]
    seed_combinations: List[Dict[str, str]]


NUMBER_UTILS_FACTORS = {
    "Prefix": ["None", "Plus", "Minus", "ZeroX", "MinusZeroX", "Hash"],
    "ValueType": [
        "Integer",
        "Decimal",
        "Scientific",
        "HexDigits",
        "Alphanumeric",
    ],
    "Suffix": ["None", "f", "F", "d", "D", "l", "L", "Invalid"],
    "Length": ["Empty", "SingleChar", "Normal", "ExceedLong"],
}


def _number_body(value_type: str, length: str) -> str:
    if length == "Empty":
        return ""

    values = {
        "Integer": {
            "SingleChar": "1",
            "Normal": "123",
            "ExceedLong": "9" * 40,
        },
        "Decimal": {
            "SingleChar": "1",
            "Normal": "12.34",
            "ExceedLong": "9" * 35 + ".1234",
        },
        "Scientific": {
            "SingleChar": "1",
            "Normal": "1.2e3",
            "ExceedLong": "9" * 32 + "e9999",
        },
        "HexDigits": {
            "SingleChar": "A",
            "Normal": "80000000",
            "ExceedLong": "F" * 40,
        },
        "Alphanumeric": {
            "SingleChar": "x",
            "Normal": "12x34",
            "ExceedLong": "abc123" * 8,
        },
    }
    try:
        return values[value_type][length]
    except KeyError as exc:
        raise ValueError(
            "Unsupported NumberUtils value type/length: {}/{}".format(
                value_type, length
            )
        ) from exc


def _materialize_number_utils(factors: Mapping[str, str]) -> Dict[str, str]:
    expected = set(NUMBER_UTILS_FACTORS)
    if set(factors) != expected:
        raise ValueError(
            "NumberUtils factors must be {}; got {}".format(
                sorted(expected), sorted(factors)
            )
        )

    prefixes = {
        "None": "",
        "Plus": "+",
        "Minus": "-",
        "ZeroX": "0x",
        "MinusZeroX": "-0x",
        "Hash": "#",
    }
    suffixes = {
        "None": "",
        "f": "f",
        "F": "F",
        "d": "d",
        "D": "D",
        "l": "l",
        "L": "L",
        "Invalid": "Z",
    }
    try:
        prefix = prefixes[factors["Prefix"]]
        suffix = suffixes[factors["Suffix"]]
    except KeyError as exc:
        raise ValueError("Unsupported NumberUtils prefix or suffix") from exc

    body = _number_body(factors["ValueType"], factors["Length"])
    concrete_input = "" if factors["Length"] == "Empty" else prefix + body + suffix
    # JSON string syntax is also a valid escaped Java string literal.
    return {"str": json.dumps(concrete_input)}


def find_semantic_override(
    fully_qualified_class: str, method: Mapping[str, object]
) -> Optional[SemanticOverride]:
    """Return a registered semantic model for an exact Java method signature."""
    parameters = method.get("parameters")
    if (
        fully_qualified_class == "org.apache.commons.lang3.math.NumberUtils"
        and method.get("name") == "createNumber"
        and isinstance(parameters, list)
        and len(parameters) == 1
        and isinstance(parameters[0], dict)
        and parameters[0].get("type") in {"String", "java.lang.String"}
    ):
        return SemanticOverride(
            name="number_utils_numeric_string",
            factor_domains={
                name: list(values) for name, values in NUMBER_UTILS_FACTORS.items()
            },
            materialize=_materialize_number_utils,
            seed_combinations=[
                {
                    "Prefix": "ZeroX",
                    "ValueType": "HexDigits",
                    "Suffix": "None",
                    "Length": "Normal",
                }
            ],
        )
    return None
