# Native IPO 2-way test generation

**เจ้าของเทคนิค:** Member 1 — นายปวริศช์ ประมวล<br>
**ผล generation ล่าสุด:** `Combinatorial_IPO/Results/verified_suites_manifest.json` และ `inventory.json`<br>
**ผล benchmark:** `results/master_benchmark_summary.csv` และ `results/master_descriptive_stats.json`

## ขอบเขตและสถานะล่าสุด

ระบบสร้าง pairwise test suites ด้วย Native In-Parameter-Order (IPO) จาก factor/value models และตรวจ pair coverage ก่อนเผยแพร่. PICT เป็น reference baseline แยกต่างหาก ไม่ใช่ backend ของ IPO ที่นำไปเปรียบเทียบใน benchmark หลัก.

- Manifest มี **277 suite records** ที่สถานะ `FIXED_VERIFIED`, ครอบคลุม **257 bug IDs** ใน 15 โปรเจกต์ และ 2,224 verified methods.
- IPO inventory มี **1,070 class records**: 319 `AUTO_READY`, 670 `NEEDS_ADAPTER`, 79 `NEEDS_ENTRY_POINT`, 2 `NOT_PAIRWISE_APPLICABLE`.
- Defects4J master catalog มี 1,073 modified-class entries (577 unique class names); จำนวนนี้ต่างจาก IPO inventory 1,070. ให้รายงานขอบเขตแต่ละแหล่งแยกกัน และอย่าอ้างว่า IPO audit ครอบคลุม 1,073 รายการจนกว่าจะ reconcile ความต่างนี้.
- ใน benchmark กลาง: 257 bug-technique pairs มี suite; 252 `DONE` พร้อม coverage, 5 `COMPILE_ERROR`, และ 597 `NO_SUITE`. ตรวจพบบั๊ก 37/257 (FDR 14.40%); coverage เฉลี่ย 26.76% line และ 18.67% branch (n=252).
- `NO_SUITE` แยกเป็น 37 generation/verification errors และ 560 skipped/not ready ตาม generation/audit manifests.

ตัวเลข baseline เดิม 173 suites และ 42,398 tests เป็นประวัติจากรอบก่อน ไม่ใช่สถิติปัจจุบัน. อ้าง manifest และ master benchmark ตามวัตถุประสงค์ของตัวเลขเสมอ.

## ไฟล์หลัก

- Source code และ unit tests: `Combinatorial_IPO/Code/`
- Suite ที่ผ่าน fixed-version verification: `Combinatorial_IPO/TestCode/`
- Manifest และ inventory: `Combinatorial_IPO/Results/`
- รายงานผล IPO: [docs/RESULTS_REPORT.md](docs/RESULTS_REPORT.md)
- คู่มือทำงานทั้งทีม: [TEAM_WORKFLOW_GUIDE.md](../docs/team/TEAM_WORKFLOW_GUIDE.md)

## ตรวจสอบ manifest และรัน unit tests

จาก root ของ repository สามารถเรียก:

```powershell
docker exec defects4j_sqa python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
docker exec defects4j_sqa sh -lc "cd /workspace/Combinatorial_IPO/Code && PYTHONPATH=. python3 -m unittest discover -s tests -p 'test_*.py' -v"
```

ผลตรวจบน Windows host วันที่ 30 กันยายน 2026: พบ 126 unit tests; ผ่าน 125 และล้มเหลว 1 ที่ `test_ast_parser_extracts_overloads_enums_and_nested_types` (ผล parse constructor `throws` clause). ไม่ควรระบุว่า test suite ผ่าน 100% จนกว่าจะแก้หรือยืนยันสาเหตุและทดสอบใหม่. ผลบน Windows ไม่แทนผลบน Docker/Linux.

การประเมิน suite เพื่อหา FDR/coverage ทำผ่าน runner กลางของ Member 4 ไม่ใช่ `all_class_pipeline.py`:

```powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1 --techniques ipo
```

การสร้าง suite ใหม่ทั้ง inventory ใช้เวลาและเปลี่ยน manifests; อย่ารัน generation batch เพียงเพื่ออ่านผล benchmark ปัจจุบัน.
