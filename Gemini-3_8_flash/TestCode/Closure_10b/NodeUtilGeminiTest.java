package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.NodeUtil
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Boolean evaluation: getPureBooleanValue, getImpureBooleanValue across STRING, NUMBER,
 *   NOT, NULL, FALSE, TRUE, VOID, NAME, REGEXP, ARRAYLIT, OBJECTLIT, ASSIGN, COMMA, AND, OR, HOOK.
 * - String conversion: getStringValue, arrayToString, getArrayElementStringValue.
 * - Number conversion: getNumberValue, getStringNumberValue, trimJsWhiteSpace, isStrWhiteSpaceChar.
 * - Function metadata & naming: getFunctionName, getNearestFunctionName, isFunctionDeclaration,
 *   isFunctionExpression, isBleedingFunctionName, isEmptyFunctionExpression, isVarArgsFunction.
 * - AST structure & analysis: isLoopStructure, getLoopCodeBlock, isWithinLoop, isControlStructure,
 *   isControlStructureCodeBlock, getConditionExpression, isStatementBlock, isStatement, isSwitchCase.
 * - Tree manipulation: removeChild, maybeAddFinally, tryMergeBlock, redeclareVarsInsideBranch.
 * - Operator semantics: isSymmetricOperation, isRelationalOperation, getInverseOperator, precedence,
 *   isAssociative, isCommutative, isAssignmentOp, getOpFromAssignmentOp, opToStr, opToStrNoFail.
 * - AST builders: newExpr, newUndefinedNode, newVarNode, newCallNode, newQualifiedNameNode,
 *   booleanNode, numberNode.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Large integers and constants: MAX_POSITIVE_INTEGER_NUMBER, LARGEST_BASIC_LATIN (0x7f vs 0x80).
 * - Hex numbers parsing: 0x0, 0x1F, invalid hex "0xG", signed hex "+0x1", "-0x1".
 * - Whitespace: Vertical tab (\u000B), BOM (\uFEFF), NBSP (\u00A0), line/paragraph separators.
 * - Names & identifiers: Latin boundary, JS keywords, qualified names, empty names.
 *
 * Partition C: Defect-Targeted Branch Zone
 * - Targeted Bug: PeepholeFoldConstantsTest::testIssue821
 * - Root Cause: NodeUtil.mayBeString(Node n, boolean recurse) historically used allResultsMatch()
 *   instead of anyResultsMatch(). For conditional (HOOK) or logical (OR, AND) expressions where one
 *   branch evaluates to a string and another does not (e.g., `cond ? "a" : 1` or `x || "str"`),
 *   allResultsMatch returned false, incorrectly classifying the expression as definitely non-string
 *   and causing invalid constant folding.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - constructorCallHasSideEffects on non-NEW node -> IllegalStateException.
 * - functionCallHasSideEffects on non-CALL node -> IllegalStateException.
 * - getOpFromAssignmentOp on non-assignment node -> IllegalArgumentException.
 * - getConditionExpression on invalid node -> IllegalArgumentException.
 * - precedence on invalid token -> Error.
 * - opToStrNoFail on invalid token -> Error.
 * - getObjectLitKeyName on non-key node -> IllegalStateException.
 * - removeChild on invalid removal context -> IllegalStateException.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - NodeUtil private constructor reflection verification.
 */

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilGeminiTest {

  private static final CodingConvention CONVENTION = new DefaultCodingConvention();

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetPureBooleanValuePrimitives() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.string("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.string("")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.number(1.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.number(-0.5)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.number(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.number(-0.0)));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.trueNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.falseNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.nullNode()));

    Node voidPure = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidPure));

    Node voidImpure = IR.voidNode(IR.call(IR.name("foo")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(voidImpure));

    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.name("Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(IR.name("otherVar")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.regexp(IR.string("abc"))));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValueAggregatesAndUnary() {
    Node pureArray = IR.arraylit(IR.number(1), IR.string("a"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(pureArray));

    Node impureArray = IR.arraylit(IR.call(IR.name("sideEffect")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(impureArray));

    Node pureObj = IR.objectlit(IR.stringKey("a", IR.number(1)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(pureObj));

    Node notTrue = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));

    Node notFalse = IR.not(IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(notFalse));

    Node notUnknown = IR.not(IR.name("unknown"));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(notUnknown));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue() {
    Node assignNode = IR.assign(IR.name("x"), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assignNode));

    Node commaNode = IR.comma(IR.number(1), IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(commaNode));

    Node andBothTrue = IR.and(IR.number(1), IR.string("yes"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(andBothTrue));

    Node andOneFalse = IR.and(IR.number(0), IR.string("yes"));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andOneFalse));

    Node orFirstTrue = IR.or(IR.number(1), IR.number(0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orFirstTrue));

    Node orBothFalse = IR.or(IR.number(0), IR.string(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(orBothFalse));

    Node hookMatching = IR.hook(IR.name("cond"), IR.number(1), IR.number(2));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookMatching));

    Node hookDiverging = IR.hook(IR.name("cond"), IR.number(1), IR.number(0));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiverging));

    Node arrayLit = IR.arraylit(IR.call(IR.name("f")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrayLit));

    Node objLit = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objLit));

    Node voidNode = IR.voidNode(IR.call(IR.name("f")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(voidNode));
  }

  @Test(timeout = 4000)
  public void testGetStringValueBasic() {
    assertEquals("foo", NodeUtil.getStringValue(IR.string("foo")));
    assertEquals("key", NodeUtil.getStringValue(IR.stringKey("key", IR.number(1))));
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    assertNull(NodeUtil.getStringValue(IR.name("other")));

    assertEquals("1", NodeUtil.getStringValue(IR.number(1.0)));
    assertEquals("1.5", NodeUtil.getStringValue(IR.number(1.5)));
    assertEquals("-2", NodeUtil.getStringValue(IR.number(-2.0)));
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    assertEquals("undefined", NodeUtil.getStringValue(IR.voidNode(IR.number(0))));
    assertEquals("[object Object]", NodeUtil.getStringValue(IR.objectlit()));

    assertEquals("false", NodeUtil.getStringValue(IR.not(IR.trueNode())));
    assertEquals("true", NodeUtil.getStringValue(IR.not(IR.falseNode())));
    assertNull(NodeUtil.getStringValue(IR.not(IR.name("x"))));
  }

  @Test(timeout = 4000)
  public void testArrayToStringConversions() {
    Node emptyArr = IR.arraylit();
    assertEquals("", NodeUtil.getStringValue(emptyArr));

    Node arr1 = IR.arraylit(IR.number(1), IR.string("abc"), IR.nullNode());
    assertEquals("1,abc,", NodeUtil.getStringValue(arr1));

    Node arrWithUndefined = IR.arraylit(IR.name("undefined"), IR.number(2));
    assertEquals(",2", NodeUtil.getStringValue(arrWithUndefined));

    Node arrWithEmptySlot = IR.arraylit(IR.empty(), IR.number(3));
    assertEquals(",3", NodeUtil.getStringValue(arrWithEmptySlot));

    Node arrUnconvertible = IR.arraylit(IR.call(IR.name("foo")));
    assertNull(NodeUtil.getStringValue(arrUnconvertible));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(IR.number(42.5)));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.voidNode(IR.number(0)))));
    assertNull(NodeUtil.getNumberValue(IR.voidNode(IR.call(IR.name("f")))));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));
    assertNull(NodeUtil.getNumberValue(IR.name("x")));

    Node negInfinity = IR.neg(IR.name("Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInfinity));

    Node negOther = IR.neg(IR.name("x"));
    assertNull(NodeUtil.getNumberValue(negOther));

    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.not(IR.trueNode())));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.not(IR.falseNode())));
    assertNull(NodeUtil.getNumberValue(IR.not(IR.name("unknown"))));

    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(IR.string("123")));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.arraylit()));
    assertEquals(Double.valueOf(5.0), NodeUtil.getNumberValue(IR.arraylit(IR.number(5))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.objectlit())));
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue() {
    assertNull(NodeUtil.getStringNumberValue("12\u000B34"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));

    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));

    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("-0x10"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));

    assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45  "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("notANumber")));
  }

  @Test(timeout = 4000)
  public void testFunctionNamingInspection() {
    // 1. function name() {}
    Node fn1 = IR.function(IR.name("fName"), IR.paramList(), IR.block());
    IR.script(fn1);
    assertEquals("fName", NodeUtil.getFunctionName(fn1));
    assertEquals("fName", NodeUtil.getNearestFunctionName(fn1));

    // 2. var vName = function() {}
    Node fn2 = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node nameNode = IR.name("vName");
    nameNode.addChildToBack(fn2);
    IR.var(nameNode);
    assertEquals("vName", NodeUtil.getFunctionName(fn2));
    assertEquals("vName", NodeUtil.getNearestFunctionName(fn2));

    // 3. qualified.prop = function() {}
    Node fn3 = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node getProp = IR.getprop(IR.name("qualified"), IR.string("prop"));
    IR.assign(getProp, fn3);
    assertEquals("qualified.prop", NodeUtil.getFunctionName(fn3));

    // 4. Object literal key: { 'keyName': function() {} }
    Node fn4 = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node keyNode = IR.stringKey("keyName", fn4);
    IR.objectlit(keyNode);
    assertEquals("keyName", NodeUtil.getNearestFunctionName(fn4));

    // 5. Getter / Setter: { get getProp() {}, set setProp(val) {} }
    Node fnGetter = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node getterNode = new Node(Token.GETTER_DEF, fnGetter);
    getterNode.setString("getProp");
    IR.objectlit(getterNode);
    assertEquals("getProp", NodeUtil.getNearestFunctionName(fnGetter));

    Node fnSetter = IR.function(IR.name(""), IR.paramList(IR.name("val")), IR.block());
    Node setterNode = new Node(Token.SETTER_DEF, fnSetter);
    setterNode.setString("setProp");
    IR.objectlit(setterNode);
    assertEquals("setProp", NodeUtil.getNearestFunctionName(fnSetter));

    assertNull(NodeUtil.getNearestFunctionName(IR.number(42)));
  }

  @Test(timeout = 4000)
  public void testImmutableAndLiteralValues() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("s")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(0)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.not(IR.trueNode())));
    assertTrue(NodeUtil.isImmutableValue(IR.voidNode(IR.number(0))));
    assertTrue(NodeUtil.isImmutableValue(IR.neg(IR.number(1))));
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));

    assertTrue(NodeUtil.isLiteralValue(IR.arraylit(IR.number(1), IR.string("a")), false));
    assertFalse(NodeUtil.isLiteralValue(IR.arraylit(IR.name("x")), false));

    Node regexp = IR.regexp(IR.string("pat"));
    assertTrue(NodeUtil.isLiteralValue(regexp, false));

    Node objLit = IR.objectlit(IR.stringKey("k", IR.number(1)));
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExpr = IR.function(IR.name(""), IR.paramList(), IR.block());
    IR.assign(IR.name("v"), fnExpr);
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test(timeout = 4000)
  public void testOperationsProperties() {
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.EQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.NE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHEQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHNE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSymmetricOperation(new Node(Token.ADD)));

    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GE)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LE)));
    assertFalse(NodeUtil.isRelationalOperation(new Node(Token.EQ)));

    assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
    assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
    assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
    assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
    assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.EQ));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
  }

  @Test(timeout = 4000)
  public void testAssignmentOperations() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MOD)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));

    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
  }

  @Test(timeout = 4000)
  public void testOperatorPrecedenceAndStrings() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN_ADD));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(6, NodeUtil.precedence(Token.BITXOR));
    assertEquals(7, NodeUtil.precedence(Token.BITAND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(8, NodeUtil.precedence(Token.SHEQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(9, NodeUtil.precedence(Token.INSTANCEOF));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(13, NodeUtil.precedence(Token.VOID));
    assertEquals(15, NodeUtil.precedence(Token.CALL));
    assertEquals(15, NodeUtil.precedence(Token.NAME));

    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.IF));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(timeout = 4000)
  public void testControlStructuresAndLoops() {
    Node forNode = new Node(Token.FOR, IR.empty(), IR.empty(), IR.empty(), IR.block());
    Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
    Node doNode = IR.doNode(IR.block(), IR.trueNode());

    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertFalse(NodeUtil.isLoopStructure(IR.ifNode(IR.trueNode(), IR.block())));

    assertEquals(forNode.getLastChild(), NodeUtil.getLoopCodeBlock(forNode));
    assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));
    assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));
    assertNull(NodeUtil.getLoopCodeBlock(IR.empty()));

    Node child = IR.name("x");
    whileNode.getLastChild().addChildToBack(child);
    assertTrue(NodeUtil.isWithinLoop(child));
    assertFalse(NodeUtil.isWithinLoop(whileNode));

    Node ifNode = IR.ifNode(IR.name("cond"), IR.block());
    assertEquals(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));
    assertEquals(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));
    assertEquals(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));

    Node for4 = new Node(Token.FOR, IR.var(IR.name("i")), IR.name("cond"), IR.inc(IR.name("i"), false), IR.block());
    assertEquals(for4.getFirstChild().getNext(), NodeUtil.getConditionExpression(for4));

    Node forIn = new Node(Token.FOR, IR.name("p"), IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));
  }

  @Test(timeout = 4000)
  public void testSideEffectAnalysis() {
    assertFalse(NodeUtil.mayHaveSideEffects(IR.number(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.string("s")));
    assertTrue(NodeUtil.mayHaveSideEffects(IR.throwNode(IR.name("e"))));

    Node newArray = new Node(Token.NEW, IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, IR.name("MyType"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node callBuiltin = IR.call(IR.name("String"), IR.number(123));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));

    Node callMathFloor = IR.call(IR.getprop(IR.name("Math"), IR.string("floor")), IR.number(1.5));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMathFloor));

    Node callToString = IR.call(IR.getprop(IR.name("obj"), IR.string("toString")));
    assertFalse(NodeUtil.functionCallHasSideEffects(callToString));

    Node callCustom = IR.call(IR.name("customFn"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callCustom));

    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
  }

  @Test(timeout = 4000)
  public void testRemoveChildScenarios() {
    // 1. Statement inside block
    Node stmt1 = IR.exprResult(IR.number(1));
    Node stmt2 = IR.exprResult(IR.number(2));
    Node block = IR.block(stmt1, stmt2);
    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertEquals(stmt2, block.getFirstChild());

    // 2. Multi-child VAR
    Node name1 = IR.name("a");
    Node name2 = IR.name("b");
    Node varNode = IR.var(name1, name2);
    block = IR.block(varNode);
    NodeUtil.removeChild(varNode, name1);
    assertEquals(1, varNode.getChildCount());

    // 3. Single-child VAR removes the VAR statement from block
    Node singleVar = IR.var(IR.name("c"));
    block = IR.block(singleVar);
    NodeUtil.removeChild(singleVar, singleVar.getFirstChild());
    assertEquals(0, block.getChildCount());

    // 4. FOR-loop with 4 children replaced with EMPTY
    Node forCond = IR.name("cond");
    Node forNode = new Node(Token.FOR, IR.empty(), forCond, IR.empty(), IR.block());
    NodeUtil.removeChild(forNode, forCond);
    assertEquals(Token.EMPTY, forNode.getChildAtIndex(1).getType());

    // 5. Block emptying
    Node blockToEmpty = IR.block(IR.exprResult(IR.number(1)), IR.exprResult(IR.number(2)));
    NodeUtil.removeChild(IR.script(blockToEmpty), blockToEmpty);
    assertEquals(0, blockToEmpty.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryFinallyAndBlockMerging() {
    Node tryBody = IR.block();
    Node catchBody = IR.block();
    Node catchNode = new Node(Token.CATCH, IR.name("e"), catchBody);
    Node catchContainer = IR.block(catchNode);
    Node tryNode = new Node(Token.TRY, tryBody, catchContainer);

    assertFalse(NodeUtil.hasFinally(tryNode));
    NodeUtil.maybeAddFinally(tryNode);
    assertTrue(NodeUtil.hasFinally(tryNode));

    // Merging block into parent statement block
    Node innerBlock = IR.block(IR.exprResult(IR.number(1)), IR.exprResult(IR.number(2)));
    Node script = IR.script(innerBlock);
    boolean merged = NodeUtil.tryMergeBlock(innerBlock);
    assertTrue(merged);
    assertEquals(2, script.getChildCount());
  }

  @Test(timeout = 4000)
  public void testFunctionPredicatesAndArguments() {
    Node fn = IR.function(IR.name("f"), IR.paramList(IR.name("p1"), IR.name("p2")), IR.block());
    IR.script(fn);

    assertTrue(NodeUtil.isFunctionDeclaration(fn));
    assertFalse(NodeUtil.isFunctionExpression(fn));
    assertFalse(NodeUtil.isVarArgsFunction(fn));

    Node p1 = NodeUtil.getArgumentForFunction(fn, 0);
    assertNotNull(p1);
    assertEquals("p1", p1.getString());
    assertNull(NodeUtil.getArgumentForFunction(fn, 5));

    Node call = IR.call(IR.name("f"), IR.number(10), IR.number(20));
    assertEquals(10.0, NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0);
    assertEquals(20.0, NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0);
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));

    Node fnCall = IR.call(IR.getprop(IR.name("fn"), IR.string("call")), IR.thisNode());
    assertTrue(NodeUtil.isFunctionObjectCall(fnCall));

    Node fnApply = IR.call(IR.getprop(IR.name("fn"), IR.string("apply")), IR.thisNode());
    assertTrue(NodeUtil.isFunctionObjectApply(fnApply));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndPrototypeHelpers() {
    Node qname = NodeUtil.newQualifiedNameNode(CONVENTION, "com.google.Test");
    assertEquals(Token.GETPROP, qname.getType());
    assertEquals("com.google.Test", qname.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qname);
    assertTrue(root.isName());
    assertEquals("com", root.getString());

    Node protoAssign = IR.exprResult(
        IR.assign(
            IR.getprop(
                IR.getprop(IR.name("MyClass"), IR.string("prototype")),
                IR.string("myMethod")),
            IR.function(IR.name(""), IR.paramList(), IR.block())));

    assertTrue(NodeUtil.isPrototypePropertyDeclaration(protoAssign));
    Node protoProp = protoAssign.getFirstChild().getFirstChild();
    assertTrue(NodeUtil.isPrototypeProperty(protoProp));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoProp).getQualifiedName());
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(protoProp));
  }

  @Test(timeout = 4000)
  public void testASTConstructors() {
    Node expr = NodeUtil.newExpr(IR.number(10));
    assertTrue(expr.isExprResult());
    assertEquals(10.0, expr.getFirstChild().getDouble(), 0.0);

    Node undef = NodeUtil.newUndefinedNode(null);
    assertTrue(NodeUtil.isUndefined(undef));

    Node varNode = NodeUtil.newVarNode("myVar", IR.number(1));
    assertTrue(varNode.isVar());
    assertEquals("myVar", varNode.getFirstChild().getString());

    Node callNode = NodeUtil.newCallNode(IR.name("f"), IR.number(1));
    assertTrue(callNode.isCall());
    assertTrue(callNode.getBooleanProp(Node.FREE_CALL));

    Node boolTrue = NodeUtil.booleanNode(true);
    assertTrue(boolTrue.isTrue());
    Node boolFalse = NodeUtil.booleanNode(false);
    assertTrue(boolFalse.isFalse());

    Node numNaN = NodeUtil.numberNode(Double.NaN, null);
    assertEquals("NaN", numNaN.getString());
    Node numInf = NodeUtil.numberNode(Double.POSITIVE_INFINITY, null);
    assertEquals("Infinity", numInf.getString());
    Node numNegInf = NodeUtil.numberNode(Double.NEGATIVE_INFINITY, null);
    assertTrue(numNegInf.isNeg());
    Node numReg = NodeUtil.numberNode(42.0, null);
    assertEquals(42.0, numReg.getDouble(), 0.0);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testTrimJsWhiteSpaceBoundaries() {
    assertEquals("", NodeUtil.trimJsWhiteSpace(""));
    assertEquals("", NodeUtil.trimJsWhiteSpace(" \t\n\r\u00A0\uFEFF "));
    assertEquals("a", NodeUtil.trimJsWhiteSpace(" \n\r\t a \t\r\n "));

    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('X'));
  }

  @Test(timeout = 4000)
  public void testLatinAndIdentifierValidation() {
    assertTrue(NodeUtil.isLatin("abcXYZ_123$"));
    assertFalse(NodeUtil.isLatin("abc\u0080xyz"));
    assertFalse(NodeUtil.isLatin("identifier\u4e16\u754c"));

    assertTrue(NodeUtil.isValidSimpleName("validIdentifier_12$"));
    assertFalse(NodeUtil.isValidSimpleName(""));
    assertFalse(NodeUtil.isValidSimpleName("123bad"));
    assertFalse(NodeUtil.isValidSimpleName("class")); // keyword

    assertTrue(NodeUtil.isValidQualifiedName("a.b.c"));
    assertTrue(NodeUtil.isValidQualifiedName("a"));
    assertFalse(NodeUtil.isValidQualifiedName(".a"));
    assertFalse(NodeUtil.isValidQualifiedName("a."));
    assertFalse(NodeUtil.isValidQualifiedName("a..b"));
    assertFalse(NodeUtil.isValidQualifiedName("a.class.c"));

    assertTrue(NodeUtil.isValidPropertyName("prop"));
    assertFalse(NodeUtil.isValidPropertyName("prop.child"));
  }

  @Test(timeout = 4000)
  public void testConstantsAndDefinesValidation() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_A", "config.MODE"));

    assertTrue(NodeUtil.isValidDefineValue(IR.string("val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.number(123), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.trueNode(), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.falseNode(), defines));

    assertTrue(NodeUtil.isValidDefineValue(IR.add(IR.number(1), IR.number(2)), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.not(IR.trueNode()), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.name("DEF_A"), defines));
    assertTrue(NodeUtil.isValidDefineValue(
        IR.getprop(IR.name("config"), IR.string("MODE")), defines));

    assertFalse(NodeUtil.isValidDefineValue(IR.name("UNKNOWN_DEF"), defines));
    assertFalse(NodeUtil.isValidDefineValue(IR.call(IR.name("f")), defines));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 821)
  // =========================================================================

  /*
   * PeepholeFoldConstantsTest::testIssue821 / Issue 821
   * The bug: NodeUtil.mayBeString(n) invoked allResultsMatch instead of anyResultsMatch.
   * A conditional expression (HOOK: cond ? 'string' : 123) or logical expression
   * (OR: num || 'string') MAY evaluate to a string. The tests below assert that
   * mayBeString correctly returns true when at least one branch can be a string.
   */

  @Test(timeout = 4000)
  public void testIssue821_HookMayBeStringWhenOneBranchIsString() {
    Node cond = IR.name("cond");
    Node strBranch = IR.string("hello");
    Node numBranch = IR.number(123);
    Node hook = IR.hook(cond, strBranch, numBranch);

    assertTrue("A ternary expression with a string branch must return true for mayBeString",
        NodeUtil.mayBeString(hook));
  }

  @Test(timeout = 4000)
  public void testIssue821_OrMayBeStringWhenOneBranchIsString() {
    Node numBranch = IR.number(42);
    Node strBranch = IR.string("world");
    Node orNode = IR.or(numBranch, strBranch);

    assertTrue("A logical OR with a string branch must return true for mayBeString",
        NodeUtil.mayBeString(orNode));
  }

  @Test(timeout = 4000)
  public void testIssue821_AndMayBeStringWhenOneBranchIsString() {
    Node numBranch = IR.number(0);
    Node strBranch = IR.string("world");
    Node andNode = IR.and(numBranch, strBranch);

    assertTrue("A logical AND with a string branch must return true for mayBeString",
        NodeUtil.mayBeString(andNode));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsThrowsOnNonNew() {
    NodeUtil.constructorCallHasSideEffects(IR.call(IR.name("f")));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsThrowsOnNonCall() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW, IR.name("Object")));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssignment() {
    NodeUtil.getOpFromAssignmentOp(IR.add(IR.number(1), IR.number(2)));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpressionThrowsOnInvalidNode() {
    NodeUtil.getConditionExpression(IR.exprResult(IR.number(1)));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceThrowsOnUnknownToken() {
    NodeUtil.precedence(Token.IF);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnNonOperator() {
    NodeUtil.opToStrNoFail(Token.IF);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetObjectLitKeyNameThrowsOnNonKey() {
    NodeUtil.getObjectLitKeyName(IR.number(123));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildThrowsOnInvalidTree() {
    Node parent = IR.add(IR.number(1), IR.number(2));
    NodeUtil.removeChild(parent, parent.getFirstChild());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorContract() throws Exception {
    Constructor<NodeUtil> constructor = NodeUtil.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    NodeUtil instance = constructor.newInstance();
    assertNotNull(instance);
  }

  @Test(timeout = 4000)
  public void testLValueAndExpressionUsage() {
    Node name = IR.name("x");
    Node assign = IR.assign(name, IR.number(1));
    Node expr = IR.exprResult(assign);
    IR.script(expr);

    assertTrue(NodeUtil.isLValue(name));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, assign));
    assertFalse(NodeUtil.isExpressionResultUsed(assign));
    assertTrue(NodeUtil.isExecutedExactlyOnce(assign));

    Node bestLVal = NodeUtil.getBestLValue(assign.getLastChild());
    assertEquals(name, bestLVal);
    assertEquals("x", NodeUtil.getBestLValueName(bestLVal));
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = IR.script();
    Node ifBlock = IR.block();
    Node varNode = IR.var(IR.name("declaredVar"));
    ifBlock.addChildToBack(varNode);
    script.addChildToBack(IR.ifNode(IR.trueNode(), ifBlock));

    NodeUtil.redeclareVarsInsideBranch(ifBlock);
    assertTrue(script.getFirstChild().isVar());
    assertEquals("declaredVar", script.getFirstChild().getFirstChild().getString());
  }
}