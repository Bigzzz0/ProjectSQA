# MIO (Many Independent Objective) ผ่าน EvoSuite

**ผู้รับผิดชอบ:** Member 2 — นายแทนคุณ พันธ์นิกุล<br>
**สถานะผลประเมิน:** snapshot 26 กันยายน 2026; ดูตัวเลขล่าสุดที่ master dataset

MIO เป็น search-based software testing algorithm. ในโครงการนี้ใช้ EvoSuite 1.0.6 กำหนด `-Dalgorithm=MIO` เพื่อสร้าง JUnit suites สำหรับ target classes ใน Defects4J. การสร้าง suite, การประเมิน coverage และการตรวจ fault detection เป็นคนละขั้นตอนและใช้สถิติแยกกัน

## ผล benchmark กลาง

| รายการ | ผลล่าสุด |
|---|---:|
| ขอบเขต | 854 bugs |
| มี suite ให้ประเมิน | 834 |
| `NO_SUITE` จาก generation failure | 20 |
| ประเมินเสร็จและวัด coverage ได้ | 797 |
| `COMPILE_ERROR` | 37 |
| `FLAKY_OR_REGRESSION` | 291 |
| `NOT_DETECTED` | 501 |
| `BUG_DETECTED` | 5 |
| FDR ต่อ suite evaluations | 5/834 = 0.60% |
| FDR เทียบ catalog ทั้งหมด | 5/854 = 0.59% |
| Line / Branch coverage เฉลี่ย | 63.85% / 56.51% (n=797) |

`BUG_DETECTED` หมายถึงมี failure บน buggy version และไม่มี failure บน fixed version. Compile errors และ flaky/regression รวมอยู่ในตัวหาร FDR ต่อ suite; `NO_SUITE` แยกออกจากผลทดสอบ. Coverage ที่วัดไม่ได้ไม่ถูกแทนด้วย 0%.

## ผล generation budget

การทดลอง 30/60/120 วินาทีและ 3 seeds เป็นผลสร้าง suite แยกจากตาราง benchmark. EvoSuite ตั้ง criterion `LINE:BRANCH` และ budget summary บันทึก coverage criterion ค่าเดียว จึงไม่ควรอ้างค่านั้นเป็น line และ branch แยกกัน. รายละเอียด cohort และ paired tests อยู่ใน [รายงาน Search Budget](Result_Round2/budget_comparison.md).

ผล 20 ช่องที่ไม่มี suite พร้อมสาเหตุ, configuration และหลักฐานอยู่ใน [รายงานวิเคราะห์ความล้มเหลว MIO](MIO_FAILURE_ANALYSIS_REPORT.md). สถานะปัจจุบันของ IPO/AI และวิธีทำซ้ำระดับทีมอยู่ใน [README หลัก](../README.md), [Data Dictionary](../results/DATA_DICTIONARY.md) และ [คู่มือ Docker](../docker/README_DOCKER.md).

## ไฟล์และการทำซ้ำ

- Suite และ EvoSuite scaffolding: `MIO_Algorithm/TestCode/`
- Budget records: `MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv`
- MIO generation failure analysis: `MIO_Algorithm/MIO_FAILURE_ANALYSIS_REPORT.md`
- Runner กลาง: `scripts/run_benchmark.py`; ขั้นตอนเปิด environment อยู่ที่ [docker/README_DOCKER.md](../docker/README_DOCKER.md)

เอกสาร `EXECUTION_PLAN.md`, `NEXT_STEPS_PLAN.md`, `MIO_RUN_CHEATSHEET.md`, `manual_member2_mio_defects4j.md` และ `workflow_การทำงาน.md` เป็นแผน/คู่มือจากรอบก่อน ให้ยึดสถานะในไฟล์ผลล่าสุดแทน.
