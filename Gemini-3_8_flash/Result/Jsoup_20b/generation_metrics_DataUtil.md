# 📊 สถิติการใช้งาน AI: Gemini 3.8 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-22 23:01:40
* **โมเดลที่ใช้:** `gemini-3.8-flash`
* **คลาสเป้าหมาย:** `org.jsoup.helper.DataUtil`
* **Token Slot:** Token #5

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **64.39 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **2,338 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **3,509 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Reasoning / Thinking Tokens (CoT)** | **4,528 tokens** | คำนวณจากกระบวนการคิดวิเคราะห์ภายใน (`total - (input + output)`) |
| **Total Tokens** | **10,375 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> 💡 **หมายเหตุทางวิชาการ (Token Economics):** โมเดล `gemini-3.8-flash` มีกระบวนการให้เหตุผลภายใน (Internal Chain-of-Thought / Reasoning Process) โดยคิดวิเคราะห์ Boundary Condition เชิงลึกก่อนสร้างโค้ดทดสอบ ทำให้ Total Tokens รวมค่า Thinking Tokens ด้วย

> **Token Quota ประจำวัน (Token #5):** ใช้ไปแล้ว 115,254 / 350,000 tokens (เหลือ 234,746 tokens)
