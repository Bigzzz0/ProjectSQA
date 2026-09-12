# Prompt Record for ExtendedBufferedReaderClaudeTest

- **Timestamp:** 2026-09-12 09:45:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.apache.commons.csv.ExtendedBufferedReader`
- **Target Project:** `Csv-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: ExtendedBufferedReader.java from Apache Commons CSV. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.apache.commons.csv;` as line 1.
   - Note that `ExtendedBufferedReader` is package-private, so the test class MUST reside in package `org.apache.commons.csv`.
   - Name the test class `ExtendedBufferedReaderClaudeTest` (declared as `public class ExtendedBufferedReaderClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import java.io.*;`
     * `import java.util.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `ExtendedBufferedReader`:
     * `read()` (single-char read, state updates for `lastChar` and `lineCounter`).
     * `read(char[] buf, int offset, int length)` (block read, CRLF/CR/LF line counter logic, buffer bounds).
     * `readLine()` (line reading, stripping terminators, `lastChar` and `lineCounter` state transitions).
     * `readAgain()` (last read character, `UNDEFINED` initial state, `END_OF_STREAM` on EOF).
     * `lookAhead()` (mark/reset peek without consuming character, EOF behavior).
     * `getLineNumber()` (tracking line counts across diverse newline sequences).
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
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

Generate the complete JUnit 4 test class ExtendedBufferedReaderClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
