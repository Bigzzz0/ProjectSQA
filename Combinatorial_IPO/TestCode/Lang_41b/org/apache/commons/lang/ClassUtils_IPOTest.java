package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ClassUtils.
 */
public class ClassUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_001() throws Exception {
        // Combination: object=new Object(), valueIfNull=""
        Object actual = ClassUtils.getShortClassName(new Object(), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_002() throws Exception {
        // Combination: object=new Object(), valueIfNull=" "
        Object actual = ClassUtils.getShortClassName(new Object(), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_003() throws Exception {
        // Combination: object=new Object(), valueIfNull="a"
        Object actual = ClassUtils.getShortClassName(new Object(), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_004() throws Exception {
        // Combination: object=new Object(), valueIfNull="test123"
        Object actual = ClassUtils.getShortClassName(new Object(), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_005() throws Exception {
        // Combination: object=new Object(), valueIfNull="!@#"
        Object actual = ClassUtils.getShortClassName(new Object(), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_006() throws Exception {
        // Combination: object=new Object(), valueIfNull="0"
        Object actual = ClassUtils.getShortClassName(new Object(), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_007() throws Exception {
        // Combination: object=new Object(), valueIfNull="-1"
        Object actual = ClassUtils.getShortClassName(new Object(), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_008() throws Exception {
        // Combination: object=new Object(), valueIfNull="1.5"
        Object actual = ClassUtils.getShortClassName(new Object(), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_009() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortClassName(new Object(), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_010() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortClassName(new Object(), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_011() throws Exception {
        // Combination: object=new Object(), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortClassName(new Object(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_012() throws Exception {
        // Combination: object="sample_str", valueIfNull=""
        Object actual = ClassUtils.getShortClassName("sample_str", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_013() throws Exception {
        // Combination: object="sample_str", valueIfNull=" "
        Object actual = ClassUtils.getShortClassName("sample_str", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_014() throws Exception {
        // Combination: object="sample_str", valueIfNull="a"
        Object actual = ClassUtils.getShortClassName("sample_str", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_015() throws Exception {
        // Combination: object="sample_str", valueIfNull="test123"
        Object actual = ClassUtils.getShortClassName("sample_str", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_016() throws Exception {
        // Combination: object="sample_str", valueIfNull="!@#"
        Object actual = ClassUtils.getShortClassName("sample_str", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_017() throws Exception {
        // Combination: object="sample_str", valueIfNull="0"
        Object actual = ClassUtils.getShortClassName("sample_str", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_018() throws Exception {
        // Combination: object="sample_str", valueIfNull="-1"
        Object actual = ClassUtils.getShortClassName("sample_str", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_019() throws Exception {
        // Combination: object="sample_str", valueIfNull="1.5"
        Object actual = ClassUtils.getShortClassName("sample_str", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_020() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortClassName("sample_str", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_021() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortClassName("sample_str", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_022() throws Exception {
        // Combination: object="sample_str", valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortClassName("sample_str", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_023() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=""
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_024() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=" "
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_025() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="a"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_026() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="test123"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_027() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="!@#"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_028() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="0"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_029() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="-1"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_030() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="1.5"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_031() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_032() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortClassName_pairwise_033() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortClassName(Integer.valueOf(1), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_034() throws Exception {
        // Combination: object=new Object(), valueIfNull=""
        Object actual = ClassUtils.getPackageName(new Object(), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_035() throws Exception {
        // Combination: object=new Object(), valueIfNull=" "
        Object actual = ClassUtils.getPackageName(new Object(), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_036() throws Exception {
        // Combination: object=new Object(), valueIfNull="a"
        Object actual = ClassUtils.getPackageName(new Object(), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_037() throws Exception {
        // Combination: object=new Object(), valueIfNull="test123"
        Object actual = ClassUtils.getPackageName(new Object(), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_038() throws Exception {
        // Combination: object=new Object(), valueIfNull="!@#"
        Object actual = ClassUtils.getPackageName(new Object(), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_039() throws Exception {
        // Combination: object=new Object(), valueIfNull="0"
        Object actual = ClassUtils.getPackageName(new Object(), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_040() throws Exception {
        // Combination: object=new Object(), valueIfNull="-1"
        Object actual = ClassUtils.getPackageName(new Object(), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_041() throws Exception {
        // Combination: object=new Object(), valueIfNull="1.5"
        Object actual = ClassUtils.getPackageName(new Object(), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_042() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageName(new Object(), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_043() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageName(new Object(), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_044() throws Exception {
        // Combination: object=new Object(), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageName(new Object(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_045() throws Exception {
        // Combination: object="sample_str", valueIfNull=""
        Object actual = ClassUtils.getPackageName("sample_str", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_046() throws Exception {
        // Combination: object="sample_str", valueIfNull=" "
        Object actual = ClassUtils.getPackageName("sample_str", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_047() throws Exception {
        // Combination: object="sample_str", valueIfNull="a"
        Object actual = ClassUtils.getPackageName("sample_str", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_048() throws Exception {
        // Combination: object="sample_str", valueIfNull="test123"
        Object actual = ClassUtils.getPackageName("sample_str", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_049() throws Exception {
        // Combination: object="sample_str", valueIfNull="!@#"
        Object actual = ClassUtils.getPackageName("sample_str", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_050() throws Exception {
        // Combination: object="sample_str", valueIfNull="0"
        Object actual = ClassUtils.getPackageName("sample_str", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_051() throws Exception {
        // Combination: object="sample_str", valueIfNull="-1"
        Object actual = ClassUtils.getPackageName("sample_str", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_052() throws Exception {
        // Combination: object="sample_str", valueIfNull="1.5"
        Object actual = ClassUtils.getPackageName("sample_str", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_053() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageName("sample_str", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_054() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageName("sample_str", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_055() throws Exception {
        // Combination: object="sample_str", valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageName("sample_str", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_056() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=""
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_057() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=" "
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_058() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="a"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_059() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="test123"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_060() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="!@#"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_061() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="0"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_062() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="-1"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_063() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="1.5"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_064() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_065() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageName_pairwise_066() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageName(Integer.valueOf(1), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_067() throws Exception {
        // Combination: classArray=String.class, toClassArray=String.class
        Object actual = ClassUtils.isAssignable(String.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_068() throws Exception {
        // Combination: classArray=String.class, toClassArray=Object.class
        Object actual = ClassUtils.isAssignable(String.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_069() throws Exception {
        // Combination: classArray=String.class, toClassArray=Integer.class
        Object actual = ClassUtils.isAssignable(String.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_070() throws Exception {
        // Combination: classArray=Object.class, toClassArray=String.class
        Object actual = ClassUtils.isAssignable(Object.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_071() throws Exception {
        // Combination: classArray=Object.class, toClassArray=Object.class
        Object actual = ClassUtils.isAssignable(Object.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_072() throws Exception {
        // Combination: classArray=Object.class, toClassArray=Integer.class
        Object actual = ClassUtils.isAssignable(Object.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_073() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=String.class
        Object actual = ClassUtils.isAssignable(Integer.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_074() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=Object.class
        Object actual = ClassUtils.isAssignable(Integer.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_075() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=Integer.class
        Object actual = ClassUtils.isAssignable(Integer.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_076() throws Exception {
        // Combination: classArray=String.class, toClassArray=String.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(String.class, String.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_077() throws Exception {
        // Combination: classArray=Object.class, toClassArray=Object.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Object.class, Object.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_078() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=Integer.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Integer.class, Integer.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_079() throws Exception {
        // Combination: classArray=String.class, toClassArray=Object.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(String.class, Object.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_080() throws Exception {
        // Combination: classArray=Object.class, toClassArray=String.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(Object.class, String.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_081() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=String.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(Integer.class, String.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_082() throws Exception {
        // Combination: classArray=Integer.class, toClassArray=Object.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Integer.class, Object.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_083() throws Exception {
        // Combination: classArray=String.class, toClassArray=Integer.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(String.class, Integer.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_084() throws Exception {
        // Combination: classArray=Object.class, toClassArray=Integer.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Object.class, Integer.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_085() throws Exception {
        // Combination: cls=String.class, toClass=String.class
        Object actual = ClassUtils.isAssignable(String.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_086() throws Exception {
        // Combination: cls=String.class, toClass=Object.class
        Object actual = ClassUtils.isAssignable(String.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_087() throws Exception {
        // Combination: cls=String.class, toClass=Integer.class
        Object actual = ClassUtils.isAssignable(String.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_088() throws Exception {
        // Combination: cls=Object.class, toClass=String.class
        Object actual = ClassUtils.isAssignable(Object.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_089() throws Exception {
        // Combination: cls=Object.class, toClass=Object.class
        Object actual = ClassUtils.isAssignable(Object.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_090() throws Exception {
        // Combination: cls=Object.class, toClass=Integer.class
        Object actual = ClassUtils.isAssignable(Object.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_091() throws Exception {
        // Combination: cls=Integer.class, toClass=String.class
        Object actual = ClassUtils.isAssignable(Integer.class, String.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_092() throws Exception {
        // Combination: cls=Integer.class, toClass=Object.class
        Object actual = ClassUtils.isAssignable(Integer.class, Object.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_093() throws Exception {
        // Combination: cls=Integer.class, toClass=Integer.class
        Object actual = ClassUtils.isAssignable(Integer.class, Integer.class);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_094() throws Exception {
        // Combination: cls=String.class, toClass=String.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(String.class, String.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_095() throws Exception {
        // Combination: cls=Object.class, toClass=Object.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Object.class, Object.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_096() throws Exception {
        // Combination: cls=Integer.class, toClass=Integer.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Integer.class, Integer.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_097() throws Exception {
        // Combination: cls=String.class, toClass=Object.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(String.class, Object.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_098() throws Exception {
        // Combination: cls=Object.class, toClass=String.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(Object.class, String.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_099() throws Exception {
        // Combination: cls=Integer.class, toClass=String.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(Integer.class, String.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_100() throws Exception {
        // Combination: cls=Integer.class, toClass=Object.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Integer.class, Object.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_101() throws Exception {
        // Combination: cls=String.class, toClass=Integer.class, autoboxing=false
        Object actual = ClassUtils.isAssignable(String.class, Integer.class, false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isAssignable_pairwise_102() throws Exception {
        // Combination: cls=Object.class, toClass=Integer.class, autoboxing=true
        Object actual = ClassUtils.isAssignable(Object.class, Integer.class, true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_103() throws Exception {
        // Combination: object=new Object(), valueIfNull=""
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_104() throws Exception {
        // Combination: object=new Object(), valueIfNull=" "
        Object actual = ClassUtils.getShortCanonicalName(new Object(), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_105() throws Exception {
        // Combination: object=new Object(), valueIfNull="a"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_106() throws Exception {
        // Combination: object=new Object(), valueIfNull="test123"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_107() throws Exception {
        // Combination: object=new Object(), valueIfNull="!@#"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_108() throws Exception {
        // Combination: object=new Object(), valueIfNull="0"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_109() throws Exception {
        // Combination: object=new Object(), valueIfNull="-1"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_110() throws Exception {
        // Combination: object=new Object(), valueIfNull="1.5"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_111() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_112() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_113() throws Exception {
        // Combination: object=new Object(), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortCanonicalName(new Object(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Object", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_114() throws Exception {
        // Combination: object="sample_str", valueIfNull=""
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_115() throws Exception {
        // Combination: object="sample_str", valueIfNull=" "
        Object actual = ClassUtils.getShortCanonicalName("sample_str", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_116() throws Exception {
        // Combination: object="sample_str", valueIfNull="a"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_117() throws Exception {
        // Combination: object="sample_str", valueIfNull="test123"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_118() throws Exception {
        // Combination: object="sample_str", valueIfNull="!@#"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_119() throws Exception {
        // Combination: object="sample_str", valueIfNull="0"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_120() throws Exception {
        // Combination: object="sample_str", valueIfNull="-1"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_121() throws Exception {
        // Combination: object="sample_str", valueIfNull="1.5"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_122() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_123() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_124() throws Exception {
        // Combination: object="sample_str", valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortCanonicalName("sample_str", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("String", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_125() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=""
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_126() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=" "
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_127() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="a"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_128() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="test123"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_129() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="!@#"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_130() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="0"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_131() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="-1"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_132() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="1.5"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_133() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_134() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getShortCanonicalName_pairwise_135() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getShortCanonicalName(Integer.valueOf(1), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("Integer", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_136() throws Exception {
        // Combination: object=new Object(), valueIfNull=""
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_137() throws Exception {
        // Combination: object=new Object(), valueIfNull=" "
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_138() throws Exception {
        // Combination: object=new Object(), valueIfNull="a"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_139() throws Exception {
        // Combination: object=new Object(), valueIfNull="test123"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_140() throws Exception {
        // Combination: object=new Object(), valueIfNull="!@#"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_141() throws Exception {
        // Combination: object=new Object(), valueIfNull="0"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_142() throws Exception {
        // Combination: object=new Object(), valueIfNull="-1"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_143() throws Exception {
        // Combination: object=new Object(), valueIfNull="1.5"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_144() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_145() throws Exception {
        // Combination: object=new Object(), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_146() throws Exception {
        // Combination: object=new Object(), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageCanonicalName(new Object(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_147() throws Exception {
        // Combination: object="sample_str", valueIfNull=""
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_148() throws Exception {
        // Combination: object="sample_str", valueIfNull=" "
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_149() throws Exception {
        // Combination: object="sample_str", valueIfNull="a"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_150() throws Exception {
        // Combination: object="sample_str", valueIfNull="test123"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_151() throws Exception {
        // Combination: object="sample_str", valueIfNull="!@#"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_152() throws Exception {
        // Combination: object="sample_str", valueIfNull="0"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_153() throws Exception {
        // Combination: object="sample_str", valueIfNull="-1"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_154() throws Exception {
        // Combination: object="sample_str", valueIfNull="1.5"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_155() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_156() throws Exception {
        // Combination: object="sample_str", valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_157() throws Exception {
        // Combination: object="sample_str", valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageCanonicalName("sample_str", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_158() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=""
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_159() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull=" "
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_160() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="a"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_161() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="test123"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_162() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="!@#"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_163() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="0"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_164() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="-1"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_165() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="1.5"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_166() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775807"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_167() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="9223372036854775808"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPackageCanonicalName_pairwise_168() throws Exception {
        // Combination: object=Integer.valueOf(1), valueIfNull="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ClassUtils.getPackageCanonicalName(Integer.valueOf(1), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("java.lang", String.valueOf(actual));
    }

}
