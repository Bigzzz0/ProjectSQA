package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * Functionality Under Test          | Targeted Branches & Boundary Cases           | Defect Relevance
 * ------------------------------------------------------------------------------------------------------
 * getBooleanValue                   | STRING, NUMBER (0, !=0), NULL, FALSE, VOID,  | Literal evaluation
 *                                   | NAME (undefined, NaN, Infinity, non-literal) | purity & edge-cases
 *                                   | TRUE, ARRAYLIT, OBJECTLIT, REGEXP, default   |
 * ------------------------------------------------------------------------------------------------------
 * getStringValue                    | NAME, STRING, NUMBER (integral vs fractional)| String cast accuracy
 *                                   | FALSE, TRUE, NULL, VOID, unknown token       |
 * ------------------------------------------------------------------------------------------------------
 * getFunctionName                   | parent NAME, ASSIGN, default (named vs anon) | Function declaration
 * ------------------------------------------------------------------------------------------------------
 * isImmutableValue & isLiteralValue | Primitives, NEG, NAME (NaN/Infinity/undef),  | Constant folding and
 *                                   | nested ARRAYLIT/OBJECTLIT/REGEXP purity      | side-effect analysis
 * ------------------------------------------------------------------------------------------------------
 * isValidDefineValue                | Literals, Unary ops (BIT*, NOT, NEG),        | Defines safety guard
 *                                   | Qualified Names matching / non-matching      |
 * ------------------------------------------------------------------------------------------------------
 * isEmptyBlock & isSimpleOperator   | BLOCK with/without EMPTY, 29 simple op types | Structure cleanups
 * ------------------------------------------------------------------------------------------------------
 * Side Effects Detection            | OBJECTLIT/ARRAYLIT/REGEXP (mutate vs create),| PureFunctionIdentifier
 * (mayHaveSideEffects,              | Functions (named vs anonymous), NEW (safe vs | and ExpressionDecomposer
 *  mayEffectMutableState,           | custom), CALL (Math.*, String, no-side-effect| calls with HOOK / OR
 *  canBeSideEffected)               | flags), Assignments (GETPROP/GETELEM/lvalues)| callee expressions
 * ------------------------------------------------------------------------------------------------------
 * Precedence & Operator string      | Precedence 0..15 coverage, Unknown op errors,| Precedence order in
 *                                   | opToStr, opToStrNoFail, isAssociative        | complex expressions
 * ------------------------------------------------------------------------------------------------------
 * Structure & CFG queries           | isLoopStructure, getLoopCodeBlock, isForIn,  | AST transformations
 *                                   | isControlStructure, isControlStructureBlock, |
 *                                   | getConditionExpression (IF, FOR 3/4, DO, etc)|
 * ------------------------------------------------------------------------------------------------------
 * AST Mutation (removeChild,        | Statement blocks, Switch-case, Try-Finally,  | Safe node removals &
 *  tryMergeBlock)                   | VAR with 1 vs >1 children, BLOCK detach,     | block inlining
 *                                   | LABEL cleanup, FOR replace with EMPTY        |
 * ------------------------------------------------------------------------------------------------------
 * Prototype & Qualified Names       | isPrototypeProperty, getPrototypeClassName,  | Prototype method and
 *                                   | getPrototypePropertyName, qualified name gen | property tracking
 * ------------------------------------------------------------------------------------------------------
 */

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.FunctionNode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanValueLiterals() {
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "non-empty")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "")));

    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(-1.0)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(-0.0)));

    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));

    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
  }

  @Test(timeout = 4000)
  public void testGetStringValueVariousTokens() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));
    assertEquals("bar", NodeUtil.getStringValue(Node.newString(Token.STRING, "bar")));

    assertEquals("0", NodeUtil.getStringValue(Node.newNumber(0.0)));
    assertEquals("42", NodeUtil.getStringValue(Node.newNumber(42.0)));
    assertEquals("-10", NodeUtil.getStringValue(Node.newNumber(-10.0)));
    assertEquals("3.1415", NodeUtil.getStringValue(Node.newNumber(3.1415)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    assertNull(NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "innerFn"));

    // Case 1: parent is NAME (var x = function innerFn() {})
    Node parentName = Node.newString(Token.NAME, "varName");
    parentName.addChildToBack(fn);
    assertEquals("varName", NodeUtil.getFunctionName(fn, parentName));

    // Case 2: parent is ASSIGN (ns.target = function innerFn() {})
    Node assign = new Node(Token.ASSIGN);
    Node qname = NodeUtil.newQualifiedNameNode("ns.target", -1, -1);
    assign.addChildToBack(qname);
    assign.addChildToBack(fn);
    assertEquals("ns.target", NodeUtil.getFunctionName(fn, assign));

    // Case 3: function statement or unassigned expression
    Node parentBlock = new Node(Token.BLOCK);
    parentBlock.addChildToBack(fn);
    assertEquals("innerFn", NodeUtil.getFunctionName(fn, parentBlock));

    // Case 4: anonymous function in block
    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    parentBlock.addChildToBack(anonFn);
    assertNull(NodeUtil.getFunctionName(anonFn, parentBlock));
  }

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "hello")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(123)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));

    Node negNumber = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isImmutableValue(negNumber));
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isImmutableValue(negVar));

    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "window")));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString(Token.STRING, "a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit));

    Node impureArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "externalVar"));
    assertFalse(NodeUtil.isLiteralValue(impureArray));

    Node emptyObjLit = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.isLiteralValue(emptyObjLit));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_A", "ns.DEF_B"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(100), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node notOp = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(notOp, defines));
    Node negOp = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isValidDefineValue(negOp, defines));
    Node bitNotOp = new Node(Token.BITNOT, Node.newNumber(7));
    assertTrue(NodeUtil.isValidDefineValue(bitNotOp, defines));

    Node validName = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(validName, defines));

    Node validProp = NodeUtil.newQualifiedNameNode("ns.DEF_B", -1, -1);
    assertTrue(NodeUtil.isValidDefineValue(validProp, defines));

    Node invalidName = Node.newString(Token.NAME, "UNKNOWN_DEF");
    assertFalse(NodeUtil.isValidDefineValue(invalidName, defines));

    Node invalidType = new Node(Token.ARRAYLIT);
    assertFalse(NodeUtil.isValidDefineValue(invalidType, defines));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.SCRIPT)));

    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmptyNodes = new Node(Token.BLOCK, new Node(Token.EMPTY), new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmptyNodes));

    Node blockWithExpr = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithExpr));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperatorTypeExhaustive() {
    int[] simpleTypes = {
        Token.ADD, Token.BITAND, Token.BITNOT, Token.BITOR, Token.BITXOR,
        Token.COMMA, Token.DIV, Token.EQ, Token.GE, Token.GETELEM,
        Token.GETPROP, Token.GT, Token.INSTANCEOF, Token.LE, Token.LSH,
        Token.LT, Token.MOD, Token.MUL, Token.NE, Token.NOT,
        Token.RSH, Token.SHEQ, Token.SHNE, Token.SUB, Token.TYPEOF,
        Token.VOID, Token.POS, Token.NEG, Token.URSH
    };
    for (int type : simpleTypes) {
      assertTrue("Expected simple op for type: " + type, NodeUtil.isSimpleOperatorType(type));
    }
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.AND));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.OR));
  }

  @Test(timeout = 4000)
  public void testNewExprAndPrecedence() {
    Node num = Node.newNumber(42);
    Node expr = NodeUtil.newExpr(num);
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertSame(num, expr.getFirstChild());

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
    assertEquals(11, NodeUtil.precedence(Token.SUB));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(12, NodeUtil.precedence(Token.DIV));
    assertEquals(13, NodeUtil.precedence(Token.INC));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
    assertEquals(15, NodeUtil.precedence(Token.CALL));
  }

  @Test(timeout = 4000)
  public void testOperatorStringConversion() {
    assertEquals("|", NodeUtil.opToStr(Token.BITOR));
    assertEquals("||", NodeUtil.opToStr(Token.OR));
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("+=", NodeUtil.opToStr(Token.ASSIGN_ADD));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.SCRIPT));

    assertEquals("===", NodeUtil.opToStrNoFail(Token.SHEQ));

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
    int[] assignOps = {
        Token.ASSIGN, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
        Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH, Token.ASSIGN_ADD,
        Token.ASSIGN_SUB, Token.ASSIGN_MUL, Token.ASSIGN_DIV, Token.ASSIGN_MOD
    };
    for (int op : assignOps) {
      Node node = new Node(op);
      assertTrue("Expected assignment op: " + op, NodeUtil.isAssignmentOp(node));
    }
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

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testControlStructuresAndConditionExpression() {
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertEquals("cond", NodeUtil.getConditionExpression(ifNode).getString());

    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "wCond"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertEquals("wCond", NodeUtil.getConditionExpression(whileNode).getString());
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileNode).getType());

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "doCond"));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals("doCond", NodeUtil.getConditionExpression(doNode).getString());
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doNode).getType());

    // Standard FOR with 4 children: init, cond, incr, body
    Node for4 = new Node(Token.FOR, new Node(Token.EMPTY), Node.newString(Token.NAME, "forCond"),
        new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(for4));
    assertFalse(NodeUtil.isForIn(for4));
    assertEquals("forCond", NodeUtil.getConditionExpression(for4).getString());
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(for4).getType());

    // FOR-IN with 3 children: var, set, body
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "iter"),
        Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));

    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isSwitchCase(caseNode));
    assertNull(NodeUtil.getConditionExpression(caseNode));

    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isSwitchCase(defaultNode));
  }

  @Test(timeout = 4000)
  public void testIsControlStructureCodeBlock() {
    Node ifCond = Node.newString(Token.NAME, "c");
    Node ifBody = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, ifCond, ifBody);
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifBody));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCond));

    Node doBody = new Node(Token.BLOCK);
    Node doCond = Node.newString(Token.NAME, "dc");
    Node doNode = new Node(Token.DO, doBody, doCond);
    assertTrue(NodeUtil.isControlStructureCodeBlock(doNode, doBody));
    assertFalse(NodeUtil.isControlStructureCodeBlock(doNode, doCond));

    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructureCodeBlock(defaultNode, defaultNode.getFirstChild()));
  }

  @Test(timeout = 4000)
  public void testIsStatementAndStatementBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node stmt = NodeUtil.newExpr(Node.newNumber(1));
    script.addChildToBack(block);
    block.addChildToBack(stmt);

    assertTrue(NodeUtil.isStatementBlock(script));
    assertTrue(NodeUtil.isStatementBlock(block));
    assertFalse(NodeUtil.isStatementBlock(stmt));

    assertTrue(NodeUtil.isStatement(block));
    assertTrue(NodeUtil.isStatement(stmt));

    Node expr = Node.newNumber(2);
    stmt.addChildToBack(expr);
    assertFalse(NodeUtil.isStatement(expr));
  }

  @Test(timeout = 4000)
  public void testIsLabelAndReferenceName() {
    Node labelName = Node.newString(Token.NAME, "myLabel");
    Node labelStmt = new Node(Token.LABEL, labelName, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLabelName(labelName));
    assertFalse(NodeUtil.isReferenceName(labelName));

    Node normalVar = Node.newString(Token.NAME, "myVar");
    new Node(Token.VAR, normalVar);
    assertFalse(NodeUtil.isLabelName(normalVar));
    assertTrue(NodeUtil.isReferenceName(normalVar));

    Node emptyName = Node.newString(Token.NAME, "");
    assertFalse(NodeUtil.isReferenceName(emptyName));
    assertFalse(NodeUtil.isLabelName(null));
  }

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin("asciiOnly123_$"));
    assertFalse(NodeUtil.isLatin("asciiWithUnicode\u00A0"));
    assertFalse(NodeUtil.isLatin("\u4e16\u754c"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("$special_12"));
    assertFalse(NodeUtil.isValidPropertyName("class"));     // JS Keyword
    assertFalse(NodeUtil.isValidPropertyName("123invalid")); // Invalid JS identifier
    assertFalse(NodeUtil.isValidPropertyName("prop\u00E9e")); // Non-latin
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyQueries() {
    Node qname = NodeUtil.newQualifiedNameNode("MyClass.prototype.render", -1, -1);
    assertTrue(NodeUtil.isPrototypeProperty(qname));

    Node nonProto = NodeUtil.newQualifiedNameNode("MyClass.regularProperty", -1, -1);
    assertFalse(NodeUtil.isPrototypeProperty(nonProto));

    Node classNameNode = NodeUtil.getPrototypeClassName(qname);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getQualifiedName());

    String propName = NodeUtil.getPrototypePropertyName(qname);
    assertEquals("render", propName);

    Node assign = new Node(Token.ASSIGN, qname, new Node(Token.FUNCTION));
    Node exprAssign = new Node(Token.EXPR_RESULT, assign);
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
    assertFalse(NodeUtil.isPrototypePropertyDeclaration(assign));
  }

  @Test(timeout = 4000)
  public void testGetAssignedValueAndLhs() {
    Node valNode = Node.newNumber(99);
    Node nameNode = Node.newString(Token.NAME, "assignedVar");
    nameNode.addChildToBack(valNode);
    Node varNode = new Node(Token.VAR, nameNode);

    assertSame(valNode, NodeUtil.getAssignedValue(nameNode));
    assertTrue(NodeUtil.isLhs(nameNode, varNode));
    assertTrue(NodeUtil.isVarDeclaration(nameNode));

    Node assignName = Node.newString(Token.NAME, "lhsVar");
    Node assignVal = Node.newNumber(100);
    Node assignNode = new Node(Token.ASSIGN, assignName, assignVal);

    assertSame(assignVal, NodeUtil.getAssignedValue(assignName));
    assertTrue(NodeUtil.isLhs(assignName, assignNode));
    assertFalse(NodeUtil.isLhs(assignVal, assignNode));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (PureFunction & ExpressionDecomposer)
  // =========================================================================

  @Test(timeout = 4000)
  public void testMayHaveSideEffectsAndStateChanges() {
    // Pure literals
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(42)));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newString(Token.STRING, "pure")));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.TRUE)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.NULL)));

    // New objects: mayHaveSideEffects allows literals; mayEffectMutableState catches them
    Node objLit = new Node(Token.OBJECTLIT);
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));
    assertTrue(NodeUtil.mayEffectMutableState(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    assertFalse(NodeUtil.mayHaveSideEffects(arrLit));
    assertTrue(NodeUtil.mayEffectMutableState(arrLit));

    Node regexLit = new Node(Token.REGEXP);
    assertFalse(NodeUtil.mayHaveSideEffects(regexLit));
    assertTrue(NodeUtil.mayEffectMutableState(regexLit));

    // Throw always has side effects
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newNumber(1))));

    // Built-in constructors without side effects
    Node safeNew = new Node(Token.NEW, Node.newString(Token.NAME, "Date"));
    assertFalse(NodeUtil.mayHaveSideEffects(safeNew));
    assertFalse(NodeUtil.constructorCallHasSideEffects(safeNew));

    Node customNew = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.mayHaveSideEffects(customNew));
    assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));

    // Built-in functions without side effects
    Node mathCall = new Node(Token.CALL, NodeUtil.newQualifiedNameNode("Math.sin", -1, -1), Node.newNumber(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node strCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(strCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "doWork"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));
    assertTrue(NodeUtil.mayHaveSideEffects(customCall));
  }

  @Test(timeout = 4000)
  public void testCanBeSideEffectedComplexCallee() {
    // Defect zone: Calls with complex callee expressions like (f || g)(h) or (f ? g : h)(i)
    Node fName = Node.newString(Token.NAME, "f");
    Node gName = Node.newString(Token.NAME, "g");
    Node orCallee = new Node(Token.OR, fName, gName);
    Node callWithOr = new Node(Token.CALL, orCallee, Node.newString(Token.NAME, "h"));

    assertTrue(NodeUtil.canBeSideEffected(callWithOr));

    // Variable reference side-effect sensitivity
    Set<String> constants = ImmutableSet.of("CONST_VAL");
    Node constNode = Node.newString(Token.NAME, "CONST_VAL");
    assertFalse(NodeUtil.canBeSideEffected(constNode, constants));

    Node normalNode = Node.newString(Token.NAME, "mutableVar");
    assertTrue(NodeUtil.canBeSideEffected(normalNode, constants));

    Node getPropOnConst = new Node(Token.GETPROP, constNode, Node.newString(Token.STRING, "field"));
    assertTrue(NodeUtil.canBeSideEffected(getPropOnConst, constants));
  }

  @Test(timeout = 4000)
  public void testFunctionCallsAndVarargsIdentification() {
    // Function calls via .call and .apply
    Node calleeCall = NodeUtil.newQualifiedNameNode("foo.call", -1, -1);
    Node callExpr = new Node(Token.CALL, calleeCall, Node.newString(Token.NAME, "thisObj"));
    assertTrue(NodeUtil.isFunctionObjectCall(callExpr));
    assertTrue(NodeUtil.isSimpleFunctionObjectCall(callExpr));
    assertFalse(NodeUtil.isFunctionObjectApply(callExpr));

    Node calleeApply = NodeUtil.newQualifiedNameNode("foo.apply", -1, -1);
    Node applyExpr = new Node(Token.CALL, calleeApply, Node.newString(Token.NAME, "thisObj"));
    assertTrue(NodeUtil.isFunctionObjectApply(applyExpr));
    assertFalse(NodeUtil.isFunctionObjectCall(applyExpr));

    // VarArgs inspection (reference to 'arguments' within function body)
    Node bodyWithArgs = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Node varArgsFn = NodeUtil.newFunctionNode("varArgFn", Collections.emptyList(), bodyWithArgs, 1, 0);
    assertTrue(NodeUtil.isVarArgsFunction(varArgsFn));

    Node bodyNoArgs = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)));
    Node fixedFn = NodeUtil.newFunctionNode("fixedFn", Collections.emptyList(), bodyNoArgs, 1, 0);
    assertFalse(NodeUtil.isVarArgsFunction(fixedFn));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetBooleanValueNonLiteralThrows() {
    NodeUtil.getBooleanValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetBooleanValueNonLiteralNameThrows() {
    NodeUtil.getBooleanValue(Node.newString(Token.NAME, "someIdentifier"));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnError() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceUnknownTokenThrows() {
    NodeUtil.precedence(9999);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssign() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsGuard() {
    NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsGuard() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpressionInvalidThrows() {
    NodeUtil.getConditionExpression(new Node(Token.EMPTY));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildInvalidAttemptThrows() {
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK));
    NodeUtil.removeChild(ifNode, ifNode.getFirstChild());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTryMergeBlockNotABlockThrows() {
    NodeUtil.tryMergeBlock(new Node(Token.EMPTY));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & AST Mutations
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveChildStatementBlock() {
    Node block = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);

    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertSame(stmt2, block.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testRemoveChildVarHandling() {
    Node script = new Node(Token.SCRIPT);

    // Multi-declaration VAR: var a = 1, b = 2; -> remove 'a'
    Node varMulti = new Node(Token.VAR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    script.addChildToBack(varMulti);
    NodeUtil.removeChild(varMulti, varMulti.getFirstChild());
    assertEquals(1, varMulti.getChildCount());
    assertEquals("b", varMulti.getFirstChild().getString());

    // Single-declaration VAR: var c = 3; -> removing 'c' removes the VAR from SCRIPT
    Node varSingle = new Node(Token.VAR, Node.newString(Token.NAME, "c"));
    script.addChildToBack(varSingle);
    NodeUtil.removeChild(varSingle, varSingle.getFirstChild());
    assertEquals(1, script.getChildCount()); // Only varMulti remains
  }

  @Test(timeout = 4000)
  public void testRemoveChildLabelAndForStructures() {
    Node script = new Node(Token.SCRIPT);

    // LABEL with single child: removing child removes LABEL
    Node labelName = Node.newString(Token.NAME, "lbl");
    Node labelBody = new Node(Token.BLOCK);
    Node labelNode = new Node(Token.LABEL, labelName, labelBody);
    script.addChildToBack(labelNode);
    NodeUtil.removeChild(labelNode, labelBody);
    assertEquals(0, script.getChildCount());

    // FOR loop: removing condition replaces it with EMPTY node
    Node init = new Node(Token.EMPTY);
    Node cond = Node.newString(Token.NAME, "condition");
    Node incr = new Node(Token.EMPTY);
    Node body = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, init, cond, incr, body);

    NodeUtil.removeChild(forNode, cond);
    assertEquals(4, forNode.getChildCount());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getNext().getType());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    // Merging block into statement block
    Node outerBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node s1 = NodeUtil.newExpr(Node.newNumber(1));
    Node s2 = NodeUtil.newExpr(Node.newNumber(2));
    innerBlock.addChildToBack(s1);
    innerBlock.addChildToBack(s2);
    outerBlock.addChildToBack(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(2, outerBlock.getChildCount());
    assertSame(s1, outerBlock.getFirstChild());
    assertSame(s2, outerBlock.getLastChild());

    // Merging block in a LABEL with single child
    Node label = new Node(Token.LABEL, Node.newString(Token.NAME, "lbl"));
    Node singleChildBlock = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(3)));
    label.addChildToBack(singleChildBlock);

    assertTrue(NodeUtil.tryMergeBlock(singleChildBlock));
    assertEquals(Token.EXPR_RESULT, label.getLastChild().getType());

    // Block under IF cannot be merged
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    assertFalse(NodeUtil.tryMergeBlock(ifNode.getLastChild()));
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node ifBlock = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "hoistedVar"));
    ifBlock.addChildToBack(varNode);
    script.addChildToBack(ifBlock);

    NodeUtil.redeclareVarsInsideBranch(ifBlock);

    // The script should now have a newly prepended VAR declaration for "hoistedVar"
    assertEquals(Token.VAR, script.getFirstChild().getType());
    assertEquals("hoistedVar", script.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testNodeTraversalAndQueryUtilities() {
    Node root = new Node(Token.BLOCK);
    Node call1 = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node call2 = new Node(Token.CALL, Node.newString(Token.NAME, "bar"));
    Node innerCall = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    call2.addChildToBack(innerCall);

    root.addChildToBack(call1);
    root.addChildToBack(call2);

    assertTrue(NodeUtil.containsCall(root));
    assertTrue(NodeUtil.isNodeTypeReferenced(root, Token.CALL));
    assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.CALL));

    assertTrue(NodeUtil.isNameReferenced(root, "foo"));
    assertEquals(2, NodeUtil.getNameReferenceCount(root, "foo"));
    assertFalse(NodeUtil.isNameReferenced(root, "nonExistent"));

    // Traversal ordering
    final int[] visitCount = {0};
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        visitCount[0]++;
      }
    }, Predicates.<Node>alwaysTrue());
    assertTrue(visitCount[0] > 0);
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyQueries() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryWithFinally = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    assertTrue(NodeUtil.hasFinally(tryWithFinally));
    assertSame(catchBlock, NodeUtil.getCatchBlock(tryWithFinally));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    assertTrue(NodeUtil.isTryFinallyNode(tryWithFinally, finallyBlock));

    Node tryWithoutFinally = new Node(Token.TRY, tryBlock, catchBlock);
    assertFalse(NodeUtil.hasFinally(tryWithoutFinally));
    assertFalse(NodeUtil.isTryFinallyNode(tryWithoutFinally, catchBlock));
  }

  @Test(timeout = 4000)
  public void testNodeCreationAndAnnotations() {
    Node undef = NodeUtil.newUndefinedNode();
    assertEquals(Token.VOID, undef.getType());
    assertEquals(0.0, undef.getFirstChild().getDouble(), 0.0);

    Node newVar = NodeUtil.newVarNode("v", Node.newNumber(10));
    assertEquals(Token.VAR, newVar.getType());
    assertEquals("v", newVar.getFirstChild().getString());
    assertEquals(10.0, newVar.getFirstChild().getFirstChild().getDouble(), 0.0);

    Node constName = Node.newString(Token.NAME, "MY_CONST");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(constName));

    Node destName = Node.newString(Token.NAME, "COPY_CONST");
    NodeUtil.copyNameAnnotations(constName, destName);
    assertTrue(NodeUtil.isConstantName(destName));
  }

  @Test(timeout = 4000)
  public void testObjectLitKeyAndSourceInfo() {
    Node key1 = Node.newString(Token.STRING, "k1");
    Node val1 = Node.newNumber(1);
    Node key2 = Node.newString(Token.STRING, "k2");
    Node val2 = Node.newNumber(2);

    Node objLit = new Node(Token.OBJECTLIT, key1, val1, key2, val2);
    assertTrue(NodeUtil.isObjectLitKey(key1, objLit));
    assertFalse(NodeUtil.isObjectLitKey(val1, objLit));
    assertTrue(NodeUtil.isObjectLitKey(key2, objLit));
    assertFalse(NodeUtil.isObjectLitKey(val2, objLit));

    objLit.putProp(Node.SOURCENAME_PROP, "source_file.js");
    assertEquals("source_file.js", NodeUtil.getSourceName(key1));
  }
}