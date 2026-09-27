# 🤖 Master Prompt AI: คลังพร้อมต์ผู้เชี่ยวชาญสำหรับศึกษาและวิเคราะห์ MIO Algorithm
## (Comprehensive AI Prompts for Studying Many Independent Objective Algorithm in SBST)

**สำหรับ:** Member 2 (นายแทนคุณ พันธ์นิกุล - Algorithm Lead: MIO / EvoSuite Specialist)  
**โครงการ:** CP353201 Software Quality Assurance (KKU CS)  
**วัตถุประสงค์:** เอกสารนี้รวบรวม **Master Prompt ระดับ Professional** สำหรับคัดลอก (Copy & Paste) ไปถาม AI (เช่น ChatGPT, Claude, Gemini, DeepSeek) เพื่อศึกษาเจาะลึกอัลกอริทึม **MIO (Many Independent Objective)** ในแง่มุมต่างๆ ทั้งทฤษฎีเชิงลึก, การเปรียบเทียบกับอัลกอริทึมอื่น, การแปลผลการทดลอง, และการซ้อมตอบคำถามกรรมการสอบ

---

## 📌 สารบัญชุดพร้อมต์ (Prompt Directory)

1. [Prompt 0: Master System Context (พร้อมต์ตั้งต้นกำหนดบทบาท AI)](#-prompt-0-master-system-context-พร้อมต์ตั้งต้นกำหนดบทบาท-ai)
2. [Prompt 1: เจาะลึกทฤษฎีและกลไกคณิตศาสตร์ของ MIO (Mathematical Mechanics & Theory)](#-prompt-1-เจาะลึกทฤษฎีและกลไกคณิตศาสตร์ของ-mio)
3. [Prompt 2: มวยถูกคู่ - เปรียบเทียบ MIO vs. WTS vs. MOSA vs. DynaMOSA vs. LLM](#-prompt-2-มวยถูกคู่---เปรียบเทียบ-mio-vs-wts-vs-mosa-vs-dynamosa-vs-llm)
4. [Prompt 3: จำลองการทำงานของ MIO บนโค้ด Java จริงทีละสเต็ป (Step-by-Step Simulation)](#-prompt-3-จำลองการทำงานของ-mio-บนโค้ด-java-จริงทีละสเต็ป)
5. [Prompt 4: ช่วยวิเคราะห์และอภิปรายผลการทดลอง Search Budget 30s vs 60s vs 120s](#-prompt-4-ช่วยวิเคราะห์และอภิปรายผลการทดลอง-search-budget-30s-vs-60s-vs-120s)
6. [Prompt 5: ซ้อมตอบคำถามกรรมการสอบและการดีเฟนด์โครงงาน (Oral Defense & Q&A Drill)](#-prompt-5-ซ้อมตอบคำถามกรรมการสอบและการดีเฟนด์โครงงาน)
7. [Prompt 6: ช่วยเขียนเนื้อหาเชิงวิชาการลงเล่มรายงาน (Academic Report Drafting)](#-prompt-6-ช่วยเขียนเนื้อหาเชิงวิชาการลงเล่มรายงาน)

---

## 🌟 Prompt 0: Master System Context (พร้อมต์ตั้งต้นกำหนดบทบาท AI)
> [!TIP]
> **วิธีใช้:** คัดลอกบล็อกนี้ส่งให้ AI เป็นข้อความแรกสุด เพื่อปูพื้นหลังให้ AI เข้าใจบริบทโครงงานทั้งหมดอย่างแม่นยำ 100%

```markdown
คุณคือผู้เชี่ยวชาญระดับโลกด้าน Search-Based Software Testing (SBST), Evolutionary Computation, Metaheuristic Optimization และการสร้างชุดทดสอบอัตโนมัติด้วย EvoSuite บนชุดข้อมูลมาตรฐาน Defects4J Benchmark

บริบทของฉัน:
- ฉันเป็นนักศึกษาภาควิชาวิทยาการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น (วิชา CP353201 Software Quality Assurance)
- ฉันรับผิดชอบขั้นตอนวิธี MIO (Many Independent Objective) algorithm ที่คิดค้นโดย Dr. Andrea Arcuri (2017/2018) ซึ่งทำงานอยู่ภายใน EvoSuite Framework (เวอร์ชัน 1.0.6)
- ฉันได้ทำการทดลองสร้างชุดทดสอบบน Defects4J Benchmark ทั้งหมด 17 โปรเจกต์ (854 บั๊ก / 1,073 คลาสเป้าหมาย) 
- มีการทดลองตามเกณฑ์ 1.7: วัด Search Budget 3 ระดับ (30s, 60s, 120s) ร่วมกับ Random Seeds (101, 102, 103) รวม 9 รันต่อคลาส
- ค่าเฉลี่ย Coverage ภาพรวมที่ได้: 30s ได้ 65.73%, 60s ได้ 68.73%, และ 120s ได้ 70.82%

กติกาและข้อกำหนดในการตอบของคุณ:
1. ใช้ชื่อเต็มที่ถูกต้องคือ "Many Independent Objective (MIO)" ห้ามเรียกผิดเป็น Mutation Insertion Optimization หรือ Many-Objective Sorting Algorithm เด็ดขาด
2. ตอบด้วยหลักการวิชาการที่แม่นยำ ลึกซึ้ง มีการอ้างอิงสูตรคณิตศาสตร์ หรือหลักการของ Arcuri (2018) อย่างชัดเจน
3. ใช้ภาษาไทยที่สละสลวย เชิงวิชาการวิศวกรรมซอฟต์แวร์ ควบคู่กับ Technical Terms ภาษาอังกฤษที่ถูกต้อง
4. หากเข้าใจบทบาทและบริบทนี้แล้ว โปรดตอบรับสั้นๆ ว่า "พร้อมให้คำปรึกษาเชิงลึกด้าน MIO Algorithm แล้วครับ"
```

---

## 🧬 Prompt 1: เจาะลึกทฤษฎีและกลไกคณิตศาสตร์ของ MIO
> **เป้าหมาย:** ทำความเข้าใจว่าเบื้องหลังโค้ด EvoSuite MIO ทำงานอย่างไรในระดับอัลกอริทึม

```markdown
ช่วยอธิบายการทำงานเชิงลึกของขั้นตอนวิธี Many Independent Objective (MIO) algorithm ของ Andrea Arcuri (2018) อย่างละเอียดในประเด็นต่อไปนี้:

1. Target-Centric Archive Architecture:
   - ทำไม MIO ถึงไม่ใช้ Global Population รวมเหมือน Genetic Algorithm ทั่วไป?
   - โครงสร้างของ Archive แต่ละ Bucket ถูกออกแบบอย่างไร? มีความจุ (Capacity) เท่าไร?
   - กฎการแทนที่ (Replacement Policy) ใน Archive ใช้เงื่อนไขใดบ้างในการตัดสินใจว่าเทสตัวใหม่จะเข้ามาแทนที่เทสตัวเก่า (ทั้งในมิติของ Fitness และ Code Length/Bloat control)?

2. No Crossover & Pure Mutation/Insertion:
   - เหตุผลทางวิชาการและวิศวกรรมซอฟต์แวร์ว่าทำไม Arcuri ถึงพิสูจน์ว่า "Crossover ไม่มีประโยชน์และให้ผลลบล้าง (Destructive) ในการสร้าง Unit Test"?
   - Mutation และ Statement Insertion ใน MIO ทำงานในระดับคำสั่ง (Statement Level) และตัวแปร (Primitive/String Level) อย่างไร?

3. Adaptive Dynamic Sampling (Exploration vs. Exploitation):
   - กลไกการสุ่ม P_random ทำงานอย่างไร? 
   - อัลกอริทึมปรับสมดุลระหว่างการสร้างเทสใหม่จากศูนย์ (Random Exploration) กับการหยิบเทสท็อปจาก Archive มากลายพันธุ์ต่อยอด (Local Exploitation) อย่างไรตลอด Search Budget?

4. Fitness Function & Distance Calculation:
   - อธิบายสูตร Fitness(t, g) = Approach Level + Normalized Branch Distance
   - ยกตัวอย่างการคำนวณระยะห่าง d และการ Normalize ด้วย d / (d + 1) ในกรณีเงื่อนไขที่เป็นตัวเลข, Boolean, และ String Comparison
```

---

## 🥊 Prompt 2: มวยถูกคู่ - เปรียบเทียบ MIO vs. WTS vs. MOSA vs. DynaMOSA vs. LLM
> **เป้าหมาย:** สรุปตารางเปรียบเทียบความแตกต่างเพื่อนำไปใส่ในบทที่ 2 ของเล่มรายงาน

```markdown
ช่วยทำตารางเปรียบเทียบเชิงวิเคราะห์ (Comparative Analysis Matrix) ระหว่างขั้นตอนวิธีสร้างชุดทดสอบ 5 ตัวนี้:
1. Whole Test Suite (WTS) - Fraser & Arcuri (2012)
2. Many-Objective Sorting Algorithm (MOSA) - Panichella et al. (2015)
3. Many Independent Objective (MIO) - Arcuri (2018)
4. Dynamic Many-Objective Sorting Algorithm (DynaMOSA) - Panichella et al. (2017)
5. LLM-Based Test Generation (เช่น Gemini / DeepSeek / Claude)

โปรดเปรียบเทียบในมิติต่อไปนี้:
- Representation (โครงสร้าง Chromosome / Test Suite)
- Genetic Operators (ใช้ Crossover หรือไม่? ใช้ Mutation แบบไหน?)
- การจัดการเป้าหมาย (Objective / Goal Handling)
- ความเร็วในการประมวลผลและการใช้หน่วยความจำ (Computational & Memory Overhead) เมื่อมีเป้าหมายมากกว่า 1,000 กิ่ง (Scalability in Large Codebases)
- การจัดการปัญหา Code Bloat (Test Suite Minimization)
- จุดเด่นที่สุด (Key Strengths)
- ข้อจำกัดที่เห็นได้ชัด (Key Limitations)

พร้อมสรุปว่า "ทำไม MIO จึงเหมาะสมเป็นพิเศษกับคลาสขนาดใหญ่ใน Defects4J เมื่อเทียบกับ WTS และ MOSA?"
```

---

## 💻 Prompt 3: จำลองการทำงานของ MIO บนโค้ด Java จริงทีละสเต็ป
> **เป้าหมาย:** ให้ AI จำลองภาพ (Trace Simulation) ว่าถ้ามีฟังก์ชัน Java ตัวหนึ่ง MIO จะค้นหาอย่างไร

```markdown
สมมติว่าเรามีคลาสภาษา Java ที่มีเมธอดที่ซับซ้อนดังนี้:

```java
public class PaymentService {
    public boolean processDiscount(String couponCode, int cartTotal, boolean isVip) {
        if (cartTotal <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (isVip && cartTotal >= 1000) {
            if ("SUPER_VIP_2026".equals(couponCode)) {
                return applySpecialRate(cartTotal, 0.50);
            }
        }
        return false;
    }
}
```

ช่วยจำลองสถานการณ์ทีละรอบ (Step-by-Step Execution Trace) ว่า MIO Algorithm จะทำงานอย่างไรตั้งแต่ต้นจนจบ:
1. การระบุ Target Goals ทั้งหมดในเมธอดนี้ (LINE และ BRANCH Goals มีอะไรบ้าง)
2. รอบที่ 1: การสุ่มสร้างเทสเคสตัวแรก (Random Test Case) และการคำนวณ Approach Level + Branch Distance
3. รอบที่ 2-4: การทำ Mutation และ Statement Insertion เพื่อค่อยๆ ปรับค่าตัวแปรให้เจาะทะลุเงื่อนไข `cartTotal >= 1000`
4. รอบที่ 5: การกลายพันธุ์ String `couponCode` จนกลายเป็น `"SUPER_VIP_2026"`
5. การอัปเดต Archive Bucket ในแต่ละรอบ
6. ช่วง Post-Processing: MIO ตัดแต่ง Test Suite (Minimization) และเติม Assertions อย่างไรก่อนส่งออกเป็นไฟล์ JUnit _ESTest.java
```

---

## 📈 Prompt 4: ช่วยวิเคราะห์และอภิปรายผลการทดลอง Search Budget 30s vs 60s vs 120s
> **เป้าหมาย:** นำตัวเลขจริงจากการทดลองไปให้อาจารย์/AI ช่วยวิเคราะห์เป็นภาษาทางการแพทย์/วิศวกรรม

```markdown
ฉันมีผลการทดลองจริงของการรัน MIO บน Defects4J Benchmark จำนวน 1,023 คลาสเป้าหมาย (รวม 3,027 รัน) ดังนี้:

- Budget 30 วินาที (Baseline):
  * Mean Combined Coverage: 65.73% ± 31.96%
  * Execution Time เฉลี่ย: 68.29 วินาที
- Budget 60 วินาที:
  * Mean Combined Coverage: 68.73% ± 31.29% (เพิ่มขึ้น +3.00%)
  * การทดสอบทางสถิติ Mann-Whitney U Test: U = 488,691.5, p = 0.0217 (p < 0.05 มีนัยสำคัญทางสถิติ)
  * Execution Time เฉลี่ย: 90.62 วินาที
- Budget 120 วินาที:
  * Mean Combined Coverage: 70.82% ± 30.40% (เพิ่มขึ้น +2.09% จาก 60s)
  * การทดสอบทางสถิติ Mann-Whitney U Test: U = 481,796.5, p = 0.1202 (p > 0.05 ไม่มีนัยสำคัญทางสถิติ)
  * Execution Time เฉลี่ย: 194.41 วินาที

ช่วยเขียน "บทอภิปรายผลการทดลอง (Empirical Discussion)" ในระดับวิชาการคุณภาพสูงสำหรับใส่ในเล่มรายงาน โดยครอบคลุม:
1. การอธิบายพฤติกรรมของ MIO ในช่วง 30s ไป 60s ว่าทำไมถึงมีนัยสำคัญทางสถิติ (การเปลี่ยนผ่านจาก Exploration สู่ Exploitation)
2. การอธิบายปรากฏการณ์ "กฎแห่งการลดลงของผลตอบแทนส่วนเพิ่ม (Law of Diminishing Returns)" ในช่วง 60s ไป 120s ว่าทำไมให้เวลาเพิ่มขึ้น 1 เท่าตัวแต่ได้ Coverage เพิ่มเพียง 2% และไม่มีนัยสำคัญทางสถิติ
3. การวิเคราะห์สาเหตุทางวิศวกรรมซอฟต์แวร์ว่า "ทำไมโค้ดอีกประมาณ 29-30% ถึงไม่สามารถ Cover ได้แม้จะให้เวลาถึง 120 วินาที?" (เช่น Dead Code, Private Reflection, Complex External Dependencies, Environmental Limitations)
4. สรุปข้อเสนอแนะเชิงปฏิบัติ (Practical Recommendations) สำหรับนักพัฒนาซอฟต์แวร์ว่าควรเลือกใช้ Budget เท่าไรจึงคุ้มค่าที่สุดระหว่างเวลาและคุณภาพ
```

---

## 🎯 Prompt 5: ซ้อมตอบคำถามกรรมการสอบและการดีเฟนด์โครงงาน
> **เป้าหมาย:** สวมบทเป็นกรรมการสอบสายโหด ยิงคำถามยากๆ เพื่อให้เราฝึกตอบและเตรียมตัว

```markdown
ช่วยสวมบทบาทเป็น "อาจารย์กรรมการสอบวิชา Software Quality Assurance ที่มีความเชี่ยวชาญสูงด้าน Automated Testing และ Search-Based Software Engineering"

โปรดยิงคำถามไล่ต้อนฉันทีละข้อ (พร้อมแนวทางการตอบที่ถูกต้องสมบูรณ์แบบ) ใน 6 ประเด็นท็อปฮิตต่อไปนี้:

1. "ทำไมในตารางของคุณ ค่าเฉลี่ย Line Coverage กับ Branch Coverage ถึงได้ตัวเลขเท่ากันเป๊ะทั้ง Mean และ SD? คุณคำนวณผิดหรือปลอมตัวเลขมาหรือเปล่า?"
2. "MIO ต่างจาก Genetic Algorithm มาตรฐาน (Classic GA) หรือ Whole Test Suite อย่างไรในทางโครงสร้างคณิตศาสตร์?"
3. "ทำไม MIO ถึงตัด Crossover ทิ้ง? ในวิชา AI สอนว่า Crossover เป็นหัวใจสำคัญของการสร้างความหลากหลาย (Diversity) ของประชากร การตัดทิ้งไม่ทำให้อัลกอริทึมติด Local Optima หรือ?"
4. "การที่ชุดทดสอบของคุณได้ Coverage สูงถึง 70% หรือ 90% รับประกันได้ไหมว่ามันจะตรวจจับบั๊กใน Defects4J ได้จริง? อะไรคือความต่างระหว่าง Coverage กับ Fault Detection Rate (FDR)?"
5. "ใน Defects4J มี 20 บั๊กที่คุณรันไม่ผ่าน (เช่น Mockito 15 บั๊ก, Gson 2 บั๊ก, Math 2 บั๊ก) นี่คือความล้มเหลวของอัลกอริทึม MIO ใช่หรือไม่? คุณจะอธิบายเรื่องนี้อย่างไรไม่ให้ถูกหักคะแนน?"
6. "ถ้าเทียบ MIO กับการใช้ Generative AI (เช่น DeepSeek หรือ Gemini) ในการเขียน Test Case คุณคิดว่า SBST ยังมีที่ยืนอยู่ในโลกยุคปัจจุบันหรือไม่? อะไรคือข้อได้เปรียบที่ AI สู้ MIO ไม่ได้?"
```

---

## 📄 Prompt 6: ช่วยเขียนเนื้อหาเชิงวิชาการลงเล่มรายงาน
> **เป้าหมาย:** สั่งให้ AI ร่างข้อความวิชาการภาษาไทยพร้อมสูตรคณิตศาสตร์สำหรับคัดลอกลง Word / LaTeX

```markdown
ช่วยร่างเนื้อหาสำหรับ "บทที่ 2.2: ทฤษฎีและขั้นตอนวิธี Many Independent Objective (MIO)" สำหรับใส่ในเล่มรายงานโครงงานวิจัย โดยมีหัวข้อย่อยและข้อกำหนดดังนี้:

โครงสร้างเนื้อหาที่ต้องการ:
1. บทนำและที่มาของปัญหาในงานวิจัย Search-Based Software Testing ยุคคลาสสิก
2. นิยามและสถาปัตยกรรมของ Many Independent Objective (MIO) Architecture
3. การจำลองกลไกคณิตศาสตร์ (Mathematical Formulations):
   - ฟังก์ชันความเหมาะสม (Fitness Function): Approach Level และ Normalized Branch Distance พร้อมสมการคณิตศาสตร์แบบ LaTeX
   - การคัดสรรประชากรและการจัดการ Archive Bucket
   - กลยุทธ์การสุ่มแบบปรับตัว (Adaptive Sampling Strategy)
4. การสังเคราะห์ Test Oracle และกระบวนการลดทอนชุดทดสอบ (Minimization & Assertion Generation)
5. จุดเด่นและข้อจำกัดเชิงเปรียบเทียบของขั้นตอนวิธี MIO

ข้อกำหนดการเขียน:
- ใช้ภาษาไทยเชิงวิชาการระดับงานวิจัยวิศวกรรมศาสตร์/วิทยาการคอมพิวเตอร์
- ทับศัพท์คำศัพท์เฉพาะทางอย่างถูกต้องและมีภาษาอังกฤษกำกับในครั้งแรกที่กล่าวถึง
- แทรกสูตรคณิตศาสตร์ด้วยรูปแบบ LaTeX ให้สวยงาม พร้อมอธิบายความหมายของตัวแปรทุกตัว
- เขียนให้มีความยาวและเนื้อหาเข้มข้น ลึกซึ้ง เหมาะสำหรับรายงานระดับปริญญาตรีชั้นปีที่ 3-4
```

---

## 💡 วิธีการใช้งาน Master Prompt ให้ได้ผลลัพธ์ดีที่สุด (Best Practices)

1. **ส่ง Prompt 0 ก่อนเสมอ:** ไม่ว่าจะคุยกับโมเดลตัวไหน (Claude 3.7 / ChatGPT 4o / Gemini 2.5 / DeepSeek V3) ให้ส่ง **Prompt 0** ไปเปิดหัวก่อน เพื่อล็อกกรอบการคิดของ AI ไม่ให้หลงทางหรือตอบแบบกว้างเกินไป
2. **แนบไฟล์โค้ดจริงร่วมด้วย:** เมื่อใช้ **Prompt 3** หรือ **Prompt 5** คุณสามารถแนบไฟล์ เช่น [NumberUtils_ESTest.java](file:///e:/Coding/ProjectSQA/MIO_Algorithm/TestCode/Lang_1b/org/apache/commons/lang3/math/NumberUtils_ESTest.java) หรือไฟล์ [MIO_FAILURE_ANALYSIS_REPORT.md](./MIO_FAILURE_ANALYSIS_REPORT.md) เข้าไปในแชทด้วย เพื่อให้ AI ตอบโดยอิงจากหลักฐานจริงในโปรเจกต์ของคุณ
3. **สอบถามแบบเจาะจงทีละข้อ:** แทนที่จะโยนพร้อมต์ทั้งหมดไปพร้อมกัน ให้เลือกใช้ทีละ Prompt ตามหัวข้อที่คุณกำลังเตรียมงาน เช่น กำลังเขียนรายงานให้อ่าน Prompt 6 หรือกำลังจะซ้อมพรีเซนต์ให้ใช้ Prompt 5
