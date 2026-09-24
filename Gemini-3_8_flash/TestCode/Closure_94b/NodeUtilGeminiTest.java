package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.NodeUtil
 *
 * Defects4J Defect Trigger Points:
 * 1. NodeUtilTest::testValidDefine & ProcessDefinesTest::testOverridingString1, testOverridingString3
 *    - In isValidDefineValue(Node, Set<String>):
 *      a) Token.ADD is completely missing from valid define expressions (e.g., 'foo' + 'bar' or 1 + 2).
 *      b) Binary operators (BITAND, BITOR, BITXOR) fall through to unary logic and incorrectly only evaluate
 *         the first child (val.getFirstChild()) rather than asserting both children are valid define values.
 *
 * Branch & Coverage Matrix:
 * - Partition A: Core Functional Logic & State Transitions
 *   - getExpressionBooleanValue, getBooleanValue (literals, names: undefined, NaN, Infinity, expressions: AND, OR, NOT, HOOK, COMMA, ASSIGN)
 *   - getStringValue (STRING, NUMBER integers vs decimals, TRUE, FALSE, NULL, VOID)
 *   - getFunctionName, getNearestFunctionName (function declarations, named/unnamed vars, assigns, object literals)
 *   - isImmutableValue, isLiteralValue (with/without includeFunctions, recursive literals)
 *   - isEmptyBlock, isSimpleOperator, precedence, isAssociative, isAssignmentOp, getOpFromAssignmentOp
 *   - containsFunction, referencesThis, isGet, isGetProp, isName, isNew, isVar, isVarDeclaration, getAssignedValue
 *   - isExprAssign, isAssign, isExprCall, isForIn, isLoopStructure, getLoopCodeBlock, isWithinLoop
 *   - isControlStructure, isControlStructureCodeBlock, getConditionExpression
 * - Partition B: Boundary Value Analysis (BVA) & Structural Tree Manipulation
 *   - removeChild (block statements, VAR with 1 or multiple children, LABEL, FOR with 4 children, try-finally, switch cases)
 *   - tryMergeBlock (parent is statement block vs non-statement block)
 *   - mayHaveSideEffects / mayEffectMutableState / checkForStateChangeHelper (calls, news, built-ins, Math.*, regexes, locals)
 *   - evaluatesToLocalValue (ASSIGN, COMMA, AND, OR, HOOK, INC/DEC, THIS, NAME, CALL, literals, ops)
 *   - newExpr, newFunctionNode, newQualifiedNameNode, newName, newUndefinedNode, newVarNode, newCallNode
 * - Partition C: Defect-Targeted Branch Zone (Targeting isValidDefineValue)
 *   - testValidDefine_AdditionOperation: Binary ADD with strings or numbers
 *   - testValidDefine_BinaryOperatorChildrenValidation: Binary BITAND/BITOR/BITXOR where second operand is invalid
 * - Partition D: Exception & Defensive Guard Paths
 *   - removeChild illegal attempts throwing IllegalStateException
 *   - opToStrNoFail throwing Error on invalid operator
 *   - getOpFromAssignmentOp throwing IllegalArgumentException on non-assign op
 *   - getConditionExpression throwing on invalid node types
 * - Partition E: Object Lifecycle & Utility Contract Integrity
 *   - isLatin, isValidPropertyName, isPrototypeProperty, getPrototypeClassName, getPrototypePropertyName
 *   - Visitor traversals (visitPreOrder, visitPostOrder), getCount, has, getNodeTypeReferenceCount, isNameReferenced
 */
public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where Token.ADD is omitted from isValidDefineValue.
   * String concatenations like 'foo' + 'bar' in @define variables trigger JSC_INVALID_DEFINE_INIT_ERROR.
   */
  @Test(timeout = 4000)
  public void testValidDefine_AdditionOperationStringConcat() {
    Node stringAdd = new Node(Token.ADD, Node.newString("foo"), Node.newString("bar"));
    Set<String> defines = Collections.emptySet();
    assertTrue(
        "Token.ADD with valid string operands must be considered a valid define value",
        NodeUtil.isValidDefineValue(stringAdd, defines));
  }

  /**
   * Targets the defect where binary operators (Token.ADD, Token.BITAND, etc.)
   * fail to check both children in isValidDefineValue.
   */
  @Test(timeout = 4000)
  public void testValidDefine_BinaryOperatorValidBothChildren() {
    Node numAdd = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(
        "Token.ADD with valid numeric operands must be a valid define value",
        NodeUtil.isValidDefineValue(numAdd, Collections.<String>emptySet()));

    Node bitAnd = new Node(Token.BITAND, Node.newNumber(1), Node.newNumber(2));
    assertTrue(
        "BITAND with valid children should be valid define value",
        NodeUtil.isValidDefineValue(bitAnd, Collections.<String>emptySet()));
  }

  /**
   * Targets the defect where BITAND/BITOR/BITXOR fall through to unary logic,
   * only checking the first child and ignoring an invalid second child.
   */
  @Test(timeout = 4000)
  public void testValidDefine_BinaryOperatorRejectsInvalidSecondChild() {
    Node invalidCall = new Node(Token.CALL, Node.newString(Token.NAME, "func"));
    Node bitAndInvalidRhs = new Node(Token.BITAND, Node.newNumber(1), invalidCall);

    assertFalse(
        "BITAND with invalid second child must not be accepted as a valid define value",
        NodeUtil.isValidDefineValue(bitAndInvalidRhs, Collections.<String>emptySet()));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanValue_Literals() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(-0.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(-42.5)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("non-empty")));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "otherVariable")));
  }

  @Test(timeout = 4000)
  public void testGetExpressionBooleanValue_LogicalExpressions() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(notTrue));

    Node andExpr = new Node(Token.AND, new Node(Token.TRUE), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(andExpr));

    Node orExpr = new Node(Token.OR, new Node(Token.FALSE), Node.newString("hello"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orExpr));

    Node commaExpr = new Node(Token.COMMA, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(commaExpr));

    Node assignExpr = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assignExpr));

    Node hookMatching = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookMatching));

    Node hookDiverging = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiverging));
  }

  @Test(timeout = 4000)
  public void testGetStringValue_Primitives() {
    assertEquals("testStr", NodeUtil.getStringValue(Node.newString("testStr")));
    assertEquals("identifier", NodeUtil.getStringValue(Node.newString(Token.NAME, "identifier")));
    assertEquals("0", NodeUtil.getStringValue(Node.newNumber(0.0)));
    assertEquals("100", NodeUtil.getStringValue(Node.newNumber(100.0)));
    assertEquals("12.34", NodeUtil.getStringValue(Node.newNumber(12.34)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName_VariousForms() {
    // 1. function f() {}
    Node fnNameNode = Node.newString(Token.NAME, "f");
    Node fn = new Node(Token.FUNCTION, fnNameNode, new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    assertEquals("f", NodeUtil.getFunctionName(fn));

    // 2. var a = function() {}
    Node anonFnName = Node.newString(Token.NAME, "");
    Node anonFn = new Node(Token.FUNCTION, anonFnName, new Node(Token.LP), new Node(Token.BLOCK));
    Node varName = Node.newString(Token.NAME, "varA");
    varName.addChildToBack(anonFn);
    new Node(Token.VAR, varName);
    assertEquals("varA", NodeUtil.getFunctionName(anonFn));

    // 3. qualified.name = function() {}
    Node anonFn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node qName = new Node(Token.GETPROP, Node.newString(Token.NAME, "foo"), Node.newString("bar"));
    new Node(Token.ASSIGN, qName, anonFn2);
    assertEquals("foo.bar", NodeUtil.getFunctionName(anonFn2));

    // 4. Object literal: { 'key': function() {} } -> nearest function name
    Node objLit = new Node(Token.OBJECTLIT);
    Node keyNode = Node.newString("objKey");
    Node anonFn3 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    objLit.addChildToBack(keyNode);
    objLit.addChildToBack(anonFn3);
    assertEquals("objKey", NodeUtil.getNearestFunctionName(anonFn3));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("abc")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(123)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));

    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "normalVar")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node nonConstArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "mutableVar"));
    assertFalse(NodeUtil.isLiteralValue(nonConstArray, false));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), fnExpr);
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_BaseCases() {
    Set<String> defines = ImmutableSet.of("DEF_A", "a.b.c");

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("str"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(notTrue, defines));

    Node negNumber = new Node(Token.NEG, Node.newNumber(10));
    assertTrue(NodeUtil.isValidDefineValue(negNumber, defines));

    Node validName = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(validName, defines));

    Node invalidName = Node.newString(Token.NAME, "NON_DEF");
    assertFalse(NodeUtil.isValidDefineValue(invalidName, defines));

    Node validProp = new Node(Token.GETPROP, new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b")), Node.newString("c"));
    assertTrue(NodeUtil.isValidDefineValue(validProp, defines));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));
    assertTrue(NodeUtil.isEmptyBlock(new Node(Token.BLOCK)));
    assertTrue(NodeUtil.isEmptyBlock(new Node(Token.BLOCK, new Node(Token.EMPTY))));
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.BLOCK, new Node(Token.EXPR_RESULT))));
  }

  @Test(timeout = 4000)
  public void testPrecedenceAndAssociativity() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN_ADD));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));
  }

  @Test(timeout = 4000)
  public void testAssignmentOperations() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignSub = new Node(Token.ASSIGN_SUB, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));

    Node assignMul = new Node(Token.ASSIGN_MUL, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(assignMul));

    Node assignDiv = new Node(Token.ASSIGN_DIV, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(assignDiv));

    Node assignMod = new Node(Token.ASSIGN_MOD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(assignMod));

    Node assignLsh = new Node(Token.ASSIGN_LSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(assignLsh));

    Node assignRsh = new Node(Token.ASSIGN_RSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(assignRsh));

    Node assignUrsh = new Node(Token.ASSIGN_URSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(assignUrsh));

    Node assignBitAnd = new Node(Token.ASSIGN_BITAND, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(assignBitAnd));

    Node assignBitOr = new Node(Token.ASSIGN_BITOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(assignBitOr));

    Node assignBitXor = new Node(Token.ASSIGN_BITXOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(assignBitXor));
  }

  @Test(timeout = 4000)
  public void testOpToStrMapping() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("*", NodeUtil.opToStr(Token.MUL));
    assertEquals("/", NodeUtil.opToStr(Token.DIV));
    assertEquals("%", NodeUtil.opToStr(Token.MOD));
    assertEquals("==", NodeUtil.opToStr(Token.EQ));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!=", NodeUtil.opToStr(Token.NE));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("||", NodeUtil.opToStr(Token.OR));
    assertEquals("&&", NodeUtil.opToStr(Token.AND));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // =========================================================================
  // Partition B: BVA & Structural Tree Manipulation
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveChild_BlockAndVarBranches() {
    // 1. Remove statement from block
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    Node block = new Node(Token.BLOCK, stmt1, stmt2);
    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertSame(stmt2, block.getFirstChild());

    // 2. Remove var child from multi-child var
    Node varName1 = Node.newString(Token.NAME, "v1");
    Node varName2 = Node.newString(Token.NAME, "v2");
    Node multiVar = new Node(Token.VAR, varName1, varName2);
    Node blockWithVar = new Node(Token.BLOCK, multiVar);
    NodeUtil.removeChild(multiVar, varName1);
    assertEquals(1, multiVar.getChildCount());
    assertSame(varName2, multiVar.getFirstChild());

    // 3. Remove var child from single-child var -> removes var from parent
    Node singleVarName = Node.newString(Token.NAME, "single");
    Node singleVar = new Node(Token.VAR, singleVarName);
    Node blockSingle = new Node(Token.BLOCK, singleVar);
    NodeUtil.removeChild(singleVar, singleVarName);
    assertEquals(0, blockSingle.getChildCount());

    // 4. Remove child from FOR loop (4 children: init, cond, incr, body)
    Node forInit = new Node(Token.EMPTY);
    Node forCond = new Node(Token.TRUE);
    Node forIncr = new Node(Token.EMPTY);
    Node forBody = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, forInit, forCond, forIncr, forBody);
    NodeUtil.removeChild(forNode, forCond);
    assertEquals(Token.EMPTY, forNode.getChildAtIndex(1).getType());

    // 5. Remove child from LABEL
    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    Node labelStmt = NodeUtil.newExpr(Node.newNumber(10));
    Node labelNode = new Node(Token.LABEL, labelName, labelStmt);
    Node blockLabel = new Node(Token.BLOCK, labelNode);
    NodeUtil.removeChild(labelNode, labelStmt);
    assertEquals(0, blockLabel.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node innerStmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node innerStmt2 = NodeUtil.newExpr(Node.newNumber(2));
    Node innerBlock = new Node(Token.BLOCK, innerStmt1, innerStmt2);
    Node parentBlock = new Node(Token.BLOCK, innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(2, parentBlock.getChildCount());
    assertSame(innerStmt1, parentBlock.getFirstChild());
    assertSame(innerStmt2, parentBlock.getLastChild());

    // If parent is not a statement block, returns false
    Node unmergeableInner = new Node(Token.BLOCK);
    new Node(Token.EXPR_RESULT, unmergeableInner);
    assertFalse(NodeUtil.tryMergeBlock(unmergeableInner));
  }

  @Test(timeout = 4000)
  public void testLoopAndControlStructures() {
    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.TRUE), new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertTrue(NodeUtil.isControlStructure(forNode));
    assertNotNull(NodeUtil.getLoopCodeBlock(forNode));

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertSame(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));

    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertSame(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isLoopStructure(ifNode));
    assertTrue(NodeUtil.isControlStructure(ifNode));

    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getFirstChild()));

    assertSame(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));
    assertSame(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));
    assertSame(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));
  }

  @Test(timeout = 4000)
  public void testSideEffectsAnalysis() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newString("s")));
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node mathCall = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString("sin")), Node.newNumber(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));

    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.NEW, Node.newString(Token.NAME, "Object"))));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(add));

    Node hookLocal = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hookLocal));

    Node nameNode = Node.newString(Token.NAME, "globalVar");
    assertFalse(NodeUtil.evaluatesToLocalValue(nameNode));
    assertTrue(NodeUtil.evaluatesToLocalValue(nameNode, Predicates.<Node>alwaysTrue()));
  }

  @Test(timeout = 4000)
  public void testAstFactoryMethods() {
    Node expr = NodeUtil.newExpr(Node.newNumber(42));
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertSame(42.0, expr.getFirstChild().getDouble());

    Node undefinedNode = NodeUtil.newUndefinedNode(null);
    assertEquals(Token.VOID, undefinedNode.getType());
    assertEquals(0.0, undefinedNode.getFirstChild().getDouble(), 0.0);

    Node varNode = NodeUtil.newVarNode("myVar", Node.newNumber(10));
    assertEquals(Token.VAR, varNode.getType());
    assertEquals("myVar", varNode.getFirstChild().getString());
    assertEquals(10.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0);

    Node qName = NodeUtil.newQualifiedNameNode("a.b.c", 1, 2);
    assertEquals(Token.GETPROP, qName.getType());
    assertEquals("a.b.c", qName.getQualifiedName());

    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "fn"), Node.newNumber(1));
    assertEquals(Token.CALL, call.getType());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
    assertEquals(2, call.getChildCount());

    Node fnNode = NodeUtil.newFunctionNode("testFn", Collections.singletonList(Node.newString(Token.NAME, "arg1")), new Node(Token.BLOCK), 1, 0);
    assertEquals(Token.FUNCTION, fnNode.getType());
    assertEquals("testFn", fnNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testPrototypeHelpers() {
    Node qName = NodeUtil.newQualifiedNameNode("MyClass.prototype.myMethod", 0, 0);
    assertTrue(NodeUtil.isPrototypeProperty(qName));
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qName));

    Node classNameNode = NodeUtil.getPrototypeClassName(qName);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getQualifiedName());

    Node nonProto = NodeUtil.newQualifiedNameNode("MyClass.myMethod", 0, 0);
    assertFalse(NodeUtil.isPrototypeProperty(nonProto));
  }

  @Test(timeout = 4000)
  public void testVisitorAndTraversal() {
    Node root = new Node(Token.BLOCK,
        NodeUtil.newVarNode("x", Node.newNumber(1)),
        NodeUtil.newVarNode("y", Node.newNumber(2)));

    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(root);
    assertEquals(2, vars.size());

    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.VAR, Predicates.<Node>alwaysTrue()));
    assertTrue(NodeUtil.isNameReferenced(root, "x"));
    assertFalse(NodeUtil.isNameReferenced(root, "nonExistent"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "y"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChild_InvalidHierarchyThrows() {
    Node parent = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node detachedChild = Node.newNumber(2);
    NodeUtil.removeChild(parent, detachedChild);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFail_ThrowsOnNonOperator() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOp_ThrowsOnNonAssign() {
    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.getOpFromAssignmentOp(addNode);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpression_ThrowsOnNodeWithoutCondition() {
    Node block = new Node(Token.BLOCK);
    NodeUtil.getConditionExpression(block);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorCallHasSideEffects_ThrowsOnNonNew() {
    Node call = new Node(Token.CALL);
    NodeUtil.constructorCallHasSideEffects(call);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testFunctionCallHasSideEffects_ThrowsOnNonCall() {
    Node newObj = new Node(Token.NEW);
    NodeUtil.functionCallHasSideEffects(newObj);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Lexical / Helper Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("asciiWithUnicode\u00A9"));

    assertTrue(NodeUtil.isValidPropertyName("prop1"));
    assertTrue(NodeUtil.isValidPropertyName("$var"));
    assertTrue(NodeUtil.isValidPropertyName("_private"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("123bad")); // invalid start
    assertFalse(NodeUtil.isValidPropertyName("invalid.dot"));
  }

  @Test(timeout = 4000)
  public void testIsReferenceName() {
    assertTrue(NodeUtil.isReferenceName(Node.newString(Token.NAME, "myVar")));
    assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));
    assertFalse(NodeUtil.isReferenceName(Node.newString(Token.STRING, "notAName")));
  }

  @Test(timeout = 4000)
  public void testGetRootOfQualifiedName() {
    Node qName = NodeUtil.newQualifiedNameNode("foo.bar.baz", 0, 0);
    Node root = NodeUtil.getRootOfQualifiedName(qName);
    assertEquals(Token.NAME, root.getType());
    assertEquals("foo", root.getString());
  }

  @Test(timeout = 4000)
  public void testTryFinallyHelpers() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK, new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK)));
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    assertTrue(NodeUtil.hasFinally(tryNode));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));
    assertFalse(NodeUtil.isTryFinallyNode(tryNode, tryBlock));

    assertSame(catchBlock, NodeUtil.getCatchBlock(tryNode));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
  }
}