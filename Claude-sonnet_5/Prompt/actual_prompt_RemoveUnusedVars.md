# Prompt Record for RemoveUnusedVarsClaudeTest

- **Timestamp:** 2026-09-09 21:05:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `com.google.javascript.jscomp.RemoveUnusedVars`
- **Target Project:** `Closure-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: RemoveUnusedVars.java from the Google Closure Compiler. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package com.google.javascript.jscomp;` as line 1.
   - Name the test class `RemoveUnusedVarsClaudeTest` (declared as `public class RemoveUnusedVarsClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard Rhino AST / Closure Compiler nodes (`Node`, `Token`, `Compiler`, `CompilerOptions`, etc.) and standard JUnit 4 assertions.

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all optimization passes and traversals in `RemoveUnusedVars`.
   - Test various configurations: `removeGlobals` (true vs false), `preserveFunctionExpressionNames` (true vs false), `modifyCallSites` (true vs false).
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of RemoveUnusedVars.java:

```java
<SOURCE_CODE_OF_RemoveUnusedVars.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J CLOSURE-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Closure-1 (Issue 253 / Issue 168b)
- Triggering Tests:
  * com.google.javascript.jscomp.CommandLineRunnerTest::testSimpleModeLeavesUnusedParams
  * com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168b
- Failure Message: junit.framework.AssertionFailedError
- Defect Scope & Root Cause:
  In `RemoveUnusedVars`, removing unused function parameters breaks `Function.prototype.length` and is only permissible in advanced optimization mode when `removeGlobals` is enabled.
  However, in `removeUnreferencedFunctionArgs(Scope fnScope)`, the pass fails to verify `if (!removeGlobals) { return; }`. As a result, when `removeGlobals` is `false` (simple mode), unreferenced function parameters are erroneously stripped from function declarations.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testIssue168b_DoNotRemoveParamsWhenNotRemovingGlobals()`) that:
1. Configures `RemoveUnusedVars` with `removeGlobals = false`.
2. Parses/processes JavaScript code containing a function declaration with an unused parameter (e.g., `function a(x, y) { return x; }`).
3. Asserts that the unused parameter (e.g., `y`) is NOT removed from the function argument list when `removeGlobals == false`.
This assertion MUST expose/trigger the bug on the defective version (where `y` is erroneously removed) while passing on the fixed version!

Generate the complete JUnit 4 test class RemoveUnusedVarsClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
