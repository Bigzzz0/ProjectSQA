package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructors:
 *    - source node verification (null vs Token.FUNCTION vs invalid token -> IllegalArgumentException)
 *    - arrowType null-check -> NullPointerException
 *    - isConstructor: true -> InstanceObjectType (or NoObjectType preservation)
 *    - isConstructor: false -> typeOfThis specified vs default to UNKNOWN_TYPE
 *    - forInterface factory: name == null guard, invalid source token guard
 * 2. Parameter / Argument Bounds:
 *    - getParameters / getParametersNode: null vs empty vs populated Node(LP)
 *    - getMinArguments: required, optional, varargs; optional preceding required parameter
 *    - getMaxArguments: varargs present (returns Integer.MAX_VALUE) vs non-varargs (child count)
 * 3. Prototypes & Subclasses:
 *    - getPrototype: lazy instantiation branching (prototype == null)
 *    - setPrototypeBasedOn: prototype null vs existing implicit prototype update
 *    - setPrototype: null check, prototype == getInstanceType() check, subtype registration in super ctor
 * 4. Interfaces:
 *    - getImplementedInterfaces: constructor vs non-constructor, superCtor == null vs present
 *    - getAllImplementedInterfaces: transitive traversal, interface inheritance cycle avoidance
 * 5. Property Handling:
 *    - hasProperty / hasOwnProperty: "prototype" special case vs inherited lookup
 *    - getPropertyType: "prototype", lazy "call" (null params vs non-null params), lazy "apply"
 *    - defineProperty: "prototype" with non-object vs equivalent vs new prototype
 *    - isPropertyTypeInferred: "prototype" branch
 * 6. Type Lattice & Subtyping (getLeastSupertype, getGreatestSubtype, isSubtype):
 *    - Identity equivalence vs FunctionType equivalence
 *    - Ordinary functions with matching parameter/this signatures vs disparate
 *    - FUNCTION_INSTANCE_TYPE supremum / infimum fallbacks
 *    - Constructor / interface subtyping rules and union type alternate checks
 * 7. String Formats & Debugging:
 *    - toString: FUNCTION_INSTANCE_TYPE special name "Function", thisType inclusion, varargs union formatting
 *    - toDebugHashCodeString: recursion "me" guard
 * 8. Defect Targeted Zone (Defects4J):
 *    - Constructor / function return type inference handling where return type defaults to VOID_TYPE (undefined)
 *      versus UNKNOWN_TYPE (?) in function signatures.
 */
public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType voidType;
  private JSType unknownType;
  private ObjectType objectType;
  private ObjectType noObjectType;
  private JSType functionInstanceType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    noObjectType = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
    functionInstanceType = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testOrdinaryFunctionCreationAndState() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn = new FunctionType(
        registry, "testFn", null, arrow, null, "T", false, false);

    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertFalse(fn.hasInstanceType());
    assertEquals("testFn", fn.getReferenceName());
    assertEquals("T", fn.getTemplateTypeName());
    assertEquals(unknownType, fn.getTypeOfThis());
    assertEquals(numberType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
    assertEquals(arrow, fn.getInternalArrowType());
  }

  @Test(timeout = 4000)
  public void testConstructorCreationAndInstanceType() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor = new FunctionType(
        registry, "MyClass", null, arrow, null, null, true, false);

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
  public void testInterfaceCreationAndState() {
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", null);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("MyInterface", iface.getReferenceName());
    assertNotNull(iface.getInstanceType());
    assertEquals(iface, iface.getInstanceType().getConstructor());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyInitializationAndMutation() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn = new FunctionType(
        registry, "LazyProto", null, arrow, null, null, false, false);

    assertFalse(fn.hasCachedValues());
    FunctionPrototypeType proto1 = fn.getPrototype();
    assertNotNull(proto1);
    assertTrue(fn.hasCachedValues());

    // Subsequent retrieval returns the same cached instance
    FunctionPrototypeType proto2 = fn.getPrototype();
    assertSame(proto1, proto2);

    // Update prototype based on an object type
    fn.setPrototypeBasedOn(objectType);
    assertEquals(objectType, fn.getPrototype().getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testHierarchySubTypesAndSuperConstructor() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType superCtor = new FunctionType(
        registry, "SuperClass", null, arrow, null, null, true, false);
    FunctionType subCtor = new FunctionType(
        registry, "SubClass", null, arrow, null, null, true, false);

    assertNull(superCtor.getSubTypes());
    assertNull(subCtor.getSuperClassConstructor());

    // Connect subCtor to superCtor via prototype inheritance
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    assertEquals(superCtor, subCtor.getSuperClassConstructor());
    List<FunctionType> superSubTypes = superCtor.getSubTypes();
    assertNotNull(superSubTypes);
    assertEquals(1, superSubTypes.size());
    assertEquals(subCtor, superSubTypes.get(0));
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesHierarchy() {
    FunctionType ifaceParent = FunctionType.forInterface(registry, "IParent", null);
    FunctionType ifaceChild = FunctionType.forInterface(registry, "IChild", null);
    ifaceChild.setPrototypeBasedOn(ifaceParent.getInstanceType());

    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType superCtor = new FunctionType(
        registry, "BaseImpl", null, arrow, null, null, true, false);
    FunctionType ifaceBase = FunctionType.forInterface(registry, "IBase", null);
    superCtor.setImplementedInterfaces(ImmutableList.of(ifaceBase.getInstanceType()));

    FunctionType subCtor = new FunctionType(
        registry, "DerivedImpl", null, arrow, null, null, true, false);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    subCtor.setImplementedInterfaces(ImmutableList.of(ifaceChild.getInstanceType()));

    Iterable<ObjectType> directInterfaces = subCtor.getImplementedInterfaces();
    int directCount = 0;
    for (ObjectType t : directInterfaces) {
      directCount++;
    }
    assertEquals(2, directCount); // ifaceChild and superCtor's ifaceBase

    Iterable<ObjectType> allInterfaces = subCtor.getAllImplementedInterfaces();
    int allCount = 0;
    boolean foundParent = false;
    for (ObjectType t : allInterfaces) {
      allCount++;
      if ("IParent".equals(t.getConstructor().getReferenceName())) {
        foundParent = true;
      }
    }
    assertTrue(foundParent);
    assertEquals(3, allCount); // ifaceChild, ifaceParent, ifaceBase
  }

  @Test(timeout = 4000)
  public void testTopMostDefiningTypeLookup() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType grandParent = new FunctionType(
        registry, "GrandParent", null, arrow, null, null, true, false);
    grandParent.getPrototype().defineProperty("sharedProp", stringType, false, false);

    FunctionType parent = new FunctionType(
        registry, "Parent", null, arrow, null, null, true, false);
    parent.setPrototypeBasedOn(grandParent.getInstanceType());
    parent.getPrototype().defineProperty("sharedProp", stringType, false, false);

    FunctionType child = new FunctionType(
        registry, "Child", null, arrow, null, null, true, false);
    child.setPrototypeBasedOn(parent.getInstanceType());
    child.getPrototype().defineProperty("sharedProp", stringType, false, false);

    JSType top = child.getTopMostDefiningType("sharedProp");
    assertEquals(grandParent.getInstanceType(), top);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testParametersArgumentBoundaries() {
    // Null parameters Node
    ArrowType arrowNullParams = new ArrowType(registry, null, numberType);
    FunctionType fnNullParams = new FunctionType(
        registry, "nullParams", null, arrowNullParams, null, null, false, false);

    assertNull(fnNullParams.getParametersNode());
    assertFalse(fnNullParams.getParameters().iterator().hasNext());
    assertEquals(0, fnNullParams.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnNullParams.getMaxArguments());

    // Empty parameters LP Node
    Node emptyLp = new Node(Token.LP);
    ArrowType arrowEmpty = new ArrowType(registry, emptyLp, numberType);
    FunctionType fnEmpty = new FunctionType(
        registry, "emptyParams", null, arrowEmpty, null, null, false, false);
    assertEquals(0, fnEmpty.getMinArguments());
    assertEquals(0, fnEmpty.getMaxArguments());

    // Function with required, optional, and varargs
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    builder.addRequiredParams(numberType);
    builder.addOptionalParams(stringType);
    builder.addVarArgs(booleanType);
    Node paramNode = builder.build();

    ArrowType arrowFull = new ArrowType(registry, paramNode, voidType);
    FunctionType fnFull = new FunctionType(
        registry, "fullParams", null, arrowFull, null, null, false, false);

    assertEquals(1, fnFull.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnFull.getMaxArguments());

    int count = 0;
    for (Node p : fnFull.getParameters()) {
      count++;
      assertNotNull(p.getJSType());
    }
    assertEquals(3, count);
  }

  @Test(timeout = 4000)
  public void testOptionalPrecedingRequiredParameterMinArgs() {
    // Edge case: an optional parameter placed BEFORE a required parameter
    Node lp = new Node(Token.LP);
    Node optParam = Node.newString(Token.NAME, "optArg");
    optParam.setOptionalArg(true);
    optParam.setJSType(numberType);
    lp.addChildToBack(optParam);

    Node reqParam = Node.newString(Token.NAME, "reqArg");
    reqParam.setJSType(stringType);
    lp.addChildToBack(reqParam);

    ArrowType arrow = new ArrowType(registry, lp, voidType);
    FunctionType fn = new FunctionType(
        registry, "strangeParams", null, arrow, null, null, false, false);

    // Algorithm iterates to position of the last non-optional, non-varargs argument (position 2)
    assertEquals(2, fn.getMinArguments());
    assertEquals(2, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testNoObjectTypePreservationInConstructor() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor = new FunctionType(
        registry, "NoObjCtor", null, arrow, noObjectType, null, true, false);

    assertEquals(noObjectType, ctor.getInstanceType());
    // getTypeOfThis() normalizes NoObjectType to OBJECT_TYPE
    assertEquals(objectType, ctor.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeRejections() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor = new FunctionType(
        registry, "RejectTest", null, arrow, null, null, true, false);

    // Null prototype rejection
    assertFalse(ctor.setPrototype(null));

    // Reject prototype if prototype == getInstanceType() on constructor
    FunctionPrototypeType cyclicProto = new FunctionPrototypeType(
        registry, ctor, ctor.getInstanceType());
    // Directly setting the instance type as prototype
    ctor.setInstanceType(cyclicProto);
    assertFalse(ctor.setPrototype(cyclicProto));
  }

  @Test(timeout = 4000)
  public void testDefinePropertyPrototypeHandling() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "DefPropTest", null, arrow, null, null, false, false);

    // 1. Defining "prototype" with a non-object type should return false
    assertFalse(fn.defineProperty("prototype", numberType, false, false));

    // 2. Defining "prototype" with identical/equivalent prototype should return true
    assertTrue(fn.defineProperty("prototype", fn.getPrototype(), false, false));

    // 3. Defining "prototype" with a new valid ObjectType
    FunctionPrototypeType newProto = new FunctionPrototypeType(registry, fn, objectType);
    assertTrue(fn.defineProperty("prototype", newProto, false, false));
    assertSame(newProto, fn.getPrototype());

    // 4. Defining a non-prototype property
    assertTrue(fn.defineProperty("regularProp", numberType, false, false));
    assertTrue(fn.hasProperty("regularProp"));
  }

  @Test(timeout = 4000)
  public void testHasPropertyAndInferredProperties() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "PropTest", null, arrow, null, null, false, false);

    assertTrue(fn.hasProperty("prototype"));
    assertTrue(fn.hasOwnProperty("prototype"));
    assertTrue(fn.isPropertyTypeInferred("prototype"));

    assertFalse(fn.hasProperty("nonExistent"));
    assertFalse(fn.hasOwnProperty("nonExistent"));
    assertFalse(fn.isPropertyTypeInferred("nonExistent"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testLazyCallPropertyWithNullParameters() {
    // Tests lazy definition of "call" when getParametersNode() is null
    ArrowType arrowNull = new ArrowType(registry, null, stringType);
    FunctionType fn = new FunctionType(
        registry, "nullParamsFn", null, arrowNull, null, null, false, false);

    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = (FunctionType) callProp;
    assertEquals(stringType, callFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testLazyCallAndApplyPropertyGeneration() {
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    builder.addRequiredParams(numberType);
    Node paramNode = builder.build();

    ArrowType arrow = new ArrowType(registry, paramNode, stringType);
    FunctionType fn = new FunctionType(
        registry, "worker", null, arrow, objectType, null, false, false);

    // Test lazy "call" property: prepends "thisType" as first optional parameter
    JSType callType = fn.getPropertyType("call");
    assertTrue(callType.isFunctionType());
    FunctionType callFn = (FunctionType) callType;
    assertEquals(stringType, callFn.getReturnType());
    Node callParams = callFn.getParametersNode();
    assertNotNull(callParams);
    assertEquals("thisType", callParams.getFirstChild().getString());
    assertTrue(callParams.getFirstChild().isOptionalArg());

    // Test lazy "apply" property: builder adds (thisType, Object)
    JSType applyType = fn.getPropertyType("apply");
    assertTrue(applyType.isFunctionType());
    FunctionType applyFn = (FunctionType) applyType;
    assertEquals(stringType, applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testLatticeSubtypesAndSupertypes() {
    ArrowType arrow1 = new ArrowType(registry, new Node(Token.LP), numberType, false);
    FunctionType fn1 = new FunctionType(
        registry, "fn1", null, arrow1, objectType, null, false, false);

    ArrowType arrow2 = new ArrowType(registry, new Node(Token.LP), stringType, true);
    FunctionType fn2 = new FunctionType(
        registry, "fn2", null, arrow2, objectType, null, false, false);

    // Equivalent join
    JSType selfJoin = fn1.getLeastSupertype(fn1);
    assertSame(fn1, selfJoin);

    // Least supertype of fn1 and fn2: union of return types, inferred propagated
    JSType leastSuper = fn1.getLeastSupertype(fn2);
    assertTrue(leastSuper.isFunctionType());
    FunctionType joinedFn = (FunctionType) leastSuper;
    assertTrue(joinedFn.isReturnTypeInferred());
    assertTrue(joinedFn.getReturnType().isUnionType());

    // Greatest subtype of fn1 and fn2: intersection of return types
    JSType greatestSub = fn1.getGreatestSubtype(fn2);
    assertTrue(greatestSub.isFunctionType());

    // Sup / Inf with FUNCTION_INSTANCE_TYPE
    assertSame(functionInstanceType, fn1.getLeastSupertype(functionInstanceType));
    assertSame(functionInstanceType, functionInstanceType.getLeastSupertype(fn1));
    assertSame(fn1, fn1.getGreatestSubtype(functionInstanceType));
    assertSame(fn1, functionInstanceType.getGreatestSubtype(fn1));

    // Sup / Inf across non-ordinary functions / mismatched signatures
    ArrowType ctorArrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor1 = new FunctionType(
        registry, "C1", null, ctorArrow, null, null, true, false);
    FunctionType ctor2 = new FunctionType(
        registry, "C2", null, ctorArrow, null, null, true, false);

    JSType ctorSup = ctor1.getLeastSupertype(ctor2);
    assertEquals(registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE), ctorSup);
    JSType ctorInf = ctor1.getGreatestSubtype(ctor2);
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), ctorInf);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRelations() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrow, null, null, false, false);
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);

    // Any function can be assigned to an interface
    assertTrue(fn.isSubtype(iface));
    // An interface cannot be assigned to another function
    assertFalse(iface.isSubtype(fn));

    // Union type subtyping
    JSType unionWithFn = registry.createUnionType(fn, stringType);
    assertTrue(fn.isSubtype(unionWithFn));

    JSType unionWithoutFn = registry.createUnionType(numberType, stringType);
    assertFalse(fn.isSubtype(unionWithoutFn));

    // Subtype of FUNCTION_PROTOTYPE or Object
    assertTrue(fn.isSubtype(objectType));
  }

  @Test(timeout = 4000)
  public void testToStringAndDebugHashCodeFormatting() {
    // 1. Native FUNCTION_INSTANCE_TYPE string
    assertEquals("Function", functionInstanceType.toString());

    // 2. Ordinary function without known this and with varargs
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    builder.addRequiredParams(numberType);
    builder.addVarArgs(registry.createUnionType(stringType, voidType));
    Node params = builder.build();

    ArrowType arrow = new ArrowType(registry, params, booleanType);
    FunctionType fn = new FunctionType(
        registry, "testStr", null, arrow, null, null, false, false);

    String str = fn.toString();
    assertTrue(str.startsWith("function (number, ...[string]): boolean"));

    // 3. Constructor with known this
    FunctionType ctor = new FunctionType(
        registry, "Foo", null, new ArrowType(registry, new Node(Token.LP), voidType),
        null, null, true, false);
    String ctorStr = ctor.toString();
    assertTrue(ctorStr.contains("this:Foo"));

    // 4. Debug hash code string recursion safeguard ("me")
    String debugStr = fn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.startsWith("function ("));

    String nativeDebug = functionInstanceType.toDebugHashCodeString();
    assertNotNull(nativeDebug);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorThrowsOnInvalidSourceNodeType() {
    Node invalidSource = new Node(Token.VAR);
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    new FunctionType(
        registry, "InvalidSrc", invalidSource, arrow, null, null, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorThrowsOnNullArrowType() {
    new FunctionType(registry, "NullArrow", null, null, null, null, false, false);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInterfaceThrowsOnNullName() {
    FunctionType.forInterface(registry, null, null);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInterfaceThrowsOnInvalidSourceNode() {
    Node invalidSource = new Node(Token.EXPR_RESULT);
    FunctionType.forInterface(registry, "BadIface", invalidSource);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetInstanceTypeThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "ord", null, arrow, null, null, false, false);
    fn.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "ord", null, arrow, null, null, false, false);
    fn.getSuperClassConstructor();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testHasUnknownSupertypeThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "ord", null, arrow, null, null, false, false);
    fn.hasUnknownSupertype();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetTopMostDefiningTypeThrowsOnOrdinaryFunction() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "ord", null, arrow, null, null, false, false);
    fn.getTopMostDefiningType("someProp");
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetTopMostDefiningTypeThrowsWhenPropertyNotFound() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, arrow, null, null, true, false);
    ctor.getTopMostDefiningType("missingProp");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testEquivalenceAndHashCodeContracts() {
    ArrowType arrow1 = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn1 = new FunctionType(
        registry, "f", null, arrow1, objectType, null, false, false);

    ArrowType arrow2 = new ArrowType(registry, new Node(Token.LP), numberType);
    FunctionType fn2 = new FunctionType(
        registry, "f", null, arrow2, objectType, null, false, false);

    ArrowType arrow3 = new ArrowType(registry, new Node(Token.LP), stringType);
    FunctionType fn3 = new FunctionType(
        registry, "f", null, arrow3, objectType, null, false, false);

    // Non-function equivalence
    assertFalse(fn1.isEquivalentTo(null));
    assertFalse(fn1.isEquivalentTo(numberType));

    // Ordinary function equivalence
    assertTrue(fn1.isEquivalentTo(fn2));
    assertEquals(fn1.hashCode(), fn2.hashCode());
    assertFalse(fn1.isEquivalentTo(fn3));
    assertTrue(fn1.hasEqualCallType(fn2));
    assertFalse(fn1.hasEqualCallType(fn3));

    // Constructor equivalence: reference equality only
    ArrowType ctorArrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctorA = new FunctionType(
        registry, "C", null, ctorArrow, null, null, true, false);
    FunctionType ctorB = new FunctionType(
        registry, "C", null, ctorArrow, null, null, true, false);
    assertTrue(ctorA.isEquivalentTo(ctorA));
    assertFalse(ctorA.isEquivalentTo(ctorB));
    assertFalse(ctorA.isEquivalentTo(fn1));
    assertFalse(fn1.isEquivalentTo(ctorA));

    // Interface equivalence: matched by reference name
    FunctionType iface1 = FunctionType.forInterface(registry, "ISame", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "ISame", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "IDifferent", null);

    assertTrue(iface1.isEquivalentTo(iface2));
    assertEquals(iface1.hashCode(), iface2.hashCode());
    assertFalse(iface1.isEquivalentTo(iface3));
    assertFalse(iface1.isEquivalentTo(fn1));
    assertFalse(fn1.isEquivalentTo(iface1));
  }

  @Test(timeout = 4000)
  public void testCloneWithNewReturnType() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), numberType, false);
    FunctionType fn = new FunctionType(
        registry, "original", null, arrow, objectType, null, false, false);

    FunctionType cloned = fn.cloneWithNewReturnType(stringType, true);
    assertFalse(cloned.isConstructor());
    assertFalse(cloned.isInterface());
    assertEquals(stringType, cloned.getReturnType());
    assertTrue(cloned.isReturnTypeInferred());
    assertEquals(objectType, cloned.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertypeDetection() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType ctor = new FunctionType(
        registry, "KnownHierarchy", null, arrow, null, null, true, false);

    // No supertype initially -> false
    assertFalse(ctor.hasUnknownSupertype());

    // Set implicit prototype to UNKNOWN_TYPE
    ctor.getPrototype().setImplicitPrototype(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    assertTrue(ctor.hasUnknownSupertype());
  }

  @Test(timeout = 4000)
  public void testSourceNodeAndInstanceTypeMutation() {
    Node fnNode = new Node(Token.FUNCTION);
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType fn = new FunctionType(
        registry, "SourceTest", fnNode, arrow, null, null, false, false);

    assertSame(fnNode, fn.getSource());
    Node newSource = new Node(Token.FUNCTION);
    fn.setSource(newSource);
    assertSame(newSource, fn.getSource());

    // Mutation of instance type for special native types
    fn.setInstanceType(objectType);
    assertEquals(objectType, fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testUniversalConstructorInstanceTypeCheck() {
    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    if (u2u instanceof FunctionType) {
      FunctionType u2uFn = (FunctionType) u2u;
      assertTrue(u2uFn.isInstanceType());
    }

    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType regularFn = new FunctionType(
        registry, "regular", null, arrow, null, null, false, false);
    assertFalse(regularFn.isInstanceType());
  }

  @Test(timeout = 4000)
  public void testResolveInternalWithSubTypesAndInterfaces() {
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), voidType);
    FunctionType superCtor = new FunctionType(
        registry, "Super", null, arrow, null, null, true, false);
    FunctionType subCtor = new FunctionType(
        registry, "Sub", null, arrow, null, null, true, false);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);
    subCtor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    SimpleErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = subCtor.resolve(reporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());

    JSType resolvedSuper = superCtor.resolve(reporter, null);
    assertNotNull(resolvedSuper);
  }
}