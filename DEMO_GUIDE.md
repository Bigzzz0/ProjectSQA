# 🎬 คู่มือการสาธิตระบบสดต่อหน้าอาจารย์ (Live Demo & Reproduction Guide)
## การจำลองและรันระบบทดสอบจริง (Step-by-Step Live Demonstration Script)

**วิชา:** CP353201 การประกันคุณภาพซอฟต์แวร์ (Software Quality Assurance)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**จัดทำโดย:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)  

---

## 📌 บทนำและลำดับการสาธิตสด (Demo Flow Overview)

คู่มือนี้ออกแบบมาเพื่อให้สมาชิกในกลุ่มสามารถเปิดหน้าจอ Terminal และสาธิตให้อาจารย์เห็นกระบวนการทำงานจริงแบบจับต้องได้ ตั้งแต่โครงสร้างพื้นฐาน Docker, การประเมินชุดทดสอบจริง, การจำแนก 5 สถานะ Bug-Level FDR, ไปจนถึงการพล็อตกราฟ 300 DPI และเปิดตาราง Excel สดๆ

| ลำดับขั้นตอน | รายการสาธิต | เวลาที่ใช้ | เครื่องมือที่ใช้ |
| :---: | :--- | :---: | :--- |
| **Stage 1** | ตรวจสอบ Docker Container และ Defects4J Environment | 1 นาที | PowerShell / Docker |
| **Stage 2** | สาธิตการรัน Universal Benchmark Runner บนบั๊กจริง (Math-2) | 2 นาที | `run_benchmark.py` |
| **Stage 3** | แสดงการพิสูจน์สถานะ `BUG_DETECTED` บนเวอร์ชัน `b` และ `f` | 1 นาที | Defects4J CLI |
| **Stage 4** | สาธิตการรันสคริปต์สถิติและการพล็อตกราฟอัตโนมัติ 6 รูปแบบ | 1 นาที | `advanced_data_analytics.py` |
| **Stage 5** | เปิดไฟล์ Excel รวม 6 ชีทและ Data Dictionary สรุปผล | 1 นาที | Excel / VS Code |

---

## 🚀 ขั้นที่ 1: ตรวจสอบความพร้อมของ Docker Container

เปิด PowerShell บนเครื่อง แล้วสั่งตรวจสอบสถานะของ Container กลาง:

```powershell
# 1.1 ตรวจสอบว่า Container defects4j_sqa กำลังทำงานอยู่หรือไม่
docker ps --filter "name=defects4j_sqa"

# หากยังไม่ได้เปิด Container ให้สั่งเปิดด้วยคำสั่ง:
docker start defects4j_sqa
```

ทดสอบการเรียกใช้งาน Defects4J CLI ภายใน Container:
```powershell
docker exec defects4j_sqa defects4j info -p Math -b 2
```
> **💡 สิ่งที่อาจารย์จะเห็น:** ข้อมูลทางการของข้อบกพร่อง Math-2 (คลาส `HypergeometricDistribution`, Root cause เป็น Integer Overflow ในการคำนวณ Variance/Mean)

---

## 🚀 ขั้นที่ 2: สาธิตการรัน Universal Benchmark Runner แบบสดๆ

สั่งรันตัวประเมินกลางบนกรณีศึกษา **Math-2** เพื่อแสดงการวัดผล Coverage และการตรวจจับข้อบกพร่อง:

```powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Math --bug 2
```

### สิ่งที่ระบบจะทำงานและแสดงผลบนหน้าจอ:
1. ดึง Source code ของ Math-2 เวอร์ชันมีบั๊ก (`Math_2b`) และเวอร์ชันแก้แล้ว (`Math_2f`)
2. ค้นหาชุดทดสอบของทั้ง 4 เทคนิคใน `TestCode/`:
   - `Combinatorial_IPO`: `HypergeometricDistribution_IPOTest.java`
   - `MIO_Algorithm`: `HypergeometricDistribution_ESTest.java`
   - `Deepseek-v4_flash`: `HypergeometricDistributionDeepseekTest.java`
   - `Gemini-3_8_flash`: `HypergeometricDistributionGeminiTest.java`
3. ทำการคอมไพล์และรันการทดสอบ พร้อมจับเวลา
4. วัด Cobertura Target-Class Coverage
5. รายงานผลสถานะการตรวจจับข้อบกพร่องทั้ง 5 สถานะทันที!

---

## 🚀 ขั้นที่ 3: สาธิตการพิสูจน์สถานะ `BUG_DETECTED` ตามหลักวิชาการ

เพื่ออธิบายให้อาจารย์เห็นว่าทำไมเทคนิค AI (Gemini) ถึงได้สถานะ **`BUG_DETECTED`** ตามกฎ 5 สถานะ:

```powershell
# 3.1 ทดสอบรันบนเวอร์ชันมีบั๊ก (Math-2b) -> จะต้องเกิด FAILURE
docker exec -w /tmp/Math_2b defects4j_sqa defects4j test

# 3.2 ทดสอบรันบนเวอร์ชันแก้บั๊กแล้ว (Math-2f) -> จะต้อง PASS 100%
docker exec -w /tmp/Math_2f defects4j_sqa defects4j test
```

> **🗣️ สิ่งที่ต้องชี้แจงอาจารย์:**  
> "นี่คือหลักฐานเชิงประจักษ์ของ **กฎความซื่อตรงของตัวหาร (Denominator Integrity Rule)** ครับอาจารย์ ชุดทดสอบของ Gemini สามารถกระตุ้นจุดบกพร่องบนโค้ด `b` จนเกิด Failure ได้จริง และเมื่อนำไปรันบนโค้ด `f` ที่นักพัฒนาได้แก้บั๊กแล้ว เทสผ่าน 100% อย่างสมบูรณ์แบบ จึงได้รับการรับรองเป็นสถานะ `BUG_DETECTED` ปราศจาก False Positive ครับ"

---

## 🚀 ขั้นที่ 4: สาธิตการรันสคริปต์สถิติขั้นสูงและการสร้างกราฟ 6 รูปแบบ

เปิด Terminal บน Windows Host และสั่งรันสคริปต์วิเคราะห์ข้อมูลขั้นสูง:

```powershell
& "C:\Users\User\AppData\Local\Programs\Python\Python313\python.exe" scripts/advanced_data_analytics.py
```

### การตอบสนองของระบบ:
```text
=================================================================
🚀 ProjectSQA Advanced Data Analytics Engine
=================================================================
🔬 1. Running Statistical Hypothesis Tests & Effect Sizes...
⏱️ 2. Analyzing MIO Search Budget Scaling (30s vs 60s vs 120s)...
✅ Generated: results\figure5_budget_scaling.png
🤝 3. Analyzing Ensemble Fault Detection Synergy & Overlap Matrix...
✅ Generated: results\figure6_ensemble_overlap.png
🏛️ 4. Analyzing Single-Class vs Multi-Class Defect Resilience...
💰 5. Evaluating AI Economics & Cost per Detected Bug...
✅ Full analytics JSON saved: results\advanced_analytics.json
📊 6. Exporting Professional Multi-Tab Excel Workbook...
✅ Master Excel saved: results\Master_Benchmark_Results.xlsx
✅ Data Dictionary saved: results\DATA_DICTIONARY.md
✅ Advanced Analytics Report saved: results\advanced_analytics_report.md
=================================================================
🎉 Advanced Data Analytics Pipeline Completed Successfully!
=================================================================
```

---

## 🚀 ขั้นที่ 5: นำเสนอไฟล์ผลลัพธ์ระดับพรีเมียม (Showcasing Deliverables)

1. **เปิดไฟล์รูปภาพกราฟวิชาการ 300 DPI ทั้ง 6 รูปในโฟลเดอร์ `results/`:**
   - `figure1_coverage_comparison.png`: เปรียบเทียบ Coverage ภาพรวม และ Effective Coverage
   - `figure2_fdr_distribution.png`: แผนภูมิสัดส่วน 5 สถานะ FDR
   - `figure3_projects_breakdown.png`: ผลการทดลองจำแนก 17 โปรเจกต์
   - `figure4_ai_economics.png`: กราฟเปรียบเทียบเวลาและความคุ้มค่าของโทเค็น
   - `figure5_budget_scaling.png`: กราฟจุดอิ่มตัวของการค้นหาใน MIO (Diminishing Returns)
   - `figure6_ensemble_overlap.png`: แผนภูมิการผสานพลังร่วมตรวจพบ 105 บั๊ก
2. **เปิดไฟล์สมุดงาน Excel หลายชีท (`results/Master_Benchmark_Results.xlsx`):**
   - ชีท 1: `Executive_Summary`
   - ชีท 2: `Master_Evaluations_Data` (2,804 แถว พร้อมสูตรและสีจัดหมวดหมู่)
   - ชีท 3: `Technique_Descriptive_Stats`
   - ชีท 4: `Hypothesis_Testing_A12`
   - ชีท 5: `MIO_Budget_Scaling`
   - ชีท 6: `AI_Economics_Cost`
3. **เปิดเล่มรายงานฉบับสมบูรณ์ (`Final_Report.md`)** เพื่อสรุปข้อเสนอแนะเชิงวิศวกรรมสำหรับอุตสาหกรรม

---
*คู่มือฉบับนี้พร้อมใช้งานจริงสำหรับการนำเสนอสดต่อหน้าอาจารย์ผู้สอน เพื่อความมั่นใจและได้คะแนนเต็ม 100%!*
