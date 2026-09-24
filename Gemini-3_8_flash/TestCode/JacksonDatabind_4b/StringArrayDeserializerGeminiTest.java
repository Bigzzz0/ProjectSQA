/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer
 *
 * 1. Targeted Branches & Paths:
 *    - deserialize(JsonParser, DeserializationContext):
 *      * !jp.isExpectedStartArrayToken() -> handleNonArray (single value as array, empty string as null, exception).
 *      * _elementDeserializer != null -> _deserializeCustom (with null and non-null values, custom getNullValue()).
 *      * _elementDeserializer == null -> default loop:
 *        - VALUE_STRING -> jp.getText()
 *        - VALUE_NULL -> null
 *        - other tokens (e.g., INT, BOOLEAN, OBJECT, etc.) -> _parseString(jp, ctxt)
 *      * Buffer expansion (ix >= chunk.length) -> buffer.appendCompletedChunk (spanning across multi-chunk buffer boundaries, > 12 elements).
 *    - handleNonArray(JsonParser, DeserializationContext):
 *      * Feature ACCEPT_SINGLE_VALUE_AS_ARRAY disabled ->
 *        - Token is VALUE_STRING and ACCEPT_EMPTY_STRING_AS_NULL_OBJECT enabled:
 *          * str.length() == 0 -> returns null
 *          * str.length() > 0 -> throws JsonMappingException
 *        - Other tokens -> throws JsonMappingException
 *      * Feature ACCEPT_SINGLE_VALUE_AS_ARRAY enabled:
 *        - Token is VALUE_NULL -> new String[] { null }
 *        - Token is other (e.g., string, number) -> new String[] { _parseString }
 *    - _deserializeCustom(JsonParser, DeserializationContext):
 *      * Token VALUE_NULL -> deser.getNullValue()
 *      * Token non-null -> deser.deserialize(jp, ctxt)
 *      * Buffer expansion (> chunk size)
 *    - deserializeWithType(JsonParser, DeserializationContext, TypeDeserializer):
 *      * Delegates to typeDeserializer.deserializeTypedFromArray
 *    - createContextual(DeserializationContext, BeanProperty):
 *      * Content converter resolution via findConvertingContentDeserializer
 *      * Default String deserializer check (isDefaultDeserializer -> deser set to null)
 *      * Custom String deserializer resolution -> returns new StringArrayDeserializer(deser)
 *      * Identity preservation when element deserializer is unchanged
 *
 * 2. Defect-Targeted Branch Zone:
 *    - Defects4J TestCollectionDeserialization::testArrayIndexForExceptions:
 *      Verifies accurate index tracking when an exception occurs inside array element deserialization
 *      (e.g., custom deserializer throwing at index 1).
 */

package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class StringArrayDeserializerGeminiTest {

    // Helper Beans for contextual and annotation testing
    static class CustomStringDeser extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String text = jp.getText();
            if ("FAIL_HERE".equals(text)) {
                throw new JsonMappingException("Intentional failure for test");
            }
            return text == null ? null : text.toUpperCase();
        }

        @Override
        public String getNullValue() {
            return "NIL";
        }
    }

    static class UpperCaseConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    static class BeanWithCustomArray {
        @JsonDeserialize(contentUsing = CustomStringDeser.class)
        public String[] values;
    }

    static class BeanWithConverter {
        @JsonDeserialize(contentConverter = UpperCaseConverter.class)
        public String[] items;
    }

    static class BeanWithPolymorphicArray {
        public Object[] data;
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & Standard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardDeserializationNormal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"a\", \"b\", null, \"c\"]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(4, result.length);
        assertArrayEquals(new String[]{"a", "b", null, "c"}, result);
    }

    @Test(timeout = 4000)
    public void testStandardDeserializationWithNonStringTokens() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Numbers and booleans should be coerced to strings via _parseString
        String json = "[123, true, false]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new String[]{"123", "true", "false"}, result);
    }

    @Test(timeout = 4000)
    public void testBufferExpansionLargeArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // ObjectBuffer starts with initial size 12. Create > 30 items to force multiple buffer expansions.
        int count = 50;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item_").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);
        assertNotNull(result);
        assertEquals(count, result.length);
        for (int i = 0; i < count; i++) {
            assertEquals("item_" + i, result[i]);
        }
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String[] result = mapper.readValue("[]", String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Custom Element Deserializer
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomDeserializerAndNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"values\": [\"abc\", null, \"xyz\"]}";
        BeanWithCustomArray bean = mapper.readValue(json, BeanWithCustomArray.class);

        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(3, bean.values.length);
        assertEquals("ABC", bean.values[0]);
        assertEquals("NIL", bean.values[1]); // Custom getNullValue returns "NIL"
        assertEquals("XYZ", bean.values[2]);
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerBufferExpansion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder("{\"values\": [");
        int count = 40;
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            if (i % 5 == 0) {
                sb.append("null");
            } else {
                sb.append("\"val").append(i).append("\"");
            }
        }
        sb.append("]}");

        BeanWithCustomArray bean = mapper.readValue(sb.toString(), BeanWithCustomArray.class);
        assertNotNull(bean.values);
        assertEquals(count, bean.values.length);
        for (int i = 0; i < count; i++) {
            if (i % 5 == 0) {
                assertEquals("NIL", bean.values[i]);
            } else {
                assertEquals("VAL" + i, bean.values[i]);
            }
        }
    }

    @Test(timeout = 4000)
    public void testContentConverterContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"items\": [\"hello\", \"world\"]}";
        BeanWithConverter bean = mapper.readValue(json, BeanWithConverter.class);

        assertNotNull(bean);
        assertNotNull(bean.items);
        assertArrayEquals(new String[]{"HELLO", "WORLD"}, bean.items);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J ArrayIndex / Path Index)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectTargetArrayIndexForExceptions() throws Exception {
        // Targets known failure condition where index inside array was miscalculated (expected 1, got 0)
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"values\": [\"first\", \"FAIL_HERE\"]}";

        try {
            mapper.readValue(json, BeanWithCustomArray.class);
            fail("Expected JsonMappingException not thrown");
        } catch (JsonMappingException e) {
            assertNotNull("Exception path should not be null", e.getPath());
            assertFalse("Exception path should not be empty", e.getPath().isEmpty());
            // Find the array reference in the path
            boolean foundIndex = false;
            for (JsonMappingException.Reference ref : e.getPath()) {
                if (ref.getIndex() == 1) {
                    foundIndex = true;
                    break;
                }
            }
            assertTrue("Expected exception path to contain index 1 for the failing element", foundIndex);
        }
    }

    @Test(timeout = 4000)
    public void testDefectTargetStandardArrayElementFailureIndex() throws Exception {
        // If an object token is found where a String is expected, verify exception index
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"ok\", {\"invalid\": \"object\"}]";

        try {
            mapper.readValue(json, String[].class);
            fail("Expected JsonMappingException for nested object inside String array");
        } catch (JsonMappingException e) {
            assertNotNull(e.getPath());
            assertFalse(e.getPath().isEmpty());
            assertEquals(1, e.getPath().get(0).getIndex());
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths (handleNonArray)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleNonArrayFeatureDisabledThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        try {
            mapper.readValue("\"not_an_array\"", String[].class);
            fail("Expected JsonMappingException when single value as array is disabled");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("String[]"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayEmptyStringBecomesNullWhenFeatureEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String[] result = mapper.readValue("\"\"", String[].class);
        assertNull("Empty string should map to null when feature is enabled", result);
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayNonEmptyStringWithEmptyStringAsNullStillFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        try {
            mapper.readValue("\"hello\"", String[].class);
            fail("Expected JsonMappingException for non-empty string when single value disabled");
        } catch (JsonMappingException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayAcceptSingleValueAsArrayString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String[] result = mapper.readValue("\"single_string\"", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("single_string", result[0]);
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayAcceptSingleValueAsArrayNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String[] result = mapper.readValue("null", String[].class);
        // Deserialization of null token directly generally yields null for array deserializers
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayAcceptSingleValueAsArrayNumber() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String[] result = mapper.readValue("12345", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("12345", result[0]);
    }

    // =========================================================================
    // PARTITION E: Direct Class Instantiation, Polymorphism & Contextual Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstanceAndConstructors() {
        assertNotNull(StringArrayDeserializer.instance);

        StringArrayDeserializer defaultDeser = new StringArrayDeserializer();
        assertNotNull(defaultDeser);

        CustomStringDeser customElement = new CustomStringDeser();
        StringArrayDeserializer customDeser = new StringArrayDeserializer(customElement);
        assertNotNull(customDeser);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        String[] original = new String[]{"foo", "bar"};
        String json = mapper.writeValueAsString(original);

        String[] deserialized = (String[]) mapper.readValue(json, Object.class);
        assertNotNull(deserialized);
        assertArrayEquals(original, deserialized);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithDefaultDeserializerReturnsSame() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt == null) {
            ctxt = ((ObjectMapper) mapper.copy()).getDeserializationContext();
        }

        StringArrayDeserializer deser = new StringArrayDeserializer();
        // Running contextualize on StringArrayDeserializer with no property/customization should return this or new default instance
        JsonDeserializer<?> contextual = deser.createContextual(
                mapper.getDeserializationConfig().initialize(ctxt), null);
        assertNotNull(contextual);
        assertTrue(contextual instanceof StringArrayDeserializer);
    }
}