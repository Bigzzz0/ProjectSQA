package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.PeepholeFoldConstants
 * Known Defects Targeted (Closure-74):
 *   - testFoldComparison3, testInvertibleOperators, testCommutativeOperators
 *   - Defect Root Cause: In tryFoldComparison(), comparisons involving non-literal operands return early
 *     unless operator is GT or LT. Consequently, equality comparisons involving 'undefined' (which is Token.NAME,
 *     not a literal according to NodeUtil.isLiteralValue) failed to fold when compared to null, undefined,
 *     numbers, or strings.
 *
 * Decision / Condition Matrix Covered:
 *   Partition A - Core Functional Logic & State Transitions:
 *     - Arithmetic operations (+, -, *, /, %) with constant numbers.
 *     - Associative/Commutative folding of left-child operations (e.g. x * 2 * 3 -> x * 6, x + 1 + 2 -> x + 3).
 *     - String concatenation folding (+), including left-child string appending (e.g. x + 'a' + 'b' -> x + 'ab').
 *     - Bitwise operations (&, |, ^, ~, <<, >>, >>>).
 *     - Logical operations (&&, ||) with truthy, falsy, and side-effect expressions.
 *     - Unary operators (!, +, -, ~) including numeric conversions and identity preserving (!0, !1).
 *     - Built-in string methods (.toLowerCase, .toUpperCase, .indexOf, .lastIndexOf, .substr, .substring).
 *     - Built-in array folding (.join, .length, [index] element access, empty array slots).
 *     - Object literal access (({a: 1}).a, ({a: 1})['a'], getter/setter handling).
 *     - typeof operator on all literal types (string, number, boolean, null, object, undefined, function).
 *     - instanceof operator on immutable primitives and Object literals.
 *     - Compound assignment conversions (x = x + y -> x += y, x = y + x -> x += y).
 *     - Void reduction (void 1 -> void 0, void 0 -> void 0).
 *
 *   Partition B - Boundary Value Analysis (BVA) & Extremes:
 *     - MAX_FOLD_NUMBER boundary (> 2^53 avoids arithmetic folding).
 *     - Division and modulo by zero guards (1 / 0, 1 % 0 remain unfolded).
 *     - Substr and substring edge cases (start < 0, length < 0, out of range, missing 2nd arg).
 *     - Empty strings and empty arrays join/length.
 *
 *   Partition C - Defect-Targeted Branch Zone (Closure-74):
 *     - Null/Undefined comparisons: null == undefined, null === undefined, undefined == null, etc.
 *     - Undefined vs string/number/boolean comparisons: "" == undefined, 0 == undefined, etc.
 *     - Commutative equality comparisons: undefined == null vs null == undefined.
 *     - Invertible comparisons: null != undefined, "" != undefined.
 *
 *   Partition D - Exception & Defensive Guard Paths:
 *     - BITWISE_OPERAND_OUT_OF_RANGE for values outside [Integer.MIN_VALUE, Integer.MAX_VALUE].
 *     - SHIFT_AMOUNT_OUT_OF_BOUNDS for shift amounts outside [0, 32).
 *     - FRACTIONAL_BITWISE_OPERAND for fractional shift and bitwise operands.
 *     - NEGATING_A_NON_NUMBER_ERROR for negating non-number types.
 *     - INVALID_GETELEM_INDEX_ERROR for fractional array indices.
 *     - INDEX_OUT_OF_BOUNDS_ERROR for negative array indices or indices >= array length.
 *
 *   Partition E - Object Lifecycle & Contract Integrity:
 *     - Direct invocation of optimizeSubtree with single/empty nodes verifying defensive null guards.
 *     - DiagnosticType constants verification.
 * ---------------------------------------------------------------------------------------------------------
 */
public class PeepholeFoldConstantsGeminiTest {

  // Helper method running PeepholeFoldConstants via PeepholeOptimizationsPass
  private void test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);

    Node expectedRoot = compiler.parseTestCode(expected);
    assertEquals(compiler.toSource(expectedRoot), compiler.toSource(root));
  }

  private void testSame(String js) {
    test(js, js);
  }

  private void testError(String js, DiagnosticType expectedError) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertTrue("Expected error to be reported", compiler.getErrorCount() > 0);
    assertEquals(expectedError.key, compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testArithmeticAddSubMulDivMod() {
    test("1 + 2", "3");
    test("10 - 4", "6");
    test("3 * 7", "21");
    test("20 / 4", "5");
    test("17 % 5", "2");
  }

  @Test(timeout = 4000)
  public void testLeftChildArithmeticFolding() {
    test("x * 2 * 3", "x * 6");
    test("x + 10 + 20", "x + 30");
    test("x & 7 & 3", "x & 3");
    test("x | 1 | 2", "x | 3");
    test("x ^ 5 ^ 5", "x ^ 0");
  }

  @Test(timeout = 4000)
  public void testStringConcatenation() {
    test("'hello ' + 'world'", "'hello world'");
    test("'a' + 1", "'a1'");
    test("2 + 'b'", "'2b'");
    test("'val: ' + true", "'val: true'");
    test("'val: ' + null", "'val: null'");
    test("foo() + 'a' + 'b'", "foo() + 'ab'");
    test("'a' + ('b' + foo())", "'ab' + foo()");
  }

  @Test(timeout = 4000)
  public void testBitwiseOperations() {
    test("5 & 3", "1");
    test("5 | 2", "7");
    test("5 ^ 3", "6");
    test("~0", "-1");
    test("~1", "-2");
    test("1 << 2", "4");
    test("8 >> 1", "4");
    test("-1 >>> 0", "4294967295");
  }

  @Test(timeout = 4000)
  public void testLogicalAndOr() {
    test("true && foo()", "foo()");
    test("false && foo()", "false");
    test("true || foo()", "true");
    test("false || foo()", "foo()");
    test("1 && foo()", "foo()");
    test("0 && foo()", "0");
    test("1 || foo()", "1");
    test("0 || foo()", "foo()");
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    test("!true", "false");
    test("!false", "true");
    test("!2", "false");
    testSame("!0");
    testSame("!1");
    test("!''", "true");
    test("!'abc'", "false");
    test("!null", "true");
    test("!void 0", "true");
    test("+10", "10");
    test("+true", "1");
    test("+false", "0");
    test("+null", "0");
    test("- -5", "5");
    test("-NaN", "NaN");
    testSame("-Infinity");
  }

  @Test(timeout = 4000)
  public void testVoidReduction() {
    test("void 1", "void 0");
    test("void 'abc'", "void 0");
    testSame("void 0");
    testSame("void foo()");
  }

  @Test(timeout = 4000)
  public void testCompoundAssignment() {
    test("x = x + y", "x += y");
    test("x = x - y", "x -= y");
    test("x = x * y", "x *= y");
    test("x = x / y", "x /= y");
    test("x = x % y", "x %= y");
    test("x = x & y", "x &= y");
    test("x = x | y", "x |= y");
    test("x = x ^ y", "x ^= y");
    test("x = x << y", "x <<= y");
    test("x = x >> y", "x >>= y");
    test("x = x >>> y", "x >>>= y");
    test("x = y + x", "x += y");
    testSame("x = y - x");
    testSame("foo().x = foo().x + 1");
  }

  @Test(timeout = 4000)
  public void testStringMethods() {
    test("'HELLO'.toLowerCase()", "'hello'");
    test("'hello'.toUpperCase()", "'HELLO'");
    test("'abcdef'.indexOf('cd')", "2");
    test("'abcdef'.indexOf('z')", "-1");
    test("'abcdef'.indexOf('cd', 1)", "2");
    test("'abcdef'.indexOf('cd', 3)", "-1");
    test("'abcabc'.lastIndexOf('c')", "5");
    test("'abcabc'.lastIndexOf('c', 4)", "2");
    test("'abcdef'.substr(2, 3)", "'cde'");
    test("'abcdef'.substr(2)", "'cdef'");
    test("'abcdef'.substring(1, 4)", "'bcd'");
    test("'abcdef'.substring(2)", "'cdef'");
  }

  @Test(timeout = 4000)
  public void testArrayMethodsAndProperties() {
    test("[1, 2, 3].length", "3");
    test("'hello'.length", "5");
    testSame("[foo()].length");
    test("[].join()", "''");
    test("['a', 'b', 'c'].join('')", "'abc'");
    test("['a', 'b', 'c'].join('-')", "'a-b-c'");
    test("[1, 2, 3][0]", "1");
    test("[1, 2, 3][2]", "3");
    test("[1,,3][1]", "void 0");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralAccess() {
    test("({a: 10}).a", "10");
    test("({'k': 20})['k']", "20");
    test("({a: 1, a: 2}).a", "2");
    test("({get a() { return 1; }}).a", "(function() { return 1; })()");
    testSame("({a: x}).a += 1");
  }

  @Test(timeout = 4000)
  public void testTypeofFolding() {
    test("typeof 'str'", "'string'");
    test("typeof 123", "'number'");
    test("typeof true", "'boolean'");
    test("typeof false", "'boolean'");
    test("typeof null", "'object'");
    test("typeof {}", "'object'");
    test("typeof []", "'object'");
    test("typeof void 0", "'undefined'");
    test("typeof undefined", "'undefined'");
    test("typeof function(){}", "'function'");
    testSame("typeof variableName");
  }

  @Test(timeout = 4000)
  public void testInstanceofFolding() {
    test("1 instanceof Object", "false");
    test("'str' instanceof Object", "false");
    test("true instanceof Object", "false");
    test("({}) instanceof Object", "true");
    test("[] instanceof Object", "true");
    testSame("({}) instanceof CustomType");
  }

  @Test(timeout = 4000)
  public void testCtorFoldingInForcedStringContext() {
    test("obj[new String('prop')]", "obj['prop']");
    test("obj[new String()]", "obj['']");
    test("obj[new String(123)]", "obj['123']");
    testSame("new String('hello')");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDivisionAndModuloByZero() {
    testSame("10 / 0");
    testSame("10 % 0");
  }

  @Test(timeout = 4000)
  public void testLargeNumberBoundaryOver2Pow53() {
    testSame("1e17 + 1");
  }

  @Test(timeout = 4000)
  public void testSubstrAndSubstringBounds() {
    testSame("'abc'.substr(-1, 2)");
    testSame("'abc'.substr(1, -2)");
    testSame("'abc'.substr(1, 10)");
    testSame("'abc'.substr(1, 2, 3)");
    testSame("'abc'.substring(-1, 2)");
    testSame("'abc'.substring(1, -2)");
    testSame("'abc'.substring(1, 10)");
    testSame("'abc'.substring(1, 2, 3)");
  }

  @Test(timeout = 4000)
  public void testEmptyBoundaries() {
    test("'' + ''", "''");
    test("''.length", "0");
    test("''.indexOf('')", "0");
    test("''.substr(0, 0)", "''");
    test("''.substring(0, 0)", "''");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-74)
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldComparison3_NullUndefined() {
    test("null == undefined", "true");
    test("null === undefined", "false");
    test("undefined == null", "true");
    test("undefined === null", "false");
  }

  @Test(timeout = 4000)
  public void testFoldComparison3_LiteralUndefined() {
    test("'' == undefined", "false");
    test("'' === undefined", "false");
    test("undefined == ''", "false");
    test("undefined === ''", "false");
    test("0 == undefined", "false");
    test("0 === undefined", "false");
    test("undefined == 0", "false");
    test("undefined === 0", "false");
    test("false == undefined", "false");
    test("false === undefined", "false");
    test("undefined == false", "false");
    test("undefined === false", "false");
  }

  @Test(timeout = 4000)
  public void testCommutativeOperators() {
    test("undefined == null", "true");
    test("null == undefined", "true");
    test("undefined === null", "false");
    test("null === undefined", "false");
  }

  @Test(timeout = 4000)
  public void testInvertibleOperators() {
    test("null != undefined", "false");
    test("null !== undefined", "true");
    test("undefined != null", "false");
    test("undefined !== null", "true");
    test("'' != undefined", "true");
    test("'' !== undefined", "true");
    test("undefined != ''", "true");
    test("undefined !== ''", "true");
  }

  @Test(timeout = 4000)
  public void testComparisonsStandard() {
    test("1 < 2", "true");
    test("2 < 1", "false");
    test("1 <= 1", "true");
    test("1 > 2", "false");
    test("2 >= 1", "true");
    test("1 == 1", "true");
    test("1 != 2", "true");
    test("1 === 1", "true");
    test("1 !== 2", "true");
    test("'a' == 'a'", "true");
    test("'a' == 'b'", "false");
    test("'a' != 'b'", "true");
    test("this == this", "true");
    test("this != this", "false");
    test("this === this", "true");
    test("this !== this", "false");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testShiftAmountOutOfBoundsError() {
    testError("1 << 32", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("1 << -1", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
  }

  @Test(timeout = 4000)
  public void testFractionalBitwiseOperandError() {
    testError("1.5 << 2", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("1 << 2.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("~1.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

  @Test(timeout = 4000)
  public void testBitwiseOperandOutOfRange() {
    testError("1e12 << 2", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testError("~1e12", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
  }

  @Test(timeout = 4000)
  public void testNegatingNonNumberError() {
    testError("~'abc'", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
    testError("-'abc'", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
  }

  @Test(timeout = 4000)
  public void testInvalidGetElemIndexError() {
    testError("[1, 2][1.5]", PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
  }

  @Test(timeout = 4000)
  public void testIndexOutOfBoundsError() {
    testError("[1, 2][-1]", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    testError("[1, 2][5]", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticTypesIntegrity() {
    assertNotNull(PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    assertNotNull(PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    assertNotNull(PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
    assertNotNull(PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    assertNotNull(PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    assertNotNull(PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

  @Test(timeout = 4000)
  public void testDirectOptimizeSubtreeDefensiveGuards() {
    PeepholeFoldConstants folder = new PeepholeFoldConstants();
    Node emptyNode = new Node(Token.EMPTY);
    Node resultEmpty = folder.optimizeSubtree(emptyNode);
    assertSame(emptyNode, resultEmpty);

    Node addWithOneChild = new Node(Token.ADD, new Node(Token.NAME, "x"));
    Node resultAdd = folder.optimizeSubtree(addWithOneChild);
    assertSame(addWithOneChild, resultAdd);
  }
}