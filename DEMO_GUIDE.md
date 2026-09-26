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
| **Stage 2** | ตรวจผลจริง Chart-3 / Gemini ที่ตรวจพบข้อบกพร่อง | 2 นาที | Master CSV / Run log |
| **Stage 3** | แสดงการพิสูจน์สถานะ `BUG_DETECTED` บนเวอร์ชัน `b` และ `f` | 1 นาที | Defects4J CLI |
| **Stage 4** | สาธิตการรันสคริปต์สถิติและการพล็อตกราฟอัตโนมัติ 6 รูปแบบ | 1 นาที | `advanced_data_analytics.py` |
| **Stage 5** | เปิดไฟล์ Excel รวม 8 ชีทและ Data Dictionary สรุปผล | 1 นาที | Excel / VS Code |

---

> **สถานะ:** benchmark รอบปัจจุบันเสร็จแล้ว; 2,768 suite evaluations มีผลครบ, 648 ช่องไม่มี suite และถูกระบุ `NO_SUITE`. ใช้ master dataset กับ structured run log ด้านล่างสำหรับการสาธิต

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

## 🚀 ขั้นที่ 2: ตรวจผลการรันที่มี provenance

ใช้ Chart-3 / Gemini เป็นตัวอย่างที่ได้สถานะ BUG_DETECTED จาก runner จริง ตรวจแถว master และเปิด structured run log:

    Import-Csv results/master_benchmark_summary.csv | Where-Object {
      $_.Project -eq 'Chart' -and $_.Bug_ID -eq '3' -and $_.Technique -eq 'Gemini 3.8 Flash'
    } | Format-List
    Get-Content results/run_logs/Chart-3-gemini-1790350744.json

เช็กว่า Run_ID และ Suite_SHA256 ใน master ตรงกับ log และผลบั๊กมี failing tests บน buggy พร้อมรายการ fixed_failures ว่าง ก่อนใช้เป็นกรณีสาธิต

---

## 🚀 ขั้นที่ 3: อธิบายเกณฑ์ BUG_DETECTED

BUG_DETECTED นับได้เมื่อชุดทดสอบ fail บน buggy และ pass บน fixed เท่านั้น เปิดผลดิบรายบั๊กใน results/run_logs และผลรวมใน master CSV เพื่อไล่กลับจากตัวเลขไปยังหลักฐาน

ถ้าต้องแสดงหน้าจอ Defects4J แบบสด ให้ทำหลังคิว benchmark หลักจบแล้วเท่านั้น ใช้ suite และ checkout ของบั๊กเดียวกัน และเก็บ output ไว้ใน run log ก่อนนำเสนอ

---

## 🚀 ขั้นที่ 4: สร้าง snapshot, สถิติ และกราฟ

รันหลังคิว benchmark หยุดหรือเสร็จ เพื่อให้ CSV, Excel, JSON และกราฟมาจาก snapshot เดียวกัน:

    .\.venv\Scripts\python.exe scripts/consolidate_master_results.py
    .\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
    .\.venv\Scripts\python.exe scripts/plot_results.py

ตรวจ `results/master_descriptive_stats.json` ก่อนพูดถึงผล ปัจจุบัน `results_complete` และ `available_suite_evaluations_complete` เป็น true; หากมีการเปลี่ยนข้อมูลภายหลัง ให้ใช้สถานะและตัวหารล่าสุดในไฟล์นี้

---

## 🚀 ขั้นที่ 5: ตรวจไฟล์ส่งมอบ

- กราฟ: figure1 ถึง figure6 ใน results/
- Excel: results/Master_Benchmark_Results.xlsx มี Summary, Master evaluations, MIO generation budget, Hypothesis tests, AI generation logs, Ensemble, Single vs multiclass และ Data dictionary
- Data dictionary และรายงาน snapshot: results/DATA_DICTIONARY.md และ results/advanced_analytics_report.md
- README, Final_Report และ PRESENTATION_SLIDES ต้องตรงกับ status และค่าจาก snapshot เดียวกัน
- ยืนยันก่อนส่งว่าไม่มีแถว key ซ้ำ และสุ่มเปิด Run_Log อย่างน้อยหนึ่งรายการต่อเทคนิค

หากผลประเมินยังไม่ครบ ให้รายงานจำนวน suite, จำนวน attempted, จำนวน DONE, จำนวน BUG_DETECTED และจำนวน NO_SUITE แยกกัน ห้ามนำผลเก่ามาแทนค่าที่หายไป

---
