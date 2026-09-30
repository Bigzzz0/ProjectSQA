# IPO suites ที่ยืนยันบน Defects4J fixed version

โฟลเดอร์นี้เก็บ JUnit suites ที่ผ่านการตรวจบน fixed version และมีรายการอ้างอิงใน `Results/verified_suites_manifest.json`.

## สถานะจาก manifest ล่าสุด

- 277 suite records ระดับ target class
- ครอบคลุม 257 Defects4J bug IDs ใน 15 โปรเจกต์
- 2,224 verified methods
- ทุก record ใน manifest มีสถานะ `FIXED_VERIFIED` และข้อมูล suite/hash อยู่ใน manifest

หนึ่งบั๊กอาจมีหลาย target classes และมีหลาย suite records. ใน benchmark กลางจึงรวมผลระดับบั๊ก–เทคนิค: IPO มี suite สำหรับ 257 จาก 854 บั๊ก, วัด coverage ได้ 252, พบ `COMPILE_ERROR` 5 และไม่มี suite 597. ผล summary อยู่ใน [master dataset](../../results/master_benchmark_summary.csv).

ตัวเลขเก่าที่พบในเอกสารก่อนหน้า เช่น 173 suites, 181 Java files, 42,398 tests และ 405,456 LOC เป็น baseline รอบก่อน ไม่ใช่จำนวนปัจจุบัน. ใช้ manifest เป็นแหล่งตรวจรับ suite; ไม่คำนวณยอดจากไฟล์ในโฟลเดอร์โดยไม่ตรวจ hash.

## ตรวจสอบ suite

```powershell
docker exec defects4j_sqa python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
```

การตรวจ suite กับ fixed version ไม่เท่ากับการประเมิน fault detection. ผล FDR และ coverage ของ benchmark กลางดูใน master CSV และ run logs ที่อ้างจากคอลัมน์ `Run_Log`.
