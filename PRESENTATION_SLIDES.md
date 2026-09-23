# 🎯 สไลด์นำเสนอผลงานฉบับสมบูรณ์ (Project Presentation Deck)
## AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark on Defects4J
**วิชา:** CP353201 Software Quality Assurance (KKU CS)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**ทีมผู้จัดทำ:** กลุ่มที่ 4 (ปวริศช์, แทนคุณ, ธนภูมิ, ศิฆรินทร์)  

---

<!-- slide -->
### 📌 Slide 1: หน้าปกโครงงาน (Title Slide)
# การเปรียบเทียบเชิงประจักษ์ระหว่าง AI Testing และ Algorithmic Test Generation บน Defects4J
### Empirical Benchmark & Test Coverage Evaluation Across 17 Projects (2,804 Evaluations)

* **สมาชิกในกลุ่ม:**
  1. นายปวริศช์ ประมวล (653380138-8) — Member 1: IPO Lead
  2. นายแทนคุณ พันธ์นิกุล (653380292-8) — Member 2: MIO Lead
  3. นายธนภูมิ จันทรา (653380295-2) — Member 3: AI Lead
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
  - **854 Active Bugs** ทั้งหมดใน Defects4J v2.0
  - **1,073 Target Class Instances** (577 Unique Modified Classes)
  - **2,804 การประเมินทั้งหมด (Evaluations Dataset)**
* **ขอบเขตการวัดผล:** วัด Target-Class Coverage ด้วย Cobertura เจาะจงเฉพาะ `classes.modified` เพื่อความเป็นธรรมของ Defect-Targeted Testing

---

<!-- slide -->
### 📌 Slide 4: เทคนิคที่ 1 — Combinatorial Testing & IPO (Member 1)
#### In-Parameter-Order (IPO/IPOG) Engine ร่วมกับ Microsoft PICT
* **หลักการ:** ข้อบกพร่อง 70–90% เกิดจากการปฏิสัมพันธ์ของพารามิเตอร์เพียง 1 หรือ 2 ตัว (Pairwise Interactions)
* **กลยุทธ์การขยาย:** Horizontal Growth (จับคู่ลงแถวเดิม) + Vertical Growth (เติมแถวใหม่เก็บตก)
* **ผลลัพธ์เชิงประจักษ์:**
  - รันการประเมิน 173 ชุดทดสอบ สังเคราะห์ **42,398 กรณีทดสอบ**
  - ตัวอย่าง Math-2 (`HypergeometricDistribution`): ลดจาก 1,000 การทดสอบ เหลือเพียง **36 กรณีทดสอบ (ลดขนาดลง 96.4%)**
  - **เวลาประมวลผลเร็วที่สุดในทุกเทคนิค:** เฉลี่ยเพียง **2.5 วินาทีต่อคลาส**

---

<!-- slide -->
### 📌 Slide 5: เทคนิคที่ 2 — Search-Based Testing & MIO (Member 2)
#### Many-Independent-Objective (MIO) Algorithm ใน EvoSuite
* **ข้อจำกัดของ NSGA-II/MOSA:** เมื่อเป้าหมาย Coverage มีเป็นร้อยเป็นพัน จะเกิดอาการ Dominance Resistance
* **นวัตกรรมของ MIO:**
  - แยกคลังเก็บประชากร (Archive) ตามแต่ละ Objective อิสระ
  - สุ่มกลายพันธุ์ (Mutation) เจาะจงเป้าหมายที่ยังไม่ครอบคลุม
* **ผลลัพธ์เชิงประจักษ์:**
  - รันครบ 1,032 คลาส ในงบประมาณหลัก
  - บรรลุ **Line Coverage สูงสุดในภาพรวมที่ $68.85 \pm 31.26\%$**
  - แต่มี **FDR 0.00%** เนื่องจากถูกออกแบบด้วย Regression Oracle Assumption (ยึดโค้ดปัจจุบันเป็นความถูกต้อง)

---

<!-- slide -->
### 📌 Slide 6: การวิเคราะห์ MIO Budget Scaling (Member 2)
#### ผลกระทบของ Search Budget (30s vs 60s vs 120s) รวม 3,028 การทดลอง
* **30 วินาที:** Line Coverage = **$65.73 \pm 32.55\%$** (เวลารันเฉลี่ย 44.2s)
* **60 วินาที:** Line Coverage = **$68.73 \pm 31.42\%$** (เพิ่มขึ้น **$+3.00\%$**, $p < 0.05$)
* **120 วินาที:** Line Coverage = **$70.82 \pm 30.28\%$** (เพิ่มขึ้นเพียง **$+2.09\%$**, $p < 0.05$)

> **📉 Search Saturation Finding:**  
> การขยายเวลาเพิ่มอีก 60 วินาที (จาก 60s เป็น 120s) ให้ผลตอบแทนชะลอตัวลงอย่างมีนัยสำคัญ **60 วินาทีจึงเป็นจุดคุ้มทุนเชิงวิศวกรรมที่ดีที่สุด (Optimal Trade-off)**

*(อ้างอิง Figure 5: MIO Budget Scaling Plot)*

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
* **Docker Container (`defects4j_sqa`):** ติดตั้ง Defects4J v2.0 บน Ubuntu 22.04 + OpenJDK 1.8.0
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
### 📌 Slide 10: ตารางสรุปผลการทดลองเปรียบเทียบภาพรวม (Master Results)
#### สรุปผลจากชุดข้อมูล 2,804 การประเมิน ครบทั้ง 4 เทคนิค

| เทคนิคการทดสอบ | $N$ | Line Cov ($\mu \pm \sigma$) | Effective Line Cov | Branch Cov | Bug-Level FDR % | บั๊กที่ตรวจพบ | เวลาเฉลี่ย |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **MIO (EvoSuite)** | 1,032 | **$68.85 \pm 31.26\%$** | $69.66\%$ | **$68.85\%$** | $0.00\%$ | 0 บั๊ก | 90.8s |
| **Gemini 3.8 Flash** | 527 | $47.08 \pm 47.47\%$ | **$92.58\%$** | $44.25\%$ | **$16.70\%$** | **88 บั๊ก** | 6.0s |
| **IPO (Native/PICT)** | 173 | $32.57 \pm 1.03\%$ | $32.57\%$ | $23.78\%$ | $5.20\%$ | 9 บั๊ก | **2.5s** |
| **DeepSeek V4 Flash**| 1,072 | $16.28 \pm 34.98\%$ | $88.12\%$ | $14.86\%$ | $1.12\%$ | 12 บั๊ก | 15.0s |
| **Ensemble (Hybrid)** | — | — | — | — | **$12.30\%$** | **105 บั๊ก** | — |

---

<!-- slide -->
### 📌 Slide 11: การทดสอบสมมติฐานทางสถิติ (Hypothesis Testing)
#### Non-parametric Mann-Whitney U Test & Vargha-Delaney $\hat{A}_{12}$ Effect Size
* **MIO vs. DeepSeek:** $U = 960,320.5, p = 7.53 \times 10^{-203}$ (**$\hat{A}_{12} = 0.8680$ Large Effect**)
* **MIO vs. IPO:** $U = 146,458.0, p = 1.15 \times 10^{-41}$ (**$\hat{A}_{12} = 0.8203$ Large Effect**)
* **Gemini vs. DeepSeek:** $U = 379,910.0, p = 1.71 \times 10^{-44}$ (**$\hat{A}_{12} = 0.6725$ Medium Effect**)
* **Effective Coverage Insight:** ในกลุ่มที่คอมไพล์ผ่าน Gemini ชนะ MIO อย่างมีนัยสำคัญ ($\hat{A}_{12} = 0.812, p < 0.001$)

---

<!-- slide -->
### 📌 Slide 12: การผสานพลังในการตรวจพบบั๊ก (Ensemble Synergy)
#### การทำงานร่วมกันระหว่างข้ามกระบวนทัศน์ (Cross-Paradigm Hybrid Testing)
* **จำนวนบั๊กที่ตรวจพบรวม:** **105 บั๊ก (Ensemble FDR = 12.30%)**
* **สัดส่วนการตรวจพบบั๊กเฉพาะตัว (Unique Detections):**
  - **Gemini:** 88 บั๊ก (Unique = 80 บั๊ก, 90.9%)
  - **DeepSeek:** 12 บั๊ก (Unique = 4 บั๊ก, 33.3%)
  - **IPO:** 9 บั๊ก (**Unique = 7 บั๊ก, 77.8%**)
* **🚀 Key Takeaway:** IPO สามารถตรวจเจอบั๊กเฉพาะตัวที่ AI มองข้ามได้ถึง **7 บั๊ก** จากพลังของการจับคู่เงื่อนไขขอบเขตตัวเลขลึก!

---

<!-- slide -->
### 📌 Slide 13: ความคุ้มค่าเชิงเศรษฐศาสตร์ AI (AI Economics)
#### การเปรียบเทียบระหว่าง Gemini 3.8 Flash vs. DeepSeek V4 Flash
* **ความเร็วในการประมวลผล:**
  - Gemini ใช้เวลาเฉลี่ย **76.6 วินาที/คลาส**
  - DeepSeek ใช้เวลาเฉลี่ย **294.5 วินาที/คลาส** (Gemini เร็วกว่า **3.84 เท่า**)
* **จำนวน Token เฉลี่ยต่อคลาส:** Gemini 18,890 tokens vs. DeepSeek 19,921 tokens
* **ต้นทุนโทเค็นต่อ 1 บั๊กที่ตรวจพบจริง (Tokens per Detected Bug):**
  - **Gemini:** **~113,000 tokens / bug**
  - **DeepSeek:** **~1,780,000 tokens / bug**
  - **Gemini คุ้มค่ากว่าถึง 15.75 เท่า!**

---

<!-- slide -->
### 📌 Slide 14: โครงสร้างบั๊ก Single-Class vs. Multi-Class Resilience
#### ผลกระทบของความซับซ้อนสถาปัตยกรรมต่อประสิทธิภาพของแต่ละเครื่องมือ
* **Single-Class Defects:**
  - MIO ทำได้ 69.12% Coverage
  - Gemini ทำได้ 49.35% Coverage และ FDR 18.20%
* **Multi-Class Defects (บั๊กที่แก้ข้ามหลายคลาส):**
  - **MIO มีความยืดหยุ่นสูงสุด:** Coverage ลดลงเพียง 1.88% (เหลือ 67.24%)
  - **Gemini และ DeepSeek ประสิทธิภาพตกลงชัดเจน:** Compile Error เพิ่มสูงขึ้นเนื่องจากขาด Context ของคลาสข้างเคียง
  - **คำแนะนำ:** ระบบ AI จำเป็นต้องมี Inter-Class Context Injection ในโปรเจกต์ขนาดใหญ่

---

<!-- slide -->
### 📌 Slide 15: ข้อเสนอแนะเชิงวิศวกรรมสำหรับอุตสาหกรรม (Industry Guidelines)
#### 3 เสาหลักของการประกันคุณภาพซอฟต์แวร์ยุคใหม่ (Modern SQA Blueprint)
1. **Hybrid SQA Pipeline:**
   - ใช้ **MIO (EvoSuite)** วางโครง Regression Test ครอบคลุมภาพรวมทั้งระบบ (60s budget)
   - ใช้ **LLM (Gemini)** สร้าง Semantic Oracle Assertions ตรวจจับ Logic Bugs
   - เสริมด้วย **IPO (PICT)** บนฟังก์ชันตรรกะคณิตศาสตร์และตัวแปรเงื่อนไขขอบเขต
2. **Compile-Feedback Auto-fixing Loop:**
   - ติดตั้งกลไกจับคู่ Compile Error ส่งกลับไปให้ LLM แก้ไขอัตโนมัติก่อนขึ้น CI/CD
3. **Budget Cap Optimization:**
   - ล็อก Search Budget ของ SBST ไว้ที่ 60 วินาทีเพื่อความคุ้มค่าสูงสุด

---

<!-- slide -->
### 📌 Slide 16: บทสรุปและการส่งมอบผลงาน (Conclusion & Deliverables)
#### สรุปส่งมอบชิ้นงานของกลุ่มที่ 4 (ครบถ้วน 100%):
* ✅ **Master Dataset:** `results/master_benchmark_summary.csv` (2,804 Records)
* ✅ **Master Excel:** `results/Master_Benchmark_Results.xlsx` (6 Multi-tab Sheets)
* ✅ **Publication Figures:** 6 แผนภูมิความละเอียดสูง (300 DPI)
* ✅ **Data Dictionary:** `results/DATA_DICTIONARY.md`
* ✅ **Final Report:** `Final_Report.md` (6 บท พร้อมสถิติและการอ้างอิงสมบูรณ์)
* ✅ **Live Reproduction:** รันได้จริง 100% ผ่าน Universal Benchmark Runner

**ขอขอบคุณ ผศ.ดร.ชิตสุธา สุ่มเล็ก และทุกท่านครับ**  
*เปิดรับคำถามและข้อเสนอแนะ (Q&A)*
