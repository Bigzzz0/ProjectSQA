package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TypeBindings.
 */
public class TypeBindings_IPOTest {
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
    public void test_isEmpty_pairwise_001() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).isEmpty();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEmpty_pairwise_002() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).isEmpty();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isEmpty_pairwise_003() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).isEmpty();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_004() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).size();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_005() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).size();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_size_pairwise_006() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).size();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_007() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=0
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_008() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=0
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_009() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=0
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_010() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=1
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_011() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=1
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_012() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=1
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_013() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=-1
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_014() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=-1
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_015() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=-1
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_016() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MAX_VALUE
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_017() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MAX_VALUE
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_018() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MAX_VALUE
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_019() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MIN_VALUE
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_020() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MIN_VALUE
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getBoundName_pairwise_021() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), index=Integer.MIN_VALUE
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).getBoundName(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_022() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=""
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_023() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=""
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_024() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=""
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_025() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=" "
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_026() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=" "
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_027() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name=" "
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_028() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="a"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_029() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="a"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_030() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="a"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_031() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="test123"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_032() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="test123"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_033() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="test123"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_034() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="!@#"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_035() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="!@#"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_036() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="!@#"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_037() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="0"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_038() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="0"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_039() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="0"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_040() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="-1"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_041() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="-1"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_042() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="-1"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_043() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="1.5"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_044() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="1.5"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_045() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="1.5"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_046() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775807"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_047() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775807"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_048() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775807"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_049() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775808"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_050() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775808"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_051() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="9223372036854775808"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_052() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_053() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasUnbound_pairwise_054() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hasUnbound("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_055() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_056() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_057() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_058() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_059() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_060() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_061() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=new Object()
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_062() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=new Object()
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_063() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=new Object()
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_064() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o="sample_str"
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_065() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o="sample_str"
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_066() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o="sample_str"
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_067() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=Integer.valueOf(1)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_068() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=Integer.valueOf(1)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_069() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class), o=Integer.valueOf(1)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_typeParameterArray_pairwise_070() throws Exception {
        // Combination: receiver__erasedType=String.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(String.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).typeParameterArray();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_typeParameterArray_pairwise_071() throws Exception {
        // Combination: receiver__erasedType=Object.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Object.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).typeParameterArray();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_typeParameterArray_pairwise_072() throws Exception {
        // Combination: receiver__erasedType=Integer.class, receiver__typeArg1=com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class)
        try {
            (TypeBindings.create(Integer.class, com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class))).typeParameterArray();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
