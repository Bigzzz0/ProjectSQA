/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. DEFECT ISSUE-1002 (Defects4J ground truth):
 *    - hasUnknownOrEmptySupertype() loop where ctor hierarchy resolves an ordinary function
 *      expression rather than a declared constructor/interface.
 *    - Triggered via: "/** @interface * / var I = function() {}; I.prototype.foo = function() {};"
 *      inducing IllegalStateException in Preconditions.checkState(ctor.isConstructor() || ctor.isInterface()).
 *
 * 2. Token Node Visitor Branches (visit()):
 *    - Token.CAST: type casting check on object literals vs non-object literals (expectCanCast).
 *    - Token.NAME: qualified name lookups, variable scope inference, this references.
 *    - Token.COMMA, Token.TRUE, Token.FALSE, Token.NULL, Token.NUMBER, Token.STRING, Token.THIS.
 *    - Token.ARRAYLIT, Token.REGEXP.
 *    - Token.GETPROP: struct property access, dict property access (illegal '.'), inexistent property suggestions.
 *    - Token.GETELEM: array index matching.
 *    - Token.VAR: variable assignment matching, enum aliasing check (checkEnumAlias).
 *    - Token.NEW: non-constructor instantiation warning, parameter count checking.
 *    - Token.CALL: non-callable warning, constructor-without-new warning, expected @this type.
 *    - Token.RETURN: return type validation vs declared return type (void vs value).
 *    - Token.INC, Token.DEC: number type expectation, property creation on struct instances.
 *    - Token.BITNOT, Token.POS, Token.NEG: bitwise & numeric context checks.
 *    - Token.EQ, Token.NE, Token.SHEQ, Token.SHNE: deterministic comparison warnings, typeof checks.
 *    - Token.LT, Token.LE, Token.GT, Token.GE: number context vs string comparison.
 *    - Token.IN: object requirement, struct restriction warning.
 *    - Token.INSTANCEOF: object requirement checks.
 *    - Token.ASSIGN & Binary Ops: prototype modification, struct property assignment, bitwise/arithmetic operations.
 *    - Token.DELPROP, Token.CASE, Token.WITH, Token.FOR (for-in on struct).
 *    - Token.OBJECTLIT: struct unquoted key validation, dict quoted key validation, getter/setter handling.
 *
 * 3. Inheritance & Property Validation:
 *    - checkDeclaredPropertyInheritance: @override presence, superclass and super-interface property tracking.
 *    - HIDDEN_SUPERCLASS_PROPERTY, HIDDEN_INTERFACE_PROPERTY, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH, UNKNOWN_OVERRIDE.
 *
 * 4. Control & Accounting Flags:
 *    - @notypecheck annotation section tracking (noTypeCheckSection).
 *    - getTypedPercent() calculation with 0 nodes vs typed/untyped nodes.
 *    - Process & constructor defensive preconditions.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckGeminiTest {

  private Compiler compiler;
  private TypeCheck typeCheck;

  private Node testTypes(String js) {
    return testTypes("", js);
  }

  private Node testTypes(String externs, String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    ReverseAbstractInterpreter reverseInterpreter = compiler.getReverseAbstractInterpreter();
    typeCheck = new TypeCheck(compiler, reverseInterpreter, registry, CheckLevel.WARNING);

    Node externsRoot = compiler.parseTestCode(externs);
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);
    typeCheck.processForTesting(externsRoot, jsRoot);
    return jsRoot;
  }

  private boolean hasWarning(DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasError(DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets issue 1002: IllegalStateException in TypeCheck.hasUnknownOrEmptySupertype
   * when an interface is assigned to a function expression on a variable declaration
   * and subsequently defines a prototype property.
   */
  @Test(timeout = 4000)
  public void testIssue1002() {
    testTypes(
        "/** @interface */\n" +
        "var I = function() {};\n" +
        "/** @type {function()} */\n" +
        "I.prototype.foo = function() {};\n");
    assertFalse(hasError(TypeCheck.UNEXPECTED_TOKEN));
  }

  /**
   * Targets issue 1002 variant: Subclass extending a function expression.
   */
  @Test(timeout = 4000)
  public void testIssue1002_ConstructorExtendingFunctionExpression() {
    testTypes(
        "var Super = function() {};\n" +
        "/** @constructor\n" +
        " * @extends {Super} */\n" +
        "function Sub() {}\n" +
        "Sub.prototype.foo = function() {};\n");
    assertFalse(hasError(TypeCheck.UNEXPECTED_TOKEN));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testLiteralTypesAndExpressions() {
    testTypes(
        "var a = true;\n" +
        "var b = false;\n" +
        "var c = null;\n" +
        "var d = 123.45;\n" +
        "var e = 'hello';\n" +
        "var f = [1, 2, 3];\n" +
        "var g = /abc/g;\n" +
        "var h = void 0;\n" +
        "var i = (1, 'second');\n");
    assertEquals(0, compiler.getErrorCount());
    assertTrue(typeCheck.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testTypeCasting() {
    testTypes(
        "var x = /** @type {number} */ ('not a number');\n" +
        "var y = /** @type {{a: number}} */ ({a: 1});\n");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testUnaryAndSignOperators() {
    testTypes(
        "var n = 5;\n" +
        "n++; ++n; n--; --n;\n" +
        "+n; -n;\n" +
        "!n;\n" +
        "~n;\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBadBitwiseNot() {
    testTypes("var s = 'str'; ~s;");
    assertTrue(hasWarning(TypeCheck.BIT_OPERATION));
  }

  @Test(timeout = 4000)
  public void testBinaryArithmeticAndBitwise() {
    testTypes(
        "var a = 10, b = 2;\n" +
        "var c = a + b;\n" +
        "var d = a - b;\n" +
        "var e = a * b;\n" +
        "var f = a / b;\n" +
        "var g = a % b;\n" +
        "var h = a << b;\n" +
        "var i = a >> b;\n" +
        "var j = a >>> b;\n" +
        "var k = a & b;\n" +
        "var l = a | b;\n" +
        "var m = a ^ b;\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCompoundAssignments() {
    testTypes(
        "var a = 10;\n" +
        "a += 1; a -= 1; a *= 2; a /= 2; a %= 2;\n" +
        "a <<= 1; a >>= 1; a >>>= 1;\n" +
        "a &= 1; a |= 1; a ^= 1;\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testComparisonsNumericAndString() {
    testTypes(
        "var a = 1 < 2;\n" +
        "var b = 2 <= 3;\n" +
        "var c = 3 > 2;\n" +
        "var d = 4 >= 4;\n" +
        "var s1 = 'a' < 'b';\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDeterministicEqualityComparison() {
    testTypes("var b = (1 === 'string');");
    assertTrue(hasWarning(TypeCheck.DETERMINISTIC_TEST));
  }

  @Test(timeout = 4000)
  public void testTypeOfExpressions() {
    testTypes(
        "var t = typeof 123;\n" +
        "var check = (typeof 'abc' === 'string');\n" +
        "var badCheck = (typeof 'abc' === 'invalid_type');\n");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testInAndInstanceofOperators() {
    testTypes(
        "var o = {p: 1};\n" +
        "var hasP = 'p' in o;\n" +
        "var isInst = o instanceof Object;\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testStructInOperatorViolation() {
    testTypes(
        "/** @struct @constructor */ function S() {}\n" +
        "var s = new S();\n" +
        "var check = 'prop' in s;\n");
    assertTrue(hasWarning(TypeCheck.IN_USED_WITH_STRUCT));
  }

  @Test(timeout = 4000)
  public void testForInWithStructViolation() {
    testTypes(
        "/** @struct @constructor */ function S() {}\n" +
        "var s = new S();\n" +
        "for (var p in s) {}\n");
    assertTrue(hasWarning(TypeCheck.IN_USED_WITH_STRUCT));
  }

  @Test(timeout = 4000)
  public void testDeleteProperty() {
    testTypes(
        "var obj = {a: 1};\n" +
        "delete obj.a;\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCaseAndSwitch() {
    testTypes(
        "switch (1) {\n" +
        "  case 1: break;\n" +
        "  case 2: break;\n" +
        "  default: break;\n" +
        "}\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testWithStatement() {
    testTypes("with ({a: 1}) { var x = 1; }");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testReturnStatements() {
    testTypes(
        "/** @return {number} */ function f() { return 10; }\n" +
        "/** @return {void} */ function g() { return; }\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testInvalidReturnStatement() {
    testTypes("/** @return {number} */ function f() { return 'not a number'; }");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testFunctionCallOnNonFunction() {
    testTypes("var x = 10; x();");
    assertTrue(hasWarning(TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testConstructorCalledWithoutNew() {
    testTypes(
        "/** @constructor */ function Foo() {}\n" +
        "Foo();\n");
    assertTrue(hasWarning(TypeCheck.CONSTRUCTOR_NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testNewOnNonConstructor() {
    testTypes(
        "function notCtor() {}\n" +
        "var x = new notCtor();\n");
    assertTrue(hasWarning(TypeCheck.NOT_A_CONSTRUCTOR));
  }

  @Test(timeout = 4000)
  public void testFunctionMasksVariable() {
    testTypes(
        "var bar = 123;\n" +
        "function test() {\n" +
        "  function bar() {}\n" +
        "}\n");
    assertTrue(hasWarning(TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testExpectedThisType() {
    testTypes(
        "/** @this {{x: number}} */ function needsThis() {}\n" +
        "needsThis();\n");
    assertTrue(hasWarning(TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testOverridingPrototypeWithNonObject() {
    testTypes(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = 123;\n");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testStructPrototypeAssignedNonStruct() {
    testTypes(
        "/** @struct @constructor */ function S() {}\n" +
        "S.prototype = {};\n");
    assertTrue(hasWarning(TypeCheck.CONFLICTING_SHAPE_TYPE));
  }

  @Test(timeout = 4000)
  public void testIllegalPropertyCreationOnStruct() {
    testTypes(
        "/** @struct @constructor */ function S() {}\n" +
        "var s = new S();\n" +
        "s.newProp = 1;\n");
    assertTrue(hasWarning(TypeCheck.ILLEGAL_PROPERTY_CREATION));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyValidationForStructAndDict() {
    testTypes(
        "/** @struct */ var s = {'quoted': 1};\n" +
        "/** @dict */ var d = {unquoted: 1};\n");
    assertTrue(hasWarning(TypeCheck.ILLEGAL_OBJLIT_KEY));
  }

  @Test(timeout = 4000)
  public void testDictPropertyDotAccess() {
    testTypes(
        "/** @dict @constructor */ function D() {}\n" +
        "var d = new D();\n" +
        "var val = d.someProp;\n");
    assertTrue(hasWarning(TypeValidator.ILLEGAL_PROPERTY_ACCESS));
  }

  @Test(timeout = 4000)
  public void testPropertySuggestionOnTypo() {
    testTypes(
        "var obj = {foobar: 1};\n" +
        "var x = obj.foobaz;\n");
    assertTrue(hasWarning(TypeCheck.INEXISTENT_PROPERTY_WITH_SUGGESTION) ||
               hasWarning(TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testInterfaceMemberChecks() {
    testTypes(
        "/** @interface */ function I() {}\n" +
        "I.prototype.prop = 123;\n" +
        "I.prototype.fn = function() { return 1; };\n");
    assertTrue(hasWarning(TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION) ||
               hasWarning(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY));
  }

  @Test(timeout = 4000)
  public void testInterfaceExtendingNonInterface() {
    testTypes(
        "function NotAnInterface() {}\n" +
        "/** @interface \n * @extends {NotAnInterface} */ function I() {}\n");
    assertTrue(hasWarning(TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  @Test(timeout = 4000)
  public void testClassImplementingNonInterface() {
    testTypes(
        "function NotAnInterface() {}\n" +
        "/** @constructor \n * @implements {NotAnInterface} */ function C() {}\n");
    assertTrue(hasWarning(TypeCheck.BAD_IMPLEMENTED_TYPE));
  }

  @Test(timeout = 4000)
  public void testInheritanceOverrideChecks() {
    testTypes(
        "/** @constructor */ function Super() {}\n" +
        "/** @type {number} */ Super.prototype.foo = 1;\n" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
        "/** @type {number} */ Sub.prototype.foo = 2;\n");
    assertTrue(hasWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testSuperclassPropertyMismatch() {
    testTypes(
        "/** @constructor */ function Super() {}\n" +
        "/** @type {number} */ Super.prototype.foo = 1;\n" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
        "/** @override \n * @type {string} */ Sub.prototype.foo = 'str';\n");
    assertTrue(hasWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH));
  }

  @Test(timeout = 4000)
  public void testUnknownOverride() {
    testTypes(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
        "/** @override */ Sub.prototype.unknownProp = 1;\n");
    assertTrue(hasWarning(TypeCheck.UNKNOWN_OVERRIDE));
  }

  @Test(timeout = 4000)
  public void testEnumAliasIncompatible() {
    testTypes(
        "/** @enum {number} */ var E1 = {A: 1};\n" +
        "/** @enum {string} */ var E2 = E1;\n");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckAnnotation() {
    testTypes(
        "/** @notypecheck */\n" +
        "function noCheck() {\n" +
        "  var x = 1;\n" +
        "  x = 'incompatible assignment';\n" +
        "}\n");
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testIllegalImplicitCast() {
    testTypes("/** @implicitCast */ var badCast = 1;");
    assertTrue(hasWarning(TypeCheck.ILLEGAL_IMPLICIT_CAST));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    testTypes("");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0.0, typeCheck.getTypedPercent(), 0.001);
  }

  @Test(timeout = 4000)
  public void testCallWithZeroVsExpectedArguments() {
    testTypes(
        "/** @param {number} a\n * @param {string} b */ function req(a, b) {}\n" +
        "req();\n" +
        "req(1);\n" +
        "req(1, 'two', 3);\n");
    assertTrue(hasWarning(TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVarArgCallBoundaries() {
    testTypes(
        "/** @param {...number} var_args */ function varArg(var_args) {}\n" +
        "varArg();\n" +
        "varArg(1);\n" +
        "varArg(1, 2, 3, 4, 5);\n");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGetterAndSetterDefinitionsInObjectLiteral() {
    testTypes(
        "var obj = {\n" +
        "  get x() { return 10; },\n" +
        "  set x(v) {}\n" +
        "};\n");
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessWithoutInitializationThrowsNPE() {
    Compiler comp = new Compiler();
    CompilerOptions options = new CompilerOptions();
    comp.initOptions(options);

    TypeCheck tc = new TypeCheck(comp, comp.getReverseAbstractInterpreter(), comp.getTypeRegistry());
    Node script = comp.parseTestCode("var a = 1;");
    // scopeCreator is null here, expects NullPointerException
    tc.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessForTestingWithoutParentThrowsIllegalStateException() {
    Compiler comp = new Compiler();
    CompilerOptions options = new CompilerOptions();
    comp.initOptions(options);

    TypeCheck tc = new TypeCheck(comp, comp.getReverseAbstractInterpreter(), comp.getTypeRegistry());
    Node script = comp.parseTestCode("var a = 1;");
    // script.getParent() is null, expects IllegalStateException
    tc.processForTesting(null, script);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorsAndChaining() {
    Compiler comp = new Compiler();
    CompilerOptions options = new CompilerOptions();
    comp.initOptions(options);

    JSTypeRegistry reg = comp.getTypeRegistry();
    ReverseAbstractInterpreter rai = comp.getReverseAbstractInterpreter();

    TypeCheck tc1 = new TypeCheck(comp, rai, reg);
    assertNotNull(tc1);

    TypeCheck tc2 = new TypeCheck(comp, rai, reg, CheckLevel.ERROR);
    assertNotNull(tc2);

    TypeCheck chained = tc2.reportMissingProperties(false);
    assertSame(tc2, chained);

    assertEquals(0.0, tc2.getTypedPercent(), 0.001);
  }
}