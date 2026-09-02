# คู่มือการใช้งาน Docker Environment สำหรับ Defects4J

คู่มือนี้จัดทำขึ้นโดย **Member 4 (Infrastructure Lead)** เพื่อให้สมาชิกทุกคนในกลุ่ม (Member 1, 2, 3) สามารถรันสภาพแวดล้อม Defects4J เดียวกันได้สะดวกและไม่มีปัญหาเรื่อง Java/OS Compatibility

---

## 🚀 ขั้นตอนการเปิดใช้งาน

### 1. Build และสั่ง Start Container
เปิด Terminal / PowerShell ในโฟลเดอร์ root ของโปรเจกต์ (`ProjectSQA/`):

```bash
docker-compose -f docker/docker-compose.yml up -d --build
```

### 2. เข้าสู่ Terminal ของ Container
```bash
docker exec -it defects4j_sqa bash
```

### 3. ทดสอบการทำงานของ Defects4J
เมื่ออยู่ใน Container แล้ว ให้ทดลองคำสั่ง Sanity Check:

```bash
defects4j sanity-check
```

---

## 🧪 การทดลองกับ Pilot Project (`Lang-1b`)

```bash
# 1. Checkout โปรเจกต์ buggy Lang 1
defects4j checkout -p Lang -v 1b -w /tmp/Lang_1_buggy

# 2. เข้าไปในโฟลเดอร์โปรเจกต์
cd /tmp/Lang_1_buggy

# 3. สั่งคอมไพล์
defects4j compile

# 4. สั่งรัน Unit Tests เดิมทั้งหมด
defects4j test

# 5. สั่งสกัดค่า Coverage
defects4j coverage
```

---

## 💡 หมายเหตุสำหรับสมาชิกในทีม
- โฟลเดอร์ `ProjectSQA/` บนเครื่องโฮสต์ของคุณจะถูก Sync เข้ากับโฟลเดอร์ `/workspace` ภายใน Container อัตโนมัติ
- สเปกข้อมูลบั๊กและคลาสเป้าหมายถูกจัดเก็บไว้ที่ `target_benchmark/Lang_1b/BUG_SPEC_LANG_1B.md`

---

## ⚙️ สคริปต์อัตโนมัติสำหรับ Member 4 (Automation Tools)

### 1. การสกัด Target Class เพื่อส่งต่อให้เพื่อน (`extract_target.sh`)
รันคำสั่งนี้ภายใน Container เพื่อ Checkout โปรเจกต์ และคัดลอกไฟล์ `.java` ออกมาไว้ที่ Host Workspace ทันที:
```bash
# ตัวอย่าง: สกัดคลาสเป้าหมายและ Ground Truth ของ Lang-1
bash /workspace/docker/extract_target.sh Lang 1
```
*ผลลัพธ์:* ซอร์สโค้ดและข้อมูลบั๊กจะถูกนำไปเก็บไว้ใน `target_benchmark/Lang_1b/` อัตโนมัติ

### 2. การรันประเมินผลและวัดผล Coverage ทุกเครื่องมือ (`evaluate_all.sh`)
เมื่อเพื่อนๆ (Member 1, 2, 3) วางไฟล์ Test ลงในโฟลเดอร์ `TestCode/` ของแต่ละสายงานแล้ว ให้ Member 4 รันคำสั่งนี้เพื่อวัดผลทั้งหมดแบบรวดเดียว:
```bash
bash /workspace/docker/evaluate_all.sh Lang 1
```
*ผลลัพธ์:* สคริปต์จะคอมไพล์ วัดผล Line/Branch Coverage และตรวจสอบ Fault Detection Rate ของทั้ง 4 เครื่องมือ พร้อมสร้างตารางสรุปผลไว้ที่ `benchmark_results.md`
