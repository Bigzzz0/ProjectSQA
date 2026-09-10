"""Render approved scenario plans and native IPO rows as JUnit 4."""

from __future__ import annotations

import re
from typing import Mapping, Sequence, Tuple

from scenario.spec import TargetScenarioPlan


def _indent(text: str, spaces: int = 8) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line if line else "" for line in text.splitlines())


def synthesize_scenario_suite(
    plan: TargetScenarioPlan,
    scenario_rows: Sequence[Tuple[object, Sequence[Mapping[str, str]]]],
) -> str:
    methods = []
    test_index = 1
    for scenario, rows in scenario_rows:
        for row in rows:
            body = scenario.materialize(row)
            details = ", ".join("{}={}".format(key, value) for key, value in row.items())
            safe_id = re.sub(r"[^A-Za-z0-9_]", "_", scenario.scenario_id)
            methods.append(
                """    @Test(timeout = 4000)
    public void test_{scenario}_{index:03d}() throws Exception {{
        // Native IPO combination: {details}
{body}
    }}
""".format(
                    scenario=safe_id,
                    index=test_index,
                    details=details.replace("*/", "* /"),
                    body=_indent(body),
                )
            )
            test_index += 1
    if not methods:
        raise ValueError("At least one scenario row is required")
    imports = ["import org.junit.Test;", "import static org.junit.Assert.*;"]
    imports.extend("import {};".format(item) for item in plan.imports)
    helpers = "\n" + plan.helpers.strip() + "\n" if plan.helpers.strip() else ""
    return """package {package};

{imports}

/** Generated from approved defect-focused scenarios using native IPO. */
public class {class_name}{extends_clause} {{
{helpers}
{methods}
}}
""".format(
        package=plan.suite_package,
        imports="\n".join(imports),
        class_name=plan.suite_class,
        extends_clause=(" extends " + plan.base_class) if plan.base_class else "",
        helpers=helpers,
        methods="\n".join(methods),
    )
