# 📚 Data Dictionary & Dataset Specification

**Project:** CP353201 Software Quality Assurance (Defects4J Benchmark Master Dataset)  
**Author:** นายศิฆรินทร์ อุปจันทร์ (Member 4: Infrastructure & Data Analysis Lead)  
**Single Source of Truth:** `results/master_benchmark_summary.csv` (2,804 Records)  

---

## 📌 1. ตารางพจนานุกรมข้อมูล (Field Definitions)

| ชื่อฟิลด์ (Field Name) | ประเภทข้อมูล (Type) | ตัวอย่างข้อมูล | คำอธิบายและสูตรการคำนวณ (Description & Formula) |
| :--- | :---: | :--- | :--- |
| **`Project`** | String | `Lang`, `Math`, `Closure` | ชื่อรหัสโครงการ 1 ใน 17 โครงการมาตรฐานของ Defects4J Benchmark |
| **`Bug_ID`** | Integer | `1`, `2`, `10` | รหัสบั๊กของข้อบกพร่องจริง (Active Bug ID) ที่ระบุใน Ground Truth |
| **`Technique`** | String | `Gemini 3.8 Flash`, `MIO` | เครื่องมือหรือขั้นตอนวิธีสร้างกรณีทดสอบ (IPO, MIO, DeepSeek, Gemini) |
| **`Target_Classes`** | String | `org.apache.commons.lang3...` | ชื่อคลาสเป้าหมายที่มีการแก้ไขโค้ดจริง (Modified Classes Under Test) |
| **`Line_Coverage_%`** | Float | `88.50`, `68.85` | เปอร์เซ็นต์ความครอบคลุมบรรทัดคำสั่ง วัดผ่าน Cobertura ($L_{cov} / L_{tot} 	imes 100$) |
| **`Branch_Coverage_%`** | Float | `72.40`, `68.85` | เปอร์เซ็นต์ความครอบคลุมกิ่งเงื่อนไข วัดผ่าน Cobertura ($B_{cov} / B_{tot} 	imes 100$) |
| **`Fault_Detection_Status`** | Enum | `BUG_DETECTED` | สถานะการตรวจจับข้อบกพร่อง จำแนกอย่างรัดกุมเป็น 5 ระดับมาตรฐานวิชาการ |
| **`Test_Count`** | Integer | `25`, `42` | จำนวนกรณีทดสอบ (@Test methods) ที่สร้างขึ้นภายใน Test Suite |
| **`Duration_Sec`** | Float | `6.0`, `90.8` | เวลาที่ใช้ในการประมวลผลเพื่อสร้างชุดทดสอบ (วินาที) |
| **`Execution_Status`** | String | `DONE` | สถานะการรัน Pipeline การทดลอง |

---

## 🎯 2. นิยาม 5 สถานะการตรวจจับข้อบกพร่อง (Bug-Level FDR Classification)

1. **`BUG_DETECTED`:** ชุดทดสอบเกิด Failure บนเวอร์ชันมีบั๊ก (`b`) ตรงตามพฤติกรรมข้อบกพร่อง และ **Pass 100% บนเวอร์ชันแก้แล้ว (`f`)** (นับเป็น $D=1$)
2. **`NOT_DETECTED`:** ชุดทดสอบ Pass ทั้งบน `b` และ `f` (ไม่สามารถเข้าถึงหรือ Trigger จุดข้อบกพร่องได้)
3. **`FLAKY_OR_REGRESSION`:** ชุดทดสอบเกิด Failure ทั้งบน `b` และ `f` (Assertion ไม่สอดคล้องกับพฤติกรรมจริงของโปรแกรม)
4. **`COMPILE_ERROR`:** ชุดทดสอบคอมไพล์ไม่ผ่านบน Java 8 / Defects4J Classpath
5. **`TIMEOUT`:** ชุดทดสอบทำงานเกินเวลาที่กำหนด (Timeout Guard > 4,000 ms)

---

## 📐 3. ระเบียบวิธีวิจัยและกฎความซื่อตรงของตัวหาร (Denominator Integrity Rule)
ในการคำนวณ **Fault Detection Rate (FDR %)**:
$$FDR = \left( \frac{{N_{{\text{{BUG\_DETECTED}}}}}}{{N_{{\text{{evaluated\_bugs}}}}}} \right) \times 100\%$$
* บั๊กที่เกิด `COMPILE_ERROR`, `TIMEOUT` หรือ `FLAKY_OR_REGRESSION` **จะถูกนับรวมอยู่ในตัวหาร $N_{{evaluated\_bugs}}$ เสมอ** ห้ามตัดทิ้งออกจากตัวหาร เพื่อให้สะท้อนความเสถียรและความพร้อมใช้งานในสภาพแวดล้อมวิศวกรรมจริง
