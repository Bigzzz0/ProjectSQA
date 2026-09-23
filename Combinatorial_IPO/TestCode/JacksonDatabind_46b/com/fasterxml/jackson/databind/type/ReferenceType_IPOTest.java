package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ReferenceType.
 */
public class ReferenceType_IPOTest {
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
    public void test_buildCanonicalName_pairwise_001() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.String<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_002() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Object<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_003() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Integer<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_004() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.String<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_005() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Object<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_006() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Integer<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_007() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Integer<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_008() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.String<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_009() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Object<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_buildCanonicalName_pairwise_010() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).buildCanonicalName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang.Object<java.lang.String", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_011() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_012() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_013() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_014() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_015() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_016() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_017() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_018() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_019() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isReferenceType_pairwise_020() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).isReferenceType();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_021() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_022() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_023() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_024() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_025() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_026() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_027() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_028() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_029() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeCount_pairwise_030() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).containedTypeCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_031() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, index=0
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).containedTypeName(0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("T", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_032() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=false, index=0
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", false)).containedTypeName(0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("T", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_033() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, index=1
        assertNull((new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).containedTypeName(1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_034() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false, index=1
        assertNull((new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).containedTypeName(1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_035() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, index=-1
        assertNull((new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).containedTypeName(-1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_036() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false, index=-1
        assertNull((new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), false)).containedTypeName(-1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_037() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true, index=Integer.MAX_VALUE
        assertNull((new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).containedTypeName(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_038() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=false, index=Integer.MAX_VALUE
        assertNull((new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), false)).containedTypeName(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_039() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, index=Integer.MIN_VALUE
        assertNull((new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).containedTypeName(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_040() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false, index=Integer.MIN_VALUE
        assertNull((new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).containedTypeName(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_041() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=true, index=-1
        assertNull((new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), true)).containedTypeName(-1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_042() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, index=Integer.MAX_VALUE
        assertNull((new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).containedTypeName(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_043() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, index=0
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), true)).containedTypeName(0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("T", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_044() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, index=1
        assertNull((new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).containedTypeName(1));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_045() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=true, index=Integer.MIN_VALUE
        assertNull((new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", true)).containedTypeName(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_046() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, index=Integer.MIN_VALUE
        assertNull((new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), true)).containedTypeName(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_containedTypeName_pairwise_047() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true, index=0
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).containedTypeName(0);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("T", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_048() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/String;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_049() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_050() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Integer;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_051() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/String;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_052() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_053() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Integer;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_054() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Integer;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_055() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/String;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_056() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).getErasedSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getErasedSignature_pairwise_057() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).getErasedSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Object;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_058() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/String<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_059() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).getGenericSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Object<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_060() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Integer<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_061() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).getGenericSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/String<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_062() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Object<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_063() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).getGenericSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Integer<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_064() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Integer<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_065() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/String<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_066() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, sb=new java.lang.StringBuilder("test")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).getGenericSignature(new java.lang.StringBuilder("test"));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("testLjava/lang/Object<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getGenericSignature_pairwise_067() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true, sb=new java.lang.StringBuilder("")
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).getGenericSignature(new java.lang.StringBuilder(""));
        assertNotNull(actual);
        assertEquals("java.lang.StringBuilder", actual.getClass().getName());
        assertEquals("Ljava/lang/Object<Ljava/lang/String;>;", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_068() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.String<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_069() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Object<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_070() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Integer<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_071() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.String<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_072() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Object<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_073() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Integer<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_074() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Integer<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_075() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), false)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.String<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_076() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Object<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_077() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("[reference type, class java.lang.Object<java.lang.String<[simple type, class java.lang.String]>]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_078() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, o=new Object()
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_079() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler="sample_str", receiver__asStatic=false, o=new Object()
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", "sample_str", false)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_080() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, o="sample_str"
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), true)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_081() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=false, o="sample_str"
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), false)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_082() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, o=Integer.valueOf(1)
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_083() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false, o=Integer.valueOf(1)
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), Integer.valueOf(1), false)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_084() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=new Object(), receiver__asStatic=true, o=Integer.valueOf(1)
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", new Object(), true)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_085() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=false, o=new Object()
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), Integer.valueOf(1), false)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_086() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler=new Object(), receiver__asStatic=true, o="sample_str"
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), new Object(), true)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_087() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler="sample_str", receiver__asStatic=true, o="sample_str"
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), "sample_str", true)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_088() throws Exception {
        // Combination: receiver__cls=Object.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=new Object(), receiver__typeHandler=new Object(), receiver__asStatic=true, o=new Object()
        Object actual = (new ReferenceType(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), new Object(), new Object(), true)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_089() throws Exception {
        // Combination: receiver__cls=Integer.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler="sample_str", receiver__typeHandler=Integer.valueOf(1), receiver__asStatic=true, o=new Object()
        Object actual = (new ReferenceType(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), "sample_str", Integer.valueOf(1), true)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_090() throws Exception {
        // Combination: receiver__cls=String.class, receiver__refType=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), receiver__valueHandler=Integer.valueOf(1), receiver__typeHandler="sample_str", receiver__asStatic=true, o=new Object()
        Object actual = (new ReferenceType(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), Integer.valueOf(1), "sample_str", true)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
