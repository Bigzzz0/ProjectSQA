# 📊 สถิติการใช้งาน AI: Claude Sonnet 5 (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-12 09:44:15
* **โมเดลที่ใช้:** `claude-sonnet-5`
* **คลาสเป้าหมาย:** `org.apache.commons.csv.ExtendedBufferedReader`

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **57.52 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **3,091 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **7,904 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **10,995 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 43,084 / 200,000 tokens
