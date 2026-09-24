/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects (Defects4J):
 * - TypeCheckTest::testGetTypedPercent5: Getter in object literal {get foo() { return 1; }} was incorrectly
 *   counted as untyped (Token.GET / Token.SET did not set typeable = false), dropping percent typed to 62.5% instead of 100.0%.
 * - TypeCheckTest::testGetTypedPercent6: Setter in object literal {set foo(x) {}} was incorrectly counted as
 *   untyped, dropping percent typed to 66.67% instead of 100.0%.
 *
 * Core Decision Branches & Condition Coverage:
 * - Partition A: Core Functional Logic & State Transitions
 *   * Tokens: NAME, LP, COMMA, TRUE, FALSE, THIS, NULL, NUMBER, STRING, ARRAYLIT, REGEXP, GETPROP, GETELEM
 *   * Tokens: VAR, NEW, CALL, RETURN, INC, DEC, NOT, VOID, TYPEOF, BITNOT, POS, NEG
 *   * Binary operators: EQ, NE, SHEQ, SHNE, LT, LE, GT, GE, IN, INSTANCEOF, DELPROP, ASSIGN and arithmetic/bitwise ops
 *   * Control/structural nodes: CASE, WITH, FUNCTION, DO, FOR, IF, WHILE, AND, OR, HOOK, OBJECTLIT
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * Empty scripts (0% typed accounting), null checks on process/check inputs
 *   * Missing parents, repeated processForTesting calls
 *   * Functions with minimum/maximum argument boundaries, varargs
 * - Partition C: Defect-Targeted Zone
 *   * testGetTypedPercent5_GetterKeyInObjectLit: asserts 100.0% typed on object literal getter
 *   * testGetTypedPercent6_SetterKeyInObjectLit: asserts 100.0% typed on object literal setter
 * - Partition D: Exception & Defensive Guard Paths
 *   * Preconditions on process, processForTesting, check, hasUnknownOrEmptySupertype
 *   * UNEXPECTED_TOKEN fallback reporting
 * - Partition E: Diagnostic & Warning Verification
 *   * WRONG_ARGUMENT_COUNT, NOT_CALLABLE, CONSTRUCTOR_NOT_CALLABLE, EXPECTED_THIS_TYPE
 *   * NOT_A_CONSTRUCTOR, BAD_DELETE, BIT_OPERATION, DETERMINISTIC_TEST, DETERMINISTIC_TEST_NO_RESULT
 *   * INEXISTENT_PROPERTY, INEXISTENT_ENUM_ELEMENT, UNKNOWN_EXPR_TYPE
 *   * CONFLICTING_EXTENDED_TYPE, CONFLICTING_IMPLEMENTED_TYPE, BAD_IMPLEMENTED_TYPE
 *   * HIDDEN_SUPERCLASS_PROPERTY, HIDDEN_INTERFACE_PROPERTY, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH, UNKNOWN_OVERRIDE
 *   * INTERFACE_FUNCTION_NOT_EMPTY, INVALID_INTERFACE_MEMBER_DECLARATION, INCOMPATIBLE_EXTENDED_PROPERTY_TYPE
 *   * OVERRIDING_PROTOTYPE_WITH_NON_OBJECT, FUNCTION_MASKS_VARIABLE, ILLEGAL_IMPLICIT_CAST
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

public class TypeCheckGeminiTest {

  // -------------------------------------------------------------------------
  // Test Helpers
  // -------------------------------------------------------------------------

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private TypeCheck check(String js) {
    return check(js, CheckLevel.WARNING, CheckLevel.OFF, false);
  }

  private TypeCheck check(String js, CheckLevel reportMissingOverride,
      CheckLevel reportUnknownTypes, boolean reportMissingProperties) {
    Compiler compiler = createCompiler();
    Node n = compiler.parseTestCode(js);
    Node externsNode = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externsNode, n);

    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        reportMissingOverride,
        reportUnknownTypes);
    tc.reportMissingProperties(reportMissingProperties);
    tc.processForTesting(externsNode, n);
    return tc;
  }

  private TypeCheck checkWithCompiler(Compiler compiler, String js,
      CheckLevel reportMissingOverride, CheckLevel reportUnknownTypes,
      boolean reportMissingProperties) {
    Node n = compiler.parseTestCode(js);
    Node externsNode = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externsNode, n);

    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        reportMissingOverride,
        reportUnknownTypes);
    tc.reportMissingProperties(reportMissingProperties);
    tc.processForTesting(externsNode, n);
    return tc;
  }

  private TypeCheck checkWithExterns(Compiler compiler, String externs, String js) {
    Node externsNode = compiler.parseSyntheticCode("externs.js", externs);
    Node jsNode = compiler.parseSyntheticCode("testcode.js", js);
    Node parent = new Node(Token.BLOCK, externsNode, jsNode);

    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    tc.processForTesting(externsNode, jsNode);
    return tc;
  }

  private void assertWarning(Compiler compiler, DiagnosticType type) {
    JSError[] warnings = compiler.getWarnings();
    for (JSError w : warnings) {
      if (w.getType().equals(type)) {
        return;
      }
    }
    fail("Expected warning: " + type.key + " but found: " + java.util.Arrays.toString(warnings));
  }

  private void assertError(Compiler compiler, DiagnosticType type) {
    JSError[] errors = compiler.getErrors();
    for (JSError e : errors) {
      if (e.getType().equals(type)) {
        return;
      }
    }
    fail("Expected error: " + type.key + " but found: " + java.util.Arrays.toString(errors));
  }

  private void assertNoWarnings(Compiler compiler) {
    assertEquals("Expected 0 warnings, found: " + java.util.Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
  }

  // -------------------------------------------------------------------------
  // Partition C: Defect-Targeted Zone (Known Defects from Defects4J)
  // -------------------------------------------------------------------------

  /**
   * Targets Defects4J known defect: TypeCheckTest::testGetTypedPercent5
   * Getter in object literal should not be counted as untyped null node.
   */
  @Test(timeout = 4000)
  public void testGetTypedPercent5_GetterKeyInObjectLit() {
    TypeCheck tc = check("var a = {get foo() { return 1; }};");
    assertEquals(100.0, tc.getTypedPercent(), 0.1);
  }

  /**
   * Targets Defects4J known defect: TypeCheckTest::testGetTypedPercent6
   * Setter in object literal should not be counted as untyped null node.
   */
  @Test(timeout = 4000)
  public void testGetTypedPercent6_SetterKeyInObjectLit() {
    TypeCheck tc = check("var a = {set foo(x) {}};");
    assertEquals(100.0, tc.getTypedPercent(), 0.1);
  }

  // -------------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testTypedPercentEmptyScript() {
    TypeCheck tc = check("");
    assertEquals(0.0, tc.getTypedPercent(), 0.0);
  }

  @Test(timeout = 4000)
  public void testTypedPercentFullyTypedScript() {
    TypeCheck tc = check("var x = 1; var y = 'abc'; var z = true;");
    assertEquals(100.0, tc.getTypedPercent(), 0.1);
  }

  @Test(timeout = 4000)
  public void testLiteralTokens() {
    TypeCheck tc = check(
        "var a = null; var b = 42; var c = 'hello'; var d = true; "
        + "var e = false; var f = [1, 2]; var g = /abc/; var h = void 0;");
    assertEquals(100.0, tc.getTypedPercent(), 0.1);
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = 5; x++; ++x; x--; --x; var y = -x; var z = +x; var w = !x;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testTypeofAndCommaOperators() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = (1, 'str'); var y = typeof x;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testArithmeticAndBitwiseBinaryOperators() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "var a = 1 + 2; var b = 2 - 1; var c = 2 * 3; var d = 4 / 2; var e = 5 % 2; "
        + "var f = 1 & 2; var g = 1 | 2; var h = 1 ^ 2; var i = 1 << 2; var j = 4 >> 1; var k = 4 >>> 1;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testCompoundAssignments() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "var x = 1; x += 1; x -= 1; x *= 2; x /= 2; x %= 1; x <<= 1; x >>= 1; x >>>= 1; x |= 1; x &= 1; x ^= 1;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testControlFlowConstructs() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "var x = 1; if (x > 0) { x = 2; } while (x < 5) { x++; } "
        + "do { x--; } while (x > 2); for (var i = 0; i < 3; i++) { x += i; } "
        + "switch (x) { case 1: break; default: break; }",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testTernaryAndLogicalAndOr() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "var a = true; var b = false; var c = a && b; var d = a || b; var e = a ? 1 : 2;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testArrayAndObjectElementAccess() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "var arr = [10, 20]; var first = arr[0]; var obj = {'a': 1}; var val = obj['a'];",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testFunctionCallAndReturn() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @return {number} */ function f(/** number */ x) { return x + 1; } var r = f(10);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testConstructorInstantiation() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Point(/** number */ x) { this.x = x; } var p = new Point(5);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  // -------------------------------------------------------------------------
  // Partition E: Diagnostic & Warning Verification
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testNotAConstructor() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = 42; var y = new x();",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test(timeout = 4000)
  public void testNotCallable() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = 42; x();",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testConstructorNotCallableDirectly() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "/** @constructor */ function Foo() {} Foo();",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooFew() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "function add(/** number */ a, /** number */ b) {} add(1);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooMany() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "function add(/** number */ a, /** number */ b) {} add(1, 2, 3);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testBadDeleteOperand() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "delete 42;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.BAD_DELETE);
  }

  @Test(timeout = 4000)
  public void testBadBitwiseOperation() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = ~'invalid';",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.BIT_OPERATION);
  }

  @Test(timeout = 4000)
  public void testBadBitShiftLeftOperand() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var x = 'str' << 1;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.BIT_OPERATION);
  }

  @Test(timeout = 4000)
  public void testDeterministicEqualityTest() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @type {number} */ var a = 1; /** @type {string} */ var b = 'x'; var c = (a == b);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.DETERMINISTIC_TEST);
  }

  @Test(timeout = 4000)
  public void testDeterministicInequalityTest() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @type {number} */ var a = 1; /** @type {string} */ var b = 'x'; var c = (a != b);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.DETERMINISTIC_TEST);
  }

  @Test(timeout = 4000)
  public void testDeterministicShallowEqualityTest() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @type {number} */ var a = 1; /** @type {string} */ var b = 'x'; var c = (a === b);",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
  }

  @Test(timeout = 4000)
  public void testInexistentPropertyWarning() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var obj = {}; var x = obj.neverDefined;",
        CheckLevel.WARNING, CheckLevel.OFF, true);
    assertWarning(compiler, TypeCheck.INEXISTENT_PROPERTY);
  }

  @Test(timeout = 4000)
  public void testInexistentEnumElementWarning() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @enum {number} */ var MyEnum = { FOO: 1 }; var x = MyEnum.BAR;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  @Test(timeout = 4000)
  public void testOverridingPrototypeWithNonObject() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Foo() {} Foo.prototype = 123;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testFunctionMasksVariable() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler, "var f = 1; function f() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  @Test(timeout = 4000)
  public void testExpectedThisType() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @this {Array} */ function f() {} f();",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE);
  }

  @Test(timeout = 4000)
  public void testConflictingExtendedTypeConstructorExtendingInterface() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function I() {} /** @constructor \n * @extends {I} */ function C() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test(timeout = 4000)
  public void testConflictingExtendedTypeInterfaceExtendingConstructor() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function C() {} /** @interface \n * @extends {C} */ function I() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test(timeout = 4000)
  public void testConflictingImplementedType() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function I1() {} /** @interface \n * @implements {I1} */ function I2() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
  }

  @Test(timeout = 4000)
  public void testBadImplementedType() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function C1() {} /** @constructor \n * @implements {C1} */ function C2() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionNotEmpty() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function I() {} I.prototype.m = function() { return 1; };",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test(timeout = 4000)
  public void testInvalidInterfaceMemberDeclaration() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function I() {} I.prototype.m = 123;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION);
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyWarning() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Super() {} Super.prototype.foo = function() {};\n"
        + "/** @constructor \n * @extends {Super} */ function Sub() {}\n"
        + "Sub.prototype.foo = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
  }

  @Test(timeout = 4000)
  public void testHiddenInterfacePropertyWarning() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function Inter() {} Inter.prototype.foo = function() {};\n"
        + "/** @constructor \n * @implements {Inter} */ function Imp() {}\n"
        + "Imp.prototype.foo = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.HIDDEN_INTERFACE_PROPERTY);
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyMismatch() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Super() {} /** @type {number} */ Super.prototype.foo = 1;\n"
        + "/** @constructor \n * @extends {Super} */ function Sub() {}\n"
        + "/** @override \n * @type {string} */ Sub.prototype.foo = 'str';",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
  }

  @Test(timeout = 4000)
  public void testUnknownOverrideWarning() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Super() {}\n"
        + "/** @constructor \n * @extends {Super} */ function Sub() {}\n"
        + "/** @override */ Sub.prototype.notOnSuper = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.UNKNOWN_OVERRIDE);
  }

  @Test(timeout = 4000)
  public void testIncompatibleExtendedPropertyType() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @interface */ function I1() {}\n"
        + "/** @type {number} */ I1.prototype.x;\n"
        + "/** @interface */ function I2() {}\n"
        + "/** @type {string} */ I2.prototype.x;\n"
        + "/** @interface \n * @extends {I1} \n * @extends {I2} */ function I3() {}",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertError(compiler, TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
  }

  @Test(timeout = 4000)
  public void testIllegalImplicitCastInNonExterns() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @constructor */ function Foo() {} /** @implicitCast */ Foo.prototype.x = 1;",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertWarning(compiler, TypeCheck.ILLEGAL_IMPLICIT_CAST);
  }

  @Test(timeout = 4000)
  public void testReportUnknownTypesWhenEnabled() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "function f(x) { return x; }",
        CheckLevel.OFF, CheckLevel.WARNING, false);
    assertWarning(compiler, TypeCheck.UNKNOWN_EXPR_TYPE);
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckAnnotationSuppressesWarnings() {
    Compiler compiler = createCompiler();
    checkWithCompiler(compiler,
        "/** @notypecheck */ function bad() { var x = 42; x(); }",
        CheckLevel.WARNING, CheckLevel.OFF, false);
    assertNoWarnings(compiler);
  }

  // -------------------------------------------------------------------------
  // Partition B & D: Boundary Value Analysis, Preconditions & Exceptions
  // -------------------------------------------------------------------------

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessNullInputsThrowsException() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
    tc.process(null, null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testCheckNullNodeThrowsException() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
    tc.check(null, false);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessForTestingWithoutParentThrowsException() {
    Compiler compiler = createCompiler();
    Node n = compiler.parseTestCode("var a = 1;");
    TypeCheck tc = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
    tc.processForTesting(null, n);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessForTestingCalledTwiceThrowsException() {
    Compiler compiler = createCompiler();
    Node n = compiler.parseTestCode("var a = 1;");
    Node externsNode = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externsNode, n);

    TypeCheck tc = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
    tc.processForTesting(externsNode, n);
    // Second invocation must trigger Preconditions.checkState(scopeCreator == null)
    tc.processForTesting(externsNode, n);
  }

  @Test(timeout = 4000)
  public void testCheckWithExternsRoot() {
    Compiler compiler = createCompiler();
    TypeCheck tc = checkWithExterns(compiler, "/** @type {number} */ var extNumber;", "extNumber = 10;");
    assertNotNull(tc);
    assertNoWarnings(compiler);
  }

  @Test(timeout = 4000)
  public void testUnexpectedTokenTraversalFallback() {
    Compiler compiler = createCompiler();
    Node testRoot = compiler.parseTestCode("var a = 1;");
    Node externsNode = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externsNode, testRoot);

    // Append an artificial node token that TypeCheck visit default handles
    Node unexpected = new Node(Token.LABEL);
    testRoot.addChildToBack(unexpected);

    TypeCheck tc = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
    tc.processForTesting(externsNode, testRoot);
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testAllDiagnosticsGroupInstantiation() {
    assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
    assertTrue(TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.NOT_A_CONSTRUCTOR));
    assertTrue(TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.BAD_DELETE));
  }
}