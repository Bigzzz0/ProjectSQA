# 🎯 Defects4J 17-Project Representative Benchmark Suite

> **ผู้ดูแลระบบ (Infrastructure Lead):** Member 4 (นายศิฆรินทร์ อุปจันทร์)  
> **กลุ่มเป้าหมาย:** สมาชิกทีมทุกคน (Member 1: IPO, Member 2: MIO, Member 3: AI Prompting, Member 4: Benchmark Runner)  
> **ไฟล์คอนฟิกเครื่องจักร (Machine-Readable Catalog):** [`target_benchmark/catalog_17_projects.json`](catalog_17_projects.json)

---

## 📌 บทนำและความสำคัญทางวิชาการ

Defects4J เวอร์ชัน 2.0.0 ประกอบด้วย **17 โปรเจกต์มาตรฐาน รวม 835 ข้อบกพร่อง**  
เพื่อสร้างงานวิจัยด้าน Software Quality Assurance (SQA) ที่มี **ความหลากหลายของโดเมนระบบซอฟต์แวร์สูงสุด (Domain Diversity)** และมีความเป็นธรรมในการทดสอบ ทีมงานได้ทำการคัดเลือก **"1 คลาสตัวแทน ต่อ 1 โปรเจกต์"** รวมทั้งสิ้น **17 บั๊กตัวแทน (The Golden 17)** ครบถ้วนทุกโปรเจกต์ 100%

### เกณฑ์การคัดเลือกคลาสตัวแทน (Selection Criteria):
1. **Single-Class Defect:** มี 1 คลาสเป้าหมายใน `classes.modified` เพื่อให้ชุดทดสอบโฟกัสที่ Defect-Targeted logic ได้ชัดเจน
2. **Deterministic & Isolated Logic:** เน้นคลาสที่มีตรรกะระดับ Algorithm, Parser, Serialization, Arithmetic หรือ Data Structure ที่ทดสอบได้อิสระ
3. **Defects4J Ground Truth Triggered:** มี Trigger Test ที่ชัดเจน สามารถประเมิน Fault Detection Rate (FDR) ได้ตามมาตรฐาน

---

## 📊 ตารางคลังคลาสตัวแทน 17 โปรเจกต์ (The 17 Benchmark Targets)

| # | Project | Bug ID | Target Class Under Test (Full Package) | Class Name | โดเมนของซอฟต์แวร์ | Ground Truth Trigger Test | โฟลเดอร์เป้าหมาย |
| :-: | :--- | :-: | :--- | :--- | :--- | :--- | :--- |
| 1 | **Chart** | `1b` | `org.jfree.chart.renderer.category.AbstractCategoryItemRenderer` | `AbstractCategoryItemRenderer` | Graphic & Chart Rendering | `AbstractCategoryItemRendererTests::test2947660` | `Chart_1b/` |
| 2 | **Cli** | `1b` | `org.apache.commons.cli.CommandLine` | `CommandLine` | Command-line Argument Parser | `CommandLineTest::testBuilder` | `Cli_1b/` |
| 3 | **Closure** | `1b` | `com.google.javascript.jscomp.RemoveUnusedVars` | `RemoveUnusedVars` | Compiler AST Optimization | `RemoveUnusedVarsTest::testIssue168b` | `Closure_1b/` |
| 4 | **Codec** | `1b` | `org.apache.commons.codec.language.Soundex` | `Soundex` | Phonetic & String Encoding | `SoundexTest::testLocaleIndependence` | `Codec_1b/` |
| 5 | **Collections** | `25b` | `org.apache.commons.collections4.IteratorUtils` | `IteratorUtils` | Data Structures & Iterators | `IteratorUtilsTest::testCollIterator` | `Collections_25b/` |
| 6 | **Compress** | `1b` | `org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream` | `CpioArchiveInputStream` | Binary Archive & Compression | `CpioTestCase::testCpioUnarchive` | `Compress_1b/` |
| 7 | **Csv** | `1b` | `org.apache.commons.csv.ExtendedBufferedReader` | `ExtendedBufferedReader` | Delimited Text Parser & Buffer | `CSVParserTest::testBackslashEscaping` | `Csv_1b/` |
| 8 | **Gson** | `1b` | `com.google.gson.TypeInfoFactory` | `TypeInfoFactory` | JSON Serialization & Reflection | `TypeHierarchyAdapterTest::testTypeHierarchy` | `Gson_1b/` |
| 9 | **JacksonCore** | `1b` | `com.fasterxml.jackson.core.io.NumberInput` | `NumberInput` | High-Speed JSON Tokenizer | `TestNumberInput::testParseBigDecimal` | `JacksonCore_1b/` |
| 10 | **JacksonDatabind** | `1b` | `com.fasterxml.jackson.databind.JavaType` | `JavaType` | Object Mapping & Introspection | `TestTypeFactory::testNullType` | `JacksonDatabind_1b/` |
| 11 | **JacksonXml** | `1b` | `com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser` | `FromXmlParser` | Streaming XML Data Binding | `TestXmlTokenStream::testRootAttributes` | `JacksonXml_1b/` |
| 12 | **Jsoup** | `1b` | `org.jsoup.nodes.Element` | `Element` | HTML Parsing & DOM Tree | `ElementTest::testSetHtmlTitle` | `Jsoup_1b/` |
| 13 | **JxPath** | `1b` | `org.apache.commons.jxpath.ri.model.dom.DOMNodePointer` | `DOMNodePointer` | XML XPath Query Engine | `DOMModelTest::testAxisChild` | `JxPath_1b/` |
| 14 | **Lang** | `1b` | `org.apache.commons.lang3.math.NumberUtils` | `NumberUtils` | Java Core Utilities & Parsing | `NumberUtilsTest::testLang747` | `Lang_1b/` ✅ |
| 15 | **Math** | `2b` | `org.apache.commons.math3.distribution.HypergeometricDistribution` | `HypergeometricDistribution` | Numerical & Probability Math | `HypergeometricDistributionTest::testMath1021` | `Math_2b/` ✅ |
| 16 | **Mockito** | `1b` | `org.mockito.internal.invocation.InvocationMatcher` | `InvocationMatcher` | Dynamic Mocking Framework | `UsingVarargsTest::shouldMatchEasilyEmptyVararg` | `Mockito_1b/` |
| 17 | **Time** | `1b` | `org.joda.time.Partial` | `Partial` | Date/Time Calculation Engine | `TestPartial_Basics::testCompareTo` | `Time_1b/` |

---

## 🛠️ คู่มือปฏิบัติการสำหรับสมาชิกแต่ละท่าน (Workflow Cheat Sheet)

### 👤 สำหรับ Member 1: Combinatorial Testing (In-Parameter-Order Specialist)
* **ไฟล์ที่ต้องอ่าน:** อ่านไฟล์ `<Class>.java` ในแต่ละโฟลเดอร์ของ `target_benchmark/<Project>_<BugID>b/`
* **สคริปต์อัตโนมัติ:** ให้โมดูล `run_all_ipo.py` โหลดลิสต์จาก `catalog_17_projects.json` แล้ววนลูปสกัด Parameter Model
* **ไฟล์ผลลัพธ์ที่ต้องส่ง:** วางไฟล์ Unit Test ที่สร้างขึ้นใน `Combinatorial_IPO/TestCode/`
  * ตัวอย่างชื่อไฟล์: `<Class>_IPOTest.java` เช่น `NumberUtils_IPOTest.java`, `HypergeometricDistribution_IPOTest.java`

### 👤 สำหรับ Member 2: Mutation Insertion Optimization (MIO / EvoSuite Specialist)
* **ข้อมูลที่ต้องใช้:** ดึงชื่อ `target_class` (Full Package Name) และ `project` + `bug_id` จาก `catalog_17_projects.json`
* **สคริปต์อัตโนมัติ:** วนลูปสั่งรัน EvoSuite 3 Budget (30s, 60s, 120s) และ 3 Seeds (101, 102, 103)
* **ไฟล์ผลลัพธ์ที่ต้องส่ง:**
  1. สถิติ Mean $\pm$ SD ใน `MIO_Algorithm/Result_Round1/`
  2. ชุดเทสรอบที่ดีที่สุดวางใน `MIO_Algorithm/TestCode/<Class>_ESTest.java`

### 👤 สำหรับ Member 3: AI Prompt Engineer (Claude Sonnet 5 & Gemini 3.8 Flash)
* **ไฟล์ที่ต้องอ่าน:** สคริปต์ `scripts/kku_generate.py` สามารถอ่านไฟล์ `<Class>.java` และ `defects4j_info.txt` ในโฟลเดอร์ของแต่ละบั๊ก
* **สคริปต์อัตโนมัติ:** สั่งรันวนลูปทั้ง 17 บั๊กด้วย Python script:
  ```bash
  python scripts/kku_generate.py --ai claude --source-file target_benchmark/<Dir>/<Class>.java
  python scripts/kku_generate.py --ai gemini --source-file target_benchmark/<Dir>/<Class>.java
  ```
* **ไฟล์ผลลัพธ์ที่ต้องส่ง:**
  * Claude Test: `Claude-sonnet_5/TestCode/<Class>ClaudeTest.java`
  * Gemini Test: `Gemini-3_8_flash/TestCode/<Class>GeminiTest.java`

### 👤 สำหรับ Member 4: Benchmark Runner & Data Analyst
* **สคริปต์ประเมินผล:** สั่งรัน `python scripts/run_benchmark.py --all`
* **ผลลัพธ์:** คำนวณ Line Coverage, Branch Coverage และ Fault Detection Rate (FDR) เปรียบเทียบทั้ง 4 วิธี ครบทั้ง 17 โปรเจกต์ลงใน `benchmark_results.json` และสรุปในเล่มรายงาน!
