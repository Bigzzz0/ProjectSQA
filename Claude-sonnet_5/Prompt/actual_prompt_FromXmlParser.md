# Prompt Record for FromXmlParserClaudeTest

- **Timestamp:** 2026-09-12 10:20:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser`
- **Target Project:** `JacksonXml-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: FromXmlParser.java from Jackson Dataformat XML. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package com.fasterxml.jackson.dataformat.xml.deser;` as line 1.
   - Name the test class `FromXmlParserClaudeTest` (declared as `public class FromXmlParserClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party mocking libraries. Use standard Jackson XML types (`XmlMapper`, `XmlFactory`, `FromXmlParser`, etc.).
   - Use standard imports:
     * `import java.io.*;`
     * `import java.util.*;`
     * `import com.fasterxml.jackson.core.*;`
     * `import com.fasterxml.jackson.databind.*;`
     * `import com.fasterxml.jackson.dataformat.xml.*;`
     * `import com.fasterxml.jackson.dataformat.xml.annotation.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `FromXmlParser`:
     * Token navigation: `nextToken()`, `nextTextValue()`, `skipChildren()`.
     * Value extraction: `getText()`, `getTextCharacters()`, `getTextLength()`, `getTextOffset()`.
     * Primitive coercion: `getIntValue()`, `getLongValue()`, `getDoubleValue()`, `getBooleanValue()`.
     * XML attributes and namespaces handling.
     * Empty elements, leaf elements, and text elements (`_cfgNameForTextElement`).
     * Nested lists and unwrapped collection deserialization.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
```

---

## User Prompt

```text
Here is the source code of FromXmlParser.java:

```java
<SOURCE_CODE_OF_FromXmlParser.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JACKSONXML-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: JacksonXml-1 (Issue 180)
- Triggering Tests:
  * com.fasterxml.jackson.dataformat.xml.lists.NestedUnwrappedLists180Test::testNestedUnwrappedLists180
  * com.fasterxml.jackson.dataformat.xml.lists.NestedUnwrappedListsTest::testNestedWithEmpty2
  * com.fasterxml.jackson.dataformat.xml.lists.NestedUnwrappedListsTest::testNestedWithEmpty
- Failure Message: junit.framework.AssertionFailedError: expected:<1> but was:<0>
- Defect Scope & Root Cause:
  In `FromXmlParser.java`, during token navigation in `nextToken()`:
  When encountering an empty element (e.g. `<records/>` or `<records></records>` or an empty leaf element) at `XmlTokenStream.XML_END_ELEMENT` when `_mayBeLeaf == true`:
  The parser erroneously returns `JsonToken.VALUE_NULL` (or swallows the token), instead of correctly handling empty object or text tokens.
  When deserializing nested unwrapped lists containing an empty list or object (e.g., `<Parent><child/></Parent>`), this misinterpretation causes the nested unwrapped structure to be completely lost or skipped, leading to `expected:<1> but was:<0>`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testNestedUnwrappedListsWithEmptyElement_Issue180()`) that:
1. Defines POJOs with nested collections / unwrapped lists:
   ```java
   static class Root {
       @JacksonXmlElementWrapper(useWrapping = false)
       public List<Folder> folder;
   }
   static class Folder {
       public String name;
       @JacksonXmlElementWrapper(useWrapping = false)
       public List<Item> item;
   }
   static class Item {
       public String id;
   }
   ```
2. Parses XML containing an empty element within an unwrapped list, e.g.:
   `"<Root><folder><name>test</name><item/></folder></Root>"`
   or `"<Root><folder><name>f1</name></folder></Root>"` (where item list is empty or folder contains empty item).
3. Asserts that the collection or folder is deserialized with the expected count (e.g. `assertEquals(1, root.folder.size())`), and does NOT lose elements or fail with `expected:<1> but was:<0>`.
This test MUST fail on the defective version and pass on the fixed version!

Generate the complete JUnit 4 test class FromXmlParserClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
