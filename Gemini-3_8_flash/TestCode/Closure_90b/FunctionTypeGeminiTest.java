/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor Flavors:
 *    - Kind.ORDINARY, Kind.CONSTRUCTOR, Kind.INTERFACE.
 *    - isConstructor(), isInterface(), isOrdinaryFunction(), isFunctionType(), canBeCalled().
 *    - source node == null vs. source.getType() == Token.FUNCTION vs. illegal node type (Preconditions check).
 * 2. Parameter Inspection & Boundaries:
 *    - getParameters(), getParametersNode() (null vs. non-null LP node).
 *    - getMinArguments() & getMaxArguments():
 *      * 0 arguments, required arguments only.
 *      * Optional arguments (Node.isOptionalArg) before/after required.
 *      * VarArgs (Node.isVarArgs) -> getMaxArguments returns Integer.MAX_VALUE.
 *      * Empty params node, params with no children.
 * 3. Prototypes & Subtyping Hierarchy:
 *    - getPrototype() lazy initialization.
 *    - setPrototype(null) -> false.
 *    - setPrototype(getInstanceType()) when isConstructor() -> false.
 *    - setPrototypeBasedOn(ObjectType).
 *    - superClass constructor registration and addSubType().
 *    - hasUnknownSupertype(): loop traversal, UnknownType super, no constructor super, cycle avoidance.
 *    - getTopMostDefiningType(prop): hierarchy traversal for defining type.
 * 4. Property Handling & Lazy Built-ins:
 *    - hasProperty("prototype") and hasOwnProperty("prototype").
 *    - getPropertyType("prototype").
 *    - getPropertyType("call") with null parameters vs. non-null cloned parameters (adds thisTypeNode).
 *    - getPropertyType("apply") with FunctionParamBuilder.
 *    - defineProperty("prototype", JSType, ...) with ObjectType, existing equivalent prototype, and non-object.
 * 5. Type Lattice (Least Supertype / Greatest Subtype):
 *    - supAndInfHelper(that, leastSuper):
 *      * Same instance / equivalent -> this.
 *      * Non-FunctionType -> delegate to super.
 *      * Both ordinary functions without unknown params/return:
 *        - One is subtype of another.
 *        - Component-wise merge via tryMergeFunctionPiecewise (equal params vs unequal params).
 *        - typeOfThis equivalence vs. supremum/infimum merge.
 *      * FUNCTION_INSTANCE_TYPE special cases.
 *      * Fallback to U2U_CONSTRUCTOR_TYPE / LEAST_FUNCTION_TYPE.
 * 6. Equality & Equivalence:
 *    - isEquivalentTo(): Non-FunctionType, Constructor vs Constructor (pointer eq),
 *      Interface vs Interface (referenceName eq), Ordinary vs Ordinary (typeOfThis & call eq).
 *    - hashCode(): Interface vs non-interface.
 * 7. Type Resolution & Backwards Typedef Defect:
 *    - resolveInternal(ErrorReporter, StaticScope):
 *      * Resolves call (ArrowType), prototype, typeOfThis (ObjectType vs non-ObjectType),
 *        implementedInterfaces, and subTypes.
 *      * Defect Targeting (testBackwardsTypedefUse8, testBackwardsTypedefUse9):
 *        Resolving function signatures involving NamedType / backwards typedef references
 *        where type resolution must correctly bind the resolved parameter/return types
 *        and allow accurate subtype checking (e.g. function(): number vs function(): string).
 */

package com.google.javascript.rhino.jstype;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private Scope mockScope;

  private static class SimpleErrorReporter implements ErrorReporter {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {}
    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {}
  }

  private static class SimpleStaticScope implements StaticScope<JSType> {
    private final java.util.Map<String, StaticSlot<JSType>> slots = new java.util.HashMap<>();

    void addSlot(String name, JSType type) {
      slots.put(name, new SimpleSlot(name, type, true));
    }

    @Override
    public Node getRootNode() { return null; }
    @Override
    public StaticScope<JSType> getParentScope() { return null; }
    @Override
    public StaticSlot<JSType> getSlot(String name) { return slots.get(name); }
    @Override
    public StaticSlot<JSType> getOwnSlot(String name) { return slots.get(name); }
    @Override
    public JSType getTypeOfThis() { return null; }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testOrdinaryFunctionCreationAndProperties() {
    Node fnNode = new Node(Token.FUNCTION);
    Node paramsNode = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, paramsNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    FunctionType fn = new FunctionType(
        registry, "foo", fnNode, arrow, null, null, false, false);

    assertTrue(fn.isOrdinaryFunction());
    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertFalse(fn.isInstanceType());
    assertEquals("foo", fn.getName());
    assertEquals(fnNode, fn.getSource());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
    assertEquals(0, fn.getMinArguments());
    assertEquals(0, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testConstructorCreationAndInstanceType() {
    Node params = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, params, null);
    FunctionType ctor = new FunctionType(
        registry, "MyClass", null, arrow, null, "T", true, false);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals("T", ctor.getTemplateTypeName());

    // Prototype lazy initialization
    FunctionPrototypeType proto = ctor.getPrototype();
    assertNotNull(proto);
    assertEquals(proto, ctor.getPropertyType("prototype"));
    assertTrue(ctor.hasProperty("prototype"));
    assertTrue(ctor.hasOwnProperty("prototype"));
    assertTrue(ctor.isPropertyTypeInferred("prototype"));
  }

  @Test(timeout = 4000)
  public void testInterfaceCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", fnNode);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("MyInterface", iface.getReferenceName());
    assertEquals(fnNode, iface.getSource());
  }

  @Test(timeout = 4000)
  public void testPrototypeManipulation() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, null);
    assertFalse(ctor.setPrototype(null));

    // Setting prototype to its own instance type should fail
    assertFalse(ctor.setPrototype(ctor.getInstanceType()));

    // Set prototype based on another object
    ObjectType baseObj = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ctor.setPrototypeBasedOn(baseObj);
    assertNotNull(ctor.getPrototype());
    assertEquals(baseObj, ctor.getPrototype().getImplicitPrototype());

    // Second call updating implicit prototype
    ObjectType newBase = new PrototypeObjectType(registry, "CustomBase", null);
    ctor.setPrototypeBasedOn(newBase);
    assertEquals(newBase, ctor.getPrototype().getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testSubClassAndSuperClassRegistration() {
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);

    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    assertEquals(superCtor, subCtor.getSuperClassConstructor());
    List<FunctionType> subTypes = superCtor.getSubTypes();
    assertNotNull(subTypes);
    assertTrue(subTypes.contains(subCtor));
  }

  @Test(timeout = 4000)
  public void testTopMostDefiningType() {
    FunctionType grandParent = registry.createConstructorType("GrandParent", null, null, null);
    grandParent.getPrototype().defineProperty("prop", registry.getNativeType(JSTypeNative.STRING_TYPE), false, false);

    FunctionType parent = registry.createConstructorType("Parent", null, null, null);
    parent.setPrototypeBasedOn(grandParent.getInstanceType());

    FunctionType child = registry.createConstructorType("Child", null, null, null);
    child.setPrototypeBasedOn(parent.getInstanceType());

    JSType top = child.getTopMostDefiningType("prop");
    assertEquals(grandParent.getInstanceType(), top);
  }

  @Test(timeout = 4000)
  public void testGetAllImplementedInterfaces() {
    FunctionType ifaceParent = FunctionType.forInterface(registry, "ParentIface", null);
    FunctionType ifaceChild = FunctionType.forInterface(registry, "ChildIface", null);
    ifaceChild.setPrototypeBasedOn(ifaceParent.getInstanceType());

    FunctionType ctor = registry.createConstructorType("ClassA", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(ifaceChild.getInstanceType()));

    Iterable<ObjectType> allInterfaces = ctor.getAllImplementedInterfaces();
    Set<ObjectType> set = com.google.common.collect.Sets.newHashSet(allInterfaces);
    assertEquals(2, set.size());
    assertTrue(set.contains(ifaceChild.getInstanceType()));
    assertTrue(set.contains(ifaceParent.getInstanceType()));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentsBoundaryCalculations() {
    // Zero arguments
    Node emptyParams = new Node(Token.LP);
    FunctionType fn0 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), emptyParams);
    assertEquals(0, fn0.getMinArguments());
    assertEquals(0, fn0.getMaxArguments());

    // Normal + Optional arguments
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    p2.setOptionalArg(true);
    Node normalParams = new Node(Token.LP, p1, p2);

    FunctionType fnNorm = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), normalParams);
    assertEquals(1, fnNorm.getMinArguments());
    assertEquals(2, fnNorm.getMaxArguments());

    // VarArgs arguments -> Max should be Integer.MAX_VALUE
    Node pVar = Node.newString(Token.NAME, "rest");
    pVar.setJSType(registry.getNativeType(JSTypeNative.ALL_TYPE));
    pVar.setVarArgs(true);
    Node varParams = new Node(Token.LP, p1.cloneNode(), pVar);

    FunctionType fnVar = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), varParams);
    assertEquals(1, fnVar.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnVar.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testNullParametersNodeBoundary() {
    ArrowType arrowNoParams = new ArrowType(
        registry, null, registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType fn = new FunctionType(
        registry, "noParams", null, arrowNoParams, null, null, false, false);

    assertNull(fn.getParametersNode());
    assertNotNull(fn.getParameters());
    assertFalse(fn.getParameters().iterator().hasNext());
    assertEquals(0, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testPropertyCallAndApplyLazyDefinition() {
    Node paramA = Node.newString(Token.NAME, "arg");
    paramA.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node params = new Node(Token.LP, paramA);
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), params);

    // "call" property lazy generation
    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp instanceof FunctionType);
    FunctionType callFn = (FunctionType) callProp;
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), callFn.getReturnType());

    // "apply" property lazy generation
    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp instanceof FunctionType);
    FunctionType applyFn = (FunctionType) applyProp;
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyPrototypeBranch() {
    FunctionType ctor = registry.createConstructorType("Widget", null, null, null);
    ObjectType protoObj = new PrototypeObjectType(registry, "WidgetProto", null);

    // Initial definition of prototype property
    boolean defined = ctor.defineProperty("prototype", protoObj, false, false);
    assertTrue(defined);

    // Defining same prototype again returns true
    boolean definedAgain = ctor.defineProperty("prototype", ctor.getPrototype(), false, false);
    assertTrue(definedAgain);

    // Defining non-object type as prototype returns false
    boolean definedPrimitive = ctor.defineProperty(
        "prototype", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, false);
    assertFalse(definedPrimitive);
  }

  @Test(timeout = 4000)
  public void testSupAndInfHelperBranches() {
    FunctionType fnA = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fnB = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    // Least supertype of equivalent functions
    JSType sup = fnA.getLeastSupertype(fnA);
    assertEquals(fnA, sup);

    // Sup with non-function type delegates to super
    JSType nonFn = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertNotNull(fnA.getLeastSupertype(nonFn));
    assertNotNull(fnA.getGreatestSubtype(nonFn));

    // FUNCTION_INSTANCE_TYPE boundaries
    JSType fnInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals(fnInstance, fnA.getLeastSupertype(fnInstance));
    assertEquals(fnA, fnA.getGreatestSubtype(fnInstance));
    assertEquals(fnInstance, fnInstance.getLeastSupertype(fnA));
    assertEquals(fnA, fnInstance.getGreatestSubtype(fnA));
  }

  @Test(timeout = 4000)
  public void testTryMergeFunctionPiecewise() {
    Node param1 = Node.newString(Token.NAME, "x");
    param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node lp1 = new Node(Token.LP, param1);

    Node param2 = Node.newString(Token.NAME, "x");
    param2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node lp2 = new Node(Token.LP, param2);

    FunctionType fn1 = registry.createFunctionTypeWithVarArgs(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), lp1);
    FunctionType fn2 = registry.createFunctionTypeWithVarArgs(
        registry.getNativeType(JSTypeNative.STRING_TYPE), lp2);

    JSType mergedSup = fn1.getLeastSupertype(fn2);
    assertTrue(mergedSup instanceof FunctionType);
    FunctionType mergedSupFn = (FunctionType) mergedSup;
    assertTrue(mergedSupFn.getReturnType().isUnionType());

    JSType mergedInf = fn1.getGreatestSubtype(fn2);
    assertTrue(mergedInf instanceof FunctionType);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Backwards Typedef & Named Type Resolution)
  // Ground truth: testBackwardsTypedefUse8 & testBackwardsTypedefUse9
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectBackwardsTypedefUseInFunctionType() {
    SimpleStaticScope scope = new SimpleStaticScope();
    ErrorReporter reporter = new SimpleErrorReporter();

    // Create a NamedType referring to "TypeDef" which is defined later
    NamedType backwardsNamedType = new NamedType(registry, "TypeDef", "test.js", 1, 1);

    // Target Function: function(TypeDef): void
    Node paramNode = Node.newString(Token.NAME, "x");
    paramNode.setJSType(backwardsNamedType);
    Node params = new Node(Token.LP, paramNode);
    ArrowType gArrow = new ArrowType(
        registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType gFn = new FunctionType(
        registry, "g", null, gArrow, null, null, false, false);

    // Underlying Typedef: function(): number
    FunctionType actualTypeDefFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    scope.addSlot("TypeDef", actualTypeDefFn);

    // Resolve the function type in scope containing the typedef
    JSType resolvedGFn = gFn.resolve(reporter, scope);
    assertTrue(resolvedGFn instanceof FunctionType);
    FunctionType resolvedFunction = (FunctionType) resolvedGFn;

    Node resolvedParam = resolvedFunction.getParametersNode().getFirstChild();
    assertNotNull(resolvedParam);
    JSType resolvedParamType = resolvedParam.getJSType();

    // The named type parameter should resolve directly or transitively to actualTypeDefFn
    assertTrue(
        "Param type must be equivalent to function(): number",
        resolvedParamType.isEquivalentTo(actualTypeDefFn) ||
        (resolvedParamType instanceof NamedType &&
            ((NamedType) resolvedParamType).getReferencedType().isEquivalentTo(actualTypeDefFn))
    );

    // Verify compatibility matching:
    // Arg 1: function(): number -> Should match formal parameter (testBackwardsTypedefUse8)
    FunctionType argMatches = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertTrue(
        "function(): number must be a valid subtype of resolved parameter type",
        argMatches.isSubtype(resolvedParamType));

    // Arg 2: function(): string -> Should NOT match formal parameter (testBackwardsTypedefUse9)
    FunctionType argMismatches = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(
        "function(): string must NOT be a subtype of resolved parameter type",
        argMismatches.isSubtype(resolvedParamType));
  }

  @Test(timeout = 4000)
  public void testResolveInternalWithInterfacesAndSubTypes() {
    SimpleStaticScope scope = new SimpleStaticScope();
    ErrorReporter reporter = new SimpleErrorReporter();

    FunctionType iface = FunctionType.forInterface(registry, "IInterface", null);
    FunctionType ctor = registry.createConstructorType("Impl", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    FunctionType subCtor = registry.createConstructorType("SubImpl", null, null, null);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());

    JSType resolved = ctor.resolve(reporter, scope);
    assertEquals(ctor, resolved);
    assertTrue(ctor.isResolved());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorWithInvalidSourceNodeTypeThrows() {
    Node invalidNode = new Node(Token.VAR);
    ArrowType arrow = new ArrowType(registry, new Node(Token.LP), null);
    new FunctionType(registry, "Bad", invalidNode, arrow, null, null, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorWithNullArrowThrows() {
    new FunctionType(registry, "NullArrow", null, null, null, null, false, false);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testForInterfaceWithNullNameThrows() {
    FunctionType.forInterface(registry, null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType ordinary = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    ordinary.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorOnOrdinaryFunctionThrows() {
    FunctionType ordinary = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    ordinary.getSuperClassConstructor();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testHasUnknownSupertypeOnOrdinaryFunctionThrows() {
    FunctionType ordinary = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    ordinary.hasUnknownSupertype();
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Subtyping, HashCode & Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testEquivalenceAndHashCodeContracts() {
    FunctionType fn1 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    assertTrue(fn1.isEquivalentTo(fn2));
    assertEquals(fn1.hashCode(), fn2.hashCode());
    assertTrue(fn1.hasEqualCallType(fn2));

    FunctionType iface1 = FunctionType.forInterface(registry, "CommonIface", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "CommonIface", null);
    FunctionType ifaceOther = FunctionType.forInterface(registry, "OtherIface", null);

    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(ifaceOther));
    assertEquals(iface1.hashCode(), iface2.hashCode());

    assertFalse(fn1.isEquivalentTo(iface1));
    assertFalse(iface1.isEquivalentTo(fn1));
    assertFalse(fn1.isEquivalentTo(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));

    FunctionType ctor1 = registry.createConstructorType("Ctor1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("Ctor2", null, null, null);
    assertFalse(ctor1.isEquivalentTo(ctor2));
    assertTrue(ctor1.isEquivalentTo(ctor1));
  }

  @Test(timeout = 4000)
  public void testSubtypingBetweenFunctionsAndInterfaces() {
    FunctionType ordinary = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType iface = FunctionType.forInterface(registry, "I", null);

    // Any function can be assigned to an interface
    assertTrue(ordinary.isSubtype(iface));
    // An interface cannot be assigned to an ordinary function
    assertFalse(iface.isSubtype(ordinary));
  }

  @Test(timeout = 4000)
  public void testToStringAndDebugHashCodeString() {
    Node paramA = Node.newString(Token.NAME, "paramA");
    paramA.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node paramVarArgs = Node.newString(Token.NAME, "varArgs");
    paramVarArgs.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    paramVarArgs.setVarArgs(true);

    Node params = new Node(Token.LP, paramA, paramVarArgs);
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), params);

    String str = fn.toString();
    assertTrue(str.startsWith("function ("));
    assertTrue(str.contains("string"));
    assertTrue(str.contains("...[number]"));
    assertTrue(str.endsWith(": boolean"));

    String debugStr = fn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.startsWith("function ("));

    // Native FUNCTION_INSTANCE_TYPE special toString()
    JSType fnNative = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnNative.toString());
  }

  @Test(timeout = 4000)
  public void testVisitorPattern() {
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE));

    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enum"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "visitedFunctionType"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "num"; }
      @Override public String caseStringType() { return "str"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    };

    assertEquals("visitedFunctionType", fn.visit(visitor));
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertypeEvaluations() {
    FunctionType ctor = registry.createConstructorType("SimpleClass", null, null, null);
    assertFalse(ctor.hasUnknownSupertype());

    FunctionType superUnknown = registry.createConstructorType("SuperUnknown", null, null, null);
    superUnknown.setPrototypeBasedOn(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    assertTrue(superUnknown.hasUnknownSupertype());
  }

  @Test(timeout = 4000)
  public void testCachedValuesAndSourceAccessors() {
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertFalse(fn.hasCachedValues());

    // Accessing prototype caches it
    fn.getPrototype();
    assertTrue(fn.hasCachedValues());

    Node src = new Node(Token.FUNCTION);
    fn.setSource(src);
    assertEquals(src, fn.getSource());
  }
}