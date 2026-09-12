# Prompt Record for TextBufferClaudeTest

- **Timestamp:** 2026-09-12 09:55:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `com.fasterxml.jackson.core.util.TextBuffer`
- **Target Project:** `JacksonCore-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: TextBuffer.java from Jackson Core. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package com.fasterxml.jackson.core.util;` as line 1.
   - Name the test class `TextBufferClaudeTest` (declared as `public class TextBufferClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import java.math.BigDecimal;`
     * `import java.util.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `TextBuffer`:
     * Buffer creation: `new TextBuffer(new BufferRecycler())` and `new TextBuffer(null)`.
     * `resetWithEmpty()`, `resetWithShared(char[], int, int)`, `resetWithCopy(char[], int, int)`.
     * `append(char)`, `append(char[], int, int)`, `append(String, int, int)`.
     * Buffer expansion across multiple segments (`_segments` collection).
     * `contentsAsString()`, `contentsAsArray()`, `contentsAsDouble()`, `contentsAsDecimal()`.
     * `releaseBuffers()`.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of TextBuffer.java:

```java
<SOURCE_CODE_OF_TextBuffer.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JACKSONCORE-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: JacksonCore-1 (Issue 98)
- Triggering Test: com.fasterxml.jackson.core.json.TestParserNonStandard::testAllowNaN
- Failure Message: junit.framework.AssertionFailedError: Expected an exception with one of substrings ([can not be represented as BigDecimal]): got one with message "null"
- Defect Scope & Root Cause:
  In `TextBuffer.java`, the `contentsAsDecimal()` method delegates to `NumberInput.parseBigDecimal(...)`:
  ```java
  public BigDecimal contentsAsDecimal() throws NumberFormatException {
      if (_resultArray != null) {
          return NumberInput.parseBigDecimal(_resultArray);
      }
      if (_inputStart >= 0) {
          return NumberInput.parseBigDecimal(_inputBuffer, _inputStart, _inputLen);
      }
      if (_segmentSize == 0) {
          return NumberInput.parseBigDecimal(_currentSegment, 0, _currentSize);
      }
      return NumberInput.parseBigDecimal(contentsAsArray());
  }
  ```
  When invalid numeric tokens such as `"NaN"`, `"Infinity"`, or `"-Infinity"` are contained in `TextBuffer` and converted via `contentsAsDecimal()`, the JDK's `new BigDecimal(...)` constructor throws a `NumberFormatException` with message `"null"`.
  Jackson requires `NumberFormatException` on BigDecimal failure to provide a descriptive error message indicating:
  `Value "<val>" can not be represented as BigDecimal`
  Because the underlying parser/converter fails to format the error message with `"can not be represented as BigDecimal"`, it passes the bare JDK exception through with message `"null"`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testContentsAsDecimal_NaN_DescriptiveMessage_Issue98()`) that:
1. Creates a `TextBuffer` and populates it with `"NaN"`, e.g. via `resetWithCopy("NaN".toCharArray(), 0, 3)` or `append("NaN", 0, 3)`.
2. Calls `textBuffer.contentsAsDecimal()`.
3. Catches `NumberFormatException`.
4. Asserts that the exception message is NOT null and contains the expected substring `"can not be represented as BigDecimal"`.
This test MUST fail on the defective version (where getMessage() is "null") and pass on the fixed version!

Generate the complete JUnit 4 test class TextBufferClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
