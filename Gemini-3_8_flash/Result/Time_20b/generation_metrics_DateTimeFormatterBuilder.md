# 📊 สถิติการใช้งาน AI: Gemini 3.8 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-17 21:12:38
* **โมเดลที่ใช้:** `gemini-3.8-flash`
* **คลาสเป้าหมาย:** `org.joda.time.format.DateTimeFormatterBuilder`
* **Token Slot:** Token #1

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **40.9 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **25,242 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **6,013 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Reasoning / Thinking Tokens (CoT)** | **1,042 tokens** | คำนวณจากกระบวนการคิดวิเคราะห์ภายใน (`total - (input + output)`) |
| **Total Tokens** | **32,297 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> 💡 **หมายเหตุทางวิชาการ (Token Economics):** โมเดล `gemini-3.8-flash` มีกระบวนการให้เหตุผลภายใน (Internal Chain-of-Thought / Reasoning Process) โดยคิดวิเคราะห์ Boundary Condition เชิงลึกก่อนสร้างโค้ดทดสอบ ทำให้ Total Tokens รวมค่า Thinking Tokens ด้วย

> **Token Quota ประจำวัน (Token #1):** ใช้ไปแล้ว 327,758 / 350,000 tokens (เหลือ 22,242 tokens)
