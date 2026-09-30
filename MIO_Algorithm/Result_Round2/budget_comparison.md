# MIO Search Budget: ผลการทดลองสร้างชุดทดสอบ

**Snapshot:** 26 กันยายน 2026<br>
**ชุดข้อมูล:** `MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv`<br>
**สถิติล่าสุด:** `results/advanced_analytics.json` → `mio_generation_budget`

## วิธีอ่านผล

ทดลอง EvoSuite MIO ด้วย search budgets 30, 60 และ 120 วินาที และ seeds 101, 102, 103. ตารางสรุปมี 3,027 records โดยจำนวนที่มีผลใช้ได้ไม่เท่ากันในแต่ละ budget.

EvoSuite ใช้ criterion `LINE:BRANCH` และไฟล์ summary เก็บค่า coverage รวมเดียวกัน ดังนั้นค่านี้ **ไม่ใช่ Line Coverage และ Branch Coverage ที่วัดแยกกัน**. อย่านำตัวเลขนี้ไปเทียบตรง ๆ กับ Line/Branch Coverage ใน master benchmark.

## ผล generation จาก summary ล่าสุด

| Budget | Records (n) | Coverage criterion เฉลี่ย ± SD | เวลาเฉลี่ยที่บันทึก |
|---:|---:|---:|---:|
| 30 วินาที | 1,023 | 65.73% ± 31.96% | 68.29 วินาที |
| 60 วินาที | 1,015 | 68.73% ± 31.29% | 90.62 วินาที |
| 120 วินาที | 989 | 70.82% ± 30.40% | 194.41 วินาที |

เวลาเป็นเวลาที่สคริปต์บันทึกจริง จึงอาจยาวกว่า search budget ที่ส่งให้ EvoSuite.

## การเปรียบเทียบแบบจับคู่

ใช้ Wilcoxon signed-rank กับ target classes ที่มีผลครบทั้งสอง budget และปรับ p-value ด้วย Holm สำหรับสองคู่เปรียบเทียบ:

| คู่ budget | จำนวนคู่ที่จับคู่ได้ | คู่ที่ coverage เปลี่ยน | p-value หลัง Holm |
|---|---:|---:|---:|
| 30 → 60 วินาที | 1,006 | 738 | 3.386 × 10⁻⁸⁴ |
| 60 → 120 วินาที | 981 | 687 | 1.541 × 10⁻⁶⁷ |

ทั้งค่าเฉลี่ยและ paired tests บ่งชี้ว่า coverage criterion สูงขึ้นใน cohort ที่มีข้อมูลจับคู่ แต่จำนวนตัวอย่างต่างกันตาม budget และผลนี้เป็น **generation experiment**. จึงไม่สรุปว่า budget ใดเป็นจุดเหมาะที่สุดทั่วไป หรือว่าการเพิ่ม budget ทำให้ fault detection สูงขึ้น.

## แยกจากผล benchmark

ผลประเมิน suite ใน runner กลางเป็นคนละการทดลองกับตาราง budget ข้างต้น. สำหรับ MIO ใน master snapshot:

- มี suite ให้ประเมิน 834 จาก 854 bug IDs; 20 เป็น `NO_SUITE` จาก generation failure
- 797 ผลประเมินเสร็จและมี coverage ที่วัดได้; 37 เป็น `COMPILE_ERROR`
- ตรวจพบบั๊ก 5/834 (FDR 0.60% ของ suite evaluations; 5/854 = 0.59% ของ catalog)
- Line/Branch Coverage ใน master คือ 63.85%/56.51% (n=797)

**แหล่งอ้างอิงหลัก:** `evosuite_budget_summary.csv`, `results/advanced_analytics.json`, `results/master_descriptive_stats.json`. อย่าใช้ตัวเลขตัวอย่างในแผนรันเก่ามาแทนข้อมูลชุดนี้.
