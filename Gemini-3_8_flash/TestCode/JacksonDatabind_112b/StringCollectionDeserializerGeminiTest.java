/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer
 *
 * 1. Defects4J Defect Databind#2324:
 *    - In contextualization (`createContextual`), delegate creators can be delegating or array-delegating.
 *    - When a custom Collection (e.g. ImmutableBag) defines `@JsonCreator(mode = DELEGATING)` taking a `List<String>`,
 *      ValueInstantiator classifies it as `getArrayDelegateCreator()` rather than `getDelegateCreator()`.
 *    - Defective implementations only query `_valueInstantiator.getDelegateCreator()`, missing the array-delegate,
 *      which leaves `_delegateDeserializer` null and subsequently fails with MismatchedInputException (no default constructor).
 *
 * 2. Coverage & Branch Analysis:
 *    - `isCachable()`: (_valueDeserializer == null && _delegateDeserializer == null).
 *    - `withResolved()`: identity shortcut when all 4 parameters match current state; new instance otherwise.
 *    - `getContentDeserializer()` & `getValueInstantiator()`: getters.
 *    - `createContextual()`:
 *      * _valueInstantiator != null vs null; delegateCreator != null vs null.
 *      * _valueDeserializer == null -> findConvertingContentDeserializer vs findContextualValueDeserializer.
 *      * _valueDeserializer != null -> handleSecondaryContextualization.
 *      * JsonFormat ACCEPT_SINGLE_VALUE_AS_ARRAY feature detection.
 *      * findContentNullProvider; isDefaultDeserializer (custom vs default string deser).
 *    - `deserialize(JsonParser, DeserializationContext)`:
 *      * _delegateDeserializer != null: delegation path.
 *      * _delegateDeserializer == null: default creator path -> array or non-array handling.
 *    - `deserialize(JsonParser, DeserializationContext, Collection<String>)`:
 *      * isExpectedStartArrayToken() == true:
 *        - _valueDeserializer == null: fast nextTextValue() loop, VALUE_NULL (skip vs nullProvider), _parseString, END_ARRAY.
 *        - _valueDeserializer != null: deserializeUsingCustom() loop, nextTextValue() == null or != null, VALUE_NULL, END_ARRAY.
 *        - Exception wrapping with wrapWithPath.
 *      * isExpectedStartArrayToken() == false: handleNonArray()
 *        - ACCEPT_SINGLE_VALUE_AS_ARRAY enabled (unwrapSingle == TRUE or feature on context) vs disabled (unexpectedToken).
 *        - Token is VALUE_NULL (_skipNullValues true vs false).
 *        - Custom valueDeserializer vs default _parseString.
 *    - `deserializeWithType()`: redirects to typeDeserializer.deserializeTypedFromArray.
 */
package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class StringCollectionDeserializerGeminiTest {

    // =========================================================================
    // Supporting Types & Mock Classes for Custom / Delegating Deserialization
    // =========================================================================

    static class ImmutableBag extends AbstractCollection<String> {
        private final Collection<String> elements;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public ImmutableBag(List<String> elements) {
            this.elements = (elements == null) ? Collections.emptyList() : new ArrayList<>(elements);
        }

        @Override
        public Iterator<String> iterator() {
            return elements.iterator();
        }

        @Override
        public int size() {
            return elements.size();
        }
    }

    static class UpperCaseDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String val = p.getValueAsString();
            return (val == null) ? null : val.toUpperCase();
        }
    }

    static class UpperCaseConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    static class SingleAcceptingBean {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Collection<String> values;
    }

    static class SingleRejectingBean {
        @JsonFormat(without = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Collection<String> values;
    }

    static class ConvertedBean {
        @JsonDeserialize(contentConverter = UpperCaseConverter.class)
        public List<String> list;
    }

    static class CustomDeserBean {
        @JsonDeserialize(contentUsing = UpperCaseDeserializer.class)
        public List<String> list;
    }

    static class SkipNullBean {
        @JsonSetter(contentNulls = Nulls.SKIP)
        public List<String> list;
    }

    static class DummyInstantiator extends ValueInstantiator {
        private final Class<?> _valueClass;

        public DummyInstantiator(Class<?> valueClass) {
            _valueClass = valueClass;
        }

        @Override
        public Class<?> getValueClass() {
            return _valueClass;
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) {
            return new ArrayList<String>();
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Databind-2324 Ground Truth)
    // =========================================================================

    /**
     * Directly targets [databind#2324]: Creator with delegating Collection/List argument
     * must be recognized via array-delegating creator resolution in createContextual().
     */
    @Test(timeout = 4000)
    public void testDelegatingArrayCreator2324BagOfStrings() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ImmutableBag bag = mapper.readValue("[\"apple\", \"banana\", \"cherry\"]", ImmutableBag.class);
        assertNotNull("Deserialized collection must not be null", bag);
        assertEquals("Collection size must match input items", 3, bag.size());
        assertTrue("Collection must contain 'apple'", bag.contains("apple"));
        assertTrue("Collection must contain 'banana'", bag.contains("banana"));
        assertTrue("Collection must contain 'cherry'", bag.contains("cherry"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardCollectionDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[\"alpha\", \"beta\", \"gamma\"]",
                new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("alpha", result.get(0));
        assertEquals("beta", result.get(1));
        assertEquals("gamma", result.get(2));
    }

    @Test(timeout = 4000)
    public void testSetCollectionDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> result = mapper.readValue("[\"one\", \"two\", \"one\"]",
                new TypeReference<Set<String>>() {});
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("one"));
        assertTrue(result.contains("two"));
    }

    @Test(timeout = 4000)
    public void testIsCachable() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new DummyInstantiator(List.class);

        // Standard instance without custom value or delegate deserializer is cachable
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        assertTrue("Standard instance should be cachable", deser.isCachable());

        // Instance with custom value deserializer is not cachable
        StringCollectionDeserializer withCustom = deser.withResolved(null, new UpperCaseDeserializer(), null, null);
        assertFalse("Instance with custom value deserializer should not be cachable", withCustom.isCachable());
    }

    @Test(timeout = 4000)
    public void testWithResolvedIdentityShortcut() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new DummyInstantiator(List.class);

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        // Calling withResolved with identical parameters should return 'this'
        StringCollectionDeserializer same = deser.withResolved(null, null, null, null);
        assertSame("Should return exact same instance when parameters are unchanged", deser, same);
    }

    @Test(timeout = 4000)
    public void testGetContentDeserializerAndValueInstantiator() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new DummyInstantiator(List.class);
        JsonDeserializer<String> valDeser = new UpperCaseDeserializer();

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, valDeser, vi);
        assertSame("ValueInstantiator getter must match input", vi, deser.getValueInstantiator());
        assertSame("ContentDeserializer getter must match provided deserializer", valDeser, deser.getContentDeserializer());
    }

    @Test(timeout = 4000)
    public void testCustomContentDeserializerExecution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomDeserBean bean = mapper.readValue("{\"list\": [\"hello\", \"world\"]}", CustomDeserBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.list.size());
        assertEquals("HELLO", bean.list.get(0));
        assertEquals("WORLD", bean.list.get(1));
    }

    @Test(timeout = 4000)
    public void testContentConverterContextualization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConvertedBean bean = mapper.readValue("{\"list\": [\"foo\", \"bar\"]}", ConvertedBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.list.size());
        assertEquals("FOO", bean.list.get(0));
        assertEquals("BAR", bean.list.get(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArrayDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[]", new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullValuesInArrayStandard() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[\"a\", null, \"b\"]", new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test(timeout = 4000)
    public void testNullValuesSkipped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SkipNullBean bean = mapper.readValue("{\"list\": [\"a\", null, \"b\"]}", SkipNullBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.list.size());
        assertEquals("a", bean.list.get(0));
        assertEquals("b", bean.list.get(1));
    }

    @Test(timeout = 4000)
    public void testNullValuesWithCustomDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomDeserBean bean = mapper.readValue("{\"list\": [\"test\", null]}", CustomDeserBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.list.size());
        assertEquals("TEST", bean.list.get(0));
        assertNull(bean.list.get(1));
    }

    @Test(timeout = 4000)
    public void testNonArrayHandlingGlobalFeatureEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = mapper.readValue("\"standalone\"", new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("standalone", result.get(0));
    }

    @Test(timeout = 4000)
    public void testNonArrayHandlingAnnotationEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Global feature disabled
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SingleAcceptingBean bean = mapper.readValue("{\"values\": \"single\"}", SingleAcceptingBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.values.size());
        assertEquals("single", bean.values.iterator().next());
    }

    @Test(timeout = 4000)
    public void testNonArrayHandlingAnnotationDisabledWhenGlobalEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            mapper.readValue("{\"values\": \"single\"}", SingleRejectingBean.class);
            fail("Expected JsonMappingException when single value array is disabled via annotation");
        } catch (JsonMappingException e) {
            // Success
            assertTrue(e.getMessage().contains("Cannot deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testNonArrayNullHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = mapper.readValue("null", new TypeReference<List<String>>() {});
        // In Jackson, root null value deserializes to null object
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testNonArrayNumberConversion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // Non-string token should be converted via _parseString
        List<String> result = mapper.readValue("12345", new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("12345", result.get(0));
    }

    @Test(timeout = 4000)
    public void testNonStringTokensInArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[123, true, 45.67]", new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("123", result.get(0));
        assertEquals("true", result.get(1));
        assertEquals("45.67", result.get(2));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnexpectedObjectTokenFailsWhenWrapDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            mapper.readValue("{\"key\": \"val\"}", new TypeReference<List<String>>() {});
            fail("Expected JsonMappingException for unexpected object token");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot deserialize")
                    || e.getMessage().contains("START_OBJECT"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        String json = mapper.writeValueAsString(list);
        List<?> result = mapper.readValue(json, List.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
    }

    @Test(timeout = 4000)
    public void testWrapWithPathOnError() throws Exception {
        JsonDeserializer<String> failingDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new IllegalStateException("Simulated deserializer failure");
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new DummyInstantiator(List.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, failingDeser, vi);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("[\"item1\"]");
        p.nextToken(); // Move to START_ARRAY
        p.nextToken(); // Move to "item1"

        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            deser.deserialize(p, ctxt, new ArrayList<String>());
            fail("Expected JsonMappingException wrapping the IllegalStateException");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("Simulated deserializer failure", e.getCause().getMessage());
            assertFalse(e.getPath().isEmpty());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithResolvedVariations() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new DummyInstantiator(List.class);
        StringCollectionDeserializer original = new StringCollectionDeserializer(type, null, vi);

        JsonDeserializer<String> customDeser = new UpperCaseDeserializer();
        NullValueProvider nuller = NullsConstantProvider.nuller();

        // 1. Change delegateDeser
        StringCollectionDeserializer resolved1 = original.withResolved(customDeser, null, null, null);
        assertNotSame(original, resolved1);
        assertFalse(resolved1.isCachable());

        // 2. Change valueDeser
        StringCollectionDeserializer resolved2 = original.withResolved(null, customDeser, null, null);
        assertNotSame(original, resolved2);
        assertSame(customDeser, resolved2.getContentDeserializer());

        // 3. Change nuller
        StringCollectionDeserializer resolved3 = original.withResolved(null, null, nuller, null);
        assertNotSame(original, resolved3);

        // 4. Change unwrapSingle
        StringCollectionDeserializer resolved4 = original.withResolved(null, null, null, Boolean.TRUE);
        assertNotSame(original, resolved4);
    }
}