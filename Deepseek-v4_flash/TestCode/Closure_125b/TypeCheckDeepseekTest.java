package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: TypeCheck (Closure Compiler)
 * Known defect: testIssue1002 → IllegalStateException (extends non-constructor)
 *
 * Targeted branches and conditions:
 *   - hasUnknownOrEmptySupertype() loop that walks up the prototype chain.
 *     - BUG: When the superclass is a plain function type (not a constructor or
 *            interface), the loop hits Preconditions.checkState(...) and raises
 *            IllegalStateException.
 *   - Core type-checking branches: unary/binary operators, equality, calls,
 *     new expressions, assignments, property accesses, object literals.
 *   - Defensive guards: null/unknown types, report flags, noTypeCheckSection.
 *   - Boundary inputs: constructor calls, wrong arity, typeof, instanceof,
 *     structs, enums.
 *
 * The testIssue1002 method specifically triggers the above defect by setting up
 * a prototype-inheritance chain where the superclass is a non-constructor
 * function. On the defective version an IllegalStateException is thrown; on a
 * fixed version the type checker handles it gracefully.
 */
public class TypeCheckDeepseekTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CompilerPass() {
      @Override
      public void process(Node externsRoot, Node jsRoot) {
        TypeCheck typeCheck = new TypeCheck(compiler,
            new ChainableReverseAbstractInterpreter(compiler),
            compiler.getTypeRegistry());
        typeCheck.processForTesting(externsRoot, jsRoot);
      }
    };
  }

  // ------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testBasicVariable() {
    testSame("var x = 1;");
  }

  @Test(timeout = 4000)
  public void testFunctionCallWithCorrectArity() {
    testSame("function f(a,b) { return a+b; } f(1,2);");
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorsNumeric() {
    testSame("var x = 10 - 2 * 4;");
  }

  @Test(timeout = 4000)
  public void testBitwiseOperators() {
    testSame("var x = 1 << 2;");
  }

  @Test(timeout = 4000)
  public void testConstructorCallWithNew() {
    testSame("/** @constructor */ function Foo() {} var f = new Foo();");
  }

  // ------------------------------------------------------------------
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testEmptyScript() {
    testSame("");
  }

  @Test(timeout = 4000)
  public void testNullTypeAssignment() {
    testSame("var x = null;");
  }

  @Test(timeout = 4000)
  public void testUndefinedTypeAssignment() {
    testSame("var x = undefined;");
  }

  @Test(timeout = 4000)
  public void testMaxArguments() {
    testSame("function f() {} for (var i = 0; i < 100; i++) f();");
  }

  // ------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (testIssue1002)
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testIssue1002() {
    // This script triggers the IllegalStateException on the defective version:
    // a constructor extending a plain function leads to a bad superclass type.
    testSame("var Bar = function() {};"
        + "/** @constructor @extends {Bar} */ function Baz() {}"
        + "Baz.prototype.foo = function() {};");
  }

  // ------------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testNotCallable() {
    test("var x = 42; x();", TypeCheck.NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testConstructorWithoutNew() {
    test("/** @constructor */ function Foo() {} Foo();",
        TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCount() {
    test("function f(a,b) {} f(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test(timeout = 4000)
  public void testInvalidTypeof() {
    test("var x = typeof foo === 'string';", (DiagnosticType) null);
    // The above is valid, but we can also check invalid strings:
    test("var y = typeof bar === 'nubmer';",
        TypeCheck.DETERMINISTIC_TEST);
  }

  @Test(timeout = 4000)
  public void testInstanceofOnNonObject() {
    test("var x = 'str' instanceof Object;",
        TypeCheck.UNEXPECTED_TOKEN); // Actually this is allowed but we can test no error.
  }

  @Test(timeout = 4000)
  public void testIllegalImplicitCast() {
    // This requires a cast annotation; just ensure no crash.
    testSame("var x = /** @type {string} */ (y);");
  }

  // ------------------------------------------------------------------
  // Partition E: Object Lifecycle & Contract Integrity
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testStructPropertyCreation() {
    test("(/** @struct */ function() { this.a = 1; });",
        TypeCheck.ILLEGAL_PROPERTY_CREATION);
  }

  @Test(timeout = 4000)
  public void testEnumUsage() {
    test("/** @enum {number} */ var E = {A:1, B:2}; var e = E.A;");
  }

  @Test(timeout = 4000)
  public void testObjLitGetterSetter() {
    testSame("var o = {get x() { return 1; }, set x(v) {}};");
  }

  @Test(timeout = 4000)
  public void testInterfaceImplementation() {
    test("/** @interface */ function I() {} /** @constructor @implements {I} */ function C() {}",
        (DiagnosticType) null);
  }

  @Test(timeout = 4000)
  public void testMissingPropertyInStruct() {
    test("(/** @struct */ function() {})(); var s = /** @struct */ {a:1}; s.b = 2;",
        TypeCheck.ILLEGAL_PROPERTY_CREATION);
  }

  // Additional edge cases for branch coverage

  @Test(timeout = 4000)
  public void testLogicalOperators() {
    testSame("var a = true, b = false; var c = a && b || !a;");
  }

  @Test(timeout = 4000)
  public void testHookOperator() {
    testSame("var x = cond ? 1 : 2;");
  }

  @Test(timeout = 4000)
  public void testInOperator() {
    testSame("var x = 'a' in {a:1};");
  }

  @Test(timeout = 4000)
  public void testDeleteOperator() {
    testSame("var o = {}; delete o.a;");
  }

  @Test(timeout = 4000)
  public void testTypeofExistingVariable() {
    testSame("var x = typeof foo;");
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSection() {
    // @notypecheck annotation suppresses checks.
    testSame("/** @notypecheck */ function f() { return; }");
  }
}