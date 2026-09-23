package org.jsoup;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Jsoup.
 */
public class Jsoup_IPOTest {
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
        Object actual = Jsoup.parse("", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_002() throws Exception {
        // Combination: html=" ", baseUri=""
        Object actual = Jsoup.parse(" ", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_003() throws Exception {
        // Combination: html="a", baseUri=""
        Object actual = Jsoup.parse("a", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_004() throws Exception {
        // Combination: html="test123", baseUri=""
        Object actual = Jsoup.parse("test123", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_005() throws Exception {
        // Combination: html="!@#", baseUri=""
        Object actual = Jsoup.parse("!@#", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_006() throws Exception {
        // Combination: html="0", baseUri=""
        Object actual = Jsoup.parse("0", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_007() throws Exception {
        // Combination: html="-1", baseUri=""
        Object actual = Jsoup.parse("-1", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_008() throws Exception {
        // Combination: html="1.5", baseUri=""
        Object actual = Jsoup.parse("1.5", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_009() throws Exception {
        // Combination: html="9223372036854775807", baseUri=""
        Object actual = Jsoup.parse("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_010() throws Exception {
        // Combination: html="9223372036854775808", baseUri=""
        Object actual = Jsoup.parse("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_011() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_012() throws Exception {
        // Combination: html="", baseUri=" "
        Object actual = Jsoup.parse("", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_013() throws Exception {
        // Combination: html=" ", baseUri=" "
        Object actual = Jsoup.parse(" ", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_014() throws Exception {
        // Combination: html="a", baseUri=" "
        Object actual = Jsoup.parse("a", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_015() throws Exception {
        // Combination: html="test123", baseUri=" "
        Object actual = Jsoup.parse("test123", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_016() throws Exception {
        // Combination: html="!@#", baseUri=" "
        Object actual = Jsoup.parse("!@#", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_017() throws Exception {
        // Combination: html="0", baseUri=" "
        Object actual = Jsoup.parse("0", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_018() throws Exception {
        // Combination: html="-1", baseUri=" "
        Object actual = Jsoup.parse("-1", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_019() throws Exception {
        // Combination: html="1.5", baseUri=" "
        Object actual = Jsoup.parse("1.5", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_020() throws Exception {
        // Combination: html="9223372036854775807", baseUri=" "
        Object actual = Jsoup.parse("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_021() throws Exception {
        // Combination: html="9223372036854775808", baseUri=" "
        Object actual = Jsoup.parse("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_022() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_023() throws Exception {
        // Combination: html="", baseUri="a"
        Object actual = Jsoup.parse("", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_024() throws Exception {
        // Combination: html=" ", baseUri="a"
        Object actual = Jsoup.parse(" ", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_025() throws Exception {
        // Combination: html="a", baseUri="a"
        Object actual = Jsoup.parse("a", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_026() throws Exception {
        // Combination: html="test123", baseUri="a"
        Object actual = Jsoup.parse("test123", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_027() throws Exception {
        // Combination: html="!@#", baseUri="a"
        Object actual = Jsoup.parse("!@#", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_028() throws Exception {
        // Combination: html="0", baseUri="a"
        Object actual = Jsoup.parse("0", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_029() throws Exception {
        // Combination: html="-1", baseUri="a"
        Object actual = Jsoup.parse("-1", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_030() throws Exception {
        // Combination: html="1.5", baseUri="a"
        Object actual = Jsoup.parse("1.5", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_031() throws Exception {
        // Combination: html="9223372036854775807", baseUri="a"
        Object actual = Jsoup.parse("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_032() throws Exception {
        // Combination: html="9223372036854775808", baseUri="a"
        Object actual = Jsoup.parse("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_033() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_034() throws Exception {
        // Combination: html="", baseUri="test123"
        Object actual = Jsoup.parse("", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_035() throws Exception {
        // Combination: html=" ", baseUri="test123"
        Object actual = Jsoup.parse(" ", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_036() throws Exception {
        // Combination: html="a", baseUri="test123"
        Object actual = Jsoup.parse("a", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_037() throws Exception {
        // Combination: html="test123", baseUri="test123"
        Object actual = Jsoup.parse("test123", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_038() throws Exception {
        // Combination: html="!@#", baseUri="test123"
        Object actual = Jsoup.parse("!@#", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_039() throws Exception {
        // Combination: html="0", baseUri="test123"
        Object actual = Jsoup.parse("0", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_040() throws Exception {
        // Combination: html="-1", baseUri="test123"
        Object actual = Jsoup.parse("-1", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_041() throws Exception {
        // Combination: html="1.5", baseUri="test123"
        Object actual = Jsoup.parse("1.5", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_042() throws Exception {
        // Combination: html="9223372036854775807", baseUri="test123"
        Object actual = Jsoup.parse("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_043() throws Exception {
        // Combination: html="9223372036854775808", baseUri="test123"
        Object actual = Jsoup.parse("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_044() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_045() throws Exception {
        // Combination: html="", baseUri="!@#"
        Object actual = Jsoup.parse("", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_046() throws Exception {
        // Combination: html=" ", baseUri="!@#"
        Object actual = Jsoup.parse(" ", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_047() throws Exception {
        // Combination: html="a", baseUri="!@#"
        Object actual = Jsoup.parse("a", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_048() throws Exception {
        // Combination: html="test123", baseUri="!@#"
        Object actual = Jsoup.parse("test123", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_049() throws Exception {
        // Combination: html="!@#", baseUri="!@#"
        Object actual = Jsoup.parse("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_050() throws Exception {
        // Combination: html="0", baseUri="!@#"
        Object actual = Jsoup.parse("0", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_051() throws Exception {
        // Combination: html="-1", baseUri="!@#"
        Object actual = Jsoup.parse("-1", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_052() throws Exception {
        // Combination: html="1.5", baseUri="!@#"
        Object actual = Jsoup.parse("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_053() throws Exception {
        // Combination: html="9223372036854775807", baseUri="!@#"
        Object actual = Jsoup.parse("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_054() throws Exception {
        // Combination: html="9223372036854775808", baseUri="!@#"
        Object actual = Jsoup.parse("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_055() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_056() throws Exception {
        // Combination: html="", baseUri="0"
        Object actual = Jsoup.parse("", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_057() throws Exception {
        // Combination: html=" ", baseUri="0"
        Object actual = Jsoup.parse(" ", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_058() throws Exception {
        // Combination: html="a", baseUri="0"
        Object actual = Jsoup.parse("a", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_059() throws Exception {
        // Combination: html="test123", baseUri="0"
        Object actual = Jsoup.parse("test123", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_060() throws Exception {
        // Combination: html="!@#", baseUri="0"
        Object actual = Jsoup.parse("!@#", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_061() throws Exception {
        // Combination: html="0", baseUri="0"
        Object actual = Jsoup.parse("0", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_062() throws Exception {
        // Combination: html="-1", baseUri="0"
        Object actual = Jsoup.parse("-1", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_063() throws Exception {
        // Combination: html="1.5", baseUri="0"
        Object actual = Jsoup.parse("1.5", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_064() throws Exception {
        // Combination: html="9223372036854775807", baseUri="0"
        Object actual = Jsoup.parse("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_065() throws Exception {
        // Combination: html="9223372036854775808", baseUri="0"
        Object actual = Jsoup.parse("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_066() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_067() throws Exception {
        // Combination: html="", baseUri="-1"
        Object actual = Jsoup.parse("", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_068() throws Exception {
        // Combination: html=" ", baseUri="-1"
        Object actual = Jsoup.parse(" ", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_069() throws Exception {
        // Combination: html="a", baseUri="-1"
        Object actual = Jsoup.parse("a", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_070() throws Exception {
        // Combination: html="test123", baseUri="-1"
        Object actual = Jsoup.parse("test123", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_071() throws Exception {
        // Combination: html="!@#", baseUri="-1"
        Object actual = Jsoup.parse("!@#", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_072() throws Exception {
        // Combination: html="0", baseUri="-1"
        Object actual = Jsoup.parse("0", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_073() throws Exception {
        // Combination: html="-1", baseUri="-1"
        Object actual = Jsoup.parse("-1", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_074() throws Exception {
        // Combination: html="1.5", baseUri="-1"
        Object actual = Jsoup.parse("1.5", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_075() throws Exception {
        // Combination: html="9223372036854775807", baseUri="-1"
        Object actual = Jsoup.parse("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_076() throws Exception {
        // Combination: html="9223372036854775808", baseUri="-1"
        Object actual = Jsoup.parse("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_077() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_078() throws Exception {
        // Combination: html="", baseUri="1.5"
        Object actual = Jsoup.parse("", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_079() throws Exception {
        // Combination: html=" ", baseUri="1.5"
        Object actual = Jsoup.parse(" ", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_080() throws Exception {
        // Combination: html="a", baseUri="1.5"
        Object actual = Jsoup.parse("a", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_081() throws Exception {
        // Combination: html="test123", baseUri="1.5"
        Object actual = Jsoup.parse("test123", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_082() throws Exception {
        // Combination: html="!@#", baseUri="1.5"
        Object actual = Jsoup.parse("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_083() throws Exception {
        // Combination: html="0", baseUri="1.5"
        Object actual = Jsoup.parse("0", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_084() throws Exception {
        // Combination: html="-1", baseUri="1.5"
        Object actual = Jsoup.parse("-1", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_085() throws Exception {
        // Combination: html="1.5", baseUri="1.5"
        Object actual = Jsoup.parse("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_086() throws Exception {
        // Combination: html="9223372036854775807", baseUri="1.5"
        Object actual = Jsoup.parse("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_087() throws Exception {
        // Combination: html="9223372036854775808", baseUri="1.5"
        Object actual = Jsoup.parse("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_088() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_089() throws Exception {
        // Combination: html="", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_090() throws Exception {
        // Combination: html=" ", baseUri="9223372036854775807"
        Object actual = Jsoup.parse(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_091() throws Exception {
        // Combination: html="a", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_092() throws Exception {
        // Combination: html="test123", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_093() throws Exception {
        // Combination: html="!@#", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_094() throws Exception {
        // Combination: html="0", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_095() throws Exception {
        // Combination: html="-1", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_096() throws Exception {
        // Combination: html="1.5", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_097() throws Exception {
        // Combination: html="9223372036854775807", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_098() throws Exception {
        // Combination: html="9223372036854775808", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_099() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_100() throws Exception {
        // Combination: html="", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_101() throws Exception {
        // Combination: html=" ", baseUri="9223372036854775808"
        Object actual = Jsoup.parse(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_102() throws Exception {
        // Combination: html="a", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_103() throws Exception {
        // Combination: html="test123", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_104() throws Exception {
        // Combination: html="!@#", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_105() throws Exception {
        // Combination: html="0", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_106() throws Exception {
        // Combination: html="-1", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_107() throws Exception {
        // Combination: html="1.5", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_108() throws Exception {
        // Combination: html="9223372036854775807", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_109() throws Exception {
        // Combination: html="9223372036854775808", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_110() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_111() throws Exception {
        // Combination: html="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_112() throws Exception {
        // Combination: html=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_113() throws Exception {
        // Combination: html="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_114() throws Exception {
        // Combination: html="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_115() throws Exception {
        // Combination: html="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_116() throws Exception {
        // Combination: html="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_117() throws Exception {
        // Combination: html="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_118() throws Exception {
        // Combination: html="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_119() throws Exception {
        // Combination: html="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_120() throws Exception {
        // Combination: html="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_121() throws Exception {
        // Combination: html="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_122() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_123() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri=""
        try {
            Jsoup.parse(new java.io.File("."), " ", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_124() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_125() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_126() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_127() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_128() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_129() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_130() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_131() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_132() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_133() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_134() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName=" ", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("temp.txt"), " ", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_135() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="a", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "a", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_136() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="test123", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "test123", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_137() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="!@#", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "!@#", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_138() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="0", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "0", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_139() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="-1", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "-1", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_140() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="1.5", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "1.5", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_141() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775807", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "9223372036854775807", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_142() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775808", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "9223372036854775808", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_143() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        try {
            Jsoup.parse(new java.io.File("."), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_144() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_145() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("."), " ", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_146() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_147() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_148() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_149() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_150() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_151() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_152() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_153() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_154() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_155() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_156() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("."), " ", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_157() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_158() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_159() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_160() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_161() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_162() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_163() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_164() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_165() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_166() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_167() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("."), " ", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_168() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_169() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_170() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_171() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_172() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_173() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_174() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_175() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_176() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_177() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_178() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("."), " ", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_179() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_180() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_181() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_182() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_183() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_184() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_185() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_186() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_187() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_188() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_189() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("."), " ", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_190() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_191() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_192() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_193() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_194() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_195() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_196() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_197() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_198() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_199() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_200() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("."), " ", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_201() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_202() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_203() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_204() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_205() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_206() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_207() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_208() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_209() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_210() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_211() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("."), " ", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_212() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_213() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_214() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_215() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_216() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_217() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_218() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_219() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_220() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_221() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_222() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("."), " ", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_223() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_224() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_225() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_226() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_227() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_228() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_229() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_230() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_231() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_232() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_233() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("."), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_234() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_235() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_236() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_237() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_238() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_239() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_240() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_241() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_242() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_243() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName=""
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_244() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=""
        try {
            Jsoup.parse(new java.io.File("."), "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_245() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName=" "
        try {
            Jsoup.parse(new java.io.File("temp.txt"), " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_246() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" "
        try {
            Jsoup.parse(new java.io.File("."), " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_247() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_248() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="a"
        try {
            Jsoup.parse(new java.io.File("."), "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_249() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_250() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="test123"
        try {
            Jsoup.parse(new java.io.File("."), "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_251() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_252() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="!@#"
        try {
            Jsoup.parse(new java.io.File("."), "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_253() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_254() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="0"
        try {
            Jsoup.parse(new java.io.File("."), "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_255() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_256() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="-1"
        try {
            Jsoup.parse(new java.io.File("."), "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_257() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_258() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="1.5"
        try {
            Jsoup.parse(new java.io.File("."), "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_259() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_260() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775807"
        try {
            Jsoup.parse(new java.io.File("."), "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_261() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_262() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775808"
        try {
            Jsoup.parse(new java.io.File("."), "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_263() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_264() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.File("."), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_265() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_266() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_267() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_268() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_269() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_270() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_271() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_272() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_273() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_274() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_275() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_276() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_277() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName=" ", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), " ", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_278() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="a", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "a", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_279() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="test123", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "test123", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_280() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="!@#", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "!@#", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_281() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="0", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "0", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_282() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="-1", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "-1", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_283() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="1.5", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "1.5", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_284() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="9223372036854775807", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "9223372036854775807", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_285() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="9223372036854775808", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "9223372036854775808", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_286() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_287() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_288() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_289() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_290() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_291() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_292() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_293() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_294() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_295() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_296() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_297() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_298() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_299() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_300() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_301() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_302() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_303() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_304() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_305() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_306() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_307() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_308() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_309() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_310() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_311() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_312() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_313() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_314() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_315() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_316() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_317() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_318() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_319() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_320() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_321() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_322() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_323() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_324() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_325() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_326() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_327() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_328() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_329() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_330() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_331() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_332() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_333() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_334() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_335() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_336() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_337() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_338() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_339() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_340() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_341() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_342() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_343() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_344() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_345() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_346() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_347() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_348() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_349() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_350() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_351() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_352() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_353() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_354() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_355() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_356() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_357() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_358() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_359() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_360() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_361() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_362() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_363() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_364() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_365() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_366() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_367() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_368() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_369() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_370() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_371() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_372() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_373() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_374() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_375() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_376() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_377() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_378() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_379() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_380() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_381() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_382() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_383() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_384() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_385() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            Jsoup.parse(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_386() throws Exception {
        // Combination: bodyHtml="", baseUri=""
        Object actual = Jsoup.parseBodyFragment("", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_387() throws Exception {
        // Combination: bodyHtml=" ", baseUri=""
        Object actual = Jsoup.parseBodyFragment(" ", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_388() throws Exception {
        // Combination: bodyHtml="a", baseUri=""
        Object actual = Jsoup.parseBodyFragment("a", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_389() throws Exception {
        // Combination: bodyHtml="test123", baseUri=""
        Object actual = Jsoup.parseBodyFragment("test123", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_390() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=""
        Object actual = Jsoup.parseBodyFragment("!@#", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_391() throws Exception {
        // Combination: bodyHtml="0", baseUri=""
        Object actual = Jsoup.parseBodyFragment("0", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_392() throws Exception {
        // Combination: bodyHtml="-1", baseUri=""
        Object actual = Jsoup.parseBodyFragment("-1", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_393() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=""
        Object actual = Jsoup.parseBodyFragment("1.5", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_394() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=""
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_395() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=""
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_396() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_397() throws Exception {
        // Combination: bodyHtml="", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_398() throws Exception {
        // Combination: bodyHtml=" ", baseUri=" "
        Object actual = Jsoup.parseBodyFragment(" ", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_399() throws Exception {
        // Combination: bodyHtml="a", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("a", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_400() throws Exception {
        // Combination: bodyHtml="test123", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("test123", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_401() throws Exception {
        // Combination: bodyHtml="!@#", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("!@#", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_402() throws Exception {
        // Combination: bodyHtml="0", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("0", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_403() throws Exception {
        // Combination: bodyHtml="-1", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("-1", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_404() throws Exception {
        // Combination: bodyHtml="1.5", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("1.5", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_405() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_406() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_407() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_408() throws Exception {
        // Combination: bodyHtml="", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_409() throws Exception {
        // Combination: bodyHtml=" ", baseUri="a"
        Object actual = Jsoup.parseBodyFragment(" ", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_410() throws Exception {
        // Combination: bodyHtml="a", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("a", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_411() throws Exception {
        // Combination: bodyHtml="test123", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("test123", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_412() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("!@#", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_413() throws Exception {
        // Combination: bodyHtml="0", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("0", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_414() throws Exception {
        // Combination: bodyHtml="-1", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("-1", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_415() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("1.5", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_416() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_417() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_418() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_419() throws Exception {
        // Combination: bodyHtml="", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_420() throws Exception {
        // Combination: bodyHtml=" ", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment(" ", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_421() throws Exception {
        // Combination: bodyHtml="a", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("a", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_422() throws Exception {
        // Combination: bodyHtml="test123", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("test123", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_423() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("!@#", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_424() throws Exception {
        // Combination: bodyHtml="0", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("0", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_425() throws Exception {
        // Combination: bodyHtml="-1", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("-1", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_426() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("1.5", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_427() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_428() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_429() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_430() throws Exception {
        // Combination: bodyHtml="", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_431() throws Exception {
        // Combination: bodyHtml=" ", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment(" ", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_432() throws Exception {
        // Combination: bodyHtml="a", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("a", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_433() throws Exception {
        // Combination: bodyHtml="test123", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("test123", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_434() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_435() throws Exception {
        // Combination: bodyHtml="0", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("0", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_436() throws Exception {
        // Combination: bodyHtml="-1", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("-1", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_437() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_438() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_439() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_440() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_441() throws Exception {
        // Combination: bodyHtml="", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_442() throws Exception {
        // Combination: bodyHtml=" ", baseUri="0"
        Object actual = Jsoup.parseBodyFragment(" ", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_443() throws Exception {
        // Combination: bodyHtml="a", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("a", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_444() throws Exception {
        // Combination: bodyHtml="test123", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("test123", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_445() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("!@#", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_446() throws Exception {
        // Combination: bodyHtml="0", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("0", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_447() throws Exception {
        // Combination: bodyHtml="-1", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("-1", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_448() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("1.5", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_449() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_450() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_451() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_452() throws Exception {
        // Combination: bodyHtml="", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_453() throws Exception {
        // Combination: bodyHtml=" ", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment(" ", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_454() throws Exception {
        // Combination: bodyHtml="a", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("a", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_455() throws Exception {
        // Combination: bodyHtml="test123", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("test123", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_456() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("!@#", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_457() throws Exception {
        // Combination: bodyHtml="0", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("0", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_458() throws Exception {
        // Combination: bodyHtml="-1", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("-1", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_459() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("1.5", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_460() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_461() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_462() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_463() throws Exception {
        // Combination: bodyHtml="", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_464() throws Exception {
        // Combination: bodyHtml=" ", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment(" ", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_465() throws Exception {
        // Combination: bodyHtml="a", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("a", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_466() throws Exception {
        // Combination: bodyHtml="test123", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("test123", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_467() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_468() throws Exception {
        // Combination: bodyHtml="0", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("0", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_469() throws Exception {
        // Combination: bodyHtml="-1", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("-1", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_470() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_471() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_472() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_473() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_474() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_475() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_476() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_477() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_478() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_479() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_480() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_481() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_482() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_483() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_484() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_485() throws Exception {
        // Combination: bodyHtml="", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_486() throws Exception {
        // Combination: bodyHtml=" ", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_487() throws Exception {
        // Combination: bodyHtml="a", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_488() throws Exception {
        // Combination: bodyHtml="test123", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_489() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_490() throws Exception {
        // Combination: bodyHtml="0", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_491() throws Exception {
        // Combination: bodyHtml="-1", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_492() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_493() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_494() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_495() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_496() throws Exception {
        // Combination: bodyHtml="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body></body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_497() throws Exception {
        // Combination: bodyHtml=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body> \n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_498() throws Exception {
        // Combination: bodyHtml="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  a\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_499() throws Exception {
        // Combination: bodyHtml="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  test123\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_500() throws Exception {
        // Combination: bodyHtml="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  !@#\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_501() throws Exception {
        // Combination: bodyHtml="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  0\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_502() throws Exception {
        // Combination: bodyHtml="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  -1\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_503() throws Exception {
        // Combination: bodyHtml="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  1.5\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_504() throws Exception {
        // Combination: bodyHtml="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775807\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_505() throws Exception {
        // Combination: bodyHtml="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  9223372036854775808\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parseBodyFragment_pairwise_506() throws Exception {
        // Combination: bodyHtml="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = Jsoup.parseBodyFragment("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Document", actual.getClass().getName());
        assertEquals("<html>\n <head></head>\n <body>\n  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n </body>\n</html>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_507() throws Exception {
        // Combination: url=new java.net.URL("http://localhost"), timeoutMillis=0
        try {
            Jsoup.parse(new java.net.URL("http://localhost"), 0);
            fail("Expected java.net.ConnectException");
        } catch (java.net.ConnectException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_508() throws Exception {
        // Combination: url=new java.net.URL("http://localhost"), timeoutMillis=1
        try {
            Jsoup.parse(new java.net.URL("http://localhost"), 1);
            fail("Expected java.net.ConnectException");
        } catch (java.net.ConnectException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_509() throws Exception {
        // Combination: url=new java.net.URL("http://localhost"), timeoutMillis=-1
        try {
            Jsoup.parse(new java.net.URL("http://localhost"), -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_510() throws Exception {
        // Combination: url=new java.net.URL("http://localhost"), timeoutMillis=Integer.MAX_VALUE
        try {
            Jsoup.parse(new java.net.URL("http://localhost"), Integer.MAX_VALUE);
            fail("Expected java.net.ConnectException");
        } catch (java.net.ConnectException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_511() throws Exception {
        // Combination: url=new java.net.URL("http://localhost"), timeoutMillis=Integer.MIN_VALUE
        try {
            Jsoup.parse(new java.net.URL("http://localhost"), Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
