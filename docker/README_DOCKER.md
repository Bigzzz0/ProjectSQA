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
- หากสมาชิกทำการเจน Test Code (เช่น จาก EvoSuite, ACTS หรือ AI) สามารถเซฟไว้ใน `/workspace/Combinatorial_IPO/TestCode` หรือโฟลเดอร์ที่เกี่ยวข้องได้ทันที
