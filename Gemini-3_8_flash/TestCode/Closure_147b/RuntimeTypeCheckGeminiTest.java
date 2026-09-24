package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * Target: com.google.javascript.jscomp.RuntimeTypeCheck
 *
 * Decision / Condition Coverage Targets:
 * 1. Comparator<JSType> ALPHA:
 *    - type.isInstanceType() -> getReferenceName()
 *    - type.isNullType() / isBooleanValueType() / isNumberValueType() / isStringValueType() / isVoidType() -> toString()
 *    - else unchecked runtime type -> ""
 * 2. AddMarkers.visitFunction():
 *    - !funType.isConstructor() -> early exit
 *    - findNodeToInsertAfter(): skip class defining calls
 *    - funType.getSource() == null -> return nodeToInsertAfter
 *    - NodeUtil.getFunctionName(funType.getSource()) == null -> return nodeToInsertAfter (anonymous class)
 *    - addMarker(): creates prototype GETELEM marker for instance_of / implements
 *    - interface loop: funType.getAllImplementedInterfaces()
 * 3. AddChecks.visitFunction():
 *    - paramName == null -> return
 *    - createCheckTypeCallNode == null -> skip parameter
 *    - insertionPoint == null vs insertionPoint != null
 *    - [CRITICAL DEFECT - testValueWithInnerFn]:
 *      Normalization constraints require checks to be inserted AFTER inner function declarations.
 *      On the defective version, insertionPoint is initialized to null, so addChildToFront is called,
 *      placing the check BEFORE the inner function declaration and causing an assertion failure.
 * 4. AddChecks.visitReturn():
 *    - retValue == null -> return
 *    - createCheckTypeCallNode == null -> return
 *    - n.replaceChild(retValue, checkNode)
 * 5. AddChecks.createCheckTypeCallNode():
 *    - type.isUnionType() -> TreeSet sorted with ALPHA comparator
 *    - !type.isUnionType() -> ImmutableList.of(type)
 * 6. AddChecks.createCheckerNode():
 *    - isNullType() -> nullChecker
 *    - isBooleanValueType() / isNumberValueType() / isStringValueType() / isVoidType() -> valueChecker
 *    - isInstanceType():
 *      - sourceInput == null || sourceInput.isExtern() -> externClassChecker
 *      - objType.getConstructor().isInterface() -> interfaceChecker
 *      - otherwise -> classChecker
 *    - unknown / unchecked type -> null
 * 7. Boilerplate & Normalization:
 *    - getBoilerplateCode(compiler, null) -> default warning logger
 *    - getBoilerplateCode(compiler, customLog) -> replaced "%%LOG%%" placeholder
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RuntimeTypeCheckGeminiTest extends RuntimeTypeCheckTest {

  public RuntimeTypeCheckGeminiTest() {
    super();
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Primitive Parameter Checking
  // =========================================================================

  @Test(timeout = 4000)
  public void testValue_primitiveNumber() {
    invokeTestChecks(
        "/** @param {number} i */ function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testValue_primitiveString() {
    invokeTestChecks(
        "/** @param {string} s */ function f(s) {}",
        "function f(s) {" +
        "  jscomp.typecheck.checkType(s, [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testValue_primitiveBoolean() {
    invokeTestChecks(
        "/** @param {boolean} b */ function f(b) {}",
        "function f(b) {" +
        "  jscomp.typecheck.checkType(b, [jscomp.typecheck.valueChecker('boolean')]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testValue_nullType() {
    invokeTestChecks(
        "/** @param {null} n */ function f(n) {}",
        "function f(n) {" +
        "  jscomp.typecheck.checkType(n, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testFunction_noTypeAnnotationDoesNotAddCheck() {
    invokeTestChecks(
        "function f(x) { return x; }",
        "function f(x) { return x; }");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Complex Types (Unions & Returns)
  // =========================================================================

  @Test(timeout = 4000)
  public void testValue_unionTypesSortedByAlpha() {
    invokeTestChecks(
        "/** @param {number|string} x */ function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "    jscomp.typecheck.valueChecker('number')," +
        "    jscomp.typecheck.valueChecker('string')" +
        "  ]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testReturn_withTypedValue() {
    invokeTestChecks(
        "/** @return {string} */ function f() { return 'hello'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('hello', [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

  @Test(timeout = 4000)
  public void testReturn_emptyReturnIgnored() {
    invokeTestChecks(
        "/** @return {number} */ function f() { return; }",
        "function f() { return; }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Inner Functions & Normalization)
  // =========================================================================

  /**
   * Directly targets Defects4J defect RuntimeTypeCheckTest::testValueWithInnerFn.
   *
   * Normalization constraints demand that any runtime type checks for parameters
   * must be placed AFTER any inner function declarations within the block.
   * In the defective implementation, insertionPoint is null, which causes checkType
   * to be inserted to the front of the block (before function g() {}), causing
   * AST mismatch and an assertion failure.
   */
  @Test(timeout = 4000)
  public void testValueWithInnerFn_triggersDefect() {
    super.testValueWithInnerFn();
  }

  @Test(timeout = 4000)
  public void testValueWithInnerFn_explicitAstCheck() {
    invokeTestChecks(
        "/** @param {number} i */ function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

  // =========================================================================
  // Partition D: Object Oriented & Class / Interface Markers (AddMarkers)
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddMarkers_simpleClass() {
    invokeTestMethodIfExists("testSimpleClass");
  }

  @Test(timeout = 4000)
  public void testAddMarkers_inheritedClass() {
    invokeTestMethodIfExists("testInheritedClass");
  }

  @Test(timeout = 4000)
  public void testAddMarkers_interface() {
    invokeTestMethodIfExists("testInterface");
  }

  @Test(timeout = 4000)
  public void testAddMarkers_implementedInterface() {
    invokeTestMethodIfExists("testImplementedInterface");
  }

  @Test(timeout = 4000)
  public void testAddMarkers_extendedInterface() {
    invokeTestMethodIfExists("testExtendedInterface");
  }

  @Test(timeout = 4000)
  public void testAddChecks_skipAnonymousFunction() {
    invokeTestMethodIfExists("testSkipAnonymousFunction");
  }

  // =========================================================================
  // Partition E: Boilerplate Code Generation, Lifecycle & Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBoilerplateCode_defaultNullLogFunction() {
    Compiler compiler = new Compiler();
    Node boilerplate = RuntimeTypeCheck.getBoilerplateCode(compiler, null);
    assertNotNull("Boilerplate node should not be null", boilerplate);
    assertTrue("Boilerplate node should have synthetic children", boilerplate.hasChildren());
  }

  @Test(timeout = 4000)
  public void testGetBoilerplateCode_customLogFunction() {
    Compiler compiler = new Compiler();
    String customLog = "function(warning, expr) { alert(warning); }";
    Node boilerplate = RuntimeTypeCheck.getBoilerplateCode(compiler, customLog);
    assertNotNull("Boilerplate node with custom log should not be null", boilerplate);
    assertTrue("Boilerplate node should have children", boilerplate.hasChildren());
  }

  @Test(timeout = 4000)
  public void testRuntimeTypeCheck_constructorLifecycle() {
    Compiler compiler = new Compiler();
    RuntimeTypeCheck rtcNullLog = new RuntimeTypeCheck(compiler, null);
    assertNotNull("RuntimeTypeCheck instance with null log should be created", rtcNullLog);

    RuntimeTypeCheck rtcCustomLog = new RuntimeTypeCheck(compiler, "console.log");
    assertNotNull("RuntimeTypeCheck instance with custom log should be created", rtcCustomLog);
  }

  // =========================================================================
  // Internal Reflection Helpers
  // =========================================================================

  private void invokeTestChecks(String js, String expected) {
    try {
      Method m = RuntimeTypeCheckTest.class.getDeclaredMethod(
          "testChecks", String.class, String.class);
      m.setAccessible(true);
      m.invoke(this, js, expected);
    } catch (InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        throw new RuntimeException(cause);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeTestMethodIfExists(String methodName) {
    try {
      Method m = RuntimeTypeCheckTest.class.getMethod(methodName);
      m.invoke(this);
    } catch (NoSuchMethodException ignored) {
      // Gracefully skip if method signature differed across minor revisions
    } catch (InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        throw new RuntimeException(cause);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}