# 📊 สถิติการใช้งาน AI: Claude Sonnet 5 (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-08 22:13:09
* **โมเดลที่ใช้:** `claude-sonnet-5`
* **คลาสเป้าหมาย:** `org.apache.commons.lang3.math.NumberUtils`

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **297.52 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **19,323 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **34,704 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **54,027 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 54,027 / 200,000 tokens
