# 🎬 คู่มือสาธิตระบบและหลักฐานผลรัน (Demo & Reproduction Guide)
## ขั้นตอนสาธิตสดควบคู่กับผล benchmark ที่บันทึกไว้

**วิชา:** CP353201 การประกันคุณภาพซอฟต์แวร์ (Software Quality Assurance)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**จัดทำโดย:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)  

---

## 📌 บทนำและลำดับการสาธิตสด (Demo Flow Overview)

คู่มือนี้สาธิต suite และผลประเมินของ IPO, MIO, DeepSeek และ Gemini โดยเปิด master CSV, suite จริง และ structured run logs ที่ตรวจสอบย้อนกลับได้ พร้อมตรวจ environment สดบนเวที ขั้นตอนหลักเป็นการสาธิตหลักฐานจากผลรันจริงที่บันทึกไว้ ไม่ได้สร้าง suite ใหม่หรือรัน benchmark ทั้งคิวสด

| ลำดับขั้นตอน | รายการสาธิต | เวลาที่ใช้ | เครื่องมือที่ใช้ |
| :---: | :--- | :---: | :--- |
| **Stage 1** | ตรวจ Docker Container และ Defects4J สด | 1 นาที | PowerShell / Docker |
| **Stage 2** | เปิด suite และผลประเมินจากทั้ง 4 เทคนิค | 3 นาที | Master CSV / suite / run logs |
| **Stage 3** | อธิบายผล buggy/fixed และนิยาม `BUG_DETECTED` | 1 นาที | Structured run logs |
| **Stage 4** | แสดงกราฟและ workbook จาก snapshot ล่าสุด | 1 นาที | Results / Excel |
| **Stage 5** | เปิดไฟล์ Excel รวม 8 ชีทและ Data Dictionary สรุปผล | 1 นาที | Excel / VS Code |

---

> **Snapshot:** 26 กันยายน 2026. Master มี 3,416 bug–technique rows; suite ที่มีอยู่ประเมินครบ 2,797 คู่ และ 619 คู่เป็น `NO_SUITE`. `available_suite_evaluations_complete: true` หมายถึงไม่มี suite ที่มีอยู่ค้างประเมิน ไม่ได้หมายถึงมี suite ครบทั้ง catalog

## 🚀 ขั้นที่ 1: ตรวจสอบความพร้อมของ Docker Container

ก่อนเริ่ม ต้องเปิด Docker Desktop และรอให้ Docker Engine พร้อม จากนั้นเปิด PowerShell ที่ root ของ repository แล้วตรวจสอบ:

```powershell
docker info
docker ps --filter "name=defects4j_sqa"

# สร้างหรือเปิด service จาก compose หาก container ยังไม่ทำงาน
docker compose -f docker/docker-compose.yml up -d
docker ps --filter "name=defects4j_sqa"
```

ถ้า `docker info` แจ้งว่าเชื่อม Docker Engine ไม่ได้ ให้เปิด Docker Desktop แล้วรอจนคำสั่งนี้ทำงานก่อนเริ่มนำเสนอ

ทดสอบการเรียกใช้งาน Defects4J CLI ภายใน Container:
```powershell
docker exec defects4j_sqa defects4j info -p Math -b 2
```
> **💡 สิ่งที่อาจารย์จะเห็น:** ข้อมูล Defects4J ของ Math-2 และคลาส `HypergeometricDistribution`; ตัวอย่างบั๊กเกี่ยวกับ integer overflow ในการคูณค่าที่ใช้คำนวณ numerical mean ก่อน cast เป็น `double`.

---

## 🚀 ขั้นที่ 2: สาธิต suite และผลประเมินจากทั้งสี่เทคนิค

ตัวอย่างด้านล่างเลือกให้เห็นทั้งกรณีตรวจพบและกรณี coverage สูงแต่ไม่ตรวจพบ ใช้ master row, suite และ run log ที่เก็บจาก benchmark จริง:

| เทคนิค | Bug / สถานะ | Suite ที่เปิดให้ดู | Line / branch coverage | Run ID | Buggy failures / fixed failures |
|---|---|---|---:|---|---:|
| Native IPO | Chart-14 / `BUG_DETECTED` | `Combinatorial_IPO/TestCode/Chart_14b/org/jfree/chart/plot/XYPlot_IPOTest.java` | 4.41% / 1.74% | `Chart-14-ipo-1790351771` | 48 / 0 |
| MIO (EvoSuite) | Jsoup-45 / `NOT_DETECTED` | `MIO_Algorithm/TestCode/Jsoup_45b/HtmlTreeBuilder_ESTest.java` | 93.35% / 86.14% | `Jsoup-45-mio-1790380739` | 0 / 0 |
| DeepSeek V4 Flash | Closure-105 / `BUG_DETECTED` | `Deepseek-v4_flash/TestCode/Closure_105b/FoldConstantsDeepseekTest.java` | 26.49% / 25.44% | `Closure-105-deepseek-1790360926` | 2 / 0 |
| Gemini 3.8 Flash | Chart-3 / `BUG_DETECTED` | `Gemini-3_8_flash/TestCode/Chart_3b/TimeSeriesGeminiTest.java` | 94.74% / 87.63% | `Chart-3-gemini-1790350744` | 2 / 0 |

ตัวอย่าง MIO ช่วยอธิบายว่า coverage สูงไม่ได้รับประกันว่าจะตรวจพบ fault เสมอ หากอาจารย์ถามถึง MIO ที่ตรวจพบจริง ให้เปิด `Jsoup-14` เพิ่มเติม: log บันทึก 31 failures บน buggy, 0 บน fixed แต่ coverage เป็น 0.00% ทั้ง line และ branch จึงต้องแสดงค่านั้นตามจริงและไม่ใช้เป็นตัวอย่าง coverage

อ่าน log ทั้งสี่รายการจากโฟลเดอร์ repository:

```powershell
$cases = @(
  @{ Project='Chart'; Bug_ID='14'; Technique='IPO (Native IPO)'; Run_ID='Chart-14-ipo-1790351771'; Log='results/run_logs/Chart-14-ipo-1790351771.json' },
  @{ Project='Jsoup'; Bug_ID='45'; Technique='MIO (EvoSuite SBST)'; Run_ID='Jsoup-45-mio-1790380739'; Log='results/run_logs/Jsoup-45-mio-1790380739.json' },
  @{ Project='Closure'; Bug_ID='105'; Technique='DeepSeek V4 Flash'; Run_ID='Closure-105-deepseek-1790360926'; Log='results/run_logs/Closure-105-deepseek-1790360926.json' },
  @{ Project='Chart'; Bug_ID='3'; Technique='Gemini 3.8 Flash'; Run_ID='Chart-3-gemini-1790350744'; Log='results/run_logs/Chart-3-gemini-1790350744.json' }
)
$master = Import-Csv results/master_benchmark_summary.csv
$logRows = foreach ($case in $cases) {
  $rows = @($master | Where-Object { $_.Project -eq $case.Project -and $_.Bug_ID -eq $case.Bug_ID -and $_.Technique -eq $case.Technique })
  if ($rows.Count -ne 1) { throw "Expected one master row for $($case.Project)-$($case.Bug_ID)-$($case.Technique)" }
  $row = $rows[0]
  $log = Get-Content -Raw $case.Log | ConvertFrom-Json
  if ($row.Run_ID -ne $case.Run_ID -or $log.run_id -ne $case.Run_ID -or $row.Suite_SHA256 -ne $log.suite_sha256 -or $row.Fault_Detection_Status -ne $log.fault_detection_status) {
    throw "Run ID or suite hash mismatch for $($case.Project)-$($case.Bug_ID)-$($case.Technique)"
  }
  [pscustomobject]@{
    Bug = "$($row.Project)-$($row.Bug_ID)"
    Technique = $log.technique
    MasterStatus = $row.Fault_Detection_Status
    LogStatus = $log.fault_detection_status
    LineCoverage = $log.line_coverage_percent
    BranchCoverage = $log.branch_coverage_percent
    BuggyFailures = $log.buggy_failures.Count
    FixedFailures = $log.fixed_failures.Count
    RunId = $log.run_id
    SuiteSHA256 = $log.suite_sha256
  }
}
$logRows | Format-Table -AutoSize
```

สคริปต์ด้านบนตรวจว่ามี master row เดียว และ Run ID/hash ใน master ตรงกับ log สำหรับตัวอย่างทั้งสี่

---

## 🚀 ขั้นที่ 3: อธิบายเกณฑ์ BUG_DETECTED

BUG_DETECTED นับได้เมื่อชุดทดสอบ fail บน buggy และ pass บน fixed เท่านั้น เปิดผลดิบรายบั๊กใน results/run_logs และผลรวมใน master CSV เพื่อไล่กลับจากตัวเลขไปยังหลักฐาน

ขั้นนี้เปิด run log จริงให้เห็นผลแยก buggy/fixed และชี้ให้เห็นว่า classifier ใช้เกณฑ์ใด `BUG_DETECTED` ต้องมี failure บน buggy และไม่มี failure บน fixed; ตัว classifier เปรียบเทียบสองเวอร์ชัน แต่ไม่ได้พิสูจน์ semantic root cause โดยอัตโนมัติ

---

## 🚀 ขั้นที่ 4: แสดงผลวิเคราะห์จาก snapshot ปัจจุบัน

ระหว่างนำเสนอให้เปิดกราฟและ workbook ที่สร้างจาก snapshot เดียวกัน ไม่ต้องสร้างไฟล์ใหม่สด ๆ เว้นแต่มีการเปลี่ยนผล benchmark; ถ้าต้องทำซ้ำหลังการนำเสนอ ใช้คำสั่งนี้:

    .\.venv\Scripts\python.exe scripts/reclassify_fault_detection.py --apply
    .\.venv\Scripts\python.exe scripts/consolidate_master_results.py
    .\.venv\Scripts\python.exe scripts/audit_suite_gaps.py
    .\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
    .\.venv\Scripts\python.exe scripts/plot_results.py

ตรวจ `results/master_descriptive_stats.json` ก่อนพูดถึงผล ปัจจุบัน `results_complete` และ `available_suite_evaluations_complete` เป็น true; หากมีการเปลี่ยนข้อมูลภายหลัง ให้ใช้สถานะและตัวหารล่าสุดในไฟล์นี้

---

## 🚀 ขั้นที่ 5: ตรวจไฟล์ส่งมอบ

- กราฟ: `figure1_coverage_comparison.png` ถึง `figure6_ensemble_overlap.png` ใน `results/`
- Excel: results/Master_Benchmark_Results.xlsx มี Summary, Master evaluations, MIO generation budget, Hypothesis tests, AI generation logs, Ensemble, Single vs multiclass และ Data dictionary
- Data dictionary และรายงาน snapshot: results/DATA_DICTIONARY.md และ results/advanced_analytics_report.md
- README, DOCX/PDF report, PRESENTATION_SLIDES และ DEMO_GUIDE ต้องตรงกับ snapshot: 3,416 แถว, 2,797 suite evaluations, 619 `NO_SUITE`, unresolved outcomes 0
- ยืนยันก่อนนำเสนอว่า Run ID, suite hash และ log ตรงกันทั้งสี่ตัวอย่างใน Stage 2
- workbook ปัจจุบันมี 8 sheets; เปิด Summary และ Data dictionary ประกอบการอธิบาย

หากผลประเมินยังไม่ครบ ให้รายงานจำนวน suite, จำนวน attempted, จำนวน DONE, จำนวน BUG_DETECTED และจำนวน NO_SUITE แยกกัน ห้ามนำผลเก่ามาแทนค่าที่หายไป

**ขอบเขตของเดโม:** flow นี้สาธิต environment สดและ suite/log จริงที่ได้จากการรัน benchmark แล้ว ไม่ได้สร้าง suite ใหม่ต่อหน้า หากต้องการสาธิตการสร้าง IPO, MIO หรือเรียก AI API สด ต้องซ้อมแยก ตรวจ quota/dependencies และกำหนดเวลาสำรองก่อนวันนำเสนอ

---
