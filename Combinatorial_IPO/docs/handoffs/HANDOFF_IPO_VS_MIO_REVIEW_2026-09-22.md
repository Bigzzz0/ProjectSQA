# Handoff: Native IPO Coverage Gap Review vs MIO/EvoSuite

วันที่ตรวจ: 2026-09-22  
Repository: `ProjectSQA`  
Branch: `main`  
HEAD หลัง `git pull --rebase origin main`: `f990ce0f`

## 1. วัตถุประสงค์ของ handoff นี้

เอกสารนี้บันทึกสถานะ Native IPO หลัง merge งานล่าสุด และวิเคราะห์ว่าเหตุใด MIO/EvoSuite จึงสร้างไฟล์ทดสอบได้เกือบทุก bug/class ขณะที่ Native IPO publish ได้เพียงบาง class เพื่อใช้หารือแนวทางแก้ไขรอบถัดไป

รอบนี้เป็นการตรวจและวิเคราะห์เท่านั้น ยังไม่ได้เปลี่ยนนโยบายของ Native IPO หรือแก้ generation pipeline เพิ่ม

## 2. สถานะ Git ล่าสุด

- `git pull --rebase origin main` สำเร็จแบบ fast-forward จาก `2bf1761b` ไป `f990ce0f`
- Commit Native IPO อยู่ในประวัติแล้วที่ `2bf1761b Implement native IPO all-class automation`
- Remote update ล่าสุดเพิ่มผล MIO/EvoSuite จำนวนมาก โดย commit ปลายทางคือ `f990ce0f`
- Worktree สะอาดก่อนสร้าง handoff นี้

## 3. สถานะ Native IPO ปัจจุบัน

ข้อมูลจาก:

- `Result_Round1/all-854-modified-classes/audit_manifest.json`
- `Result_Round2/all-854-modified-classes/generation_manifest.json`
- `TestCode/all-modified-classes/verified_suites_manifest.json`

### Audit accounting

| สถานะ | จำนวน class instances |
|---|---:|
| `AUTO_READY` | 48 |
| `NEEDS_ADAPTER` | 551 |
| `NEEDS_ENTRY_POINT` | 440 |
| `NOT_PAIRWISE_APPLICABLE` | 27 |
| `DATA_ERROR` | 3 |
| `ANALYSIS_ERROR` | 1 |
| รวม | 1,070 |

### Generation accounting

| สถานะ | จำนวน class instances |
|---|---:|
| `FIXED_VERIFIED` | 46 |
| `GENERATION_OR_VERIFICATION_ERROR` | 2 |
| `SKIPPED_NOT_READY` | 1,022 |
| รวม | 1,070 |

ผล validate ล่าสุดของ `verified_suites_manifest.json` คือ `valid: true`, 46 suites และทุก method model ที่ publish มี pair coverage 100%

Generation errors ที่เหลือ:

1. `Math-92 / org.apache.commons.math.util.MathUtils`: oracle Java process เปิดไม่สำเร็จ
2. `Math-99 / org.apache.commons.math.util.MathUtils`: fixed Defects4J checkout compile test sources ไม่ผ่าน

## 4. สถานะ MIO/EvoSuite ล่าสุด

จากไฟล์จริงใต้ `MIO_Algorithm/TestCode`:

- 860 ไฟล์ `*_ESTest.java`
- ครอบคลุม 706 bug directories จาก 854 bugs หรือ 82.7%
- พบประมาณ 32,582 methods ที่มี `@Test`

เทียบกับ Native IPO:

- 46 ไฟล์ `*_IPOTest.java`
- ครอบคลุม 46 bug/class instances
- พบ 3,261 methods ที่มี `@Test`

ตัวเลข `@Test`, null และ exception ด้านบนเป็น static text count เพื่ออธิบายลักษณะ suite ไม่ใช่ผล runtime coverage

## 5. เหตุผลหลักที่ผลต่างกันมาก

### 5.1 หน่วยเป้าหมายต่างกัน

MIO ส่ง `-class <target_class>` ให้ EvoSuite แล้วค้นหา test sequence สำหรับ bytecode ทั้งคลาสตาม LINE/BRANCH goals ไม่ได้บังคับว่าต้องระบุ defect-related method ก่อน

Native IPO ปัจจุบันเริ่มจาก defect evidence แล้วเลือกเฉพาะ callable ที่สัมพันธ์กับ changed diff hunk หรือ triggering-test call evidence จึงไม่ scan/generate ทุก public method โดยไม่มีหลักฐาน

ผลคือ MIO สามารถสร้าง suite ให้ class แม้ไม่ครอบคลุมทุก method ส่วน IPO จะหยุดเป็น `NEEDS_ENTRY_POINT` จำนวน 440 class หากยังชี้ defect-related callable ไม่ได้

### 5.2 MIO สร้าง call sequence และ object graph ได้ แต่ IPO ยังเป็น direct-call model

EvoSuite ใช้ search, reflection, concrete subclasses, Mockito/shaded runtime, sandbox scaffolding และ sequence ของ constructor/method calls เพื่อสร้าง state ก่อนเรียก method เป้าหมาย

ตัวอย่าง `Chart_1b/AbstractCategoryItemRenderer_ESTest.java` ไม่ instantiate abstract class โดยตรง แต่ใช้ concrete renderers เช่น `StackedBarRenderer`, `LevelRenderer` และ object graph จำนวนมาก

Native IPO มี receiver/constructor factories แบบ deterministic แต่ยังไม่มี generic sequence planner, subclass discovery, builder/factory discovery, dependency graph construction หรือ mock synthesis จึงมี `NEEDS_ADAPTER` 551 class

### 5.3 นโยบาย input ต่างกัน

แผน Native IPO ห้าม:

- `null`
- dummy values
- no-crash assertions
- unstable observers

MIO generated tests ใช้สิ่งเหล่านี้ได้ตามธรรมชาติของ EvoSuite จาก static scan พบประมาณ 14,254 บรรทัดที่เกี่ยวกับ null และ 7,972 `fail("Expecting exception...")` calls นี่เป็นเหตุผลสำคัญที่ MIO สำรวจ path ได้กว้างกว่า แต่ไม่ควรนำตัวเลข suite มาเทียบกับ IPO โดยถือว่ามาตรฐาน input/oracle เหมือนกัน

### 5.4 Acceptance gate ต่างกัน

MIO `batch_evosuite.py` กำหนด run success จาก:

```python
metrics["success"] = (code == 0 and has_stat)
```

จากนั้น copy `*_ESTest.java` และ scaffolding ไป `TestCode` แต่ไม่มีขั้นตอนใน batch runner ที่ compile และรัน generated suite ทุกไฟล์บน fixed checkout ก่อนนับว่า publish สำเร็จ

Native IPO จะ publish ต่อเมื่อ:

1. defect evidence ผ่าน
2. model มี factors อย่างน้อยสองตัว
3. Native IPO pair coverage = 100%
4. เก็บ fixed oracle สำเร็จ
5. generated JUnit compile และ run บน fixed version ผ่าน
6. suite/artifact hashes ตรง

ดังนั้น MIO “มีไฟล์ test” หรือ “EvoSuite run success” ยังไม่เท่ากับ IPO `FIXED_VERIFIED`

### 5.5 Oracle philosophy ต่างกัน

MIO optimize structural goals และ EvoSuite สร้าง assertions จาก observed execution ของ version ที่ใช้ generate ซึ่งใน batch ปัจจุบัน checkout `<bug_id>b`

Native IPO generate inputs แบบ pairwise แล้วเก็บ expected outcomes จาก `<bug_id>f` โดยตรง จึงตั้งใจเป็น fixed-version characterization oracle และตรวจ suite บน fixed versionก่อน publish

## 6. ข้อสรุปสำคัญ

ความต่าง 860 ต่อ 46 ไม่ได้แปลว่า IPO algorithm สร้าง test ไม่ได้ในอัตราเดียวกับ MIO โดยตรง แต่เกิดจาก pipeline scope และ acceptance contract ต่างกันมาก:

- MIO: class-level, search-based, stateful sequences, permissive inputs, success เมื่อ search/statistics สำเร็จ
- IPO: defect-callable-level, deterministic pairwise, direct invocation, strict input policy, fixed oracle และ fixed runtime verification

อย่างไรก็ตาม Native IPO ยัง conservative เกินไปในสองจุดจริง:

1. `NEEDS_ENTRY_POINT 440` สูง เพราะ evidence resolver ยังหา call chain/overload/indirect trigger ไม่พอ
2. `NEEDS_ADAPTER 551` สูง เพราะ receiver/value construction ยังรองรับ object graph น้อย

จุดเหล่านี้ควรเป็นเป้าหมายของการแก้ ไม่ควรแก้โดยลด verification gate หรือประกาศ Java file ที่ยังไม่ได้ run ว่า verified

## 7. ตัวเลือกสำหรับหารือรอบถัดไป

### Option A: รักษา strict IPO และขยาย adapter/evidence ทีละ family (แนะนำ)

- เพิ่ม factory-method discovery เช่น `getInstance`, `of`, `create`, `builder`
- เพิ่ม concrete subtype discovery สำหรับ abstract/interface receivers
- เพิ่ม deterministic setup sequence 1–3 steps ก่อน invocation
- เพิ่ม adapters ตาม backlog frequency ไม่เขียนเฉพาะ class รายตัว
- เพิ่ม trigger-call analysis จาก test source, inheritance และ delegated calls
- คง no-null, fixed oracle, pair coverage และ fixed verification เดิม

ข้อดี: ยังเปรียบเทียบในฐานะ Native IPO ที่ตรวจสอบได้จริง  
ข้อเสีย: coverage class จะเพิ่มทีละช่วง ไม่พุ่งถึงระดับ EvoSuite ทันที

### Option B: เพิ่ม `CLASS_SMOKE_PAIRWISE` route แยกจาก defect-focused IPO

- หากหา defect entry point ไม่ได้ ให้เลือก callable ที่สร้าง model ได้จาก class เดียวกัน
- ต้องติด label ชัดว่า `CLASS_SMOKE_PAIRWISE` ไม่ใช่ `DEFECT_FOCUSED_IPO`
- ห้ามนำมารวมตัวเลขเดียวกันในรายงานโดยไม่แยกประเภท

ข้อดี: ได้ JUnit ต่อ class มากขึ้น  
ข้อเสีย: ลดความสัมพันธ์กับ defect และอาจสร้าง suite ที่ไม่แตะ changed behavior

### Option C: อนุญาต deterministic null/exception partitions แบบมีเงื่อนไข

- null เป็น explicit factor value เฉพาะเมื่อ signature/annotation/guard evidence รองรับ
- exception oracle ต้อง assert exact type และ deterministic message policy
- ห้าม no-crash-only test

ข้อดี: เปิด path เพิ่มโดยยังควบคุม semantics  
ข้อเสีย: เปลี่ยนสมมติฐานเดิมของโครงการ ต้องอนุมัติก่อน implement

### Option D: ใช้ EvoSuite ช่วยสร้าง receiver/setup แต่ IPO ยังเป็นผู้สร้าง parameter combinations

- ใช้ generated sequence เป็น fixture factory เท่านั้น
- Native IPO ยังคำนวณ 2-way combinations ของ defect callable
- fixed oracle และ verification ยังเป็น gate ของ IPO

ข้อดี: แก้ object graph ได้เร็วที่สุด  
ข้อเสีย: ไม่ใช่ Native IPO ล้วน ต้องรายงานเป็น hybrid และเพิ่ม dependency ต่อ EvoSuite runtime

## 8. ข้อเสนอสำหรับรอบถัดไป

ก่อน implement ให้ตกลงสองคำถาม:

1. เป้าหมายหลักคือ “ทุก modified class ต้องมีไฟล์ JUnit” หรือ “ทุก published suite ต้อง defect-focused และ fixed-verified”
2. ยอมเปลี่ยนนโยบาย no-null/no-dummy หรือไม่

หากยังยืนยัน strict Native IPO แนะนำเริ่ม Option A ด้วย audit backlog ranking แล้วทำ canary 4 families:

1. abstract/interface + concrete subtype
2. static factory/builder receiver
3. common JDK/domain value objects
4. short deterministic setup sequences

Acceptance ของรอบต่อไปควรวัดทั้ง:

- จำนวน class instances ที่ย้ายจาก `NEEDS_ADAPTER`/`NEEDS_ENTRY_POINT` ไป `AUTO_READY`
- จำนวน `FIXED_VERIFIED`
- pair coverage 100%
- fixed compile/run pass
- แยก defect-focused กับ smoke-only อย่างชัดเจน

## 9. คำสั่งที่ใช้ตรวจรอบนี้

```powershell
git pull --rebase origin main

# นับ generated test classes และ bug directories
Get-ChildItem MIO_Algorithm\TestCode -Recurse -Filter '*_ESTest.java'
Get-ChildItem Combinatorial_IPO\TestCode\all-modified-classes -Recurse -Filter '*_IPOTest.java'

# อ่าน accounting ปัจจุบัน
Get-Content -Raw Combinatorial_IPO\Result_Round1\all-854-modified-classes\audit_manifest.json | ConvertFrom-Json
Get-Content -Raw Combinatorial_IPO\Result_Round2\all-854-modified-classes\generation_manifest.json | ConvertFrom-Json
```

## 10. ขอบเขตที่ยังไม่ควรทำจนกว่าจะหารือ

- ไม่ลบหรือแก้ MIO artifacts
- ไม่ลด fixed verification gate
- ไม่รวม MIO success count กับ IPO verified count โดยตรง
- ไม่เปิด null/dummy/no-crash policy โดยไม่มีการอนุมัติ
- ไม่เริ่ม all-class rerun รอบใหม่จนกว่าจะเลือก Option A/B/C/D หรือรูปแบบผสม
