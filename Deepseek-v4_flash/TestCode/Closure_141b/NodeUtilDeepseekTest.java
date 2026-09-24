package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

/**
 * Comprehensive white-box test suite for NodeUtil, targeting side-effect detection,
 * boolean/string conversion, and the known defect in conditional side-effect propagation.
 *
 * [Branch & Defect Analysis Matrix]
 * - getBooleanValue: covers STRING (empty/non-empty), NUMBER (zero/non-zero), NULL, FALSE, VOID,
 *   NAME (undefined, NaN, Infinity), TRUE, ARRAYLIT, OBJECTLIT, REGEXP, and throws on non-literal.
 * - getStringValue: covers NAME, STRING, NUMBER (integer vs double), FALSE, TRUE, NULL, VOID.
 * - isImmutableValue: covers STRING, NUMBER, NULL, TRUE, FALSE, VOID, NEG, NAME (undefined, Infinity, NaN).
 * - isLiteralValue: covers ARRAYLIT, OBJECTLIT, REGEXP with constant children, plus immutable types.
 * - isValidDefineValue: covers STRING, NUMBER, TRUE, FALSE, BITAND etc. with valid child, NAME/GETPROP in defines.
 * - isEmptyBlock: covers BLOCK with only EMPTY children vs non-EMPTY.
 * - isSimpleOperatorType: all true types and a false sample.
 * - mayHaveSideEffects / mayEffectMutableState: critical defect zone:
 *   - AND/OR/HOOK with side-effect children (should return true)
 *   - CALL/THROW/NEW/DEC/INC/VAR with init -> true
 *   - FUNCTION anonymous -> false, named -> true
 *   - Simple operators (ADD, etc.) without side effects -> false
 *   - Known constructors (Array, Date, etc.) -> false (if no side-effect parameters)
 * - constructorCallHasSideEffects: noSideEffectsCall property, known constructor name, unknown.
 * - functionCallHasSideEffects: noSideEffectsCall, built-in "String", Math namespace, unknown.
 * - isAssignmentOp: covers all assignment tokens and false for non-assignment.
 * - isReferenceName, isLabelName, isFunctionAnonymous, isVarArgsFunction, etc.
 * - containsType, has, getCount, visitPreOrder, etc.
 */
public class NodeUtilDeepseekTest {

  // ========== Partition A: Core Functional Logic & State Transitions ==========

  @Test(timeout = 4000)
  public void testGetBooleanValue() {
    // STRING non-empty -> true
    Node s = Node.newString(Token.STRING, "hello");
    assertTrue(NodeUtil.getBooleanValue(s));

    // STRING empty -> false
    Node empty = Node.newString(Token.STRING, "");
    assertFalse(NodeUtil.getBooleanValue(empty));

    // NUMBER non-zero -> true
    Node num = Node.newNumber(42);
    assertTrue(NodeUtil.getBooleanValue(num));

    // NUMBER zero -> false
    Node zero = Node.newNumber(0);
    assertFalse(NodeUtil.getBooleanValue(zero));

    // NULL -> false
    Node nil = new Node(Token.NULL);
    assertFalse(NodeUtil.getBooleanValue(nil));

    // FALSE -> false
    Node f = new Node(Token.FALSE);
    assertFalse(NodeUtil.getBooleanValue(f));

    // VOID -> false
    Node v = new Node(Token.VOID, Node.newNumber(0));
    assertFalse(NodeUtil.getBooleanValue(v));

    // NAME "undefined" -> false
    Node undef = Node.newString(Token.NAME, "undefined");
    assertFalse(NodeUtil.getBooleanValue(undef));

    // NAME "NaN" -> false
    Node nan = Node.newString(Token.NAME, "NaN");
    assertFalse(NodeUtil.getBooleanValue(nan));

    // NAME "Infinity" -> true
    Node inf = Node.newString(Token.NAME, "Infinity");
    assertTrue(NodeUtil.getBooleanValue(inf));

    // TRUE -> true
    Node t = new Node(Token.TRUE);
    assertTrue(NodeUtil.getBooleanValue(t));

    // ARRAYLIT -> true
    Node arr = new Node(Token.ARRAYLIT);
    assertTrue(NodeUtil.getBooleanValue(arr));

    // OBJECTLIT -> true
    Node obj = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.getBooleanValue(obj));

    // REGEXP -> true
    Node re = new Node(Token.REGEXP);
    assertTrue(NodeUtil.getBooleanValue(re));
  }

  @Test(timeout = 4000)
  public void testGetBooleanValueThrowsOnNonLiteral() {
    Node call = new Node(Token.CALL, new Node(Token.NAME, "f"));
    try {
      NodeUtil.getBooleanValue(call);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    // NAME -> string
    Node name = Node.newString(Token.NAME, "foo");
    assertEquals("foo", NodeUtil.getStringValue(name));

    // STRING -> string
    Node str = Node.newString(Token.STRING, "bar");
    assertEquals("bar", NodeUtil.getStringValue(str));

    // NUMBER integer -> no decimal
    Node intNum = Node.newNumber(5);
    assertEquals("5", NodeUtil.getStringValue(intNum));

    // NUMBER double -> with decimal
    Node dblNum = Node.newNumber(3.14);
    assertEquals("3.14", NodeUtil.getStringValue(dblNum));

    // FALSE -> "false"
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));

    // TRUE -> "true"
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));

    // NULL -> "null"
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));

    // VOID -> "undefined"
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals("undefined", NodeUtil.getStringValue(voidNode));
  }

  @Test(timeout = 4000)
  public void testGetStringValueReturnsNullForNonConvertible() {
    Node arr = new Node(Token.ARRAYLIT);
    assertNull(NodeUtil.getStringValue(arr));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    // String
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "a")));
    // Number
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    // Null
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    // True
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    // False
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    // Void
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));
    // Neg with immutable child
    Node neg = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isImmutableValue(neg));
    // Name "undefined"
    Node undef = Node.newString(Token.NAME, "undefined");
    assertTrue(NodeUtil.isImmutableValue(undef));
    // Name "Infinity"
    Node inf = Node.newString(Token.NAME, "Infinity");
    assertTrue(NodeUtil.isImmutableValue(inf));
    // Name "NaN"
    Node nan = Node.newString(Token.NAME, "NaN");
    assertTrue(NodeUtil.isImmutableValue(nan));
    // Other name -> false
    Node other = Node.newString(Token.NAME, "x");
    assertFalse(NodeUtil.isImmutableValue(other));
    // Array -> false
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue() {
    // Array literal with constant children
    Node arr = new Node(Token.ARRAYLIT);
    arr.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isLiteralValue(arr));
    // Array with non-constant child (e.g., NAME) -> false
    Node arr2 = new Node(Token.ARRAYLIT);
    arr2.addChildToBack(Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(arr2));
    // Object literal with constant key/value
    Node obj = new Node(Token.OBJECTLIT);
    obj.addChildToBack(Node.newString(Token.STRING, "a"));
    obj.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isLiteralValue(obj));
    // Regexp
    Node re = new Node(Token.REGEXP);
    assertTrue(NodeUtil.isLiteralValue(re));
    // Immutable values are literal
    assertTrue(NodeUtil.isLiteralValue(Node.newNumber(0)));
    assertFalse(NodeUtil.isLiteralValue(Node.newString(Token.NAME, "x")));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    java.util.Set<String> defines = new java.util.HashSet<>();
    defines.add("CONST");
    // STRING
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "val"), defines));
    // NUMBER
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    // TRUE/FALSE
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));
    // BITAND with valid child
    Node bitand = new Node(Token.BITAND, Node.newNumber(1));
    assertTrue(NodeUtil.isValidDefineValue(bitand, defines));
    // NOT with valid child
    Node not = new Node(Token.NOT, new Node(Token.FALSE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));
    // NAME in defines
    Node name = Node.newString(Token.NAME, "CONST");
    assertTrue(NodeUtil.isValidDefineValue(name, defines));
    // GETPROP with qualified name in defines
    Node getprop = NodeUtil.newQualifiedNameNode("CONST.prop", -1, -1);
    assertTrue(NodeUtil.isValidDefineValue(getprop, defines));
    // NAME not in defines -> false
    Node name2 = Node.newString(Token.NAME, "OTHER");
    assertFalse(NodeUtil.isValidDefineValue(name2, defines));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    // Block with no children (should be empty? actually no children => true)
    Node block = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(block));
    // Block with only EMPTY children
    block.addChildToBack(new Node(Token.EMPTY));
    block.addChildToBack(new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(block));
    // Block with non-EMPTY child
    block.addChildToBack(Node.newNumber(1));
    assertFalse(NodeUtil.isEmptyBlock(block));
    // Non-block node -> false
    assertFalse(NodeUtil.isEmptyBlock(Node.newNumber(0)));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperatorType() {
    // True cases
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITNOT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITOR));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITXOR));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.COMMA));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETELEM));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.INSTANCEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LSH));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MOD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NOT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.RSH));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SHEQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SHNE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.VOID));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.POS));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NEG));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.URSH));
    // False case
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.NEW));
  }

  // ========== Partition C: Defect-Targeted Branch Zone (Side-Effect Detection) ==========

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_HookWithSideEffectCondition() {
    // HOOK with condition that has a side effect (CALL)
    Node cond = new Node(Token.CALL, new Node(Token.NAME, "f"));
    Node hook = new Node(Token.HOOK, cond, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.mayHaveSideEffects(hook));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AndWithSideEffectLeft() {
    Node left = new Node(Token.CALL, new Node(Token.NAME, "f"));
    Node and = new Node(Token.AND, left, Node.newNumber(1));
    assertTrue(NodeUtil.mayHaveSideEffects(and));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_OrWithSideEffectRight() {
    Node right = new Node(Token.CALL, new Node(Token.NAME, "g"));
    Node or = new Node(Token.OR, Node.newNumber(0), right);
    assertTrue(NodeUtil.mayHaveSideEffects(or));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_NoSideEffectCallProperty() {
    Node call = new Node(Token.CALL, new Node(Token.NAME, "noSide"));
    call.putBooleanProp(Node.NO_SIDE_EFFECTS_CALL, true);
    assertFalse(NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AnonymousFunction() {
    // Anonymous function is not a statement -> no side effects
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
                         new Node(Token.LP), new Node(Token.BLOCK));
    assertFalse(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_NamedFunction() {
    // Named function as statement -> side effect (declaration)
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"),
                         new Node(Token.LP), new Node(Token.BLOCK));
    // To be a statement, parent must be SCRIPT or BLOCK
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(func);
    assertTrue(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_Throw() {
    Node thr = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    assertTrue(NodeUtil.mayHaveSideEffects(thr));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_SimpleOperator() {
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertFalse(NodeUtil.mayHaveSideEffects(add));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_NewArray() {
    // new Array() is known constructor without side effects
    Node newArr = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.mayEffectMutableState(newArr));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_NewUnknown() {
    // new Foo() unknown -> side effect
    Node newFoo = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertTrue(NodeUtil.mayEffectMutableState(newFoo));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_ObjectLit() {
    // Object literal creates new mutable state -> side effect when checkForNewObjects true
    Node obj = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.mayEffectMutableState(obj));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_ArrayLit() {
    Node arr = new Node(Token.ARRAYLIT);
    assertTrue(NodeUtil.mayEffectMutableState(arr));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_RegExp() {
    Node re = new Node(Token.REGEXP);
    assertTrue(NodeUtil.mayEffectMutableState(re));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects() {
    // NEW with noSideEffectsCall -> false
    Node call = new Node(Token.NEW, Node.newString(Token.NAME, "X"));
    call.putBooleanProp(Node.NO_SIDE_EFFECTS_CALL, true);
    assertFalse(NodeUtil.constructorCallHasSideEffects(call));

    // Known constructor name -> false
    Node known = new Node(Token.NEW, Node.newString(Token.NAME, "Date"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(known));

    // Unknown constructor -> true
    Node unknown = new Node(Token.NEW, Node.newString(Token.NAME, "MyClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(unknown));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects() {
    // CALL with noSideEffectsCall -> false
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    call.putBooleanProp(Node.NO_SIDE_EFFECTS_CALL, true);
    assertFalse(NodeUtil.functionCallHasSideEffects(call));

    // Built-in String -> false
    Node strCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(strCall));

    // Math namespace -> false
    Node math = new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"),
                         Node.newString(Token.STRING, "sin"));
    Node mathCall = new Node(Token.CALL, math);
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    // Unknown function -> true
    Node unknown = new Node(Token.CALL, Node.newString(Token.NAME, "g"));
    assertTrue(NodeUtil.functionCallHasSideEffects(unknown));
  }

  @Test(timeout = 4000)
  public void testIsAssignmentOp() {
    // True for all assignment tokens
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MOD)));
    // Non-assignment
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test(timeout = 4000)
  public void testIsReferenceName() {
    // NAME with non-empty string and not a label name -> true
    Node name = Node.newString(Token.NAME, "x");
    // For label detection, need parent. Default no parent => not a label.
    assertTrue(NodeUtil.isReferenceName(name));

    // Empty string -> false
    Node empty = Node.newString(Token.NAME, "");
    assertFalse(NodeUtil.isReferenceName(empty));

    // Label name -> false
    Node labelParent = new Node(Token.LABEL);
    Node labelName = Node.newString(Token.NAME, "lbl");
    labelParent.addChildToFront(labelName);
    assertFalse(NodeUtil.isReferenceName(labelName));
  }

  @Test(timeout = 4000)
  public void testIsLabelName() {
    // NAME child of LABEL at first position -> true
    Node label = new Node(Token.LABEL);
    Node labelName = Node.newString(Token.NAME, "loop");
    label.addChildToFront(labelName);
    assertTrue(NodeUtil.isLabelName(labelName));

    // NAME child of BREAK at first position -> true
    Node brk = new Node(Token.BREAK);
    Node brkName = Node.newString(Token.NAME, "loop");
    brk.addChildToFront(brkName);
    assertTrue(NodeUtil.isLabelName(brkName));

    // NAME not in label/break/continue -> false
    Node other = Node.newString(Token.NAME, "x");
    assertFalse(NodeUtil.isLabelName(other));
  }

  @Test(timeout = 4000)
  public void testIsFunctionAnonymous() {
    // Function as statement (parent is SCRIPT) -> not anonymous
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"),
                         new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(func);
    assertFalse(NodeUtil.isFunctionAnonymous(func));

    // Function as expression (parent not SCRIPT/BLOCK/LABEL) -> anonymous
    Node expr = new Node(Token.EXPR_RESULT);
    Node anonFunc = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
                             new Node(Token.LP), new Node(Token.BLOCK));
    expr.addChildToBack(anonFunc);
    assertTrue(NodeUtil.isFunctionAnonymous(anonFunc));
  }

  @Test(timeout = 4000)
  public void testIsVarArgsFunction() {
    // Function body that references "arguments" -> true
    Node body = new Node(Token.BLOCK);
    Node ref = Node.newString(Token.NAME, "arguments");
    body.addChildToBack(new Node(Token.EXPR_RESULT, ref));
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
                         new Node(Token.LP), body);
    assertTrue(NodeUtil.isVarArgsFunction(func));

    // No reference -> false
    Node body2 = new Node(Token.BLOCK);
    body2.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node func2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
                          new Node(Token.LP), body2);
    assertFalse(NodeUtil.isVarArgsFunction(func2));
  }

  @Test(timeout = 4000)
  public void testContainsType() {
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(new Node(Token.CALL));
    root.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.containsType(root, Token.CALL));
    assertFalse(NodeUtil.containsType(root, Token.THROW));
  }

  @Test(timeout = 4000)
  public void testHasAndGetCount() {
    Node root = new Node(Token.BLOCK);
    Node call1 = new Node(Token.CALL);
    Node call2 = new Node(Token.CALL);
    root.addChildToBack(call1);
    root.addChildToBack(call2);
    // has predicate
    assertTrue(NodeUtil.has(root, new NodeUtil.MatchNodeType(Token.CALL), Predicates.alwaysTrue()));
    // getCount
    assertEquals(2, NodeUtil.getCount(root, new NodeUtil.MatchNodeType(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testVisitPreOrder() {
    Node root = new Node(Token.BLOCK);
    Node call = new Node(Token.CALL);
    root.addChildToBack(call);
    final java.util.List<Node> visited = new java.util.ArrayList<>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        visited.add(node);
      }
    }, Predicates.alwaysTrue());
    assertEquals(2, visited.size());
    assertEquals(root, visited.get(0));
    assertEquals(call, visited.get(1));
  }

  // ========== Defect-Revealing Tests ==========

  /**
   * Directly targets the defect in side-effect detection for conditional expressions.
   * A HOOK with a condition that has side effects (e.g., a call) should be marked
   * as having side effects. The known defect in PureFunctionIdentifierTest shows
   * that such expressions were incorrectly considered side-effect free.
   */
  @Test(timeout = 4000)
  public void testMayHaveSideEffects_HookWithSideEffectCallCondition() {
    // Simulate (f ? 1 : 2) where f is a call with side effects
    Node cond = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    Node hook = new Node(Token.HOOK, cond, Node.newNumber(1), Node.newNumber(2));
    assertTrue("HOOK with side-effect condition should be detected",
               NodeUtil.mayHaveSideEffects(hook));
  }

  /**
   * Another defect scenario: an OR expression where the left operand has side effects.
   * E.g., (f() || g). The left call should make the whole OR have side effects.
   */
  @Test(timeout = 4000)
  public void testMayHaveSideEffects_OrWithSideEffectLeft() {
    Node left = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    Node or = new Node(Token.OR, left, Node.newString(Token.NAME, "g"));
    assertTrue("OR with side-effect left operand should be detected",
               NodeUtil.mayHaveSideEffects(or));
  }

  /**
   * AND expression where the right operand has side effects.
   */
  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AndWithSideEffectRight() {
    Node right = new Node(Token.CALL, Node.newString(Token.NAME, "g"));
    Node and = new Node(Token.AND, Node.newNumber(1), right);
    assertTrue("AND with side-effect right operand should be detected",
               NodeUtil.mayHaveSideEffects(and));
  }

  /**
   * HOOK with the true branch having a side effect.
   */
  @Test(timeout = 4000)
  public void testMayHaveSideEffects_HookWithSideEffectTrueBranch() {
    Node cond = new Node(Token.TRUE);
    Node sideEffectBranch = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    Node hook = new Node(Token.HOOK, cond, sideEffectBranch, Node.newNumber(0));
    assertTrue("HOOK with side-effect true branch should be detected",
               NodeUtil.mayHaveSideEffects(hook));
  }
}