# Combinatorial Testing: Native IPO 2-Way Pipeline

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล)

คู่มือแม่บทของทีมอยู่ที่ [`TEAM_WORKFLOW_GUIDE.md`](../TEAM_WORKFLOW_GUIDE.md) ส่วนนี้อธิบายการใช้งาน implementation ปัจจุบันของ Member 1

## All-modified-class automation (ผู้ใช้เป็นผู้รัน)

workflow นี้อ่าน `target_benchmark/all_bugs_catalog.json` แบบ read-only, checkout buggy/fixed source ใน temporary directories และเผยแพร่เฉพาะ Native IPO suites ที่ผ่าน fixed-version verification แล้ว ห้ามใช้คำสั่ง extraction แบบ `--force` หรือแก้ master catalog เพื่อรองรับ IPO

### 1. Host setup และ regression tests

รันจาก PowerShell ที่ root ของ `ProjectSQA`:

```powershell
$repo = (Resolve-Path '.').Path
$ipo = Join-Path $repo 'Combinatorial_IPO'
$py = 'C:\Users\User\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$env:PYTHONPATH = Join-Path $ipo 'Code'

& $py -m unittest discover -s (Join-Path $ipo 'Code\tests') -p 'test_*.py' -v
```

### 2. สร้าง IPO-local snapshot และ experiment lock

สองคำสั่งนี้ไม่เขียนกลับ shared master catalog:

```powershell
& $py (Join-Path $ipo 'Code\runner\catalog_snapshot.py') `
  --input (Join-Path $repo 'target_benchmark\all_bugs_catalog.json') `
  --output (Join-Path $ipo 'Configuration\catalogs\all-modified-classes.normalized.json')

& $py (Join-Path $ipo 'Code\runner\all_class_experiment.py') `
  --catalog (Join-Path $ipo 'Configuration\catalogs\all-modified-classes.normalized.json') `
  --experiment-id all-854-modified-classes `
  --output (Join-Path $ipo 'Configuration\experiments\all-854-modified-classes.json')
```

รัน snapshot ซ้ำได้ ผล `catalog_sha256` ต้องเหมือนเดิมหาก master catalog ไม่เปลี่ยน

### 3. Docker preflight และ audit-only

ตัวอย่างใช้ container `sqa-defects4j` และ repository mount ที่ `/workspace`:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && PYTHONPATH=Code python3 Code/runner/all_class_pipeline.py --mode preflight --summary-only'

docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && PYTHONPATH=Code python3 Code/runner/all_class_pipeline.py --mode audit --workers 4 --resume --summary-only'
```

ผล audit อยู่ที่:

- `Result_Round1/all-854-modified-classes/audit_manifest.json`
- `Result_Round1/all-854-modified-classes/routing_manifest.json`
- class plans ใต้ `Result_Round1/all-854-modified-classes/plans/`

ทุก modified Java class instance ต้องปรากฏหนึ่งครั้งใน `audit_manifest.json` ส่วน `routing_manifest.json` ส่งต่อ `NEEDS_ADAPTER`, `NEEDS_ENTRY_POINT`, `NOT_PAIRWISE_APPLICABLE`, `DATA_ERROR` และ `ANALYSIS_ERROR` โดยไม่สร้าง JUnit ปลอม

### 4. Canary ก่อน full generation

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && PYTHONPATH=Code python3 Code/runner/all_class_pipeline.py --mode canary --resume --summary-only'
```

Canary เป็น deterministic union ของ class แรกต่อ project, source-presence state และ adapter family ห้ามเริ่ม full generation หาก canary ยังมี `GENERATION_OR_VERIFICATION_ERROR`

### 5. Full generation ทีละ project

```powershell
$projects = @('Chart','Cli','Closure','Codec','Collections','Compress','Csv','Gson','JacksonCore','JacksonDatabind','JacksonXml','Jsoup','JxPath','Lang','Math','Mockito','Time')
foreach ($project in $projects) {
  docker exec sqa-defects4j sh -lc "cd /workspace/Combinatorial_IPO && PYTHONPATH=Code python3 Code/runner/all_class_pipeline.py --mode generate --project $project --resume --summary-only"
  if ($LASTEXITCODE -ne 0) { throw "IPO generation stopped at $project" }
}
```

จำกัดการแก้ปัญหาเฉพาะ Bug ID ได้ด้วย `--project Lang --bug 1`; ใช้ `--resume` ทุกครั้งที่รันต่อ งานที่ hashes และ suite checksum ตรงจะไม่ถูกทำซ้ำ

### 6. Validate publication

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && PYTHONPATH=Code python3 Code/runner/all_class_pipeline.py --mode validate --summary-only'
```

Member 4 ต้องอ่านเฉพาะ `TestCode/all-modified-classes/verified_suites_manifest.json` ซึ่งมีเพียง `FIXED_VERIFIED`, `generation_backend=native_ipo` และ pair coverage 100% ห้าม fallback ไป PICT, Round1 หรือ scan Java files ที่ไม่มี manifest entry

หากคำสั่งล้มเหลว ให้ส่งกลับมาเฉพาะ JSON summary และ `class_record.json` ของ class ที่ผิดพลาด ไม่ต้องส่ง Docker log ทั้งหมด สถานะ `NEEDS_*` และ `NOT_PAIRWISE_APPLICABLE` เป็น routing outcome ไม่ใช่ automation crash

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
  -> atomic TestCode publication after every target selected by the experiment passes
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

ขอบเขตของรอบนี้ถูกล็อกแยกจาก engine ใน `Configuration/experiments/round2-17-targets.json` ซึ่งระบุ target ทั้ง 17, expected metadata summary และ SHA-256 fingerprints ของ selected catalog metadata กับ scenario set การเพิ่ม target อื่นใน catalog หรือวาง scenario แบบ `DRAFT` ไว้ข้างกันจึงไม่เปลี่ยนผลของ experiment เดิม

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
├── Configuration/
│   ├── experiments/           # Explicit, reproducible target sets
│   └── targets/               # One defect-focused scenario spec per bug
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

คำสั่ง readiness จะสำเร็จเมื่อทั้ง 4 representatives ผ่านเดิม, feasibility audit ตรงกับ experiment manifest และทุก target ที่ experiment เลือกมี approved scenario ที่ fingerprint ตรงกัน โดยจะบล็อกเฉพาะขณะที่ run เดิมมีสถานะ `STARTED`/`PREFLIGHT`; run ที่จบเป็น `VERIFIED` แล้วสามารถกด Run ซ้ำเพื่อพิสูจน์ reproducibility ได้ ตัวเลข 17/22/56 เป็นคุณสมบัติของ manifest รอบนี้ ไม่ได้ฝังเป็นข้อจำกัดของ IPO engine

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
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/scenario_catalog.py --catalog /workspace/target_benchmark/catalog_17_projects.json --scenarios /workspace/Combinatorial_IPO/Configuration/targets --experiment /workspace/Combinatorial_IPO/Configuration/experiments/round2-17-targets.json --output-root /workspace/Combinatorial_IPO'
```

entry point จะส่ง `--experiment /workspace/Combinatorial_IPO/Configuration/experiments/round2-17-targets.json` ให้ runner ด้วย เพื่อให้การรันซ้ำเลือก target และ input definitions ชุดเดิมเสมอ

คำสั่งนี้ไม่ auto-scan ทุก method แต่ประมวลผลเฉพาะ approved scenarios ที่ experiment เลือกและโยงกับ patch/triggering tests เก็บ experiment snapshot, fingerprints, scenario snapshot, domains, constraints, combinations, materialized inputs, oracle metadata, JUnit และ manifest ใต้ `Result_Round2/<run-id>/` จากนั้นเผยแพร่ `TestCode/` เมื่อ fixed verification ผ่านครบทุก target ที่เลือกเท่านั้น

## Extend with another bug target

ตัว engine ไม่จำกัดจำนวน target หากเพิ่ม `Lang_2b` ให้เก็บ benchmark metadata ก่อน เพิ่ม `Configuration/targets/Lang_2b.json` เป็น `DRAFT`, วิเคราะห์ patch/triggering test และเปลี่ยนเป็น `APPROVED` เมื่อ fixed preflight ผ่าน จากนั้นสร้าง experiment ใหม่แทนการแก้ manifest ของรอบเดิม:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/create_experiment.py --experiment-id extended-18-targets --catalog /workspace/target_benchmark/catalog_17_projects.json --scenarios Configuration/targets --target Chart_1b --target Cli_1b --target Lang_1b --target Lang_2b --output Configuration/experiments/extended-18-targets.json'
```

ตัวอย่างย่อด้านบนต้องใส่ `--target` ให้ครบทุก target ที่ต้องการจริง เครื่องมือจะคำนวณจำนวน modified sources, triggering tests และ fingerprints โดยอัตโนมัติ Target/scenario อื่นที่ไม่ได้อยู่ใน manifest สามารถอยู่ใน repository ได้แต่จะไม่ถูกรัน หาก selected input ถูกแก้หลังสร้าง manifest readiness จะหยุดจนกว่าจะ review และสร้าง manifest รุ่นใหม่อย่างตั้งใจ

Member 1 ไม่รัน buggy version, coverage หรือ Fault Detection Rate ในขั้นนี้ งานดังกล่าวเป็นความรับผิดชอบของ Member 4

## Current limitations

- Scenario-driven runner รองรับจำนวน target ตาม experiment manifest แต่ repository ปัจจุบันมี approved scenarios จริง 17 targets และไม่ได้อ้างว่าใช้ได้กับ Defects4J ทุก bug ID
- Target ใหม่ต้องมี spec ที่อ้าง modified source, source patch และ triggering test; runner จะไม่ย้อนกลับไป auto-scan เมธอดเพื่อเดา scenario
- Java snippets ใน spec เป็น trusted configuration ที่ผ่าน fixed compile/run ไม่ใช่ input จากผู้ใช้ภายนอก
- `null` ใช้ได้เฉพาะเมื่อเป็น input ตามสัญญาและมีเหตุผลจาก defect เช่น null comparator ของ Collections; unknown object type ยังห้าม fallback เป็น `null`
- Chart รุ่นเก่าต้องใช้ `svn`; Dockerfile ติดตั้ง `subversion` เพื่อให้ rebuild แล้วทำซ้ำได้
- Member 1 ยังไม่รัน buggy version, coverage หรือ Fault Detection Rate
