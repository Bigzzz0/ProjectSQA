# 🎯 สไลด์นำเสนอผลงานฉบับสมบูรณ์ (Project Presentation Deck)
## AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark on Defects4J
**วิชา:** CP353201 Software Quality Assurance (KKU CS)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**ทีมผู้จัดทำ:** กลุ่มที่ 4 (ปวริศช์, แทนคุณ, ธนภูมิ, ศิฆรินทร์)  

> **Snapshot ผลประเมิน (26 กันยายน 2026)** — master มี 3,416 แถวจาก 854 บั๊ก × 4 เทคนิค; ประเมิน suite ที่มีอยู่ครบ 2,797 คู่ และ 619 คู่เป็น `NO_SUITE`. ไม่มีผลค้างที่ยังไม่สรุป (`unresolved_rows: 0`). `results_complete` หมายถึงประเมิน suite ที่มีอยู่ครบ ไม่ได้หมายความว่ามี suite ครบทุกคู่บั๊ก–เทคนิค.

---

<!-- slide -->
### 📌 Slide 1: หน้าปกโครงงาน (Title Slide)
# การเปรียบเทียบเชิงประจักษ์ระหว่าง AI Testing และ Algorithmic Test Generation บน Defects4J
### Empirical Benchmark & Test Coverage Evaluation Across 17 Projects (854 bugs)

* **สมาชิกในกลุ่ม:**
  1. นายปวริศช์ ประมวล (673380278-9) — Member 1: IPO Lead
  2. นายแทนคุณ พันธ์นิกุล (673380301-0) — Member 2: MIO Lead
  3. นายธนภูมิ จันทรา (673380272-1) — Member 3: AI Lead
  4. นายศิฆรินทร์ อุปจันทร์ (673380292-5) — Member 4: Infra & Data Analysis Lead

> **🗣️ Speaker Note (ผู้บรรยาย):**  
> "กราบเรียน ผศ.ดร.ชิตสุธา สุ่มเล็ก และสวัสดีเพื่อนๆ ทุกคนครับ วันนี้กลุ่มของพวกเราจะขอนำเสนอผลการวิจัยเชิงประจักษ์ขนาดใหญ่ ในการเปรียบเทียบระหว่างขั้นตอนวิธีสร้างเทสอัตโนมัติแบบดั้งเดิม (IPO และ MIO) กับโมเดลปัญญาประดิษฐ์ LLM ยุคใหม่ (DeepSeek และ Gemini) บนคลังข้อบกพร่องจริง Defects4J ครับ"

---

<!-- slide -->
### 📌 Slide 2: ที่มาและความสำคัญ (Motivation & Research Questions)
#### ปัญหาและความท้าทายในวงการ Software Testing
* **การสร้าง test อัตโนมัติ:** IPO สร้างชุดทดสอบจาก input factors ส่วน MIO ค้นหาชุดทดสอบตาม coverage objectives ทั้งสองวิธีต้องอาศัย target และ oracle ที่เหมาะสม
* **การสร้างด้วย Generative AI:** โมเดลสร้าง test และ assertions จาก prompt; ผลทดลองชุดนี้พบทั้ง compile errors และ flaky/regression

#### 5 คำถามวิจัยหลัก (Research Questions):
1. **RQ1 (Coverage):** เทคนิคใดบรรลุความครอบคลุมรหัสคำสั่งสูงที่สุด?
2. **RQ2 (FDR):** เทคนิคใดตรวจจับข้อบกพร่องจริงได้แม่นยำที่สุดภายใต้กฎความซื่อตรงของตัวหาร?
3. **RQ3 (Ensemble):** เมื่อนับผลตรวจจับแบบ union เทคนิคต่าง ๆ ตรวจพบบั๊กซ้ำกันและเพิ่มการตรวจพบเฉพาะได้เท่าใด?
4. **RQ4 (MIO Budget):** coverage และเวลา generation เปลี่ยนอย่างไรเมื่อเพิ่ม budget และผลนี้มีข้อจำกัดใด?
5. **RQ5 (AI Generation):** token usage และเวลา generation ต่อ generation record แตกต่างกันระหว่างโมเดลอย่างไร?

---

<!-- slide -->
### 📌 Slide 3: ขอบเขตและสถาปัตยกรรมคลังข้อมูล (Scope & Master Catalog)
#### Catalog ครอบคลุม 854 บั๊ก; ผลประเมินครอบคลุม suite ที่มีอยู่
* **17 โปรเจกต์:** Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson, JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito และ Time
* **854 active bugs** ใน Defects4J 3.0.1-7-g8c16da82; master มี **3,416 bug–technique rows** (854 × 4)
* ประเมิน suite ที่มีอยู่ครบ **2,797 คู่**; **619 คู่ `NO_SUITE`**; unresolved outcomes **0**
* ตัวเลข `available_suite_evaluations_complete: true` หมายถึงไม่มี suite ที่มีอยู่ค้างประเมิน; ไม่ได้หมายถึงมี suite ครบทั้ง 3,416 คู่
* Coverage รวม covered/total จาก aggregate summary ของ modified target classes ใน `classes.modified`

---

<!-- slide -->
### 📌 Slide 4: เทคนิคที่ 1 — Combinatorial Testing & IPO (Member 1)
#### In-Parameter-Order (IPO/IPOG) และแหล่ง suite ปัจจุบัน
- IPO Native สร้าง pairwise combinations จาก factor/value model ของคลาสเป้าหมาย
- ผลที่ใช้ประเมินต้องอยู่ใน verified_suites_manifest.json และ hash ต้องตรง
- verified manifest มี 277 suite records ระดับคลาส ครอบคลุม 257 bug IDs
- IPO inventory มี 1,070 class records ขณะที่ master catalog มี 1,073 modified-class entries; ส่วนต่าง 3 รายการยังไม่ reconcile จึงไม่อ้างว่า IPO audit ครอบคลุมทุก class entry
- มี suite ประเมินได้ 257/854 คู่; 252 วัด coverage ได้, compile error 5, `NO_SUITE` 597
- ตรวจพบ 37 บั๊ก (FDR 14.40% ของ 257 คู่ที่ประเมิน); `FLAKY_OR_REGRESSION` 18 และ `NOT_DETECTED` 197
- Line/branch coverage เฉลี่ย 26.76%/18.67% (n=252); `NO_SUITE` แบ่งเป็น generation/verification error 37 และ skipped/not ready 560 คู่

---

<!-- slide -->
### 📌 Slide 5: เทคนิคที่ 2 — Search-Based Testing & MIO (Member 2)
#### Many-Independent-Objective (MIO) Algorithm ใน EvoSuite
- EvoSuite สร้าง regression suites ด้วย search budget ที่กำหนด
- สถิติ generation แยกจาก coverage และ FDR ใน benchmark กลาง
- มี suite ประเมินได้ 834/854 คู่; `NO_SUITE` 20 คู่จาก generation failure
- 797 วัด coverage ได้; ตรวจพบ 5 บั๊ก (FDR 0.60% ของ 834 คู่ที่ประเมิน)
- Line/branch coverage เฉลี่ย 63.85%/56.51% (n=797); มี `FLAKY_OR_REGRESSION` 291 และ compile error 37

---

<!-- slide -->
### 📌 Slide 6: MIO Search Budget
#### แหล่งข้อมูล generation budget แยกจาก benchmark evaluation
- ใช้ MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv เป็น source ของสถิติ budget, seed และเวลา generation
- สร้างตารางทดสอบและกราฟใหม่ด้วย advanced_data_analytics.py
- coverage เฉลี่ยตาม budget 30/60/120 วินาที: 65.73%/68.73%/70.82%; เวลา generation เฉลี่ย 68.29/90.62/194.41 วินาที
- Wilcoxon แบบจับคู่: 30→60 วินาที n=1,006, p หลัง Holm=3.39×10⁻⁸⁴; 60→120 วินาที n=981, p หลัง Holm=1.54×10⁻⁶⁷
- EvoSuite ใช้ criterion `LINE:BRANCH` ซึ่งให้ coverage ค่าเดียวใน budget summary ไม่ใช่ line/branch แยก
- จำนวน records และ cohort ต่างกันตาม budget; เป็นผล generation เชิงพรรณนา ไม่ใช่ FDR และยังสรุปค่า budget ที่ดีที่สุดทั่วไปไม่ได้
- ดูผลที่สร้างล่าสุดใน results/advanced_analytics.json และ results/figure5_budget_scaling.png

---

<!-- slide -->
### 📌 Slide 7: เทคนิคที่ 3 & 4 — สถาปัตยกรรม Dual-AI Testing (Member 3)
#### DeepSeek V4 Flash vs. Gemini 3.8 Flash ผ่าน KKU IntelSphere
* **System Prompt Architecture พร้อม 5 Guardrails:**
  1. `JUnit 4 Strict Compliance` (ห้าม JUnit 5 เด็ดขาด)
  2. `Execution Timeout Guard` (`@Test(timeout = 4000)`)
  3. `No External Dependencies` (ห้าม Mockito/AssertJ)
  4. `Defect Context` (inject known defect specification จาก Defects4J เมื่อมีข้อมูล เพื่อสร้าง test แบบ defect-targeted)
  5. `Boundary Value Analysis (BVA)` (ทดสอบขอบเขต Null, Overflow, Empty)

---

<!-- slide -->
### 📌 Slide 8: สถาปัตยกรรมระบบทดสอบกลาง Docker (Member 4)
#### Universal Benchmark Pipeline & Dockerized Defects4J Environment
* **Docker Container (`defects4j_sqa`):** Defects4J 3.0.1-7-g8c16da82; Java 11 เป็นค่าเริ่มต้น และติดตั้ง Java 8 สำหรับขั้นตอน MIO ที่กำหนด
* **การ build:** Dockerfile สร้างจาก Ubuntu 20.04 และตรึง commit ของ Defects4J/PICT ไว้ใน repository; build ครั้งแรกดาวน์โหลด catalog ของ Defects4J และใช้พื้นที่กับเวลามาก
* **กลไกการวัดผล:**
  - Automated Cobertura Coverage Instrumenter
  - Auto-discovery Test Loader (รองรับทั้ง Single-Class และ Multi-Class Folders)
  - Incremental Resume via `progress.json` ป้องกันการสูญหายของข้อมูล

---

<!-- slide -->
### 📌 Slide 9: ระเบียบวิธีคำนวณ FDR และกฎ 5 สถานะ (Member 4)
#### Bug–Technique FDR และตัวหารที่ใช้
$$FDR_{\text{evaluated}} = \left( \frac{N_{\text{BUG\_DETECTED}}}{N_{\text{suite evaluations}}} \right) \times 100\%$$

| สถานะการประเมิน | เงื่อนไขการจำแนก | นับเป็น Bug Detected? |
| :--- | :--- | :---: |
| **`BUG_DETECTED`** | **Fail บนเวอร์ชันมีบั๊ก (`b`) และ Pass 100% บนเวอร์ชันแก้แล้ว (`f`)** | ✅ **นับ ($D=1$)** |
| **`NOT_DETECTED`** | Pass 100% ทั้งบน `b` และ `f` (ไม่กระตุ้นจุดบั๊ก) | ❌ ไม่นับ |
| **`FLAKY_OR_REGRESSION`** | Fail ทั้งบน `b` และ `f` (Assertion ผิดจากสเปกจริง) | ❌ ไม่นับ |
| **`COMPILE_ERROR`** | Syntax error หรือขาด Classpath | ❌ ไม่นับ |
| **`TIMEOUT`** | คำสั่ง Defects4J coverage/test ที่ runner เรียกเกิน 240 วินาที (แยกจาก `@Test(timeout = 4000)`) | ❌ ไม่นับ |

> **ตัวหาร:** ใช้จำนวน bug–technique rows ที่มี suite และถูกประเมิน รวม `COMPILE_ERROR`, `FLAKY_OR_REGRESSION` และ `TIMEOUT` (ถ้ามี); รายงาน `BUG_DETECTED ÷ 854` เป็นอัตราเทียบ catalog เพิ่มเติม ส่วน `NO_SUITE` ไม่ใช่ผลทดสอบ
> **ข้อจำกัด:** runner จำแนกจากผล buggy/fixed และไม่ได้ยืนยันเชิงความหมายว่า failure มาจาก root cause ที่รายงานไว้ของ Defects4J

---

<!-- slide -->
### 📌 Slide 10: ผลการประเมินปัจจุบัน (Master Results)
- ใช้ results/master_benchmark_summary.csv เป็นตารางหลัก หนึ่งแถวต่อบั๊กและเทคนิค

| เทคนิค | มี suite / 854 | NO_SUITE | Coverage n | Line / Branch coverage | ตรวจพบ | FDR ต่อ suite |
|---|---:|---:|---:|---:|---:|---:|
| Native IPO | 257 | 597 | 252 | 26.76% / 18.67% | 37 | 14.40% |
| MIO (EvoSuite) | 834 | 20 | 797 | 63.85% / 56.51% | 5 | 0.60% |
| DeepSeek V4 Flash | 853 | 1 | 192 | 78.02% / 70.22% | 11 | 1.29% |
| Gemini 3.8 Flash | 853 | 1 | 424 | 86.29% / 79.54% | 107 | 12.54% |

*ทุก suite ที่มีอยู่ถูกประเมินแล้ว (รวม 2,797 คู่); FDR ใช้จำนวน suite evaluations ของเทคนิคนั้นเป็นตัวหารและรวม compile errors กับ flaky/regression ส่วน coverage ใช้เฉพาะผลที่วัดได้*

---

<!-- slide -->
### 📌 Slide 11: เปรียบเทียบ coverage แบบจับคู่
- เปรียบเทียบ line coverage เฉพาะ project–bug ที่ทั้งสองเทคนิคมีค่าที่วัดได้
- ใช้ Wilcoxon signed-rank และปรับ p-value ด้วย Holm สำหรับหกคู่
- ผล matched N, p-value หลัง Holm และ paired rank-biserial:

| คู่เปรียบเทียบ | Matched N | p หลัง Holm | Rank-biserial |
|---|---:|---:|---:|
| Gemini – DeepSeek | 136 | 4.92×10⁻¹² | 0.834 |
| MIO – Gemini | 399 | 7.28×10⁻³³ | -0.759 |
| MIO – DeepSeek | 183 | 0.0175 | -0.222 |
| MIO – IPO | 245 | 1.64×10⁻³⁸ | 0.993 |
| Gemini – IPO | 147 | 1.90×10⁻²⁴ | 1.000 |
| DeepSeek – IPO | 69 | 4.92×10⁻¹² | 1.000 |
- การเปรียบเทียบเป็น exploratory เพราะแต่ละเทคนิคมี suite ที่ compile และวัด coverage ได้ไม่เท่ากัน
- ค่า rank-biserial บวกหมายถึงเทคนิคทางซ้ายมี line coverage สูงกว่า; รายละเอียดเต็มอยู่ใน `results/advanced_analytics.json`

---

<!-- slide -->
### 📌 Slide 12: ผลตรวจจับบั๊กและ Ensemble
- ใช้จำนวน detected และ FDR จากผลประเมินที่ provenance ตรงกับ suite ปัจจุบัน
- นับหนึ่งบั๊กต่อหนึ่งเทคนิค; BUG_DETECTED ต้อง fail บน buggy และ pass บน fixed
- ผลรวมเทคนิคตรวจพบ 144 บั๊กไม่ซ้ำจาก 853 บั๊กที่มี suite อย่างน้อยหนึ่งเทคนิค (16.88%)
- ผลตรวจพบเฉพาะเทคนิค: IPO 27, MIO 5, DeepSeek 5, Gemini 91 บั๊ก
- แหล่งข้อมูล: results/master_descriptive_stats.json และ results/advanced_analytics.json

---

<!-- slide -->
### 📌 Slide 13: AI Generation Economics
- แสดงค่าเฉลี่ย token และเวลา generation จากบันทึกจริงใน Deepseek_vs_Gemini_Economics.csv
- Gemini: 1,079 generation records, เฉลี่ย 20,855.06 tokens และ 89.78 วินาทีต่อ record
- DeepSeek: 1,082 generation records, เฉลี่ย 21,045.26 tokens และ 295.54 วินาทีต่อ record
- Generation logs ไม่มี run ID เชื่อมกับผล benchmark จึงรายงาน token/time แยกจาก detection และไม่คำนวณ token ต่อบั๊กที่ตรวจพบ
- แหล่งข้อมูลสรุป: results/advanced_analytics.json

---

<!-- slide -->
### 📌 Slide 14: Single-Class และ Multi-Class
- ใช้การแบ่งกลุ่มจาก target classes ใน all_bugs_catalog.json
- ค่า coverage ใช้แถว DONE ที่มีค่าการวัดจริงเท่านั้น
- ตารางนี้เป็นการแยกกลุ่มเพิ่มเติม; ตารางหลักรวม single-class และ multi-class bugs แล้ว
- FDR แสดงจำนวน attempted และตัวหารของแต่ละกลุ่มแยกกัน
- Gemini single-class: line coverage 87.07% (n=404), FDR 14.60% (106/726); multi-class: 70.61% (n=20), FDR 0.79% (1/127)
- ผลแยกทุกเทคนิคอยู่ใน `single_vs_multiclass` ภายใน results/advanced_analytics.json

---

<!-- slide -->
### 📌 Slide 15: ข้อเสนอแนะเชิงวิศวกรรม
- เลือกแนวทางสร้าง suite จากผล coverage, FDR, compile error และเวลา generation ที่วัดได้
- ระบุข้อจำกัดของแต่ละเทคนิคและจำนวน suite ที่มีจริง
- แยก generation budget และ AI token logs ออกจากผล benchmark
- Gemini ให้ coverage เฉลี่ยสูงสุดและตรวจพบ 107 บั๊ก; Native IPO ให้ FDR สูงสุดที่ 14.40% จาก 257 suites
- ควรพิจารณาควบคู่กับ compile errors: DeepSeek 661/853 และ Gemini 429/853; Gemini มี coverage mean สูงสุด ส่วน Native IPO มี FDR ต่อ suite สูงสุดใน snapshot นี้
- AI prompt inject known defect specification จาก Defects4J เมื่อมีข้อมูล; ผลจึงเป็นการสร้างแบบ defect-targeted ไม่ใช่ blind generation และไม่ควรขยายข้อสรุปเกินเงื่อนไขนี้

---

<!-- slide -->
### 📌 Slide 16: บทสรุปและการส่งมอบผลงาน (Conclusion & Deliverables)
#### รายการส่งมอบที่ Member 4 ตรวจแล้ว:
* Master CSV ต้องมีหนึ่งแถวต่อ project, bug และ technique โดยไม่มี key ซ้ำ
* Excel, JSON, report และกราฟต้องสร้างจาก master CSV snapshot เดียวกัน
* Snapshot มี 3,416 แถว; ไม่มี unresolved outcomes; ประเมิน suite ที่มีอยู่ครบ (`available_suite_evaluations_complete: true`)
* มี 619 `NO_SUITE`; แยกออกจากผลไม่ตรวจพบบั๊กและตัวหาร FDR
* ผลครบเฉพาะ suite ที่มีอยู่; การไม่มี suite สำหรับ 619 คู่ยังเป็นข้อจำกัดของการครอบคลุม dataset
* ต้องซ้อมสาธิตกรณี BUG_DETECTED โดยมี log แสดงผล fail บน buggy และ pass บน fixed

**ขอขอบคุณ ผศ.ดร.ชิตสุธา สุ่มเล็ก และทุกท่านครับ**  
*เปิดรับคำถามและข้อเสนอแนะ (Q&A)*
