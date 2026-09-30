# IPO handoff — สถานะและขั้นตอนตรวจรับ

**อัปเดต:** 30 กันยายน 2026<br>
**ผู้ส่งมอบ:** Member 1 — นายปวริศช์ ประมวล<br>
**ผู้รับผิดชอบ benchmark:** Member 4 — นายศิฆรินทร์ อุปจันทร์

## สถานะปัจจุบัน

- verified_suites_manifest.json มี 277 FIXED_VERIFIED class-suite records ครอบคลุม 257 bug IDs ใน 15 โปรเจกต์
- IPO inventory มี 1,070 class records: 319 AUTO_READY, 670 NEEDS_ADAPTER, 79 NEEDS_ENTRY_POINT, 2 NOT_PAIRWISE_APPLICABLE
- Master benchmark ประเมิน IPO ได้ 257 bug–technique pairs: 252 DONE, 5 COMPILE_ERROR, 597 NO_SUITE; ตรวจพบ 37 บั๊ก
- Defects4J master catalog นับ 1,073 modified-class entries. จำนวนนี้ยังต่างจาก IPO inventory 1,070; อย่าอ้างว่าครอบคลุมครบจนกว่าจะตรวจสอบความต่าง
- Baseline manifest 173 suites ที่ commit a11795acc5 เป็นข้อมูลย้อนหลัง ไม่ใช่ยอดปัจจุบัน

## ตรวจรับ suite

1. ตรวจว่า suite ปรากฏใน Combinatorial_IPO/Results/verified_suites_manifest.json
2. ตรวจ suite_path, suite_sha256, target class และ bug ID ว่าตรงกัน
3. ใช้ validation mode เพื่อตรวจ hash และ pair coverage:

~~~powershell
docker exec defects4j_sqa python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
~~~

4. ประเมิน FDR และ target-class coverage ผ่าน runner กลาง:

~~~powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1 --techniques ipo
~~~

ผล fixed-version verification ของ IPO เป็นหลักฐานว่าชุดทดสอบทำงานได้กับ fixed revision; ไม่เท่ากับผล fault detection. ใช้ master CSV และ Run_Log สำหรับสรุป benchmark.

## เอกสารและ manifests

- คู่มือ Native IPO: ../README.md
- IPO results report: RESULTS_REPORT.md
- Design: DESIGN.md
- Verified manifest: ../Results/verified_suites_manifest.json
- Inventory: ../Results/inventory.json
