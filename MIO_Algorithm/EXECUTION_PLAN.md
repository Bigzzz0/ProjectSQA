# 📋 แผนการดำเนินงานการทดลอง MIO Algorithm (Step-by-Step Execution Plan)

**โครงการ:** CP353201 Software Quality Assurance (ปีการศึกษา 1/2569)  
**บทบาท:** Member 2 (นายแทนคุณ พันธ์นิกุล - Algorithm Lead 2: MIO / EvoSuite Specialist)  
**ขอบเขต:** การทดลอง Option A (17 โปรเจกต์ตัวแทน ใน Defects4J) ตามเกณฑ์ข้อ 1.7 และ 2.2 ใน PDF

---

## 🎯 1. สิ่งที่ควรทำเป็น "อันดับแรก" (First Priority: Proof of Pipeline)

> [!IMPORTANT]
> **ภารกิจอันดับแรก:** รันโปรเจกต์ที่ 1 **`Lang-1b` (คลาส `NumberUtils`)** ให้ครบ 9 รัน (30s, 60s, 120s $\times$ 3 Seeds 101, 102, 103) ด้วยสคริปต์อัตโนมัติ `batch_evosuite.py`

### เหตุผลที่ต้องทำ Lang-1b เป็นอันดับแรก:
1. **เป็นคลาสที่เราคุ้นเคยที่สุด:** เป็นคลาสเดียวกับที่เราเคยรันรอบนำร่อง ทำให้มั่นใจ 100% ว่าไม่มีปัญหาเรื่อง Environment
2. **พิสูจน์ระบบอัตโนมัติแบบครบวงจร (End-to-End Verification):**
   - ตรวจสอบว่าระบบวนลูปครบ 9 รันโดยไม่สะดุด
   - ตรวจสอบว่าสูตรคำนวณ Mean ($\mu$) และ SD ($\sigma$) บันทึกลง `evosuite_budget_summary.csv` ถูกต้อง
   - ตรวจสอบว่าไฟล์เทสที่ดีที่สุดถูกคัดลอกลง `MIO_Algorithm/TestCode/Lang_1b/` อัตโนมัติ
3. **ใช้เวลาสั้น:** ประมาณ **10 นาที** ก็จะได้ผลลัพธ์โปรเจกต์แรกสมบูรณ์ 100%

### คำสั่งสำหรับทำอันดับแรก:
```bash
python MIO_Algorithm/Code/batch_evosuite.py --project Lang --bug 1
```

---

## 🔍 2. การตรวจสอบผลลัพธ์หลังทำอันดับแรกเสร็จ (Sanity Check)

เมื่อรัน `Lang-1b` เสร็จ ให้เปิดเช็คไฟล์ 2 จุดนี้:
1. **ดูตารางสรุป:** เปิดดูไฟล์ `MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv`  
   - ตรวจสอบว่ามีแถวของ Lang-1 ที่ Budget 30s, 60s, 120s ครบทั้ง 3 แถว
   - มีค่า Mean และ SD สวยงามตามเกณฑ์ข้อ 1.7
2. **ดูไฟล์ Test Code:** เปิดดูโฟลเดอร์ `MIO_Algorithm/TestCode/Lang_1b/`  
   - ตรวจสอบว่ามีไฟล์ `NumberUtils_ESTest.java` และ `_scaffolding.java` เข้าไปอยู่เรียบร้อย

---

## 🗂️ 3. แผนการรันให้ครบทั้ง 17 โปรเจกต์ (แบ่งเป็น 4 ชุดย่อย / Batches)

เพื่อไม่ให้เครื่องร้อนหรือทำงานหนักเกินไป และเพื่อให้เราสามารถ **หยุดพักเครื่อง หรือตรวจสอบผลเป็นระยะได้** เราจะแบ่งการรัน 17 โปรเจกต์ออกเป็น 4 ชุดย่อย (Batches) ดังนี้:

### 📦 ชุดที่ 1: คอร์ไลบรารีขนาดเล็ก-กลาง (รันไว สำเร็จเร็ว)
*ใช้เวลารวมประมาณ 40–50 นาที*
1. **Lang-1b:** `org.apache.commons.lang3.math.NumberUtils` *(ทำเป็นอันดับแรก)*
2. **Math-2b:** `org.apache.commons.math3.distribution.HypergeometricDistribution`
3. **Csv-1b:** `org.apache.commons.csv.ExtendedBufferedReader`
4. **Codec-1b:** `org.apache.commons.codec.language.Soundex`
5. **Cli-1b:** `org.apache.commons.cli.CommandLine`

---

### 📦 ชุดที่ 2: ไลบรารีโครงสร้างข้อมูล และ JSON Processing
*ใช้เวลารวมประมาณ 40–50 นาที*
6. **Collections-25b:** `org.apache.commons.collections4.IteratorUtils`
7. **Gson-1b:** `com.google.gson.TypeInfoFactory`
8. **JacksonCore-1b:** `com.fasterxml.jackson.core.io.NumberInput`
9. **JacksonDatabind-1b:** `com.fasterxml.jackson.databind.JavaType`
10. **JacksonXml-1b:** `com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser`

---

### 📦 ชุดที่ 3: ไลบรารีจัดการไฟล์, HTML, XML และวันเวลา
*ใช้เวลารวมประมาณ 40–50 นาที*
11. **Compress-1b:** `org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream`
12. **Jsoup-1b:** `org.jsoup.nodes.Document`
13. **JxPath-1b:** `org.apache.commons.jxpath.ri.model.dom.DOMNodePointer`
14. **Time-1b:** `org.joda.time.Partial`

---

### 📦 ชุดที่ 4: ไลบรารีขนาดใหญ่ (Complex / Compiler / Frameworks)
*ใช้เวลารวมประมาณ 40–50 นาที*
15. **Chart-1b:** `org.jfree.chart.renderer.category.AbstractCategoryItemRenderer`
16. **Closure-1b:** `com.google.javascript.jscomp.RemoveUnusedVars`
17. **Mockito-1b:** `org.mockito.internal.invocation.InvocationMatcher`

---

## 🛡️ 4. ระบบความปลอดภัยและการหยุดพักเครื่อง (Resume & Pause)

สคริปต์ `batch_evosuite.py` มีระบบบันทึกความคืบหน้า (**Checkpoint & Resume**) ลงไฟล์ `progress_mio.json`:
* **ถ้าต้องการพักเครื่อง:** กด `Ctrl + C` เพื่อหยุดการรันได้ทุกเมื่อ
* **เมื่อเปิดเครื่องใหม่:** รันคำสั่งเดิม ระบบจะตรวจสอบอัตโนมัติว่าโปรเจกต์ไหนเสร็จแล้ว จะ **ข้ามตัวที่เสร็จแล้วทันที** และทำต่อจากบั๊กที่ค้างอยู่ ไม่ต้องเริ่มนับหนึ่งใหม่

---

## 📊 5. สรุปสิ่งที่จะได้รับเมื่อทำครบทั้ง 17 โปรเจกต์

1. **ตารางสถิติฉบับสมบูรณ์ (`Result_Round2/evosuite_budget_summary.csv`):**  
   มีข้อมูลครบ $17 \times 3 = 51$ แถว สำหรับนำไปใส่ตารางเปรียบเทียบในบทที่ 2.2 ของเล่มรายงาน
2. **ชุดทดสอบ JUnit ดิบ (`TestCode/<Project>_<Bug_ID>b/`):**  
   มีไฟล์ `.java` ครบทั้ง 17 คลาส พร้อมให้ Member 4 สั่งรัน `python3 scripts/run_benchmark.py --sample-17` เพื่อวัดคะแนนรวมเปรียบเทียบกับ IPO และ AI
3. **กราฟและบทวิเคราะห์:**  
   นำตัวเลข Mean/SD ไปพล็อตกราฟเปรียบเทียบ Search Budget (30s vs 60s vs 120s) แสดงให้เห็นว่าเวลาค้นหาเพิ่มขึ้น Coverage เพิ่มขึ้นอย่างไรตามหลักวิชาการ
