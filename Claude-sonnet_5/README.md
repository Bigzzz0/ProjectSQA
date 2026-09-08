# Claude Sonnet 5 - AI-Assisted Test Generation

**ผู้รับผิดชอบหลัก:** Member 3 (นายธนภูมิ จันทรา - AI Prompt Engineer & Test Automation)

> [!TIP]
> **📖 คู่มือปฏิบัติงานและคำสั่งทีละขั้นตอน:** สามารถอ่านฉบับเต็มได้ที่ [TEAM_WORKFLOW_GUIDE.md (ส่วนของ Member 3)](../TEAM_WORKFLOW_GUIDE.md#-member-3-นายธนภูมิ-จันทรา-ai-prompt-engineer---claude--gemini)

---

## 📌 บทบาทและคำนิยามทางวิชาการ (Academic Context)

* **เครื่องมือ (Tool):** **Claude Sonnet 5** ผ่านระบบ **KKU IntelSphere API** (`https://gen.ai.kku.ac.th`)
* **บทบาทของ Member 3:** ออกแบบสถาปัตยกรรม Prompt (Prompt Engineering) ให้ AI ทำหน้าที่เป็น Principal SQA Engineer เพื่อวิเคราะห์โครงสร้างโค้ดแบบ White-Box Testing และสร้างชุดทดสอบ JUnit 4 สำหรับ **Target Modified Classes** ใน Defects4J
* **เป้าหมาย:** สร้างชุดทดสอบที่มี Code Coverage สูง ครอบคลุม Edge Cases และสามารถเปิดเผยข้อบกพร่อง (Fault Detection) ใน Defects4J ได้จริง พร้อมบันทึกหลักฐานเชิงประจักษ์ (Token Usage & Generation Time)

---

## 🛠️ ขั้นตอนการทำงานจริงแบบละเอียด (Step-by-Step Workflow)

### ขั้นตอนที่ 1: ดึงซอร์สโค้ดของคลาสเป้าหมาย
* ดึงไฟล์ซอร์สโค้ดจาก `target_benchmark/<Project>_<BugID>b/` (เช่น `NumberUtils.java`)
* **ข้อควรระวัง:** ส่งเฉพาะ Source Code ของ **Target Modified Class** เข้าไปใน Prompt เท่านั้น ห้ามส่งทั้ง Repository เพื่อป้องกันไม่ให้ Context ล้นและเกิน Token Quota

---

### ขั้นตอนที่ 2: สถาปัตยกรรม Prompt (Prompt Architecture)
Prompt ที่ใช้ต้องประกอบด้วย 4 ส่วนประกอบหลัก:
1. **Role & Objective:** กำหนดบทบาทเป็น Lead SQA Specialist มุ่งเน้นการทำ Branch Coverage และ Boundary Value Analysis
2. **Defect & Branch Analysis Matrix (CoT):** สั่งให้ AI วิเคราะห์ Control Flow และเงื่อนไขข้อบกพร่องออกมาก่อนเริ่มเขียนโค้ด
3. **Strict Constraints (กฎเหล็กบังคับ):**
   * บังคับใช้ **Java 8** และ **JUnit 4** เท่านั้น (`import org.junit.Test;`, `import static org.junit.Assert.*;`)
   * บรรทัดแรกต้องประกาศ `package` ตรงกับ Defects4J (เช่น `package org.apache.commons.lang3.math;`)
   * ทุกเมธอดต้องใส่ `@Test(timeout = 4000)` เพื่อป้องกัน Timeout
   * ห้ามใช้ JUnit 5 (Jupiter), AssertJ, หรือ Mockito
4. **Target Output:** สั่งให้ส่งคืนเฉพาะบล็อกโค้ด ````java ... ```` โดยไม่มีข้อความเกริ่นนำหรือปิดท้าย

---

### ขั้นตอนที่ 3: สั่งรันเจนเทสอัตโนมัติด้วยสคริปต์
Member 3 สามารถสั่งรันผ่านสคริปต์ [scripts/kku_generate.py](../scripts/kku_generate.py):

```bash
# เจนเทสด้วย Claude Sonnet
python scripts/kku_generate.py --ai claude
```

*สคริปต์จะทำงานให้อัตโนมัติ:*
1. อ่าน API Key จาก `.env`
2. ส่ง Source Code และ System Prompt ไปยัง KKU API
3. สกัดเฉพาะบล็อกรหัสภาษา Java ออกมา
4. จับเวลาการสร้าง (Generation Time) และดึงสถิติ Token จาก API
5. บันทึกไฟล์เทสลงใน `TestCode/` อัตโนมัติ

---

### ขั้นตอนที่ 4: การบันทึกสถิติและหลักฐาน (Reproducibility Records)
หลังการรัน สคริปต์จะบันทึกหลักฐานสำคัญ 2 ไฟล์:
1. **บันทึก Prompt ที่ใช้จริง:** บันทึกลงใน `Claude-sonnet_5/Prompt/actual_prompt_<ClassName>.md`
2. **บันทึก Empirical Metrics:** บันทึกลงใน `Claude-sonnet_5/Result/generation_metrics_<ClassName>.md`
   * เวลาที่ใช้ในการสร้าง (วินาที)
   * Input Tokens (Prompt + Source Code)
   * Output Tokens (Generated Test Code)
   * Total Tokens และ Daily Quota ที่เหลือ

---

### ขั้นตอนที่ 5: การส่งมอบ Test Code ให้ Runner ของ Member 4
* ไฟล์เทสจะถูกจัดเก็บไว้ที่:
  `Claude-sonnet_5/TestCode/<ClassName>_ClaudeTest.java` (เช่น `NumberUtilsClaudeTest.java`)
* สมาชิกสามารถตรวจสอบความเรียบร้อยของโค้ด แล้วส่งมอบให้ Member 4 นำไปรันวัด Coverage และ Fault Detection ต่อไป

---

## 📂 รายการไฟล์ที่ Member 3 ต้องส่งมอบในส่วนของ Claude

* **โฟลเดอร์ Prompt/:** เก็บ Template Prompts และ Prompt ที่ใช้จริง
* **โฟลเดอร์ Result/:** บันทึกค่า Token Usage, Generation Time, และตารางสถิติเปรียบเทียบ
* **โฟลเดอร์ TestCode/:** ไฟล์ JUnit 4 Test Suite พร้อมคอมไพล์บน Defects4J
