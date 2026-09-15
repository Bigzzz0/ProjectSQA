#!/usr/bin/env python3
"""
Autonomous Batch Test Generator for DeepSeek V4 Flash
ProjectSQA - Member 3 (AI Prompt Engineer)
Iterates through all 21 representative benchmark targets across 17 Defects4J projects.
"""

import os
import sys
import time

if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPTS_DIR)
sys.path.insert(0, SCRIPTS_DIR)

from kku_generate import get_api_keys, run_test_generation

TARGET_FILES = [
    "target_benchmark/Lang_1b/NumberUtils.java",
    "target_benchmark/Chart_1b/AbstractCategoryItemRenderer.java",
    "target_benchmark/Cli_1b/CommandLine.java",
    "target_benchmark/Closure_1b/RemoveUnusedVars.java",
    "target_benchmark/Codec_1b/Caverphone.java",
    "target_benchmark/Codec_1b/Metaphone.java",
    "target_benchmark/Codec_1b/SoundexUtils.java",
    "target_benchmark/Collections_25b/IteratorUtils.java",
    "target_benchmark/Compress_1b/CpioArchiveOutputStream.java",
    "target_benchmark/Csv_1b/ExtendedBufferedReader.java",
    "target_benchmark/Gson_1b/TypeInfoFactory.java",
    "target_benchmark/JacksonCore_1b/TextBuffer.java",
    "target_benchmark/JacksonCore_1b/NumberInput.java",
    "target_benchmark/JacksonDatabind_1b/BeanPropertyWriter.java",
    "target_benchmark/JacksonXml_1b/FromXmlParser.java",
    "target_benchmark/Jsoup_1b/Document.java",
    "target_benchmark/JxPath_1b/DOMNodePointer.java",
    "target_benchmark/JxPath_1b/JDOMNodePointer.java",
    "target_benchmark/Math_2b/HypergeometricDistribution.java",
    "target_benchmark/Mockito_1b/InvocationMatcher.java",
    "target_benchmark/Time_1b/Partial.java",
]

def main():
    api_keys = get_api_keys()
    total_targets = len(TARGET_FILES)
    print("=" * 70)
    print(f"🤖 Autonomous DeepSeek V4 Flash Batch Test Generator")
    print(f"📦 Total Targets: {total_targets} classes across 17 Defects4J projects")
    print(f"🔑 API Key Pool: {len(api_keys)} keys with auto-failover")
    print("=" * 70)

    success_count = 0
    failed_count = 0
    skipped_count = 0

    overall_start = time.time()

    for idx, rel_path in enumerate(TARGET_FILES, 1):
        abs_path = os.path.join(PROJECT_ROOT, rel_path)
        if not os.path.exists(abs_path):
            print(f"\n[{idx}/{total_targets}] ⚠️ Target file not found: {rel_path} -> Skipping")
            failed_count += 1
            continue

        base_name = os.path.splitext(os.path.basename(abs_path))[0]
        folder_name = os.path.basename(os.path.dirname(abs_path))
        test_file_sub = os.path.join(PROJECT_ROOT, "Deepseek-v4_flash", "TestCode", folder_name, f"{base_name}DeepseekTest.java")
        test_file_root = os.path.join(PROJECT_ROOT, "Deepseek-v4_flash", "TestCode", f"{base_name}DeepseekTest.java")
        test_file = test_file_sub if os.path.exists(test_file_sub) else test_file_root

        # Check if already generated with non-trivial size
        if os.path.exists(test_file) and os.path.getsize(test_file) > 500:
            print(f"\n[{idx}/{total_targets}] ⏩ Already generated: {folder_name}/{base_name}DeepseekTest.java ({os.path.getsize(test_file)} bytes) -> Skipping")
            skipped_count += 1
            continue

        print(f"\n[{idx}/{total_targets}] 🚀 Generating DeepSeek V4 Flash test for {base_name} ({rel_path})...")
        max_attempts = 2
        success = False
        for attempt in range(1, max_attempts + 1):
            try:
                ok = run_test_generation(
                    target_ai="deepseek",
                    api_keys=api_keys,
                    model_identifier="deepseek-v4-flash",
                    source_file=abs_path
                )
                if ok:
                    success = True
                    break
                else:
                    print(f"   ⚠️ Attempt {attempt} failed, retrying...")
                    time.sleep(3)
            except Exception as e:
                print(f"   ⚠️ Attempt {attempt} raised exception: {e}")
                time.sleep(3)

        if success:
            success_count += 1
            print(f"   ✅ Successfully generated and saved {base_name}DeepseekTest.java")
        else:
            failed_count += 1
            print(f"   ❌ Failed to generate test for {base_name}")

        time.sleep(2.0)  # Rate limiting cushion

    total_elapsed = round(time.time() - overall_start, 2)
    print("\n" + "=" * 70)
    print(f"🏁 DeepSeek V4 Flash Batch Generation Finished in {total_elapsed}s!")
    print(f"   ✅ Successful: {success_count}")
    print(f"   ⏩ Skipped (already exists): {skipped_count}")
    print(f"   ❌ Failed: {failed_count}")
    print("=" * 70)

if __name__ == "__main__":
    main()
