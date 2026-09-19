package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ParseSettings.
 */
public class ParseSettings_IPOTest {
    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_001() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=true
        Object actual = (new ParseSettings(true, true)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_002() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=true
        Object actual = (new ParseSettings(false, true)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_003() throws Exception {
        // Combination: receiver__tag=true, receiver__attribute=false
        Object actual = (new ParseSettings(true, false)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_preserveAttributeCase_pairwise_004() throws Exception {
        // Combination: receiver__tag=false, receiver__attribute=false
        Object actual = (new ParseSettings(false, false)).preserveAttributeCase();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
