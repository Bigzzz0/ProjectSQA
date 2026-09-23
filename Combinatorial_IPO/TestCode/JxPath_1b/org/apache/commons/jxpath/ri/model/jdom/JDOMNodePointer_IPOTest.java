package org.apache.commons.jxpath.ri.model.jdom;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for JDOMNodePointer.
 */
public class JDOMNodePointer_IPOTest {
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
    public void test_getNamespaceURI_pairwise_001() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_002() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_003() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_004() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.US)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_005() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_006() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_007() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_008() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_009() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_010() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix=""
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI(""));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_011() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix=""
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI(""));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_012() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix=""
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI(""));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_013() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT, prefix=" "
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).getNamespaceURI(" "));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_014() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, prefix=" "
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.US)).getNamespaceURI(" "));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_015() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN, prefix=" "
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).getNamespaceURI(" "));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_016() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT, prefix="a"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).getNamespaceURI("a"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_017() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, prefix="a"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.US)).getNamespaceURI("a"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_018() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN, prefix="a"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).getNamespaceURI("a"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_019() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="test123"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("test123"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_020() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US, prefix="test123"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).getNamespaceURI("test123"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_021() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN, prefix="test123"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).getNamespaceURI("test123"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_022() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="!@#"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("!@#"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_023() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="!@#"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("!@#"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_024() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="!@#"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("!@#"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_025() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="0"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("0"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_026() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="0"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("0"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_027() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="0"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("0"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_028() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="-1"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("-1"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_029() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="-1"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("-1"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_030() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="-1"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("-1"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_031() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="1.5"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("1.5"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_032() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="1.5"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("1.5"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_033() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="1.5"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("1.5"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_034() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="9223372036854775807"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("9223372036854775807"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_035() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="9223372036854775807"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("9223372036854775807"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_036() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="9223372036854775807"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("9223372036854775807"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_037() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="9223372036854775808"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("9223372036854775808"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_038() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="9223372036854775808"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("9223372036854775808"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_039() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="9223372036854775808"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("9223372036854775808"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_040() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, prefix="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getNamespaceURI("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_041() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, prefix="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getNamespaceURI("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_042() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, prefix="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getNamespaceURI("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test(timeout = 4000)
    public void test_getNamespaceURI_pairwise_043() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT, prefix=" "
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).getNamespaceURI(" "));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_044() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_045() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_046() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_047() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_048() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_049() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_050() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_051() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isCollection_pairwise_052() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isCollection();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_053() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_054() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_055() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_056() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_057() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_058() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_059() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_060() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLength_pairwise_061() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getLength();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_062() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_063() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_064() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_065() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_066() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_067() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_068() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_069() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLeaf_pairwise_070() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLeaf();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_071() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang=""
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_072() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang=""
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_073() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang=""
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_074() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT, lang=" "
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).isLanguage(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_075() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, lang=" "
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).isLanguage(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_076() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN, lang=" "
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).isLanguage(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_077() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT, lang="a"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).isLanguage("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_078() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, lang="a"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).isLanguage("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_079() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN, lang="a"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).isLanguage("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_080() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="test123"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_081() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US, lang="test123"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).isLanguage("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_082() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN, lang="test123"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).isLanguage("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_083() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="!@#"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_084() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="!@#"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_085() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="!@#"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_086() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="0"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_087() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="0"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_088() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="0"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_089() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="-1"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_090() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="-1"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_091() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="-1"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_092() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="1.5"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_093() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="1.5"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_094() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="1.5"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_095() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="9223372036854775807"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_096() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="9223372036854775807"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_097() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="9223372036854775807"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_098() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="9223372036854775808"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_099() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="9223372036854775808"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_100() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="9223372036854775808"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_101() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, lang="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).isLanguage("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_102() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, lang="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).isLanguage("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_103() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, lang="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).isLanguage("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isLanguage_pairwise_104() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT, lang=" "
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).isLanguage(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_105() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_106() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_107() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_108() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.US)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_109() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.US)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_110() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_111() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_112() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_getLanguage_pairwise_113() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        assertNull((new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).getLanguage());
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_114() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_115() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_116() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_117() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_118() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_119() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_120() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_121() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asPath_pairwise_122() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).asPath();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_123() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.ROOT, object=new Object()
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_124() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.US, object=new Object()
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.US)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_125() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.JAPAN, object=new Object()
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.JAPAN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_126() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.ROOT, object="sample_str"
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.ROOT)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_127() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, object="sample_str"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_128() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.JAPAN, object="sample_str"
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.JAPAN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_129() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.ROOT, object=Integer.valueOf(1)
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.ROOT)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_130() throws Exception {
        // Combination: receiver__node=new Object(), receiver__locale=java.util.Locale.US, object=Integer.valueOf(1)
        Object actual = (new JDOMNodePointer(new Object(), java.util.Locale.US)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_131() throws Exception {
        // Combination: receiver__node="sample_str", receiver__locale=java.util.Locale.JAPAN, object=Integer.valueOf(1)
        Object actual = (new JDOMNodePointer("sample_str", java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_132() throws Exception {
        // Combination: receiver__node=Integer.valueOf(1), receiver__locale=java.util.Locale.US, object="sample_str"
        Object actual = (new JDOMNodePointer(Integer.valueOf(1), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
