/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 * Key Methods Tested:
 *   - FunctionType constructors (ordinary, constructor, interface)
 *   - forInterface(registry, name, source)
 *   - isConstructor(), isInterface(), isOrdinaryFunction(), isInstanceType(), canBeCalled()
 *   - getParameters(), getParametersNode(), getMinArguments(), getMaxArguments()
 *   - getReturnType(), isReturnTypeInferred(), getInternalArrowType()
 *   - getPrototype(), setPrototype(PrototypeObjectType), setPrototypeBasedOn(ObjectType)
 *   - getSlot(String), getOwnPropertyNames(), defineProperty(String, JSType, boolean, Node)
 *   - getPropertyType(String) ("call", "apply", and general properties)
 *   - getSuperClassConstructor(), getSubTypes(), hasImplementedInterfaces(), getImplementedInterfaces()
 *   - getAllImplementedInterfaces(), setImplementedInterfaces(List)
 *   - getExtendedInterfaces(), getExtendedInterfacesCount(), getAllExtendedInterfaces(), setExtendedInterfaces(List)
 *   - getTopDefiningInterface(ObjectType, String), getTopMostDefiningType(String)
 *   - isEquivalentTo(JSType), hashCode(), hasEqualCallType(FunctionType), toString(), toDebugHashCodeString()
 *   - isSubtype(JSType), supAndInfHelper via getLeastSupertype(JSType), getGreatestSubtype(JSType)
 *   - tryMergeFunctionPiecewise(FunctionType, boolean)
 *   - getInstanceType(), setInstanceType(ObjectType), hasInstanceType(), getTypeOfThis()
 *   - clearCachedValues(), hasCachedValues(), resolveInternal(ErrorReporter, StaticScope)
 *
 * Defect Zone Targeted (Closure-537 / TypedScopeCreator inheritance):
 *   - setPrototypeBasedOn(ObjectType baseType): When baseType is an instance of another class
 *     (e.g., Sub.prototype = new Super()), the buggy logic in FunctionType misses `baseType.isInstanceType()`,
 *     failing to wrap the instance into a new PrototypeObjectType whose implicit prototype is the instance.
 *     This causes `subCtor.getSuperClassConstructor()` and prototype chain resolution to fail.
 */

package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.testing.SimpleErrorReporter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testOrdinaryFunctionCreationAndProperties() {
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn = new FunctionType(
        registry, "foo", null, arrow, null, null, false, false);

    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertFalse(fn.hasInstanceType());
    assertTrue(fn.canBeCalled());
    assertEquals("foo", fn.getReferenceName());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
    assertEquals(fn, fn.toMaybeFunctionType());
  }

  @Test(timeout = 4000)
  public void testConstructorCreationAndInstanceType() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());

    ObjectType instance = ctor.getInstanceType();
    assertNotNull(instance);
    assertEquals(ctor, instance.getConstructor());
    assertEquals(instance, ctor.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInterfaceCreationAndExtension() {
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", null);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("MyInterface", iface.getReferenceName());

    FunctionType parentIface = FunctionType.forInterface(registry, "ParentInterface", null);
    iface.setExtendedInterfaces(ImmutableList.of(parentIface.getInstanceType()));

    assertEquals(1, iface.getExtendedInterfacesCount());
    Iterable<ObjectType> extended = iface.getExtendedInterfaces();
    Iterator<ObjectType> it = extended.iterator();
    assertTrue(it.hasNext());
    assertEquals(parentIface.getInstanceType(), it.next());
    assertFalse(it.hasNext());

    Iterable<ObjectType> allExtended = iface.getAllExtendedInterfaces();
    Iterator<ObjectType> allIt = allExtended.iterator();
    assertTrue(allIt.hasNext());
    assertEquals(parentIface.getInstanceType(), allIt.next());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyInitializationAndSlots() {
    FunctionType fn = registry.createFunctionType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(fn.getOwnPropertyNames().contains("prototype"));

    ObjectType proto = fn.getPrototype();
    assertNotNull(proto);
    assertTrue(fn.getOwnPropertyNames().contains("prototype"));
    assertNotNull(fn.getSlot("prototype"));
    assertEquals(proto, fn.getSlot("prototype").getType());
    assertEquals(fn, proto.getOwnerFunction());

    // Calling again returns the cached prototype
    assertSame(proto, fn.getPrototype());
  }

  @Test(timeout = 4000)
  public void testCallAndApplyPropertyGeneration() {
    Node paramA = Node.newString(Token.NAME, "x");
    paramA.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node params = new Node(Token.LP, paramA);

    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), params);

    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = callProp.toMaybeFunctionType();
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), callFn.getReturnType());

    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = applyProp.toMaybeFunctionType();
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testCallPropertyGenerationWithoutParams() {
    ArrowType arrowNoParams = new ArrowType(
        registry, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fnNoParams = new FunctionType(
        registry, "noParams", null, arrowNoParams, null, null, false, false);

    JSType callProp = fnNoParams.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), callProp.toMaybeFunctionType().getReturnType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Parameter Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentCountCalculation() {
    // 0 params
    FunctionType fn0 = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(0, fn0.getMinArguments());
    assertEquals(0, fn0.getMaxArguments());

    // 1 required, 1 optional, 1 varargs
    Node p1 = Node.newString(Token.NAME, "req");
    Node p2 = Node.newString(Token.NAME, "opt");
    p2.setOptionalArg(true);
    Node p3 = Node.newString(Token.NAME, "var");
    p3.setVarArgs(true);

    Node params = new Node(Token.LP, p1, p2, p3);
    FunctionType fnComplex = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), params);

    assertEquals(1, fnComplex.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnComplex.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testMinArgumentsWithOptionalBeforeRequired() {
    // Unusual scenario supported by Rhino: optional param followed by required param
    Node p1 = Node.newString(Token.NAME, "opt");
    p1.setOptionalArg(true);
    Node p2 = Node.newString(Token.NAME, "req");

    Node params = new Node(Token.LP, p1, p2);
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), params);

    assertEquals(2, fn.getMinArguments());
    assertEquals(2, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testGetParametersWhenNull() {
    ArrowType arrow = new ArrowType(registry, null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrow, null, null, false, false);
    assertNull(fn.getParametersNode());
    Iterable<Node> it = fn.getParameters();
    assertNotNull(it);
    assertFalse(it.iterator().hasNext());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeNullAndSelfInstance() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    // Passing null should be silently discarded and return false
    assertFalse(ctor.setPrototype(null));

    // Passing constructor's own instance should return false
    ObjectType instance = ctor.getInstanceType();
    if (instance instanceof PrototypeObjectType) {
      assertFalse(ctor.setPrototype((PrototypeObjectType) instance));
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-537 / Subclass Hierarchy)
  // =========================================================================

  /**
   * Targets the defect where `setPrototypeBasedOn` does not wrap an `InstanceObjectType`
   * into a fresh `PrototypeObjectType`.
   *
   * When `SubClass.prototype = new SuperClass()` is executed:
   * The prototype of `SubClass` must have its implicit prototype set to the `new SuperClass()`
   * instance, and `SubClass.getSuperClassConstructor()` must resolve to `SuperClass`.
   */
  @Test(timeout = 4000)
  public void testSetPrototypeBasedOnInstanceObjectTypeDefect537() {
    FunctionType superCtor = registry.createConstructorType(
        "SuperClass", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    ObjectType superInstance = superCtor.getInstanceType();

    FunctionType subCtor = registry.createConstructorType(
        "SubClass", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    // Mimic SubClass.prototype = new SuperClass()
    subCtor.setPrototypeBasedOn(superInstance);

    // The subCtor's prototype should NOT be identical to superInstance itself.
    // If it is identical, modifying subCtor.prototype pollutes superInstance and owner function is corrupted.
    assertNotSame(
        "Prototype should be wrapped in a new PrototypeObjectType, not directly superInstance",
        superInstance,
        subCtor.getPrototype());

    // The implicit prototype of SubClass.prototype must be the super instance
    assertEquals(superInstance, subCtor.getPrototype().getImplicitPrototype());

    // SuperClass constructor should correctly be retrieved as the super constructor
    assertEquals(
        "SubClass super constructor must resolve to SuperClass",
        superCtor,
        subCtor.getSuperClassConstructor());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOnAnonymousObjectLiteral() {
    FunctionType ctor = registry.createConstructorType(
        "Bar", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    // Anonymous object literal: { x: 1 }
    PrototypeObjectType record = new PrototypeObjectType(
        registry, null, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));

    ctor.setPrototypeBasedOn(record);

    // For anonymous object literal, it is directly set as prototype
    assertSame(record, ctor.getPrototype());
    assertEquals(ctor, record.getOwnerFunction());
  }

  @Test(timeout = 4000)
  public void testGetTopMostDefiningTypeHierarchy() {
    FunctionType superCtor = registry.createConstructorType(
        "Base", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    superCtor.getPrototype().defineProperty("sharedProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    FunctionType subCtor = registry.createConstructorType(
        "Derived", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    PrototypeObjectType derivedProto = new PrototypeObjectType(
        registry, "Derived.prototype", superCtor.getInstanceType());
    subCtor.setPrototype(derivedProto);

    ObjectType top = subCtor.getTopMostDefiningType("sharedProp");
    assertEquals(superCtor.getInstanceType(), top);
  }

  @Test(timeout = 4000)
  public void testGetTopMostDefiningTypeInterface() {
    FunctionType parentIface = FunctionType.forInterface(registry, "ParentIface", null);
    parentIface.getPrototype().defineProperty("ifaceProp", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    FunctionType childIface = FunctionType.forInterface(registry, "ChildIface", null);
    childIface.setExtendedInterfaces(ImmutableList.of(parentIface.getInstanceType()));

    ObjectType top = childIface.getTopMostDefiningType("ifaceProp");
    assertEquals(parentIface.getInstanceType(), top);
  }

  // =========================================================================
  // Partition D: Type Lattice, Subtyping & Least Supertype / Greatest Subtype
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubtypingBetweenFunctions() {
    FunctionType fnNumberReturn = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fnAllReturn = registry.createFunctionType(registry.getNativeType(JSTypeNative.ALL_TYPE));

    // Return type is covariant: fnNumberReturn is a subtype of fnAllReturn
    assertTrue(fnNumberReturn.isSubtype(fnAllReturn));
    assertFalse(fnAllReturn.isSubtype(fnNumberReturn));

    // Any function is subtype of FUNCTION_INSTANCE_TYPE
    JSType funcInstanceType = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertTrue(fnNumberReturn.isSubtype(funcInstanceType));
  }

  @Test(timeout = 4000)
  public void testInterfaceSubtypingRules() {
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);
    FunctionType ordinary = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));

    // Any ordinary function can be assigned to an interface function
    assertTrue(ordinary.isSubtype(iface));
    // An interface cannot be assigned to ordinary function
    assertFalse(iface.isSubtype(ordinary));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType fnA = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fnB = registry.createFunctionType(registry.getNativeType(JSTypeNative.STRING_TYPE));

    JSType sup = fnA.getLeastSupertype(fnB);
    assertTrue(sup.isFunctionType());
    FunctionType supFn = sup.toMaybeFunctionType();
    // Least supertype of Number and String returns Union(Number, String)
    assertTrue(supFn.getReturnType().isUnionType());

    JSType inf = fnA.getGreatestSubtype(fnB);
    assertTrue(inf.isFunctionType());
    FunctionType infFn = inf.toMaybeFunctionType();
    // Greatest subtype of Number and String returns Bottom/NoType
    assertTrue(infFn.getReturnType().isNoType() || infFn.getReturnType().isEmptyType());
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeWithFunctionInstance() {
    FunctionType fn = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    JSType funcInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);

    assertEquals(funcInstance, fn.getLeastSupertype(funcInstance));
    assertEquals(fn, fn.getGreatestSubtype(funcInstance));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeWithDifferentParametersFailsToMergePiecewise() {
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn1 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), new Node(Token.LP, p1));

    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType fn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), new Node(Token.LP, p2));

    JSType sup = fn1.getLeastSupertype(fn2);
    // When params differ, tryMergeFunctionPiecewise returns null and falls back to U2U_CONSTRUCTOR_TYPE
    assertEquals(registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE), sup);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Equality, Subtypes & Resolution
  // =========================================================================

  @Test(timeout = 4000)
  public void testEquivalenceAndHashCode() {
    FunctionType fn1 = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn2 = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    assertTrue(fn1.isEquivalentTo(fn2));
    assertTrue(fn1.hasEqualCallType(fn2));
    assertEquals(fn1.hashCode(), fn2.hashCode());

    FunctionType iface1 = FunctionType.forInterface(registry, "SameName", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "SameName", null);
    assertTrue(iface1.isEquivalentTo(iface2));
    assertEquals(iface1.hashCode(), iface2.hashCode());

    FunctionType iface3 = FunctionType.forInterface(registry, "DiffName", null);
    assertFalse(iface1.isEquivalentTo(iface3));
    assertFalse(iface1.isEquivalentTo(fn1));
    assertFalse(fn1.isEquivalentTo(null));
  }

  @Test(timeout = 4000)
  public void testToStringAndDebugHashCodeString() {
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setVarArgs(true);
    p2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));

    Node params = new Node(Token.LP, p1, p2);
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), params);

    String str = fn.toString();
    assertTrue(str.contains("number"));
    assertTrue(str.contains("...[string]"));
    assertTrue(str.contains("boolean"));

    String debugStr = fn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.startsWith("function ("));

    // Native Function instance type toString
    assertEquals("Function", registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE).toString());
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesHierarchy() {
    FunctionType ifaceA = FunctionType.forInterface(registry, "InterfaceA", null);
    FunctionType ctorBase = registry.createConstructorType(
        "BaseClass", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    ctorBase.setImplementedInterfaces(ImmutableList.of(ifaceA.getInstanceType()));
    assertTrue(ctorBase.hasImplementedInterfaces());

    FunctionType ctorSub = registry.createConstructorType(
        "SubClass", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    ctorSub.setPrototype(new PrototypeObjectType(registry, "SubClass.prototype", ctorBase.getInstanceType()));

    // SubClass inherits interfaces from BaseClass
    assertTrue(ctorSub.hasImplementedInterfaces());
    Iterable<ObjectType> allIfaces = ctorSub.getAllImplementedInterfaces();
    Iterator<ObjectType> it = allIfaces.iterator();
    assertTrue(it.hasNext());
    assertEquals(ifaceA.getInstanceType(), it.next());
  }

  @Test(timeout = 4000)
  public void testClearCachedValuesCascade() {
    FunctionType baseCtor = registry.createConstructorType(
        "Base", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType subCtor = registry.createConstructorType(
        "Sub", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    subCtor.setPrototype(new PrototypeObjectType(registry, "Sub.prototype", baseCtor.getInstanceType()));

    assertTrue(baseCtor.hasCachedValues());
    baseCtor.clearCachedValues();
    // Cache clearing cascades to subtypes without throwing NullPointerException
    assertNotNull(baseCtor.getSubTypes());
    assertEquals(1, baseCtor.getSubTypes().size());
    assertEquals(subCtor, baseCtor.getSubTypes().get(0));
  }

  @Test(timeout = 4000)
  public void testResolveInternal() {
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "arg"));
    params.getFirstChild().setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), params);

    FunctionType resolved = (FunctionType) fn.resolve(errorReporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isResolved());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyPrototypeDelegation() {
    FunctionType ctor = registry.createConstructorType(
        "TestCtor", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));

    ObjectType customProto = new PrototypeObjectType(
        registry, "CustomProto", registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));

    boolean defined = ctor.defineProperty("prototype", customProto, false, null);
    assertTrue(defined);
    assertSame(customProto, ctor.getPrototype());

    // Defining the exact same prototype again returns true early
    assertTrue(ctor.defineProperty("prototype", customProto, false, null));

    // Defining non-object type for prototype fails
    assertFalse(ctor.defineProperty("prototype", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null));
  }

  // =========================================================================
  // Partition F: Exception Handling & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType ordinary = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    ordinary.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorOnOrdinaryFunctionThrows() {
    FunctionType ordinary = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    ordinary.getSuperClassConstructor();
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testSetExtendedInterfacesOnNonInterfaceThrows() {
    FunctionType ctor = registry.createConstructorType(
        "NotAnInterface", null, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    ctor.setExtendedInterfaces(Collections.<ObjectType>emptyList());
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInvalidSourceNodeTypeThrows() {
    Node invalidSourceNode = new Node(Token.VAR);
    ArrowType arrow = new ArrowType(registry, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    new FunctionType(registry, "badFn", invalidSourceNode, arrow, null, null, false, false);
  }

  @Test(timeout = 4000)
  public void testSourceNodeGetterAndSetter() {
    FunctionType fn = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertNull(fn.getSource());

    Node fnNode = new Node(Token.FUNCTION);
    fn.setSource(fnNode);
    assertEquals(fnNode, fn.getSource());
  }

  @Test(timeout = 4000)
  public void testUniversalConstructorIsInstanceType() {
    FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    assertTrue("Universal constructor is its own instance", u2u.isInstanceType());

    FunctionType normalCtor = registry.createConstructorType("Normal", null, null, null);
    assertFalse("Normal constructor is not an instance type", normalCtor.isInstanceType());
  }
}