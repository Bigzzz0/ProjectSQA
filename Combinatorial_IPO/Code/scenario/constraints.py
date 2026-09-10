"""Compile declarative scenario constraints into a valid combination universe."""

from __future__ import annotations

import itertools
from typing import Dict, List, Mapping, Sequence


OPS = {
    "eq": lambda left, right: left == right,
    "ne": lambda left, right: left != right,
    "lt": lambda left, right: float(left) < float(right),
    "le": lambda left, right: float(left) <= float(right),
    "gt": lambda left, right: float(left) > float(right),
    "ge": lambda left, right: float(left) >= float(right),
}


def _matches(row: Mapping[str, str], expected: Mapping[str, object]) -> bool:
    return all(
        row.get(factor) in (value if isinstance(value, list) else [value])
        for factor, value in expected.items()
    )


def satisfies(row: Mapping[str, str], constraints: Sequence[Mapping[str, object]]) -> bool:
    for rule in constraints:
        kind = rule.get("type")
        if kind == "forbid" and _matches(row, rule.get("when", {})):
            return False
        if kind == "require" and _matches(row, rule.get("if", {})):
            if not _matches(row, rule.get("then", {})):
                return False
        if kind == "compare":
            operator = str(rule.get("op"))
            if operator not in OPS:
                raise ValueError("Unsupported comparison operator: {}".format(operator))
            left = row[str(rule["left"])]
            right_operand = rule["right"]
            right = row[right_operand] if right_operand in row else str(right_operand)
            if not OPS[operator](left, right):
                return False
        if kind not in {"forbid", "require", "compare"}:
            raise ValueError("Unsupported constraint type: {}".format(kind))
    return True


def valid_combinations(
    domains: Mapping[str, Sequence[str]],
    constraints: Sequence[Mapping[str, object]],
) -> List[Dict[str, str]]:
    factors = list(domains)
    return [
        dict(zip(factors, values))
        for values in itertools.product(*(domains[factor] for factor in factors))
        if satisfies(dict(zip(factors, values)), constraints)
    ]
