package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.util.NameTransformer;

/* [Branch & Defect Analysis Matrix]
 *
 * Target: BeanPropertyMap
 * Known Defect (Defects4J): NoSuchElementException when removing property
 *   in case-insensitive mode due to incorrect key comparison in `remove()`.
 *   The method compares lowercased key with `prop.getName()` (original case)
 *   instead of lowercased property name (`getPropertyName(prop)`).
 *
 * Key Branches:
 *   - `_caseInsensitive` flag: true/false
 *   - Hash slot: primary, secondary, spill area
 *   - `find()`: null key, empty slot, match in primary/secondary/spill
 *   - `remove()`: property not found (NoSuchElementException), found in primary/secondary/spill
 *   - `withProperty()`: replace existing vs add new, slot allocation
 *   - `replace()`: property found vs not found
 *   - `_findIndexInHash()`: primary, secondary, spill, no match
 *   - `_findFromOrdered()`: property found, not found (IllegalStateException)
 *   - `withoutProperties()`, `renameAll()`, `assignIndexes()`
 *   - Constructor: null property skipping, collection with nulls
 *   - Boundary: empty collection, hash size transitions (<=5, <=12, larger)
 *
 * Partitions:
 * A. Core Functional Logic & State Transitions
 * B. Boundary Analysis (nulls, extremes, empty)
 * C. Defect-Targeted (case-insensitive removal)
 * D. Exception Paths (illegal arguments, non-existent elements)
 * E. Lifecycle/Contract (no clone/serialization tests)
 */

public class BeanPropertyMapDeepseekTest {
    
    // Minimal SettableBeanProperty implementation for testing
    private static class MockProperty extends SettableBeanProperty {
        private final String name;
        private int propertyIndex;

        public MockProperty(String name) {
            this.name = name;
            this.propertyIndex = -1;
        }

        @Override
        public String getName() { return name; }

        @Override
        public int getPropertyIndex() { return propertyIndex; }

        @Override
        public void assignIndex(int index) { this.propertyIndex = index; }

        @Override
        public JavaType getType() { return null; }

        @Override
        public PropertyName getFullName() { return null; }

        @Override
        public Annotations getMetadata() { return null; }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> cls) { return null; }

        @Override
        public Object deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return null;
        }

        @Override
        public void set(Object instance, Object value) throws IOException { }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object fixAccess(DeserializationContext ctxt) { return this; }

        @Override
        public void markAsIgnorable() { }

        @Override
        public boolean isIgnorable() { return false; }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }

        @Override
        public SettableBeanProperty withName(String name) {
            return new MockProperty(name);
        }

        @Override
        public SettableBeanProperty withSimpleName(String simpleName) {
            return new MockProperty(simpleName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) { return this; }

        @Override
        public SettableBeanProperty withAccessor(Class<?> ref) { return this; }

        @Override
        public int getCreatorIndex() { return -1; }
    }

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyMap() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.<SettableBeanProperty>emptyList());
        assertEquals(0, map.size());
        assertNull(map.find("any"));
        assertFalse(map.iterator().hasNext());
        assertEquals("Properties=[]", map.toString());
    }

    @Test(timeout = 4000)
    public void testSinglePropertyCaseInsensitiveFind() {
        MockProperty prop = new MockProperty("Name");
        BeanPropertyMap map = new BeanPropertyMap(true, Collections.singletonList(prop));
        assertEquals(1, map.size());
        // Case-insensitive lookup
        assertSame(prop, map.find("name"));
        assertSame(prop, map.find("NAME"));
        assertSame(prop, map.find("Name"));
        // Null key throws
        try {
            map.find(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSinglePropertyCaseSensitiveFind() {
        MockProperty prop = new MockProperty("Name");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(prop));
        assertSame(prop, map.find("Name"));
        assertNull(map.find("name"));
        assertNull(map.find("NAME"));
    }

    @Test(timeout = 4000)
    public void testMultiplePropertiesOrderAndIteration() {
        MockProperty p1 = new MockProperty("first");
        MockProperty p2 = new MockProperty("second");
        MockProperty p3 = new MockProperty("third");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, p2, p3));
        assertEquals(3, map.size());
        // Insertion order preserved
        assertArrayEquals(new SettableBeanProperty[]{p1, p2, p3}, map.getPropertiesInInsertionOrder());
        // Iterator order is arbitrary (hash order) but we can check all present
        Set<SettableBeanProperty> iterSet = new HashSet<>();
        for (SettableBeanProperty prop : map) {
            iterSet.add(prop);
        }
        assertEquals(new HashSet<>(Arrays.asList(p1, p2, p3)), iterSet);
    }

    @Test(timeout = 4000)
    public void testFindByIndex() {
        MockProperty p1 = new MockProperty("a");
        MockProperty p2 = new MockProperty("b");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, p2));
        map.assignIndexes();
        // Indexes are assigned in hash order (not insertion order)
        assertNotNull(map.find(0));
        assertNotNull(map.find(1));
        assertNull(map.find(5));
    }

    @Test(timeout = 4000)
    public void testAssignIndexes() {
        MockProperty p1 = new MockProperty("x");
        MockProperty p2 = new MockProperty("y");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, p2));
        map.assignIndexes();
        assertTrue(p1.getPropertyIndex() >= 0);
        assertTrue(p2.getPropertyIndex() >= 0);
        assertNotEquals(p1.getPropertyIndex(), p2.getPropertyIndex());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullPropertyInCollection() {
        MockProperty p1 = new MockProperty("real");
        // null should be skipped
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, null));
        assertEquals(1, map.size()); // null skipped
        assertNotNull(map.find("real"));
    }

    @Test(timeout = 4000)
    public void testManyPropertiesSpillover() {
        // More than 8 properties to exercise spill area
        List<SettableBeanProperty> props = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            props.add(new MockProperty("prop" + i));
        }
        BeanPropertyMap map = new BeanPropertyMap(false, props);
        assertEquals(15, map.size());
        // All properties findable
        for (SettableBeanProperty prop : props) {
            assertSame(prop, map.find(prop.getName()));
        }
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (case-insensitive removal)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCaseInsensitiveRemove() {
        // The known defect: remove fails with NoSuchElementException
        // because key comparison uses prop.getName() (original case) instead of
        // lowercased getPropertyName(prop). We test that removal works.
        MockProperty prop = new MockProperty("BusinessAddress");
        BeanPropertyMap map = new BeanPropertyMap(true, Collections.singletonList(prop));
        assertEquals(1, map.size());
        // Remove should succeed
        map.remove(prop);
        assertEquals(0, map.size());
        assertNull(map.find("businessaddress"));
        assertNull(map.find("BusinessAddress"));
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveRemoveMultiple() {
        MockProperty p1 = new MockProperty("ALLCAPS");
        MockProperty p2 = new MockProperty("lower");
        BeanPropertyMap map = new BeanPropertyMap(true, Arrays.asList(p1, p2));
        map.remove(p2);
        assertEquals(1, map.size());
        assertNull(map.find("lower"));
        assertNotNull(map.find("ALLCAPS"));
        map.remove(p1);
        assertEquals(0, map.size());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFindNullKey() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.<SettableBeanProperty>emptyList());
        map.find(null);
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testRemoveNonExistent() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(new MockProperty("a")));
        map.remove(new MockProperty("b"));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testReplaceNonExistent() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(new MockProperty("a")));
        map.replace(new MockProperty("b"));
    }

    @Test(timeout = 4000)
    public void testReplaceExisting() {
        MockProperty original = new MockProperty("key");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(original));
        MockProperty replacement = new MockProperty("key");
        map.replace(replacement);
        assertSame(replacement, map.find("key"));
        // _propsInOrder also replaced
        assertEquals(replacement, map.getPropertiesInInsertionOrder()[0]);
    }

    // -----------------------------------------------------------------------
    // Partition E: withProperty, withoutProperties, renameAll, toString
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWithPropertyAdd() {
        MockProperty p1 = new MockProperty("a");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(p1));
        MockProperty p2 = new MockProperty("b");
        map = map.withProperty(p2);
        assertEquals(2, map.size());
        assertSame(p2, map.find("b"));
    }

    @Test(timeout = 4000)
    public void testWithPropertyReplace() {
        MockProperty p1 = new MockProperty("a");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(p1));
        MockProperty p2 = new MockProperty("a"); // same name
        map = map.withProperty(p2);
        assertEquals(1, map.size());
        assertSame(p2, map.find("a"));
    }

    @Test(timeout = 4000)
    public void testWithoutProperties() {
        MockProperty p1 = new MockProperty("keep");
        MockProperty p2 = new MockProperty("remove");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, p2));
        map = map.withoutProperties(Collections.singletonList("remove"));
        assertEquals(1, map.size());
        assertNotNull(map.find("keep"));
        assertNull(map.find("remove"));
    }

    @Test(timeout = 4000)
    public void testWithoutPropertiesEmptyExclude() {
        MockProperty p = new MockProperty("a");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(p));
        assertSame(map, map.withoutProperties(Collections.<String>emptyList()));
    }

    @Test(timeout = 4000)
    public void testRenameAll() {
        MockProperty p = new MockProperty("old");
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(p));
        // Create a NameTransformer that adds prefix
        NameTransformer xf = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "new_" + name;
            }
            @Override
            public NameTransformer revert() { return this; }
        };
        BeanPropertyMap renamed = map.renameAll(xf);
        assertEquals(1, renamed.size());
        SettableBeanProperty renamedProp = renamed.find("new_old");
        assertNotNull(renamedProp);
        assertEquals("new_old", renamedProp.getName());
    }

    @Test(timeout = 4000)
    public void testRenameAllWithNopTransformer() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.singletonList(new MockProperty("x")));
        assertSame(map, map.renameAll(NameTransformer.NOP));
        assertSame(map, map.renameAll(null));
    }

    @Test(timeout = 4000)
    public void testToString() {
        MockProperty p1 = new MockProperty("a");
        MockProperty p2 = new MockProperty("b");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.asList(p1, p2));
        String str = map.toString();
        assertTrue(str.startsWith("Properties=["));
        assertTrue(str.contains("a"));
        assertTrue(str.contains("b"));
        assertTrue(str.endsWith("]"));
    }

    // Additional edge: case-insensitive withProperty (should replace correctly)
    @Test(timeout = 4000)
    public void testWithPropertyCaseInsensitiveReplace() {
        MockProperty p1 = new MockProperty("Key");
        BeanPropertyMap map = new BeanPropertyMap(true, Collections.singletonList(p1));
        // Using withProperty with same lowercased name should replace existing entry
        MockProperty p2 = new MockProperty("key");
        map = map.withProperty(p2);
        assertEquals(1, map.size());
        assertSame(p2, map.find("key"));
        // Also verify with original casing
        assertSame(p2, map.find("Key"));
    }
}