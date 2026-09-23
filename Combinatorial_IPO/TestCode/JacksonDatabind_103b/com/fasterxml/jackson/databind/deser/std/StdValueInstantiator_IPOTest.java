package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StdValueInstantiator.
 */
public class StdValueInstantiator_IPOTest {
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
    public void test_getValueTypeDesc_pairwise_001() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).getValueTypeDesc();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("`java.lang.String`", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValueTypeDesc_pairwise_002() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).getValueTypeDesc();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("`java.lang.Object`", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValueTypeDesc_pairwise_003() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).getValueTypeDesc();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("`java.lang.Integer`", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromString_pairwise_004() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromString();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromString_pairwise_005() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromString();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromString_pairwise_006() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromString();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromInt_pairwise_007() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromInt();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromInt_pairwise_008() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromInt();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromInt_pairwise_009() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromInt();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromLong_pairwise_010() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromLong();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromLong_pairwise_011() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromLong();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromLong_pairwise_012() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromLong();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromDouble_pairwise_013() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromDouble();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromDouble_pairwise_014() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromDouble();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromDouble_pairwise_015() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromDouble();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromBoolean_pairwise_016() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromBoolean();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromBoolean_pairwise_017() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromBoolean();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromBoolean_pairwise_018() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromBoolean();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDefault_pairwise_019() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateUsingDefault();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDefault_pairwise_020() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateUsingDefault();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDefault_pairwise_021() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateUsingDefault();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDelegate_pairwise_022() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateUsingDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDelegate_pairwise_023() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateUsingDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingDelegate_pairwise_024() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateUsingDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingArrayDelegate_pairwise_025() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateUsingArrayDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingArrayDelegate_pairwise_026() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateUsingArrayDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateUsingArrayDelegate_pairwise_027() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateUsingArrayDelegate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromObjectWith_pairwise_028() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canCreateFromObjectWith();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromObjectWith_pairwise_029() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canCreateFromObjectWith();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canCreateFromObjectWith_pairwise_030() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canCreateFromObjectWith();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canInstantiate_pairwise_031() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).canInstantiate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canInstantiate_pairwise_032() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).canInstantiate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_canInstantiate_pairwise_033() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class
        Object actual = (new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).canInstantiate();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFromObjectArguments_pairwise_034() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=String.class, config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()
        assertNull((new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), String.class)).getFromObjectArguments(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()));
    }

    @Test(timeout = 4000)
    public void test_getFromObjectArguments_pairwise_035() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Object.class, config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()
        assertNull((new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Object.class)).getFromObjectArguments(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()));
    }

    @Test(timeout = 4000)
    public void test_getFromObjectArguments_pairwise_036() throws Exception {
        // Combination: receiver__config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), receiver__valueType=Integer.class, config=new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()
        assertNull((new StdValueInstantiator(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig(), Integer.class)).getFromObjectArguments(new com.fasterxml.jackson.databind.ObjectMapper().getDeserializationConfig()));
    }

}
