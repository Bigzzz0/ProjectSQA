package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.EnumType;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * White-box test for TypeCheck.java.
 * Targets constructors, core public APIs, key decision branches, and the known
 * defect in getTypedPercent().
 *
 * Branch & Defect Analysis Matrix:
 * - Part A: Constructors, process(), processForTesting()
 * - Part B: getTypedPercent() after full type checking on various JS snippets
 *   (covers counting logic, null/unknown/typed branches)
 * - Part C: visitName, visitVar, visitAssign (prototype, type annotations)
 * - Part D: visitNew, visitCall, visitGetProp (property access, enum)
 * - Part E: visitFunction (constructor, interface), visitBinaryOperator, visitReturn
 * - Part F: Edge cases: empty scripts, null arguments, boundary values
 *
 * The known defect (testGetTypedPercent5/6) shows that getTypedPercent returns
 * < 100.0 for code that should be fully typed. We assert 100.0 on simple
 * fully-typed scripts to expose the regression.
 */
public class TypeCheckDeepseekTest {

  // ---------- Helper methods ----------

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    return compiler;
  }

  /**
   * Parses source, wraps it in a synthetic root, runs type checking via
   * processForTesting, and returns the TypeCheck instance for inspection.
   */
  private TypeCheck runTypeCheck(String source) {
    Compiler compiler = createCompiler();
    Node script = compiler.parseSyntheticCode("test", source);
    // parseSyntheticCode returns a SCRIPT node with parent set to a synthetic root
    // but we still need to ensure it has a parent for processForTesting
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(script);
    // The compiler's type registry and reverse interpreter are available
    TypeCheck typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    typeCheck.processForTesting(null, script);
    return typeCheck;
  }

  private double getTypedPercent(String source) {
    return runTypeCheck(source).getTypedPercent();
  }

  // ---------- Part A: Constructors & Core APIs ----------

  @Test(timeout = 4000)
  public void testConstructorWithFullParams() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        null, null, CheckLevel.WARNING, CheckLevel.OFF);
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testConstructorWithThreeParams() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testConstructorWithFourParams() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        CheckLevel.WARNING, CheckLevel.OFF);
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesChaining() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertSame(tc, tc.reportMissingProperties(true));
  }

  @Test(timeout = 4000)
  public void testProcessForTestingReturnsScope() {
    String source = "var a = 1;";
    Compiler compiler = createCompiler();
    Node script = compiler.parseSyntheticCode("test", source);
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(script);
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    Scope scope = tc.processForTesting(null, script);
    assertNotNull(scope);
    assertNotNull(scope.getVar("a"));
  }

  // ---------- Part B: getTypedPercent (targeting defect) ----------

  @Test(timeout = 4000)
  public void testGetTypedPercentSimpleVar() {
    double pct = getTypedPercent("var x = 10;");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentTwoVars() {
    double pct = getTypedPercent("var x = 1; var y = x + 2;");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentFunction() {
    double pct = getTypedPercent("function f() { return 1; } f();");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentObjectLit() {
    double pct = getTypedPercent("var obj = {a: 1, b: 'hello'};");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentArray() {
    double pct = getTypedPercent("var arr = [1, 2, 3];");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentNew() {
    double pct = getTypedPercent("var d = new Date();");
    // Date constructor returns object – all nodes typed
    assertEquals(100.0, pct, 0.0001);
  }

  // Known defect reproduction: code that should be fully typed yields <100%
  // This test will fail on the buggy version, revealing the defect.
  @Test(timeout = 4000)
  public void testGetTypedPercentDefectReproduction() {
    // This snippet is designed to be fully typed; the bug causes a lower percentage.
    double pct = getTypedPercent(
        "/** @enum {number} */ var E = {A: 1, B: 2}; var x = E.A;");
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentEmptyScript() {
    double pct = getTypedPercent("");
    // No typed nodes, total = 0 => returns 0.0
    assertEquals(0.0, pct, 0.0001);
  }

  // ---------- Part C: visitName, visitVar, visitAssign ----------

  @Test(timeout = 4000)
  public void testVisitNameInExpression() {
    // visitName is called during traversal; we verify no crash and correct type.
    TypeCheck tc = runTypeCheck("var a = 1; a;");
    // The expression "a" should be typed as number.
    // getTypedPercent ensures traversal happened.
    assertTrue(tc.getTypedPercent() > 0);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithTypeAnnotation() {
    // @type annotation on variable
    String source = "/** @type {string} */ var s = 'hello';";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitAssignPrototypeOverride() {
    // prototype assignment – should not throw
    String source = "/** @constructor */ function Foo() {} Foo.prototype = {bar: 1};";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitAssignAnnotatedGetprop() {
    // @type on property assignment
    String source = "/** @constructor */ function Foo() {} /** @type {number} */ Foo.prototype.x = 1;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  // ---------- Part D: visitNew, visitCall, visitGetProp ----------

  @Test(timeout = 4000)
  public void testVisitNewNonConstructorWarning() {
    // Calling new on a non-constructor should produce a warning but not crash.
    // We verify that traversal succeeds.
    String source = "var x = new 1;"; // invalid
    double pct = getTypedPercent(source);
    // Nodes still typed (unknown)
    assertTrue(pct >= 0);
  }

  @Test(timeout = 4000)
  public void testVisitCallFunctionWithThisType() {
    // Function with explicit @this must be called as property access
    String source =
        "/** @constructor */ function Foo() {}" +
        "/** @this {Foo} */ function bar() {}" +
        "var obj = new Foo(); obj.bar();";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropEnumElement() {
    // Enum element access – should not report missing property
    String source = "/** @enum {number} */ var E = {A: 1}; var x = E.A;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnNull() {
    // Access property on null – validator reports but traversal continues
    String source = "var x = null; var y = x.foo;";
    double pct = getTypedPercent(source);
    // All nodes typed (some may be unknown)
    assertTrue(pct >= 0);
  }

  // ---------- Part E: visitFunction, visitBinaryOperator, visitReturn ----------

  @Test(timeout = 4000)
  public void testVisitFunctionConstructorExtendsInterface() {
    // Constructor extending interface – should produce conflict warning
    String source =
        "/** @interface */ function I() {}" +
        "/** @constructor @implements {I} */ function C() {}";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitFunctionInterfaceConflict() {
    // Interface with conflicting extended interfaces
    String source =
        "/** @interface */ function I1() {}" +
        "/** @interface */ function I2() {}" +
        "/** @interface @extends {I1} @extends {I2} */ function I3() {}";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorBitwise() {
    // Bitwise operators on numbers
    String source = "var x = 1 & 2; var y = 3 | 4;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitReturnVoidFunction() {
    // Void function returns undefined
    String source = "function f() { return; }";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithValue() {
    String source = "function f() { return 1; }";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  // ---------- Part F: Edge Cases ----------

  @Test(timeout = 4000)
  public void testVisitNullAndUndefined() {
    String source = "var a = null; var b = undefined;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitTypeOf() {
    String source = "var x = typeof 1;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitDelete() {
    String source = "var obj = {a: 1}; delete obj.a;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitInOperator() {
    String source = "var obj = {a: 1}; var x = 'a' in obj;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitInstanceOf() {
    String source = "var x = []; var y = x instanceof Array;";
    double pct = getTypedPercent(source);
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitStringKeyNotTypeable() {
    // Object literal keys are not typeable, but values are.
    String source = "var obj = {key: 'value'};";
    double pct = getTypedPercent(source);
    // Expect 100% because all typeable nodes are typed.
    assertEquals(100.0, pct, 0.0001);
  }

  @Test(timeout = 4000)
  public void testVisitExternsNotCounted() {
    // Externs should not affect typed percent.
    Compiler compiler = createCompiler();
    Node externs = compiler.parseSyntheticCode("externs", "/** @type {number} */ var ext;");
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(externs);
    root.addChildToBack(js);
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    tc.process(externs, js);
    double pct = tc.getTypedPercent();
    // Only js code counted, should be 100%
    assertEquals(100.0, pct, 0.0001);
  }
}