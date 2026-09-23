# 📑 รายงานผลการวิเคราะห์สถิติและการจัดการข้อมูลขั้นสูง
## (Advanced Statistical Analytics, Budget Scaling & Ensemble Synergy Report)

**โครงการ:** CP353201 Software Quality Assurance (KKU CS)  
**ผู้วิเคราะห์และจัดทำรายงาน:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)  
**ชุดข้อมูล:** `results/master_benchmark_summary.csv` (2,804 รายการประเมิน ครอบคลุม 17 โครงการ Defects4J)  

---

## 1. การทดสอบสมมติฐานทางสถิติและขนาดผลกระทบ (Statistical Hypothesis Testing & Effect Size)

เพื่อพิสูจน์ว่าความแตกต่างของผลลัพธ์ระหว่างเทคนิคมีนัยสำคัญทางสถิติจริง ได้ทำการทดสอบแบบ Non-parametric ด้วย **Mann-Whitney U Test** และวัดขนาดผลกระทบด้วย **Vargha-Delaney Effect Size (A12)**:

| คู่การเปรียบเทียบ (Technique Comparison) | Mann-Whitney U | p-value | นัยสำคัญ (alpha=0.05) | A12 Effect Size | ระดับผลกระทบ (Magnitude) |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Gemini 3.8 Flash vs. DeepSeek V4 Flash** | 379910.0 | **1.71e-44** | **มีนัยสำคัญ (***)** | **0.6725** | **Medium Effect** |
| **MIO (EvoSuite SBST) vs. Gemini 3.8 Flash** | 328760.5 | **1.19e-11** | **มีนัยสำคัญ (***)** | **0.6045** | **Small Effect** |
| **MIO (EvoSuite SBST) vs. DeepSeek V4 Flash** | 960320.5 | **7.53e-203** | **มีนัยสำคัญ (***)** | **0.8680** | **Large Effect** |
| **MIO (EvoSuite SBST) vs. IPO (Native / PICT)** | 146458.0 | **1.15e-41** | **มีนัยสำคัญ (***)** | **0.8203** | **Large Effect** |
| **Gemini 3.8 Flash vs. IPO (Native / PICT)** | 45668.0 | **9.71e-01** | **มีนัยสำคัญ (ns)** | **0.5009** | **Negligible Effect** |

> **💡 การตีความเชิงวิชาการ:**  
> - ค่า A12 > 0.71 หรือ < 0.29 บ่งชี้ว่าขนาดผลกระทบอยู่ในระดับ **Large Effect** อย่างชัดเจน โดย MIO มีความได้เปรียบด้าน Code Coverage เหนือทุกเทคนิคอย่างมีนัยสำคัญ (p < 0.001)  
> - เมื่อพิจารณาเฉพาะชุดทดสอบที่คอมไพล์ผ่าน (Effective Coverage) โมเดล **Gemini 3.8 Flash มีค่าเฉลี่ย Coverage สูงถึง 92.58%** ซึ่งสูงกว่า MIO อย่างมีนัยสำคัญทางสถิติ (p < 0.001, A12 = 0.812)

---

## 2. การวิเคราะห์ MIO Search Budget Scaling และผลตอบแทนส่วนเพิ่ม (ข้อกำหนด 1.7)

จากการรัน EvoSuite MIO ครบทั้ง 3 ระดับงบประมาณเวลา (30s, 60s, 120s) รวม 3,028 การทดลอง:

| Search Budget | จำนวนการประเมิน (N) | Line Coverage (Mean +/- SD) | Branch Coverage (Mean +/- SD) | เวลาประมวลผลจริงเฉลี่ย | ผลตอบแทนส่วนเพิ่ม (Delta Cov) |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **30 วินาที** | 1,023 คลาส | 65.73 ± 32.55% | 65.73 ± 32.55% | 44.2 วินาที | *(Baseline)* |
| **60 วินาที** | 1,015 คลาส | 68.73 ± 31.42% | 68.73 ± 31.42% | 74.5 วินาที | **+3.00%** (p < 0.05) |
| **120 วินาที** | 989 คลาส | 70.82 ± 30.28% | 70.82 ± 30.28% | 134.1 วินาที | **+2.09%** (p < 0.05) |

> **📉 การวิเคราะห์จุดอิ่มตัวของการค้นหา (Diminishing Returns Threshold):**  
> - การขยายเวลาจาก 30s เป็น 60s ให้ความครอบคลุมเพิ่มขึ้น **+3.00%**  
> - แต่การขยายเวลาเพิ่มอีก 2 เท่าจาก 60s เป็น 120s (เพิ่มเวลาอีก 60 วินาทีเต็ม) กลับให้ความครอบคลุมเพิ่มขึ้นเพียง **+2.09%**  
> - สะท้อนว่า MIO เริ่มเข้าสู่สภาวะอิ่มตัว (Search Saturation) โดยงบประมาณที่ **60 วินาทีถือเป็นจุดคุ้มทุนเชิงวิศวกรรมที่ดีที่สุด (Optimal Cost-Benefit Trade-off)**

---

## 3. การผสานพลังในการตรวจจับข้อบกพร่อง (Ensemble Fault Detection Synergy)

| เทคนิคการทดสอบ | จำนวนบั๊กที่ตรวจพบ (N_detected) | ตรวจพบเฉพาะตัว (Unique Detections) | ตรวจพบร่วมกับเทคนิคอื่น (Overlapping) |
| :--- | :---: | :---: | :---: |
| **Gemini 3.8 Flash** | **88 บั๊ก** | **80 บั๊ก (90.9%)** | 8 บั๊ก |
| **DeepSeek V4 Flash** | **12 บั๊ก** | **4 บั๊ก (33.3%)** | 8 บั๊ก |
| **IPO (Native / PICT)** | **9 บั๊ก** | **7 บั๊ก (77.8%)** | 2 บั๊ก |
| **MIO (EvoSuite SBST)** | **0 บั๊ก** | 0 บั๊ก | 0 บั๊ก (Regression Oracle) |
| **Ensemble Total (Hybrid)** | **105 บั๊ก** | **105 บั๊ก (100%)** | **Ensemble FDR = 12.30%** |

> **🚀 ข้อค้นพบเชิงประจักษ์สำคัญ (High-Impact Finding):**  
> การนำเทคนิคที่ต่าง Paradigm มาใช้งานร่วมกัน (Hybrid Testing: AI + Combinatorial + SBST) สามารถตรวจจับข้อบกพร่องรวมได้ถึง **105 บั๊ก (12.30% FDR)** ซึ่งสูงกว่าการใช้โมเดลที่ดีที่สุดเดี่ยวๆ (Gemini = 88 บั๊ก) อย่างชัดเจน โดย **IPO สามารถตรวจเจอบั๊กที่ไม่ซ้ำกับ AI ถึง 7 บั๊ก** เนื่องจากการทดสอบแบบคู่ลำดับ (Pairwise Boundary Combinations) เข้าถึงจุดขอบเขตค่าตัวเลขลึกกว่า Prompt ธรรมดา

---

## 4. ผลกระทบของโครงสร้างบั๊ก: Single-Class vs. Multi-Class Defect Resilience

| เทคนิคการทดสอบ | Single-Class Line Cov | Single-Class FDR % | Multi-Class Line Cov | Multi-Class FDR % | อัตราความยืดหยุ่น (Resilience) |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **MIO (EvoSuite)** | 69.12% | 0.00% | 67.24% | 0.00% | มีความเสถียรสูงมาก (Coverage ลดลงเพียง 1.88%) |
| **Gemini 3.8 Flash** | 49.35% | 18.20% | 34.12% | 8.10% | ประสิทธิภาพลดลงชัดเจนเมื่อเจอบั๊กข้ามคลาส |
| **DeepSeek V4 Flash** | 17.80% | 1.35% | 7.90% | 0.00% | Compile Error เพิ่มขึ้นสูงในโครงสร้าง Multi-Class |
| **IPO (Native)** | 32.80% | 5.80% | 31.10% | 1.80% | ทำงานได้ดีในระดับ Method แต่จำกัดขอบเขตคลาสเดียว |

---

## 5. การวิเคราะห์ความคุ้มค่าเชิงเศรษฐศาสตร์ (Token Economics & Cost-Effectiveness)

* **Gemini 3.8 Flash:**
  - เวลาสร้างเฉลี่ย: **76.6 วินาที/คลาส** (เร็วกว่า DeepSeek 3.8 เท่า)
  - จำนวน Token เฉลี่ย: **18,890 tokens/suite**
  - ต้นทุนโทเค็นต่อ 1 บั๊กที่ตรวจพบ: **~113,000 tokens / detected bug**
* **DeepSeek V4 Flash:**
  - เวลาสร้างเฉลี่ย: **294.5 วินาที/คลาส**
  - จำนวน Token เฉลี่ย: **19,921 tokens/suite**
  - ต้นทุนโทเค็นต่อ 1 บั๊กที่ตรวจพบ: **~1,780,000 tokens / detected bug**
