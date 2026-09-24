package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Test suite for NodeUtil covering core functional logic, boundary conditions,
 * and known defect scenarios.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core Functional Logic: getPureBooleanValue, getImpureBooleanValue,
 *   getStringValue, getNumberValue, isImmutableValue, isLiteralValue, isSimpleOperator,
 *   mayHaveSideEffects, functionCallHasSideEffects, constructorCallHasSideEffects,
 *   isFunctionExpression, isFunctionDeclaration, isGet, evaluatesToLocalValue,
 *   getFunctionName, precedence, allResultsMatch, anyResultsMatch
 * - Partition B: Boundary & BVA: null/empty strings, zero, NaN, Infinity, undefined,
 *   empty arrays/objects, MAX_POSITIVE_INTEGER_NUMBER, char boundaries
 * - Partition C: Defect-Targeted: ScopedAliases-related patterns (function names,
 *   qualified names, var declarations in goog.scope context)
 * - Partition D: Exception paths: null parents, illegal states, invalid arguments
 * - Partition E: Object Lifecycle: mapMainToClone, mtocHelper, verifyScopeChanges
 */
public class NodeUtilDeepseekTest {

  /*
   * ==========================================================================
   * Partition A: Core Functional Logic & State Transitions
   * ==========================================================================
   */

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_String() {
    Node stringNode = IR.string("");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(stringNode));

    stringNode = IR.string("hello");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(stringNode));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_Number() {
    Node numberNode = IR.number(0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(numberNode));

    numberNode = IR.number(1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(numberNode));

    numberNode = IR.number(-1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(numberNode));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_Keywords() {
    Node undefinedNode = IR.name("undefined");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(undefinedNode));

    Node nanNode = IR.name("NaN");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nanNode));

    Node infinityNode = IR.name("Infinity");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(infinityNode));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_LogicalOps() {
    // OR: lhs=true, rhs=false => true OR false = true
    Node orNode = IR.or(IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    // AND: lhs=true, rhs=false => true AND false = false
    Node andNode = IR.and(IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_Hook() {
    // HOOK: trueValue = TRUE, falseValue = TRUE => same => return TRUE
    Node hookNode = IR.hook(IR.trueNode(), IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookNode));

    // HOOK: trueValue = TRUE, falseValue = FALSE => different => UNKNOWN
    hookNode = IR.hook(IR.trueNode(), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookNode));
  }

  @Test(timeout = 4000)
  public void testGetStringValue_Keywords() {
    Node undefinedNode = IR.name("undefined");
    assertEquals("undefined", NodeUtil.getStringValue(undefinedNode));

    Node infinityNode = IR.name("Infinity");
    assertEquals("Infinity", NodeUtil.getStringValue(infinityNode));

    Node nanNode = IR.name("NaN");
    assertEquals("NaN", NodeUtil.getStringValue(nanNode));
  }

  @Test(timeout = 4000)
  public void testGetStringValue_Number() {
    assertEquals("1", NodeUtil.getStringValue(1.0));
    assertEquals("1.5", NodeUtil.getStringValue(1.5));
    assertEquals("0", NodeUtil.getStringValue(0.0));
    assertEquals("-1", NodeUtil.getStringValue(-1.0));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue_Keywords() {
    Node trueNode = IR.trueNode();
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(trueNode));

    Node falseNode = IR.falseNode();
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(falseNode));

    Node nullNode = IR.nullNode();
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(nullNode));

    Node undefinedNode = IR.name("undefined");
    assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(undefinedNode));

    Node nanNode = IR.name("NaN");
    assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(nanNode));

    Node infinityNode = IR.name("Infinity");
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(infinityNode));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue_StringHex() {
    Node hexNode = IR.string("0xFF");
    assertEquals(Double.valueOf(255.0), NodeUtil.getNumberValue(hexNode));

    hexNode = IR.string("0x1A");
    assertEquals(Double.valueOf(26.0), NodeUtil.getNumberValue(hexNode));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("test")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(42)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue() {
    // Array with empty elements
    Node arr = IR.arraylit();
    assertTrue(NodeUtil.isLiteralValue(arr, false));

    // Object with literal values
    Node obj = IR.objectlit();
    Node key = IR.stringKey("key");
    key.addChildToBack(IR.number(1));
    obj.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(obj, false));

    // Function: not a literal unless includeFunctions = true and not declaration
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    // Simulate expression position: need parent context for isStatement
    // Without parent, isStatement returns false via isStatementParent(null) -> exception
    // So the default isFunctionExpression returns true when parent is null? Let's check isFunctionExpression:
    // isStatement checks parent, if parent null Preconditions.checkState fails.
    // Let's test with a proper parent: assign = function() {}
    Node assign = IR.assign(IR.name("f"), func);
    assign.addChildToFront(IR.name("f")); // Actually assign first child is target, second is value
    // Reset: assign = target, value
    assign = IR.assign(IR.name("f"), func);
    // func is second child of assign, so isStatement for func checks parent = assign, assign is not BLOCK/SCRIPT/LABEL, so false => isFunctionExpression = true
    assertTrue(NodeUtil.isLiteralValue(func, true));
    assertFalse(NodeUtil.isLiteralValue(func, false));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.COMMA));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.OR));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.AND));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_SafeNodes() {
    assertFalse(NodeUtil.mayHaveSideEffects(IR.number(0)));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.string("")));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.trueNode()));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.falseNode()));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.nullNode()));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.name("undefined")));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.name("NaN")));
    assertFalse(NodeUtil.mayHaveSideEffects(IR.name("Infinity")));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_VarWithInit() {
    Node varNode = IR.var(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.mayHaveSideEffects(varNode));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_FunctionExpression() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    // Make it a function expression by wrapping in an assignment context
    Node assign = IR.assign(IR.name("f"), func);
    // func is now second child of assign, isFunctionExpression => true
    assertFalse(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_ObjectLitWithSideEffects() {
    // Object literal with a side-effecting value
    Node obj = IR.objectlit();
    Node key = IR.stringKey("key");
    key.addChildToBack(IR.call(IR.name("eval"))); // call has side effects
    obj.addChildToBack(key);
    assertTrue(NodeUtil.mayHaveSideEffects(obj));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_NewArray() {
    Node newArray = IR.newNode(IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_NewObject() {
    Node newObject = IR.newNode(IR.name("Object"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newObject));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_Builtins() {
    Node callObject = IR.call(IR.name("Object"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callObject));

    Node callArray = IR.call(IR.name("Array"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callArray));

    Node callString = IR.call(IR.name("String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callString));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_MathFloor() {
    Node mathFloor = IR.getprop(IR.name("Math"), IR.string("floor"));
    Node call = IR.call(mathFloor, IR.number(1.5));
    call.putBooleanProp(Node.FREE_CALL, false);
    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  /*
   * ==========================================================================
   * Partition B: Boundary Value Analysis & Extremes
   * ==========================================================================
   */

  @Test(timeout = 4000)
  public void testGetStringNumberValue_Whitespace() {
    // empty string after trim
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    // with vertical tab => null
    assertNull(NodeUtil.getStringNumberValue("\u000b"));
    // hex with sign => null for explicit signs
    assertNull(NodeUtil.getStringNumberValue("-0xFF"));
    assertNull(NodeUtil.getStringNumberValue("+0xFF"));
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue_InfinityVariants() {
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
  }

  @Test(timeout = 4000)
  public void testIsStrWhiteSpaceChar() {
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('a'));
  }

  @Test(timeout = 4000)
  public void testPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));
    assertEquals(15, NodeUtil.precedence(Token.STRING));
  }

  @Test(timeout = 4000)
  public void testIsGet() {
    Node getprop = IR.getprop(IR.name("a"), IR.string("b"));
    assertTrue(NodeUtil.isGet(getprop));

    Node getelem = IR.getelem(IR.name("a"), IR.string("b"));
    assertTrue(NodeUtil.isGet(getelem));

    assertFalse(NodeUtil.isGet(IR.name("a")));
  }

  /*
   * ==========================================================================
   * Partition C: Defect-Targeted Branch Zone (ScopedAliases context)
   * ==========================================================================
   */

  @Test(timeout = 4000)
  public void testGetFunctionName_VarPattern() {
    // var name = function() {}
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node var = IR.var(IR.name("myFunc"), func);
    // func parent = var, need to check: parent is NAME? var's first child is NAME
    // getFunctionName: parent type = NAME => returns parent.getQualifiedName()
    assertEquals("myFunc", NodeUtil.getFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName_AssignPattern() {
    // qualified.name = function() {}
    Node target = IR.getprop(IR.name("obj"), IR.string("method"));
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node assign = IR.assign(target, func);
    // getFunctionName: parent type = ASSIGN => returns parent.getFirstChild().getQualifiedName()
    assertEquals("obj.method", NodeUtil.getFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName_NamedFunctionExpression() {
    // function foo() {}
    // Standalone function: parent could be BLOCK or SCRIPT
    Node funcName = IR.name("foo");
    Node func = IR.function(funcName, IR.paramList(), IR.block());
    // Set up parent as BLOCK
    Node block = IR.block();
    block.addChildToBack(func);
    // getFunctionName: default case => returns first child qualified name
    assertEquals("foo", NodeUtil.getFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testGetNearestFunctionName_ObjectLiteralKey() {
    // { 'x' : function() {} }
    Node key = IR.stringKey("x");
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    key.addChildToBack(func);
    Node objLit = IR.objectlit();
    objLit.addChildToBack(key);
    assertEquals("x", NodeUtil.getNearestFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testAllResultsMatch_Hook() {
    Node hook = IR.hook(IR.trueNode(), IR.number(1), IR.number(2));
    assertTrue(NodeUtil.allResultsMatch(hook, NodeUtil.IMMUTABLE_PREDICATE));
  }

  @Test(timeout = 4000)
  public void testAllResultsMatch_AndWithNonImmutable() {
    Node and = IR.and(IR.number(1), IR.name("x"));
    assertFalse(NodeUtil.allResultsMatch(and, NodeUtil.IMMUTABLE_PREDICATE));
  }

  @Test(timeout = 4000)
  public void testAnyResultsMatch_Or() {
    Node or = IR.or(IR.name("x"), IR.number(42));
    assertTrue(NodeUtil.anyResultsMatch(or, NodeUtil.IMMUTABLE_PREDICATE));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_Function() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    assertTrue(NodeUtil.evaluatesToLocalValue(func));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_ArrayLit() {
    Node arr = IR.arraylit(IR.number(1), IR.number(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(arr));
  }

  /*
   * ==========================================================================
   * Partition D: Exception & Defensive Guard Paths
   * ==========================================================================
   */

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testConstructorCallHasSideEffects_NotNew() {
    Node call = IR.call(IR.name("Object"));
    NodeUtil.constructorCallHasSideEffects(call);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testFunctionCallHasSideEffects_NotCall() {
    Node name = IR.name("foo");
    NodeUtil.functionCallHasSideEffects(name);
  }

  @Test(timeout = 4000)
  public void testIsFunctionDeclaration_WithParent() {
    Node func = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node block = IR.block();
    block.addChildToBack(func);
    assertTrue(NodeUtil.isFunctionDeclaration(func));
    assertFalse(NodeUtil.isFunctionExpression(func));
  }

  @Test(timeout = 4000)
  public void testGetOpFromAssignmentOp() {
    Node assignAdd = new Node(Token.ASSIGN_ADD);
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignSub = new Node(Token.ASSIGN_SUB);
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));
  }

  /*
   * ==========================================================================
   * Partition E: Object Lifecycle & Contract Integrity
   * ==========================================================================
   */

  @Test(timeout = 4000)
  public void testMapMainToClone() {
    Node main = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node clone = main.cloneTree();
    assertTrue(main.isEquivalentTo(clone));

    java.util.Map<Node, Node> map = NodeUtil.mapMainToClone(main, clone);
    assertEquals(clone, map.get(main));
  }

  @Test(timeout = 4000)
  public void testVerifyScopeChanges() {
    Node main = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node clone = main.cloneTree();
    // Simulate change by setting different change time via internal API
    // To avoid reflection, just test that method doesn't throw for unchanged nodes
    // with verifyUnchangedNodes = true
    java.util.Map<Node, Node> map = NodeUtil.mapMainToClone(main, clone);
    // This should pass without assertions
    NodeUtil.verifyScopeChanges(map, main, true, null);
  }
}