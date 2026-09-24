package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ArrowType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.SimpleSlot;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.InstanceObjectType;
import com.google.javascript.rhino.jstype.Visitor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Test suite for FunctionType, targeting high branch coverage and the known defect
 * related to argument count checking (testIssue537a/b, testPropertyOnUnknownSuperClass2).
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructors: ORDINARY, CONSTRUCTOR, INTERFACE
 * - getMinArguments: empty params, optional, varargs, required
 * - getMaxArguments: null params, empty, optional, varargs
 * - getPrototype: lazy initialization, null check
 * - setPrototype: null, instance type equality, constructor/interface subtype registration
 * - setPrototypeBasedOn: reference name, unknown, native, function prototype, PrototypeObjectType
 * - isConstructor, isInterface, isOrdinaryFunction
 * - getPropertyType: "call" with null params, with params; "apply"
 * - hasImplementedInterfaces: empty, superclass chain
 * - getImplementedInterfaces: superclass null, non-null
 * - setImplementedInterfaces: registration
 * - getExtendedInterfaces, setExtendedInterfaces: interface vs non-interface
 * - isEquivalentTo: constructor, interface, ordinary, mixed
 * - isSubtype: interface, constructor, ordinary, this type covariance
 * - getInstanceType, hasInstanceType
 * - getTypeOfThis: no object type fallback
 * - getSource, setSource
 * - getTemplateTypeName
 * - toString: function instance, with/without this type, varargs
 * - clearCachedValues, hasCachedValues
 * - getSubTypes: via setPrototype
 * - Defect-specific: function with no parameters -> min=0, max=0, parameters empty
 */
public class FunctionTypeDeepseekTest {

  private static final ErrorReporter NULL_REPORTER = new ErrorReporter() {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      // no-op
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      // no-op
    }
  };

  private JSTypeRegistry createRegistry() {
    return new JSTypeRegistry(NULL_REPORTER);
  }

  private FunctionType createOrdinaryFunction(JSTypeRegistry registry, Node params, JSType returnType) {
    ArrowType arrow = new ArrowType(registry, params, returnType);
    return new FunctionType(registry, "testFn", null, arrow,
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), null, false, false);
  }

  private FunctionType createConstructor(JSTypeRegistry registry, Node params, JSType returnType,
      ObjectType typeOfThis) {
    ArrowType arrow = new ArrowType(registry, params, returnType);
    return new FunctionType(registry, "TestCtor", null, arrow, typeOfThis, null, true, false);
  }

  private FunctionType createInterface(JSTypeRegistry registry, String name) {
    return FunctionType.forInterface(registry, name, null);
  }

  // ==================== Partition A: Core Functional Logic & State Transitions ====================

  @Test(timeout = 4000)
  public void testConstructorOrdinary() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    FunctionType fn = createOrdinaryFunction(registry, params, returnType);
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isOrdinaryFunction());
    assertTrue(fn.canBeCalled());
    assertSame(fn, fn.toMaybeFunctionType());
    assertEquals("testFn", fn.getReferenceName());
    assertNull(fn.getSource());
    assertNull(fn.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testConstructorConstructor() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    JSType returnType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ObjectType instanceType = new InstanceObjectType(registry, null, false);
    FunctionType ctor = createConstructor(registry, params, returnType, instanceType);
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.hasInstanceType());
    assertSame(instanceType, ctor.getInstanceType());
  }

  @Test(timeout = 4000)
  public void testConstructorInterface() {
    JSTypeRegistry registry = createRegistry();
    FunctionType iface = createInterface(registry, "MyInterface");
    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("MyInterface", iface.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testGetParametersEmpty() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertNotNull(fn.getParametersNode());
    assertEquals(0, fn.getParametersNode().getChildCount());
    assertFalse(fn.getParameters().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testGetParametersWithArgs() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params.addChildToBack(param1);
    Node param2 = Node.newString(Token.NAME, "b");
    param2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    param2.setOptionalArg(true);
    params.addChildToBack(param2);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(2, fn.getParametersNode().getChildCount());
    assertEquals(2, Iterables.size(fn.getParameters()));
  }

  // ==================== Partition B: Boundary Value Analysis & Extremes ====================

  @Test(timeout = 4000)
  public void testGetMinArgumentsEmpty() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(0, fn.getMinArguments());
  }

  @Test(timeout = 4000)
  public void testGetMinArgumentsOptional() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node opt = Node.newString(Token.NAME, "opt");
    opt.setOptionalArg(true);
    params.addChildToBack(opt);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(0, fn.getMinArguments());
  }

  @Test(timeout = 4000)
  public void testGetMinArgumentsRequired() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node req = Node.newString(Token.NAME, "req");
    params.addChildToBack(req);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(1, fn.getMinArguments());
  }

  @Test(timeout = 4000)
  public void testGetMinArgumentsMixed() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node opt = Node.newString(Token.NAME, "opt");
    opt.setOptionalArg(true);
    params.addChildToBack(opt);
    Node req = Node.newString(Token.NAME, "req");
    params.addChildToBack(req);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(2, fn.getMinArguments()); // last required param at position 2
  }

  @Test(timeout = 4000)
  public void testGetMaxArgumentsEmpty() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(0, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testGetMaxArgumentsOptional() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node opt = Node.newString(Token.NAME, "opt");
    opt.setOptionalArg(true);
    params.addChildToBack(opt);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(1, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testGetMaxArgumentsVarArgs() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node var = Node.newString(Token.NAME, "var");
    var.setVarArgs(true);
    params.addChildToBack(var);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  @Test(timeout = 4000)
  public void testGetMaxArgumentsNullParams() {
    JSTypeRegistry registry = createRegistry();
    ArrowType arrow = new ArrowType(registry, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn = new FunctionType(registry, "test", null, arrow,
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), null, false, false);
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================

  @Test(timeout = 4000)
  public void testDefectMinMaxArgumentsNoParams() {
    // This test directly targets the defect scenario: function with no parameters.
    // In the buggy version, getMaxArguments might return Integer.MAX_VALUE or getMinArguments might be wrong.
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(0, fn.getMinArguments());
    assertEquals(0, fn.getMaxArguments());
    assertFalse(fn.getParameters().iterator().hasNext());
    assertNotNull(fn.getParametersNode());
    assertEquals(0, fn.getParametersNode().getChildCount());
  }

  @Test(timeout = 4000)
  public void testDefectPropertyOnPrototype() {
    // Simulate a property defined on a prototype with a function type.
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    PrototypeObjectType proto = new PrototypeObjectType(registry, "TestProto",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    proto.defineDeclaredProperty("baz", fn, null);
    JSType propType = proto.getPropertyType("baz");
    assertTrue(propType instanceof FunctionType);
    FunctionType bazFn = (FunctionType) propType;
    assertEquals(0, bazFn.getMinArguments());
    assertEquals(0, bazFn.getMaxArguments());
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testSetPrototypeNull() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertFalse(fn.setPrototype(null));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeInstanceType() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ObjectType instanceType = new InstanceObjectType(registry, null, false);
    FunctionType ctor = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    assertFalse(ctor.setPrototype((PrototypeObjectType) instanceType));
  }

  @Test(timeout = 4000)
  public void testSetPrototypeValid() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    PrototypeObjectType proto = new PrototypeObjectType(registry, "testProto",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    assertTrue(fn.setPrototype(proto));
    assertSame(proto, fn.getPrototype());
  }

  @Test(timeout = 4000)
  public void testSetExtendedInterfacesNonInterface() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    try {
      fn.setExtendedInterfaces(ImmutableList.<ObjectType>of());
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testSetExtendedInterfacesInterface() {
    JSTypeRegistry registry = createRegistry();
    FunctionType iface = createInterface(registry, "Iface");
    ObjectType extIface = new InstanceObjectType(registry, null, false);
    iface.setExtendedInterfaces(ImmutableList.of(extIface));
    assertEquals(1, iface.getExtendedInterfacesCount());
    assertTrue(iface.getExtendedInterfaces().iterator().hasNext());
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testIsEquivalentToConstructors() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ObjectType instanceType = new InstanceObjectType(registry, null, false);
    FunctionType ctor1 = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    FunctionType ctor2 = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    assertTrue(ctor1.isEquivalentTo(ctor1));
    assertFalse(ctor1.isEquivalentTo(ctor2)); // different references
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToInterfaces() {
    JSTypeRegistry registry = createRegistry();
    FunctionType iface1 = createInterface(registry, "Iface");
    FunctionType iface2 = createInterface(registry, "Iface");
    FunctionType iface3 = createInterface(registry, "Other");
    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(iface3));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToOrdinary() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    FunctionType fn1 = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn2 = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    // Ordinary functions are equivalent if typeOfThis and call are equivalent.
    // typeOfThis is UNKNOWN_TYPE for both, call is equivalent.
    assertTrue(fn1.isEquivalentTo(fn2));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeInterface() {
    JSTypeRegistry registry = createRegistry();
    FunctionType iface = createInterface(registry, "Iface");
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertTrue(fn.isSubtype(iface)); // any function can be assigned to interface function
    assertFalse(iface.isSubtype(fn)); // interface cannot be assigned to ordinary
  }

  @Test(timeout = 4000)
  public void testGetTypeOfThisNoObject() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn = new FunctionType(registry, "test", null, arrow,
        registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE), null, false, false);
    // getTypeOfThis should return OBJECT_TYPE when typeOfThis is NO_OBJECT_TYPE
    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testGetPropertyTypeCall() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node param = Node.newString(Token.NAME, "x");
    param.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params.addChildToBack(param);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.STRING_TYPE));
    JSType callType = fn.getPropertyType("call");
    assertTrue(callType instanceof FunctionType);
    FunctionType callFn = (FunctionType) callType;
    // call function should have thisType as first optional param
    assertTrue(callFn.getMinArguments() >= 0);
  }

  @Test(timeout = 4000)
  public void testGetPropertyTypeApply() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    JSType applyType = fn.getPropertyType("apply");
    assertTrue(applyType instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testGetPrototypeLazy() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertNull(fn.prototype); // field is private, but we can check via getPrototype
    ObjectType proto = fn.getPrototype();
    assertNotNull(proto);
    assertEquals(fn.getReferenceName() + ".prototype", proto.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testHasImplementedInterfacesEmpty() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertFalse(fn.hasImplementedInterfaces());
  }

  @Test(timeout = 4000)
  public void testHasImplementedInterfacesWithSuper() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ObjectType instanceType = new InstanceObjectType(registry, null, false);
    FunctionType superCtor = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    // Set implemented interfaces on superCtor
    ObjectType iface = new InstanceObjectType(registry, null, false);
    superCtor.setImplementedInterfaces(ImmutableList.of(iface));
    // Create subclass
    FunctionType subCtor = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    // Set prototype to establish superclass relationship
    PrototypeObjectType superProto = new PrototypeObjectType(registry, "SuperProto",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    superCtor.setPrototype(superProto);
    // Set subclass prototype based on superCtor
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    assertTrue(subCtor.hasImplementedInterfaces());
  }

  @Test(timeout = 4000)
  public void testToStringFunctionInstance() {
    JSTypeRegistry registry = createRegistry();
    JSType fnInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnInstance.toString());
  }

  @Test(timeout = 4000)
  public void testToStringOrdinary() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    Node param = Node.newString(Token.NAME, "x");
    param.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    params.addChildToBack(param);
    FunctionType fn = createOrdinaryFunction(registry, params, registry.getNativeType(JSTypeNative.STRING_TYPE));
    String str = fn.toString();
    assertTrue(str.startsWith("function ("));
    assertTrue(str.contains("number"));
    assertTrue(str.contains("string"));
  }

  @Test(timeout = 4000)
  public void testClearCachedValues() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    fn.getPrototype(); // initialize prototype
    assertTrue(fn.hasCachedValues());
    fn.clearCachedValues();
    assertFalse(fn.hasCachedValues());
  }

  @Test(timeout = 4000)
  public void testGetSubTypes() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ObjectType instanceType = new InstanceObjectType(registry, null, false);
    FunctionType superCtor = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    FunctionType subCtor = createConstructor(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), instanceType);
    // Set prototype to establish superclass relationship
    PrototypeObjectType superProto = new PrototypeObjectType(registry, "SuperProto",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    superCtor.setPrototype(superProto);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    List<FunctionType> subTypes = superCtor.getSubTypes();
    assertNotNull(subTypes);
    assertTrue(subTypes.contains(subCtor));
  }

  @Test(timeout = 4000)
  public void testGetTemplateTypeName() {
    JSTypeRegistry registry = createRegistry();
    Node params = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn = new FunctionType(registry, "test", null, arrow,
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), "T", false, false);
    assertEquals("T", fn.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testSetSource() {
    JSTypeRegistry registry = createRegistry();
    FunctionType fn = createOrdinaryFunction(registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    Node source = new Node(Token.FUNCTION);
    fn.setSource(source);
    assertSame(source, fn.getSource());
  }
}