# MIO Algorithm (Mutation Insertion Optimization) via EvoSuite

**ผู้รับผิดชอบหลัก:** Member 2 (นายแทนคุณ พันธ์นิกุล - Algorithm Lead 2)

> [!TIP]
> **📖 คู่มือปฏิบัติงานและคำสั่งทีละขั้นตอน:** สามารถอ่านฉบับเต็มได้ที่ [TEAM_WORKFLOW_GUIDE.md (ส่วนของ Member 2)](../TEAM_WORKFLOW_GUIDE.md#-member-2-นายแทนคุณ-พันธ์นิกุล-algorithm-lead-2---mio--evosuite)

---

## 📌 บทบาทและคำนิยามทางวิชาการ (Academic Context)

* **ขั้นตอนวิธี (Algorithm):** **Mutation Insertion Optimization (MIO)** เป็น Search-Based Software Testing (SBST) Algorithm ที่ออกแบบโดย Andrea Arcuri (2018) สำหรับการสร้าง Test Suite แบบไดนามิก โดย MIO จะเก็บ Candidate Tests ไว้ในคลังข้อมูล (Archive) และค้นหากรณีทดสอบใหม่ด้วยการสุ่มแทรก (Insertion) และปรับเปลี่ยน (Mutation) ภายใต้ Search Budget ที่กำหนด
* **เครื่องมือที่เลือกใช้ (Tool):** **EvoSuite Framework (Version 1.0.6)** โดยกำหนดพารามิเตอร์ `-Dalgorithm=MIO`
* **เป้าหมายของ Member 2:** สร้าง JUnit Test Suite ระดับ State-of-the-Art ให้กับ **Target Modified Classes** ใน Defects4J พร้อมทำการทดลองเปรียบเทียบ Search Budget ต่างๆ ตามข้อกำหนด 1.7 ของรายวิชา

---

## 🛠️ ขั้นตอนการทำงานจริงแบบละเอียด (Step-by-Step Workflow)

### ขั้นตอนที่ 1: เตรียม Classpath ของโปรเจกต์ใน Defects4J
การรัน EvoSuite ต้องรันภายใน **Defects4J Docker Container** เพราะต้องใช้ Compiled Bytecode และ Dependencies ทั้งหมดของโปรเจกต์:

```bash
# 1. เข้าไปในโฟลเดอร์ของโปรเจกต์ที่ Checkout ไว้
cd /tmp/Lang_1_buggy

# 2. คอมไพล์โปรเจกต์ให้ได้ Bytecode
defects4j compile

# 3. ดึง Classpath ทั้งหมดของโปรเจกต์ออกมาเก็บในตัวแปร CP (สำคัญมาก!)
CP=$(defects4j export -p cp.compile)
```

---

### ขั้นตอนที่ 2: สั่งรัน EvoSuite MIO บน Target Class
สั่งรัน EvoSuite โดยส่ง Classpath ของโปรเจกต์เข้าไป พร้อมระบุ Target Class:

```bash
# ตัวอย่าง: รัน MIO บน NumberUtils (Budget 60 วินาที)
java -jar /opt/evosuite/evosuite-1.0.6.jar \
  -class org.apache.commons.lang3.math.NumberUtils \
  -projectCP $CP \
  -Dalgorithm=MIO \
  -Dcriterion=LINE:BRANCH:EXCEPTION:MUTATION \
  -Dsearch_budget=60 \
  -base_dir /workspace/MIO_Algorithm/TestCode
```

*ผลลัพธ์:* EvoSuite จะสร้างไฟล์ 2 ไฟล์ออกมาใน `TestCode/`:
1. `<TargetClass>_ESTest.java` (ไฟล์เทสหลัก)
2. `<TargetClass>_ESTest_scaffolding.java` (ไฟล์ตั้งค่า Sandbox ของ EvoSuite)

---

### ขั้นตอนที่ 3: การทดลองปรับ Search Budget และ Multiple Runs (ตามข้อ 1.7)
ตามเกณฑ์ข้อ 1.7 ในใบงานอาจารย์ Member 2 ต้องทำการทดลองเปรียบเทียบ **Search Budget ที่แตกต่างกัน** และรันซ้ำหลายรอบเพื่อหาค่าเฉลี่ย (Mean) และส่วนเบี่ยงเบนมาตรฐาน (SD):

#### 1. การทดลองปรับ Budget:
* **Budget = 30 วินาที:** วัดประสิทธิภาพในโหมด Fast Exploration
* **Budget = 60 วินาที:** โหมด Default มาตรฐาน
* **Budget = 120 วินาที:** วัดผลว่าเมื่อให้เวลาค้นหาเพิ่มขึ้น Coverage และ Mutation Score เพิ่มขึ้นหรือไม่

#### 2. การทดลองซ้ำ (Multiple Runs):
เนื่องจาก MIO มีส่วนประกอบของการสุ่ม (Stochastic / Evolutionary Algorithm) ในแต่ละ Budget ต้อง**รันซ้ำ 3–5 รอบ** ด้วยการเปลี่ยน Random Seed:
```bash
# ตัวอย่างรันซ้ำด้วย Random Seed ต่างกัน
for SEED in 101 202 303; do
  java -jar /opt/evosuite/evosuite-1.0.6.jar \
    -class org.apache.commons.lang3.math.NumberUtils \
    -projectCP $CP \
    -Dalgorithm=MIO \
    -Dsearch_budget=60 \
    -Drandom_seed=$SEED \
    -Dreport_dir=/workspace/MIO_Algorithm/Result_Round2/seed_$SEED
done
```

---

### ขั้นตอนที่ 4: การบันทึกสถิติและผลลัพธ์ (Reporting)
รวบรวมค่าสถิติจากรายงานของ EvoSuite (`statistics.csv`) นำมาบันทึกลงใน:
* `MIO_Algorithm/Result_Round1/` (ผลการรันรอบแรก)
* `MIO_Algorithm/Result_Round2/budget_comparison.md` (ตารางเปรียบเทียบ Budget 30s vs 60s vs 120s พร้อมค่า Mean/SD)

#### ตัวอย่างตารางที่ต้องส่งใน Result_Round2/:
| Search Budget | Line Coverage (Mean ± SD) | Branch Coverage (Mean ± SD) | Mutation Score (%) | เวลาที่ใช้เฉลี่ย |
| :---: | :---: | :---: | :---: | :---: |
| **30 วินาที** | 76.2% ± 2.1% | 68.4% ± 1.8% | 61.5% | 34.2s |
| **60 วินาที** | 83.5% ± 1.4% | 74.8% ± 1.2% | 72.0% | 66.8s |
| **120 วินาที** | 86.1% ± 0.9% | 77.3% ± 0.8% | 75.8% | 128.5s |

---

### ขั้นตอนที่ 5: การส่งมอบ Test Code ให้ Runner ของ Member 4
* วางไฟล์ `<TargetClass>_ESTest.java` และ `<TargetClass>_ESTest_scaffolding.java` ไว้ที่:
  `MIO_Algorithm/TestCode/`
* **เรื่อง Runtime Dependency:** โค้ดของ EvoSuite มีการเรียกใช้ `org.evosuite.runtime.*` ซึ่ง Member 4 ได้จัดเตรียม `evosuite-standalone-runtime-1.0.6.jar` ไว้ในระบบแล้ว ทำให้ Member 4 สามารถนำไฟล์นี้ไปรันประเมินผลใน Defects4J ได้ทันทีโดยไม่ติด Compile Error

---

## 📂 รายการไฟล์ที่ Member 2 ต้องส่งมอบ

* **รอบที่ 1 (Phase 1):**
  * `Configuration/evosuite_mio_config.properties`
  * `Result_Round1/statistics_Lang1.csv`
  * `TestCode/NumberUtils_ESTest.java` & `_scaffolding.java`
* **รอบที่ 2 (Phase 2):**
  * ขยายผลรันกับ Target Classes ในชุด Benchmark
  * `Result_Round2/budget_comparison.md` (ตาราง Mean ± SD ตามข้อ 1.7)
  * สรุปจุดเด่น/ข้อจำกัดของ MIO ในเชิง Search-Based Testing
