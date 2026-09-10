"""Map common Java parameter types to reusable boundary-value domains."""

from __future__ import annotations

import argparse
import json
import re
from typing import Dict, List, Tuple


# Values are Java expressions so the JUnit generator can use them directly.
TYPE_DOMAINS: Dict[str, Tuple[str, ...]] = {
    "byte": ("(byte) 0", "(byte) 1", "(byte) -1", "Byte.MAX_VALUE", "Byte.MIN_VALUE"),
    "Byte": ("null", "(byte) 0", "(byte) 1", "(byte) -1", "Byte.MAX_VALUE"),
    "short": ("(short) 0", "(short) 1", "(short) -1", "Short.MAX_VALUE", "Short.MIN_VALUE"),
    "Short": ("null", "(short) 0", "(short) 1", "(short) -1", "Short.MAX_VALUE"),
    "int": ("0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"),
    "Integer": ("null", "0", "1", "-1", "Integer.MAX_VALUE"),
    "long": ("0L", "1L", "-1L", "Long.MAX_VALUE", "Long.MIN_VALUE"),
    "Long": ("null", "0L", "1L", "-1L", "Long.MAX_VALUE"),
    "float": ("0.0f", "1.0f", "-1.0f", "Float.NaN", "Float.POSITIVE_INFINITY"),
    "Float": ("null", "0.0f", "1.0f", "-1.0f", "Float.MAX_VALUE"),
    "double": ("0.0d", "1.0d", "-1.0d", "Double.NaN", "Double.POSITIVE_INFINITY"),
    "Double": ("null", "0.0d", "1.0d", "-1.0d", "Double.MAX_VALUE"),
    "boolean": ("true", "false"),
    "Boolean": ("null", "true", "false"),
    "char": ("'\\0'", "'a'", "'0'", "Character.MIN_VALUE", "Character.MAX_VALUE"),
    "Character": ("null", "'\\0'", "'a'", "'0'", "Character.MAX_VALUE"),
    "String": (
        "null",
        '""',
        '" "',
        '"a"',
        '"test123"',
        '"!@#"',
        '"0"',
        '"-1"',
        '"1.5"',
        '"9223372036854775807"',
        '"9223372036854775808"',
        '"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"',
    ),
}

JAVA_LANG_TYPES = {
    "Byte",
    "Short",
    "Integer",
    "Long",
    "Float",
    "Double",
    "Boolean",
    "Character",
    "String",
}


class DomainGenerationError(ValueError):
    """Base error for Java types that cannot use a generic value domain."""


class UnsupportedTypeError(DomainGenerationError):
    """Raised for an unsupported primitive-like Java type."""


class NeedsSemanticModelError(DomainGenerationError):
    """Raised when a reference type needs meaningful constructed values."""


def normalize_java_type(parameter_type: str) -> str:
    """Normalize java.lang names while retaining arrays and generic types."""
    normalized = re.sub(r"\s+", " ", parameter_type.strip()).replace("...", "[]")
    if normalized.startswith("java.lang."):
        candidate = normalized[len("java.lang.") :]
        if candidate in JAVA_LANG_TYPES:
            return candidate
    return normalized


def get_domain_for_type(parameter_type: str) -> List[str]:
    """Return values for a supported type without guessing object semantics."""
    normalized = normalize_java_type(parameter_type)
    domain = TYPE_DOMAINS.get(normalized)
    if domain is not None:
        return list(domain)

    # Primitive-looking unknown types cannot safely receive null.
    if re.fullmatch(r"[a-z]+", normalized):
        raise UnsupportedTypeError(
            "Unsupported primitive Java type: {}".format(parameter_type)
        )
    raise NeedsSemanticModelError(
        "No generic value domain for reference type: {}".format(parameter_type)
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Show the default domain for a Java type")
    parser.add_argument("java_type", help="Java type such as int or java.lang.String")
    args = parser.parse_args()
    print(json.dumps(get_domain_for_type(args.java_type), indent=2))


if __name__ == "__main__":
    main()
