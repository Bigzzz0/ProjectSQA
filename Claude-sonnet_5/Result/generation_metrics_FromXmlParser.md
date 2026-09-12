# 📊 สถิติการใช้งาน AI: Claude Sonnet 5 (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-12 10:25:05
* **โมเดลที่ใช้:** `claude-sonnet-5`
* **คลาสเป้าหมาย:** `com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser`

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **372.11 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **12,781 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **46,956 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **59,737 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน:** ใช้ไปแล้ว 202,704 / 200,000 tokens
