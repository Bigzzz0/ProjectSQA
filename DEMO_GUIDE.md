# 🎬 คู่มือการสาธิตระบบสดต่อหน้าอาจารย์ (Live Demo & Reproduction Guide)
## การจำลองและรันระบบทดสอบจริง (Step-by-Step Live Demonstration Script)

**วิชา:** CP353201 การประกันคุณภาพซอฟต์แวร์ (Software Quality Assurance)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**จัดทำโดย:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)  

---

## 📌 บทนำและลำดับการสาธิตสด (Demo Flow Overview)

คู่มือนี้ใช้สาธิตผลของ IPO, MIO, DeepSeek และ Gemini จาก master CSV กับ structured run logs ที่ตรวจสอบย้อนกลับได้ แล้วอธิบายตัวอย่าง `BUG_DETECTED` ว่าล้มเหลวบน buggy และผ่านบน fixed อย่างไร โดยไม่รัน benchmark ทั้งคิวซ้ำบนเวที

| ลำดับขั้นตอน | รายการสาธิต | เวลาที่ใช้ | เครื่องมือที่ใช้ |
| :---: | :--- | :---: | :--- |
| **Stage 1** | ตรวจสอบ Docker Container และ Defects4J Environment | 1 นาที | PowerShell / Docker |
| **Stage 2** | ตรวจตัวอย่าง `BUG_DETECTED` จากทั้ง 4 เทคนิค | 2 นาที | Master CSV / Run logs |
| **Stage 3** | แสดงการพิสูจน์สถานะ `BUG_DETECTED` บนเวอร์ชัน `b` และ `f` | 1 นาที | Defects4J CLI |
| **Stage 4** | แสดงกราฟและ workbook จาก snapshot ล่าสุด | 1 นาที | Results / Excel |
| **Stage 5** | เปิดไฟล์ Excel รวม 8 ชีทและ Data Dictionary สรุปผล | 1 นาที | Excel / VS Code |

---

> **Snapshot:** 26 กันยายน 2026. Master มี 3,416 bug–technique rows; suite ที่มีอยู่ประเมินครบ 2,797 คู่ และ 619 คู่เป็น `NO_SUITE`. `available_suite_evaluations_complete: true` หมายถึงไม่มี suite ที่มีอยู่ค้างประเมิน ไม่ได้หมายถึงมี suite ครบทั้ง catalog

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
> **💡 สิ่งที่อาจารย์จะเห็น:** ข้อมูล Defects4J ของ Math-2 และคลาส `HypergeometricDistribution`; ตัวอย่างบั๊กเกี่ยวกับ integer overflow ในการคูณค่าที่ใช้คำนวณ numerical mean ก่อน cast เป็น `double`.

---

## 🚀 ขั้นที่ 2: ตรวจผลจากทั้งสี่เทคนิค

ใช้ตัวอย่างที่มีสถานะ `BUG_DETECTED` ใน master และ structured log จริงของแต่ละเทคนิค:

| เทคนิค | Bug | Suite | Line / branch coverage | Run ID | Buggy failures / fixed failures |
|---|---|---|---:|---|---:|
| Native IPO | Chart-14 | `XYPlot_IPOTest.java` | 4.41% / 1.74% | `Chart-14-ipo-1790351771` | 48 / 0 |
| MIO (EvoSuite) | Jsoup-14 | `TokeniserState_ESTest.java`; `Tokeniser_ESTest.java` | 0.00% / 0.00% | `Jsoup-14-mio-1790379300` | 31 / 0 |
| DeepSeek V4 Flash | Closure-105 | `FoldConstantsDeepseekTest.java` | 26.49% / 25.44% | `Closure-105-deepseek-1790360926` | 2 / 0 |
| Gemini 3.8 Flash | Chart-3 | `TimeSeriesGeminiTest.java` | 94.74% / 87.63% | `Chart-3-gemini-1790350744` | 2 / 0 |

อ่าน log ทั้งสี่รายการจากโฟลเดอร์ repository:

```powershell
$logPaths = @(
  'results/run_logs/Chart-14-ipo-1790351771.json',
  'results/run_logs/Jsoup-14-mio-1790379300.json',
  'results/run_logs/Closure-105-deepseek-1790360926.json',
  'results/run_logs/Chart-3-gemini-1790350744.json'
)
$logRows = foreach ($path in $logPaths) {
  $log = Get-Content -Raw $path | ConvertFrom-Json
  [pscustomobject]@{
    Bug = "$($log.project)-$($log.bug_id)"
    Technique = $log.technique
    Status = $log.fault_detection_status
    BuggyFailures = $log.buggy_failures.Count
    FixedFailures = $log.fixed_failures.Count
    RunId = $log.run_id
    SuiteSHA256 = $log.suite_sha256
  }
}
$logRows | Format-Table -AutoSize
```

ตัวอย่าง MIO / Jsoup-14 มี coverage ที่บันทึกเป็น 0.00% แต่ runner ยังยืนยัน `BUG_DETECTED` จาก failure บน buggy และไม่มี failure บน fixed; ให้นำเสนอทั้งสองค่าตาม log และไม่อนุมานสาเหตุจาก coverage เพียงอย่างเดียว.

---

## 🚀 ขั้นที่ 3: อธิบายเกณฑ์ BUG_DETECTED

BUG_DETECTED นับได้เมื่อชุดทดสอบ fail บน buggy และ pass บน fixed เท่านั้น เปิดผลดิบรายบั๊กใน results/run_logs และผลรวมใน master CSV เพื่อไล่กลับจากตัวเลขไปยังหลักฐาน

หากจะรัน Defects4J สด ให้เลือกหนึ่ง suite ที่ทดสอบไว้ล่วงหน้าและใช้ buggy/fixed checkout ของ bug เดียวกันเท่านั้น; อย่ารันคิว 854 บั๊กระหว่างการนำเสนอ. ตัวอย่างหลักใน stage นี้คือ Chart-3 / Gemini ซึ่งมี `buggy_failures` 2 รายการและ `fixed_failures` ว่างใน log

---

## 🚀 ขั้นที่ 4: แสดงผลวิเคราะห์จาก snapshot ปัจจุบัน

ระหว่างนำเสนอให้เปิดกราฟและ workbook ที่สร้างจาก snapshot เดียวกัน ไม่ต้องสร้างไฟล์ใหม่สด ๆ เว้นแต่มีการเปลี่ยนผล benchmark; ถ้าต้องทำซ้ำหลังการนำเสนอ ใช้คำสั่งนี้:

    .\.venv\Scripts\python.exe scripts/consolidate_master_results.py
    .\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
    .\.venv\Scripts\python.exe scripts/plot_results.py

ตรวจ `results/master_descriptive_stats.json` ก่อนพูดถึงผล ปัจจุบัน `results_complete` และ `available_suite_evaluations_complete` เป็น true; หากมีการเปลี่ยนข้อมูลภายหลัง ให้ใช้สถานะและตัวหารล่าสุดในไฟล์นี้

---

## 🚀 ขั้นที่ 5: ตรวจไฟล์ส่งมอบ

- กราฟ: figure1 ถึง figure6 ใน results/
- Excel: results/Master_Benchmark_Results.xlsx มี Summary, Master evaluations, MIO generation budget, Hypothesis tests, AI generation logs, Ensemble, Single vs multiclass และ Data dictionary
- Data dictionary และรายงาน snapshot: results/DATA_DICTIONARY.md และ results/advanced_analytics_report.md
- README, DOCX/PDF report, PRESENTATION_SLIDES และ DEMO_GUIDE ต้องตรงกับ snapshot: 3,416 แถว, 2,797 suite evaluations, 619 `NO_SUITE`, unresolved outcomes 0
- ยืนยันก่อนนำเสนอว่า Run ID, suite hash และ log ตรงกันทั้งสี่ตัวอย่างใน Stage 2
- workbook ปัจจุบันมี 8 sheets; เปิด Summary และ Data dictionary ประกอบการอธิบาย

หากผลประเมินยังไม่ครบ ให้รายงานจำนวน suite, จำนวน attempted, จำนวน DONE, จำนวน BUG_DETECTED และจำนวน NO_SUITE แยกกัน ห้ามนำผลเก่ามาแทนค่าที่หายไป

---
