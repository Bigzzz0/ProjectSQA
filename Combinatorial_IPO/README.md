# Combinatorial Testing: Native IPO 2-Way Pipeline

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล)

คู่มือแม่บทของทีมอยู่ที่ [`TEAM_WORKFLOW_GUIDE.md`](../TEAM_WORKFLOW_GUIDE.md) ส่วนนี้อธิบายการใช้งาน implementation ปัจจุบันของ Member 1

## Academic distinction

- **IPO (In-Parameter-Order)** คือ algorithm ที่โครงการ implement เองใน `Code/algorithm/ipo.py` โดยมี initial construction, Horizontal Growth และ Vertical Growth
- **Microsoft PICT** เป็นเครื่องมือแยกต่างหากที่เก็บไว้ใน `Code/backends/pict_backend.py` สำหรับ reference baseline เท่านั้น
- ผลจาก PICT ห้ามรายงานว่าเป็นผลจาก IPO แม้ทั้งสองชุดจะมี pair coverage ครบหรือมีจำนวนแถวเท่ากัน

Main pipeline ไม่เรียก PICT และไม่ต้องติดตั้ง PICT:

```text
Defects4J catalog + approved scenario spec
  -> defect-related constructor/setup/input/action factors
  -> constraint-aware valid combinations
  -> native IPO 2-way generation
  -> independent valid-pair verification
  -> Java scenario materialization + invariant/fixed oracle
  -> fixed-version JUnit 4 verification
  -> atomic TestCode publication after all 17 targets pass
```

## Current readiness status

ชุด `NumberUtils.createNumber(String)` จำนวน 48 cases ที่อยู่ใน repository ก่อน native IPO implementation เป็น **PICT pilot baseline** ซึ่งผ่านการเก็บ oracle และ fixed-version verification แล้ว และถูกเก็บแยกไว้ใต้ `baselines/pict/Lang_1b/` ห้ามนำชุดนี้ไปรายงานเป็นผล IPO

Native IPO สร้าง abstract combinations 48 แถว ครอบคลุม 194/194 pairs รักษา mandatory seed เก็บ oracle ใหม่ครบ 48 outcomes และผ่าน JUnit 4 บน `Lang-1f` ครบ `OK (48 tests)` แล้ว ชุดส่งมอบอยู่ใน `TestCode/Lang_1b/`

Readiness representatives ผ่าน fixed-version verification แล้ว 4/4 methods ครอบคลุม primitive หลาย parameters, String, floating point, boolean และสอง Defects4J projects:

- `NumberUtils.createNumber(String)`: 48 tests, 194/194 pairs
- `NumberUtils.min(int,int,int)`: 28 tests, 75/75 pairs
- `NumberInput.parseAsDouble(String,double)`: 60 tests, 60/60 pairs
- `NumberInput.inLongRange(String,boolean)`: 24 tests, 24/24 pairs

Catalog ถูกสร้างจาก `defects4j_info.txt` โดยอัตโนมัติครบ 17 bug targets, 22 modified source classes และ 56 triggering tests โดย approved defect-focused specs อยู่ใน `Configuration/targets/` ครบหนึ่งไฟล์ต่อ bug target

Full scenario run `20260910T071416Z` ผ่าน fixed-version verification ครบ 17/17 targets และเผยแพร่ 17 suites แล้ว ทุก scenario มี native IPO valid-pair coverage 100% รายละเอียดอยู่ใน `Result_Round2/20260910T071416Z/batch_manifest.json` ส่วน `Result_Round1/` ยังคงเป็นหลักฐาน readiness เดิมและไม่ถูกเขียนทับ

## Project structure

```text
Combinatorial_IPO/
├── Code/                      # Source code and automated tests
│   ├── algorithm/             # Native IPO implementation
│   ├── analyzer/              # Java source and signature analysis
│   ├── backends/              # Optional reference backends such as PICT
│   ├── domain/                # Generic domains and semantic overrides
│   ├── generator/             # Oracle-backed JUnit 4 synthesis
│   ├── oracle/                # Fixed-version oracle collection/verification
│   ├── runner/                # Safe filtered batch orchestration
│   ├── verification/          # Backend-independent pair coverage
│   └── tests/                 # Python unit and integration tests
├── baselines/                 # Reference results; PICT is not native IPO
├── Models/                    # Native IPO factor-domain artifacts
├── Result_Round1/             # Representative trials and readiness evidence
├── Result_Round2/             # Results from the real catalog loop
└── TestCode/                  # Fixed-verified JUnit suites for Member 4
```

PICT pilot artifacts are archived under `baselines/pict/`. `Result_Round1/` เก็บตัวทดลอง native IPO และ readiness โดยไม่ถูก catalog-loop เขียนทับ ส่วนผลลูปจริงอยู่ `Result_Round2/` และ JUnit ที่ verify ผ่านอยู่ `TestCode/`

## Run tests

จาก PowerShell ที่ repository root:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO/Code && python3 -m unittest discover -s tests -p "test_*.py" -v'
```

Tests ไม่เรียก PICT executable และใช้ temporary directories สำหรับ runner integration

## Audit and readiness checks

สร้าง catalog ใหม่จาก ground truth ในแต่ละ `defects4j_info.txt` (ฟิลด์ `target_class`/`simple_name` ยังคงไว้เป็น primary modified source เพื่อรองรับเครื่องมือเดิมของทีม):

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace && python3 Combinatorial_IPO/Code/runner/generate_catalog.py --target-root /workspace/target_benchmark --output /workspace/target_benchmark/catalog_17_projects.json --preserve-domains-from /workspace/target_benchmark/catalog_17_projects.json'
```

ตรวจ 17-target catalog โดยไม่สร้าง combinations, oracle หรือ JUnit:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/feasibility_audit.py --catalog /workspace/target_benchmark/catalog_17_projects.json --target-root /workspace/target_benchmark --output Result_Round1/feasibility_audit.json --summary-only'
```

ตรวจ artifacts ของ representative methods และ delivery gates โดยไม่เริ่ม loop:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/readiness_check.py'
```

คำสั่ง readiness จะสำเร็จเมื่อทั้ง 4 representatives ผ่านเดิม, feasibility audit ครบ และ approved scenario catalog ตรงกับ 17 bug targets, 22 modified sources และ 56 triggering tests โดยจะบล็อกเฉพาะขณะที่ run เดิมมีสถานะ `STARTED`/`PREFLIGHT`; run ที่จบเป็น `VERIFIED` แล้วสามารถกด Run ซ้ำเพื่อพิสูจน์ reproducibility ได้

## Safe native IPO trial

ระบุ project, bug และ exact signature พร้อมเขียนผลลงพื้นที่ทดลองเสมอ:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/run_ipo_batch.py --target-root /workspace/target_benchmark --output-root /tmp/ipo-native-trial --project Lang --bug 1 --signature "createNumber(String)"'
```

ผลรอบแรกประกอบด้วย domains, abstract combinations, concrete inputs และ manifest เมื่อยังไม่มี oracle ที่ตรงกับ arguments ระบบจะรายงาน `oracle_status: MISSING` และไม่สร้างไฟล์ใน `TestCode/`

หากพบ oracle เก่าที่จำนวนหรือลำดับ arguments ไม่ตรง ระบบจะรายงาน `oracle_status: STALE` และไม่สร้าง JUnit

## Delivery gate

ไฟล์ `<Class>_<method_id>_IPOTest.java` จะเผยแพร่ลง `TestCode/<Project>_<BugID>b/` ได้เมื่อ:

1. native IPO generation สำเร็จ
2. independent pair coverage ครบ 100%
3. oracle มี case ID และ arguments ตรงทุก combination
4. generated JUnit ใช้ JUnit 4 และทุก test มี `@Test(timeout = 4000)`
5. suite compile และผ่านบน Defects4J fixed version ก่อนนำไปวางเป็นชุดส่งมอบ

เมื่อเขียนลง output หลัก ระบบบังคับใช้ `--verify-suites`; ถ้าไม่ระบุจะบันทึก `VERIFICATION_REQUIRED` และไม่เผยแพร่ JUnit ส่วน method ที่ล้มเหลวจะมีสถานะแยกของตัวเองและไม่หยุด method/target อื่น

## Start the 17-target loop

วิธีหลักสำหรับผู้ควบคุมการทดลอง: เปิด `Code/runner/start_catalog_loop.py` ใน editor แล้วกด **Run Python File** ได้ทันที ไม่ต้องใส่ arguments สคริปต์จะตรวจ readiness ก่อนและจะไม่เริ่มลูปหากตรวจไม่ผ่าน

หรือสั่ง entry point เดียวจาก repository root:

```powershell
python Combinatorial_IPO/Code/runner/start_catalog_loop.py
```

คำสั่งแบบละเอียดที่ entry point เรียกภายใน Dockerคือ:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/scenario_catalog.py --catalog /workspace/target_benchmark/catalog_17_projects.json --scenarios /workspace/Combinatorial_IPO/Configuration/targets --output-root /workspace/Combinatorial_IPO'
```

คำสั่งนี้ไม่ auto-scan ทุก method แต่ประมวลผลเฉพาะ approved scenarios ที่โยงกับ patch และ triggering tests เก็บ snapshot, domains, constraints, combinations, materialized inputs, oracle metadata, JUnit และ manifest ใต้ `Result_Round2/<run-id>/` จากนั้นเผยแพร่ `TestCode/` เมื่อ fixed verification ผ่านครบทั้ง 17 targets เท่านั้น

Member 1 ไม่รัน buggy version, coverage หรือ Fault Detection Rate ในขั้นนี้ งานดังกล่าวเป็นความรับผิดชอบของ Member 4

## Current limitations

- Scenario-driven catalog รองรับ 17 targets ที่อนุมัติแล้ว ไม่ได้อ้างว่า config เหล่านี้ใช้ได้กับ Defects4J ทุก bug ID
- Target ใหม่ต้องมี spec ที่อ้าง modified source, source patch และ triggering test; runner จะไม่ย้อนกลับไป auto-scan เมธอดเพื่อเดา scenario
- Java snippets ใน spec เป็น trusted configuration ที่ผ่าน fixed compile/run ไม่ใช่ input จากผู้ใช้ภายนอก
- `null` ใช้ได้เฉพาะเมื่อเป็น input ตามสัญญาและมีเหตุผลจาก defect เช่น null comparator ของ Collections; unknown object type ยังห้าม fallback เป็น `null`
- Chart รุ่นเก่าต้องใช้ `svn`; Dockerfile ติดตั้ง `subversion` เพื่อให้ rebuild แล้วทำซ้ำได้
- Member 1 ยังไม่รัน buggy version, coverage หรือ Fault Detection Rate
