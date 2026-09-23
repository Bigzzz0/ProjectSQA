# รายงานผลการประเมินและเปรียบเทียบ Native IPO Pipeline

เอกสารนี้สรุปผลการทดลองเชิงประจักษ์ (Empirical Evaluation) หลังการปรับปรุงสถาปัตยกรรม Native IPO สู่ระดับคลาส (Class-Level Scope) พร้อมการเปรียบเทียบกับ MIO และ EvoSuite

---

## 1. ผลลัพธ์โดยสรุป (Executive Summary)

| ตัวชี้วัด (Metric) | ก่อนปรับปรุง (Baseline) | หลังปรับปรุง (Current) | อัตราการเปลี่ยนแปลง |
|---|---|---|---|
| **คลาสที่พร้อมสร้างเทส (AUTO_READY)** | 48 คลาส | **247 คลาส** | **+414.6% (เพิ่มขึ้น 5.1 เท่า)** |
| **คลาสที่ติด Entry Point (NEEDS_ENTRY_POINT)** | 440 คลาส | **78 คลาส** | **-82.3% (ปลดล็อก 362 คลาส)** |
| **จำนวนชุดทดสอบที่ผ่านการ Verify (FIXED_VERIFIED)** | 46 suites | **173 suites** | **+276.1% (เพิ่มขึ้น 3.8 เท่า)** |
| **จำนวนเมธอดเป้าหมายที่ครอบคลุม (Methods)** | 48 methods | **1,175 methods** | **+2,347.9% (เพิ่มขึ้น 24.5 เท่า)** |
| **จำนวนเคสทดสอบทั้งหมด (@Test Cases)** | ~1,200 tests | **42,398 tests** | **+3,433.2% (เพิ่มขึ้น 35.3 เท่า)** |
| **จำนวนบรรทัดโค้ดทดสอบ (Java Test LOC)** | ~15,000 LOC | **405,456 LOC** | **เพิ่มขึ้น 27 เท่า** |
| **ความถูกต้องของชุดทดสอบที่ Publish** | 100% Verified | **100% Verified** | คงมาตรฐานความถูกต้องสมบูรณ์ (Zero Flaky) |
| **Pair Coverage ของทุก Method ใน Suite** | 100.0% | **100.0% (1,175/1,175)** | ไม่มี missing pair แม้แต่คู่เดียว |
| **Unit Tests ของระบบ IPO** | 108 tests | **110 tests** | ผ่าน 100% (Windows & Docker) |

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

## 3. ผลการทดลอง All-Class Execution (173 Verified Suites)

ระบบได้ทำการประมวลผลครอบคลุมทั้ง 1,070 คลาสเป้าหมายใน Defects4J ผลการสร้างชุดทดสอบและ Verify บนสภาพแวดล้อมจริงเป็นดังนี้:

### ก) การกระจายตัวของ 173 Verified Suites รายโปรเจกต์ (13 โปรเจกต์)
| ลำดับ | โปรเจกต์ | จำนวน Suites ที่ผ่าน | จุดเด่นของคลาสที่ครอบคลุม |
|---|---|:---:|---|
| 1 | **Commons Math** | **31 suites** | `MathUtils`, `FastMath`, `Complex`, `Fraction`, `Variance`, `ChiSquareTest` |
| 2 | **Commons Compress** | **30 suites** | `TarUtils`, `SevenZFile`, `ArArchiveInputStream`, `ZipArchiveInputStream` |
| 3 | **Commons Lang** | **28 suites** | `NumberUtils`, `WordUtils`, `FastDatePrinter`, `ArrayUtils`, `StringUtils` |
| 4 | **Jsoup** | **26 suites** | `Attribute`, `ParseSettings`, `TokenQueue`, `XmlDeclaration`, `FormElement` |
| 5 | **Commons Codec** | **12 suites** | `DoubleMetaphone`, `Base64InputStream`, `Caverphone`, `Metaphone`, `StringUtils` |
| 6 | **Commons Collections** | **12 suites** | `ExtendedProperties`, `CollectionUtils`, `MultiValueMap`, `SetUniqueList` |
| 7 | **Closure Compiler** | **11 suites** | `Compiler`, `ProcessCommonJSModules`, `SourceFile` |
| 8 | **Joda Time** | **6 suites** | `FieldUtils`, `DateTimeZone`, `GJChronology` |
| 9 | **Commons Cli** | **5 suites** | `Option`, `GroupImpl`, `HelpFormatter` |
| 10 | **Commons Csv** | **4 suites** | `CSVFormat`, `ExtendedBufferedReader` |
| 11 | **Jackson Databind** | **4 suites** | `StdKeyDeserializer`, `TypeFactory` |
| 12 | **JFreeChart** | **3 suites** | `DefaultKeyedValues2D`, `ValueMarker`, `BoxAndWhiskerCategoryDataset` |
| 13 | **Google Gson** | **1 suite** | `ISO8601Utils` |
| **รวม** | **13 โปรเจกต์** | **173 suites** | **1,175 methods, 42,398 @Test cases, 405,456 LOC** |

### ข) การวิเคราะห์สาเหตุของคลาสที่ไม่ผ่านในกลุ่ม AUTO_READY (~74 คลาส)
จากการตรวจสอบ Log เชิงลึกใน `Results/cache/` พบสาเหตุทางเทคนิค 3 ประการที่ระบบคัดทิ้งเพื่อรักษาความถูกต้อง (Soundness Guarantee):
1. **Abstract Class (Static Detection Incompleteness):** เช่น `Chart_26b Axis`, `Math_13b AbstractLeastSquaresOptimizer` ใน Static Analysis มองเห็น Constructor แต่เมื่อ Compile จริง Java ไม่อนุญาตให้ `new` คลาส Abstract ตรงๆ
2. **Fixed Version Refactoring:** เช่น `Codec_2b Base64` เมธอดที่ถูกแก้ไขใน Buggy Version ถูก Refactor เปลี่ยน Signature หรือลบทิ้งไปใน Fixed Version ทำให้ไม่สามารถเก็บ Fixed Oracle ได้
3. **Numerical Solver Timeout:** เมธอดใน Commons Math บางฟังก์ชันคำนวณไม่สิ้นสุดเมื่อเจอบาง Combination สุดโต่ง (เช่น NaN, Infinity) จนติด Safe Timeout (300 วินาที) ระบบจึงตัดทิ้งเฉพาะเมธอดดังกล่าว

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
