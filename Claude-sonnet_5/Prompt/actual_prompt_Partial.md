# Prompt Record for PartialClaudeTest

- **Timestamp:** 2026-09-13 13:50:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.joda.time.Partial`
- **Target Project:** `Time-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: Partial.java from Joda-Time. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.joda.time;` as line 1.
   - Name the test class `PartialClaudeTest` (declared as `public class PartialClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Truth).
   - Use standard imports:
     * `import org.joda.time.*;`
     * `import org.joda.time.chrono.*;`
     * `import org.joda.time.format.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `Partial`:
     * Constructors: default, `(Chronology)`, `(DateTimeFieldType, int)`, `(DateTimeFieldType[], int[])`, `(DateTimeFieldType[], int[], Chronology)`, copy constructor.
     * Ordering validations in constructor:
       - Null types array / null values array -> IllegalArgumentException.
       - Mismatched array lengths -> IllegalArgumentException.
       - Duplicate field types -> IllegalArgumentException.
       - Out-of-order fields (largest-to-smallest ordering requirement).
     * Value manipulation & arithmetic: `with(DateTimeFieldType, int)`, `without(DateTimeFieldType)`, `plus(ReadablePeriod)`, `minus(ReadablePeriod)`.
     * Querying & formatting: `isMatch(ReadableInstant)`, `isMatch(ReadablePartial)`, `toString()`, `toString(String)`, `toString(String, Locale)`.
     * Equality and hashing: `equals(Object)`, `hashCode()`, `compareTo(ReadablePartial)`.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of Partial.java:

```java
<SOURCE_CODE_OF_Partial.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J TIME-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Time-1 (Issue 93)
- Triggering Test: org.joda.time.TestPartial_Constructors::testConstructorEx7_TypeArray_intArray
- Failure Message: junit.framework.AssertionFailedError
- Defect Scope & Root Cause:
  In `Partial.java`, the constructor validates that the `DateTimeFieldType[]` array is sorted from largest to smallest unit duration:
  ```java
  int compare = lastUnitField.compareTo(loopUnitField);
  if (compare < 0) {
      throw new IllegalArgumentException("Types array must be in order largest-smallest: " +
              types[i - 1].getName() + " < " + loopType.getName());
  }
  ```
  However, when an unsupported duration field such as `DateTimeFieldType.era()` is included at the end of the array after smaller units (e.g. `[year, dayOfMonth, era]`), `lastUnitField.compareTo(loopUnitField)` compares `days.compareTo(eras)`.
  Because `days` is supported and `eras` is an `UnsupportedDurationField`, the comparison erroneously evaluates to positive (`1`), so `compare < 0` is false!
  Consequently, `Partial` fails to reject the invalid largest-to-smallest field order `[year, dayOfMonth, era]`, allowing an improperly ordered Partial to be created without throwing `IllegalArgumentException`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testConstructor_RejectOutOfOrderWithEra_Time1()`) that:
1. Constructs a `Partial` with field types where `era()` is placed after smaller units such as `year()` and `dayOfMonth()`:
   ```java
   DateTimeFieldType[] types = new DateTimeFieldType[] {
       DateTimeFieldType.year(),
       DateTimeFieldType.dayOfMonth(),
       DateTimeFieldType.era()
   };
   int[] values = new int[] { 2000, 15, 1 };
   ```
2. Asserts that this invalid largest-to-smallest field order throws `IllegalArgumentException`:
   ```java
   try {
       new Partial(types, values);
       fail("Expected IllegalArgumentException: types array must be in order largest-smallest");
   } catch (IllegalArgumentException expected) {
       assertTrue("Expected order error message", expected.getMessage().contains("order"));
   }
   ```
This test MUST fail on the defective version (where no exception is thrown and fail() is reached) and pass on the fixed version!

Generate the complete JUnit 4 test class PartialClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
