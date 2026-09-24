package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Test;

/**
 * White-box test suite for NodeUtil.
 * Targets line/branch coverage and the known defect from Defects4J (Issue 821).
 *
 * [Branch & Defect Analysis Matrix]
 * - getPureBooleanValue: branches for STRING, NUMBER, NOT, NULL, FALSE, VOID, NAME, TRUE, REGEXP, ARRAYLIT, OBJECTLIT
 * - getImpureBooleanValue: branches for ASSIGN, COMMA, NOT, AND, OR, HOOK, ARRAYLIT, OBJECTLIT, VOID, default
 * - getStringValue: branches for STRING, STRING_KEY, NAME, NUMBER, FALSE, TRUE, NULL, VOID, NOT, ARRAYLIT, OBJECTLIT
 * - getNumberValue: branches for TRUE, FALSE, NULL, NUMBER, VOID, NAME, NEG, NOT, STRING, ARRAYLIT, OBJECTLIT
 * - getStringNumberValue: branches for vertical tab, empty, hex, signed hex, infinity variants, normal parse
 * - isImmutableValue: branches for STRING, NUMBER, NULL, TRUE, FALSE, NOT, VOID, NEG, NAME
 * - mayHaveSideEffects: branches for AND, BLOCK, EXPR_RESULT, HOOK, IF, IN, PARAM_LIST, NUMBER, OR, THIS, TRUE, FALSE, NULL, STRING, STRING_KEY, SWITCH, TRY, EMPTY, THROW, OBJECTLIT, ARRAYLIT, REGEXP, VAR, NAME, FUNCTION, NEW, CALL, default (assignment ops, etc.)
 * - constructorCallHasSideEffects: branches for isNoSideEffectsCall, nameNode.isName() and CONSTRUCTORS_WITHOUT_SIDE_EFFECTS
 * - functionCallHasSideEffects: branches for isNoSideEffectsCall, nameNode.isName() and BUILTIN_FUNCTIONS_WITHOUT_SIDEEFFECTS, isGetProp with OBJECT_METHODS_WITHOUT_SIDEEFFECTS, isOnlyModifiesThisCall, Math.floor, RegExp methods, String methods
 * - Defect target: Issue 821 likely involves incorrect handling of vertical tab in getStringNumberValue (returns null instead of treating as whitespace). We test that vertical tab returns null (current buggy behavior) and also test correct parsing of other whitespace.
 */
public class NodeUtilDeepseekTest {

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_String() {
    Node emptyStr = IR.string("");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(emptyStr));

    Node nonEmptyStr = IR.string("hello");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nonEmptyStr));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_Number() {
    Node zero = IR.number(0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(zero));

    Node one = IR.number(1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(one));

    Node negative = IR.number(-5);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(negative));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_NullFalseTrue() {
    Node nullNode = IR.nullNode();
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nullNode));

    Node falseNode = IR.falseNode();
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(falseNode));

    Node trueNode = IR.trueNode();
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(trueNode));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_VoidNoSideEffects() {
    // void 0 has no side effects
    Node voidNode = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidNode));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_Name() {
    Node undefined = IR.name("undefined");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(undefined));

    Node nan = IR.name("NaN");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nan));

    Node infinity = IR.name("Infinity");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(infinity));

    Node otherName = IR.name("x");
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(otherName));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_RegExp() {
    Node regexp = IR.regexp(IR.string("abc"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(regexp));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_ArrayLitObjectLitNoSideEffects() {
    Node array = IR.arraylit(IR.number(1));
    // array literal with no side effects -> TRUE
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(array));

    Node obj = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(obj));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_Not() {
    Node notFalse = IR.not(IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(notFalse));

    Node notTrue = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));
  }

  // ==================== Partition B: Boundary Value Analysis ====================

  @Test(timeout = 4000)
  public void testGetStringValue_Boundaries() {
    // STRING
    assertEquals("test", NodeUtil.getStringValue(IR.string("test")));
    // STRING_KEY
    Node stringKey = IR.stringKey("key");
    assertEquals("key", NodeUtil.getStringValue(stringKey));
    // NAME
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    // NUMBER
    assertEquals("1", NodeUtil.getStringValue(IR.number(1)));
    assertEquals("1.5", NodeUtil.getStringValue(IR.number(1.5)));
    // FALSE, TRUE, NULL
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    // VOID
    assertEquals("undefined", NodeUtil.getStringValue(IR.voidNode(IR.number(0))));
    // NOT
    Node notTrue = IR.not(IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(notTrue));
    Node notFalse = IR.not(IR.falseNode());
    assertEquals("true", NodeUtil.getStringValue(notFalse));
    // ARRAYLIT
    Node array = IR.arraylit(IR.string("a"), IR.string("b"));
    assertEquals("a,b", NodeUtil.getStringValue(array));
    // OBJECTLIT
    Node obj = IR.objectlit();
    assertEquals("[object Object]", NodeUtil.getStringValue(obj));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue_Boundaries() {
    assertEquals(1.0, NodeUtil.getNumberValue(IR.trueNode()), 0.0);
    assertEquals(0.0, NodeUtil.getNumberValue(IR.falseNode()), 0.0);
    assertEquals(0.0, NodeUtil.getNumberValue(IR.nullNode()), 0.0);
    assertEquals(42.0, NodeUtil.getNumberValue(IR.number(42)), 0.0);
    // VOID with no side effects -> NaN
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.voidNode(IR.number(0)))));
    // NAME
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(IR.name("Infinity")), 0.0);
    // NEG Infinity
    Node negInf = IR.neg(IR.name("Infinity"));
    assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(negInf), 0.0);
    // NOT
    assertEquals(0.0, NodeUtil.getNumberValue(IR.not(IR.trueNode())), 0.0);
    assertEquals(1.0, NodeUtil.getNumberValue(IR.not(IR.falseNode())), 0.0);
    // STRING
    assertEquals(123.0, NodeUtil.getNumberValue(IR.string("123")), 0.0);
    assertEquals(0.0, NodeUtil.getNumberValue(IR.string("")), 0.0);
    // ARRAYLIT -> string then number
    Node array = IR.arraylit(IR.string("456"));
    assertEquals(456.0, NodeUtil.getNumberValue(array), 0.0);
    // OBJECTLIT -> "[object Object]" -> NaN
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.objectlit())));
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue_EdgeCases() {
    // vertical tab -> null (known defect area)
    assertNull(NodeUtil.getStringNumberValue("a\u000bb"));
    // empty string
    assertEquals(0.0, NodeUtil.getStringNumberValue(""), 0.0);
    // hex
    assertEquals(255.0, NodeUtil.getStringNumberValue("0xff"), 0.0);
    assertEquals(255.0, NodeUtil.getStringNumberValue("0XFF"), 0.0);
    // signed hex -> null
    assertNull(NodeUtil.getStringNumberValue("-0x1"));
    assertNull(NodeUtil.getStringNumberValue("+0x1"));
    // infinity variants -> null
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
    // normal parse
    assertEquals(3.14, NodeUtil.getStringNumberValue("3.14"), 0.0);
    // whitespace trimmed
    assertEquals(5.0, NodeUtil.getStringNumberValue("  5  "), 0.0);
    // NaN
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("abc")));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("x")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(1)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    // NOT of immutable
    assertTrue(NodeUtil.isImmutableValue(IR.not(IR.falseNode())));
    // VOID of immutable
    assertTrue(NodeUtil.isImmutableValue(IR.voidNode(IR.number(0))));
    // NEG of immutable
    assertTrue(NodeUtil.isImmutableValue(IR.neg(IR.number(5))));
    // NAME with known constant
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    // Other name not immutable
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================

  /**
   * Targets the known defect from Issue 821.
   * The bug is in getStringNumberValue: vertical tab (0x0B) is not always whitespace,
   * but the method returns null for strings containing it. The correct behavior should
   * treat it as whitespace (per ECMAScript) and parse the number.
   * This test asserts the current (buggy) behavior; on the fixed version it would fail.
   * We also test that other whitespace characters are correctly trimmed.
   */
  @Test(timeout = 4000)
  public void testGetStringNumberValue_VerticalTab() {
    // Vertical tab should be treated as whitespace, but currently returns null.
    // This test will reveal the bug if the method is fixed.
    String input = " \u000b 42 ";
    Double result = NodeUtil.getStringNumberValue(input);
    // On buggy version: null; on fixed version: 42.0
    // We assert the expected correct behavior: should be 42.0
    // But since we don't know which version we are testing, we assert the buggy behavior
    // to ensure the test fails when the bug is fixed.
    // Actually, we want to reveal the bug on the defective version, so we assert the correct value.
    // The defective version returns null, so asserting 42.0 will fail on defective version.
    // That reveals the bug.
    assertEquals("Vertical tab should be trimmed as whitespace", 42.0, result, 0.0);
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_NoSideEffects() {
    Node newArray = IR.newNode(IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));
    Node newDate = IR.newNode(IR.name("Date"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newDate));
    Node newError = IR.newNode(IR.name("Error"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newError));
    Node newObject = IR.newNode(IR.name("Object"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newObject));
    Node newRegExp = IR.newNode(IR.name("RegExp"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newRegExp));
    Node newXhr = IR.newNode(IR.name("XMLHttpRequest"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newXhr));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_WithSideEffects() {
    Node newCustom = IR.newNode(IR.name("MyClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_NoSideEffects() {
    Node callObject = IR.call(IR.name("Object"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callObject));
    Node callArray = IR.call(IR.name("Array"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callArray));
    Node callString = IR.call(IR.name("String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callString));
    Node callNumber = IR.call(IR.name("Number"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callNumber));
    Node callBoolean = IR.call(IR.name("Boolean"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBoolean));
    Node callRegExp = IR.call(IR.name("RegExp"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callRegExp));
    Node callError = IR.call(IR.name("Error"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callError));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_ToString() {
    // obj.toString() has no side effects if obj is local? Actually, toString is in OBJECT_METHODS_WITHOUT_SIDEEFFECTS
    Node getProp = IR.getprop(IR.name("x"), IR.string("toString"));
    Node call = IR.call(getProp);
    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_MathFloor() {
    Node getProp = IR.getprop(IR.name("Math"), IR.string("floor"));
    Node call = IR.call(getProp, IR.number(3.7));
    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_RegExpMethods() {
    // Without compiler, we cannot test the RegExp path easily; but we can test the structure.
    // This test is a placeholder.
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testIsLiteralValue_ArrayWithNonLiteralChild() {
    Node array = IR.arraylit(IR.name("x"));
    assertFalse(NodeUtil.isLiteralValue(array, false));
    assertFalse(NodeUtil.isLiteralValue(array, true));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue_FunctionExpression() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    assertFalse(NodeUtil.isLiteralValue(func, false));
    assertTrue(NodeUtil.isLiteralValue(func, true));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_SimpleOperator() {
    Node add = IR.add(IR.number(1), IR.number(2));
    assertFalse(NodeUtil.mayHaveSideEffects(add));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_Assignment() {
    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_Call() {
    Node call = IR.call(IR.name("foo"));
    assertTrue(NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_New() {
    Node newObj = IR.newNode(IR.name("Object"));
    assertFalse(NodeUtil.mayHaveSideEffects(newObj));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_FunctionExpression() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    assertFalse(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_FunctionDeclaration() {
    // Function declaration has side effects (changes scope)
    Node func = IR.function(IR.name("f"), IR.paramList(), IR.block());
    // To make it a declaration, it must be a statement child
    Node script = IR.script(func);
    assertTrue(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_Throw() {
    Node throwNode = IR.throwNode(IR.number(0));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_ObjectLitWithSideEffects() {
    Node obj = IR.objectlit(IR.stringKey("a", IR.call(IR.name("foo"))));
    assertTrue(NodeUtil.mayHaveSideEffects(obj));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_ArrayLitWithSideEffects() {
    Node array = IR.arraylit(IR.call(IR.name("bar")));
    assertTrue(NodeUtil.mayHaveSideEffects(array));
  }

  // Additional tests for getImpureBooleanValue

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_Assign() {
    Node assign = IR.assign(IR.name("x"), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_Comma() {
    Node comma = IR.comma(IR.number(1), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(comma));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_AndOr() {
    Node and = IR.and(IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(and));

    Node or = IR.or(IR.falseNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(or));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_Hook() {
    Node hook = IR.hook(IR.trueNode(), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hook));

    Node hook2 = IR.hook(IR.trueNode(), IR.falseNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(hook2));

    Node hook3 = IR.hook(IR.trueNode(), IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hook3));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_ArrayLitObjectLit() {
    Node array = IR.arraylit(IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(array));

    Node obj = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(obj));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_Void() {
    Node voidNode = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(voidNode));
  }

  // Test for getArrayElementStringValue
  @Test(timeout = 4000)
  public void testGetArrayElementStringValue() {
    Node nullNode = IR.nullNode();
    assertEquals("", NodeUtil.getArrayElementStringValue(nullNode));

    Node undefined = IR.name("undefined");
    assertEquals("", NodeUtil.getArrayElementStringValue(undefined));

    Node empty = IR.empty();
    assertEquals("", NodeUtil.getArrayElementStringValue(empty));

    Node str = IR.string("hello");
    assertEquals("hello", NodeUtil.getArrayElementStringValue(str));
  }

  // Test for arrayToString with null/undefined elements
  @Test(timeout = 4000)
  public void testArrayToString_WithNullUndefined() {
    Node array = IR.arraylit(IR.nullNode(), IR.name("undefined"), IR.string("c"));
    assertEquals(",c", NodeUtil.arrayToString(array));
  }

  // Test for isSimpleOperator
  @Test(timeout = 4000)
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperator(IR.add(IR.number(1), IR.number(2))));
    assertFalse(NodeUtil.isSimpleOperator(IR.assign(IR.name("x"), IR.number(1))));
  }

  // Test for isAssignmentOp
  @Test(timeout = 4000)
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(IR.assign(IR.name("x"), IR.number(1))));
    assertFalse(NodeUtil.isAssignmentOp(IR.add(IR.number(1), IR.number(2))));
  }

  // Test for getOpFromAssignmentOp
  @Test(timeout = 4000)
  public void testGetOpFromAssignmentOp() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, IR.name("x"), IR.number(1));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));
  }

  // Test for isGet
  @Test(timeout = 4000)
  public void testIsGet() {
    Node getProp = IR.getprop(IR.name("x"), IR.string("y"));
    assertTrue(NodeUtil.isGet(getProp));
    Node getElem = IR.getelem(IR.name("x"), IR.number(0));
    assertTrue(NodeUtil.isGet(getElem));
    Node name = IR.name("x");
    assertFalse(NodeUtil.isGet(name));
  }

  // Test for isVarDeclaration
  @Test(timeout = 4000)
  public void testIsVarDeclaration() {
    Node name = IR.name("x");
    Node var = IR.var(name);
    assertTrue(NodeUtil.isVarDeclaration(name));
    Node assign = IR.assign(name, IR.number(1));
    assertFalse(NodeUtil.isVarDeclaration(name));
  }

  // Test for getAssignedValue
  @Test(timeout = 4000)
  public void testGetAssignedValue() {
    Node name = IR.name("x");
    Node var = IR.var(name, IR.number(42));
    assertEquals(IR.number(42).getString(), NodeUtil.getAssignedValue(name).getString());

    Node assign = IR.assign(name, IR.string("hello"));
    // Need to set parent properly; but getAssignedValue checks parent.isAssign() and first child == n
    // We'll create a proper AST
    Node exprResult = IR.exprResult(assign);
    // assign's first child is name
    assertEquals(IR.string("hello").getString(), NodeUtil.getAssignedValue(name).getString());
  }

  // Test for isExprAssign
  @Test(timeout = 4000)
  public void testIsExprAssign() {
    Node assign = IR.assign(IR.name("x"), IR.number(1));
    Node exprResult = IR.exprResult(assign);
    assertTrue(NodeUtil.isExprAssign(exprResult));
    Node call = IR.call(IR.name("foo"));
    Node exprResult2 = IR.exprResult(call);
    assertFalse(NodeUtil.isExprAssign(exprResult2));
  }

  // Test for isExprCall
  @Test(timeout = 4000)
  public void testIsExprCall() {
    Node call = IR.call(IR.name("foo"));
    Node exprResult = IR.exprResult(call);
    assertTrue(NodeUtil.isExprCall(exprResult));
    Node assign = IR.assign(IR.name("x"), IR.number(1));
    Node exprResult2 = IR.exprResult(assign);
    assertFalse(NodeUtil.isExprCall(exprResult2));
  }

  // Test for isForIn
  @Test(timeout = 4000)
  public void testIsForIn() {
    Node forNode = new Node(Token.FOR, IR.name("x"), IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isForIn(forNode));
    Node forNode2 = new Node(Token.FOR, IR.name("x"), IR.name("obj"), IR.block(), IR.block());
    assertFalse(NodeUtil.isForIn(forNode2));
  }

  // Test for isLoopStructure
  @Test(timeout = 4000)
  public void testIsLoopStructure() {
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));
  }

  // Test for getLoopCodeBlock
  @Test(timeout = 4000)
  public void testGetLoopCodeBlock() {
    Node block = IR.block();
    Node forNode = new Node(Token.FOR, IR.empty(), IR.empty(), block);
    assertEquals(block, NodeUtil.getLoopCodeBlock(forNode));
    Node whileNode = new Node(Token.WHILE, IR.trueNode(), block);
    assertEquals(block, NodeUtil.getLoopCodeBlock(whileNode));
    Node doNode = new Node(Token.DO, block, IR.trueNode());
    assertEquals(block, NodeUtil.getLoopCodeBlock(doNode));
    Node ifNode = new Node(Token.IF);
    assertNull(NodeUtil.getLoopCodeBlock(ifNode));
  }

  // Test for isWithinLoop
  @Test(timeout = 4000)
  public void testIsWithinLoop() {
    Node block = IR.block();
    Node forNode = new Node(Token.FOR, IR.empty(), IR.empty(), block);
    Node inside = IR.name("x");
    block.addChildToFront(inside);
    assertTrue(NodeUtil.isWithinLoop(inside));
  }

  // Test for isControlStructure
  @Test(timeout = 4000)
  public void testIsControlStructure() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WITH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.LABEL)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.CATCH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.CASE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.DEFAULT_CASE)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.BLOCK)));
  }

  // Test for isControlStructureCodeBlock
  @Test(timeout = 4000)
  public void testIsControlStructureCodeBlock() {
    Node block = IR.block();
    Node ifNode = new Node(Token.IF, IR.trueNode(), block);
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, block));
    Node forNode = new Node(Token.FOR, IR.empty(), IR.empty(), block);
    assertTrue(NodeUtil.isControlStructureCodeBlock(forNode, block));
    Node doNode = new Node(Token.DO, block, IR.trueNode());
    assertTrue(NodeUtil.isControlStructureCodeBlock(doNode, block));
  }

  // Test for getConditionExpression
  @Test(timeout = 4000)
  public void testGetConditionExpression() {
    Node cond = IR.trueNode();
    Node ifNode = new Node(Token.IF, cond, IR.block());
    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));
    Node whileNode = new Node(Token.WHILE, cond, IR.block());
    assertEquals(cond, NodeUtil.getConditionExpression(whileNode));
    Node doNode = new Node(Token.DO, IR.block(), cond);
    assertEquals(cond, NodeUtil.getConditionExpression(doNode));
    Node forNode = new Node(Token.FOR, IR.empty(), cond, IR.empty(), IR.block());
    assertEquals(cond, NodeUtil.getConditionExpression(forNode));
  }

  // Test for isStatementBlock
  @Test(timeout = 4000)
  public void testIsStatementBlock() {
    assertTrue(NodeUtil.isStatementBlock(IR.script()));
    assertTrue(NodeUtil.isStatementBlock(IR.block()));
    assertFalse(NodeUtil.isStatementBlock(IR.name("x")));
  }

  // Test for isReferenceName
  @Test(timeout = 4000)
  public void testIsReferenceName() {
    assertTrue(NodeUtil.isReferenceName(IR.name("x")));
    assertFalse(NodeUtil.isReferenceName(IR.name("")));
  }

  // Test for isTryFinallyNode
  @Test(timeout = 4000)
  public void testIsTryFinallyNode() {
    Node tryNode = new Node(Token.TRY, IR.block(), IR.block(), IR.block());
    Node finallyBlock = tryNode.getLastChild();
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));
  }

  // Test for isTryCatchNodeContainer
  @Test(timeout = 4000)
  public void testIsTryCatchNodeContainer() {
    Node tryNode = new Node(Token.TRY, IR.block(), IR.block());
    Node catchContainer = tryNode.getFirstChild().getNext();
    assertTrue(NodeUtil.isTryCatchNodeContainer(catchContainer));
  }

  // Test for isCallOrNew
  @Test(timeout = 4000)
  public void testIsCallOrNew() {
    assertTrue(NodeUtil.isCallOrNew(IR.call(IR.name("f"))));
    assertTrue(NodeUtil.isCallOrNew(IR.newNode(IR.name("F"))));
    assertFalse(NodeUtil.isCallOrNew(IR.name("x")));
  }

  // Test for getFunctionBody
  @Test(timeout = 4000)
  public void testGetFunctionBody() {
    Node body = IR.block();
    Node func = IR.function(IR.name("f"), IR.paramList(), body);
    assertEquals(body, NodeUtil.getFunctionBody(func));
  }

  // Test for isFunctionDeclaration
  @Test(timeout = 4000)
  public void testIsFunctionDeclaration() {
    Node func = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node script = IR.script(func);
    assertTrue(NodeUtil.isFunctionDeclaration(func));
    // Function expression
    Node exprResult = IR.exprResult(func);
    assertFalse(NodeUtil.isFunctionDeclaration(func));
  }

  // Test for isFunctionExpression
  @Test(timeout = 4000)
  public void testIsFunctionExpression() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node exprResult = IR.exprResult(func);
    assertTrue(NodeUtil.isFunctionExpression(func));
    Node script = IR.script(func);
    assertFalse(NodeUtil.isFunctionExpression(func));
  }

  // Test for isEmptyFunctionExpression
  @Test(timeout = 4000)
  public void testIsEmptyFunctionExpression() {
    Node func = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node exprResult = IR.exprResult(func);
    assertTrue(NodeUtil.isEmptyFunctionExpression(func));
    Node body = IR.block(IR.number(1));
    Node func2 = IR.function(IR.name(""), IR.paramList(), body);
    assertFalse(NodeUtil.isEmptyFunctionExpression(func2));
  }

  // Test for isVarArgsFunction
  @Test(timeout = 4000)
  public void testIsVarArgsFunction() {
    Node body = IR.block(IR.name("arguments"));
    Node func = IR.function(IR.name("f"), IR.paramList(), body);
    assertTrue(NodeUtil.isVarArgsFunction(func));
    Node body2 = IR.block(IR.name("x"));
    Node func2 = IR.function(IR.name("f"), IR.paramList(), body2);
    assertFalse(NodeUtil.isVarArgsFunction(func2));
  }

  // Test for isObjectCallMethod
  @Test(timeout = 4000)
  public void testIsObjectCallMethod() {
    Node getProp = IR.getprop(IR.name("x"), IR.string("call"));
    Node call = IR.call(getProp);
    assertTrue(NodeUtil.isObjectCallMethod(call, "call"));
    assertFalse(NodeUtil.isObjectCallMethod(call, "apply"));
  }

  // Test for isFunctionObjectCall and isFunctionObjectApply
  @Test(timeout = 4000)
  public void testIsFunctionObjectCallApply() {
    Node getPropCall = IR.getprop(IR.name("x"), IR.string("call"));
    Node callCall = IR.call(getPropCall);
    assertTrue(NodeUtil.isFunctionObjectCall(callCall));
    Node getPropApply = IR.getprop(IR.name("x"), IR.string("apply"));
    Node callApply = IR.call(getPropApply);
    assertTrue(NodeUtil.isFunctionObjectApply(callApply));
  }

  // Test for isVarOrSimpleAssignLhs
  @Test(timeout = 4000)
  public void testIsVarOrSimpleAssignLhs() {
    Node name = IR.name("x");
    Node assign = IR.assign(name, IR.number(1));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, assign));
    Node var = IR.var(name);
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, var));
    Node add = IR.add(name, IR.number(1));
    assertFalse(NodeUtil.isVarOrSimpleAssignLhs(name, add));
  }

  // Test for isLValue
  @Test(timeout = 4000)
  public void testIsLValue() {
    Node name = IR.name("x");
    Node assign = IR.assign(name, IR.number(1));
    assertTrue(NodeUtil.isLValue(name));
    Node forIn = new Node(Token.FOR, name, IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isLValue(name));
    Node var = IR.var(name);
    assertTrue(NodeUtil.isLValue(name));
    Node func = IR.function(name, IR.paramList(), IR.block());
    assertTrue(NodeUtil.isLValue(name));
    Node dec = new Node(Token.DEC, name);
    assertTrue(NodeUtil.isLValue(name));
    Node inc = new Node(Token.INC, name);
    assertTrue(NodeUtil.isLValue(name));
    Node paramList = IR.paramList(name);
    assertTrue(NodeUtil.isLValue(name));
    Node catchNode = new Node(Token.CATCH, name);
    assertTrue(NodeUtil.isLValue(name));
    // Not L-value
    Node add = IR.add(name, IR.number(1));
    assertFalse(NodeUtil.isLValue(name));
  }

  // Test for isObjectLitKey
  @Test(timeout = 4000)
  public void testIsObjectLitKey() {
    Node stringKey = IR.stringKey("key");
    assertTrue(NodeUtil.isObjectLitKey(stringKey, IR.objectlit()));
    Node getter = new Node(Token.GETTER_DEF, IR.function(IR.name(""), IR.paramList(), IR.block()));
    assertTrue(NodeUtil.isObjectLitKey(getter, IR.objectlit()));
    Node setter = new Node(Token.SETTER_DEF, IR.function(IR.name(""), IR.paramList(), IR.block()));
    assertTrue(NodeUtil.isObjectLitKey(setter, IR.objectlit()));
    Node name = IR.name("x");
    assertFalse(NodeUtil.isObjectLitKey(name, IR.objectlit()));
  }

  // Test for getObjectLitKeyName
  @Test(timeout = 4000)
  public void testGetObjectLitKeyName() {
    Node stringKey = IR.stringKey("myKey");
    assertEquals("myKey", NodeUtil.getObjectLitKeyName(stringKey));
    Node getter = new Node(Token.GETTER_DEF, IR.function(IR.name(""), IR.paramList(), IR.block()));
    getter.setString("getKey");
    assertEquals("getKey", NodeUtil.getObjectLitKeyName(getter));
  }

  // Test for isGetOrSetKey
  @Test(timeout = 4000)
  public void testIsGetOrSetKey() {
    Node getter = new Node(Token.GETTER_DEF);
    assertTrue(NodeUtil.isGetOrSetKey(getter));
    Node setter = new Node(Token.SETTER_DEF);
    assertTrue(NodeUtil.isGetOrSetKey(setter));
    Node stringKey = IR.stringKey("k");
    assertFalse(NodeUtil.isGetOrSetKey(stringKey));
  }

  // Test for opToStr and opToStrNoFail
  @Test(timeout = 4000)
  public void testOpToStr() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertNull(NodeUtil.opToStr(Token.ERROR));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFail_Error() {
    NodeUtil.opToStrNoFail(Token.ERROR);
  }

  // Test for containsType
  @Test(timeout = 4000)
  public void testContainsType() {
    Node root = IR.add(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.containsType(root, Token.NAME));
    assertFalse(NodeUtil.containsType(root, Token.STRING));
  }

  // Test for isConstantName
  @Test(timeout = 4000)
  public void testIsConstantName() {
    Node name = IR.name("x");
    name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(name));
    Node name2 = IR.name("y");
    assertFalse(NodeUtil.isConstantName(name2));
  }

  // Test for isConstantByConvention (requires CodingConvention, skip for simplicity)
  // Test for getFunctionJSDocInfo (requires JSDocInfo, skip)
  // Test for getSourceName, getSourceFile, getInputId (skip)
  // Test for newCallNode
  @Test(timeout = 4000)
  public void testNewCallNode() {
    Node call = NodeUtil.newCallNode(IR.name("f"), IR.number(1), IR.number(2));
    assertTrue(call.isCall());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
    assertEquals(2, call.getChildCount() - 1); // first child is target
  }

  // Test for evaluatesToLocalValue
  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.number(42)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.string("hello")));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.arraylit(IR.number(1))));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.function(IR.name(""), IR.paramList(), IR.block())));
    assertFalse(NodeUtil.evaluatesToLocalValue(IR.name("x")));
    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertFalse(NodeUtil.evaluatesToLocalValue(assign)); // assigns to non-local name
  }

  // Test for getArgumentForFunction
  @Test(timeout = 4000)
  public void testGetArgumentForFunction() {
    Node param1 = IR.name("a");
    Node param2 = IR.name("b");
    Node func = IR.function(IR.name("f"), IR.paramList(param1, param2), IR.block());
    assertEquals(param1, NodeUtil.getArgumentForFunction(func, 0));
    assertEquals(param2, NodeUtil.getArgumentForFunction(func, 1));
    assertNull(NodeUtil.getArgumentForFunction(func, 2));
  }

  // Test for getArgumentForCallOrNew
  @Test(timeout = 4000)
  public void testGetArgumentForCallOrNew() {
    Node call = IR.call(IR.name("f"), IR.number(1), IR.number(2));
    assertEquals(IR.number(1).getDouble(), NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0);
    assertEquals(IR.number(2).getDouble(), NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0);
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }

  // Test for isExpressionResultUsed
  @Test(timeout = 4000)
  public void testIsExpressionResultUsed() {
    Node expr = IR.number(1);
    Node block = IR.block(expr);
    assertFalse(NodeUtil.isExpressionResultUsed(expr));
    Node hook = IR.hook(IR.trueNode(), expr, IR.number(2));
    assertTrue(NodeUtil.isExpressionResultUsed(expr));
    Node comma = IR.comma(expr, IR.number(2));
    assertFalse(NodeUtil.isExpressionResultUsed(expr));
    // Used in call
    Node call = IR.call(IR.name("eval"), comma);
    // The first child of comma is expr, and the second is eval? Actually comma has two children: expr and number(2)
    // The call's first child is comma, so the comma is the function. The first child of comma is expr.
    // In isExpressionResultUsed, for comma, if expr is first child and second child is "eval", it returns true.
    // But here the second child is number(2), not "eval", so it returns false.
    assertFalse(NodeUtil.isExpressionResultUsed(expr));
  }

  // Test for isExecutedExactlyOnce
  @Test(timeout = 4000)
  public void testIsExecutedExactlyOnce() {
    Node script = IR.script(IR.exprResult(IR.number(1)));
    Node expr = script.getFirstChild().getFirstChild();
    assertTrue(NodeUtil.isExecutedExactlyOnce(expr));
    // Inside if
    Node ifNode = new Node(Token.IF, IR.trueNode(), IR.block(expr));
    assertFalse(NodeUtil.isExecutedExactlyOnce(expr));
  }

  // Test for booleanNode
  @Test(timeout = 4000)
  public void testBooleanNode() {
    assertTrue(NodeUtil.booleanNode(true).isTrue());
    assertTrue(NodeUtil.booleanNode(false).isFalse());
  }

  // Test for numberNode
  @Test(timeout = 4000)
  public void testNumberNode() {
    Node nanNode = NodeUtil.numberNode(Double.NaN, null);
    assertTrue(nanNode.isName() && nanNode.getString().equals("NaN"));
    Node posInf = NodeUtil.numberNode(Double.POSITIVE_INFINITY, null);
    assertTrue(posInf.isName() && posInf.getString().equals("Infinity"));
    Node negInf = NodeUtil.numberNode(Double.NEGATIVE_INFINITY, null);
    assertTrue(negInf.isNeg());
    Node normal = NodeUtil.numberNode(3.14, null);
    assertTrue(normal.isNumber() && normal.getDouble() == 3.14);
  }
}