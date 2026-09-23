package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NodeUtil.
 */
public class NodeUtil_IPOTest {
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
    public void test_newQualifiedNameNode_pairwise_001() throws Exception {
        // Combination: name="", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME  0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_002() throws Exception {
        // Combination: name=" ", lineno=1, charno=0
        Object actual = NodeUtil.newQualifiedNameNode(" ", 1, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME   1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_003() throws Exception {
        // Combination: name="a", lineno=-1, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("a", -1, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_004() throws Exception {
        // Combination: name="test123", lineno=Integer.MAX_VALUE, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("test123", Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME test123 1048575", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_005() throws Exception {
        // Combination: name="!@#", lineno=Integer.MIN_VALUE, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("!@#", Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME !@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_006() throws Exception {
        // Combination: name=" ", lineno=0, charno=1
        Object actual = NodeUtil.newQualifiedNameNode(" ", 0, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME   0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_007() throws Exception {
        // Combination: name="", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME  1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_008() throws Exception {
        // Combination: name="test123", lineno=-1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("test123", -1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_009() throws Exception {
        // Combination: name="a", lineno=Integer.MAX_VALUE, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("a", Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME a 1048575", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_010() throws Exception {
        // Combination: name="0", lineno=Integer.MIN_VALUE, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("0", Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_011() throws Exception {
        // Combination: name="a", lineno=0, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("a", 0, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_012() throws Exception {
        // Combination: name="test123", lineno=1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("test123", 1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_013() throws Exception {
        // Combination: name="", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_014() throws Exception {
        // Combination: name=" ", lineno=Integer.MAX_VALUE, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode(" ", Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME  ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_015() throws Exception {
        // Combination: name="-1", lineno=Integer.MIN_VALUE, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("-1", Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME -1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_016() throws Exception {
        // Combination: name="test123", lineno=0, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("test123", 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME test123 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_017() throws Exception {
        // Combination: name="a", lineno=1, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("a", 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME a 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_018() throws Exception {
        // Combination: name=" ", lineno=-1, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode(" ", -1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME  ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_019() throws Exception {
        // Combination: name="", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_020() throws Exception {
        // Combination: name="1.5", lineno=Integer.MIN_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("1.5", Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node", actual.getClass().getName());
        assertEquals("GETPROP", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_021() throws Exception {
        // Combination: name="!@#", lineno=0, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("!@#", 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME !@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_022() throws Exception {
        // Combination: name="0", lineno=1, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("0", 1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_023() throws Exception {
        // Combination: name="-1", lineno=-1, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("-1", -1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME -1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_024() throws Exception {
        // Combination: name="1.5", lineno=Integer.MAX_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("1.5", Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node", actual.getClass().getName());
        assertEquals("GETPROP", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_025() throws Exception {
        // Combination: name="", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_026() throws Exception {
        // Combination: name=" ", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode(" ", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME  ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_027() throws Exception {
        // Combination: name="a", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("a", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_028() throws Exception {
        // Combination: name="test123", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("test123", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_029() throws Exception {
        // Combination: name="!@#", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("!@#", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME !@# 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_030() throws Exception {
        // Combination: name="!@#", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("!@#", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME !@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_031() throws Exception {
        // Combination: name="!@#", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("!@#", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME !@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_032() throws Exception {
        // Combination: name="0", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("0", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 0 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_033() throws Exception {
        // Combination: name="0", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("0", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_034() throws Exception {
        // Combination: name="0", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("0", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_035() throws Exception {
        // Combination: name="-1", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("-1", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME -1 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_036() throws Exception {
        // Combination: name="-1", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("-1", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME -1 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_037() throws Exception {
        // Combination: name="-1", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("-1", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME -1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_038() throws Exception {
        // Combination: name="1.5", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("1.5", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node", actual.getClass().getName());
        assertEquals("GETPROP 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_039() throws Exception {
        // Combination: name="1.5", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("1.5", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node", actual.getClass().getName());
        assertEquals("GETPROP 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_040() throws Exception {
        // Combination: name="1.5", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("1.5", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node", actual.getClass().getName());
        assertEquals("GETPROP", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_041() throws Exception {
        // Combination: name="9223372036854775807", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775807", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775807 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_042() throws Exception {
        // Combination: name="9223372036854775807", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775807", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775807 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_043() throws Exception {
        // Combination: name="9223372036854775807", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775807", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_044() throws Exception {
        // Combination: name="9223372036854775807", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_045() throws Exception {
        // Combination: name="9223372036854775807", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_046() throws Exception {
        // Combination: name="9223372036854775808", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775808", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775808 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_047() throws Exception {
        // Combination: name="9223372036854775808", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775808", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775808 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_048() throws Exception {
        // Combination: name="9223372036854775808", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775808", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_049() throws Exception {
        // Combination: name="9223372036854775808", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_050() throws Exception {
        // Combination: name="9223372036854775808", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME 9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_051() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=0, charno=0
        Object actual = NodeUtil.newQualifiedNameNode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa 0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_052() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=1, charno=1
        Object actual = NodeUtil.newQualifiedNameNode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa 1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_053() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=-1, charno=-1
        Object actual = NodeUtil.newQualifiedNameNode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_054() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=Integer.MAX_VALUE, charno=Integer.MAX_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_newQualifiedNameNode_pairwise_055() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=Integer.MIN_VALUE, charno=Integer.MIN_VALUE
        Object actual = NodeUtil.newQualifiedNameNode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("com.google.javascript.rhino.Node$StringNode", actual.getClass().getName());
        assertEquals("NAME aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

}
