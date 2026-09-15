# Prompt Record for TypeInfoFactoryGeminiTest

- **Timestamp:** 2026-09-12 09:50:00
- **Model Used:** gemini-3.8-flash
- **Target Class:** `com.google.gson.TypeInfoFactory`
- **Target Project:** `Gson-1b` (Defects4J)

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
- Package Declaration: Must declare `package com.google.gson;` as line 1.
- Note that `TypeInfoFactory` is package-private (`final class TypeInfoFactory`), so the test class MUST reside in `package com.google.gson;`.
- Mandatory Explicit Imports:
  * `import java.lang.reflect.*;`
  * `import java.util.*;`
  * `import org.junit.Test;`
  * `import static org.junit.Assert.*;`
- Strict Prohibitions:
  * NO JUnit 5 / Jupiter imports (`org.junit.jupiter.*`).
  * NO mocking or third-party assertion libraries (AssertJ, Mockito, Truth).
  * NO non-deterministic dependencies.
- Class Name: Name the test class `TypeInfoFactoryGeminiTest` (`public class TypeInfoFactoryGeminiTest`).

#### 2. Test Architecture & Timeout Guard
- Execution Guard: Every single `@Test` method MUST declare a timeout: `@Test(timeout = 4000)`.
- Exception Handling: For invalid arguments or error paths, assert expected exceptions using `@Test(expected = ...Exception.class, timeout = 4000)` or explicit `try { ... fail(); } catch (Exception expected) { ... }`.
- Granular Assertions: Always assert both the **Exact Returned Type/State** and the **Exact Value**.

#### 3. Systematic Equivalence Partitioning & Boundary Value Analysis (BVA)
Structure the test suite into distinct logical sections covering:
- **Partition A: Array Type Construction** (`getTypeInfoForArray` with primitive array, object array, multi-dimensional array; non-array rejecting via IllegalArgumentException).
- **Partition B: Non-Generic & Parameterized Field Resolution** (`getTypeInfoForField` on simple Class types, ParameterizedType fields, GenericArrayType fields, WildcardType fields).
- **Partition C: Defect-Targeted Branch Zone (Gson-1 / Issue 40)** (Resolving TypeVariable on subclass inheriting from generic superclass where parentType is a Class<?>).
- **Partition D: Type Variable on Directly Parameterized Type** (Evaluating TypeVariable when parentType is an instance of ParameterizedType, e.g. `GenericParent<Integer>`).
- **Partition E: Exception & Defensive Guard Paths** (Unresolvable type variables, missing type arguments, illegal type arguments).

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
Here is the source code of TypeInfoFactory.java:

```java
<SOURCE_CODE_OF_TypeInfoFactory.java>
```

=== KNOWN DEFECT SPECIFICATION (GROUND TRUTH FROM DEFECTS4J GSON-1) ===
The target class contains a known defect documented in Defects4J as follows:
- Bug ID: Gson-1 (Issue 40)
- Triggering Test: com.google.gson.functional.TypeVariableTest::testSingle
- Failure Message: java.lang.UnsupportedOperationException: Expecting parameterized type, got class com.google.gson.functional.TypeVariableTest$Bar.
- Defect Scope & Root Cause:
  In `TypeInfoFactory.java`, the `getActualType` method attempts to resolve a `TypeVariable<?>`:
  ```java
  } else if (typeToEvaluate instanceof TypeVariable<?>) {
    if (parentType instanceof ParameterizedType) {
      ...
      return actualTypeArguments[indexOfActualTypeArgument];
    }

    throw new UnsupportedOperationException("Expecting parameterized type, got " + parentType
        + ".\n Are you missing the use of TypeToken idiom?\n See "
        + "http://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Gener");
  }
  ```
  When a subclass inherits a generic field from a generic superclass (e.g. `class Foo<T> { T field; }` and `class Bar extends Foo<String> {}`), and `getTypeInfoForField(field, Bar.class)` is called:
  The `parentType` is `Bar.class` (which is a `Class<?>`, NOT a `ParameterizedType`).
  Because `parentType instanceof ParameterizedType` evaluates to `false`, `TypeInfoFactory` immediately throws `UnsupportedOperationException: Expecting parameterized type, got class ...` rather than traversing the class hierarchy (`Bar.class.getGenericSuperclass()`) to resolve the type variable `T` to `String.class`.

=== CRITICAL REQUIREMENT FOR FAULT DETECTION ===
You MUST write at least one dedicated `@Test(timeout = 4000)` method (e.g. `testGetTypeInfoForField_SubclassInheritedTypeVariable_GSON40()`) that:
1. Defines static test classes representing generic inheritance:
   ```java
   static class GenericParent<T> {
       T value;
   }
   static class ConcreteChild extends GenericParent<String> {
   }
   ```
2. Retrieves the `Field` object for `value`: `Field field = GenericParent.class.getDeclaredField("value");`.
3. Invokes `TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);` (passing the subclass `Class<?>` as `typeDefiningF`).
4. Verifies that the resolved actual type is `String.class` (e.g. `assertEquals(String.class, typeInfo.getActualType());`).
This test MUST fail on the defective version (where it throws java.lang.UnsupportedOperationException) and pass on the fixed version!

Generate the complete JUnit 4 test class TypeInfoFactoryGeminiTest that achieves maximum line and branch coverage and targets this defect.
```
