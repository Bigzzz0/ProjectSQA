# Claude Sonnet 5 - AI-Assisted Test Generation

**ผู้รับผิดชอบหลัก:** Member 3 (AI Prompt Engineer)

---

## 📌 บทบาทและความรับผิดชอบ
1. ออกแบบ System Prompt และ Few-Shot Prompts สำหรับ **Claude Sonnet 5** เพื่อสร้าง JUnit 4 Test Cases
2. ป้อน Source Code จาก Defects4J Target Classes เข้าสู่ Claude Sonnet 5 ผ่าน Antigravity หรือ KKU IntelSphere API
3. สกัดโค้ด Unit Test ที่ได้ ตรวจสอบความถูกต้องทางไวยากรณ์ (Syntax / Imports) และบันทึกลงใน `TestCode/`
4. บันทึกประวัติ Prompts, ค่า Context Length, และเวลาที่ใช้ในการสร้าง Test Suite

---

## 📂 โครงสร้างโฟลเดอร์

```text
Claude-sonnet_5/
├── Prompt/                        # ไฟล์แม่แบบ Prompt ทั้งหมด (System Prompt, Context Ingestion, Few-Shot)
├── Result/                        # ค่า Metric การวัดผล Coverage & Fault Detection
└── TestCode/                      # Java Test Code ที่ Claude Sonnet 5 สร้างขึ้น
```
