# Native IPO pipeline tests

โฟลเดอร์นี้มี unit tests สำหรับ catalog, parser, domain adapters, IPO generation, oracle, manifests และ batch pipeline.

## การรัน

จาก root repository:

```powershell
docker exec defects4j_sqa sh -lc "cd /workspace/Combinatorial_IPO/Code && PYTHONPATH=. python3 -m unittest discover -s tests -p 'test_*.py' -v"
```

หรือบน Windows host:

```powershell
$env:PYTHONPATH = (Resolve-Path 'Combinatorial_IPO/Code').Path
python -m unittest discover -s Combinatorial_IPO/Code/tests -p 'test_*.py' -v
```

มี test methods 126 รายการ. การรันบน Windows host ด้วย Python 3.12 วันที่ 30 กันยายน 2026 ได้ 125 ผ่านและ 1 ล้มเหลว: `test_ast_parser_extracts_overloads_enums_and_nested_types` ไม่พบ `IllegalArgumentException` ใน throws clause ของ constructor ที่ parser ส่งกลับ. สถานะนี้ยังไม่ใช่ green test suite; ผลบน Docker ควรยืนยันแยกต่างหาก.

| กลุ่ม | ขอบเขต |
|---|---|
| `test_ipo.py`, `test_pair_coverage.py` | IPO algorithm และ pair-coverage validation |
| `test_java_parser.py` | Java AST extraction และ callable metadata |
| `test_fixed_version_oracle.py` | การสร้าง oracle จาก fixed version |
| `test_all_class_pipeline.py`, `test_batch_controller_gates.py` | pipeline, publish gate และ resume |
| ไฟล์ `test_*.py` อื่น | catalog, adapters, scenario, readiness และ helper functions |

ผล unit tests เป็นคนละหลักฐานกับ 277 suites ใน verified manifest และ benchmark coverage/FDR.
