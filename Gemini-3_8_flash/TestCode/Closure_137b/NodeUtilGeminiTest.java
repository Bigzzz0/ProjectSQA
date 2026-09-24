package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
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

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.NodeUtil
 * Defects4J Bug Focus: Closure-79 (MakeDeclaredNamesUnique, Normalize variable redeclarations,
 *                      handling of arguments, catch parameters, and hoisted/anonymous functions).
 *
 * Specific Targeted Branches:
 * 1. redeclareVarsInsideBranch & getVarsDeclaredInBranch:
 *    - Traversal avoiding Token.FUNCTION scope boundary.
 *    - Hoisting vars into getAddingRoot (SCRIPT or FUNCTION body BLOCK).
 *    - Preservation of IS_CONSTANT_NAME property via copyNameAnnotations.
 * 2. isVarArgsFunction & getFnParameters:
 *    - Function scope analysis: detecting references to 'arguments' in body.
 *    - Ignoring 'arguments' referenced only within nested Token.FUNCTION children.
 * 3. removeChild & tryMergeBlock:
 *    - Removing child from statement blocks, switch cases, and try-finally nodes.
 *    - Removing solitary vs multiple var declaration children, cascading removal to parent VAR.
 *    - Specialized handling for Token.FOR (4-child loop replacing condition with Token.EMPTY).
 *    - Merging single-child blocks within labels and statement blocks.
 * 4. getBooleanValue & getStringValue:
 *    - STRING: empty ("" -> false) vs non-empty ("a" -> true).
 *    - NUMBER: 0.0 -> false vs non-zero -> true. String conversion for integer (1 -> "1") vs float.
 *    - Special names: "undefined" -> false, "NaN" -> false, "Infinity" -> true.
 *    - Literal tokens: NULL, FALSE, VOID, TRUE, ARRAYLIT, OBJECTLIT, REGEXP.
 *    - Non-literal exceptions (e.g. operators or unregistered NAME nodes).
 * 5. getFunctionName, isFunctionDeclaration, isHoistedFunctionDeclaration, isFunctionAnonymous:
 *    - Parent forms: NAME (var f = function), ASSIGN (q.name = function), default block (function f(){}).
 *    - Hoisting check: parent is SCRIPT or grand-parent is FUNCTION.
 * 6. Side-effect & State mutation analysis:
 *    - mayHaveSideEffects & mayEffectMutableState:
 *      Token.NEW for side-effect free builtin constructors (Array, Date, Error, Object, RegExp, XMLHttpRequest).
 *      Calls to Math namespace, String builtin, expressions with no side effects.
 * 7. Operator precedence, associativity, and string conversion:
 *    - Precedence ranges 0 through 15, opToStr/opToStrNoFail complete coverage.
 * -----------------------------------------------------------------------------------------
 */
public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-79 / Variable Scoping)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectRedeclareVarsInsideBranchHoistingAndAnnotations() {
    // Targets bug symptoms in MakeDeclaredNamesUnique & NormalizeTest:
    // Redeclaring vars from an inner branch to the root script node and preserving constant annotations.
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);

    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "MY_CONST");
    nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    varNode.addChildToBack(nameNode);
    block.addChildToBack(varNode);

    Node regularVarNode = new Node(Token.VAR);
    Node regularName = Node.newString(Token.NAME, "regularVar");
    regularVarNode.addChildToBack(regularName);
    block.addChildToBack(regularVarNode);

    NodeUtil.redeclareVarsInsideBranch(block);

    // Assert new VAR nodes were hoisted to the front of script
    Node hoisted1 = script.getFirstChild();
    assertEquals(Token.VAR, hoisted1.getType());
    assertEquals("regularVar", hoisted1.getFirstChild().getString());

    Node hoisted2 = hoisted1.getNext();
    assertEquals(Token.VAR, hoisted2.getType());
    assertEquals("MY_CONST", hoisted2.getFirstChild().getString());
    assertTrue(hoisted2.getFirstChild().getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(timeout = 4000)
  public void testDefectVarsDeclaredInBranchExcludesInnerFunctions() {
    // Tests that var declarations inside nested functions are NOT collected as branch vars.
    Node block = new Node(Token.BLOCK);
    Node var1 = NodeUtil.newVarNode("outerVar", Node.newNumber(1));
    block.addChildToBack(var1);

    // Inner function with its own var
    Node innerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "innerFn"),
        new Node(Token.LP), new Node(Token.BLOCK, NodeUtil.newVarNode("innerVar", Node.newNumber(2))));
    block.addChildToBack(innerFn);

    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(block);
    assertEquals(1, vars.size());
    assertEquals("outerVar", vars.iterator().next().getString());
  }

  @Test(timeout = 4000)
  public void testDefectIsVarArgsFunctionExcludesNestedFunctions() {
    // Targets MakeDeclaredNamesUniqueTest::testArguments:
    // Ensure that references to 'arguments' inside nested functions don't make the outer function var_args.
    Node nestedFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "inner"),
        new Node(Token.LP), new Node(Token.BLOCK, NodeUtil.newExpr(Node.newString(Token.NAME, "arguments"))));

    Node outerBodyWithoutArgs = new Node(Token.BLOCK, nestedFn);
    Node outerFnWithoutArgs = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outerNo"),
        new Node(Token.LP), outerBodyWithoutArgs);

    assertFalse(NodeUtil.isVarArgsFunction(outerFnWithoutArgs));

    // Outer function directly referencing 'arguments'
    Node outerBodyWithArgs = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Node outerFnWithArgs = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outerYes"),
        new Node(Token.LP), outerBodyWithArgs);

    assertTrue(NodeUtil.isVarArgsFunction(outerFnWithArgs));
  }

  @Test(timeout = 4000)
  public void testDefectRemoveChildCascadingVarInBlock() {
    // Targets NormalizeTest::testRemoveDuplicateVarDeclarations:
    // When the only declaration in a VAR is removed, the VAR node itself must be safely removed from parent.
    Node block = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameNode);
    block.addChildToBack(varNode);

    assertEquals(1, block.getChildCount());
    NodeUtil.removeChild(varNode, nameNode);
    assertEquals(0, block.getChildCount());
    assertNull(varNode.getParent());
  }

  @Test(timeout = 4000)
  public void testDefectRemoveChildMultiVarDeclarationPreservesVar() {
    // When a VAR contains multiple declarations, only the specified child should be removed.
    Node block = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR);
    Node x = Node.newString(Token.NAME, "x");
    Node y = Node.newString(Token.NAME, "y");
    varNode.addChildToBack(x);
    varNode.addChildToBack(y);
    block.addChildToBack(varNode);

    NodeUtil.removeChild(varNode, x);
    assertEquals(1, block.getChildCount());
    assertEquals(1, varNode.getChildCount());
    assertEquals("y", varNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testDefectRemoveChildForLoopConditionReplacement() {
    // A 4-child FOR loop condition removal replaces the node with Token.EMPTY.
    Node forNode = new Node(Token.FOR);
    Node init = Node.newString(Token.NAME, "i");
    Node cond = Node.newString(Token.NAME, "cond");
    Node incr = Node.newString(Token.NAME, "incr");
    Node body = new Node(Token.BLOCK);
    forNode.addChildToBack(init);
    forNode.addChildToBack(cond);
    forNode.addChildToBack(incr);
    forNode.addChildToBack(body);

    NodeUtil.removeChild(forNode, cond);
    assertEquals(4, forNode.getChildCount());
    Node secondChild = forNode.getFirstChild().getNext();
    assertEquals(Token.EMPTY, secondChild.getType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanValueLiteralsAndSpecialNames() {
    assertTrue(NodeUtil.getBooleanValue(Node.newString("non-empty")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString("")));

    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(-0.5)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(-0.0)));

    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  @Test(timeout = 4000)
  public void testGetStringValueConversions() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));
    assertEquals("bar", NodeUtil.getStringValue(Node.newString(Token.STRING, "bar")));

    assertEquals("1", NodeUtil.getStringValue(Node.newNumber(1.0)));
    assertEquals("100", NodeUtil.getStringValue(Node.newNumber(100.0)));
    assertEquals("1.5", NodeUtil.getStringValue(Node.newNumber(1.5)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  @Test(timeout = 4000)
  public void testGetFunctionNameInVariousContexts() {
    // 1. Direct function statement: function foo() {}
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"),
        new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    assertEquals("foo", NodeUtil.getFunctionName(fn, script));

    // 2. Anonymous function: function() {}
    Node fnAnon = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
        new Node(Token.LP), new Node(Token.BLOCK));
    assertNull(NodeUtil.getFunctionName(fnAnon, script));

    // 3. Variable assignment: var myVar = function() {}
    Node varName = Node.newString(Token.NAME, "myVar");
    varName.addChildToBack(fnAnon);
    assertEquals("myVar", NodeUtil.getFunctionName(fnAnon, varName));

    // 4. Qualified name assign: a.b.c = function() {}
    Node assign = new Node(Token.ASSIGN, NodeUtil.newQualifiedNameNode("a.b.c", 0, 0), fnAnon);
    assertEquals("a.b.c", NodeUtil.getFunctionName(fnAnon, assign));
  }

  @Test(timeout = 4000)
  public void testFunctionAndDeclarationClassification() {
    Node fnNamed = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"),
        new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fnNamed);

    assertTrue(NodeUtil.isFunction(fnNamed));
    assertTrue(NodeUtil.isFunctionDeclaration(fnNamed));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fnNamed));
    assertFalse(NodeUtil.isAnonymousFunction(fnNamed));

    // Inside a function body block (hoisted)
    Node outerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outer"),
        new Node(Token.LP), new Node(Token.BLOCK));
    Node innerBlock = outerFn.getLastChild();
    innerBlock.addChildToBack(fnNamed);
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fnNamed));

    // Inside an expression (anonymous / not a statement)
    Node expr = NodeUtil.newExpr(fnNamed);
    assertFalse(NodeUtil.isStatement(fnNamed));
    assertTrue(NodeUtil.isFunctionAnonymous(fnNamed));
    assertTrue(NodeUtil.isAnonymousFunction(fnNamed));
    assertFalse(NodeUtil.isFunctionDeclaration(fnNamed));
  }

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(42)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newString(Token.NAME, "x"))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "other")));

    // Literal values
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"))));
    assertFalse(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "mutableVar"))));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.REGEXP)));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_NAME", "pkg.CONSTANT"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(123), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    // Operators
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.NOT, new Node(Token.TRUE)), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.NEG, Node.newNumber(5)), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITNOT, Node.newNumber(5)), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITAND, Node.newNumber(1)), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITOR, Node.newNumber(1)), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITXOR, Node.newNumber(1)), defines));

    // Names and Qualified Names
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_NAME"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNDEFINED_VAR"), defines));
    assertTrue(NodeUtil.isValidDefineValue(NodeUtil.newQualifiedNameNode("pkg.CONSTANT", 0, 0), defines));
    assertFalse(NodeUtil.isValidDefineValue(NodeUtil.newQualifiedNameNode("pkg.UNKNOWN", 0, 0), defines));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.SCRIPT)));

    Node block = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(block));

    block.addChildToBack(new Node(Token.EMPTY));
    block.addChildToBack(new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(block));

    block.addChildToBack(NodeUtil.newExpr(Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(block));
  }

  @Test(timeout = 4000)
  public void testAssignmentOperators() {
    int[] assignOps = {
        Token.ASSIGN, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
        Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH, Token.ASSIGN_ADD,
        Token.ASSIGN_SUB, Token.ASSIGN_MUL, Token.ASSIGN_DIV, Token.ASSIGN_MOD
    };
    int[] underlyingOps = {
        0, Token.BITOR, Token.BITXOR, Token.BITAND,
        Token.LSH, Token.RSH, Token.URSH, Token.ADD,
        Token.SUB, Token.MUL, Token.DIV, Token.MOD
    };

    for (int i = 0; i < assignOps.length; i++) {
      Node n = new Node(assignOps[i]);
      assertTrue(NodeUtil.isAssignmentOp(n));
      if (assignOps[i] != Token.ASSIGN) {
        assertEquals(underlyingOps[i], NodeUtil.getOpFromAssignmentOp(n));
      }
    }

    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test(timeout = 4000)
  public void testPrecedenceAndAssociativity() {
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
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(13, NodeUtil.precedence(Token.INC));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
    assertEquals(15, NodeUtil.precedence(Token.CALL));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));
    assertFalse(NodeUtil.isAssociative(Token.DIV));
  }

  @Test(timeout = 4000)
  public void testOpToStrMappings() {
    assertEquals("|", NodeUtil.opToStr(Token.BITOR));
    assertEquals("||", NodeUtil.opToStr(Token.OR));
    assertEquals("^", NodeUtil.opToStr(Token.BITXOR));
    assertEquals("&&", NodeUtil.opToStr(Token.AND));
    assertEquals("&", NodeUtil.opToStr(Token.BITAND));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("==", NodeUtil.opToStr(Token.EQ));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("!=", NodeUtil.opToStr(Token.NE));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("<<", NodeUtil.opToStr(Token.LSH));
    assertEquals("in", NodeUtil.opToStr(Token.IN));
    assertEquals("<=", NodeUtil.opToStr(Token.LE));
    assertEquals("<", NodeUtil.opToStr(Token.LT));
    assertEquals(">>>", NodeUtil.opToStr(Token.URSH));
    assertEquals(">>", NodeUtil.opToStr(Token.RSH));
    assertEquals(">=", NodeUtil.opToStr(Token.GE));
    assertEquals(">", NodeUtil.opToStr(Token.GT));
    assertEquals("*", NodeUtil.opToStr(Token.MUL));
    assertEquals("/", NodeUtil.opToStr(Token.DIV));
    assertEquals("%", NodeUtil.opToStr(Token.MOD));
    assertEquals("~", NodeUtil.opToStr(Token.BITNOT));
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("+", NodeUtil.opToStr(Token.POS));
    assertEquals("-", NodeUtil.opToStr(Token.NEG));
    assertEquals("=", NodeUtil.opToStr(Token.ASSIGN));
    assertEquals("|=", NodeUtil.opToStr(Token.ASSIGN_BITOR));
    assertEquals("^=", NodeUtil.opToStr(Token.ASSIGN_BITXOR));
    assertEquals("&=", NodeUtil.opToStr(Token.ASSIGN_BITAND));
    assertEquals("<<=", NodeUtil.opToStr(Token.ASSIGN_LSH));
    assertEquals(">>=", NodeUtil.opToStr(Token.ASSIGN_RSH));
    assertEquals(">>>=", NodeUtil.opToStr(Token.ASSIGN_URSH));
    assertEquals("+=", NodeUtil.opToStr(Token.ASSIGN_ADD));
    assertEquals("-=", NodeUtil.opToStr(Token.ASSIGN_SUB));
    assertEquals("*=", NodeUtil.opToStr(Token.ASSIGN_MUL));
    assertEquals("/=", NodeUtil.opToStr(Token.ASSIGN_DIV));
    assertEquals("%=", NodeUtil.opToStr(Token.ASSIGN_MOD));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.SCRIPT));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(timeout = 4000)
  public void testControlStructuresAndConditionExpressions() {
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    // While loop
    Node cond = Node.newString(Token.NAME, "c");
    Node body = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, cond, body);
    assertTrue(NodeUtil.isControlStructure(whileNode));
    assertEquals(body, NodeUtil.getLoopCodeBlock(whileNode));
    assertEquals(cond, NodeUtil.getConditionExpression(whileNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(whileNode, body));
    assertFalse(NodeUtil.isControlStructureCodeBlock(whileNode, cond));

    // Do loop
    Node doNode = new Node(Token.DO, body, cond);
    assertEquals(body, NodeUtil.getLoopCodeBlock(doNode));
    assertEquals(cond, NodeUtil.getConditionExpression(doNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(doNode, body));

    // If condition
    Node ifNode = new Node(Token.IF, cond, body);
    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, body));

    // For-in (3 children) vs standard for (4 children)
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"), body);
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));

    Node for4 = new Node(Token.FOR, Node.newString(Token.NAME, "init"), cond, Node.newString(Token.NAME, "inc"), body);
    assertFalse(NodeUtil.isForIn(for4));
    assertEquals(cond, NodeUtil.getConditionExpression(for4));

    // Case condition is null
    assertEquals(null, NodeUtil.getConditionExpression(new Node(Token.CASE)));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyUtilities() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK, new Node(Token.CATCH));
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryNodeWithFinally = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    assertTrue(NodeUtil.hasFinally(tryNodeWithFinally));
    assertEquals(catchBlock, NodeUtil.getCatchBlock(tryNodeWithFinally));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    assertTrue(NodeUtil.isTryFinallyNode(tryNodeWithFinally, finallyBlock));

    Node tryNodeNoFinally = new Node(Token.TRY, tryBlock, catchBlock);
    assertFalse(NodeUtil.hasFinally(tryNodeNoFinally));

    Node emptyCatchBlock = new Node(Token.BLOCK);
    assertFalse(NodeUtil.hasCatchHandler(emptyCatchBlock));
  }

  @Test(timeout = 4000)
  public void testSideEffectsAndMutableState() {
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node objLit = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.mayEffectMutableState(objLit));
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    assertTrue(NodeUtil.mayEffectMutableState(arrLit));
    assertFalse(NodeUtil.mayHaveSideEffects(arrLit));

    // Side-effect free constructor call
    Node newDate = new Node(Token.NEW, Node.newString(Token.NAME, "Date"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newDate));
    assertFalse(NodeUtil.mayHaveSideEffects(newDate));
    assertTrue(NodeUtil.mayEffectMutableState(newDate));

    // Constructor with side effects
    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));
    assertTrue(NodeUtil.mayHaveSideEffects(newCustom));

    // Builtin function calls without side effects: String() and Math.round()
    Node callString = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callString));

    Node mathRound = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString("round")));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathRound));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));
  }

  @Test(timeout = 4000)
  public void testCanBeSideEffected() {
    Node simpleName = Node.newString(Token.NAME, "x");
    assertTrue(NodeUtil.canBeSideEffected(simpleName));
    assertFalse(NodeUtil.canBeSideEffected(simpleName, Collections.singleton("x")));

    Node constName = Node.newString(Token.NAME, "CONST");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertFalse(NodeUtil.canBeSideEffected(constName));

    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    assertTrue(NodeUtil.canBeSideEffected(call));

    Node prop = new Node(Token.GETPROP, constName, Node.newString("val"));
    assertTrue(NodeUtil.canBeSideEffected(prop));
  }

  @Test(timeout = 4000)
  public void testTreeTraversalAndMatching() {
    Node root = new Node(Token.BLOCK);
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "targetFunc"));
    root.addChildToBack(call);
    root.addChildToBack(new Node(Token.THIS));

    assertTrue(NodeUtil.containsCall(root));
    assertTrue(NodeUtil.referencesThis(root));
    assertTrue(NodeUtil.isNameReferenced(root, "targetFunc"));
    assertFalse(NodeUtil.isNameReferenced(root, "nonExistent"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "targetFunc"));
    assertEquals(1, NodeUtil.getNodeTypeReferenceCount(root, Token.CALL));
    assertTrue(NodeUtil.isNodeTypeReferenced(root, Token.THIS));

    final int[] preCount = {0};
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) { preCount[0]++; }
    }, Predicates.<Node>alwaysTrue());
    assertTrue(preCount[0] >= 3);

    final int[] postCount = {0};
    NodeUtil.visitPostOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) { postCount[0]++; }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(preCount[0], postCount[0]);
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node parentBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node expr1 = NodeUtil.newExpr(Node.newNumber(1));
    Node expr2 = NodeUtil.newExpr(Node.newNumber(2));
    innerBlock.addChildToBack(expr1);
    innerBlock.addChildToBack(expr2);
    parentBlock.addChildToBack(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(2, parentBlock.getChildCount());
    assertEquals(expr1, parentBlock.getFirstChild());
    assertEquals(expr2, parentBlock.getLastChild());

    // Single-child block inside label
    Node label = new Node(Token.LABEL, Node.newString(Token.NAME, "lbl"));
    Node singleChildBlock = new Node(Token.BLOCK, expr1);
    label.addChildToBack(singleChildBlock);
    assertTrue(NodeUtil.tryMergeBlock(singleChildBlock));
    assertEquals(expr1, label.getLastChild());
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndPrototypeUtilities() {
    Node qName = NodeUtil.newQualifiedNameNode("MyClass.prototype.myMethod", 10, 5);
    assertEquals(Token.GETPROP, qName.getType());
    assertTrue(NodeUtil.isPrototypeProperty(qName));
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qName));

    Node classNameNode = NodeUtil.getPrototypeClassName(qName);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getString());

    Node exprAssign = NodeUtil.newExpr(new Node(Token.ASSIGN, qName, Node.newNumber(1)));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));

    Node basis = Node.newString(Token.NAME, "basis");
    basis.setLineno(42);
    basis.putProp(Node.SOURCENAME_PROP, "source.js");

    Node copyNode = NodeUtil.newName("newName", basis, "origName");
    assertEquals("newName", copyNode.getString());
    assertEquals("origName", copyNode.getProp(Node.ORIGINALNAME_PROP));
    assertEquals(42, copyNode.getLineno());
  }

  @Test(timeout = 4000)
  public void testObjectLitKeyAndLhs() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    Node val = Node.newNumber(123);
    objLit.addChildToBack(key);
    objLit.addChildToBack(val);

    assertTrue(NodeUtil.isObjectLitKey(key, objLit));
    assertFalse(NodeUtil.isObjectLitKey(val, objLit));

    Node lhs = Node.newString(Token.NAME, "target");
    Node assign = new Node(Token.ASSIGN, lhs, Node.newNumber(1));
    assertTrue(NodeUtil.isLhs(lhs, assign));
    assertFalse(NodeUtil.isLhs(Node.newNumber(1), assign));
  }

  @Test(timeout = 4000)
  public void testMethodCallChecks() {
    Node call1 = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "fn"), Node.newString("call")));
    assertTrue(NodeUtil.isFunctionObjectCall(call1));
    assertTrue(NodeUtil.isSimpleFunctionObjectCall(call1));
    assertFalse(NodeUtil.isFunctionObjectApply(call1));

    Node call2 = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "fn"), Node.newString("apply")));
    assertTrue(NodeUtil.isFunctionObjectApply(call2));
    assertFalse(NodeUtil.isFunctionObjectCall(call2));

    Node call3 = new Node(Token.CALL,
        new Node(Token.GETPROP, new Node(Token.OBJECTLIT), Node.newString("call")));
    assertTrue(NodeUtil.isFunctionObjectCall(call3));
    assertFalse(NodeUtil.isSimpleFunctionObjectCall(call3));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin(""));
    assertTrue(NodeUtil.isLatin("abcXYZ0123_$$"));
    assertFalse(NodeUtil.isLatin("latin\u0080"));
    assertFalse(NodeUtil.isLatin("unicode\u0100"));

    assertTrue(NodeUtil.isValidPropertyName("validIdent"));
    assertTrue(NodeUtil.isValidPropertyName("$"));
    assertTrue(NodeUtil.isValidPropertyName("_123"));
    assertFalse(NodeUtil.isValidPropertyName("123bad"));
    assertFalse(NodeUtil.isValidPropertyName("var")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("function")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("prop\u0080")); // non-latin
  }

  @Test(timeout = 4000)
  public void testNewFunctionNodeCreation() {
    Node param1 = Node.newString(Token.NAME, "a");
    Node param2 = Node.newString(Token.NAME, "b");
    Node body = new Node(Token.BLOCK);

    FunctionNode fn = NodeUtil.newFunctionNode("func", Arrays.asList(param1, param2), body, 15, 8);
    assertEquals("func", fn.getFunctionName());
    assertEquals(15, fn.getLineno());
    assertEquals(8, fn.getCharno());

    Node params = NodeUtil.getFnParameters(fn);
    assertEquals(Token.LP, params.getType());
    assertEquals(2, params.getChildCount());
    assertEquals("a", params.getFirstChild().getString());
    assertEquals(body, NodeUtil.getFunctionBody(fn));
  }

  @Test(timeout = 4000)
  public void testNewUndefinedNodeAndNewVarNode() {
    Node undef = NodeUtil.newUndefinedNode();
    assertEquals(Token.VOID, undef.getType());
    assertEquals(0.0, undef.getFirstChild().getDouble(), 0.0);

    Node varWithoutVal = NodeUtil.newVarNode("v", null);
    assertEquals(Token.VAR, varWithoutVal.getType());
    assertEquals("v", varWithoutVal.getFirstChild().getString());
    assertFalse(varWithoutVal.getFirstChild().hasChildren());
  }

  @Test(timeout = 4000)
  public void testLabelNameChecks() {
    Node labelName = Node.newString(Token.NAME, "myLabel");
    Node labelStmt = new Node(Token.LABEL, labelName, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLabelName(labelName));

    Node breakStmt = new Node(Token.BREAK, labelName);
    assertTrue(NodeUtil.isLabelName(labelName));

    Node continueStmt = new Node(Token.CONTINUE, labelName);
    assertTrue(NodeUtil.isLabelName(labelName));

    assertFalse(NodeUtil.isLabelName(Node.newString(Token.NAME, "freeName")));
    assertFalse(NodeUtil.isLabelName(null));
  }

  @Test(timeout = 4000)
  public void testGetAssignedValue() {
    Node nameInVar = Node.newString(Token.NAME, "x");
    Node initVal = Node.newNumber(10);
    nameInVar.addChildToBack(initVal);
    Node varNode = new Node(Token.VAR, nameInVar);

    assertEquals(initVal, NodeUtil.getAssignedValue(nameInVar));

    Node assignName = Node.newString(Token.NAME, "y");
    Node assignVal = Node.newNumber(20);
    new Node(Token.ASSIGN, assignName, assignVal);
    assertEquals(assignVal, NodeUtil.getAssignedValue(assignName));

    Node nonLhsName = Node.newString(Token.NAME, "z");
    new Node(Token.ADD, nonLhsName, Node.newNumber(1));
    assertNull(NodeUtil.getAssignedValue(nonLhsName));
  }

  @Test(timeout = 4000)
  public void testGetInfoForNameNodeAndSourceName() {
    Node nameNode = Node.newString(Token.NAME, "docName");
    assertNull(NodeUtil.getInfoForNameNode(nameNode));

    JSDocInfo info = new JSDocInfo();
    nameNode.setJSDocInfo(info);
    assertEquals(info, NodeUtil.getInfoForNameNode(nameNode));

    // Fallback to parent VAR info
    Node childName = Node.newString(Token.NAME, "varDocName");
    Node parentVar = new Node(Token.VAR, childName);
    parentVar.setJSDocInfo(info);
    assertEquals(info, NodeUtil.getInfoForNameNode(childName));

    // Source name inheritance
    assertNull(NodeUtil.getSourceName(parentVar));
    parentVar.putProp(Node.SOURCENAME_PROP, "test.js");
    assertEquals("test.js", NodeUtil.getSourceName(parentVar));
    assertEquals("test.js", NodeUtil.getSourceName(childName));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetBooleanValueThrowsOnNonLiteral() {
    NodeUtil.getBooleanValue(new Node(Token.ADD));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetBooleanValueThrowsOnUnknownName() {
    NodeUtil.getBooleanValue(Node.newString(Token.NAME, "someIdentifier"));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssignOp() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceThrowsOnUnknownToken() {
    NodeUtil.precedence(-999);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnNonOp() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsThrowsOnNonNew() {
    NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsThrowsOnNonCall() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpressionThrowsOnNodeWithoutCondition() {
    NodeUtil.getConditionExpression(new Node(Token.BLOCK));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testIsStatementThrowsWhenParentIsNull() {
    Node orphan = new Node(Token.EXPR_RESULT);
    NodeUtil.isStatement(orphan);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildThrowsOnInvalidAttempt() {
    Node parent = new Node(Token.IF);
    Node child = new Node(Token.BLOCK);
    parent.addChildToBack(child);
    NodeUtil.removeChild(parent, child);
  }
}