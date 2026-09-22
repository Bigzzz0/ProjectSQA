# รายงานผลการประเมินและเปรียบเทียบ Native IPO Pipeline

เอกสารนี้สรุปผลการทดลองเชิงประจักษ์ (Empirical Evaluation) หลังการปรับปรุงสถาปัตยกรรม Native IPO สู่ระดับคลาส (Class-Level Scope) พร้อมการเปรียบเทียบกับ MIO และ EvoSuite

---

## 1. ผลลัพธ์โดยสรุป (Executive Summary)

| ตัวชี้วัด (Metric) | ก่อนปรับปรุง (Baseline) | หลังปรับปรุง (Current) | อัตราการเปลี่ยนแปลง |
|---|---|---|---|
| **คลาสที่พร้อมสร้างเทส (AUTO_READY)** | 48 คลาส | **247 คลาส** | **+414.6% (เพิ่มขึ้น 5.1 เท่า)** |
| **คลาสที่ติด Entry Point (NEEDS_ENTRY_POINT)** | 440 คลาส | **78 คลาส** | **-82.3% (ปลดล็อก 362 คลาส)** |
| **จำนวนชุดทดสอบที่ผ่านการ Verify (FIXED_VERIFIED)** | 46 suites | **67 suites** (และกำลังเพิ่มขึ้น) | **+45.7%** |
| **ความถูกต้องของชุดทดสอบที่ Publish** | 100% Verified | **100% Verified** | คงมาตรฐานความถูกต้องสมบูรณ์ |
| **Pair Coverage ของทุก Factor ใน Suite** | 100.0% | **100.0%** | ไม่มี missing pair ใดๆ |
| **Unit Tests ของระบบ IPO** | 108 tests | **110 tests** | ผ่าน 100% |

---

## 2. การวิเคราะห์ความพร้อมระดับคลาส (Feasibility Audit Breakdown)

จากการตรวจสอบคลาสเป้าหมายทั้งหมด 1,070 คลาสอินสแตนซ์ (จาก 854 Bug IDs ใน Defects4J) สรุปสถานะได้ดังนี้:

```text
สถานะความพร้อมของ 1,070 คลาสเป้าหมาย:
├── AUTO_READY: 247 คลาส (23.1%)
│   └── มี Constructor/Receiver ที่สร้างได้ และ Method มีพารามิเตอร์ที่ระบบรองรับ
├── NEEDS_ADAPTER: 712 คลาส (66.5%)
│   └── เมธอดต้องการ Type Adapter เพิ่มเติม (เช่น IO Stream พิเศษ, Third-party Object)
├── NEEDS_ENTRY_POINT: 78 คลาส (7.3%)
│   └── คลาสที่มีเฉพาะ private/package-private methods หรือ abstract class ที่ยังไม่มี concrete subtype
└── NOT_PAIRWISE_APPLICABLE: 33 คลาส (3.1%)
    └── คลาสที่ถูกลบใน Fixed version หรือไม่มีเมธอดที่สามารถสร้างคู่ทดสอบได้
```

---

## 3. ผลการทดลอง Canary Experiment (40 Class Instances)

เราได้ทำการคัดเลือก 40 คลาสตัวแทนจาก **16 โปรเจกต์** ใน Defects4J เพื่อทดสอบการสร้างและ Verify จริง:
- **จำนวนคลาสที่รันประมวลผล:** 29 คลาส (รันไปถึง JacksonDatabind ก่อนติด timeout ของสภาพแวดล้อมเบื้องหลัง)
- **จำนวนคลาสที่ผ่านการ Verify (FIXED_VERIFIED):** **25 คลาส (อัตราสำเร็จ 86.2%)**
- **คลาสที่พบปัญหา (ERROR):** 4 คลาส
  1. `JacksonCore-9`: Constructor 9 arguments ถูกเข้าใจว่าเป็น 0-arg ctor ในแคชเดิม (แก้ไขได้โดยดึง AST ล่าสุด)
  2. `Codec-2`: Overloaded method type collision
  3. `Closure-57`: Parser node representation
  4. `Closure-14`: Control flow analysis entry node

---

## 4. กรณีศึกษา: การแก้ปัญหา Math-99 และ Math-92

### กรณี Math-99 (`MathUtils.lcm`)
- **สาเหตุเดิม:** Fixed version ของ Math-99 โยน Anonymous Inner Class `MathRuntimeException$1` ออกมา ทำให้ generator สร้างโค้ด `catch (MathRuntimeException$1 e)` ซึ่ง Java Compiler ไม่อนุญาตให้เขียนชื่อคลาสประเภทนิรนามใน catch block ส่งผลให้ทั้งคลาส compile ไม่ผ่าน
- **วิธีแก้:** เพิ่ม `getPublicExceptionType` ใน `fixed_version_oracle.py` เพื่อ unwind หาคลาสแม่ที่เป็น `public` (`ArithmeticException` หรือ `MathRuntimeException`)
- **ผลลัพธ์:** สร้างสำเร็จ 21 เมธอด ได้ **513 test cases** และผ่านการรันบน Defects4J 100%

### กรณี Math-92 (`MathUtils.binomialCoefficient`)
- **สาเหตุเดิม:** มี 1 เมธอดที่คำนวณนานจนเกิด Timeout (300 วินาที) ทำให้ระบบเดิม abort การทำงานและทิ้งอีก 2 เมธอดที่รันสำเร็จไปแล้ว
- **วิธีแก้:** นำสถาปัตยกรรม **Partial-Class Publication** มาใช้ แยก try-except ราย callable
- **ผลลัพธ์:** เมธอดที่ผ่านถูกนำมารวมเป็น suite และ publish เป็น `FIXED_VERIFIED` สำเร็จ

---

## 5. การเปรียบเทียบกับ MIO และ EvoSuite

| มิติการเปรียบเทียบ | MIO / EvoSuite | Native IPO (Current) |
|---|---|---|
| **แนวทางการเลือก Target** | All methods in class | All supported methods in class (Class-level) |
| **ลักษณะของ Test Inputs** | Genetic/Search-based, สุ่ม byte/null | Combinatorial 2-way covering array (Deterministic) |
| **การรับประกัน Coverage** | Branch / Instruction Coverage | **100% Pairwise Coverage** ของ Input Domains |
| **การตั้ง Oracle** | Assertion generation จาก execution trace | Empirical fixed-version oracle collection |
| **Handling of Exceptions** | Catch Throwable กว้างๆ | Strict exception type checking และ public unwinding |
| **Reproducibility** | Stochastic (ผลแต่ละรอบอาจต่างกัน) | **Deterministic 100%** (รันกี่รอบได้ผลเหมือนเดิมทุกตัวอักษร) |
