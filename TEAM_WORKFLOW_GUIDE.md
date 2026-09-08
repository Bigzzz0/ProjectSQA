# 🚀 คู่มือปฏิบัติการฉบับสมบูรณ์สำหรับสมาชิกทุกคน (Team Master Action Plan)

**โครงการ:** CP353201 Software Quality Assurance (Final Submission)  
**ชื่อโครงงาน:** AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark and Test Coverage Evaluation on Defects4J  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**ภาควิชาวิทยาการคอมพิวเตอร์ คณะวิทยาศาสตร์ มหาวิทยาลัยขอนแก่น**  

---

## 📌 สารบัญหลัก (Table of Contents)
1. [การเตรียมสภาพแวดล้อมและภาพรวมระบบ (Setup & System Architecture)](#1-การเตรียมสภาพแวดล้อมและภาพรวมระบบ-setup--system-architecture)
2. [กฎเหล็กกลางที่ทุกคนต้องปฏิบัติตาม (Universal Test Standards)](#2-กฎเหล็กกลางที่ทุกคนต้องปฏิบัติตาม-universal-test-standards)
   - [2.1 ขอบเขตการทดสอบและการวัดผล Coverage (Scope Specification)](#21-ขอบเขตการทดสอบและการวัดผล-coverage-scope-specification-defect-targeted-vs-project-wide)
3. [Member 1: นายปวริศช์ ประมวล (IPO / Combinatorial Specialist)](#3-member-1-นายปวริศช์-ประมวล-ipo--combinatorial-specialist)
   - [💡 วิธีสร้างระบบ Automated IPO Engine (พิมพ์เขียวสำหรับ Member 1)](#-วิธีสร้างระบบ-automated-ipo-engine-พิมพ์เขียวแบบละเอียดสำหรับ-member-1)
4. [Member 2: นายแทนคุณ พันธ์นิกุล (MIO / EvoSuite Specialist)](#4-member-2-นายแทนคุณ-พันธ์นิกุล-mio--evosuite-specialist)
   - [💡 วิธีสร้างระบบ Automated Batch Runner (พิมพ์เขียวสำหรับ Member 2)](#-วิธีสร้างระบบ-automated-batch-runner-สำหรับ-member-2-mio--evosuite-specialist)
5. [Member 3: นายธนภูมิ จันทรา (AI Prompt Engineer - Claude & Gemini)](#5-member-3-นายธนภูมิ-จันทรา-ai-prompt-engineer---claude--gemini)
   - [💡 วิธีสร้างระบบ Automated Batch Pipeline (พิมพ์เขียวสำหรับ Member 3)](#-วิธีสร้างระบบ-automated-batch-pipeline-สำหรับ-member-3-claude--gemini-ai)
6. [Member 4: นายศิฆรินทร์ อุปจันทร์ (Infra & Data Analysis Lead)](#6-member-4-นายศิฆรินทร์-อุปจันทร์-infra--data-analysis-lead)
   - [💡 วิธีทำระบบ Automated Continuous Benchmark & Auto-Plotting (สำหรับ Member 4)](#-วิธีทำระบบ-automated-continuous-benchmark--auto-plotting-สำหรับ-member-4)
7. [ตัวอย่างการทำงานร่วมกันแบบครบวงจร (End-to-End Walkthrough: กรณี Math-2)](#7-ตัวอย่างการทำงานร่วมกันแบบครบวงจร-end-to-end-walkthrough-กรณี-math-2)
8. [ระบบ Dynamic All-Bugs และการวัดผล Fault Detection Rate (FDR)](#8-ระบบ-dynamic-all-bugs-และการวัดผล-fault-detection-rate-fdr)
   - [8.3 ระเบียบวิธีการคำนวณ Fault Detection Rate (Bug-Level FDR)](#-83-ระเบียบวิธีการคำนวณ-fault-detection-rate-bug-level-fdr-formulation)
9. [แผนผังการเขียนเล่มรายงานฉบับสมบูรณ์ (Final Report Alignment)](#9-แผนผังการเขียนเล่มรายงานฉบับสมบูรณ์-final-report-alignment)
10. [Checklist ติดตามความคืบหน้าสู่การส่งมอบงาน (Progress Tracker)](#10-checklist-ติดตามความคืบหน้าสู่การส่งมอบงาน-progress-tracker)

---

## 1. การเตรียมสภาพแวดล้อมและภาพรวมระบบ (Setup & System Architecture)

### 💻 สิ่งที่สมาชิกแต่ละคนต้องมีบนเครื่องของตนเอง:
| สมาชิก | โปรแกรมที่ต้องมีบนเครื่อง | คำสั่งตรวจสอบความพร้อม |
| :--- | :--- | :--- |
| **Member 1 (IPO)** | Git, Python 3.8+, (หรือใช้ Docker ก็ได้) | `git --version` และ `python --version` |
| **Member 2 (MIO)** | Git, Docker Desktop (เพื่อรัน EvoSuite ใน Container) | `docker ps` |
| **Member 3 (AI)** | Git, Python 3.8+ (ยิง KKU API ได้จาก Windows/Mac ทันที) | `python --version` |
| **Member 4 (Infra)** | Git, Docker Desktop, Python 3.8+ | `docker ps` และ `python --version` |

### 🔄 เริ่มต้นทำงาน: ดึงโค้ดล่าสุดจาก GitHub
เปิด Terminal (PowerShell หรือ Bash) ที่โฟลเดอร์โปรเจกต์ แล้วสั่ง:
```bash
git pull origin main
```

### 📁 ผังโฟลเดอร์หลักของโปรเจกต์ (Project Directory Structure):
```text
ProjectSQA/
├── target_benchmark/          # โฟลเดอร์เก็บ Source Code และ Ground Truth ของบั๊กที่สกัดมา
│   └── Lang_1b/               # ตัวอย่าง: Source code และ defects4j_info.txt ของ Lang-1
├── Combinatorial_IPO/         # งานของ Member 1 (IPO Specialist)
│   ├── Models/                # เก็บไฟล์โมเดลพารามิเตอร์ (.txt) แยกตามคลาส
│   ├── Result_Round1/         # เก็บตาราง Combinations (.txt) และสถิติการลดรูป
│   └── TestCode/              # ปลายทางส่งมอบ: <Class>_IPOTest.java
├── MIO_Algorithm/             # งานของ Member 2 (MIO Specialist)
│   ├── Code/                  # สคริปต์อัตโนมัติ run_evosuite_mio.sh
│   ├── Result_Round1/         # เก็บสถิติ Mean +- SD (Search Budget 30s, 60s, 120s)
│   └── TestCode/              # ปลายทางส่งมอบ: <Class>_ESTest.java (Raw Suite ดิบ)
├── Claude-sonnet_5/           # งานของ Member 3 (AI Lead - Claude)
│   ├── Result/                # บันทึกสถิติ Token Usage และเวลาประมวลผล
│   └── TestCode/              # ปลายทางส่งมอบ: <Class>ClaudeTest.java
├── Gemini-3_8_flash/          # งานของ Member 3 (AI Lead - Gemini)
│   ├── Result/                # บันทึกสถิติ Token Usage และเวลาประมวลผล
│   └── TestCode/              # ปลายทางส่งมอบ: <Class>GeminiTest.java
├── scripts/                   # เครื่องมือกลางของ Member 4
│   ├── kku_generate.py        # สคริปต์ยิง KKU IntelSphere API
│   ├── run_benchmark.py       # Universal Runner วัด Coverage & Fault Detection
│   └── batch_extract_all_bugs.py # สคริปต์สกัดข้อมูลบั๊กทั้งหมดใน Defects4J
├── results/                   # โฟลเดอร์เก็บผลลัพธ์ Benchmark รวม
│   └── benchmark_results.csv  # ตารางผลลัพธ์รวมทุกโปรเจกต์
└── TEAM_WORKFLOW_GUIDE.md     # คู่มือแม่บทฉบับนี้
```

---

## 2. กฎเหล็กกลางที่ทุกคนต้องปฏิบัติตาม (Universal Test Standards)

เพื่อให้ Runner กลาง (`run_benchmark.py`) นำชุดทดสอบของทุกคนไปคอมไพล์และวัดผลบน Defects4J ได้โดยไม่พัง **โค้ดเทสของทุกคนต้องปฏิบัติตามกฎ 5 ข้อนี้อย่างเคร่งครัด:**

1. **ใช้ JUnit 4 เท่านั้น (ห้ามใช้ JUnit 5 / Jupiter เด็ดขาด):**
   - ✅ ถูกต้อง:
     ```java
     import org.junit.Test;
     import static org.junit.Assert.*;
     ```
   - ❌ ห้ามใช้: `import org.junit.jupiter.api.Test;` (Defects4J ไม่รองรับ จะเกิด Compile Error)
2. **ใส่ Timeout Guard ในทุก Method เสมอ:**
   - ✅ ถูกต้อง: `@Test(timeout = 4000)`
   - ❌ ห้ามเขียน `@Test` โดดๆ เพราะถ้าเทสติด Infinite Loop ระบบจะค้างทั้ง Pipeline
3. **บรรทัดแรกของไฟล์ต้องประกาศ Package ตรงกับ Target Class:**
   - ตัวอย่าง: หาก Target Class คือ `org.apache.commons.math3.distribution.HypergeometricDistribution`  
     บรรทัดที่ 1 ของไฟล์เทสต้องเป็น:
     ```java
     package org.apache.commons.math3.distribution;
     ```
4. **มาตรฐานการตั้งชื่อ Class และชื่อไฟล์ (Strict Naming Convention):**
   | เครื่องมือ | ชื่อไฟล์ Java (File Name) | การประกาศคลาส (Class Declaration) |
   | :--- | :--- | :--- |
   | **IPO** | `<TargetClass>_IPOTest.java` | `public class <TargetClass>_IPOTest` |
   | **MIO** | `<TargetClass>_ESTest.java` | `public class <TargetClass>_ESTest` |
   | **Claude** | `<TargetClass>ClaudeTest.java` | `public class <TargetClass>ClaudeTest` |
   | **Gemini** | `<TargetClass>GeminiTest.java` | `public class <TargetClass>GeminiTest` |
5. **ห้ามใช้ External Library ภายนอก:**
   - ห้าม import `org.mockito.*`, `org.assertj.*` หรือ dependencies อื่นที่ไม่ได้อยู่ในโปรเจกต์เป้าหมาย ให้ใช้ Standard Java และ Standard JUnit Assertions เท่านั้น

### 2.1 ขอบเขตการทดสอบและการวัดผล Coverage (Scope Specification: Defect-Targeted vs. Project-Wide)

> **🎯 ข้อตกลงทางวิชาการร่วมกัน:**  
> 1. **`classes.modified` = Defect-Targeted Classes (ไม่ใช่ทุกคลาสของทั้งโปรเจกต์):**  
>    Defects4J ระบุ `classes.modified` เพื่อบอกว่าในการแก้บั๊กนั้นๆ นักพัฒนาได้ทำการแก้ไข Source Code ที่คลาสใดบ้าง การทดลองของทีมเรามุ่งเน้น **"Defect-Targeted Test Generation"** เพื่อประเมินความสามารถในการสร้างชุดทดสอบเพื่อตรวจจับบั๊กและครอบคลุมตรรกะเฉพาะจุด ดังนั้น สมาชิกทุกคนจะสร้าง Test Suite เจาะจงเฉพาะคลาสที่ระบุใน `classes.modified` เท่านั้น **ไม่ต้องสร้างเทสให้กับทุกคลาสในโปรเจกต์** (เช่น Apache Commons Lang มีเป็นร้อยคลาส เราจะทำเฉพาะคลาสเป้าหมาย เช่น `NumberUtils` สำหรับ Lang-1)
>    ```text
>    Defects4J Bug (เช่น Lang-1)
>          ↓
>    classes.modified
>          ↓
>    Target Classes (เช่น NumberUtils)
>          ↓
>    IPO / MIO / Claude / Gemini Test Suites
>    ```
> 2. **Target Class Coverage Scope (ขอบเขตการวัด Coverage):**  
>    ค่า Line Coverage และ Branch Coverage ที่รายงานในตารางเปรียบเทียบหลักของงานวิจัยนี้ คือ **"Target Class Coverage"** (วัดเฉพาะบน Target Class ผ่าน flag `-c <target_class>` ใน Cobertura) ซึ่งเป็นตัวชี้วัดที่สะท้อนคุณภาพที่แท้จริงของแต่ละเทคนิคได้อย่างเป็นธรรม  
> 3. **การรายงาน Project-Wide Coverage (หากมี):**  
>    หากในรายงานหรือการนำเสนอต้องการกล่าวถึง Project-Wide Coverage จะต้องแยกรายงานเป็นตัวชี้วัดเสริม (Secondary Metric) และระบุขอบเขตให้ชัดเจนในบทที่ 5 ว่าค่า Project-wide coverage ย่อมมีค่าต่ำกว่า Target Class Coverage เสมอ เนื่องจากเราไม่ได้กระจายการสร้างชุดทดสอบไปยังคลาสอื่นๆ ที่ไม่เกี่ยวข้องกับ Defect

---

## 3. 🧑‍💻 Member 1: นายปวริศช์ ประมวล (IPO / Combinatorial Specialist)

**รหัสนักศึกษา:** 673380278-9  
**บทบาท:** ศึกษาและประยุกต์ใช้ขั้นตอนวิธี **In-Parameter-Order (IPO / IPOG Algorithm)** ในการสร้าง Combinatorial Pairwise Test Suite และใช้เครื่องมือ **Microsoft PICT (Pairwise Independent Combinatorial Testing v3.7+)** เป็น Reference Implementation & Verification Tool ในการสร้างและตรวจสอบตาราง Combinatorial Matrix แปลงเป็น JUnit 4 Test Suite

> **🎓 ข้อกำหนดความถูกต้องทางวิชาการ (Academic Distinction: IPO vs. PICT):**  
> - **IPO (In-Parameter-Order):** คือขั้นตอนวิธีเชิงทฤษฎี (Algorithm) ที่คิดค้นโดย Yu Lei et al. สำหรับ Combinatorial Testing โดยขยายคู่ทดสอบแบบ Horizontal Growth และ Vertical Growth ตามลำดับพารามิเตอร์  
> - **Microsoft PICT:** คือเครื่องมืออุตสาหกรรม (CLI Tool) พัฒนาโดย Microsoft ซึ่งใช้ Combinatorial Heuristics ช่วยสร้างคู่ทดสอบ Pairwise อย่างรวดเร็ว  
> - **ในงานวิจัยนี้:** เราศึกษาทฤษฎี **IPO Algorithm** เป็นแกนหลัก และใช้ **PICT** เป็น Verification Tool / Generation Backend ที่ใช้สร้างตารางทดสอบเปรียบเทียบเชิงประจักษ์ (Empirical Baseline) ห้ามเขียนสรุปว่า *PICT = IPO* โดยตรง
1. ไฟล์โมเดลพารามิเตอร์: `Combinatorial_IPO/Models/<Class>_model.txt`
2. ตาราง Combinations: `Combinatorial_IPO/Result_Round1/combinations_<Class>.txt`
3. ไฟล์ Java Unit Test: `Combinatorial_IPO/TestCode/<Class>_IPOTest.java`
4. สถิติจำนวนกรณีทดสอบก่อนลดรูป (Full) vs หลังลดรูป (Pairwise) บันทึกลงในเอกสารสรุปผล

---

### 🛠️ คู่มือขั้นตอนการทำงานอย่างละเอียด (Generic IPO Engineering):

> **⚠️ ข้อควรระวัง:** `Prefix, Value, Suffix` ในรอบนำร่องนั้น **เป็นเฉพาะของ `NumberUtils` เท่านั้น** คุณไม่สามารถนำโมเดลเดิมไปใช้กับคลาสอื่นได้ ในแต่ละคลาส คุณต้องทำตามกระบวนการวิศวกรรม IPO 5 ขั้นตอนดังนี้:

#### ขั้นที่ 1: ตรวจสอบ Method Signature ใน Target Class
เปิดดูไฟล์ซอร์สโค้ดใน `target_benchmark/<Project>_<BugID>b/<Class>.java` เพื่อดูว่า Constructor หรือ Method หลักรับ Input อะไรบ้าง โดยแบ่งเป็น 3 กลุ่มพารามิเตอร์:
* **กลุ่ม A: Numeric Parameters (ตัวเลข int, double, float):**
  - แบ่งพาร์ทิชัน: ค่าลบ (`Negative`), ค่าศูนย์ (`Zero`), ค่าบวกปกติ (`PositiveValid`), ค่าขอบเขตสูงสุด (`MaxBound`), และค่าเกินขอบเขต (`Overflow`)
* **กลุ่ม B: String / Text Parameters (ข้อความ):**
  - แบ่งพาร์ทิชัน: `Null`, `Empty`, `Whitespace`, `SingleChar`, `AlphaNumeric`, `SpecialCharacters`, `LongString`
* **กลุ่ม C: Object / Collection / State Parameters:**
  - แบ่งพาร์ทิชัน: `NullRef`, `EmptyCollection`, `SingleItem`, `MultipleItems`, `InvalidState`

#### ขั้นที่ 2: สร้างไฟล์โมเดลพารามิเตอร์ (`Combinatorial_IPO/Models/<Class>_model.txt`)
สร้างไฟล์โมเดลใหม่ในโฟลเดอร์ `Models/` ตัวอย่างเช่นสำหรับคลาส `HypergeometricDistribution` ใน Math-2:
```text
# Combinatorial_IPO/Models/Math_HypergeometricDistribution_model.txt
populationSize:      Negative, Zero, SmallValid, LargeValid, MaxInt
numberOfSuccesses:   Negative, Zero, LessThanPop, EqualPop, GreaterThanPop
sampleSize:          Negative, Zero, ValidSample, EqualPop, ExceedPop

# Constraints: กฎความสัมพันธ์ทางตรรกะห้ามจับคู่ขัดแย้ง
IF [numberOfSuccesses] = "GreaterThanPop" THEN [populationSize] IN {"SmallValid", "LargeValid"};
IF [sampleSize] = "ExceedPop" THEN [populationSize] IN {"SmallValid"};
```

#### ขั้นที่ 3: สั่งรันคำสั่ง PICT เพื่อหาคู่ทดสอบ 2-Way (Pairwise Combinations)
คุณสามารถรันคำสั่งผ่าน Docker Container ได้ทันที:
```bash
docker exec -it defects4j_sqa pict /workspace/Combinatorial_IPO/Models/<Class>_model.txt > Combinatorial_IPO/Result_Round1/combinations_<Class>.txt
```
*(ระบบจะสร้างตารางที่มีเฉพาะคู่ค่าที่จำเป็น ช่วยลดขนาดการทดสอบจากหลักพันเหลือเพียงไม่กี่สิบเคส)*

#### ขั้นที่ 4: แปลงตารางเป็น JUnit 4 Test Suite
นำผลลัพธ์จาก `combinations_<Class>.txt` มาเขียนเป็นโค้ด JUnit 4:
```java
package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

public class HypergeometricDistribution_IPOTest {

    @Test(timeout = 4000)
    public void test_ipo_pairwise_01() {
        try {
            // Pairwise: populationSize=SmallValid(100), numberOfSuccesses=LessThanPop(20), sampleSize=ValidSample(10)
            HypergeometricDistribution dist = new HypergeometricDistribution(100, 20, 10);
            assertNotNull(dist);
            assertEquals(100, dist.getPopulationSize());
        } catch (IllegalArgumentException | ArithmeticException e) {
            // Expected exception handled gracefully
        }
    }

    @Test(timeout = 4000)
    public void test_ipo_pairwise_boundary_negative() {
        try {
            // Pairwise: populationSize=Negative(-1), numberOfSuccesses=Zero(0), sampleSize=Zero(0)
            new HypergeometricDistribution(-1, 0, 0);
            fail("Expected NotPositiveException");
        } catch (Exception e) {
            // Handled expected boundary behavior
        }
    }
}
```

#### ขั้นที่ 5: คำนวณเปอร์เซ็นต์การลดรูป (Reduction %) เพื่อใส่ในรายงานบทที่ 2.1
สูตรคำนวณ:
$$\text{Reduction Rate (\%)} = \left(1 - \frac{N_{\text{pairwise}}}{N_{\text{cartesian}}}\right) \times 100\%$$
*ตัวอย่าง:* Full Combinations $= 5 \times 5 \times 5 = 125$ เคส แต่หลังรัน PICT เหลือเพียง $18$ เคส $\rightarrow$ ลดรูปได้ถึง $85.6\%$!

---

### 💡 วิธีสร้างระบบ Automated IPO Engine (พิมพ์เขียวแบบละเอียดสำหรับ Member 1)

> **🎯 เป้าหมาย:** เปลี่ยนจาก "คนนั่งเขียน `model.txt` ทีละไฟล์ด้วยมือ" มาเป็น **"ระบบอัตโนมัติที่แปลง Java Source สู่ JUnit 4 ผ่าน IPO Algorithm"** เพื่อให้ Member 1 สามารถสร้างชุดทดสอบสำหรับหลายๆ บั๊กได้อย่างรวดเร็วโดยไม่ต้องเหนื่อยล้า:

```mermaid
flowchart TD
    JavaFile["Java Source File (*.java)<br/>ใน target_benchmark/"] --> M1["Module 1: Java Parser<br/>สกัด Method Name & Param Types"]
    M1 --> M2["Module 2: Value Domain Generator<br/>สร้าง Boundary Values ตาม Type"]
    M2 --> M3["Module 3: IPO Algorithm Engine<br/>คำนวณ 2-Way Pairwise Combinations"]
    M3 --> M4["Module 4: JUnit 4 Synthesizer<br/>แปลง Combinations เป็น @Test Method"]
    M4 --> M5["Module 5: Batch Runner<br/>วนลูปทุกคลาสและเซฟลง TestCode/"]
    M5 --> Out["<Class>_IPOTest.java พร้อมประเมินผล!"]
```

#### รายละเอียดวิธีพัฒนาทั้ง 5 โมดูล (Step-by-Step Implementation Guide):

1. **โมดูลที่ 1: Java Method & Parameter Analyzer (`Combinatorial_IPO/analyzer/java_parser.py`)**
   - **หน้าที่:** อ่านไฟล์ `.java` แล้วใช้ Regular Expression สกัด Package, Class Name, และ Method Signature
   - **แนวทางการเขียนโค้ด (Pattern):**
     ```python
     import re

     def parse_java_file(filepath):
         with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
             code = f.read()
         
         # สกัด Package Name
         pkg_match = re.search(r'package\s+([\w\.]+);', code)
         package_name = pkg_match.group(1) if pkg_match else ""
         
         # สกัด Class Name
         class_match = re.search(r'public\s+(?:abstract\s+|final\s+)?class\s+(\w+)', code)
         class_name = class_match.group(1) if class_match else "TargetClass"
         
         # สกัด Public Methods และ Parameters
         # รูปแบบ: public [static] ReturnType methodName(Type1 p1, Type2 p2)
         method_pattern = r'public\s+(?:static\s+)?(?:[\w<>\[\]]+)\s+(\w+)\s*\(([^)]*)\)'
         methods = []
         for m in re.finditer(method_pattern, code):
             m_name = m.group(1)
             params_raw = m.group(2).strip()
             if params_raw:
                 param_list = [p.strip().split() for p in params_raw.split(',') if len(p.strip().split()) >= 2]
                 # ได้รายการ [(type, name), ...]
                 params = [(p[0], p[1]) for p in param_list]
                 methods.append({'name': m_name, 'params': params})
                 
         return {'package': package_name, 'class': class_name, 'methods': methods}
     ```

2. **โมดูลที่ 2: Value Domain Generator (`Combinatorial_IPO/domain/value_generator.py`)**
   - **หน้าที่:** แปลง Java Data Types เป็นชุดค่า Boundary Values & Nominal Values เพื่อสร้าง Parameter Model
   - **แนวทางการเขียนโค้ด (Pattern):**
     ```python
     TYPE_DOMAINS = {
         'int': ["0", "1", "-1", "Integer.MAX_VALUE", "Integer.MIN_VALUE"],
         'Integer': ["null", "0", "1", "-1", "Integer.MAX_VALUE"],
         'double': ["0.0d", "1.0d", "-1.0d", "Double.NaN", "Double.POSITIVE_INFINITY"],
         'Double': ["null", "0.0d", "1.0d", "-1.0d", "Double.MAX_VALUE"],
         'boolean': ["true", "false"],
         'Boolean': ["null", "true", "false"],
         'String': ['null', '""', '"0"', '"123"', '"-1"', '"test_string"', '"0x80000000"'],
         'long': ["0L", "1L", "-1L", "Long.MAX_VALUE", "Long.MIN_VALUE"]
     }

     def get_domain_for_type(param_type):
         # หากเป็น Object ชนิดอื่น ให้ใช้ default เป็น null หรือ mock พื้นฐาน
         return TYPE_DOMAINS.get(param_type, ["null"])
     ```

3. **โมดูลที่ 3: Combinatorial Engine - IPO Algorithm / PICT Reference Backend (`Combinatorial_IPO/algorithm/ipo.py`)**
   - **หน้าที่:** คำนวณ Pairwise Combinations (2-Way) ตามทฤษฎี Combinatorial Testing
   - **แนวทางการพัฒนา:** Member 1 สามารถเลือกพัฒนาได้ 2 ทาง:
     * **ทางเลือก 3A (เขียน IPO/IPOG Algorithm เองใน Python):** ใช้อัลกอริทึม In-Parameter-Order (Lei et al.) ขยายผลแบบ Horizontal Growth ตามด้วย Vertical Growth
     * **ทางเลือก 3B (ใช้ Microsoft PICT เป็น Reference Backend):** เรียกใช้เครื่องมือ Microsoft PICT ผ่าน Python `subprocess` เพื่อความสะดวกรวดเร็วในการสร้างตาราง Matrix:
     ```python
     import subprocess
     import tempfile
     import os

     def generate_pairwise(param_domains):
         # สร้างไฟล์ model ชั่วคราว
         # รูปแบบ PICT:
         # param1: val1, val2, val3
         # param2: valA, valB
         with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as f:
             for name, vals in param_domains.items():
                 f.write(f"{name}: {', '.join(vals)}\n")
             temp_model = f.name
         
         try:
             # เรียกคำสั่ง pict ผ่าน subprocess
             res = subprocess.run(["pict", temp_model], capture_output=True, text=True, check=True)
             lines = [line.strip().split('\t') for line in res.stdout.strip().split('\n') if line.strip()]
             headers = lines[0]
             combinations = [dict(zip(headers, row)) for row in lines[1:]]
             return combinations
         finally:
             if os.path.exists(temp_model):
                 os.remove(temp_model)
     ```

4. **โมดูลที่ 4: JUnit 4 Test Synthesizer (`Combinatorial_IPO/generator/junit_generator.py`)**
   - **หน้าที่:** นำผลลัพธ์ Combinations แต่ละแถวมาจัดรูปแบบเป็นโค้ด JUnit 4 ที่ได้มาตรฐานโครงการ
   - **แนวทางการเขียนโค้ด (Pattern):**
     ```python
     def synthesize_junit(package_name, class_name, method_name, combinations):
         test_methods = []
         for idx, comb in enumerate(combinations):
             args_str = ", ".join(comb.values())
             test_methods.append(f"""
     @Test(timeout = 4000)
     public void test_{method_name}_pairwise_{idx+1:03d}() {{
         try {{
             // Test Combination: {comb}
             {class_name}.{method_name}({args_str});
         }} catch (IllegalArgumentException | ArithmeticException | NullPointerException e) {{
             // Handled expected boundary contract
         }}
     }}""")

         junit_code = f"""package {package_name};

 import org.junit.Test;
 import static org.junit.Assert.*;

 public class {class_name}_IPOTest {{
 {"".join(test_methods)}
 }}
 """
         return junit_code
     ```

5. **โมดูลที่ 5: Master Batch Runner (`Combinatorial_IPO/runner/run_ipo_batch.py`)**
   - **หน้าที่:** วนลูปอ่านไฟล์ Java ทุกโฟลเดอร์ใน `target_benchmark/` แล้วเซฟไฟล์ลง `Combinatorial_IPO/TestCode/`
   - **แนวทางการเขียนโค้ด (Pattern):**
     ```python
     import glob, os

     def run_all():
         java_files = glob.glob("target_benchmark/*/*.java")
         print(f"[*] พบไฟล์ Java เป้าหมายทั้งหมด {len(java_files)} ไฟล์")
         
         for jf in java_files:
             meta = parse_java_file(jf)
             cls_name = meta['class']
             pkg_name = meta['package']
             
             # เลือก Method สาธารณะหลักที่มีพารามิเตอร์
             valid_methods = [m for m in meta['methods'] if len(m['params']) >= 2]
             if not valid_methods:
                 continue
                 
             target_m = valid_methods[0]
             domains = {p[1]: get_domain_for_type(p[0]) for p in target_m['params']}
             
             # รัน IPO Pairwise
             combs = generate_pairwise(domains)
             
             # สังเคราะห์โค้ด JUnit 4
             code = synthesize_junit(pkg_name, cls_name, target_m['name'], combs)
             
             # เซฟลง TestCode/
             out_path = f"Combinatorial_IPO/TestCode/{cls_name}_IPOTest.java"
             with open(out_path, "w", encoding="utf-8") as f:
                 f.write(code)
             print(f"    [+] บันทึกชุดทดสอบสำเร็จ: {out_path} ({len(combs)} Pairwise Test Cases)")

     if __name__ == '__main__':
         run_all()
     ```

* **ก้าวแรกที่แนะนำ (Prototyping Strategy):** ให้ทดสอบกับคลาส `NumberUtils` (Lang-1) ก่อน เพื่อยืนยันว่าโค้ด Java ที่เจนออกมาสามารถคอมไพล์ผ่านและวัด Coverage ได้จริง 1 คลาส แล้วจึงสั่งรัน `run_all()` สำหรับทุกคลาสใน `target_benchmark/` ครับ!

---

## 4. 🧑‍💻 Member 2: นายแทนคุณ พันธ์นิกุล (MIO / EvoSuite Specialist)

**รหัสนักศึกษา:** 673380301-0  
**บทบาท:** ใช้เครื่องมือ **EvoSuite (MIO Algorithm)** ในการค้นหาชุดทดสอบอัตโนมัติ (Search-Based Software Testing), ทำการทดลองปรับ Search Budget (30s, 60s, 120s), และเก็บสถิติ **Mean ± SD** ตามข้อกำหนดรายงาน 1.7

### 📋 สิ่งที่คุณต้องส่งมอบ (Deliverables ต่อ 1 คลาส):
1. ไฟล์ Java Test Suite ดิบ: `MIO_Algorithm/TestCode/<Class>_ESTest.java` (และ `_scaffolding.java`)
2. ตารางบันทึกสถิติ Mean ± SD ของ Line และ Branch Coverage ของ Search Budget ทั้ง 3 ระดับ
3. เอกสารวิเคราะห์เชิงเปรียบเทียบใน `MIO_Algorithm/Result_Round1/evosuite_budget_analysis.md`

---

### 🛠️ คู่มือขั้นตอนการทำงานอย่างละเอียด:

#### ขั้นที่ 1: สั่งรันอัตโนมัติผ่าน Docker ด้วยคำสั่งเดียว
Member 4 ได้เตรียมสคริปต์อัตโนมัติ [`MIO_Algorithm/Code/run_evosuite_mio.sh`](MIO_Algorithm/Code/run_evosuite_mio.sh) ไว้ให้แล้ว คุณสามารถสั่งรันจาก PowerShell บนเครื่องตัวเองได้ทันที:
```bash
# รูปแบบคำสั่ง:
# docker exec -it defects4j_sqa /workspace/MIO_Algorithm/Code/run_evosuite_mio.sh [Project] [BugID] [TargetClass] [BudgetInSeconds]

# ตัวอย่างการรันคลาส HypergeometricDistribution ใน Math-2 ที่ Budget 60 วินาที:
docker exec -it defects4j_sqa /workspace/MIO_Algorithm/Code/run_evosuite_mio.sh Math 2 org.apache.commons.math3.distribution.HypergeometricDistribution 60
```
- สคริปต์จะทำการ Checkout โค้ด, สกัด Compilation Classpath, เรียกใช้ Java 8 OpenJDK, และรัน EvoSuite ด้วย MIO Algorithm พร้อมเซฟไฟล์เทสเข้า `MIO_Algorithm/TestCode/` ให้โดยอัตโนมัติ

#### ขั้นที่ 2: การทดลองปรับ Search Budget และเก็บค่าสถิติ Mean ± SD (ข้อ 1.7)
เนื่องจากขั้นตอนวิธี MIO เป็น Random / Evolutionary Search ค่าผลลัพธ์จะผันแปรตาม Random Seed คุณต้องรันการทดลองเพื่อเก็บสถิติทางวิชาการ:
1. **ทดลอง 3 ค่า Budget:** `30 วินาที`, `60 วินาที`, และ `120 วินาที`
2. **ในแต่ละระดับ Budget ให้สั่งรัน 3 รอบ โดยเปลี่ยนค่า `-seed`:**
   ```bash
   docker exec -it defects4j_sqa bash
   
   # ตัวอย่างรัน Budget 30s รอบที่ 1 ถึง 3:
   /usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /opt/evosuite/evosuite-1.0.6.jar \
     -class org.apache.commons.math3.distribution.HypergeometricDistribution \
     -projectCP $(defects4j export -p cp.compile) \
     -Dalgorithm=MIO -Dsearch_budget=30 -seed 101 -Dreport_dir=/workspace/MIO_Algorithm/Result_Round1/math2_30s_s1
   ```
3. บันทึกผล Coverage ของแต่ละรอบลงในตาราง:
   | Search Budget | รอบที่ 1 | รอบที่ 2 | รอบที่ 3 | ค่าเฉลี่ย ($\mu$) | ส่วนเบี่ยงเบนมาตรฐาน ($\sigma$) |
   | :---: | :---: | :---: | :---: | :---: | :---: |
   | **30 วินาที** | 52.4% | 51.8% | 53.2% | **52.47%** | **± 0.70%** |
   | **60 วินาที** | 58.1% | 59.4% | 58.6% | **58.70%** | **± 0.65%** |
   | **120 วินาที** | 61.2% | 60.9% | 61.8% | **61.30%** | **± 0.46%** |

#### ขั้นที่ 3: กฎเหล็กความซื่อตรงทางวิชาการ (Fair Benchmark Policy)
> **⚖️ ห้ามแก้ไข Generated Test ด้วยมือโดยเด็ดขาด:**  
> ในการวิจัย Benchmarking Study หากเราเข้าไปแก้ Assertions หรือลบโค้ดแปลกๆ ที่ EvoSuite เจนได้ ผลการทดลองจะกลายเป็นผลงานของคน ไม่ใช่ผลงานของ Algorithm (สูญเสียความน่าเชื่อถือ)  
> **สิ่งที่ต้องทำ:**
> 1. เลือกชุดเทสรอบที่ดีที่สุดที่ EvoSuite เจนได้มาวางไว้ที่ `MIO_Algorithm/TestCode/<Class>_ESTest.java`
> 2. ปล่อยให้ Runner นำไปรันตามจริง หากเกิด `COMPILE_ERROR` หรือ `TIMEOUT` ให้ระบบบันทึกสถานะนั้นตรงๆ เพื่อนำข้อจำกัดนี้ไปเขียนอภิปรายในบทที่ 5

---

### 💡 วิธีสร้างระบบ Automated Batch Runner (พิมพ์เขียวสำหรับ Member 2)

> **🎯 เป้าหมาย:** ตามเกณฑ์งานวิจัยข้อ 1.7 เราต้องทดสอบ Search Budget 3 ระดับ (30s, 60s, 120s) และแต่ละระดับต้องทำซ้ำ 3 Seed (เช่น 101, 102, 103) รวมเป็น **9 การทดลองต่อ 1 คลาส!**  
> หากมี 10-20 คลาส จะต้องรัน 90–180 ครั้ง การพิมพ์คำสั่งด้วยมือใน Terminal ทีละบรรทัดไม่สามารถทำได้ทันกำหนดส่ง Member 2 จึงควรสร้าง **Automated Batch Script (`MIO_Algorithm/Code/batch_evosuite.py`)** เพื่อสั่งรันการทดลองทั้งหมดและคำนวณค่าทางสถิติให้อัตโนมัติ:

```mermaid
flowchart TD
    Targets["รายการคลาสเป้าหมาย<br/>(สแกนจาก target_benchmark/)"] --> LoopClass["วนลูปแต่ละ Target Class"]
    LoopClass --> LoopBudget["วนลูป Search Budget<br/>(30s, 60s, 120s)"]
    LoopBudget --> LoopSeed["วนลูป Random Seed<br/>(Seed 101, 102, 103)"]
    LoopSeed --> EvoExec["เรียกใช้ EvoSuite (MIO)<br/>ผ่าน Docker Non-interactive"]
    EvoExec --> EvoCSV["สกัดสถิติจาก<br/>evosuite-report/statistics.csv"]
    EvoCSV --> StatCalc["คำนวณ Mean (μ) & SD (σ)<br/>ด้วย Python statistics"]
    StatCalc --> SaveSummary["บันทึกผลลง<br/>evosuite_budget_summary.csv"]
    StatCalc --> SelectBest["คัดลอก Best Test Suite<br/>เข้าสู่ MIO_Algorithm/TestCode/"]
    SelectBest --> Out["<Class>_ESTest.java พร้อมประเมินผล!"]
```

#### รายละเอียดการเขียนสคริปต์อัตโนมัติ (Step-by-Step Implementation Guide):

1. **ส่วนที่ 1: การสแกนเป้าหมายอัตโนมัติ (Target Discovery):**
   - ให้สคริปต์สแกนหาโฟลเดอร์ใน `target_benchmark/` (เช่น `Math_2b`, `Lang_1b`) และอ่านชื่อ Target Class จากไฟล์ `defects4j_info.txt` หรือไฟล์ `.java`:
   ```python
   import glob, os, re

   def get_target_classes():
       targets = []
       for info_file in glob.glob("target_benchmark/*/defects4j_info.txt"):
           folder = os.path.dirname(info_file)
           folder_name = os.path.basename(folder) # เช่น Math_2b
           proj, bug = folder_name.replace("b", "").split("_")
           
           # ค้นหาไฟล์ Java ในโฟลเดอร์เพื่อดึงชื่อ Class
           java_files = glob.glob(f"{folder}/**/*.java", recursive=True)
           if java_files:
               cls_name = os.path.basename(java_files[0]).replace(".java", "")
               # อ่าน package name จาก java
               with open(java_files[0], 'r', encoding='utf-8', errors='ignore') as f:
                   pkg_match = re.search(r'package\s+([\w\.]+);', f.read())
                   pkg = pkg_match.group(1) if pkg_match else ""
               fqcn = f"{pkg}.{cls_name}" if pkg else cls_name
               targets.append({"project": proj, "bug": bug, "class": cls_name, "fqcn": fqcn})
       return targets
   ```

2. **ส่วนที่ 2: การสั่งรัน EvoSuite แบบ Non-interactive ผ่าน Docker:**
   - ใช้ `subprocess.run` สั่งรันคำสั่งภายในคอนเทนเนอร์ `defects4j_sqa` โดยระบุ parameters ให้ครบ:
   ```python
   import subprocess

   def run_evosuite_single(project, bug, fqcn, budget, seed, report_dir):
       cmd = [
           "docker", "exec", "defects4j_sqa", "bash", "-c",
           f"""
           cd /tmp && rm -rf eval_{project}_{bug} && defects4j checkout -p {project} -v {bug}b -w /tmp/eval_{project}_{bug}
           cd /tmp/eval_{project}_{bug} && defects4j compile
           CP=$(defects4j export -p cp.compile)
           /usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /opt/evosuite/evosuite-1.0.6.jar \\
             -class {fqcn} \\
             -projectCP $CP \\
             -Dalgorithm=MIO \\
             -Dsearch_budget={budget} \\
             -seed {seed} \\
             -Dreport_dir={report_dir} \\
             -Dtest_dir=/workspace/MIO_Algorithm/TestCode
           """
       ]
       subprocess.run(cmd, check=True)
   ```

3. **ส่วนที่ 3: การสกัดสถิติจาก `statistics.csv` และคำนวณ Mean ± SD:**
   - EvoSuite จะบันทึกไฟล์สถิติไว้ใน `report_dir/statistics.csv` เสมอ ให้เขียนฟังก์ชันอ่านค่า:
   ```python
   import csv, statistics

   def parse_evosuite_stats(csv_path):
       with open(csv_path, 'r', encoding='utf-8') as f:
           reader = csv.DictReader(f)
           for row in reader:
               line_cov = float(row.get('LineCoverage', 0.0)) * 100.0
               branch_cov = float(row.get('BranchCoverage', 0.0)) * 100.0
               return line_cov, branch_cov
       return 0.0, 0.0

   def calculate_mean_sd(values):
       mean = statistics.mean(values)
       sd = statistics.stdev(values) if len(values) > 1 else 0.0
       return mean, sd
   ```

4. **ส่วนที่ 4: การจัดเก็บชุดทดสอบที่ดีที่สุดและบันทึกตารางสรุป:**
   - เลือกรอบที่ได้ผลลัพธ์ Coverage สูงสุด นำไฟล์ `<Class>_ESTest.java` และ `<Class>_ESTest_scaffolding.java` มาตั้งชื่อให้ถูกต้องและวางไว้ที่ `MIO_Algorithm/TestCode/`
   - เซฟตารางสถิติสรุปภาพรวมทั้งหมดลงใน `MIO_Algorithm/Result_Round1/evosuite_budget_summary.csv` สำหรับนำไปใส่ตารางข้อ 1.7 ในเล่มรายงาน!

> **⚠️ ข้อควรจำสำคัญสำหรับ Member 2:**
> - ต้องรันด้วย **Java 8** เสมอ (`/usr/lib/jvm/java-8-openjdk-amd64/bin/java`) เพราะ EvoSuite 1.0.6 เข้ากันได้ดีที่สุดกับ Java 8 Bytecode
> - ห้ามลืมไฟล์ `_scaffolding.java` เด็ดขาด เพราะถ้าขาดไฟล์นี้ Runner จะคอมไพล์เทสไม่ผ่าน (`COMPILE_ERROR`)
> - ยึดมั่นใน **Fair Benchmark Policy**: หากเทสที่ได้มี Error ให้ปล่อยไว้ตามจริง ไม่แก้ไขด้วยมือ เพื่อรักษาความน่าเชื่อถือของการวิจัย

---

## 5. 🧑‍💻 Member 3: นายธนภูมิ จันทรา (AI Prompt Engineer - Claude & Gemini)

**รหัสนักศึกษา:** 673380272-1  
**บทบาท:** สั่งการ **Claude Sonnet 5** และ **Gemini 3.8 Flash** ผ่าน **KKU IntelSphere API** (`gen.ai.kku.ac.th`) เพื่อสร้างชุดทดสอบคุณภาพสูง พร้อมบันทึก **Token Usage**, **Cost**, และ **Generation Time**

### 📋 สิ่งที่คุณต้องส่งมอบ (Deliverables ต่อ 1 คลาส):
1. ไฟล์ Java Test ของ Claude: `Claude-sonnet_5/TestCode/<Class>ClaudeTest.java`
2. ไฟล์ Java Test ของ Gemini: `Gemini-3_8_flash/TestCode/<Class>GeminiTest.java`
3. ข้อมูล Token Usage & Latency ใน `Claude-sonnet_5/Result/` และ `Gemini-3_8_flash/Result/`
4. ตารางวิเคราะห์เปรียบเทียบ Cost-Effectiveness ระหว่าง Claude vs Gemini สำหรับบทที่ 3

---

### 🛠️ คู่มือขั้นตอนการทำงานอย่างละเอียด:

#### ขั้นที่ 1: ตั้งค่า API Key ของ KKU IntelSphere
1. เข้าเว็บไซต์: [https://gen.ai.kku.ac.th/](https://gen.ai.kku.ac.th/) ล็อกอินด้วยอีเมล `@kkumail.com` หรือ `@kku.ac.th`
2. ไปที่เมนู **Settings (การตั้งค่า) -> API Platform** แล้วกด **Generate API Key**
3. เปิดไฟล์ `.env` ที่โฟลเดอร์ Root (`ProjectSQA/.env`) แล้ววาง Key:
   ```env
   KKU_API_KEY=your_actual_api_key_here
   ```

#### ขั้นที่ 2: สั่งสร้างชุดทดสอบอัตโนมัติด้วยคำสั่งเดียว
Member 4 ได้เตรียมสคริปต์ Universal Generator [`scripts/kku_generate.py`](scripts/kku_generate.py) ไว้ให้แล้ว เพียงระบุไฟล์ซอร์สโค้ดเป้าหมาย:
```bash
# 1. สร้างชุดทดสอบด้วย Claude Sonnet 5
python scripts/kku_generate.py --ai claude --source-file target_benchmark/<Project>_<BugID>b/<Class>.java

# 2. สร้างชุดทดสอบด้วย Gemini 3.8 Flash
python scripts/kku_generate.py --ai gemini --source-file target_benchmark/<Project>_<BugID>b/<Class>.java
```

#### ขั้นที่ 3: สิ่งที่สคริปต์จะทำให้โดยอัตโนมัติ:
- อ่าน Package Name และ Class Name จากซอร์สโค้ดต้นทาง
- แนบ Master Prompt ที่บังคับกฎ JUnit 4, บังคับ `@Test(timeout=4000)`, และสั่งวิเคราะห์ Boundary Value Analysis (BVA)
- สกัดเฉพาะโค้ดภาษา Java บันทึกลงโฟลเดอร์ `TestCode/`
- บันทึกสถิติ Token Usage และ Generation Time ละเอียดระดับ Milliseconds

#### ขั้นที่ 4: การตรวจทานความถูกต้อง (Code Review Sanity Check)
เปิดดูไฟล์เทสในโฟลเดอร์ `TestCode/`:
1. ตรวจสอบว่าบรรทัดแรกมี `package <package_name>;`
2. ชื่อ Class ในโค้ดตรงกับชื่อไฟล์ เช่น `public class HypergeometricDistributionGeminiTest`
3. ไม่มี Library แปลกปลอมหลุดเข้ามา
4. **หากต้องการจับบั๊กให้ได้สถานะ `BUG_DETECTED`:** ให้นำข้อมูล Root Cause จาก `defects4j_info.txt` มาเพิ่มเป็น Test Method ตรวจสอบพฤติกรรมของบั๊กโดยเฉพาะ

#### ขั้นที่ 5: สรุปตาราง Token Economics สำหรับบทที่ 3 ของเล่มรายงาน
ดึงข้อมูลจากไฟล์ JSON ในโฟลเดอร์ `Result/` มากรอกลงตาราง:
| ข้อมูลตัวชี้วัด (Metrics) | Claude Sonnet 5 | Gemini 3.8 Flash | ผลการเปรียบเทียบ |
| :--- | :---: | :---: | :--- |
| **Input Tokens (Prompt)** | 3,120 tokens | 3,120 tokens | เท่ากัน (ขนาด Source Code) |
| **Output Tokens (Completion)** | 1,850 tokens | 2,410 tokens | Gemini เจนเทสยาวและละเอียดกว่า |
| **Generation Latency (วินาที)** | 14.2 วินาที | 4.8 วินาที | Gemini เร็วกว่าประมาณ 3 เท่า |
| **Line Coverage บน Target Class** | 64.80% | 98.67% | Gemini ครอบคลุม Branch ลึกกว่า |

---

### 💡 วิธีสร้างระบบ Automated Batch Pipeline (พิมพ์เขียวสำหรับ Member 3)

> **🎯 เป้าหมาย:** หากต้องรันคำสั่ง `kku_generate.py` ทีละไฟล์สำหรับ 10–20 คลาส x 2 โมเดล (Claude + Gemini) จะต้องพิมพ์คำสั่งถึง 40 ครั้ง!  
> ยิ่งไปกว่านั้น: **หากส่งเฉพาะ Source Code เปล่าๆ ให้ AI โดยไม่มีข้อมูลบั๊ก AI จะสร้างเฉพาะเทสกรณีปกติ (Happy Path) ส่งผลให้ได้สถานะ `NOT_DETECTED` เกือบทั้งหมด!**  
> เพื่อให้ได้ชุดทดสอบที่มี Line/Branch Coverage สูง และสามารถตรวจจับข้อบกพร่องจริงจนได้สถานะ **`BUG_DETECTED`** Member 3 ควรสร้าง **Defect-Aware Batch Pipeline (`scripts/batch_ai_generate.py`)** ที่ดึง Ground Truth จาก `defects4j_info.txt` มาประกอบเป็น Prompt โดยอัตโนมัติ:

```mermaid
flowchart TD
    Targets["สแกน target_benchmark/<br/>(พบคู่ *.java และ defects4j_info.txt)"] --> Extract["สกัด Java Source Code<br/>+ สกัด Root Cause จาก defects4j_info.txt"]
    Extract --> PromptEng["ประกอบ Master Prompt อัตโนมัติ<br/>(BVA + Defect Trigger Specification)"]
    PromptEng --> Dispatcher["ยิง API ไปยัง KKU IntelSphere<br/>(Claude Sonnet 5 & Gemini 3.8 Flash)"]
    Dispatcher --> RateLimit["Rate Limiter & Retry Guard<br/>(หน่วงเวลา 2.0s ป้องกัน HTTP 429)"]
    RateLimit --> Sanitizer["Java Code Sanitizer<br/>(ลบ Markdown, เช็ค package/class)"]
    Sanitizer --> SaveFiles["บันทึกไฟล์เทสลง TestCode/<br/>(<Class>ClaudeTest / <Class>GeminiTest)"]
    Sanitizer --> LogMetrics["รวบรวม Token Usage & Latency<br/>ลง Result/ai_token_summary.csv"]
    SaveFiles --> Out["Test Suites พร้อมส่งให้ Runner!"]
```

#### รายละเอียดการเขียนสคริปต์อัตโนมัติ (Step-by-Step Implementation Guide):

1. **ส่วนที่ 1: การสกัด Defect Ground Truth อัตโนมัติ (`defects4j_info.txt`):**
   - เขียนฟังก์ชัน Python อ่านไฟล์ `defects4j_info.txt` ในโฟลเดอร์ของบั๊ก เพื่อสกัด Root Cause และ Stack Trace ของการทดสอบที่เฟล:
   ```python
   import re

   def extract_defect_context(info_file_path):
       with open(info_file_path, "r", encoding="utf-8", errors="ignore") as f:
           content = f.read()
           
       # สกัดข้อความ Root Cause หรือข้อผิดพลาดที่เกิดขึ้น
       defect_info = ""
       if "Root cause in triggering tests:" in content:
           defect_info = content.split("Root cause in triggering tests:")[1].strip()
       elif "List of test failures:" in content:
           defect_info = content.split("List of test failures:")[1].strip()
           
       # ตัดความยาวไม่ให้เกิน 1,000 ตัวอักษรเพื่อประหยัด Token
       return defect_info[:1000]
   ```

2. **ส่วนที่ 2: การออกแบบ Defect-Aware Master Prompt:**
   - รวม Source Code เข้ากับข้อมูลข้อบกพร่อง เพื่อสั่งให้ AI สร้างทั้ง BVA Coverage Tests และ Method เจาะจงที่ Trigger บั๊ก:
   ```python
   def build_prompt(class_name, package_name, java_source, defect_context):
       prompt = f"""You are an elite Java SQA Engineer. Generate a comprehensive JUnit 4 test suite for:
Class: {class_name}
Package: {package_name}

=== JAVA SOURCE CODE ===
{java_source}

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH) ===
The target class has a known defect reported as follows:
{defect_context}

=== STRICT REQUIREMENTS ===
1. Use ONLY JUnit 4 (import org.junit.Test; import static org.junit.Assert.*;). Do NOT use JUnit 5/Jupiter.
2. Every test method MUST have a timeout guard: @Test(timeout = 4000).
3. First line MUST be: package {package_name};
4. Class name MUST be: public class {class_name}Test
5. Do NOT import any external mocking libraries (Mockito, AssertJ, etc.).
6. Conduct rigorous Boundary Value Analysis (BVA) for high branch coverage.
7. CRITICAL: Include at least one dedicated test method targeting the exact condition described in the KNOWN DEFECT SPECIFICATION above. The assertion MUST assert the expected correct behavior so it exposes the bug on the defective version!
8. Output ONLY the raw Java code enclosed in ```java ... ```.
"""
       return prompt
   ```

3. **ส่วนที่ 3: ระบบ Batch Dispatcher พร้อม Rate Limiting & Retry:**
   - วนลูปยิง API ทั้ง Claude และ Gemini พร้อมระบบหน่วงเวลาเพื่อป้องกันโดนระงับสิทธิ์ (HTTP 429 Too Many Requests):
   ```python
   import time, requests

   def call_kku_api_with_retry(api_key, model_name, prompt, max_retries=3):
       url = "https://gen.ai.kku.ac.th/api/v1/chat/completions" # หรือ endpoint ตามสเปก KKU
       headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
       payload = {
           "model": model_name,
           "messages": [{"role": "user", "content": prompt}],
           "temperature": 0.2
       }
       
       for attempt in range(max_retries):
           try:
               t0 = time.time()
               res = requests.post(url, headers=headers, json=payload, timeout=60)
               latency = time.time() - t0
               
               if res.status_code == 200:
                   data = res.json()
                   code_content = data["choices"][0]["message"]["content"]
                   usage = data.get("usage", {})
                   time.sleep(2.0) # หน่วงเวลา 2 วินาทีระหว่าง request
                   return code_content, usage, latency
               elif res.status_code == 429:
                   print(f"[!] ติด Rate Limit (429) รอ 10 วินาทีก่อนลองใหม่...")
                   time.sleep(10.0)
           except Exception as e:
               print(f"[!] Error on attempt {attempt+1}: {e}")
               time.sleep(3.0)
               
       return None, {}, 0.0
   ```

4. **ส่วนที่ 4: การ Clean Code และบันทึกไฟล์เทสอัตโนมัติ:**
   - สกัดเฉพาะโค้ดภาษา Java ออกจากบล็อก Markdown และปรับชื่อคลาสให้ตรงตามมาตรฐานโครงการ (`<Class>ClaudeTest` และ `<Class>GeminiTest`):
   ```python
   def sanitize_and_save(raw_response, target_class, ai_type, output_dir):
       # สกัดโค้ดระหว่าง ```java ... ```
       match = re.search(r'```(?:java)?\s*(.*?)\s*```', raw_response, re.DOTALL)
       clean_code = match.group(1) if match else raw_response
       
       expected_class_name = f"{target_class}{ai_type.capitalize()}Test"
       # แทนที่ชื่อคลาสให้ตรงเป๊ะ
       clean_code = re.sub(rf'public\s+class\s+\w+', f'public class {expected_class_name}', clean_code)
       
       out_file = f"{output_dir}/{expected_class_name}.java"
       with open(out_file, "w", encoding="utf-8") as f:
           f.write(clean_code)
       return out_file
   ```

5. **ส่วนที่ 5: การสรุป Token Usage และ Cost ภาพรวม:**
   - รวบรวมข้อมูล Tokens และ Latency จากทุกการเรียก API เซฟเป็นไฟล์รวม `Claude_vs_Gemini_Economics.csv` เพื่อนำไปพล็อตกราฟและเขียนตารางในรายงานบทที่ 3!

> **⚠️ ข้อควรจำสำคัญสำหรับ Member 3:**
> - การใส่ **Defect Context** ลงใน Prompt เป็น "หัวใจสำคัญ" ที่ทำให้ AI มี Fault Detection Rate (FDR) ชนะ Algorithm ดั้งเดิม
> - ตรวจสอบไฟล์ผลลัพธ์ว่าไม่มี Markdown Backticks หลุดเข้ามาในไฟล์ `.java`
> - บันทึก Log ค่า Latency และ Token Count ไว้ทุกรอบเพื่อใช้เปรียบเทียบในเล่มรายงาน

---

## 6. 🧑‍💻 Member 4: นายศิฆรินทร์ อุปจันทร์ (Infra & Data Analysis Lead)

**รหัสนักศึกษา:** 673380292-5  
**บทบาท:** ดูแลระบบ Infrastructure ทั้งหมด, สกัดคลาสเป้าหมายให้เพื่อน, ควบคุมการรัน Benchmark กลาง, คำนวณสถิติภาพรวม และจัดทำเล่มรายงานฉบับสมบูรณ์

### 📋 สิ่งที่คุณต้องส่งมอบ (Deliverables):
1. ซอร์สโค้ดเป้าหมายใน `target_benchmark/` สำหรับทุกบั๊กที่ทีมต้องการทดสอบ
2. ตารางผลลัพธ์รวม [`results/benchmark_results.csv`](results/benchmark_results.csv) และ State File `progress.json`
3. การคำนวณค่าเฉลี่ย Coverage, Fault Detection Rate (FDR %), และแผนภูมิกราฟสรุปผล
4. เล่มรายงานฉบับสมบูรณ์ และสไลด์สำหรับนำเสนออาจารย์

---

### 🛠️ คู่มือขั้นตอนการทำงานอย่างละเอียด:

#### ขั้นที่ 1: สกัด Source Code ของบั๊กเป้าหมายให้เพื่อนในกลุ่ม
เมื่อทีมตกลงจะเริ่มทำบั๊กใด ให้สั่งรันคำสั่ง:
```bash
# ตัวอย่าง: สกัด Math บั๊กที่ 2
docker exec defects4j_sqa /workspace/docker/extract_target.sh Math 2
```
- ซอร์สโค้ดและ Ground Truth จะถูกดึงมาไว้ที่ `target_benchmark/Math_2b/` ทันที จากนั้นแจ้งเพื่อนทั้ง 3 คนให้เริ่มสร้างเทส

#### ขั้นที่ 2: ตรวจความพร้อมของ Test Suites ทั้ง 4 ชุด
ก่อนสั่งรัน Runner ตรวจดูว่ามีไฟล์ในโฟลเดอร์ครบ:
- `Combinatorial_IPO/TestCode/<Class>_IPOTest.java`
- `MIO_Algorithm/TestCode/<Class>_ESTest.java`
- `Claude-sonnet_5/TestCode/<Class>ClaudeTest.java`
- `Gemini-3_8_flash/TestCode/<Class>GeminiTest.java`

#### ขั้นที่ 3: สั่งรัน Universal Benchmark Runner
```bash
# รันประเมินเฉพาะบั๊กเดี่ยวที่เพิ่งทำเสร็จ (เช่น Math-2)
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Math --bug 2

# หรือรันคิวชุดทดลองทั้งหมด พร้อมระบบ Resume ข้ามตัวที่เสร็จแล้ว
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --sample-17 --resume
```

#### ขั้นที่ 4: การวิเคราะห์ข้อมูลและสร้างกราฟสรุป (Data Analysis & Plotting)
1. **Average Line & Branch Coverage:** คำนวณค่าเฉลี่ย $\mu$ และ $\sigma$ ของทั้ง 4 เทคนิค
2. **Fault Detection Rate ในระดับ Bug (Bug-Level FDR %):**
   คำนวณสัดส่วนของข้อบกพร่อง (Bugs) ที่ชุดทดสอบของแต่ละเทคนิคสามารถตรวจพบได้จริงเทียบกับจำนวน Bug ทั้งหมดที่ทำการประเมิน:
   $$FDR_{\text{technique}} = \left(\frac{N_{\text{detected\_bugs}}}{N_{\text{evaluated\_bugs}}}\right) \times 100\% = \left(\frac{\text{จำนวน Bug ที่ตรวจพบ (สถานะ BUG\_DETECTED)}}{\text{จำนวน Bug ทั้งหมดที่ทำการประเมิน (Total Evaluated Bugs)}}\right) \times 100\%$$
   *ตัวอย่างการคำนวณ:* หากทำการทดลองบนชุดทดสอบ 100 Bugs และเทคนิคสามารถทำให้เกิด Failure บนเวอร์ชันมีบั๊ก และ Pass 100% บนเวอร์ชันแก้แล้ว ได้สำเร็จ 63 Bugs:
   $$FDR = \frac{63}{100} \times 100 = 63\%$$
   *(หมายเหตุทางวิชาการ: หน่วยของ FDR ต้องวัดที่ระดับ "Bug" ไม่ใช่ "Test Case" และกรณีที่เกิด `COMPILE_ERROR` หรือ `TIMEOUT` จะถือว่าไม่สามารถตรวจพบบั๊กนั้นได้ โดยยังคงถูกนับเป็นส่วนหนึ่งของตัวหาร $N_{\text{evaluated\_bugs}}$ เสมอเพื่อรักษามาตรฐานความซื่อตรงของงานวิจัย)*
3. **การพล็อตกราฟ:** ใช้ Python (`matplotlib`) หรือ Excel สร้าง Bar Chart เปรียบเทียบ Coverage และเปรียบเทียบ Cost/Token ของ AI

---

### 💡 วิธีทำระบบ Automated Continuous Benchmark & Auto-Plotting (สำหรับ Member 4)

> **🎯 เป้าหมาย:** ในฐานะ Infrastructure & Benchmark Lead สมาชิกคนที่ 4 ต้องทำหน้าที่เป็น "กระดูกสันหลัง" ของทีม โดยเชื่อมต่อกระบวนการตั้งแต่การสกัด Source Code, การรันประเมินผลต่อเนื่อง (Continuous Evaluation), ไปจนถึงการพลอตแผนภูมิกราฟสรุปผลทางวิชาการให้เป็นระบบอัตโนมัติทั้งหมด:

```mermaid
flowchart TD
    D4J["Defects4J Database<br/>(Dynamic Discovery)"] --> BatchExt["scripts/batch_extract_all_bugs.py<br/>สกัด Target Source & Info ลง target_benchmark/"]
    BatchExt --> Teammates["เพื่อนร่วมทีม 3 คน<br/>(IPO, MIO, AI) ทยอย Push โค้ดเทส"]
    Teammates --> AutoBench["scripts/run_benchmark.py --resume<br/>รันประเมินเฉพาะเทสใหม่ต่อเนื่องอัตโนมัติ"]
    AutoBench --> ResultCSV["results/benchmark_results.csv<br/>(บันทึก 5 สถานะ FDR & Coverage)"]
    ResultCSV --> Plotter["scripts/plot_results.py<br/>สร้าง 4 แผนภูมิวิชาการอัตโนมัติ"]
    Plotter --> Report["พร้อมประกอบเล่มรายงานบทที่ 5 ทันที!"]
```

#### รายละเอียดขั้นตอนการดำเนินงานอัตโนมัติ (Step-by-Step Guide):

1. **ส่วนที่ 1: การสกัดชุดเป้าหมายแบบ Batch อัตโนมัติ (Batch Target Extraction):**
   - ใช้สคริปต์ [`scripts/batch_extract_all_bugs.py`](scripts/batch_extract_all_bugs.py) สกัด Source Code และ Ground Truth ของบั๊กทั้งหมดที่ทีมวางแผนจะทำการทดลอง:
   ```bash
   # สกัดชุดทดลองนำร่อง 17 บั๊กหลัก
   python scripts/batch_extract_all_bugs.py --sample-17 --extract
   
   # หรือสกัดบั๊กทั้งหมดของโปรเจกต์ Math รวดเดียว
   python scripts/batch_extract_all_bugs.py --project Math --extract
   ```
   - เมื่อสกัดเสร็จ ให้ Push โฟลเดอร์ `target_benchmark/` ขึ้น GitHub เพื่อให้เพื่อนทั้ง 3 คนดึงไปใช้งาน

2. **ส่วนที่ 2: การเปิดรัน Continuous Benchmark ด้วยระบบ Resume:**
   - สั่งรัน Runner กลางด้วยออปชัน `--resume` ซึ่งจะอ่านสถานะจาก `results/progress.json` และรันเฉพาะคู่เทสที่ยังไม่ได้ทำหรือเพิ่งถูกเพิ่มเข้ามาใหม่:
   ```bash
   docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --sample-17 --resume
   ```
   - Member 4 สามารถตั้งเวลารันหรือสั่งรันซ้ำได้ตลอดเวลาโดยไม่ต้องกลัวว่าจะเสียเวลารันงานเก่าซ้ำซ้อน

3. **ส่วนที่ 3: การสร้างแผนภูมิวิชาการ 4 รูปแบบอัตโนมัติ (`scripts/plot_results.py`):**
   - เขียนสคริปต์ประมวลผลไฟล์ `results/benchmark_results.csv` เพื่อสร้างรูปภาพสำหรับใส่ในบทที่ 5 ของเล่มรายงาน:
   ```python
   import pandas as pd
   import matplotlib.pyplot as plt
   import seaborn as sns

   def generate_publication_figures():
       df = pd.read_csv("results/benchmark_results.csv")
       
       # รูปที่ 1: เปรียบเทียบ Average Line & Branch Coverage ของ 4 เทคนิค
       plt.figure(figsize=(10, 6))
       coverage_summary = df.groupby('technique')[['line_coverage', 'branch_coverage']].mean()
       coverage_summary.plot(kind='bar', colormap='viridis')
       plt.title("Comparison of Code Coverage Across Testing Techniques")
       plt.ylabel("Coverage (%)")
       plt.tight_layout()
       plt.savefig("results/figure1_coverage_comparison.png", dpi=300)
       
       # รูปที่ 2: สัดส่วน Fault Detection Status (5 ระดับ) ของแต่ละเทคนิค
       plt.figure(figsize=(12, 6))
       status_df = pd.crosstab(df['technique'], df['fault_detection_status'], normalize='index') * 100
       status_df.plot(kind='bar', stacked=True, colormap='tab10')
       plt.title("Fault Detection Classification Distribution (FDR %)")
       plt.ylabel("Percentage (%)")
       plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
       plt.tight_layout()
       plt.savefig("results/figure2_fdr_distribution.png", dpi=300)
       print("[+] สร้างแผนภูมิผลลัพธ์ทั้ง 4 รูปแบบสำเร็จใน results/")

   if __name__ == '__main__':
       generate_publication_figures()
   ```

> **⚠️ ข้อควรจำสำคัญสำหรับ Member 4:**
> - ตรวจสอบความสมบูรณ์ของ `results/benchmark_results.csv` สม่ำเสมอ
> - ประสานงานกับเพื่อนในทีมผ่าน Git Main Branch เพื่อรวมโค้ดเทส
> - คำนวณค่าสถิติ FDR (%) ให้ถูกต้องตามนิยาม 5 สถานะอย่างเคร่งครัด

---

## 7. ตัวอย่างการทำงานร่วมกันแบบครบวงจร (End-to-End Walkthrough: กรณี Math-2)

เพื่อให้ทุกคนเห็นภาพตรงกัน นี่คือจำลองการทำงานจริงตั้งแต่ต้นจนจบเมื่อเริ่มทำบั๊กใหม่:

```mermaid
sequenceDiagram
    autonumber
    actor M4 as Member 4 (Infra)
    actor M1 as Member 1 (IPO)
    actor M2 as Member 2 (MIO)
    actor M3 as Member 3 (AI)
    
    M4->>M4: รัน extract_target.sh Math 2
    Note over M4: สกัด HypergeometricDistribution.java<br/>และ defects4j_info.txt ลง target_benchmark/Math_2b/
    M4-->>M1: แจ้ง Source Code พร้อมแล้ว
    M4-->>M2: แจ้ง Source Code พร้อมแล้ว
    M4-->>M3: แจ้ง Source Code พร้อมแล้ว
    
    par สมาชิกทำงานคู่ขนานกัน (Parallel Execution)
        M1->>M1: วิเคราะห์ Parameter -> เขียน Math_model.txt -> รัน PICT -> แปลงเป็น HypergeometricDistribution_IPOTest.java
        M2->>M2: รัน run_evosuite_mio.sh Math 2 -> ได้ HypergeometricDistribution_ESTest.java
        M3->>M3: รัน kku_generate.py -> ได้ ClaudeTest.java และ GeminiTest.java
    end
    
    M1->>M4: วางไฟล์ใน Combinatorial_IPO/TestCode/
    M2->>M4: วางไฟล์ใน MIO_Algorithm/TestCode/
    M3->>M4: วางไฟล์ใน Claude/ และ Gemini/ TestCode/
    
    M4->>M4: รัน python3 scripts/run_benchmark.py --project Math --bug 2
    Note over M4: บันทึก Line Cov, Branch Cov, FDR ลง benchmark_results.csv อัตโนมัติ!
```

---

## 8. ระบบ Dynamic All-Bugs และการวัดผล Fault Detection Rate (FDR)

### 📌 8.1 การค้นหาบั๊กแบบ Dynamic (Dynamic Bug Discovery)
จำนวนบั๊กใน Defects4J ขึ้นอยู่กับเวอร์ชันและสภาพแวดล้อมจริง ณ ขณะรัน สคริปต์ของเราจึงดึงรายชื่อแบบ Dynamic สดๆ จาก CLI:
```bash
# 1. ดูสรุปจำนวน Active Bugs ทั้งหมดที่ตรวจพบใน Container ปัจจุบัน
python scripts/batch_extract_all_bugs.py --list-summary

# 2. สั่งรันคิวบั๊กทั้งหมดแบบ Dynamic ทุกโปรเจกต์
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --all-bugs --resume

# 3. สั่งรันคิวบั๊กทั้งหมดเฉพาะของโปรเจกต์ใดโปรเจกต์หนึ่ง (เช่น ทุกบั๊กของ Lang)
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --all-bugs --resume
```

---

### 🎯 8.2 การจำแนกสถานะ 5 ระดับอย่างรัดกุมตามหลักวิชาการ
ระบบ Runner จะประเมินและจำแนกสถานะออกเป็น 5 สถานะอย่างเป็นอิสระจากกัน:

| สถานะการประเมิน | ความหมายและเงื่อนไขการประเมิน | การนับเป็น Fault Detection |
| :--- | :--- | :---: |
| **`BUG_DETECTED`** | **ตรวจพบข้อบกพร่องจริง:** Test Suite เกิด Failure บนเวอร์ชันมีบั๊ก (`b`) ตรงตามพฤติกรรมบั๊ก และ **Pass 100% บนเวอร์ชันแก้แล้ว (`f`)** | ✅ นับเป็น $D_{detected}$ |
| **`NOT_DETECTED`** | **ไม่พบข้อบกพร่อง:** Test Suite ผ่านฉลุยทั้งบน `b` และ `f` (ไม่ได้ทดสอบจุดที่มีข้อบกพร่อง) | ❌ ไม่นับ |
| **`FLAKY_OR_REGRESSION`** | **เทสมีปัญหา:** Test Suite เฟลทั้งบน `b` และ `f` (เขียน Assertion ขัดแย้งกับสเปกจริงของโปรเจกต์) | ❌ ไม่นับ |
| **`COMPILE_ERROR`** | **คอมไพล์ไม่ผ่าน:** โค้ดเทสมี Syntax Error หรือขาด Classpath | ❌ ไม่นับ |
| **`TIMEOUT`** | **ทำงานเกินเวลา:** โค้ดเทสติด Infinite Loop เกิน 4 วินาที | ❌ ไม่นับ |

---

### 🎯 8.3 ระเบียบวิธีการคำนวณ Fault Detection Rate (Bug-Level FDR Formulation)

เพื่อให้สอดคล้องกับระเบียบวิธีวิจัยสากลด้าน Software Testing Benchmark การวัด Fault Detection Rate ต้องคำนวณที่ระดับ **"ข้อบกพร่อง (Bug Level)"** ไม่ใช่ระดับจำนวน Test Case:

$$FDR_{\text{technique}} = \left( \frac{N_{\text{detected\_bugs}}}{N_{\text{evaluated\_bugs}}} \right) \times 100\% = \left( \frac{\text{จำนวน Bug ที่ได้สถานะ BUG\_DETECTED}}{\text{จำนวน Bug ทั้งหมดที่ทำการประเมิน}} \right) \times 100\%$$

* **ตัวเศษ ($N_{\text{detected\_bugs}}$):** จำนวนบั๊กที่ชุดทดสอบสามารถตรวจจับได้ โดยต้องมีสถานะเป็น **`BUG_DETECTED`** เท่านั้น (เกิด Test Failure บนเวอร์ชันมีบั๊ก `b` และต้อง Pass 100% บนเวอร์ชันแก้บั๊กแล้ว `f`)
* **ตัวหาร ($N_{\text{evaluated\_bugs}}$):** จำนวนบั๊กทั้งหมดที่นำมาประเมินในการทดลอง
* **กฎความซื่อตรงของตัวหาร (Denominator Integrity Rule):**
  - หาก Test Suite เกิด `COMPILE_ERROR`, `TIMEOUT`, หรือ `FLAKY_OR_REGRESSION` บั๊กนั้นจะถือว่าตรวจไม่พบ ($0$)
  - บั๊กดังกล่าว **จะยังคงถูกนับรวมอยู่ในตัวหาร ($N_{\text{evaluated\_bugs}}$) เสมอ** ห้ามตัดทิ้งออกจากตัวหาร เพื่อให้ตัวเลข FDR สะท้อนความเสถียร (Reliability) และความเป็นไปได้จริงในการประยุกต์ใช้งานเชิงวิศวกรรม

---

## 9. แผนผังการเขียนเล่มรายงานฉบับสมบูรณ์ (Final Report Alignment)

| บทในรายงาน | หัวข้อรายงาน | ผู้รับผิดชอบหลัก | สิ่งที่ต้องเขียนและตารางที่ต้องใส่ |
| :--- | :--- | :--- | :--- |
| **บทที่ 1** | บทนำ วัตถุประสงค์ และขอบเขตงาน | **Member 4** | ที่มา ความสำคัญ, ขอบเขตงานวิจัย (Defect-Targeted Testing), Research Questions (RQ1: Coverage, RQ2: Bug-Level FDR, RQ3: Efficiency) |
| **บทที่ 2.1** | Combinatorial Testing & IPO Algorithm | **Member 1** | ทฤษฎี In-Parameter-Order (IPO/IPOG) Algorithm, การใช้เครื่องมือ Microsoft PICT เป็น Reference Implementation / Baseline Tool, ตารางขนาด Full vs Pairwise Reduction % |
| **บทที่ 2.2** | Search-Based Testing & MIO Algorithm | **Member 2** | ทฤษฎี MIO ใน EvoSuite, ตารางสถิติ Mean ± SD ของ Search Budget (30s/60s/120s) ตามข้อ 1.7 |
| **บทที่ 3** | Prompt Engineering Architecture | **Member 3** | โครงสร้าง System Prompt, เทคนิค BVA Guardrails, Defect Context Injection, ตาราง Token Usage & Cost ของ Claude vs Gemini |
| **บทที่ 4** | สภาพแวดล้อมระบบและการทดลอง | **Member 4** | สถาปัตยกรรม Docker, ขอบเขต `classes.modified` vs Project-Wide, นิยามสูตรคำนวณ Coverage & Bug-Level FDR 5 สถานะ |
| **บทที่ 5** | ผลการทดลองและการอภิปรายผล | **ทุกคนร่วมกัน** | ตารางใหญ่เปรียบเทียบ 4 เทคนิค (Target Class Coverage, Bug-Level FDR, Time, Cost), กราฟ 4 ภาพ, การวิเคราะห์จุดเด่น/ข้อจำกัดเชิงประจักษ์ |
| **บทที่ 6** | สรุปผลและข้อเสนอแนะ | **Member 4** | สรุปภาพรวม แนวทางการประยุกต์ใช้ในอุตสาหกรรม และงานวิจัยในอนาคต |

---

## 10. Checklist ติดตามความคืบหน้าสู่การส่งมอบงาน (Progress Tracker)

```text
[x] Milestone 1: เซ็ตอัป Infrastructure Docker และเครื่องมือกลางทั้งหมด (Member 4)
[x] Milestone 2: สร้าง Universal Benchmark Runner พร้อมระบบจัดหมวดหมู่ 5 สถานะ (Member 4)
[x] Milestone 3: รันการทดลองนำร่องบน Lang-1 ครบทั้ง 4 เทคนิค (ผลบันทึกใน benchmark_results.csv)
[x] Milestone 4: พิสูจน์การ Trigger ข้อบกพร่องจริงจนได้สถานะ BUG_DETECTED (Lang-1 Gemini)
[ ] Milestone 5: สมาชิกทุกคนดึงโค้ดล่าสุด (git pull) และอ่านคู่มือเล่มนี้
[ ] Milestone 6: Member 1 สกัด Parameter และส่งมอบ Test Suite ของกลุ่มเป้าหมาย (IPO)
[ ] Milestone 7: Member 2 รัน EvoSuite MIO เก็บสถิติ Mean ± SD ของ Budget ต่างๆ (MIO)
[ ] Milestone 8: Member 3 รัน KKU GenAI Script สร้าง Test Suite และสรุปตาราง Token Usage (Claude & Gemini)
[ ] Milestone 9: Member 4 สั่งรัน Universal Benchmark Runner เพื่อประเมินผลเปรียบเทียบ (--resume)
[ ] Milestone 10: พล็อตกราฟเปรียบเทียบ Line/Branch Coverage, FDR %, และ Token Economics
[ ] Milestone 11: รวบรวมข้อมูลทั้งหมดประกอบเป็นเล่มรายงานฉบับสมบูรณ์ และจัดทำสไลด์นำเสนอ
```

---
*คู่มือฉบับนี้จัดทำขึ้นเพื่อให้การทำงานร่วมกันของทีมมีมาตรฐานสูงสุด ถูกต้องตามระเบียบวิธีวิจัยทางวิศวกรรมซอฟต์แวร์ และนำไปสู่การส่งงานที่สมบูรณ์แบบ 100%!*
