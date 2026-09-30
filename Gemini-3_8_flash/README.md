# Gemini 3.8 Flash — AI-assisted test generation

**ผู้รับผิดชอบ:** Member 3 — นายธนภูมิ จันทรา<br>
**ช่องทางที่ใช้:** KKU IntelSphere API ผ่าน `scripts/kku_generate.py`<br>
**Benchmark snapshot:** 26 กันยายน 2026

## ผลประเมิน benchmark กลาง

| รายการ | ผลล่าสุด |
|---|---:|
| มี suite ให้ประเมิน | 853/854 |
| `NO_SUITE` | 1 |
| ประเมินเสร็จและวัด coverage ได้ | 424 |
| `COMPILE_ERROR` | 429 |
| `FLAKY_OR_REGRESSION` | 308 |
| `NOT_DETECTED` | 9 |
| `BUG_DETECTED` | 107 |
| FDR ต่อ suite evaluations | 107/853 = 12.54% |
| Line / Branch coverage เฉลี่ย | 86.29% / 79.54% (n=424) |

สถานะ `BUG_DETECTED` ต้องมี failure บน buggy version และไม่มี failure บน fixed version. Compile errors และ flaky/regression ไม่ใช่ coverage 0%; ไม่มีค่า coverage ที่วัดได้ให้แสดง.

## ผล generation และหลักฐาน

แหล่งข้อมูล generation สรุปมี 1,079 records เฉลี่ย 20,855.06 tokens และ 89.78 วินาทีต่อ record. บันทึกนี้ไม่มี run ID ที่เชื่อมกับ benchmark detection จึงห้ามตีความเป็น token/เวลาต่อบั๊กที่ตรวจพบ.

- ไฟล์ suite: `Gemini-3_8_flash/TestCode/`
- Prompt ที่บันทึกและ metrics ราย generation: `Gemini-3_8_flash/Prompt/` และ `Gemini-3_8_flash/Result/`
- สรุปรวมและตัวหาร: [master stats](../results/master_descriptive_stats.json), [analytics](../results/advanced_analytics_report.md)
- บันทึกการส่งมอบชุดจาก Member 3: [delivery report](../results/DELIVERY_REPORT_M3.md)

ไฟล์ใน Prompt/Result เป็นหลักฐานตามการสร้างครั้งนั้น ให้เก็บเนื้อหาเดิมไว้; ใช้ master data สำหรับผล benchmark ปัจจุบัน. ข้อความเรื่อง Claude ในเอกสารเก่าหรือในสคริปต์ที่ยังรองรับ alias ไม่ใช่ผลเทคนิคที่เปรียบเทียบในรายงานนี้.
