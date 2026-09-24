package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.DATE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumElementType;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.ParameterizedType;
import com.google.javascript.rhino.jstype.StaticReference;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.TemplateType;
import com.google.javascript.rhino.jstype.UnionType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: ChainableReverseAbstractInterpreter.java
 *
 * Partition A: Core Functional Logic & State Transitions
 * - append: chaining link1 -> link2 -> link3, ensuring firstLink and nextLink consistency.
 * - getFirst: retrieving head interpreter from arbitrary links in the chain.
 * - firstPreciserScopeKnowingConditionOutcome / nextPreciserScopeKnowingConditionOutcome.
 * - getTypeIfRefinable: Token.NAME with slot hit (type != null and type == null fallback to node type).
 * - getTypeIfRefinable: Token.GETPROP with qualified name (slot hit, node type fallback, UNKNOWN fallback).
 * - declareNameInScope: Token.NAME slot inference, Token.GETPROP qualified slot inference.
 * - getRestrictedWithoutUndefined: All JSType visitor branches (all, no_object, no_type, boolean,
 *   function, null, number, object, string, union, unknown, void, enum, parameterized, template).
 * - getRestrictedWithoutNull: All JSType visitor branches (all, no_object, no_type, boolean,
 *   function, null, number, object, string, union, unknown, void, enum, parameterized, template).
 * - getRestrictedByTypeOfResult: All combinations of resultEqualsValue (true/false) and typeOf
 *   results ("number", "boolean", "string", "undefined", "function", "object", unrecognized).
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - null types passed into getRestrictedWithoutUndefined and getRestrictedWithoutNull (returns null).
 * - null type in getRestrictedByTypeOfResult: equality check returns matched native or UNKNOWN, inequality returns null.
 * - GETPROP nodes with null qualified names (e.g., expression call target `foo()[bar]`).
 * - getNativeTypeForTypeOf with undefined string constants.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Defect: TypeInferenceTest::testNoThisInference -> IllegalArgumentException: Node cannot be refined.
 * - Root Cause: ChainableReverseAbstractInterpreter#declareNameInScope lacks `case Token.THIS:`,
 *   causing Token.THIS nodes encountered during refinement to hit `default:` and throw an exception.
 * - Target Test: testDeclareNameInScope_withThisNode_defectFix asserts Token.THIS is handled gracefully.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - ChainableReverseAbstractInterpreter constructor with null CodingConvention throws NullPointerException.
 * - append() with interpreter that already has a nextLink throws IllegalArgumentException.
 * - declareNameInScope with unrefinable node type (e.g., Token.NUMBER) throws IllegalArgumentException.
 * - declareNameInScope with GETPROP having null qualifiedName throws NullPointerException.
 *
 * Partition E: Inner Class & Visitor Subclass Coverage
 * - Direct instantiation and method verification of RestrictByTrueTypeOfResultVisitor,
 *   RestrictByFalseTypeOfResultVisitor, and RestrictByTypeOfResultVisitor.
 * -----------------------------------------------------------------------------------------
 */
public class ChainableReverseAbstractInterpreterGeminiTest {

  private CodingConvention convention;
  private JSTypeRegistry registry;
  private ConcreteInterpreter interpreter;

  private static class ConcreteInterpreter extends ChainableReverseAbstractInterpreter {
    private FlowScope preciserScopeResult;

    ConcreteInterpreter(CodingConvention convention, JSTypeRegistry registry) {
      super(convention, registry);
    }

    void setPreciserScopeResult(FlowScope scope) {
      this.preciserScopeResult = scope;
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(
        Node condition, FlowScope blindScope, boolean outcome) {
      return preciserScopeResult != null ? preciserScopeResult : blindScope;
    }
  }

  private static class TestTrueVisitor
      extends ChainableReverseAbstractInterpreter.RestrictByTrueTypeOfResultVisitor {
    TestTrueVisitor(ChainableReverseAbstractInterpreter interpreter) {
      interpreter.super();
    }

    @Override
    protected JSType caseTopType(JSType topType) {
      return topType;
    }
  }

  private static class TestFalseVisitor
      extends ChainableReverseAbstractInterpreter.RestrictByFalseTypeOfResultVisitor {
    TestFalseVisitor(ChainableReverseAbstractInterpreter interpreter) {
      interpreter.super();
    }
  }

  @Before
  public void setUp() {
    convention = new DefaultCodingConvention();
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    interpreter = new ConcreteInterpreter(convention, registry);
  }

  private StaticSlot<JSType> createSlot(final String name, final JSType type) {
    return new StaticSlot<JSType>() {
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
      public StaticReference<JSType> getDeclaration() {
        return null;
      }
    };
  }

  private FlowScope createFlowScope(
      final Map<String, StaticSlot<JSType>> slots,
      final Map<String, JSType> inferredSlots,
      final Map<String, JSType> inferredQualifiedSlots) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("getSlot".equals(methodName)) {
          return slots != null ? slots.get(args[0]) : null;
        } else if ("inferSlotType".equals(methodName)) {
          if (inferredSlots != null) {
            inferredSlots.put((String) args[0], (JSType) args[1]);
          }
          return null;
        } else if ("inferQualifiedSlot".equals(methodName)) {
          if (inferredQualifiedSlots != null) {
            inferredQualifiedSlots.put((String) args[1], (JSType) args[3]);
          }
          return null;
        } else if ("createChildFlowScope".equals(methodName) || "optimize".equals(methodName)) {
          return proxy;
        } else if ("equals".equals(methodName)) {
          return proxy == args[0];
        } else if ("hashCode".equals(methodName)) {
          return Integer.valueOf(1);
        } else if ("toString".equals(methodName)) {
          return "TestFlowScopeProxy";
        }
        return null;
      }
    };
    return (FlowScope) Proxy.newProxyInstance(
        FlowScope.class.getClassLoader(),
        new Class<?>[] { FlowScope.class },
        handler);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAppendAndGetFirst() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);

    assertSame(link1, link1.getFirst());

    ChainableReverseAbstractInterpreter returned2 = link1.append(link2);
    assertSame(link2, returned2);
    assertSame(link1, link1.getFirst());
    assertSame(link1, link2.getFirst());

    ChainableReverseAbstractInterpreter returned3 = link2.append(link3);
    assertSame(link3, returned3);
    assertSame(link1, link3.getFirst());
  }

  @Test(timeout = 4000)
  public void testFirstAndNextPreciserScope() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    link1.append(link2);

    FlowScope scopeBlind = createFlowScope(null, null, null);
    FlowScope scope1 = createFlowScope(null, null, null);
    FlowScope scope2 = createFlowScope(null, null, null);

    link1.setPreciserScopeResult(scope1);
    link2.setPreciserScopeResult(scope2);

    Node condNode = new Node(Token.TRUE);

    FlowScope resFromLink2First = link2.firstPreciserScopeKnowingConditionOutcome(condNode, scopeBlind, true);
    assertSame(scope1, resFromLink2First);

    FlowScope resFromLink1Next = link1.nextPreciserScopeKnowingConditionOutcome(condNode, scopeBlind, true);
    assertSame(scope2, resFromLink1Next);

    FlowScope resFromLink2Next = link2.nextPreciserScopeKnowingConditionOutcome(condNode, scopeBlind, true);
    assertSame(scopeBlind, resFromLink2Next);
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinable_NameNode() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    slots.put("a", createSlot("a", registry.getNativeType(NUMBER_TYPE)));
    slots.put("b", createSlot("b", null));
    FlowScope scope = createFlowScope(slots, null, null);

    Node nodeA = Node.newString(Token.NAME, "a");
    assertEquals(registry.getNativeType(NUMBER_TYPE), interpreter.getTypeIfRefinable(nodeA, scope));

    Node nodeB = Node.newString(Token.NAME, "b");
    nodeB.setJSType(registry.getNativeType(STRING_TYPE));
    assertEquals(registry.getNativeType(STRING_TYPE), interpreter.getTypeIfRefinable(nodeB, scope));

    Node nodeC = Node.newString(Token.NAME, "c");
    assertNull(interpreter.getTypeIfRefinable(nodeC, scope));
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinable_GetPropNode() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    slots.put("obj.prop1", createSlot("obj.prop1", registry.getNativeType(BOOLEAN_TYPE)));
    slots.put("obj.prop2", createSlot("obj.prop2", null));
    FlowScope scope = createFlowScope(slots, null, null);

    Node getProp1 = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "prop1"));
    assertEquals(registry.getNativeType(BOOLEAN_TYPE), interpreter.getTypeIfRefinable(getProp1, scope));

    Node getProp2 = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "prop2"));
    getProp2.setJSType(registry.getNativeType(STRING_TYPE));
    assertEquals(registry.getNativeType(STRING_TYPE), interpreter.getTypeIfRefinable(getProp2, scope));

    Node getProp3 = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "prop3"));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE), interpreter.getTypeIfRefinable(getProp3, scope));
  }

  @Test(timeout = 4000)
  public void testDeclareNameInScope_NameAndGetProp() {
    Map<String, JSType> inferredSlots = new HashMap<String, JSType>();
    Map<String, JSType> inferredQualifiedSlots = new HashMap<String, JSType>();
    FlowScope scope = createFlowScope(null, inferredSlots, inferredQualifiedSlots);

    Node nameNode = Node.newString(Token.NAME, "x");
    interpreter.declareNameInScope(scope, nameNode, registry.getNativeType(NUMBER_TYPE));
    assertEquals(registry.getNativeType(NUMBER_TYPE), inferredSlots.get("x"));

    Node getPropNode = new Node(Token.GETPROP, Node.newString(Token.NAME, "foo"), Node.newString(Token.STRING, "bar"));
    interpreter.declareNameInScope(scope, getPropNode, registry.getNativeType(STRING_TYPE));
    assertEquals(registry.getNativeType(STRING_TYPE), inferredQualifiedSlots.get("foo.bar"));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedWithoutUndefined_AllTypes() {
    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(BOOLEAN_TYPE)));
    assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NUMBER_TYPE)));
    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(STRING_TYPE)));
    assertEquals(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NULL_TYPE)));
    assertEquals(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_TYPE)));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_OBJECT_TYPE)));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(UNKNOWN_TYPE)));
    assertNull(interpreter.getRestrictedWithoutUndefined(registry.getNativeType(VOID_TYPE)));

    FunctionType funcType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    assertSame(funcType, interpreter.getRestrictedWithoutUndefined(funcType));

    ObjectType objType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    assertSame(objType, interpreter.getRestrictedWithoutUndefined(objType));

    ParameterizedType paramType = registry.createParameterizedType(objType, registry.getNativeType(STRING_TYPE));
    assertSame(paramType, interpreter.getRestrictedWithoutUndefined(paramType));

    TemplateType templateType = new TemplateType(registry, "T");
    assertSame(templateType, interpreter.getRestrictedWithoutUndefined(templateType));

    JSType allRestricted = interpreter.getRestrictedWithoutUndefined(registry.getNativeType(ALL_TYPE));
    assertTrue(allRestricted.isUnionType());
    assertFalse(allRestricted.contains(registry.getNativeType(VOID_TYPE)));

    JSType unionWithVoid = registry.createUnionType(registry.getNativeType(NUMBER_TYPE), registry.getNativeType(VOID_TYPE));
    assertEquals(registry.getNativeType(NUMBER_TYPE), interpreter.getRestrictedWithoutUndefined(unionWithVoid));

    EnumType enumType1 = registry.createEnumType("EnumNum", null, registry.getNativeType(NUMBER_TYPE));
    assertSame(enumType1.getElementsType(), interpreter.getRestrictedWithoutUndefined(enumType1.getElementsType()));

    EnumType enumType2 = registry.createEnumType("EnumVoid", null, unionWithVoid);
    assertEquals(registry.getNativeType(NUMBER_TYPE), interpreter.getRestrictedWithoutUndefined(enumType2.getElementsType()));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedWithoutNull_AllTypes() {
    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(BOOLEAN_TYPE)));
    assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NUMBER_TYPE)));
    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(STRING_TYPE)));
    assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(VOID_TYPE)));
    assertEquals(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_TYPE)));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_OBJECT_TYPE)));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(UNKNOWN_TYPE)));
    assertNull(interpreter.getRestrictedWithoutNull(registry.getNativeType(NULL_TYPE)));

    FunctionType funcType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    assertSame(funcType, interpreter.getRestrictedWithoutNull(funcType));

    ObjectType objType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    assertSame(objType, interpreter.getRestrictedWithoutNull(objType));

    ParameterizedType paramType = registry.createParameterizedType(objType, registry.getNativeType(STRING_TYPE));
    assertSame(paramType, interpreter.getRestrictedWithoutNull(paramType));

    TemplateType templateType = new TemplateType(registry, "T");
    assertSame(templateType, interpreter.getRestrictedWithoutNull(templateType));

    JSType allRestricted = interpreter.getRestrictedWithoutNull(registry.getNativeType(ALL_TYPE));
    assertTrue(allRestricted.isUnionType());
    assertFalse(allRestricted.contains(registry.getNativeType(NULL_TYPE)));

    JSType unionWithNull = registry.createUnionType(registry.getNativeType(STRING_TYPE), registry.getNativeType(NULL_TYPE));
    assertEquals(registry.getNativeType(STRING_TYPE), interpreter.getRestrictedWithoutNull(unionWithNull));

    EnumType enumType1 = registry.createEnumType("EnumNum", null, registry.getNativeType(NUMBER_TYPE));
    assertSame(enumType1.getElementsType(), interpreter.getRestrictedWithoutNull(enumType1.getElementsType()));

    EnumType enumType2 = registry.createEnumType("EnumNull", null, unionWithNull);
    assertEquals(registry.getNativeType(STRING_TYPE), interpreter.getRestrictedWithoutNull(enumType2.getElementsType()));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfResult_EqualTrue() {
    assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "number", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "string", true));

    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "boolean", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "number", true));

    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "string", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "number", true));

    assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "undefined", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "number", true));

    assertEquals(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "object", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "number", true));

    FunctionType funcType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    assertSame(funcType, interpreter.getRestrictedByTypeOfResult(funcType, "function", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(funcType, "number", true));

    ObjectType objType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    assertSame(objType, interpreter.getRestrictedByTypeOfResult(objType, "object", true));
    assertEquals(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(objType, "function", true));

    ObjectType dateType = registry.getNativeObjectType(DATE_TYPE);
    assertNull(interpreter.getRestrictedByTypeOfResult(dateType, "function", true));

    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "object", true));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "function", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "number", true));

    assertEquals(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_TYPE), "number", true));

    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "string", true));
    assertEquals(registry.getNativeType(ALL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "unknown_val", true));

    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(UNKNOWN_TYPE), "boolean", true));

    JSType unionType = registry.createUnionType(registry.getNativeType(STRING_TYPE), registry.getNativeType(NUMBER_TYPE));
    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(unionType, "string", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(unionType, "boolean", true));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedByTypeOfResult_EqualFalse() {
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "number", false));
    assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "string", false));

    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "boolean", false));
    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "number", false));

    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "string", false));
    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "number", false));

    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "undefined", false));
    assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "number", false));

    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "object", false));
    assertEquals(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "number", false));

    FunctionType funcType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    assertNull(interpreter.getRestrictedByTypeOfResult(funcType, "function", false));
    assertSame(funcType, interpreter.getRestrictedByTypeOfResult(funcType, "number", false));

    ObjectType objType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    assertNull(interpreter.getRestrictedByTypeOfResult(objType, "object", false));
    assertNull(interpreter.getRestrictedByTypeOfResult(objType, "function", false));
    assertSame(objType, interpreter.getRestrictedByTypeOfResult(objType, "string", false));

    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "object", false));
    assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "function", false));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "number", false));

    assertEquals(registry.getNativeType(ALL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "string", false));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(UNKNOWN_TYPE), "number", false));

    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE), registry.getNativeType(NUMBER_TYPE), registry.getNativeType(BOOLEAN_TYPE));
    JSType notString = interpreter.getRestrictedByTypeOfResult(unionType, "string", false);
    assertTrue(notString.isUnionType());
    assertFalse(notString.contains(registry.getNativeType(STRING_TYPE)));
    assertTrue(notString.contains(registry.getNativeType(NUMBER_TYPE)));
    assertTrue(notString.contains(registry.getNativeType(BOOLEAN_TYPE)));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullInputTypes() {
    assertNull(interpreter.getRestrictedWithoutUndefined(null));
    assertNull(interpreter.getRestrictedWithoutNull(null));
    assertNull(interpreter.getRestrictedByTypeOfResult(null, "number", false));

    assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "number", true));
    assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "boolean", true));
    assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "string", true));
    assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "undefined", true));
    assertEquals(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "function", true));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "object", true));
    assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "unrecognized", true));
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinable_GetPropWithNullQualifiedName() {
    Node callTarget = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP, callTarget, Node.newString(Token.STRING, "bar"));
    assertNull(getProp.getQualifiedName());

    FlowScope scope = createFlowScope(null, null, null);
    assertNull(interpreter.getTypeIfRefinable(getProp, scope));
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinable_NonRefinableTokens() {
    FlowScope scope = createFlowScope(null, null, null);
    assertNull(interpreter.getTypeIfRefinable(new Node(Token.NUMBER), scope));
    assertNull(interpreter.getTypeIfRefinable(new Node(Token.STRING), scope));
    assertNull(interpreter.getTypeIfRefinable(new Node(Token.TRUE), scope));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  /**
   * Targets the defect exposed by TypeInferenceTest::testNoThisInference.
   * Defect Ground Truth:
   * java.lang.IllegalArgumentException: Node cannot be refined.
   *
   * In ChainableReverseAbstractInterpreter#declareNameInScope, Token.THIS
   * must be gracefully handled (no-op) instead of falling through to default
   * and throwing IllegalArgumentException.
   */
  @Test(timeout = 4000)
  public void testDeclareNameInScope_withThisNode_defectFix() {
    Node thisNode = new Node(Token.THIS);
    FlowScope scope = createFlowScope(null, null, null);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    interpreter.declareNameInScope(scope, thisNode, numberType);
  }

  @Test(timeout = 4000)
  public void testGetTypeIfRefinable_ThisNodeReturnsNull() {
    Node thisNode = new Node(Token.THIS);
    FlowScope scope = createFlowScope(null, null, null);
    assertNull(interpreter.getTypeIfRefinable(thisNode, scope));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructor_nullConventionThrows() {
    new ConcreteInterpreter(null, registry);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testAppend_alreadyChainedThrows() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);

    link2.append(link3);
    link1.append(link2);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testDeclareNameInScope_unrefinableNodeThrows() {
    Node numNode = Node.newNumber(42.0);
    FlowScope scope = createFlowScope(null, null, null);
    interpreter.declareNameInScope(scope, numNode, registry.getNativeType(NUMBER_TYPE));
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testDeclareNameInScope_getPropWithNullQualifiedNameThrows() {
    Node callTarget = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP, callTarget, Node.newString(Token.STRING, "bar"));
    FlowScope scope = createFlowScope(null, null, null);
    interpreter.declareNameInScope(scope, getProp, registry.getNativeType(STRING_TYPE));
  }

  // =========================================================================
  // Partition E: Inner Class & Visitor Subclass Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testRestrictByTrueTypeOfResultVisitor_Subclass() {
    TestTrueVisitor visitor = new TestTrueVisitor(interpreter);

    assertNull(visitor.caseNoObjectType());
    assertNull(visitor.caseBooleanType());
    assertNull(visitor.caseFunctionType(registry.createFunctionType(registry.getNativeType(NUMBER_TYPE))));
    assertNull(visitor.caseNullType());
    assertNull(visitor.caseNumberType());
    assertNull(visitor.caseObjectType((ObjectType) registry.getNativeType(OBJECT_TYPE)));
    assertNull(visitor.caseStringType());
    assertNull(visitor.caseVoidType());
    assertEquals(registry.getNativeType(ALL_TYPE), visitor.caseAllType());
    assertEquals(registry.getNativeType(UNKNOWN_TYPE), visitor.caseUnknownType());
  }

  @Test(timeout = 4000)
  public void testRestrictByFalseTypeOfResultVisitor_Subclass() {
    TestFalseVisitor visitor = new TestFalseVisitor(interpreter);

    assertEquals(registry.getNativeType(ALL_TYPE), visitor.caseTopType(registry.getNativeType(ALL_TYPE)));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE), visitor.caseNoObjectType());
    assertEquals(registry.getNativeType(BOOLEAN_TYPE), visitor.caseBooleanType());

    FunctionType funcType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    assertSame(funcType, visitor.caseFunctionType(funcType));

    assertEquals(registry.getNativeType(NULL_TYPE), visitor.caseNullType());
    assertEquals(registry.getNativeType(NUMBER_TYPE), visitor.caseNumberType());

    ObjectType objType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    assertSame(objType, visitor.caseObjectType(objType));

    assertEquals(registry.getNativeType(STRING_TYPE), visitor.caseStringType());
    assertEquals(registry.getNativeType(VOID_TYPE), visitor.caseVoidType());
    assertEquals(registry.getNativeType(ALL_TYPE), visitor.caseAllType());
    assertEquals(registry.getNativeType(UNKNOWN_TYPE), visitor.caseUnknownType());
    assertEquals(registry.getNativeType(NO_TYPE), visitor.caseNoType());

    ParameterizedType paramType = registry.createParameterizedType(objType, registry.getNativeType(STRING_TYPE));
    assertSame(paramType, visitor.caseParameterizedType(paramType));

    TemplateType templateType = new TemplateType(registry, "U");
    assertSame(templateType, visitor.caseTemplateType(templateType));

    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(STRING_TYPE));
    assertSame(enumType.getElementsType(), visitor.caseEnumElementType(enumType.getElementsType()));

    JSType unionType = registry.createUnionType(registry.getNativeType(NUMBER_TYPE), registry.getNativeType(STRING_TYPE));
    assertEquals(unionType, visitor.caseUnionType((UnionType) unionType));
  }
}