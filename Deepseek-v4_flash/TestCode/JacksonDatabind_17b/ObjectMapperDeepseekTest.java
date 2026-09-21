package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: ObjectMapper, specifically DefaultTypeResolverBuilder.useForType(JavaType)
 * Known Defect (Defects4J #??): 
 *   For DefaultTyping.OBJECT_AND_NON_CONCRETE and NON_CONCRETE_AND_ARRAYS,
 *   the condition incorrectly uses OR instead of AND when excluding TreeNode subtypes.
 *   Original: return (t.getRawClass() == Object.class) || (!t.isConcrete() || TreeNode.class.isAssignableFrom(t.getRawClass()));
 *   Correct:  return (t.getRawClass() == Object.class) || (!t.isConcrete() && !TreeNode.class.isAssignableFrom(t.getRawClass()));
 * Bug trigger: enabling default typing and serializing/deserializing JSON tree nodes (e.g., ArrayNode) under OBJECT_AND_NON_CONCRETE.
 *
 * Branches covered in useForType:
 *   - JAVA_LANG_OBJECT: returns (t.getRawClass() == Object.class)
 *   - OBJECT_AND_NON_CONCRETE: returns (t.getRawClass() == Object.class) || (!t.isConcrete() || TreeNode.isAssignableFrom(...))
 *   - NON_CONCRETE_AND_ARRAYS: unwraps array, then falls through to OBJECT_AND_NON_CONCRETE condition
 *   - NON_FINAL: unwraps array, returns !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass())
 * 
 * Test Cases:
 *   A. Core functional: ObjectMapper construction, copy, config methods, serialization/deserialization basics.
 *   B. Boundary: null inputs, empty strings, extreme values.
 *   C. Defect-targeted: default typing with JSON tree nodes (ArrayNode) to expose the faulty logic.
 *   D. Exception paths: invalid arguments, copy without override.
 *   E. Lifecycle: mixin count, version, factory access.
 */

public class ObjectMapperDeepseekTest {

    /* ======================================================
     * Partition A: Core Functional Logic & State Transitions
     * ====================================================== */

    @Test(timeout = 4000)
    public void testDefaultConstruction() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper);
        // Config objects exist
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getDeserializationContext());
        assertNotNull(mapper.getSerializerProvider());
        assertNotNull(mapper.getSerializerFactory());
        assertNotNull(mapper.getFactory());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getNodeFactory());
        assertNotNull(mapper.getSubtypeResolver());
    }

    @Test(timeout = 4000)
    public void testCopy() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ObjectMapper copy = mapper.copy();
        assertNotNull(copy);
        assertNotSame(mapper, copy);
        assertTrue(copy.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test(timeout = 4000)
    public void testConfigurationChaining() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertFalse(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    @Test(timeout = 4000)
    public void testSimpleSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString("hello");
        assertEquals("\"hello\"", json);
        String result = mapper.readValue(json, String.class);
        assertEquals("hello", result);
    }

    @Test(timeout = 4000)
    public void testBeanRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean(42, "test");
        String json = mapper.writeValueAsString(bean);
        SimpleBean back = mapper.readValue(json, SimpleBean.class);
        assertEquals(bean.x, back.x);
        assertEquals(bean.name, back.name);
    }

    // Helper bean
    static class SimpleBean {
        public int x;
        public String name;
        public SimpleBean() {}
        public SimpleBean(int x, String name) { this.x = x; this.name = name; }
    }

    @Test(timeout = 4000)
    public void testJsonNodeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        assertNotNull(obj);
        ArrayNode arr = mapper.createArrayNode();
        assertNotNull(arr);
    }

    /* ======================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ====================================================== */

    @Test(timeout = 4000)
    public void testNullInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // readValue with null content - should throw
        try {
            mapper.readValue((String) null, Object.class);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("", Object.class);
            fail("Should throw JsonMappingException");
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            // expected: no content
        }
    }

    @Test(timeout = 4000)
    public void testBoundaryNumeric() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Integer.MAX_VALUE);
        assertEquals(String.valueOf(Integer.MAX_VALUE), json);
        int back = mapper.readValue(json, int.class);
        assertEquals(Integer.MAX_VALUE, back);
    }

    /* ======================================================
     * Partition C: Defect-Targeted Branch Zone
     * ====================================================== */

    @Test(timeout = 4000)
    public void testArrayWithDefaultTyping() throws Exception {
        // Defects4J fails here: default typing incorrectly applied to tree nodes
        // Expected: successful read/write without type id errors
        ObjectMapper mapper = new ObjectMapper();
        // Enable default typing for OBJECT_AND_NON_CONCRETE (used in issue)
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        // Create an array node
        ArrayNode arr = mapper.createArrayNode();
        arr.add("foo");
        arr.add(42);
        // Serialize (should include type id for nodes? bug: yes)
        String json = mapper.writeValueAsString(arr);
        // Deserialize back
        ArrayNode result = mapper.readValue(json, ArrayNode.class);
        // Bug: reads fine? Actually bug causes exception when reading because type id expected but not present?
        // The known defect shows: JsonMappingException: Unexpected token (VALUE_NUMBER_INT), expected VALUE_STRING
        // That happens when reading back JSON that contains type id? Wait: The defect description says:
        // "Unexpected token (VALUE_NUMBER_INT), expected VALUE_STRING: need JSON String that contains type id"
        // That indicates that when reading, the parser expects a string type id but gets a number.
        // So writing with default typing produces JSON with type id for each node, but reading fails because
        // the type id is not recognized? Actually it's the opposite: the default typing is applied to TreeNode,
        // so when writing ArrayNode, it wraps each element with type info. Then when reading, it expects a string
        // as type id but encounters a number. That matches the bug: default typing should NOT be applied to TreeNode.
        // So the test should expect success. In the defective version, it throws JsonMappingException.
        // We assert that the deserialization succeeds to reveal the bug.
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testDefaultTypingNonFinal() throws Exception {
        // NON_FINAL should not apply to final types like String, but apply to non-final
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString("test");
        // String is final, so should not have type id
        String back = mapper.readValue(json, String.class);
        assertEquals("test", back);
    }

    @Test(timeout = 4000)
    public void testDefaultTypingJavaLangObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        // For Object.class, type id should be added
        Object value = "test";
        String json = mapper.writeValueAsString(value);
        Object back = mapper.readValue(json, Object.class);
        assertEquals("test", back);
    }

    /* ======================================================
     * Partition D: Exception & Defensive Guard Paths
     * ====================================================== */

    @Test(timeout = 4000)
    public void testInvalidCopy() {
        ObjectMapper mapper = new ObjectMapper() {
            // Do not override copy()
        };
        try {
            mapper.copy();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("does not override copy()"));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRegisterModuleNullName() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Module() {
            @Override public String getModuleName() { return null; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public void setupModule(SetupContext context) {}
        });
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRegisterModuleNullVersion() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Module() {
            @Override public String getModuleName() { return "test"; }
            @Override public Version version() { return null; }
            @Override public void setupModule(SetupContext context) {}
        });
    }

    @Test(timeout = 4000)
    public void testVerifySchemaTypeInvalid() {
        ObjectMapper mapper = new ObjectMapper();
        // Using a dummy schema that is not supported by JsonFactory
        try {
            mapper.reader(new FormatSchema() {
                @Override public String getSchemaType() { return "dummy"; }
            });
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not use FormatSchema")); // form depends on factory - simplified
        }
    }

    /* ======================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ====================================================== */

    @Test(timeout = 4000)
    public void testMixInCount() {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(0, mapper.mixInCount());
        mapper.addMixIn(String.class, Object.class);
        assertEquals(1, mapper.mixInCount());
        Class<?> mixin = mapper.findMixInClassFor(String.class);
        assertEquals(Object.class, mixin);
    }

    @Test(timeout = 4000)
    public void testVersion() {
        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.core.Version v = mapper.version();
        assertNotNull(v);
    }

    @Test(timeout = 4000)
    public void testSerializationConfigAccess() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT) == false);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) == true);
    }

    @Test(timeout = 4000)
    public void testSetVisibility() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
        assertTrue(mapper.getVisibilityChecker().isFieldVisible(SimpleBean.class.getDeclaredFields()[0]));
    }

    // Additional test to cover Mixed conditions
    @Test(timeout = 4000)
    public void testEnableDisableFeatures() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.CLOSE_CLOSEABLE);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(mapper.isEnabled(SerializationFeature.CLOSE_CLOSEABLE));
        // Test disable with multiple
        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE, SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(mapper.isEnabled(SerializationFeature.CLOSE_CLOSEABLE));
    }
}