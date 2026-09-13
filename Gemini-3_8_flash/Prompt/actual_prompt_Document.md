# Prompt Record for DocumentGeminiTest

- **Timestamp:** 2026-09-13 13:05:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `org.jsoup.nodes.Document`
- **Target Project:** `Jsoup-1b` (Defects4J)

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
- Package Declaration: Must declare `package org.jsoup.nodes;` as line 1.
- Mandatory Explicit Imports:
  * `import java.util.*;`
  * `import org.jsoup.Jsoup;`
  * `import org.jsoup.nodes.*;`
  * `import org.jsoup.parser.Tag;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `DocumentGeminiTest` (`public class DocumentGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Document Construction & Shell Structure** (`new Document(baseUri)`, `createShell(baseUri)`, null baseUri validation, `head()`, `body()`, `createElement()`).
- **Partition B: Document Title Management** (`title()`, `title(newTitle)` updating title in head vs inserting when head lacks title).
- **Partition C: Defect-Targeted Branch Zone (Jsoup-1 / Issue 23 - Normalise Text Ordering)** (Parsing body snippet where text precedes block elements, verifying text nodes outside body are prepended rather than appended to preserve "foo bar baz" order).
- **Partition D: Document Normalisation Transitions** (`normalise()` creating missing html, head, or body elements, moving non-blank text nodes into body).
- **Partition E: Output & Text Mutation** (`outerHtml()` without root wrapper, `text(String)` resetting body content, method chaining).

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
Here is the source code of Document.java:

```java
<SOURCE_CODE_OF_Document.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JSOUP-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Jsoup-1 (Issue 23)
- Triggering Test: org.jsoup.parser.ParserTest::createsStructureFromBodySnippet
- Failure Message: junit.framework.AssertionFailedError: expected:<[foo bar baz]> but was:<[bar baz foo]>
- Defect Scope & Root Cause:
  In `Document.java`, the `normalise()` method attempts to relocate non-blank `TextNode`s that are outside the `<body>` element (such as leading text from body snippets) into the `<body>`:
  ```java
  private void normalise(Element element) {
      List<Node> toMove = new ArrayList<Node>();
      for (Node node: element.childNodes) {
          if (node instanceof TextNode) {
              TextNode tn = (TextNode) node;
              if (!tn.isBlank())
                  toMove.add(tn);
          }
      }

      for (Node node: toMove) {
          element.removeChild(node);
          body().appendChild(new TextNode(" ", ""));
          body().appendChild(node);
      }
  }
  ```
  Notice that `body().appendChild(node)` APPENDS the leading text to the END of `<body>`!
  When parsing an HTML snippet such as `"foo <p>bar</p> baz"`, the text `"foo"` occurs BEFORE `<p>bar</p>`.
  Because `normalise()` appends `"foo"` to the end of the body rather than prepending it, the resulting document text is `"bar baz foo"` instead of `"foo bar baz"`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testCreatesStructureFromBodySnippet_TextOrder_Issue23()`) that:
1. Parses an HTML snippet where text appears before a block element:
   `String html = "foo <p>bar</p> baz";`
   `Document doc = Jsoup.parse(html);`
2. Asserts that the document text maintains original structural order:
   `assertEquals("foo bar baz", doc.text());`
3. Also tests direct normalization on a document where text is added to root or before body:
   Verifying that text outside body is prepended to body, so leading text remains at the beginning of the body.
This test MUST fail on the defective version (where it returns "bar baz foo") and pass on the fixed version!

Generate the complete JUnit 4 test class DocumentGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
