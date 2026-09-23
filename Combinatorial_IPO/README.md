# Combinatorial Testing: Native IPO 2-Way Pipeline

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล)  
**คู่มือแม่บทของทีม:** [`TEAM_WORKFLOW_GUIDE.md`](../TEAM_WORKFLOW_GUIDE.md)

---

## 1. บทนำและสถาปัตยกรรมโครงการ (Overview)

Pipeline นี้พัฒนาขึ้นเพื่อสร้างชุดทดสอบเชิงผสมผสาน (Combinatorial Testing) แบบ 2-way (Pairwise) ด้วยขั้นตอนวิธี **Native In-Parameter-Order (IPO)** โดยครอบคลุมเมธอดระดับคลาส (Class-Level Scope) ของทุกคลาสเป้าหมายที่ถูกแก้ไขใน Defects4J (1,070 modified class instances จาก 854 Bug IDs)

### หลักการสำคัญ
1. **Pure Native IPO Engine:** พัฒนาอัลกอริทึม IPO (Lei & Tai) ขึ้นเองโดยตรง (`Code/algorithm/ipo.py`) ไม่พึ่งพา generator ภายนอกอย่าง PICT หรือ EvoSuite ในการสร้างคู่ทดสอบ
2. **Fixed-Version Oracle Collection:** รันและสกัดผลลัพธ์จากคลาสเวอร์ชันแก้ไขจริง (`*f`) เพื่อให้ได้ assertion ที่ถูกต้องแม่นยำ ไม่สุ่มค่า
3. **100% Pair Coverage Guarantee:** ทุกเมธอดใน suite ที่ publish ต้องผ่านการตรวจวัด pair coverage ครบ 100.0% โดยอิสระ
4. **Partial-Class Fault Isolation:** หากเมธอดใดเกิด timeout หรือมีข้อยกเว้นที่ไม่รองรับ ระบบจะตัดเฉพาะเมธอดนั้นออก และ publish เฉพาะเมธอดที่ผ่านการ verify สำเร็จ
5. **Deterministic Formatting & Construction:** จัดรูปแบบอาร์เรย์ (`Arrays.toString`, `Arrays.deepToString`) ป้องกัน address identity hash (`[I@...`) และรองรับ static factories / concrete subtypes

---

## 2. โครงสร้างโครงการรวมศูนย์ (Single Clean Layout)

โครงสร้างไดเรกทอรีของโครงการถูกจัดระเบียบให้เหลือชุดใช้งานเดียวอย่างสมบูรณ์:

```text
Combinatorial_IPO/
├── README.md                          # เอกสารแนะนำและคู่มือการใช้งาน
├── run_batch.ps1                      # สคริปต์ PowerShell สำหรับรัน batch แบบคำสั่งเดียว
├── run_batch.sh                       # สคริปต์ Bash สำหรับรัน batch ใน Linux / Docker
├── Code/                              # ซอร์สโค้ดและชุดทดสอบของระบบ
│   ├── algorithm/                     # Native IPO 2-way covering array algorithm
│   ├── analyzer/                      # Java Compiler Tree API AST Parser
│   ├── domain/                        # Type adapters, semantic domains, construction planner
│   ├── generator/                     # Oracle-backed JUnit 4 synthesizer
│   ├── oracle/                        # Fixed-version runner, exception unwinding & formatting
│   ├── runner/                        # All-class pipeline orchestration, audits, fast-resume
│   ├── verification/                  # Independent pairwise coverage validator
│   └── tests/                         # Unit tests (126 tests ผ่าน 100%)
├── Configuration/
│   └── catalogs/                      # Normalized benchmark catalogs
├── TestCode/                          # Published fixed-verified JUnit 4 suites สำหรับ Member 4 (277 คลาส)
│   └── <Project>_<BugID>b/
│       └── <package-path>/<Class>_IPOTest.java
├── Results/                           # ผลการทดลอง แคช และ manifest รวมศูนย์
│   ├── baseline/                      # Baseline manifest แช่แข็ง (173 suites ณ commit a11795acc5)
│   ├── cache/                         # ข้อมูล intermediate และ class_record.json รายคลาส
│   ├── logs/                          # failures.log และ batch_progress.log
│   ├── inventory.json                 # Audit status ของทั้ง 1,070 คลาสอินสแตนซ์
│   ├── verified_suites_manifest.json  # ดัชนีหลักของ suites ที่ publish ทั้งหมด (277 suites, 109,700 tests)
│   └── routing_manifest.json          # สรุป routing และ backlog (NEEDS_ADAPTER, ฯลฯ)
└── docs/                              # เอกสารทางเทคนิค
    ├── DESIGN.md                      # รายละเอียดสถาปัตยกรรม อัลกอริทึม และการแก้ปัญหา
    ├── RESULTS_REPORT.md              # รายงานผลประเมินเชิงประจักษ์ เปรียบเทียบกับ baseline
    └── HANDOFF.md                     # คู่มือรับช่วงต่องาน การขยาย Adapter และคำแนะนำผู้ใช้
```

---

## 3. วิธีการรันระบบ (Execution Guide)

### ก) การรัน Unified Pipeline อัตโนมัติ (แนะนำสำหรับผู้ใช้รันเองจาก Terminal)

สามารถสั่งรันจาก PowerShell บนเครื่อง Host ได้ทันที:

```powershell
# รันผ่านสคริปต์อัตโนมัติ (Audit -> Canary Gate -> Full Generation -> Summary & Validate)
.\Combinatorial_IPO\run_batch.ps1
```

หรือสั่งผ่าน Docker CLI โดยตรง:

```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j sh -c "python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode audit --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode canary --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate"
```

**คุณสมบัติของ Batch Runner:**
- **Audit & Inventory Refresh:** ประเมินความพร้อมของทั้ง 1,070 คลาสด้วย Adapters ล่าสุด (ปลดล็อก 319 `AUTO_READY` คลาส)
- **Canary Gate:** ตรวจสอบความถูกต้องของ Baseline Canary ครบ 40/40 คลาส (100% Passed) ก่อนเริ่มคิวเต็ม
- **Fast Resume:** คลาสที่มี suite ที่ verify แล้วใน manifest (277 suites) จะถูกข้ามทันที ($<1$ ms) ไม่เสียเวลารันซ้ำ
- **Zero Regression:** ป้องกันการรันซ้ำของ baseline suites อย่างสมบูรณ์
- **Atomic Manifest Updates:** อัปเดต `Results/verified_suites_manifest.json` ทันทีที่แต่ละคลาสผ่าน ไม่สูญเสียความคืบหน้าหากหยุดกลางคัน
- **Failure Isolation:** คลาสที่ไม่ผ่านจะถูกแยก log ละเอียดไว้ที่ `Results/logs/failures.log` โดยไม่หยุด batch

### ข) การรันเฉพาะ Project หรือเฉพาะ Bug ID (สำหรับ Debug / ตรวจสอบสั้น)

```powershell
# รันเฉพาะโปรเจกต์ เช่น Chart
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --project Chart --mode generate --resume

# รันเฉพาะเจาะจง Bug ID เดียว เช่น Chart-11
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --project Chart --bug 11 --mode generate --resume
```

### ค) การดูสรุปผลและการตรวจสอบความถูกต้อง (Summary & Validate)

```powershell
# แสดงสถานะภาพรวมของทั้ง 1,070 คลาส
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary

# ตรวจสอบความถูกต้องของ Checksum และ Pair Coverage ของทุก Suite ใน TestCode
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
```

### ง) การรัน Unit Tests ของระบบ (118 tests)

```powershell
docker --context default exec sqa-defects4j sh -lc "cd /workspace/Combinatorial_IPO/Code && python3 -m unittest discover -s tests -p 'test_*.py' -v"
```

---

## 4. มาตรฐานการส่งมอบสู่ Member 4 (Delivery Gate)

ชุดทดสอบใน `TestCode/` ทุกไฟล์จะต้อง:
1. ปรากฏอยู่ใน `Results/verified_suites_manifest.json` พร้อมค่า SHA-256 และ toolchain hash
2. เป็น Native IPO 2-Way Covering Array ที่มี Pair Coverage 100.0%
3. สังเคราะห์เป็น JUnit 4 โดยทุกเมธอดทดสอบมี `@Test(timeout = 4000)`
4. คอมไพล์และรันผ่าน (`OK (...)`) บน Defects4J Fixed Version เสมอ

Member 4 สามารถนำไฟล์จาก `TestCode/` ไปรัน Fault Detection Rate (FDR), Buggy Execution และ Mutation Coverage ต่อไปได้ทันที
