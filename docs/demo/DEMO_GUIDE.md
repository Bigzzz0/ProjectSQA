# คู่มือสาธิตระบบประเมินชุดทดสอบ (Live Demo)

**วิชา:** CP353201 การประกันคุณภาพซอฟต์แวร์

**จัดทำโดย:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)

## เดโมนี้แสดงอะไร

เดโมนี้รัน **suite ที่สร้างไว้แล้ว** ของ Native IPO, MIO/EvoSuite, DeepSeek และ Gemini ผ่าน benchmark runner จริงทีละชุด โดยรัน test กับ Defects4J เวอร์ชัน buggy และ fixed แล้วแสดง coverage, จำนวน test ที่ fail, สถานะ fault detection, run ID และ suite hash ใน terminal

จึงเป็นเดโมการ **ประเมิน suite แบบสด** และการตรวจว่า suite แยกเวอร์ชัน buggy/fixed ได้หรือไม่ ไม่ใช่การสร้าง suite ใหม่หรือเรียก AI API ต่อหน้า การสร้าง suite เป็นขั้นตอนก่อนหน้าและไฟล์ที่ใช้จะระบุไว้ในตารางด้านล่าง

เดโมสี่กรณีใช้เวลารันจริงประมาณ 1–2 นาทีบนเครื่องที่ทดสอบ; เผื่อเวลาอธิบายและตรวจ output รวมประมาณ 5 นาที

## เตรียมเครื่อง

1. เปิด Docker Desktop และรอให้ Docker Engine พร้อม
2. เปิด PowerShell ที่โฟลเดอร์ root ของ repository
3. เริ่ม container หากยังไม่ทำงาน:

```powershell
docker compose -f docker/docker-compose.yml up -d
docker exec defects4j_sqa defects4j info -p Math -b 2
```

คำสั่งที่สองควรแสดงข้อมูล Defects4J ของ Math-2 หากไม่สำเร็จ ให้แก้สถานะ Docker/container ก่อนเริ่มเดโม

## รันเดโมสดทั้ง 4 เทคนิค

รันสคริปต์นี้จาก root ของ repository:

```powershell
.\scripts\demo_four_techniques.ps1
```

สคริปต์ประเมินหนึ่ง suite ต่อเทคนิคตามลำดับนี้:

| เทคนิค | ตัวอย่าง | Suite ที่ runner ใช้ | ผลที่คาดจากการทดสอบรอบยืนยัน |
|---|---|---|---|
| Native IPO | Chart-14 | `Combinatorial_IPO/TestCode/Chart_14b/org/jfree/chart/plot/XYPlot_IPOTest.java` | line 4.41%, branch 1.74%; buggy fail 48, fixed fail 0; `BUG_DETECTED` |
| MIO / EvoSuite | Jsoup-45 | `MIO_Algorithm/TestCode/Jsoup_45b/HtmlTreeBuilder_ESTest.java` | line 93.35%, branch 86.14%; buggy fail 0, fixed fail 0; `NOT_DETECTED` |
| DeepSeek V4 Flash | Closure-105 | `Deepseek-v4_flash/TestCode/Closure_105b/FoldConstantsDeepseekTest.java` | line 26.49%, branch 25.44%; buggy fail 2, fixed fail 0; `BUG_DETECTED` |
| Gemini 3.8 Flash | Chart-3 | `Gemini-3_8_flash/TestCode/Chart_3b/TimeSeriesGeminiTest.java` | line 94.74%, branch 87.63%; buggy fail 2, fixed fail 0; `BUG_DETECTED` |

ตัวเลขในตารางเป็นผลที่คาดจากการรันยืนยันก่อนหน้า การรันสดจะสร้าง run ID และเวลาใหม่ ให้ใช้ค่าที่สคริปต์พิมพ์ออกมาบนเวที หากผลต่างจากตาราง ให้ยึด output สดและเก็บ log ไว้ตรวจสอบ ไม่แก้ตัวเลขให้ตรงตาราง

### สิ่งที่สคริปต์ทำเพื่อรักษาผลเดิม

- ส่ง CSV ของเดโมไปยังไฟล์ชั่วคราวใน container ไม่เขียนทับ master CSV
- สำรองแล้วคืน `progress.json` และไฟล์ผลรายบั๊กของสี่กรณีหลังจบ
- ลบ CSV ชั่วคราวและไฟล์สำรองที่สคริปต์สร้าง
- ใช้ suite ใน repository ที่มีอยู่แล้ว ไม่สร้างหรือแก้ suite

หากสคริปต์แจ้งว่า Docker Engine หรือ container ไม่พร้อม ให้เริ่ม Docker/compose ตามขั้นเตรียมเครื่องแล้วลองใหม่ หาก checkout, compile หรือ test ล้มเหลว ให้แสดงสถานะและ error จริง ห้ามกล่าวว่าเป็น `BUG_DETECTED` เว้นแต่ test fail บน buggy และผ่านบน fixed

## ประเด็นที่ควรอธิบายขณะสาธิต

1. runner ใช้ suite เดียวกันทดสอบทั้ง buggy และ fixed version
2. `BUG_DETECTED` หมายถึงมี test fail บน buggy และไม่มี test fail บน fixed
3. MIO/Jsoup-45 แสดงว่า coverage สูงอย่างเดียวไม่ได้แปลว่าจะตรวจพบ bug: กรณีนี้ coverage สูงแต่ test ไม่ fail บน buggy
4. ผลที่รายงานเป็นการประเมิน suite ที่สร้างไว้ก่อน ไม่ใช่การเทียบเวลาหรือคุณภาพของขั้นตอน generation แบบสด
5. ผลรวมที่นำไปอ้างอิงอยู่ใน master dataset; สี่กรณีนี้เป็นตัวอย่างสาธิต ไม่ใช่ตัวแทนผลครบทุก 854 บั๊ก

กรณี MIO ที่ตรวจพบ bug (`Jsoup-14`) ใช้เป็นตัวอย่างเสริมได้: ผลที่บันทึกไว้มี buggy failures 31, fixed failures 0 แต่ coverage เป็น 0.00% ทั้ง line และ branch จึงควรอธิบายข้อจำกัดและไม่ใช้เป็นตัวอย่าง coverage

## ผลรวม benchmark ใน snapshot ของ repository

Snapshot ที่คู่มือปรับปรุง: **26 กันยายน 2026**. Master dataset มี 3,416 bug–technique rows จาก 854 บั๊ก × 4 เทคนิค; 2,797 คู่มี suite ที่ประเมินแล้ว และ 619 คู่เป็น `NO_SUITE`. `available_suite_evaluations_complete: true` หมายถึง suite ที่มีอยู่ใน snapshot ได้รับการประเมินครบ ไม่ได้หมายถึงมี suite ครบทุกคู่ใน catalog

ก่อนนำเสนอตัวเลขรวม ให้ตรวจ `results/master_descriptive_stats.json` และ `results/master_benchmark_summary.csv` ว่าข้อมูลไม่เปลี่ยนจาก snapshot นี้ หากมีการรันหรืออัปเดตข้อมูลใหม่ ให้ใช้ตัวเลขล่าสุดจากไฟล์ผลแทนตัวเลขในคู่มือ

## ไฟล์ประกอบ

- กราฟผล: `results/figure1_coverage_comparison.png` ถึง `results/figure6_ensemble_overlap.png`
- Workbook: `results/Master_Benchmark_Results.xlsx` (8 ชีท)
- นิยามฟิลด์: `results/DATA_DICTIONARY.md`
- รายงานวิเคราะห์: `results/advanced_analytics_report.md`
- ผลรวมรายบั๊ก: `results/master_benchmark_summary.csv`
- สถิติรวม: `results/master_descriptive_stats.json`
- ผลดิบต่อรอบ: `results/run_logs/`

## ทำซ้ำการสร้างผลสรุป

เมื่อมีการเปลี่ยนผล benchmark และต้องสร้างผลรวมใหม่ ให้รันตามลำดับ:

```powershell
.\.venv\Scripts\python.exe scripts/reclassify_fault_detection.py --apply
.\.venv\Scripts\python.exe scripts/consolidate_master_results.py
.\.venv\Scripts\python.exe scripts/audit_suite_gaps.py
.\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
.\.venv\Scripts\python.exe scripts/plot_results.py
```

ตรวจจำนวนบั๊ก ตัวหาร สถานะ `NO_SUITE` และผล `BUG_DETECTED` ในข้อมูลที่สร้างใหม่ก่อนแก้รายงานหรือสไลด์ เพื่อให้ทุกชิ้นใช้ snapshot เดียวกัน
