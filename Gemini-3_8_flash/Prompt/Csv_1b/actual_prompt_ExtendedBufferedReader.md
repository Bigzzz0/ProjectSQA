# Prompt Record for ExtendedBufferedReaderGeminiTest

- **Timestamp:** 2026-09-12 09:45:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.apache.commons.csv.ExtendedBufferedReader`
- **Target Project:** `Csv-1b` (Defects4J)

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
- Package Declaration: Must declare `package org.apache.commons.csv;` as line 1.
- Note that `ExtendedBufferedReader` is package-private, so the test class MUST be in package `org.apache.commons.csv`.
- Mandatory Explicit Imports:
  * `import java.io.*;`
  * `import java.util.*;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `ExtendedBufferedReaderGeminiTest` (`public class ExtendedBufferedReaderGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Core Character & Lookahead Operations** (`read()`, `readAgain()`, `lookAhead()`, `UNDEFINED`, `END_OF_STREAM`).
- **Partition B: Block Buffer Operations** (`read(char[] buf, int offset, int length)` with offset, length zero, buffer boundaries).
- **Partition C: Line Counting & Newline Semantics** (`readLine()`, `getLineNumber()` across CRLF `\r\n`, LF `\n`, CR `\r`).
- **Partition D: Defect-Targeted Branch Zone (Csv-1 / CSV-75)** (`read()` tracking of standalone carriage return `\r` and CRLF sequences).
- **Partition E: Edge Cases & Stream Exhaustion** (Empty stream, single-character stream, repeated EOF reads, lookahead at EOF).

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
Here is the source code of ExtendedBufferedReader.java:

```java
<SOURCE_CODE_OF_ExtendedBufferedReader.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J CSV-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Csv-1 (JIRA CSV-75)
- Triggering Test: org.apache.commons.csv.CSVParserTest::testGetLineNumberWithCR
- Failure Message: junit.framework.AssertionFailedError: expected:<1> but was:<0>
- Defect Scope & Root Cause:
  In `ExtendedBufferedReader.java`, the single-character `read()` method:
  ```java
  @Override
  public int read() throws IOException {
      int current = super.read();
      if (current == '\n') {
          lineCounter++;
      }
      lastChar = current;
      return lastChar;
  }
  ```
  ONLY increments `lineCounter` when `current == '\n'`. It completely ignores carriage return `\r`!
  In contrast, `read(char[] buf, int offset, int length)` correctly accounts for `\r`, `\n`, and `\r\n`.
  When a stream using standalone carriage returns (CR / `\r`, typical of classic Mac OS line breaks) or CR in CRLF is consumed using `read()`, `lineCounter` is never incremented for `\r`.
  Consequently, `getLineNumber()` returns 0 instead of 1.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testRead_IncrementsLineCounterOnCarriageReturn_CSV75()`) that:
1. Instantiates an `ExtendedBufferedReader` wrapping a `StringReader` containing standalone carriage returns, e.g. `"a\rb\rc"`.
2. Reads the stream character-by-character using `read()`.
3. Verifies that after reading past each `\r`, `getLineNumber()` increments accordingly (e.g., after `"a\r"`, `getLineNumber()` must equal 1; after `"b\r"`, it must equal 2).
4. Also test `\r\n` (CRLF) sequence using `read()` to verify that `\r\n` increments the line counter exactly once.
This test MUST fail on the defective version (where read() returns 0 for standalone CR) and pass on the fixed version!

Generate the complete JUnit 4 test class ExtendedBufferedReaderGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
