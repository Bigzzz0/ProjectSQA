# 🎯 สไลด์นำเสนอผลงานฉบับสมบูรณ์ (Project Presentation Deck)
## AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark on Defects4J
**วิชา:** CP353201 Software Quality Assurance (KKU CS)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**ทีมผู้จัดทำ:** กลุ่มที่ 4 (ปวริศช์, แทนคุณ, ธนภูมิ, ศิฆรินทร์)  

> **ผลการประเมินครบแล้ว (26 กันยายน 2026)** — master มีครบ 3,416 แถวสำหรับ 854 บั๊ก × 4 เทคนิค; suite ที่มีอยู่ 2,768 คู่ถูกรันครบ และ 648 คู่ไม่มี suite (`NO_SUITE`). ไม่มีรายการ `NOT_RUN` ค้าง (`results_complete: true`, `available_suite_evaluations_complete: true`).

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
* **Manual Testing:** ใช้เวลา 40–60% ของการพัฒนาซอฟต์แวร์ และมักเกิดความผิดพลาดของมนุษย์ (Human Error)
* **Algorithmic Generation (IPO / MIO):** รวดเร็ว มีทฤษฎีทางคณิตศาสตร์รองรับ แต่ขาดความเข้าใจเชิงความหมาย (Semantic Blindness) และติดปัญหา Regression Oracle
* **LLM-Assisted Testing (DeepSeek / Gemini):** เข้าใจตรรกะโปรแกรมและสร้าง Assertion ได้ฉลาด แต่เสี่ยงต่อ Compile Error และ Flaky Tests

#### 5 คำถามวิจัยหลัก (Research Questions):
1. **RQ1 (Coverage):** เทคนิคใดบรรลุความครอบคลุมรหัสคำสั่งสูงที่สุด?
2. **RQ2 (FDR):** เทคนิคใดตรวจจับข้อบกพร่องจริงได้แม่นยำที่สุดภายใต้กฎความซื่อตรงของตัวหาร?
3. **RQ3 (Ensemble):** การรวมเทคนิคต่าง Paradigm (Hybrid) มีพลังตรวจจับบั๊กเหนือกว่าใช้เดี่ยวๆ หรือไม่?
4. **RQ4 (Scaling):** จุดอิ่มตัวของการค้นหา (Search Saturation) ใน MIO อยู่ที่งบประมาณใด?
5. **RQ5 (Economics):** ความคุ้มค่าของโทเค็นและเวลาประมวลผลต่อบั๊กที่ตรวจพบเป็นอย่างไร?

---

<!-- slide -->
### 📌 Slide 3: ขอบเขตและสถาปัตยกรรมคลังข้อมูล (Scope & Master Catalog)
#### ครอบคลุมทุกคลาสเป้าหมายของทุกบั๊กใน Defects4J (All-Bugs & All-Classes)
* **17 โครงการมาตรฐานระดับโลก:** Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson, JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito, Time
* **สถิติสเกลการประเมินผล:**
  - **854 Active Bugs** ใน Defects4J 3.0.1-7-g8c16da82 ครอบคลุม 17 โปรเจกต์
  - **3,416 แถว** ใน master matrix (854 bugs × 4 techniques)
  - **2,768 suite evaluations เสร็จ**, **648 `NO_SUITE`**, และ **0 `NOT_RUN`**
* **ขอบเขตการวัดผล:** ใช้ aggregate coverage summary ของทุกคลาสใน `classes.modified`; รวม covered/total ก่อนคิดเปอร์เซ็นต์

---

<!-- slide -->
### 📌 Slide 4: เทคนิคที่ 1 — Combinatorial Testing & IPO (Member 1)
#### In-Parameter-Order (IPO/IPOG) และแหล่ง suite ปัจจุบัน
- IPO Native สร้าง pairwise combinations จาก factor/value model ของคลาสเป้าหมาย
- ผลที่ใช้ประเมินต้องอยู่ใน verified_suites_manifest.json และ hash ต้องตรง
- manifest ปัจจุบันมี 277 class-level suite records ครอบคลุม 257 bug-technique slots
- ประเมินครบ 257 suite slots; 252 ได้ผลวัด coverage, ตรวจพบ 37 บั๊ก (FDR 14.40%)
- ค่าเฉลี่ย line/branch coverage จากผลที่วัดได้: 26.76%/18.67% (n=252)

---

<!-- slide -->
### 📌 Slide 5: เทคนิคที่ 2 — Search-Based Testing & MIO (Member 2)
#### Many-Independent-Objective (MIO) Algorithm ใน EvoSuite
- EvoSuite สร้าง regression suites ด้วย search budget ที่กำหนด
- สถิติ generation แยกจาก coverage และ FDR ใน benchmark กลาง
- ประเมินครบ 834 suite slots; 797 ได้ผลวัด coverage, ตรวจพบ 5 บั๊ก (FDR 0.60%)
- ค่าเฉลี่ย line/branch coverage จากผลที่วัดได้: 63.85%/56.51% (n=797)

---

<!-- slide -->
### 📌 Slide 6: MIO Search Budget
#### แหล่งข้อมูล generation budget แยกจาก benchmark evaluation
- ใช้ MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv เป็น source ของสถิติ budget, seed และเวลา generation
- สร้างตารางทดสอบและกราฟใหม่ด้วย advanced_data_analytics.py
- Wilcoxon signed-rank บน class ที่จับคู่กัน: 30→60 วินาที n=1,006, p หลัง Holm=3.39×10⁻⁸⁴; 60→120 วินาที n=981, p หลัง Holm=1.54×10⁻⁶⁷
- ผล budget นี้อธิบายการสร้าง suite ไม่ใช่จำนวนบั๊กที่ตรวจพบหรือ coverage จากการประเมินกลาง
- ดูผลที่สร้างล่าสุดใน results/advanced_analytics.json และ results/figure5_budget_scaling.png

---

<!-- slide -->
### 📌 Slide 7: เทคนิคที่ 3 & 4 — สถาปัตยกรรม Dual-AI Testing (Member 3)
#### DeepSeek V4 Flash vs. Gemini 3.8 Flash ผ่าน KKU IntelSphere
* **System Prompt Architecture พร้อม 5 Guardrails:**
  1. `JUnit 4 Strict Compliance` (ห้าม JUnit 5 เด็ดขาด)
  2. `Execution Timeout Guard` (`@Test(timeout = 4000)`)
  3. `No External Dependencies` (ห้าม Mockito/AssertJ)
  4. `Defect Context & Oracle Injection` (ป้อน Ground truth context)
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
#### Bug-Level FDR Formulation & Denominator Integrity Rule
$$FDR = \left( \frac{N_{\text{BUG\_DETECTED}}}{N_{\text{evaluated\_bugs}}} \right) \times 100\%$$

| สถานะการประเมิน | เงื่อนไขการจำแนก | นับเป็น Bug Detected? |
| :--- | :--- | :---: |
| **`BUG_DETECTED`** | **Fail บนเวอร์ชันมีบั๊ก (`b`) และ Pass 100% บนเวอร์ชันแก้แล้ว (`f`)** | ✅ **นับ ($D=1$)** |
| **`NOT_DETECTED`** | Pass 100% ทั้งบน `b` และ `f` (ไม่กระตุ้นจุดบั๊ก) | ❌ ไม่นับ |
| **`FLAKY_OR_REGRESSION`** | Fail ทั้งบน `b` และ `f` (Assertion ผิดจากสเปกจริง) | ❌ ไม่นับ |
| **`COMPILE_ERROR`** | Syntax error หรือขาด Classpath | ❌ ไม่นับ |
| **`TIMEOUT`** | ทำงานค้างเกิน 4 วินาที | ❌ ไม่นับ |

> **⚠️ กฎความซื่อตรงของตัวหาร:** บั๊กที่เกิด `COMPILE_ERROR` หรือ `TIMEOUT` **จะถูกนับในตัวหารเสมอ ห้ามตัดทิ้ง!**

---

<!-- slide -->
### 📌 Slide 10: ผลการประเมินปัจจุบัน (Master Results)
- ใช้ results/master_benchmark_summary.csv เป็นตารางหลัก หนึ่งแถวต่อบั๊กและเทคนิค

| เทคนิค | Suite ที่ประเมิน | Coverage N | Line / Branch coverage | ตรวจพบ | FDR |
|---|---:|---:|---:|---:|---:|
| Native IPO | 257/257 | 252 | 26.76% / 18.67% | 37 | 14.40% |
| MIO (EvoSuite) | 834/834 | 797 | 63.85% / 56.51% | 5 | 0.60% |
| DeepSeek V4 Flash | 836/836 | 191 | 77.99% / 70.25% | 11 | 1.32% |
| Gemini 3.8 Flash | 841/841 | 422 | 86.24% / 79.45% | 107 | 12.72% |

*FDR ใช้ suite evaluations ทั้งหมดของเทคนิคนั้นเป็นตัวหาร รวม compile errors และ flaky/regression; coverage เฉลี่ยใช้เฉพาะผล DONE ที่มีค่าจริง*

---

<!-- slide -->
### 📌 Slide 11: เปรียบเทียบ coverage แบบจับคู่
- เปรียบเทียบ line coverage เฉพาะ project–bug ที่ทั้งสองเทคนิคมีค่าที่วัดได้
- ใช้ Wilcoxon signed-rank และปรับ p-value ด้วย Holm สำหรับหกคู่
- ผล matched N, p-value หลัง Holm และ paired rank-biserial:

| คู่เปรียบเทียบ | Matched N | p หลัง Holm | Rank-biserial |
|---|---:|---:|---:|
| Gemini – DeepSeek | 135 | 7.28×10⁻¹² | 0.830 |
| MIO – Gemini | 397 | 1.09×10⁻³² | -0.758 |
| MIO – DeepSeek | 182 | 0.0154 | -0.227 |
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
- Gemini: 1,068 generation records, เฉลี่ย 20,699.64 tokens และ 88.66 วินาที; benchmark ตรวจพบ 107 บั๊ก
- DeepSeek: 1,069 generation records, เฉลี่ย 20,900.15 tokens และ 295.11 วินาที; benchmark ตรวจพบ 11 บั๊ก
- ไม่คำนวณ token ต่อบั๊กที่ตรวจพบ เพราะ generation log ไม่มี run ID สำหรับจับคู่ผล benchmark
- แหล่งข้อมูลสรุป: results/advanced_analytics.json

---

<!-- slide -->
### 📌 Slide 14: Single-Class และ Multi-Class
- ใช้การแบ่งกลุ่มจาก target classes ใน all_bugs_catalog.json
- ค่า coverage ใช้แถว DONE ที่มีค่าการวัดจริงเท่านั้น
- ตารางนี้เป็นการแยกกลุ่มเพิ่มเติม; ตารางหลักรวม single-class และ multi-class bugs แล้ว
- FDR แสดงจำนวน attempted และตัวหารของแต่ละกลุ่มแยกกัน
- Gemini single-class: line coverage 87.01% (n=402), FDR 14.83% (106/715); multi-class: 70.61% (n=20), FDR 0.79% (1/126)
- ผลแยกทุกเทคนิคอยู่ใน `single_vs_multiclass` ภายใน results/advanced_analytics.json

---

<!-- slide -->
### 📌 Slide 15: ข้อเสนอแนะเชิงวิศวกรรม
- เลือกแนวทางสร้าง suite จากผล coverage, FDR, compile error และเวลา generation ที่วัดได้
- ระบุข้อจำกัดของแต่ละเทคนิคและจำนวน suite ที่มีจริง
- แยก generation budget และ AI token logs ออกจากผล benchmark
- Gemini ให้ coverage เฉลี่ยสูงสุดและตรวจพบ 107 บั๊ก; Native IPO ให้ FDR สูงสุดที่ 14.40% จาก 257 suites
- ควรพิจารณาควบคู่กับ compile errors: DeepSeek 645/836 และ Gemini 419/841; ผลนี้จึงไม่ชี้ผู้ชนะตัวเดียวสำหรับทุกเกณฑ์

---

<!-- slide -->
### 📌 Slide 16: บทสรุปและการส่งมอบผลงาน (Conclusion & Deliverables)
#### รายการส่งมอบที่ Member 4 ตรวจแล้ว:
* Master CSV ต้องมีหนึ่งแถวต่อ project, bug และ technique โดยไม่มี key ซ้ำ
* Excel, JSON, report และกราฟต้องสร้างจาก master CSV snapshot เดียวกัน
* Snapshot ปัจจุบันมี 3,416 แถว, 0 `NOT_RUN`, และ `results_complete: true`
* มี 648 `NO_SUITE` ซึ่งแยกออกจากตัวหาร FDR และไม่ตีความเป็นการตรวจไม่พบบั๊ก
* ต้องซ้อมสาธิตกรณี BUG_DETECTED โดยมี log แสดงผล fail บน buggy และ pass บน fixed

**ขอขอบคุณ ผศ.ดร.ชิตสุธา สุ่มเล็ก และทุกท่านครับ**  
*เปิดรับคำถามและข้อเสนอแนะ (Q&A)*
