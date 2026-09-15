# Prompt Record for JDOMNodePointerGeminiTest

- **Timestamp:** 2026-09-13 13:10:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer`
- **Target Project:** `JxPath-1b` (Defects4J)

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
- Package Declaration: Must declare `package org.apache.commons.jxpath.ri.model.jdom;` as line 1.
- Mandatory Explicit Imports:
  * `import java.util.*;`
  * `import org.jdom.*;`
  * `import org.apache.commons.jxpath.*;`
  * `import org.apache.commons.jxpath.ri.*;`
  * `import org.apache.commons.jxpath.ri.model.*;`
  * `import org.apache.commons.jxpath.ri.compiler.*;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `JDOMNodePointerGeminiTest` (`public class JDOMNodePointerGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Node Pointer Initialization & Type Introspection** (`JDOMNodePointer(node, locale)`, `JDOMNodePointer(parent, node)`, `isLeaf()`, `isCollection()`, `getLength()`, `getBaseValue()`, `getImmediateNode()`).
- **Partition B: Node Matching & Filtering** (`testNode(NodeTest)` across `NodeNameTest` with wildcards/prefixes/namespaces, `NodeTypeTest` for element/text/comment/PI).
- **Partition C: Defect-Targeted Branch Zone (JxPath-1 / JXPATH-12)** (`asPath()` on child element pointers with null `NamespaceResolver`, verifying no `NullPointerException` is thrown).
- **Partition D: Value & Content Mutation** (`getValue()`, `setValue(Object)` replacing string, cloning nodes, removing child nodes, `createAttribute`, `remove()`).
- **Partition E: Namespace & Pointer Comparisons** (`getNamespaceURI()`, `getDefaultNamespaceURI()`, `getNamespaceURI(String)`, `compareChildNodePointers`).

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
Here is the source code of JDOMNodePointer.java:

```java
<SOURCE_CODE_OF_JDOMNodePointer.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JXPATH-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: JxPath-1 (JIRA JXPATH-12)
- Triggering Test: org.apache.commons.jxpath.ri.model.jdom.JDOMModelTest::testGetNode
- Failure Message: java.lang.NullPointerException
- Defect Scope & Root Cause:
  In `JDOMNodePointer.java`, the `asPath()` method:
  ```java
  String nsURI = getNamespaceURI();
  String ln = JDOMNodePointer.getLocalName(node);
  if (equalStrings(nsURI, 
          getNamespaceResolver().getDefaultNamespaceURI())) {
      buffer.append(ln);
      buffer.append('[');
      buffer.append(getRelativePositionByName()).append(']');
  }
  else {
      String prefix = getNamespaceResolver().getPrefix(nsURI);
      ...
  ```
  invokes `getNamespaceResolver().getDefaultNamespaceURI()` and `getNamespaceResolver().getPrefix(nsURI)` WITHOUT checking whether `getNamespaceResolver()` returns `null`!
  When a `JDOMNodePointer` is created without an explicitly attached `NamespaceResolver`, `getNamespaceResolver()` evaluates to `null`.
  Calling `asPath()` on child element pointers immediately triggers a `NullPointerException`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testAsPathWithoutNamespaceResolver_NullPointerException_JXPATH12()`) that:
1. Creates JDOM Elements with parent-child structure:
   ```java
   Element root = new Element("root");
   Element child = new Element("child");
   root.addContent(child);
   ```
2. Wraps them in parent and child `JDOMNodePointer`s:
   ```java
   JDOMNodePointer rootPointer = new JDOMNodePointer(root, Locale.getDefault());
   JDOMNodePointer childPointer = new JDOMNodePointer(rootPointer, child);
   ```
3. Invokes `childPointer.asPath()`.
4. Asserts that `asPath()` completes successfully without throwing `NullPointerException`.
This test MUST fail on the defective version (where it throws java.lang.NullPointerException) and pass on the fixed version!

Generate the complete JUnit 4 test class JDOMNodePointerGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
