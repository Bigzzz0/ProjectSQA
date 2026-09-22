# คู่มือการใช้งาน Native IPO Test Generation Pipeline

เอกสารนี้อธิบายโครงสร้างใหม่ ขั้นตอนการทำงาน และคำสั่งสำหรับรันการสร้างชุดทดสอบเชิงคอมบิเนทอเรียล (Combinatorial Testing - In-Parameter-Order: IPO) ครอบคลุมทุกคลาสเป้าหมายใน Defects4J

---

## 1. ภาพรวมโครงสร้างไดเรกทอรีใหม่ (Reorganized Structure)

โครงสร้างทั้งหมดจัดระเบียบอยู่ภายใต้ `Combinatorial_IPO/` ดังนี้:

```text
Combinatorial_IPO/
├── Code/                   # ซอร์สโค้ดและ Unit Tests ของ IPO Pipeline
│   ├── algorithm/          # อัลกอริทึม IPO (2-way covering array)
│   ├── analyzer/           # ตัววิเคราะห์ Java AST และ Diff evidence
│   ├── domain/             # Construction planner, value generators, adapters
│   ├── generator/          # ตัวสังเคราะห์โค้ด JUnit 4
│   ├── oracle/             # Fixed-version oracle runner และ verification
│   ├── runner/             # all_class_pipeline.py, catalog loaders
│   └── tests/              # Unit tests (110 tests ผ่านทั้งหมด)
├── Configuration/          # แคตตาล็อก, สแนปช็อต และการตั้งค่า
│   ├── catalogs/           # all-modified-classes.normalized.json
│   ├── experiments/        # all-854-modified-classes.json
│   └── regression_reference_46.json # อ้างอิง 46 suites ดั้งเดิม
├── TestCode/               # ชุดทดสอบ JUnit ที่ผ่านการ verify แล้วเท่านั้น
│   └── <Project>_<BugID>b/ # แยกตาม bug directory โดยตรง
│       └── <package-path>/<Class>_IPOTest.java
├── Results/                # ผลลัพธ์และดัชนีชี้วัด
│   ├── inventory.json      # สถานะความพร้อมของ 1,070 คลาสเป้าหมาย (247 AUTO_READY)
│   ├── routing_manifest.json# งานที่ส่งต่อให้ Member 4 หรือเครื่องมืออื่น
│   ├── verified_suites_manifest.json # รายการชุดทดสอบที่ผ่าน Fixed-Verification 100%
│   ├── generation_manifest.json # ประวัติการรันล่าสุด
│   └── cache/              # แคช intermediate oracle/combinations เพื่อ resume
└── docs/                   # เอกสารประกอบโครงการ
    ├── README.md           # คู่มือนี้ (คู่มือภาษาไทยและคำสั่งรัน)
    ├── DESIGN.md           # เอกสารสถาปัตยกรรมและการออกแบบระบบ
    ├── RESULTS_REPORT.md   # รายงานผลการทดลองและการเปรียบเทียบเชิงประจักษ์
    ├── HANDOFF.md          # เอกสารส่งต่องาน
    └── handoffs/           # เอกสารบันทึกการส่งต่องานย้อนหลัง
```

---

## 2. การตรวจสอบความพร้อมเบื้องต้น (Preflight Check)

ตรวจสอบความพร้อมของ Docker container `sqa-defects4j` และเครื่องมือ Defects4J:

### คำสั่งบน Host (PowerShell):
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode preflight
```

ผลลัพธ์ที่ถูกต้อง:
```json
{
  "ready": true,
  "checks": {
    "catalog_exists": true,
    "experiment_exists": true,
    "defects4j_available": true,
    "defects4j_metadata_ok": true
  }
}
```

---

## 3. คำสั่งสำหรับการรันงาน (Execution Commands)

เนื่องจากขั้นตอนการทดสอบและเก็บ Oracle บน Defects4J ใช้เวลานาน (มีทั้งการ checkout, compile, run fixed version oracle, compile JUnit และ verify ด้วย Defects4J test) ผู้ใช้สามารถรันคำสั่งต่อไปนี้ได้โดยตรงใน Terminal:

### 3.1 ตรวจสอบสรุปสถานะปัจจุบัน (Summary - รันได้ทันที เร็วมาก)
```powershell
python Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary
```
หรือรันผ่าน Docker:
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode summary
```

### 3.2 ตรวจสอบความถูกต้องของ Test Suites ที่ Publish แล้ว (Validation)
```powershell
python Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode validate
```

### 3.3 รันชุดทดสอบ Canary (40 คลาสตัวแทนจาก 16 โปรเจกต์)
คำสั่งนี้จะรันเฉพาะ 40 คลาสตัวแทนที่มีความหลากหลายของโปรเจกต์และชนิดตัวแปร:
```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode canary --resume
```

### 3.4 รันการสร้าง Test Suites เต็มรูปแบบ (Full Generation - รันนาน)
คำสั่งนี้จะทำการสร้างชุดทดสอบให้กับทุกคลาสที่พร้อม (`AUTO_READY` จำนวน 247 คลาส) โดยระบบรองรับ `--resume` อย่างสมบูรณ์ หากหยุดรันกลางคัน สามารถรันคำสั่งเดิมซ้ำเพื่อทำต่อจากคลาสล่าสุดได้ทันทีโดยไม่เสียเวลาทำคลาสเดิมซ้ำ:

```powershell
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --resume
```

### 3.5 รันเฉพาะเจาะจงรายโปรเจกต์ (Selective by Project)
หากต้องการรันเฉพาะบางโปรเจกต์ เช่น Math, Lang หรือ Chart:
```powershell
# ตัวอย่าง: รันเฉพาะโปรเจกต์ Math
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --project Math --resume

# ตัวอย่าง: รันเฉพาะโปรเจกต์ Lang
docker --context default exec -e PYTHONPATH=/workspace/Combinatorial_IPO/Code sqa-defects4j python3 /workspace/Combinatorial_IPO/Code/runner/all_class_pipeline.py --mode generate --project Lang --resume
```

---

## 4. มาตรฐานทางวิทยาศาสตร์และการควบคุมคุณภาพ

1. **Pure Native IPO 2-Way Combinations:**
   - 100% Pair Coverage สำหรับ method ที่มี factor $\ge 2$
   - ไม่มี dummy inputs ไม่มีการสุ่มเดา
2. **Fixed-Version Oracle Verification:**
   - ค่าผลลัพธ์และข้อยกเว้นถูกสังเกตจาก Fixed Version จริงของ Defects4J
   - ผ่านการรันซ้ำเพื่อยืนยัน Determinism
3. **Partial-Class Publication:**
   - คลาสที่มีหลาย method หากมีบาง method เกิด timeout หรือ exception จะไม่ทำให้ method อื่นที่ผ่านถูกทิ้ง
   - ชุดทดสอบ JUnit ที่เผยแพร่จะมีเฉพาะ method ที่ compile และรันผ่าน 100% บน Fixed Version เท่านั้น
4. **Platform Independence:**
   - มีการ normalize Line Endings (CRLF / LF) ในการคำนวณ Checksum SHA-256 ทำให้ผลลัพธ์บน Windows Host และ Linux Docker ตรงกันเสมอ
