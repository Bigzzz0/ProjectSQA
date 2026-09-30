# คู่มือการทำงานของทีม ProjectSQA

เอกสารนี้สรุปสถานะและขั้นตอนทำงานจาก repository ณ วันที่ 30 กันยายน 2026 ใช้ร่วมกับผล benchmark snapshot วันที่ 26 กันยายน 2026 เอกสารแผนเก่าของแต่ละสายงานเก็บไว้เป็นประวัติ ไม่ใช่คำสั่งปัจจุบัน

## สถานะโครงการปัจจุบัน

- ขอบเขต Defects4J: 854 บั๊กใน 17 โปรเจกต์ และ 1,073 รายการ `classes.modified` (577 ชื่อคลาสไม่ซ้ำ)
- Master dataset: 3,416 คีย์ไม่ซ้ำ (`project + bug_id + technique`)
- มี suite และผลประเมินครบ 2,797 คู่; 619 คู่เป็น `NO_SUITE`
- ผลประเมิน: 160 `BUG_DETECTED`, 711 `NOT_DETECTED`, 794 `FLAKY_OR_REGRESSION`, 1,132 `COMPILE_ERROR`; ไม่มีผลค้างใน snapshot
- `results_complete: true` หมายถึงประเมิน suite ที่มีอยู่ครบ ไม่ได้หมายความว่าทุกบั๊กมี suite ครบทุกเทคนิค

ตัวเลขต้นทางและรายละเอียดแยกเทคนิคอยู่ใน [master_descriptive_stats.json](results/master_descriptive_stats.json), [master_benchmark_summary.csv](results/master_benchmark_summary.csv) และ [DATA_DICTIONARY.md](results/DATA_DICTIONARY.md)

## บทบาทสมาชิก

| สมาชิก | ความรับผิดชอบ | หลักฐานและไฟล์หลัก |
|---|---|---|
| Member 1 — นายปวริศช์ ประมวล | Native IPO และการยืนยัน suite บน fixed version | `Combinatorial_IPO/Results/verified_suites_manifest.json`, `Combinatorial_IPO/TestCode/` |
| Member 2 — นายแทนคุณ พันธ์นิกุล | MIO ผ่าน EvoSuite และการวิเคราะห์ generation budget/ข้อจำกัด | `MIO_Algorithm/TestCode/`, `MIO_Algorithm/Result_Round2/`, `MIO_Algorithm/MIO_FAILURE_ANALYSIS_REPORT.md` |
| Member 3 — นายธนภูมิ จันทรา | การสร้าง suite ด้วย DeepSeek และ Gemini พร้อม prompt/configuration/log | `Deepseek-v4_flash/`, `Gemini-3_8_flash/`, `results/DELIVERY_REPORT_M3.md` |
| Member 4 — นายศิฆรินทร์ อุปจันทร์ | Docker/runner, การตรวจ provenance, รวมผลและรายงาน | `scripts/`, `results/`, `Final_Report.md`, `DEMO_GUIDE.md` |

## กติกาการอ่านผล

- ผลหลักใช้หนึ่งแถวต่อบั๊กและเทคนิค; การประเมินหลาย target classes ภายในบั๊กเดียวรวมเป็นผลระดับบั๊ก
- `BUG_DETECTED` ต้องมี failure บน buggy และไม่มี failure บน fixed; ตัว runner เปรียบเทียบผลระหว่างสองเวอร์ชัน ไม่ได้ยืนยัน root cause ทางความหมายแยกต่างหาก
- `NOT_DETECTED` คือไม่มี failure บนทั้งสองเวอร์ชัน; `FLAKY_OR_REGRESSION` คือพบ failure บน fixed; `COMPILE_ERROR` คือ suite คอมไพล์ไม่ได้; `NO_SUITE` คือไม่มี suite ที่รับเข้า benchmark
- FDR หลัก = จำนวนบั๊ก `BUG_DETECTED` ÷ จำนวน bug–technique ที่มี suite และถูกประเมิน โดยรวม compile error และ flaky/regression ในตัวหาร; แสดงอัตราเทียบ 854 บั๊กแยกต่างหาก
- Coverage ใช้ค่าที่วัดได้จาก target classes เท่านั้น; compile error หรือค่าที่วัดไม่ได้เป็นค่าว่าง ไม่ใช่ 0%
- ตัวเลข MIO budget และ AI token/time เป็นผล generation แยกจาก benchmark coverage/FDR เว้นแต่มี run ID เชื่อมโยงได้

## ขั้นตอนทำซ้ำผล benchmark

### 1. เริ่ม Docker

รันจาก root ของ repository บน Windows หรือ Linux ที่ติดตั้ง Docker:

```powershell
docker compose -f docker/docker-compose.yml up -d --build
docker ps --filter "name=defects4j_sqa"
docker exec defects4j_sqa defects4j info -p Math -b 2
```

### 2. รัน/ทำต่อ benchmark

รันทดสอบตัวอย่างหนึ่งบั๊ก:

```powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1
```

ทำต่อคิวทุกบั๊กด้วย suite ที่ runner ค้นพบ:

```powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --all-bugs --resume
```

อย่าใช้สถานะ `available_suite_evaluations_complete` แทนการมี suite ครบทั้ง catalog; ตรวจจำนวน `NO_SUITE` ควบคู่เสมอ

### 3. สร้างสรุปใหม่หลังผลรันเปลี่ยน

รันบน host จาก root repository หลังคิว benchmark หยุดหรือเสร็จแล้ว:

```powershell
.\.venv\Scripts\python.exe scripts/reclassify_fault_detection.py --apply
.\.venv\Scripts\python.exe scripts/consolidate_master_results.py
.\.venv\Scripts\python.exe scripts/audit_suite_gaps.py
.\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
.\.venv\Scripts\python.exe scripts/plot_results.py
```

ตรวจว่า master มี 3,416 คีย์ไม่ซ้ำ, `unresolved_rows` เป็น 0 และตัวเลข CSV/JSON/Excel/กราฟ/รายงานตรงกัน จากนั้นอัปเดต DOCX/PDF แยกตามคู่มือรายงานและ render ตรวจหน้าก่อนส่ง

## ไฟล์นำเสนอและสาธิต

- [PRESENTATION_SLIDES.md](PRESENTATION_SLIDES.md) เป็นเนื้อหาสไลด์และแหล่งตัวเลข
- [DEMO_GUIDE.md](DEMO_GUIDE.md) เป็นลำดับสาธิตหลักฐาน suite, log, buggy/fixed และ workbook
- [Final_Report.md](Final_Report.md) เป็นรายงานเนื้อหา; ไฟล์ DOCX/PDF ฉบับส่งอยู่ที่ `SQA_Final_Report.docx` และ `SQA_Final_Report.pdf`

ใช้ค่า snapshot เดียวกันในเอกสารทั้งหมด หาก benchmark ถูกเปลี่ยน ต้องคำนวณและตรวจเอกสารที่อ้างตัวเลขใหม่ก่อนนำเสนอ

## เอกสารอ้างอิงที่เป็นแผนเก่า

`MIO_Algorithm/EXECUTION_PLAN.md`, `NEXT_STEPS_PLAN.md`, `MIO_RUN_CHEATSHEET.md`, `manual_member2_mio_defects4j.md`, `workflow_การทำงาน.md` และ IPO handoff/results reports เป็นบันทึกแผนหรือสถานะตามวันที่ในเอกสารนั้น ๆ ไม่ใช่คำสั่งทำงานล่าสุด ให้ยึด README, master data, manifest และรายงาน failure/budget ที่ลิงก์ไว้ข้างต้น
