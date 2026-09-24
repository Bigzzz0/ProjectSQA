package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------
 * TARGET DEFECT (Defects4J / Issue 504):
 * - Target: NodeUtil.getPureBooleanValue(Node n) for Token.VOID
 * - Defect: For Token.VOID, getPureBooleanValue directly returns TernaryValue.FALSE even if the
 *   subexpression contains side-effects (e.g. `void foo()`), violating the contract that
 *   getPureBooleanValue returns UNKNOWN for expressions with side-effects.
 * - Test Target: NodeUtil.getPureBooleanValue(new Node(Token.VOID, new Node(Token.CALL, ...)))
 *   Expected: TernaryValue.UNKNOWN, Defective Result: TernaryValue.FALSE
 *
 * COVERAGE PATHS:
 * - Partition A: Core Functional Logic & State Transitions
 *   * getImpureBooleanValue / getPureBooleanValue: STRING, NUMBER, NOT, NULL, FALSE, TRUE, VOID,
 *     NAME (undefined, NaN, Infinity, unknown), REGEXP, ARRAYLIT, OBJECTLIT, ASSIGN, COMMA, AND, OR, HOOK.
 *   * getStringValue / arrayToString: String/Double representation, array empty/null/elements.
 *   * getNumberValue / getStringNumberValue: numbers, hex, signed hex, spaces, vertical tabs, Infinity.
 *   * getFunctionName / getNearestFunctionName: function declaration, var, assign, object lit getters/setters.
 *   * isImmutableValue / isLiteralValue: literal validation with/without function expressions.
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * Double conversions (1.0 -> "1", -0.0, NaN, Infinity), hex string parsing boundaries.
 *   * String trimming with ECMAScript whitespace and Unicode spaces.
 *   * Precedence mappings for all operator types and error handling on unknown types.
 * - Partition C: Defect-Targeted Branch Zone
 *   * getPureBooleanValue on VOID containing side-effect operations (function calls).
 * - Partition D: Exception & Defensive Guard Paths
 *   * constructorCallHasSideEffects on non-NEW throws IllegalStateException.
 *   * functionCallHasSideEffects on non-CALL throws IllegalStateException.
 *   * precedence on invalid token throws Error.
 *   * getOpFromAssignmentOp on non-assignment op throws IllegalArgumentException.
 *   * removeChild boundary tree removals (FOR, VAR, TRY/CATCH/FINALLY).
 * - Partition E: Object Lifecycle & Complex Tree Analysis
 *   * mayHaveSideEffects / mayEffectMutableState, evaluatesToLocalValue, canBeSideEffected.
 *   * AST manipulation: tryMergeBlock, removeChild, redeclareVarsInsideBranch, newQualifiedNameNode.
 * ------------------------------------------------------------------------------------------------
 */
public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 504 / Defects4J Target)
  // =========================================================================

  @Test(timeout = 4000)
  public void testPureBooleanValueOfVoidWithSideEffects_Issue504() {
    // Contract: getPureBooleanValue should return UNKNOWN for VOID expressions with side-effects.
    // e.g., "void foo()" has side effects and its pure boolean evaluation must yield UNKNOWN.
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node voidNode = new Node(Token.VOID, call);

    TernaryValue result = NodeUtil.getPureBooleanValue(voidNode);
    assertEquals("getPureBooleanValue on VOID with side effects must be UNKNOWN",
        TernaryValue.UNKNOWN, result);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue() {
    Node num1 = Node.newNumber(1);
    Node num0 = Node.newNumber(0);
    Node strEmpty = Node.newString("");

    // ASSIGN and COMMA evaluate RHS
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), num1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, num1, num0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(comma));

    // NOT
    Node notTrue = new Node(Token.NOT, num1);
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notTrue));

    // AND
    Node andNode = new Node(Token.AND, num1, num0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    // OR
    Node orNode = new Node(Token.OR, num0, num1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    // HOOK (Ternary)
    Node hookSame = new Node(Token.HOOK, num1, Node.newNumber(5), Node.newNumber(6));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, num1, num1, num0);
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));

    // ARRAYLIT and OBJECTLIT
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValueLiterals() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(42.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.REGEXP)));

    // Pure VOID (no side-effect child)
    Node pureVoid = new Node(Token.VOID, Node.newNumber(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(pureVoid));

    // NAME constants
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "customVar")));

    // Pure Array & Object literal without side effects
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "variable")));

    assertEquals("10", NodeUtil.getStringValue(Node.newNumber(10.0)));
    assertEquals("10.5", NodeUtil.getStringValue(Node.newNumber(10.5)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    // NOT
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals("false", NodeUtil.getStringValue(notTrue));

    // OBJECTLIT
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));

    // ARRAYLIT
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(Node.newString("a"));
    arrayLit.addChildToBack(new Node(Token.NULL));
    arrayLit.addChildToBack(Node.newNumber(2));
    assertEquals("a,,2", NodeUtil.getStringValue(arrayLit));

    Node emptyElemArray = new Node(Token.ARRAYLIT, new Node(Token.EMPTY), Node.newString("b"));
    assertEquals(",b", NodeUtil.getStringValue(emptyElemArray));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(123.45), NodeUtil.getNumberValue(Node.newNumber(123.45)));

    Node pureVoid = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(pureVoid)));

    Node sideEffectVoid = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "f")));
    assertNull(NodeUtil.getNumberValue(sideEffectVoid));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY),
        NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));

    Node negInfinity = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInfinity));

    Node notZero = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notZero));

    Node notOne = new Node(Token.NOT, Node.newNumber(1));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notOne));
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue() {
    assertNull(NodeUtil.getStringNumberValue("1\u000b2"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("-0x10"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertEquals(Double.valueOf(12.34), NodeUtil.getStringNumberValue("  12.34  \n\t"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not-a-number")));
  }

  @Test(timeout = 4000)
  public void testGetFunctionNameAndNearestFunctionName() {
    // function foo() {}
    Node fnName = Node.newString(Token.NAME, "foo");
    Node fn = new Node(Token.FUNCTION, fnName, new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    assertEquals("foo", NodeUtil.getFunctionName(fn));
    assertEquals("foo", NodeUtil.getNearestFunctionName(fn));

    // var bar = function() {}
    Node anonFn1 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varName = Node.newString(Token.NAME, "bar");
    varName.addChildToBack(anonFn1);
    Node varNode = new Node(Token.VAR, varName);
    assertEquals("bar", NodeUtil.getFunctionName(anonFn1));
    assertEquals("bar", NodeUtil.getNearestFunctionName(anonFn1));

    // obj.prop = function() {}
    Node anonFn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "prop"));
    Node assign = new Node(Token.ASSIGN, getProp, anonFn2);
    assertEquals("obj.prop", NodeUtil.getFunctionName(anonFn2));

    // Object lit key: { 'key': function() {} }
    Node anonFn3 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node keyStr = Node.newString(Token.STRING, "keyProp");
    keyStr.addChildToBack(anonFn3);
    Node objLit = new Node(Token.OBJECTLIT, keyStr);
    assertNull(NodeUtil.getFunctionName(anonFn3));
    assertEquals("keyProp", NodeUtil.getNearestFunctionName(anonFn3));

    // Object lit key number: { 123: function() {} }
    Node anonFn4 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node keyNum = Node.newNumber(123.0);
    keyNum.addChildToBack(anonFn4);
    Node objLitNum = new Node(Token.OBJECTLIT, keyNum);
    assertEquals("123", NodeUtil.getNearestFunctionName(anonFn4));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Predicates
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(0)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "randomVar")));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(2));
    objLit.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExp = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isLiteralValue(fnExp, false));
    assertTrue(NodeUtil.isLiteralValue(fnExp, true));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_A", "DEF_B", "pkg.DEF_C"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(100), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN"), defines));

    Node qualifiedDef = new Node(Token.GETPROP, Node.newString(Token.NAME, "pkg"), Node.newString(Token.STRING, "DEF_C"));
    assertTrue(NodeUtil.isValidDefineValue(qualifiedDef, defines));
  }

  @Test(timeout = 4000)
  public void testIsNumericBooleanAndMayBeStringResult() {
    Node addNums = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isNumericResult(addNums));
    assertFalse(NodeUtil.mayBeString(addNums));

    Node addStrings = new Node(Token.ADD, Node.newString("a"), Node.newNumber(1));
    assertFalse(NodeUtil.isNumericResult(addStrings));
    assertTrue(NodeUtil.mayBeString(addStrings));

    Node bitNot = new Node(Token.BITNOT, Node.newNumber(5));
    assertTrue(NodeUtil.isNumericResult(bitNot));

    Node eq = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isBooleanResult(eq));
    assertFalse(NodeUtil.isNumericResult(eq));
    assertFalse(NodeUtil.mayBeString(eq));

    Node delProp = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    assertTrue(NodeUtil.isBooleanResult(delProp));
  }

  @Test(timeout = 4000)
  public void testAssociativeAndCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertFalse(NodeUtil.isAssociative(Token.ADD)); // "+" is not associative in JS due to string concat

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.SUB));
  }

  @Test(timeout = 4000)
  public void testOpToStrAndPrecedence() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));

    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsOnInvalidNode() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    NodeUtil.constructorCallHasSideEffects(call);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsOnInvalidNode() {
    Node newN = new Node(Token.NEW, Node.newString(Token.NAME, "foo"));
    NodeUtil.functionCallHasSideEffects(newN);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnError() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceUnknownTypeThrows() {
    NodeUtil.precedence(Token.SCRIPT);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssign() {
    Node nonAssign = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.getOpFromAssignmentOp(nonAssign);
  }

  @Test(timeout = 4000)
  public void testGetOpFromAssignmentOpValid() {
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
  }

  // =========================================================================
  // Partition E: Tree Modification, AST Query & Lifecycle Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testMayHaveSideEffectsAndBuiltins() {
    // Builtin constructors without side-effects
    for (String builtin : Arrays.asList("Array", "Date", "Error", "Object", "RegExp", "XMLHttpRequest")) {
      Node newBuiltin = new Node(Token.NEW, Node.newString(Token.NAME, builtin));
      assertFalse(NodeUtil.constructorCallHasSideEffects(newBuiltin));
    }

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    // Builtin functions without side effects
    for (String fn : Arrays.asList("Object", "Array", "String", "Number", "Boolean", "RegExp", "Error")) {
      Node callBuiltin = new Node(Token.CALL, Node.newString(Token.NAME, fn));
      assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));
    }

    // Throw has side effects
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("error"))));

    // Simple expressions
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(42)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.EMPTY)));
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node script = new Node(Token.SCRIPT);
    Node innerBlock = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    innerBlock.addChildToBack(stmt1);
    innerBlock.addChildToBack(stmt2);
    script.addChildToBack(innerBlock);

    boolean merged = NodeUtil.tryMergeBlock(innerBlock);
    assertTrue(merged);
    assertEquals(2, script.getChildCount());
    assertEquals(stmt1, script.getFirstChild());
    assertEquals(stmt2, script.getLastChild());
  }

  @Test(timeout = 4000)
  public void testRemoveChildScenarios() {
    // 1. Remove statement from BLOCK
    Node block = new Node(Token.BLOCK);
    Node expr = NodeUtil.newExpr(Node.newNumber(1));
    block.addChildToBack(expr);
    NodeUtil.removeChild(block, expr);
    assertEquals(0, block.getChildCount());

    // 2. Remove var with multiple children
    Node varNode = new Node(Token.VAR);
    Node v1 = Node.newString(Token.NAME, "a");
    Node v2 = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(v1);
    varNode.addChildToBack(v2);
    NodeUtil.removeChild(varNode, v1);
    assertEquals(1, varNode.getChildCount());
    assertEquals(v2, varNode.getFirstChild());

    // 3. Remove child in 4-child FOR loop
    Node forNode = new Node(Token.FOR,
        Node.newString(Token.NAME, "init"),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "inc"),
        new Node(Token.BLOCK));
    Node cond = forNode.getFirstChild().getNext();
    NodeUtil.removeChild(forNode, cond);
    assertEquals(Token.EMPTY, forNode.getFirstChild().getNext().getType());
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyOperations() {
    Node tryBody = new Node(Token.BLOCK);
    Node catchContainer = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchContainer.addChildToBack(catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryNode = new Node(Token.TRY, tryBody, catchContainer, finallyBlock);

    assertTrue(NodeUtil.hasFinally(tryNode));
    assertEquals(catchContainer, NodeUtil.getCatchBlock(tryNode));
    assertTrue(NodeUtil.hasCatchHandler(catchContainer));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));
    assertTrue(NodeUtil.isTryCatchNodeContainer(catchContainer));

    // Remove finally node safely when catch exists
    NodeUtil.removeChild(tryNode, finallyBlock);
    assertEquals(2, tryNode.getChildCount());
    assertFalse(NodeUtil.hasFinally(tryNode));

    // Test maybeAddFinally
    NodeUtil.maybeAddFinally(tryNode);
    assertTrue(NodeUtil.hasFinally(tryNode));
  }

  @Test(timeout = 4000)
  public void testLoopAndControlStructureMethods() {
    Node whileNode = new Node(Token.WHILE, Node.newNumber(1), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertTrue(NodeUtil.isControlStructure(whileNode));
    assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));
    assertEquals(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newNumber(1));
    assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));
    assertEquals(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));

    Node ifNode = new Node(Token.IF, Node.newNumber(1), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertEquals(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getFirstChild()));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndPrototypeHelpers() {
    CodingConvention convention = new DefaultCodingConvention();
    Node qName = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 1, 0);

    assertEquals(Token.GETPROP, qName.getType());
    assertEquals("a.b.c", qName.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qName);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());

    Node protoAssign = NodeUtil.newExpr(new Node(Token.ASSIGN,
        NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.foo", 1, 0),
        Node.newNumber(1)));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(protoAssign));
    assertEquals("foo", NodeUtil.getPrototypePropertyName(protoAssign.getFirstChild().getFirstChild()));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoAssign.getFirstChild().getFirstChild()).getQualifiedName());
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node ifBlock = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    ifBlock.addChildToBack(varNode);
    script.addChildToBack(ifBlock);

    NodeUtil.redeclareVarsInsideBranch(ifBlock);
    // Redeclaration adds a VAR to the top of current scope (script)
    assertEquals(2, script.getChildCount());
    assertEquals(Token.VAR, script.getFirstChild().getType());
    assertEquals("x", script.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(10)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(add));

    Node name = Node.newString(Token.NAME, "undefined");
    assertTrue(NodeUtil.evaluatesToLocalValue(name));

    Node mutableName = Node.newString(Token.NAME, "globalVar");
    assertFalse(NodeUtil.evaluatesToLocalValue(mutableName));
  }

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin("abcXYZ123_$-"));
    assertFalse(NodeUtil.isLatin("abc\u00A0def"));
    assertFalse(NodeUtil.isLatin("Hello\u4E16\u754C"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("$dollar"));
    assertTrue(NodeUtil.isValidPropertyName("_underscore"));
    assertFalse(NodeUtil.isValidPropertyName("for")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("while")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("123bad")); // invalid start identifier
    assertFalse(NodeUtil.isValidPropertyName("prop\u4E16")); // non-latin
  }
}