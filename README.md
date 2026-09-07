# Project - AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark and Test Coverage Evaluation

**รายวิชา:** CP353201 Software Quality Assurance (ปีการศึกษา 1/2569)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก | **หลักสูตร:** วิทยาการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น  

---

## 👥 รายชื่อสมาชิกในกลุ่ม (Group Members)

| ลำดับ | รหัสนักศึกษา | ชื่อ - สกุล | บทบาทหน้าที่ในโครงการ |
| :---: | :---: | :--- | :--- |
| 1 | 673380278-9 | นายปวริศช์ ประมวล | **Member 1: Algorithm Lead 1** (IPO Algorithm Specialist / PICT Tool) |
| 2 | 673380301-0 | นายแทนคุณ พันธ์นิกุล | **Member 2: Algorithm Lead 2** (MIO Algorithm Specialist / EvoSuite) |
| 3 | 673380272-1 | นายธนภูมิ จันทรา | **Member 3: AI Prompt Engineer & Test Automation** (Claude 4.6 & Gemini 3.6 Flash) |
| 4 | 673380292-5 | นายศิฆรินทร์ อุปจันทร์ | **Member 4: Infrastructure & Data Analysis Lead** (Defects4J & Repository Manager) |

---

## 📂 โครงสร้าง Repository (Directory Structure)

โปรเจกต์นี้จัดวางโครงสร้างโฟลเดอร์ตามข้อกำหนดของอาจารย์ เพื่อให้สามารถตรวจสอบและทำซ้ำ (Reproduce) ผลลัพธ์ได้ง่าย:

```text
ProjectSQA/
├── README.md                          # เอกสารแนะนำโปรเจกต์ ขั้นตอนการรัน และสรุปผลลัพธ์
├── Report_Round1_Draft.md             # ร่างรายงานส่งรอบที่ 1 (Deadline 22 ส.ค. 2569)
├── scripts/                           # สคริปต์เสริมสำหรับอำนวยความสะดวกในการทดลอง
│   └── kku_generate.py                # สคริปต์ยิง KKU IntelSphere API สร้าง Test Suite อัตโนมัติ
├── target_benchmark/                  # ข้อมูลคลาสและ Ground Truth จาก Defects4J
│   └── Lang_1b/                       # Benchmark เป้าหมาย: Commons-Lang (Bug 1)
├── docker/                            # สภาพแวดล้อมกลางสำหรับรัน Defects4J Dataset
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── README_DOCKER.md
├── Combinatorial_IPO/                 # อัลกอริทึมที่ 1: IPO Algorithm (Microsoft PICT)
│   ├── Code/                          # Source code / Wrapper Scripts
│   ├── Configuration/                 # ค่า Parameter / Combination Models
│   ├── Result_Round1/                 # ผลลัพธ์การทดลองรอบที่ 1
│   ├── Result_Round2/                 # ผลลัพธ์การทดลองรอบที่ 2 (ค่าเฉลี่ย/SD)
│   └── TestCode/                      # JUnit Test Cases ที่ IPO สร้างขึ้น
├── MIO_Algorithm/                     # อัลกอริทึมที่ 2: MIO Algorithm (EvoSuite)
│   ├── Code/                          # สคริปต์การสั่งรัน EvoSuite MIO
│   ├── Configuration/                 # ค่า Search Budget (30s, 60s, 120s)
│   ├── Result_Round1/
│   ├── Result_Round2/
│   └── TestCode/                      # JUnit Test Cases ที่ MIO/EvoSuite สร้างขึ้น
├── Claude-sonnet_5/                   # AI Tool 1: Claude Sonnet 5
│   ├── Prompt/                        # System Prompts & Few-Shot Templates
│   ├── Result/                        # ค่า Coverage & Bug Finding Metrics
│   └── TestCode/                      # Java Test Code ที่ Claude สร้างให้
└── Gemini-3_8_flash/                  # AI Tool 2: Gemini 3.8 Flash
    ├── Prompt/                        # System Prompts & Few-Shot Templates
    ├── Result/                        # ค่า Coverage & Bug Finding Metrics
    └── TestCode/                      # Java Test Code ที่ Gemini สร้างให้
```

---

## 🛠️ ขั้นตอนการรันเพื่อทำซ้ำผลลัพธ์ (Steps to Reproduce)

### 1. การเตรียมสภาพแวดล้อม (Environment Setup)

แนะนำให้รันผ่าน Docker Container ที่จัดเตรียมไว้ในโฟลเดอร์ `docker/`:

```bash
# Clone Repository
git clone https://github.com/YourGroup/ProjectSQA.git
cd ProjectSQA

# สั่ง Build และรัน Docker Container
docker-compose -f docker/docker-compose.yml up -d
docker exec -it defects4j_sqa bash
```

### 2. การทดสอบกับ Defects4J Pilot Project (e.g., Commons-Lang)

```bash
# Checkout โปรเจกต์ buggy
defects4j checkout -p Lang -v 1b -w /tmp/Lang_1_buggy
cd /tmp/Lang_1_buggy

# คอมไพล์และรัน Test เดิม
defects4j compile
defects4j test

# วัดผล Code Coverage ด้วย JaCoCo
defects4j coverage
```

---

## 📊 ตารางสรุปผลการเปรียบเทียบประสิทธิภาพ (Benchmark Results)

*จะอัปเดตผลลัพธ์การทดลองในเฟสที่ 2*

| เครื่องมือ / อัลกอริทึม | Line Coverage (%) | Branch Coverage (%) | Fault Detection Rate (Bugs Found) | เวลาที่ใช้สร้าง (Seconds/Minutes) | ข้อจำกัด / ปัญหาที่พบ |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **IPO Algorithm (Microsoft PICT)** | - | - | - | - | - |
| **MIO Algorithm (EvoSuite)** | - | - | - | - | - |
| **Claude Sonnet 5** | - | - | - | - | - |
| **Gemini 3.8 Flash** | - | - | - | - | - |

---

## 📅 กำหนดการนำส่งงาน (Deliverables Schedule)

1. **รายงานรอบที่ 1 (5%)**: ส่งภายในวันที่ 22 สิงหาคม 2569 (ก่อนสอบกลางภาค) ทาง Google Classroom
2. **รายงานฉบับสมบูรณ์ & GitHub (10%)**: ส่งภายในวันสุดท้ายของการเรียนการสอน
3. **Presentation & Live Demo**: นำเสนอในวันสุดท้ายของการเรียนการสอน
