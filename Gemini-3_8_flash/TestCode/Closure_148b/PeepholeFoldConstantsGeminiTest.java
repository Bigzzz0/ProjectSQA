package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.PeepholeFoldConstants
 *
 * Decision / Branch Zones Analyzed:
 * 1. optimizeSubtree dispatch:
 *    - CALL: tryFoldKnownMethods (String.join, String.indexOf, String.lastIndexOf)
 *    - TYPEOF: tryFoldTypeof (string, number, boolean, object, undefined, void, function)
 *    - NOT / NEG / BITNOT: tryFoldUnaryOperator (unused expression removal, boolean inversion,
 *      Infinity/NaN edge cases, double negation, bitwise not, boundary / fractional / non-numeric errors)
 *    - Default: tryFoldBinaryOperator (GETPROP, GETELEM, INSTANCEOF, AND/OR, BITAND/BITOR,
 *      LSH/RSH/URSH, ASSIGN, ADD, SUB/MUL/DIV, LT/GT/LE/GE/EQ/NE/SHEQ/SHNE)
 *
 * 2. Targeted Defect (Defects4J ground truth: PeepholeFoldConstantsTest::testFoldTypeof):
 *    - tryFoldTypeof on `typeof void 0` and `typeof function() {}`
 *    - In the defective version, `tryFoldTypeof` only handles a subset of literal types in its switch statement
 *      and fails to fold expressions like `typeof void 0` -> "undefined" or `typeof function() {}` -> "function".
 *
 * 3. Error Reporting Paths:
 *    - DIVIDE_BY_0_ERROR on arithmetic division by 0
 *    - SHIFT_AMOUNT_OUT_OF_BOUNDS for shift amounts < 0 or >= 32
 *    - FRACTIONAL_BITWISE_OPERAND for fractional operands in shifts and bitwise NOT/AND/OR
 *    - BITWISE_OPERAND_OUT_OF_RANGE for operands exceeding [Integer.MIN_VALUE, Integer.MAX_VALUE]
 *    - NEGATING_A_NON_NUMBER_ERROR for negating non-numeric literals
 *    - INDEX_OUT_OF_BOUNDS_ERROR and INVALID_GETELEM_INDEX_ERROR on array indexing
 *
 * 4. Boundary Values:
 *    - MAX_FOLD_NUMBER (2^53 boundary in arithmetic folding)
 *    - 32-bit shift boundary (shift by 0, 31, 32, -1)
 *    - Empty arrays in join, single element join, multi-element join
 *    - Equality and comparison of null, undefined, void 0, this, and NaN
 */
public class PeepholeFoldConstantsGeminiTest {

  private Node foldAndGetFirstChild(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return root.getFirstChild();
  }

  private Node foldAndGetValue(String js) {
    Node statement = foldAndGetFirstChild(js);
    assertNotNull("Parsed statement must not be null", statement);
    if (statement.getType() == Token.VAR) {
      Node nameNode = statement.getFirstChild();
      assertNotNull("Var must have name child", nameNode);
      return nameNode.getFirstChild();
    } else if (statement.getType() == Token.EXPR_RESULT) {
      return statement.getFirstChild();
    }
    return statement;
  }

  private Compiler foldWithCompiler(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return compiler;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldTypeof_defectTargetVoid() {
    // Specifically targets testFoldTypeof failure on `typeof void 0` -> "undefined"
    Node val = foldAndGetValue("var x = typeof void 0;");
    assertNotNull("Folded node should exist", val);
    assertEquals("typeof void 0 should fold to a string literal", Token.STRING, val.getType());
    assertEquals("undefined", val.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeof_defectTargetFunction() {
    // Specifically targets testFoldTypeof failure on `typeof function() {}` -> "function"
    Node val = foldAndGetValue("var x = typeof function() {};");
    assertNotNull("Folded node should exist", val);
    assertEquals("typeof function() {} should fold to a string literal", Token.STRING, val.getType());
    assertEquals("function", val.getString());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldTypeof_standardLiterals() {
    Node valString = foldAndGetValue("var x = typeof 'hello';");
    assertEquals(Token.STRING, valString.getType());
    assertEquals("string", valString.getString());

    Node valNumber = foldAndGetValue("var x = typeof 42;");
    assertEquals(Token.STRING, valNumber.getType());
    assertEquals("number", valNumber.getString());

    Node valTrue = foldAndGetValue("var x = typeof true;");
    assertEquals(Token.STRING, valTrue.getType());
    assertEquals("boolean", valTrue.getString());

    Node valFalse = foldAndGetValue("var x = typeof false;");
    assertEquals(Token.STRING, valFalse.getType());
    assertEquals("boolean", valFalse.getString());

    Node valNull = foldAndGetValue("var x = typeof null;");
    assertEquals(Token.STRING, valNull.getType());
    assertEquals("object", valNull.getString());

    Node valObject = foldAndGetValue("var x = typeof {};");
    assertEquals(Token.STRING, valObject.getType());
    assertEquals("object", valObject.getString());

    Node valArray = foldAndGetValue("var x = typeof [];");
    assertEquals(Token.STRING, valArray.getType());
    assertEquals("object", valArray.getString());

    Node valUndefined = foldAndGetValue("var x = typeof undefined;");
    assertEquals(Token.STRING, valUndefined.getType());
    assertEquals("undefined", valUndefined.getString());
  }

  @Test(timeout = 4000)
  public void testFoldUnary_not() {
    Node valTrue = foldAndGetValue("var x = !false;");
    assertEquals(Token.TRUE, valTrue.getType());

    Node valFalse = foldAndGetValue("var x = !true;");
    assertEquals(Token.FALSE, valFalse.getType());

    Node valFromZero = foldAndGetValue("var x = !0;");
    assertEquals(Token.TRUE, valFromZero.getType());

    Node valFromOne = foldAndGetValue("var x = !1;");
    assertEquals(Token.FALSE, valFromOne.getType());
  }

  @Test(timeout = 4000)
  public void testFoldUnary_unusedExpressionElimination() {
    // If the unary op is an expression statement, its operator is discarded
    Node stmt = foldAndGetFirstChild("!foo();");
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    assertEquals(Token.CALL, stmt.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testFoldUnary_neg() {
    Node valNeg = foldAndGetValue("var x = -5;");
    assertEquals(Token.NUMBER, valNeg.getType());
    assertEquals(-5.0, valNeg.getDouble(), 1e-9);

    Node valNegNeg = foldAndGetValue("var x = -(-10);");
    assertEquals(Token.NUMBER, valNegNeg.getType());
    assertEquals(10.0, valNegNeg.getDouble(), 1e-9);

    Node valNegNaN = foldAndGetValue("var x = -NaN;");
    assertEquals(Token.NAME, valNegNaN.getType());
    assertEquals("NaN", valNegNaN.getString());

    Node valNegInf = foldAndGetValue("var x = -Infinity;");
    assertEquals(Token.NEG, valNegInf.getType());
  }

  @Test(timeout = 4000)
  public void testFoldUnary_bitnot() {
    Node valBitNot = foldAndGetValue("var x = ~1;");
    assertEquals(Token.NUMBER, valBitNot.getType());
    assertEquals(-2.0, valBitNot.getDouble(), 1e-9);

    Node valBitNotZero = foldAndGetValue("var x = ~0;");
    assertEquals(Token.NUMBER, valBitNotZero.getType());
    assertEquals(-1.0, valBitNotZero.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldArithmetic_binary() {
    Node valAdd = foldAndGetValue("var x = 10 + 25;");
    assertEquals(Token.NUMBER, valAdd.getType());
    assertEquals(35.0, valAdd.getDouble(), 1e-9);

    Node valSub = foldAndGetValue("var x = 50 - 18;");
    assertEquals(Token.NUMBER, valSub.getType());
    assertEquals(32.0, valSub.getDouble(), 1e-9);

    Node valMul = foldAndGetValue("var x = 6 * 7;");
    assertEquals(Token.NUMBER, valMul.getType());
    assertEquals(42.0, valMul.getDouble(), 1e-9);

    Node valDiv = foldAndGetValue("var x = 20 / 4;");
    assertEquals(Token.NUMBER, valDiv.getType());
    assertEquals(5.0, valDiv.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldAdd_stringConcatenation() {
    Node valStr = foldAndGetValue("var x = 'foo' + 'bar';");
    assertEquals(Token.STRING, valStr.getType());
    assertEquals("foobar", valStr.getString());

    Node valStrNum = foldAndGetValue("var x = 'foo' + 1;");
    assertEquals(Token.STRING, valStrNum.getType());
    assertEquals("foo1", valStrNum.getString());

    Node valLeftChildAdd = foldAndGetValue("var x = a + 'foo' + 'bar';");
    assertEquals(Token.ADD, valLeftChildAdd.getType());
    assertEquals(Token.NAME, valLeftChildAdd.getFirstChild().getType());
    assertEquals("foobar", valLeftChildAdd.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldBitwise_andOr() {
    Node valAnd = foldAndGetValue("var x = 6 & 3;");
    assertEquals(Token.NUMBER, valAnd.getType());
    assertEquals(2.0, valAnd.getDouble(), 1e-9);

    Node valOr = foldAndGetValue("var x = 4 | 2;");
    assertEquals(Token.NUMBER, valOr.getType());
    assertEquals(6.0, valOr.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldShifts() {
    Node valLsh = foldAndGetValue("var x = 1 << 3;");
    assertEquals(Token.NUMBER, valLsh.getType());
    assertEquals(8.0, valLsh.getDouble(), 1e-9);

    Node valRsh = foldAndGetValue("var x = -8 >> 2;");
    assertEquals(Token.NUMBER, valRsh.getType());
    assertEquals(-2.0, valRsh.getDouble(), 1e-9);

    Node valUrsh = foldAndGetValue("var x = -1 >>> 31;");
    assertEquals(Token.NUMBER, valUrsh.getType());
    assertEquals(1.0, valUrsh.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldAssign_compound() {
    Node valAdd = foldAndGetValue("x = x + y;");
    assertEquals(Token.ASSIGN_ADD, valAdd.getType());

    Node valSub = foldAndGetValue("x = x - y;");
    assertEquals(Token.ASSIGN_SUB, valSub.getType());

    Node valMul = foldAndGetValue("x = x * y;");
    assertEquals(Token.ASSIGN_MUL, valMul.getType());

    Node valDiv = foldAndGetValue("x = x / y;");
    assertEquals(Token.ASSIGN_DIV, valDiv.getType());

    Node valMod = foldAndGetValue("x = x % y;");
    assertEquals(Token.ASSIGN_MOD, valMod.getType());

    Node valBitAnd = foldAndGetValue("x = x & y;");
    assertEquals(Token.ASSIGN_BITAND, valBitAnd.getType());

    Node valBitOr = foldAndGetValue("x = x | y;");
    assertEquals(Token.ASSIGN_BITOR, valBitOr.getType());

    Node valBitXor = foldAndGetValue("x = x ^ y;");
    assertEquals(Token.ASSIGN_BITXOR, valBitXor.getType());

    Node valLsh = foldAndGetValue("x = x << y;");
    assertEquals(Token.ASSIGN_LSH, valLsh.getType());

    Node valRsh = foldAndGetValue("x = x >> y;");
    assertEquals(Token.ASSIGN_RSH, valRsh.getType());

    Node valUrsh = foldAndGetValue("x = x >>> y;");
    assertEquals(Token.ASSIGN_URSH, valUrsh.getType());
  }

  @Test(timeout = 4000)
  public void testFoldLogical_andOr() {
    Node valAnd1 = foldAndGetValue("var x = true && 'abc';");
    assertEquals(Token.STRING, valAnd1.getType());
    assertEquals("abc", valAnd1.getString());

    Node valAnd2 = foldAndGetValue("var x = false && 'abc';");
    assertEquals(Token.FALSE, valAnd2.getType());

    Node valOr1 = foldAndGetValue("var x = true || 'abc';");
    assertEquals(Token.TRUE, valOr1.getType());

    Node valOr2 = foldAndGetValue("var x = false || 'abc';");
    assertEquals(Token.STRING, valOr2.getType());
    assertEquals("abc", valOr2.getString());

    Node valOrNumber = foldAndGetValue("var x = 3 || y;");
    assertEquals(Token.NUMBER, valOrNumber.getType());
    assertEquals(3.0, valOrNumber.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldLogical_controlFlowCondition() {
    Node ifStmt = foldAndGetFirstChild("if (x && true) {}");
    assertEquals(Token.IF, ifStmt.getType());
    assertEquals(Token.NAME, ifStmt.getFirstChild().getType());
    assertEquals("x", ifStmt.getFirstChild().getString());

    Node whileStmt = foldAndGetFirstChild("while (x || false) {}");
    assertEquals(Token.WHILE, whileStmt.getType());
    assertEquals(Token.NAME, whileStmt.getFirstChild().getType());
    assertEquals("x", whileStmt.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldComparison_numbers() {
    assertEquals(Token.TRUE, foldAndGetValue("var x = 1 < 2;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = 2 < 1;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 2 <= 2;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 3 > 2;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 2 >= 2;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 2 == 2;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = 2 != 2;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 2 === 2;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 2 !== 3;").getType());
  }

  @Test(timeout = 4000)
  public void testFoldComparison_strings() {
    assertEquals(Token.TRUE, foldAndGetValue("var x = 'a' == 'a';").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 'a' != 'b';").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 'a' === 'a';").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = 'a' !== 'b';").getType());
  }

  @Test(timeout = 4000)
  public void testFoldComparison_nullUndefinedThis() {
    assertEquals(Token.TRUE, foldAndGetValue("var x = null == undefined;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = null === undefined;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = void 0 == undefined;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = void 0 === undefined;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = void 0 < null;").getType());
    assertEquals(Token.TRUE, foldAndGetValue("var x = this == this;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = this != this;").getType());
    assertEquals(Token.FALSE, foldAndGetValue("var x = a < a;").getType());
  }

  @Test(timeout = 4000)
  public void testFoldInstanceof() {
    Node val1 = foldAndGetValue("var x = 'hello' instanceof Object;");
    assertEquals(Token.FALSE, val1.getType());

    Node val2 = foldAndGetValue("var x = ({}) instanceof Object;");
    assertEquals(Token.TRUE, val2.getType());

    Node val3 = foldAndGetValue("var x = [] instanceof Object;");
    assertEquals(Token.TRUE, val3.getType());
  }

  @Test(timeout = 4000)
  public void testFoldGetProp_length() {
    Node valArr = foldAndGetValue("var x = [1, 2, 3].length;");
    assertEquals(Token.NUMBER, valArr.getType());
    assertEquals(3.0, valArr.getDouble(), 1e-9);

    Node valStr = foldAndGetValue("var x = 'hello'.length;");
    assertEquals(Token.NUMBER, valStr.getType());
    assertEquals(5.0, valStr.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldGetElem() {
    Node valElem0 = foldAndGetValue("var x = [10, 20, 30][0];");
    assertEquals(Token.NUMBER, valElem0.getType());
    assertEquals(10.0, valElem0.getDouble(), 1e-9);

    Node valElem2 = foldAndGetValue("var x = [10, 20, 30][2];");
    assertEquals(Token.NUMBER, valElem2.getType());
    assertEquals(30.0, valElem2.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldKnownMethods_stringJoin() {
    Node valEmpty = foldAndGetValue("var x = [].join(',');");
    assertEquals(Token.STRING, valEmpty.getType());
    assertEquals("", valEmpty.getString());

    Node valSingle = foldAndGetValue("var x = ['a'].join(',');");
    assertEquals(Token.STRING, valSingle.getType());
    assertEquals("a", valSingle.getString());

    Node valMulti = foldAndGetValue("var x = ['a', 'b', 'c'].join('-');");
    assertEquals(Token.STRING, valMulti.getType());
    assertEquals("a-b-c", valMulti.getString());

    Node valCoerce = foldAndGetValue("var x = [1].join(',');");
    assertEquals(Token.ADD, valCoerce.getType());
    assertEquals("", valCoerce.getFirstChild().getString());
    assertEquals(1.0, valCoerce.getLastChild().getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testFoldKnownMethods_stringIndexOf() {
    Node valIdx = foldAndGetValue("var x = 'abcdef'.indexOf('cd');");
    assertEquals(Token.NUMBER, valIdx.getType());
    assertEquals(2.0, valIdx.getDouble(), 1e-9);

    Node valIdxFrom = foldAndGetValue("var x = 'abcdefcd'.indexOf('cd', 3);");
    assertEquals(Token.NUMBER, valIdxFrom.getType());
    assertEquals(6.0, valIdxFrom.getDouble(), 1e-9);

    Node valLastIdx = foldAndGetValue("var x = 'abcdefcd'.lastIndexOf('cd');");
    assertEquals(Token.NUMBER, valLastIdx.getType());
    assertEquals(6.0, valLastIdx.getDouble(), 1e-9);

    Node valLastIdxFrom = foldAndGetValue("var x = 'abcdefcd'.lastIndexOf('cd', 4);");
    assertEquals(Token.NUMBER, valLastIdxFrom.getType());
    assertEquals(2.0, valLastIdxFrom.getDouble(), 1e-9);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBVA_arithmeticMaxFoldNumber() {
    // MAX_FOLD_NUMBER is 2^53. Above that point, arithmetic should not fold
    Node valHuge = foldAndGetValue("var x = 9007199254740994 + 2;");
    // 9007199254740994 + 2 = 9007199254740996 > 2^53 (9007199254740992)
    assertEquals("Should not fold numbers exceeding 2^53", Token.ADD, valHuge.getType());
  }

  @Test(timeout = 4000)
  public void testBVA_shiftBoundaries() {
    Node valZeroShift = foldAndGetValue("var x = 16 >> 0;");
    assertEquals(Token.NUMBER, valZeroShift.getType());
    assertEquals(16.0, valZeroShift.getDouble(), 1e-9);

    Node val31Shift = foldAndGetValue("var x = 1 << 31;");
    assertEquals(Token.NUMBER, val31Shift.getType());
    assertEquals((double) Integer.MIN_VALUE, val31Shift.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testBVA_bitwiseIntMinMax() {
    Node valMin = foldAndGetValue("var x = -2147483648 & -2147483648;");
    assertEquals(Token.NUMBER, valMin.getType());
    assertEquals((double) Integer.MIN_VALUE, valMin.getDouble(), 1e-9);

    Node valMax = foldAndGetValue("var x = 2147483647 | 0;");
    assertEquals(Token.NUMBER, valMax.getType());
    assertEquals((double) Integer.MAX_VALUE, valMax.getDouble(), 1e-9);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testError_divideByZero() {
    Compiler compiler = foldWithCompiler("var x = 10 / 0;");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(PeepholeFoldConstants.DIVIDE_BY_0_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_shiftAmountOutOfBounds() {
    Compiler compilerTooLarge = foldWithCompiler("var x = 1 << 32;");
    assertEquals(1, compilerTooLarge.getErrorCount());
    assertEquals(PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS.key,
        compilerTooLarge.getErrors()[0].getType().key);

    Compiler compilerNegative = foldWithCompiler("var x = 1 << -1;");
    assertEquals(1, compilerNegative.getErrorCount());
    assertEquals(PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS.key,
        compilerNegative.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_fractionalBitwiseOperand() {
    Compiler compilerShiftLeft = foldWithCompiler("var x = 1.5 << 2;");
    assertEquals(1, compilerShiftLeft.getErrorCount());
    assertEquals(PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND.key,
        compilerShiftLeft.getErrors()[0].getType().key);

    Compiler compilerShiftRight = foldWithCompiler("var x = 1 << 2.5;");
    assertEquals(1, compilerShiftRight.getErrorCount());
    assertEquals(PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND.key,
        compilerShiftRight.getErrors()[0].getType().key);

    Compiler compilerBitNot = foldWithCompiler("var x = ~1.5;");
    assertEquals(1, compilerBitNot.getErrorCount());
    assertEquals(PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND.key,
        compilerBitNot.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_bitwiseOperandOutOfRange() {
    Compiler compilerShift = foldWithCompiler("var x = 1e12 << 2;");
    assertEquals(1, compilerShift.getErrorCount());
    assertEquals(PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE.key,
        compilerShift.getErrors()[0].getType().key);

    Compiler compilerBitNot = foldWithCompiler("var x = ~1e12;");
    assertEquals(1, compilerBitNot.getErrorCount());
    assertEquals(PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE.key,
        compilerBitNot.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_negatingNonNumber() {
    Compiler compiler = foldWithCompiler("var x = -'abc';");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR.key,
        compiler.getErrors()[0].getType().key);

    Compiler compilerBitNot = foldWithCompiler("var x = ~'abc';");
    assertEquals(1, compilerBitNot.getErrorCount());
    assertEquals(PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR.key,
        compilerBitNot.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_arrayIndexOutOfBounds() {
    Compiler compilerNegative = foldWithCompiler("var x = [1, 2][-1];");
    assertEquals(1, compilerNegative.getErrorCount());
    assertEquals(PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR.key,
        compilerNegative.getErrors()[0].getType().key);

    Compiler compilerTooLarge = foldWithCompiler("var x = [1, 2][5];");
    assertEquals(1, compilerTooLarge.getErrorCount());
    assertEquals(PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR.key,
        compilerTooLarge.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_invalidGetElemIndex() {
    Compiler compiler = foldWithCompiler("var x = [1, 2][1.5];");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPeepholeFoldConstants_instantiationAndPassIntegration() {
    PeepholeFoldConstants peephole = new PeepholeFoldConstants();
    assertNotNull(peephole);

    Compiler compiler = new Compiler();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    Node root = compiler.parseTestCode("var a = 1 + 2;");
    pass.process(null, root);

    Node val = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, val.getType());
    assertEquals(3.0, val.getDouble(), 1e-9);
  }

  @Test(timeout = 4000)
  public void testNoFold_sideEffectsPreserved() {
    Node valAssign = foldAndGetValue("foo() = foo() + 1;");
    assertEquals(Token.ASSIGN, valAssign.getType());

    Node valProp = foldAndGetValue("var x = [foo()].length;");
    assertEquals(Token.GETPROP, valProp.getType());
  }
}