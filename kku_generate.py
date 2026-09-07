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

API_BASE_URL = "https://gen.ai.kku.ac.th/api/v1"

def get_api_key():
    """ดึง API Key จาก Environment Variable หรือไฟล์ .env หรือถามผู้ใช้"""
    key = os.environ.get("KKU_API_KEY")
    if key:
        return key.strip()
    
    # ลองหาจากไฟล์ .env
    env_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
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
    pattern = r"```java(.*?)```"
    matches = re.findall(pattern, text, re.DOTALL)
    if matches:
        return matches[0].strip()
    # หากไม่มี ```java ลองหา ``` ธรรมดา
    pattern_generic = r"```(.*?)```"
    matches_generic = re.findall(pattern_generic, text, re.DOTALL)
    if matches_generic:
        return matches_generic[0].strip()
    return text.strip()

def run_test_generation(target_ai, api_key, model_identifier=None):
    """ส่ง Prompt ไปยัง KKU API และบันทึกผลลัพธ์อัตโนมัติ"""
    base_dir = os.path.dirname(os.path.abspath(__file__))
    source_file = os.path.join(base_dir, "target_benchmark", "Lang_1b", "NumberUtils.java")
    
    if not os.path.exists(source_file):
        print(f"❌ ไม่พบไฟล์ซอร์สโค้ด: {source_file}")
        sys.exit(1)
        
    with open(source_file, "r", encoding="utf-8") as f:
        target_code = f.read()

    # กำหนดค่าตาม AI ที่เลือก
    if target_ai.lower() == "claude":
        tool_dir = "Claude-sonnet_4_6"
        output_class_name = "NumberUtilsClaudeTest"
        ai_display_name = "Claude Sonnet (via KKU API)"
        default_model = "claude-sonnet-4"
    else:
        tool_dir = "Gemini-3_6_flash"
        output_class_name = "NumberUtilsGeminiTest"
        ai_display_name = "Gemini Flash (via KKU API)"
        default_model = "gemini-3.5-flash"

    model_to_use = model_identifier if model_identifier else default_model

    system_prompt = (
        "You are an expert Software Quality Assurance Engineer specializing in Java Unit Testing with JUnit 4.\n"
        "Your task is to generate high-coverage Unit Tests for org.apache.commons.lang3.math.NumberUtils from Defects4J.\n\n"
        "Constraints:\n"
        "1. Package declaration MUST be: package org.apache.commons.lang3.math;\n"
        "2. Framework: JUnit 4 ONLY (import org.junit.Test; import static org.junit.Assert.*;). Do NOT use JUnit 5/Jupiter.\n"
        f"3. Class name MUST be: public class {output_class_name}\n"
        "4. Fully compatible with JDK 8.\n"
        "5. Maximize Line & Branch Coverage on createNumber(String str).\n"
        "6. Return ONLY pure executable Java code inside ```java block. No greetings or Markdown explanations."
    )

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
        "max_tokens": 4096
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
            timeout=180
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

    # 2. ดึงสถิติ Token
    usage = res_json.get("usage", {})
    prompt_tokens = usage.get("prompt_tokens", 0)
    completion_tokens = usage.get("completion_tokens", 0)
    total_tokens = usage.get("total_tokens", prompt_tokens + completion_tokens)
    quota = res_json.get("model_quota", {})

    print(f"\n✅ สร้างโค้ดสำเร็จในเวลา {elapsed_time} วินาที!")
    print(f"   📊 Token Usage -> Input: {prompt_tokens} | Output: {completion_tokens} | Total: {total_tokens}")
    if quota:
        print(f"   💳 Quota เหลือวันนี้: {quota.get('daily_remaining_tokens', 'N/A')} tokens")

    # 3. บันทึกไฟล์ TestCode
    testcode_dir = os.path.join(base_dir, tool_dir, "TestCode")
    os.makedirs(testcode_dir, exist_ok=True)
    test_file_path = os.path.join(testcode_dir, f"{output_class_name}.java")
    with open(test_file_path, "w", encoding="utf-8") as f:
        f.write(java_code)
    print(f"   💾 บันทึกไฟล์ Test เรียบร้อยที่: {test_file_path}")

    # 4. บันทึก Prompt ที่ใช้
    prompt_dir = os.path.join(base_dir, tool_dir, "Prompt")
    os.makedirs(prompt_dir, exist_ok=True)
    prompt_record_path = os.path.join(prompt_dir, f"actual_prompt_used.md")
    with open(prompt_record_path, "w", encoding="utf-8") as f:
        f.write(f"# Prompt Record for {output_class_name}\n\n")
        f.write(f"- **Timestamp:** {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"- **Model Used:** {model_to_use}\n\n")
        f.write(f"## System Prompt\n```text\n{system_prompt}\n```\n\n")
        f.write(f"## User Prompt\n(Source code of NumberUtils.java included)\n")
    print(f"   📝 บันทึกประวัติ Prompt เรียบร้อยที่: {prompt_record_path}")

    # 5. บันทึกสถิติลงใน Result/
    result_dir = os.path.join(base_dir, tool_dir, "Result")
    os.makedirs(result_dir, exist_ok=True)
    metrics_path = os.path.join(result_dir, "generation_metrics.md")
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
