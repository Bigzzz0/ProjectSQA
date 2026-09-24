package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;

import java.util.Set;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted defect (TypeCheckTest.testIssue700):
 * Properties declared on interfaces implemented by a constructor are not
 * visible through PrototypeObjectType.getSlot(). The implementation searches
 * only getCtorExtendedInterfaces(), missing getCtorImplementedInterfaces().
 * A dedicated test asserts that a property defined on an implemented interface
 * is found through the implementing class's prototype slot lookup.
 *
 * Additional targeted branches:
 * - Constructors with/without implicit prototype and native flag.
 * - getSlot: own property, implicit prototype, extended interface, absent.
 * - getPropertiesCount: null prototype, non-null prototype, shadowed property.
 * - hasProperty / hasOwnProperty / getOwnPropertyNames.
 * - isPropertyTypeDeclared / isPropertyTypeInferred.
 * - collectPropertyNames through prototype chain.
 * - getPropertyType fallback to UNKNOWN_TYPE.
 * - isPropertyInExterns paths.
 * - defineProperty / removeProperty / setPropertyJSDocInfo.
 * - matchesNumberContext / matchesStringContext / canBeCalled /
 *   matchesObjectContext / unboxesTo.
 * - toStringHelper: named, anonymous, pretty-print, 4-property truncation.
 * - setImplicitPrototype, setOwnerFunction, getReferenceName.
 * - Default getCtorImplementedInterfaces / getCtorExtendedInterfaces.
 */
public class PrototypeObjectTypeDeepseekTest {

  private static JSTypeRegistry newRegistry() {
    return new JSTypeRegistry(new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line,
          int lineOffset) {
      }

      @Override
      public void error(String message, String sourceName, int line,
          int lineOffset) {
      }
    });
  }

  @Test(timeout = 4000)
  public void testConstructorsNativeFlagAndReferenceName() {
    JSTypeRegistry r = newRegistry();

    PrototypeObjectType anon = new PrototypeObjectType(r, null, null);
    assertNotNull(anon.getImplicitPrototype());
    assertFalse(anon.isNativeObjectType());
    assertFalse(anon.hasReferenceName());
    assertNull(anon.getReferenceName());

    PrototypeObjectType named = new PrototypeObjectType(r, "Foo", null);
    assertEquals("Foo", named.getReferenceName());
    assertTrue(named.hasReferenceName());

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    assertSame(parent, child.getImplicitPrototype());

    PrototypeObjectType nativeObj =
        new PrototypeObjectType(r, "Native", null, true);
    assertNull(nativeObj.getImplicitPrototype());
    assertTrue(nativeObj.isNativeObjectType());
  }

  @Test(timeout = 4000)
  public void testGetSlotOwnAndImplicitPrototypeProperties() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    parent.defineProperty("parentProp", number, false, null);

    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    child.defineProperty("childProp", number, false, null);

    assertNotNull(child.getSlot("childProp"));
    assertNotNull(child.getSlot("parentProp"));
    assertNull(child.getSlot("absent"));
    assertEquals(number, child.getPropertyType("childProp"));
  }

  @Test(timeout = 4000)
  public void testGetSlotFindsPropertiesOnImplementedInterfaces() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    final PrototypeObjectType implementedInterface =
        new PrototypeObjectType(r, "I", null);
    implementedInterface.defineProperty("interfaceMethod", number, false, null);

    PrototypeObjectType implementingClass =
        new PrototypeObjectType(r, "C", null) {
          @Override
          public Iterable<ObjectType> getCtorImplementedInterfaces() {
            return ImmutableList.<ObjectType>of(implementedInterface);
          }

          @Override
          public Iterable<ObjectType> getCtorExtendedInterfaces() {
            return ImmutableList.<ObjectType>of();
          }
        };

    assertNotNull(
        "Property declared on an implemented interface must be visible",
        implementingClass.getSlot("interfaceMethod"));
    assertTrue(implementingClass.hasProperty("interfaceMethod"));
  }

  @Test(timeout = 4000)
  public void testGetSlotFindsPropertiesOnExtendedInterfaces() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    final PrototypeObjectType superInterface =
        new PrototypeObjectType(r, "Super", null);
    superInterface.defineProperty("superMethod", number, false, null);

    PrototypeObjectType subInterface =
        new PrototypeObjectType(r, "Sub", null) {
          @Override
          public Iterable<ObjectType> getCtorImplementedInterfaces() {
            return ImmutableList.<ObjectType>of();
          }

          @Override
          public Iterable<ObjectType> getCtorExtendedInterfaces() {
            return ImmutableList.<ObjectType>of(superInterface);
          }
        };

    assertNotNull(subInterface.getSlot("superMethod"));
  }

  @Test(timeout = 4000)
  public void testGetPropertiesCount() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType nullProto =
        new PrototypeObjectType(r, "NullProto", null, true);
    assertEquals(0, nullProto.getPropertiesCount());
    nullProto.defineProperty("x", number, false, null);
    assertEquals(1, nullProto.getPropertiesCount());

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    parent.defineProperty("shared", number, false, null);
    int parentCount = parent.getPropertiesCount();

    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    child.defineProperty("shared", number, false, null);

    assertEquals(parentCount, child.getPropertiesCount());

    child.defineProperty("childOnly", number, false, null);
    assertEquals(parentCount + 1, child.getPropertiesCount());
  }

  @Test(timeout = 4000)
  public void testHasPropertyAndHasOwnProperty() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    obj.defineProperty(
        "own", r.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    assertTrue(obj.hasProperty("own"));
    assertTrue(obj.hasOwnProperty("own"));
    assertFalse(obj.hasOwnProperty("missing"));
    assertFalse(obj.hasProperty("missing"));
  }

  @Test(timeout = 4000)
  public void testHasPropertyShortCircuitsOnUnknownType() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType unknown = new PrototypeObjectType(r, "U", null) {
      @Override
      public boolean isUnknownType() {
        return true;
      }
    };
    assertTrue(unknown.hasProperty("anything"));
  }

  @Test(timeout = 4000)
  public void testGetOwnPropertyNames() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    obj.defineProperty(
        "a", r.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    obj.defineProperty(
        "b", r.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    Set<String> names = obj.getOwnPropertyNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("a"));
    assertTrue(names.contains("b"));
  }

  @Test(timeout = 4000)
  public void testPropertyDeclaredAndInferredFlags() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    obj.defineProperty("declared", number, false, null);
    obj.defineProperty("inferred", number, true, null);

    assertTrue(obj.isPropertyTypeDeclared("declared"));
    assertFalse(obj.isPropertyTypeDeclared("inferred"));
    assertFalse(obj.isPropertyTypeDeclared("absent"));

    assertFalse(obj.isPropertyTypeInferred("declared"));
    assertTrue(obj.isPropertyTypeInferred("inferred"));
    assertFalse(obj.isPropertyTypeInferred("absent"));
  }

  @Test(timeout = 4000)
  public void testCollectPropertyNamesIncludesPrototypeChain() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    parent.defineProperty("parentProp", number, false, null);

    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    child.defineProperty("childProp", number, false, null);

    Set<String> names = Sets.newHashSet();
    child.collectPropertyNames(names);

    assertTrue(names.contains("parentProp"));
    assertTrue(names.contains("childProp"));
  }

  @Test(timeout = 4000)
  public void testGetPropertyTypeFallbackToUnknown() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    assertEquals(
        r.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        obj.getPropertyType("missing"));
  }

  @Test(timeout = 4000)
  public void testIsPropertyInExterns() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    parent.defineProperty("p", number, false, null);

    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    assertFalse(child.isPropertyInExterns("p"));
    assertFalse(child.isPropertyInExterns("missing"));
  }

  @Test(timeout = 4000)
  public void testDefineAndRemoveProperty() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    assertTrue(obj.defineProperty("x", number, false, null));
    assertFalse(obj.defineProperty("x", number, false, null));

    assertTrue(obj.removeProperty("x"));
    assertFalse(obj.removeProperty("x"));
  }

  @Test(timeout = 4000)
  public void testGetPropertyNode() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType parent = new PrototypeObjectType(r, "Parent", null);
    parent.defineProperty("p", number, false, null);

    PrototypeObjectType child = new PrototypeObjectType(r, "Child", parent);
    assertNull(parent.getPropertyNode("p"));
    assertNull(child.getPropertyNode("p"));
    assertNull(child.getPropertyNode("missing"));
  }

  @Test(timeout = 4000)
  public void testSetPropertyJSDocInfo() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSDocInfo info = new JSDocInfo();

    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);

    obj.setPropertyJSDocInfo("createdByDoc", info);
    assertNotNull(obj.getOwnPropertyJSDocInfo("createdByDoc"));

    obj.defineProperty("existing", number, false, null);
    obj.setPropertyJSDocInfo("existing", info);
    assertSame(info, obj.getOwnPropertyJSDocInfo("existing"));

    assertNull(obj.getOwnPropertyJSDocInfo("missing"));
    obj.setPropertyJSDocInfo("missing", null);
    assertNull(obj.getOwnPropertyJSDocInfo("missing"));
  }

  @Test(timeout = 4000)
  public void testContextMatchingAndCallability() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    assertFalse(obj.matchesNumberContext());
    assertFalse(obj.matchesStringContext());
    assertTrue(obj.matchesObjectContext());
    assertFalse(obj.canBeCalled());

    obj.defineProperty("valueOf", number, false, null);
    assertTrue(obj.matchesNumberContext());

    obj.defineProperty("toString", number, false, null);
    assertTrue(obj.matchesStringContext());

    assertTrue(r.getNativeObjectType(JSTypeNative.REGEXP_TYPE).canBeCalled());
  }

  @Test(timeout = 4000)
  public void testUnboxesToNativeWrappers() {
    JSTypeRegistry r = newRegistry();

    assertEquals(
        r.getNativeType(JSTypeNative.STRING_TYPE),
        r.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE).unboxesTo());
    assertEquals(
        r.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        r.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE).unboxesTo());
    assertEquals(
        r.getNativeType(JSTypeNative.NUMBER_TYPE),
        r.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE).unboxesTo());
  }

  @Test(timeout = 4000)
  public void testToStringHelperNamedAnonymousAndPrettyPrint() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType anon = new PrototypeObjectType(r, null, null);
    assertEquals("?", anon.toStringHelper(true));
    assertEquals("{...}", anon.toStringHelper(false));

    PrototypeObjectType named = new PrototypeObjectType(r, "Foo", null);
    assertEquals("Foo", named.toStringHelper(true));
    assertEquals("Foo", named.toStringHelper(false));

    named.setPrettyPrint(true);
    assertTrue(named.isPrettyPrint());
    named.defineProperty("a", number, false, null);
    named.defineProperty(
        "b", r.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    String printed = named.toStringHelper(false);
    assertTrue(printed.startsWith("{"));
    assertTrue(printed.endsWith("}"));
    assertTrue(printed.contains("a:"));
    assertTrue(printed.contains("b:"));
    assertTrue(named.isPrettyPrint());
  }

  @Test(timeout = 4000)
  public void testToStringHelperPrettyPrintTruncatesAtFourProperties() {
    JSTypeRegistry r = newRegistry();
    JSType number = r.getNativeType(JSTypeNative.NUMBER_TYPE);

    PrototypeObjectType obj = new PrototypeObjectType(r, null, null);
    obj.setPrettyPrint(true);
    for (int i = 0; i < 5; i++) {
      obj.defineProperty("p" + i, number, false, null);
    }

    String limited = obj.toStringHelper(false);
    assertTrue(limited.contains("..."));
    assertFalse(limited.contains("p4"));

    String all = obj.toStringHelper(true);
    assertFalse(all.contains("..."));
    assertTrue(all.contains("p4"));
  }

  @Test(timeout = 4000)
  public void testSetImplicitPrototype() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);
    ObjectType parent = new PrototypeObjectType(r, "P", null);

    obj.setImplicitPrototype(parent);
    assertSame(parent, obj.getImplicitPrototype());

    obj.setImplicitPrototype(null);
    assertNull(obj.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testOwnerFunctionReferenceName() {
    JSTypeRegistry r = newRegistry();
    FunctionType fn =
        r.getNativeFunctionType(JSTypeNative.FUNCTION_FUNCTION_TYPE);

    PrototypeObjectType proto = new PrototypeObjectType(r, null, null);
    assertNull(proto.getOwnerFunction());
    assertFalse(proto.hasReferenceName());

    proto.setOwnerFunction(fn);
    assertSame(fn, proto.getOwnerFunction());
    assertTrue(proto.hasReferenceName());
    assertEquals(fn.getReferenceName() + ".prototype", proto.getReferenceName());

    proto.setOwnerFunction(null);
    assertNull(proto.getOwnerFunction());
    assertFalse(proto.hasReferenceName());
  }

  @Test(timeout = 4000)
  public void testDefaultCtorInterfacesAreEmpty() {
    JSTypeRegistry r = newRegistry();
    PrototypeObjectType obj = new PrototypeObjectType(r, "O", null);

    assertFalse(obj.getCtorImplementedInterfaces().iterator().hasNext());
    assertFalse(obj.getCtorExtendedInterfaces().iterator().hasNext());
    assertNull(obj.getConstructor());
  }
}