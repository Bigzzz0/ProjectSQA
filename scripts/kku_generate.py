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

def run_test_generation(target_ai, api_key, model_identifier=None):
    """ส่ง Prompt ไปยัง KKU API และบันทึกผลลัพธ์อัตโนมัติ"""
    source_file = os.path.join(PROJECT_ROOT, "target_benchmark", "Lang_1b", "NumberUtils.java")
    
    if not os.path.exists(source_file):
        print(f"❌ ไม่พบไฟล์ซอร์สโค้ด: {source_file}")
        sys.exit(1)
        
    with open(source_file, "r", encoding="utf-8") as f:
        target_code = f.read()

    # กำหนดค่าตาม AI ที่เลือก
    if target_ai.lower() == "claude":
        tool_dir = "Claude-sonnet_5"
        output_class_name = "NumberUtilsClaudeTest"
        ai_display_name = "Claude Sonnet 5 (via KKU API)"
        default_model = "claude-sonnet-5"
    else:
        tool_dir = "Gemini-3_8_flash"
        output_class_name = "NumberUtilsGeminiTest"
        ai_display_name = "Gemini 3.8 Flash (via KKU API)"
        default_model = "gemini-3.8-flash"

    model_to_use = model_identifier if model_identifier else default_model

    system_prompt = f"""You are a Principal Software Quality Assurance (SQA) Engineer and Test Automation Specialist.
Your mission is to perform advanced White-Box Testing on an Apache Commons Lang Java source class from the Defects4J benchmark to generate a production-grade, fault-revealing JUnit 4 test suite.

---

###  Core Objectives:
1. Maximize **Line Coverage** and **Branch Coverage (Decision/Condition Coverage)** on the core numeric parsing logic (specifically `createNumber(String str)` and related conversion paths).
2. Expose latent defects, boundary regressions, and type-handling flaws (focusing on Lang-1b defect patterns).
3. Ensure **100% deterministic, zero-flakiness, and zero-compilation-error** execution on Java 8 / Defects4J.

---

###  Engineering Guidelines & Rules:

#### 1. Imports & Environment Hygiene
- Target Environment: Strictly **Java 8** and **JUnit 4**.
- Package Declaration: Must declare `package org.apache.commons.lang3.math;` as line 1.
- Mandatory Explicit Imports:
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
  * `import java.math.BigInteger;`
  * `import java.math.BigDecimal;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies (e.g., `System.currentTimeMillis()`, `new Random()`).
- Class Name: Name the test class `{output_class_name}` (`public class {output_class_name}`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid input scenarios expecting `NumberFormatException`, test them using `@Test(expected = NumberFormatException.class, timeout = 4000)` or explicit `try {{ ... fail(); }} catch (NumberFormatException expected) {{ ... }}`.
- Granular Assertions: Always assert both the **Exact Returned Type** (e.g., `assertTrue(result instanceof Long)`) and the **Exact Value** (`assertEquals(...)`).

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Standard Numeric Primitives** (`Integer`, `Long`, `Float`, `Double`, `BigInteger`, `BigDecimal`).
- **Partition B: Hexadecimal Representations (Critical Bug Zone)**:
  * Prefixes: `0x`, `0X`, `#`
  * Sign handling: Positive (`0x10`), Negative (`-0x10`, `-#1234`)
  * Boundary & Large Hex: Values exceeding 32-bit signed int (e.g., `0x80000000`, `0xFFFFFFFF`)
  * Suffix with Hex: e.g., `0x12L`, `0x12l`
- **Partition C: Scientific / Exponent Notations**:
  * Formats: `e`, `E`, signs in exponent (`1.2e+3`, `-2.5E-4`, `00E0`)
- **Partition D: Type Qualifiers & Case Sensitivity**:
  * Suffixes: `f`, `F`, `d`, `D`, `l`, `L`
- **Partition E: Edge & Degenerate Cases**:
  * `null`, empty string `""`, single whitespace, strings with leading/trailing characters, all zeros `000`, multiple signs/dots (`--1`, `1.2.3`).

#### 4. In-Code Reasoning (Mental Sandbox)
Before writing the Java test methods, include an in-line Javadoc/block comment at the top of the class summarizing your:
`/* [Branch & Defect Analysis Matrix] */` listing the specific decision branches and boundary conditions being targeted.

---

###  ABSOLUTE OUTPUT CONSTRAINT:
- Output MUST contain **ONLY compilable Java code** within a single ```java ... ``` block.
- DO NOT output any introductory text, markdown explanations outside the code block, notes, or conversational closings."""

    user_prompt = (
        f"Here is the source code of NumberUtils.java:\n\n"
        f"```java\n{target_code}\n```\n\n"
        f"Generate the complete JUnit 4 test class {output_class_name} that achieves maximum line and branch coverage."
    )

    payload = {
        "model": model_to_use,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        "temperature": 0.2,
        "max_tokens": 65536
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    print(f"\n🚀 กำลังส่ง Request ไปยัง KKU IntelSphere API...")
    print(f"   🤖 Model: {model_to_use} ({ai_display_name})")
    print(f"   🎯 Target Class: {output_class_name}.java")
    print("   ⏳ กรุณารอสักครู่ (กำลังจับเวลาการสร้าง)...")

    start_time = time.time()
    try:
        response = requests.post(
            f"{API_BASE_URL}/chat/completions",
            headers=headers,
            json=payload,
            timeout=300
        )
    except Exception as e:
        print(f"❌ เกิดข้อผิดพลาดในการเชื่อมต่อ: {e}")
        return

    elapsed_time = round(time.time() - start_time, 2)

    if response.status_code != 200:
        print(f"❌ API Error {response.status_code}: {response.text}")
        print("💡 แนะนำ: ลองรัน `python kku_generate.py --list-models` เพื่อดู Model ID/Name ที่ถูกต้องในระบบ")
        return

    res_json = response.json()
    
    # 1. ดึงข้อความตอบกลับ
    choices = res_json.get("choices", [])
    if not choices:
        print("❌ ไม่พบ choices ในคำตอบจาก AI")
        return
    raw_content = choices[0].get("message", {}).get("content", "")
    java_code = extract_java_code(raw_content)

    # 2. ดึงสถิติ Token และตรวจสอบ finish_reason
    finish_reason = choices[0].get("finish_reason", "")
    usage = res_json.get("usage", {})
    prompt_tokens = usage.get("prompt_tokens", 0)
    completion_tokens = usage.get("completion_tokens", 0)
    total_tokens = usage.get("total_tokens", prompt_tokens + completion_tokens)
    quota = res_json.get("model_quota", {})

    print(f"\n✅ สร้างโค้ดสำเร็จในเวลา {elapsed_time} วินาที!")
    if finish_reason == "length":
        print("   ⚠️ คำเตือน: โค้ดถูกตัดจบก่อนเสร็จสมบูรณ์เนื่องจากชนขีดจำกัด token (finish_reason='length')")
    print(f"   📊 Token Usage -> Input: {prompt_tokens} | Output: {completion_tokens} | Total: {total_tokens}")
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
        f.write(f"* **คลาสเป้าหมาย:** `org.apache.commons.lang3.math.NumberUtils` (Lang-1b)\n\n")
        f.write(f"### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)\n")
        f.write(f"| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |\n")
        f.write(f"| :--- | :---: | :--- |\n")
        f.write(f"| **เวลาที่ใช้สร้าง (Generation Time)** | **{elapsed_time} วินาที** | จับเวลาผ่าน Python System Clock |\n")
        f.write(f"| **Input Tokens (Prompt + Source Code)** | **{prompt_tokens:,} tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |\n")
        f.write(f"| **Output Tokens (Generated Test Code)** | **{completion_tokens:,} tokens** | คืนค่าจาก API (`usage.completion_tokens`) |\n")
        f.write(f"| **Total Tokens** | **{total_tokens:,} tokens** | คืนค่าจาก API (`usage.total_tokens`) |\n")
        f.write(f"| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |\n\n")
        if quota:
            f.write(f"> **Token Quota ประจำวัน:** ใช้ไปแล้ว {quota.get('daily_usage_tokens', 0):,} / {quota.get('daily_quota_tokens', 0):,} tokens\n")
    print(f"   📊 บันทึกตารางสถิติเรียบร้อยที่: {metrics_path}")
    print("\n🎉 ทำงานเสร็จสมบูรณ์ 100%! Member 4 สามารถรัน `evaluate_all.sh` ต่อได้เลย")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="KKU IntelSphere API Test Generator")
    parser.add_argument("--list-models", action="store_true", help="แสดงรายชื่อโมเดลทั้งหมดในระบบ KKU API")
    parser.add_argument("--ai", choices=["claude", "gemini"], help="เลือก AI ที่ต้องการสร้าง (claude หรือ gemini)")
    parser.add_argument("--model-id", type=str, help="ระบุ Model ID หรือ Model Name เจาะจง")
    args = parser.parse_args()

    api_key = get_api_key()

    if args.list_models:
        list_available_models(api_key)
    elif args.ai:
        run_test_generation(args.ai, api_key, args.model_id)
    else:
        print("\nตัวอย่างการใช้งาน:")
        print("  1. ดูรายชื่อโมเดล: python kku_generate.py --list-models")
        print("  2. เจนเทสด้วย Claude: python kku_generate.py --ai claude")
        print("  3. เจนเทสด้วย Gemini: python kku_generate.py --ai gemini")
        print("  4. ระบุโมเดลเฉพาะ:  python kku_generate.py --ai claude --model-id 1\n")
