# เอกสารส่งต่องาน (Handoff): Native IPO Class-Level Upgrade

**วันที่:** 23 กันยายน 2026  
**Repository:** `ProjectSQA`  
**สถานะปัจจุบัน:** 173 Verified Suites, 1,175 Methods, 42,398 @Test Cases, 405,456 LOC, 110 Unit Tests Passing (100%)

---

## 1. วัตถุประสงค์และสรุปการส่งมอบงาน

งานนี้เป็นการยกระดับ **Native IPO (Combinatorial Testing)** จากเดิมที่สร้างเทสได้เฉพาะเมธอดที่อยู่ใน Git Diff (Defect-focused: 46 suites) ให้สามารถสร้างเทสครอบคลุม **ทุกเมธอดที่ระบบรองรับในคลาสเป้าหมายตามแคตตาล็อก (Class-Level Scope)** เช่นเดียวกับขอบเขตของ MIO และ EvoSuite พร้อมทั้งจัดระเบียบโครงสร้างไดเรกทอรีใหม่ทั้งหมดให้อยู่ในมาตรฐานเดียวกัน

---

## 2. สิ่งที่ดำเนินการสำเร็จแล้ว (Completed Items)

1. **การจัดระเบียบโครงสร้างไดเรกทอรี (Single Clean Layout):**
   - โค้ดทั้งหมดอยู่ภายใต้ `Combinatorial_IPO/Code/`
   - การตั้งค่าและสแนปช็อตแคตตาล็อกอยู่ที่ `Combinatorial_IPO/Configuration/`
   - ผลการทดลอง, แคช และดัชนีชี้วัดรวมศูนย์ที่ `Combinatorial_IPO/Results/`
   - Test suites ที่ verify แล้ว publish ลงที่ `Combinatorial_IPO/TestCode/<Project>_<BugID>b/<package-path>/<Class>_IPOTest.java` โดยตรง
   - ย้ายเอกสาร handoff เดิมไปไว้ที่ `Combinatorial_IPO/docs/handoffs/`
   - ลบไดเรกทอรีขยะและผลลัพธ์เก่า (`Models/`, `Result_Round1/`, `Result_Round2/`) ออกจาก working tree อย่างหมดจด

2. **Java AST Parser (`JavaAstParser.java` & `java_parser.py`):**
   - พัฒนาตัววิเคราะห์ Abstract Syntax Tree โดยใช้ Standard Java Compiler Tree API (`com.sun.source.tree.*`)
   - สกัด constructors, methods, parameters, generics, annotations (`@Nullable`), throws clause ได้อย่างแม่นยำในเวลา ~0.2 วินาที

3. **Construction Planner (`construction_planner.py`):**
   - ระบบวางแผนการสร้าง receiver object อัตโนมัติ (implicit zero-arg, explicit zero-arg, factorized constructor, static factories, concrete subtypes)
   - ปลดล็อกคลาสที่เป็น instance methods ให้เข้าสู่กระบวนการ Combinatorial Testing ได้

4. **Oracle Hardening และการแก้ปัญหา Math-99 / Math-92:**
   - **Math-99:** แก้ปัญหา anonymous inner class (`MathRuntimeException$1`) โดยการ unwind หา public superclass (`getPublicExceptionType`)
   - **Math-92:** แก้ปัญหา timeout ของเมธอดเดี่ยว โดยใช้สถาปัตยกรรม **Partial-Class Publication** (ความล้มเหลวของ 1 เมธอดจะไม่ทำให้เมธอดอื่นที่ผ่านถูกทิ้ง)
   - ปัจจุบันทั้ง Math-99 (21 methods, 513 tests) และ Math-92 ได้รับการ verify และ publish สำเร็จเป็น `FIXED_VERIFIED`

5. **การเพิ่มผลผลิตเชิงประจักษ์ (Empirical Yield):**
   - คลาสที่พร้อมสร้างเทส (`AUTO_READY`) เพิ่มขึ้นจาก 48 คลาสเป็น **247 คลาส** (เพิ่มขึ้น 5.1 เท่า)
   - คลาสที่ติด Entry Point (`NEEDS_ENTRY_POINT`) ลดลงจาก 440 คลาสเหลือเพียง **78 คลาส**
   - จำนวนชุดทดสอบที่ผ่านการ Verify เพิ่มขึ้นจาก 46 เป็น **173 suites (+276.1% หรือเพิ่มขึ้น 3.8 เท่า)**
   - ครอบคลุม **1,175 methods** และสร้างเทสเคสทั้งหมด **42,398 @Test cases**
   - ทุก Method ได้ **100.0% Pair Coverage** สมบูรณ์
   - Unit tests ผ่านครบ 100% (110/110 tests) ทั้งบน Windows Host และ Linux Docker

---

## 3. คำแนะนำสำหรับผู้ใช้ (คำสั่งรันงานระยะยาวด้วยตนเอง)

ตามที่ผู้ใช้แจ้งว่า *"ขั้นตอนรันนานๆ คุณสามารให้ผมรันเองได้นะ เพื่อลดการใช้ limit usage แต่ต้องนานมากๆ"*  
ขั้นตอนการสร้าง Test Suites ครบทั้ง 247 คลาส (`--mode generate`) ใช้เวลาประมาณ 1 - 2 ชั่วโมงผ่าน Docker Container เนื่องจากต้องทำการ checkout, compile, run fixed version oracle และ verify ผ่าน Defects4J ทีละคลาส

ผู้ใช้สามารถเปิด Terminal (PowerShell) บนเครื่องของตนเองและรันคำสั่งด้านล่างนี้ได้โดยตรง:

```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume
```

> **ข้อดีของ `--resume`:**  
> ระบบได้บันทึกผลลัพธ์ของคลาสที่ผ่านแล้ว (**173 suites**) ไว้ใน `Results/verified_suites_manifest.json` และแคชใน `Results/cache/` เรียบร้อยแล้ว หากมีการรันคำสั่งซ้ำ ระบบจะข้ามคลาสที่ทำเสร็จแล้วในเสี้ยววินาที และสามารถรันต่อจากจุดเดิมได้เสมอ

### คำสั่งตรวจสอบผล (รันเร็ว):
```powershell
# 1. ดูสรุปจำนวนและสถานะทั้งหมด
python Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary

# 2. ตรวจสอบความถูกต้องของ Checksum และ Pair Coverage
python Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate

# 3. รัน Unit Tests ทั้ง 110 tests
python -m unittest discover -s Combinatorial_IPO/Code/tests -t Combinatorial_IPO/Code
```

---

## 4. คู่มือการรับช่วงต่องานสำหรับ Member 4 หรือผู้พัฒนาต่อ (Developer Takeover Guide)

สำหรับผู้ที่มารับงานต่อเพื่อขยายผลผลิตให้เกิน 173 suites:

### ขั้นที่ 1: ตรวจสอบ Backlog คลาสที่รอ Adapter
เปิดไฟล์ **`Combinatorial_IPO/Results/routing_manifest.json`** ในไฟล์นี้จะมีรายการคลาส 712 คลาสที่อยู่ในสถานะ `NEEDS_ADAPTER` พร้อมระบุชื่อ Type ที่คลาสนั้นต้องการ เช่น `Base64Variant`, `Shape`, `JsonParser`

### ขั้นที่ 2: วิธีการเขียน Custom Type Adapter เพิ่มเติม
เปิดไฟล์ **`Combinatorial_IPO/Code/domain/adapter_registry.py`**  
เพิ่มฟังก์ชัน Generator และลงทะเบียนเข้า Dictionary เช่น:

```python
# ตัวอย่าง: เพิ่ม Adapter สำหรับ org.apache.commons.codec.binary.Base64Variant
def base64_variant_adapter() -> Sequence[str]:
    return [
        "org.apache.commons.codec.binary.Base64Variants.MIME",
        "org.apache.commons.codec.binary.Base64Variants.PEM",
    ]

# ลงทะเบียนเข้า TYPE_ADAPTERS ใน adapter_registry.py:
TYPE_ADAPTERS["Base64Variant"] = base64_variant_adapter
```

### ขั้นที่ 3: สั่งรัน Audit ซ้ำเพื่ออัปเดต Inventory
เมื่อเพิ่ม Adapter ใหม่ ให้รันคำสั่ง Audit เพื่อตรวจจับคลาสที่จะถูกปลดล็อกเป็น `AUTO_READY`:
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode audit --resume
```

### ขั้นที่ 4: สั่งรันสร้าง Test Suite สำหรับคลาสที่ปลดล็อกใหม่
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume
```
ระบบจะดึงเฉพาะคลาสที่ปลดล็อกใหม่มารัน โดยข้าม 173 suites เดิมที่ผ่านแล้วโดยอัตโนมัติ

---

## 5. การจำแนกประเภทงานที่ไม่สามารถแปลงเป็น Unit Test ได้

1. **`NEEDS_ENTRY_POINT` (78 คลาส):** คลาสที่เป็น Abstract หรือมีเฉพาะ private logic ซึ่งต้องอาศัย Subclass หรือ Integration Driver ภายนอกมาเรียก
2. **`NOT_PAIRWISE_APPLICABLE` (33 คลาส):** คลาสที่ถูกลบไปใน Fixed Version หรือมีพารามิเตอร์ < 2 แนะนำให้ใช้ Example-based หรือ Integration test แทน

