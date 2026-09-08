# คู่มือการใช้งาน Docker Environment & Universal Runner สำหรับ Defects4J

คู่มือฉบับนี้จัดทำโดย **Member 4 (Infrastructure & Data Analysis Lead)** เพื่อให้สมาชิกทุกคน (Member 1, 2, 3) ใช้งานสภาพแวดล้อมมาตรฐานเดียวกันได้อย่างราบรื่น 100% โดยไม่ต้องกังวลเรื่อง OS Compatibility หรือปัญหา Dependencies

---

## 🛠️ สภาพแวดล้อมที่จัดเตรียมไว้ใน Container

1. **Multi-JDK Environment:**
   * ติดตั้งทั้ง **OpenJDK 8** (เป็น Default สำหรับโปรเจกต์ดั้งเดิมของ Defects4J) และ **OpenJDK 11** (สำหรับโปรเจกต์ที่ต้องการ Java รุ่นใหม่)
   * สามารถสลับเวอร์ชันได้ง่ายดายในคอนเทนเนอร์
2. **Defects4J Framework:** ติดตั้งและตั้งค่า Environment Variable ไว้อย่างสมบูรณ์ พร้อมคำสั่ง `defects4j pids`, `bids`, `checkout`, `compile`, `test`, `coverage`, `export`
3. **Microsoft PICT Tool:** คอมไพล์และติดตั้งไว้ที่ `/usr/local/bin/pict` (พร้อมให้ Member 1 รันได้ทันที)
4. **EvoSuite Framework (Version 1.0.6):** จัดเตรียมไฟล์ `evosuite-1.0.6.jar` และ `evosuite-standalone-runtime-1.0.6.jar` ไว้ที่ `/opt/evosuite/` (พร้อมให้ Member 2 รัน MIO)
5. **Python 3 & Dependencies:** ติดตั้ง `requests` และไลบรารีสำหรับการประมวลผลข้อมูลอัตโนมัติ

---

## 🚀 ขั้นตอนการเปิดใช้งาน

### 1. Build และสั่ง Start Container
เปิด Terminal / PowerShell ในโฟลเดอร์หลักของโปรเจกต์ (`ProjectSQA/`):

```bash
docker-compose -f docker/docker-compose.yml up -d --build
```

### 2. เข้าสู่ Terminal ของ Container
```bash
docker exec -it defects4j_sqa bash
```

### 3. ตรวจสอบความพร้อมของระบบ (Sanity Check)
```bash
# ตรวจสอบ Defects4J
defects4j sanity-check

# ตรวจสอบเวอร์ชัน Java (ต้องเป็น Java 8 เป็นค่าเริ่มต้น)
java -version

# ตรวจสอบคำสั่ง PICT ของ Member 1
pict

# ตรวจสอบ EvoSuite ของ Member 2
java -jar /opt/evosuite/evosuite-1.0.6.jar --help
```

---

## 💻 การสั่งรัน Benchmark ผ่าน Universal Runner (Member 4 Tool)

เมื่อสมาชิกแต่ละสายงาน (Member 1, 2, 3) นำไฟล์เทสมาวางไว้ในโฟลเดอร์ `TestCode/` เรียบร้อยแล้ว สคริปต์ `scripts/run_benchmark.py` จะทำหน้าที่ประเมินผลอัตโนมัติ:

### 1. โหมดทดสอบเดี่ยวเฉพาะบั๊ก (Single Bug Verification)
สำหรับใช้ทดสอบระหว่างการพัฒนา เช่น ต้องการรันเฉพาะ `Lang` Bug 1:
```bash
python3 scripts/run_benchmark.py --project Lang --bug 1
```

### 2. โหมด 17 Projects Benchmark (`--sample-17`)
รันประเมินผลกลุ่มตัวแทน 17 โปรเจกต์ใน Defects4J:
```bash
python3 scripts/run_benchmark.py --sample-17
```
*ระบบจะวนลูปทดสอบโปรเจกต์ตัวแทนทั้ง 17 ตัว $\times$ 4 เทคนิค = 68 การทดลอง และสรุปผลออกมาเป็นตาราง*

### 3. โหมด Exhaustive Benchmark (`--all-bugs`)
รันประเมินผลกับทุก Active Bug ใน Defects4J โดยดึงรายชื่อบั๊กจาก `defects4j bids` สดตอนรัน:
```bash
python3 scripts/run_benchmark.py --all-bugs
```

### 4. โหมด Resume ทำงานต่อจากจุดเดิม (`--resume`)
หากเกิดเหตุขัดข้อง (เช่น ไฟดับ, เครื่องค้าง, หรือเน็ตหลุด) ระบบจะอ่านสถานะล่าสุดจาก `progress.json` แล้วข้ามบั๊กที่ทำเสร็จแล้วเพื่อทำต่อทันที:
```bash
python3 scripts/run_benchmark.py --all-bugs --resume
```

---

## 📊 การอ่านและตรวจสอบผลลัพธ์ (Output Artifacts)

เมื่อสคริปต์รันเสร็จ ผลลัพธ์จะถูกจัดเก็บเป็น 2 รูปแบบ:

1. **ไฟล์ผลลัพธ์ละเอียดรายบั๊ก (JSON format):**
   เก็บไว้ที่ `results/<Project>/<Bug_ID>/<Technique>.json` เช่น:
   * `results/Lang/1/ipo.json`
   * `results/Lang/1/mio.json`
   * `results/Lang/1/claude.json`
   * `results/Lang/1/gemini.json`
   ข้างในจะเก็บบันทึกค่า Line Coverage, Branch Coverage, สถานะ Fault Detection (`DEFECT_TRIGGERED` / `FLAKY` / `COMPILE_ERROR`) และ Execution Time
2. **ตารางผลรวมทั้งหมด (CSV format):**
   เก็บไว้ที่ `results/benchmark_results.csv` สามารถนำไปเปิดใน Excel หรือเขียนสคริปต์พล็อตเป็นกราฟแท่งเปรียบเทียบในเล่มรายงานได้ทันที

---

## 🧹 การจัดการทรัพยากรเครื่อง (Resource & Disk Management)

* สคริปต์ Runner จะสร้างโฟลเดอร์ทำงานชั่วคราวไว้ที่ `/tmp/d4j_eval/` ในระหว่างที่คอมไพล์และรันเทส
* เมื่อประเมินผลแต่ละบั๊กเสร็จสิ้น สคริปต์จะสั่ง **ลบโฟลเดอร์ชั่วคราวทิ้งทันทีอัตโนมัติ** เพื่อไม่ให้พื้นที่ Hard Disk เต็ม (กินพื้นที่ดิสก์หมุนเวียนไม่เกิน 2-3 GB ตลอดการรัน)
