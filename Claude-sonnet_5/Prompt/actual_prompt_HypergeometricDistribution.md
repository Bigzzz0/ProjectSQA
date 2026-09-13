# Prompt Record for HypergeometricDistributionClaudeTest

- **Timestamp:** 2026-09-13 13:35:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.apache.commons.math3.distribution.HypergeometricDistribution`
- **Target Project:** `Math-2b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: HypergeometricDistribution.java from Apache Commons Math. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.apache.commons.math3.distribution;` as line 1.
   - Name the test class `HypergeometricDistributionClaudeTest` (declared as `public class HypergeometricDistributionClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import org.apache.commons.math3.distribution.HypergeometricDistribution;`
     * `import org.apache.commons.math3.exception.NotPositiveException;`
     * `import org.apache.commons.math3.exception.NotStrictlyPositiveException;`
     * `import org.apache.commons.math3.exception.NumberIsTooLargeException;`
     * `import org.apache.commons.math3.random.Well19937c;`
     * `import org.junit.Test;`
     * `import org.junit.Assert;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `HypergeometricDistribution`:
     * Constructor validations:
       - `populationSize <= 0` -> throws `NotStrictlyPositiveException`
       - `numberOfSuccesses < 0` -> throws `NotPositiveException`
       - `sampleSize < 0` -> throws `NotPositiveException`
       - `numberOfSuccesses > populationSize` -> throws `NumberIsTooLargeException`
       - `sampleSize > populationSize` -> throws `NumberIsTooLargeException`
     * Probability Density / Mass Function (`probability(int x)`):
       - Out-of-bounds: `x < lowerDomain`, `x > upperDomain` returning 0.0
       - Valid domain values with precision delta assertions
     * Cumulative Distribution (`cumulativeProbability(int x)`):
       - `x < lowerDomain` returning 0.0
       - `x >= upperDomain` returning 1.0
       - Inner summation loop branches
     * Upper Cumulative Distribution (`upperCumulativeProbability(int x)`):
       - `x <= lowerDomain` returning 1.0
       - `x > upperDomain` returning 0.0
       - Inner summation loop branches
     * Statistical Properties:
       - `getNumericalMean()`
       - `getNumericalVariance()` (including caching behavior)
       - `getSupportLowerBound()`, `getSupportUpperBound()`
       - `isSupportConnected()`
     * Sampling:
       - `sample()` with custom `RandomGenerator` / `reseedRandomGenerator(long)`
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of HypergeometricDistribution.java:

```java
<SOURCE_CODE_OF_HypergeometricDistribution.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J MATH-2) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Math-2 (JIRA MATH-1021)
- Triggering Test: org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMath1021
- Failure Message: junit.framework.AssertionFailedError: sample=-50
- Defect Scope & Root Cause:
  In `HypergeometricDistribution.java`, the `getNumericalMean()` method is implemented as:
  ```java
  public double getNumericalMean() {
      return (double) (getSampleSize() * getNumberOfSuccesses()) / (double) getPopulationSize();
  }
  ```
  Both `getSampleSize()` and `getNumberOfSuccesses()` return 32-bit signed integers (`int`).
  When `getSampleSize() * getNumberOfSuccesses()` exceeds `Integer.MAX_VALUE` (2,147,483,647), a 32-bit **integer overflow** occurs **before** the result is cast to `double`!
  This produces a negative product, causing `getNumericalMean()` to return a negative mean ($\mu < 0$), even though the mean of a hypergeometric distribution must strictly satisfy $\mu \ge 0$.
  Consequently, when `sample()` utilizes this negative mean during inverse cumulative probability calculation, it returns invalid negative samples (such as `sample = -50`), violating the distribution support $[0, \text{numberOfSuccesses}]$.

  Fixed version casts to `long` before multiplication:
  ```java
  return (double) ((long) getSampleSize() * (long) getNumberOfSuccesses()) / (double) getPopulationSize();
  ```

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testMath1021_IntegerOverflowInNumericalMean()`) that:
1. Instantiates `HypergeometricDistribution` with large parameters that trigger 32-bit integer overflow:
   ```java
   final int populationSize = 1437651;
   final int numberOfSuccesses = 28975;
   final int sampleSize = 76182;
   // Note: 28975 * 76182 = 2,207,373,450 > Integer.MAX_VALUE (2,147,483,647)
   HypergeometricDistribution dist = new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);
   ```
2. Asserts that `getNumericalMean()` is non-negative:
   ```java
   double mean = dist.getNumericalMean();
   assertTrue("Numerical mean must be >= 0, but was: " + mean, mean >= 0.0);
   ```
3. Samples 100 times using `dist.sample()` and asserts that all sample values are valid:
   ```java
   for (int i = 0; i < 100; ++i) {
       int sample = dist.sample();
       assertTrue("sample must be >= 0, but was: " + sample, 0 <= sample);
       assertTrue("sample must be <= numberOfSuccesses, but was: " + sample, sample <= numberOfSuccesses);
   }
   ```
This test MUST fail on the defective version (where mean is negative and sample=-50) and pass on the fixed version!

Generate the complete JUnit 4 test class HypergeometricDistributionClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
