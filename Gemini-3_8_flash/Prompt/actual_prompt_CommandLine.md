# Prompt Record for CommandLineGeminiTest

- **Timestamp:** 2026-09-09 20:45:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.apache.commons.cli.CommandLine`
- **Target Project:** `Cli-1b` (Defects4J)

---

## System Prompt
```text
You are a Principal Software Quality Assurance (SQA) Engineer and Test Automation Specialist.
Your mission is to perform advanced White-Box Testing on the target Java class from the Defects4J benchmark (Apache Commons CLI - CommandLine) to generate a production-grade, fault-revealing JUnit 4 test suite.

---

### 🎯 Core Objectives:
1. Maximize **Line Coverage** and **Branch Coverage (Decision/Condition Coverage)** on `CommandLine` logic.
2. Expose latent defects in option resolution, hyphen handling, and short/long option lookups (specifically targeting the Cli-1 / CLI-13 defect pattern).
3. Ensure **100% deterministic, zero-flakiness, and zero-compilation-error** execution on Java 8 / Defects4J.

---

### 🛠️ Engineering Guidelines & Rules:

#### 1. Imports & Environment Hygiene
- Target Environment: Strictly **Java 8** and **JUnit 4**.
- Package Declaration: Must declare `package org.apache.commons.cli;` as line 1.
- Mandatory Explicit Imports:
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
  * `import java.util.Iterator;`
  * `import java.util.List;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies (e.g., `System.currentTimeMillis()`, `new Random()`).
- Class Name: Name the test class `CommandLineGeminiTest` (`public class CommandLineGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Assertions: Always assert both state validity, nullability, and exact string/array return values.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Option Presence & Lookup (`hasOption`)**:
  * Single character option: `hasOption('a')`, `hasOption("a")`.
  * Hyphenated inputs: `hasOption("-a")`, `hasOption("--long")`.
  * Non-existent options: null, empty string, unconfigured keys.
- **Partition B: Value Retrieval (`getOptionValue`, `getOptionValues`)**:
  * Options with no arguments vs single argument vs multiple arguments.
  * Default values: key present with value vs key absent (returns default).
  * Leading hyphen stripping via `Util.stripLeadingHyphens`.
- **Partition C: Option Object & Type Conversion (`getOptionObject`)**:
  * TypeHandler resolution: numeric/class/file types vs unconfigured option.
- **Partition D: Unrecognized Arguments Handling (`addArg`, `getArgs`, `getArgList`)**:
  * Adding arguments in sequence, verifying array conversion in `getArgs()`, verifying list mutability/order in `getArgList()`.
- **Partition E: Option Collections & Iteration (`getOptions`, `iterator`)**:
  * Adding multiple distinct options and options with identical hashcodes.
  * Verifying `getOptions()` returns all processed `Option` elements.
  * Iterating through `iterator()` and verifying count.
- **Partition F: Cli-1 Critical Bug Zone**:
  * Cross-resolution between short opt and long opt when querying values.

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
Here is the source code of CommandLine.java:

```java
<SOURCE_CODE_OF_CommandLine.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J CLI-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Cli-1 (JIRA CLI-13)
- Triggering Test: org.apache.commons.cli.bug.BugCLI13Test::testCLI13
- Failure Message: junit.framework.AssertionFailedError
- Defect Scope & Behavior:
  In Apache Commons CLI `CommandLine`, resolving options using `hasOption`, `getOptionValue`, and `getOptionValues` requires proper mapping between short options, long options, and hyphenated option strings. When options are registered with both short and long forms, queries using either format or with leading hyphens must correctly resolve to the option's value.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testCLI13_MultipleOptionsAndHyphenResolution()`) that:
1. Creates `Option` instances with both short and long names (e.g. `OptionBuilder.withLongOpt("debug").create('d')` or `new Option("d", "debug", true, "desc")`).
2. Adds values to the option and registers it into a `CommandLine` instance via `addOption(...)`.
3. Asserts that querying via short opt (`"d"` or `'d'`), long opt (`"debug"`), or hyphenated form resolves the correct argument value.

Generate the complete JUnit 4 test class CommandLineGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
