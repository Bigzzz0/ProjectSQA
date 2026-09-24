/*
 * [Branch & Defect Analysis Matrix]
 * Class under Test: com.google.javascript.rhino.jstype.RecordTypeBuilder
 *
 * Decision / Branch Matrix:
 * 1. addProperty(String name, JSType type, Node propertyNode)
 *    - Branch 1.1: properties.containsKey(name) == true
 *      -> returns null (duplicate property detected, prevents duplicate keys)
 *    - Branch 1.2: properties.containsKey(name) == false
 *      -> stores new RecordProperty(type, propertyNode), returns `this`
 *    - State Side-Effect: isEmpty is mutated from true to false unconditionally
 *
 * 2. build()
 *    - Branch 2.1: isEmpty == true
 *      -> returns registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE)
 *    - Branch 2.2: isEmpty == false
 *      -> constructs and returns new RecordType(registry, unmodifiableMap(properties))
 *
 * 3. RecordProperty (Inner Class)
 *    - Constructor: RecordProperty(JSType, Node)
 *    - getType(): returns type
 *    - getPropertyNode(): returns propertyNode
 *
 * Defect-Targeted Branch Zone:
 * - Closure Compiler Issue 725 (com.google.javascript.jscomp.TypeCheckTest::testIssue725):
 *   Target: Detection of duplicate property keys in record types (e.g., "{a: number, a: number}").
 *   When a duplicate key is encountered, addProperty must return null so callers (parsers/type-checkers)
 *   can emit a duplicate key warning. If duplicate detection fails, no warning is emitted.
 */

package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

public class RecordTypeBuilderGeminiTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyRecordBuildReturnsNativeObjectType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType result = builder.build();

    assertNotNull("Built type must not be null", result);
    assertTrue("Empty record must resolve to ObjectType", result.isObjectType());
    assertFalse("Empty record must not be a RecordType instance", result.isRecordType());
    assertEquals("Empty record must return native OBJECT_TYPE",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testSinglePropertyAdditionAndBuild() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node propNode = Node.newString("age");

    RecordTypeBuilder ret = builder.addProperty("age", numberType, propNode);
    assertSame("addProperty must return this on successful addition", builder, ret);

    JSType builtType = builder.build();
    assertNotNull("Built type must not be null", builtType);
    assertTrue("Non-empty record must be a RecordType", builtType.isRecordType());
    assertTrue("Record must have added property 'age'", builtType.toObjectType().hasProperty("age"));
    assertEquals("Property type must match the registered type",
        numberType, builtType.toObjectType().getPropertyType("age"));
  }

  @Test(timeout = 4000)
  public void testMethodChaining() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node nodeA = Node.newString("a");
    Node nodeB = Node.newString("b");

    RecordTypeBuilder chained = builder
        .addProperty("a", numberType, nodeA)
        .addProperty("b", stringType, nodeB);

    assertSame("Chained calls must return the same builder instance", builder, chained);
    JSType builtType = chained.build();
    assertTrue(builtType.isRecordType());
    assertTrue(builtType.toObjectType().hasProperty("a"));
    assertTrue(builtType.toObjectType().hasProperty("b"));
  }

  @Test(timeout = 4000)
  public void testMultiplePropertiesAdditionAndBuild() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node nodeX = Node.newString("x");
    Node nodeY = Node.newString("y");
    Node nodeZ = Node.newString("z");

    builder.addProperty("x", numberType, nodeX);
    builder.addProperty("y", stringType, nodeY);
    builder.addProperty("z", booleanType, nodeZ);

    JSType result = builder.build();
    assertTrue(result.isRecordType());
    ObjectType objType = result.toObjectType();

    assertTrue(objType.hasProperty("x"));
    assertTrue(objType.hasProperty("y"));
    assertTrue(objType.hasProperty("z"));
    assertEquals(numberType, objType.getPropertyType("x"));
    assertEquals(stringType, objType.getPropertyType("y"));
    assertEquals(booleanType, objType.getPropertyType("z"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyStringPropertyName() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node emptyStrNode = Node.newString("");

    RecordTypeBuilder ret = builder.addProperty("", stringType, emptyStrNode);
    assertSame("Empty string property name should be accepted", builder, ret);

    JSType builtType = builder.build();
    assertTrue("Record with empty string property must be RecordType", builtType.isRecordType());
    assertTrue(builtType.toObjectType().hasProperty(""));
    assertEquals(stringType, builtType.toObjectType().getPropertyType(""));
  }

  @Test(timeout = 4000)
  public void testNullPropertyNameAllowedInBuilder() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("nullProp");

    RecordTypeBuilder ret = builder.addProperty(null, numberType, node);
    assertSame("Null property name should be accepted by builder map", builder, ret);
  }

  @Test(timeout = 4000)
  public void testNullPropertyTypeAllowed() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("foo");

    RecordTypeBuilder ret = builder.addProperty("foo", null, node);
    assertSame(builder, ret);

    JSType builtType = builder.build();
    assertTrue(builtType.isRecordType());
    assertTrue(builtType.toObjectType().hasProperty("foo"));
    assertNull("Property type should be null when initialized with null",
        builtType.toObjectType().getPropertyType("foo"));
  }

  @Test(timeout = 4000)
  public void testNullPropertyNodeAllowed() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);

    RecordTypeBuilder ret = builder.addProperty("bar", numberType, null);
    assertSame(builder, ret);

    JSType builtType = builder.build();
    assertTrue(builtType.isRecordType());
    assertTrue(builtType.toObjectType().hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testRecordPropertyClassDirectAccess() {
    Node node = Node.newString("test");
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(stringType, node);

    assertSame("getType must return configured JSType", stringType, prop.getType());
    assertSame("getPropertyNode must return configured Node", node, prop.getPropertyNode());
  }

  @Test(timeout = 4000)
  public void testRecordPropertyClassWithNullValues() {
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(null, null);

    assertNull("getType must return null", prop.getType());
    assertNull("getPropertyNode must return null", prop.getPropertyNode());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 725)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue725DuplicatePropertyReturnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node firstNode = Node.newString("prop");
    Node duplicateNode = Node.newString("prop");

    RecordTypeBuilder firstAdd = builder.addProperty("prop", numberType, firstNode);
    assertNotNull("First addition of property 'prop' must succeed", firstAdd);

    // Issue 725: Duplicate key in record type must return null so caller emits a warning
    RecordTypeBuilder duplicateAdd = builder.addProperty("prop", numberType, duplicateNode);
    assertNull("Adding duplicate property 'prop' must return null to trigger warning for issue 725", duplicateAdd);
  }

  @Test(timeout = 4000)
  public void testIssue725DuplicatePropertyPreservesOriginalProperty() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node firstNode = Node.newString("foo");
    Node duplicateNode = Node.newString("foo");

    builder.addProperty("foo", numberType, firstNode);
    RecordTypeBuilder duplicateResult = builder.addProperty("foo", stringType, duplicateNode);
    assertNull("Duplicate addition must return null", duplicateResult);

    JSType builtType = builder.build();
    assertTrue(builtType.isRecordType());
    ObjectType objType = builtType.toObjectType();

    assertTrue("Record must still have 'foo'", objType.hasProperty("foo"));
    assertEquals("Original type must not be overwritten by duplicate addition",
        numberType, objType.getPropertyType("foo"));
  }

  @Test(timeout = 4000)
  public void testIssue725MultipleDuplicateKeys() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = Node.newString("k1");
    Node node2 = Node.newString("k2");

    assertNotNull(builder.addProperty("k1", numberType, node1));
    assertNotNull(builder.addProperty("k2", stringType, node2));

    assertNull("Duplicate addition of k1 must return null", builder.addProperty("k1", booleanType, node1));
    assertNull("Duplicate addition of k2 must return null", builder.addProperty("k2", booleanType, node2));

    Node node3 = Node.newString("k3");
    assertNotNull("New distinct property k3 must succeed", builder.addProperty("k3", booleanType, node3));
  }

  @Test(timeout = 4000)
  public void testIssue725DuplicateEmptyStringKeyReturnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("");

    assertNotNull("Initial empty string property must succeed", builder.addProperty("", numberType, node));
    assertNull("Duplicate empty string property must return null", builder.addProperty("", stringType, node));
  }

  @Test(timeout = 4000)
  public void testIssue725DuplicateNullKeyReturnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("test");

    assertNotNull("Initial null property name must succeed", builder.addProperty(null, numberType, node));
    assertNull("Duplicate null property name must return null", builder.addProperty(null, stringType, node));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testBuildWithNullRegistryThrowsNullPointerException() {
    RecordTypeBuilder builder = new RecordTypeBuilder(null);
    builder.build();
  }

  @Test(timeout = 4000)
  public void testAddPropertyDoesNotThrowEvenWithNullRegistry() {
    RecordTypeBuilder builder = new RecordTypeBuilder(null);
    Node node = Node.newString("prop");
    RecordTypeBuilder res = builder.addProperty("prop", numberType, node);
    assertSame("addProperty should not dereference registry", builder, res);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & State Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsEmptyFlagTransitionOnDuplicate() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("first");

    builder.addProperty("first", numberType, node);
    builder.addProperty("first", numberType, node); // returns null, isEmpty remains false

    JSType result = builder.build();
    assertTrue("Builder must remain non-empty even after duplicate rejected", result.isRecordType());
  }

  @Test(timeout = 4000)
  public void testBuildIdempotenceOnEmptyBuilder() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType first = builder.build();
    JSType second = builder.build();

    assertSame("Subsequent calls to build() on empty builder must return identical instance", first, second);
  }

  @Test(timeout = 4000)
  public void testBuildIdempotenceOnNonEmptyBuilder() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("val", numberType, Node.newString("val"));

    JSType first = builder.build();
    JSType second = builder.build();

    assertNotNull(first);
    assertNotNull(second);
    assertTrue(first.isRecordType());
    assertTrue(second.isRecordType());
    assertTrue(first.isEquivalentTo(second));
  }

  @Test(timeout = 4000)
  public void testAddPropertyAfterBuild() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("prop1", numberType, Node.newString("prop1"));
    JSType type1 = builder.build();
    assertTrue(type1.toObjectType().hasProperty("prop1"));

    RecordTypeBuilder addRet = builder.addProperty("prop2", stringType, Node.newString("prop2"));
    assertSame("Builder should allow further additions after build", builder, addRet);

    JSType type2 = builder.build();
    assertTrue(type2.toObjectType().hasProperty("prop1"));
    assertTrue(type2.toObjectType().hasProperty("prop2"));
  }
}