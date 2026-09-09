# 📊 สถิติการใช้งาน AI: Gemini 3.8 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-09 20:54:02
* **โมเดลที่ใช้:** `gemini-3.8-flash`
* **คลาสเป้าหมาย:** `org.apache.commons.cli.CommandLine`

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **43.36 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **3,406 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **3,402 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **10,820 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 37,508 / 350,000 tokens
