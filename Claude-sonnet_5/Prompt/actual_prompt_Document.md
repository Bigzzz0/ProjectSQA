# Prompt Record for DocumentClaudeTest

- **Timestamp:** 2026-09-13 13:05:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `org.jsoup.nodes.Document`
- **Target Project:** `Jsoup-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: Document.java from jsoup. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package org.jsoup.nodes;` as line 1.
   - Name the test class `DocumentClaudeTest` (declared as `public class DocumentClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import java.util.*;`
     * `import org.jsoup.Jsoup;`
     * `import org.jsoup.nodes.*;`
     * `import org.jsoup.parser.Tag;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `Document`:
     * `createShell(String baseUri)` (creates html, head, body structure; null validation).
     * `head()` and `body()` accessors (retrieving first head/body element).
     * `title()` and `title(String)` (retrieving title, updating title, adding title to head when missing).
     * `createElement(String)` (element creation with document base URI).
     * `normalise()` (moving text out of root/html/head into body, auto-creating missing html, head, body).
     * `outerHtml()` and `text(String)` on body.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
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

Generate the complete JUnit 4 test class DocumentClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
