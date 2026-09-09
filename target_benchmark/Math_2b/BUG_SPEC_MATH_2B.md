# 📋 รายละเอียดบั๊กเป้าหมาย: Apache Commons-Math 2 (Math-2b)

> **เอกสารจัดทำโดย:** Member 4 (Infrastructure & Data Analysis Lead)  
> **วัตถุประสงค์:** ส่งมอบข้อมูลคลาสเป้าหมายและบั๊กมาตรฐาน (Ground Truth) ให้แก่ Member 1 (IPO), Member 2 (MIO), และ Member 3 (AI Claude & Gemini) นำไปใช้สร้าง Unit Test

---

## 1. ข้อมูลพื้นฐานของโปรเจกต์ (Project Metadata)

* **Repository / Benchmark:** Defects4J Dataset
* **Project Name:** Apache Commons-Math (`Math`)
* **Bug ID:** `2` (เวอร์ชัน Buggy: `2b`, เวอร์ชัน Fixed: `2f`)
* **Target Class Name:** `org.apache.commons.math3.distribution.HypergeometricDistribution`
* **Target Source Path:** `src/main/java/org/apache/commons/math3/distribution/HypergeometricDistribution.java`
* **Test Directory Path:** `src/test/java/org/apache/commons/math3/distribution/`
* **Language & Framework:** Java 8, JUnit 4

---

## 2. เมธอดที่มีข้อบกพร่อง (Faulty Method)

```java
public double getNumericalMean() {
    return (double) (getSampleSize() * getNumberOfSuccesses()) / (double) getPopulationSize();
}
```

### หน้าที่ของเมธอด:
คำนวณค่าเฉลี่ยทางคณิตศาสตร์ (Mathematical Mean หรือ Expected Value: $E[X]$) ของการแจกแจงแบบไฮเปอร์จีโอเมตริก โดยใช้สูตร:
$$\mu = \frac{n \times m}{N}$$
โดยที่:
* $N$ = ขนาดประชากร (`populationSize`)
* $m$ = จำนวนความสำเร็จในประชากร (`numberOfSuccesses`)
* $n$ = ขนาดตัวอย่างที่สุ่ม (`sampleSize`)

---

## 3. รายละเอียดข้อบกพร่อง (Root Cause & Bug Behavior)

### พฤติกรรมที่ผิดพลาด (Bug):
ในการคำนวณค่าเฉลี่ย `getSampleSize() * getNumberOfSuccesses()`:
* ทั้งสองตัวแปรมีชนิดข้อมูลเป็น `int` (32-bit signed integer ค่าสูงสุด $2^{31}-1 \approx 2.14 \times 10^9$)
* เมื่อนำมาคูณกัน **ก่อนที่จะถูกแปลง (cast) เป็น `double`** หากผลคูณเกินค่า $2,147,483,647$ จะเกิดภาวะ **Integer Overflow** ทำให้ค่าผลคูณกลายเป็น **ค่าติดลบ**
* ส่งผลให้ `getNumericalMean()` คืนค่าติดลบออกมา ทั้งที่ตามหลักความน่าจะเป็น ค่าเฉลี่ยของการแจกแจงแบบนี้จะต้องมากกว่าหรือเท่ากับศูนย์เสมอ ($\mu \ge 0$)
* เมื่อเมธอด `sample()` นำค่าเฉลี่ยนี้ไปคำนวณช่วงการสุ่มตัวเลข จะทำให้ได้ค่าตัวอย่างติดลบ ส่งผลให้ Assertions `assertTrue(sample >= 0)` ล้มเหลว

### การแก้ไขในเวอร์ชัน Fixed (2f):
```diff
- return (double) (getSampleSize() * getNumberOfSuccesses()) / (double) getPopulationSize();
+ return (double) ((long) getSampleSize() * (long) getNumberOfSuccesses()) / (double) getPopulationSize();
```
*(ทำการแปลงเป็น `long` 64-bit ก่อนคูณ เพื่อป้องกัน Overflow)*

---

## 4. การทดสอบดั้งเดิมที่จับบั๊กได้ (Trigger Test - Ground Truth)

* **Test Class:** `org.apache.commons.math3.distribution.HypergeometricDistributionTest`
* **Test Method:** `testMath1021()`
* **โค้ดทดสอบ Ground Truth:**
```java
@Test
public void testMath1021() {
    final int populationSize = 1437651;
    final int numberOfSuccesses = 28975;
    final int sampleSize = 76182;

    final HypergeometricDistribution dist = new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);

    for (int i = 0; i < 100; ++i) {
        final int sample = dist.sample();
        Assert.assertTrue("sample=" + sample, 0 <= sample);
        Assert.assertTrue("sample=" + sample, sample <= numberOfSuccesses);
    }
}
```
* **ค่าพารามิเตอร์ที่กระตุ้นบั๊ก:**
  * $N = 1,437,651$
  * $m = 28,975$
  * $n = 76,182$
  * ผลคูณ $m \times n = 28,975 \times 76,182 = 2,207,373,450$ ($> 2^{31}-1$ ทำให้เกิด Overflow ติดลบ!)

---

## 5. แนวทางการสร้าง Unit Test สำหรับแต่ละวิธี

1. **Member 1 (IPO):** สร้าง Parameter Model ที่มีค่าตัวเลขขอบเขตใหญ่ (Large Boundary Values) สำหรับ $(N, m, n)$ เช่น ค่าใกล้เคียง $10^5, 10^6$ เพื่อให้เกิด Pairwise Combinations ที่ผลคูณเกิน $2^{31}-1$
2. **Member 2 (MIO / EvoSuite):** กำหนด `-class org.apache.commons.math3.distribution.HypergeometricDistribution` ให้ EvoSuite สำรวจ Branch ของ `getNumericalMean()` และ `sample()`
3. **Member 3 (AI Claude & Gemini):** ใส่เงื่อนไขใน Prompt ให้ AI สร้าง Test Case สำหรับ Extreme Large Value Boundary และ Assert ค่า $sample \ge 0$
