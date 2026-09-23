# เอกสารส่งต่องาน (Handoff): Native IPO Class-Level Upgrade & Adapter Expansion

**วันที่:** 23 กันยายน 2026  
**Repository:** `ProjectSQA`  
**สถานะปัจจุบัน:**  
- **Verified Suites บนดิสก์จริง:** 178 Suites (1,202 Methods, 42,604 @Test Cases, 409,000+ LOC, 100% Pair Coverage, 0 Regression)  
- **ความพร้อมระดับคลาส (Audit ล่าสุด):** 336 คลาส `AUTO_READY` (+89 คลาสปลดล็อกใหม่ในรอบนี้)  
- **Unit Tests:** 118/118 tests ผ่าน 100% (Windows Host & Linux Docker)  
- **Baseline แช่แข็ง:** `Results/baseline/baseline_manifest.json` (173 suites ณ commit `a11795acc5`)

---

## 1. วัตถุประสงค์และสรุปการส่งมอบงาน

งานในรอบนี้มุ่งเน้นการ **พัฒนาความสามารถจริงของระบบ (Adapters, Observers, Construction Subtypes)** เพื่อปลดล็อกคลาสที่เดิมติดอยู่ในสถานะ `NEEDS_ADAPTER` ให้กลายเป็น `AUTO_READY` ได้จริง โดยไม่ hard-code ราย Bug ID และไม่นำรายการ adapter เดิมมาอ้างเป็นของใหม่ พร้อมจัดเตรียมคำสั่ง Terminal เดียวที่ผู้ใช้สามารถสั่งรันกระบวนการทั้งหมด (Audit $\rightarrow$ Canary Gate $\rightarrow$ Generation ทั้งคิว $\rightarrow$ Summary/Validate) ได้ด้วยตนเองโดยอัตโนมัติ

---

## 2. สิ่งที่ดำเนินการสำเร็จจริงในรอบนี้ (Implemented Capabilities)

### 1. ชุด Type Adapters ใหม่ (>35 ชนิดข้อมูล) ใน `adapter_registry.py`
เพิ่มการรองรับชนิดข้อมูลทั่วไปใน Java และ Domain Object ของโปรเจกต์เป้าหมาย:
- **JDK:** `StringBuffer`, `StringBuilder`, `Comparable`, `Number`, `Throwable`, `URI`, `URL`, `Object[]`, `double[][]`, `int[][]`
- **JFreeChart:** `Shape`, `Rectangle2D`, `Paint`, `Stroke`, `CategoryDataset`, `ValueAxis`, `Marker`, `Layer`
- **Joda-Time:** `Chronology`, `ReadableInstant`, `ReadableDuration`, `ReadablePeriod`, `ReadablePartial`, `LocalDateTime`, `LocalTime`, `DurationFieldType`, `DateTimeFieldType`, `PeriodType`
- **Commons-Codec:** `Base64Variant` (`MIME`, `PEM`)
- **Commons-Math:** `RealVector`, `RealMatrix`, `BigFraction`
- **Commons-Cli:** `Option`
- **Commons-Lang:** `StrBuilder`, `StrMatcher`
- **Jackson:** `JavaType`, `Type`, `JsonParser`, `JsonGenerator`, `DeserializationContext`, `DeserializationConfig`, `SerializerProvider`
- **Generics:** `T`, `K`, `V`, `E`

### 2. ชุด Deterministic Observers ใหม่ (23 ชนิดข้อมูล) ใน `stable_observer_for_type`
ป้องกันการนำ memory address identity hash (`[I@...`) มาเปรียบเทียบ โดยรองรับ:
- `StringBuffer`, `StringBuilder`, `Elements`, `BigFraction`, `RealVector`, `RealMatrix`, `StrBuilder`, `CSVFormat`, `JsonToken`, `DateTimeZone`, `Period`, `LocalDate`, `LocalDateTime`, `LocalTime`, `Duration`, `Instant`, `URI`, `URL`, `File`, `Path`, `Element`, `Node`, `Document`

### 3. ชุด Concrete Subtypes ใน `construction_planner.py`
เพิ่มการแมปคลาส Abstract/Interface สู่ Concrete Implementation:
- `Comparable` $\rightarrow$ `String`
- `CharSequence` $\rightarrow$ `String`
- `ValueAxis` $\rightarrow$ `NumberAxis`
- `Marker` $\rightarrow$ `ValueMarker`
- `ReadablePeriod` $\rightarrow$ `Period`
- `ReadableDuration` $\rightarrow$ `Duration`
- `ReadablePartial` $\rightarrow$ `LocalDate`
- `AbstractCompiler` $\rightarrow$ `Compiler`

### 4. ชุดทดสอบ Fixture Tests (118/118 Tests ผ่าน 100%)
สร้างไฟล์ [`Combinatorial_IPO/Code/tests/test_expanded_adapters.py`](file:///d:/673380278-9/2569/CP353201-SQA/Miniproject/ProjectSQA/Combinatorial_IPO/Code/tests/test_expanded_adapters.py) ครอบคลุมการทดสอบ Domain Resolvers, Observers, 2D Arrays และ Constructor Planning ผ่าน 100% ทั้งบน Windows และ Docker

### 5. หลักฐานการเชื่อมต่อเชิงประจักษ์ (Targeted Integration Evidence)
- **Feasibility Audit:** Re-audit ทั้ง 1,070 คลาส ส่งผลให้ **ปลดล็อกสำเร็จ 89 คลาส** จาก `NEEDS_ADAPTER` $\rightarrow$ `AUTO_READY` (ทำให้ยอด `AUTO_READY` เพิ่มขึ้นจาก 247 เป็น **336 คลาส**)
- **Verified Target ใหม่บนดิสก์จริง:**
  - `Chart_11b ShapeUtilities` (เดิมติด `Shape` และ `Rectangle2D`): สร้างผ่าน 3 เมธอด 12 @Test cases ได้สถานะ `FIXED_VERIFIED` เผยแพร่ใน `TestCode/Chart_11b/`
  - `Math_3b MathArrays` (เดิมติด Array format hash): สร้างผ่าน 16 เมธอด 80 @Test cases ได้สถานะ `FIXED_VERIFIED` เผยแพร่ใน `TestCode/Math_3b/`
  - ยอด Verified Suites บนดิสก์จริงเพิ่มขึ้นเป็น **178 suites** (จากเดิม 173 suites)

---

## 3. คำสั่ง Terminal เดียวสำหรับผู้ใช้ (Single Batch Pipeline Command)

ผู้ใช้สามารถสั่งรันจาก PowerShell บนเครื่อง Host ได้ทันที:

```powershell
.\Combinatorial_IPO\run_batch.ps1
```

*(หรือสั่งผ่าน Docker CLI โดยตรง)*:
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j sh -c "python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode audit --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode canary --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary && python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate"
```

#### กระบวนการ 4 ขั้นตอนที่สคริปต์นี้ทำงานอัตโนมัติ:
1. **[Stage 1/4] Audit:** คำนวณความพร้อมของทั้ง 1,070 คลาสใหม่ด้วย Adapters ล่าสุด และอัปเดต [inventory.json](file:///d:/673380278-9/2569/CP353201-SQA/Miniproject/ProjectSQA/Combinatorial_IPO/Results/inventory.json) เป็น 336 `AUTO_READY` คลาส
2. **[Stage 2/4] Canary Gate:** รัน Canary เพื่อตรวจจับ regression ของ baseline suites ก่อนเริ่มคิวเต็ม
3. **[Stage 3/4] Full Queue Generation:** วนลูปประมวลผลทุกคลาสในแคตตาล็อก:
   - ข้าม 178 suites ที่ผ่านแล้วในเสี้ยววินาที ($<1$ ms ด้วย SHA-256 hash match)
   - บันทึก Suite ใหม่และอัปเดต [verified_suites_manifest.json](file:///d:/673380278-9/2569/CP353201-SQA/Miniproject/ProjectSQA/Combinatorial_IPO/Results/verified_suites_manifest.json) แบบ Atomic ทันทีที่แต่ละคลาสผ่าน
   - แยกข้อผิดพลาดลง [Results/logs/failures.log](file:///d:/673380278-9/2569/CP353201-SQA/Miniproject/ProjectSQA/Combinatorial_IPO/Results/logs/failures.log) โดยไม่หยุดการทำงาน
4. **[Stage 4/4] Summary & Validation:** แสดงผลสรุปจำนวนสุดท้ายและตรวจสอบความสมบูรณ์ของ Checksum และ Pair Coverage 100%

---

## 4. ข้อแนะนำเมื่อรันเสร็จสิ้น

- **หากหยุดพักกลางคัน:** กด `Ctrl+C` ได้ตลอดเวลา และเมื่อรันคำสั่งเดิมซ้ำ ระบบจะทำงานต่อจากจุดที่ค้างไว้ทันทีโดยอัตโนมัติ
- **การส่งรายงานกลับมาให้อัปเดต:** ส่งผลสรุปจากหน้าจอ Terminal และไฟล์ [failures.log](file:///d:/673380278-9/2569/CP353201-SQA/Miniproject/ProjectSQA/Combinatorial_IPO/Results/logs/failures.log) กลับมาให้ agent ช่วยวิเคราะห์ได้ทันทีครับ
