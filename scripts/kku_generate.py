#!/usr/bin/env python3
"""
KKU IntelSphere API - Automated Test Generator for ProjectSQA
Designed for Member 3 (AI Prompt Engineer) & Member 4 (Infra Lead)
"""

import os
import sys
import time
import json
import re
import argparse
import requests

# ป้องกัน UnicodeEncodeError บน Windows terminal เมื่อแสดงผล emoji และภาษาไทย
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

API_BASE_URL = "https://gen.ai.kku.ac.th/api/v1"
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def get_api_key():
    """ดึง API Key จาก Environment Variable หรือไฟล์ .env หรือถามผู้ใช้"""
    key = os.environ.get("KKU_API_KEY")
    if key:
        return key.strip()
    
    # ลองหาจากไฟล์ .env ใน project root
    env_file = os.path.join(PROJECT_ROOT, ".env")
    if os.path.exists(env_file):
        with open(env_file, "r", encoding="utf-8") as f:
            for line in f:
                if line.strip().startswith("KKU_API_KEY="):
                    return line.strip().split("=", 1)[1].strip().strip('"\'')
    
    # หากไม่พบ ให้รับค่าจาก prompt
    print("=" * 60)
    print("🔑 ไม่พบ KKU_API_KEY ในระบบ")
    print("กรุณาขอ API Key จาก: https://gen.ai.kku.ac.th/ (หน้าตั้งค่า -> API Platform)")
    print("=" * 60)
    key = input("ใส่ KKU API Key ของคุณที่นี่: ").strip()
    if not key:
        print("❌ ข้อผิดพลาด: ไม่ได้ระบุ API Key")
        sys.exit(1)
    
    # บันทึกลง .env อัตโนมัติ (อยู่ใน .gitignore แล้ว ปลอดภัย)
    with open(env_file, "w", encoding="utf-8") as f:
        f.write(f"KKU_API_KEY={key}\n")
    print(f"✅ บันทึก API Key ลงใน .env เรียบร้อยแล้ว (ครั้งต่อไปไม่ต้องกรอกใหม่)")
    return key

def list_available_models(api_key):
    """เรียกดูรายชื่อโมเดลทั้งหมดที่ KKU เปิดให้บริการ"""
    headers = {"Authorization": f"Bearer {api_key}"}
    url = f"{API_BASE_URL}/models"
    try:
        res = requests.get(url, headers=headers, timeout=15)
        if res.status_code == 200:
            data = res.json()
            models = data.get("data", [])
            print("\n📋 รายการโมเดลที่ให้บริการใน KKU IntelSphere API:")
            print("-" * 60)
            print(f"{'ID':<6} | {'Name / Owned By':<30}")
            print("-" * 60)
            for m in models:
                m_id = m.get("id")
                m_name = m.get("owned_by") or m.get("name") or "Unknown"
                print(f"{str(m_id):<6} | {m_name:<30}")
            print("-" * 60)
            return models
        else:
            print(f"❌ Error {res.status_code}: {res.text}")
    except Exception as e:
        print(f"❌ Connection error: {e}")
    return []

def extract_java_code(text):
    """สกัดเฉพาะบล็อกรหัสภาษา Java ออกมาจากข้อความที่ AI ตอบ"""
    if not text or not isinstance(text, str):
        return ""
    pattern = r"```java\s*(.*?)\s*```"
    matches = re.findall(pattern, text, re.DOTALL)
    if matches:
        return matches[0].strip()
    # หากไม่มี ```java ลองหา ``` ธรรมดา
    pattern_generic = r"```\s*(.*?)\s*```"
    matches_generic = re.findall(pattern_generic, text, re.DOTALL)
    if matches_generic:
        return matches_generic[0].strip()
    # กรณีโค้ดถูกตัดจบกลางคัน (ไม่มี ``` ปิดท้าย) ให้ลบแท็กเปิด ```java หรือ ``` ออก
    cleaned = text.strip()
    if cleaned.startswith("```java"):
        cleaned = cleaned[7:].strip()
    elif cleaned.startswith("```"):
        cleaned = cleaned[3:].strip()
    if cleaned.endswith("```"):
        cleaned = cleaned[:-3].strip()
    return cleaned

def extract_defect_context(source_file):
    """สกัดข้อมูล Root cause และ Triggering tests จาก defects4j_info.txt ในโฟลเดอร์เดียวกัน"""
    folder = os.path.dirname(os.path.abspath(source_file))
    info_file = os.path.join(folder, "defects4j_info.txt")
    if not os.path.exists(info_file):
        return ""
    try:
        with open(info_file, "r", encoding="utf-8", errors="replace") as f:
            content = f.read()
        if "Root cause in triggering tests:" in content:
            defect_part = content.split("Root cause in triggering tests:")[1]
            if "--------------------------------------------------------------------------------" in defect_part:
                defect_part = defect_part.split("--------------------------------------------------------------------------------")[0]
            return defect_part.strip()
        elif "List of test failures:" in content:
            defect_part = content.split("List of test failures:")[1]
            if "--------------------------------------------------------------------------------" in defect_part:
                defect_part = defect_part.split("--------------------------------------------------------------------------------")[0]
            return defect_part.strip()
    except Exception:
        pass
    return ""

def run_test_generation(target_ai, api_key, model_identifier=None, source_file=None):
    """ส่ง Prompt ไปยัง KKU API และบันทึกผลลัพธ์อัตโนมัติ"""
    if not source_file:
        source_file = os.path.join(PROJECT_ROOT, "target_benchmark", "Lang_1b", "NumberUtils.java")
    
    if not os.path.exists(source_file):
        print(f"❌ ไม่พบไฟล์ซอร์สโค้ด: {source_file}")
        sys.exit(1)
        
    with open(source_file, "r", encoding="utf-8", errors="replace") as f:
        target_code = f.read()

    # Extract target class name and package dynamically
    base_class = os.path.splitext(os.path.basename(source_file))[0]
    package_name = ""
    for line in target_code.splitlines():
        line = line.strip()
        if line.startswith("package ") and line.endswith(";"):
            package_name = line[8:-1].strip()
            break

    # กำหนดค่าตาม AI ที่เลือก
    if target_ai.lower() == "claude":
        tool_dir = "Claude-sonnet_5"
        output_class_name = f"{base_class}ClaudeTest"
        ai_display_name = "Claude Sonnet 5 (via KKU API)"
        default_model = "claude-sonnet-5"
    else:
        tool_dir = "Gemini-3_8_flash"
        output_class_name = f"{base_class}GeminiTest"
        ai_display_name = "Gemini 3.8 Flash (via KKU API)"
        default_model = "gemini-3.8-flash"

    model_to_use = model_identifier if model_identifier else default_model

    pkg_decl = f"package {package_name};" if package_name else "// No package"

    prompt_dir = os.path.join(PROJECT_ROOT, tool_dir, "Prompt")
    os.makedirs(prompt_dir, exist_ok=True)
    custom_prompt_file = os.path.join(prompt_dir, f"actual_prompt_{base_class}.md")

    system_prompt = ""
    custom_defect_req = ""

    # ตรวจสอบว่ามีไฟล์ actual_prompt_<class>.md ที่ปรับแต่งเฉพาะไว้หรือไม่
    if os.path.exists(custom_prompt_file):
        try:
            with open(custom_prompt_file, "r", encoding="utf-8") as pf:
                custom_text = pf.read()
            if "## System Prompt" in custom_text:
                sys_part = custom_text.split("## System Prompt")[1]
                if "```text" in sys_part:
                    sys_code = sys_part.split("```text")[1].split("```")[0].strip()
                    if sys_code:
                        system_prompt = sys_code
            if "=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===" in custom_text:
                req_part = custom_text.split("=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===")[1]
                if "Generate the complete JUnit 4" in req_part:
                    req_part = req_part.split("Generate the complete JUnit 4")[0]
                custom_defect_req = req_part.strip()
        except Exception as e:
            print(f"   ℹ️ ไม่สามารถอ่าน custom prompt ({e}), ใช้ Universal Prompt แทน")

    if not system_prompt:
        system_prompt = f"""You are a Principal Software Quality Assurance (SQA) Engineer and Test Automation Specialist.
Your mission is to perform advanced White-Box Testing on the target Java class from the Defects4J benchmark to generate a production-grade, fault-revealing JUnit 4 test suite.

---

### 🎯 Core Objectives:
1. Maximize **Line Coverage** and **Branch Coverage (Decision/Condition Coverage)** on the target class logic.
2. Expose latent defects, boundary regressions, and state-handling flaws.
3. Ensure **100% deterministic, zero-flakiness, and zero-compilation-error** execution on Java 8 / Defects4J.

---

### 🛠️ Engineering Guidelines & Rules:

#### 1. Imports & Environment Hygiene
- Target Environment: Strictly **Java 8** and **JUnit 4**.
- Package Declaration: Must declare `{pkg_decl}` as line 1.
- Mandatory Explicit Imports:
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies (e.g., `System.currentTimeMillis()`, `new Random()`).
- Class Name: Name the test class `{output_class_name}` (`public class {output_class_name}`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try {{ ... fail(); }} catch (Exception expected) {{ ... }}`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Core Functional Logic & State Transitions** (Normal operational paths, state getters/setters).
- **Partition B: Boundary Value Analysis (BVA) & Extremes** (null arguments, empty collections/strings, zero/negative/MAX boundaries).
- **Partition C: Defect-Targeted Branch Zone** (Targeting known failure conditions from Defects4J ground truth).
- **Partition D: Exception & Defensive Guard Paths** (Illegal arguments, out-of-range parameters).
- **Partition E: Object Lifecycle & Contract Integrity** (equals, hashCode, clone, serialization if applicable).

#### 4. In-Code Reasoning (Mental Sandbox)
Before writing the Java test methods, include an in-line Javadoc/block comment at the top of the class summarizing your:
`/* [Branch & Defect Analysis Matrix] */` listing the specific decision branches and boundary conditions being targeted.

---

### 🚫 ABSOLUTE OUTPUT CONSTRAINT:
- Output MUST contain **ONLY compilable Java code** within a single ```java ... ``` block.
- DO NOT output any introductory text, markdown explanations outside the code block, notes, or conversational closings."""

    defect_context = extract_defect_context(source_file)
    defect_prompt_section = ""
    if defect_context:
        defect_prompt_section = (
            f"\n\n=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J) ===\n"
            f"The target class contains a known defect documented as follows:\n"
            f"```text\n{defect_context}\n```\n\n"
            f"=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===\n"
        )
        if custom_defect_req:
            defect_prompt_section += f"{custom_defect_req}\n"
        else:
            defect_prompt_section += (
                f"You MUST write at least one dedicated @Test(timeout = 4000) method that directly targets this specific failure condition.\n"
                f"The test MUST assert the expected correct behavior so that it reveals/triggers the bug on the defective version!"
            )

    user_prompt = (
        f"Here is the source code of {base_class}.java:\n\n"
        f"```java\n{target_code}\n```"
        f"{defect_prompt_section}\n\n"
        f"Generate the complete JUnit 4 test class {output_class_name} that achieves maximum line and branch coverage and targets the defect."
    )

    # เพดาน max_tokens สูงสุดตามสถาปัตยกรรมของ Gemini & Claude API คือ 65,536 (ห้ามเกิน 65536 มิฉะนั้น Google API จะโยน 400 Bad Request)
    max_tokens_val = 65536

    payload = {
        "model": model_to_use,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        "temperature": 0.2,
        "max_tokens": max_tokens_val,
        "stream": True
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    print(f"\n🚀 กำลังส่ง Request ไปยัง KKU IntelSphere API...")
    print(f"   🤖 Model: {model_to_use} ({ai_display_name})")
    print(f"   🎯 Target Class: {output_class_name}.java")
    print("   ⏳ เริ่มต้นสตรีมมิงรับโค้ดแบบ Real-time...", flush=True)

    start_time = time.time()
    try:
        response = requests.post(
            f"{API_BASE_URL}/chat/completions",
            headers=headers,
            json=payload,
            stream=True,
            timeout=(30, 600)
        )
    except Exception as e:
        print(f"❌ เกิดข้อผิดพลาดในการเชื่อมต่อ: {e}")
        return

    if response.status_code != 200:
        error_msg = response.text
        try:
            err_json = response.json()
            if "error" in err_json:
                error_msg = err_json["error"]
        except Exception:
            pass
        print(f"❌ API Error ({response.status_code}): {error_msg}")
        if "reached daily limit" in str(error_msg).lower():
            print(f"   ⚠️ โมเดล '{model_to_use}' ชนขีดจำกัดโควต้าประจำวันของระบบ KKU แล้ว (Daily Limit Reached)")
            print(f"   💡 โควต้าของโมเดลนี้จะรีเซ็ตในวันถัดไป หรือสามารถใช้โมเดล Gemini ทำงานต่อได้ก่อน")
        else:
            print("💡 แนะนำ: ลองรัน `python kku_generate.py --list-models` เพื่อดู Model ID/Name ที่ถูกต้องในระบบ")
        return

    # อ่านข้อมูลแบบ Real-time Stream เพื่อป้องกัน TCP Connection Aborted / Idle Timeout
    full_content = []
    finish_reason = ""
    usage = {}
    quota = {}
    stream_error = None

    print("   📡 กำลังสตรีมรับข้อมูลโค้ดจาก AI...", end="", flush=True)
    chunk_count = 0
    for line_bytes in response.iter_lines():
        if not line_bytes:
            continue
        line_str = line_bytes.decode("utf-8", errors="replace").strip()
        if line_str.startswith("{") and ('"status":' in line_str or '"error":' in line_str):
            try:
                err_chunk = json.loads(line_str)
                if err_chunk.get("status") and err_chunk.get("status") != 200:
                    stream_error = err_chunk
                    break
            except Exception:
                pass
        if line_str.startswith("data: "):
            json_str = line_str[6:].strip()
            if json_str == "[DONE]":
                break
            try:
                chunk = json.loads(json_str)
                choices = chunk.get("choices", [])
                if choices:
                    delta = choices[0].get("delta", {})
                    content_piece = delta.get("content", "")
                    if content_piece:
                        full_content.append(content_piece)
                    fr = choices[0].get("finish_reason")
                    if fr:
                        finish_reason = fr
                if "usage" in chunk and chunk["usage"]:
                    usage = chunk["usage"]
                if "model_quota" in chunk and chunk["model_quota"]:
                    quota = chunk["model_quota"]
                chunk_count += 1
                if chunk_count % 30 == 0:
                    print(".", end="", flush=True)
            except Exception:
                pass

    if stream_error:
        print(f"\n❌ Stream Error จากเซิร์ฟเวอร์: {stream_error}")
        return

    print(" เรียบร้อย!")

    elapsed_time = round(time.time() - start_time, 2)
    raw_content = "".join(full_content)
    java_code = extract_java_code(raw_content)

    if not java_code.strip():
        print(f"❌ AI ไม่ได้ส่งเนื้อหาโค้ด Java กลับมา (Content is empty หรือถูกบล็อก)")
        print(f"   ℹ️ finish_reason: {finish_reason}")
        return

    # 2. ดึงสถิติ Token และคำนวณสถิติ
    prompt_tokens = usage.get("prompt_tokens", 0)
    completion_tokens = usage.get("completion_tokens", len(raw_content) // 4)
    total_tokens = usage.get("total_tokens", prompt_tokens + completion_tokens)
    
    # คำนวณ Thinking / Reasoning Tokens (สำหรับโมเดลที่มี Chain-of-Thought เช่น Gemini 3.8 Flash)
    reasoning_tokens = usage.get("completion_tokens_details", {}).get("reasoning_tokens", 0)
    if not reasoning_tokens and total_tokens > (prompt_tokens + completion_tokens):
        reasoning_tokens = total_tokens - (prompt_tokens + completion_tokens)

    print(f"\n✅ สร้างโค้ดสำเร็จในเวลา {elapsed_time} วินาที!")
    if finish_reason == "length":
        print("   ⚠️ คำเตือน: โค้ดถูกตัดจบก่อนเสร็จสมบูรณ์เนื่องจากชนขีดจำกัด token (finish_reason='length')")
    if reasoning_tokens > 0:
        print(f"   📊 Token Usage -> Input: {prompt_tokens:,} | Output: {completion_tokens:,} | Thinking (CoT): {reasoning_tokens:,} | Total: {total_tokens:,}")
    else:
        print(f"   📊 Token Usage -> Input: {prompt_tokens:,} | Output: {completion_tokens:,} | Total: {total_tokens:,}")
    if quota:
        print(f"   💳 Quota เหลือวันนี้: {quota.get('daily_remaining_tokens', 'N/A')} tokens")

    # 3. บันทึกไฟล์ TestCode
    testcode_dir = os.path.join(PROJECT_ROOT, tool_dir, "TestCode")
    os.makedirs(testcode_dir, exist_ok=True)
    test_file_path = os.path.join(testcode_dir, f"{output_class_name}.java")
    with open(test_file_path, "w", encoding="utf-8") as f:
        f.write(java_code)
    print(f"   💾 บันทึกไฟล์ Test เรียบร้อยที่: {test_file_path}")

    # 4. บันทึก Prompt ที่ใช้
    prompt_dir = os.path.join(PROJECT_ROOT, tool_dir, "Prompt")
    os.makedirs(prompt_dir, exist_ok=True)
    target_class_name = os.path.splitext(os.path.basename(source_file))[0]
    prompt_record_path = os.path.join(prompt_dir, f"actual_prompt_{target_class_name}.md")
    if not os.path.exists(prompt_record_path):
        with open(prompt_record_path, "w", encoding="utf-8") as f:
            f.write(f"# Prompt Record for {output_class_name}\n\n")
            f.write(f"- **Timestamp:** {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"- **Model Used:** {model_to_use}\n\n")
            f.write(f"## System Prompt\n```text\n{system_prompt}\n```\n\n")
            f.write(f"## User Prompt\n(Source code of {target_class_name}.java included)\n")
    print(f"   📝 บันทึกประวัติ Prompt เรียบร้อยที่: {prompt_record_path}")

    # 5. บันทึกสถิติลงใน Result/
    result_dir = os.path.join(PROJECT_ROOT, tool_dir, "Result")
    os.makedirs(result_dir, exist_ok=True)
    metrics_path = os.path.join(result_dir, f"generation_metrics_{target_class_name}.md")
    with open(metrics_path, "w", encoding="utf-8") as f:
        f.write(f"# 📊 สถิติการใช้งาน AI: {ai_display_name}\n\n")
        f.write(f"* **วัน-เวลาที่ทดลอง:** {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"* **โมเดลที่ใช้:** `{model_to_use}`\n")
        f.write(f"* **คลาสเป้าหมาย:** `{package_name}.{base_class}`\n\n")
        f.write(f"### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)\n")
        f.write(f"| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |\n")
        f.write(f"| :--- | :---: | :--- |\n")
        f.write(f"| **เวลาที่ใช้สร้าง (Generation Time)** | **{elapsed_time} วินาที** | จับเวลาผ่าน Python System Clock |\n")
        f.write(f"| **Input Tokens (Prompt + Source Code)** | **{prompt_tokens:,} tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |\n")
        f.write(f"| **Output Tokens (Generated Test Code)** | **{completion_tokens:,} tokens** | คืนค่าจาก API (`usage.completion_tokens`) |\n")
        if reasoning_tokens > 0:
            f.write(f"| **Reasoning / Thinking Tokens (CoT)** | **{reasoning_tokens:,} tokens** | คำนวณจากกระบวนการคิดวิเคราะห์ภายใน (`total - (input + output)`) |\n")
        f.write(f"| **Total Tokens** | **{total_tokens:,} tokens** | คืนค่าจาก API (`usage.total_tokens`) |\n")
        f.write(f"| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |\n\n")
        if reasoning_tokens > 0:
            f.write(f"> 💡 **หมายเหตุทางวิชาการ (Token Economics):** โมเดล `{model_to_use}` มีกระบวนการให้เหตุผลภายใน (Internal Chain-of-Thought / Reasoning Process) โดยคิดวิเคราะห์ Boundary Condition เชิงลึกก่อนสร้างโค้ดทดสอบ ทำให้ Total Tokens รวมค่า Thinking Tokens ด้วย\n\n")
        if quota:
            f.write(f"> **Token Quota ประจำวัน:** ใช้ไปแล้ว {quota.get('daily_usage_tokens', 0):,} / {quota.get('daily_quota_tokens', 0):,} tokens\n")
    print(f"   📊 บันทึกตารางสถิติเรียบร้อยที่: {metrics_path}")

    # 6. บันทึกสถิติรวมลงใน results/Claude_vs_Gemini_Economics.csv ตามระเบียบวิธีวิจัยบทที่ 3
    results_dir = os.path.join(PROJECT_ROOT, "results")
    os.makedirs(results_dir, exist_ok=True)
    economics_csv = os.path.join(results_dir, "Claude_vs_Gemini_Economics.csv")

    folder_name = os.path.basename(os.path.dirname(os.path.abspath(source_file)))
    proj_name = "Unknown"
    bug_id = "Unknown"
    if "_" in folder_name:
        parts = folder_name.replace("b", "").replace("f", "").split("_")
        if len(parts) >= 2:
            proj_name = parts[0]
            bug_id = parts[1]

    file_exists = os.path.exists(economics_csv)
    try:
        import csv
        with open(economics_csv, "a", encoding="utf-8", newline="") as f:
            writer = csv.writer(f)
            if not file_exists:
                writer.writerow([
                    "Project", "Bug_ID", "Target_Class", "AI_Tool", "Model",
                    "Input_Tokens", "Output_Tokens", "Total_Tokens",
                    "Generation_Time_Sec", "Timestamp"
                ])
            writer.writerow([
                proj_name, bug_id, f"{package_name}.{base_class}" if package_name else base_class,
                ai_display_name, model_to_use, prompt_tokens, completion_tokens,
                total_tokens, elapsed_time, time.strftime('%Y-%m-%d %H:%M:%S')
            ])
        print(f"   📈 บันทึกข้อมูล Token Economics ลงไฟล์รวมเรียบร้อยที่: {economics_csv}")
    except Exception as e:
        print(f"   ⚠️ ไม่สามารถบันทึก economics CSV: {e}")

    print("\n🎉 ทำงานเสร็จสมบูรณ์ 100%! Member 4 สามารถรัน `evaluate_all.sh` ต่อได้เลย")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="KKU IntelSphere API Test Generator")
    parser.add_argument("--list-models", action="store_true", help="แสดงรายชื่อโมเดลทั้งหมดในระบบ KKU API")
    parser.add_argument("--ai", choices=["claude", "gemini"], help="เลือก AI ที่ต้องการสร้าง (claude หรือ gemini)")
    parser.add_argument("--model-id", type=str, help="ระบุ Model ID หรือ Model Name เจาะจง")
    parser.add_argument("--source-file", type=str, help="ระบุที่อยู่ไฟล์ Java ซอร์สโค้ดเป้าหมาย")
    args = parser.parse_args()

    api_key = get_api_key()

    if args.list_models:
        list_available_models(api_key)
    elif args.ai:
        run_test_generation(args.ai, api_key, args.model_id, args.source_file)
    else:
        print("\nตัวอย่างการใช้งาน:")
        print("  1. ดูรายชื่อโมเดล: python kku_generate.py --list-models")
        print("  2. เจนเทสด้วย Claude: python kku_generate.py --ai claude")
        print("  3. เจนเทสด้วย Gemini: python kku_generate.py --ai gemini")
        print("  4. ระบุไฟล์ซอร์สโค้ด: python kku_generate.py --ai gemini --source-file target_benchmark/Lang_1b/NumberUtils.java")
        print("  5. ระบุโมเดลเฉพาะ:  python kku_generate.py --ai claude --model-id 1\n")
