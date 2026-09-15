#!/usr/bin/env python3
"""
Autonomous Batch Test Generator for Tier 2 Expansion Projects (Csv, Codec, Gson, etc.)
ProjectSQA - Member 3 (AI Prompt Engineer)
Automatically discovers extracted classes in target_benchmark/ and generates comprehensive test suites.
"""

import os
import sys
import glob
import time
import argparse

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

TIER_2_PROJECTS = ["Csv", "Codec", "Gson"]

def get_target_files_for_projects(projects):
    """Find all .java class files inside target_benchmark/<Project>_*"""
    targets = []
    for proj in projects:
        pattern = os.path.join(PROJECT_ROOT, "target_benchmark", f"{proj}_*", "*.java")
        files = glob.glob(pattern)
        targets.extend(files)
    
    # Sort naturally by folder and file name
    def sort_key(p):
        folder = os.path.basename(os.path.dirname(p))
        fname = os.path.basename(p)
        parts = folder.split("_")
        proj = parts[0]
        bug_num = int(parts[1][:-1]) if len(parts) > 1 and parts[1].endswith("b") and parts[1][:-1].isdigit() else 0
        return (proj, bug_num, fname)

    return sorted(targets, key=sort_key)

def main():
    parser = argparse.ArgumentParser(description="Autonomous Tier 2 Batch Test Generator for ProjectSQA")
    parser.add_argument("--project", type=str, default=None, help="Specific project to generate (e.g. Csv, Codec, Gson)")
    parser.add_argument("--ai", type=str, choices=["deepseek", "gemini"], default="deepseek", help="Target AI model")
    parser.add_argument("--limit", type=int, default=None, help="Limit number of classes to generate in this run")
    args = parser.parse_args()

    target_projects = [args.project] if args.project else TIER_2_PROJECTS
    target_files = get_target_files_for_projects(target_projects)

    if args.limit:
        target_files = target_files[:args.limit]

    api_keys = get_api_keys()
    tool_dir = "Deepseek-v4_flash" if args.ai == "deepseek" else "Gemini-3_8_flash"
    ai_suffix = "DeepseekTest.java" if args.ai == "deepseek" else "GeminiTest.java"
    model_id = "deepseek-v4-flash" if args.ai == "deepseek" else "gemini-3.8-flash"

    total_targets = len(target_files)
    print("=" * 70)
    print(f"🤖 Autonomous Tier 2 Batch Test Generator ({args.ai.upper()})")
    print(f"📦 Target Projects: {', '.join(target_projects)}")
    print(f"📄 Total Target Classes: {total_targets}")
    print(f"🔑 API Key Pool: {len(api_keys)} keys available")
    print("=" * 70)

    success_count = 0
    failed_count = 0
    skipped_count = 0
    overall_start = time.time()

    for idx, abs_path in enumerate(target_files, 1):
        base_name = os.path.splitext(os.path.basename(abs_path))[0]
        folder_name = os.path.basename(os.path.dirname(abs_path))
        
        test_file_sub = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", folder_name, f"{base_name}{ai_suffix}")
        test_file_root = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", f"{base_name}{ai_suffix}")
        test_file = test_file_sub if os.path.exists(test_file_sub) else test_file_root

        # Check if already generated with non-trivial size
        if os.path.exists(test_file) and os.path.getsize(test_file) > 500:
            print(f"\n[{idx}/{total_targets}] ⏩ Already generated: {folder_name}/{base_name}{ai_suffix} ({os.path.getsize(test_file):,} bytes) -> Skipping")
            skipped_count += 1
            continue

        print(f"\n[{idx}/{total_targets}] 🚀 Generating {args.ai.upper()} test for {folder_name}/{base_name}.java...")
        max_attempts = 2
        success = False
        for attempt in range(1, max_attempts + 1):
            try:
                ok = run_test_generation(
                    target_ai=args.ai,
                    api_keys=api_keys,
                    model_identifier=model_id,
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
            print(f"   ✅ Successfully generated and saved {folder_name}/{base_name}{ai_suffix}")
        else:
            failed_count += 1
            print(f"   ❌ Failed to generate test for {folder_name}/{base_name}")

        time.sleep(2.0)  # Rate limiting cushion

    total_elapsed = round(time.time() - overall_start, 2)
    print("\n" + "=" * 70)
    print(f"🏁 Batch Generation for {', '.join(target_projects)} Completed in {total_elapsed}s!")
    print(f"   ✅ Successfully Generated: {success_count}")
    print(f"   ⏩ Skipped (Already Exists): {skipped_count}")
    print(f"   ❌ Failed: {failed_count}")
    print("=" * 70)

if __name__ == "__main__":
    main()
