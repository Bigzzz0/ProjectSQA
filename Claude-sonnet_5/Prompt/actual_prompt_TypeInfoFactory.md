# Prompt Record for TypeInfoFactoryClaudeTest

- **Timestamp:** 2026-09-12 09:50:00
- **Model Used:** claude-sonnet-5
- **Target Class:** `com.google.gson.TypeInfoFactory`
- **Target Project:** `Gson-1b` (Defects4J)

---

## System Prompt
```text
You are an expert Software Quality Assurance (SQA) Engineer specializing in Java Unit Testing, Defect Localization, and Mutation Testing using the Defects4J benchmark.

I have attached a target Java source file: TypeInfoFactory.java from Google Gson. Your task is to analyze the attached file and generate a high-coverage, fault-detecting JUnit 4 test suite.

### Rules & Instructions:
1. Automated Package & Class Resolution:
   - Must declare `package com.google.gson;` as line 1.
   - Note that `TypeInfoFactory` is package-private (`final class TypeInfoFactory`), so the test class MUST reside in `package com.google.gson;`.
   - Name the test class `TypeInfoFactoryClaudeTest` (declared as `public class TypeInfoFactoryClaudeTest`).

2. Framework & Environment:
   - Must be strictly compatible with Java 8 and JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`, `@Before`, etc.).
   - DO NOT use JUnit 5 (Jupiter) or third-party assertion libraries (AssertJ, Mockito, Truth).
   - Use standard imports:
     * `import java.lang.reflect.*;`
     * `import java.util.*;`
     * `import org.junit.Test;`
     * `import static org.junit.Assert.*;`

3. Test Architecture & Timeout Guard:
   - Every test method MUST declare a timeout: `@Test(timeout = 4000)`.
   - Maximize Line Coverage and Branch Coverage across all methods in `TypeInfoFactory`:
     * `getTypeInfoForArray(Type)`: Valid array types, non-array types (throwing IllegalArgumentException via Preconditions).
     * `getTypeInfoForField(Field, Type)`:
       - Regular non-generic fields (`Class<?>`).
       - Parameterized fields (`ParameterizedType`).
       - Generic array fields (`GenericArrayType`).
       - Wildcard type fields (`WildcardType`).
       - Type variable fields (`TypeVariable<?>`) on directly parameterized parent type vs inherited subclass (`Class<?>`).
     * Boundary conditions and exception paths.
   - For every `@Test` method, include a concise Javadoc comment specifying `@target` (method/branch), `@scenario` (input condition), and `@defectRisk` (potential bug targeted).

4. ABSOLUTE OUTPUT CONSTRAINT:
   - Output MUST contain ONLY the raw Java code enclosed within a single ```java ... ``` block.
   - DO NOT include greetings, conversational intros, explanations, conclusions, or any markdown outside the single ```java``` code block.
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

Generate the complete JUnit 4 test class TypeInfoFactoryClaudeTest that achieves maximum line and branch coverage and targets this defect.
```
