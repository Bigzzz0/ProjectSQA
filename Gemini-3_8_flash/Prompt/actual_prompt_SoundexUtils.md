# Prompt Record for SoundexUtilsGeminiTest

- **Timestamp:** 2026-09-11 17:20:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.apache.commons.codec.language.SoundexUtils`
- **Target Project:** `Codec-1b` (Defects4J)

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
- Package Declaration: Must declare `package org.apache.commons.codec.language;` as line 1.
- Mandatory Explicit Imports:
  * `import org.apache.commons.codec.EncoderException;`
  * `import org.apache.commons.codec.StringEncoder;`
  * `import java.util.Locale;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `SoundexUtilsGeminiTest` (`public class SoundexUtilsGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Core Functional Logic & String Cleaning** (clean with purely alphabetical strings, strings with symbols/numbers, empty/null).
- **Partition B: Difference & Similarity Calculations** (difference, differenceEncoded with null, matching, and mismatched strings).
- **Partition C: Defect-Targeted Branch Zone (Codec-1 / Locale Independence)** (clean("i") under Turkish locale, count == len branch).
- **Partition D: Exception & Defensive Paths** (difference with custom or failing encoders throwing EncoderException).
- **Partition E: Contract & Structural Integrity** (Static utility methods behavior, null handling).

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
Here is the source code of SoundexUtils.java:

```java
<SOURCE_CODE_OF_SoundexUtils.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J CODEC-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Codec-1 (CODEC-65)
- Triggering Tests:
  * org.apache.commons.codec.language.SoundexTest::testLocaleIndependence
  * org.apache.commons.codec.language.RefinedSoundexTest::testLocaleIndependence
- Failure Message: junit.framework.AssertionFailedError: tr: The character is not mapped: İ
- Defect Scope & Root Cause:
  In `SoundexUtils.java`, lines 52-56:
  ```java
  if (count == len) {
      return str.toUpperCase();
  }
  return new String(chars, 0, count).toUpperCase(java.util.Locale.ENGLISH);
  ```
  When all characters in the string are letters (`count == len`), the method calls `str.toUpperCase()` using the system default Locale instead of `Locale.ENGLISH`!
  In Turkish locale (`new Locale("tr")`), lowercase 'i' is converted to dotted uppercase 'İ' (\u0130), NOT ASCII 'I'.
  This non-ASCII character causes subsequent Soundex mappings to fail.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testCleanLocaleIndependence_Turkish()`) that:
1. Saves the current default locale: `Locale orig = Locale.getDefault();`
2. Sets the default locale to Turkish: `Locale.setDefault(new Locale("tr"));`
3. Inside a `try { ... } finally { Locale.setDefault(orig); }` block:
   - Calls `SoundexUtils.clean("i")` and `SoundexUtils.clean("test")`.
   - Asserts that `SoundexUtils.clean("i")` returns `"I"` (ASCII 0x49) and NOT `"İ"` (U+0130).
   - Also asserts `assertEquals("TEST", SoundexUtils.clean("test"));`
This test MUST fail on the defective version (where clean("i") returns "İ") and pass on the fixed version!

Generate the complete JUnit 4 test class SoundexUtilsGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
