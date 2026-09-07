# 📋 รายละเอียดบั๊กเป้าหมาย: Apache Commons-Lang 1 (Lang-1b)

> **เอกสารจัดทำโดย:** Member 4 (Infrastructure & Data Analysis Lead)  
> **วัตถุประสงค์:** ส่งมอบข้อมูลคลาสเป้าหมายและบั๊กมาตรฐาน (Ground Truth) ให้แก่ Member 1 (IPO), Member 2 (MIO), และ Member 3 (AI Claude & Gemini) นำไปใช้สร้าง Unit Test

---

## 1. ข้อมูลพื้นฐานของโปรเจกต์ (Project Metadata)

* **Repository / Benchmark:** Defects4J Dataset
* **Project Name:** Apache Commons-Lang (`Lang`)
* **Bug ID:** `1` (เวอร์ชัน Buggy: `1b`, เวอร์ชัน Fixed: `1f`)
* **Target Class Name:** `org.apache.commons.lang3.math.NumberUtils`
* **Target Source Path (in repository):** `src/main/java/org/apache/commons/lang3/math/NumberUtils.java`
* **Test Directory Path (in repository):** `src/test/java/org/apache/commons/lang3/math/`
* **Language & Framework:** Java 8 (OpenJDK 8/11), JUnit 4

---

## 2. เมธอดที่มีข้อบกพร่อง (Faulty Method)

เมธอดที่เป็นต้นตอของปัญหาคือ:

```java
public static Number createNumber(final String str) throws NumberFormatException
```

### หน้าที่ของเมธอด:
รับสตริงอินพุต (`str`) แล้วพยายามแปลง (Parse) ให้อยู่ในรูปของชนิดข้อมูลตัวเลขที่เหมาะสมที่สุด เช่น `Integer`, `Long`, `Float`, `Double`, `BigInteger` หรือ `BigDecimal` โดยรองรับทั้ง:
* จำนวนเต็มบวก/ลบ
* ทศนิยม (เช่น `1.23`, `.45`)
* สัญกรณ์วิทยาศาสตร์ (Scientific Notation เช่น `1.2e3`, `-2E-4`)
* เลขฐาน 16 (Hexadecimal เช่น `0x1A`, `-0XFF`, `#1234`)
* Type Specifier Suffix (เช่น `123L`, `45.6f`, `78.9D`)

---

## 3. รายละเอียดข้อบกพร่อง (Root Cause & Bug Behavior)

### พฤติกรรมที่ผิดพลาด (Bug):
ในเวอร์ชัน `1b` เมื่อส่งสตริงเลขฐาน 16 (Hexadecimal) ที่มี Type Suffix หรือมีการใส่เครื่องหมาย เช่น:
* สตริงที่มี Prefix `0x` หรือ `0X` แล้วตามด้วยตัวเลขที่มีเครื่องหมาย เช่น `"-0x12"` หรือ `"0x80000000"`
* หรือสตริงเลขฐาน 16 ที่ลงท้ายด้วย `L` หรือ `l` เช่น `"0x12L"`

เมธอดจะเกิดข้อผิดพลาดในการตรวจสอบเงื่อนไขความยาวสตริงและ Type specifier ทำให้ข้ามการแปลงด้วย `Integer.decode()` หรือ `Long.decode()` แล้วไปเข้าบล็อกแปลงสตริงทั่วไป ส่งผลให้ **โยน `java.lang.NumberFormatException` ออกมาโดยไม่ถูกต้อง** แทนที่จะแปลงเป็น `Long` หรือ `BigInteger` ได้ตามที่ระบุไว้ในสเปก

### การทดสอบดั้งเดิมที่จับบั๊กได้ (Trigger Test - Ground Truth):
* **Class:** `org.apache.commons.lang3.math.NumberUtilsTest`
* **Method:** `testLang747()`
* **ตัวอย่าง Test Case ที่ทริกเกอร์บั๊ก:**
  ```java
  // กรณีนี้ใน 1b จะ throw NumberFormatException (ถือว่ามีบั๊ก)
  // แต่ใน 1f (เวอร์ชันแก้แล้ว) จะ return Long หรือ BigInteger ถูกต้อง
  NumberUtils.createNumber("0x80000000");
  NumberUtils.createNumber("0xFA");
  NumberUtils.createNumber("#1234");
  ```

---

## 4. คำแนะนำสำหรับเพื่อนในแต่ละสายงาน

### 🔹 สำหรับ Member 1 (Algorithm Lead 1 - IPO / Microsoft PICT)
* **เป้าหมาย:** สกัด Input Space ของเมธอด `createNumber(String str)` ออกเป็น Parameters & Values
* **ตัวอย่าง Parameters ใน `model.txt`:**
  * `Prefix`: `None`, `Plus`, `Minus`, `ZeroX`, `MinusZeroX`, `Hash`
  * `ValueType`: `Integer`, `Decimal`, `Scientific`, `HexDigits`, `Alphanumeric`
  * `Suffix`: `None`, `f`, `F`, `d`, `D`, `l`, `L`, `Invalid`
  * `Length`: `Empty`, `SingleChar`, `Normal`, `ExceedLong`
* นำ Matrix Combinations ที่ได้จาก PICT ไปสร้างเป็นไฟล์ `NumberUtilsIPOTest.java`

### 🔹 สำหรับ Member 2 (Algorithm Lead 2 - MIO / EvoSuite)
* **เป้าหมาย:** กำหนด Target Class ในการสั่งรัน EvoSuite:
  ```bash
  -class org.apache.commons.lang3.math.NumberUtils
  -Dalgorithm=MIO
  -Dsearch_budget=30, 60, 120
  ```
* ดึงไฟล์เทสที่ได้มาบันทึกเป็น `NumberUtils_ESTest.java` พร้อมบันทึกผล Coverage ในรายงาน

### 🔹 สำหรับ Member 3 (AI Prompt Engineer - Claude & Gemini)
* **เป้าหมาย:** นำซอร์สโค้ดของ `NumberUtils.java` ไปใส่ใน Master Prompt
* **ชื่อคลาสสำหรับ Test Case ที่ต้องการ:**
  * Claude: `NumberUtilsClaudeTest.java` (วางที่ `Claude-sonnet_5/TestCode/`)
  * Gemini: `NumberUtilsGeminiTest.java` (วางที่ `Gemini-3_8_flash/TestCode/`)
* **ข้อกำหนดทางเทคนิค:**
  * ต้องประกาศ `package org.apache.commons.lang3.math;`
  * ใช้ JUnit 4 (`import org.junit.Test;`, `import static org.junit.Assert.*;`)
  * สั่งให้ AI เน้นเขียนกรณีทดสอบคลุม Boundary, Null, และ Hexadecimal formats
