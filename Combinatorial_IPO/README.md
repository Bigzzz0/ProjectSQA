# Combinatorial Testing: Native IPO 2-Way Pipeline

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล)

คู่มือแม่บทของทีมอยู่ที่ [`TEAM_WORKFLOW_GUIDE.md`](../TEAM_WORKFLOW_GUIDE.md) ส่วนนี้อธิบายการใช้งาน implementation ปัจจุบันของ Member 1

## Academic distinction

- **IPO (In-Parameter-Order)** คือ algorithm ที่โครงการ implement เองใน `algorithm/ipo.py` โดยมี initial construction, Horizontal Growth และ Vertical Growth
- **Microsoft PICT** เป็นเครื่องมือแยกต่างหากที่เก็บไว้ใน `backends/pict_backend.py` สำหรับ reference baseline เท่านั้น
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

## Current Lang-1 status

ชุด `NumberUtils.createNumber(String)` จำนวน 48 cases ที่อยู่ใน repository ก่อน native IPO implementation เป็น **PICT pilot baseline** ซึ่งผ่านการเก็บ oracle และ fixed-version verification แล้ว ห้ามเขียนทับชุดนี้ระหว่างทดลอง

Native IPO สร้าง abstract combinations จาก factor model เดียวกันได้ 48 แถว ครอบคลุม 194/194 pairs และรักษา mandatory seed แต่ยังต้องเก็บ oracle ใหม่ตาม arguments ของ native IPO ก่อนใช้เป็น TestCode รอบสุดท้าย

## Project structure

```text
Combinatorial_IPO/
├── algorithm/                 # Native IPO implementation
├── analyzer/                  # Java source and signature analysis
├── backends/                  # Optional reference backends such as PICT
├── domain/                    # Generic domains and semantic overrides
├── generator/                 # Oracle-backed JUnit 4 synthesis
├── oracle/                    # Fixed-version oracle collection/verification
├── runner/                    # Safe filtered batch orchestration
├── verification/              # Backend-independent pair coverage
├── tests/                     # Python unit and integration tests
├── Models/                    # Native IPO factor-domain artifacts
├── Result_Round1/             # Native IPO combinations, inputs and oracle
└── TestCode/                  # Fixed-verified JUnit suites for Member 4
```

PICT pilot artifacts will be archived under `baselines/pict/` immediately before native IPO artifacts replace the legacy delivery paths

## Run tests

จาก PowerShell ที่ repository root:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 -m unittest discover -s tests -p "test_*.py" -v'
```

Tests ไม่เรียก PICT executable และใช้ temporary directories สำหรับ runner integration

## Safe native IPO trial

ระบุ project, bug และ exact signature พร้อมเขียนผลลงพื้นที่ทดลองเสมอ:

```powershell
docker exec sqa-defects4j sh -lc 'cd /workspace/Combinatorial_IPO && python3 runner/run_ipo_batch.py --target-root /workspace/target_benchmark --output-root /tmp/ipo-native-trial --project Lang --bug 1 --signature "createNumber(String)"'
```

ผลรอบแรกประกอบด้วย domains, abstract combinations, concrete inputs และ manifest เมื่อยังไม่มี oracle ที่ตรงกับ arguments ระบบจะรายงาน `oracle_status: MISSING` และไม่สร้างไฟล์ใน `TestCode/`

หากพบ oracle เก่าที่จำนวนหรือลำดับ arguments ไม่ตรง ระบบจะรายงาน `oracle_status: STALE` และไม่สร้าง JUnit

## Delivery gate

ไฟล์ `<Class>_IPOTest.java` จะสร้างได้เมื่อ:

1. native IPO generation สำเร็จ
2. independent pair coverage ครบ 100%
3. oracle มี case ID และ arguments ตรงทุก combination
4. generated JUnit ใช้ JUnit 4 และทุก test มี `@Test(timeout = 4000)`
5. suite compile และผ่านบน Defects4J fixed version ก่อนนำไปวางเป็นชุดส่งมอบ

Member 1 ไม่รัน buggy version, coverage หรือ Fault Detection Rate ในขั้นนี้ งานดังกล่าวเป็นความรับผิดชอบของ Member 4

## Current limitations

- ยืนยัน end-to-end เดิมแล้วเฉพาะ `NumberUtils.createNumber(String)`; native IPO oracle/fixed verification ยังเป็นขั้นถัดไป
- รองรับหลัก ๆ เฉพาะ public static methods ที่มี parameters
- parser เป็น lightweight regex analyzer
- constructor, instance method, collection, complex object และ constraints ยังไม่รองรับทั่วไป
- unsupported targets ต้องถูก skip พร้อมสถานะ ห้ามสร้าง input โดยเดา
- ห้ามเปิด all-target batch จนกว่า representative methods และ failure isolation จะผ่าน readiness gates ใน `TEAM_WORKFLOW_GUIDE.md`
