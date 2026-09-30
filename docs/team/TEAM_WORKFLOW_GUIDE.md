# คู่มือปฏิบัติงานของทีม ProjectSQA

**รายวิชา:** CP353201 Software Quality Assurance
**โครงงาน:** การเปรียบเทียบอัลกอริทึมสร้างชุดทดสอบกับ AI-assisted test generation บน Defects4J
**ผู้สอน:** ผศ.ดร.ชิตสุธา สุ่มเล็ก
**สถานะเอกสาร:** ตรวจเมื่อ 30 กันยายน 2569; อ้างอิง benchmark snapshot วันที่ 26 กันยายน 2569

คู่มือนี้เป็นแนวทางร่วมของสมาชิกทุกคน ตั้งแต่ตรวจ Defects4J target ส่งมอบ suite รัน benchmark วิเคราะห์ข้อมูล และเตรียมรายงาน/เดโม รายละเอียดเฉพาะเทคนิคอยู่ใน README ของแต่ละสายงาน

ลิงก์ในคู่มือนี้ชี้ไปยังไฟล์ใน repository; คำสั่ง shell ให้รันจากโฟลเดอร์ root ของ repository เว้นแต่ระบุไว้เป็นอย่างอื่น

> ตัวเลขและสถานะในเอกสารนี้ผูกกับ snapshot วันที่ 26 กันยายน 2569 หากมีการเพิ่มหรือแก้ suite ต้องประเมินใหม่และสร้างผลสรุปทั้งหมดจาก snapshot เดียวกันก่อนอ้างอิง

## สารบัญ

1. ภาพรวมงานและขอบเขต
2. สถานะ benchmark ล่าสุด
3. บทบาทและสิ่งส่งมอบ
4. แหล่งข้อมูลหลัก
5. ข้อตกลงกลางสำหรับ suite
6. เตรียม environment และตรวจ target
7. ขั้นตอนของแต่ละสมาชิก
8. รัน benchmark และอัปเดตผล
9. ตีความสถานะผล
10. ตรวจรายงานและเตรียมเดโม
11. Checklist ก่อนส่ง
12. เอกสารย้อนหลัง

## 1. ภาพรวมงานและขอบเขต

เปรียบเทียบ 4 เทคนิคบน Java projects ใน Defects4J ทั้ง 17 โปรเจกต์ รวม **854 บั๊ก**: Native IPO, MIO ผ่าน EvoSuite, DeepSeek V4 Flash และ Gemini 3.8 Flash

การสร้างและวัด coverage มุ่งที่คลาสในฟิลด์ classes.modified ของบั๊ก ไม่ใช่ทุกคลาสในโปรเจกต์ คลาสชื่อเดียวกันในบั๊กคนละรายการต้องประเมินแยก เพราะเป็นคนละ revision และจุดบกพร่องอาจต่างกัน

| โปรเจกต์ | บั๊ก | โปรเจกต์ | บั๊ก | โปรเจกต์ | บั๊ก |
|---|---:|---|---:|---|---:|
| Chart | 26 | Cli | 39 | Closure | 174 |
| Codec | 18 | Collections | 28 | Compress | 47 |
| Csv | 16 | Gson | 18 | JacksonCore | 26 |
| JacksonDatabind | 110 | JacksonXml | 6 | Jsoup | 93 |
| JxPath | 22 | Lang | 61 | Math | 106 |
| Mockito | 38 | Time | 26 | **รวม** | **854** |

Defects4J catalog มี **1,073 target-class entries** หรือ **577 ชื่อคลาสไม่ซ้ำ**. IPO inventory มี 1,070 class records; ความต่าง 3 รายการยังไม่ reconcile ให้แยกยอดตามแหล่งข้อมูล และอย่าอ้างว่า inventory ของ IPO ครอบคลุม catalog ทั้งหมด

## 2. สถานะ benchmark ล่าสุด

Master dataset มีหนึ่งแถวต่อคีย์ **project + bug_id + technique** รวม 3,416 คีย์จาก 854 บั๊ก × 4 เทคนิค

| เทคนิค | มี suite | วัด coverage ได้ | Compile error | ตรวจพบบั๊ก | FDR ต่อ suite | Line / branch |
|---|---:|---:|---:|---:|---:|---:|
| Native IPO | 257 | 252 | 5 | 37 | 14.40% | 26.76% / 18.67% |
| MIO / EvoSuite | 834 | 797 | 37 | 5 | 0.60% | 63.85% / 56.51% |
| DeepSeek V4 Flash | 853 | 192 | 661 | 11 | 1.29% | 78.02% / 70.22% |
| Gemini 3.8 Flash | 853 | 424 | 429 | 107 | 12.54% | 86.29% / 79.54% |
| **รวม** | **2,797** | **1,665** | **1,132** | **160** | — | — |

อีก **619 คู่เป็น NO_SUITE**: IPO 597, MIO 20, DeepSeek 1 และ Gemini 1. ใน 2,797 คู่ที่มี suite มี 711 NOT_DETECTED และ 794 FLAKY_OR_REGRESSION; snapshot ไม่มี TIMEOUT หรือผลค้าง

FDR ในตารางคำนวณจากจำนวน BUG_DETECTED หารด้วยจำนวนคู่บั๊ก–เทคนิคที่มี suite และถูกประเมิน โดยรวม compile error และ flaky/regression ในตัวหาร ส่วน NO_SUITE ไม่มี suite ให้ประเมินจึงอยู่นอกตัวหารนี้ หากรายงาน FDR เทียบ catalog 854 บั๊ก ให้แสดงตัวหารนั้นแยกต่างหาก

คำว่า results_complete หมายถึงประเมิน suite ที่มีอยู่ใน snapshot ครบ ไม่ได้หมายความว่ามี suite ครบทุกคู่

## 3. บทบาทและสิ่งส่งมอบ

| สมาชิก | หน้าที่ | สิ่งส่งมอบหลัก |
|---|---|---|
| **Member 1 — นายปวริศช์ ประมวล (673380278-9)** | Native IPO / combinatorial testing | โค้ดและ configuration, pairwise models, suite ที่ผ่าน fixed-version verification, manifest และ logs |
| **Member 2 — นายแทนคุณ พันธ์นิกุล (673380301-0)** | MIO ผ่าน EvoSuite | Suite/scaffolding, budget/seed configuration, generation results และรายงานข้อจำกัด |
| **Member 3 — นายธนภูมิ จันทรา (673380272-1)** | DeepSeek และ Gemini | Prompt/configuration, test suites, model/generation logs, token/time records และ mapping manifest |
| **Member 4 — นายศิฆรินทร์ อุปจันทร์ (673380292-5)** | Environment, benchmark, analytics และเอกสาร | Docker/Defects4J, runner/logs, master data, audit, analytics, workbook/กราฟ และเอกสารรวม |

เจ้าของเทคนิคยืนยันคุณภาพและหลักฐาน generation ของตนเอง Member 4 ตรวจรับและประเมิน suite ด้วย runner กลาง การที่เทคนิคยังไม่มี suite หรือสร้าง suite ไม่สำเร็จต้องรายงานตามหลักฐานจริง ห้ามแทนด้วยผลสมมติ

## 4. แหล่งข้อมูลหลัก

| ต้องการตรวจ | แหล่งอ้างอิง |
|---|---|
| ผลระดับ bug–technique | [master_benchmark_summary.csv](../../results/master_benchmark_summary.csv) |
| สถิติและจำนวนสถานะ | [master_descriptive_stats.json](../../results/master_descriptive_stats.json) |
| Suite และ hash | [suite_inventory.csv](../../results/suite_inventory.csv) |
| ช่อง NO_SUITE และสาเหตุ audit | [suite_gap_audit.csv](../../results/suite_gap_audit.csv) |
| ความหมาย field/ตัวหาร | [DATA_DICTIONARY.md](../../results/DATA_DICTIONARY.md) |
| ผลดิบและ run records | [benchmark_results.csv](../../results/benchmark_results.csv), [results/](../../results/) |
| สถานะ resume เฉพาะเครื่อง | `progress.json` (runner สร้างให้อัตโนมัติ; ไม่เก็บใน Git ควรใช้ workspace เดิมเพื่อทำคิวต่อ) |
| Catalog และ target classes | [all_bugs_catalog.json](../../target_benchmark/all_bugs_catalog.json), [target_benchmark/](../../target_benchmark/) |
| วิธีใช้ IPO | [Combinatorial_IPO/README.md](../../Combinatorial_IPO/README.md) |
| วิธีใช้ MIO | [MIO_Algorithm/README.md](../../MIO_Algorithm/README.md) |
| วิธีใช้ DeepSeek | [Deepseek-v4_flash/README.md](../../Deepseek-v4_flash/README.md) |
| วิธีใช้ Gemini | [Gemini-3_8_flash/README.md](../../Gemini-3_8_flash/README.md) |
| Docker และ Defects4J | [docker/README_DOCKER.md](../../docker/README_DOCKER.md) |
| รายงานฉบับส่งหลัก | [SQA_Final_Report.docx](../final/SQA_Final_Report.docx), [SQA_Final_Report.pdf](../final/SQA_Final_Report.pdf) |
| เอกสาร Markdown ประกอบ/สไลด์/เดโม | [Final_Report.md](../reports/Final_Report.md), [PRESENTATION_SLIDES.md](../presentation/PRESENTATION_SLIDES.md), [DEMO_GUIDE.md](../demo/DEMO_GUIDE.md) |

ใช้ master benchmark สำหรับ FDR/coverage; ใช้ manifest เป็นหลักฐาน generation/verification; ใช้ prompt, configuration และ logs สำหรับทำซ้ำการสร้าง suite

## 5. ข้อตกลงกลางสำหรับ suite

ก่อนส่ง suite ให้ผู้ประเมิน ให้ตรวจรายการนี้:

1. ระบุ Defects4J project, bug ID, technique, target class แบบ fully qualified name และ path ของ suite
2. ถ้าบั๊กมีหลาย modified classes ให้ระบุทุกคลาส และเชื่อมไฟล์ suite ทั้งหมดกับคีย์เดียวกันอย่างชัดเจน
3. บันทึกเวอร์ชันที่ใช้ยืนยัน suite; fixed-version verification เป็นหลักฐาน generation แยกจากการประเมิน buggy/fixed ของ benchmark
4. บันทึก SHA-256, run ID, เวลา, configuration, seed/budget ที่เกี่ยวข้อง และ log path
5. ใช้ JUnit/API และ package ที่ compile ได้กับ Defects4J project รุ่นนั้น ห้ามอาศัย library ที่ไม่มีใน classpath
   - รูปแบบชื่อไฟล์ที่ใช้ใน repository: IPO คือ `<TargetClass>_IPOTest.java`, MIO คือ `<TargetClass>_ESTest.java`, DeepSeek คือ `<TargetClass>DeepseekTest.java`, Gemini คือ `<TargetClass>GeminiTest.java`; suite ของ MIO อาจมี scaffolding file เพิ่ม
6. JUnit test timeout และ runner subprocess timeout เป็นคนละระดับ; @Test(timeout = 4000) ไม่ได้แทน timeout ของ runner ซึ่งปัจจุบันตั้งไว้ 240 วินาทีต่อขั้น
7. Suite ที่ยังจับคู่ target ไม่ได้เป็น candidate/NO_SUITE; compile error ไม่ใช่ coverage 0
8. ผล master มีหนึ่งแถวต่อ bug–technique แม้ภายใน suite มีหลาย target class

ข้อมูลขั้นต่ำสำหรับ manifest หรือ handoff:

    project | bug_id | technique | target_class | suite_path | suite_sha256
    generation_status | configuration | seed/budget | generated_at | evidence/log_path

## 6. เตรียม environment และตรวจ target

### 6.1 ตรวจและซิงก์ repository

จาก root ของ repository ตรวจงานที่ยังไม่ commit ก่อนดึงการเปลี่ยนแปลง ห้ามทับงานค้าง:

    git status --short
    git fetch origin
    git status -sb

ถ้าไม่มี divergence หรือไฟล์ค้างที่ต้องเก็บ จึงอัปเดตแบบ fast-forward:

    git pull --ff-only origin main

### 6.2 เปิด Docker และตรวจ Defects4J

จาก root ของ repository บนเครื่องที่ติดตั้ง Docker:

    docker compose -f docker/docker-compose.yml up -d --build
    docker ps --filter "name=defects4j_sqa"
    docker exec defects4j_sqa java -version
docker exec defects4j_sqa defects4j info -p Math -b 2
docker exec defects4j_sqa git -C /opt/defects4j rev-parse HEAD

ดูรายละเอียดรุ่นเครื่องมือและคำสั่งปิด container ใน [คู่มือ Docker](../../docker/README_DOCKER.md). อย่าปิด Docker/WSL ระหว่างมี job ทำงาน และอย่าลบ volume ที่ยังเก็บผลโดยไม่สำรอง logs

### 6.3 ตรวจหรือสกัด target

จาก root ของ repository:

    python scripts/extract_target_classes.py --list
    python scripts/extract_target_classes.py --list --project Lang
    python scripts/extract_target_classes.py --info --project Math --bug 2
    python scripts/extract_target_classes.py --project Lang --bug 1

ใช้ info ตรวจ target/FQCN ก่อนสร้าง suite. การสกัดต้องใช้ environment ตามคู่มือ Docker. คำสั่ง --all สกัดได้ทั้ง catalog แต่เป็นงานใหญ่ ให้ตรวจพื้นที่และสถานะไฟล์ก่อนใช้

## 7. ขั้นตอนของแต่ละสมาชิก

### Member 1 — Native IPO

1. ตรวจ configuration และ inventory ว่า class อยู่ในสถานะใดก่อนสร้าง
2. สร้าง pairwise combinations ด้วย Native IPO; เก็บ model, configuration และ logs
3. สร้าง JUnit suite และยืนยันกับ fixed version ตาม pipeline ของ IPO
4. ส่ง suite ที่ manifest ยืนยัน พร้อม path และ SHA-256
5. แยก PICT reference ออกจาก Native IPO ห้ามเรียกผล PICT ว่าเป็น IPO

อ้างอิง [IPO README](../../Combinatorial_IPO/README.md), [verified suite manifest](../../Combinatorial_IPO/Results/verified_suites_manifest.json) และ [IPO results report](../../Combinatorial_IPO/docs/RESULTS_REPORT.md). ปัจจุบัน manifest มี 277 verified target-class records ใน 257 bug IDs; benchmark มี suite 257 bug–IPO pairs.

### Member 2 — MIO / EvoSuite

1. ใช้ configuration ของรอบ budget: 30, 60, 120 วินาที และ seeds 101, 102, 103
2. เก็บผล budget/seed เป็น generation metrics แยกจาก benchmark
3. ส่ง suite พร้อม EvoSuite scaffolding, target mapping, configuration และ log
4. หาก generation ล้มเหลว ให้ส่ง error/log/configuration; benchmark เป็น NO_SUITE เมื่อไม่มี suite ให้ประเมิน
5. ใช้ผลสรุปงบค้นหาจากรายงานปัจจุบัน อย่านำค่าสถิติรอบก่อนมาอ้างโดยไม่ตรวจ

อ้างอิง [MIO README](../../MIO_Algorithm/README.md), [budget comparison](../../MIO_Algorithm/Result_Round2/budget_comparison.md) และ [failure analysis](../../MIO_Algorithm/MIO_FAILURE_ANALYSIS_REPORT.md).

### Member 3 — DeepSeek และ Gemini

1. ตรวจ target class และ defect context จาก catalog/Defects4J ก่อนสร้าง prompt
2. บันทึก prompt version, model ID/provider, configuration, generation status, token/time และเวลาเรียก
3. ส่ง source test code พร้อม project/bug/class mapping และ SHA-256
4. สำหรับบั๊กหลาย target classes ให้ระบุ class-to-suite mapping ให้ตรวจได้
5. ให้ Member 4 ประเมิน suite บน buggy/fixed; generation log อย่างเดียวไม่ใช่หลักฐาน coverage/FDR

อ้างอิง [DeepSeek README](../../Deepseek-v4_flash/README.md), [Gemini README](../../Gemini-3_8_flash/README.md) และ [M3 delivery report](../../results/DELIVERY_REPORT_M3.md). เก็บ prompt/result รายรอบไว้เป็น provenance; เมื่อแก้ suite ให้บันทึก run ใหม่ ไม่เขียนทับหลักฐานเก่า

### Member 4 — Benchmark, analytics และเอกสาร

1. ตรวจ manifest, suite hash, target class และ path ก่อนประเมิน
2. เปิด Docker, ตรวจ Defects4J/Java แล้วรันตัวอย่างหนึ่ง bug–technique
3. ประเมินคีย์ใหม่หรือผล stale ด้วย resume; แยก checkout, compile, timeout และ execution errors
4. ตรวจ BUG_DETECTED ว่ามี failure บน buggy และไม่มี failure บน fixed
5. สร้าง master, suite-gap audit, analytics, workbook และกราฟจากชุดผลเดียวกัน
6. สุ่มไล่จาก master กลับไป suite, hash, run ID และ log
7. อัปเดตรายงานและสื่อหลังตัวเลขนิ่ง พร้อมระบุ snapshot date เดียวกัน

## 8. รัน benchmark และอัปเดตผล

### 8.1 ทดสอบบั๊กและเทคนิคเดียว

จาก host ที่เปิด Docker:

    docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1 --techniques ipo

เทคนิคที่รองรับ: ipo, mio, deepseek, gemini; ตรวจ --help หาก option เปลี่ยน

### 8.2 ทำต่อคิวที่มี suite

    docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --all-bugs --resume

runner ประเมิน suite ที่ค้นพบและทำต่อจาก progress; ไม่ได้สร้าง suite ที่ไม่มี ห้ามเปิดคิวซ้ำก่อนตรวจ process เดิม, inventory, logs และสถานะ resume

### 8.3 สร้าง consolidated outputs

รันบน Windows host หลังคิวหยุดหรือเสร็จแล้ว:

    .\.venv\Scripts\python.exe scripts/reclassify_fault_detection.py --apply
    .\.venv\Scripts\python.exe scripts/consolidate_master_results.py
    .\.venv\Scripts\python.exe scripts/audit_suite_gaps.py
    .\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
    .\.venv\Scripts\python.exe scripts/plot_results.py

หากยังไม่มี virtual environment ให้ติดตั้ง dependencies ตาม README. ห้ามรันสองคิวเขียนผลลงไฟล์เดียวกันพร้อมกัน `progress.json` เป็น checkpoint เฉพาะเครื่องและถูก Git มองข้าม ส่วน run records อาจเปลี่ยน ให้ตรวจรายการไฟล์ก่อน commit

เตรียม virtual environment บน Windows host เมื่อจำเป็น:

    python -m venv .venv
    .\.venv\Scripts\python.exe -m pip install -r requirements-analysis.txt

### 8.4 ตรวจผลก่อนนำไปใช้

- มี 3,416 คีย์ไม่ซ้ำสำหรับ snapshot นี้
- 2,797 คู่มี suite; 619 คู่เป็น NO_SUITE
- unresolved_rows เป็น 0; results_complete หมายถึง available-suite evaluations ครบ
- สถานะรวม: 160 BUG_DETECTED, 711 NOT_DETECTED, 794 FLAKY_OR_REGRESSION, 1,132 COMPILE_ERROR
- coverage ว่างเมื่อ compile ไม่ผ่านหรือวัดไม่ได้ ห้ามกรอก 0 แทน
- BUG_DETECTED trace กลับไปผล buggy/fixed และ suite hash ได้
- CSV, JSON, Excel, analytics และกราฟใช้ snapshot เดียวกัน

## 9. ตีความสถานะผล

| สถานะ | ความหมาย |
|---|---|
| BUG_DETECTED | test fail บน buggy และไม่มี failure บน fixed |
| NOT_DETECTED | ไม่พบ failure บนทั้ง buggy และ fixed |
| FLAKY_OR_REGRESSION | พบ failure บน fixed; ไม่นับเป็นการตรวจพบ fault ตามนิยามนี้ |
| COMPILE_ERROR | suite คอมไพล์ไม่ผ่าน; ไม่มี coverage ที่นำเสนอได้ |
| TIMEOUT | subprocess เกินเวลาที่ runner กำหนด |
| NO_SUITE | ไม่มี suite ส่งเข้าประเมิน; ไม่ใช่ผลการทดสอบ |

Classifier เปรียบเทียบผลสองเวอร์ชัน แต่ไม่ได้ยืนยัน semantic root cause ของ failure โดยอัตโนมัติ จึงต้องตรวจ test/log ตัวอย่างและกล่าวถึงข้อจำกัดนี้ในรายงาน

Coverage ที่เปรียบเทียบเป็น target-class coverage. เมื่อบั๊กมีหลาย target classes ให้อธิบายวิธีรวม coverage และจำนวนตัวอย่างที่วัดได้ Compile error และ missing measurements ไม่ใช่ศูนย์เปอร์เซ็นต์

MIO budget, IPO suite size และ AI token/time เป็น generation metrics แยกจาก benchmark coverage/FDR เว้นแต่มี provenance หรือ run ID เชื่อมโยงกัน ห้ามอ้าง token ต่อบั๊กที่ตรวจพบจากค่าเฉลี่ย generation ที่จับคู่ไม่ได้

## 10. ตรวจรายงานและเตรียมเดโม

ก่อนคัดลอกตัวเลข ให้ตรวจ snapshot date, denominator, จำนวน suite, detections และ coverage จาก master summary/JSON แล้วเทียบ [รายงานฉบับส่งหลัก DOCX](../final/SQA_Final_Report.docx), [รายงานฉบับส่งหลัก PDF](../final/SQA_Final_Report.pdf), [Markdown ประกอบ](../reports/Final_Report.md), [สไลด์](../presentation/PRESENTATION_SLIDES.md), [DEMO_GUIDE.md](../demo/DEMO_GUIDE.md), README และ workbook

ลำดับเดโมที่แนะนำ:

1. ระบุ Defects4J project/bug และ target class ที่เลือก
2. แสดง suite, technique, path/hash และ configuration/prompt ที่เกี่ยวข้อง
3. รันหรือเปิด log ที่มีผลบน buggy และ fixed versions
4. แสดงสถานะและ coverage ที่วัดได้จริง
5. ครอบคลุมทั้ง 4 เทคนิคเมื่อมีหลักฐานที่รันได้; ถ้าไม่มี suite หรือ compile ไม่ผ่าน ให้อธิบายตามผลจริง ไม่สร้างผลแทน

ใช้ลำดับและคำสั่งใน [DEMO_GUIDE.md](../demo/DEMO_GUIDE.md). เตรียม log สำรองได้เมื่อ Docker/API ช้า แต่ให้บอกผู้ฟังเมื่อสาธิตจาก log ที่บันทึกไว้

## 11. Checklist ก่อนส่ง

### เจ้าของเทคนิค

- [ ] suite ทุกชุดระบุ project/bug/target class ถูกต้อง
- [ ] source, prompt/model/configuration, seed/budget และ logs อยู่ใน repository ตามที่เกี่ยวข้อง
- [ ] hash ใน manifest ตรงกับไฟล์ที่ส่ง
- [ ] candidate, generation failure และ verified suite แยกสถานะชัดเจน

### ผู้รวมผล / Member 4

- [ ] master มีหนึ่งคีย์ต่อ project + bug_id + technique และไม่มี duplicate
- [ ] status, hash, buggy/fixed results, run ID และ log สอดคล้องกัน
- [ ] NO_SUITE, compile error และ coverage missing ไม่ถูกแทนด้วยผล/coverage 0
- [ ] FDR/coverage แสดงตัวหาร ขอบเขต และจำนวนตัวอย่าง
- [ ] รายงาน สไลด์ กราฟ workbook และ README ใช้ snapshot เดียวกัน
- [ ] README ระบุชื่อ/รหัสสมาชิกและขั้นตอนทำซ้ำ
- [ ] ทดสอบลิงก์ คำสั่ง และ suite จาก checkout ใหม่หรือเครื่องอื่น

### ก่อนนำเสนอ

- [ ] ตัวเลขบนสไลด์ตรงกับรายงานและ master
- [ ] มีตัวอย่าง buggy/fixed และ log สำรอง
- [ ] แยก generation metrics จาก evaluation metrics
- [ ] อธิบาย NO_SUITE, compile error และข้อจำกัดของ classifier ได้
- [ ] ส่ง GitHub และ Google Classroom ตามช่องทางที่กำหนด

## 12. เอกสารย้อนหลัง

เอกสารเหล่านี้บันทึกแผนหรือขั้นตอนรอบก่อน ให้ตรวจวันที่และสถานะก่อนอ้าง ไม่ใช่คำสั่ง batch ล่าสุด:

- MIO: EXECUTION_PLAN.md, NEXT_STEPS_PLAN.md, MIO_RUN_CHEATSHEET.md, manual_member2_mio_defects4j.md และ workflow_การทำงาน.md
- IPO: docs/HANDOFF.md และ docs/RESULTS_REPORT.md เมื่อกล่าวถึงแผนหรือผลย้อนหลัง
- [รายงานรอบที่ 1](../reports/history/Report_Round1_Draft.md)

เอกสารย้อนหลังมีประโยชน์ต่อประวัติการตัดสินใจ แต่ตัวเลข จำนวน suite และคำสั่งเก่าห้ามใช้แทน snapshot ปัจจุบัน
