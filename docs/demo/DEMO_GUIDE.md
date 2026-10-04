# คู่มือสาธิตระบบประเมินชุดทดสอบ (Live Demo)

**วิชา:** CP353201 การประกันคุณภาพซอฟต์แวร์

**จัดทำโดย:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)

## เดโมนี้แสดงอะไร

เดโมนี้ใช้ **Jsoup-40 และคลาส `org.jsoup.nodes.DocumentType` เดียวกันทั้งสี่เทคนิค** รัน **suite ที่สร้างไว้แล้ว** ของ Native IPO, MIO/EvoSuite, DeepSeek และ Gemini ผ่าน benchmark runner จริงทีละชุด โดยรัน test กับ Defects4J เวอร์ชัน buggy และ fixed แล้วแสดง coverage, จำนวน test ที่ fail, สถานะ fault detection, run ID และ suite hash ใน terminal

จึงเป็นเดโมการ **ประเมิน suite แบบสด** และการตรวจว่า suite แยกเวอร์ชัน buggy/fixed ได้หรือไม่ ไม่ใช่การสร้าง suite ใหม่หรือเรียก AI API ต่อหน้า การสร้าง suite เป็นขั้นตอนก่อนหน้าและไฟล์ที่ใช้จะระบุไว้ในตารางด้านล่าง

คู่มือเดิมบันทึกเวลารันสี่กรณีประมาณ 1–2 นาทีบนเครื่องที่ทดสอบ; ต้องซ้อมจับเวลาบนเครื่องนำเสนออีกครั้ง การอธิบายเต็มใช้ประมาณ 5 นาที ส่วน [สไลด์นำเสนอ 10 นาที](../presentation/PRESENTATION_SLIDES.md) จัดช่วงเดโมแบบย่อไว้ **2:30 นาที** โดยเริ่มที่ 6:20 และกลับหน้าสรุปที่ 8:50

## คิวเดโมสำหรับการนำเสนอ 10 นาที

| เวลาในช่วงเดโม | สิ่งที่แสดง |
|---|---|
| 0:00–0:15 | เริ่มสคริปต์และบอกว่าใช้ suite ที่สร้างไว้แล้วมาประเมินใหม่ |
| 0:15–1:40 | ระหว่างรันชี้ suite และการใช้ buggy/fixed; เจ้าของเทคนิคอธิบายสั้น ๆ ตาม output |
| 1:40–2:15 | ชี้ผลทั้งสี่เทคนิค: BUG_DETECTED ต้อง buggy fail / fixed pass และตัวอย่าง MIO ที่ coverage สูงแต่ NOT_DETECTED |
| 2:15–2:30 | ชี้ run ID/hash หนึ่งกรณี แล้วกลับหน้าสรุป |

เปิด Docker และเตรียมหน้าต่างก่อนเริ่มนำเสนอ หากถึง 2:00 นาทีของเดโมแล้วยังรันไม่ครบหรือเกิด error ให้เปิดหลักฐานรอบก่อนที่เตรียมไว้และบอกว่าเป็นผลที่บันทึกไว้ ไม่ใช้ผลสำรองกล่าวอ้างว่า live run สำเร็จ ไม่ปิด terminal กลางคัน ให้สคริปต์ทำงานและคืนไฟล์เดิมจนจบ

### เตรียมผลสำรองก่อนเริ่มเดโม

เปิด PowerShell อีกหน้าต่างที่ root ของ repository แล้วรันคำสั่งต่อไปนี้ **ก่อนเริ่มสคริปต์สด** เพื่อเก็บผลเดิมไว้ในหน่วยความจำของหน้าต่างสำรอง ไม่อ่านไฟล์ที่ live runner อาจกำลังเขียนอยู่:

```powershell
$demoSavedPaths = @(
    'results/Jsoup/40/ipo.json',
    'results/Jsoup/40/mio.json',
    'results/Jsoup/40/deepseek.json',
    'results/Jsoup/40/gemini.json'
)
$demoSavedResults = foreach ($path in $demoSavedPaths) {
    $r = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
    [pscustomobject]@{
        Case = $path
        Status = $r.fault_detected
        BuggyFail = @($r.buggy_failures).Count
        FixedFail = @($r.fixed_failures).Count
        Line = $r.line_cov
        Branch = $r.branch_cov
        Suite = $r.test_file
        RunID = $r.run_id
        Hash = $r.suite_sha256
    }
}
Write-Host 'SAVED RESULTS FROM AN EARLIER RUN — NOT LIVE OUTPUT'
$demoSavedResults | Format-Table Case, Status, BuggyFail, FixedFail, Line, Branch -AutoSize
$demoSavedResults[0] | Format-List Suite, RunID, Hash
```

เตรียมเปิด source, prompt และ log ของตัวอย่างไว้ด้วย เพื่ออธิบายหลักฐานได้แม้การรันสดติดขัด ห้ามเปลี่ยนผลสำรองให้ตรงกับค่าที่คาดหวัง

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

หาก PowerShell แจ้งว่า running scripts is disabled ให้รันเฉพาะครั้งนี้ด้วย:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\demo_four_techniques.ps1"
```

เมื่อจบ จะมีตาราง **LIVE DEMO SUMMARY** แสดง Line/Branch coverage, จำนวน test fail บน buggy/fixed และสถานะผล โดยสีเขียวคือ `BUG_DETECTED` สีเหลืองคือ `NOT_DETECTED` และสีแดงคือผลผิดพลาดหรือสถานะอื่น

เครื่องที่ clone ใหม่ไม่ต้องสร้างหรือคัดลอก `progress.json` เพราะเป็นไฟล์สถานะเฉพาะเครื่อง สคริปต์รองรับกรณีไม่มีไฟล์นี้หรือไม่มีผล JSON เดิมแล้ว หลังเดโมจะคืนไฟล์ที่มีอยู่ก่อนรัน และนำไฟล์สถานะ/ผลชั่วคราวที่เกิดใหม่ออก โดยเก็บหลักฐานรอบเดโมไว้ใน `.local/demo/<session>/`

ระหว่างรัน แต่ละกรณีแสดงขั้นตอนใน terminal แบบสด: **[1/6] target classes → [2/6] checkout buggy และ suite → [3/6] coverage → [4/6] buggy test → [5/6] fixed test → [6/6] ผลลัพธ์** พร้อมจำนวน test fail, เวลาที่ใช้, suite SHA-256 และ run ID เมื่อเสร็จ หากต้องการรายละเอียด runner ทั้งหมด ให้เพิ่ม `-ShowRunnerOutput` ท้ายคำสั่ง

คอลัมน์ Buggy/Fixed ในตารางคือ **จำนวน test ที่ fail**; `BUG_DETECTED` ต้องมี test fail บน buggy และผ่านบน fixed ค่า coverage ที่วัดไม่ได้แสดง `N/A` ตัวอย่างทั้งสี่ใช้บั๊กและคลาสเดียวกัน จึงเปรียบเทียบผลของ suite ในกรณีนี้ได้ แต่ไม่ใช้จัดอันดับเทคนิคโดยรวม

ในขั้นตอน buggy/fixed จะแสดงผล **JUnit PASS/FAIL**, ข้อความสรุปจาก Defects4J และชื่อ test ที่ fail พร้อมสาเหตุจาก `failing_tests` จริง โดยแสดงไม่เกิน 3 รายการต่อเวอร์ชันเพื่อให้อ่านบนจอได้ง่าย ผลดิบ stdout/stderr และ failure traces ทั้งหมดเก็บใน `.local/demo/<session>/junit/<project-bug-technique>/` ไม่ประมาณจำนวน test ที่ผ่านหรือจำนวน test ทั้งหมดจากจำนวน fail

ผลทั้งหมดแสดงใน terminal โดยไม่มี HTML หลักฐานแยกแต่ละรอบอยู่ใน `.local/demo/<session>/`: `results.json` บันทึกหลังแต่ละกรณี, `results.csv` เมื่อรันครบ และ `run.log` เก็บในเครื่องโดยไม่ติดตามใน Git หากหยุดก่อนครบ JSON มีเฉพาะกรณีที่บันทึกแล้ว ส่วนขั้นตอนที่ค้างตรวจได้จาก log

สคริปต์ประเมินหนึ่ง suite ต่อเทคนิคตามลำดับนี้:

| เทคนิค | ตัวอย่าง | Suite ที่ runner ใช้ | ผลที่คาดจากการทดสอบรอบยืนยัน |
|---|---|---|---|
| Native IPO | Jsoup-40 | `Combinatorial_IPO/TestCode/Jsoup_40b/org/jsoup/nodes/DocumentType_IPOTest.java` | line 41.18%, branch 0.00%; buggy fail 18, fixed fail 0; `BUG_DETECTED` |
| MIO / EvoSuite | Jsoup-40 | `MIO_Algorithm/TestCode/Jsoup_40b/DocumentType_ESTest.java` | line 88.24%, branch 66.67%; buggy fail 0, fixed fail 0; `NOT_DETECTED` |
| DeepSeek V4 Flash | Jsoup-40 | `Deepseek-v4_flash/TestCode/Jsoup_40b/DocumentTypeDeepseekTest.java` | line 100.00%, branch 100.00%; buggy fail 6, fixed fail 5; `FIXED_FAILED` |
| Gemini 3.8 Flash | Jsoup-40 | `Gemini-3_8_flash/TestCode/Jsoup_40b/DocumentTypeGeminiTest.java` | line 100.00%, branch 100.00%; buggy fail 2, fixed fail 0; `BUG_DETECTED` |

ตัวเลขในตารางเป็นผลที่คาดจากการรันยืนยันก่อนหน้า การรันสดจะสร้าง run ID และเวลาใหม่ ให้ใช้ค่าที่สคริปต์พิมพ์ออกมาบนเวที หากผลต่างจากตาราง ให้ยึด output สดและเก็บ log ไว้ตรวจสอบ ไม่แก้ตัวเลขให้ตรงตาราง

เดโมแสดงชื่อ `FIXED_FAILED` เมื่อยังมี test fail บน fixed โดยไม่สรุปว่าเป็น flaky หรือ regression ส่วนข้อมูลดิบ JSON/CSV ของ benchmark ยังคงชื่อเดิม `FLAKY_OR_REGRESSION` เพื่อให้เทียบกับข้อมูลและรายงานเดิมได้ ทั้งสองชื่ออ้างถึงกลุ่มผลเดียวกัน

### สิ่งที่สคริปต์ทำเพื่อรักษาผลเดิม

- ส่ง CSV ของเดโมไปยังไฟล์ชั่วคราวใน container ไม่เขียนทับ master CSV
- สำรองแล้วคืน `progress.json` และไฟล์ผลรายบั๊กของสี่กรณีหลังจบ
- ลบ CSV ชั่วคราวและไฟล์สำรองที่สคริปต์สร้าง
- ใช้ suite ใน repository ที่มีอยู่แล้ว ไม่สร้างหรือแก้ suite

หากสคริปต์แจ้งว่า Docker Engine หรือ container ไม่พร้อม ให้เริ่ม Docker/compose ตามขั้นเตรียมเครื่องแล้วลองใหม่ หาก checkout, compile หรือ test ล้มเหลว ให้แสดงสถานะและ error จริง ห้ามกล่าวว่าเป็น `BUG_DETECTED` เว้นแต่ test fail บน buggy และผ่านบน fixed

## ประเด็นที่ควรอธิบายขณะสาธิต

1. runner ใช้ suite เดียวกันทดสอบทั้ง buggy และ fixed version
2. `BUG_DETECTED` หมายถึงมี test fail บน buggy และไม่มี test fail บน fixed
3. MIO/Jsoup-40 แสดงว่า coverage สูงอย่างเดียวไม่ได้แปลว่าจะตรวจพบ bug: กรณีนี้ coverage สูงแต่ test ไม่ fail บน buggy
4. ผลที่รายงานเป็นการประเมิน suite ที่สร้างไว้ก่อน ไม่ใช่การเทียบเวลาหรือคุณภาพของขั้นตอน generation แบบสด
5. DeepSeek/Jsoup-40 มี test fail บน fixed ด้วย จึงไม่นับเป็นตรวจพบบั๊กตามเกณฑ์นี้ แม้ coverage 100%; สาเหตุต้องดู assertions และ log ไม่สรุปว่าเป็น flaky แน่นอนจากการรันครั้งเดียว
6. ผลรวมที่นำไปอ้างอิงอยู่ใน master dataset; สี่กรณีนี้เป็นตัวอย่างสาธิต ไม่ใช่ตัวแทนผลครบทุก 854 บั๊ก

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
