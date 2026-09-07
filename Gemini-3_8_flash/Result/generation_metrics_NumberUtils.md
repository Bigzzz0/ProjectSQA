# 📊 สถิติการใช้งาน AI: Gemini 3.8 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-07 21:44:24
* **โมเดลที่ใช้:** `gemini-3.8-flash`
* **คลาสเป้าหมาย:** `org.apache.commons.lang3.math.NumberUtils` (Lang-1b)

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **136.89 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **14,510 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **11,912 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **39,999 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 75,458 / 350,000 tokens
