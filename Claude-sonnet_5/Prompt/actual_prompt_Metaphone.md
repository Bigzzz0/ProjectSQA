# Prompt Record for MetaphoneClaudeTest

- **Timestamp:** 2026-09-11 17:20:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.apache.commons.codec.language.Metaphone`
- **Target Project:** `Codec-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: Metaphone.java from Apache Commons Codec. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.apache.commons.codec.language;` as line 1.
   - Name the test class `MetaphoneClaudeTest` (declared as `public class MetaphoneClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import org.apache.commons.codec.EncoderException;`
     * `import java.util.Locale;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `Metaphone`:
     * `metaphone(String txt)`
     * `encode(Object pObject)` (valid String, null, non-String throwing `EncoderException`)
     * `encode(String pString)`
     * `isMetaphoneEqual(String str1, String str2)`
     * `getMaxCodeLen()`, `setMaxCodeLen(int)`
   - Thoroughly cover all Metaphone phonetic transformation rules:
     * Initial transformations (KN, GN, PN, AE, WR, WH, X)
     * Vowels handling (only retain vowels at the beginning of words)
     * Consonant rules (B, MB, C combinations: CIA, CH, C followed by I/E/Y vs others, D combinations: DGE, DGY, DGI, DT, G combinations: GH, GN, GNED, G followed by I/E/Y, H rules, K, P, PH, Q, S combinations: SH, SIO, SIA, SC, T combinations: TIA, TIO, TH, TCH, V, W, Y, X, Z)
     * Max code length truncation and configuration
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of Metaphone.java:

```java
<SOURCE_CODE_OF_Metaphone.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J CODEC-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Codec-1 (CODEC-65)
- Triggering Test: org.apache.commons.codec.language.MetaphoneTest::testLocaleIndependence
- Failure Message: junit.framework.ComparisonFailure: tr: expected:<[I]> but was:<[İ]>
- Defect Scope & Root Cause:
  In `Metaphone.java`, lines 89 and 97:
  `return txt.toUpperCase();` and `inwd = txt.toUpperCase();`
  The string is converted to uppercase using the platform's default Locale (`Locale.getDefault()`) instead of `Locale.ENGLISH`.
  In Turkish locale (`new Locale("tr")`), lowercase 'i' converts to dotted uppercase 'İ' (\u0130), NOT ASCII 'I'.
  Consequently, `metaphone("i")` under Turkish locale returns `"İ"` instead of `"I"`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testLocaleIndependence_Turkish()`) that:
1. Saves the current default locale: `Locale orig = Locale.getDefault();`
2. Sets the default locale to Turkish: `Locale.setDefault(new Locale("tr"));`
3. Inside a `try { ... } finally { Locale.setDefault(orig); }` block:
   - Creates a `Metaphone` instance.
   - Encodes `"i"`.
   - Asserts `assertEquals("I", metaphone.metaphone("i"));`
   - Also asserts `assertEquals(metaphone.metaphone("I"), metaphone.metaphone("i"));`
This test MUST fail on the defective version (where "i" produces "İ") and pass on the fixed version!

Generate the complete JUnit 4 test class MetaphoneClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
