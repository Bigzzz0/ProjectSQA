package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: FoldConstants
 *
 * Primary Defect Target:
 * - Defects4J Ground Truth: FoldConstantsTest::testStringJoinAdd
 *   Target Method: tryFoldStringJoin(NodeTraversal, Node, Node, Node, Node)
 *   Flaw: In array join folding when separator is empty (""), multiple adjacent elements
 *         with non-literal/variable children fail to fold into string concatenation (+)
 *         nodes, leaving inefficient [].join("") calls.
 *
 * Core Decision Logic & Branches Targeted:
 * 1. tryFoldStringJoin:
 *    - 0 folded children -> empty string literal ""
 *    - 1 folded child (literal string vs non-string coerced with "" + x)
 *    - >=2 children with empty join string ("") folding to Token.ADD vs preserving join()
 * 2. tryFoldRegularExpressionConstructor:
 *    - Single string pattern, flags (valid 'i', 'm', vs unsafe 'g', vs invalid)
 *    - Slash escaping (makeForwardSlashBracketSafe)
 *    - Patterns with length >= 100 or containing unicode escapes (containsUnicodeEscape)
 * 3. tryFoldLiteralConstructor:
 *    - new Array() -> [], new Object() -> {}
 *    - Local shadowing of Array/Object prevents folding
 * 4. tryFoldArithmetic & tryFoldShift & tryFoldBitAndOr:
 *    - Division by 0 error (DIVIDE_BY_0_ERROR)
 *    - Shift amounts < 0 or >= 32 (SHIFT_AMOUNT_OUT_OF_BOUNDS)
 *    - Fractional operands (FRACTIONAL_BITWISE_OPERAND)
 *    - Out of range operands (BITWISE_OPERAND_OUT_OF_RANGE)
 * 5. tryFoldComparison:
 *    - Comparisons with undefined, null, boolean, numbers, strings, this, and identical names
 * 6. tryFoldHookIf & tryMinimizeIf:
 *    - Dead branch elimination, condition inversion, duplicate statement extraction
 *    - HOOK transforms, VAR declaration folding in then/else branches
 * 7. Loops (WHILE, FOR, DO):
 *    - Always-false elimination, always-true condition stripping, break/continue guards
 * 8. Unary ops (NOT, NEG, BITNOT):
 *    - Expression statement dead operator removal
 *    - Negating non-number, Infinity/-NaN handling
 * -----------------------------------------------------------------------------------------
 */
public class FoldConstantsGeminiTest {

  // Helper: process JavaScript source and compare formatted result
  private Compiler test(String js, String expected) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    FoldConstants pass = new FoldConstants(compiler);
    pass.process(null, root);

    Compiler compilerExpected = new Compiler();
    Node expectedRoot = compilerExpected.parseTestCode(expected);

    assertEquals(compilerExpected.toSource(expectedRoot), compiler.toSource(root));
    return compiler;
  }

  // Helper: assert JavaScript is unchanged by FoldConstants
  private Compiler testSame(String js) {
    return test(js, js);
  }

  // Helper: assert expected DiagnosticType is reported
  private void testError(String js, DiagnosticType errorType) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    FoldConstants pass = new FoldConstants(compiler);
    pass.process(null, root);
    assertEquals("Expected error to be reported", 1, compiler.getErrorCount());
    assertEquals(errorType, compiler.getErrors()[0].getType());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known Defects4J failure: FoldConstantsTest::testStringJoinAdd.
   * Tests folding of Array.prototype.join('') with non-literal elements into string additions (+).
   */
  @Test(timeout = 4000)
  public void testStringJoinAddDefect() {
    test("x = ['a', 'b', c].join('')", "x = \"ab\" + c");
    test("x = [a, 'b', 'c'].join('')", "x = a + \"bc\"");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypeofFolding() {
    test("x = typeof 'hello'", "x = 'string'");
    test("x = typeof 123", "x = 'number'");
    test("x = typeof true", "x = 'boolean'");
    test("x = typeof false", "x = 'boolean'");
    test("x = typeof null", "x = 'object'");
    test("x = typeof {}", "x = 'object'");
    test("x = typeof []", "x = 'object'");
    test("x = typeof undefined", "x = 'undefined'");
    testSame("x = typeof unknownVar");
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    test("x = !true", "x = false");
    test("x = !false", "x = true");
    test("x = !0", "x = true");
    test("x = !1", "x = false");

    test("x = - -5", "x = 5");
    test("x = -0", "x = -0");
    testSame("x = -Infinity");
    test("x = -NaN", "x = NaN");

    test("x = ~0", "x = -1");
    test("x = ~1", "x = -2");
    test("x = ~-1", "x = 0");

    // Dead unary expressions discarded when result is unused
    test("!x;", "x;");
    test("-x;", "x;");
    test("~x;", "x;");
  }

  @Test(timeout = 4000)
  public void testMinimizeNot() {
    test("!(x == y)", "x != y");
    test("!(x != y)", "x == y");
    test("!(x === y)", "x !== y");
    test("!(x !== y)", "x === y");
    // Comparisons that cannot be safely inverted due to NaN
    testSame("!(x < y)");
    testSame("!(x <= y)");
    testSame("!(x > y)");
    testSame("!(x >= y)");
  }

  @Test(timeout = 4000)
  public void testLiteralConstructors() {
    test("x = new Array()", "x = []");
    test("x = new Object()", "x = {}");
    testSame("x = new Array(1, 2)");
    testSame("x = new Object('foo')");

    // Local variable shadowing prevents folding
    testSame("function f() { var Array = function(){}; var x = new Array(); }");
    testSame("function f() { var Object = function(){}; var x = new Object(); }");
  }

  @Test(timeout = 4000)
  public void testRegExpConstructor() {
    test("x = new RegExp('foobar')", "x = /foobar/");
    test("x = new RegExp('foo/bar')", "x = /foo\\/bar/");
    test("x = new RegExp('foobar', 'i')", "x = /foobar/i");
    test("x = new RegExp('foobar', 'm')", "x = /foobar/m");

    // Unsafe or unhandled flags / patterns
    testSame("x = new RegExp('foobar', 'g')"); // 'g' flag maintains state
    testSame("x = new RegExp('')"); // empty pattern avoided
    testSame("x = new RegExp('a\\u0020b')"); // unicode escape avoided
    testSame("x = new RegExp()"); // too few args
    testSame("x = new RegExp('a', 'i', 'extra')"); // too many args
  }

  @Test(timeout = 4000)
  public void testInstanceOfFolding() {
    test("x = 'hello' instanceof Object", "x = false");
    test("x = 123 instanceof Object", "x = false");
    test("x = true instanceof Object", "x = false");
    test("x = ({}) instanceof Object", "x = true");
    testSame("x = ({}) instanceof CustomClass");
    testSame("x = foo() instanceof Object");
  }

  @Test(timeout = 4000)
  public void testReduceReturn() {
    test("function f() { return undefined; }", "function f() { return; }");
    test("function f() { return void 0; }", "function f() { return; }");
    testSame("function f() { return void foo(); }");
    testSame("function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testTryFoldBlock() {
    // Remove dead statements without side effects
    test("{ 1; 2; foo(); }", "foo();");
    test("{ 'pure'; true; }", "");
    test("{ var a = 1; }", "var a = 1;");
  }

  @Test(timeout = 4000)
  public void testLogicalAndOr() {
    test("x = true || y", "x = true");
    test("x = false || y", "x = y");
    test("x = 1 || y", "x = 1");
    test("x = 0 || y", "x = y");

    test("x = true && y", "x = y");
    test("x = false && y", "x = false");
    test("x = 1 && y", "x = y");
    test("x = 0 && y", "x = 0");

    // Contextual folding in condition
    test("if (x || false) foo();", "if (x) foo();");
    test("if (x && true) foo();", "if (x) foo();");
    test("if (pure && false) foo();", "");
    test("if (pure || true) foo();", "foo();");
  }

  @Test(timeout = 4000)
  public void testHookAndIfFolding() {
    // Dead branch removal
    test("if (x) { foo(); } else { }", "if (x) { foo(); }");
    test("if (x) { } else { foo(); }", "if (!x) { foo(); }");
    test("if (foo()) { }", "foo();");
    test("if (pure) { }", "");

    // Constant condition
    test("if (true) { foo(); }", "foo();");
    test("if (false) { foo(); }", "");
    test("if (true) { foo(); } else { bar(); }", "foo();");
    test("if (false) { foo(); } else { bar(); }", "bar();");
    test("x = true ? 1 : 2", "x = 1");
    test("x = false ? 1 : 2", "x = 2");

    // Expression HOOK to IF
    test("x ? void 0 : y;", "if (!x) y;");
    test("!x ? void 0 : y;", "if (x) y;");
    test("x ? y : void 0;", "if (x) y;");
  }

  @Test(timeout = 4000)
  public void testMinimizeIf() {
    test("if (x) foo();", "x && foo();");
    test("if (!x) foo();", "x || foo();");
    test("if (x) foo(); else bar();", "x ? foo() : bar();");
    test("if (x) return 1; else return 2;", "return x ? 1 : 2;");
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");

    // Repeated statements hoisted from IF
    test("if (a) { x = 1; return true; } else { x = 2; return true; }",
         "if (a) x = 1; else x = 2; return true;");
  }

  @Test(timeout = 4000)
  public void testAssignCompoundFolding() {
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

    testSame("x = y + z");
    testSame("foo().x = foo().x + y");
  }

  @Test(timeout = 4000)
  public void testLoopFolding() {
    test("while (false) { foo(); }", "");
    testSame("while (true) { foo(); }");

    test("for (; false;) { foo(); }", "");
    test("for (; true;) { foo(); }", "for (;;) { foo(); }");
    testSame("for (var i = 0; false;) { foo(); }");

    test("do { foo(); } while (false);", "{ foo(); }");
    testSame("do { break; } while (false);");
    testSame("do { continue; } while (false);");
  }

  @Test(timeout = 4000)
  public void testArithmeticAndStringAdd() {
    test("x = 1 + 2", "x = 3");
    test("x = 5 - 2", "x = 3");
    test("x = 2 * 3", "x = 6");
    test("x = 6 / 2", "x = 3");

    test("x = 'a' + 'b'", "x = 'ab'");
    test("x = 'a' + 1", "x = 'a1'");
    test("x = 1 + 'b'", "x = '1b'");
    test("x = (foo() + 'a') + 'b'", "x = foo() + 'ab'");
    testSame("x = (foo() + 1) + 'b'");
  }

  @Test(timeout = 4000)
  public void testBitwiseAndShiftFolding() {
    test("x = 1 & 3", "x = 1");
    test("x = 1 | 2", "x = 3");

    test("x = 1 << 2", "x = 4");
    test("x = 8 >> 1", "x = 4");
    test("x = -1 >>> 1", "x = 2147483647");
  }

  @Test(timeout = 4000)
  public void testComparisonFolding() {
    test("x = 1 == 1", "x = true");
    test("x = 1 == 2", "x = false");
    test("x = 1 != 2", "x = true");
    test("x = 1 < 2", "x = true");
    test("x = 2 <= 2", "x = true");
    test("x = 3 > 2", "x = true");
    test("x = 2 >= 3", "x = false");

    test("x = 'a' == 'a'", "x = true");
    test("x = 'a' == 'b'", "x = false");
    test("x = 'a' != 'b'", "x = true");

    test("x = true == true", "x = true");
    test("x = true == false", "x = false");
    test("x = null == null", "x = true");
    test("x = null == undefined", "x = true");
    test("x = undefined == null", "x = true");
    test("x = undefined == undefined", "x = true");
    test("x = undefined === undefined", "x = true");
    test("x = undefined === null", "x = false");
    test("x = void 0 == undefined", "x = true");
    test("x = void 0 === undefined", "x = true");
    test("x = void 0 === null", "x = false");
    test("x = void 0 < 5", "x = false");

    test("x = this == this", "x = true");
    test("x = this != this", "x = false");

    test("x = y < y", "x = false");
    test("x = y > y", "x = false");
  }

  @Test(timeout = 4000)
  public void testGetPropAndGetElem() {
    test("x = [1, 2, 3].length", "x = 3");
    test("x = 'hello'.length", "x = 5");
    testSame("x = [foo()].length");

    test("x = ['a', 'b', 'c'][0]", "x = 'a'");
    test("x = ['a', 'b', 'c'][2]", "x = 'c'");
  }

  @Test(timeout = 4000)
  public void testStringIndexOfAndJoin() {
    test("x = 'abcdef'.indexOf('bc')", "x = 1");
    test("x = 'abcdef'.indexOf('bc', 2)", "x = -1");
    test("x = 'abcdefbc'.lastIndexOf('bc')", "x = 6");
    test("x = 'abcdefbc'.lastIndexOf('bc', 3)", "x = 1");

    test("x = [].join(',')", "x = ''");
    test("x = ['a'].join(',')", "x = 'a'");
    test("x = [1].join(',')", "x = '' + 1");
    test("x = ['a', 'b', 'c'].join('-')", "x = 'a-b-c'");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnicodeEscapeDetection() {
    assertTrue(FoldConstants.containsUnicodeEscape("\\u0020"));
    assertFalse(FoldConstants.containsUnicodeEscape("abc"));
    assertFalse(FoldConstants.containsUnicodeEscape("\\\\u0020")); // Escaped backslash
    assertTrue(FoldConstants.containsUnicodeEscape("\\\\\\u0020")); // Escaped backslash + unicode
  }

  @Test(timeout = 4000)
  public void testBitwiseLargeValues() {
    // Numbers outside 32-bit signed range should not be folded
    testSame("x = 3000000000 & 1");
    testSame("x = 1 | 3000000000");
  }

  @Test(timeout = 4000)
  public void testConditionMinimization() {
    test("if (!!x) foo();", "if (x) foo();");
    test("if (!(!a && !b)) foo();", "if (a || b) foo();");
    test("if (!(!a || !b)) foo();", "if (a && b) foo();");
    test("if (true) foo();", "foo();");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDivideByZeroDiagnostic() {
    testError("x = 1 / 0", FoldConstants.DIVIDE_BY_0_ERROR);
  }

  @Test(timeout = 4000)
  public void testShiftAmountOutOfBounds() {
    testError("x = 1 << -1", FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("x = 1 << 32", FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testError("x = 1 >> 35", FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
  }

  @Test(timeout = 4000)
  public void testFractionalBitwiseOperand() {
    testError("x = 1.5 << 2", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("x = 1 << 2.5", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testError("x = ~1.5", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

  @Test(timeout = 4000)
  public void testBitwiseOperandOutOfRange() {
    testError("x = 10000000000000 << 1", FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testError("x = ~10000000000000", FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
  }

  @Test(timeout = 4000)
  public void testInvalidGetElemIndex() {
    testError("x = [1, 2][1.5]", FoldConstants.INVALID_GETELEM_INDEX_ERROR);
    testError("x = [1, 2][-1]", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    testError("x = [1, 2][5]", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

  @Test(timeout = 4000)
  public void testInvalidRegExpFlags() {
    testError("x = new RegExp('foo', 'invalid')", FoldConstants.INVALID_REGULAR_EXPRESSION_FLAGS);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessWithEmptyAndSyntheticNodes() {
    Compiler compiler = new Compiler();
    FoldConstants pass = new FoldConstants(compiler);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);

    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testVisitEmptyNodeTraversal() {
    Compiler compiler = new Compiler();
    FoldConstants pass = new FoldConstants(compiler);
    Node emptyNode = new Node(Token.EMPTY);
    Node parent = new Node(Token.EXPR_RESULT, emptyNode);

    NodeTraversal t = new NodeTraversal(compiler, pass);
    pass.visit(t, emptyNode, parent);

    assertEquals(0, compiler.getErrorCount());
  }
}