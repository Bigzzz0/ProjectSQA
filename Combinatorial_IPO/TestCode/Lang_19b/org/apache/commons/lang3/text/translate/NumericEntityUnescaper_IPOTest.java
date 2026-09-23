package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumericEntityUnescaper.
 */
public class NumericEntityUnescaper_IPOTest {
    @Test(timeout = 4000)
    public void test_translate_pairwise_001() throws Exception {
        // Combination: input="", index=0, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("", 0, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_002() throws Exception {
        // Combination: input="a", index=0, out=new java.io.StringWriter(16)
        Object actual = (new NumericEntityUnescaper()).translate("a", 0, new java.io.StringWriter(16));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_003() throws Exception {
        // Combination: input="test", index=0, out=new java.io.StringWriter()
        Object actual = (new NumericEntityUnescaper()).translate("test", 0, new java.io.StringWriter());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_004() throws Exception {
        // Combination: input="", index=1, out=new java.io.StringWriter(16)
        try {
            (new NumericEntityUnescaper()).translate("", 1, new java.io.StringWriter(16));
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_005() throws Exception {
        // Combination: input="a", index=1, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("a", 1, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_006() throws Exception {
        // Combination: input="test", index=1, out=new java.io.StringWriter(16)
        Object actual = (new NumericEntityUnescaper()).translate("test", 1, new java.io.StringWriter(16));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_007() throws Exception {
        // Combination: input="", index=-1, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("", -1, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_008() throws Exception {
        // Combination: input="a", index=-1, out=new java.io.StringWriter(16)
        try {
            (new NumericEntityUnescaper()).translate("a", -1, new java.io.StringWriter(16));
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_009() throws Exception {
        // Combination: input="test", index=-1, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("test", -1, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_010() throws Exception {
        // Combination: input="", index=Integer.MAX_VALUE, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("", Integer.MAX_VALUE, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_011() throws Exception {
        // Combination: input="a", index=Integer.MAX_VALUE, out=new java.io.StringWriter(16)
        try {
            (new NumericEntityUnescaper()).translate("a", Integer.MAX_VALUE, new java.io.StringWriter(16));
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_012() throws Exception {
        // Combination: input="test", index=Integer.MAX_VALUE, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("test", Integer.MAX_VALUE, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_013() throws Exception {
        // Combination: input="", index=Integer.MIN_VALUE, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("", Integer.MIN_VALUE, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_014() throws Exception {
        // Combination: input="a", index=Integer.MIN_VALUE, out=new java.io.StringWriter(16)
        try {
            (new NumericEntityUnescaper()).translate("a", Integer.MIN_VALUE, new java.io.StringWriter(16));
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_translate_pairwise_015() throws Exception {
        // Combination: input="test", index=Integer.MIN_VALUE, out=new java.io.StringWriter()
        try {
            (new NumericEntityUnescaper()).translate("test", Integer.MIN_VALUE, new java.io.StringWriter());
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
