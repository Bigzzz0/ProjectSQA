# รายงานโครงการรอบที่ 1 (Phase 1 Report)

**ชื่อโครงการ:** Project – AI-Assisted Testing vs. Automatic Test Case Generation Algorithms: A Benchmark and Test Coverage Evaluation  
**รายวิชา:** CP353201 Software Quality Assurance (ปีการศึกษา 1/2569)  
**อาจารย์ประจำวิชา:** ผศ.ดร.ชิตสุธา สุ่มเล็ก  
**กำหนดส่ง:** 22 สิงหาคม 2569 (น้ำหนักคะแนน 5%)

---

## 👥 รายชื่อสมาชิกกลุ่ม

| ลำดับ | รหัสนักศึกษา | ชื่อ - นามสกุล | บทบาทในกลุ่ม |
| :---: | :---: | :--- | :--- |
| 1 | 673380278-9 | นายปวริศช์ ประมวล | Member 1: Algorithm Lead 1 (IPO Specialist) |
| 2 | 673380301-0 | นายแทนคุณ พันธ์นิกุล | Member 2: Algorithm Lead 2 (MIO Specialist) |
| 3 | 673380272-1 | นายธนภูมิ จันทรา | Member 3: AI Prompt Engineer |
| 4 | 673380292-5 | นายศิฆรินทร์ อุปจันทร์ | Member 4: Infrastructure & Data Analysis Lead |

---

## 1. บทนำและวัตถุประสงค์ (Introduction & Objectives)

*(ผู้เขียนหลัก: Member 4)*

### 1.1 ที่มาและความสำคัญ

การทดสอบซอฟต์แวร์ระดับหน่วย (Unit Testing) เป็นกระบวนการสำคัญในการรับประกันคุณภาพของซอฟต์แวร์ โครงการนี้มีวัตถุประสงค์เพื่อศึกษาและเปรียบเทียบประสิทธิภาพระหว่าง **Automatic Test Case Generation Algorithms** (IPO Algorithm และ MIO Algorithm) กับ **AI-Assisting Tools** (Claude Sonnet 5 และ Gemini 3.8 Flash ผ่าน Antigravity) บนชุดข้อมูลมาตรฐาน Defects4J Dataset

### 1.2 วัตถุประสงค์

1. เพื่อประยุกต์ใช้ IPO Algorithm (NIST ACTS Tool) และ MIO Algorithm (EvoSuite) ในการสร้างชุดทดสอบ Unit Test แบบอัตโนมัติ
2. เพื่อออกแบบ Prompt Engineering สั่งการ Claude Sonnet 5 และ Gemini 3.8 Flash ให้สร้างชุดทดสอบสำหรับ Java Projects ใน Defects4J
3. เพื่อเปรียบเทียบประสิทธิภาพในมิติของ Code Coverage (Line / Branch Coverage) และ Fault Detection Rate (อัตราการตรวจจับข้อบกพร่องจริง)

---

## 2. ทฤษฎีและการทำงานของ Automated Test Case Generation Algorithms

### 2.1 Combinatorial Testing: In-Parameter-Order (IPO) Algorithm

*(ผู้เขียนหลัก: Member 1 - นายปวริศช์ ประมวล)*

- **หลักการของ IPO Algorithm**: เป็น Deterministic Algorithm สำหรับสร้าง Pairwise (2-way) หรือ t-way Test Combinations โดยขยายกรณีทดสอบทีละพารามิเตอร์ (Horizontal Growth) และเติมกรณีทดสอบใหม่เมื่อครอบคลุมไม่ครบ (Vertical Growth)
- **เครื่องมือที่เลือกใช้**: Microsoft PICT (Pairwise Independent Combinatorial Testing Tool)

### 2.2 Mutation Insertion Optimization (MIO) Algorithm

*(ผู้เขียนหลัก: Member 2)*

- **หลักการของ MIO Algorithm**: เป็น Search-Based Software Testing (SBST) Algorithm ที่ออกแบบมาเพื่อเพิ่ม Code Coverage อย่างรวดเร็ว โดยอาศัยการสุ่มปรับเปลี่ยน (Mutation) และการแทรกชุดคำสั่ง (Insertion) เข้าไปใน Test Suite
- **เครื่องมือที่เลือกใช้**: EvoSuite Framework (กำหนดค่า Configuration `-Dalgorithm=MIO`)

---

## 3. การออกแบบ Prompt Architecture สำหรับ Generative AI

*(ผู้เขียนหลัก: Member 3)*

### 3.1 เทคนิคการออกแบบ Prompt

- ใช้โครงสร้าง System Prompt + Context Ingestion + Few-Shot Prompting + Chain-of-Thought (CoT)
- กำหนดเงื่อนไขบังคับ (Constraints) เช่น บังคับใช้ JUnit 4 เท่านั้น (ห้ามใช้ JUnit 5 เพื่อความเข้ากันได้กับ Defects4J), บังคับรองรับ JDK 8 และให้ส่งคืนเฉพาะบล็อกรหัส Java เท่านั้น

### 3.2 เปรียบเทียบแนวทางระหว่าง Claude Sonnet 5 และ Gemini 3.8 Flash

- **Claude Sonnet 5**: เน้นการวิเคราะห์ Edge Cases และการเขียน Assertion ที่แม่นยำ
- **Gemini 3.8 Flash**: เน้นความเร็วในการประมวลผลและการใช้ Chain-of-Thought เพื่อแกะ Branch Logic

---

## 4. สภาพแวดล้อมระบบและการวัดผล (Experimental Setup & Metrics)

*(ผู้เขียนหลัก: Member 4)*

### 4.1 สภาพแวดล้อม Defects4J

ใช้ Docker Container (Ubuntu 20.04 + Multi-JDK OpenJDK 8/11 + Defects4J Framework + PICT + EvoSuite) เพื่อให้ทุกคนในทีมทดลองบนสภาพแวดล้อมมาตรฐานเดียวกันและสามารถทำซ้ำได้ (Reproducible)

### 4.2 ดรรชนีชี้วัดประสิทธิภาพ (Evaluation Metrics)

1. **Target Class Line Coverage ($Coverage_{Line}$)**:
   $$Coverage_{Line} = \left(\frac{L_{covered}}{L_{total}}\right) \times 100\%$$
2. **Target Class Branch Coverage ($Coverage_{Branch}$)**:
   $$Coverage_{Branch} = \left(\frac{B_{covered}}{B_{total}}\right) \times 100\%$$
3. **Fault Detection Rate on Evaluated Sample ($FDR$)**:
   $$FDR = \left(\frac{D_{detected}}{D_{total\_sample\_bugs}}\right) \times 100\%$$

### 4.3 ชุดข้อมูลการทดลอง (Evaluated Benchmark Dataset)

งานวิจัยนี้ประเมินผลบนชุดข้อมูล **17 คลาสตัวแทนจาก 17 โปรเจกต์มาตรฐานใน Defects4J (The 17-Project Representative Benchmark)** ครอบคลุมโปรเจกต์หลากหลายประเภท ได้แก่ Mathematical Library (`Math`), Text & Data Parser (`Csv`, `Cli`), Compiler (`Closure`), Data Structures (`Collections`), JSON/XML Serialization (`Gson`, `JacksonCore`, `JacksonDatabind`, `JacksonXml`), DOM Engine (`Jsoup`, `JxPath`), Date/Time (`Time`), Encoding (`Codec`), Graphic (`Chart`), Core Utils (`Lang`), และ Test Framework (`Mockito`) โดยมีไฟล์คอนฟิกกลางควบคุมอยู่ที่ [`target_benchmark/catalog_17_projects.json`](target_benchmark/catalog_17_projects.json)

