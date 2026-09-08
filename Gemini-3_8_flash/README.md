# Gemini 3.8 Flash - AI-Assisted Test Generation

**ผู้รับผิดชอบหลัก:** Member 3 (นายธนภูมิ จันทรา - AI Prompt Engineer & Test Automation)

> [!TIP]
> **📖 คู่มือปฏิบัติงานและคำสั่งทีละขั้นตอน:** สามารถอ่านฉบับเต็มได้ที่ [TEAM_WORKFLOW_GUIDE.md (ส่วนของ Member 3)](../TEAM_WORKFLOW_GUIDE.md#-member-3-นายธนภูมิ-จันทรา-ai-prompt-engineer---claude--gemini)

---

## 📌 บทบาทและคำนิยามทางวิชาการ (Academic Context)

* **เครื่องมือ (Tool):** **Gemini 3.8 Flash** ผ่านระบบ **KKU IntelSphere API** (`https://gen.ai.kku.ac.th`)
* **บทบาทของ Member 3:** ออกแบบสถาปัตยกรรม Prompt สำหรับ Gemini 3.8 Flash ซึ่งเป็นโมเดลความเร็วสูงที่มีความโดดเด่นด้าน Throughput และ Context Processing เพื่อทำการวิเคราะห์โครงสร้างโค้ดแบบ White-Box Testing และสร้างชุดทดสอบ JUnit 4 สำหรับ **Target Modified Classes** ใน Defects4J
* **เป้าหมาย:** ศึกษาเปรียบเทียบประสิทธิภาพระหว่างโมเดลความเร็วสูง (Gemini Flash) กับโมเดลขนาดใหญ่ (Claude Sonnet) ในมิติของ Code Coverage, Fault Detection Rate, ปริมาณ Token และเวลาที่ใช้ในการประมวลผล

---

## 🛠️ ขั้นตอนการทำงานจริงแบบละเอียด (Step-by-Step Workflow)

### ขั้นตอนที่ 1: ดึงซอร์สโค้ดของคลาสเป้าหมาย
* ดึงไฟล์ซอร์สโค้ดจาก `target_benchmark/<Project>_<BugID>b/` (เช่น `NumberUtils.java`)
* ป้อนเฉพาะ Source Code ของ **Target Modified Class** เข้าสู่ Prompt

---

### ขั้นตอนที่ 2: สถาปัตยกรรม Prompt สำหรับ Gemini (Optimization Guidelines)
เนื่องจาก Gemini 3.8 Flash ตอบสนองรวดเร็วมาก การกำหนดโครงสร้างคำสั่งจึงต้องกระชับและชัดเจน:
1. **System Prompt:** ระบุชัดเจนว่าต้องการชุดทดสอบระดับ Unit Testing ด้วย **JUnit 4**
2. **Defect Focus:** เน้นการทดสอบกรณีอินพุตที่มีความเสี่ยงต่อข้อบกพร่อง (เช่น รูปแบบตัวเลข Hexadecimal, สัญกรณ์วิทยาศาสตร์, ขอบเขต 32-bit integer)
3. **Strict Formatting:**
   * บรรทัดแรกต้องประกาศ `package` ตรงกับ Defects4J (เช่น `package org.apache.commons.lang3.math;`)
   * ทุกเมธอดต้องใส่ `@Test(timeout = 4000)`
   * บังคับส่งคืนเฉพาะโค้ดภาษา Java ในบล็อก ````java ... ````

---

### ขั้นตอนที่ 3: สั่งรันเจนเทสอัตโนมัติด้วยสคริปต์
Member 3 สามารถสั่งรันผ่านสคริปต์ [scripts/kku_generate.py](../scripts/kku_generate.py):

```bash
# เจนเทสด้วย Gemini 3.8 Flash
python scripts/kku_generate.py --ai gemini
```

*สคริปต์จะทำงานให้อัตโนมัติ:*
1. อ่าน API Key จาก `.env`
2. ส่งคำสั่งไปยังโมเดล Gemini บน KKU API
3. สกัดโค้ดภาษา Java และตรวจสอบความสมบูรณ์
4. จับเวลาการสร้าง (Generation Time) และดึงสถิติ Token จาก API
5. บันทึกไฟล์เทสลงใน `TestCode/` อัตโนมัติ

---

### ขั้นตอนที่ 4: การบันทึกสถิติและหลักฐาน (Reproducibility Records)
หลังการรัน สคริปต์จะบันทึกหลักฐานสำคัญ 2 ไฟล์:
1. **บันทึก Prompt ที่ใช้จริง:** บันทึกลงใน `Gemini-3_8_flash/Prompt/actual_prompt_<ClassName>.md`
2. **บันทึก Empirical Metrics:** บันทึกลงใน `Gemini-3_8_flash/Result/generation_metrics_<ClassName>.md`
   * เวลาที่ใช้ในการสร้าง (วินาที)
   * Input Tokens, Output Tokens, Total Tokens
   * เปรียบเทียบความเร็วกับ Claude Sonnet

---

### ขั้นตอนที่ 5: การส่งมอบ Test Code ให้ Runner ของ Member 4
* ไฟล์เทสจะถูกจัดเก็บไว้ที่:
  `Gemini-3_8_flash/TestCode/<ClassName>_GeminiTest.java` (เช่น `NumberUtilsGeminiTest.java`)
* ส่งมอบไฟล์ให้ Member 4 นำไปรันประเมินผลใน Defects4J

---

## 📂 รายการไฟล์ที่ Member 3 ต้องส่งมอบในส่วนของ Gemini

* **โฟลเดอร์ Prompt/:** เก็บ Template Prompts และบันทึกคำสั่งที่ใช้จริง
* **โฟลเดอร์ Result/:** บันทึกค่า Token Usage, Generation Time, และผลการวิเคราะห์เปรียบเทียบกับ Claude
* **โฟลเดอร์ TestCode/:** ไฟล์ JUnit 4 Test Suite สำหรับส่งต่อให้ Defects4J Runner
