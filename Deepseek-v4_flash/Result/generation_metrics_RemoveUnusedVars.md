# 📊 สถิติการใช้งาน AI: DeepSeek V4 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-15 20:28:26
* **โมเดลที่ใช้:** `deepseek-v4-flash`
* **คลาสเป้าหมาย:** `com.google.javascript.jscomp.RemoveUnusedVars`
* **Token Slot:** Token #1

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **47.1 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **8,620 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **4,651 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **13,271 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #1):** ใช้ไปแล้ว 143,626 / 1,000,000 tokens (เหลือ 856,374 tokens)
