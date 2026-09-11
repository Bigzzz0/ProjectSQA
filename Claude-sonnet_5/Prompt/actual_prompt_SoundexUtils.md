# Prompt Record for SoundexUtilsClaudeTest

- **Timestamp:** 2026-09-11 17:20:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.apache.commons.codec.language.SoundexUtils`
- **Target Project:** `Codec-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: SoundexUtils.java from Apache Commons Codec. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.apache.commons.codec.language;` as line 1.
   - Name the test class `SoundexUtilsClaudeTest` (declared as `public class SoundexUtilsClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import org.apache.commons.codec.EncoderException;`
     * `import org.apache.commons.codec.StringEncoder;`
     * `import java.util.Locale;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all package-private and public methods in `SoundexUtils`:
     * `clean(String str)`
     * `difference(StringEncoder encoder, String s1, String s2)`
     * `differenceEncoded(String es1, String es2)`
   - Thoroughly cover boundary conditions:
     * null and empty string inputs
     * strings with all letters (`count == len`)
     * strings with mixed letters and non-letters (`count < len`)
     * strings with no letters at all (`count == 0`)
     * difference comparison with null arguments, unequal lengths, identical strings, completely different strings
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
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

Generate the complete JUnit 4 test class SoundexUtilsClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
