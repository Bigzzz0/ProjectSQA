package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * This suite targets FunctionType's major public and package-private decision
 * branches:
 *  - Constructors: ORDINARY / CONSTRUCTOR / INTERFACE classification.
 *  - Parameter handling: null/empty/optional/varargs, min/max argument counts.
 *  - Prototype lifecycle: lazy getPrototype(), setPrototype(), setPrototypeBasedOn(),
 *    prototype property definition, subtype registration.
 *  - Implemented interface handling, including inherited interfaces through
 *    superclass constructors. A missing `isInterface()` check in this traversal
 *    is directly probed by testImplementedInterfacesForInterfaceInheritanceDefect,
 *    which reproduces the class of failure seen in TypeCheckTest.testGoodExtends9.
 *  - Equality and hashCode for ordinary functions, constructors, and interfaces.
 *  - Subtype/lattice operations: getLeastSupertype(), getGreatestSubtype(),
 *    isSubtype().
 *  - Lazily created "call" and "apply" properties, and toString() formatting.
 */
public class FunctionTypeDeepseekTest {

  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(createErrorReporter());
  }

  private static ErrorReporter createErrorReporter() {
    return new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, int lineOffset) {
        // No-op test reporter.
      }

      @Override
      public void error(String message, String sourceName, int line, int lineOffset) {
        // No-op test reporter.
      }
    };
  }

  private JSType num() {
    return registry.getNativeType(JSTypeNative.NUMBER_TYPE);
  }

  private JSType str() {
    return registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  private ObjectType obj() {
    return registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  private ObjectType arr() {
    return registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
  }

  private Node param(String name, JSType type) {
    Node n = Node.newString(Token.NAME, name);
    n.setJSType(type);
    return n;
  }

  private Node params(JSType... types) {
    Node lp = new Node(Token.LP);
    for (int i = 0; i < types.length; i++) {
      lp.addChildToBack(param("p" + i, types[i]));
    }
    return lp;
  }

  private FunctionType fn(String name, Node parameters, JSType returnType, ObjectType typeOfThis) {
    return new FunctionType(registry, name, null, parameters, returnType, typeOfThis);
  }

  private FunctionType ctor(String name) {
    return new FunctionType(registry, name, null, null, num(), null, null, true, false);
  }

  private FunctionType iface(String name) {
    return new FunctionType(registry, name, null);
  }

  private int count(Iterable<Node> nodes) {
    int i = 0;
    for (Node n : nodes) {
      i++;
    }
    return i;
  }

  private boolean contains(Iterable<ObjectType> types, ObjectType type) {
    for (ObjectType t : types) {
      if (t.equals(type)) {
        return true;
      }
    }
    return false;
  }

  @Test(timeout = 4000)
  public void testKindFlagsAndInstanceType() {
    FunctionType ordinary = fn("f", null, num(), null);
    assertTrue(ordinary.isFunctionType());
    assertTrue(ordinary.canBeCalled());
    assertTrue(ordinary.isOrdinaryFunction());
    assertFalse(ordinary.isConstructor());
    assertFalse(ordinary.isInterface());
    assertFalse(ordinary.hasInstanceType());
    assertNotNull(ordinary.getTypeOfThis());
    assertSame(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), ordinary.getTypeOfThis());

    FunctionType ctor = ctor("C");
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertSame(ctor.getInstanceType(), ctor.getTypeOfThis());

    FunctionType interfaceType = iface("I");
    assertTrue(interfaceType.isInterface());
    assertTrue(interfaceType.hasInstanceType());
    assertNotNull(interfaceType.getInstanceType());
    assertNotNull(interfaceType.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testParameterCountsWhenParametersUnknownOrEmpty() {
    FunctionType unknownParams = fn("f", null, num(), null);
    assertNull(unknownParams.getParametersNode());
    assertEquals(0, count(unknownParams.getParameters()));
    assertEquals(0, unknownParams.getMinArguments());
    assertEquals(Integer.MAX_VALUE, unknownParams.getMaxArguments());

    FunctionType emptyParams = fn("f", new Node(Token.LP), num(), null);
    assertNotNull(emptyParams.getParametersNode());
    assertEquals(0, count(emptyParams.getParameters()));
    assertEquals(0, emptyParams.getMinArguments());
    assertEquals(0, emptyParams.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testMinMaxArgumentsWithOptionalAndVarArgs() {
    Node mixed = new Node(Token.LP);
    Node optional = param("a", num());
    optional.setOptionalArg(true);
    mixed.addChildToBack(optional);

    Node required = param("b", str());
    mixed.addChildToBack(required);

    Node varargs = param("c", obj());
    varargs.setVarArgs(true);
    mixed.addChildToBack(varargs);

    FunctionType ft = fn("f", mixed, num(), obj());
    assertEquals(2, ft.getMinArguments());
    assertEquals(Integer.MAX_VALUE, ft.getMaxArguments());

    Node fixed = new Node(Token.LP);
    fixed.addChildToBack(param("x", num()));
    fixed.addChildToBack(param("y", str()));
    FunctionType fixedFt = fn("g", fixed, num(), obj());
    assertEquals(2, fixedFt.getMinArguments());
    assertEquals(2, fixedFt.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testGetParametersReturnsChildrenInOrder() {
    Node lp = new Node(Token.LP);
    lp.addChildToBack(param("first", num()));
    lp.addChildToBack(param("second", str()));
    FunctionType ft = fn("f", lp, num(), null);

    Iterator<Node> it = ft.getParameters().iterator();
    assertTrue(it.hasNext());
    assertEquals("first", it.next().getString());
    assertTrue(it.hasNext());
    assertEquals("second", it.next().getString());
    assertFalse(it.hasNext());
  }

  @Test(timeout = 4000)
  public void testReturnTypeSourceAndTemplateType() {
    JSType ret = num();
    FunctionType ft = fn("f", null, ret, obj());
    assertSame(ret, ft.getReturnType());

    Node source = new Node(Token.FUNCTION);
    ft.setSource(source);
    assertSame(source, ft.getSource());

    FunctionType templated = new FunctionType(registry, "f", null, null, ret, null, "T");
    assertEquals("T", templated.getTemplateTypeName());
    assertNull(fn("g", null, ret, null).getTemplateTypeName());

    assertNull(iface("I").getReturnType());
  }

  @Test(timeout = 4000)
  public void testPrototypeLazyConstructionAndProperty() {
    FunctionType ft = fn("f", null, num(), null);
    assertTrue(ft.hasProperty("prototype"));

    FunctionPrototypeType proto = ft.getPrototype();
    assertNotNull(proto);
    assertSame(proto, ft.getPropertyType("prototype"));
    assertTrue(ft.isPropertyTypeInferred("prototype"));
    assertTrue(ft.hasCachedValues());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeRejectsNullAndAcceptsPrototype() {
    FunctionType ft = fn("f", null, num(), null);
    assertFalse(ft.setPrototype(null));

    FunctionPrototypeType proto = ft.getPrototype();
    assertTrue(ft.setPrototype(proto));
    assertSame(proto, ft.getPrototype());
  }

  @Test(timeout = 4000)
  public void testSetPrototypeBasedOnRegistersSuperClassAndSubtype() {
    FunctionType base = ctor("Base");
    FunctionType derived = ctor("Derived");

    assertNull(base.getSubTypes());
    derived.setPrototypeBasedOn(base.getPrototype());

    assertSame(base, derived.getSuperClassConstructor());
    assertNotNull(base.getSubTypes());
    assertTrue(base.getSubTypes().contains(derived));
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesDirectAndInherited() {
    FunctionType ifaceK = iface("K");
    FunctionType c = ctor("C");
    c.setImplementedInterfaces(Collections.singletonList(ifaceK.getInstanceType()));
    assertTrue(contains(c.getImplementedInterfaces(), ifaceK.getInstanceType()));

    FunctionType base = ctor("Base");
    FunctionType ifaceJ = iface("J");
    base.setImplementedInterfaces(Collections.singletonList(ifaceJ.getInstanceType()));

    FunctionType derived = ctor("Derived");
    derived.setPrototypeBasedOn(base.getPrototype());
    assertTrue(contains(derived.getImplementedInterfaces(), ifaceJ.getInstanceType()));
  }

  @Test(timeout = 4000)
  public void testImplementedInterfacesForInterfaceInheritanceDefect() {
    // This is the defect-targeted regression: an interface that has a superclass
    // must still traverse that superclass when collecting implemented interfaces.
    FunctionType ifaceK = iface("K");
    FunctionType ifaceI = iface("I");
    FunctionType ifaceJ = iface("J");

    ifaceI.setImplementedInterfaces(Collections.singletonList(ifaceK.getInstanceType()));
    ifaceJ.setPrototypeBasedOn(ifaceI.getPrototype());

    assertTrue(
        "An interface should inherit implemented interfaces from its superclass",
        contains(ifaceJ.getImplementedInterfaces(), ifaceK.getInstanceType()));
  }

  @Test(timeout = 4000)
  public void testGetAllImplementedInterfacesIsTransitive() {
    FunctionType ifaceI = iface("I");
    FunctionType ifaceJ = iface("J");
    ifaceJ.setPrototypeBasedOn(ifaceI.getPrototype());

    FunctionType c = ctor("C");
    c.setImplementedInterfaces(Collections.singletonList(ifaceJ.getInstanceType()));

    Iterable<ObjectType> all = c.getAllImplementedInterfaces();
    assertTrue(contains(all, ifaceJ.getInstanceType()));
    assertTrue(contains(all, ifaceI.getInstanceType()));
  }

  @Test(timeout = 4000)
  public void testEqualsAndHashCodeOrdinaryConstructorInterface() {
    Node p1 = params(num());
    Node p2 = params(num());
    FunctionType f1 = fn("f", p1, num(), obj());
    FunctionType f2 = fn("f", p2, num(), obj());

    assertTrue(f1.equals(f2));
    assertEquals(f1.hashCode(), f2.hashCode());

    FunctionType f3 = fn("f", params(num()), num(), arr());
    assertFalse(f1.equals(f3));

    FunctionType c1 = ctor("C");
    FunctionType c2 = ctor("C");
    assertFalse(c1.equals(c2));
    assertFalse(f1.equals(c1));
    assertFalse(c1.equals(f1));

    FunctionType i1 = iface("I");
    FunctionType i2 = iface("I");
    assertTrue(i1.equals(i2));
    assertEquals(i1.hashCode(), i2.hashCode());

    assertFalse(f1.equals(i1));
    assertFalse(i1.equals(f1));
  }

  @Test(timeout = 4000)
  public void testHasEqualCallTypeIgnoresThisType() {
    FunctionType f1 = fn("f", params(num()), num(), obj());
    FunctionType f2 = fn("f", params(num()), num(), arr());

    assertFalse(f1.equals(f2));
    assertTrue(f1.hasEqualCallType(f2));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType f1 = fn("f", null, num(), obj());
    FunctionType f2 = fn("g", null, num(), arr());

    assertSame(f1, f1.getLeastSupertype(f1));
    assertSame(f1, f1.getGreatestSubtype(f1));

    JSType functionInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertSame(functionInstance, f1.getLeastSupertype(functionInstance));
    assertSame(f1, f1.getGreatestSubtype(functionInstance));

    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    assertSame(u2u, f1.getLeastSupertype(f2));

    JSType noObject = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    assertSame(noObject, f1.getGreatestSubtype(f2));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeFunctionTypeBranches() {
    FunctionType fObj = fn("f", null, num(), obj());
    FunctionType fArr = fn("f", null, num(), arr());

    assertTrue(fObj.isSubtype(fObj));
    assertTrue(fArr.isSubtype(fObj));
    assertTrue(fObj.isSubtype(fArr));

    FunctionType interfaceType = iface("I");
    assertTrue(fObj.isSubtype(interfaceType));
    assertFalse(interfaceType.isSubtype(fObj));
  }

  @Test(timeout = 4000)
  public void testLazyCallAndApplyProperties() {
    FunctionType ft = fn("f", params(num()), num(), obj());

    FunctionType callFn = (FunctionType) ft.getPropertyType("call");
    assertNotNull(callFn.getParametersNode());
    assertEquals(2, callFn.getParametersNode().getChildCount());
    assertSame(num(), callFn.getReturnType());

    FunctionType applyFn = (FunctionType) ft.getPropertyType("apply");
    assertNotNull(applyFn.getParametersNode());
    assertEquals(2, applyFn.getParametersNode().getChildCount());
    assertSame(num(), applyFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testDefinePrototypeProperty() {
    FunctionType ft = fn("f", null, num(), null);
    ObjectType base = obj();

    assertTrue(ft.defineProperty("prototype", base, false, false));
    assertSame(base, ft.getPrototype().getImplicitPrototype());

    assertFalse(ft.defineProperty("prototype", num(), false, false));
  }

  @Test(timeout = 4000)
  public void testToStringVariants() {
    FunctionType noParamsNoReturn = fn("f", null, null, obj());
    String s1 = noParamsNoReturn.toString();
    assertTrue(s1.startsWith("function ("));
    assertTrue(s1.contains("this:Object"));

    FunctionType withReturn = fn("f", params(num()), num(), null);
    assertEquals("function (number): number", withReturn.toString());

    FunctionType noParamsWithReturn = fn("f", null, num(), null);
    assertEquals("function (): number", noParamsWithReturn.toString());

    JSType functionInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", functionInstance.toString());
  }

  @Test(timeout = 4000)
  public void testHasUnknownSupertypeFalseForNormalHierarchy() {
    FunctionType base = ctor("Base");
    FunctionType derived = ctor("Derived");
    derived.setPrototypeBasedOn(base.getPrototype());

    assertFalse(derived.hasUnknownSupertype());
    assertFalse(base.hasUnknownSupertype());
  }

  @Test(timeout = 4000)
  public void testGetTypeOfThisMapsNoObjectTypeToObjectType() {
    ObjectType noObject = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
    FunctionType ft = fn("f", null, num(), noObject);

    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), ft.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testUniversalConstructorIsInstanceType() {
    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    if (u2u instanceof FunctionType) {
      assertTrue(((FunctionType) u2u).isInstanceType());
    }
  }
}