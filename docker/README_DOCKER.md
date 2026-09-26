# คู่มือการใช้งาน Docker Environment & Universal Runner สำหรับ Defects4J

คู่มือนี้บันทึกสภาพแวดล้อม Docker ที่ใช้ประเมินผลและข้อกำหนดก่อนเปิด container เพื่อให้สมาชิกตรวจสอบรุ่นเครื่องมือกับ snapshot ผลได้ตรงกัน

---

## 🛠️ สภาพแวดล้อมที่จัดเตรียมไว้ใน Container

1. **Multi-JDK Environment:** ติดตั้ง OpenJDK 8 และ 11; `JAVA_HOME` เริ่มที่ JDK 11 สำหรับ Defects4J CLI ส่วน MIO generation บางขั้นตอนกำหนด JDK 8
2. **Defects4J Framework:** Dockerfile checkout commit `8c16da8230843cdc918eaf4ddb449637f02b83c6` (`3.0.1-7-g8c16da82`); Defects4J 3.x ใช้ Java 11
3. **Microsoft PICT Tool:** คอมไพล์และติดตั้งไว้ที่ `/usr/local/bin/pict` (พร้อมให้ Member 1 รันได้ทันที)
4. **EvoSuite Framework (Version 1.0.6):** จัดเตรียมไฟล์ `evosuite-1.0.6.jar` และ `evosuite-standalone-runtime-1.0.6.jar` ไว้ที่ `/opt/evosuite/` (พร้อมให้ Member 2 รัน MIO)
5. **Python 3:** container มี `requests` และ `tabulate` สำหรับ runner บางส่วน; analytics บน host ใช้ dependencies จาก `requirements-member4.txt`

Dockerfile เริ่มจาก Ubuntu 20.04 และติดตั้ง dependencies, Defects4J commit ที่ตรึงไว้, PICT commit ที่ตรึงไว้ และ EvoSuite 1.0.6 พร้อมตรวจ SHA-256 ของ JAR. การ build ครั้งแรกดาวน์โหลดและ initialize project repositories ของ Defects4J จึงใช้พื้นที่และเวลามาก; ครั้งต่อไป Docker ใช้ build cache.

---

## 🚀 ขั้นตอนการเปิดใช้งาน

### 1. Build และสั่ง Start Container
เปิด Terminal / PowerShell ในโฟลเดอร์หลักของโปรเจกต์ (`ProjectSQA/`):

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

### 2. เข้าสู่ Terminal ของ Container
```bash
docker exec -it defects4j_sqa bash
```

### 3. ตรวจสอบความพร้อมของระบบ (Sanity Check)
```bash
# ตรวจว่ารายชื่อโปรเจกต์และบั๊กอ่านได้
defects4j pids
defects4j bids -p Lang | head

# ตรวจสอบเวอร์ชัน Java (ค่าเริ่มต้นเป็น Java 11)
java -version

# ตรวจสอบ Defects4J ที่ติดตั้ง
git -C /opt/defects4j rev-parse HEAD

# ตรวจสอบคำสั่ง PICT ของ Member 1
pict

# ตรวจสอบ EvoSuite ของ Member 2
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
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
