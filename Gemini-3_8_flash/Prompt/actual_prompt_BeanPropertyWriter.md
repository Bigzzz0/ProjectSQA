# Prompt Record for BeanPropertyWriterGeminiTest

- **Timestamp:** 2026-09-12 10:06:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `com.fasterxml.jackson.databind.ser.BeanPropertyWriter`
- **Target Project:** `JacksonDatabind-1b` (Defects4J)

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
- Package Declaration: Must declare `package com.fasterxml.jackson.databind.ser;` as line 1.
- Mandatory Explicit Imports:
  * `import java.io.*;`
  * `import java.util.*;`
  * `import com.fasterxml.jackson.annotation.*;`
  * `import com.fasterxml.jackson.core.*;`
  * `import com.fasterxml.jackson.databind.*;`
  * `import com.fasterxml.jackson.databind.ser.*;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth). Use standard Jackson types (`ObjectMapper`, `JsonFactory`, `JsonGenerator`, etc.) and POJOs.
  * NO non-deterministic dependencies.
- Class Name: Name the test class `BeanPropertyWriterGeminiTest` (`public class BeanPropertyWriterGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Property Introspection & Metadata** (`getName()`, `getFullName()`, `getType()`, `getMember()`, `getSerializationType()`, `getViews()`).
- **Partition B: Standard Field Serialization** (`serializeAsField` with non-null values, null values, suppressed empty/default values).
- **Partition C: Defect-Targeted Branch Zone (JacksonDatabind-1 / Issue 223)** (`serializeAsColumn` in POJO-as-Array mode with null values, verifying exactly one null element is written and no duplicate null or NullPointerException occurs).
- **Partition D: Value Suppression & Custom Serializers** (`MARKER_FOR_EMPTY`, non-empty suppression, custom null serializers, `serializeAsPlaceholder`).
- **Partition E: Dynamic Serializers & Circular Reference Guards** (`PropertySerializerMap` dynamic resolution, direct self-reference cycle detection).

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
Here is the source code of BeanPropertyWriter.java:

```java
<SOURCE_CODE_OF_BeanPropertyWriter.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J JACKSONDATABIND-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: JacksonDatabind-1 (Issue 223)
- Triggering Test: com.fasterxml.jackson.databind.struct.TestPOJOAsArray::testNullColumn
- Failure Message: junit.framework.ComparisonFailure: expected:<[null,[]"bar"]> but was:<[null,[null,]"bar"]>
- Defect Scope & Root Cause:
  In `BeanPropertyWriter.java`, the `serializeAsColumn` method:
  ```java
  public void serializeAsColumn(Object bean, JsonGenerator jgen, SerializerProvider prov)
      throws Exception
  {
      Object value = get(bean);
      if (value == null) { // nulls need specialized handling
          if (_nullSerializer != null) {
              _nullSerializer.serialize(null, jgen, prov);
          } else { // can NOT suppress entries in tabular output
              jgen.writeNull();
          }
      }
      // otherwise find serializer to use
      JsonSerializer<Object> ser = _serializer;
      ...
  ```
  FAILS to `return;` after handling the `value == null` branch!
  In contrast, `serializeAsField` has an explicit `return;` after null handling.
  Because `return;` is missing in `serializeAsColumn`, when a POJO property has a `null` value in POJO-as-Array mode (tabular column mode):
  1. If `_serializer != null`, it writes `null` via `jgen.writeNull()`, then continues down and calls `ser.serialize(value, jgen, prov)`, producing a DUPLICATE null element (`[null, null, "bar"]` instead of `[null, "bar"]`)!
  2. If `_serializer == null`, it calls `value.getClass()`, which throws a `NullPointerException`!

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testSerializeAsColumn_NullValue_NoDuplicateNull_Issue223()`) that:
1. Defines a POJO configured for array format with `@JsonFormat(shape = JsonFormat.Shape.ARRAY)` or `@JsonPropertyOrder`:
   ```java
   @JsonFormat(shape = JsonFormat.Shape.ARRAY)
   @JsonPropertyOrder({"first", "second"})
   static class PojoAsArray {
       public String first;
       public String second;
       public PojoAsArray(String first, String second) {
           this.first = first;
           this.second = second;
       }
   }
   ```
2. Instantiates `PojoAsArray` where `first = null` and `second = "bar"`.
3. Serializes the instance using `new ObjectMapper().writeValueAsString(pojo)`.
4. Asserts that the output JSON is exactly `[null,"bar"]` and DOES NOT contain duplicate nulls like `[null,null,"bar"]`.
This test MUST fail on the defective version (where it outputs `[null,null,"bar"]` or throws NPE) and pass on the fixed version!

Generate the complete JUnit 4 test class BeanPropertyWriterGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
