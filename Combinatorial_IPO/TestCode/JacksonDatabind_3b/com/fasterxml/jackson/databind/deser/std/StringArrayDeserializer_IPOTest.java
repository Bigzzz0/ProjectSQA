package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StringArrayDeserializer.
 */
public class StringArrayDeserializer_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_deserialize_pairwise_001() throws Exception {
        // Combination: jp=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), ctxt=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationContext()
        try {
            (new StringArrayDeserializer()).deserialize(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationContext());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__deserializeCustom_pairwise_002() throws Exception {
        // Combination: jp=new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), ctxt=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationContext()
        try {
            (new StringArrayDeserializer())._deserializeCustom(new com.fasterxml.jackson.core.JsonFactory().createParser("{}"), new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationContext());
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
