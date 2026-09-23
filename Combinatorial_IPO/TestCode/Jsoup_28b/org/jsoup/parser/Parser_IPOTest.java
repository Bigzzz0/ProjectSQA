package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Parser.
 */
public class Parser_IPOTest {
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
    public void test_parse_pairwise_001() throws Exception {
        // Combination: html="", baseUri=""
        Object actual = Parser.parse("", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_002() throws Exception {
        // Combination: html=" ", baseUri=""
        Object actual = Parser.parse(" ", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_003() throws Exception {
        // Combination: html="a", baseUri=""
        Object actual = Parser.parse("a", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_004() throws Exception {
        // Combination: html="test123", baseUri=""
        Object actual = Parser.parse("test123", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_005() throws Exception {
        // Combination: html="!@#", baseUri=""
        Object actual = Parser.parse("!@#", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_006() throws Exception {
        // Combination: html="0", baseUri=""
        Object actual = Parser.parse("0", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_007() throws Exception {
        // Combination: html="-1", baseUri=""
        Object actual = Parser.parse("-1", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_008() throws Exception {
        // Combination: html="1.5", baseUri=""
        Object actual = Parser.parse("1.5", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_009() throws Exception {
        // Combination: html="9223372036854775807", baseUri=""
        Object actual = Parser.parse("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_010() throws Exception {
        // Combination: html="9223372036854775808", baseUri=""
        Object actual = Parser.parse("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_011() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_012() throws Exception {
        // Combination: html="", baseUri=" "
        Object actual = Parser.parse("", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_013() throws Exception {
        // Combination: html=" ", baseUri=" "
        Object actual = Parser.parse(" ", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_014() throws Exception {
        // Combination: html="a", baseUri=" "
        Object actual = Parser.parse("a", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_015() throws Exception {
        // Combination: html="test123", baseUri=" "
        Object actual = Parser.parse("test123", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_016() throws Exception {
        // Combination: html="!@#", baseUri=" "
        Object actual = Parser.parse("!@#", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_017() throws Exception {
        // Combination: html="0", baseUri=" "
        Object actual = Parser.parse("0", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_018() throws Exception {
        // Combination: html="-1", baseUri=" "
        Object actual = Parser.parse("-1", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_019() throws Exception {
        // Combination: html="1.5", baseUri=" "
        Object actual = Parser.parse("1.5", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_020() throws Exception {
        // Combination: html="9223372036854775807", baseUri=" "
        Object actual = Parser.parse("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_021() throws Exception {
        // Combination: html="9223372036854775808", baseUri=" "
        Object actual = Parser.parse("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_022() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_023() throws Exception {
        // Combination: html="", baseUri="a"
        Object actual = Parser.parse("", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_024() throws Exception {
        // Combination: html=" ", baseUri="a"
        Object actual = Parser.parse(" ", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_025() throws Exception {
        // Combination: html="a", baseUri="a"
        Object actual = Parser.parse("a", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_026() throws Exception {
        // Combination: html="test123", baseUri="a"
        Object actual = Parser.parse("test123", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_027() throws Exception {
        // Combination: html="!@#", baseUri="a"
        Object actual = Parser.parse("!@#", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_028() throws Exception {
        // Combination: html="0", baseUri="a"
        Object actual = Parser.parse("0", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_029() throws Exception {
        // Combination: html="-1", baseUri="a"
        Object actual = Parser.parse("-1", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_030() throws Exception {
        // Combination: html="1.5", baseUri="a"
        Object actual = Parser.parse("1.5", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_031() throws Exception {
        // Combination: html="9223372036854775807", baseUri="a"
        Object actual = Parser.parse("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_032() throws Exception {
        // Combination: html="9223372036854775808", baseUri="a"
        Object actual = Parser.parse("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_033() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_034() throws Exception {
        // Combination: html="", baseUri="test123"
        Object actual = Parser.parse("", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_035() throws Exception {
        // Combination: html=" ", baseUri="test123"
        Object actual = Parser.parse(" ", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_036() throws Exception {
        // Combination: html="a", baseUri="test123"
        Object actual = Parser.parse("a", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_037() throws Exception {
        // Combination: html="test123", baseUri="test123"
        Object actual = Parser.parse("test123", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_038() throws Exception {
        // Combination: html="!@#", baseUri="test123"
        Object actual = Parser.parse("!@#", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_039() throws Exception {
        // Combination: html="0", baseUri="test123"
        Object actual = Parser.parse("0", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_040() throws Exception {
        // Combination: html="-1", baseUri="test123"
        Object actual = Parser.parse("-1", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_041() throws Exception {
        // Combination: html="1.5", baseUri="test123"
        Object actual = Parser.parse("1.5", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_042() throws Exception {
        // Combination: html="9223372036854775807", baseUri="test123"
        Object actual = Parser.parse("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_043() throws Exception {
        // Combination: html="9223372036854775808", baseUri="test123"
        Object actual = Parser.parse("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_044() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_045() throws Exception {
        // Combination: html="", baseUri="!@#"
        Object actual = Parser.parse("", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_046() throws Exception {
        // Combination: html=" ", baseUri="!@#"
        Object actual = Parser.parse(" ", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_047() throws Exception {
        // Combination: html="a", baseUri="!@#"
        Object actual = Parser.parse("a", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_048() throws Exception {
        // Combination: html="test123", baseUri="!@#"
        Object actual = Parser.parse("test123", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_049() throws Exception {
        // Combination: html="!@#", baseUri="!@#"
        Object actual = Parser.parse("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_050() throws Exception {
        // Combination: html="0", baseUri="!@#"
        Object actual = Parser.parse("0", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_051() throws Exception {
        // Combination: html="-1", baseUri="!@#"
        Object actual = Parser.parse("-1", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_052() throws Exception {
        // Combination: html="1.5", baseUri="!@#"
        Object actual = Parser.parse("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_053() throws Exception {
        // Combination: html="9223372036854775807", baseUri="!@#"
        Object actual = Parser.parse("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_054() throws Exception {
        // Combination: html="9223372036854775808", baseUri="!@#"
        Object actual = Parser.parse("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_055() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_056() throws Exception {
        // Combination: html="", baseUri="0"
        Object actual = Parser.parse("", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_057() throws Exception {
        // Combination: html=" ", baseUri="0"
        Object actual = Parser.parse(" ", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_058() throws Exception {
        // Combination: html="a", baseUri="0"
        Object actual = Parser.parse("a", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_059() throws Exception {
        // Combination: html="test123", baseUri="0"
        Object actual = Parser.parse("test123", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_060() throws Exception {
        // Combination: html="!@#", baseUri="0"
        Object actual = Parser.parse("!@#", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_061() throws Exception {
        // Combination: html="0", baseUri="0"
        Object actual = Parser.parse("0", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_062() throws Exception {
        // Combination: html="-1", baseUri="0"
        Object actual = Parser.parse("-1", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_063() throws Exception {
        // Combination: html="1.5", baseUri="0"
        Object actual = Parser.parse("1.5", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_064() throws Exception {
        // Combination: html="9223372036854775807", baseUri="0"
        Object actual = Parser.parse("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_065() throws Exception {
        // Combination: html="9223372036854775808", baseUri="0"
        Object actual = Parser.parse("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_066() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_067() throws Exception {
        // Combination: html="", baseUri="-1"
        Object actual = Parser.parse("", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_068() throws Exception {
        // Combination: html=" ", baseUri="-1"
        Object actual = Parser.parse(" ", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_069() throws Exception {
        // Combination: html="a", baseUri="-1"
        Object actual = Parser.parse("a", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_070() throws Exception {
        // Combination: html="test123", baseUri="-1"
        Object actual = Parser.parse("test123", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_071() throws Exception {
        // Combination: html="!@#", baseUri="-1"
        Object actual = Parser.parse("!@#", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_072() throws Exception {
        // Combination: html="0", baseUri="-1"
        Object actual = Parser.parse("0", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_073() throws Exception {
        // Combination: html="-1", baseUri="-1"
        Object actual = Parser.parse("-1", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_074() throws Exception {
        // Combination: html="1.5", baseUri="-1"
        Object actual = Parser.parse("1.5", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_075() throws Exception {
        // Combination: html="9223372036854775807", baseUri="-1"
        Object actual = Parser.parse("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_076() throws Exception {
        // Combination: html="9223372036854775808", baseUri="-1"
        Object actual = Parser.parse("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_077() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_078() throws Exception {
        // Combination: html="", baseUri="1.5"
        Object actual = Parser.parse("", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_079() throws Exception {
        // Combination: html=" ", baseUri="1.5"
        Object actual = Parser.parse(" ", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_080() throws Exception {
        // Combination: html="a", baseUri="1.5"
        Object actual = Parser.parse("a", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_081() throws Exception {
        // Combination: html="test123", baseUri="1.5"
        Object actual = Parser.parse("test123", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_082() throws Exception {
        // Combination: html="!@#", baseUri="1.5"
        Object actual = Parser.parse("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_083() throws Exception {
        // Combination: html="0", baseUri="1.5"
        Object actual = Parser.parse("0", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_084() throws Exception {
        // Combination: html="-1", baseUri="1.5"
        Object actual = Parser.parse("-1", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_085() throws Exception {
        // Combination: html="1.5", baseUri="1.5"
        Object actual = Parser.parse("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_086() throws Exception {
        // Combination: html="9223372036854775807", baseUri="1.5"
        Object actual = Parser.parse("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_087() throws Exception {
        // Combination: html="9223372036854775808", baseUri="1.5"
        Object actual = Parser.parse("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_088() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_089() throws Exception {
        // Combination: html="", baseUri="9223372036854775807"
        Object actual = Parser.parse("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_090() throws Exception {
        // Combination: html=" ", baseUri="9223372036854775807"
        Object actual = Parser.parse(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_091() throws Exception {
        // Combination: html="a", baseUri="9223372036854775807"
        Object actual = Parser.parse("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_092() throws Exception {
        // Combination: html="test123", baseUri="9223372036854775807"
        Object actual = Parser.parse("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_093() throws Exception {
        // Combination: html="!@#", baseUri="9223372036854775807"
        Object actual = Parser.parse("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_094() throws Exception {
        // Combination: html="0", baseUri="9223372036854775807"
        Object actual = Parser.parse("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_095() throws Exception {
        // Combination: html="-1", baseUri="9223372036854775807"
        Object actual = Parser.parse("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_096() throws Exception {
        // Combination: html="1.5", baseUri="9223372036854775807"
        Object actual = Parser.parse("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_097() throws Exception {
        // Combination: html="9223372036854775807", baseUri="9223372036854775807"
        Object actual = Parser.parse("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_098() throws Exception {
        // Combination: html="9223372036854775808", baseUri="9223372036854775807"
        Object actual = Parser.parse("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_099() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_100() throws Exception {
        // Combination: html="", baseUri="9223372036854775808"
        Object actual = Parser.parse("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_101() throws Exception {
        // Combination: html=" ", baseUri="9223372036854775808"
        Object actual = Parser.parse(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_102() throws Exception {
        // Combination: html="a", baseUri="9223372036854775808"
        Object actual = Parser.parse("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_103() throws Exception {
        // Combination: html="test123", baseUri="9223372036854775808"
        Object actual = Parser.parse("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_104() throws Exception {
        // Combination: html="!@#", baseUri="9223372036854775808"
        Object actual = Parser.parse("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_105() throws Exception {
        // Combination: html="0", baseUri="9223372036854775808"
        Object actual = Parser.parse("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_106() throws Exception {
        // Combination: html="-1", baseUri="9223372036854775808"
        Object actual = Parser.parse("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_107() throws Exception {
        // Combination: html="1.5", baseUri="9223372036854775808"
        Object actual = Parser.parse("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_108() throws Exception {
        // Combination: html="9223372036854775807", baseUri="9223372036854775808"
        Object actual = Parser.parse("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_109() throws Exception {
        // Combination: html="9223372036854775808", baseUri="9223372036854775808"
        Object actual = Parser.parse("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_110() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_111() throws Exception {
        // Combination: html="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_112() throws Exception {
        // Combination: html=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_113() throws Exception {
        // Combination: html="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_114() throws Exception {
        // Combination: html="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_115() throws Exception {
        // Combination: html="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_116() throws Exception {
        // Combination: html="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_117() throws Exception {
        // Combination: html="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_118() throws Exception {
        // Combination: html="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_119() throws Exception {
        // Combination: html="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_120() throws Exception {
        // Combination: html="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_121() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_122() throws Exception {
        // Combination: bodyHtml="", baseUri=""
        Object actual = Parser.parseBodyFragment("", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_123() throws Exception {
        // Combination: bodyHtml=" ", baseUri=""
        Object actual = Parser.parseBodyFragment(" ", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_124() throws Exception {
        // Combination: bodyHtml="a", baseUri=""
        Object actual = Parser.parseBodyFragment("a", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_125() throws Exception {
        // Combination: bodyHtml="test123", baseUri=""
        Object actual = Parser.parseBodyFragment("test123", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_126() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=""
        Object actual = Parser.parseBodyFragment("!@#", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_127() throws Exception {
        // Combination: bodyHtml="0", baseUri=""
        Object actual = Parser.parseBodyFragment("0", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_128() throws Exception {
        // Combination: bodyHtml="-1", baseUri=""
        Object actual = Parser.parseBodyFragment("-1", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_129() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=""
        Object actual = Parser.parseBodyFragment("1.5", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_130() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=""
        Object actual = Parser.parseBodyFragment("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_131() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=""
        Object actual = Parser.parseBodyFragment("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_132() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_133() throws Exception {
        // Combination: bodyHtml="", baseUri=" "
        Object actual = Parser.parseBodyFragment("", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_134() throws Exception {
        // Combination: bodyHtml=" ", baseUri=" "
        Object actual = Parser.parseBodyFragment(" ", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_135() throws Exception {
        // Combination: bodyHtml="a", baseUri=" "
        Object actual = Parser.parseBodyFragment("a", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_136() throws Exception {
        // Combination: bodyHtml="test123", baseUri=" "
        Object actual = Parser.parseBodyFragment("test123", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_137() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=" "
        Object actual = Parser.parseBodyFragment("!@#", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_138() throws Exception {
        // Combination: bodyHtml="0", baseUri=" "
        Object actual = Parser.parseBodyFragment("0", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_139() throws Exception {
        // Combination: bodyHtml="-1", baseUri=" "
        Object actual = Parser.parseBodyFragment("-1", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_140() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=" "
        Object actual = Parser.parseBodyFragment("1.5", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_141() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=" "
        Object actual = Parser.parseBodyFragment("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_142() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=" "
        Object actual = Parser.parseBodyFragment("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_143() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_144() throws Exception {
        // Combination: bodyHtml="", baseUri="a"
        Object actual = Parser.parseBodyFragment("", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_145() throws Exception {
        // Combination: bodyHtml=" ", baseUri="a"
        Object actual = Parser.parseBodyFragment(" ", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_146() throws Exception {
        // Combination: bodyHtml="a", baseUri="a"
        Object actual = Parser.parseBodyFragment("a", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_147() throws Exception {
        // Combination: bodyHtml="test123", baseUri="a"
        Object actual = Parser.parseBodyFragment("test123", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_148() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="a"
        Object actual = Parser.parseBodyFragment("!@#", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_149() throws Exception {
        // Combination: bodyHtml="0", baseUri="a"
        Object actual = Parser.parseBodyFragment("0", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_150() throws Exception {
        // Combination: bodyHtml="-1", baseUri="a"
        Object actual = Parser.parseBodyFragment("-1", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_151() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="a"
        Object actual = Parser.parseBodyFragment("1.5", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_152() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="a"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_153() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="a"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_154() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_155() throws Exception {
        // Combination: bodyHtml="", baseUri="test123"
        Object actual = Parser.parseBodyFragment("", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_156() throws Exception {
        // Combination: bodyHtml=" ", baseUri="test123"
        Object actual = Parser.parseBodyFragment(" ", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_157() throws Exception {
        // Combination: bodyHtml="a", baseUri="test123"
        Object actual = Parser.parseBodyFragment("a", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_158() throws Exception {
        // Combination: bodyHtml="test123", baseUri="test123"
        Object actual = Parser.parseBodyFragment("test123", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_159() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="test123"
        Object actual = Parser.parseBodyFragment("!@#", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_160() throws Exception {
        // Combination: bodyHtml="0", baseUri="test123"
        Object actual = Parser.parseBodyFragment("0", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_161() throws Exception {
        // Combination: bodyHtml="-1", baseUri="test123"
        Object actual = Parser.parseBodyFragment("-1", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_162() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="test123"
        Object actual = Parser.parseBodyFragment("1.5", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_163() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="test123"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_164() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="test123"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_165() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_166() throws Exception {
        // Combination: bodyHtml="", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_167() throws Exception {
        // Combination: bodyHtml=" ", baseUri="!@#"
        Object actual = Parser.parseBodyFragment(" ", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_168() throws Exception {
        // Combination: bodyHtml="a", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("a", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_169() throws Exception {
        // Combination: bodyHtml="test123", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("test123", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_170() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_171() throws Exception {
        // Combination: bodyHtml="0", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("0", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_172() throws Exception {
        // Combination: bodyHtml="-1", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("-1", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_173() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_174() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_175() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_176() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_177() throws Exception {
        // Combination: bodyHtml="", baseUri="0"
        Object actual = Parser.parseBodyFragment("", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_178() throws Exception {
        // Combination: bodyHtml=" ", baseUri="0"
        Object actual = Parser.parseBodyFragment(" ", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_179() throws Exception {
        // Combination: bodyHtml="a", baseUri="0"
        Object actual = Parser.parseBodyFragment("a", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_180() throws Exception {
        // Combination: bodyHtml="test123", baseUri="0"
        Object actual = Parser.parseBodyFragment("test123", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_181() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="0"
        Object actual = Parser.parseBodyFragment("!@#", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_182() throws Exception {
        // Combination: bodyHtml="0", baseUri="0"
        Object actual = Parser.parseBodyFragment("0", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_183() throws Exception {
        // Combination: bodyHtml="-1", baseUri="0"
        Object actual = Parser.parseBodyFragment("-1", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_184() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="0"
        Object actual = Parser.parseBodyFragment("1.5", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_185() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="0"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_186() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="0"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_187() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_188() throws Exception {
        // Combination: bodyHtml="", baseUri="-1"
        Object actual = Parser.parseBodyFragment("", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_189() throws Exception {
        // Combination: bodyHtml=" ", baseUri="-1"
        Object actual = Parser.parseBodyFragment(" ", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_190() throws Exception {
        // Combination: bodyHtml="a", baseUri="-1"
        Object actual = Parser.parseBodyFragment("a", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_191() throws Exception {
        // Combination: bodyHtml="test123", baseUri="-1"
        Object actual = Parser.parseBodyFragment("test123", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_192() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="-1"
        Object actual = Parser.parseBodyFragment("!@#", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_193() throws Exception {
        // Combination: bodyHtml="0", baseUri="-1"
        Object actual = Parser.parseBodyFragment("0", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_194() throws Exception {
        // Combination: bodyHtml="-1", baseUri="-1"
        Object actual = Parser.parseBodyFragment("-1", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_195() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="-1"
        Object actual = Parser.parseBodyFragment("1.5", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_196() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="-1"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_197() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="-1"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_198() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_199() throws Exception {
        // Combination: bodyHtml="", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_200() throws Exception {
        // Combination: bodyHtml=" ", baseUri="1.5"
        Object actual = Parser.parseBodyFragment(" ", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_201() throws Exception {
        // Combination: bodyHtml="a", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("a", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_202() throws Exception {
        // Combination: bodyHtml="test123", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("test123", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_203() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_204() throws Exception {
        // Combination: bodyHtml="0", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("0", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_205() throws Exception {
        // Combination: bodyHtml="-1", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("-1", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_206() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_207() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_208() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_209() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_210() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_211() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_212() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_213() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_214() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_215() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_216() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_217() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_218() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_219() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_220() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_221() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_222() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_223() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_224() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_225() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_226() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_227() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_228() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_229() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_230() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_231() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_232() throws Exception {
        // Combination: bodyHtml="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_233() throws Exception {
        // Combination: bodyHtml=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_234() throws Exception {
        // Combination: bodyHtml="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_235() throws Exception {
        // Combination: bodyHtml="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_236() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_237() throws Exception {
        // Combination: bodyHtml="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_238() throws Exception {
        // Combination: bodyHtml="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_239() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_240() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_241() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_242() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_243() throws Exception {
        // Combination: string="", inAttribute=true
        Object actual = Parser.unescapeEntities("", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_244() throws Exception {
        // Combination: string=" ", inAttribute=true
        Object actual = Parser.unescapeEntities(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_245() throws Exception {
        // Combination: string="a", inAttribute=true
        Object actual = Parser.unescapeEntities("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_246() throws Exception {
        // Combination: string="test123", inAttribute=true
        Object actual = Parser.unescapeEntities("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_247() throws Exception {
        // Combination: string="!@#", inAttribute=true
        Object actual = Parser.unescapeEntities("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_248() throws Exception {
        // Combination: string="0", inAttribute=true
        Object actual = Parser.unescapeEntities("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_249() throws Exception {
        // Combination: string="-1", inAttribute=true
        Object actual = Parser.unescapeEntities("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_250() throws Exception {
        // Combination: string="1.5", inAttribute=true
        Object actual = Parser.unescapeEntities("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_251() throws Exception {
        // Combination: string="9223372036854775807", inAttribute=true
        Object actual = Parser.unescapeEntities("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_252() throws Exception {
        // Combination: string="9223372036854775808", inAttribute=true
        Object actual = Parser.unescapeEntities("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_253() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", inAttribute=true
        Object actual = Parser.unescapeEntities("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_254() throws Exception {
        // Combination: string="", inAttribute=false
        Object actual = Parser.unescapeEntities("", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_255() throws Exception {
        // Combination: string=" ", inAttribute=false
        Object actual = Parser.unescapeEntities(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_256() throws Exception {
        // Combination: string="a", inAttribute=false
        Object actual = Parser.unescapeEntities("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_257() throws Exception {
        // Combination: string="test123", inAttribute=false
        Object actual = Parser.unescapeEntities("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_258() throws Exception {
        // Combination: string="!@#", inAttribute=false
        Object actual = Parser.unescapeEntities("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_259() throws Exception {
        // Combination: string="0", inAttribute=false
        Object actual = Parser.unescapeEntities("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_260() throws Exception {
        // Combination: string="-1", inAttribute=false
        Object actual = Parser.unescapeEntities("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_261() throws Exception {
        // Combination: string="1.5", inAttribute=false
        Object actual = Parser.unescapeEntities("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_262() throws Exception {
        // Combination: string="9223372036854775807", inAttribute=false
        Object actual = Parser.unescapeEntities("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_263() throws Exception {
        // Combination: string="9223372036854775808", inAttribute=false
        Object actual = Parser.unescapeEntities("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_unescapeEntities_pairwise_264() throws Exception {
        // Combination: string="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", inAttribute=false
        Object actual = Parser.unescapeEntities("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_265() throws Exception {
        // Combination: bodyHtml="", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_266() throws Exception {
        // Combination: bodyHtml=" ", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_267() throws Exception {
        // Combination: bodyHtml="a", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("a", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_268() throws Exception {
        // Combination: bodyHtml="test123", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_269() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_270() throws Exception {
        // Combination: bodyHtml="0", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("0", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_271() throws Exception {
        // Combination: bodyHtml="-1", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_272() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_273() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_274() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_275() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_276() throws Exception {
        // Combination: bodyHtml="", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_277() throws Exception {
        // Combination: bodyHtml=" ", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed(" ", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_278() throws Exception {
        // Combination: bodyHtml="a", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("a", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_279() throws Exception {
        // Combination: bodyHtml="test123", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("test123", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_280() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_281() throws Exception {
        // Combination: bodyHtml="0", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("0", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_282() throws Exception {
        // Combination: bodyHtml="-1", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("-1", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_283() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_284() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_285() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_286() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_287() throws Exception {
        // Combination: bodyHtml="", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_288() throws Exception {
        // Combination: bodyHtml=" ", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_289() throws Exception {
        // Combination: bodyHtml="a", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_290() throws Exception {
        // Combination: bodyHtml="test123", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_291() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_292() throws Exception {
        // Combination: bodyHtml="0", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_293() throws Exception {
        // Combination: bodyHtml="-1", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_294() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_295() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_296() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_297() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_298() throws Exception {
        // Combination: bodyHtml="", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_299() throws Exception {
        // Combination: bodyHtml=" ", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_300() throws Exception {
        // Combination: bodyHtml="a", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_301() throws Exception {
        // Combination: bodyHtml="test123", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_302() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_303() throws Exception {
        // Combination: bodyHtml="0", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_304() throws Exception {
        // Combination: bodyHtml="-1", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_305() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_306() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_307() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_308() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_309() throws Exception {
        // Combination: bodyHtml="", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_310() throws Exception {
        // Combination: bodyHtml=" ", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_311() throws Exception {
        // Combination: bodyHtml="a", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_312() throws Exception {
        // Combination: bodyHtml="test123", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_313() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_314() throws Exception {
        // Combination: bodyHtml="0", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_315() throws Exception {
        // Combination: bodyHtml="-1", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_316() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_317() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_318() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_319() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_320() throws Exception {
        // Combination: bodyHtml="", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_321() throws Exception {
        // Combination: bodyHtml=" ", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_322() throws Exception {
        // Combination: bodyHtml="a", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_323() throws Exception {
        // Combination: bodyHtml="test123", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_324() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_325() throws Exception {
        // Combination: bodyHtml="0", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_326() throws Exception {
        // Combination: bodyHtml="-1", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_327() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_328() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_329() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_330() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_331() throws Exception {
        // Combination: bodyHtml="", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_332() throws Exception {
        // Combination: bodyHtml=" ", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_333() throws Exception {
        // Combination: bodyHtml="a", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_334() throws Exception {
        // Combination: bodyHtml="test123", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_335() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_336() throws Exception {
        // Combination: bodyHtml="0", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_337() throws Exception {
        // Combination: bodyHtml="-1", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_338() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_339() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_340() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_341() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_342() throws Exception {
        // Combination: bodyHtml="", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_343() throws Exception {
        // Combination: bodyHtml=" ", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_344() throws Exception {
        // Combination: bodyHtml="a", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_345() throws Exception {
        // Combination: bodyHtml="test123", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_346() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_347() throws Exception {
        // Combination: bodyHtml="0", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_348() throws Exception {
        // Combination: bodyHtml="-1", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_349() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_350() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_351() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_352() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_353() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_354() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_355() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_356() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_357() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_358() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_359() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_360() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_361() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_362() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_363() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_364() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_365() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_366() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_367() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_368() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_369() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_370() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_371() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_372() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_373() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_374() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_375() throws Exception {
        // Combination: bodyHtml="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_376() throws Exception {
        // Combination: bodyHtml=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_377() throws Exception {
        // Combination: bodyHtml="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_378() throws Exception {
        // Combination: bodyHtml="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_379() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_380() throws Exception {
        // Combination: bodyHtml="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_381() throws Exception {
        // Combination: bodyHtml="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_382() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_383() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_384() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragmentRelaxed_pairwise_385() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Parser.parseBodyFragmentRelaxed("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

}
