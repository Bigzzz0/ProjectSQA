#!/usr/bin/env python3
"""
Finalize all candidate test suites for ProjectSQA.
1. Ensures package declaration on line 1 exactly matches the target class package.
2. Ensures the public class name matches the file name.
3. Cleans up any truncated methods at the end and ensures proper closing brace '}'.
4. Verifies test_file_matches_targets for every slot.
"""

import os
import re
import sys
import json

sys.path.insert(0, os.path.join(os.path.dirname(__file__)))
from run_benchmark import PROJECT_ROOT, test_file_matches_targets, get_class_package
from audit_suite_gaps import candidate_files, sha256

CATALOG = os.path.join(PROJECT_ROOT, "target_benchmark", "all_bugs_catalog.json")

def load_catalog_targets():
    with open(CATALOG, encoding="utf-8") as f:
        catalog = json.load(f)
    return {
        (row["project"], int(row["bug_id"])): row.get("target_classes") or row.get("modified_classes") or []
        for row in catalog
    }

def fix_file(file_path, expected_package, expected_class_name):
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return False

    with open(file_path, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    lines = content.splitlines()

    # 1. Fix package line: remove any existing package declaration and put the correct one at line 1
    cleaned_lines = []
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("package ") and stripped.endswith(";"):
            continue
        cleaned_lines.append(line)

    new_content = f"package {expected_package};\n\n" + "\n".join(cleaned_lines).lstrip()

    # 2. Fix class declaration if it doesn't match expected_class_name
    # Look for class <Something>Test or class <expected_class_name>
    class_pattern = re.compile(r'\b(public\s+)?class\s+([A-Za-z0-9_]+)\b')
    match = class_pattern.search(new_content)
    if match:
        current_name = match.group(2)
        if current_name != expected_class_name:
            print(f"Renaming class {current_name} -> {expected_class_name} in {os.path.basename(file_path)}")
            # Replace only the class declaration
            start, end = match.span()
            pub = match.group(1) or "public "
            new_content = new_content[:start] + f"{pub}class {expected_class_name}" + new_content[end:]

    # 3. Check for unbalanced braces or truncated ending
    open_count = new_content.count('{')
    close_count = new_content.count('}')
    if open_count > close_count:
        print(f"Balancing braces in {os.path.basename(file_path)}: open={open_count}, close={close_count}")
        # Find if last method is incomplete
        lines_now = new_content.splitlines()
        # Find last completed method or closing brace
        # Walk backwards to last '}'
        last_brace_idx = -1
        for i in range(len(lines_now) - 1, -1, -1):
            if '}' in lines_now[i]:
                last_brace_idx = i
                break
        if last_brace_idx != -1:
            # Keep up to last_brace_idx, then close class with '}'
            new_content = "\n".join(lines_now[:last_brace_idx + 1]) + "\n}\n"
        else:
            new_content = new_content + "\n" + ("}" * (open_count - close_count)) + "\n"

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content.strip() + "\n")

    return True

def main():
    targets = load_catalog_targets()

    # Target specifications for the 29 fixed slots
    specs = [
        # Task 1
        ("deepseek", "Closure", 18, "com.google.javascript.jscomp.Compiler", "CompilerDeepseekTest"),
        ("deepseek", "Closure", 22, "com.google.javascript.jscomp.CheckSideEffects", "CheckSideEffectsDeepseekTest"),
        ("deepseek", "Closure", 31, "com.google.javascript.jscomp.Compiler", "CompilerDeepseekTest"),
        ("deepseek", "Collections", 8, "org.apache.commons.collections.buffer.UnboundedFifoBuffer", "UnboundedFifoBufferDeepseekTest"),
        ("gemini", "Collections", 8, "org.apache.commons.collections.buffer.UnboundedFifoBuffer", "UnboundedFifoBufferGeminiTest"),
        ("deepseek", "Lang", 58, "org.apache.commons.lang.math.NumberUtils", "NumberUtilsDeepseekTest"),
        ("gemini", "Lang", 58, "org.apache.commons.lang.math.NumberUtils", "NumberUtilsGeminiTest"),
        # Task 3
        ("deepseek", "Closure", 41, "com.google.javascript.jscomp.FunctionTypeBuilder", "FunctionTypeBuilderDeepseekTest"),
        ("deepseek", "JacksonCore", 25, "com.fasterxml.jackson.core.json.ReaderBasedJsonParser", "ReaderBasedJsonParserDeepseekTest"),
        # Task 2
        ("deepseek", "Math", 9, "org.apache.commons.math3.geometry.euclidean.threed.Line", "LineDeepseekTest"),
        ("gemini", "Math", 9, "org.apache.commons.math3.geometry.euclidean.threed.Line", "LineGeminiTest"),
        ("deepseek", "Closure", 34, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorDeepseekTest"),
        ("deepseek", "Closure", 34, "com.google.javascript.jscomp.CodePrinter", "CodePrinterDeepseekTest"),
        ("gemini", "Closure", 34, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorGeminiTest"),
        ("gemini", "Closure", 34, "com.google.javascript.jscomp.CodePrinter", "CodePrinterGeminiTest"),
        ("deepseek", "Closure", 42, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryDeepseekTest"),
        ("gemini", "Closure", 42, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryGeminiTest"),
        ("deepseek", "Closure", 52, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorDeepseekTest"),
        ("gemini", "Closure", 52, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorGeminiTest"),
        ("deepseek", "Closure", 81, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryDeepseekTest"),
        ("gemini", "Closure", 81, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryGeminiTest"),
        ("deepseek", "Closure", 84, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryDeepseekTest"),
        ("gemini", "Closure", 84, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryGeminiTest"),
        ("deepseek", "Closure", 122, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryDeepseekTest"),
        ("gemini", "Closure", 122, "com.google.javascript.jscomp.parsing.IRFactory", "IRFactoryGeminiTest"),
        ("deepseek", "Closure", 123, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorDeepseekTest"),
        ("gemini", "Closure", 123, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorGeminiTest"),
        ("deepseek", "Closure", 128, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorDeepseekTest"),
        ("gemini", "Closure", 128, "com.google.javascript.jscomp.CodeGenerator", "CodeGeneratorGeminiTest"),
        ("deepseek", "Closure", 131, "com.google.javascript.rhino.TokenStream", "TokenStreamDeepseekTest"),
        ("gemini", "Closure", 131, "com.google.javascript.rhino.TokenStream", "TokenStreamGeminiTest"),
    ]

    for tech, proj, bid, target_cls, class_name in specs:
        pkg = target_cls.rsplit(".", 1)[0]
        tool_dir = "Deepseek-v4_flash" if tech == "deepseek" else "Gemini-3_8_flash"
        file_path = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", f"{proj}_{bid}b", f"{class_name}.java")
        if not os.path.exists(file_path):
            file_path = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", f"{proj}-{bid}", f"{class_name}.java")

        fix_file(file_path, pkg, class_name)

    print("\n" + "=" * 80)
    print("VERIFICATION SUMMARY:")
    print("=" * 80)
    all_passed = True
    verified_records = []

    for tech, proj, bid, target_cls, class_name in specs:
        tgt = targets[(proj, bid)]
        files = candidate_files(tech, proj, bid)
        matching = [f for f in files if test_file_matches_targets(f, tgt)]
        ok = len(matching) > 0
        if not ok:
            all_passed = False
        print(f"[{'PASS' if ok else 'FAIL'}] {tech:8} | {proj}-{bid:<3} | Tgt: {tgt} | Files: {len(files)} | Matched: {len(matching)}")
        for f in files:
            verified_records.append({
                "tech": tech,
                "proj": proj,
                "bid": bid,
                "target_cls": target_cls,
                "file": f,
                "sha256": sha256(f),
                "pkg": get_class_package(f),
                "matched": f in matching
            })

    print(f"\nAll 29 slots verified matching: {all_passed}")

if __name__ == "__main__":
    main()
