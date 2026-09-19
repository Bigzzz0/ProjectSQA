"""Render validated Java invocations for static and planned instance methods."""

from __future__ import annotations

from typing import Mapping, Sequence


def factor_names(method: Mapping[str, object]) -> Sequence[str]:
    declared = method.get("factor_names")
    if isinstance(declared, list) and all(isinstance(item, str) for item in declared):
        return declared
    parameters = method.get("parameters")
    if not isinstance(parameters, list):
        raise ValueError("Method parameters must be a list")
    names = [parameter.get("name") for parameter in parameters if isinstance(parameter, dict)]
    if len(names) != len(parameters) or any(not isinstance(name, str) for name in names):
        raise ValueError("Every method parameter must have a name")
    return names


def render_invocation(
    class_name: str,
    method: Mapping[str, object],
    combination: Mapping[str, str],
) -> str:
    expected = set(factor_names(method))
    if set(combination) != expected:
        raise ValueError("Combination keys do not match callable factors")
    parameters = method.get("parameters", [])
    arguments = ", ".join(combination[str(item["name"])] for item in parameters)
    method_name = method.get("name")
    if method.get("static") is True:
        return "{}.{}({})".format(class_name, method_name, arguments)
    strategy = method.get("receiver_strategy")
    if not isinstance(strategy, dict) or not isinstance(strategy.get("expression_template"), str):
        raise ValueError("Instance methods require a receiver strategy")
    receiver = strategy["expression_template"].format(**combination)
    return "({}).{}({})".format(receiver, method_name, arguments)
