# Prompt Record for RemoveUnusedVarsGeminiTest

- **Timestamp:** 2026-09-09 21:05:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `com.google.javascript.jscomp.RemoveUnusedVars`
- **Target Project:** `Closure-1b` (Defects4J)

---

## System Prompt
```text
You are a Principal Software Quality Assurance (SQA) Engineer and Test Automation Specialist.
Your mission is to perform advanced White-Box Testing on the target Java class from the Defects4J benchmark (Google Closure Compiler - RemoveUnusedVars) to generate a production-grade, fault-revealing JUnit 4 test suite.

---

### 🎯 Core Objectives:
1. Maximize **Line Coverage** and **Branch Coverage (Decision/Condition Coverage)** on `RemoveUnusedVars` traversal and optimization logic.
2. Expose latent defects, specifically the Closure-1 defect pattern where function parameters are improperly stripped when `removeGlobals` is disabled.
3. Ensure **100% deterministic, zero-flakiness, and zero-compilation-error** execution on Java 8 / Defects4J.

---

### 🛠️ Engineering Guidelines & Rules:

#### 1. Imports & Environment Hygiene
- Target Environment: Strictly **Java 8** and **JUnit 4**.
- Package Declaration: Must declare `package com.google.javascript.jscomp;` as line 1.
- Mandatory Explicit Imports:
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
  * `import com.google.javascript.rhino.Node;`
  * `import com.google.javascript.rhino.Token;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies (e.g., `System.currentTimeMillis()`, `new Random()`).
- Class Name: Name the test class `RemoveUnusedVarsGeminiTest` (`public class RemoveUnusedVarsGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Granular Assertions: Verify AST mutations, child count of parameter lists, and node structure before and after pass execution.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Function Parameter Stripping & Closure-1 Defect Zone**:
  * Simple mode (`removeGlobals = false`): unused function parameters MUST NOT be removed.
  * Advanced mode (`removeGlobals = true`): unused trailing parameters can be stripped if not referenced.
  * Preserving parameters on object literal getters/setters (`NodeUtil.isGetOrSetKey`).
- **Partition B: Unused Global & Local Variable Elimination**:
  * Unused `var` declarations, unused function declarations, recursive functions.
  * Variables with assignments having side effects vs without side effects.
- **Partition C: Call Site Optimization (`modifyCallSites`)**:
  * `modifyCallSites = false` vs `modifyCallSites = true` with definition finder.
- **Partition D: Function Expression Names (`preserveFunctionExpressionNames`)**:
  * Anonymous functions, named function expressions assigned to variables.
- **Partition E: Compiler Lifecycle Precondition Guard**:
  * `Preconditions.checkState(compiler.getLifeCycleStage().isNormalized())`.

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
  In `RemoveUnusedVars.java`, method `removeUnreferencedFunctionArgs(Scope fnScope)` was designed to remove unused arguments only in advanced optimization mode where `removeGlobals` is enabled.
  However, it lacks the condition `if (!removeGlobals) { return; }`. Because of this omission, when `removeGlobals == false` (simple mode), the compiler incorrectly strips unreferenced parameters off function declarations, altering `Function.prototype.length` and violating simple-mode guarantees.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testClosure1_DoNotRemoveFunctionArgsWhenNotRemovingGlobals()`) that:
1. Instantiates `RemoveUnusedVars` with `removeGlobals = false`.
2. Processes a function AST containing an unused parameter (e.g., `function foo(x, y) { return x; }`).
3. Asserts that parameter `y` is preserved in the function AST argument list when `removeGlobals == false`.
This assertion MUST trigger/expose the bug on the defective version (where `y` is stripped) and pass on the fixed version!

Generate the complete JUnit 4 test class RemoveUnusedVarsGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
