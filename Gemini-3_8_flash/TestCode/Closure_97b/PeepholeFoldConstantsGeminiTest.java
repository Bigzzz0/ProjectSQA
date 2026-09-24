/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.PeepholeFoldConstants
 *
 * Decision / Condition Matrix:
 * - optimizeSubtree:
 *   - CALL: tryFoldKnownMethods (String.indexOf, String.lastIndexOf, Array.join)
 *   - TYPEOF: tryFoldTypeof (STRING, NUMBER, TRUE, FALSE, NULL, OBJECTLIT, ARRAYLIT, VOID, NAME "undefined", unhandled)
 *   - NOT, NEG, BITNOT: tryFoldUnaryOperator (Boolean folding, Infinity, NaN, non-numeric NEG, BITNOT range, bitnot fractional, expression statement dropping)
 *   - Binary: tryFoldBinaryOperator (GETPROP, GETELEM, INSTANCEOF, AND, OR, BITAND, BITOR, LSH, RSH, URSH, ASSIGN, ADD, SUB, MUL, DIV, LT, GT, LE, GE, EQ, NE, SHEQ, SHNE)
 * - tryFoldShift:
 *   - Out-of-range bitwise operand (< Integer.MIN_VALUE or > Integer.MAX_VALUE)
 *   - Shift amount out of bounds (< 0 or >= 32)
 *   - Fractional operands
 *   - LSH, RSH, URSH.
 *   - DEFECT ZONE (Defects4J testFoldBitShifts): URSH with negative operands (e.g., -1 >>> 0).
 *     Java evaluates `(int) -1 >>> 0` to `-1` (type int, widening to -1.0).
 *     JavaScript specification defines `>>>` as producing an unsigned 32-bit integer in [0, 2^32 - 1],
 *     so `-1 >>> 0` MUST evaluate to 4294967295.0.
 * - tryFoldArithmetic:
 *   - ADD, SUB, MUL, DIV
 *   - Divide by 0 diagnostic error
 *   - Length expansion prevention (> lval length + rval length + 1)
 *   - Exceeding MAX_FOLD_NUMBER (2^53)
 * - tryFoldComparison:
 *   - VOID / undefined comparisons (EQ, NE, SHEQ, SHNE, relational)
 *   - NULL, TRUE, FALSE, THIS comparisons
 *   - STRING comparisons
 *   - NUMBER comparisons
 *   - NAME comparisons (e.g. undefined, same name, different name)
 * - tryFoldStringIndexOf / lastIndexOf:
 *   - 1-argument and 2-argument calls, invalid 2nd argument, 3-arguments
 * - tryFoldStringJoin:
 *   - 0 elements, 1 element (string, non-string coercion), multiple elements with separator
 * - tryFoldGetElem:
 *   - Valid integer index, non-numeric index, fractional index, negative index, out-of-bounds index
 * - tryFoldGetProp:
 *   - Array.length (with/without side effects), String.length
 * - tryFoldAssign:
 *   - Compound assignments: +=, -=, *=, /=, %=, <<=, >>=, >>>=, &=, |=, ^=
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsGeminiTest {

  private Node fold(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return root;
  }

  private String foldToSource(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return compiler.toSource(root).trim();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J testFoldBitShifts)
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldBitShiftsUnsignedDefect() {
    // JavaScript treats `>>> 0` on negative integers as an unsigned 32-bit conversion.
    // -1 >>> 0 must fold to 4294967295, not -1.
    Node root = fold("var x = -1 >>> 0;");
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node valueNode = nameNode.getFirstChild();

    assertEquals("Node must be folded to a NUMBER", Token.NUMBER, valueNode.getType());
    assertEquals("Unsigned right shift must produce unsigned 32-bit integer 4294967295",
        4294967295.0, valueNode.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldBitShiftsSigned() {
    Node root = fold("var x = 8 >> 1; var y = 8 << 2;");
    Node var1 = root.getFirstChild();
    Node val1 = var1.getFirstChild().getFirstChild();
    assertEquals(4.0, val1.getDouble(), 0.0);

    Node var2 = var1.getNext();
    Node val2 = var2.getFirstChild().getFirstChild();
    assertEquals(32.0, val2.getDouble(), 0.0);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypeofFolding() {
    assertEquals("var x=\"string\";", foldToSource("var x = typeof 'hello';"));
    assertEquals("var x=\"number\";", foldToSource("var x = typeof 123;"));
    assertEquals("var x=\"boolean\";", foldToSource("var x = typeof true;"));
    assertEquals("var x=\"boolean\";", foldToSource("var x = typeof false;"));
    assertEquals("var x=\"object\";", foldToSource("var x = typeof null;"));
    assertEquals("var x=\"object\";", foldToSource("var x = typeof {};"));
    assertEquals("var x=\"object\";", foldToSource("var x = typeof [];"));
    assertEquals("var x=\"undefined\";", foldToSource("var x = typeof void 0;"));
    assertEquals("var x=\"undefined\";", foldToSource("var x = typeof undefined;"));
    // Non-literal typeof should remain
    assertEquals("var x=typeof foo;", foldToSource("var x = typeof foo;"));
  }

  @Test(timeout = 4000)
  public void testUnaryFolding() {
    assertEquals("var x=false;", foldToSource("var x = !true;"));
    assertEquals("var x=true;", foldToSource("var x = !false;"));
    assertEquals("var x=false;", foldToSource("var x = !1;"));
    assertEquals("var x=true;", foldToSource("var x = !0;"));
    assertEquals("var x=-5;", foldToSource("var x = -5;"));
    assertEquals("var x=-NaN;", foldToSource("var x = -NaN;"));
    assertEquals("var x=-Infinity;", foldToSource("var x = -Infinity;"));
    assertEquals("var x=-2;", foldToSource("var x = ~1;"));

    // Expression statements where unused unary ops can drop the op
    assertEquals("true;", foldToSource("!true;"));
    assertEquals("1;", foldToSource("~1;"));
  }

  @Test(timeout = 4000)
  public void testBinaryArithmeticFolding() {
    assertEquals("var x=7;", foldToSource("var x = 3 + 4;"));
    assertEquals("var x=-1;", foldToSource("var x = 3 - 4;"));
    assertEquals("var x=12;", foldToSource("var x = 3 * 4;"));
    assertEquals("var x=2;", foldToSource("var x = 8 / 4;"));
  }

  @Test(timeout = 4000)
  public void testStringConcatFolding() {
    assertEquals("var x=\"ab\";", foldToSource("var x = 'a' + 'b';"));
    assertEquals("var x=\"a1\";", foldToSource("var x = 'a' + 1;"));
    assertEquals("var x=\"1a\";", foldToSource("var x = 1 + 'a';"));
    // Left-child string concatenation: (foo() + 'a') + 'b' -> foo() + 'ab'
    assertEquals("var x=foo()+\"ab\";", foldToSource("var x = (foo() + 'a') + 'b';"));
  }

  @Test(timeout = 4000)
  public void testBitwiseAndOr() {
    assertEquals("var x=1;", foldToSource("var x = 3 & 1;"));
    assertEquals("var x=3;", foldToSource("var x = 2 | 1;"));
  }

  @Test(timeout = 4000)
  public void testLogicalAndOrFolding() {
    assertEquals("var x=foo();", foldToSource("var x = true && foo();"));
    assertEquals("var x=false;", foldToSource("var x = false && foo();"));
    assertEquals("var x=true;", foldToSource("var x = true || foo();"));
    assertEquals("var x=foo();", foldToSource("var x = false || foo();"));

    // Conditions in if statements
    assertEquals("if(x);", foldToSource("if (x && true);"));
    assertEquals("if(x);", foldToSource("if (x || false);"));
    assertEquals("if(true);", foldToSource("if (x || true);"));
    assertEquals("if(false);", foldToSource("if (x && false);"));
  }

  @Test(timeout = 4000)
  public void testComparisonFolding() {
    // Equality
    assertEquals("var x=true;", foldToSource("var x = 1 == 1;"));
    assertEquals("var x=false;", foldToSource("var x = 1 == 2;"));
    assertEquals("var x=true;", foldToSource("var x = 1 === 1;"));
    assertEquals("var x=false;", foldToSource("var x = 1 !== 1;"));
    assertEquals("var x=true;", foldToSource("var x = 'a' == 'a';"));
    assertEquals("var x=false;", foldToSource("var x = 'a' == 'b';"));
    assertEquals("var x=true;", foldToSource("var x = null == null;"));
    assertEquals("var x=false;", foldToSource("var x = null == true;"));
    assertEquals("var x=true;", foldToSource("var x = void 0 == null;"));
    assertEquals("var x=false;", foldToSource("var x = void 0 === null;"));
    assertEquals("var x=true;", foldToSource("var x = undefined == null;"));
    assertEquals("var x=false;", foldToSource("var x = undefined === null;"));

    // Relational
    assertEquals("var x=true;", foldToSource("var x = 1 < 2;"));
    assertEquals("var x=false;", foldToSource("var x = 2 < 1;"));
    assertEquals("var x=true;", foldToSource("var x = 1 <= 1;"));
    assertEquals("var x=false;", foldToSource("var x = 2 <= 1;"));
    assertEquals("var x=true;", foldToSource("var x = 2 > 1;"));
    assertEquals("var x=false;", foldToSource("var x = 1 > 2;"));
    assertEquals("var x=true;", foldToSource("var x = 2 >= 2;"));
    assertEquals("var x=false;", foldToSource("var x = 1 >= 2;"));

    // Relational undefined
    assertEquals("var x=false;", foldToSource("var x = undefined < 1;"));
    assertEquals("var x=false;", foldToSource("var x = void 0 < 1;"));
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentFolding() {
    assertEquals("x+=y;", foldToSource("x = x + y;"));
    assertEquals("x-=y;", foldToSource("x = x - y;"));
    assertEquals("x*=y;", foldToSource("x = x * y;"));
    assertEquals("x/=y;", foldToSource("x = x / y;"));
    assertEquals("x%=y;", foldToSource("x = x % y;"));
    assertEquals("x<<=y;", foldToSource("x = x << y;"));
    assertEquals("x>>=y;", foldToSource("x = x >> y;"));
    assertEquals("x>>>=y;", foldToSource("x = x >>> y;"));
    assertEquals("x&=y;", foldToSource("x = x & y;"));
    assertEquals("x|=y;", foldToSource("x = x | y;"));
    assertEquals("x^=y;", foldToSource("x = x ^ y;"));

    // Side-effects on LHS should prevent folding
    assertEquals("foo().x=foo().x+y;", foldToSource("foo().x = foo().x + y;"));
  }

  @Test(timeout = 4000)
  public void testKnownMethods() {
    // String.indexOf
    assertEquals("var x=1;", foldToSource("var x = 'abcdef'.indexOf('bc');"));
    assertEquals("var x=-1;", foldToSource("var x = 'abcdef'.indexOf('z');"));
    assertEquals("var x=6;", foldToSource("var x = 'abcdefbc'.indexOf('bc', 3);"));

    // String.lastIndexOf
    assertEquals("var x=6;", foldToSource("var x = 'abcdefbc'.lastIndexOf('bc');"));

    // Array.join
    assertEquals("var x=\"abc\";", foldToSource("var x = ['a', 'b', 'c'].join('');"));
    assertEquals("var x=\"a-b-c\";", foldToSource("var x = ['a', 'b', 'c'].join('-');"));
    assertEquals("var x=\"\";", foldToSource("var x = [].join(',');"));
    assertEquals("var x=\"foo\"+bar;", foldToSource("var x = ['foo', bar].join('');"));
  }

  @Test(timeout = 4000)
  public void testGetPropAndElem() {
    // Array length
    assertEquals("var x=3;", foldToSource("var x = [1, 2, 3].length;"));
    assertEquals("var x=0;", foldToSource("var x = [].length;"));

    // String length
    assertEquals("var x=5;", foldToSource("var x = 'hello'.length;"));

    // Array getelem
    assertEquals("var x=2;", foldToSource("var x = [1, 2, 3][1];"));
    assertEquals("var x=1;", foldToSource("var x = [1, 2, 3][0];"));
  }

  @Test(timeout = 4000)
  public void testInstanceOfFolding() {
    assertEquals("var x=false;", foldToSource("var x = 'string' instanceof Object;"));
    assertEquals("var x=false;", foldToSource("var x = 123 instanceof Object;"));
    assertEquals("var x=false;", foldToSource("var x = true instanceof Object;"));
    assertEquals("var x=true;", foldToSource("var x = {} instanceof Object;"));
    assertEquals("var x=true;", foldToSource("var x = [] instanceof Object;"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBitwiseShiftBounds() {
    // Shift amounts out of bounds [0, 32)
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = 1 << 32;");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report SHIFT_AMOUNT_OUT_OF_BOUNDS error",
        1, compiler.getErrorCount());

    Compiler compilerNeg = new Compiler();
    Node rootNeg = compilerNeg.parseTestCode("var x = 1 << -1;");
    PeepholeOptimizationsPass passNeg = new PeepholeOptimizationsPass(
        compilerNeg, new PeepholeFoldConstants());
    passNeg.process(null, rootNeg);
    assertEquals("Should report SHIFT_AMOUNT_OUT_OF_BOUNDS error for negative shift",
        1, compilerNeg.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBitwiseOperandOutOfRange() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = 999999999999999 << 1;");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report BITWISE_OPERAND_OUT_OF_RANGE",
        1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFractionalBitwiseOperand() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = 1.5 << 1;");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report FRACTIONAL_BITWISE_OPERAND",
        1, compiler.getErrorCount());

    Compiler compilerNot = new Compiler();
    Node rootNot = compilerNot.parseTestCode("var x = ~1.5;");
    PeepholeOptimizationsPass passNot = new PeepholeOptimizationsPass(
        compilerNot, new PeepholeFoldConstants());
    passNot.process(null, rootNot);
    assertEquals("Should report FRACTIONAL_BITWISE_OPERAND for fractional bitnot",
        1, compilerNot.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDivideByZeroError() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = 1 / 0;");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report DIVIDE_BY_0_ERROR", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMaxFoldNumberLimit() {
    // Numbers exceeding 2^53 should not fold
    double huge = Math.pow(2, 54);
    String js = "var x = " + (long) huge + " + 1;";
    Node root = fold(js);
    Node assignVal = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals("Huge numbers must not fold arithmetic", Token.ADD, assignVal.getType());
  }

  @Test(timeout = 4000)
  public void testGetElemOutOfBounds() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = [1, 2][5];");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report INDEX_OUT_OF_BOUNDS_ERROR", 1, compiler.getErrorCount());

    Compiler compilerNeg = new Compiler();
    Node rootNeg = compilerNeg.parseTestCode("var x = [1, 2][-1];");
    PeepholeOptimizationsPass passNeg = new PeepholeOptimizationsPass(
        compilerNeg, new PeepholeFoldConstants());
    passNeg.process(null, rootNeg);
    assertEquals("Should report INDEX_OUT_OF_BOUNDS_ERROR for negative index", 1, compilerNeg.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGetElemInvalidIndex() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = [1, 2][1.5];");
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    assertEquals("Should report INVALID_GETELEM_INDEX_ERROR", 1, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testOptimizeSubtreeDirectGuards() {
    PeepholeFoldConstants folder = new PeepholeFoldConstants();

    // Leaf node without children
    Node leafNode = new Node(Token.EMPTY);
    Node result = folder.optimizeSubtree(leafNode);
    assertSame(leafNode, result);

    // Binary node with missing right child
    Node binaryIncomplete = new Node(Token.ADD, new Node(Token.NUMBER, 1));
    Node binResult = folder.optimizeSubtree(binaryIncomplete);
    assertSame(binaryIncomplete, binResult);
  }

  @Test(timeout = 4000)
  public void testArrayLengthWithSideEffectsDoesNotFold() {
    // Array with side-effects in elements: [foo()].length should not fold
    String js = "var x = [foo()].length;";
    assertEquals("var x=[foo()].length;", foldToSource(js));
  }

  // =========================================================================
  // Partition E: Comparison Corner Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testSameNameComparison() {
    assertEquals("var x=false;", foldToSource("var x = a < a;"));
    assertEquals("var x=false;", foldToSource("var x = a > a;"));
    // a == a should not fold because NaN != NaN
    assertEquals("var x=a==a;", foldToSource("var x = a == a;"));
  }

  @Test(timeout = 4000)
  public void testThisComparisons() {
    assertEquals("var x=true;", foldToSource("var x = this == this;"));
    assertEquals("var x=false;", foldToSource("var x = this != this;"));
    assertEquals("var x=false;", foldToSource("var x = this == null;"));
  }

  @Test(timeout = 4000)
  public void testStringJoinSingleNonStringCoercion() {
    // Single non-string element in join should be coerced via "" + elem
    assertEquals("var x=\"\"+1;", foldToSource("var x = [1].join('');"));
    assertEquals("var x=\"hello\";", foldToSource("var x = ['hello'].join('');"));
  }
}