/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: ClosureReverseAbstractInterpreter
 *
 * Core Decision Branches Analyzed:
 * 1. getPreciserScopeKnowingConditionOutcome:
 *    - condition.isCall() == true/false
 *    - condition.getChildCount() == 2 vs != 2
 *    - callee.isGetProp() == true/false
 *    - param.isQualifiedName() == true/false
 *    - left.isName() == true/false
 *    - "goog".equals(left.getString()) == true/false
 *    - right.isString() == true/false
 *    - restricters.get(...) != null vs null (unknown method)
 *    - outcome: true vs false for all restriction functions
 * 2. Restriction Functions:
 *    - isDef: true -> getRestrictedWithoutUndefined, false -> VOID_TYPE.getGreatestSubtype
 *    - isNull: true -> NULL_TYPE.getGreatestSubtype, false -> getRestrictedWithoutNull
 *    - isDefAndNotNull: true -> both removed, false -> NULL_VOID.getGreatestSubtype
 *    - isString, isBoolean, isNumber, isFunction: getRestrictedByTypeOfResult with outcome
 *    - isArray:
 *        - null type handling: outcome ? ARRAY_TYPE : null
 *        - true visitor: caseTopType -> topType; caseObjectType -> arrayType.isSubtype(type) ? arrayType : null
 *        - false visitor: caseObjectType -> type.isSubtype(ARRAY_TYPE) ? null : type
 *    - isObject:
 *        - null type handling: outcome ? OBJECT_TYPE : null
 *        - true visitor: caseTopType -> NO_OBJECT_TYPE; caseObjectType -> type; caseFunctionType -> type
 *        - false visitor: caseAllType -> NUMBER_STRING_BOOLEAN | NULL_VOID; caseObjectType -> null; caseFunctionType -> null
 * 3. Defect-Targeted Branch Zone (Defects4J ground truth):
 *    - testGoogIsArray2: Verifying type refinement when parameter is of OBJECT_TYPE with goog.isArray.
 *      When true, expected: ARRAY_TYPE.
 */

package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_VOID;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_STRING_BOOLEAN;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.jstype.Visitor;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClosureReverseAbstractInterpreterGeminiTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ClosureReverseAbstractInterpreter rai;

  // Mock FlowScope implementation for deterministic test execution
  private static class SimpleFlowScope implements FlowScope {
    private final FlowScope parent;
    private final String slotName;
    private final JSType slotType;

    SimpleFlowScope() {
      this(null, null, null);
    }

    SimpleFlowScope(FlowScope parent, String slotName, JSType slotType) {
      this.parent = parent;
      this.slotName = slotName;
      this.slotType = slotType;
    }

    @Override
    public FlowScope createChildFlowScope() {
      return new SimpleFlowScope(this, null, null);
    }

    @Override
    public FlowScope optimize() {
      return this;
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      if (slotName != null && slotName.equals(name)) {
        return new SimpleSlot(name, slotType);
      }
      return parent != null ? parent.getSlot(name) : null;
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      if (slotName != null && slotName.equals(name)) {
        return new SimpleSlot(name, slotType);
      }
      return null;
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public FlowScope getParentScope() {
      return parent;
    }

    @Override
    public void inferSlotType(String name, JSType type) {}

    @Override
    public void inferQualifiedSlot(Node node, String qName, JSType blindType, JSType refinedType) {}

    @Override
    public FlowScope findBestFlowScope(StaticSlot<JSType> slot) {
      return this;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof SimpleFlowScope;
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  private static class SimpleSlot implements StaticSlot<JSType> {
    private final String name;
    private final JSType type;

    SimpleSlot(String name, JSType type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public JSType getType() {
      return type;
    }

    @Override
    public boolean isTypeInferred() {
      return false;
    }

    @Override
    public Node getDeclaration() {
      return null;
    }

    @Override
    public JSType getJSType() {
      return type;
    }
  }

  private static class DummyErrorReporter implements ErrorReporter {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {}

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {}
  }

  @Before
  public void setUp() {
    this.registry = new JSTypeRegistry(new DummyErrorReporter());
    this.convention = new GoogleCodingConvention();
    this.rai = new ClosureReverseAbstractInterpreter(convention, registry);
  }

  private JSType getNative(JSTypeNative type) {
    return registry.getNativeType(type);
  }

  private Node createCall(String namespace, String method, String paramName) {
    Node callee = IR.getprop(IR.name(namespace), IR.string(method));
    Node param = IR.name(paramName);
    return IR.call(callee, param);
  }

  private JSType refine(String method, JSType inputType, boolean outcome) {
    Node callNode = createCall("goog", method, "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", inputType);
    FlowScope resultScope = rai.getPreciserScopeKnowingConditionOutcome(callNode, blindScope, outcome);
    if (resultScope == blindScope) {
      return null; // No refinement made or returned blindScope directly
    }
    StaticSlot<JSType> slot = resultScope.getSlot("x");
    return slot != null ? slot.getType() : null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions (All Restricters)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsDefRefinement() {
    JSType union = registry.createUnionType(getNative(STRING_TYPE), getNative(VOID_TYPE));
    JSType trueResult = refine("isDef", union, true);
    assertEquals(getNative(STRING_TYPE), trueResult);

    JSType falseResult = refine("isDef", union, false);
    assertEquals(getNative(VOID_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsNullRefinement() {
    JSType union = registry.createUnionType(getNative(NUMBER_TYPE), getNative(NULL_TYPE));
    JSType trueResult = refine("isNull", union, true);
    assertEquals(getNative(NULL_TYPE), trueResult);

    JSType falseResult = refine("isNull", union, false);
    assertEquals(getNative(NUMBER_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsDefAndNotNullRefinement() {
    JSType union = registry.createUnionType(
        getNative(STRING_TYPE), getNative(NULL_TYPE), getNative(VOID_TYPE));
    JSType trueResult = refine("isDefAndNotNull", union, true);
    assertEquals(getNative(STRING_TYPE), trueResult);

    JSType falseResult = refine("isDefAndNotNull", union, false);
    assertEquals(getNative(NULL_VOID), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsStringRefinement() {
    JSType union = registry.createUnionType(getNative(STRING_TYPE), getNative(NUMBER_TYPE));
    JSType trueResult = refine("isString", union, true);
    assertEquals(getNative(STRING_TYPE), trueResult);

    JSType falseResult = refine("isString", union, false);
    assertEquals(getNative(NUMBER_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsBooleanRefinement() {
    JSType union = registry.createUnionType(getNative(BOOLEAN_TYPE), getNative(NUMBER_TYPE));
    JSType trueResult = refine("isBoolean", union, true);
    assertEquals(getNative(BOOLEAN_TYPE), trueResult);

    JSType falseResult = refine("isBoolean", union, false);
    assertEquals(getNative(NUMBER_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsNumberRefinement() {
    JSType union = registry.createUnionType(getNative(NUMBER_TYPE), getNative(STRING_TYPE));
    JSType trueResult = refine("isNumber", union, true);
    assertEquals(getNative(NUMBER_TYPE), trueResult);

    JSType falseResult = refine("isNumber", union, false);
    assertEquals(getNative(STRING_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsFunctionRefinement() {
    FunctionType funcType = registry.createFunctionType(getNative(STRING_TYPE));
    JSType union = registry.createUnionType(funcType, getNative(NUMBER_TYPE));
    JSType trueResult = refine("isFunction", union, true);
    assertEquals(funcType, trueResult);

    JSType falseResult = refine("isFunction", union, false);
    assertEquals(getNative(NUMBER_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsObjectRefinement() {
    ObjectType objectType = (ObjectType) getNative(OBJECT_TYPE);
    JSType union = registry.createUnionType(objectType, getNative(NUMBER_TYPE));

    JSType trueResult = refine("isObject", union, true);
    assertEquals(objectType, trueResult);

    JSType falseResult = refine("isObject", union, false);
    assertEquals(getNative(NUMBER_TYPE), falseResult);
  }

  @Test(timeout = 4000)
  public void testIsObjectOnFunctionType() {
    FunctionType funcType = registry.createFunctionType(getNative(STRING_TYPE));
    JSType trueResult = refine("isObject", funcType, true);
    assertEquals(funcType, trueResult);

    JSType falseResult = refine("isObject", funcType, false);
    assertNull(falseResult); // Should restrict to null/empty
  }

  @Test(timeout = 4000)
  public void testIsObjectOnAllType() {
    JSType allType = getNative(ALL_TYPE);
    JSType falseResult = refine("isObject", allType, false);
    JSType expected = registry.createUnionType(
        getNative(NUMBER_STRING_BOOLEAN), getNative(NULL_VOID));
    assertEquals(expected, falseResult);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullInputTypeForIsArray() {
    Node call = createCall("goog", "isArray", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", null);

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertEquals(getNative(ARRAY_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, false);
    assertSame(blindScope, falseScope);
  }

  @Test(timeout = 4000)
  public void testNullInputTypeForIsObject() {
    Node call = createCall("goog", "isObject", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", null);

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertEquals(getNative(OBJECT_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, false);
    assertSame(blindScope, falseScope);
  }

  @Test(timeout = 4000)
  public void testNullInputTypeForIsDefAndIsNull() {
    Node callDef = createCall("goog", "isDef", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", null);
    FlowScope resDefFalse = rai.getPreciserScopeKnowingConditionOutcome(callDef, blindScope, false);
    assertSame(blindScope, resDefFalse);

    Node callNull = createCall("goog", "isNull", "x");
    FlowScope resNullTrue = rai.getPreciserScopeKnowingConditionOutcome(callNull, blindScope, true);
    assertSame(blindScope, resNullTrue);

    Node callDefNull = createCall("goog", "isDefAndNotNull", "x");
    FlowScope resDefNullFalse = rai.getPreciserScopeKnowingConditionOutcome(callDefNull, blindScope, false);
    assertSame(blindScope, resDefNullFalse);
  }

  @Test(timeout = 4000)
  public void testIsArrayWithNonArrayObjectType() {
    ObjectType objType = (ObjectType) getNative(OBJECT_TYPE);
    ObjectType arrayType = (ObjectType) getNative(ARRAY_TYPE);

    // false visitor on an Array type should reduce to null
    JSType falseResultOnArray = refine("isArray", arrayType, false);
    assertNull(falseResultOnArray);

    // false visitor on non-array object type that does not subtype array should retain the type
    FunctionType funcType = registry.createFunctionType(getNative(NUMBER_TYPE));
    JSType falseResultOnFunc = refine("isArray", funcType, false);
    assertEquals(funcType, falseResultOnFunc);
  }

  @Test(timeout = 4000)
  public void testUnknownTypeHandling() {
    JSType unknown = getNative(UNKNOWN_TYPE);
    JSType trueArray = refine("isArray", unknown, true);
    assertEquals(unknown, trueArray);

    JSType trueObject = refine("isObject", unknown, true);
    assertEquals(getNative(NO_OBJECT_TYPE), trueObject);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug: testGoogIsArray2)
  // =========================================================================

  /**
   * Targets defects4j defect: testGoogIsArray2
   * Verifies that when goog.isArray(param) evaluates to true on a param of type OBJECT_TYPE,
   * the refined type is strictly ARRAY_TYPE.
   */
  @Test(timeout = 4000)
  public void testDefects4JGoogIsArrayOnObjectType() {
    JSType objectType = getNative(OBJECT_TYPE);
    JSType refinedType = refine("isArray", objectType, true);
    assertNotNull("Refined type for goog.isArray(OBJECT_TYPE) must not be null", refinedType);
    assertEquals("Refinement of OBJECT_TYPE under goog.isArray must be Array",
        getNative(ARRAY_TYPE), refinedType);
  }

  @Test(timeout = 4000)
  public void testDefects4JGoogIsArrayOnAllType() {
    JSType allType = getNative(ALL_TYPE);
    JSType refinedType = refine("isArray", allType, true);
    assertNotNull(refinedType);
    assertEquals(allType, refinedType);
  }

  // =========================================================================
  // Partition D: Decision/Condition Short-Circuiting & Defensive Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testConditionNotCall() {
    Node notCall = IR.name("x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope result = rai.getPreciserScopeKnowingConditionOutcome(notCall, blindScope, true);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testConditionCallWrongArgCount() {
    Node callee = IR.getprop(IR.name("goog"), IR.string("isDef"));
    Node call0 = IR.call(callee);
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res0 = rai.getPreciserScopeKnowingConditionOutcome(call0, blindScope, true);
    assertSame(blindScope, res0);

    Node call3 = IR.call(callee.cloneTree(), IR.name("a"), IR.name("b"));
    FlowScope res3 = rai.getPreciserScopeKnowingConditionOutcome(call3, blindScope, true);
    assertSame(blindScope, res3);
  }

  @Test(timeout = 4000)
  public void testConditionCalleeNotGetProp() {
    Node callee = IR.name("isDef");
    Node call = IR.call(callee, IR.name("x"));
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertSame(blindScope, res);
  }

  @Test(timeout = 4000)
  public void testConditionParamNotQualifiedName() {
    Node callee = IR.getprop(IR.name("goog"), IR.string("isDef"));
    Node call = IR.call(callee, IR.number(42));
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertSame(blindScope, res);
  }

  @Test(timeout = 4000)
  public void testConditionNotGoogNamespace() {
    Node call = createCall("customNamespace", "isDef", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertSame(blindScope, res);
  }

  @Test(timeout = 4000)
  public void testConditionUnknownGoogFunction() {
    Node call = createCall("goog", "someUnknownFunction", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertSame(blindScope, res);
  }

  @Test(timeout = 4000)
  public void testConditionLeftNotNameNode() {
    Node getprop = IR.getprop(IR.number(123), IR.string("isDef"));
    Node call = IR.call(getprop, IR.name("x"));
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope res = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertSame(blindScope, res);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Chaining Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testChainingDelegation() {
    ReverseAbstractInterpreter dummyNext = new SemanticReverseAbstractInterpreter(convention, registry);
    rai.append(dummyNext);
    assertSame(dummyNext, rai.getFirst().getNext());

    // Call unknown method to trigger chain delegation
    Node call = createCall("goog", "nonExistentMethod", "x");
    FlowScope blindScope = new SimpleFlowScope(null, "x", getNative(NUMBER_TYPE));
    FlowScope outcomeScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);
    assertNotNull(outcomeScope);
  }

  @Test(timeout = 4000)
  public void testTypeRefinementProducesNewChildScopeWhenTypeMatches() {
    Node call = createCall("goog", "isString", "x");
    SimpleFlowScope blindScope = new SimpleFlowScope(null, "x", getNative(STRING_TYPE));
    FlowScope refinedScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);

    assertNotSame(blindScope, refinedScope);
    assertSame(blindScope, refinedScope.getParentScope());
    assertEquals(getNative(STRING_TYPE), refinedScope.getSlot("x").getType());
  }

  @Test(timeout = 4000)
  public void testTypeRefinementReturnsBlindScopeWhenRestrictedToNull() {
    // If testing isString on a NUMBER_TYPE with outcome true, result type is null/empty
    Node call = createCall("goog", "isString", "x");
    SimpleFlowScope blindScope = new SimpleFlowScope(null, "x", getNative(NUMBER_TYPE));
    FlowScope refinedScope = rai.getPreciserScopeKnowingConditionOutcome(call, blindScope, true);

    // Since restricted type is null, it should return blindScope directly
    assertSame(blindScope, refinedScope);
  }
}