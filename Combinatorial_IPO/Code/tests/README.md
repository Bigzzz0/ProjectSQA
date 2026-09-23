# Native IPO Pipeline - Unit Test Suite

โฟลเดอร์นี้รวบรวม **Unit Tests ทั้งหมด (110 tests)** สำหรับตรวจสอบความถูกต้องของสถาปัตยกรรม Native IPO Pipeline ครอบคลุมตั้งแต่อัลกอริทึม In-Parameter-Order, Java AST Parser, Construction Planner ไปจนถึง Fixed-Version Oracle และ Validation Manifests

---

## 1. คำสั่งการรัน Unit Tests

### รันบน Windows Host (ผ่าน PowerShell):
```powershell
# จากรูทของโปรเจกต์
python -m unittest discover -s Combinatorial_IPO/Code/tests -t Combinatorial_IPO/Code

# หรือ cd เข้าไปที่โฟลเดอร์ Code
cd Combinatorial_IPO/Code
python -m unittest discover -s tests -t .
```

### รันบน Linux Docker Container (`sqa-defects4j`):
```powershell
docker --context default exec sqa-defects4j python3 -m unittest discover -s /workspace/Combinatorial_IPO/Code/tests -t /workspace/Combinatorial_IPO/Code
```

**ผลลัพธ์ที่ถูกต้อง:**
```text
Ran 110 tests in ~4.5s
OK
```

---

## 2. โครงสร้างและการแบ่งหน้าที่ของชุดทดสอบ (Test Map)

| ไฟล์ชุดทดสอบ | จำนวน Tests | หน้าที่และความรับผิดชอบ |
|---|:---:|---|
| **`test_ipo.py`** | 10 | ตรวจสอบอัลกอริทึม In-Parameter-Order (IPO 2-way), Initial Construction, Horizontal Growth, Vertical Growth |
| **`test_pair_coverage.py`** | 6 | ตัวตรวจทางคณิตศาสตร์อิสระ (Independent Coverage Verifier) ยืนยัน 100% Pair Coverage |
| **`test_pict_backend.py`** | 6 | การสอบเทียบผลลัพธ์ (Benchmarking) กับ Microsoft PICT Reference Baseline |
| **`test_java_parser.py`** | 8 | ตรวจสอบ Java Compiler Tree API AST Parser (`JavaAstParser.java`), Generics, Throws, Annotations |
| **`test_construction_planner.py`** | 5 | ตรวจสอบการวางแผนสร้าง Receiver Object (Zero-arg, Static factories, Concrete subtypes) |
| **`test_class_planner.py`** | 7 | ตรวจสอบการสกัด Candidate Methods, Factor Domains และ Class-Level Planning |
| **`test_fixed_version_oracle.py`** | 4 | ตรวจสอบการรัน Fixed Version Oracle, Sandbox Execution และการ Unwind Anonymous Inner Class |
| **`test_all_class_pipeline.py`** | 12 | ตรวจสอบการทำงานของ Pipeline (Preflight, Audit, Canary, Generation, Partial-Class Publication, Summary) |
| **`test_defect_evidence.py`** | 5 | ตรวจสอบการสกัดข้อมูล Diff Evidence และ Trigger Tests |
| **`test_catalog.py`** | 6 | ตรวจสอบการโหลด Catalog และ Normalization ของ 854 Bug IDs |
| **`test_catalog_normalization.py`** | 4 | ตรวจสอบความถูกต้องของ Checksum และ Format แคตตาล็อก |
| **`test_feasibility_audit.py`** | 4 | ตรวจสอบตรรกะการจัดกลุ่ม (AUTO_READY, NEEDS_ADAPTER, NEEDS_ENTRY_POINT, NOT_PAIRWISE_APPLICABLE) |
| **`test_run_ipo_batch.py`** | 15 | Regression tests สำหรับการสร้างเทสแบบ Batch |
| **`test_scenario_*.py`** | 10 | ตรวจสอบ Scenario Constraints และ Synthesis Spec |
| **`test_experiment.py`** | 8 | ตรวจสอบ Experiment Manifest Generation |
| **รวมทั้งหมด** | **110 tests** | **ผ่าน 100% ทุกตัวทั้งบน Windows และ Linux Docker** |

---

## 3. ข้อกำหนดสำหรับผู้พัฒนาต่อ (Guidelines for Next Contributors)

1. ทุกครั้งที่มีการแก้ไขโค้ดใน `Code/algorithm/`, `Code/analyzer/`, `Code/domain/` หรือ `Code/runner/` **ต้องรัน Unit Test ให้ผ่าน 110/110 ทั้งหมดก่อน Commit**
2. หากมีการเพิ่ม Custom Type Adapter ใหม่ใน `domain/adapter_registry.py` แนะนำให้เพิ่ม Unit Test เคสตัวอย่างใน `test_class_planner.py` หรือ `test_construction_planner.py`
3. ห้าม commit ไฟล์ไบนารีคอมไพล์แล้ว (`*.class` หรือ `__pycache__`) เข้าสู่ repository
