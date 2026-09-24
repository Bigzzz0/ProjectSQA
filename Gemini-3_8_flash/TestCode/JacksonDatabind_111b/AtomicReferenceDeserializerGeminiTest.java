package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer
 * Extends: ReferenceTypeDeserializer<AtomicReference<Object>>
 *
 * -----------------------------------------------------------------------------------------------------
 * Method / Branch                    Covered Conditions & Equivalence Partitions
 * -----------------------------------------------------------------------------------------------------
 * <init> (Lifecycle)                 - Full valid parameters (JavaType, ValueInstantiator, TypeDeser, Deser)
 *                                    - Null parameters across all constructor arguments
 * withResolved                       - Re-binding with null/non-null TypeDeserializer and JsonDeserializer
 * getNullValue(ctxt)                 - Default null representation (must properly delegate to inner deser)
 * getEmptyValue(ctxt)                - Empty reference representation (AtomicReference with null content)
 * referenceValue(contents)           - Wrapping valid objects, primitives, null payloads
 * getReferenced(reference)           - Extraction of referenced content; NPE on null input container
 * updateReference(reference, content)- In-place value mutation and reference chaining
 * supportsUpdate(config)             - Config-independent Boolean.TRUE guarantee
 *
 * -----------------------------------------------------------------------------------------------------
 * Defect-Targeted Branch Zone (Defects4J Ground Truth):
 * - JDKAtomicTypesDeserTest::testNullWithinNested
 *   Fault: AtomicReferenceDeserializer historically overrode getNullValue(ctxt) returning
 *   `new AtomicReference<Object>()` (empty payload) rather than delegating to
 *   `_valueDeserializer.getNullValue(ctxt)`. In nested reference structures (e.g.,
 *   AtomicReference<AtomicReference<T>>), deserializing JSON null resulted in an outer reference
 *   containing null instead of an outer reference containing a nested AtomicReference(null).
 * -----------------------------------------------------------------------------------------------------
 */
public class AtomicReferenceDeserializerGeminiTest {

    public static class SampleBean implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id;
        public String name;

        public SampleBean() {}
        public SampleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class NestedAtomicHolder {
        public AtomicReference<AtomicReference<String>> nested;

        public NestedAtomicHolder() {}
        public NestedAtomicHolder(AtomicReference<AtomicReference<String>> nested) {
            this.nested = nested;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReferenceValueWrapping() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);

        String payload = "testPayload";
        AtomicReference<Object> ref = deser.referenceValue(payload);
        assertNotNull("Resulting AtomicReference container must not be null", ref);
        assertEquals("Payload must match referenced content", payload, ref.get());
    }

    @Test(timeout = 4000)
    public void testGetReferencedExtraction() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);

        AtomicReference<Object> refWithContent = new AtomicReference<Object>("content");
        assertEquals("content", deser.getReferenced(refWithContent));

        AtomicReference<Object> emptyRef = new AtomicReference<Object>(null);
        assertNull("Empty container must return null referenced content", deser.getReferenced(emptyRef));
    }

    @Test(timeout = 4000)
    public void testUpdateReferenceMutation() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);

        AtomicReference<Object> originalRef = new AtomicReference<Object>("initial");
        AtomicReference<Object> updatedRef = deser.updateReference(originalRef, "updated");

        assertSame("updateReference must return the exact same instance", originalRef, updatedRef);
        assertEquals("Referenced state must be updated to new content", "updated", originalRef.get());
    }

    @Test(timeout = 4000)
    public void testSupportsUpdateAlwaysTrue() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);

        assertEquals("supportsUpdate must return Boolean.TRUE with null config",
                Boolean.TRUE, deser.supportsUpdate((DeserializationConfig) null));

        ObjectMapper mapper = new ObjectMapper();
        assertEquals("supportsUpdate must return Boolean.TRUE with active mapper config",
                Boolean.TRUE, deser.supportsUpdate(mapper.getDeserializationConfig()));
    }

    @Test(timeout = 4000)
    public void testWithResolvedRebinding() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String.class);
        AtomicReferenceDeserializer initial = new AtomicReferenceDeserializer(type, null, null, null);

        AtomicReferenceDeserializer resolved = initial.withResolved(null, null);
        assertNotNull("withResolved must return a newly configured instance", resolved);
        assertNotSame("withResolved must instantiate a distinct deserializer", initial, resolved);
    }

    @Test(timeout = 4000)
    public void testNormalValueDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "\"Hello World\"";
        AtomicReference<String> result = mapper.readValue(json,
                new TypeReference<AtomicReference<String>>() {});

        assertNotNull(result);
        assertEquals("Hello World", result.get());
    }

    @Test(timeout = 4000)
    public void testComplexBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":42,\"name\":\"Gemini\"}";
        AtomicReference<SampleBean> result = mapper.readValue(json,
                new TypeReference<AtomicReference<SampleBean>>() {});

        assertNotNull(result);
        assertNotNull(result.get());
        assertEquals(42, result.get().id);
        assertEquals("Gemini", result.get().name);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReferenceValueWithNullPayload() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        AtomicReference<Object> ref = deser.referenceValue(null);

        assertNotNull("Container itself must never be null", ref);
        assertNull("Referenced value must be null", ref.get());
    }

    @Test(timeout = 4000)
    public void testUpdateReferenceWithNullPayload() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");

        AtomicReference<Object> updated = deser.updateReference(ref, null);
        assertSame(ref, updated);
        assertNull("Referenced value must be reset to null", ref.get());
    }

    @Test(timeout = 4000)
    public void testGetEmptyValueState() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        Object empty = deser.getEmptyValue(null);

        assertNotNull("Empty value representation must not be null", empty);
        assertTrue("Empty value must be an AtomicReference instance", empty instanceof AtomicReference);
        assertNull("Empty value content must be null", ((AtomicReference<?>) empty).get());
    }

    @Test(timeout = 4000)
    public void testDeserializationOfEmptyString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue("\"\"",
                new TypeReference<AtomicReference<String>>() {});

        assertNotNull(result);
        assertEquals("", result.get());
    }

    @Test(timeout = 4000)
    public void testCollectionWithNullAndNonNullAtomicReferences() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"first\", null, \"third\"]";
        List<AtomicReference<String>> list = mapper.readValue(json,
                new TypeReference<List<AtomicReference<String>>>() {});

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("first", list.get(0).get());
        assertNotNull("Null element in collection must deserialize to an AtomicReference wrapper", list.get(1));
        assertNull("Inner content of null element must be null", list.get(1).get());
        assertEquals("third", list.get(2).get());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Target Defect: JDKAtomicTypesDeserTest::testNullWithinNested
     *
     * In defective versions, AtomicReferenceDeserializer.getNullValue() produces an
     * AtomicReference containing null rather than delegating to the nested deserializer's
     * getNullValue(). When deserializing nested references from JSON null:
     * AtomicReference<AtomicReference<String>>
     * Expected: an AtomicReference whose get() returns another AtomicReference whose get() is null.
     * Defective: an AtomicReference whose get() returns null directly.
     */
    @Test(timeout = 4000)
    public void testNullWithinNested() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<AtomicReference<AtomicReference<String>>> typeRef =
                new TypeReference<AtomicReference<AtomicReference<String>>>() {};

        AtomicReference<AtomicReference<String>> result = mapper.readValue("null", typeRef);

        assertNotNull("Outer AtomicReference must not be null", result);
        assertNotNull("Inner AtomicReference must not be null when deserializing nested reference from null",
                result.get());
        assertNull("Deepest value must be null", result.get().get());
    }

    @Test(timeout = 4000)
    public void testNullWithinNestedInObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"nested\":null}";

        NestedAtomicHolder holder = mapper.readValue(json, NestedAtomicHolder.class);
        assertNotNull("Enclosing holder bean must be instantiated", holder);
        assertNotNull("Outer nested reference must not be null", holder.nested);
        assertNotNull("Inner nested reference must not be null when null is supplied", holder.nested.get());
        assertNull("Innermost string payload must be null", holder.nested.get().get());
    }

    @Test(timeout = 4000)
    public void testGetNullValueDelegationDirectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType strType = mapper.getTypeFactory().constructType(String.class);
        JavaType innerRefType = mapper.getTypeFactory().constructReferenceType(AtomicReference.class, strType);
        JavaType outerRefType = mapper.getTypeFactory().constructReferenceType(AtomicReference.class, innerRefType);

        JsonDeserializer<?> innerDeser = mapper.getDeserializationConfig().findRootValueDeserializer(innerRefType);
        AtomicReferenceDeserializer outerDeser = new AtomicReferenceDeserializer(
                outerRefType, null, null, innerDeser);

        AtomicReference<Object> nullResult = outerDeser.getNullValue(ctxt);
        assertNotNull("Outer container must not be null", nullResult);
        assertNotNull("Inner container must be resolved via inner deser getNullValue()", nullResult.get());
        assertTrue("Inner content must be an instance of AtomicReference",
                nullResult.get() instanceof AtomicReference);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetReferencedThrowsNullPointerExceptionOnNullContainer() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        deser.getReferenced(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testUpdateReferenceThrowsNullPointerExceptionOnNullContainer() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        deser.updateReference(null, "value");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & In-Place Mutation Contract
    // =========================================================================

    @Test(timeout = 4000)
    public void testReaderForUpdatingContract() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> existingRef = new AtomicReference<String>("initialValue");

        AtomicReference<String> result = mapper.readerForUpdating(existingRef)
                .readValue("\"updatedValue\"");

        assertSame("readerForUpdating must preserve and return existing instance", existingRef, result);
        assertEquals("Underlying reference content must be updated in-place", "updatedValue", existingRef.get());
    }

    @Test(timeout = 4000)
    public void testTripleNestedAtomicReferenceDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<AtomicReference<AtomicReference<AtomicReference<Integer>>>> typeRef =
                new TypeReference<AtomicReference<AtomicReference<AtomicReference<Integer>>>>() {};

        AtomicReference<AtomicReference<AtomicReference<Integer>>> result =
                mapper.readValue("null", typeRef);

        assertNotNull("Level 1 reference must not be null", result);
        assertNotNull("Level 2 reference must not be null", result.get());
        assertNotNull("Level 3 reference must not be null", result.get().get());
        assertNull("Level 3 content must be null", result.get().get().get());
    }
}