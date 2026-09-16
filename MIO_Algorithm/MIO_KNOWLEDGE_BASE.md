# 📚 คู่มือข้อควรรู้และการแปลผลเชิงวิชาการ MIO Algorithm (Academic Knowledge Base)

**สำหรับ:** Member 2 (นายแทนคุณ พันธ์นิกุล - MIO / EvoSuite Specialist)  
**โครงการ:** CP353201 Software Quality Assurance (KKU CS)  
**หัวข้อ:** การแปลความหมาย Code Coverage, กลไกการตรวจจับบั๊กใน Defects4J (FDR), และการเชื่อมโยงสู่เล่มรายงานฉบับสมบูรณ์

---

## 📌 สารบัญ (Table of Contents)
1. [บทที่ 1: ความหมายเชิงลึกของ Code Coverage (Coverage Decoded)](#-บทที่-1-ความหมายเชิงลึกของ-code-coverage-coverage-decoded)
2. [บทที่ 2: Defects4J Ground Truth & การพิสูจน์ว่า "หาบั๊กเจอหรือไม่?"](#-บทที่-2-defects4j-ground-truth--การพิสูจน์ว่า-หาบั๊กเจอหรือไม่)
3. [บทที่ 3: วิธีการทดสอบตรวจจับบั๊กด้วย Runner กลาง (How to Verify with Runner)](#-บทที่-3-วิธีการทดสอบตรวจจับบั๊กด้วย-runner-กลาง-how-to-verify-with-runner)
4. [บทที่ 4: การนำผลลัพธ์ไปเขียนเล่มรายงาน (Final Report Alignment)](#-บทที่-4-การนำผลลัพธ์ไปเขียนเล่มรายงาน-final-report-alignment)
5. [บทที่ 5: คำถามที่พบบ่อยสำหรับ Member 2 (MIO FAQ)](#-บทที่-5-คำถามที่พบบ่อยสำหรับ-member-2-mio-faq)

---

## 🎯 บทที่ 1: ความหมายเชิงลึกของ Code Coverage (Coverage Decoded)

เมื่อเราสั่งรันสคริปต์ [batch_evosuite.py](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/MIO_Algorithm/Code/batch_evosuite.py) แล้วได้ผลลัพธ์ เช่น **`Coverage: 57.13%`** ตัวเลขนี้มีความหมายทางวิศวกรรมซอฟต์แวร์ดังนี้ครับ:

### 1.1 Line Coverage vs. Branch Coverage
ในเกณฑ์ข้อกำหนดวิชาการข้อ **1.7** รายวิชากำหนดให้วัดผล 2 เกณฑ์หลัก:

```text
       ┌────────────────────────────────────────────────────────┐
       │                 Defects4J Target Class                 │
       │                                                        │
       │  Line Coverage: คำสั่งแต่ละบรรทัดถูกรันผ่านหรือไม่?     │
       │  [Line 1] int x = a + b;             ✅ Covered        │
       │                                                        │
       │  Branch Coverage: เงื่อนไขการตัดสินใจผ่านครบไหม?       │
       │  [Line 2] if (x > 100) {             🔀 True / False  │
       │  [Line 3]     status = "HIGH";       ✅ Covered (True) │
       │  [Line 4] } else {                                     │
       │  [Line 5]     status = "LOW";        ❌ Missed (False) │
       │  [Line 6] }                                            │
       └────────────────────────────────────────────────────────┘
```

* **Line Coverage (%):** คือสัดส่วนของ **"บรรทัดคำสั่ง (Statements/Lines)"** ในคลาสเป้าหมายที่ถูกชุดทดสอบรันผ่านจริง เทียบกับบรรทัดคำสั่งทั้งหมด
  $$\text{Line Coverage (\%)} = \left(\frac{\text{Covered Lines}}{\text{Total Executable Lines}}\right) \times 100\%$$
* **Branch Coverage (%):** คือสัดส่วนของ **"กิ่งเงื่อนไขการตัดสินใจ (Branches: if, else, switch, while, for)"** ที่ชุดทดสอบทดสอบครบทั้งกรณีที่เป็น **จริง (True)** และ **เท็จ (False)**
  $$\text{Branch Coverage (\%)} = \left(\frac{\text{Covered Branches}}{\text{Total Branches}}\right) \times 100\%$$

---

### 1.2 ตัวอย่างจริงจากการทดลอง (กรณีศึกษา `Chart-1b`)
จากผลการรัน [statistics.csv](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/MIO_Algorithm/Result_Round2/raw_reports/Chart_1b_30s_s101/statistics.csv):
```csv
TARGET_CLASS,criterion,Coverage,Total_Goals,Covered_Goals
org.jfree.chart.renderer.category.AbstractCategoryItemRenderer,LINE;BRANCH,0.4805882652152903,848,401
```
* **Total Goals = 848:** ในคลาสนี้มีเป้าหมายที่ต้องพิสูจน์ (บรรทัด + กิ่งเงื่อนไข) รวมทั้งสิ้น 848 จุด
* **Covered Goals = 401:** อัลกอริทึม MIO สามารถสร้าง Test Case วิ่งเข้าไปทดสอบได้สำเร็จถึง 401 จุด
* **Coverage = 48.06%:** คิดเป็นเกือบครึ่งหนึ่งของคลาสขนาดใหญ่ระดับ Enterprise ซึ่งถือว่ามีประสิทธิภาพสูงมาก

---

### 1.3 ทำไม Coverage ถึงไม่เต็ม 100%?
เป็นเรื่องปกติของโค้ดในโลกความเป็นจริงครับ เพราะ:
1. **Unreachable Code (Dead Code):** โค้ดบางบรรทัดไม่มีทางเกิดขึ้นได้ในการทำงานจริง
2. **Defensive Programming / Hard Exceptions:** บล็อก `catch (OutOfMemoryError e)` หรือกรณีฮาร์ดแวร์ล้มเหลว ซึ่ง Unit Test จำลองได้ยากมาก
3. **Private Constructors & Utility Classes:** เมธอดบางตัวถูกล็อกการเข้าถึงจากภายนอก

---

### 1.4 การวิเคราะห์ความไวต่อเวลา (Search Budget Sensitivity) ตามเกณฑ์ 1.7
อาจารย์กำหนดให้เปรียบเทียบ **30s, 60s, 120s $\times$ 3 Seeds (101, 102, 103)** เพื่อพิสูจน์สมมติฐานทางวิชาการ:
> *"เมื่อให้อัลกอริทึม MIO มีเวลาในการค้นหา (Search Budget) เพิ่มขึ้น โครงสร้างชุดทดสอบจะครอบคลุมกิ่งเงื่อนไขที่ซับซ้อนได้ลึกขึ้น ทำให้ค่า Mean Coverage ค่อย ๆ เพิ่มขึ้น และมีค่าส่วนเบี่ยงเบนมาตรฐาน (SD) ที่แคบลง"*

ตัวอย่างผลลัพธ์จริงที่ระบบคำนวณและบันทึกลงใน [evosuite_budget_summary.csv](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv):
* **Budget 30s:** Mean Coverage = **`47.09% ± 2.97%`**
* **Budget 60s:** Mean Coverage = **`57.13% ± 3.05%`**
* **Budget 120s:** Mean Coverage = **`60.13% ± 2.09%`**  
*(เห็นได้ชัดว่ายิ่งให้เวลาค้นหามาก จาก 47% จะไต่ระดับขึ้นไปเป็น 60% อย่างมีนัยสำคัญ)*

---

## 🐛 บทที่ 2: Defects4J Ground Truth & การพิสูจน์ว่า "หาบั๊กเจอหรือไม่?"

### 2.1 Defects4J รู้ได้อย่างไรว่ามีบั๊ก? (Ground Truth)
Defects4J เป็นชุดข้อมูลมาตรฐานโลกที่รวบรวม **"บั๊กจริงที่เคยเกิดขึ้นจริงในประวัติศาสตร์ของซอฟต์แวร์ Open-Source"**
* ทุกบั๊กในโฟลเดอร์ [target_benchmark/](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/target_benchmark) จะมีไฟล์บันทึกประวัติเฉลย (Ground Truth) เช่น [target_benchmark/Chart_1b/defects4j_info.txt](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/target_benchmark/Chart_1b/defects4j_info.txt):
  ```text
  Summary for Bug: Chart-1
  Root cause in triggering tests:
   - org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::test2947660
     --> junit.framework.AssertionFailedError: expected:<1> but was:<0>
  List of modified sources:
   - org.jfree.chart.renderer.category.AbstractCategoryItemRenderer
  ```
* **ความหมาย:** Defects4J รู้แน่ชัดอยู่แล้วว่า ถ้าฟังก์ชันในคลาสนี้ทำงานถูกต้อง ผลลัพธ์ต้องได้ `1` แต่โค้ดเวอร์ชันที่มีบั๊ก (`buggy: 1b`) กลับส่งค่าออกมาเป็น `0`

---

### 2.2 นิยามเชิงวิชาการของการ "หาบั๊กเจอ" (Fault Detection Condition)
เราจะสรุปว่าชุดทดสอบที่ MIO ผลิตขึ้น **"ตรวจจับบั๊กเจอ (Fault Detected)"** ก็ต่อเมื่อผ่านเงื่อนไข **Differential Testing** ทั้ง 2 ข้อนี้:

$$\text{Fault Detected} \iff (\text{Run on Buggy } b \implies \mathbf{FAIL}) \land (\text{Run on Fixed } f \implies \mathbf{PASS})$$

```mermaid
flowchart TD
    TestSuite["Generated Test Suite<br/>(AbstractCategoryItemRenderer_ESTest.java)"]
    
    subgraph Test1["การทดสอบที่ 1: รันบน Buggy Version (Chart-1b)"]
        TestSuite --> RunB["Run on Buggy Code"]
        RunB --> ResultB{"ผลลัพธ์เป็นอย่างไร?"}
        ResultB -- "❌ FAIL (AssertionError)" --> Pass1["ผ่านเกณฑ์ข้อที่ 1 ✅<br/>(แปลว่าเทสกระตุ้นให้บั๊กแสดงตัว)"]
        ResultB -- "✅ PASS" --> Fail1["ไม่เจอบั๊ก ❌<br/>(เทสไม่ได้ตรวจจุดที่พัง)"]
    end

    subgraph Test2["การทดสอบที่ 2: รันบน Fixed Version (Chart-1f)"]
        Pass1 --> RunF["Run on Fixed Code (โค้ดที่แก้แล้ว)"]
        RunF --> ResultF{"ผลลัพธ์เป็นอย่างไร?"}
        ResultF -- "✅ PASS" --> FinalSuccess["🎉 FAULT DETECTED!<br/>(ชุดทดสอบจับบั๊กได้สำเร็จ 100%)"]
        ResultF -- "❌ FAIL" --> FinalFlaky["Flaky Test ⚠️<br/>(เทสเขียนผิดหรือเพี้ยนเอง)"]
    end
```

1. **เมื่อรันบนเวอร์ชัน Buggy (`Chart-1b`):** โค้ดเทสต้องเกิด **AssertionFailedError** หรือ **Exception** ตรงจุดที่เป็นบั๊ก (แสดงว่าเทสไปกระตุ้นจุดที่มีข้อบกพร่องได้จริง)
2. **เมื่อนำเทสชุดเดิมไปรันบนเวอร์ชัน Fixed (`Chart-1f`):** โค้ดเทสต้อง **ผ่านหมด (PASS)** (แสดงว่าข้อผิดพลาดในข้อ 1 เกิดจากตัวบั๊กของโปรแกรมจริง ไม่ใช่เกิดจากเทสเขียนมั่ว)

> 💡 **ข้อสังเกตสำคัญ:**  
> การได้ **High Coverage ไม่ได้รับประกันว่าต้องเจอบั๊กเสมอไป!** เพราะเทสเคสอาจวิ่งผ่านบรรทัดที่มีบั๊ก แต่ขาดคำสั่ง Assert ตรวจสอบค่าที่ผิด (Weak Oracle)  
> ดังนั้นในโครงงานนี้เราจึงต้องประเมินทั้ง **Coverage** และ **Fault Detection Rate (FDR)** ควบคู่กันเสมอ!

---

## 🧪 บทที่ 3: วิธีการทดสอบตรวจจับบั๊กด้วย Runner กลาง (How to Verify with Runner)

Member 4 ได้เตรียมเครื่องมือกลางสำหรับวัดผล Fault Detection ไว้แล้วใน [scripts/run_benchmark.py](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/scripts/run_benchmark.py)

เมื่อเราสั่งรัน MIO จนได้ไฟล์เทสใน `TestCode/<Project>_<Bug>b/` แล้ว เราสามารถทดสอบตรวจสอบได้ทันทีด้วยคำสั่ง:

```powershell
# คำสั่งตรวจสอบว่าชุดเทส MIO จับบั๊ก Chart-1 ได้หรือไม่:
python scripts/run_benchmark.py --technique mio --project Chart --bug 1
```

### สิ่งที่ Runner กลางจะทำในเบื้องหลัง:
1. คัดลอกไฟล์ `AbstractCategoryItemRenderer_ESTest.java` ของเราเข้าไปใน Defects4J Container
2. สั่งคอมไพล์และรันเทสบน `Chart-1b` (Buggy)
3. สั่งคอมไพล์และรันเทสบน `Chart-1f` (Fixed)
4. บันทึกผลลัพธ์ลงในไฟล์กลาง `results/benchmark_results.csv`:

| Project | Bug_ID | Technique | Line_Coverage_% | Branch_Coverage_% | Fault_Detection_Status | Failures_Count |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Chart | 1 | MIO (EvoSuite SBST) | 60.13% | 60.13% | **DETECTED** | 1 |

* หากขึ้นว่า **`DETECTED`**: แสดงว่าชุดทดสอบของ Member 2 จับบั๊กของ Defects4J ได้จริง!
* หากขึ้นว่า **`NOT_DETECTED`**: แสดงว่าชุดทดสอบครอบคลุมโค้ดได้ดี แต่ยังไม่สามารถกระตุ้นให้เกิด Assertion Error บนบั๊กตัวนั้นได้

---

## 📊 บทที่ 4: การนำผลลัพธ์ไปเขียนเล่มรายงาน (Final Report Alignment)

ผลลัพธ์ที่ Member 2 ผลิตขึ้นจะถูกนำไปใช้อย่างสง่างามใน 2 บทหลักของเล่มรายงานวิจัย:

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                       ผังการนำข้อมูลไปลงเล่มรายงาน                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. ตารางใน MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv    │
│     └──> นำไปใส่ใน 【บทที่ 2.2: การวิเคราะห์พารามิเตอร์ของอัลกอริทึม】   │
│          - แสดงตารางเปรียบเทียบ Mean ± SD ของ 30s, 60s, 120s            │
│          - พล็อต Bar Chart แสดงแนวโน้ม Coverage ที่เพิ่มขึ้นตามเวลา     │
│                                                                         │
│  2. ผลการรันใน results/benchmark_results.csv                            │
│     └──> นำไปใส่ใน 【บทที่ 4: การประเมินและเปรียบเทียบประสิทธิภาพ】       │
│          - เปรียบเทียบ Fault Detection Rate (FDR %) ระหว่าง 4 เทคนิค:   │
│            • MIO (EvoSuite SBST)                                        │
│            • IPO (Combinatorial Testing)                                │
│            • DeepSeek V4 Flash (AI-Assisted)                            │
│            • Gemini 3.8 Flash (AI-Assisted)                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## ❓ บทที่ 5: คำถามที่พบบ่อยสำหรับ Member 2 (MIO FAQ)

### Q1: ถ้าเทสเคสได้ Coverage ต่ำกว่า 30% ถือว่าล้มเหลวไหม?
> **ไม่ล้มเหลวครับ!** ในเชิงวิชาการ คลาสบางประเภทมี Dependencies สูงมาก (เช่น ต้องต่อ Database จริง หรือเปิด Window GUI จริง) ทำให้อัลกอริทึมอัตโนมัติทำได้ 20–30% ซึ่งถือเป็น **ผลการทดลองจริง (Empirical Finding)** ที่สามารถนำมาอภิปรายข้อจำกัด (Limitations) ในรายงานบทที่ 5 ได้อย่างดีเยี่ยมครับ

### Q2: ทำไมไฟล์ `_scaffolding.java` ถึงถูกสร้างขึ้นมาคู่กับ `_ESTest.java`?
> ไฟล์ Scaffolding คือไฟล์ควบคุมสภาพแวดล้อมจำลอง (Sandbox) ของ EvoSuite เพื่อป้องกันไม่ให้โค้ดเทสไปลบไฟล์จริงในเครื่อง หรือเปิด Socket กวนระบบเครือข่าย ทั้งสองไฟล์นี้จะทำงานคู่กันเสมอ

### Q3: เราต้องรัน `run_benchmark.py` เองทุกบั๊กหรือไม่?
> **ไม่ต้องรันเองทุกบั๊กครับ!** หน้าที่หลักของ Member 2 คือรันเจนเนอเรตเทสเคสด้วย [MIO_RUN_CHEATSHEET.md](file:///c:/Users/tanku/Documents/GitHub/claude-code-main/ProjectSQA/MIO_Algorithm/MIO_RUN_CHEATSHEET.md) แล้วเก็บสถิติลง `evosuite_budget_summary.csv` และส่งมอบไฟล์ใน `TestCode/`  
> ส่วนการสั่งรันวัดผลรวมทั้งโครงการจะเป็นหน้าที่ของ **Member 4 (Infra Lead)** ที่จะรันแบตช์ใหญ่เทียบผลของสมาชิกทุกคนพร้อมกันครับ!
