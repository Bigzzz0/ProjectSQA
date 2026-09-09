# 📊 สถิติการใช้งาน AI: Claude Sonnet 5 (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-09 20:48:30
* **โมเดลที่ใช้:** `claude-sonnet-5`
* **คลาสเป้าหมาย:** `org.apache.commons.cli.CommandLine`

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **287.7 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **4,172 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **31,098 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **35,270 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 99,281 / 200,000 tokens
