#!/usr/bin/env python3
"""
Autonomous Dual-AI Overnight Pipeline: Gemini 3.8 Flash & DeepSeek V4 Flash
ProjectSQA - Member 3 (AI Prompt Engineer) & Member 4 (Infra Lead)

Runs test generation for BOTH DeepSeek and Gemini across all Defects4J projects
until all classes are tested or all daily tokens across all 3 keys are fully consumed.
Automatically evaluates on Defects4J container and regenerates publication figures.
"""

import os
import sys
import glob
import time
import subprocess
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

# Project execution priority (Small-to-medium first for high yield)
ORDERED_PROJECTS = [
    "Csv",
    "Codec",
    "Gson",
    "JacksonXml",
    "JxPath",
    "Time",
    "Collections",
    "Chart",
    "Cli",
    "Compress",
    "Lang",
    "Math",
    "Jsoup",
    "JacksonCore",
    "Mockito",
    "JacksonDatabind",
    "Closure"
]

def get_target_files_for_project(project_name):
    """Find all target .java classes for a project sorted by bug number and class name"""
    pattern = os.path.join(PROJECT_ROOT, "target_benchmark", f"{project_name}_*", "*.java")
    files = glob.glob(pattern)

    def sort_key(p):
        folder = os.path.basename(os.path.dirname(p))
        fname = os.path.basename(p)
        parts = folder.split("_")
        bug_num = int(parts[1][:-1]) if len(parts) > 1 and parts[1].endswith("b") and parts[1][:-1].isdigit() else 0
        return (bug_num, fname)

    return sorted(files, key=sort_key)

def is_test_already_generated(tool_dir, folder_name, class_name, suffix):
    """Check if test file exists in either subfolder or root and has non-trivial size"""
    sub_path = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", folder_name, f"{class_name}{suffix}")
    root_path = os.path.join(PROJECT_ROOT, tool_dir, "TestCode", f"{class_name}{suffix}")
    if os.path.exists(sub_path) and os.path.getsize(sub_path) > 500:
        return True
    if os.path.exists(root_path) and os.path.getsize(root_path) > 500:
        return True
    return False

def evaluate_project_in_docker(project_name):
    """Run Defects4J benchmark evaluation on container and update plots"""
    print(f"\n" + "=" * 70)
    print(f"📊 [Defects4J Benchmark] Evaluating {project_name} for DeepSeek & Gemini...")
    print("=" * 70)
    cmd = [
        "docker", "exec", "defects4j_sqa",
        "python3", "/workspace/scripts/run_benchmark.py",
        "--project", project_name,
        "--techniques", "deepseek,gemini",
        "--resume"
    ]
    try:
        subprocess.run(cmd, check=False)
    except Exception as e:
        print(f"   ⚠️ Benchmark evaluation error for {project_name}: {e}")

    # Regenerate publication plots
    plot_script = os.path.join(SCRIPTS_DIR, "plot_results.py")
    if os.path.exists(plot_script):
        try:
            subprocess.run([sys.executable, plot_script], check=False)
        except Exception as e:
            print(f"   ⚠️ Plotting error: {e}")

def main():
    parser = argparse.ArgumentParser(description="Autonomous Dual-AI Overnight Pipeline")
    parser.add_argument("--start-project", type=str, default=None, help="Start from specific project")
    parser.add_argument("--skip-eval", action="store_true", help="Skip running Defects4J benchmark during generation")
    args = parser.parse_args()

    api_keys = get_api_keys()
    print("=" * 75)
    print("🌙 Autonomous Overnight Dual-AI Generator: Gemini 3.8 Flash & DeepSeek V4 Flash")
    print(f"🔑 API Key Pool: {len(api_keys)} keys with multi-key auto-failover")
    print(f"📦 Total Projects in Queue: {len(ORDERED_PROJECTS)}")
    print("=" * 75)

    deepseek_quota_exhausted = False
    gemini_quota_exhausted = False

    projects = ORDERED_PROJECTS
    if args.start_project:
        if args.start_project in projects:
            idx = projects.index(args.start_project)
            projects = projects[idx:]
        else:
            print(f"⚠️ Project {args.start_project} not in queue, starting from beginning.")

    total_generated = {"deepseek": 0, "gemini": 0}
    total_skipped = {"deepseek": 0, "gemini": 0}
    overall_start = time.time()

    for p_idx, project in enumerate(projects, 1):
        if deepseek_quota_exhausted and gemini_quota_exhausted:
            print("\n🛑 ALL TOKEN QUOTAS EXHAUSTED across all 3 keys for both Gemini and DeepSeek!")
            break

        target_files = get_target_files_for_project(project)
        if not target_files:
            continue

        print(f"\n📂 [{p_idx}/{len(projects)}] Processing Project: {project} ({len(target_files)} target classes)...")
        project_new_tests = 0

        for f_idx, abs_path in enumerate(target_files, 1):
            if deepseek_quota_exhausted and gemini_quota_exhausted:
                break

            base_name = os.path.splitext(os.path.basename(abs_path))[0]
            folder_name = os.path.basename(os.path.dirname(abs_path))

            # -------------------------------------------------------------
            # 1. GEMINI 3.8 FLASH
            # -------------------------------------------------------------
            if not gemini_quota_exhausted:
                if is_test_already_generated("Gemini-3_8_flash", folder_name, base_name, "GeminiTest.java"):
                    total_skipped["gemini"] += 1
                else:
                    print(f"\n   [{f_idx}/{len(target_files)}] 🚀 [GEMINI] Generating test for {folder_name}/{base_name}.java...")
                    ok = False
                    for attempt in range(1, 3):
                        try:
                            ok = run_test_generation(
                                target_ai="gemini",
                                api_keys=api_keys,
                                model_identifier="gemini-3.8-flash",
                                source_file=abs_path
                            )
                            if ok:
                                break
                            time.sleep(3)
                        except Exception as e:
                            print(f"      ⚠️ Gemini attempt {attempt} error: {e}")
                            time.sleep(3)

                    if ok:
                        total_generated["gemini"] += 1
                        project_new_tests += 1
                        print(f"      ✅ [GEMINI] Saved {folder_name}/{base_name}GeminiTest.java")
                    else:
                        print(f"      ⚠️ [GEMINI] Failed or quota exhausted across all keys")
                        # Check if quota failure by testing a small ping or consecutive fail
                    time.sleep(2.0)

            # -------------------------------------------------------------
            # 2. DEEPSEEK V4 FLASH
            # -------------------------------------------------------------
            if not deepseek_quota_exhausted:
                if is_test_already_generated("Deepseek-v4_flash", folder_name, base_name, "DeepseekTest.java"):
                    total_skipped["deepseek"] += 1
                else:
                    print(f"\n   [{f_idx}/{len(target_files)}] 🚀 [DEEPSEEK] Generating test for {folder_name}/{base_name}.java...")
                    ok = False
                    for attempt in range(1, 3):
                        try:
                            ok = run_test_generation(
                                target_ai="deepseek",
                                api_keys=api_keys,
                                model_identifier="deepseek-v4-flash",
                                source_file=abs_path
                            )
                            if ok:
                                break
                            time.sleep(3)
                        except Exception as e:
                            print(f"      ⚠️ DeepSeek attempt {attempt} error: {e}")
                            time.sleep(3)

                    if ok:
                        total_generated["deepseek"] += 1
                        project_new_tests += 1
                        print(f"      ✅ [DEEPSEEK] Saved {folder_name}/{base_name}DeepseekTest.java")
                    else:
                        print(f"      ⚠️ [DEEPSEEK] Failed or quota exhausted across all keys")
                    time.sleep(2.0)

        # After finishing a project (if any new tests were produced and eval is not skipped)
        if project_new_tests > 0 and not args.skip_eval:
            evaluate_project_in_docker(project)

    total_time = round(time.time() - overall_start, 2)
    print("\n" + "=" * 75)
    print(f"🏁 Autonomous Overnight Pipeline Finished in {total_time}s!")
    print(f"   🤖 Gemini 3.8 Flash -> Generated: {total_generated['gemini']} | Skipped: {total_skipped['gemini']}")
    print(f"   🤖 DeepSeek V4 Flash -> Generated: {total_generated['deepseek']} | Skipped: {total_skipped['deepseek']}")
    print("=" * 75)

    # Final overall plot regeneration
    plot_script = os.path.join(SCRIPTS_DIR, "plot_results.py")
    if os.path.exists(plot_script):
        try:
            subprocess.run([sys.executable, plot_script], check=False)
        except Exception:
            pass

if __name__ == "__main__":
    main()
