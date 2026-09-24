package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: ClosureReverseAbstractInterpreter
 *
 * 1. Decision Branches Analyzed:
 *    - condition.getType() == CALL && condition.getChildCount() == 2
 *      - False paths: non-CALL tokens, childCount != 2 (0, 1, 3 children).
 *    - callee.getType() == GETPROP && param.isQualifiedName()
 *      - False paths: non-GETPROP callee, non-qualified param (e.g. literals).
 *    - paramType != null (via getTypeIfRefinable)
 *      - False path: refinable type is null (DEFECT ZONE).
 *    - left.getType() == NAME && "goog".equals(left.getString()) && right.getType() == STRING
 *      - False paths: left not NAME, left not "goog", right not STRING.
 *    - restricters.get(right.getString()) != null
 *      - False path: unrecognized method under "goog.*" (e.g., goog.unknown).
 *
 * 2. Restricters Equivalence Partitioning:
 *    - "isDef": outcome=true -> getRestrictedWithoutUndefined; outcome=false -> null.
 *    - "isNull": outcome=true -> NULL_TYPE; outcome=false -> getRestrictedWithoutNull.
 *    - "isDefAndNotNull": outcome=true -> getRestrictedWithoutUndefined & null; outcome=false -> null.
 *    - "isString": RestrictByTypeOfResult("string", outcome).
 *    - "isBoolean": RestrictByTypeOfResult("boolean", outcome).
 *    - "isNumber": RestrictByTypeOfResult("number", outcome).
 *    - "isFunction": RestrictByTypeOfResult("function", outcome).
 *    - "isArray":
 *        - type == null: outcome ? ARRAY_TYPE : null (Defects4J known failure path).
 *        - outcome=true: restrictToArrayVisitor (caseTopType -> topType, caseObjectType -> arrayType/null).
 *        - outcome=false: restrictToNotArrayVisitor (caseObjectType -> non-array or null).
 *    - "isObject":
 *        - type == null: outcome ? OBJECT_TYPE : null (Defects4J known failure path).
 *        - outcome=true: restrictToObjectVisitor (caseTopType -> NO_OBJECT_TYPE, caseObjectType -> type, caseFunctionType -> type).
 *        - outcome=false: restrictToNotObjectVisitor (caseObjectType -> null, caseFunctionType -> null).
 *
 * 3. Defects4J Defect Targeting:
 *    - testGoogIsArrayOnNull / testGoogIsFunctionOnNull / testGoogIsObjectOnNull:
 *      The defective version contains an unintended guard `if (paramType != null)`
 *      preventing refinement when the initial parameter type is untyped/null in blindScope.
 */
public class ClosureReverseAbstractInterpreterGeminiTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new GoogleCodingConvention();
    interpreter = new ClosureReverseAbstractInterpreter(convention, registry);
  }

  // --- Helper Methods ---

  private Node createCallNode(String calleeProp, Node paramNode) {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, calleeProp);
    Node getProp = new Node(Token.GETPROP, goog, prop);
    return new Node(Token.CALL, getProp, paramNode);
  }

  private FlowScope createBlindScope(Node paramNode, JSType initialType) {
    FlowScope scope = new LinkedFlowScope(
        new LinkedFlowScope.FlowScopeJoinOp());
    if (initialType != null && paramNode.isQualifiedName()) {
      StaticSlot<JSType> slot = scope.getSlot(paramNode.getQualifiedName());
      if (slot == null) {
        scope.inferSlotType(paramNode.getQualifiedName(), initialType);
      }
    }
    return scope;
  }

  private JSType getNative(JSTypeNative type) {
    return registry.getNativeType(type);
  }

  // ==========================================================================
  // Partition A: Core Functional Logic (All Restricters with Typed Inputs)
  // ==========================================================================

  @Test(timeout = 4000)
  public void testIsDefTrueAndFalse() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isDef", param);
    JSType unionType = registry.createUnionType(
        getNative(STRING_TYPE), getNative(VOID_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(STRING_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertSame(scope, falseScope);
  }

  @Test(timeout = 4000)
  public void testIsNullTrueAndFalse() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isNull", param);
    JSType unionType = registry.createUnionType(
        getNative(STRING_TYPE), getNative(NULL_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(NULL_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(STRING_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsDefAndNotNullTrueAndFalse() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isDefAndNotNull", param);
    JSType unionType = registry.createUnionType(
        getNative(NUMBER_TYPE), getNative(NULL_TYPE), getNative(VOID_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(NUMBER_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertSame(scope, falseScope);
  }

  @Test(timeout = 4000)
  public void testIsString() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isString", param);
    JSType unionType = registry.createUnionType(
        getNative(STRING_TYPE), getNative(NUMBER_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(STRING_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(NUMBER_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsBoolean() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isBoolean", param);
    JSType unionType = registry.createUnionType(
        getNative(BOOLEAN_TYPE), getNative(NUMBER_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(BOOLEAN_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(NUMBER_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsNumber() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isNumber", param);
    JSType unionType = registry.createUnionType(
        getNative(NUMBER_TYPE), getNative(STRING_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(NUMBER_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(STRING_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsFunction() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isFunction", param);
    JSType unionType = registry.createUnionType(
        getNative(FUNCTION_INSTANCE_TYPE), getNative(STRING_TYPE));

    FlowScope scope = createBlindScope(param, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(FUNCTION_INSTANCE_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(STRING_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsArrayWithObjectType() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isArray", param);

    FlowScope scope = createBlindScope(param, getNative(OBJECT_TYPE));

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(ARRAY_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(getNative(OBJECT_TYPE), falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsArrayWithTopType() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isArray", param);

    FlowScope scope = createBlindScope(param, getNative(UNKNOWN_TYPE));

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    // caseTopType returns topType directly
    assertEquals(getNative(UNKNOWN_TYPE), trueScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testIsObjectWithFunctionAndObject() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isObject", param);

    // Object type should be preserved on true, removed on false
    FlowScope scopeObj = createBlindScope(param, getNative(OBJECT_TYPE));
    FlowScope trueScopeObj = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scopeObj, true);
    assertEquals(getNative(OBJECT_TYPE), trueScopeObj.getSlot("x").getType());

    FlowScope falseScopeObj = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scopeObj, false);
    assertSame(scopeObj, falseScopeObj);

    // Function type should be preserved on true, removed on false
    FlowScope scopeFn = createBlindScope(param, getNative(FUNCTION_INSTANCE_TYPE));
    FlowScope trueScopeFn = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scopeFn, true);
    assertEquals(getNative(FUNCTION_INSTANCE_TYPE), trueScopeFn.getSlot("x").getType());

    FlowScope falseScopeFn = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scopeFn, false);
    assertSame(scopeFn, falseScopeFn);
  }

  @Test(timeout = 4000)
  public void testIsObjectWithTopType() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isObject", param);

    FlowScope scope = createBlindScope(param, getNative(UNKNOWN_TYPE));

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(NO_OBJECT_TYPE), trueScope.getSlot("x").getType());
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis & Guard Branches
  // ==========================================================================

  @Test(timeout = 4000)
  public void testConditionNotCall() {
    Node nonCall = Node.newString(Token.NAME, "goog");
    FlowScope scope = createBlindScope(nonCall, null);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        nonCall, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testCallWithWrongChildCount() {
    Node param1 = Node.newString(Token.NAME, "x");
    Node param2 = Node.newString(Token.NAME, "y");
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node getProp = new Node(Token.GETPROP, goog, prop);

    // 1 child (callee only)
    Node call1 = new Node(Token.CALL, getProp.cloneTree());
    FlowScope scope1 = createBlindScope(param1, null);
    assertSame(scope1, interpreter.getPreciserScopeKnowingConditionOutcome(
        call1, scope1, true));

    // 3 children (callee + 2 params)
    Node call3 = new Node(Token.CALL, getProp.cloneTree(), param1, param2);
    FlowScope scope3 = createBlindScope(param1, null);
    assertSame(scope3, interpreter.getPreciserScopeKnowingConditionOutcome(
        call3, scope3, true));
  }

  @Test(timeout = 4000)
  public void testCalleeNotGetProp() {
    Node callee = Node.newString(Token.NAME, "isDef");
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createBlindScope(param, getNative(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testParamNotQualifiedName() {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node getProp = new Node(Token.GETPROP, goog, prop);
    Node paramLiteral = Node.newNumber(42);
    Node call = new Node(Token.CALL, getProp, paramLiteral);

    FlowScope scope = createBlindScope(paramLiteral, null);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testCalleeNotGoogNamespace() {
    Node notGoog = Node.newString(Token.NAME, "other");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node getProp = new Node(Token.GETPROP, notGoog, prop);
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, getProp, param);

    FlowScope scope = createBlindScope(param, getNative(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testCalleeLeftNotName() {
    Node numberLeft = Node.newNumber(10);
    Node prop = Node.newString(Token.STRING, "isDef");
    Node getProp = new Node(Token.GETPROP, numberLeft, prop);
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, getProp, param);

    FlowScope scope = createBlindScope(param, getNative(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testCalleeRightNotString() {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newNumber(123);
    Node getProp = new Node(Token.GETPROP, goog, prop);
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, getProp, param);

    FlowScope scope = createBlindScope(param, getNative(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testUnrecognizedGoogMethod() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("unknownMethodName", param);

    FlowScope scope = createBlindScope(param, getNative(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  @Test(timeout = 4000)
  public void testQualifiedNameParameter() {
    // a.b.c qualified name
    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.STRING, "b");
    Node getpropAB = new Node(Token.GETPROP, a, b);
    Node c = Node.newString(Token.STRING, "c");
    Node paramProp = new Node(Token.GETPROP, getpropAB, c);

    Node call = createCallNode("isString", paramProp);
    JSType unionType = registry.createUnionType(
        getNative(STRING_TYPE), getNative(NUMBER_TYPE));

    FlowScope scope = createBlindScope(paramProp, unionType);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertEquals(getNative(STRING_TYPE), trueScope.getSlot("a.b.c").getType());
  }

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 124 / Null Type)
  // ==========================================================================

  /**
   * Ground Truth Failure:
   * When param has no initial type in blindScope (paramType is null),
   * goog.isArray, goog.isFunction, and goog.isObject should refine the type to
   * ARRAY_TYPE, FUNCTION_INSTANCE_TYPE, or OBJECT_TYPE respectively.
   * On the defective code, `if (paramType != null)` prevents any restriction when
   * `paramType` is null, failing with `expected:<Array> but was:<null>`.
   */
  @Test(timeout = 4000)
  public void testGoogIsArrayOnNull() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isArray", param);

    // Initial scope has NO type for "x" (paramType is null)
    FlowScope blindScope = createBlindScope(param, null);

    FlowScope outcomeTrue = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);

    assertNotNull("Scope must not be null", outcomeTrue);
    StaticSlot<JSType> slot = outcomeTrue.getSlot("x");
    assertNotNull("Slot 'x' should be defined after goog.isArray(x) is true", slot);
    assertEquals(getNative(ARRAY_TYPE), slot.getType());
  }

  @Test(timeout = 4000)
  public void testGoogIsFunctionOnNull() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isFunction", param);

    FlowScope blindScope = createBlindScope(param, null);

    FlowScope outcomeTrue = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);

    assertNotNull("Scope must not be null", outcomeTrue);
    StaticSlot<JSType> slot = outcomeTrue.getSlot("x");
    assertNotNull("Slot 'x' should be defined after goog.isFunction(x) is true", slot);
    assertEquals(getNative(FUNCTION_INSTANCE_TYPE), slot.getType());
  }

  @Test(timeout = 4000)
  public void testGoogIsObjectOnNull() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isObject", param);

    FlowScope blindScope = createBlindScope(param, null);

    FlowScope outcomeTrue = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);

    assertNotNull("Scope must not be null", outcomeTrue);
    StaticSlot<JSType> slot = outcomeTrue.getSlot("x");
    assertNotNull("Slot 'x' should be defined after goog.isObject(x) is true", slot);
    assertEquals(getNative(OBJECT_TYPE), slot.getType());
  }

  // ==========================================================================
  // Partition D: Additional Branch Coverage for RestrictByVisitors
  // ==========================================================================

  @Test(timeout = 4000)
  public void testIsArrayWhenAlreadyArray() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isArray", param);

    FlowScope scope = createBlindScope(param, getNative(ARRAY_TYPE));

    // When outcome is false for an array type, it restricts to null -> scope unchanged
    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertSame(scope, falseScope);
  }

  @Test(timeout = 4000)
  public void testIsArrayWhenNonArraySubtype() {
    Node param = Node.newString(Token.NAME, "x");
    Node call = createCallNode("isArray", param);

    // Create an object type that is NOT a subtype of Array
    ObjectType customObj = registry.createAnonymousObjectType();
    FlowScope scope = createBlindScope(param, customObj);

    // trueOutcome visitor: arrayType.isSubtype(customObj) is false -> returns null -> returns blindScope
    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, trueScope);

    // falseOutcome visitor: customObj is not Subtype of Array -> returns customObj
    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, false);
    assertEquals(customObj, falseScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testChainingDelegation() {
    // Verify that when condition does not match, delegation passes to next interpreter in chain
    ChainableReverseAbstractInterpreter mockNext =
        new ChainableReverseAbstractInterpreter(convention, registry) {
          @Override
          public FlowScope getPreciserScopeKnowingConditionOutcome(
              Node condition, FlowScope blindScope, boolean outcome) {
            FlowScope child = blindScope.createChildFlowScope();
            child.inferSlotType("delegated", getNative(NUMBER_TYPE));
            return child;
          }
        };

    interpreter.append(mockNext);

    Node nonCall = Node.newString(Token.NAME, "goog");
    FlowScope blindScope = createBlindScope(nonCall, null);
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        nonCall, blindScope, true);

    assertNotNull(result.getSlot("delegated"));
    assertEquals(getNative(NUMBER_TYPE), result.getSlot("delegated").getType());
  }
}