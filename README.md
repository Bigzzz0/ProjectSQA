# Project SQA: AI-Assisted Testing and Automatic Test Generation

โครงการนี้เปรียบเทียบการสร้างชุดทดสอบอัตโนมัติด้วย **Native IPO**, **MIO (EvoSuite)**, **DeepSeek V4 Flash** และ **Gemini 3.8 Flash** บน Java projects ในชุดข้อมูล Defects4J โดยประเมิน line coverage, branch coverage และความสามารถในการตรวจพบบั๊กจากการรันทดสอบบนเวอร์ชัน buggy และ fixed

**รายวิชา:** CP353201 Software Quality Assurance · ภาคการศึกษา 1/2569<br>
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก<br>
**หลักสูตร:** วิทยาการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น

---

## สมาชิกและหน้าที่

| สมาชิก | รหัสนักศึกษา | ชื่อ | ความรับผิดชอบหลัก |
|---|---|---|---|
| Member 1 | 673380278-9 | นายปวริศช์ ประมวล | Native IPO และ combinatorial testing; จัดทำ factor/value models, pairwise combinations และ JUnit 4 suites ใน `Combinatorial_IPO/` |
| Member 2 | 673380301-0 | นายแทนคุณ พันธ์นิกุล | MIO บน EvoSuite; ทดลอง search budgets 30, 60 และ 120 วินาทีด้วย seeds 101, 102 และ 103; ดูแล suites และผล generation ใน `MIO_Algorithm/` |
| Member 3 | 673380272-1 | นายธนภูมิ จันทรา | Prompt และชุดทดสอบจาก DeepSeek V4 Flash กับ Gemini 3.8 Flash; ดูแล generation logs และ suites ในโฟลเดอร์ของแต่ละโมเดล |
| Member 4 | 673380292-5 | นายศิฆรินทร์ อุปจันทร์ | Docker/Defects4J environment, benchmark runner, target catalog, การรวมผล coverage/FDR และเอกสารโครงการ |

ดูขั้นตอนการทำงาน คำสั่ง และตำแหน่งส่งมอบของแต่ละคนได้ที่ [คู่มือการทำงานของทีม](docs/team/TEAM_WORKFLOW_GUIDE.md)

---

## ขอบเขตและวิธีประเมินผล

### ชุดข้อมูลและขอบเขต

- ใช้ Java projects 17 โครงการใน Defects4J: `Chart`, `Cli`, `Closure`, `Codec`, `Collections`, `Compress`, `Csv`, `Gson`, `JacksonCore`, `JacksonDatabind`, `JacksonXml`, `Jsoup`, `JxPath`, `Lang`, `Math`, `Mockito` และ `Time`.
- Catalog ครอบคลุม 854 บั๊ก โดยประเมิน 4 เทคนิค จึงมี 3,416 คู่บั๊ก–เทคนิคในตารางผลหลัก.
- ขอบเขต coverage คือ modified target classes (`classes.modified`) ของแต่ละบั๊ก.
- `--sample-17` ใช้บั๊กตัวแทนหนึ่งรายการต่อโครงการ รวม 68 คู่บั๊ก–เทคนิค; `--all-bugs` ใช้ catalog ทั้งหมด.
- โหมด `--resume` อ่าน checkpoint `progress.json` ใน workspace ปัจจุบัน ไฟล์นี้สร้างโดย runner และไม่ถูกติดตามใน Git; ใช้ workspace เดิมเมื่อต้องการทำคิวต่อ.

### ตัวชี้วัด

- **Line coverage** และ **branch coverage** วัดบน modified target classes ด้วยเครื่องมือที่เชื่อมกับ Defects4J. คำนวณค่าเฉลี่ยจากผลที่วัด coverage ได้เท่านั้น; compile errors และค่าที่วัดไม่ได้ไม่ถูกนับเป็นศูนย์.
- **Fault Detection Rate (FDR)** นับระดับบั๊ก ตรวจพบเมื่อมี test อย่างน้อยหนึ่งรายการ fail บนเวอร์ชัน buggy และผ่านบนเวอร์ชัน fixed. รายงานทั้งตัวหารจากรายการที่พยายามประเมินและจาก catalog 854 บั๊ก.
- **Generation metrics** เช่น เวลาและ token ใช้บรรยายขั้นตอนสร้าง suite และแยกจากผล benchmark เว้นแต่มี run identity ที่เชื่อมโยงกันได้.
- ผล `NO_SUITE` หมายถึงไม่มี suite ที่ส่งเข้าประเมิน ไม่ใช่ผลการทดสอบที่ผ่านหรือตรวจไม่พบบั๊ก.

---

## ข้อกำหนดของ test suites

เพื่อให้ runner ประเมินชุดทดสอบได้ แต่ละ suite ควรเป็นไปตามข้อกำหนดต่อไปนี้:

1. ใช้ JUnit 4 ซึ่งเป็นรูปแบบที่ Defects4J projects ในการทดลองรองรับ.
2. ประกาศ Java package ให้ตรงกับ target package ของโปรเจกต์.
3. กำหนด timeout ให้ test ที่อาจใช้เวลานาน เพื่อป้องกันการค้างระหว่างประเมิน.
4. ควบคุม random seed และหลีกเลี่ยงการพึ่งพาเวลา ระบบ หรือสถานะภายนอกที่ทำให้ผลทดสอบไม่สม่ำเสมอ.

---

## โครงสร้าง repository

```text
ProjectSQA/
├── README.md                     # ภาพรวมโครงการและคู่มือเริ่มต้น
├── Combinatorial_IPO/            # Native IPO, models, suites และผล generation
├── MIO_Algorithm/                # MIO/EvoSuite, configurations และ suites
├── Deepseek-v4_flash/            # Prompts, generation records และ suites
├── Gemini-3_8_flash/             # Prompts, generation records และ suites
├── target_benchmark/             # Bug catalog และ modified target classes
├── results/                      # Master data, run records, reports และ figures
├── scripts/                      # Benchmark runner และสคริปต์วิเคราะห์ผล
├── docker/                       # Dockerfile, Compose และ environment guide
├── docs/
│   ├── final/                    # รายงาน DOCX/PDF ฉบับส่ง
│   ├── reports/                  # รายงาน Markdown และเอกสารรอบก่อน
│   ├── presentation/             # เนื้อหาสำหรับสไลด์
│   ├── demo/                     # คู่มือสาธิตและทำซ้ำ
│   ├── team/                     # คู่มือทำงานและการส่งมอบ
│   └── reference/                # โจทย์และเอกสารอ้างอิง
├── archive/                      # ไฟล์ legacy ที่ไม่ใช้เป็นผลปัจจุบัน
├── tests/                        # Tests ของ project tooling
└── requirements-analysis.txt    # Dependencies สำหรับวิเคราะห์ผลบน host
```

---

## วิธีทำซ้ำผลการทดลอง

### สิ่งที่ต้องเตรียม

- Git และ Docker Desktop หรือ Docker Engine พร้อม Docker Compose
- Python 3 บน host สำหรับสร้างตารางสรุปและกราฟ
- พื้นที่ดิสก์และเครือข่ายสำหรับ build Docker image และ checkout Defects4J projects

### 1. ดาวน์โหลด repository และเริ่ม environment

```bash
git clone https://github.com/Bigzzz0/ProjectSQA.git
cd ProjectSQA

docker compose -f docker/docker-compose.yml up -d --build
docker exec defects4j_sqa defects4j info -p Math -b 2
```

คำสั่ง `defects4j info` ใช้ตรวจว่า container และ Defects4J พร้อมใช้งาน

### 2. รัน benchmark

คำสั่งต่อไปนี้รันจาก PowerShell หรือ terminal ที่ root ของ repository:

```powershell
# ประเมิน Lang-1 ด้วยทุกเทคนิค
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1

# ประเมินบั๊กตัวแทนหนึ่งรายการต่อโครงการ
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --sample-17

# ประเมิน catalog ทั้งหมดและทำต่อจาก checkpoint ใน workspace เดิม
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --all-bugs --resume

# ตรวจรายการส่งมอบของ Member 3 ก่อนประเมิน
docker exec defects4j_sqa python3 /workspace/scripts/run_member3_delivery.py --dry-run
```

`progress.json` เป็น checkpoint ในเครื่องและถูก Git มองข้าม หากย้ายเครื่องหรือ clone ใหม่ ให้ตรวจผลและ run records ก่อนเริ่มคิวใหม่

### 3. สร้างตารางสรุปและกราฟ

ทำขั้นตอนนี้บน host หลัง benchmark หยุดหรือเสร็จแล้ว:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements-analysis.txt
.\.venv\Scripts\python.exe scripts/reclassify_fault_detection.py --apply
.\.venv\Scripts\python.exe scripts/consolidate_master_results.py
.\.venv\Scripts\python.exe scripts/audit_suite_gaps.py
.\.venv\Scripts\python.exe scripts/advanced_data_analytics.py
.\.venv\Scripts\python.exe scripts/plot_results.py
```

## ผลประเมินล่าสุด

ตารางต่อไปนี้มาจาก snapshot วันที่ 26 กันยายน 2026. Master dataset มี 3,416 คู่บั๊ก–เทคนิคจาก 854 บั๊กและ 4 เทคนิค; suite ที่มีอยู่ 2,797 คู่ได้รับการประเมิน และ 619 คู่เป็น `NO_SUITE`. การประเมิน suite ที่มีอยู่ครบไม่ได้หมายความว่ามี suite ครบทุกคู่ใน catalog.

| สถานะผลประเมิน | จำนวนคู่ |
|---|---:|
| `BUG_DETECTED` | 160 |
| `NOT_DETECTED` | 711 |
| `FLAKY_OR_REGRESSION` | 794 |
| `COMPILE_ERROR` | 1,132 |
| `NO_SUITE` | 619 |

| เทคนิค | พยายามประเมิน | ประเมินสำเร็จ | Line coverage เฉลี่ย | Branch coverage เฉลี่ย | ตรวจพบบั๊ก | FDR ของรายการที่พยายามประเมิน | FDR ของ catalog |
|---|---:|---:|---:|---:|---:|---:|---:|
| Native IPO | 257 | 252 | 26.76% (n=252) | 18.67% (n=252) | 37 | 14.40% | 4.33% |
| MIO (EvoSuite) | 834 | 797 | 63.85% (n=797) | 56.51% (n=797) | 5 | 0.60% | 0.59% |
| DeepSeek V4 Flash | 853 | 192 | 78.02% (n=192) | 70.22% (n=192) | 11 | 1.29% | 1.29% |
| Gemini 3.8 Flash | 853 | 424 | 86.29% (n=424) | 79.54% (n=424) | 107 | 12.54% | 12.53% |

FDR นับบั๊กที่มี test fail บน buggy version และผ่านบน fixed version. FDR ของรายการที่พยายามประเมินใช้ตัวหาร `DONE + COMPILE_ERROR + TIMEOUT`; FDR ของ catalog ใช้ 854 บั๊ก. ค่า coverage เฉลี่ยคำนวณเฉพาะผลที่วัดได้ และไม่รวม compile errors หรือค่าที่หายไป.

| แหล่งข้อมูล | รายละเอียด |
|---|---|
| [Master benchmark CSV](results/master_benchmark_summary.csv) | สถานะและผลวัดหนึ่งแถวต่อคู่บั๊ก–เทคนิค |
| [สถิติเชิงพรรณนา JSON](results/master_descriptive_stats.json) | จำนวน suite, ผลประเมิน, FDR และ coverage |
| [รายงานวิเคราะห์สถิติ](results/advanced_analytics_report.md) | ตารางเปรียบเทียบและการวิเคราะห์เพิ่มเติม |
| [บัญชี suite](results/suite_inventory.csv) | suite ที่พบพร้อม hash สำหรับตรวจสอบย้อนกลับ |
| [บัญชีช่อง NO_SUITE](results/suite_gap_audit.csv) | รายละเอียด suite ที่ขาดและสถานะไฟล์ candidate |

หากมีการเพิ่มหรือแก้ suite ให้รัน benchmark และสร้าง CSV, Excel, JSON, รายงาน analytics และกราฟใหม่จาก snapshot เดียวกันก่อนอ้างตัวเลข.

---

## แผนภูมิผลการทดลอง

### รูปที่ 1: Line และ branch coverage ของทั้ง 4 เทคนิค
![Figure 1: Code Coverage Comparison](results/figure1_coverage_comparison.png)

### รูปที่ 2: การแจกแจงสถานะผลประเมิน
![Figure 2: Fault Detection Rate Distribution](results/figure2_fdr_distribution.png)

### รูปที่ 3: Line coverage แยกตามโครงการ
![Figure 3: Project-by-Project Coverage Breakdown](results/figure3_projects_breakdown.png)

### รูปที่ 4: Token และเวลา generation ของ AI
![Figure 4: AI Economics & Latency](results/figure4_ai_economics.png)

### รูปที่ 5: ผลของ search budget ต่อ MIO
![Figure 5: MIO Budget Scaling](results/figure5_budget_scaling.png)

### รูปที่ 6: การตรวจพบบั๊กและผลทับซ้อนระหว่างเทคนิค
![Figure 6: Ensemble Overlap](results/figure6_ensemble_overlap.png)

## เอกสารและไฟล์ส่งมอบ

- [รายงานฉบับส่งหลัก (DOCX)](docs/final/SQA_Final_Report.docx)
- [รายงานฉบับส่งหลัก (PDF)](docs/final/SQA_Final_Report.pdf)
- [เนื้อหา Markdown ประกอบรายงาน](docs/reports/Final_Report.md) — เอกสารประกอบ ไม่ใช่รายงานฉบับส่งหลัก
- [โจทย์รายวิชา](docs/reference/SQA_Project_2026_Assignment.pdf)
- [เนื้อหาสไลด์และ speaker notes](docs/presentation/PRESENTATION_SLIDES.md) — โครงสไลด์ในรูปแบบ Markdown
- [คู่มือสาธิตและทำซ้ำ](docs/demo/DEMO_GUIDE.md)
- [คู่มือการทำงานของทีม](docs/team/TEAM_WORKFLOW_GUIDE.md)
- [Master benchmark workbook](results/Master_Benchmark_Results.xlsx)
- [Data dictionary](results/DATA_DICTIONARY.md)
- [Advanced analytics report](results/advanced_analytics_report.md)

ข้อกำหนดการส่งรายงานฉบับสมบูรณ์ ผลการทดลอง source code, test code, prompts, configurations และเอกสารประกอบผ่าน GitHub และ Google Classroom อ้างอิงจาก [โจทย์รายวิชา](docs/reference/SQA_Project_2026_Assignment.pdf). การนำเสนอและสาธิตให้ใช้ผลและสถานะจาก snapshot เดียวกับรายงาน.

