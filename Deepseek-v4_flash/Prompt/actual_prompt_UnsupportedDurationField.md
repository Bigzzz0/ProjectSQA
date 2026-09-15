# Prompt Record for UnsupportedDurationFieldDeepseekTest

- **Timestamp:** 2026-09-13 13:50:00
- **Model Used:** deepseek-v4-flash
- **Target Class:** `org.joda.time.field.UnsupportedDurationField`
- **Target Project:** `Time-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: UnsupportedDurationField.java from Joda-Time. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.joda.time.field;` as line 1.
   - Name the test class `UnsupportedDurationFieldDeepseekTest` (declared as `public class UnsupportedDurationFieldDeepseekTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Truth).
   - Use standard imports:
     * `import org.joda.time.*;`
     * `import org.joda.time.chrono.*;`
     * `import org.joda.time.field.UnsupportedDurationField;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `UnsupportedDurationField`:
     * Singleton factory: `getInstance(DurationFieldType)`.
     * Core properties: `getType()`, `getName()`, `isSupported()` (must be false), `isPrecise()` (must be true), `getUnitMillis()` (must be 0).
     * Unsupported operations (must throw UnsupportedOperationException):
       - `getValue(long)`, `getValueAsLong(long)`, `getValue(long, long)`, `getValueAsLong(long, long)`
       - `getMillis(int)`, `getMillis(long)`, `getMillis(int, long)`, `getMillis(long, long)`
       - `add(long, int)`, `add(long, long)`
       - `getDifference(long, long)`, `getDifferenceAsLong(long, long)`
     * Comparisons & Contract:
       - `compareTo(DurationField)`: comparison with supported fields and unsupported fields.
       - `equals(Object)`: same instance, different types, null, non-UnsupportedDurationField, same name.
       - `hashCode()`: consistent with equals.
       - `toString()`.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
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

Generate the complete JUnit 4 test class UnsupportedDurationFieldDeepseekTest that achieves maximum line and branch coverage and targets this defect.
```
