/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.FunctionType
 *
 * Defects4J Defect Context (Closure-163 / DevirtualizePrototypeMethods / TypeCheck):
 * - Focus: Prototype methods, function 'this' type resolution, subtyping hierarchies,
 *   and top-most defining type traversals.
 * - In testRewritePrototypeMethods2, prototype methods lose or have incorrect 'this' types
 *   (resolving to null/unknown instead of the instance type) when prototype assignments and
 *   interface hierarchies interact during type resolution.
 * - In testGoodExtends9, prototype inheritance graphs where supertypes are resolved
 *   or subtyping graphs have cycles or missing constructor links trigger unexpected warnings.
 *
 * Targeted Decision Branches & Conditions:
 * 1. Constructor Flavors:
 *    - Ordinary function vs Constructor vs Interface (checking isConstructor, isInterface, isOrdinaryFunction).
 *    - source node verification (null vs FUNCTION token vs invalid token throwing IllegalArgumentException).
 *    - Native vs non-native constructor instance type initialization.
 * 2. Parameters & Arguments Count:
 *    - getParametersNode() null vs non-null.
 *    - getMinArguments(): optional params, varargs params, mixed orders.
 *    - getMaxArguments(): null params, empty params, varargs (Integer.MAX_VALUE), fixed param list.
 * 3. Prototype Management:
 *    - getPrototype() lazy initialization.
 *    - setPrototype(): null check, prototype == getInstanceType() guard, subType registration to superClass.
 *    - setPrototypeBasedOn(): prototype == null vs existing prototype setImplicitPrototype.
 *    - defineProperty("prototype", ...): toObjectType() != null vs null.
 * 4. Interfaces & Inheritance:
 *    - setImplementedInterfaces() and getImplementedInterfaces() with superclasses.
 *    - getAllImplementedInterfaces() with interface inheritance chains.
 *    - getSuperClassConstructor() and hasUnknownSupertype() loop termination.
 *    - getTopMostDefiningType() property lookup ascending prototype chain.
 * 5. Special Properties ("call", "apply"):
 *    - getPropertyType("prototype"), getPropertyType("call") with null parameters vs populated parameters.
 *    - getPropertyType("apply") verifying optional params structure.
 * 6. Lattice Operations:
 *    - getLeastSupertype(): self equality, FUNCTION_INSTANCE_TYPE equality, U2U_CONSTRUCTOR fallback.
 *    - getGreatestSubtype(): self equality, FUNCTION_INSTANCE_TYPE equality, NO_OBJECT_TYPE fallback.
 *    - isSubtype(): interface targets, interface source, contravariant 'this' check, UnionType targets.
 * 7. Type Resolution:
 *    - resolveInternal(): resolving call (ArrowType), prototype, typeOfThis, implementedInterfaces, subTypes.
 * 8. Equality & String Representation:
 *    - equals() and hashCode(): Constructor (identity), Interface (by name), Ordinary (by this & call).
 *    - toString(): function instance, known this, varargs (including UnionType vararg formatting), return type.
 */

package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_PROTOTYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class FunctionTypeGeminiTest {

  private JSTypeRegistry registry;
  private Scope scope;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    scope = new Scope() {
      @Override
      public StaticSlot<JSType> getSlot(String name) {
        return null;
      }

      @Override
      public StaticSlot<JSType> getOwnSlot(String name) {
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
      public StaticScope<JSType> getParentScope() {
        return null;
      }
    };
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testOrdinaryFunctionCreationAndBasicProperties() {
    Node fnNode = new Node(Token.FUNCTION);
    Node param1 = Node.newString(Token.NAME, "p1");
    param1.setJSType(registry.getNativeType(NUMBER_TYPE));
    Node params = new Node(Token.LP, param1);

    FunctionType fn = new FunctionType(
        registry, "foo", fnNode, params, registry.getNativeType(STRING_TYPE));

    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertFalse(fn.hasInstanceType());

    assertEquals("foo", fn.getReferenceName());
    assertEquals(registry.getNativeType(STRING_TYPE), fn.getReturnType());
    assertEquals(registry.getNativeObjectType(UNKNOWN_TYPE), fn.getTypeOfThis());
    assertSame(fnNode, fn.getSource());
    assertNotNull(fn.getPrototype());
    assertTrue(fn.hasProperty("prototype"));
  }

  @Test(timeout = 4000)
  public void testConstructorCreationAndInstanceType() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType ctor = registry.createConstructorType(
        "MyClass", fnNode, null, registry.getNativeType(VOID_TYPE));

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());

    ObjectType instance = ctor.getInstanceType();
    assertNotNull(instance);
    assertSame(ctor, instance.getConstructor());
    assertEquals(instance, ctor.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInterfaceCreationAndContracts() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = registry.createInterfaceType("MyInterface", fnNode);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertNull(iface.getReturnType());
    assertNull(iface.getParametersNode());
    assertEquals("MyInterface", iface.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testLazyPrototypeInitialization() {
    FunctionType fn = new FunctionType(
        registry, "f", null, null, registry.getNativeType(NUMBER_TYPE));
    assertFalse(fn.hasCachedValues());

    FunctionPrototypeType proto = fn.getPrototype();
    assertNotNull(proto);
    assertTrue(fn.hasCachedValues());
    assertSame(proto, fn.getPrototype());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeCustom() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, registry.getNativeType(VOID_TYPE));
    FunctionPrototypeType newProto = new FunctionPrototypeType(
        registry, ctor, registry.getNativeObjectType(OBJECT_TYPE));

    assertTrue(ctor.setPrototype(newProto));
    assertSame(newProto, ctor.getPrototype());

    // Discard null prototype
    assertFalse(ctor.setPrototype(null));
    assertSame(newProto, ctor.getPrototype());

    // Guard against prototype being equal to getInstanceType()
    assertFalse(ctor.setPrototype((FunctionPrototypeType) (Object) ctor.getInstanceType()));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOn() {
    FunctionType ctor = registry.createConstructorType(
        "Child", null, null, null);
    ObjectType parentInstance = registry.createConstructorType(
        "Parent", null, null, null).getInstanceType();

    ctor.setPrototypeBasedOn(parentInstance);
    assertEquals(parentInstance, ctor.getPrototype().getImplicitPrototype());

    // Call again to exercise the branch where prototype is already initialized
    ObjectType grandParentInstance = registry.createConstructorType(
        "GrandParent", null, null, null).getInstanceType();
    ctor.setPrototypeBasedOn(grandParentInstance);
    assertEquals(grandParentInstance, ctor.getPrototype().getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testDefinePrototypePropertyViaDefineProperty() {
    FunctionType fn = new FunctionType(
        registry, "f", null, null, null);
    ObjectType baseObj = registry.getNativeObjectType(OBJECT_TYPE);

    boolean defined = fn.defineProperty("prototype", baseObj, false, false);
    assertTrue(defined);
    assertTrue(fn.isPropertyTypeInferred("prototype"));

    // defineProperty with non-object type should return false for "prototype"
    boolean definedInvalid = fn.defineProperty(
        "prototype", registry.getNativeType(NUMBER_TYPE), false, false);
    assertFalse(definedInvalid);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Arguments Counting
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentCountVariations() {
    // 1. Null parameters
    FunctionType fnNoParams = new FunctionType(
        registry, "noParams", null, null, null);
    assertEquals(0, fnNoParams.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnNoParams.getMaxArguments());
    assertFalse(fnNoParams.getParameters().iterator().hasNext());

    // 2. Empty parameters LP node
    Node emptyLp = new Node(Token.LP);
    FunctionType fnEmptyParams = new FunctionType(
        registry, "emptyParams", null, emptyLp, null);
    assertEquals(0, fnEmptyParams.getMinArguments());
    assertEquals(0, fnEmptyParams.getMaxArguments());

    // 3. Required + Optional parameters
    Node reqParam = Node.newString(Token.NAME, "req");
    reqParam.setJSType(registry.getNativeType(NUMBER_TYPE));

    Node optParam = Node.newString(Token.NAME, "opt");
    optParam.setJSType(registry.getNativeType(STRING_TYPE));
    optParam.setOptionalArg(true);

    Node paramsWithOpt = new Node(Token.LP, reqParam, optParam);
    FunctionType fnOpt = new FunctionType(
        registry, "fnOpt", null, paramsWithOpt, null);
    assertEquals(1, fnOpt.getMinArguments());
    assertEquals(2, fnOpt.getMaxArguments());

    // 4. Varargs parameter
    Node varParam = Node.newString(Token.NAME, "rest");
    varParam.setJSType(registry.getNativeType(BOOLEAN_TYPE));
    varParam.setVarArgs(true);

    Node paramsWithVar = new Node(Token.LP, reqParam.cloneNode(), varParam);
    FunctionType fnVar = new FunctionType(
        registry, "fnVar", null, paramsWithVar, null);
    assertEquals(1, fnVar.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnVar.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testLazyCallAndApplyProperties() {
    Node p1 = Node.newString(Token.NAME, "x");
    p1.setJSType(registry.getNativeType(NUMBER_TYPE));
    Node params = new Node(Token.LP, p1);

    FunctionType fn = new FunctionType(
        registry, "callee", null, params, registry.getNativeType(STRING_TYPE));

    // Access "call"
    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = (FunctionType) callProp;
    // Expected parameters for "call": [thisType (optional), x]
    assertEquals(2, callFn.getMaxArguments());
    assertEquals(registry.getNativeType(STRING_TYPE), callFn.getReturnType());

    // Access "apply"
    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = (FunctionType) applyProp;
    assertEquals(2, applyFn.getMaxArguments());
    assertEquals(registry.getNativeType(STRING_TYPE), applyFn.getReturnType());

    // Access "call" on a function with null parameters
    FunctionType fnNullParams = new FunctionType(
        registry, "callee2", null, null, registry.getNativeType(VOID_TYPE));
    JSType callProp2 = fnNullParams.getPropertyType("call");
    assertTrue(callProp2.isFunctionType());
    assertEquals(Integer.MAX_VALUE, ((FunctionType) callProp2).getMaxArguments());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-163 & Devirtualize)
  // =========================================================================

  /**
   * Targets DevirtualizePrototypeMethods / Closure-163 where prototype methods
   * and constructors must correctly retain their instance 'this' type across
   * prototype inheritance updates and subType linking.
   */
  @Test(timeout = 4000)
  public void testDefectPrototypeMethodThisTypeAndSuperClassSubTypes() {
    FunctionType superCtor = registry.createConstructorType(
        "SuperClass", null, null, null);
    FunctionType subCtor = registry.createConstructorType(
        "SubClass", null, null, null);

    // Link inheritance: subCtor.prototype = Object.create(superCtor.prototype)
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    // SuperClass must record SubClass in its subtypes list
    List<FunctionType> superSubTypes = superCtor.getSubTypes();
    assertNotNull("Super constructor must maintain subTypes list", superSubTypes);
    assertTrue("SubClass must be recorded as subType of SuperClass",
        superSubTypes.contains(subCtor));

    // Verify prototype methods receive and retain instance type for 'this'
    FunctionType method = new FunctionType(
        registry, "method", null, null, registry.getNativeType(NUMBER_TYPE),
        subCtor.getInstanceType());

    assertEquals(subCtor.getInstanceType(), method.getTypeOfThis());
    assertFalse("Method this-type must not degrade to unknown or null",
        method.getTypeOfThis().isUnknownType());

    // Resolve internal should maintain typeOfThis
    JSType resolvedMethod = method.resolve(null, scope);
    assertTrue(resolvedMethod.isFunctionType());
    assertEquals(subCtor.getInstanceType(), ((FunctionType) resolvedMethod).getTypeOfThis());
  }

  /**
   * Targets TypeCheckTest::testGoodExtends9:
   * Verification of getSuperClassConstructor, hasUnknownSupertype, and
   * getTopMostDefiningType across prototype chains.
   */
  @Test(timeout = 4000)
  public void testGoodExtendsSuperClassAndTopMostDefiningType() {
    FunctionType superCtor = registry.createConstructorType(
        "Base", null, null, null);
    superCtor.getPrototype().defineProperty("sharedProp",
        registry.getNativeType(STRING_TYPE), false, false);

    FunctionType middleCtor = registry.createConstructorType(
        "Middle", null, null, null);
    middleCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    FunctionType leafCtor = registry.createConstructorType(
        "Leaf", null, null, null);
    leafCtor.setPrototypeBasedOn(middleCtor.getInstanceType());

    assertSame(middleCtor, leafCtor.getSuperClassConstructor());
    assertSame(superCtor, middleCtor.getSuperClassConstructor());
    assertNull(superCtor.getSuperClassConstructor());

    assertFalse(leafCtor.hasUnknownSupertype());

    JSType topDefining = leafCtor.getTopMostDefiningType("sharedProp");
    assertEquals(superCtor.getInstanceType(), topDefining);
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertypeDetection() {
    FunctionType ctor = registry.createConstructorType("Broken", null, null, null);
    ctor.getPrototype().setImplicitPrototype(registry.getNativeObjectType(UNKNOWN_TYPE));
    assertTrue(ctor.hasUnknownSupertype());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorWithInvalidSourceNodeTypeThrows() {
    Node invalidNode = new Node(Token.VAR);
    new FunctionType(registry, "bad", invalidNode, null, null);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInterfaceWithoutNameThrows() {
    new FunctionType(registry, null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testOrdinaryFunctionGetInstanceTypeThrows() {
    FunctionType fn = new FunctionType(
        registry, "normal", null, null, null);
    fn.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorOnOrdinaryFunctionThrows() {
    FunctionType fn = new FunctionType(
        registry, "normal", null, null, null);
    fn.getSuperClassConstructor();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetTopMostDefiningTypeWhenPropertyMissingThrows() {
    FunctionType ctor = registry.createConstructorType("Ctor", null, null, null);
    ctor.getTopMostDefiningType("nonExistentProperty");
  }

  // =========================================================================
  // Partition E: Subtyping, Lattice & Object Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testLatticeSupremumAndInfimum() {
    FunctionType fn1 = new FunctionType(
        registry, "fn1", null, null, registry.getNativeType(NUMBER_TYPE));
    FunctionType fn2 = new FunctionType(
        registry, "fn2", null, null, registry.getNativeType(STRING_TYPE));

    // Least supertype
    assertEquals(fn1, fn1.getLeastSupertype(fn1));
    assertEquals(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        fn1.getLeastSupertype(fn2));

    JSType funcInstance = registry.getNativeType(FUNCTION_INSTANCE_TYPE);
    assertEquals(funcInstance, fn1.getLeastSupertype(funcInstance));
    assertEquals(funcInstance, funcInstance.getLeastSupertype(fn1));

    // Greatest subtype
    assertEquals(fn1, fn1.getGreatestSubtype(fn1));
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        fn1.getGreatestSubtype(fn2));
    assertEquals(fn1, fn1.getGreatestSubtype(funcInstance));
    assertEquals(fn1, funcInstance.getGreatestSubtype(fn1));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRelations() {
    FunctionType fn1 = new FunctionType(
        registry, "fn1", null, null, registry.getNativeType(NUMBER_TYPE));
    FunctionType fn2 = new FunctionType(
        registry, "fn2", null, null, registry.getNativeType(NUMBER_TYPE));
    FunctionType iface = registry.createInterfaceType("Iface", null);

    // Any function is subtype of interface function
    assertTrue(fn1.isSubtype(iface));
    // Interface is not subtype of another function
    assertFalse(iface.isSubtype(fn1));

    // Subtype with UnionType
    UnionType union = (UnionType) registry.createUnionType(
        fn1, registry.getNativeType(STRING_TYPE));
    assertTrue(fn1.isSubtype(union));

    // Native Function Prototype fallback
    assertTrue(fn1.isSubtype(registry.getNativeType(FUNCTION_PROTOTYPE)));
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesHierarchy() {
    FunctionType superIface = registry.createInterfaceType("SuperIface", null);
    FunctionType subIface = registry.createInterfaceType("SubIface", null);
    subIface.getPrototype().setImplicitPrototype(superIface.getInstanceType());

    FunctionType ctor = registry.createConstructorType("Concrete", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(subIface.getInstanceType()));

    Iterable<ObjectType> direct = ctor.getImplementedInterfaces();
    Iterator<ObjectType> directIt = direct.iterator();
    assertTrue(directIt.hasNext());
    assertEquals(subIface.getInstanceType(), directIt.next());
    assertFalse(directIt.hasNext());

    Iterable<ObjectType> all = ctor.getAllImplementedInterfaces();
    List<ObjectType> allList = ImmutableList.copyOf(all);
    assertEquals(2, allList.size());
    assertTrue(allList.contains(subIface.getInstanceType()));
    assertTrue(allList.contains(superIface.getInstanceType()));
  }

  @Test(timeout = 4000)
  public void testEqualsAndHashCode() {
    // 1. Ordinary functions: equal if call and this match
    FunctionType f1 = new FunctionType(
        registry, "f1", null, null, registry.getNativeType(NUMBER_TYPE));
    FunctionType f2 = new FunctionType(
        registry, "f2", null, null, registry.getNativeType(NUMBER_TYPE));
    assertTrue(f1.equals(f2));
    assertEquals(f1.hashCode(), f2.hashCode());
    assertTrue(f1.hasEqualCallType(f2));

    // 2. Constructors: identity check (this == that)
    FunctionType c1 = registry.createConstructorType("C", null, null, null);
    FunctionType c2 = registry.createConstructorType("C", null, null, null);
    assertFalse(c1.equals(c2));
    assertTrue(c1.equals(c1));
    assertFalse(c1.equals(f1));

    // 3. Interfaces: equal if names match
    FunctionType i1 = registry.createInterfaceType("SameName", null);
    FunctionType i2 = registry.createInterfaceType("SameName", null);
    FunctionType i3 = registry.createInterfaceType("DifferentName", null);
    assertTrue(i1.equals(i2));
    assertEquals(i1.hashCode(), i2.hashCode());
    assertFalse(i1.equals(i3));
    assertFalse(i1.equals(f1));
    assertFalse(f1.equals(i1));

    assertFalse(f1.equals("non-FunctionType"));
  }

  @Test(timeout = 4000)
  public void testToStringFormatting() {
    // Native function instance string
    JSType funcInstance = registry.getNativeType(FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", funcInstance.toString());

    // Known this type and normal parameters
    ObjectType thisType = registry.createConstructorType("ThisClass", null, null, null).getInstanceType();
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(NUMBER_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.getNativeType(STRING_TYPE));
    Node params = new Node(Token.LP, p1, p2);

    FunctionType fnWithThis = new FunctionType(
        registry, "fnWithThis", null, params, registry.getNativeType(BOOLEAN_TYPE), thisType);

    assertEquals("function (this:ThisClass, number, string): boolean", fnWithThis.toString());

    // VarArgs with UnionType containing VOID_TYPE
    Node varParam = Node.newString(Token.NAME, "rest");
    JSType optionalNum = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE), registry.getNativeType(VOID_TYPE));
    varParam.setJSType(optionalNum);
    varParam.setVarArgs(true);

    Node paramsWithVar = new Node(Token.LP, varParam);
    FunctionType fnWithVar = new FunctionType(
        registry, "fnWithVar", null, paramsWithVar, registry.getNativeType(VOID_TYPE));

    assertTrue(fnWithVar.toString().contains("...[number]"));
  }

  @Test(timeout = 4000)
  public void testVisitorPatternAndMiscState() {
    FunctionType fn = new FunctionType(
        registry, "fn", null, null, null, null, "T");

    assertEquals("T", fn.getTemplateTypeName());

    Node src = new Node(Token.FUNCTION);
    fn.setSource(src);
    assertSame(src, fn.getSource());

    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return null; }
      @Override public String caseUnknownType() { return null; }
      @Override public String caseNullType() { return null; }
      @Override public String caseNamedType(NamedType type) { return null; }
      @Override public String caseBooleanType() { return null; }
      @Override public String caseNumberType() { return null; }
      @Override public String caseStringType() { return null; }
      @Override public String caseObjectType(ObjectType type) { return null; }
      @Override public String caseUnionType(UnionType type) { return null; }
      @Override public String caseRecordType(RecordType type) { return null; }
      @Override public String caseFunctionType(FunctionType type) { return "visitedFunction"; }
      @Override public String caseTemplateType(TemplateType templateType) { return null; }
      @Override public String caseVoidType() { return null; }
      @Override public String caseNoObjectType() { return null; }
    };

    assertEquals("visitedFunction", fn.visit(visitor));
  }

  @Test(timeout = 4000)
  public void testResolveInternalWithSubTypesAndInterfaces() {
    FunctionType iface = registry.createInterfaceType("IResolve", null);
    FunctionType superCtor = registry.createConstructorType("SuperCtor", null, null, null);
    FunctionType subCtor = registry.createConstructorType("SubCtor", null, null, null);

    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    superCtor.setImplementedInterfaces(Collections.singletonList(iface.getInstanceType()));

    ErrorReporter reporter = new ErrorReporter() {
      @Override public void warning(String message, String sourceName, int line, int lineOffset) {}
      @Override public void error(String message, String sourceName, int line, int lineOffset) {}
    };

    JSType resolved = superCtor.resolve(reporter, scope);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testIsInstanceTypeUniversalConstructor() {
    FunctionType normalCtor = registry.createConstructorType("Normal", null, null, null);
    assertFalse(normalCtor.isInstanceType());

    JSType u2u = registry.getNativeType(U2U_CONSTRUCTOR_TYPE);
    if (u2u.isFunctionType()) {
      assertTrue(((FunctionType) u2u).isInstanceType());
    }
  }

  @Test(timeout = 4000)
  public void testTypeOfThisWithNoObjectTypeFallback() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null,
        registry.getNativeObjectType(NO_OBJECT_TYPE), null, true, false);

    assertEquals(registry.getNativeObjectType(OBJECT_TYPE), ctor.getTypeOfThis());
  }
}