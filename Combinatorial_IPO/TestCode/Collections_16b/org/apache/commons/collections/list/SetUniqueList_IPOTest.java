package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for SetUniqueList.
 */
public class SetUniqueList_IPOTest {
    @Test(timeout = 4000)
    public void test_asSet_pairwise_001() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).asSet();
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.set.UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asSet_pairwise_002() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a")
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).asSet();
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.set.UnmodifiableSet", actual.getClass().getName());
        assertEquals("[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asSet_pairwise_003() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).asSet();
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.set.UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asSet_pairwise_004() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a")
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).asSet();
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.set.UnmodifiableSet", actual.getClass().getName());
        assertEquals("[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_005() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=new Object()
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).add(new Object());
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_006() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=new Object()
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).add(new Object());
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_007() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), object="sample_str"
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).add("sample_str");
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_008() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), object="sample_str"
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).add("sample_str");
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_009() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=Integer.valueOf(1)
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).add(Integer.valueOf(1));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_010() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=Integer.valueOf(1)
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).add(Integer.valueOf(1));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_011() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).addAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_012() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).addAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_013() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).addAll(java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_014() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).addAll(java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_015() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), index=0, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).addAll(0, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_016() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), index=1, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).addAll(1, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_017() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), index=-1, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).addAll(-1, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_018() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), index=Integer.MAX_VALUE, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).addAll(Integer.MAX_VALUE, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_019() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), index=Integer.MIN_VALUE, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).addAll(Integer.MIN_VALUE, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_020() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), index=0, coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).addAll(0, java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_021() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), index=1, coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).addAll(1, java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_022() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), index=-1, coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).addAll(-1, java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_023() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), index=Integer.MAX_VALUE, coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).addAll(Integer.MAX_VALUE, java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_024() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), index=Integer.MIN_VALUE, coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).addAll(Integer.MIN_VALUE, java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAll_pairwise_025() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), index=0, coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).addAll(0, java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_026() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=new Object()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).remove(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_027() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=new Object()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).remove(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_028() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), object="sample_str"
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).remove("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_029() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), object="sample_str"
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).remove("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_030() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=Integer.valueOf(1)
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).remove(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_remove_pairwise_031() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=Integer.valueOf(1)
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).remove(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_032() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).removeAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_033() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).removeAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_034() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).removeAll(java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_removeAll_pairwise_035() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), coll=java.util.Arrays.asList("a", "b")
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).removeAll(java.util.Arrays.asList("a", "b"));
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_036() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).retainAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_037() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Collections.emptyList()
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).retainAll(java.util.Collections.emptyList());
            fail("Expected java.lang.UnsupportedOperationException");
        } catch (java.lang.UnsupportedOperationException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_038() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).retainAll(java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_retainAll_pairwise_039() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), coll=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).retainAll(java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_040() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=new Object()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).contains(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_041() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=new Object()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).contains(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_042() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), object="sample_str"
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).contains("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_043() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), object="sample_str"
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).contains("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_044() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), object=Integer.valueOf(1)
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).contains(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_contains_pairwise_045() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), object=Integer.valueOf(1)
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).contains(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAll_pairwise_046() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).containsAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAll_pairwise_047() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).containsAll(java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAll_pairwise_048() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), coll=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).containsAll(java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsAll_pairwise_049() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), coll=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).containsAll(java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_050() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=0, toIndex=0
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(0, 0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.list.SetUniqueList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_051() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), fromIndex=0, toIndex=1
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).subList(0, 1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.collections.list.SetUniqueList", actual.getClass().getName());
        assertEquals("[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_052() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), fromIndex=1, toIndex=-1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).subList(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_053() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), fromIndex=1, toIndex=Integer.MAX_VALUE
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).subList(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_054() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=-1, toIndex=1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_055() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), fromIndex=-1, toIndex=0
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).subList(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_056() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MAX_VALUE, toIndex=Integer.MIN_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_057() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), fromIndex=Integer.MAX_VALUE, toIndex=-1
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).subList(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_058() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MIN_VALUE, toIndex=-1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_059() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), fromIndex=Integer.MIN_VALUE, toIndex=Integer.MIN_VALUE
        try {
            (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).subList(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_060() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=1, toIndex=0
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_061() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MAX_VALUE, toIndex=0
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_062() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MIN_VALUE, toIndex=0
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_063() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=1, toIndex=1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_064() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MAX_VALUE, toIndex=1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_065() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MIN_VALUE, toIndex=1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_066() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=0, toIndex=-1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_067() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=-1, toIndex=-1
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_068() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), fromIndex=0, toIndex=Integer.MAX_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).subList(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_069() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=-1, toIndex=Integer.MAX_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_070() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MAX_VALUE, toIndex=Integer.MAX_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_071() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=Integer.MIN_VALUE, toIndex=Integer.MAX_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_072() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=0, toIndex=Integer.MIN_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_073() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=1, toIndex=Integer.MIN_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subList_pairwise_074() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), fromIndex=-1, toIndex=Integer.MIN_VALUE
        try {
            (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).subList(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_075() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), set=java.util.Collections.emptySet(), list=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).createSetBasedOnList(java.util.Collections.emptySet(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_076() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.singleton("a"), set=java.util.Collections.singleton("a"), list=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.singleton("a"))).createSetBasedOnList(java.util.Collections.singleton("a"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_077() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.singleton("a"), set=java.util.Collections.emptySet(), list=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.singleton("a"))).createSetBasedOnList(java.util.Collections.emptySet(), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[a, b]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_078() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), set=java.util.Collections.singleton("a"), list=java.util.Arrays.asList("a", "b")
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).createSetBasedOnList(java.util.Collections.singleton("a"), java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[a, b]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_079() throws Exception {
        // Combination: receiver__list=java.util.Arrays.asList("a", "b"), receiver__set=java.util.Collections.emptySet(), set=java.util.Collections.emptySet(), list=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Arrays.asList("a", "b"), java.util.Collections.emptySet())).createSetBasedOnList(java.util.Collections.emptySet(), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createSetBasedOnList_pairwise_080() throws Exception {
        // Combination: receiver__list=java.util.Collections.emptyList(), receiver__set=java.util.Collections.emptySet(), set=java.util.Collections.singleton("a"), list=java.util.Collections.emptyList()
        Object actual = (new SetUniqueList(java.util.Collections.emptyList(), java.util.Collections.emptySet())).createSetBasedOnList(java.util.Collections.singleton("a"), java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.HashSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

}
