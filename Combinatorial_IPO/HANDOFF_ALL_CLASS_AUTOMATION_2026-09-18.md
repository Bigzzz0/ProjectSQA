# Handoff: Automated Native IPO for All Modified Classes

วันที่ส่งต่อ: 2026-09-18  
Repository: `ProjectSQA`  
Branch: `main`  
HEAD ขณะเขียน: `b59a334`  
สถานะ: งานพัฒนาค้างอยู่ใน working tree, ยังไม่ commit และยังไม่เริ่ม full loop

## 1. เป้าหมายที่กำลังพัฒนา

ขยายงาน Member 1 จาก 17 representative bug targets ไปสู่การตรวจทุก active Bug ID และทุก modified Java class โดยใช้ Native IPO 2-way โดยมีหลักการสำคัญดังนี้:

- ทุก class instance ต้องถูกตรวจและมีสถานะ แม้ยังสร้าง JUnit ไม่ได้
- Native IPO รับประกัน pair coverage ของ factor model เท่านั้น ไม่ได้รับประกันว่าเข้าใจ semantics ของทุก Java API
- ห้ามใช้ `null`, dummy input หรือ assertion แบบ “ไม่ crash ก็ผ่าน” เพื่อทำให้จำนวน test ครบ
- JUnit จะเผยแพร่ได้เมื่อมี meaningful factors, pair coverage ครบ, fixed oracle, compile และ fixed-version verification ผ่านแล้ว
- Member 1 ไม่รัน buggy version, coverage หรือ FDR ซึ่งยังเป็นงาน Member 4

## 2. ขอบเขตหน้าที่ที่ต้องรักษาในแผนใหม่

จาก `TEAM_WORKFLOW_GUIDE.md`:

- `target_benchmark/` เป็น Source Code และ Ground Truth กลางของทีม
- Member 4 เป็นเจ้าของ master catalog, extraction infrastructure และ benchmark runner
- Member 1 อ่านข้อมูลจาก `target_benchmark/` เพื่อสร้าง model, combinations และ JUnit
- สมาชิกสามารถเรียกเครื่องมือกลางเพื่อดึง source ที่ขาดได้ แต่ Member 1 ไม่ควรเปลี่ยน schema, layout หรือ overwrite Ground Truth กลางโดยไม่ตกลงกับ Member 4

สถาปัตยกรรมที่ควรใช้ต่อ:

```text
target_benchmark/ (read-only input)
        ↓
IPO-local normalizer / snapshot
        ↓
Combinatorial_IPO/Configuration/
        ↓
Class Planner → Adapter → Native IPO → Pair Verifier
        ↓
fixed oracle → temporary JUnit → fixed verification
        ↓
Result_Round1 / Result_Round2 / TestCode
```

หากต้องใช้ buggy/fixed source เพิ่ม ให้ checkout ใน temporary directory ระหว่างทำงาน ไม่ควรเปลี่ยน layout ของ `target_benchmark/` เพื่อรองรับ IPO เพียงสมาชิกเดียว

## 3. สิ่งที่พัฒนาแล้วใน working tree

### IPO-local implementation

- ขยาย lightweight Java parser ให้เห็น top-level class/interface/enum, constructor, method visibility, static และ instance method
- เพิ่ม class-level planner ซึ่งจำแนก `AUTO_READY`, `NEEDS_ADAPTER`, `NOT_PAIRWISE_APPLICABLE`, `DATA_ERROR` และ `ANALYSIS_ERROR`
- เพิ่ม adapter registry เบื้องต้นสำหรับ primitive/String, array, Reader/InputStream และ collection/iterator บางชนิด
- แยก constructor ที่ยังไม่มี stable observer ไปเป็น `NEEDS_ADAPTER` แทนการสร้าง assertion ที่อ่อนเกินไป
- ปรับ catalog resolver ให้รองรับทั้ง legacy flat source และ package-relative source หากมีอยู่
- เพิ่ม all-class experiment manifest schema และ resumable audit runner
- เพิ่ม tests สำหรับ catalog normalization, package-relative source, constructor/instance parsing และ class planning

### หลักฐานทดสอบล่าสุด

Python regression tests ผ่านทั้งหมด:

```text
Ran 83 tests
OK
```

คำสั่งที่ใช้บน host:

```powershell
$py = 'C:\Users\User\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$code = 'D:\673380278-9\2569\CP353201-SQA\Miniproject\ProjectSQA\Combinatorial_IPO\Code'
$env:PYTHONPATH = $code
& $py -m unittest discover -s "$code\tests" -p 'test_*.py' -v
```

### ข้อมูลที่ตรวจพบจาก master catalog เดิม

การ normalize ทดลองพบ:

- 854 active bug targets
- 1,070 modified Java class instances
- 3 modified resources ซึ่งไม่ควรถูกนับเป็น Java classes
- 1,779 triggering-test identifiers หลังตัด stack traces ออก

Audit snapshot ก่อน classifier adjustment ครั้งสุดท้ายพบ:

```text
AUTO_READY                    298 classes
NEEDS_ADAPTER                676 classes
NOT_PAIRWISE_APPLICABLE       69 classes
DATA_ERROR                    26 classes
ANALYSIS_ERROR                 1 class
Total                      1,070 classes
```

ตัวเลขนี้เป็น snapshot เพื่อสำรวจเท่านั้น ต้องรันใหม่หลังแก้ขอบเขตและ classifier แล้ว ห้ามใช้เป็นผลการทดลองสุดท้าย

## 4. การแก้ที่เกินขอบเขตและยังไม่ควร commit

ระหว่างพัฒนาได้แก้ shared infrastructure และ Ground Truth โดยตรง ซึ่งขัดกับ boundary ที่ล็อกไว้:

- `scripts/batch_extract_all_bugs.py`
- `scripts/extract_target_classes.py`
- `target_benchmark/all_bugs_catalog.json`
- `scripts/normalize_all_bugs_catalog.py` ถูกวางใน shared `scripts/` แม้เป็นความต้องการเฉพาะ IPO

นอกจากนี้ `Combinatorial_IPO/Configuration/experiments/all-854-modified-classes.json` ถูกสร้างจาก catalog ที่ถูก normalize ทับแล้ว fingerprint จึงต้องสร้างใหม่หลังย้าย normalization มาอยู่ฝั่ง IPO

ข้อเสนอสำหรับแผนใหม่:

1. คืน shared scripts และ master catalog เป็น version ของทีม
2. ย้าย normalizer เข้า `Combinatorial_IPO/Code/runner/`
3. ให้ normalizer อ่าน master catalog โดยไม่เขียนกลับ แล้วสร้าง IPO-local snapshot ใต้ `Combinatorial_IPO/Configuration/`
4. สร้าง experiment manifest ใหม่จาก local snapshot
5. ใช้ temporary Defects4J checkout เพื่อแก้ missing/added-fixed source โดยไม่เขียน source layout ใหม่ลง `target_benchmark/`

อย่าใช้ `git reset --hard` หรือ restore ทั้ง repository เพราะ working tree มีงานที่ต้องเก็บ ให้เลือกคืนเฉพาะสาม shared files ที่ระบุหลังตรวจ diff แล้วเท่านั้น

## 5. Working tree ขณะส่งต่อ

ไฟล์ IPO ที่ควร review และอาจเก็บต่อ:

```text
Combinatorial_IPO/Code/analyzer/java_parser.py
Combinatorial_IPO/Code/analyzer/class_planner.py
Combinatorial_IPO/Code/domain/adapter_registry.py
Combinatorial_IPO/Code/runner/catalog.py
Combinatorial_IPO/Code/runner/feasibility_audit.py
Combinatorial_IPO/Code/runner/generate_catalog.py
Combinatorial_IPO/Code/runner/all_class_experiment.py
Combinatorial_IPO/Code/runner/run_all_class_audit.py
Combinatorial_IPO/Code/tests/test_catalog.py
Combinatorial_IPO/Code/tests/test_catalog_normalization.py
Combinatorial_IPO/Code/tests/test_class_planner.py
Combinatorial_IPO/Code/tests/test_feasibility_audit.py
Combinatorial_IPO/Code/tests/test_java_parser.py
```

ไฟล์ที่ต้องย้าย คืน หรือ regenerate ตามขอบเขตใหม่:

```text
scripts/batch_extract_all_bugs.py
scripts/extract_target_classes.py
scripts/normalize_all_bugs_catalog.py
target_benchmark/all_bugs_catalog.json
Combinatorial_IPO/Configuration/experiments/all-854-modified-classes.json
```

ระหว่างพักมี upstream updates เข้ามาและ HEAD ปัจจุบันเปลี่ยนเป็น `b59a334`; ก่อนแก้ต่อให้ตรวจ diff เทียบ HEAD นี้อีกครั้งและห้าม overwrite งาน Member 2/3/4

## 6. สิ่งที่ยังไม่ได้ทำ

- ยังไม่ได้ออกแบบ IPO-local normalized catalog path ที่ลงตัว
- ยังไม่ได้ย้าย source checkout ให้เป็น temporary-only flow
- ยังไม่ได้รัน audit ใหม่จาก clean shared master catalog
- ยังไม่ได้เชื่อม class planner เข้ากับการสร้าง combinations สำหรับ all-class run อย่างสมบูรณ์
- ยังไม่ได้เพิ่ม generic instance receiver เข้า fixed oracle/JUnit generator
- constructor, stateful API และ complex object ยังต้องมี observer/factory adapters
- ยังไม่ได้ทำ atomic class-level publication และ resume สำหรับ generation/verification จริง
- ยังไม่ได้อัปเดต `Combinatorial_IPO/README.md` ให้แยก 17-target baseline ออกจาก all-class audit
- ยังไม่ได้ทำ Docker preflight หรือ full fixed-version verification
- ยังไม่มี JUnit ใหม่จาก all-class workflow
- ยังไม่ได้ commit งานชุดนี้

## 7. เกณฑ์ที่แผนใหม่ต้องตอบให้ชัด

1. Source of truth ยังคงเป็น master catalog ของ Member 4 และ Member 1 จะสร้าง derived snapshot ที่ใด
2. หน่วยนับคือ modified Java class instance ต่อ Bug ID ไม่ใช่ unique FQCN และไม่รวม resource
3. วิธีเลือก defect-related public entry point จาก diff/triggering tests โดยไม่ scan ทุก public method แบบไร้เป้าหมาย
4. สถานะใดถือว่าเสร็จ และ `NOT_PAIRWISE_APPLICABLE` ต้องส่งต่อวิธีทดสอบทางเลือกอย่างไร
5. Adapter families ใดคุ้มค่าที่สุดจาก backlog จริง
6. ขั้นใดเป็น audit-only, model generation, oracle collection, fixed verification และ publication
7. การ resume ใช้ input hash อะไรและ invalidate เมื่อ catalog/parser/adapter เปลี่ยนอย่างไร
8. Member 4 จะค้นเฉพาะ verified IPO suites จาก manifest ใด โดยไม่ fallback ไป PICT หรือ Round1

## 8. ข้อควรระวังสำหรับผู้รับช่วง

- อย่ารัน `extract_target_classes.py --force` หรือ `--all` ตามคำสั่งก่อนหน้า จนกว่าจะตกลง ownership ของ `target_benchmark/`
- อย่ารายงาน 100% pair coverage ว่าหมายถึงครอบคลุม semantics หรือ defects ทุก class
- อย่านับ `NEEDS_ADAPTER`/`UNSUPPORTED` เป็นความสำเร็จ
- อย่าสร้าง JUnit เพียงเพื่อให้มีไฟล์ครบจำนวน
- อย่าเริ่ม full 854-bug loop ก่อน catalog accounting, audit manifest และ representative regression ผ่าน
- PICT เป็น reference baseline เท่านั้น ผล all-class ต้องมาจาก Native IPO

