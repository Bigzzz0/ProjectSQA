#!/usr/bin/env python3
"""Export delivery reports to Markdown and CSV format for Member 4."""

import json
import csv
import os

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
json_path = os.path.join(PROJECT_ROOT, "results", "delivery_report_m3.json")

with open(json_path, encoding="utf-8") as f:
    items = json.load(f)

# 1. CSV
csv_path = os.path.join(PROJECT_ROOT, "results", "delivery_report_m3.csv")
fields = ["bug", "tech", "target_cls", "package_cls", "file_rel", "sha256", "prompt_rel"]
with open(csv_path, "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.DictWriter(f, fieldnames=fields)
    writer.writeheader()
    writer.writerows(items)
print(f"Generated {csv_path}")

# 2. Markdown
md_path = os.path.join(PROJECT_ROOT, "results", "DELIVERY_REPORT_M3.md")
with open(md_path, "w", encoding="utf-8") as f:
    f.write("# 📋 Member 3 Test Suite Delivery Report (Audit Gap Resolution)\n\n")
    f.write("**ผู้ส่งมอบ:** Member 3 (AI Prompt Engineer - DeepSeek & Gemini)  \n")
    f.write("**ผู้รับมอบ:** Member 4 (Infra Lead & Data Analysis)  \n")
    f.write("**วันที่:** 2026-09-26  \n\n")
    f.write("## 📌 สรุปภาพรวมการแก้ไข 31 ช่อง\n")
    f.write("- **พร้อมนำเข้า Benchmark ทันที (MATCHING_CANDIDATE_NOT_ADMITTED):** 29 ช่อง (ผ่านการ Matcher 100%)\n")
    f.write("- **พักการประเมินไว้ก่อนตามข้อตกลง (TARGET_MISMATCH_CANDIDATE):** 2 ช่อง (Math-13 DeepSeek & Gemini)\n\n")
    f.write("## 📊 ตารางส่งมอบรายละเอียดทั้ง 31 รายการ\n\n")
    f.write("| # | Project-Bug | Technique | Target Class | Package / Test Class | SHA-256 Checksum | File Path |\n")
    f.write("| :-: | :--- | :--- | :--- | :--- | :--- | :--- |\n")
    for i, it in enumerate(items, 1):
        f.write(f"| {i} | **{it['bug']}** | {it['tech']} | `{it['target_cls']}` | `{it['package_cls']}` | `{it['sha256']}` | [`{it['file_rel']}`](../{it['file_rel']}) |\n")
    f.write("\n## ⚙️ Configuration & Prompt Records\n")
    f.write("- **KKU IntelSphere API Engine:** `https://gen.ai.kku.ac.th/api/v1`\n")
    f.write("- **DeepSeek Model:** `deepseek-v4-flash` (JUnit 4, Java 8, `@Test(timeout = 4000)`)\n")
    f.write("- **Gemini Model:** `gemini-3.8-flash` (JUnit 4, Java 8, `@Test(timeout = 4000)`)\n")
    f.write("- **Audit CSV:** [`results/suite_gap_audit.csv`](./suite_gap_audit.csv)\n")
    f.write("- **Token Economics:** [`results/Deepseek_vs_Gemini_Economics.csv`](./Deepseek_vs_Gemini_Economics.csv)\n")
    f.write("- **Machine-readable JSON:** [`results/delivery_report_m3.json`](./delivery_report_m3.json)\n")

print(f"Generated {md_path}")
