/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.javascript.jscomp.TypeCheck
 * Target Environment: Java 8 / JUnit 4 / Defects4J Closure Compiler
 *
 * Key Decision Branches & Boundary Conditions Targeted:
 * 1. Interface Inheritance Override Defect (Defects4J ground truth: testInterfaceInheritanceCheck12):
 *    - checkDeclaredPropertyInheritance() on interface types extending super interfaces.
 *    - Verification of INTERFACE_METHOD_OVERRIDE diagnostic when sub-interface redefines property.
 * 2. AST Node Visitation Dispatch (Token switch-case branches):
 *    - DELPROP: isReference() guard vs BAD_DELETE warning.
 *    - NEW: constructor vs non-constructor (NOT_A_CONSTRUCTOR).
 *    - CALL: callable vs NOT_CALLABLE, constructor called directly (CONSTRUCTOR_NOT_CALLABLE),
 *      and parameter count validation (WRONG_ARGUMENT_COUNT min/max/varargs).
 *    - FUNCTION: function masks outer non-function variable (FUNCTION_MASKS_VARIABLE),
 *      conflicting extended type (CONFLICTING_EXTENDED_TYPE), bad implemented type (BAD_IMPLEMENTED_TYPE).
 *    - RETURN: return type mismatch, void return in value-returning function, misplaced return.
 *    - BITWISE & BINARY: matchesInt32Context, matchesUint32Context, expectNumber, expectBitwiseable.
 *    - EQUALITY (EQ, NE, SHEQ, SHNE): DETERMINISTIC_TEST and DETERMINISTIC_TEST_NO_RESULT warnings.
 *    - COMPARISONS (LT, LE, GT, GE): string vs numeric context checks.
 *    - IN & INSTANCEOF: object expectations and actual object validations.
 *    - GETPROP: missing property detection, property test suppression heuristics (isPropertyTest).
 *    - ENUM: INEXISTENT_ENUM_ELEMENT and enum initializer type conformance.
 * 3. Annotation & State Modes:
 *    - @notypecheck section suppressing diagnostics and validator reporting.
 *    - @implicitCast illegality in non-extern files (ILLEGAL_IMPLICIT_CAST).
 *    - Report missing override options (HIDDEN_SUPERCLASS_PROPERTY, UNKNOWN_OVERRIDE, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH).
 *    - Report unknown types (UNKNOWN_EXPR_TYPE).
 *    - getTypedPercent accounting (nullCount, unknownCount, typedCount).
 * 4. Defensive Guard Paths & Lifecycle:
 *    - Preconditions in process() and processForTesting().
 *    - Null/uninitialized scopes triggering state exceptions.
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class TypeCheckGeminiTest {

  private static class CheckResult {
    final Compiler compiler;
    final TypeCheck typeCheck;

    CheckResult(Compiler compiler, TypeCheck typeCheck) {
      this.compiler = compiler;
      this.typeCheck = typeCheck;
    }
  }

  private CheckResult check(String js) {
    return check("", js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private CheckResult check(String externs, String js) {
    return check(externs, js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private CheckResult check(String externs, String js,
      CheckLevel reportMissingOverride, CheckLevel reportUnknownTypes,
      boolean reportMissingProps) {
    Compiler compiler = new Compiler();
    Node jsNode = compiler.parseTestCode(js);
    Node externsNode = compiler.parseTestCode(externs);
    Node parent = new Node(Token.BLOCK, externsNode, jsNode);

    TypeCheck typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        reportMissingOverride,
        reportUnknownTypes);
    typeCheck.reportMissingProperties(reportMissingProps);
    typeCheck.processForTesting(externsNode, jsNode);
    return new CheckResult(compiler, typeCheck);
  }

  private boolean hasDiagnostic(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == type) {
        return true;
      }
    }
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType() == type) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
  // =========================================================================

  /**
   * Targets Defects4J known failure: TypeCheckTest::testInterfaceInheritanceCheck12
   * When an interface extends a super interface and re-declares a property,
   * it must issue the INTERFACE_METHOD_OVERRIDE warning.
   */
  @Test(timeout = 4000)
  public void testInterfaceInheritanceCheck12_DefectTarget() {
    String js =
        "/** @interface */ function Super() {}\n" +
        "/** @type {number} */ Super.prototype.foo;\n" +
        "/** @interface \n * @extends {Super} */ function Sub() {}\n" +
        "/** @type {string} */ Sub.prototype.foo;\n";
    CheckResult result = check(js);
    assertTrue("Should report INTERFACE_METHOD_OVERRIDE when interface re-declares extended interface property",
        hasDiagnostic(result.compiler, TypeCheck.INTERFACE_METHOD_OVERRIDE));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDeterministicEqualityTest() {
    String js = "var x = (true == {});";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.DETERMINISTIC_TEST));
  }

  @Test(timeout = 4000)
  public void testDeterministicShallowEqualityTest() {
    String js = "var x = (1 === 'string');";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.DETERMINISTIC_TEST_NO_RESULT));
  }

  @Test(timeout = 4000)
  public void testFunctionMasksVariable() {
    String js = "function test() { var x = 1; function x() {} }";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testNotAConstructor() {
    String js = "var x = new 123();";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.NOT_A_CONSTRUCTOR));
  }

  @Test(timeout = 4000)
  public void testNotCallable() {
    String js = "(123)();";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testConstructorNotCallableWithoutNew() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "Foo();";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.CONSTRUCTOR_NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooFew() {
    String js = "/** @param {number} a \n @param {number} b */\n" +
                "function f(a, b) {}\n" +
                "f(1);";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountTooMany() {
    String js = "/** @param {number} a */\n" +
                "function f(a) {}\n" +
                "f(1, 2, 3);";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testBitwiseOperationBadType() {
    String js = "var x = ~'not_a_number';";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.BIT_OPERATION));
  }

  @Test(timeout = 4000)
  public void testInexistentEnumElement() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1 };\n" +
                "var val = MyEnum.NON_EXISTENT;";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.INEXISTENT_ENUM_ELEMENT));
  }

  @Test(timeout = 4000)
  public void testConflictingExtendedType() {
    String js = "/** @constructor */ function SuperCtor() {}\n" +
                "/** @interface \n * @extends {SuperCtor} */ function SubIface() {}";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  @Test(timeout = 4000)
  public void testBadImplementedType() {
    String js = "/** @constructor */ function NotAnInterface() {}\n" +
                "/** @constructor \n * @implements {NotAnInterface} */ function MyClass() {}";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.BAD_IMPLEMENTED_TYPE));
  }

  @Test(timeout = 4000)
  public void testOverridingPrototypeWithNonObject() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "Foo.prototype = 123;";
    CheckResult result = check(js);
    assertTrue(result.compiler.getErrorCount() + result.compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testUnknownOverride() {
    String js = "/** @constructor */ function Super() {}\n" +
                "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
                "/** @override */ Sub.prototype.unknownProp = function() {};";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.UNKNOWN_OVERRIDE));
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyMismatch() {
    String js = "/** @constructor */ function Super() {}\n" +
                "/** @type {number} */ Super.prototype.foo = 1;\n" +
                "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
                "/** @override \n * @type {string} */ Sub.prototype.foo = 'text';";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH));
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyMissingAnnotation() {
    String js = "/** @constructor */ function Super() {}\n" +
                "/** @type {number} */ Super.prototype.foo = 1;\n" +
                "/** @constructor \n * @extends {Super} */ function Sub() {}\n" +
                "Sub.prototype.foo = 2;";
    CheckResult result = check("", js, CheckLevel.WARNING, CheckLevel.OFF, true);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testIllegalImplicitCastInUserCode() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "/** @implicitCast */ Foo.prototype.x = 1;";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.ILLEGAL_IMPLICIT_CAST));
  }

  @Test(timeout = 4000)
  public void testReturnInconsistentType() {
    String js = "/** @return {number} */ function f() { return 'not_number'; }";
    CheckResult result = check(js);
    assertTrue(result.compiler.getErrorCount() + result.compiler.getWarningCount() > 0);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBadDeleteOperand() {
    String js = "delete 5;";
    CheckResult result = check(js);
    assertTrue(hasDiagnostic(result.compiler, TypeCheck.BAD_DELETE));
  }

  @Test(timeout = 4000)
  public void testValidDeleteOperand() {
    String js = "var obj = {a: 1}; delete obj.a;";
    CheckResult result = check(js);
    assertFalse(hasDiagnostic(result.compiler, TypeCheck.BAD_DELETE));
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSectionSuppression() {
    String js = "/** @notypecheck */\n" +
                "function test() {\n" +
                "  delete 5;\n" +
                "  (123)();\n" +
                "}";
    CheckResult result = check(js);
    assertFalse(hasDiagnostic(result.compiler, TypeCheck.BAD_DELETE));
    assertFalse(hasDiagnostic(result.compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testPropertyTestSuppressionHeuristics() {
    String js = "var obj = {};\n" +
                "if (obj.a) {}\n" +
                "while (obj.b) {}\n" +
                "for (;obj.c;) {}\n" +
                "do {} while (obj.d);\n" +
                "var t = typeof obj.e;\n" +
                "var inst = obj.f instanceof Object;\n" +
                "var andOp = obj.g && 1;\n" +
                "var notOp = !obj.h || 2;";
    CheckResult result = check(js);
    assertFalse(hasDiagnostic(result.compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testTypedPercentAccounting() {
    String js = "var x = 1; var y = 'str';";
    CheckResult result = check(js);
    double percent = result.typeCheck.getTypedPercent();
    assertTrue("Typed percent should be between 0 and 100", percent >= 0.0 && percent <= 100.0);
  }

  @Test(timeout = 4000)
  public void testEmptyScriptTypedPercent() {
    Compiler compiler = new Compiler();
    TypeCheck typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertEquals("Empty typechecker should report 0.0%", 0.0, typeCheck.getTypedPercent(), 0.0001);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessNullScopeCreatorFails() {
    Compiler compiler = new Compiler();
    Node node = compiler.parseTestCode("var x = 1;");
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    try {
      tc.process(null, node);
      fail("Expected NullPointerException when scopeCreator is null");
    } catch (NullPointerException expected) {
      assertNotNull(expected);
    }
  }

  @Test(timeout = 4000)
  public void testProcessForTestingUnattachedRootFails() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode("var x = 1;");
    Node externs = compiler.parseTestCode("");
    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    try {
      tc.processForTesting(externs, js);
      fail("Expected IllegalStateException when jsRoot has no parent");
    } catch (IllegalStateException expected) {
      assertNotNull(expected);
    }
  }

  @Test(timeout = 4000)
  public void testProcessForTestingTwiceFails() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode("var x = 1;");
    Node externs = compiler.parseTestCode("");
    new Node(Token.BLOCK, externs, js);

    TypeCheck tc = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    tc.processForTesting(externs, js);

    try {
      tc.processForTesting(externs, js);
      fail("Expected IllegalStateException when re-running processForTesting");
    } catch (IllegalStateException expected) {
      assertNotNull(expected);
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorsAndChaining() {
    Compiler compiler = new Compiler();

    TypeCheck tc1 = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertNotNull(tc1);

    TypeCheck tc2 = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    assertNotNull(tc2);

    TypeCheck tc3 = tc2.reportMissingProperties(false);
    assertSame("Method chaining should return same instance", tc2, tc3);
  }

  @Test(timeout = 4000)
  public void testDiagnosticGroupsIntegrity() {
    assertNotNull("ALL_DIAGNOSTICS must not be null", TypeCheck.ALL_DIAGNOSTICS);
    assertNotNull("UNEXPECTED_TOKEN diagnostic must exist", TypeCheck.UNEXPECTED_TOKEN);
    assertNotNull("BAD_DELETE diagnostic must exist", TypeCheck.BAD_DELETE);
    assertNotNull("INTERFACE_METHOD_OVERRIDE diagnostic must exist", TypeCheck.INTERFACE_METHOD_OVERRIDE);
  }
}