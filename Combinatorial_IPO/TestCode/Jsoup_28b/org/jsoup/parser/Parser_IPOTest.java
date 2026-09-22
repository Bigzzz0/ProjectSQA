package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Parser.
 */
public class Parser_IPOTest {
    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_001() throws Exception {
        // Combination: string="", inAttribute=true
        Object actual = Parser.unescapeEntities("", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_002() throws Exception {
        // Combination: string=" ", inAttribute=true
        Object actual = Parser.unescapeEntities(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_003() throws Exception {
        // Combination: string="a", inAttribute=true
        Object actual = Parser.unescapeEntities("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_004() throws Exception {
        // Combination: string="test123", inAttribute=true
        Object actual = Parser.unescapeEntities("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_005() throws Exception {
        // Combination: string="!@#", inAttribute=true
        Object actual = Parser.unescapeEntities("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_006() throws Exception {
        // Combination: string="0", inAttribute=true
        Object actual = Parser.unescapeEntities("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_007() throws Exception {
        // Combination: string="-1", inAttribute=true
        Object actual = Parser.unescapeEntities("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_008() throws Exception {
        // Combination: string="1.5", inAttribute=true
        Object actual = Parser.unescapeEntities("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_009() throws Exception {
        // Combination: string="9223372036854775807", inAttribute=true
        Object actual = Parser.unescapeEntities("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_010() throws Exception {
        // Combination: string="9223372036854775808", inAttribute=true
        Object actual = Parser.unescapeEntities("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_011() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", inAttribute=true
        Object actual = Parser.unescapeEntities("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_012() throws Exception {
        // Combination: string="", inAttribute=false
        Object actual = Parser.unescapeEntities("", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_013() throws Exception {
        // Combination: string=" ", inAttribute=false
        Object actual = Parser.unescapeEntities(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_014() throws Exception {
        // Combination: string="a", inAttribute=false
        Object actual = Parser.unescapeEntities("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_015() throws Exception {
        // Combination: string="test123", inAttribute=false
        Object actual = Parser.unescapeEntities("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_016() throws Exception {
        // Combination: string="!@#", inAttribute=false
        Object actual = Parser.unescapeEntities("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_017() throws Exception {
        // Combination: string="0", inAttribute=false
        Object actual = Parser.unescapeEntities("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_018() throws Exception {
        // Combination: string="-1", inAttribute=false
        Object actual = Parser.unescapeEntities("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_019() throws Exception {
        // Combination: string="1.5", inAttribute=false
        Object actual = Parser.unescapeEntities("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_020() throws Exception {
        // Combination: string="9223372036854775807", inAttribute=false
        Object actual = Parser.unescapeEntities("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_021() throws Exception {
        // Combination: string="9223372036854775808", inAttribute=false
        Object actual = Parser.unescapeEntities("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_022() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", inAttribute=false
        Object actual = Parser.unescapeEntities("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

}
