# ⚡ MIO Algorithm: Multi-Terminal Run Cheat Sheet (คู่มือคำสั่งรันจริงแบบแบ่งหน้าต่าง)

**สำหรับ:** Member 2 (นายแทนคุณ พันธ์นิกุล - MIO / EvoSuite Specialist)  
**โครงการ:** CP353201 Software Quality Assurance (KKU CS)  
**เป้าหมาย:** สรุปคำสั่ง Ready-to-Copy สำหรับเปิด **2 เทอร์มินัลรันคู่ขนานกัน (Dual-Terminal Parallel Run)** ครบทั้ง 17 โปรเจกต์ (854 บั๊ก) พร้อมคำสั่ง Git Commit อัตโนมัติทีละโปรเจกต์

---

## 🖥️ วิธีตั้งค่าเปิด 2 เทอร์มินัลคู่ขนาน (Dual-Terminal Setup)

> [!TIP]
> **เทคนิคเปิดหน้าต่างซ้าย-ขวาบน Windows Terminal:**
> 1. เปิด **PowerShell** ในโฟลเดอร์โปรเจกต์ `ProjectSQA`
> 2. กดปุ่มลัด **`Alt + Shift + D`** (หรือกดเครื่องหมาย `v` เล็กๆ ด้านบนเลือก Split Pane) จะได้หน้าต่างแบ่งครึ่ง ซ้าย-ขวา ทันที
> 3. หน้าต่างซ้าย = **Terminal 1** | หน้าต่างขวา = **Terminal 2**
> 4. ตรวจสอบว่า Docker Container ทำงานอยู่เสมอ:
>    ```powershell
>    docker ps   # ต้องเห็นคอนเทนเนอร์ defects4j_sqa สถานะ Up
>    ```

---

## 📋 สารบัญและ Checklist ติดตามความคืบหน้า (Progress Tracker)

- [ ] [Phase 1: กลุ่มโปรเจกต์ขนาดเล็ก (Quick Wins: ~2–3 ชม.)](#-phase-1-quick-wins-โปรเจกต์ขนาดเล็ก-รวม-58-บั๊ก)
  - [ ] `JacksonXml` (6 บั๊ก)
  - [ ] `Csv` (16 บั๊ก)
  - [ ] `Codec` (18 บั๊ก)
  - [ ] `Gson` (18 บั๊ก)
- [ ] [Phase 2: กลุ่มโปรเจกต์ขนาดกลาง (50/50 Dual-Terminal: ~4–5 ชม.)](#-phase-2-medium-projects-โปรเจกต์ขนาดกลาง-รวม-130-บั๊ก)
  - [ ] `JxPath` (22 บั๊ก)
  - [ ] `Chart` (26 บั๊ก)
  - [ ] `JacksonCore` (26 บั๊ก)
  - [ ] `Time` (26 บั๊ก)
  - [ ] `Collections` (28 บั๊ก)
- [ ] [Phase 3: กลุ่มโปรเจกต์ขนาดใหญ่ (Heavy Duty - 3 Terminals: ~6 ชม.)](#-phase-3-heavy-duty-โปรเจกต์ขนาดใหญ่-รวม-185-บั๊ก)
  - [ ] `Mockito` (38 บั๊ก)
  - [ ] `Cli` (39 บั๊ก)
  - [ ] `Compress` (47 บั๊ก)
  - [ ] `Lang` (61 บั๊ก)
- [ ] [Phase 4: กลุ่มโปรเจกต์ยักษ์ใหญ่ (Mega Projects - 3 Terminals: รันข้ามคืน)](#-phase-4-mega-projects-โปรเจกต์ยักษ์ใหญ่-รวม-483-บั๊ก---แบ่ง-3-เทอร์มินัล-รันคู่ขนาน)
  - [ ] `Jsoup` (93 บั๊ก)
  - [ ] `Math` (106 บั๊ก)
  - [ ] `JacksonDatabind` (110 บั๊ก)
  - [ ] `Closure` (174 บั๊ก)

---

## 🟢 Phase 1: Quick Wins (โปรเจกต์ขนาดเล็ก รวม 58 บั๊ก)

### 1. [ ] JacksonXml (6 บั๊ก) - รันเทอร์มินัลเดียว รวดเดียวจบ (~10-15 นาที)
```powershell
# [Terminal 1]:
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonXml

# เมื่อรันเสร็จ สั่ง Commit ทันที:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/JacksonXml_*
git commit -m "feat(mio): complete EvoSuite MIO generation for JacksonXml (bugs 1-6)"
git push origin main
```

---

### 2. [ ] Csv (16 บั๊ก) - แบ่ง 2 เทอร์มินัล (~30-40 นาที)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 8
python MIO_Algorithm/Code/batch_evosuite.py --project Csv --start-bug 1 --end-bug 8

# [Terminal 2 - ขวา]: บั๊ก 9 ถึง 16
python MIO_Algorithm/Code/batch_evosuite.py --project Csv --start-bug 9 --end-bug 16

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Csv_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Csv (bugs 1-16)"
git push origin main
```

---

### 3. [ ] Codec (18 บั๊ก) - แบ่ง 2 เทอร์มินัล (~45 นาที)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 9
python MIO_Algorithm/Code/batch_evosuite.py --project Codec --start-bug 1 --end-bug 9

# [Terminal 2 - ขวา]: บั๊ก 10 ถึง 18
python MIO_Algorithm/Code/batch_evosuite.py --project Codec --start-bug 10 --end-bug 18

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Codec_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Codec (bugs 1-18)"
git push origin main
```

---

### 4. [ ] Gson (18 บั๊ก) - แบ่ง 2 เทอร์มินัล (~45 นาที)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 9
python MIO_Algorithm/Code/batch_evosuite.py --project Gson --start-bug 1 --end-bug 9

# [Terminal 2 - ขวา]: บั๊ก 10 ถึง 18
python MIO_Algorithm/Code/batch_evosuite.py --project Gson --start-bug 10 --end-bug 18

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Gson_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Gson (bugs 1-18)"
git push origin main
```

---

## 🟡 Phase 2: Medium Projects (โปรเจกต์ขนาดกลาง รวม 130 บั๊ก)

### 5. [ ] JxPath (22 บั๊ก) - แบ่ง 2 เทอร์มินัล (~1 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 11
python MIO_Algorithm/Code/batch_evosuite.py --project JxPath --start-bug 1 --end-bug 11

# [Terminal 2 - ขวา]: บั๊ก 12 ถึง 22
python MIO_Algorithm/Code/batch_evosuite.py --project JxPath --start-bug 12 --end-bug 22

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/JxPath_*
git commit -m "feat(mio): complete EvoSuite MIO generation for JxPath (bugs 1-22)"
git push origin main
```

---

### 6. [ ] Chart (26 บั๊ก) - แบ่ง 2 เทอร์มินัล (~1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 13
python MIO_Algorithm/Code/batch_evosuite.py --project Chart --start-bug 1 --end-bug 13

# [Terminal 2 - ขวา]: บั๊ก 14 ถึง 26
python MIO_Algorithm/Code/batch_evosuite.py --project Chart --start-bug 14 --end-bug 26

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Chart_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Chart (bugs 1-26)"
git push origin main
```

---

### 7. [ ] JacksonCore (26 บั๊ก) - แบ่ง 2 เทอร์มินัล (~1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 13
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonCore --start-bug 1 --end-bug 13

# [Terminal 2 - ขวา]: บั๊ก 14 ถึง 26
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonCore --start-bug 14 --end-bug 26

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/JacksonCore_*
git commit -m "feat(mio): complete EvoSuite MIO generation for JacksonCore (bugs 1-26)"
git push origin main
```

---

### 8. [ ] Time (26 บั๊ก: 1–27 ข้าม 21) - แบ่ง 2 เทอร์มินัล (~1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 13
python MIO_Algorithm/Code/batch_evosuite.py --project Time --start-bug 1 --end-bug 13

# [Terminal 2 - ขวา]: บั๊ก 14 ถึง 27
python MIO_Algorithm/Code/batch_evosuite.py --project Time --start-bug 14 --end-bug 27

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Time_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Time (bugs 1-27)"
git push origin main
```

---

### 9. [ ] Collections (28 บั๊ก) - แบ่ง 2 เทอร์มินัล (~1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 14
python MIO_Algorithm/Code/batch_evosuite.py --project Collections --start-bug 1 --end-bug 14

# [Terminal 2 - ขวา]: บั๊ก 15 ถึง 28
python MIO_Algorithm/Code/batch_evosuite.py --project Collections --start-bug 15 --end-bug 28

# เมื่อทั้ง 2 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Collections_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Collections (bugs 1-28)"
git push origin main
```

---

## 🟠 Phase 3: Heavy Duty (โปรเจกต์ขนาดใหญ่ รวม 185 บั๊ก)

> [!TIP]
> **วิธีเปิด 3 หน้าต่างบน Windows Terminal (Triple-Pane):**
> 1. หน้าต่าง 1 + 2: กด **`Alt + Shift + D`** เพื่อแบ่งครึ่งซ้าย-ขวา
> 2. หน้าต่าง 3: คลิกที่หน้าต่างขวา แล้วกด **`Alt + Shift + -`** (แบ่งล่าง) หรือกด **`Alt + Shift + D`** ซ้ำเพื่อแบ่ง 3 ช่องแนวตั้งตามความถนัด

### 10. [ ] Mockito (38 บั๊ก) - แบ่ง 3 เทอร์มินัล (~1.2–1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 13
python MIO_Algorithm/Code/batch_evosuite.py --project Mockito --start-bug 1 --end-bug 13

# [Terminal 2 - กลาง]: บั๊ก 14 ถึง 26
python MIO_Algorithm/Code/batch_evosuite.py --project Mockito --start-bug 14 --end-bug 26

# [Terminal 3 - ขวา]: บั๊ก 27 ถึง 38
python MIO_Algorithm/Code/batch_evosuite.py --project Mockito --start-bug 27 --end-bug 38

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Mockito_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Mockito (bugs 1-38)"
git push origin main
```

---

### 11. [ ] Cli (39 บั๊ก: 1–40 ข้าม 6) - แบ่ง 3 เทอร์มินัล (~1.2–1.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 13
python MIO_Algorithm/Code/batch_evosuite.py --project Cli --start-bug 1 --end-bug 13

# [Terminal 2 - กลาง]: บั๊ก 14 ถึง 26
python MIO_Algorithm/Code/batch_evosuite.py --project Cli --start-bug 14 --end-bug 26

# [Terminal 3 - ขวา]: บั๊ก 27 ถึง 40
python MIO_Algorithm/Code/batch_evosuite.py --project Cli --start-bug 27 --end-bug 40

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Cli_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Cli (bugs 1-40)"
git push origin main
```

---

### 12. [ ] Compress (47 บั๊ก) - แบ่ง 3 เทอร์มินัล (~1.5–1.8 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 16
python MIO_Algorithm/Code/batch_evosuite.py --project Compress --start-bug 1 --end-bug 16

# [Terminal 2 - กลาง]: บั๊ก 17 ถึง 32
python MIO_Algorithm/Code/batch_evosuite.py --project Compress --start-bug 17 --end-bug 32

# [Terminal 3 - ขวา]: บั๊ก 33 ถึง 47
python MIO_Algorithm/Code/batch_evosuite.py --project Compress --start-bug 33 --end-bug 47

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Compress_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Compress (bugs 1-47)"
git push origin main
```

---

### 13. [ ] Lang (61 บั๊ก: 1–65) - แบ่ง 3 เทอร์มินัล (~2–2.2 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 22
python MIO_Algorithm/Code/batch_evosuite.py --project Lang --start-bug 1 --end-bug 22

# [Terminal 2 - กลาง]: บั๊ก 23 ถึง 44
python MIO_Algorithm/Code/batch_evosuite.py --project Lang --start-bug 23 --end-bug 44

# [Terminal 3 - ขวา]: บั๊ก 45 ถึง 65
python MIO_Algorithm/Code/batch_evosuite.py --project Lang --start-bug 45 --end-bug 65

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Lang_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Lang (bugs 1-65)"
git push origin main
```

---

## 🔴 Phase 4: Mega Projects (โปรเจกต์ยักษ์ใหญ่ รวม 483 บั๊ก - แบ่ง 3 เทอร์มินัล รันคู่ขนาน)

### 14. [ ] Jsoup (93 บั๊ก) - แบ่ง 3 เทอร์มินัล (~2.5–3 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 31
python MIO_Algorithm/Code/batch_evosuite.py --project Jsoup --start-bug 1 --end-bug 31

# [Terminal 2 - กลาง]: บั๊ก 32 ถึง 62
python MIO_Algorithm/Code/batch_evosuite.py --project Jsoup --start-bug 32 --end-bug 62

# [Terminal 3 - ขวา]: บั๊ก 63 ถึง 93
python MIO_Algorithm/Code/batch_evosuite.py --project Jsoup --start-bug 63 --end-bug 93

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Jsoup_* MIO_Algorithm/Result_Round2/raw_reports/Jsoup_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Jsoup (bugs 1-93)"
git push origin main
```

---

### 15. [ ] Math (106 บั๊ก) - แบ่ง 3 เทอร์มินัล (~3–3.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 35
python MIO_Algorithm/Code/batch_evosuite.py --project Math --start-bug 1 --end-bug 35

# [Terminal 2 - กลาง]: บั๊ก 36 ถึง 70
python MIO_Algorithm/Code/batch_evosuite.py --project Math --start-bug 36 --end-bug 70

# [Terminal 3 - ขวา]: บั๊ก 71 ถึง 106
python MIO_Algorithm/Code/batch_evosuite.py --project Math --start-bug 71 --end-bug 106

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Math_* MIO_Algorithm/Result_Round2/raw_reports/Math_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Math (bugs 1-106)"
git push origin main
```

---

### 16. [ ] JacksonDatabind (110 บั๊ก: 1–112) - แบ่ง 3 เทอร์มินัล (~3.5 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 37
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonDatabind --start-bug 1 --end-bug 37

# [Terminal 2 - กลาง]: บั๊ก 38 ถึง 74
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonDatabind --start-bug 38 --end-bug 74

# [Terminal 3 - ขวา]: บั๊ก 75 ถึง 112
python MIO_Algorithm/Code/batch_evosuite.py --project JacksonDatabind --start-bug 75 --end-bug 112

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/JacksonDatabind_* MIO_Algorithm/Result_Round2/raw_reports/JacksonDatabind_*
git commit -m "feat(mio): complete EvoSuite MIO generation for JacksonDatabind (bugs 1-112)"
git push origin main
```

---

### 17. [ ] Closure (174 บั๊ก: 1–176) - แบ่ง 3 เทอร์มินัล (รันข้ามคืน ~5–6 ชม.)
```powershell
# [Terminal 1 - ซ้าย]: บั๊ก 1 ถึง 58
python MIO_Algorithm/Code/batch_evosuite.py --project Closure --start-bug 1 --end-bug 58

# [Terminal 2 - กลาง]: บั๊ก 59 ถึง 116
python MIO_Algorithm/Code/batch_evosuite.py --project Closure --start-bug 59 --end-bug 116

# [Terminal 3 - ขวา]: บั๊ก 117 ถึง 176
python MIO_Algorithm/Code/batch_evosuite.py --project Closure --start-bug 117 --end-bug 176

# เมื่อทั้ง 3 เทอร์มินัลรันเสร็จ สั่ง Commit:
git add MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv MIO_Algorithm/progress_mio.json MIO_Algorithm/TestCode/Closure_* MIO_Algorithm/Result_Round2/raw_reports/Closure_*
git commit -m "feat(mio): complete EvoSuite MIO generation for Closure (bugs 1-176)"
git push origin main
```

---

## 🛠️ เครื่องมือตรวจเช็คและคำสั่งฉุกเฉิน (Quick Utilities)

### 1. ตรวจสอบจำนวนแถวที่รันสำเร็จแล้วใน CSV:
```powershell
Get-Content MIO_Algorithm/Result_Round2/evosuite_budget_summary.csv | Measure-Object -Line
```

### 2. ตรวจสอบการใช้ทรัพยากร CPU และ RAM ของ Docker แบบ Real-time:
```powershell
docker stats defects4j_sqa
```

### 3. ดูตัวอย่างคลาสที่จะรันล่วงหน้าโดยยังไม่เริ่มรัน (Dry Run):
```powershell
python MIO_Algorithm/Code/batch_evosuite.py --project Chart --start-bug 1 --end-bug 13 --dry-run
```

### 4. สั่งรันซ้ำแบบบังคับ (Force Rerun ข้าม Checkpoint):
```powershell
python MIO_Algorithm/Code/batch_evosuite.py --project Chart --bug 1 --force
```
