# Combinatorial Testing: IPO Algorithm (Microsoft PICT Tool)

**ผู้รับผิดชอบหลัก:** Member 1 (นายปวริศช์ ประมวล - Algorithm Lead 1)

---

## 📌 บทบาทและความรับผิดชอบ
1. ศึกษาขั้นตอนวิธี **In-Parameter-Order (IPO)** และการสร้างกรณีทดสอบแบบ Combinatorial Testing
2. ใช้เครื่องมือ **Microsoft PICT (Pairwise Independent Combinatorial Testing)** ในการสร้าง Input Combination Models
3. พัฒนา Wrapper / Script เพื่อแปลง Input Combinations จาก PICT ให้อยู่ในรูปของ JUnit Test Cases (`.java`)
4. รันการทดสอบบน Defects4J Dataset บันทึก Log และสถิติดรรชนีชี้วัด

---

## 📂 โครงสร้างโฟลเดอร์

```text
Combinatorial_IPO/
├── Code/                          # Script แปลง Output ของ PICT เป็น JUnit Test Case
├── Configuration/                 # ไฟล์แบบจำลอง (.txt) สำหรับ PICT (Parameters, Values, Constraints)
├── Result_Round1/                 # ผลการวัดผลรอบที่ 1
├── Result_Round2/                 # ผลการวัดผลรอบที่ 2 (ค่าเฉลี่ย/SD)
└── TestCode/                      # Java JUnit Test Cases ที่เจนได้จาก PICT
```

---

## 🛠️ ขั้นตอนการรัน Microsoft PICT Tool

### 1. รูปแบบไฟล์ Input Model (`Configuration/model.txt`)
สร้างไฟล์ข้อความระบุ Parameter และ Values ของ Target Method ใน Defects4J:

```text
# ตัวอย่าง model.txt สำหรับระบุพารามิเตอร์
Type: String, int, double, boolean
Length: positive, zero, negative
Mode: FAST, STRICT, RELAXED

# Constraints (ถ้ามี)
IF [Type] = "boolean" THEN [Length] = "positive";
```

### 2. สั่งรันคำสั่ง PICT เพื่อเจน Combinations
```bash
# รัน PICT ผ่าน CLI ( Pairwise / 2-way )
pict Configuration/model.txt > Result_Round1/combinations.txt

# กำหนด Order ของ Combination (เช่น 3-way testing ด้วยตัวเลือก /o:3)
pict Configuration/model.txt /o:3 > Result_Round1/combinations_3way.txt
```

### 3. แปลง Combinations เป็น JUnit Test Cases
นำผลลัพธ์จาก `combinations.txt` ไปผ่าน Wrapper Script ในโฟลเดอร์ `Code/` เพื่อสร้างไฟล์ `.java` เก็บลงใน `TestCode/`
