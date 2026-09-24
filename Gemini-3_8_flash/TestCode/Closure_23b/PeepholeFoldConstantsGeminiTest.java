package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.PeepholeFoldConstants
 *
 * Defects4J Ground Truth Target:
 * - Bug: tryFoldArrayAccess fails when accessing index 0 because loop condition `i < intIndex` evaluates to `0 < 0`
 *   (false) initially. `elem` remains null, erroneously reporting JSC_INDEX_OUT_OF_BOUNDS_ERROR instead of folding.
 *   Targeted in Partition C: `testFoldGetElemDefectIndexZero()`.
 *
 * Branch & Condition Coverage Matrix:
 * - optimizeSubtree: NEW (ctor call), TYPEOF, NOT/POS/NEG/BITNOT, VOID, Binary operators default.
 * - tryFoldBinaryOperator: GETPROP, GETELEM, INSTANCEOF, AND/OR, LSH/RSH/URSH, ASSIGN, ASSIGN compound, ADD,
 *   SUB/DIV/MOD, MUL/BITAND/BITOR/BITXOR, Comparisons (LT, GT, LE, GE, EQ, NE, SHEQ, SHNE), default fallback.
 * - tryReduceOperandsForOp / tryConvertToNumber: NUMBER, AND, OR, COMMA, HOOK, NAME (undefined), conversion to numeric node.
 * - tryFoldTypeof: literals (function, string, number, boolean, object/array, void, undefined, name).
 * - tryFoldUnaryOperator: NOT (late vs non-late 0/1 guards), POS (numeric result bypass), NEG (-NaN, -Infinity,
 *   UnsupportedOperationException), BITNOT (Integer range, fractional operand, non-number error).
 * - tryFoldInstanceof: literal immutables -> false, Object constructor -> true, side-effects check.
 * - tryFoldAssign / tryUnfoldAssignOp: late mode gating, commutative operand reordering, compound unfold.
 * - tryFoldAndOr: boolean purity checks, side-effect elimination.
 * - tryFoldChildAddString: left/right child string concatenation with non-const expressions.
 * - tryFoldArithmeticOp / performArithmeticOp: overflow (> 2^53), string add bypass, div/mod by zero, string length expansion.
 * - tryFoldLeftChildOp: associative ops re-association (MUL, BITAND, BITOR, BITXOR).
 * - tryFoldShift: shift bounds [0, 32), integer range, fractional operands.
 * - tryFoldComparison: VOID, NULL, BOOLEAN, THIS, STRING (\u000B guard), NUMBER, NAME, NEG, Literals (ARRAY/OBJECT/REGEXP/FUNCTION).
 * - tryFoldCtorCall: forced string context (GETELEM property index, ADD concatenation).
 * - tryFoldGetElem / tryFoldArrayAccess: assignment target protection, non-number, fractional index, negative index,
 *   bounds error, empty slot folding to undefined.
 * - tryFoldGetProp / tryFoldObjectPropAccess: array length, string length, getter definitions, setter skip, side effects.
 * -----------------------------------------------------------------------------------------------------------------
 */
public class PeepholeFoldConstantsGeminiTest {

  private void test(String js, String expected) {
    test(js, expected, false);
  }

  private void test(String js, String expected, boolean late) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeFoldConstants fold = new PeepholeFoldConstants(late);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, fold);
    pass.process(null, root);
    assertEquals("Unexpected compilation errors", 0, compiler.getErrorCount());
    Node expectedRoot = compiler.parseTestCode(expected);
    assertEquals(compiler.toSource(expectedRoot), compiler.toSource(root));
  }

  private void testSame(String js) {
    test(js, js, false);
  }

  private void testSame(String js, boolean late) {
    test(js, js, late);
  }

  private void testError(String js, DiagnosticType error) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeFoldConstants fold = new PeepholeFoldConstants(false);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, fold);
    pass.process(null, root);
    assertTrue("Expected compilation error", compiler.getErrorCount() > 0);
    assertEquals(error.key, compiler.getErrors()[0].getType().key);
  }

  // =================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =================================================================================================

  @Test(timeout = 4000)
  public void testFoldArithmeticBasicOps() {
    test("var x = 10 + 20;", "var x = 30;");
    test("var x = 20 - 7;", "var x = 13;");
    test("var x = 4 * 5;", "var x = 20;");
    test("var x = 24 / 6;", "var x = 4;");
    test("var x = 14 % 5;", "var x = 4;");
  }

  @Test(timeout = 4000)
  public void testFoldBitwiseLogicalOps() {
    test("var x = 6 & 3;", "var x = 2;");
    test("var x = 6 | 3;", "var x = 7;");
    test("var x = 6 ^ 3;", "var x = 5;");
    test("var x = ~5;", "var x = -6;");
  }

  @Test(timeout = 4000)
  public void testFoldShiftOperations() {
    test("var x = 1 << 3;", "var x = 8;");
    test("var x = -16 >> 2;", "var x = -4;");
    test("var x = -1 >>> 0;", "var x = 4294967295;");
  }

  @Test(timeout = 4000)
  public void testFoldStringConcatenation() {
    test("var x = 'foo' + 'bar';", "var x = 'foobar';");
    test("var x = 'foo' + 123;", "var x = 'foo123';");
    test("var x = 123 + 'foo';", "var x = '123foo';");
    test("var x = foo() + 'a' + 'b';", "var x = foo() + 'ab';");
    test("var x = 'a' + ('b' + foo());", "var x = 'ab' + foo();");
  }

  @Test(timeout = 4000)
  public void testFoldTypeofLiterals() {
    test("var x = typeof function() {};", "var x = 'function';");
    test("var x = typeof 'hello';", "var x = 'string';");
    test("var x = typeof 42;", "var x = 'number';");
    test("var x = typeof true;", "var x = 'boolean';");
    test("var x = typeof false;", "var x = 'boolean';");
    test("var x = typeof null;", "var x = 'object';");
    test("var x = typeof {};", "var x = 'object';");
    test("var x = typeof [];", "var x = 'object';");
    test("var x = typeof void 0;", "var x = 'undefined';");
    test("var x = typeof undefined;", "var x = 'undefined';");
    testSame("var x = typeof someVariable;");
  }

  @Test(timeout = 4000)
  public void testFoldLogicalAndOr() {
    test("var x = true && y;", "var x = y;");
    test("var x = false && y;", "var x = false;");
    test("var x = true || y;", "var x = true;");
    test("var x = false || y;", "var x = y;");
    test("var x = 1 || y;", "var x = 1;");
    test("var x = 0 || y;", "var x = y;");
    testSame("var x = foo() && y;");
  }

  @Test(timeout = 4000)
  public void testFoldInstanceof() {
    test("var x = 1 instanceof Object;", "var x = false;");
    test("var x = 'text' instanceof Object;", "var x = false;");
    test("var x = true instanceof Object;", "var x = false;");
    test("var x = ({}) instanceof Object;", "var x = true;");
    test("var x = [] instanceof Object;", "var x = true;");
    testSame("var x = ({}) instanceof CustomClass;");
    testSame("var x = y instanceof Object;");
    testSame("var x = ({}) instanceof foo();");
  }

  @Test(timeout = 4000)
  public void testFoldGetPropLength() {
    test("var x = [1, 2, 3].length;", "var x = 3;");
    test("var x = 'abcdef'.length;", "var x = 6;");
    testSame("var x = [foo(), 1].length;");
    testSame("var x = obj.length;");
    testSame("var x = [1, 2].otherProp;");
  }

  @Test(timeout = 4000)
  public void testFoldObjectPropertyAccess() {
    test("var x = ({a: 1}).a;", "var x = 1;");
    test("var x = ({a: 1})['a'];", "var x = 1;");
    test("var x = ({a: 1, a: 2}).a;", "var x = 2;");
    test("var x = ({get a() { return 10; }}).a;", "var x = (function() { return 10; })();");
    testSame("var x = ({set a(v) {}}).a;");
    testSame("var x = ({b: 1}).a;");
    testSame("var x = ({b: foo(), a: 1}).a;");
    testSame("var x = ({a: function() { return this.v; }}).a;");
  }

  @Test(timeout = 4000)
  public void testFoldCtorCall() {
    test("var x = '' + new String('hello');", "var x = '' + 'hello';");
    test("var x = '' + new String();", "var x = '' + '';");
    test("var x = obj[new String('prop')];", "var x = obj['prop'];");
    testSame("var x = new String('hello');");
    testSame("var x = '' + new Object();");
    testSame("var x = '' + new String(unknownArg);");
  }

  // =================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =================================================================================================

  @Test(timeout = 4000)
  public void testArithmeticBoundariesAndNoFold() {
    testSame("var x = 5 / 0;");
    testSame("var x = 5 % 0;");
    testSame("var x = 2 / 3;");
    testSame("var x = 1e16 + 1e16;");
  }

  @Test(timeout = 4000)
  public void testShiftOperandBoundaries() {
    testError("var x = 1 << 32;", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("var x = 1 << -1;", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("var x = 1.5 << 2;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("var x = 1 << 2.5;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("var x = 1e20 << 2;", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
  }

  @Test(timeout = 4000)
  public void testBitwiseNotBoundaries() {
    testError("var x = ~1.5;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("var x = ~1e20;", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testError("var x = ~[];", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
    testError("var x = -[];", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
  }

  @Test(timeout = 4000)
  public void testComparisonsBoundaries() {
    test("var x = void 0 == void 0;", "var x = true;");
    test("var x = void 0 != void 0;", "var x = false;");
    test("var x = void 0 === void 0;", "var x = true;");
    test("var x = void 0 !== void 0;", "var x = false;");
    test("var x = void 0 == null;", "var x = true;");
    test("var x = void 0 === null;", "var x = false;");
    test("var x = void 0 < 1;", "var x = false;");
    test("var x = 1 > void 0;", "var x = false;");

    test("var x = null == null;", "var x = true;");
    test("var x = null != null;", "var x = false;");
    test("var x = null === null;", "var x = true;");
    test("var x = null !== null;", "var x = false;");

    test("var x = true == true;", "var x = true;");
    test("var x = true != false;", "var x = true;");
    test("var x = true < false;", "var x = false;");
    test("var x = false < true;", "var x = true;");
    test("var x = true <= true;", "var x = true;");
    test("var x = true > false;", "var x = true;");
    test("var x = true >= true;", "var x = true;");
    test("var x = true == undefined;", "var x = false;");

    test("var x = 'abc' == 'abc';", "var x = true;");
    test("var x = 'abc' != 'def';", "var x = true;");
    test("var x = 'abc' === 'abc';", "var x = true;");
    test("var x = 'abc' !== 'def';", "var x = true;");
    test("var x = 'abc' == undefined;", "var x = false;");
    test("var x = 'abc' == null;", "var x = false;");
    testSame("var x = 'abc' == 1;");
    testSame("var x = '\\v' == '\\v';");

    test("var x = 1 == 1;", "var x = true;");
    test("var x = 1 != 2;", "var x = true;");
    test("var x = 1 === 1;", "var x = true;");
    test("var x = 1 !== 2;", "var x = true;");
    test("var x = 1 < 2;", "var x = true;");
    test("var x = 2 <= 1;", "var x = false;");
    test("var x = 2 > 1;", "var x = true;");
    test("var x = 1 >= 2;", "var x = false;");
    test("var x = 1 == undefined;", "var x = false;");
    test("var x = 1 == null;", "var x = false;");

    test("var x = this == this;", "var x = true;");
    test("var x = this === this;", "var x = true;");
    test("var x = this != this;", "var x = false;");
    test("var x = this !== this;", "var x = false;");
    testSame("var x = this < this;");
    testSame("var x = this == y;");

    test("var x = y < y;", "var x = false;");
    test("var x = y > y;", "var x = false;");
    testSame("var x = y == y;");
    testSame("var x = y < z;");
    test("var x = undefined == undefined;", "var x = true;");
    test("var x = undefined == null;", "var x = true;");
    test("var x = undefined === null;", "var x = false;");

    test("var x = (-1) == undefined;", "var x = false;");
    test("var x = (-1) == null;", "var x = false;");
    test("var x = [] == undefined;", "var x = false;");
    test("var x = [] != null;", "var x = true;");
    test("var x = ({}) == undefined;", "var x = false;");
    test("var x = (/abc/) == null;", "var x = false;");
    test("var x = (function() {}) == undefined;", "var x = false;");
    test("var x = !0 == true;", "var x = true;");
    test("var x = !1 == false;", "var x = true;");
  }

  // =================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =================================================================================================

  /**
   * Targets the known defect in PeepholeFoldConstants#tryFoldArrayAccess.
   * Accessing index 0 of an array literal ([10, 20][0]) should cleanly fold to 10.
   * In defective versions, `i < intIndex` prevents loop execution, leaving `elem == null`
   * and emitting an unexpected JSC_INDEX_OUT_OF_BOUNDS_ERROR.
   */
  @Test(timeout = 4000)
  public void testFoldGetElemDefectIndexZero() {
    test("var x = [10, 20][0];", "var x = 10;");
  }

  @Test(timeout = 4000)
  public void testFoldGetElemIndexNonZero() {
    test("var x = [10, 20][1];", "var x = 20;");
  }

  @Test(timeout = 4000)
  public void testFoldGetElemEmptySlot() {
    test("var x = [10, , 30][1];", "var x = void 0;");
  }

  @Test(timeout = 4000)
  public void testFoldGetElemOutOfBoundsNegative() {
    testError("var x = [10, 20][-1];", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

  @Test(timeout = 4000)
  public void testFoldGetElemOutOfBoundsTooLarge() {
    testError("var x = [10, 20][2];", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

  @Test(timeout = 4000)
  public void testFoldGetElemFractionalIndex() {
    testError("var x = [10, 20][0.5];", PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
  }

  @Test(timeout = 4000)
  public void testFoldGetElemNonNumberOrComplex() {
    testSame("var x = [10, 20]['prop'];");
    testSame("var x = [10, 20][y];");
  }

  // =================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =================================================================================================

  @Test(timeout = 4000)
  public void testAssignmentTargetGuards() {
    testSame("[1, 2][0] += 1;");
    testSame("[1, 2][0]++;");
    testSame("[1, 2][0]--;");
    testSame("({a: 1}).a += 1;");
    testSame("({a: 1}).a++;");
    testSame("({a: 1}).a--;");
  }

  @Test(timeout = 4000)
  public void testUnarySpecialValues() {
    test("var x = - -5;", "var x = 5;");
    test("var x = -NaN;", "var x = NaN;");
    testSame("var x = -Infinity;");
    test("var x = +1;", "var x = 1;");
  }

  @Test(timeout = 4000)
  public void testReduceVoidVariations() {
    test("void 1;", "void 0;");
    test("void 'foo';", "void 0;");
    testSame("void 0;");
    testSame("void foo();");
  }

  @Test(timeout = 4000)
  public void testOperandConversionsForArithmeticAndAssign() {
    test("var x = true - false;", "var x = 1;");
    test("var x = null - 0;", "var x = 0;");
    test("var x = ~true;", "var x = -2;");
    test("x -= '5';", "x -= 5;", true);
    test("x -= (foo ? '5' : '6');", "x -= (foo ? 5 : 6);", true);
    test("x -= (foo, '5');", "x -= (foo, 5);", true);
    test("x -= (foo && '5');", "x -= (foo && 5);", true);
    test("x -= (foo || '5');", "x -= (foo || 5);", true);
    test("x -= undefined;", "x -= NaN;", true);
  }

  @Test(timeout = 4000)
  public void testLeftChildAssociativeFolding() {
    test("var x = foo() * 2 * 3;", "var x = foo() * 6;");
    test("var x = foo() & 1 & 3;", "var x = foo() & 1;");
    test("var x = foo() | 1 | 2;", "var x = foo() | 3;");
    test("var x = foo() ^ 1 ^ 3;", "var x = foo() ^ 2;");
    test("var x = 2 * foo() * 3;", "var x = foo() * 6;");
  }

  @Test(timeout = 4000)
  public void testDirectNodeSubtreeHandling() {
    PeepholeFoldConstants fold = new PeepholeFoldConstants(false);
    Node nameNode = IR.name("standaloneName");
    Node result = fold.optimizeSubtree(nameNode);
    assertSame(nameNode, result);
  }

  // =================================================================================================
  // Partition E: Object Lifecycle & Late Mode Invariants
  // =================================================================================================

  @Test(timeout = 4000)
  public void testNotOperatorLateModeGuard() {
    test("var x = !0;", "var x = true;", false);
    testSame("var x = !0;", true);
    testSame("var x = !1;", true);
    test("var x = !2;", "var x = false;", true);
  }

  @Test(timeout = 4000)
  public void testAssignFoldingLateMode() {
    test("x = x + y;", "x += y;", true);
    test("x = y + x;", "x += y;", true);
    test("x = x - y;", "x -= y;", true);
    test("x = x * y;", "x *= y;", true);
    test("x = x / y;", "x /= y;", true);
    test("x = x % y;", "x %= y;", true);
    test("x = x & y;", "x &= y;", true);
    test("x = x | y;", "x |= y;", true);
    test("x = x ^ y;", "x ^= y;", true);
    test("x = x << y;", "x <<= y;", true);
    test("x = x >> y;", "x >>= y;", true);
    test("x = x >>> y;", "x >>>= y;", true);
    testSame("x = x + y;", false);
    testSame("x = y - x;", true);
    testSame("foo() = foo() + y;", true);
  }

  @Test(timeout = 4000)
  public void testAssignUnfoldingEarlyMode() {
    test("x += y;", "x = x + y;", false);
    test("x -= y;", "x = x - y;", false);
    test("x *= y;", "x = x * y;", false);
    testSame("x += y;", true);
    testSame("foo() += y;", false);
  }
}