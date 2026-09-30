# Native IPO — รายงานผล generation และ benchmark

**วันที่อัปเดต:** 30 กันยายน 2026<br>
**แหล่งข้อมูล:** Combinatorial_IPO/Results/verified_suites_manifest.json, inventory.json, generation_manifest.json, routing_manifest.json และ master benchmark snapshot วันที่ 26 กันยายน 2026.

## สถานะ generation

| รายการ | สถานะปัจจุบัน |
|---|---:|
| Verified suite records ใน manifest | 277 |
| Bug IDs ที่มี suite อย่างน้อยหนึ่ง target class | 257 |
| โปรเจกต์ที่มี suite | 15 |
| Verified methods | 2,224 |
| Class records ใน IPO inventory | 1,070 |
| AUTO_READY | 319 |
| NEEDS_ADAPTER | 670 |
| NEEDS_ENTRY_POINT | 79 |
| NOT_PAIRWISE_APPLICABLE | 2 |

Defects4J master catalog ปัจจุบันมี 1,073 modified-class entries และ 577 unique class names. IPO inventory มี 1,070 records; ความต่าง 3 records ยังไม่ reconcile จึงต้องแสดงขอบเขตทั้งสองแหล่งแยกกัน.

Baseline 173 suites และ 42,398 tests เป็น snapshot เก่า. ตัวเลข suite/test count ปัจจุบันให้คำนวณจาก manifest และไฟล์ปัจจุบันเท่านั้น; อย่าใช้ baseline แทน.

## ผล benchmark กลางของ IPO

| สถานะ/ตัวชี้วัด | จำนวน/ค่า |
|---|---:|
| Bug–technique pairs ที่มี suite | 257/854 |
| DONE และมี coverage ที่วัดได้ | 252 |
| COMPILE_ERROR | 5 |
| BUG_DETECTED | 37 |
| NOT_DETECTED | 197 |
| FLAKY_OR_REGRESSION | 18 |
| NO_SUITE | 597 |
| FDR ต่อ suite evaluation | 37/257 = 14.40% |
| FDR ต่อ catalog | 37/854 = 4.33% |
| Line / Branch coverage เฉลี่ย | 26.76% / 18.67% (n=252) |

NO_SUITE breakdown: 37 generation/verification errors และ 560 skipped/not ready (รวม 495 adapter only, 47 missing entry point, 16 ทั้งสองเหตุ, และ 2 adapter plus not pairwise applicable). ผลเหล่านี้เป็นสถานะ generation ไม่ใช่ผล benchmark และไม่เข้าสู่ coverage/FDR.

PICT เป็น reference baseline ของงานสาย IPO; ผลหลักใช้ Native IPO และ suite ที่อ้างใน verified manifest. BUG_DETECTED ใน runner กลางต้องมี failure บน buggy และไม่มี failure บน fixed.

## แหล่งข้อมูล

- Verified manifest: ../Results/verified_suites_manifest.json
- Inventory: ../Results/inventory.json
- Generation outcomes: ../Results/generation_manifest.json
- Routing reasons: ../Results/routing_manifest.json
- Master benchmark CSV: ../../results/master_benchmark_summary.csv
- Master statistics: ../../results/master_descriptive_stats.json
