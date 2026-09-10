# Combinatorial Testing: Native IPO 2-Way Pipeline

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล)

คู่มือแม่บทของทีมอยู่ที่ [`TEAM_WORKFLOW_GUIDE.md`](../TEAM_WORKFLOW_GUIDE.md) ส่วนนี้อธิบายการใช้งาน implementation ปัจจุบันของ Member 1

## Academic distinction

- **IPO (In-Parameter-Order)** คือ algorithm ที่โครงการ implement เองใน `Code/algorithm/ipo.py` โดยมี initial construction, Horizontal Growth และ Vertical Growth
- **Microsoft PICT** เป็นเครื่องมือแยกต่างหากที่เก็บไว้ใน `Code/backends/pict_backend.py` สำหรับ reference baseline เท่านั้น
- ผลจาก PICT ห้ามรายงานว่าเป็นผลจาก IPO แม้ทั้งสองชุดจะมี pair coverage ครบหรือมีจำนวนแถวเท่ากัน

Main pipeline ไม่เรียก PICT และไม่ต้องติดตั้ง PICT:

```text
Java source
  -> exact method signature
  -> factor domains
  -> native IPO 2-way generation
  -> independent pair-coverage verification
  -> concrete Java inputs
  -> fixed-version oracle
  -> oracle-backed JUnit 4
```

## Current readiness status

ชุด `NumberUtils.createNumber(String)` จำนวน 48 cases ที่อยู่ใน repository ก่อน native IPO implementation เป็น **PICT pilot baseline** ซึ่งผ่านการเก็บ oracle และ fixed-version verification แล้ว และถูกเก็บแยกไว้ใต้ `baselines/pict/Lang_1b/` ห้ามนำชุดนี้ไปรายงานเป็นผล IPO

Native IPO สร้าง abstract combinations 48 แถว ครอบคลุม 194/194 pairs รักษา mandatory seed เก็บ oracle ใหม่ครบ 48 outcomes และผ่าน JUnit 4 บน `Lang-1f` ครบ `OK (48 tests)` แล้ว ชุดส่งมอบอยู่ใน `TestCode/Lang_1b/`

Readiness representatives ผ่าน fixed-version verification แล้ว 4/4 methods ครอบคลุม primitive หลาย parameters, String, floating point, boolean และสอง Defects4J projects:

- `NumberUtils.createNumber(String)`: 48 tests, 194/194 pairs
- `NumberUtils.min(int,int,int)`: 28 tests, 75/75 pairs
- `NumberInput.parseAsDouble(String,double)`: 60 tests, 60/60 pairs
- `NumberInput.inLongRange(String,boolean)`: 24 tests, 24/24 pairs

การตรวจ catalog แบบไม่ generate พบ 13 targets ที่อ่าน source ได้ และ catalog mismatch 4 targets ซึ่งระบบบันทึกตามจริงโดยไม่เดา source ทดแทน รายละเอียดอยู่ใน `Result_Round1/feasibility_audit.json` และผล readiness ล่าสุดอยู่ใน `Result_Round1/readiness_report.json` ทั้งสองไฟล์ยืนยันว่า `generation_performed`/`loop_started` เป็น `false` โดยเมื่อเริ่ม catalog batch ระบบจะสร้าง `Result_Round1/catalog_loop_state.json` ทันทีเพื่อไม่ให้ readiness รายงานสถานะจากค่าคงที่

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
├── Result_Round1/             # Native IPO combinations, inputs and oracle
└── TestCode/                  # Fixed-verified JUnit suites for Member 4
```

PICT pilot artifacts are archived under `baselines/pict/`. ตำแหน่ง `Models/`, `Result_Round1/` และ `TestCode/` สงวนไว้สำหรับผลจาก native IPO เท่านั้น

## Run tests

จาก PowerShell ที่ repository root:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO/Code && python3 -m unittest discover -s tests -p "test_*.py" -v'
```

Tests ไม่เรียก PICT executable และใช้ temporary directories สำหรับ runner integration

## Audit and readiness checks

ตรวจ 17-target catalog โดยไม่สร้าง combinations, oracle หรือ JUnit:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/feasibility_audit.py --catalog /workspace/target_benchmark/catalog_17_projects.json --target-root /workspace/target_benchmark --output Result_Round1/feasibility_audit.json --summary-only'
```

ตรวจ artifacts ของ representative methods และ delivery gates โดยไม่เริ่ม loop:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/readiness_check.py'
```

คำสั่ง readiness จะสำเร็จเมื่อทั้ง 4 representatives ผ่าน pair coverage, oracle alignment, timeout/class naming, fixed-version verification และ feasibility audit ครบตาม configuration ใน `Configuration/readiness_representatives.json`

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

ให้ผู้ควบคุมการทดลองเป็นผู้สั่งคำสั่งนี้เองหลังตรวจ `readiness_check.py` ผ่าน:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 Code/runner/run_ipo_batch.py --target-root /workspace/target_benchmark --catalog /workspace/target_benchmark/catalog_17_projects.json --collect-oracles --verify-suites'
```

คำสั่งนี้จะประมวลผลเฉพาะ source ที่ระบุใน catalog, เก็บ fixed-version oracle และเผยแพร่เฉพาะ suite ที่ verify ผ่าน ทั้ง 4 catalog mismatches จะถูกรายงานและข้ามอย่างปลอดภัยแทนการเลือกคลาสอื่นโดยอัตโนมัติ

Member 1 ไม่รัน buggy version, coverage หรือ Fault Detection Rate ในขั้นนี้ งานดังกล่าวเป็นความรับผิดชอบของ Member 4

## Current limitations

- ยืนยัน native IPO end-to-end แล้ว 4 representative methods ตามหัวข้อ Current readiness status; ยังไม่ได้อ้างว่ารองรับทุก signature ใน Defects4J
- รองรับหลัก ๆ เฉพาะ public static methods ที่มี parameters
- parser เป็น lightweight regex analyzer
- constructor, instance method, collection, complex object และ constraints ยังไม่รองรับทั่วไป
- reference type ที่ไม่มี semantic model และ unsupported type ต้องถูก skip พร้อมสถานะ ห้ามแทนค่าเป็น `null` หรือสร้าง input โดยเดา
- catalog mismatch ต้องบันทึกและข้าม ห้ามเดา source file ทดแทน
