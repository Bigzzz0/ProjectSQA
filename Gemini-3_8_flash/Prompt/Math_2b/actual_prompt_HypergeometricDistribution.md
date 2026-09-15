# Prompt Record for HypergeometricDistributionGeminiTest

- **Timestamp:** 2026-09-13 13:35:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.apache.commons.math3.distribution.HypergeometricDistribution`
- **Target Project:** `Math-2b` (Defects4J)

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
- Package Declaration: Must declare `package org.apache.commons.math3.distribution;` as line 1.
- Mandatory Explicit Imports:
  * `import org.apache.commons.math3.distribution.HypergeometricDistribution;`
  * `import org.apache.commons.math3.exception.NotPositiveException;`
  * `import org.apache.commons.math3.exception.NotStrictlyPositiveException;`
  * `import org.apache.commons.math3.exception.NumberIsTooLargeException;`
  * `import org.apache.commons.math3.random.Well19937c;`
  * `import org.junit.Test;`
  * `import org.junit.Assert;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `HypergeometricDistributionGeminiTest` (`public class HypergeometricDistributionGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Parameter Validation & Edge Construction** (Negative population, zero population, negative successes, negative sample size, sample > population, successes > population).
- **Partition B: Probability Mass Function (`probability`) & Support Bounds** (`x < lowerBound`, `x > upperBound`, exact PMF calculation, `getSupportLowerBound()`, `getSupportUpperBound()`, `isSupportConnected()`).
- **Partition C: Cumulative Probabilities** (`cumulativeProbability` & `upperCumulativeProbability` at left/right tails and internal steps).
- **Partition D: Statistical Moments & Defect Zone (Math-2 / MATH-1021)** (`getNumericalMean()`, `getNumericalVariance()` with caching, 32-bit integer overflow in mean calculation).
- **Partition E: Sampling & Inversion Method** (`sample()` across normal and extreme scale parameters).

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

Generate the complete JUnit 4 test class HypergeometricDistributionGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
