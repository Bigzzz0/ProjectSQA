# Combinatorial Testing: IPO Algorithm (Microsoft PICT Tool)

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล - Algorithm Lead 1)

> [!TIP]
> **📖 คู่มือปฏิบัติงานและคำสั่งทีละขั้นตอน:** สามารถอ่านฉบับเต็มได้ที่ [TEAM_WORKFLOW_GUIDE.md (ส่วนของ Member 1)](../TEAM_WORKFLOW_GUIDE.md#-member-1-นายปวริศช์-ประมวล-algorithm-lead-1---ipo--combinatorial)

---

## 📌 บทบาทและคำนิยามทางวิชาการ (Academic Context)

* **ขั้นตอนวิธี (Algorithm):** **In-Parameter-Order (IPO)** เป็นเทคนิค Combinatorial Testing แบบ Deterministic สำหรับสร้าง Pairwise (2-way) และ t-way Test Combinations โดยขยายกรณีทดสอบทีละพารามิเตอร์ในแนวนอน (Horizontal Growth) และเติมกรณีทดสอบใหม่ในแนวตั้ง (Vertical Growth)
* **เครื่องมือที่เลือกใช้ (Tool):** **Microsoft PICT (Pairwise Independent Combinatorial Testing)** ซึ่งเป็นเครื่องมือสร้าง Combination Matrix ระดับมาตรฐานอุตสาหกรรม โดยนำแนวคิด Combinatorial Interaction Testing มาใช้
* **เป้าหมายของ Member 1:** เปลี่ยน Input Space ของ **Target Modified Classes** ใน Defects4J ให้เป็นไฟล์ JUnit 4 Test Suite ที่มีคุณภาพสูงและจับข้อบกพร่องได้จริง

---

## 🛠️ ขั้นตอนการทำงานจริงแบบละเอียด (Step-by-Step Workflow)

### ขั้นตอนที่ 1: รับข้อมูลคลาสเป้าหมาย (Target Class Input)
1. เปิดดู Source Code และข้อมูลข้อบกพร่องที่ Member 4 จัดเตรียมไว้ให้ในโฟลเดอร์:
   `target_benchmark/<Project>_<BugID>b/` (เช่น `target_benchmark/Lang_1b/NumberUtils.java`)
2. ศึกษาเมธอดเป้าหมายที่มีข้อบกพร่อง (Faulty Method) เช่น `createNumber(String str)` เพื่อดูว่ารับพารามิเตอร์อะไร และมีเงื่อนไข Validation อะไรบ้าง

---

### ขั้นตอนที่ 2: ออกแบบ Parameter Model (`Configuration/model.txt`)
สร้างไฟล์โมเดลใน `Configuration/` โดยแตกพารามิเตอร์ของเมธอดออกมาเป็นหมวดหมู่ (Categories & Values) พร้อมเงื่อนไขบังคับ (Constraints):

```text
# ตัวอย่าง: Configuration/Lang_1b_model.txt (สำหรับ NumberUtils.createNumber)
Prefix:      None, Plus, Minus, Hex0x, HexMinus0x, Hash, Invalid
ValueType:   Integer, Float, ScientificE, HexDigits, Empty, AllZeros, Exceed32Bit
Suffix:      None, l, L, f, F, d, D, Invalid
Sign:        Positive, Negative

# Constraints (กฎเงื่อนไขเพื่อตัดกรณีที่ไม่เมคเซนส์ออก)
IF [Prefix] in {"Hex0x", "HexMinus0x", "Hash"} THEN [ValueType] in {"HexDigits", "Empty"};
IF [Prefix] in {"Hex0x", "HexMinus0x"} THEN [Suffix] in {"None", "l", "L"};
```

---

### ขั้นตอนที่ 3: สั่งรัน PICT เพื่อสร้าง Combinations
สั่งรันคำสั่ง PICT ภายใน Defects4J Docker Container:

```bash
# 1. รัน Pairwise (2-way Combinations)
pict Configuration/Lang_1b_model.txt > Result_Round1/combinations_2way.txt

# 2. รัน 3-way Combinations (เพื่อเปรียบเทียบ Configuration ตามข้อ 1.7)
pict Configuration/Lang_1b_model.txt /o:3 > Result_Round2/combinations_3way.txt
```

---

### ขั้นตอนที่ 4: พัฒนา Generator Script แปลง Combinations เป็น JUnit 4 Test
เขียนสคริปต์ Python ในโฟลเดอร์ `Combinatorial_IPO/Code/generate_ipo_tests.py` เพื่ออ่านไฟล์ `combinations_2way.txt` แล้วสร้างเป็นโค้ดภาษา Java โดยอัตโนมัติ:

#### ตัวอย่างโค้ด Generator Script (`Code/generate_ipo_tests.py`):
```python
import csv

def generate_junit():
    header = """package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtils_IPOTest {
"""
    body = ""
    with open("../Result_Round1/combinations_2way.txt", "r") as f:
        reader = csv.reader(f, delimiter='\t')
        headers = next(reader)
        for idx, row in enumerate(reader):
            # ประกอบ Input String ตามค่า Combination ที่ได้
            val = build_input_string(row)
            body += f"""
    @Test(timeout = 4000)
    public void testIPO_Case_{idx+1}() {{
        try {{
            Number res = NumberUtils.createNumber("{val}");
            assertNotNull(res);
        }} catch (NumberFormatException expected) {{
            // ผ่านการตรวจสอบเมื่ออินพุตไม่ถูกต้อง
        }} catch (Exception e) {{
            fail("Unexpected exception: " + e.getMessage());
        }}
    }}
"""
    footer = "}\n"
    with open("../TestCode/NumberUtils_IPOTest.java", "w") as out:
        out.write(header + body + footer)

if __name__ == "__main__":
    generate_junit()
```

---

### ขั้นตอนที่ 5: ตรวจสอบความถูกต้องและส่งมอบ (Delivery to TestCode/)

ไฟล์เทสที่เจนได้ต้องนำมาเก็บไว้ที่:
`Combinatorial_IPO/TestCode/<ClassName>_IPOTest.java`

#### 📋 กฎเหล็กที่ Member 1 ต้องตรวจสอบก่อนส่งมอบ:
1. **Package Declaration:** บรรทัดแรกต้องตรงกับคลาสเป้าหมายเสมอ เช่น `package org.apache.commons.lang3.math;`
2. **Framework:** บังคับใช้ **JUnit 4** (`import org.junit.Test;`)
3. **Timeout:** มี `@Test(timeout = 4000)` ทุกเมธอด ป้องกัน Runner ค้าง
4. **Exception Handling:** กรณีที่ป้อนค่าผิดปกติ ต้องดักจับ Exception ที่ถูกต้อง (เช่น `catch (NumberFormatException expected)`) เพื่อไม่ให้เทสพังโดยไม่ตั้งใจ

---

## 📂 รายการไฟล์ที่ Member 1 ต้องส่งมอบในแต่ละรอบ

* **รอบที่ 1 (Phase 1):**
  * `Configuration/Lang_1b_model.txt`
  * `Result_Round1/combinations_2way.txt`
  * `Code/generate_ipo_tests.py`
  * `TestCode/NumberUtils_IPOTest.java`
* **รอบที่ 2 (Phase 2):**
  * ขยายผล Model สำหรับ Target Classes ในชุด Benchmark
  * `Result_Round2/combinations_3way.txt` (เปรียบเทียบ 2-way vs 3-way ตามข้อ 1.7)
  * สรุปจุดเด่น/ข้อจำกัดของ IPO Algorithm ลงใน `Result_Round2/summary_ipo.md`
