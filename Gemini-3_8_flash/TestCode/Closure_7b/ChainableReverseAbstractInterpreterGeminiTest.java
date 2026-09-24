package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.UnionType;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J ground truth):
 *    - In RestrictByOneTypeOfResultVisitor.caseObjectType:
 *      When value.equals("function") and resultEqualsValue is false (e.g. typeof x != 'function'),
 *      the implementation improperly computes:
 *          resultEqualsValue && ctorType.isSubtype(type) ? ctorType : null;
 *      When resultEqualsValue is false, this always evaluates to null for any ObjectType,
 *      wrongfully discarding non-function ObjectTypes (e.g., standard Object) from unions.
 *      The correct behavior should retain Object types that are NOT subtypes of function.
 *
 * 2. CHAIN LINKING & DELEGATION:
 *    - append(): validates precondition that lastLink.nextLink == null; updates firstLink and nextLink.
 *    - getFirst(): returns head of chain.
 *    - firstPreciserScopeKnowingConditionOutcome(): forwards to firstLink.
 *    - nextPreciserScopeKnowingConditionOutcome(): forwards to nextLink if non-null, else returns blindScope.
 *
 * 3. REFINABILITY & DECLARATION:
 *    - getTypeIfRefinable:
 *      - Token.NAME: slot exists (type from slot or fallback to node.getJSType()), slot missing (returns null).
 *      - Token.GETPROP: qualifiedName null vs non-null, slot exists vs missing, fallback to node.getJSType(),
 *        fallback to UNKNOWN_TYPE.
 *      - other token types: returns null.
 *    - declareNameInScope:
 *      - Token.NAME: delegates to inferSlotType.
 *      - Token.GETPROP: delegates to inferQualifiedSlot, handles null node JSType fallback to UNKNOWN_TYPE.
 *      - Token.THIS: ignored (no-op).
 *      - default: throws IllegalArgumentException.
 *
 * 4. RESTRICTION VISITORS:
 *    - getRestrictedWithoutUndefined:
 *      - null input, void type, all type, union type, enum element type, primitives, functions, objects.
 *    - getRestrictedWithoutNull:
 *      - null input, null type, all type, union type, enum element type, primitives, functions, objects.
 *    - getRestrictedByTypeOfResult:
 *      - type == null with resultEqualsValue true (resolves native type or CHECKED_UNKNOWN) vs false (null).
 *      - typeof on number, boolean, string, undefined, function, object, unknown, union, enum element.
 */
public class ChainableReverseAbstractInterpreterGeminiTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private TestInterpreter interpreter;

  private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
    int invokeCount = 0;
    Node lastCondition = null;
    FlowScope lastScope = null;
    Boolean lastOutcome = null;

    TestInterpreter(CodingConvention convention, JSTypeRegistry registry) {
      super(convention, registry);
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(
        Node condition, FlowScope blindScope, boolean outcome) {
      invokeCount++;
      lastCondition = condition;
      lastScope = blindScope;
      lastOutcome = outcome;
      return blindScope;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    convention = new DefaultCodingConvention();
    interpreter = new TestInterpreter(convention, registry);
  }

  @SuppressWarnings("unchecked")
  private FlowScope createMockFlowScope(
      final Map<String, StaticSlot<JSType>> slots,
      final Map<String, JSType> inferredSlots,
      final Map<String, JSType> inferredQualifiedSlots) {
    return (FlowScope) Proxy.newProxyInstance(
        FlowScope.class.getClassLoader(),
        new Class<?>[] { FlowScope.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (name.equals("getSlot") && args != null && args.length == 1) {
              return slots.get(args[0]);
            } else if (name.equals("inferSlotType") && args != null && args.length == 2) {
              if (inferredSlots != null) {
                inferredSlots.put((String) args[0], (JSType) args[1]);
              }
              return null;
            } else if (name.equals("inferQualifiedSlot") && args != null && args.length == 4) {
              if (inferredQualifiedSlots != null) {
                inferredQualifiedSlots.put((String) args[1], (JSType) args[3]);
              }
              return null;
            } else if (name.equals("equals")) {
              return proxy == args[0];
            } else if (name.equals("hashCode")) {
              return System.identityHashCode(proxy);
            }
            return null;
          }
        });
  }

  @SuppressWarnings("unchecked")
  private StaticSlot<JSType> createMockSlot(final String name, final JSType type) {
    return (StaticSlot<JSType>) Proxy.newProxyInstance(
        StaticSlot.class.getClassLoader(),
        new Class<?>[] { StaticSlot.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getType")) {
              return type;
            } else if (method.getName().equals("getName")) {
              return name;
            }
            return null;
          }
        });
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where `typeof x != 'function'` on an ObjectType returns null
   * instead of preserving the ObjectType. When x is Object (not a subtype of Function),
   * restricting by NOT equal to 'function' must preserve the Object type.
   */
  @Test(timeout = 4000)
  public void testDefectTypeofNotFunctionPreservesObjectType() {
    ObjectType objectType = registry.getNativeObjectType(OBJECT_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(objectType, "function", false);
    assertNotNull("Restricting Object by typeof != 'function' must not return null", result);
    assertEquals("Restricting Object by typeof != 'function' must retain Object", objectType, result);
  }

  /**
   * Targets the union restriction failure when restricting (Object|number) by != 'function'.
   */
  @Test(timeout = 4000)
  public void testDefectTypeofNotFunctionOnUnionContainingObject() {
    ObjectType objectType = registry.getNativeObjectType(OBJECT_TYPE);
    JSType numberType = interpreter.getNativeType(NUMBER_TYPE);
    UnionType union = (UnionType) registry.createUnionType(objectType, numberType);

    JSType restricted = interpreter.getRestrictedByTypeOfResult(union, "function", false);
    assertNotNull("Restricting Object|number by typeof != 'function' must not be null", restricted);
    assertTrue("Restricted union must contain ObjectType", restricted.isSubtype(registry.createUnionType(objectType, numberType)));
    assertTrue("Object must be a subtype of restricted union", objectType.isSubtype(restricted));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInterpreterChainLinkage() {
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter third = new TestInterpreter(convention, registry);

    assertSame(first, first.getFirst());
    ChainableReverseAbstractInterpreter last = first.append(second).append(third);

    assertSame(third, last);
    assertSame(first, first.getFirst());
    assertSame(first, second.getFirst());
    assertSame(first, third.getFirst());
  }

  @Test(timeout = 4000)
  public void testScopeDelegationAlongChain() {
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry);
    first.append(second);

    Node cond = Node.newString("a");
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope scope = createMockFlowScope(slots, null, null);

    FlowScope resFirst = second.firstPreciserScopeKnowingConditionOutcome(cond, scope, true);
    assertSame(scope, resFirst);
    assertEquals(1, first.invokeCount);
    assertSame(cond, first.lastCondition);
    assertEquals(Boolean.TRUE, first.lastOutcome);

    FlowScope resNext = first.nextPreciserScopeKnowingConditionOutcome(cond, scope, false);
    assertSame(scope, resNext);
    assertEquals(1, second.invokeCount);
    assertSame(cond, second.lastCondition);
    assertEquals(Boolean.FALSE, second.lastOutcome);

    // Terminal link delegation returns blindScope directly
    FlowScope terminalRes = second.nextPreciserScopeKnowingConditionOutcome(cond, scope, true);
    assertSame(scope, terminalRes);
    assertEquals(1, second.invokeCount); // Count should not increment
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinableNameToken() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    JSType numType = interpreter.getNativeType(NUMBER_TYPE);
    slots.put("x", createMockSlot("x", numType));
    FlowScope scope = createMockFlowScope(slots, null, null);

    Node nameNode = Node.newString(Token.NAME, "x");
    assertEquals(numType, interpreter.getTypeIfRefinable(nameNode, scope));

    Node unknownNameNode = Node.newString(Token.NAME, "y");
    assertNull(interpreter.getTypeIfRefinable(unknownNameNode, scope));

    // Name node with slot returning null type falls back to node.getJSType()
    slots.put("z", createMockSlot("z", null));
    Node zNode = Node.newString(Token.NAME, "z");
    zNode.setJSType(interpreter.getNativeType(STRING_TYPE));
    assertEquals(interpreter.getNativeType(STRING_TYPE), interpreter.getTypeIfRefinable(zNode, scope));
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinableGetPropToken() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    JSType boolType = interpreter.getNativeType(BOOLEAN_TYPE);
    slots.put("a.b", createMockSlot("a.b", boolType));
    FlowScope scope = createMockFlowScope(slots, null, null);

    Node target = Node.newString(Token.NAME, "a");
    Node prop = Node.newString(Token.STRING, "b");
    Node getPropNode = new Node(Token.GETPROP, target, prop);

    assertEquals(boolType, interpreter.getTypeIfRefinable(getPropNode, scope));

    // Unregistered qualified property falls back to node type
    Node cProp = Node.newString(Token.STRING, "c");
    Node getPropNode2 = new Node(Token.GETPROP, target, cProp);
    getPropNode2.setJSType(interpreter.getNativeType(NUMBER_TYPE));
    assertEquals(interpreter.getNativeType(NUMBER_TYPE), interpreter.getTypeIfRefinable(getPropNode2, scope));

    // When node type is also null, falls back to UNKNOWN_TYPE
    Node dProp = Node.newString(Token.STRING, "d");
    Node getPropNode3 = new Node(Token.GETPROP, target, dProp);
    assertEquals(interpreter.getNativeType(UNKNOWN_TYPE), interpreter.getTypeIfRefinable(getPropNode3, scope));

    // Non-qualified getprop returns null
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    Node dynamicGetProp = new Node(Token.GETPROP, callTarget, Node.newString(Token.STRING, "p"));
    assertNull(interpreter.getTypeIfRefinable(dynamicGetProp, scope));

    // Other node types return null
    Node numberNode = Node.newNumber(42);
    assertNull(interpreter.getTypeIfRefinable(numberNode, scope));
  }

  @Test(timeout = 4000)
  public void testDeclareNameInScope() {
    Map<String, JSType> inferredSlots = new HashMap<String, JSType>();
    Map<String, JSType> inferredQualifiedSlots = new HashMap<String, JSType>();
    FlowScope scope = createMockFlowScope(
        new HashMap<String, StaticSlot<JSType>>(),
        inferredSlots,
        inferredQualifiedSlots);

    Node nameNode = Node.newString(Token.NAME, "varName");
    JSType strType = interpreter.getNativeType(STRING_TYPE);
    interpreter.declareNameInScope(scope, nameNode, strType);
    assertEquals(strType, inferredSlots.get("varName"));

    Node target = Node.newString(Token.NAME, "foo");
    Node prop = Node.newString(Token.STRING, "bar");
    Node getPropNode = new Node(Token.GETPROP, target, prop);
    JSType numType = interpreter.getNativeType(NUMBER_TYPE);
    interpreter.declareNameInScope(scope, getPropNode, numType);
    assertEquals(numType, inferredQualifiedSlots.get("foo.bar"));

    // THIS node should be ignored without exception
    Node thisNode = new Node(Token.THIS);
    interpreter.declareNameInScope(scope, thisNode, numType);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Restriction Visitors
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetRestrictedWithoutUndefined() {
    assertNull(interpreter.getRestrictedWithoutUndefined(null));
    assertNull(interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(VOID_TYPE)));

    assertEquals(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(NUMBER_TYPE)));
    assertEquals(interpreter.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(STRING_TYPE)));
    assertEquals(interpreter.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(BOOLEAN_TYPE)));
    assertEquals(interpreter.getNativeType(NULL_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(NULL_TYPE)));
    assertEquals(interpreter.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(NO_TYPE)));
    assertEquals(interpreter.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(NO_OBJECT_TYPE)));
    assertEquals(interpreter.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutUndefined(interpreter.getNativeType(UNKNOWN_TYPE)));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    assertEquals(objType, interpreter.getRestrictedWithoutUndefined(objType));

    // Union type containing undefined
    JSType union = registry.createUnionType(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getNativeType(VOID_TYPE));
    assertEquals(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(union));

    // All type
    JSType allType = interpreter.getNativeType(ALL_TYPE);
    JSType restrictedAll = interpreter.getRestrictedWithoutUndefined(allType);
    assertNotNull(restrictedAll);
    assertFalse(restrictedAll.isSubtype(interpreter.getNativeType(VOID_TYPE)));

    // Enum element type
    EnumType enumType = registry.createEnumType("MyEnum", null, interpreter.getNativeType(NUMBER_TYPE));
    assertEquals(enumType.getElementsType(),
        interpreter.getRestrictedWithoutUndefined(enumType.getElementsType()));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedWithoutNull() {
    assertNull(interpreter.getRestrictedWithoutNull(null));
    assertNull(interpreter.getRestrictedWithoutNull(interpreter.getNativeType(NULL_TYPE)));

    assertEquals(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(NUMBER_TYPE)));
    assertEquals(interpreter.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(STRING_TYPE)));
    assertEquals(interpreter.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(BOOLEAN_TYPE)));
    assertEquals(interpreter.getNativeType(VOID_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(VOID_TYPE)));
    assertEquals(interpreter.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(NO_TYPE)));
    assertEquals(interpreter.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(NO_OBJECT_TYPE)));
    assertEquals(interpreter.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutNull(interpreter.getNativeType(UNKNOWN_TYPE)));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    assertEquals(objType, interpreter.getRestrictedWithoutNull(objType));

    // Union containing null
    JSType union = registry.createUnionType(interpreter.getNativeType(STRING_TYPE),
        interpreter.getNativeType(NULL_TYPE));
    assertEquals(interpreter.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(union));

    // All type
    JSType allType = interpreter.getNativeType(ALL_TYPE);
    JSType restrictedAll = interpreter.getRestrictedWithoutNull(allType);
    assertNotNull(restrictedAll);
    assertFalse(restrictedAll.isSubtype(interpreter.getNativeType(NULL_TYPE)));

    // Enum element type
    EnumType enumType = registry.createEnumType("MyEnum2", null, interpreter.getNativeType(STRING_TYPE));
    assertEquals(enumType.getElementsType(),
        interpreter.getRestrictedWithoutNull(enumType.getElementsType()));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfResultNullType() {
    // When input type is null and resultEqualsValue is true
    assertEquals(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "number", true));
    assertEquals(interpreter.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "boolean", true));
    assertEquals(interpreter.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "string", true));
    assertEquals(interpreter.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "undefined", true));
    assertEquals(interpreter.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "function", true));
    // Arbitrary/unmatched typeof string returns CHECKED_UNKNOWN_TYPE
    assertEquals(interpreter.getNativeType(CHECKED_UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "some_unknown_type", true));

    // When input type is null and resultEqualsValue is false
    assertNull(interpreter.getRestrictedByTypeOfResult(null, "number", false));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfPrimitives() {
    JSType num = interpreter.getNativeType(NUMBER_TYPE);
    assertEquals(num, interpreter.getRestrictedByTypeOfResult(num, "number", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(num, "number", false));
    assertNull(interpreter.getRestrictedByTypeOfResult(num, "string", true));
    assertEquals(num, interpreter.getRestrictedByTypeOfResult(num, "string", false));

    JSType bool = interpreter.getNativeType(BOOLEAN_TYPE);
    assertEquals(bool, interpreter.getRestrictedByTypeOfResult(bool, "boolean", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(bool, "boolean", false));

    JSType str = interpreter.getNativeType(STRING_TYPE);
    assertEquals(str, interpreter.getRestrictedByTypeOfResult(str, "string", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(str, "string", false));

    JSType voidType = interpreter.getNativeType(VOID_TYPE);
    assertEquals(voidType, interpreter.getRestrictedByTypeOfResult(voidType, "undefined", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(voidType, "undefined", false));

    JSType nullType = interpreter.getNativeType(NULL_TYPE);
    // typeof null is "object"
    assertEquals(nullType, interpreter.getRestrictedByTypeOfResult(nullType, "object", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(nullType, "object", false));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfFunctionAndNoObject() {
    JSType fnType = interpreter.getNativeType(U2U_CONSTRUCTOR_TYPE);
    assertEquals(fnType, interpreter.getRestrictedByTypeOfResult(fnType, "function", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(fnType, "function", false));

    JSType noObjType = interpreter.getNativeType(NO_OBJECT_TYPE);
    assertEquals(noObjType, interpreter.getRestrictedByTypeOfResult(noObjType, "object", true));
    assertEquals(noObjType, interpreter.getRestrictedByTypeOfResult(noObjType, "function", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(noObjType, "object", false));
    assertNull(interpreter.getRestrictedByTypeOfResult(noObjType, "number", true));

    JSType noType = interpreter.getNativeType(NO_TYPE);
    assertEquals(noType, interpreter.getRestrictedByTypeOfResult(noType, "number", true));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfTopTypes() {
    JSType allType = interpreter.getNativeType(ALL_TYPE);
    assertEquals(interpreter.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(allType, "number", true));
    assertEquals(interpreter.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(allType, "boolean", true));

    JSType unknownType = interpreter.getNativeType(UNKNOWN_TYPE);
    assertEquals(interpreter.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(unknownType, "string", true));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfEnumElement() {
    EnumType enumType = registry.createEnumType("TestEnum", null, interpreter.getNativeType(NUMBER_TYPE));
    JSType elemType = enumType.getElementsType();

    assertEquals(elemType, interpreter.getRestrictedByTypeOfResult(elemType, "number", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(elemType, "string", true));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullConventionThrows() {
    new TestInterpreter(null, registry);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testAppendLinkWithExistingNextThrows() {
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter third = new TestInterpreter(convention, registry);

    second.append(third);
    // second already has nextLink != null, cannot be appended
    first.append(second);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testDeclareNameInScopeInvalidNodeThrows() {
    FlowScope scope = createMockFlowScope(new HashMap<String, StaticSlot<JSType>>(), null, null);
    Node invalidNode = Node.newNumber(123);
    interpreter.declareNameInScope(scope, invalidNode, interpreter.getNativeType(NUMBER_TYPE));
  }
}