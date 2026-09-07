# 📊 สถิติการใช้งาน AI: Claude Sonnet 5 (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-07 22:00:20
* **โมเดลที่ใช้:** `claude-sonnet-5`
* **คลาสเป้าหมาย:** `org.apache.commons.lang3.math.NumberUtils` (Lang-1b)

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **253.92 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **19,140 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **30,272 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **49,412 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

