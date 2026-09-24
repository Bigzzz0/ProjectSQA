/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.PeepholeFoldConstants
 *
 * 1. Defects4J Known Defect (Issue 522):
 *    - Method: tryFoldArrayAccess(Node n, Node left, Node right)
 *    - Branch: When intIndex >= array.length, elem becomes null -> error(INDEX_OUT_OF_BOUNDS_ERROR, right)
 *    - Flaw: Accessing an out-of-bounds index (e.g. [][1] or [0][1]) in JavaScript evaluates to undefined;
 *            it should not emit a compiler error (JSC_INDEX_OUT_OF_BOUNDS_ERROR).
 *    - Target: testIssue522, testIssue522NonEmptyArrayOutOfBounds.
 *
 * 2. Unary Operators Branch Coverage:
 *    - NOT: !0 and !1 preserved; !2 -> false; !"" -> true; !"hello" -> false; !true -> false.
 *    - POS: numeric result preserved and stripped; non-numeric kept.
 *    - NEG: -Infinity preserved; -NaN -> NaN; numeric negation; non-number -> NEGATING_A_NON_NUMBER_ERROR.
 *    - BITNOT: int in range -> ~val; non-int -> FRACTIONAL_BITWISE_OPERAND; out-of-range -> BITWISE_OPERAND_OUT_OF_RANGE; non-number -> NEGATING_A_NON_NUMBER_ERROR.
 *
 * 3. Binary Operators Branch Coverage:
 *    - Bitwise shifts (LSH, RSH, URSH): valid shifts, out-of-range operands, fractional operands, shift amount [0, 32).
 *    - Arithmetic (ADD, SUB, MUL, DIV, MOD): division/modulo by zero, string concatenation vs numeric add,
 *      commutative/associative left-child folding (e.g., foo() * 2 * 3), MAX_FOLD_NUMBER boundary (2^53).
 *    - Assignment conversions: x = x + y -> x += y for commutative and non-commutative operators.
 *    - Logical (AND, OR): short-circuit folding for pure/impure left values (true || x -> true, false && x -> false).
 *    - Comparisons (EQ, NE, SHEQ, SHNE, LT, LE, GT, GE): undefined/null equality, boolean vs number, string comparison, this comparison.
 *
 * 4. Property and Element Access Branch Coverage:
 *    - Array length: [1, 2].length -> 2; "hello".length -> 5; side-effect array lit kept.
 *    - Object property access: static literal lookup, GET getter -> CALL, side-effects guard, assignment target guard.
 *    - Array index access: valid index lookup, sparse array empty slot (newUndefinedNode), fractional index error, negative index error.
 *    - Typeof: typeof literal expressions for string, number, boolean, object, function, undefined.
 *    - Void: void 1 -> void 0; side-effect void kept.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsGeminiTest {

  private Compiler compiler;

  private Node fold(String js) {
    compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return root;
  }

  private Node getFirstExpression(Node root) {
    assertNotNull("Root cannot be null", root);
    Node script = root.getFirstChild();
    assertNotNull("Script child cannot be null", script);
    Node stmt = script.getFirstChild();
    assertNotNull("Statement child cannot be null", stmt);
    if (stmt.getType() == Token.EXPR_RESULT) {
      return stmt.getFirstChild();
    }
    return stmt;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldArithmeticBasicOperations() {
    Node root = fold("x = 10 + 20;");
    Node assign = getFirstExpression(root);
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(30.0, assign.getLastChild().getDouble(), 0.0);

    root = fold("x = 20 - 5;");
    assign = getFirstExpression(root);
    assertEquals(15.0, assign.getLastChild().getDouble(), 0.0);

    root = fold("x = 4 * 5;");
    assign = getFirstExpression(root);
    assertEquals(20.0, assign.getLastChild().getDouble(), 0.0);

    root = fold("x = 20 / 4;");
    assign = getFirstExpression(root);
    assertEquals(5.0, assign.getLastChild().getDouble(), 0.0);

    root = fold("x = 7 % 4;");
    assign = getFirstExpression(root);
    assertEquals(3.0, assign.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldBitwiseOperations() {
    Node root = fold("x = 6 & 3;");
    assertEquals(2.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);

    root = fold("x = 6 | 3;");
    assertEquals(7.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);

    root = fold("x = 6 ^ 3;");
    assertEquals(5.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);

    root = fold("x = ~0;");
    assertEquals(-1.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldShiftOperations() {
    Node root = fold("x = 1 << 3;");
    assertEquals(8.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);

    root = fold("x = 16 >> 2;");
    assertEquals(4.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);

    root = fold("x = -1 >>> 0;");
    assertEquals(4294967295.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldStringConcatenation() {
    Node root = fold("x = 'a' + 'b';");
    assertEquals("ab", getFirstExpression(root).getLastChild().getString());

    root = fold("x = 'a' + 1;");
    assertEquals("a1", getFirstExpression(root).getLastChild().getString());

    root = fold("x = 1 + 'b';");
    assertEquals("1b", getFirstExpression(root).getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldLeftChildAssociativeExpressions() {
    Node root = fold("x = foo() * 2 * 3;");
    Node assign = getFirstExpression(root);
    Node mul = assign.getLastChild();
    assertEquals(Token.MUL, mul.getType());
    assertEquals(6.0, mul.getLastChild().getDouble(), 0.0);

    root = fold("x = 2 * foo() * 3;");
    assign = getFirstExpression(root);
    mul = assign.getLastChild();
    assertEquals(Token.MUL, mul.getType());
    assertEquals(6.0, mul.getLastChild().getDouble(), 0.0);

    root = fold("x = foo() + 'a' + 'b';");
    assign = getFirstExpression(root);
    Node add = assign.getLastChild();
    assertEquals(Token.ADD, add.getType());
    assertEquals("ab", add.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldLogicalShortCircuit() {
    Node root = fold("x = true || y;");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = false || y;");
    assertEquals("y", getFirstExpression(root).getLastChild().getString());

    root = fold("x = true && y;");
    assertEquals("y", getFirstExpression(root).getLastChild().getString());

    root = fold("x = false && y;");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = 3 || y;");
    assertEquals(3.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldComparisonNodes() {
    Node root = fold("x = 1 < 2;");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = 2 < 1;");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = 'a' === 'a';");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = 'a' === 'b';");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = null == undefined;");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = null === undefined;");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = this === this;");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = this !== this;");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofLiterals() {
    assertEquals("number", getFirstExpression(fold("x = typeof 123;")).getLastChild().getString());
    assertEquals("string", getFirstExpression(fold("x = typeof 'hello';")).getLastChild().getString());
    assertEquals("boolean", getFirstExpression(fold("x = typeof true;")).getLastChild().getString());
    assertEquals("boolean", getFirstExpression(fold("x = typeof false;")).getLastChild().getString());
    assertEquals("object", getFirstExpression(fold("x = typeof null;")).getLastChild().getString());
    assertEquals("object", getFirstExpression(fold("x = typeof {};")).getLastChild().getString());
    assertEquals("object", getFirstExpression(fold("x = typeof [];")).getLastChild().getString());
    assertEquals("undefined", getFirstExpression(fold("x = typeof void 0;")).getLastChild().getString());
    assertEquals("undefined", getFirstExpression(fold("x = typeof undefined;")).getLastChild().getString());
    assertEquals("function", getFirstExpression(fold("x = typeof function(){};")).getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldInstanceofLiterals() {
    assertEquals(Token.FALSE, getFirstExpression(fold("x = 5 instanceof Object;")).getLastChild().getType());
    assertEquals(Token.FALSE, getFirstExpression(fold("x = 'str' instanceof Object;")).getLastChild().getType());
    assertEquals(Token.FALSE, getFirstExpression(fold("x = true instanceof Object;")).getLastChild().getType());
    assertEquals(Token.TRUE, getFirstExpression(fold("x = ({}) instanceof Object;")).getLastChild().getType());
    assertEquals(Token.TRUE, getFirstExpression(fold("x = [] instanceof Object;")).getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testFoldAssignmentsToCompound() {
    assertEquals(Token.ASSIGN_ADD, getFirstExpression(fold("x = x + 1;")).getType());
    assertEquals(Token.ASSIGN_ADD, getFirstExpression(fold("x = 1 + x;")).getType());
    assertEquals(Token.ASSIGN_SUB, getFirstExpression(fold("x = x - 1;")).getType());
    assertEquals(Token.ASSIGN_MUL, getFirstExpression(fold("x = x * 2;")).getType());
    assertEquals(Token.ASSIGN_DIV, getFirstExpression(fold("x = x / 2;")).getType());
    assertEquals(Token.ASSIGN_MOD, getFirstExpression(fold("x = x % 2;")).getType());
    assertEquals(Token.ASSIGN_BITAND, getFirstExpression(fold("x = x & 1;")).getType());
    assertEquals(Token.ASSIGN_BITOR, getFirstExpression(fold("x = x | 1;")).getType());
    assertEquals(Token.ASSIGN_BITXOR, getFirstExpression(fold("x = x ^ 1;")).getType());
    assertEquals(Token.ASSIGN_LSH, getFirstExpression(fold("x = x << 1;")).getType());
    assertEquals(Token.ASSIGN_RSH, getFirstExpression(fold("x = x >> 1;")).getType());
    assertEquals(Token.ASSIGN_URSH, getFirstExpression(fold("x = x >>> 1;")).getType());
  }

  @Test(timeout = 4000)
  public void testFoldArrayAndStringLength() {
    assertEquals(3.0, getFirstExpression(fold("x = [1, 2, 3].length;")).getLastChild().getDouble(), 0.0);
    assertEquals(0.0, getFirstExpression(fold("x = [].length;")).getLastChild().getDouble(), 0.0);
    assertEquals(5.0, getFirstExpression(fold("x = 'hello'.length;")).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldObjectPropertyAccess() {
    assertEquals(10.0, getFirstExpression(fold("x = ({a: 10}).a;")).getLastChild().getDouble(), 0.0);
    assertEquals(10.0, getFirstExpression(fold("x = ({a: 10})['a'];")).getLastChild().getDouble(), 0.0);
    assertEquals(20.0, getFirstExpression(fold("x = ({a: 10, a: 20}).a;")).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldArrayElementValidAccess() {
    assertEquals(20.0, getFirstExpression(fold("x = [10, 20, 30][1];")).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldArrayEmptySlotAccess() {
    Node root = fold("x = [, 20][0];");
    Node assign = getFirstExpression(root);
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testFoldReduceVoid() {
    Node root = fold("void 1;");
    Node expr = getFirstExpression(root);
    assertEquals(Token.VOID, expr.getType());
    assertEquals(0.0, expr.getFirstChild().getDouble(), 0.0);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldUnaryNotPreservesZeroAndOne() {
    // According to JS minification rules, !0 and !1 are already optimal representations of true/false
    Node root = fold("x = !0;");
    assertEquals(Token.NOT, getFirstExpression(root).getLastChild().getType());

    root = fold("x = !1;");
    assertEquals(Token.NOT, getFirstExpression(root).getLastChild().getType());

    root = fold("x = !2;");
    assertEquals(Token.FALSE, getFirstExpression(root).getLastChild().getType());

    root = fold("x = !'';");
    assertEquals(Token.TRUE, getFirstExpression(root).getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testFoldUnaryNegationSpecialValues() {
    Node root = fold("x = -NaN;");
    assertEquals("NaN", getFirstExpression(root).getLastChild().getString());

    root = fold("x = -Infinity;");
    assertEquals(Token.NEG, getFirstExpression(root).getLastChild().getType());
    assertEquals("Infinity", getFirstExpression(root).getLastChild().getFirstChild().getString());

    root = fold("x = - -5;");
    assertEquals(5.0, getFirstExpression(root).getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testArithmeticDivisionByZeroDoesNotFold() {
    Node root = fold("x = 10 / 0;");
    assertEquals(Token.DIV, getFirstExpression(root).getLastChild().getType());

    root = fold("x = 10 % 0;");
    assertEquals(Token.MOD, getFirstExpression(root).getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testArithmeticExceedingMaxFoldNumberDoesNotFold() {
    // 2^53 is 9007199254740992; calculations exceeding 2^53 lose precision
    Node root = fold("x = 9007199254740992 * 2;");
    assertEquals(Token.MUL, getFirstExpression(root).getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testForcedStringContextCtorCall() {
    Node root = fold("this[new String('abc')];");
    Node expr = getFirstExpression(root);
    assertEquals(Token.GETELEM, expr.getType());
    assertEquals("abc", expr.getLastChild().getString());

    root = fold("this[new String()];");
    expr = getFirstExpression(root);
    assertEquals(Token.GETELEM, expr.getType());
    assertEquals("", expr.getLastChild().getString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 522)
  // =========================================================================

  /**
   * Ground Truth Defect: Issue 522
   * Accessing an out-of-bounds array element (e.g. [][1] or [0][1]) should NOT emit
   * a compiler error (JSC_INDEX_OUT_OF_BOUNDS_ERROR). In JS, array out-of-bounds access
   * is a valid runtime expression evaluating to undefined.
   *
   * On the defective version, tryFoldArrayAccess throws:
   * JSC_INDEX_OUT_OF_BOUNDS_ERROR causing compiler.getErrorCount() > 0.
   */
  @Test(timeout = 4000)
  public void testIssue522() {
    fold("x = [][1];");
    assertEquals("Unexpected error(s): JSC_INDEX_OUT_OF_BOUNDS_ERROR. Out-of-bounds access should not fail compilation.",
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue522NonEmptyArrayOutOfBounds() {
    fold("var x = [0][1];");
    assertEquals("Unexpected error(s): JSC_INDEX_OUT_OF_BOUNDS_ERROR. Out-of-bounds access should not fail compilation.",
        0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths (Diagnostic Errors)
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidGetElemIndexFractionalReportsError() {
    fold("x = [1, 2][1.5];");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_INVALID_GETELEM_INDEX_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testIndexOutOfBoundsNegativeReportsError() {
    fold("x = [1, 2][-1];");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_INDEX_OUT_OF_BOUNDS_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testBitwiseOperandOutOfRangeReportsError() {
    fold("x = 10000000000000 << 2;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_BITWISE_OPERAND_OUT_OF_RANGE", compiler.getErrors()[0].getType().key);

    fold("x = ~10000000000000;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_BITWISE_OPERAND_OUT_OF_RANGE", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testShiftAmountOutOfBoundsReportsError() {
    fold("x = 1 << 35;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_SHIFT_AMOUNT_OUT_OF_BOUNDS", compiler.getErrors()[0].getType().key);

    fold("x = 1 << -1;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_SHIFT_AMOUNT_OUT_OF_BOUNDS", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testFractionalBitwiseOperandReportsError() {
    fold("x = 1.5 << 2;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_FRACTIONAL_BITWISE_OPERAND", compiler.getErrors()[0].getType().key);

    fold("x = 1 << 2.5;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_FRACTIONAL_BITWISE_OPERAND", compiler.getErrors()[0].getType().key);

    fold("x = ~1.5;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_FRACTIONAL_BITWISE_OPERAND", compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Structural Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testNoFoldingWhenSideEffectsPresent() {
    // Functions with side-effects should not be folded or pruned incorrectly
    Node root = fold("x = [foo(), 2].length;");
    assertEquals(Token.GETPROP, getFirstExpression(root).getLastChild().getType());

    root = fold("void foo();");
    Node expr = getFirstExpression(root);
    assertEquals(Token.VOID, expr.getType());
    assertEquals(Token.CALL, expr.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testObjectPropAccessWithGetterFoldsToCall() {
    Node root = fold("x = ({get a() { return 1; }}).a;");
    Node assign = getFirstExpression(root);
    Node call = assign.getLastChild();
    assertEquals(Token.CALL, call.getType());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

  @Test(timeout = 4000)
  public void testAssignmentTargetNotFolded() {
    Node root = fold("({a: x}).a += 1;");
    Node op = getFirstExpression(root);
    assertEquals(Token.ASSIGN_ADD, op.getType());
    assertEquals(Token.GETPROP, op.getFirstChild().getType());

    root = fold("[][0] += 1;");
    op = getFirstExpression(root);
    assertEquals(Token.ASSIGN_ADD, op.getType());
    assertEquals(Token.GETELEM, op.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testObjectMethodReferencingThisNotFolded() {
    Node root = fold("x = ({a: function() { return this; }}).a;");
    assertEquals(Token.GETPROP, getFirstExpression(root).getLastChild().getType());
  }
}