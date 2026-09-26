#!/usr/bin/env python3
"""
Batch test generator for SQA Member 3 tasks.
Regenerates tests for updated targets using KKU API for DeepSeek and Gemini.
"""

import os
import sys
import time

# Add scripts directory to path to import kku_generate
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import kku_generate

TARGET_FILES = [
    # Math-9 (3D Line)
    "target_benchmark/Math_9b/Line.java",
    # Closure-34 (CodeGenerator & CodePrinter)
    "target_benchmark/Closure_34b/CodeGenerator.java",
    "target_benchmark/Closure_34b/CodePrinter.java",
    # Closure-42 (IRFactory)
    "target_benchmark/Closure_42b/IRFactory.java",
    # Closure-52 (CodeGenerator)
    "target_benchmark/Closure_52b/CodeGenerator.java",
    # Closure-81 (IRFactory)
    "target_benchmark/Closure_81b/IRFactory.java",
    # Closure-84 (IRFactory)
    "target_benchmark/Closure_84b/IRFactory.java",
    # Closure-122 (IRFactory)
    "target_benchmark/Closure_122b/IRFactory.java",
    # Closure-123 (CodeGenerator)
    "target_benchmark/Closure_123b/CodeGenerator.java",
    # Closure-128 (CodeGenerator)
    "target_benchmark/Closure_128b/CodeGenerator.java",
    # Closure-131 (TokenStream)
    "target_benchmark/Closure_131b/TokenStream.java",
]

def main():
    api_keys = kku_generate.get_api_keys()
    print(f"Loaded {len(api_keys)} API keys from environment / .env")

    models = [
        ("deepseek", "deepseek-v4-flash"),
        ("gemini", "gemini-3.8-flash")
    ]

    total = len(TARGET_FILES) * len(models)
    current = 0

    results = []

    for src_rel in TARGET_FILES:
        src_path = os.path.join(kku_generate.PROJECT_ROOT, src_rel)
        if not os.path.exists(src_path):
            print(f"❌ Source file not found: {src_path}")
            continue

        for ai_name, model_id in models:
            current += 1
            print("\n" + "=" * 70)
            print(f"[{current}/{total}] Generating for AI={ai_name}, Target={src_rel}")
            print("=" * 70)

            # Round-robin key rotation so consecutive requests use different keys
            rotated_keys = api_keys[(current - 1) % len(api_keys):] + api_keys[:(current - 1) % len(api_keys)]

            success = False
            for attempt in range(1, 4):
                try:
                    success = kku_generate.run_test_generation(
                        target_ai=ai_name,
                        api_keys=rotated_keys,
                        model_identifier=model_id,
                        source_file=src_path
                    )
                    if success:
                        break
                    else:
                        print(f"⚠️ Attempt {attempt} failed, retrying in 5 seconds...")
                        time.sleep(5)
                except Exception as e:
                    print(f"❌ Exception on attempt {attempt}: {e}")
                    time.sleep(5)

            results.append((src_rel, ai_name, success))
            time.sleep(2)  # Short pause between calls

    print("\n" + "=" * 70)
    print("BATCH GENERATION SUMMARY:")
    print("=" * 70)
    for src, ai, ok in results:
        status_str = "✅ SUCCESS" if ok else "❌ FAILED"
        print(f"{status_str} | {ai:8} | {src}")

if __name__ == "__main__":
    main()
