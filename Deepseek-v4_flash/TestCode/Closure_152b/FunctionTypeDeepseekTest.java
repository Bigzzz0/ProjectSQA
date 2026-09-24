package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * This suite directly exercises FunctionType's core decisions:
 * - kind discrimination (constructor/interface/ordinary), callability, instance-type identity
 * - parameter boundary analysis: empty, optional-before-required, varargs
 * - return type and inferred-return flag propagation
 * - lazy prototype creation, setPrototype rejection of null, setPrototypeBasedOn
 * - implemented interface aggregation and traversal
 * - lazy "call"/"apply" property definition, including null parameter nodes
 * - prototype property handling in defineProperty/hasProperty/isPropertyTypeInferred
 * - equivalence/hash, least-supertype/greatest-subtype merging, subtyping rules
 * - constructor superclass/subtype graph registration
 * - instance type contracts and getTypeOfThis fallback
 * - Defect-targeted regression: resolving a `this` type that is a typedef
 *   resolving to StringType or UnionType must not trigger ClassCastException
 *   in FunctionType.resolveInternal.
 */
public class FunctionTypeDeepseekTest {

  private static class TestErrorReporter implements ErrorReporter {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
    }
  }

  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter());
  }

  private ObjectType objectType() {
    return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  private JSType numberType() {
    return registry.getNativeType(JSTypeNative.NUMBER_TYPE);
  }

  private JSType stringType() {
    return registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  private static Node param(JSType type) {
    Node p = Node.newString(Token.NAME, "p");
    p.setJSType(type);
    return p;
  }

  private static Node params(JSType... types) {
    Node lp = new Node(Token.LP);
    for (JSType type : types) {
      lp.addChildToBack(param(type));
    }
    return lp;
  }

  private FunctionType createOrdinaryFunction(
      String name, Node parameters, JSType returnType, ObjectType thisType, boolean inferred) {
    return new FunctionType(
        registry,
        name,
        null,
        new ArrowType(registry, parameters, returnType, inferred),
        thisType,
        null,
        false,
        false);
  }

  private FunctionType createOrdinaryFunction() {
    return createOrdinaryFunction(
        "f", new Node(Token.LP), numberType(), objectType(), false);
  }

  private FunctionType createOrdinaryFunctionWithNullParameters() {
    return new FunctionType(
        registry,
        "f",
        null,
        new ArrowType(registry, null, numberType(), false),
        objectType(),
        null,
        false,
        false);
  }

  private FunctionType createConstructor(String name) {
    return new FunctionType(
        registry,
        name,
        null,
        new ArrowType(registry, new Node(Token.LP), numberType(), false),
        objectType(),
        null,
        true,
        false);
  }

  @Test(timeout = 4000)
  public void testKindPredicatesAndIsInstanceType() {
    FunctionType fn = createOrdinaryFunction();
    FunctionType ctor = createConstructor("Ctor");
    FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
    FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);

    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());

    assertTrue(u2u.isInstanceType());
    assertFalse(fn.isInstanceType());
  }

  @Test(timeout = 4000)
  public void testMinMaxArguments() {
    FunctionType empty =
        createOrdinaryFunction("empty", new Node(Token.LP), numberType(), objectType(), false);
    assertEquals(0, empty.getMinArguments());
    assertEquals(0, empty.getMaxArguments());

    Node optionalBeforeRequired = params(numberType(), numberType());
    optionalBeforeRequired.getFirstChild().setOptionalArg(true);
    FunctionType optBeforeReq = createOrdinaryFunction(
        "f", optionalBeforeRequired, numberType(), objectType(), false);
    assertEquals(2, optBeforeReq.getMinArguments());
    assertEquals(2, optBeforeReq.getMaxArguments());

    Node varargLp = params(numberType());
    varargLp.getFirstChild().setVarArgs(true);
    FunctionType vararg =
        createOrdinaryFunction("vararg", varargLp, numberType(), objectType(), false);
    assertEquals(0, vararg.getMinArguments());
    assertEquals(Integer.MAX_VALUE, vararg.getMaxArguments());

    int count = 0;
    for (Node p : vararg.getParameters()) {
      count++;
    }
    assertEquals(1, count);
  }

  @Test(timeout = 4000)
  public void testReturnTypeAndInferredFlag() {
    JSType ret = stringType();
    FunctionType inferred =
        createOrdinaryFunction("f", new Node(Token.LP), ret, objectType(), true);
    assertSame(ret, inferred.getReturnType());
    assertTrue(inferred.isReturnTypeInferred());

    FunctionType notInferred =
        createOrdinaryFunction("g", new Node(Token.LP), ret, objectType(), false);
    assertSame(ret, notInferred.getReturnType());
    assertFalse(notInferred.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyInitializationAndSetPrototype() {
    FunctionType fn = createOrdinaryFunction();
    assertFalse(fn.hasCachedValues());

    FunctionPrototypeType proto = fn.getPrototype();
    assertTrue(fn.hasCachedValues());
    assertSame(proto, fn.getPrototype());

    assertFalse(fn.setPrototype(null));
    assertTrue(fn.setPrototype(proto));
    assertSame(proto, fn.getPrototype());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOn() {
    FunctionType ctor = createConstructor("C");
    FunctionPrototypeType original = ctor.getPrototype();
    assertNull(original.getImplicitPrototype());

    ObjectType base = objectType();
    ctor.setPrototypeBasedOn(base);
    assertSame(base, original.getImplicitPrototype());

    ObjectType ifaceBase = FunctionType.forInterface(registry, "I", null).getInstanceType();
    ctor.setPrototypeBasedOn(ifaceBase);
    assertSame(original, ctor.getPrototype());
    assertSame(ifaceBase, original.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testImplementedInterfaces() {
    FunctionType iface = FunctionType.forInterface(registry, "I", null);
    FunctionType ctor = createConstructor("C");
    ObjectType ifaceInstance = iface.getInstanceType();

    ctor.setImplementedInterfaces(Collections.<ObjectType>singletonList(ifaceInstance));

    int count = 0;
    for (ObjectType t : ctor.getImplementedInterfaces()) {
      assertSame(ifaceInstance, t);
      count++;
    }
    assertEquals(1, count);

    count = 0;
    for (ObjectType t : ctor.getAllImplementedInterfaces()) {
      assertSame(ifaceInstance, t);
      count++;
    }
    assertEquals(1, count);
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyAndLazyCallApply() {
    FunctionType fn = createOrdinaryFunction();

    assertTrue(fn.hasProperty("prototype"));
    assertTrue(fn.hasOwnProperty("prototype"));
    assertSame(fn.getPrototype(), fn.getPropertyType("prototype"));

    JSType call = fn.getPropertyType("call");
    assertNotNull(call);
    assertTrue(call instanceof FunctionType);
    assertTrue(fn.hasOwnProperty("call"));

    JSType apply = fn.getPropertyType("apply");
    assertNotNull(apply);
    assertTrue(apply instanceof FunctionType);
    assertTrue(fn.hasOwnProperty("apply"));
  }

  @Test(timeout = 4000)
  public void testCallPropertyWhenParametersNodeIsNull() {
    FunctionType fn = createOrdinaryFunctionWithNullParameters();
    JSType call = fn.getPropertyType("call");
    assertNotNull(call);
    assertTrue(call instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testDefinePropertyPrototype() {
    FunctionType fn = createOrdinaryFunction();
    FunctionPrototypeType original = fn.getPrototype();

    assertTrue(fn.defineProperty("prototype", original, false, false));
    assertSame(original, fn.getPrototype());

    assertTrue(fn.defineProperty("prototype", objectType(), false, false));
    assertNotNull(fn.getPrototype());

    assertFalse(fn.defineProperty("prototype", stringType(), false, false));
  }

  @Test(timeout = 4000)
  public void testIsPropertyTypeInferred() {
    FunctionType fn = createOrdinaryFunction();
    assertTrue(fn.isPropertyTypeInferred("prototype"));
    assertFalse(fn.isPropertyTypeInferred("someProp"));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo() {
    FunctionType fn1 =
        createOrdinaryFunction("f", params(numberType()), numberType(), objectType(), false);
    FunctionType fn2 =
        createOrdinaryFunction("g", params(numberType()), numberType(), objectType(), false);
    assertTrue(fn1.isEquivalentTo(fn2));
    assertTrue(fn2.isEquivalentTo(fn1));
    assertTrue(fn1.hasEqualCallType(fn2));

    FunctionType ctor1 = createConstructor("C");
    FunctionType ctor2 = createConstructor("C");
    assertFalse(ctor1.isEquivalentTo(ctor2));
    assertTrue(ctor1.isEquivalentTo(ctor1));

    FunctionType iface1 = FunctionType.forInterface(registry, "I", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I", null);
    assertTrue(iface1.isEquivalentTo(iface2));
    assertEquals(iface1.hashCode(), iface2.hashCode());

    assertFalse(fn1.isEquivalentTo(ctor1));
    assertFalse(fn1.isEquivalentTo(iface1));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeAndGreatestSubtypeEquivalentFunctions() {
    FunctionType fn1 =
        createOrdinaryFunction("f", params(numberType()), numberType(), objectType(), false);
    FunctionType fn2 =
        createOrdinaryFunction("g", params(numberType()), numberType(), objectType(), false);

    assertSame(fn1, fn1.getLeastSupertype(fn2));
    assertSame(fn1, fn1.getGreatestSubtype(fn2));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeMergesOrdinaryFunctions() {
    FunctionType fn1 =
        createOrdinaryFunction("f", params(numberType()), numberType(), objectType(), false);
    FunctionType fn2 =
        createOrdinaryFunction("g", params(numberType()), stringType(), objectType(), false);

    JSType sup = fn1.getLeastSupertype(fn2);
    assertNotNull(sup);
    assertTrue(sup.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testSubtypeInterfaceRules() {
    FunctionType fn = createOrdinaryFunction();
    FunctionType iface = FunctionType.forInterface(registry, "I", null);

    assertTrue(fn.isSubtype(iface));
    assertFalse(iface.isSubtype(fn));
  }

  @Test(timeout = 4000)
  public void testSuperClassConstructorAndSubtypes() {
    FunctionType a = createConstructor("A");
    FunctionType b = createConstructor("B");

    assertNull(a.getSuperClassConstructor());
    assertNull(b.getSubTypes());

    a.setPrototypeBasedOn(b.getInstanceType());

    assertSame(b, a.getSuperClassConstructor());
    assertNotNull(b.getSubTypes());
    assertEquals(1, b.getSubTypes().size());
    assertSame(a, b.getSubTypes().get(0));
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertypeNoSuperclass() {
    FunctionType ctor = createConstructor("C");
    assertFalse(ctor.hasUnknownSupertype());
  }

  @Test(timeout = 4000)
  public void testGetInstanceType() {
    FunctionType ctor = createConstructor("C");
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());

    FunctionType fn = createOrdinaryFunction();
    assertFalse(fn.hasInstanceType());
    try {
      fn.getInstanceType();
      fail("getInstanceType should throw for an ordinary function");
    } catch (IllegalStateException expected) {
      // Expected.
    }

    FunctionType iface = FunctionType.forInterface(registry, "I", null);
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
  }

  @Test(timeout = 4000)
  public void testGetTypeOfThis() {
    FunctionType unknownThis =
        createOrdinaryFunction("f", new Node(Token.LP), numberType(), null, false);
    assertSame(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), unknownThis.getTypeOfThis());

    FunctionType noObjectThis = new FunctionType(
        registry,
        "C",
        null,
        new ArrowType(registry, new Node(Token.LP), numberType(), false),
        registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE),
        null,
        true,
        false);
    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), noObjectThis.getTypeOfThis());

    FunctionType normalThis = createOrdinaryFunction();
    assertSame(objectType(), normalThis.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testSourceAndTemplateTypeName() {
    FunctionType fn = createOrdinaryFunction();
    assertNull(fn.getSource());

    Node source = new Node(Token.FUNCTION);
    fn.setSource(source);
    assertSame(source, fn.getSource());

    FunctionType templated = new FunctionType(
        registry,
        "f",
        null,
        new ArrowType(registry, new Node(Token.LP), numberType(), false),
        objectType(),
        "T",
        false,
        false);
    assertEquals("T", templated.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testToString() {
    FunctionType knownThis =
        createOrdinaryFunction("f", params(numberType()), stringType(), objectType(), false);
    String knownString = knownThis.toString();
    assertTrue(knownString.contains("function ("));
    assertTrue(knownString.contains("this:"));
    assertTrue(knownString.contains("number"));
    assertTrue(knownString.contains("string"));

    FunctionType unknownThis =
        createOrdinaryFunction("g", new Node(Token.LP), numberType(), null, false);
    assertFalse(unknownThis.toString().contains("this:"));
  }

  @Test(timeout = 4000)
  public void testResolveSimpleFunction() {
    FunctionType fn = createOrdinaryFunction();
    assertSame(fn, fn.resolve(new TestErrorReporter(), null));
  }

  @Test(timeout = 4000)
  public void testResolveWithStringTypedefThisTypeDoesNotThrow() {
    String name = "BackwardsStringTypedef";
    registry.registerType(stringType(), name);
    Node source = Node.newString(Token.NAME, name);
    ObjectType typedefThis = registry.createNamedType(name, source, 0, 0);

    FunctionType fn = createOrdinaryFunction("f", new Node(Token.LP), numberType(), typedefThis, false);

    try {
      fn.resolve(new TestErrorReporter(), null);
    } catch (ClassCastException e) {
      fail("FunctionType.resolveInternal must not cast a typedef resolving to StringType to ObjectType");
    }
  }

  @Test(timeout = 4000)
  public void testResolveWithUnionTypedefThisTypeDoesNotThrow() {
    String name = "BackwardsUnionTypedef";
    JSType union = registry.createUnionType(stringType(), numberType());
    registry.registerType(union, name);
    Node source = Node.newString(Token.NAME, name);
    ObjectType typedefThis = registry.createNamedType(name, source, 0, 0);

    FunctionType fn = createOrdinaryFunction("f", new Node(Token.LP), numberType(), typedefThis, false);

    try {
      fn.resolve(new TestErrorReporter(), null);
    } catch (ClassCastException e) {
      fail("FunctionType.resolveInternal must not cast a typedef resolving to UnionType to ObjectType");
    }
  }
}