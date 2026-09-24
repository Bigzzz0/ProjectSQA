package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

public class FunctionTypeDeepseekTest {

  /*
   * [Branch & Defect Analysis Matrix]
   * - Kind transitions: ORDINARY / CONSTRUCTOR / INTERFACE.
   * - Prototype lazy initialization and setPrototype guard branches.
   * - Parameter optional/vararg boundaries for min/max argument counts.
   * - Implemented interface aggregation through superclass chains.
   * - Equivalence, hashCode, subtype and least/greatest supertype branches.
   * - Defect target: an inferred UNKNOWN return type must render as "undefined"
   *   in function type string output, not as "?".
   */

  private static final class NullErrorReporter implements ErrorReporter {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
    }
  }

  private final JSTypeRegistry registry = newRegistry();

  private static JSTypeRegistry newRegistry() {
    return new JSTypeRegistry(new NullErrorReporter());
  }

  private JSType number() {
    return registry.getNativeType(JSTypeNative.NUMBER_TYPE);
  }

  private JSType string() {
    return registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  private JSType voidType() {
    return registry.getNativeType(JSTypeNative.VOID_TYPE);
  }

  private JSType unknownType() {
    return registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
  }

  private FunctionType ordinary(Node params, JSType returnType, boolean inferred) {
    return new FunctionType(
        registry, "f", null,
        new ArrowType(registry, params, returnType, inferred),
        null, null, false, false);
  }

  private FunctionType constructor(String name) {
    return new FunctionType(
        registry, name, null,
        new ArrowType(registry, new Node(Token.LP), unknownType(), false),
        null, null, true, false);
  }

  private FunctionType interfaceType(String name) {
    return FunctionType.forInterface(registry, name, null);
  }

  @Test(timeout = 4000)
  public void testConstructorKindAndInstanceType() {
    FunctionType ctor = constructor("Foo");
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.isFunctionType());
    assertTrue(ctor.canBeCalled());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertNotNull(ctor.getTypeOfThis());
    assertFalse(ctor.isInstanceType());
  }

  @Test(timeout = 4000)
  public void testOrdinaryFunctionDefaults() {
    FunctionType fn = ordinary(new Node(Token.LP), number(), false);
    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertFalse(fn.hasInstanceType());
    assertTrue(fn.getTypeOfThis().isUnknownType());
    assertNull(fn.getSource());
  }

  @Test(timeout = 4000)
  public void testInterfaceKind() {
    FunctionType iface = interfaceType("I");
    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
    assertEquals("I", iface.getReferenceName());
    assertTrue(iface.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testReturnTypeAndInferredFlag() {
    FunctionType declared = ordinary(new Node(Token.LP), number(), false);
    assertSame(number(), declared.getReturnType());
    assertFalse(declared.isReturnTypeInferred());

    FunctionType inferred = ordinary(new Node(Token.LP), unknownType(), true);
    assertSame(unknownType(), inferred.getReturnType());
    assertTrue(inferred.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testGetParametersAndMinMax() {
    Node params = new Node(Token.LP);
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(number());
    p1.setOptionalArg(true);
    params.addChildToBack(p1);
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(string());
    params.addChildToBack(p2);

    FunctionType fn = ordinary(params, voidType(), false);
    assertEquals(2, fn.getMaxArguments());
    assertEquals(2, fn.getMinArguments());

    int count = 0;
    for (Node n : fn.getParameters()) {
      assertNotNull(n.getJSType());
      count++;
    }
    assertEquals(2, count);
  }

  @Test(timeout = 4000)
  public void testVarArgsMaxArguments() {
    Node params = new Node(Token.LP);
    Node p = Node.newString(Token.NAME, "args");
    p.setJSType(number());
    p.setVarArgs(true);
    params.addChildToBack(p);

    FunctionType fn = ordinary(params, voidType(), false);
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    assertEquals(0, fn.getMinArguments());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazySingleton() {
    FunctionType fn = ordinary(new Node(Token.LP), voidType(), false);
    FunctionPrototypeType p1 = fn.getPrototype();
    FunctionPrototypeType p2 = fn.getPrototype();
    assertSame(p1, p2);
    assertTrue(fn.hasProperty("prototype"));
    assertTrue(fn.hasOwnProperty("prototype"));
    assertTrue(fn.isPropertyTypeInferred("prototype"));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeNullRejected() {
    FunctionType fn = ordinary(new Node(Token.LP), voidType(), false);
    assertFalse(fn.setPrototype(null));
    assertNotNull(fn.getPrototype());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOnSuperClass() {
    FunctionType superCtor = constructor("Super");
    FunctionType subCtor = constructor("Sub");
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    assertSame(superCtor, subCtor.getSuperClassConstructor());
    assertNotNull(superCtor.getSubTypes());
    assertTrue(superCtor.getSubTypes().contains(subCtor));
  }

  @Test(timeout = 4000)
  public void testImplementedInterfaces() {
    FunctionType iface = interfaceType("I");
    ObjectType ifaceInstance = iface.getInstanceType();
    FunctionType ctor = constructor("C");
    ctor.setImplementedInterfaces(Collections.singletonList(ifaceInstance));

    int count = 0;
    for (ObjectType t : ctor.getImplementedInterfaces()) {
      assertTrue(t.isEquivalentTo(ifaceInstance));
      count++;
    }
    assertEquals(1, count);

    count = 0;
    for (ObjectType t : ctor.getAllImplementedInterfaces()) {
      count++;
    }
    assertEquals(1, count);
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesIncludeSuperclass() {
    FunctionType iface = interfaceType("I");
    FunctionType superCtor = constructor("Super");
    superCtor.setImplementedInterfaces(Collections.singletonList(iface.getInstanceType()));

    FunctionType subCtor = constructor("Sub");
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    int count = 0;
    for (ObjectType t : subCtor.getImplementedInterfaces()) {
      assertTrue(t.isEquivalentTo(iface.getInstanceType()));
      count++;
    }
    assertEquals(1, count);
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToSameSignature() {
    FunctionType fn1 = ordinary(new Node(Token.LP), number(), false);
    FunctionType fn2 = ordinary(new Node(Token.LP), number(), false);
    assertTrue(fn1.isEquivalentTo(fn2));
    assertFalse(fn1.isEquivalentTo(ordinary(new Node(Token.LP), string(), false)));
    assertFalse(fn1.isEquivalentTo(constructor("Foo")));
  }

  @Test(timeout = 4000)
  public void testInterfaceEquivalenceByName() {
    FunctionType i1 = interfaceType("I");
    FunctionType i2 = interfaceType("I");
    FunctionType i3 = interfaceType("J");
    assertTrue(i1.isEquivalentTo(i2));
    assertFalse(i1.isEquivalentTo(i3));
    assertEquals(i1.hashCode(), i2.hashCode());
  }

  @Test(timeout = 4000)
  public void testHashCodeConsistentWithEquals() {
    FunctionType fn1 = ordinary(new Node(Token.LP), number(), false);
    FunctionType fn2 = ordinary(new Node(Token.LP), number(), false);
    assertTrue(fn1.isEquivalentTo(fn2));
    assertEquals(fn1.hashCode(), fn2.hashCode());
  }

  @Test(timeout = 4000)
  public void testHasEqualCallType() {
    FunctionType fn1 = ordinary(new Node(Token.LP), number(), false);
    FunctionType fn2 = ordinary(new Node(Token.LP), number(), false);
    FunctionType fn3 = ordinary(new Node(Token.LP), string(), false);
    assertTrue(fn1.hasEqualCallType(fn2));
    assertFalse(fn1.hasEqualCallType(fn3));
  }

  @Test(timeout = 4000)
  public void testToStringWithThisAndParam() {
    Node params = new Node(Token.LP);
    Node p = Node.newString(Token.NAME, "x");
    p.setJSType(number());
    params.addChildToBack(p);
    ObjectType thisType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);

    FunctionType fn = new FunctionType(
        registry, "f", null,
        new ArrowType(registry, params, string(), false),
        thisType, null, false, false);

    assertEquals("function (this:Object, number): string", fn.toString());
  }

  @Test(timeout = 4000)
  public void testInferredUnknownReturnTypeRendersAsUndefined() {
    FunctionType fn = ordinary(new Node(Token.LP), unknownType(), true);
    assertEquals("function (): undefined", fn.toString());
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeSameFunction() {
    FunctionType fn = ordinary(new Node(Token.LP), number(), false);
    assertSame(fn, fn.getLeastSupertype(fn));
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeSameFunction() {
    FunctionType fn = ordinary(new Node(Token.LP), number(), false);
    assertSame(fn, fn.getGreatestSubtype(fn));
  }

  @Test(timeout = 4000)
  public void testGetPropertyTypePrototype() {
    FunctionType fn = ordinary(new Node(Token.LP), voidType(), false);
    assertSame(fn.getPrototype(), fn.getPropertyType("prototype").toObjectType());
  }

  @Test(timeout = 4000)
  public void testGetPropertyTypeCallLazilyDefined() {
    Node params = new Node(Token.LP);
    Node p = Node.newString(Token.NAME, "x");
    p.setJSType(number());
    params.addChildToBack(p);

    FunctionType fn = ordinary(params, voidType(), false);
    JSType call = fn.getPropertyType("call");
    assertNotNull(call);
    assertTrue(call.isFunctionType());
    assertTrue(fn.hasOwnProperty("call"));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorRejectsBadSource() {
    Node badSource = Node.newString(Token.NAME, "notAFunction");
    new FunctionType(
        registry, "f", badSource,
        new ArrowType(registry, new Node(Token.LP), voidType(), false),
        null, null, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorRejectsNullArrowType() {
    new FunctionType(registry, "f", null, null, null, null, false, false);
  }

  @Test(timeout = 4000)
  public void testGetInstanceTypeOnOrdinaryThrows() {
    FunctionType fn = ordinary(new Node(Token.LP), voidType(), false);
    try {
      fn.getInstanceType();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertNotNull(expected.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSuperClassConstructorOnOrdinaryThrows() {
    FunctionType fn = ordinary(new Node(Token.LP), voidType(), false);
    fn.getSuperClassConstructor();
  }
}