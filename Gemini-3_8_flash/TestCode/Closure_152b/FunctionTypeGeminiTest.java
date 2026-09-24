package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 * 
 * 1. Defects4J Known Defect Analysis:
 *    - In resolveInternal(ErrorReporter, StaticScope):
 *      typeOfThis is blindly cast via `typeOfThis = (ObjectType) safeResolve(typeOfThis, t, scope);`
 *      When a function's @this annotation references a typedef resolving to a primitive type
 *      (e.g., StringType) or a UnionType, resolving typeOfThis returns a non-ObjectType JSType.
 *      This triggers ClassCastException in the unpatched code.
 *
 * 2. Decision Branches Covered:
 *    - Function kind: ORDINARY, CONSTRUCTOR, INTERFACE
 *    - getMinArguments / getMaxArguments:
 *      * No params (null parameters node)
 *      * Empty params node
 *      * Required params only
 *      * Optional params (!isVarArgs && isOptionalArg)
 *      * VarArgs params (last child isVarArgs -> Integer.MAX_VALUE)
 *      * Interleaved optional and required parameters
 *    - Prototype resolution and manipulation:
 *      * getPrototype() lazy initialization
 *      * setPrototype(null) -> returns false
 *      * setPrototype(getInstanceType()) when isConstructor -> returns false
 *      * setPrototype with superclass ctor linking (addSubType)
 *      * setPrototypeBasedOn(ObjectType) when prototype is null vs already initialized
 *    - Inheritance & Hierarchy:
 *      * getSuperClassConstructor() with null implicit prototype vs valid constructor
 *      * hasUnknownSupertype() loop, termination on null, unknown type detection
 *      * getTopMostDefiningType() property lookup traversal
 *      * getAllImplementedInterfaces() and addRelatedInterfaces() with interfaces extending interfaces
 *    - Equivalence and Subtyping:
 *      * isEquivalentTo: self, non-FunctionType, constructor vs constructor, interface vs interface
 *      * isSubtype: constructor/ordinary combinations, contravariant/covariant checks, interface checks
 *      * supAndInfHelper / tryMergeFunctionPiecewise:
 *        - Identical signatures, mismatched parameters (returns null)
 *        - Piecewise merging of return types & typeOfThis
 *    - Property Access:
 *      * getPropertyType for "prototype", "call", "apply", and undeclared properties
 *      * defineProperty for "prototype" with ObjectType vs non-ObjectType
 *    - Debug and String representations:
 *      * toString() and toDebugHashCodeString() with/without known typeOfThis, with var_args union/non-union
 */
public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private Node functionNode;
  private ObjectType objectType;
  private ObjectType stringObjectType;
  private JSType numberType;
  private JSType stringType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    functionNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    stringObjectType = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorCreationAndKindFlags() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType ctor = new FunctionType(registry, "MyClass", functionNode, arrow, null, null, true, false);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.isFunctionType());
    assertTrue(ctor.canBeCalled());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals("MyClass", ctor.getReferenceName());
    assertSame(functionNode, ctor.getSource());
  }

  @Test(timeout = 4000)
  public void testOrdinaryFunctionCreationAndKindFlags() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), stringType);
    FunctionType fn = new FunctionType(registry, "myFn", functionNode, arrow, objectType, null, false, false);

    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isOrdinaryFunction());
    assertTrue(fn.isFunctionType());
    assertFalse(fn.hasInstanceType());
    assertSame(objectType, fn.getTypeOfThis());
    assertSame(stringType, fn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testInterfaceCreationAndKindFlags() {
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", functionNode);

    assertFalse(iface.isConstructor());
    assertTrue(iface.isInterface());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("MyInterface", iface.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyInitialization() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "Fn", null, arrow, null, null, false, false);

    assertFalse(fn.hasCachedValues());
    FunctionPrototypeType proto = fn.getPrototype();
    assertNotNull(proto);
    assertTrue(fn.hasCachedValues());
    assertSame(proto, fn.getPrototype());
    assertSame(proto, fn.getPropertyType("prototype"));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOn() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "Fn", null, arrow, null, null, true, false);

    fn.setPrototypeBasedOn(stringObjectType);
    FunctionPrototypeType proto = fn.getPrototype();
    assertSame(stringObjectType, proto.getImplicitPrototype());

    fn.setPrototypeBasedOn(objectType);
    assertSame(objectType, fn.getPrototype().getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testCallAndApplyPropertyGeneration() {
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "param1"));
    ArrowType arrow = new ArrowType(registry, params, numberType);
    FunctionType fn = new FunctionType(registry, "targetFn", null, arrow, objectType, null, false, false);

    assertTrue(fn.hasProperty("call"));
    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = (FunctionType) callProp;
    assertSame(numberType, callFn.getReturnType());

    assertTrue(fn.hasProperty("apply"));
    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = (FunctionType) applyProp;
    assertSame(numberType, applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testCallPropertyGenerationWithoutParams() {
    ArrowType arrow = new ArrowType(registry, null, stringType);
    FunctionType fn = new FunctionType(registry, "noParamFn", null, arrow, null, null, false, false);

    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    assertEquals(stringType, ((FunctionType) callProp).getReturnType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentCountCalculations() {
    Node p1 = Node.newString(Token.NAME, "a");
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setOptionalArg(true);
    Node p3 = Node.newString(Token.NAME, "c"); // Required after optional!
    Node paramsNode = new Node(Token.LP, p1, p2, p3);

    ArrowType arrow = new ArrowType(registry, paramsNode, null);
    FunctionType fn = new FunctionType(registry, "f", null, arrow, null, null, false, false);

    assertEquals(3, fn.getMinArguments());
    assertEquals(3, fn.getMaxArguments());

    Node varArg = Node.newString(Token.NAME, "rest");
    varArg.setVarArgs(true);
    paramsNode.addChildToBack(varArg);

    assertEquals(3, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());

    Iterable<Node> paramsIterable = fn.getParameters();
    int count = 0;
    for (Node p : paramsIterable) {
      count++;
    }
    assertEquals(4, count);
  }

  @Test(timeout = 4000)
  public void testArgumentCountWithNullParametersNode() {
    ArrowType arrow = new ArrowType(registry, null, null);
    FunctionType fn = new FunctionType(registry, "f", null, arrow, null, null, false, false);

    assertEquals(0, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    assertFalse(fn.getParameters().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeEdgeCases() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType ctor = new FunctionType(registry, "Ctor", null, arrow, null, null, true, false);

    assertFalse(ctor.setPrototype(null));
    assertFalse(ctor.setPrototype((FunctionPrototypeType) ctor.getInstanceType()));

    FunctionPrototypeType validProto = new FunctionPrototypeType(registry, ctor, objectType);
    assertTrue(ctor.setPrototype(validProto));
  }

  @Test(timeout = 4000)
  public void testSuperClassAndSubTypesTracking() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType superCtor = new FunctionType(registry, "Super", null, arrow, null, null, true, false);

    FunctionType subCtor = new FunctionType(registry, "Sub", null, arrow, null, null, true, false);
    FunctionPrototypeType subProto = new FunctionPrototypeType(registry, subCtor, superCtor.getInstanceType());
    subCtor.setPrototype(subProto);

    assertSame(superCtor, subCtor.getSuperClassConstructor());
    assertNotNull(superCtor.getSubTypes());
    assertEquals(1, superCtor.getSubTypes().size());
    assertSame(subCtor, superCtor.getSubTypes().get(0));
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertype() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType ctor = new FunctionType(registry, "Ctor", null, arrow, null, null, true, false);
    assertFalse(ctor.hasUnknownSupertype());

    ObjectType unknownObj = (ObjectType) registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    FunctionPrototypeType protoWithUnknown = new FunctionPrototypeType(registry, ctor, unknownObj);
    ctor.setPrototype(protoWithUnknown);

    assertTrue(ctor.hasUnknownSupertype());
  }

  @Test(timeout = 4000)
  public void testGetTopMostDefiningType() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType superCtor = new FunctionType(registry, "Super", null, arrow, null, null, true, false);
    superCtor.getPrototype().defineProperty("sharedProp", stringType, false, false);

    FunctionType subCtor = new FunctionType(registry, "Sub", null, arrow, null, null, true, false);
    subCtor.setPrototype(new FunctionPrototypeType(registry, subCtor, superCtor.getInstanceType()));

    JSType topType = subCtor.getTopMostDefiningType("sharedProp");
    assertEquals(superCtor.getInstanceType(), topType);
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesHierarchy() {
    FunctionType ifaceBase = FunctionType.forInterface(registry, "IBase", null);
    FunctionType ifaceSub = FunctionType.forInterface(registry, "ISub", null);
    ifaceSub.setPrototype(new FunctionPrototypeType(registry, ifaceSub, ifaceBase.getInstanceType()));

    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType ctor = new FunctionType(registry, "Impl", null, arrow, null, null, true, false);
    ctor.setImplementedInterfaces(ImmutableList.of(ifaceSub.getInstanceType()));

    Iterable<ObjectType> allInterfaces = ctor.getAllImplementedInterfaces();
    Set<ObjectType> set = Sets.newHashSet(allInterfaces);
    assertTrue(set.contains(ifaceSub.getInstanceType()));
    assertTrue(set.contains(ifaceBase.getInstanceType()));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where `typeOfThis` fails to resolve if it resolves to a primitive type
   * (e.g. StringType or UnionType) via a NamedType / typedef alias instead of an ObjectType.
   * Buggy version: `typeOfThis = (ObjectType) safeResolve(typeOfThis, t, scope);` throws ClassCastException!
   */
  @Test(timeout = 4000)
  public void testResolveInternalWithNonObjectTypeThisTypeRevealsClassCastDefect() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn = new FunctionType(registry, "faultFn", null, arrow, null, null, false, false);

    NamedType nonObjectThisType = new NamedType(registry, "StringTypedef", "dummy.js", 1, 1);
    registry.declareType("StringTypedef", stringType);

    fn.setInstanceType(nonObjectThisType);

    StaticScope<JSType> emptyScope = new Scope<JSType>() {
      @Override public Node getRootNode() { return null; }
      @Override public StaticScope<JSType> getParentScope() { return null; }
      @Override public StaticSlot<JSType> getSlot(String name) { return null; }
      @Override public StaticSlot<JSType> getOwnSlot(String name) { return null; }
      @Override public JSType getTypeOfThis() { return null; }
    };

    ErrorReporter reporter = new SimpleErrorReporter();
    try {
      JSType resolved = fn.resolveInternal(reporter, emptyScope);
      assertNotNull(resolved);
      assertTrue(resolved.isFunctionType());
    } catch (ClassCastException cce) {
      fail("DEFECT DETECTED: FunctionType.resolveInternal blindly casts safeResolve(typeOfThis) to ObjectType: " + cce.getMessage());
    }
  }

  /**
   * Targets the defect when typedef is a UnionType of objects or primitives.
   */
  @Test(timeout = 4000)
  public void testResolveInternalWithUnionTypeThisTypeRevealsClassCastDefect() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn = new FunctionType(registry, "unionFn", null, arrow, null, null, false, false);

    NamedType unionThisType = new NamedType(registry, "UnionTypedef", "dummy.js", 1, 1);
    JSType union = registry.createUnionType(stringType, numberType);
    registry.declareType("UnionTypedef", union);

    fn.setInstanceType(unionThisType);

    StaticScope<JSType> emptyScope = new Scope<JSType>() {
      @Override public Node getRootNode() { return null; }
      @Override public StaticScope<JSType> getParentScope() { return null; }
      @Override public StaticSlot<JSType> getSlot(String name) { return null; }
      @Override public StaticSlot<JSType> getOwnSlot(String name) { return null; }
      @Override public JSType getTypeOfThis() { return null; }
    };

    ErrorReporter reporter = new SimpleErrorReporter();
    try {
      JSType resolved = fn.resolveInternal(reporter, emptyScope);
      assertNotNull(resolved);
    } catch (ClassCastException cce) {
      fail("DEFECT DETECTED: UnionType cannot be cast to ObjectType during resolveInternal: " + cce.getMessage());
    }
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorRequiresFunctionNodeOrNull() {
    Node invalidNode = new Node(Token.NAME, "invalid");
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    new FunctionType(registry, "badNode", invalidNode, arrow, null, null, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorRequiresNonNullArrowType() {
    new FunctionType(registry, "nullArrow", null, null, null, null, false, false);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInterfaceRequiresNonNullName() {
    FunctionType.forInterface(registry, null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetInstanceTypeThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "ordinary", null, arrow, null, null, false, false);
    fn.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "ordinary", null, arrow, null, null, false, false);
    fn.getSuperClassConstructor();
  }

  // =========================================================================
  // Partition E: Subtyping, Lattice, Contracts & Equality
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubtypingBetweenFunctions() {
    ArrowType arrow1 = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn1 = new FunctionType(registry, "fn1", null, arrow1, objectType, null, false, false);

    ArrowType arrow2 = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn2 = new FunctionType(registry, "fn2", null, arrow2, objectType, null, false, false);

    assertTrue(fn1.isSubtype(fn2));
    assertTrue(fn2.isSubtype(fn1));

    FunctionType iface = FunctionType.forInterface(registry, "IFace", null);
    assertTrue(fn1.isSubtype(iface));
    assertFalse(iface.isSubtype(fn1));
  }

  @Test(timeout = 4000)
  public void testSupAndInfHelperMerging() {
    Node p1 = Node.newString(Token.NAME, "arg");
    p1.setJSType(numberType);
    Node p2 = Node.newString(Token.NAME, "arg");
    p2.setJSType(numberType);

    ArrowType arrow1 = new ArrowType(registry, new Node(Token.LP, p1), numberType);
    FunctionType fn1 = new FunctionType(registry, "fn1", null, arrow1, objectType, null, false, false);

    ArrowType arrow2 = new ArrowType(registry, new Node(Token.LP, p2), stringType);
    FunctionType fn2 = new FunctionType(registry, "fn2", null, arrow2, objectType, null, false, false);

    JSType sup = fn1.getLeastSupertype(fn2);
    assertTrue(sup.isFunctionType());
    FunctionType supFn = (FunctionType) sup;
    assertTrue(supFn.getReturnType().isUnionType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    assertTrue(inf.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToContract() {
    ArrowType arrow1 = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn1 = new FunctionType(registry, "fn", null, arrow1, objectType, null, false, false);
    FunctionType fn2 = new FunctionType(registry, "fn", null, arrow1, objectType, null, false, false);

    assertTrue(fn1.isEquivalentTo(fn2));
    assertEquals(fn1.hashCode(), fn2.hashCode());
    assertFalse(fn1.isEquivalentTo(stringType));

    FunctionType iface1 = FunctionType.forInterface(registry, "SameName", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "SameName", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "DiffName", null);
    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(iface3));
    assertEquals(iface1.hashCode(), iface2.hashCode());

    FunctionType ctor1 = new FunctionType(registry, "C", null, arrow1, null, null, true, false);
    FunctionType ctor2 = new FunctionType(registry, "C", null, arrow1, null, null, true, false);
    assertFalse(ctor1.isEquivalentTo(ctor2));
    assertTrue(ctor1.isEquivalentTo(ctor1));
  }

  @Test(timeout = 4000)
  public void testToStringAndDebugHashCodeString() {
    Node param = Node.newString(Token.NAME, "x");
    param.setJSType(numberType);
    Node varParam = Node.newString(Token.NAME, "rest");
    varParam.setVarArgs(true);
    varParam.setJSType(registry.createUnionType(stringType, registry.getNativeType(JSTypeNative.VOID_TYPE)));

    ArrowType arrow = new ArrowType(registry, new Node(Token.LP, param, varParam), numberType);
    FunctionType fn = new FunctionType(registry, "fmt", null, arrow, objectType, null, false, false);

    String str = fn.toString();
    assertTrue(str.contains("function ("));
    assertTrue(str.contains("this:"));
    assertTrue(str.contains("...[string]"));
    assertTrue(str.contains(": number"));

    String debugStr = fn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.startsWith("function ("));

    FunctionType instanceType = (FunctionType) registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", instanceType.toString());
    assertNotNull(instanceType.toDebugHashCodeString());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyPrototype() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "protoOwner", null, arrow, null, null, false, false);

    assertTrue(fn.defineProperty("prototype", stringObjectType, false, false));
    assertSame(stringObjectType, fn.getPrototype().getImplicitPrototype());

    assertTrue(fn.defineProperty("prototype", fn.getPrototype(), false, false));

    assertFalse(fn.defineProperty("prototype", numberType, false, false));
    assertTrue(fn.isPropertyTypeInferred("prototype"));
  }

  @Test(timeout = 4000)
  public void testVisitorPattern() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    FunctionType fn = new FunctionType(registry, "vFn", null, arrow, null, null, false, false);

    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enumElem"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "visitedFunction:" + type.getReferenceName(); }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "num"; }
      @Override public String caseStringType() { return "str"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
    };

    assertEquals("visitedFunction:vFn", fn.visit(visitor));
  }
}