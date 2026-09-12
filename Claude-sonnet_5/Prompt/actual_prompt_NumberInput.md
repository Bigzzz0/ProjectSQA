# Prompt Record for NumberInputClaudeTest

- **Timestamp:** 2026-09-12 09:55:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `com.fasterxml.jackson.core.io.NumberInput`
- **Target Project:** `JacksonCore-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: NumberInput.java from Jackson Core. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package com.fasterxml.jackson.core.io;` as line 1.
   - Name the test class `NumberInputClaudeTest` (declared as `public class NumberInputClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import java.math.BigDecimal;`
     * `import java.math.BigInteger;`
     * `import java.util.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `NumberInput`:
     * `parseInt(char[], int, int)` and `parseInt(String)` (1-9 digits, negative, edge boundaries, overflow).
     * `parseLong(char[], int, int)` and `parseLong(String)` (up to 18 digits, 19 digits with inLongRange check, MIN/MAX values).
     * `inLongRange(...)` (exact comparison with Long.MIN_VALUE and Long.MAX_VALUE).
     * `parseAsInt`, `parseAsLong`, `parseAsDouble` (null, empty, whitespace trimming, fallbacks to defaultValue).
     * `parseDouble(String)` (including NASTY_SMALL_DOUBLE "2.2250738585072012e-308" returning Double.MIN_VALUE).
     * `parseBigDecimal(String)` and `parseBigDecimal(char[], int, int)`.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of NumberInput.java:

```java
<SOURCE_CODE_OF_NumberInput.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JACKSONCORE-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: JacksonCore-1 (Issue 98)
- Triggering Test: com.fasterxml.jackson.core.json.TestParserNonStandard::testAllowNaN
- Failure Message: junit.framework.AssertionFailedError: Expected an exception with one of substrings ([can not be represented as BigDecimal]): got one with message "null"
- Defect Scope & Root Cause:
  In `NumberInput.java`, `parseBigDecimal(String numStr)` and `parseBigDecimal(char[] buffer, int offset, int len)` directly delegate to JDK's `new BigDecimal(...)`:
  ```java
  public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException {
      return new BigDecimal(numStr);
  }
  public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len) throws NumberFormatException {
      return new BigDecimal(buffer, offset, len);
  }
  ```
  When invalid numeric tokens such as `"NaN"`, `"Infinity"`, or `"-Infinity"` are parsed into `BigDecimal`, JDK 7/8's `new BigDecimal(...)` constructor throws a `NumberFormatException` with message `"null"`.
  Jackson requires `NumberFormatException` on BigDecimal failure to provide a descriptive error message indicating:
  `Value "<val>" can not be represented as BigDecimal`
  Because `NumberInput.java` lacks a `try-catch (NumberFormatException e)` wrapper to format the error message with `"can not be represented as BigDecimal"`, it passes the bare JDK exception through with message `"null"`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testParseBigDecimal_NaN_DescriptiveMessage_Issue98()`) that:
1. Calls `NumberInput.parseBigDecimal("NaN")` and/or `NumberInput.parseBigDecimal("NaN".toCharArray(), 0, 3)`.
2. Catches `NumberFormatException`.
3. Asserts that the exception message is NOT null and contains the expected substring `"can not be represented as BigDecimal"`.
4. Also verify behavior for other non-representable values like `"Infinity"` and `"-Infinity"`.
This test MUST fail on the defective version (where getMessage() is "null") and pass on the fixed version!

Generate the complete JUnit 4 test class NumberInputClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
