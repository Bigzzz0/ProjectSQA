# Combinatorial IPO - Published JUnit Test Suites

โฟลเดอร์นี้รวบรวม **ชุดทดสอบ JUnit 4 ทั้งหมด (173 Verified Suites / 181 ไฟล์ Java)** ที่ผ่านการสังเคราะห์ด้วยอัลกอริทึม **Native IPO (2-Way Pairwise Testing)** และผ่านการคอมไพล์พร้อมทดสอบจริง (Verified) บน Defects4J Fixed Version เรียบร้อยแล้ว 100%

---

## 1. ข้อมูลสถิติเชิงประจักษ์ (Empirical Metrics)

* **จำนวน Verified Test Suites:** **173 Suites**
* **จำนวนไฟล์ซอร์สโค้ด Java:** **181 ไฟล์ (`*_IPOTest.java`)**
* **จำนวนเมธอดเป้าหมายที่ครอบคลุม:** **1,175 Methods**
* **จำนวนเคสทดสอบเดี่ยว (`@Test`):** **42,398 Test Cases**
* **จำนวนบรรทัดโค้ดทั้งหมด (LOC):** **405,456 บรรทัด**
* **การรับประกัน Pair Coverage:** **100.0% ทุกเมธอด (1,175 / 1,175 methods)** ไม่มี missing pair
* **ความถูกต้องของผลลัพธ์ (Flakiness):** **0% Flaky Tests** ทุกเทสรันผ่านฉลุยบน Fixed Version

---

## 2. โครงสร้างและการจัดเก็บไฟล์ (Directory Layout)

ชุดทดสอบจัดเก็บแยกตาม **Bug Target Directory** สอดคล้องกับมาตรฐานของ Defects4J โดยตรง:

```text
TestCode/
├── Chart_18b/
│   └── org/jfree/data/DefaultKeyedValues2D_IPOTest.java
├── Closure_18b/
│   └── com/google/javascript/jscomp/Compiler_IPOTest.java
├── Codec_1b/
│   ├── org/apache/commons/codec/language/Caverphone_IPOTest.java
│   └── org/apache/commons/codec/language/Metaphone_IPOTest.java
├── Compress_14b/
│   └── org/apache/commons/compress/archivers/tar/TarUtils_IPOTest.java
├── Jsoup_42b/
│   └── org/jsoup/nodes/FormElement_IPOTest.java
├── Lang_1b/
│   └── org/apache/commons/lang3/math/NumberUtils_IPOTest.java
├── Math_99b/
│   └── org/apache/commons/math/util/MathUtils_IPOTest.java
└── ... (รวม 173 suites ใน 13 โปรเจกต์)
```

---

## 3. การกระจายตัวรายโปรเจกต์ (13 โปรเจกต์ใน Defects4J)

| โปรเจกต์ | จำนวน Suites | เมธอดเด่นที่ครอบคลุม |
|---|:---:|---|
| **Commons Math** | **31 suites** | `MathUtils`, `FastMath`, `Complex`, `Fraction`, `Variance`, `ChiSquareTest` |
| **Commons Compress** | **30 suites** | `TarUtils`, `SevenZFile`, `ArArchiveInputStream`, `ZipArchiveInputStream` |
| **Commons Lang** | **28 suites** | `NumberUtils`, `WordUtils`, `FastDatePrinter`, `ArrayUtils`, `StringUtils` |
| **Jsoup** | **26 suites** | `Attribute`, `ParseSettings`, `TokenQueue`, `XmlDeclaration`, `FormElement` |
| **Commons Codec** | **12 suites** | `DoubleMetaphone`, `Base64InputStream`, `Caverphone`, `Metaphone`, `StringUtils` |
| **Commons Collections** | **12 suites** | `ExtendedProperties`, `CollectionUtils`, `MultiValueMap`, `SetUniqueList` |
| **Closure Compiler** | **11 suites** | `Compiler`, `ProcessCommonJSModules`, `SourceFile` |
| **Joda Time** | **6 suites** | `FieldUtils`, `DateTimeZone`, `GJChronology` |
| **Commons Cli** | **5 suites** | `Option`, `GroupImpl`, `HelpFormatter` |
| **Commons Csv** | **4 suites** | `CSVFormat`, `ExtendedBufferedReader` |
| **Jackson Databind** | **4 suites** | `StdKeyDeserializer`, `TypeFactory` |
| **JFreeChart** | **3 suites** | `DefaultKeyedValues2D`, `ValueMarker`, `BoxAndWhiskerCategoryDataset` |
| **Google Gson** | **1 suite** | `ISO8601Utils` |

---

## 4. วิธีการนำชุดทดสอบไปรันด้วยตนเอง (Manual Execution via Defects4J)

คุณสามารถนำไฟล์เทสตัวใดตัวหนึ่งไปทดสอบรันใน Docker Container ด้วย Defects4J ได้โดยตรง:

```powershell
# ตัวอย่าง: รันเทส Math_99b
docker --context default exec sqa-defects4j bash -c "
  cd /tmp && rm -rf /tmp/manual_run && defects4j checkout -p Math -v 99f -w /tmp/manual_run
  cp /workspace/Combinatorial_IPO/TestCode/Math_99b/org/apache/commons/math/util/MathUtils_IPOTest.java /tmp/manual_run/src/test/java/org/apache/commons/math/util/
  cd /tmp/manual_run && defects4j compile && defects4j test -t org.apache.commons.math.util.MathUtils_IPOTest
"
```

---

## 5. การตรวจสอบความสมบูรณ์ผ่าน Manifest (Manifest Verification)

ข้อมูลเมทาดาต้า, Checksum SHA-256 และ Pair Coverage ของทุกชุดทดสอบในโฟลเดอร์นี้ ถูกบันทึกไว้ใน:
* **`Combinatorial_IPO/Results/verified_suites_manifest.json`**

ตรวจสอบความถูกต้องของชุดทดสอบทั้งหมดได้ด้วยคำสั่ง:
```powershell
python Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
```
