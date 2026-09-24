package com.fasterxml.jackson.databind.deser.impl;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap
 *
 * 1. DEFECT UNDER TEST (Defects4J Ground Truth):
 *    - com.fasterxml.jackson.databind.struct.TestUnwrapped::testCaseInsensitiveUnwrap
 *    - Root Cause: In remove(SettableBeanProperty propToRm), when _caseInsensitive is true,
 *      'key' is lower-cased via getPropertyName(propToRm), but the matching condition mistakenly
 *      evaluates 'found = key.equals(prop.getName())'. Since prop.getName() retains original case
 *      (e.g., 'businessAddress'), comparing against lower-cased key ('businessaddress') yields false.
 *      This triggers NoSuchElementException ("No entry 'businessAddress' found, can't remove").
 *
 * 2. SYSTEMATIC EQUIVALENCE PARTITIONS & BRANCH COVERAGE:
 *    - Partition A: Core Functional Logic & State Transitions
 *      * Normal lookups (find), iterator traversal, size accessor.
 *      * withCaseInsensitivity state transitions (same state returns this, different state re-inits).
 *      * assignIndexes assigning sequential indices and find(int index).
 *      * replace in-place updating _hashArea and _propsInOrder.
 *      * toString formatting with empty and non-empty property sets.
 *    - Partition B: Boundary Value Analysis (BVA) & Extremes
 *      * findSize boundary bins: size <= 5 (size 8), size <= 12 (size 16), size 13 (size 32),
 *        size 30 (size 64), size 60 (size 128).
 *      * Empty map behavior (size 0, empty iterator, find returns null).
 *      * Holes / null entries in props collection during init and in _propsInOrder.
 *    - Partition C: Defect-Targeted Branch Zone & Hash Collisions
 *      * Case-insensitive removal of mixed-case property names (reproducing defect).
 *      * Hash collision paths: Primary slot, Secondary slot (slot >> 1), Spill area, and
 *        spill array expansion (ix >= hashed.length).
 *      * _find2 collision handling: secondary match, spill-over match, null secondary, not found.
 *      * withProperty branches: replace existing vs append (primary free, secondary free, spill expansion).
 *      * renameAll branches: null transformer, NameTransformer.NOP, active unwrapping deserializers
 *        (newDeser != deser vs newDeser == deser), retaining holes in _propsInOrder.
 *      * withoutProperties branches: empty exclusion set, filtering with holes in _propsInOrder.
 *    - Partition D: Defensive Guards & Exception Paths
 *      * find(null) -> IllegalArgumentException.
 *      * replace(nonExistentProp) -> NoSuchElementException.
 *      * remove(nonExistentProp) -> NoSuchElementException.
 *      * findDeserializeAndSet and wrapAndThrow paths:
 *        - Key not found -> returns false.
 *        - Error thrown -> rethrown unmodified.
 *        - IOException thrown -> rethrown unmodified.
 *        - JsonProcessingException -> wrapped into JsonMappingException.
 *        - RuntimeException -> wrapped into JsonMappingException.
 *        - InvocationTargetException -> unwrapped cause and handled.
 *    - Partition E: Object Lifecycle & Serialization Integrity
 *      * Java Serialization and Deserialization round-trip preserving state and lookup capability.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyMapGeminiTest {

    // =========================================================================
    // Test Stub Infrastructure
    // =========================================================================

    private static class DummyProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private int _index = -1;
        private Throwable _throwOnDeserialize;

        public DummyProperty(String name) {
            super(new PropertyName(name), TypeFactory.unknownType(), PropertyMetadata.STD_OPTIONAL, null);
        }

        public DummyProperty(PropertyName name, JsonDeserializer<Object> deser) {
            super(name, TypeFactory.unknownType(), PropertyMetadata.STD_OPTIONAL, deser);
        }

        public DummyProperty withExceptionOnDeserialize(Throwable t) {
            this._throwOnDeserialize = t;
            return this;
        }

        @Override
        public void assignIndex(int index) {
            _index = index;
        }

        @Override
        public int getPropertyIndex() {
            return _index;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            DummyProperty copy = new DummyProperty(getFullName(), (JsonDeserializer<Object>) deser);
            copy._index = this._index;
            copy._throwOnDeserialize = this._throwOnDeserialize;
            return copy;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            DummyProperty copy = new DummyProperty(newName, getValueDeserializer());
            copy._index = this._index;
            copy._throwOnDeserialize = this._throwOnDeserialize;
            return copy;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            if (_throwOnDeserialize != null) {
                if (_throwOnDeserialize instanceof IOException) {
                    throw (IOException) _throwOnDeserialize;
                }
                if (_throwOnDeserialize instanceof RuntimeException) {
                    throw (RuntimeException) _throwOnDeserialize;
                }
                if (_throwOnDeserialize instanceof Error) {
                    throw (Error) _throwOnDeserialize;
                }
                throw new RuntimeException(_throwOnDeserialize);
            }
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            deserializeAndSet(p, ctxt, instance);
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            return instance;
        }
    }

    private static class DummyUnwrappingDeserializer extends JsonDeserializer<Object> {
        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer transformer) {
            return new DummyUnwrappingDeserializer();
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    private static class DummySameDeserializer extends JsonDeserializer<Object> {
        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer transformer) {
            return this;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT: com.fasterxml.jackson.databind.struct.TestUnwrapped::testCaseInsensitiveUnwrap
     * Removing a property with mixed-case name ("businessAddress") from a case-insensitive
     * BeanPropertyMap fails with NoSuchElementException on the defective code because
     * 'found = key.equals(prop.getName())' compares lower-cased 'key' with mixed-case 'prop.getName()'.
     */
    @Test(timeout = 4000)
    public void testDefectCaseInsensitiveRemoveMixedCase() {
        SettableBeanProperty prop = new DummyProperty("businessAddress");
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        props.add(prop);
        BeanPropertyMap map = BeanPropertyMap.construct(props, true);

        assertEquals(1, map.size());
        assertNotNull(map.find("businessAddress"));
        assertNotNull(map.find("businessaddress"));
        assertNotNull(map.find("BUSINESSADDRESS"));

        // This operation triggers NoSuchElementException on defective version:
        map.remove(prop);

        assertEquals(0, map.size());
        assertNull(map.find("businessAddress"));
        assertNull(map.find("businessaddress"));
    }

    @Test(timeout = 4000)
    public void testDefectCaseInsensitiveRemoveMultiplePropsWithMixedCase() {
        SettableBeanProperty prop1 = new DummyProperty("businessAddress");
        SettableBeanProperty prop2 = new DummyProperty("homeAddress");
        List<SettableBeanProperty> props = Arrays.asList(prop1, prop2);
        BeanPropertyMap map = BeanPropertyMap.construct(props, true);

        assertEquals(2, map.size());
        map.remove(prop1);

        assertEquals(1, map.size());
        assertNull(map.find("businessAddress"));
        assertNotNull(map.find("homeaddress"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructAndFindCaseSensitive() {
        SettableBeanProperty propA = new DummyProperty("myProp");
        SettableBeanProperty propB = new DummyProperty("otherProp");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(propA, propB), false);

        assertEquals(2, map.size());
        assertSame(propA, map.find("myProp"));
        assertSame(propB, map.find("otherProp"));
        assertNull(map.find("myprop"));
        assertNull(map.find("MYPROP"));
    }

    @Test(timeout = 4000)
    public void testConstructAndFindCaseInsensitive() {
        SettableBeanProperty prop = new DummyProperty("mixedCaseName");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), true);

        assertEquals(1, map.size());
        assertSame(prop, map.find("mixedCaseName"));
        assertSame(prop, map.find("mixedcasename"));
        assertSame(prop, map.find("MIXEDCASENAME"));
    }

    @Test(timeout = 4000)
    public void testWithCaseInsensitivityTransitions() {
        SettableBeanProperty prop = new DummyProperty("propertyName");
        BeanPropertyMap mapFalse = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        // Same state returns this
        assertSame(mapFalse, mapFalse.withCaseInsensitivity(false));

        // Different state returns new instance with updated sensitivity
        BeanPropertyMap mapTrue = mapFalse.withCaseInsensitivity(true);
        assertNotSame(mapFalse, mapTrue);
        assertSame(mapTrue, mapTrue.withCaseInsensitivity(true));

        assertNull(mapFalse.find("PROPERTYNAME"));
        assertNotNull(mapTrue.find("PROPERTYNAME"));
    }

    @Test(timeout = 4000)
    public void testAssignIndexesAndFindByIndex() {
        SettableBeanProperty prop1 = new DummyProperty("p1");
        SettableBeanProperty prop2 = new DummyProperty("p2");
        SettableBeanProperty prop3 = new DummyProperty("p3");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2, prop3), false);

        assertSame(map, map.assignIndexes());

        int idx1 = prop1.getPropertyIndex();
        int idx2 = prop2.getPropertyIndex();
        int idx3 = prop3.getPropertyIndex();

        assertTrue(idx1 >= 0 && idx1 <= 2);
        assertTrue(idx2 >= 0 && idx2 <= 2);
        assertTrue(idx3 >= 0 && idx3 <= 2);

        assertSame(prop1, map.find(idx1));
        assertSame(prop2, map.find(idx2));
        assertSame(prop3, map.find(idx3));
        assertNull(map.find(999));
        assertNull(map.find(-1));
    }

    @Test(timeout = 4000)
    public void testIteratorAndPropertiesInInsertionOrder() {
        SettableBeanProperty prop1 = new DummyProperty("prop1");
        SettableBeanProperty prop2 = new DummyProperty("prop2");
        List<SettableBeanProperty> list = Arrays.asList(prop1, prop2);
        BeanPropertyMap map = BeanPropertyMap.construct(list, false);

        SettableBeanProperty[] inOrder = map.getPropertiesInInsertionOrder();
        assertEquals(2, inOrder.length);
        assertSame(prop1, inOrder[0]);
        assertSame(prop2, inOrder[1]);

        int count = 0;
        for (SettableBeanProperty p : map) {
            count++;
            assertTrue(p == prop1 || p == prop2);
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testToString() {
        SettableBeanProperty prop = new DummyProperty("age");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);
        String str = map.toString();
        assertTrue(str.startsWith("Properties=["));
        assertTrue(str.contains("age"));
        assertTrue(str.endsWith("]"));

        BeanPropertyMap emptyMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        assertEquals("Properties=[]", emptyMap.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyCollectionBoundaries() {
        BeanPropertyMap emptyMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        assertEquals(0, emptyMap.size());
        assertNull(emptyMap.find("anything"));
        assertNull(emptyMap.find(0));
        assertFalse(emptyMap.iterator().hasNext());
        assertEquals(0, emptyMap.getPropertiesInInsertionOrder().length);
    }

    @Test(timeout = 4000)
    public void testFindSizeBinBoundaries() {
        // Bin 1: size <= 5 -> alloc 8
        List<SettableBeanProperty> list5 = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 5; i++) {
            list5.add(new DummyProperty("prop_" + i));
        }
        BeanPropertyMap map5 = BeanPropertyMap.construct(list5, false);
        assertEquals(5, map5.size());

        // Bin 2: 5 < size <= 12 -> alloc 16
        List<SettableBeanProperty> list12 = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 12; i++) {
            list12.add(new DummyProperty("prop_" + i));
        }
        BeanPropertyMap map12 = BeanPropertyMap.construct(list12, false);
        assertEquals(12, map12.size());

        // Bin 3: size = 13 -> needed = 16, result = 32
        List<SettableBeanProperty> list13 = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 13; i++) {
            list13.add(new DummyProperty("prop_" + i));
        }
        BeanPropertyMap map13 = BeanPropertyMap.construct(list13, false);
        assertEquals(13, map13.size());

        // Bin 4: size = 30 -> needed = 37, result loop runs once -> 64
        List<SettableBeanProperty> list30 = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 30; i++) {
            list30.add(new DummyProperty("prop_" + i));
        }
        BeanPropertyMap map30 = BeanPropertyMap.construct(list30, false);
        assertEquals(30, map30.size());

        // Bin 5: size = 60 -> needed = 75, result loop runs twice -> 128
        List<SettableBeanProperty> list60 = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 60; i++) {
            list60.add(new DummyProperty("prop_" + i));
        }
        BeanPropertyMap map60 = BeanPropertyMap.construct(list60, false);
        assertEquals(60, map60.size());
    }

    @Test(timeout = 4000)
    public void testInitWithHolesNullProps() {
        List<SettableBeanProperty> propsWithHoles = new ArrayList<SettableBeanProperty>();
        SettableBeanProperty p1 = new DummyProperty("p1");
        SettableBeanProperty p2 = new DummyProperty("p2");
        propsWithHoles.add(p1);
        propsWithHoles.add(null);
        propsWithHoles.add(p2);

        BeanPropertyMap map = BeanPropertyMap.construct(propsWithHoles, false);
        assertSame(p1, map.find("p1"));
        assertSame(p2, map.find("p2"));
    }

    // =========================================================================
    // Partition C (Continued): Hash Collisions, Spills, Replacements, Mutations
    // =========================================================================

    /**
     * Characters 'A', 'I', 'Q', 'Y', 'a' all yield (char.hashCode() & 7) == 1.
     * When size <= 5 (hashSize = 8, hashMask = 7), these properties intentionally collide:
     * - 1st: Primary slot (ix = 2)
     * - 2nd: Secondary slot (ix = 16)
     * - 3rd: Spill slot (ix = 24), expands hashArea array
     * - 4th: Spill slot (ix = 26)
     * - 5th: Spill slot (ix = 28), expands hashArea array again
     */
    @Test(timeout = 4000)
    public void testCollisionsPrimarySecondarySpillAndExpansion() {
        SettableBeanProperty propA = new DummyProperty("A");
        SettableBeanProperty propI = new DummyProperty("I");
        SettableBeanProperty propQ = new DummyProperty("Q");
        SettableBeanProperty propY = new DummyProperty("Y");
        SettableBeanProperty propLowerA = new DummyProperty("a");

        List<SettableBeanProperty> list = Arrays.asList(propA, propI, propQ, propY, propLowerA);
        BeanPropertyMap map = BeanPropertyMap.construct(list, false);

        assertSame(propA, map.find("A"));
        assertSame(propI, map.find("I"));
        assertSame(propQ, map.find("Q"));
        assertSame(propY, map.find("Y"));
        assertSame(propLowerA, map.find("a"));

        // Non-existent key that hashes to slot 1 ('i' = 105, 105 & 7 == 1)
        assertNull(map.find("i"));

        // Non-existent key that hashes to slot 0 ('h' = 104, 104 & 7 == 0)
        assertNull(map.find("h"));
    }

    @Test(timeout = 4000)
    public void testWithPropertyReplacingExisting() {
        SettableBeanProperty prop1 = new DummyProperty("name");
        SettableBeanProperty prop2 = new DummyProperty("other");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), false);

        SettableBeanProperty replacement = new DummyProperty("name");
        BeanPropertyMap result = map.withProperty(replacement);

        assertSame(map, result);
        assertSame(replacement, map.find("name"));
        assertSame(replacement, map.getPropertiesInInsertionOrder()[0]);
    }

    @Test(timeout = 4000)
    public void testWithPropertyAppendCollisionsAndExpansion() {
        // Start with primary entry in slot 1
        SettableBeanProperty propA = new DummyProperty("A");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(propA), false);

        // Append to secondary slot
        SettableBeanProperty propI = new DummyProperty("I");
        map.withProperty(propI);
        assertSame(propI, map.find("I"));

        // Append to spill slot with array expansion
        SettableBeanProperty propQ = new DummyProperty("Q");
        map.withProperty(propQ);
        assertSame(propQ, map.find("Q"));

        // Append second spill entry
        SettableBeanProperty propY = new DummyProperty("Y");
        map.withProperty(propY);
        assertSame(propY, map.find("Y"));

        // Append third spill entry causing second expansion
        SettableBeanProperty propLowerA = new DummyProperty("a");
        map.withProperty(propLowerA);
        assertSame(propLowerA, map.find("a"));
    }

    @Test(timeout = 4000)
    public void testReplaceInPrimarySecondaryAndSpillSlots() {
        SettableBeanProperty propA = new DummyProperty("A");
        SettableBeanProperty propI = new DummyProperty("I");
        SettableBeanProperty propQ = new DummyProperty("Q");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(propA, propI, propQ), false);

        // Replace in primary
        DummyProperty replA = new DummyProperty("A");
        map.replace(replA);
        assertSame(replA, map.find("A"));

        // Replace in secondary
        DummyProperty replI = new DummyProperty("I");
        map.replace(replI);
        assertSame(replI, map.find("I"));

        // Replace in spill
        DummyProperty replQ = new DummyProperty("Q");
        map.replace(replQ);
        assertSame(replQ, map.find("Q"));
    }

    @Test(timeout = 4000)
    public void testRenameAllWithHolesAndUnwrappingDeserializers() {
        SettableBeanProperty prop1 = new DummyProperty("prop1");
        DummyProperty prop2 = new DummyProperty(new PropertyName("prop2"), new DummyUnwrappingDeserializer());
        DummyProperty prop3 = new DummyProperty(new PropertyName("prop3"), new DummySameDeserializer());

        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(prop1, prop2, prop3), false);

        // Null or NOP transformer returns same instance
        assertSame(map, map.renameAll(null));
        assertSame(map, map.renameAll(NameTransformer.NOP));

        // Remove prop1 to create a hole in _propsInOrder
        map.remove(prop1);

        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "x_" + name;
            }
            @Override
            public String reverse(String transformed) {
                return transformed.startsWith("x_") ? transformed.substring(2) : null;
            }
        };

        BeanPropertyMap renamedMap = map.renameAll(transformer);
        assertNull(renamedMap.find("x_prop1"));
        assertNotNull(renamedMap.find("x_prop2"));
        assertNotNull(renamedMap.find("x_prop3"));
    }

    @Test(timeout = 4000)
    public void testWithoutPropertiesWithHolesAndEmptyExclusions() {
        SettableBeanProperty p1 = new DummyProperty("p1");
        SettableBeanProperty p2 = new DummyProperty("p2");
        SettableBeanProperty p3 = new DummyProperty("p3");
        BeanPropertyMap map = BeanPropertyMap.construct(Arrays.asList(p1, p2, p3), false);

        // Empty exclusion returns this
        assertSame(map, map.withoutProperties(Collections.<String>emptyList()));

        // Create a null hole by removing p1
        map.remove(p1);

        // Exclude p2
        BeanPropertyMap filtered = map.withoutProperties(Collections.singletonList("p2"));
        assertNull(filtered.find("p1"));
        assertNull(filtered.find("p2"));
        assertNotNull(filtered.find("p3"));
        assertEquals(1, filtered.size());
    }

    // =========================================================================
    // Partition D: Defensive Guards & Exception Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindWithNullKeyThrowsException() {
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        map.find((String) null);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testReplaceNonExistentPropertyThrowsException() {
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        map.replace(new DummyProperty("doesNotExist"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveNonExistentPropertyThrowsException() {
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        map.remove(new DummyProperty("doesNotExist"));
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetSuccessAndNotFound() throws Exception {
        SettableBeanProperty prop = new DummyProperty("foo");
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        assertFalse(map.findDeserializeAndSet(null, null, new Object(), "missing"));
        assertTrue(map.findDeserializeAndSet(null, null, new Object(), "foo"));
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetPropagatesErrorDirectly() throws Exception {
        SettableBeanProperty prop = new DummyProperty("err").withExceptionOnDeserialize(new OutOfMemoryError("OOM"));
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        try {
            map.findDeserializeAndSet(null, null, new Object(), "err");
            fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError expected) {
            assertEquals("OOM", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetPropagatesIOException() throws Exception {
        SettableBeanProperty prop = new DummyProperty("ioe").withExceptionOnDeserialize(new IOException("Disk error"));
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        try {
            map.findDeserializeAndSet(null, null, new Object(), "ioe");
            fail("Expected IOException");
        } catch (IOException expected) {
            assertEquals("Disk error", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetWrapsRuntimeException() throws Exception {
        SettableBeanProperty prop = new DummyProperty("rte").withExceptionOnDeserialize(new IllegalArgumentException("Bad arg"));
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        try {
            map.findDeserializeAndSet(null, null, new Object(), "rte");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetWrapsJsonProcessingException() throws Exception {
        SettableBeanProperty prop = new DummyProperty("jpe").withExceptionOnDeserialize(
                new JsonParseException("Syntax error", JsonLocation.NA));
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        try {
            map.findDeserializeAndSet(null, null, new Object(), "jpe");
            fail("Expected JsonMappingException wrapping JsonParseException");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getCause() instanceof JsonParseException);
        }
    }

    @Test(timeout = 4000)
    public void testFindDeserializeAndSetUnwrapsInvocationTargetException() throws Exception {
        SettableBeanProperty prop = new DummyProperty("ite").withExceptionOnDeserialize(
                new InvocationTargetException(new OutOfMemoryError("Inner OOM")));
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.singletonList(prop), false);

        try {
            map.findDeserializeAndSet(null, null, new Object(), "ite");
            fail("Expected unwrapped OutOfMemoryError");
        } catch (OutOfMemoryError expected) {
            assertEquals("Inner OOM", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        SettableBeanProperty prop1 = new DummyProperty("prop1");
        SettableBeanProperty prop2 = new DummyProperty("prop2");
        BeanPropertyMap original = BeanPropertyMap.construct(Arrays.asList(prop1, prop2), true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        BeanPropertyMap deserialized = (BeanPropertyMap) ois.readObject();
        ois.close();

        assertEquals(original.size(), deserialized.size());
        assertNotNull(deserialized.find("prop1"));
        assertNotNull(deserialized.find("PROP1"));
        assertNotNull(deserialized.find("prop2"));
    }
}