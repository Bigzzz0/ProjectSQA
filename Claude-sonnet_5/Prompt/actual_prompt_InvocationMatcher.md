# Prompt Record for InvocationMatcherClaudeTest

- **Timestamp:** 2026-09-13 13:40:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.mockito.internal.invocation.InvocationMatcher`
- **Target Project:** `Mockito-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: InvocationMatcher.java from the Mockito framework. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.mockito.internal.invocation;` as line 1.
   - Name the test class `InvocationMatcherClaudeTest` extending `TestBase`: `public class InvocationMatcherClaudeTest extends TestBase`.

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Truth).
   - Use standard imports:
     * `import java.lang.reflect.Method;`
     * `import java.util.*;`
     * `import org.hamcrest.Matcher;`
     * `import org.junit.Test;`
     * `import org.junit.Before;`
     * `import org.mockito.Mock;`
     * `import org.mockito.Mockito;`
     * `import org.mockito.internal.matchers.*;`
     * `import org.mockito.internal.reporting.PrintSettings;`
     * `import org.mockito.invocation.Invocation;`
     * `import org.mockito.invocation.Location;`
     * `import org.mockitousage.IMethods;`
     * `import org.mockitoutil.TestBase;`
     * `import static java.util.Arrays.asList;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `InvocationMatcher`:
     * Constructors: `InvocationMatcher(Invocation)`, `InvocationMatcher(Invocation, List<Matcher>)` with empty/non-empty matchers.
     * Accessors: `getMethod()`, `getInvocation()`, `getMatchers()`, `getLocation()`.
     * Matching: `matches(Invocation actual)` (matching count, matching types, non-matching count, non-matching types).
     * Method similarity: `hasSameMethod(Invocation candidate)` and `hasSimilarMethod(Invocation candidate)` (overloaded methods, mock instance matching, name comparison).
     * String representation: `toString(PrintSettings)` and `toString()`.
     * Argument capturing (`captureArgumentsFrom(Invocation)`):
       - Normal non-vararg invocation: capturing arguments via `CapturesArguments` / `CapturingMatcher`.
       - Vararg invocation: capturing arguments when `invocation.getMethod().isVarArgs()` is true.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of InvocationMatcher.java:

```java
<SOURCE_CODE_OF_InvocationMatcher.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J MOCKITO-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Mockito-1 (Issue 188)
- Triggering Test: org.mockito.internal.invocation.InvocationMatcherTest::should_capture_arguments_when_args_count_does_NOT_match
- Failure Message: java.lang.UnsupportedOperationException
- Defect Scope & Root Cause:
  In `InvocationMatcher.java`, the `captureArgumentsFrom(Invocation invocation)` method is implemented as:
  ```java
  public void captureArgumentsFrom(Invocation invocation) {
      if (invocation.getMethod().isVarArgs()) {
          int indexOfVararg = invocation.getRawArguments().length - 1;
          throw new UnsupportedOperationException();

      } else {
          for (int position = 0; position < matchers.size(); position++) {
              Matcher m = matchers.get(position);
              if (m instanceof CapturesArguments) {
                  ((CapturesArguments) m).captureFrom(invocation.getArgumentAt(position, Object.class));
              }
          }
      }
  }
  ```
  Notice that when `invocation.getMethod().isVarArgs()` evaluates to `true`, the code explicitly throws `UnsupportedOperationException()`!
  This causes any verification or stubbing that triggers argument capture on methods with variable arguments (`varargs`) to immediately crash with `UnsupportedOperationException`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testCaptureArgumentsFromVarargMethod_UnsupportedOperationException()`) that:
1. Calls a vararg method on a mock object (using `@Mock private IMethods mock;` from `TestBase`):
   ```java
   mock.varargs(new String[] { "arg1", "arg2" });
   Invocation invocation = getLastInvocation();
   ```
2. Instantiates an `InvocationMatcher` with the invocation and argument matcher:
   ```java
   Matcher matcher = new LocalizedMatcher(new CapturingMatcher());
   InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));
   ```
3. Calls `invocationMatcher.captureArgumentsFrom(invocation);`.
4. Asserts that argument capture succeeds without throwing `UnsupportedOperationException`.
This test MUST fail on the defective version (where it throws `java.lang.UnsupportedOperationException`) and pass on the fixed version!

Generate the complete JUnit 4 test class InvocationMatcherClaudeTest extending TestBase that achieves maximum line and branch coverage and targets this defect.
```
