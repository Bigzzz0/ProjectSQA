package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.TypeCheck
 * Defects4J Ground Truth Defect:
 *   - Closure Issue 810 / testGetprop4 / testIssue810:
 *     In visitGetProp, a guard checking "else if (n.getJSType() != null && parent.isAssign()) return;"
 *     fails to check if n is the target (LHS) of the assignment (parent.getFirstChild() == n).
 *     Consequently, when a non-existent property appears on the RHS of an assignment (e.g. `x = f.bar;`
 *     or `f.a = f.bar;`), the missing property check is erroneously bypassed and INEXISTENT_PROPERTY
 *     warning is suppressed.
 *
 * Targeted Decision Branches & Coverage Zones:
 *   Partition A: Core Functional Logic & State Transitions
 *     - Basic literals & keywords: null, number, string, boolean, this, comma, regex, array
 *     - Variable declarations and initializations (inferred vs declared)
 *     - Function traversal & parameter validation
 *     - Return type validations
 *     - Percentage typed metric calculation (getTypedPercent)
 *
 *   Partition B: Boundary Value Analysis (BVA) & Operators
 *     - Binary operators: bitwise (LSH, RSH, URSH, BITOR, BITXOR, BITAND), arithmetic (DIV, MOD, MUL, SUB)
 *     - Unary operators: INC, DEC, POS, NEG, BITNOT, NOT, VOID, TYPEOF
 *     - Relational comparisons: LT, LE, GT, GE, EQ, NE, SHEQ, SHNE (deterministic evaluation warnings)
 *     - Keyword operators: IN, INSTANCEOF, DELPROP, WITH, CASE
 *
 *   Partition C: Defect-Targeted Zone (Issue 810 & testGetprop4)
 *     - Property access on RHS of ASSIGN node (`x = obj.nonExistentProp;`)
 *     - Property access on RHS of property assignment (`obj1.a = obj2.nonExistentProp;`)
 *     - Missing property access warning expectation with reportMissingProperties enabled
 *
 *   Partition D: Diagnostics & Inheritance Guard Paths
 *     - @noTypeCheck section suppression
 *     - FUNCTION_MASKS_VARIABLE detection
 *     - NOT_CALLABLE, CONSTRUCTOR_NOT_CALLABLE, EXPECTED_THIS_TYPE
 *     - WRONG_ARGUMENT_COUNT (underflow and overflow)
 *     - NOT_A_CONSTRUCTOR instantiation
 *     - Interface conformance: INTERFACE_FUNCTION_NOT_EMPTY, INVALID_INTERFACE_MEMBER_DECLARATION
 *     - Subtyping & extension conflicts: CONFLICTING_EXTENDED_TYPE, CONFLICTING_IMPLEMENTED_TYPE,
 *       BAD_IMPLEMENTED_TYPE, INCOMPATIBLE_EXTENDED_PROPERTY_TYPE
 *     - Inheritance annotations: HIDDEN_SUPERCLASS_PROPERTY, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
 *       UNKNOWN_OVERRIDE
 *     - Cast annotations: ILLEGAL_IMPLICIT_CAST
 *
 *   Partition E: Defensive Guards & Lifecycle Assertions
 *     - Preconditions on process, processForTesting, check null inputs
 * ---------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class TypeCheckGeminiTest {

  private Compiler compiler;
  private TypeCheck typeCheck;

  /**
   * Helper to initialize compiler, parse code, configure diagnostic groups, and execute TypeCheck.
   */
  private Scope check(String js) {
    return check("", js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private Scope check(String externs, String js) {
    return check(externs, js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private Scope check(String externs, String js, CheckLevel missingOverride, CheckLevel unknownTypes, boolean reportMissingProperties) {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();

    DiagnosticGroup missingProps = new DiagnosticGroup(TypeCheck.INEXISTENT_PROPERTY);
    compiler.getOptions().setWarningLevel(missingProps, CheckLevel.WARNING);

    Node externsRoot = compiler.parseTestCode(externs == null ? "" : externs);
    Node jsRoot = compiler.parseTestCode(js);
    new Node(Token.BLOCK, externsRoot, jsRoot);

    typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        missingOverride,
        unknownTypes);
    typeCheck.reportMissingProperties(reportMissingProperties);

    return typeCheck.processForTesting(externsRoot, jsRoot);
  }

  private void assertWarning(DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == type) {
        return;
      }
    }
    fail("Expected warning " + type.key + " but got warnings: "
        + java.util.Arrays.toString(compiler.getWarnings())
        + " and errors: " + java.util.Arrays.toString(compiler.getErrors()));
  }

  private void assertError(DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == type) {
        return;
      }
    }
    fail("Expected error " + type.key + " but got errors: "
        + java.util.Arrays.toString(compiler.getErrors()));
  }

  private void assertNoWarningsOrErrors() {
    assertEquals("Expected 0 warnings, found: " + java.util.Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
    assertEquals("Expected 0 errors, found: " + java.util.Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 810 & testGetprop4)
  // =========================================================================

  /**
   * Targets the defect where an assignment RHS property read (e.g., `x = f.bar;`)
   * prematurely aborts missing property validation because parent.isAssign() is true.
   */
  @Test(timeout = 4000)
  public void testIssue810() {
    check(
        "/** @constructor */ function Foo() {}\n" +
        "var f = new Foo();\n" +
        "var x;\n" +
        "x = f.bar;\n");
    assertWarning(TypeCheck.INEXISTENT_PROPERTY);
  }

  /**
   * Targets the defect where both sides of assignment are properties (e.g., `f.a = f.bar;`),
   * and the RHS property access is skipped from being checked.
   */
  @Test(timeout = 4000)
  public void testGetprop4() {
    check(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.a = 1;\n" +
        "var f = new Foo();\n" +
        "f.a = f.bar;\n");
    assertWarning(TypeCheck.INEXISTENT_PROPERTY);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testLiteralsAndPercentTyped() {
    check(
        "var a = null;\n" +
        "var b = 42;\n" +
        "var c = 'closure';\n" +
        "var d = true;\n" +
        "var e = false;\n" +
        "var f = [1, 2, 3];\n" +
        "var g = /abc/;\n" +
        "var h = (1, 'two');\n");
    assertNoWarningsOrErrors();
    assertTrue("Typed percentage should be greater than 0", typeCheck.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testTypedPercentEmpty() {
    TypeCheck tc = new TypeCheck(
        new Compiler(),
        null,
        new Compiler().getTypeRegistry());
    assertEquals(0.0, tc.getTypedPercent(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testFunctionAndReturnVerification() {
    check(
        "/** @return {number} */\n" +
        "function foo() {\n" +
        "  return 'not a number';\n" +
        "}\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testFunctionVoidReturnViolation() {
    check(
        "/** @return {void} */\n" +
        "function foo() {\n" +
        "  return 42;\n" +
        "}\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testFunctionMasksVariable() {
    check(
        "var f = 10;\n" +
        "function f() {}\n");
    assertWarning(TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSection() {
    check(
        "/** @noTypeCheck */\n" +
        "function foo() {\n" +
        "  var x = 1;\n" +
        "  x = 'str';\n" +
        "}\n");
    assertNoWarningsOrErrors();
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Operators
  // =========================================================================

  @Test(timeout = 4000)
  public void testBitwiseOperationsMismatch() {
    check(
        "var x = 'text';\n" +
        "var y = ~x;\n");
    assertWarning(TypeCheck.BIT_OPERATION);
  }

  @Test(timeout = 4000)
  public void testBinaryBitwiseOperatorsMismatch() {
    check(
        "var x = 'str';\n" +
        "var y = x << 2;\n");
    assertWarning(TypeCheck.BIT_OPERATION);
  }

  @Test(timeout = 4000)
  public void testDeterministicEqualityTest() {
    check(
        "var x = 10;\n" +
        "var result = (x === 'string');\n");
    assertWarning(TypeCheck.DETERMINISTIC_TEST);
  }

  @Test(timeout = 4000)
  public void testDeterministicInequalityTest() {
    check(
        "var x = 10;\n" +
        "var result = (x !== 'string');\n");
    assertWarning(TypeCheck.DETERMINISTIC_TEST);
  }

  @Test(timeout = 4000)
  public void testTypeofStringValidation() {
    check(
        "var x = 10;\n" +
        "if (typeof x === 'numb') {}\n");
    assertWarning(TypeValidator.UNKNOWN_TYPEOF_VALUE);
  }

  @Test(timeout = 4000)
  public void testSignOperatorMismatch() {
    check(
        "var s = 'abc';\n" +
        "var n = -s;\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testIncDecOperatorMismatch() {
    check(
        "var s = 'abc';\n" +
        "s++;\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testInOperatorRequiresObject() {
    check(
        "var res = 'prop' in 123;\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testInstanceofExpectations() {
    check(
        "var res = 123 instanceof 456;\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testWithRequiresObject() {
    check(
        "with (123) {}\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testDelpropEvaluation() {
    check(
        "var obj = {a: 1};\n" +
        "var res = delete obj.a;\n");
    assertNoWarningsOrErrors();
  }

  // =========================================================================
  // Partition D: Diagnostics & Inheritance Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testNotCallableDiagnostic() {
    check(
        "var notFn = 42;\n" +
        "notFn();\n");
    assertWarning(TypeCheck.NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testConstructorNotCallableDiagnostic() {
    check(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "Foo();\n");
    assertWarning(TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testNotAConstructorDiagnostic() {
    check(
        "var notCtor = 42;\n" +
        "new notCtor();\n");
    assertWarning(TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test(timeout = 4000)
  public void testExpectedThisTypeDiagnostic() {
    check(
        "/** @this {Array} */\n" +
        "function foo() {}\n" +
        "foo();\n");
    assertWarning(TypeCheck.EXPECTED_THIS_TYPE);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountUnderflow() {
    check(
        "function foo(a, b) {}\n" +
        "foo(1);\n");
    assertWarning(TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountOverflow() {
    check(
        "function foo(a) {}\n" +
        "foo(1, 2);\n");
    assertWarning(TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionNotEmpty() {
    check(
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "AnInterface.prototype.foo = function() { return 1; };\n");
    assertWarning(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test(timeout = 4000)
  public void testInvalidInterfaceMemberDeclaration() {
    check(
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "AnInterface.prototype.foo = 42;\n");
    assertWarning(TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION);
  }

  @Test(timeout = 4000)
  public void testConflictingExtendedTypeConstructorExtendsInterface() {
    check(
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "/** @constructor\n" +
        " *  @extends {AnInterface} */\n" +
        "function MyClass() {}\n");
    assertWarning(TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test(timeout = 4000)
  public void testConflictingImplementedTypeInterfaceImplements() {
    check(
        "/** @interface */\n" +
        "function InterfaceA() {}\n" +
        "/** @interface\n" +
        " *  @implements {InterfaceA} */\n" +
        "function InterfaceB() {}\n");
    assertWarning(TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
  }

  @Test(timeout = 4000)
  public void testBadImplementedTypeNotAnInterface() {
    check(
        "/** @constructor */\n" +
        "function NotAnInterface() {}\n" +
        "/** @constructor\n" +
        " *  @implements {NotAnInterface} */\n" +
        "function MyClass() {}\n");
    assertWarning(TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  @Test(timeout = 4000)
  public void testIncompatibleExtendedPropertyType() {
    check(
        "/** @interface */\n" +
        "function InterfaceA() {}\n" +
        "/** @type {number} */\n" +
        "InterfaceA.prototype.prop;\n" +
        "/** @interface */\n" +
        "function InterfaceB() {}\n" +
        "/** @type {string} */\n" +
        "InterfaceB.prototype.prop;\n" +
        "/** @interface\n" +
        " *  @extends {InterfaceA}\n" +
        " *  @extends {InterfaceB} */\n" +
        "function InterfaceC() {}\n");
    assertWarning(TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyWarning() {
    check(
        "",
        "/** @constructor */\n" +
        "function Super() {}\n" +
        "/** @type {number} */\n" +
        "Super.prototype.foo = 1;\n" +
        "/** @constructor\n" +
        " *  @extends {Super} */\n" +
        "function Sub() {}\n" +
        "/** @type {number} */\n" +
        "Sub.prototype.foo = 2;\n",
        CheckLevel.WARNING,
        CheckLevel.OFF,
        true);
    assertWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyMismatch() {
    check(
        "/** @constructor */\n" +
        "function Super() {}\n" +
        "/** @type {number} */\n" +
        "Super.prototype.foo = 1;\n" +
        "/** @constructor\n" +
        " *  @extends {Super} */\n" +
        "function Sub() {}\n" +
        "/** @override\n" +
        " *  @type {string} */\n" +
        "Sub.prototype.foo = 'str';\n");
    assertWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
  }

  @Test(timeout = 4000)
  public void testUnknownOverride() {
    check(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "/** @override */\n" +
        "Foo.prototype.notOnSuper = function() {};\n");
    assertWarning(TypeCheck.UNKNOWN_OVERRIDE);
  }

  @Test(timeout = 4000)
  public void testIllegalImplicitCastInUserCode() {
    check(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "/** @implicitCast */\n" +
        "Foo.prototype.bar = 1;\n");
    assertWarning(TypeCheck.ILLEGAL_IMPLICIT_CAST);
  }

  @Test(timeout = 4000)
  public void testReportUnknownTypesSetting() {
    check(
        "",
        "var unknownVar;\n",
        CheckLevel.OFF,
        CheckLevel.WARNING,
        true);
    assertWarning(TypeCheck.UNKNOWN_EXPR_TYPE);
  }

  // =========================================================================
  // Partition E: Defensive Guards & Lifecycle Assertions
  // =========================================================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testCheckNullNodeGuard() {
    Compiler comp = new Compiler();
    TypeCheck tc = new TypeCheck(comp, null, comp.getTypeRegistry());
    tc.check(null, false);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testProcessNullScopeCreatorGuard() {
    Compiler comp = new Compiler();
    Node jsRoot = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK, jsRoot);
    TypeCheck tc = new TypeCheck(comp, null, comp.getTypeRegistry());
    tc.process(null, jsRoot);
  }

  @Test(timeout = 4000)
  public void testConstructorChainingAndReportMissingProperties() {
    Compiler comp = new Compiler();
    TypeCheck tc = new TypeCheck(comp, null, comp.getTypeRegistry());
    assertSame(tc, tc.reportMissingProperties(false));
    assertSame(tc, tc.reportMissingProperties(true));
  }
}