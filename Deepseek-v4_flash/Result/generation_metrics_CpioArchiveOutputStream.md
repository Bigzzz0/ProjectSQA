# 📊 สถิติการใช้งาน AI: DeepSeek V4 Flash (via KKU API)

* **วัน-เวลาที่ทดลอง:** 2026-09-15 20:47:02
* **โมเดลที่ใช้:** `deepseek-v4-flash`
* **คลาสเป้าหมาย:** `org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream`
* **Token Slot:** Token #1

### 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)
| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
| :--- | :---: | :--- |
| **เวลาที่ใช้สร้าง (Generation Time)** | **352.69 วินาที** | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | **4,393 tokens** | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | **7,450 tokens** | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | **11,843 tokens** | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | **สำเร็จ (Code Extracted)** | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #1):** ใช้ไปแล้ว 205,917 / 1,000,000 tokens (เหลือ 794,083 tokens)
