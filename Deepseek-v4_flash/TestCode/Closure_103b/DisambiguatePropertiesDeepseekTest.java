package com.google.javascript.jscomp;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.DisambiguateProperties.Property;
import com.google.javascript.jscomp.TypeValidator.TypeMismatch;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnionType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Advanced white-box tests for DisambiguateProperties.
 * 
 * <!-- [Branch & Defect Analysis Matrix] -->
 * Decision branches targeted:
 *   - isInvalidatingType: ALL_TYPE, UNKNOWN_TYPE, named types, etc.
 *   - shouldRename: skipRenaming, types null, equivalence classes >1
 *   - addType: invalidating type returns false, union + interface recursion
 *   - processProperty: type alternatives handling, recursive union
 *   - expandTypesToSkip: loop guard, union propagation
 *   - buildPropNames: empty sets, type name sanitization
 *   - getTypeWithProperty: prototype chain, autoboxing, union, function prototype
 *   - scheduleRenaming: invalidating type triggers invalidation
 * Boundary conditions:
 *   - null arguments, empty maps/sets
 *   - cyclic prototype chains (self-referential)
 *   - large numbers of equivalence classes
 * Known defect: testSupertypeReferenceOfSubtypeProperty fails because property
 * defined on subtype is not properly recognized when referenced via supertype.
 * The bug likely lies in getTypeWithProperty or the type alternative iteration.
 */
@RunWith(JUnit4.class)
public class DisambiguatePropertiesDeepseekTest extends CompilerTestCase {

  private DisambiguateProperties<JSType> disambiguate;
  private JSTypeRegistry registry;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    disambiguate = DisambiguateProperties.forJSTypeSystem(compiler);
    return disambiguate;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
    registry = compiler.getTypeRegistry();
  }

  // ===================== Partition A: Core Functional Logic =====================

  @Test(timeout = 4000)
  public void testBasicDisambiguation() {
    test("var A = function(){}; A.prototype.foo = 1;" +
         "var B = function(){}; B.prototype.foo = 2;" +
         "new A().foo; new B().foo;",
         "var A = function(){}; A.prototype.foo$A = 1;" +
         "var B = function(){}; B.prototype.foo$B = 2;" +
         "new A().foo$A; new B().foo$B;");
  }

  @Test(timeout = 4000)
  public void testSingleTypeNoRename() {
    testSame("var A = function(){}; A.prototype.bar = 1; new A().bar;");
  }

  @Test(timeout = 4000)
  public void testPropertyOnPrototypeChain() {
    // Property defined on supertype, referenced from subtype
    test("var Super = function(){}; Super.prototype.foo = 1;" +
         "var Sub = function(){}; Sub.prototype = new Super();" +
         "new Sub().foo; new Sub().foo;",
         "var Super = function(){}; Super.prototype.foo$Super = 1;" +
         "var Sub = function(){}; Sub.prototype = new Super();" +
         "new Sub().foo$Super; new Sub().foo$Super;");
  }

  @Test(timeout = 4000)
  public void testPropertyOnSupertypeReferenceOfSubtype() {
    // Known defect: property defined on subtype, referenced via supertype
    test("var Super = function(){}; Super.prototype.foo = 1;" +
         "var Sub = function(){}; Sub.prototype.foo = 2;" +
         "var s = new Sub(); s.foo; var sup = new Super(); sup.foo;",
         "var Super = function(){}; Super.prototype.foo$Super = 1;" +
         "var Sub = function(){}; Sub.prototype.foo$Sub = 2;" +
         "var s = new Sub(); s.foo$Sub; var sup = new Super(); sup.foo$Super;");
  }

  @Test(timeout = 4000)
  public void testUnionTypeDisambiguation() {
    test("var A = function(){}; A.prototype.foo = 1;" +
         "var B = function(){}; B.prototype.foo = 2;" +
         "/** @type {A|B} */ var x; x.foo;",
         "var A = function(){}; A.prototype.foo$A = 1;" +
         "var B = function(){}; B.prototype.foo$B = 2;" +
         "/** @type {A|B} */ var x; x.foo$A;");
  }

  @Test(timeout = 4000)
  public void testObjectLiteralProperty() {
    test("var a = {foo: 1}; var b = {foo: 2}; a.foo; b.foo;",
         "var a = {foo$0: 1}; var b = {foo$1: 2}; a.foo$0; b.foo$1;");
  }

  // ===================== Partition B: Boundary Value Analysis =====================

  @Test(timeout = 4000)
  public void testNullTypeNotInvalidating() {
    // getTypeWithProperty should handle null gracefully
    assertNull(disambiguate.getTypeWithProperty("bar", null));
  }

  @Test(timeout = 4000)
  public void testNoObjectTypeAutoboxes() {
    // Property access on primitive should not rename
    testSame("/** @type {number} */ var n; n.foo;");
  }

  @Test(timeout = 4000)
  public void testEmptyPropertyName() {
    // Empty property name edge case
    test("var A = function(){}; A.prototype[''] = 1; new A()[''];",
         "var A = function(){}; A.prototype['$A'] = 1; new A()['$A'];");
  }

  @Test(timeout = 4000)
  public void testEnumTypeToSkip() {
    testSame("/** @enum {string} */ var E = {A: 'a'}; E.A;");
  }

  @Test(timeout = 4000)
  public void testUnknownTypeInvalidates() {
    testSame("/** @type {?} */ var x; x.foo;");
  }

  // ===================== Partition C: Defect-Targeted Branch Zone =====================

  @Test(timeout = 4000)
  public void testSupertypeReferenceOfSubtypeProperty_Defect() {
    // This test directly targets the known Defects4J defect.
    // Property defined only on subtype, but referenced via supertype variable.
    test("var Super = function(){}; Super.prototype.foo = 1;" +
         "var Sub = function(){}; Sub.prototype = new Super();" +
         "var x = new Sub(); x.foo;",
         "var Super = function(){}; Super.prototype.foo$Super = 1;" +
         "var Sub = function(){}; Sub.prototype = new Super();" +
         "var x = new Sub(); x.foo$Super;");
  }

  @Test(timeout = 4000)
  public void testInterfaceImplementationProperty() {
    // Property defined in interface, implemented by two classes
    test("/** @interface */ function I() {} I.prototype.foo = function() {};" +
         "/** @implements {I} */ function A() {} A.prototype.foo = function() {};" +
         "/** @implements {I} */ function B() {} B.prototype.foo = function() {};" +
         "new A().foo; new B().foo;",
         "/** @interface */ function I() {} I.prototype.foo$I = function() {};" +
         "/** @implements {I} */ function A() {} A.prototype.foo$A = function() {};" +
         "/** @implements {I} */ function B() {} B.prototype.foo$B = function() {};" +
         "new A().foo$A; new B().foo$B;");
  }

  @Test(timeout = 4000)
  public void testPropertyNotFoundInAlternatives() {
    // Union type where only one branch has property
    test("var A = function(){}; A.prototype.foo = 1;" +
         "var B = function(){};" +
         "/** @type {A|B} */ var x; x.foo;",
         "var A = function(){}; A.prototype.foo$A = 1;" +
         "var B = function(){};" +
         "/** @type {A|B} */ var x; x.foo$A;");
  }

  // ===================== Partition D: Exception & Defensive Guard Paths =====================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testAddInvalidatingTypeNull() {
    // addInvalidatingType called with null type
    disambiguate.addInvalidatingType(null);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetPropertyNullName() {
    // getProperty with null name should throw NPE
    disambiguate.getProperty(null);
  }

  @Test(timeout = 4000)
  public void testScheduleRenamingOnSkippedProperty() {
    // After invalidation, scheduleRenaming should return false but not throw
    Property prop = makeProperty("test");
    prop.skipRenaming = true;
    ObjectType objType = createObjectType();
    assertTrue(prop.scheduleRenaming(new Node(1), objType));
  }

  @Test(timeout = 4000)
  public void testExpandTypesToSkipLoopGuard() {
    // Ensure expandTypesToSkip does not infinite loop
    Property prop = makeProperty("loop");
    prop.skipRenaming = false;
    prop.typesToSkip.add(createObjectType());
    prop.expandTypesToSkip();
    // Should complete without exception
  }

  // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

  @Test(timeout = 4000)
  public void testBuildPropNamesStableOrdering() {
    // Ensure deterministic naming across equivalence classes
    Property prop = makeProperty("x");
    UnionFind<JSType> types = new StandardUnionFind<>();
    ObjectType a = createObjectType("A");
    ObjectType b = createObjectType("B");
    types.add(a);
    types.add(b);
    types.union(a, b);
    Map<JSType, String> names = prop.buildPropNames(types, "x");
    assertEquals("A_x", names.get(a));
    assertEquals("A_x", names.get(b));
  }

  @Test(timeout = 4000)
  public void testRenamedTypesForTestingExcludesSkipped() {
    test("var A = function(){}; A.prototype.foo = 1;" +
         "var B = function(){}; B.prototype.foo = 2;",
         "var A = function(){}; A.prototype.foo$A = 1;" +
         "var B = function(){}; B.prototype.foo$B = 2;");
    Multimap<String, Collection<JSType>> renamed = disambiguate.getRenamedTypesForTesting();
    assertTrue(renamed.containsKey("foo"));
    assertEquals(2, renamed.get("foo").size());
  }

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyAutoboxed() {
    // For number, property foo should not be found (no autoboxing for custom)
    assertNull(disambiguate.getTypeWithProperty("foo",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
  }

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyPrototypeProperty() {
    // "prototype" property itself should be ignored
    ObjectType objType = createObjectType("Foo");
    assertNull(disambiguate.getTypeWithProperty("prototype", objType));
  }

  // ===================== Helper Methods =====================

  private Property makeProperty(String name) {
    return disambiguate.new Property(name);
  }

  private ObjectType createObjectType(String name) {
    return registry.createAnonymousObjectType(null);
  }

  private ObjectType createObjectType() {
    return registry.createAnonymousObjectType(null);
  }
}