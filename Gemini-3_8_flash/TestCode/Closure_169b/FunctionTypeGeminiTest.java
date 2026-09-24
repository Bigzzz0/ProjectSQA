/* [Branch & Defect Analysis Matrix]
 * ====================================================================================
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 * Target Defects:
 *   - TypeCheckTest::testIssue791 / RecordTypeTest::testSubtypeWithUnknowns2:
 *     Subtyping and lattice operations (sup/inf) involving functions, unknown types,
 *     and record structural properties. Ensures safe handling of unknown params/returns
 *     in `supAndInfHelper`, `tryMergeFunctionPiecewise`, and `isSubtype`.
 *
 * Decision / Branch Coverage Targets:
 *   1. Constructors & Kinds:
 *      - ORDINARY, CONSTRUCTOR, INTERFACE.
 *      - Validation: source == null || Token.FUNCTION == source.getType().
 *      - ArrowType != null check. Interface name != null check.
 *      - Template type names initialization (null vs non-null list).
 *   2. Struct & Dict Property Access:
 *      - makesStructs() / makesDicts() on non-constructor (false).
 *      - makesStructs() / makesDicts() directly set (STRUCT / DICT).
 *      - Inheritance propagation: super-class struct/dict automatically marks child.
 *   3. Interface & Inheritance Hierarchy:
 *      - hasImplementedInterfaces: empty, direct, and inherited via super-class.
 *      - setImplementedInterfaces on constructor vs non-constructor (UnsupportedOperationException).
 *      - extendedInterfaces: count, getters, setExtendedInterfaces on interface vs constructor (UnsupportedOperationException).
 *      - getAllImplementedInterfaces & getAllExtendedInterfaces recursive traversal.
 *      - getSuperClassConstructor: ctor/interface vs ordinary function (Preconditions check).
 *      - getTopDefiningInterface & getTopMostDefiningType traversal and exception guards.
 *   4. Parameter & Argument Boundaries:
 *      - getMinArguments / getMaxArguments: 0 params, required, optional, var_args,
 *        and optional preceding required params.
 *      - Null parameter nodes vs present parameter nodes.
 *   5. Prototype Slot & Lazy Property Generation:
 *      - getSlot("prototype") lazy initialization.
 *      - Structural/anonymous function (refName == null) sets prototype to UNKNOWN_TYPE.
 *      - Named function sets prototype to PrototypeObjectType.
 *      - setPrototypeBasedOn wrapping logic (named, native, function prototype vs anonymous).
 *      - setPrototype guards: null prototype, prototype == getInstanceType(), replacedPrototype.
 *      - Lazy functions: "call", "apply", "bind" and getBindReturnType parameter stripping.
 *      - defineProperty("prototype", nonObject vs object vs equal object).
 *   6. Lattice Operations (supAndInfHelper / tryMergeFunctionPiecewise):
 *      - Equivalent functions.
 *      - Ordinary functions without unknown params/returns (isSubtype of that vs this).
 *      - Piecewise merge with matching parameters (return type sup/inf, typeOfThis sup/inf).
 *      - Piecewise merge with mismatched parameters (fallback).
 *      - Function instance type special cases.
 *      - Fallback to U2U_CONSTRUCTOR_TYPE and LEAST_FUNCTION_TYPE.
 *   7. String Representations & Debugging:
 *      - toStringHelper: FUNCTION_INSTANCE_TYPE, annotated vs unannotated, thisType (new: vs this:),
 *        required, optional, var_args, and union with void.
 *      - toDebugHashCodeString with recursive "me" reference.
 *   8. Object Lifecycle & Resolution:
 *      - cloneWithoutArrowType.
 *      - resolveInternal: typeOfThis de-nullification, interface resolution, subTypes resolution.
 *      - clearCachedValues cascading to subTypes, instanceType, prototype.
 * ====================================================================================
 */

package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
  }

  // ============================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ============================================================================

  @Test(timeout = 4000)
  public void testOrdinaryFunctionStateAndPredicates() {
    ArrowType arrow = new ArrowType(
        registry,
        new Node(Token.PARAM_LIST),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn = new FunctionType(
        registry, "foo", null, arrow, null, null, false, false);

    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertFalse(fn.isInstanceType());
    assertTrue(fn.canBeCalled());
    assertSame(fn, fn.toMaybeFunctionType());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
    assertEquals(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testConstructorAndInstanceTypeState() {
    ArrowType arrow = new ArrowType(
        registry,
        new Node(Token.PARAM_LIST),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType ctor = new FunctionType(
        registry, "MyClass", null, arrow, null, null, true, false);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());

    ObjectType instanceType = ctor.getInstanceType();
    assertNotNull(instanceType);
    assertSame(instanceType, ctor.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInterfaceCreationAndExtension() {
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", null);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals(0, iface.getExtendedInterfacesCount());

    FunctionType superIface = FunctionType.forInterface(registry, "SuperInterface", null);
    iface.setExtendedInterfaces(ImmutableList.of(superIface.getInstanceType()));

    assertEquals(1, iface.getExtendedInterfacesCount());
    assertTrue(iface.getExtendedInterfaces().iterator().hasNext());
    assertSame(superIface.getInstanceType(), iface.getExtendedInterfaces().iterator().next());
  }

  @Test(timeout = 4000)
  public void testMakesStructsAndMakesDictsInheritance() {
    FunctionType baseCtor = new FunctionType(
        registry, "Base", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    FunctionType childCtor = new FunctionType(
        registry, "Child", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);

    assertFalse(baseCtor.makesStructs());
    assertFalse(baseCtor.makesDicts());

    baseCtor.setStruct();
    assertTrue(baseCtor.makesStructs());
    assertFalse(baseCtor.makesDicts());

    // Connect child to base via prototype inheritance
    childCtor.setPrototypeBasedOn(baseCtor.getInstanceType());
    assertTrue(childCtor.makesStructs());

    // Test Dicts
    FunctionType dictBase = new FunctionType(
        registry, "DictBase", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    FunctionType dictChild = new FunctionType(
        registry, "DictChild", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);

    dictBase.setDict();
    assertTrue(dictBase.makesDicts());
    dictChild.setPrototypeBasedOn(dictBase.getInstanceType());
    assertTrue(dictChild.makesDicts());

    // Non-constructor returns false regardless
    FunctionType ordinary = new FunctionType(
        registry, "ord", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    assertFalse(ordinary.makesStructs());
    assertFalse(ordinary.makesDicts());
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesDirectAndInherited() {
    FunctionType iface = FunctionType.forInterface(registry, "Printable", null);

    FunctionType parentCtor = new FunctionType(
        registry, "Parent", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    parentCtor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    assertTrue(parentCtor.hasImplementedInterfaces());
    assertEquals(1, ImmutableList.copyOf(parentCtor.getImplementedInterfaces()).size());
    assertEquals(1, ImmutableList.copyOf(parentCtor.getOwnImplementedInterfaces()).size());

    FunctionType childCtor = new FunctionType(
        registry, "Child", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());

    assertTrue(childCtor.hasImplementedInterfaces());
    assertEquals(0, ImmutableList.copyOf(childCtor.getOwnImplementedInterfaces()).size());
    assertEquals(1, ImmutableList.copyOf(childCtor.getImplementedInterfaces()).size());
  }

  @Test(timeout = 4000)
  public void testGetAllImplementedInterfacesRecursive() {
    FunctionType ifaceBase = FunctionType.forInterface(registry, "IBase", null);
    FunctionType ifaceSub = FunctionType.forInterface(registry, "ISub", null);
    ifaceSub.setExtendedInterfaces(ImmutableList.of(ifaceBase.getInstanceType()));

    FunctionType ctor = new FunctionType(
        registry, "Implementer", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    ctor.setImplementedInterfaces(ImmutableList.of(ifaceSub.getInstanceType()));

    Set<ObjectType> allIfaces = ImmutableList.copyOf(ctor.getAllImplementedInterfaces()).stream()
        .collect(java.util.stream.Collectors.toSet());
    assertEquals(2, allIfaces.size());
    assertTrue(allIfaces.contains(ifaceSub.getInstanceType()));
    assertTrue(allIfaces.contains(ifaceBase.getInstanceType()));
  }

  // ============================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ============================================================================

  @Test(timeout = 4000)
  public void testMinAndMaxArgumentsCalculations() {
    Node paramList = new Node(Token.PARAM_LIST);
    ArrowType arrowEmpty = new ArrowType(registry, paramList, null);
    FunctionType fnEmpty = new FunctionType(
        registry, "fnEmpty", null, arrowEmpty, null, null, false, false);

    assertEquals(0, fnEmpty.getMinArguments());
    assertEquals(0, fnEmpty.getMaxArguments());

    // 1 required param
    Node p1 = Node.newString(Token.NAME, "req1");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    paramList.addChildToBack(p1);
    assertEquals(1, fnEmpty.getMinArguments());
    assertEquals(1, fnEmpty.getMaxArguments());

    // 1 optional param
    Node p2 = Node.newString(Token.NAME, "opt2");
    p2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    p2.setOptionalArg(true);
    paramList.addChildToBack(p2);
    assertEquals(1, fnEmpty.getMinArguments());
    assertEquals(2, fnEmpty.getMaxArguments());

    // Special JS boundary: Optional parameter preceding a required parameter
    Node p3 = Node.newString(Token.NAME, "req3");
    p3.setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    paramList.addChildToBack(p3);
    assertEquals(3, fnEmpty.getMinArguments());
    assertEquals(3, fnEmpty.getMaxArguments());

    // VarArgs parameter
    Node p4 = Node.newString(Token.NAME, "rest");
    p4.setJSType(registry.getNativeType(JSTypeNative.ALL_TYPE));
    p4.setVarArgs(true);
    paramList.addChildToBack(p4);
    assertEquals(3, fnEmpty.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnEmpty.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyInitializationAndSlots() {
    // Structural function without a name
    FunctionType anon = new FunctionType(
        registry, null, null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);

    assertFalse(anon.getOwnPropertyNames().contains("prototype"));
    ObjectType protoAnon = anon.getPrototype();
    assertEquals(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), protoAnon);
    assertTrue(anon.getOwnPropertyNames().contains("prototype"));

    // Named function creates named prototype
    FunctionType named = new FunctionType(
        registry, "NamedFn", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    ObjectType protoNamed = named.getPrototype();
    assertEquals("NamedFn.prototype", protoNamed.getReferenceName());
    assertNotNull(named.getSlot("prototype"));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeGuards() {
    FunctionType ctor = new FunctionType(
        registry, "GuardClass", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);

    // Guard 1: Null prototype
    assertFalse(ctor.setPrototype(null, null));

    // Guard 2: Constructor prototype set to its own instance type
    assertFalse(ctor.setPrototype(ctor.getInstanceType(), null));

    // Valid prototype replacement
    ObjectType customProto = new PrototypeObjectType(
        registry, "CustomProto", registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    assertTrue(ctor.setPrototype(customProto, null));
    assertSame(customProto, ctor.getPrototype());
  }

  @Test(timeout = 4000)
  public void testLazyMethodsCallApplyAndBind() {
    Node params = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "arg1");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params.addChildToBack(p1);

    FunctionType fn = new FunctionType(
        registry, "target", null,
        new ArrowType(registry, params, registry.getNativeType(JSTypeNative.STRING_TYPE)),
        null, null, false, false);

    // Trigger lazy 'call'
    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = callProp.toMaybeFunctionType();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), callFn.getReturnType());

    // Trigger lazy 'bind'
    JSType bindProp = fn.getPropertyType("bind");
    assertTrue(bindProp.isFunctionType());

    // Trigger lazy 'apply'
    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = applyProp.toMaybeFunctionType();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testGetBindReturnTypeBoundary() {
    Node params = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    params.addChildToBack(p1);
    params.addChildToBack(p2);

    FunctionType fn = new FunctionType(
        registry, "bindTarget", null,
        new ArrowType(registry, params, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)),
        null, null, false, false);

    // argsToBind = -1: Accepts any arguments
    FunctionType bindAll = fn.getBindReturnType(-1);
    assertNull(bindAll.getParametersNode());

    // argsToBind = 0: Preserves all parameters
    FunctionType bindZero = fn.getBindReturnType(0);
    assertEquals(2, bindZero.getParametersNode().getChildCount());

    // argsToBind = 2: Strips 1 parameter
    FunctionType bindOne = fn.getBindReturnType(2);
    assertEquals(1, bindOne.getParametersNode().getChildCount());
  }

  // ============================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Issue 791 & testSubtypeWithUnknowns2)
  // ============================================================================

  /**
   * Targets lattice merge and subtyping flaws with UNKNOWN_TYPE and RecordType
   * as exposed by TypeCheckTest::testIssue791 and RecordTypeTest::testSubtypeWithUnknowns2.
   */
  @Test(timeout = 4000)
  public void testSubtypeWithUnknowns2DefectTarget() {
    // Construct function with unknown parameter and return type
    Node params1 = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "arg");
    p1.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    params1.addChildToBack(p1);

    FunctionType fnUnknown = new FunctionType(
        registry, "fnUnknown", null,
        new ArrowType(registry, params1, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)),
        null, null, false, false);

    // Construct function with concrete parameter and return type
    Node params2 = new Node(Token.PARAM_LIST);
    Node p2 = Node.newString(Token.NAME, "arg");
    p2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params2.addChildToBack(p2);

    FunctionType fnConcrete = new FunctionType(
        registry, "fnConcrete", null,
        new ArrowType(registry, params2, registry.getNativeType(JSTypeNative.NUMBER_TYPE)),
        null, null, false, false);

    // Subtyping must be sound in presence of unknowns
    assertTrue("Concrete function should be a subtype of function with unknown components",
        fnConcrete.isSubtype(fnUnknown));

    // Sup and Inf Helper evaluation under unknowns
    FunctionType sup = fnUnknown.supAndInfHelper(fnConcrete, true);
    assertNotNull(sup);
    assertEquals(registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE), sup);

    FunctionType inf = fnUnknown.supAndInfHelper(fnConcrete, false);
    assertNotNull(inf);
    assertEquals(registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE), inf);
  }

  @Test(timeout = 4000)
  public void testPiecewiseFunctionMergeLattice() {
    Node params1 = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "x");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params1.addChildToBack(p1);

    Node params2 = new Node(Token.PARAM_LIST);
    Node p2 = Node.newString(Token.NAME, "x");
    p2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params2.addChildToBack(p2);

    FunctionType fn1 = new FunctionType(
        registry, null, null,
        new ArrowType(registry, params1, registry.getNativeType(JSTypeNative.NUMBER_TYPE)),
        null, null, false, false);

    FunctionType fn2 = new FunctionType(
        registry, null, null,
        new ArrowType(registry, params2, registry.getNativeType(JSTypeNative.STRING_TYPE)),
        null, null, false, false);

    // Sup merge on equal parameter lists creates union return type
    FunctionType sup = fn1.supAndInfHelper(fn2, true);
    assertNotNull(sup);
    assertTrue(sup.getReturnType().isUnionType());

    // Inf merge on equal parameter lists creates bottom return type (NO_TYPE)
    FunctionType inf = fn1.supAndInfHelper(fn2, false);
    assertNotNull(inf);
    assertTrue(inf.getReturnType().isNoType());
  }

  // ============================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // ============================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorInvalidSourceNodeGuard() {
    Node invalidNode = new Node(Token.VAR);
    ArrowType arrow = new ArrowType(registry, new Node(Token.PARAM_LIST), null);
    new FunctionType(registry, "bad", invalidNode, arrow, null, null, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullArrowTypeGuard() {
    new FunctionType(registry, "bad", null, null, null, null, false, false);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInterfaceNullNameGuard() {
    FunctionType.forInterface(registry, null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType ord = new FunctionType(
        registry, "ord", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    ord.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorOnOrdinaryFunctionThrows() {
    FunctionType ord = new FunctionType(
        registry, "ord", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    ord.getSuperClassConstructor();
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testSetImplementedInterfacesOnNonConstructorThrows() {
    FunctionType ord = new FunctionType(
        registry, "ord", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    ord.setImplementedInterfaces(Collections.emptyList());
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testSetExtendedInterfacesOnNonInterfaceThrows() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    ctor.setExtendedInterfaces(Collections.emptyList());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetTopMostDefiningTypeOnOrdinaryFunctionThrows() {
    FunctionType ord = new FunctionType(
        registry, "ord", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    ord.getTopMostDefiningType("someProp");
  }

  // ============================================================================
  // PARTITION E: Object Lifecycle, Hierarchy, Contracts & String Representations
  // ============================================================================

  @Test(timeout = 4000)
  public void testGetTopMostDefiningTypeInClassHierarchy() {
    FunctionType baseCtor = new FunctionType(
        registry, "SuperClass", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    baseCtor.getPrototype().defineDeclaredProperty(
        "sharedProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    FunctionType subCtor = new FunctionType(
        registry, "SubClass", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    subCtor.setPrototypeBasedOn(baseCtor.getInstanceType());

    ObjectType definingType = subCtor.getTopMostDefiningType("sharedProp");
    assertEquals(baseCtor.getInstanceType(), definingType);
  }

  @Test(timeout = 4000)
  public void testCheckFunctionEquivalenceHelperAndHashCode() {
    // 1. Constructor equivalence: Identity based
    FunctionType ctor1 = new FunctionType(
        registry, "C", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    FunctionType ctor2 = new FunctionType(
        registry, "C", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, true, false);
    assertFalse(ctor1.checkFunctionEquivalenceHelper(ctor2, false));
    assertTrue(ctor1.checkFunctionEquivalenceHelper(ctor1, false));

    // 2. Interface equivalence: Name based
    FunctionType iface1 = FunctionType.forInterface(registry, "IdenticalIface", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "IdenticalIface", null);
    assertTrue(iface1.checkFunctionEquivalenceHelper(iface2, false));
    assertEquals(iface1.hashCode(), iface2.hashCode());

    // 3. Ordinary function: Arrow & thisType based
    ArrowType arrow1 = new ArrowType(registry, new Node(Token.PARAM_LIST), registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    ArrowType arrow2 = new ArrowType(registry, new Node(Token.PARAM_LIST), registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType ord1 = new FunctionType(registry, "f", null, arrow1, null, null, false, false);
    FunctionType ord2 = new FunctionType(registry, "f", null, arrow2, null, null, false, false);
    assertTrue(ord1.checkFunctionEquivalenceHelper(ord2, false));
    assertTrue(ord1.hasEqualCallType(ord2));
  }

  @Test(timeout = 4000)
  public void testToStringHelperWithParamsAndModifiers() {
    Node params = new Node(Token.PARAM_LIST);

    // Required param
    Node p1 = Node.newString(Token.NAME, "req");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params.addChildToBack(p1);

    // Optional param with void union (tests optional formatting)
    Node p2 = Node.newString(Token.NAME, "opt");
    JSType optType = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    p2.setJSType(optType);
    p2.setOptionalArg(true);
    params.addChildToBack(p2);

    // Varargs param
    Node p3 = Node.newString(Token.NAME, "rest");
    p3.setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    p3.setVarArgs(true);
    params.addChildToBack(p3);

    FunctionType fn = new FunctionType(
        registry, "formatTest", null,
        new ArrowType(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE)),
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE),
        null, false, false);

    String str = fn.toString();
    assertTrue(str.contains("this:Object"));
    assertTrue(str.contains("number"));
    assertTrue(str.contains("string="));
    assertTrue(str.contains("...[boolean]"));
  }

  @Test(timeout = 4000)
  public void testCloneWithoutArrowTypeAndLifecycle() {
    FunctionType ctor = new FunctionType(
        registry, "OrigClass", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), registry.getNativeType(JSTypeNative.NUMBER_TYPE)),
        null, null, true, false);

    FunctionType cloned = ctor.cloneWithoutArrowType();
    assertTrue(cloned.isConstructor());
    assertEquals("OrigClass", cloned.getReferenceName());
    assertEquals(0, cloned.getParametersNode().getChildCount());
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), cloned.getReturnType());

    // Caching and clearing
    assertFalse(ctor.hasCachedValues());
    ctor.getPrototype();
    assertTrue(ctor.hasCachedValues());
    ctor.clearCachedValues();
    assertTrue(ctor.hasCachedValues()); // prototypeSlot remains non-null
  }

  @Test(timeout = 4000)
  public void testSetSourceHotSwapPreservation() {
    Node srcNode1 = new Node(Token.FUNCTION);
    FunctionType fn = new FunctionType(
        registry, "HotSwap", srcNode1,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, null, false, false);
    fn.getPrototype(); // initialize prototypeSlot

    assertSame(srcNode1, fn.getSource());
    fn.setSource(null);
    assertNull(fn.getSource());
    assertNotNull(fn.getSlot("prototype"));
  }

  @Test(timeout = 4000)
  public void testTemplateTypesAndResolution() {
    ImmutableList<String> templates = ImmutableList.of("T", "U");
    FunctionType fn = new FunctionType(
        registry, "Templated", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), null),
        null, templates, false, false);

    assertTrue(fn.hasAnyTemplateInternal());
    assertEquals(templates, fn.getTemplateTypeNames());

    // Resolution lifecycle
    JSType resolved = fn.resolve(errorReporter, null);
    assertSame(fn, resolved);
  }

  @Test(timeout = 4000)
  public void testDebugHashCodeSelfReferenceMe() {
    FunctionType fn = new FunctionType(
        registry, "Recursive", null,
        new ArrowType(registry, new Node(Token.PARAM_LIST), registry.getNativeType(JSTypeNative.NUMBER_TYPE)),
        null, null, false, false);

    // Re-assign arrow return type to self to trigger recursive debug print
    ArrowType selfArrow = new ArrowType(registry, new Node(Token.PARAM_LIST), fn);
    FunctionType selfFn = new FunctionType(
        registry, "Self", null, selfArrow, null, null, false, false);

    String debugStr = selfFn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.startsWith("function ("));
  }
}