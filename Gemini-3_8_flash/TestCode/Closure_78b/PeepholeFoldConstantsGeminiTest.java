package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.PeepholeFoldConstants
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - performArithmeticOp: Case Token.DIV and Token.MOD with divisor (rval) == 0.
 *      Buggy implementation logs JSC_DIVIDE_BY_0_ERROR via error(...) call.
 *      In JavaScript, division by zero is valid (yielding +/-Infinity or NaN).
 *      Compiler passes must not report JSC_DIVIDE_BY_0_ERROR, causing unexpected error counts.
 *
 * 2. CORE FUNCTIONAL LOGIC & STATE TRANSITIONS:
 *    - Arithmetic operations: ADD, SUB, MUL, DIV, MOD, BITAND, BITOR, BITXOR.
 *    - Associative left-child folding: (x * 2 * 3 -> x * 6), (x + 2 + 3 -> x + 5), (x & 1 & 3 -> x & 1).
 *    - String additions and concats: ('a' + 'b' -> 'ab'), (x + 'a' + 'b' -> x + 'ab'), ('a' + ('b' + x) -> 'ab' + x).
 *    - Unary operators: NOT (!true -> false, !2 -> false, !0 preserved), POS (+42 -> 42),
 *      NEG (- -2 -> 2, -NaN -> NaN, -Infinity preserved), BITNOT (~0 -> -1).
 *    - Typeof folding: string, number, boolean, object, undefined, function literals.
 *    - Known methods folding:
 *      * Array.prototype.join: empty, single element non-string coercion, sparse elements.
 *      * String methods: indexOf, lastIndexOf, substr, substring, toLowerCase, toUpperCase.
 *    - Assignment conversions: (x = x + y -> x += y) including commutative (x = y + x -> x += y).
 *    - Logical short-circuiting: AND / OR with truthy / falsy constants.
 *
 * 3. BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES:
 *    - MAX_FOLD_NUMBER (2^53): Overflow values outside threshold returning Infinity or unreduced.
 *    - Shifts: Shift bounds [0, 32), URSH unsigned 32-bit conversion to Long.
 *    - Comparisons: undefined, null, boolean, string, number, this, and identical names.
 *
 * 4. DEFENSIVE & DIAGNOSTIC ERROR PATHS:
 *    - SHIFT_AMOUNT_OUT_OF_BOUNDS: shift >= 32 or < 0.
 *    - FRACTIONAL_BITWISE_OPERAND: fractional operands for shifts or BITNOT.
 *    - BITWISE_OPERAND_OUT_OF_RANGE: bitwise operands outside [Integer.MIN_VALUE, Integer.MAX_VALUE].
 *    - INVALID_GETELEM_INDEX_ERROR: non-integer array index.
 *    - INDEX_OUT_OF_BOUNDS_ERROR: negative or out-of-bounds array index.
 *    - NEGATING_A_NON_NUMBER_ERROR: negating or bitwise-not on non-number object literals.
 */
public class PeepholeFoldConstantsGeminiTest {

  // =========================================================================
  // Test Harness Helpers
  // =========================================================================

  private void test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants());
    pass.process(null, root);

    Compiler expectedCompiler = new Compiler();
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    String actualSource = compiler.toSource(root);
    String expectedSource = expectedCompiler.toSource(expectedRoot);

    assertEquals("Unexpected compiler error(s): " + Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
    assertEquals("AST folding mismatch for input: " + js, expectedSource, actualSource);
  }

  private void testSame(String js) {
    test(js, js);
  }

  private void testError(String js, DiagnosticType errorType) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants());
    pass.process(null, root);

    assertEquals("Expected error to be reported for: " + js, 1, compiler.getErrorCount());
    assertEquals("Expected diagnostic type mismatch",
        errorType.key, compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldArithmeticDivideByZeroKnownDefect() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("x = 2 / 0;");
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants());
    pass.process(null, root);

    // In JS, division by zero is legal and evaluates to Infinity.
    // Defective version incorrectly emits JSC_DIVIDE_BY_0_ERROR, resulting in getErrorCount() == 1.
    assertEquals("Unexpected error(s): JSC_DIVIDE_BY_0_ERROR reported on 2 / 0",
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFoldArithmeticModuloByZeroKnownDefect() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("x = 3 % 0;");
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants());
    pass.process(null, root);

    // In JS, modulo by zero is legal and evaluates to NaN.
    // Defective version incorrectly emits JSC_DIVIDE_BY_0_ERROR.
    assertEquals("Unexpected error(s): JSC_DIVIDE_BY_0_ERROR reported on 3 % 0",
        0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldArithmeticBasicOps() {
    test("x = 10 + 20;", "x = 30;");
    test("x = 30 - 10;", "x = 20;");
    test("x = 5 * 6;", "x = 30;");
    test("x = 10 / 2;", "x = 5;");
    test("x = 10 % 3;", "x = 1;");
    test("x = 5 & 3;", "x = 1;");
    test("x = 4 | 2;", "x = 6;");
    test("x = 7 ^ 3;", "x = 4;");
  }

  @Test(timeout = 4000)
  public void testFoldLeftChildAssociativeOps() {
    test("x = y * 2 * 3;", "x = y * 6;");
    test("x = y + 2 + 3;", "x = y + 5;");
    test("x = y & 1 & 3;", "x = y & 1;");
    test("x = y | 1 | 2;", "x = y | 3;");
    test("x = y ^ 1 ^ 1;", "x = y ^ 0;");
  }

  @Test(timeout = 4000)
  public void testFoldStringAdd() {
    test("x = 'a' + 'b';", "x = 'ab';");
    test("x = 'a' + 1;", "x = 'a1';");
    test("x = 1 + 'b';", "x = '1b';");
    test("x = y + 'a' + 'b';", "x = y + 'ab';");
    test("x = 'a' + ('b' + y);", "x = 'ab' + y;");
  }

  @Test(timeout = 4000)
  public void testFoldUnaryOps() {
    test("x = !true;", "x = false;");
    test("x = !false;", "x = true;");
    test("x = !2;", "x = false;");
    test("x = !'';", "x = true;");
    test("x = !'hello';", "x = false;");
    testSame("x = !0;"); // Guarded: !0 should not fold to true
    testSame("x = !1;"); // Guarded: !1 should not fold to false
    testSame("x = !y;"); // Unknown boolean value

    test("x = +42;", "x = 42;");
    test("x = +'42';", "x = 42;");
    testSame("x = +y;");

    test("x = -(-42);", "x = 42;");
    test("x = -NaN;", "x = NaN;");
    testSame("x = -Infinity;");

    test("x = ~0;", "x = -1;");
    test("x = ~-1;", "x = 0;");
  }

  @Test(timeout = 4000)
  public void testFoldTypeof() {
    test("x = typeof 1;", "x = 'number';");
    test("x = typeof 'foo';", "x = 'string';");
    test("x = typeof true;", "x = 'boolean';");
    test("x = typeof false;", "x = 'boolean';");
    test("x = typeof null;", "x = 'object';");
    test("x = typeof {};", "x = 'object';");
    test("x = typeof [];", "x = 'object';");
    test("x = typeof void 0;", "x = 'undefined';");
    test("x = typeof undefined;", "x = 'undefined';");
    test("x = typeof function(){};", "x = 'function';");
    testSame("x = typeof y;");
  }

  @Test(timeout = 4000)
  public void testFoldInstanceof() {
    test("x = 1 instanceof Object;", "x = false;");
    test("x = 'foo' instanceof Object;", "x = false;");
    test("x = true instanceof Object;", "x = false;");
    test("x = ({}) instanceof Object;", "x = true;");
    test("x = [] instanceof Object;", "x = true;");
    testSame("x = y instanceof Object;");
    testSame("x = ({}) instanceof CustomClass;");
  }

  @Test(timeout = 4000)
  public void testFoldAssign() {
    test("x = x + y;", "x += y;");
    test("x = y + x;", "x += y;");
    test("x = x - y;", "x -= y;");
    testSame("x = y - x;"); // Subtraction is not commutative
    test("x = x * y;", "x *= y;");
    test("x = x / y;", "x /= y;");
    test("x = x % y;", "x %= y;");
    test("x = x & y;", "x &= y;");
    test("x = x | y;", "x |= y;");
    test("x = x ^ y;", "x ^= y;");
    test("x = x << y;", "x <<= y;");
    test("x = x >> y;", "x >>= y;");
    test("x = x >>> y;", "x >>>= y;");
  }

  @Test(timeout = 4000)
  public void testFoldAndOr() {
    test("x = true && y;", "x = y;");
    test("x = false && y;", "x = false;");
    test("x = true || y;", "x = true;");
    test("x = false || y;", "x = y;");
    test("x = 1 && y;", "x = y;");
    test("x = 0 && y;", "x = 0;");
    test("x = 3 || y;", "x = 3;");
    test("x = '' || y;", "x = y;");
    testSame("x = a && b;");
  }

  @Test(timeout = 4000)
  public void testFoldArrayJoin() {
    test("x = ['a', 'b', 'c'].join('');", "x = 'abc';");
    test("x = ['a', 'b', 'c'].join(',');", "x = 'a,b,c';");
    test("x = [].join();", "x = '';");
    test("x = [y].join();", "x = '' + y;");
    test("x = [1, , 2].join(',');", "x = '1,,2';");
  }

  @Test(timeout = 4000)
  public void testFoldKnownStringMethods() {
    test("x = 'ABC'.toLowerCase();", "x = 'abc';");
    test("x = 'abc'.toUpperCase();", "x = 'ABC';");
    test("x = 'abcdef'.indexOf('cd');", "x = 2;");
    test("x = 'abcdef'.indexOf('cd', 1);", "x = 2;");
    test("x = 'abcdef'.indexOf('cd', 3);", "x = -1;");
    test("x = 'abcdefcd'.lastIndexOf('cd');", "x = 6;");
    test("x = 'abcdefcd'.lastIndexOf('cd', 5);", "x = 2;");
    test("x = 'abcdef'.substr(1, 3);", "x = 'bcd';");
    test("x = 'abcdef'.substr(2);", "x = 'cdef';");
    testSame("x = 'abcdef'.substr(-1);");
    test("x = 'abcdef'.substring(1, 4);", "x = 'bcd';");
    test("x = 'abcdef'.substring(2);", "x = 'cdef';");
    testSame("x = 'abcdef'.substring(-1);");
  }

  @Test(timeout = 4000)
  public void testFoldGetElem() {
    test("x = [10, 20, 30][0];", "x = 10;");
    test("x = [10, 20, 30][1];", "x = 20;");
    test("x = [10, 20, 30][2];", "x = 30;");
    test("x = [10, , 30][1];", "x = void 0;");
    testSame("x = [10, 20][y];");
  }

  @Test(timeout = 4000)
  public void testFoldGetProp() {
    test("x = [1, 2, 3].length;", "x = 3;");
    test("x = [].length;", "x = 0;");
    test("x = 'hello'.length;", "x = 5;");
    test("x = ''.length;", "x = 0;");
    testSame("x = [foo()].length;");
  }

  @Test(timeout = 4000)
  public void testFoldReduceVoid() {
    test("x = void 1;", "x = void 0;");
    test("x = void 'foo';", "x = void 0;");
    test("x = void true;", "x = void 0;");
    testSame("x = void 0;");
    testSame("x = void foo();");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldShifts() {
    test("x = 1 << 2;", "x = 4;");
    test("x = 8 >> 2;", "x = 2;");
    test("x = -1 >>> 0;", "x = 4294967295;");
  }

  @Test(timeout = 4000)
  public void testFoldComparisons() {
    test("x = null == null;", "x = true;");
    test("x = null === null;", "x = true;");
    test("x = null != null;", "x = false;");
    test("x = null !== null;", "x = false;");
    test("x = null == undefined;", "x = true;");
    test("x = null === undefined;", "x = false;");
    test("x = null != undefined;", "x = false;");
    test("x = null !== undefined;", "x = true;");

    test("x = true == true;", "x = true;");
    test("x = true == false;", "x = false;");
    test("x = true != false;", "x = true;");
    test("x = true === true;", "x = true;");
    test("x = true !== false;", "x = true;");
    test("x = true < false;", "x = false;");
    test("x = false < true;", "x = true;");

    test("x = 'a' == 'a';", "x = true;");
    test("x = 'a' == 'b';", "x = false;");
    test("x = 'a' != 'b';", "x = true;");
    test("x = 'a' === 'a';", "x = true;");
    test("x = 'a' !== 'b';", "x = true;");
    test("x = 'a' < 'b';", "x = true;");
    test("x = 'b' > 'a';", "x = true;");
    test("x = 'a' <= 'a';", "x = true;");
    test("x = 'a' >= 'b';", "x = false;");

    test("x = 1 == 1;", "x = true;");
    test("x = 1 == 2;", "x = false;");
    test("x = 1 != 2;", "x = true;");
    test("x = 1 === 1;", "x = true;");
    test("x = 1 !== 2;", "x = true;");
    test("x = 1 < 2;", "x = true;");
    test("x = 2 <= 2;", "x = true;");
    test("x = 2 > 1;", "x = true;");
    test("x = 2 >= 3;", "x = false;");

    test("x = this == this;", "x = true;");
    test("x = this != this;", "x = false;");
    test("x = this === this;", "x = true;");
    test("x = this !== this;", "x = false;");
    testSame("x = this < this;");

    test("x = y < y;", "x = false;");
    test("x = y > y;", "x = false;");

    test("x = void 0 == undefined;", "x = true;");
    test("x = void 0 === undefined;", "x = true;");
    test("x = void 0 == null;", "x = true;");
    test("x = void 0 === null;", "x = false;");
    test("x = void 0 < undefined;", "x = false;");
  }

  @Test(timeout = 4000)
  public void testFoldExtremeArithmeticNumbers() {
    // Overflows double precision to +/-Infinity
    test("x = 1e308 * 1e308;", "x = Infinity;");
    test("x = -1e308 * 1e308;", "x = -Infinity;");
    test("x = Infinity - Infinity;", "x = NaN;");
    // Numbers exceeding MAX_FOLD_NUMBER (2^53) that are finite are guarded against folding
    testSame("x = 1e20 * 1e20;");
  }

  // =========================================================================
  // Partition D: Defensive & Diagnostic Error Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testErrorShiftAmountOutOfBounds() {
    testError("x = 1 << 32;", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("x = 1 << -1;", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
  }

  @Test(timeout = 4000)
  public void testErrorFractionalBitwiseOperand() {
    testError("x = 1.5 << 1;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("x = 1 << 1.5;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("x = ~1.5;", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

  @Test(timeout = 4000)
  public void testErrorBitwiseOperandOutOfRange() {
    testError("x = 10000000000000 << 1;", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testError("x = ~10000000000000;", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
  }

  @Test(timeout = 4000)
  public void testErrorInvalidGetElemIndex() {
    testError("x = [1, 2][1.5];", PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
  }

  @Test(timeout = 4000)
  public void testErrorIndexOutOfBounds() {
    testError("x = [1, 2][-1];", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    testError("x = [1, 2][5];", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

  @Test(timeout = 4000)
  public void testErrorNegatingNonNumber() {
    testError("x = -({});", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
    testError("x = ~({});", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Forced String Context
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldCtorCallForcedStringContext() {
    test("x = this[new String('foo')];", "x = this['foo'];");
    test("x = this[new String()];", "x = this[''];");
    testSame("x = new String('foo');"); // Outside forced string context: unchanged
  }

  @Test(timeout = 4000)
  public void testFoldOperandConversionToNumber() {
    test("x = true - false;", "x = 1;");
    test("x = true + true;", "x = 2;");
    test("x = false + null;", "x = 0;");
    test("x = ~true;", "x = -2;");
    test("x = ~null;", "x = -1;");
    test("x = -(y ? true : false);", "x = -(y ? 1 : 0);");
    test("x = -(y, true);", "x = -(y, 1);");
    test("x = -(y && true);", "x = -(y && 1);");
    test("x = -(y || true);", "x = -(y || 1);");
  }
}