package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CollectionUtils.
 */
public class CollectionUtils_IPOTest {
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
    public void test_union_pairwise_001() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.union(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_union_pairwise_002() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.union(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_union_pairwise_003() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.union(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_union_pairwise_004() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.union(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersection_pairwise_005() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.intersection(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersection_pairwise_006() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.intersection(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersection_pairwise_007() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.intersection(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_intersection_pairwise_008() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.intersection(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_disjunction_pairwise_009() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.disjunction(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_disjunction_pairwise_010() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.disjunction(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_disjunction_pairwise_011() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.disjunction(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_disjunction_pairwise_012() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.disjunction(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_013() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.subtract(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_014() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.subtract(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_015() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.subtract(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_016() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.subtract(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAny_pairwise_017() throws Exception {
        // Combination: coll1=java.util.Collections.emptyList(), coll2=java.util.Collections.emptyList()
        Object actual = CollectionUtils.containsAny(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAny_pairwise_018() throws Exception {
        // Combination: coll1=java.util.Collections.emptyList(), coll2=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.containsAny(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAny_pairwise_019() throws Exception {
        // Combination: coll1=java.util.Arrays.asList("a", "b"), coll2=java.util.Collections.emptyList()
        Object actual = CollectionUtils.containsAny(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAny_pairwise_020() throws Exception {
        // Combination: coll1=java.util.Arrays.asList("a", "b"), coll2=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.containsAny(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSubCollection_pairwise_021() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isSubCollection(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSubCollection_pairwise_022() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isSubCollection(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSubCollection_pairwise_023() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isSubCollection(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSubCollection_pairwise_024() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isSubCollection(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isProperSubCollection_pairwise_025() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isProperSubCollection(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isProperSubCollection_pairwise_026() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isProperSubCollection(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isProperSubCollection_pairwise_027() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isProperSubCollection(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isProperSubCollection_pairwise_028() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isProperSubCollection(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualCollection_pairwise_029() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isEqualCollection(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualCollection_pairwise_030() throws Exception {
        // Combination: a=java.util.Collections.emptyList(), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isEqualCollection(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualCollection_pairwise_031() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Collections.emptyList()
        Object actual = CollectionUtils.isEqualCollection(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isEqualCollection_pairwise_032() throws Exception {
        // Combination: a=java.util.Arrays.asList("a", "b"), b=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.isEqualCollection(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_033() throws Exception {
        // Combination: obj=new Object(), coll=java.util.Collections.emptyList()
        Object actual = CollectionUtils.cardinality(new Object(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_034() throws Exception {
        // Combination: obj="sample_str", coll=java.util.Collections.emptyList()
        Object actual = CollectionUtils.cardinality("sample_str", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_035() throws Exception {
        // Combination: obj=Integer.valueOf(1), coll=java.util.Collections.emptyList()
        Object actual = CollectionUtils.cardinality(Integer.valueOf(1), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_036() throws Exception {
        // Combination: obj=new Object(), coll=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.cardinality(new Object(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_037() throws Exception {
        // Combination: obj="sample_str", coll=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.cardinality("sample_str", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cardinality_pairwise_038() throws Exception {
        // Combination: obj=Integer.valueOf(1), coll=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.cardinality(Integer.valueOf(1), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_039() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), object=new Object()
        try {
            CollectionUtils.addIgnoreNull(java.util.Collections.emptyList(), new Object());
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_040() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), object="sample_str"
        try {
            CollectionUtils.addIgnoreNull(java.util.Collections.emptyList(), "sample_str");
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_041() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), object=Integer.valueOf(1)
        try {
            CollectionUtils.addIgnoreNull(java.util.Collections.emptyList(), Integer.valueOf(1));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_042() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), object=new Object()
        try {
            CollectionUtils.addIgnoreNull(java.util.Arrays.asList("a", "b"), new Object());
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_043() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), object="sample_str"
        try {
            CollectionUtils.addIgnoreNull(java.util.Arrays.asList("a", "b"), "sample_str");
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addIgnoreNull_pairwise_044() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), object=Integer.valueOf(1)
        try {
            CollectionUtils.addIgnoreNull(java.util.Arrays.asList("a", "b"), Integer.valueOf(1));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_045() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), retain=java.util.Collections.emptyList()
        Object actual = CollectionUtils.retainAll(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_046() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), retain=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.retainAll(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_047() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), retain=java.util.Collections.emptyList()
        Object actual = CollectionUtils.retainAll(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_048() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), retain=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.retainAll(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_049() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), remove=java.util.Collections.emptyList()
        Object actual = CollectionUtils.removeAll(java.util.Collections.emptyList(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_050() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), remove=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.removeAll(java.util.Collections.emptyList(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_051() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), remove=java.util.Collections.emptyList()
        Object actual = CollectionUtils.removeAll(java.util.Arrays.asList("a", "b"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_052() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), remove=java.util.Arrays.asList("a", "b")
        Object actual = CollectionUtils.removeAll(java.util.Arrays.asList("a", "b"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.ArrayList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_053() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), type=String.class
        Object actual = CollectionUtils.typedCollection(java.util.Collections.emptyList(), String.class);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.collection.PredicatedCollection", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_054() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), type=Object.class
        Object actual = CollectionUtils.typedCollection(java.util.Collections.emptyList(), Object.class);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.collection.PredicatedCollection", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_055() throws Exception {
        // Combination: collection=java.util.Collections.emptyList(), type=Integer.class
        Object actual = CollectionUtils.typedCollection(java.util.Collections.emptyList(), Integer.class);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.collection.PredicatedCollection", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_056() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), type=String.class
        Object actual = CollectionUtils.typedCollection(java.util.Arrays.asList("a", "b"), String.class);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.collection.PredicatedCollection", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_057() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), type=Object.class
        Object actual = CollectionUtils.typedCollection(java.util.Arrays.asList("a", "b"), Object.class);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.collection.PredicatedCollection", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_typedCollection_pairwise_058() throws Exception {
        // Combination: collection=java.util.Arrays.asList("a", "b"), type=Integer.class
        try {
            CollectionUtils.typedCollection(java.util.Arrays.asList("a", "b"), Integer.class);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
