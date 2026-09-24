package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: StringArrayDeserializer
 * ------------------------------------------------------------------------------------------------------
 * Defect ID: Jackson-databind (Defects4J ground truth: TestArrayDeserialization::testStringArray NPE)
 * Root Cause: In deserialize(), when _elementDeserializer is null (fast-path) and the parser encounters
 *             JsonToken.VALUE_NULL, line 80 executes: `value = _elementDeserializer.getNullValue();`,
 *             causing an immediate java.lang.NullPointerException.
 * Target Branch: deserialize() -> while loop -> (t == JsonToken.VALUE_NULL) when _elementDeserializer == null
 * ------------------------------------------------------------------------------------------------------
 * Branches Covered:
 * 1. jp.isExpectedStartArrayToken() [true -> array flow; false -> handleNonArray]
 * 2. handleNonArray:
 *    - ACCEPT_SINGLE_VALUE_AS_ARRAY disabled + empty string + ACCEPT_EMPTY_STRING_AS_NULL_OBJECT enabled -> return null
 *    - ACCEPT_SINGLE_VALUE_AS_ARRAY disabled + non-empty string / other -> throw JsonMappingException
 *    - ACCEPT_SINGLE_VALUE_AS_ARRAY enabled + VALUE_NULL -> String[] { null }
 *    - ACCEPT_SINGLE_VALUE_AS_ARRAY enabled + scalar value (String/Int/Boolean) -> String[] { _parseString() }
 * 3. Fast-path deserialize (_elementDeserializer == null):
 *    - Token VALUE_STRING -> jp.getText()
 *    - Token VALUE_NULL -> null value handling (DEFECT TARGET)
 *    - Token other (numeric, boolean) -> _parseString()
 *    - Chunk buffer overflow (ix >= chunk.length) -> buffer.appendCompletedChunk()
 * 4. Custom deserializer path (_elementDeserializer != null):
 *    - Custom element deserializer invoked for non-null items
 *    - Token VALUE_NULL returns null without calling custom deserializer
 *    - Custom deserializer chunk buffer overflow
 * 5. deserializeWithType:
 *    - Delegated to typeDeserializer.deserializeTypedFromArray()
 * 6. createContextual:
 *    - Default string deserializer recognized -> resets to null / returns unmodified instance
 *    - Custom element deserializer or converter attached -> creates new StringArrayDeserializer instance
 */
public class StringArrayDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (NPE on array with null element)
    // =========================================================================

    /**
     * Defects4J Ground Truth Target:
     * When deserializing an array containing null elements using the default inlined deserializer,
     * the code references `_elementDeserializer.getNullValue()` where `_elementDeserializer` is null.
     * The test asserts that `["a", null, "b"]` correctly yields `new String[] { "a", null, "b" }`.
     */
    @Test(timeout = 4000)
    public void testDefectNullPointerExceptionOnNullElement() throws IOException {
        String json = "[\"a\", null, \"b\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull("Deserialized result should not be null", result);
        assertEquals("Array length must match", 3, result.length);
        assertEquals("First element", "a", result[0]);
        assertNull("Second element must be null", result[1]);
        assertEquals("Third element", "b", result[2]);
    }

    /**
     * Defect Target Boundary:
     * Array starting with null or consisting solely of null values.
     */
    @Test(timeout = 4000)
    public void testDefectOnlyNullElements() throws IOException {
        String json = "[null, null]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardStringArrayDeserialization() throws IOException {
        String json = "[\"first\", \"second\", \"third\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "first", "second", "third" }, result);
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNonStringTokensCoercedToString() throws IOException {
        // Numbers and booleans parsed through _parseString
        String json = "[123, true, false, 45.67]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "123", "true", "false", "45.67" }, result);
    }

    @Test(timeout = 4000)
    public void testBufferChunkGrowthStandard() throws IOException {
        // Default ObjectBuffer chunk size is 12. Create > 12 elements to trigger chunk expansion
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item_").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);
        assertNotNull(result);
        assertEquals(50, result.length);
        assertEquals("item_0", result[0]);
        assertEquals("item_49", result[49]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Non-Array Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testAcceptSingleValueAsArrayEnabledScalarString() throws IOException {
        ObjectMapper singleValMapper = new ObjectMapper();
        singleValMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String json = "\"solo\"";
        String[] result = singleValMapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "solo" }, result);
    }

    @Test(timeout = 4000)
    public void testAcceptSingleValueAsArrayEnabledScalarNumber() throws IOException {
        ObjectMapper singleValMapper = new ObjectMapper();
        singleValMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String json = "999";
        String[] result = singleValMapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "999" }, result);
    }

    @Test(timeout = 4000)
    public void testAcceptSingleValueAsArrayEnabledNull() throws IOException {
        ObjectMapper singleValMapper = new ObjectMapper();
        singleValMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String json = "null";
        String[] result = singleValMapper.readValue(json, String[].class);
        // Note: root null usually results in null from mapper.readValue,
        // but explicit call directly triggers handleNonArray with VALUE_NULL.
        JsonParser jp = singleValMapper.getFactory().createParser(json);
        jp.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = singleValMapper.getDeserializationContext();
        String[] res = StringArrayDeserializer.instance.deserialize(jp, ctxt);
        assertNotNull(res);
        assertEquals(1, res.length);
        assertNull(res[0]);
    }

    @Test(timeout = 4000)
    public void testAcceptEmptyStringAsNullObjectEnabled() throws IOException {
        ObjectMapper emptyToNullMapper = new ObjectMapper();
        emptyToNullMapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        emptyToNullMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String json = "\"\"";
        String[] result = emptyToNullMapper.readValue(json, String[].class);
        assertNull("Empty string should deserialize to null when flag is enabled", result);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testNonEmptyScalarWithoutSingleValueFeatureThrows() throws IOException {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        strictMapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        strictMapper.readValue("\"notAnArray\"", String[].class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testObjectTokenThrowsException() throws IOException {
        mapper.readValue("{\"key\":\"value\"}", String[].class);
    }

    // =========================================================================
    // Partition D: Custom Element Deserializer & Converter Coverage
    // =========================================================================

    public static class UpperCaseDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String val = jp.getText();
            return val == null ? null : val.toUpperCase();
        }
    }

    public static class CustomHolder {
        @JsonDeserialize(contentUsing = UpperCaseDeserializer.class)
        public String[] items;
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerWithElementsAndNull() throws IOException {
        String json = "{\"items\": [\"hello\", null, \"world\"]}";
        CustomHolder holder = mapper.readValue(json, CustomHolder.class);
        assertNotNull(holder);
        assertNotNull(holder.items);
        assertEquals(3, holder.items.length);
        assertEquals("HELLO", holder.items[0]);
        assertNull("Null element in custom deserializer should remain null", holder.items[1]);
        assertEquals("WORLD", holder.items[2]);
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerChunkOverflow() throws IOException {
        StringBuilder sb = new StringBuilder("{\"items\": [");
        for (int i = 0; i < 40; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"val").append(i).append("\"");
        }
        sb.append("]}");

        CustomHolder holder = mapper.readValue(sb.toString(), CustomHolder.class);
        assertNotNull(holder);
        assertNotNull(holder.items);
        assertEquals(40, holder.items.length);
        assertEquals("VAL0", holder.items[0]);
        assertEquals("VAL39", holder.items[39]);
    }

    public static class PrefixConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return "prefix_" + value;
        }
    }

    public static class ConverterHolder {
        @JsonDeserialize(contentConverter = PrefixConverter.class)
        public String[] items;
    }

    @Test(timeout = 4000)
    public void testContentConverterContextualization() throws IOException {
        String json = "{\"items\": [\"alpha\", \"beta\"]}";
        ConverterHolder holder = mapper.readValue(json, ConverterHolder.class);
        assertNotNull(holder);
        assertNotNull(holder.items);
        assertArrayEquals(new String[] { "prefix_alpha", "prefix_beta" }, holder.items);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Polymorphic & Contextual Integrity
    // =========================================================================

    public static class PolymorphicArrayHolder {
        public Object[] data;
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws IOException {
        ObjectMapper polyMapper = new ObjectMapper();
        polyMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        String[] original = new String[] { "itemA", "itemB" };
        String json = polyMapper.writeValueAsString(original);

        Object result = polyMapper.readValue(json, Object.class);
        assertNotNull(result);
        assertTrue("Result should be String[]", result instanceof String[]);
        assertArrayEquals(original, (String[]) result);
    }

    @Test(timeout = 4000)
    public void testDirectCreateContextualWithDefault() throws JsonMappingException {
        StringArrayDeserializer deser = new StringArrayDeserializer();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        assertNotNull(contextual);
        assertSame("When no custom deserializer is present, instance should be returned unchanged", deser, contextual);
    }

    @Test(timeout = 4000)
    public void testDirectCreateContextualWithCustomElementDeser() throws JsonMappingException {
        JsonDeserializer<String> customInner = new UpperCaseDeserializer();
        StringArrayDeserializer deser = new StringArrayDeserializer(customInner);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        assertNotNull(contextual);
        assertTrue("Should return a StringArrayDeserializer", contextual instanceof StringArrayDeserializer);
    }

    @Test(timeout = 4000)
    public void testInstanceConstant() {
        assertNotNull(StringArrayDeserializer.instance);
        assertEquals(String[].class, StringArrayDeserializer.instance.getValueType());
    }
}