# Prompt Record for UnsupportedDurationFieldGeminiTest

- **Timestamp:** 2026-09-13 13:50:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.joda.time.field.UnsupportedDurationField`
- **Target Project:** `Time-1b` (Defects4J)

---

## System Prompt
```text
You are a Principal Software Quality Assurance (SQA) Engineer and Test Automation Specialist.
Your mission is to perform advanced White-Box Testing on the target Java class from the Defects4J benchmark to generate a production-grade, fault-revealing JUnit 4 test suite.

---

### 🎯 Core Objectives:
1. Maximize **Line Coverage** and **Branch Coverage (Decision/Condition Coverage)** on the target class logic.
2. Expose latent defects, boundary regressions, and state-handling flaws.
3. Ensure **100% deterministic, zero-flakiness, and zero-compilation-error** execution on Java 8 / Defects4J.

---

### 🛠️ Engineering Guidelines & Rules:

#### 1. Imports & Environment Hygiene
- Target Environment: Strictly **Java 8** and **JUnit 4**.
- Package Declaration: Must declare `package org.joda.time.field;` as line 1.
- Mandatory Explicit Imports:
  * `import org.joda.time.*;`
  * `import org.joda.time.chrono.*;`
  * `import org.joda.time.field.UnsupportedDurationField;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `UnsupportedDurationFieldGeminiTest` (`public class UnsupportedDurationFieldGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Factory & Invariant Properties** (`getInstance(DurationFieldType)`, `getType()`, `getName()`, `isSupported() == false`, `isPrecise() == true`, `getUnitMillis() == 0`).
- **Partition B: Unsupported Operation Exceptions** (Testing all `getValue`, `getMillis`, `add`, `getDifference` variants throw `UnsupportedOperationException`).
- **Partition C: Comparable Contract & Defect Analysis (Time-1 / Issue 93)** (`compareTo(DurationField)` with supported vs unsupported fields, checking anti-symmetry).
- **Partition D: Object Identity & Hash Integrity** (`equals(Object)` covering same instance, null, different type, different name; `hashCode()`, `toString()`).

#### 4. In-Code Reasoning (Mental Sandbox)
Before writing the Java test methods, include an in-line Javadoc/block comment at the top of the class summarizing your:
`/* [Branch & Defect Analysis Matrix] */` listing the specific decision branches and boundary conditions being targeted.

---

### 🚫 ABSOLUTE OUTPUT CONSTRAINT:
- Output MUST contain **ONLY compilable Java code** within a single ```java ... ``` block.
- DO NOT output any introductory text, markdown explanations outside the code block, notes, or conversational closings.
```

---

## User Prompt

```text
Here is the source code of UnsupportedDurationField.java:

```java
<SOURCE_CODE_OF_UnsupportedDurationField.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J TIME-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Time-1 (Issue 93)
- Triggering Test: org.joda.time.TestPartial_Constructors::testConstructorEx7_TypeArray_intArray
- Defect Scope & Root Cause:
  In `UnsupportedDurationField.java`, the `compareTo(DurationField durationField)` method is implemented as:
  ```java
  public int compareTo(DurationField durationField) {
      if (durationField.isSupported()) {
          return 1;
      }
      return 0;
  }
  ```
  When an unsupported duration field (e.g. `eras()`) is compared against a supported duration field (e.g. `years()`, `days()`), this method returns `1` (indicating that the unsupported field is considered "larger" than supported fields).
  However, in `PreciseDurationField` and `BaseDurationField`, comparing a supported field against an unsupported field also returns `1`!
  This creates a direct violation of the `Comparable` contract:
  `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))` (both return 1!).
  In the fixed version, comparing an unsupported duration field against a supported duration field returns `0` (or does not consider it larger).

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testCompareToContractWithSupportedDurationField_Time1()`) that:
1. Obtains an `UnsupportedDurationField` (e.g. for `DurationFieldType.eras()`):
   ```java
   DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.eras());
   DurationField supported = ISOChronology.getInstanceUTC().days();
   ```
2. Compares `unsupported.compareTo(supported)` and `supported.compareTo(unsupported)`.
3. Verifies that the `Comparable` anti-symmetry invariant is not violated (i.e. both comparisons cannot simultaneously return positive values > 0):
   ```java
   int c1 = unsupported.compareTo(supported);
   int c2 = supported.compareTo(unsupported);
   assertFalse("Comparable contract broken: both cannot be > 0", c1 > 0 && c2 > 0);
   ```
This test targets the comparison flaw in Time-1.

Generate the complete JUnit 4 test class UnsupportedDurationFieldGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
