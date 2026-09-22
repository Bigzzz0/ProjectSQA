# Native IPO Architecture and Engineering Design

This document details the architectural principles, component designs, and engineering solutions implemented in the **Native In-Parameter-Order (IPO) Combinatorial Test Generation Pipeline**.

---

## 1. Architectural Principles

The pipeline is built on strict scientific combinatorial testing standards:

1. **Deterministic Covering Arrays**: Pure IPO 2-way horizontal and vertical growth guaranteeing 100% pair coverage across all input factors.
2. **Fixed-Version Oracle**: Empirical output extraction from the bug-fixed reference implementation (`*f`), verified through execution.
3. **Fault Isolation (Partial-Class Publication)**: A defect or timeout in one callable within a class must never cause valid sister methods in that class to be discarded.
4. **Platform Independence**: Canonical JSON hashing and line-ending normalization (`\r\n` to `\n`) ensure bit-exact reproducibility across Windows host systems and Linux Docker containers.
5. **No Dummy Inputs**: Inputs must belong to verifiable semantic domains; null is never generated indiscriminately.

---

## 2. Core Components

```
                +------------------------------------+
                |  Defects4J Catalog & Source Code   |
                +-----------------+------------------+
                                  |
                                  v
                        [ JavaAstParser ]
                     (Compiler Tree API AST)
                                  |
                                  v
                    [ ConstructionPlanner ]
                 (Receivers, Factories, Types)
                                  |
                                  v
                   [ Combinatorial IPO Engine ]
                    (2-way Pairwise Generation)
                                  |
                                  v
                   [ Fixed-Version Oracle Runner ]
                 (Double-run, Public Exception Unwinding)
                                  |
                                  v
                   [ Partial-Class Publication ]
                (Isolated Try-Except per Callable)
                                  |
                                  v
                   [ JUnit 4 Suite Synthesizer ]
                 (@Test(timeout = 4000), Assertions)
                                  |
                                  v
                   [ Defects4J Verification Gate ]
                  (Compile + Test on Fixed Version)
                                  |
                                  v
                +-----------------+------------------+
                | TestCode/<Project>_<BugID>b/ ...   |
                | Results/verified_suites_manifest   |
                +------------------------------------+
```

### 2.1 Java AST Parser (`JavaAstParser.java` & `java_parser.py`)
- **Direct Java Compiler Tree API**: Uses standard JDK `com.sun.source.tree.*` without heavy external dependencies.
- **Accurate Metadata Extraction**: Extracts constructors, methods, annotations (e.g. `@Nullable`), modifiers (public, protected, package, private, static, abstract), parameter types with nested generics, and explicit throws clauses.
- **Fallback**: Retains regex parsing for non-standard environments or unparseable source fragments.

### 2.2 Construction Planner (`construction_planner.py`)
Resolves how an instance method's receiver object (`this`) can be constructed deterministically:
1. **Implicit Zero-Arg Constructor**: When no constructor is declared in a concrete class.
2. **Explicit Zero-Arg Constructor**: Declared public 0-arg constructor.
3. **Factorized Constructor**: Parameterized constructor whose parameters are treated as factors in the combinatorial model.
4. **Static Factory Methods**: Methods returning the target type (e.g., `valueOf`, `getInstance`).
5. **Concrete Subtypes**: Mapping known abstract classes or interfaces to concrete implementations (e.g., JFreeChart category renderers, Math probability distributions, Jsoup nodes).

### 2.3 Combinatorial In-Parameter-Order Engine (`ipo.py`)
- Implements deterministic Lei & Tai 2-way covering array construction.
- Horizontal growth followed by vertical growth for optimal covering array reduction.
- Independently verified by `verify_pair_coverage.py` requiring 100.0% pair coverage before oracle generation.
- Handles methods with $\ge 2$ factors (pairwise combinations), 1 factor (exhaustive domain coverage), and 0 factors (state checks).

### 2.4 Exception Type Sanitization & Oracle Hardening (`fixed_version_oracle.py`)
- **Anonymous Inner Class Unwinding**: In Math-99, `MathUtils.lcm` threw an anonymous inner class `org.apache.commons.math.MathRuntimeException$1`. Java syntax forbids catching anonymous inner classes by name (`catch (MathRuntimeException$1 e)` does not compile). The oracle now unwinds to the public superclass using `getPublicExceptionType(Throwable error)`:
  ```java
  private static Class<?> getPublicExceptionType(Throwable error) {
      Class<?> current = error.getClass();
      while (current != null && (current.isAnonymousClass() || current.isLocalClass() || !Modifier.isPublic(current.getModifiers()))) {
          current = current.getSuperclass();
      }
      return current != null ? current : Exception.class;
  }
  ```
- **Execution Timeout & Error Reporting**: Distinguishes between OS errors (e.g. out of memory, missing JVM) and execution timeouts (e.g., long-running loops), capturing full stderr and stack traces.

### 2.5 Partial-Class Publication (`all_class_pipeline.py`)
- In earlier versions, `generate_class` looped over all ready callables in a single try block. If callable $N$ failed, callables $1 \dots N-1$ were dropped.
- The new architecture isolates each callable's oracle execution:
  - If a callable times out or throws an unhandled exception, it is logged to `method_error.json` and added to `failed_methods`.
  - Sister callables that succeeded are retained.
  - As long as $\ge 1$ callable passes verification, a clean JUnit test suite containing only the verified callables is synthesized and published.
  - Demonstrated on `Math-99` (21 methods verified, 513 tests passing; 1 timed-out method isolated) and `Math-92` (2 methods verified; timeout isolated).

---

## 3. Results Management and Manifests

All outputs reside in `Results/`:
- `inventory.json`: Audit status for all 1,070 modified class instances across the 854 bug targets. Identifies 247 `AUTO_READY` classes.
- `verified_suites_manifest.json`: Single source of truth for all published, fixed-verified test suites. Contains SHA-256 hashes, toolchain hashes, and method-level pair coverage statistics.
- `generation_manifest.json`: Detailed logs from generation runs.
- `routing_manifest.json`: Structured backlog identifying missing adapters and recommending target approaches for Member 4 or alternative generators.
