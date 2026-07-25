# MIO Algorithm (Mutation Insertion Optimization) via EvoSuite

**ผู้รับผิดชอบหลัก:** Member 2 (Algorithm Lead 2 - MIO Specialist)

---

## 📌 บทบาทและความรับผิดชอบ
1. ศึกษาขั้นตอนวิธี **Mutation Insertion Optimization (MIO)** ใน Search-Based Software Testing (SBST)
2. กำหนด Configuration สำหรับ EvoSuite โดยเลือก `-Dalgorithm=MIO`
3. ทำการทดลองเปรียบเทียบผลลัพธ์ภายใต้ **Search Budget** ต่างๆ (เช่น 30 วินาที, 60 วินาที, 120 วินาที ต่อ Class)
4. ทำการทดลองซ้ำหลายรอบเพื่อคำนวณหาค่าเฉลี่ย (Mean) และส่วนเบี่ยงเบนมาตรฐาน (SD)
5. สรุปจุดเด่น ข้อจำกัด และประสิทธิภาพในการตรวจจับข้อบกพร่อง

---

## 📂 โครงสร้างโฟลเดอร์

```text
MIO_Algorithm/
├── Code/                          # สคริปต์สั่งรัน EvoSuite อัตโนมัติ (run_evosuite_mio.sh)
├── Configuration/                 # ไฟล์คอนฟิก Search Budget (30s, 60s, 120s)
├── Result_Round1/                 # ผลการทดลองรอบที่ 1
├── Result_Round2/                 # ผลการทดลองรอบที่ 2 (Multiple Runs)
└── TestCode/                      # JUnit Test Cases ที่ EvoSuite สร้างขึ้น
```

---

## 🛠️ ตัวอย่างคำสั่งรัน EvoSuite MIO บน Defects4J

```bash
# สั่งรัน EvoSuite บน Class Target ด้วย MIO Algorithm และกำหนด Search Budget 60 วินาที
java -jar evosuite.jar \
  -class org.apache.commons.lang3.StringUtils \
  -projectCP target/classes \
  -Dalgorithm=MIO \
  -Dsearch_budget=60 \
  -Dcriterion=LINE:BRANCH:EXCEPTION \
  -Dreport_dir=MIO_Algorithm/Result_Round1
```
