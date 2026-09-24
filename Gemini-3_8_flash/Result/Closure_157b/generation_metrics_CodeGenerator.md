# 📊 สถิติการใช้งาน AI: Gemini 3.8 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-24 15:26:56
* **โมเดลที่ใช้:** `gemini-3.8-flash`
* **คลาสเป้าหมาย:** `com.google.javascript.jscomp.CodeGenerator`
* **Token Slot:** Token #8

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **42.48 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **11,684 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **6,581 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **18,265 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #8):** ใช้ไปแล้ว 285,953 / 350,000 tokens (เหลือ 64,047 tokens)
