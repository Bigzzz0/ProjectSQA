package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Document.
 */
public class Document_IPOTest {
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
    public void test_createElement_pairwise_001() throws Exception {
        // Combination: receiver__baseUri="", tagName=""
        try {
            (new Document("")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_002() throws Exception {
        // Combination: receiver__baseUri="", tagName=" "
        try {
            (new Document("")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_003() throws Exception {
        // Combination: receiver__baseUri="", tagName="a"
        Object actual = (new Document("")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_004() throws Exception {
        // Combination: receiver__baseUri="", tagName="test123"
        Object actual = (new Document("")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_005() throws Exception {
        // Combination: receiver__baseUri="", tagName="!@#"
        Object actual = (new Document("")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_006() throws Exception {
        // Combination: receiver__baseUri="", tagName="0"
        Object actual = (new Document("")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_007() throws Exception {
        // Combination: receiver__baseUri="", tagName="-1"
        Object actual = (new Document("")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_008() throws Exception {
        // Combination: receiver__baseUri="", tagName="1.5"
        Object actual = (new Document("")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_009() throws Exception {
        // Combination: receiver__baseUri="", tagName="9223372036854775807"
        Object actual = (new Document("")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_010() throws Exception {
        // Combination: receiver__baseUri="", tagName="9223372036854775808"
        Object actual = (new Document("")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_011() throws Exception {
        // Combination: receiver__baseUri="", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_012() throws Exception {
        // Combination: receiver__baseUri=" ", tagName=""
        try {
            (new Document(" ")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_013() throws Exception {
        // Combination: receiver__baseUri=" ", tagName=" "
        try {
            (new Document(" ")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_014() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="a"
        Object actual = (new Document(" ")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_015() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="test123"
        Object actual = (new Document(" ")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_016() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="!@#"
        Object actual = (new Document(" ")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_017() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="0"
        Object actual = (new Document(" ")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_018() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="-1"
        Object actual = (new Document(" ")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_019() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="1.5"
        Object actual = (new Document(" ")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_020() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="9223372036854775807"
        Object actual = (new Document(" ")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_021() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="9223372036854775808"
        Object actual = (new Document(" ")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_022() throws Exception {
        // Combination: receiver__baseUri=" ", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document(" ")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_023() throws Exception {
        // Combination: receiver__baseUri="a", tagName=""
        try {
            (new Document("a")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_024() throws Exception {
        // Combination: receiver__baseUri="a", tagName=" "
        try {
            (new Document("a")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_025() throws Exception {
        // Combination: receiver__baseUri="a", tagName="a"
        Object actual = (new Document("a")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_026() throws Exception {
        // Combination: receiver__baseUri="a", tagName="test123"
        Object actual = (new Document("a")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_027() throws Exception {
        // Combination: receiver__baseUri="a", tagName="!@#"
        Object actual = (new Document("a")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_028() throws Exception {
        // Combination: receiver__baseUri="a", tagName="0"
        Object actual = (new Document("a")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_029() throws Exception {
        // Combination: receiver__baseUri="a", tagName="-1"
        Object actual = (new Document("a")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_030() throws Exception {
        // Combination: receiver__baseUri="a", tagName="1.5"
        Object actual = (new Document("a")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_031() throws Exception {
        // Combination: receiver__baseUri="a", tagName="9223372036854775807"
        Object actual = (new Document("a")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_032() throws Exception {
        // Combination: receiver__baseUri="a", tagName="9223372036854775808"
        Object actual = (new Document("a")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_033() throws Exception {
        // Combination: receiver__baseUri="a", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("a")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_034() throws Exception {
        // Combination: receiver__baseUri="test123", tagName=""
        try {
            (new Document("test123")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_035() throws Exception {
        // Combination: receiver__baseUri="test123", tagName=" "
        try {
            (new Document("test123")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_036() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="a"
        Object actual = (new Document("test123")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_037() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="test123"
        Object actual = (new Document("test123")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_038() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="!@#"
        Object actual = (new Document("test123")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_039() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="0"
        Object actual = (new Document("test123")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_040() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="-1"
        Object actual = (new Document("test123")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_041() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="1.5"
        Object actual = (new Document("test123")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_042() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="9223372036854775807"
        Object actual = (new Document("test123")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_043() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="9223372036854775808"
        Object actual = (new Document("test123")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_044() throws Exception {
        // Combination: receiver__baseUri="test123", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("test123")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_045() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName=""
        try {
            (new Document("!@#")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_046() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName=" "
        try {
            (new Document("!@#")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_047() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="a"
        Object actual = (new Document("!@#")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_048() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="test123"
        Object actual = (new Document("!@#")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_049() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="!@#"
        Object actual = (new Document("!@#")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_050() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="0"
        Object actual = (new Document("!@#")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_051() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="-1"
        Object actual = (new Document("!@#")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_052() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="1.5"
        Object actual = (new Document("!@#")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_053() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="9223372036854775807"
        Object actual = (new Document("!@#")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_054() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="9223372036854775808"
        Object actual = (new Document("!@#")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_055() throws Exception {
        // Combination: receiver__baseUri="!@#", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("!@#")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_056() throws Exception {
        // Combination: receiver__baseUri="0", tagName=""
        try {
            (new Document("0")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_057() throws Exception {
        // Combination: receiver__baseUri="0", tagName=" "
        try {
            (new Document("0")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_058() throws Exception {
        // Combination: receiver__baseUri="0", tagName="a"
        Object actual = (new Document("0")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_059() throws Exception {
        // Combination: receiver__baseUri="0", tagName="test123"
        Object actual = (new Document("0")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_060() throws Exception {
        // Combination: receiver__baseUri="0", tagName="!@#"
        Object actual = (new Document("0")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_061() throws Exception {
        // Combination: receiver__baseUri="0", tagName="0"
        Object actual = (new Document("0")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_062() throws Exception {
        // Combination: receiver__baseUri="0", tagName="-1"
        Object actual = (new Document("0")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_063() throws Exception {
        // Combination: receiver__baseUri="0", tagName="1.5"
        Object actual = (new Document("0")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_064() throws Exception {
        // Combination: receiver__baseUri="0", tagName="9223372036854775807"
        Object actual = (new Document("0")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_065() throws Exception {
        // Combination: receiver__baseUri="0", tagName="9223372036854775808"
        Object actual = (new Document("0")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_066() throws Exception {
        // Combination: receiver__baseUri="0", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("0")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_067() throws Exception {
        // Combination: receiver__baseUri="-1", tagName=""
        try {
            (new Document("-1")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_068() throws Exception {
        // Combination: receiver__baseUri="-1", tagName=" "
        try {
            (new Document("-1")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_069() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="a"
        Object actual = (new Document("-1")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_070() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="test123"
        Object actual = (new Document("-1")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_071() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="!@#"
        Object actual = (new Document("-1")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_072() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="0"
        Object actual = (new Document("-1")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_073() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="-1"
        Object actual = (new Document("-1")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_074() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="1.5"
        Object actual = (new Document("-1")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_075() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="9223372036854775807"
        Object actual = (new Document("-1")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_076() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="9223372036854775808"
        Object actual = (new Document("-1")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_077() throws Exception {
        // Combination: receiver__baseUri="-1", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("-1")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_078() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName=""
        try {
            (new Document("1.5")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_079() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName=" "
        try {
            (new Document("1.5")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_080() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="a"
        Object actual = (new Document("1.5")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_081() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="test123"
        Object actual = (new Document("1.5")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_082() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="!@#"
        Object actual = (new Document("1.5")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_083() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="0"
        Object actual = (new Document("1.5")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_084() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="-1"
        Object actual = (new Document("1.5")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_085() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="1.5"
        Object actual = (new Document("1.5")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_086() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="9223372036854775807"
        Object actual = (new Document("1.5")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_087() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="9223372036854775808"
        Object actual = (new Document("1.5")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_088() throws Exception {
        // Combination: receiver__baseUri="1.5", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("1.5")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_089() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName=""
        try {
            (new Document("9223372036854775807")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_090() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName=" "
        try {
            (new Document("9223372036854775807")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_091() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="a"
        Object actual = (new Document("9223372036854775807")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_092() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="test123"
        Object actual = (new Document("9223372036854775807")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_093() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="!@#"
        Object actual = (new Document("9223372036854775807")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_094() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="0"
        Object actual = (new Document("9223372036854775807")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_095() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="-1"
        Object actual = (new Document("9223372036854775807")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_096() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="1.5"
        Object actual = (new Document("9223372036854775807")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_097() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="9223372036854775807"
        Object actual = (new Document("9223372036854775807")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_098() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="9223372036854775808"
        Object actual = (new Document("9223372036854775807")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_099() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("9223372036854775807")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_100() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName=""
        try {
            (new Document("9223372036854775808")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_101() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName=" "
        try {
            (new Document("9223372036854775808")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_102() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="a"
        Object actual = (new Document("9223372036854775808")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_103() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="test123"
        Object actual = (new Document("9223372036854775808")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_104() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="!@#"
        Object actual = (new Document("9223372036854775808")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_105() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="0"
        Object actual = (new Document("9223372036854775808")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_106() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="-1"
        Object actual = (new Document("9223372036854775808")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_107() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="1.5"
        Object actual = (new Document("9223372036854775808")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_108() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="9223372036854775807"
        Object actual = (new Document("9223372036854775808")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_109() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="9223372036854775808"
        Object actual = (new Document("9223372036854775808")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_110() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("9223372036854775808")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_111() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName=""
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_112() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName=" "
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_113() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="a"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("a");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<a></a>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_114() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="test123"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("test123");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<test123>\n</test123>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_115() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="!@#"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("!@#");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<!@#>\n</!@#>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_116() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="0"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("0");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<0>\n</0>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_117() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="-1"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("-1");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<-1>\n</-1>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_118() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="1.5"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("1.5");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<1.5>\n</1.5>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_119() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="9223372036854775807"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("9223372036854775807");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775807>\n</9223372036854775807>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_120() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="9223372036854775808"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("9223372036854775808");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<9223372036854775808>\n</9223372036854775808>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createElement_pairwise_121() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", tagName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).createElement("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("org.jsoup.nodes.Element", actual.getClass().getName());
        assertEquals("<aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>\n</aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa>", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_122() throws Exception {
        // Combination: receiver__baseUri="", text=""
        try {
            (new Document("")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_123() throws Exception {
        // Combination: receiver__baseUri="", text=" "
        try {
            (new Document("")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_124() throws Exception {
        // Combination: receiver__baseUri="", text="a"
        try {
            (new Document("")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_125() throws Exception {
        // Combination: receiver__baseUri="", text="test123"
        try {
            (new Document("")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_126() throws Exception {
        // Combination: receiver__baseUri="", text="!@#"
        try {
            (new Document("")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_127() throws Exception {
        // Combination: receiver__baseUri="", text="0"
        try {
            (new Document("")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_128() throws Exception {
        // Combination: receiver__baseUri="", text="-1"
        try {
            (new Document("")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_129() throws Exception {
        // Combination: receiver__baseUri="", text="1.5"
        try {
            (new Document("")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_130() throws Exception {
        // Combination: receiver__baseUri="", text="9223372036854775807"
        try {
            (new Document("")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_131() throws Exception {
        // Combination: receiver__baseUri="", text="9223372036854775808"
        try {
            (new Document("")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_132() throws Exception {
        // Combination: receiver__baseUri="", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_133() throws Exception {
        // Combination: receiver__baseUri=" ", text=""
        try {
            (new Document(" ")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_134() throws Exception {
        // Combination: receiver__baseUri=" ", text=" "
        try {
            (new Document(" ")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_135() throws Exception {
        // Combination: receiver__baseUri=" ", text="a"
        try {
            (new Document(" ")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_136() throws Exception {
        // Combination: receiver__baseUri=" ", text="test123"
        try {
            (new Document(" ")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_137() throws Exception {
        // Combination: receiver__baseUri=" ", text="!@#"
        try {
            (new Document(" ")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_138() throws Exception {
        // Combination: receiver__baseUri=" ", text="0"
        try {
            (new Document(" ")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_139() throws Exception {
        // Combination: receiver__baseUri=" ", text="-1"
        try {
            (new Document(" ")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_140() throws Exception {
        // Combination: receiver__baseUri=" ", text="1.5"
        try {
            (new Document(" ")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_141() throws Exception {
        // Combination: receiver__baseUri=" ", text="9223372036854775807"
        try {
            (new Document(" ")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_142() throws Exception {
        // Combination: receiver__baseUri=" ", text="9223372036854775808"
        try {
            (new Document(" ")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_143() throws Exception {
        // Combination: receiver__baseUri=" ", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document(" ")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_144() throws Exception {
        // Combination: receiver__baseUri="a", text=""
        try {
            (new Document("a")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_145() throws Exception {
        // Combination: receiver__baseUri="a", text=" "
        try {
            (new Document("a")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_146() throws Exception {
        // Combination: receiver__baseUri="a", text="a"
        try {
            (new Document("a")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_147() throws Exception {
        // Combination: receiver__baseUri="a", text="test123"
        try {
            (new Document("a")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_148() throws Exception {
        // Combination: receiver__baseUri="a", text="!@#"
        try {
            (new Document("a")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_149() throws Exception {
        // Combination: receiver__baseUri="a", text="0"
        try {
            (new Document("a")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_150() throws Exception {
        // Combination: receiver__baseUri="a", text="-1"
        try {
            (new Document("a")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_151() throws Exception {
        // Combination: receiver__baseUri="a", text="1.5"
        try {
            (new Document("a")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_152() throws Exception {
        // Combination: receiver__baseUri="a", text="9223372036854775807"
        try {
            (new Document("a")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_153() throws Exception {
        // Combination: receiver__baseUri="a", text="9223372036854775808"
        try {
            (new Document("a")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_154() throws Exception {
        // Combination: receiver__baseUri="a", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("a")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_155() throws Exception {
        // Combination: receiver__baseUri="test123", text=""
        try {
            (new Document("test123")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_156() throws Exception {
        // Combination: receiver__baseUri="test123", text=" "
        try {
            (new Document("test123")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_157() throws Exception {
        // Combination: receiver__baseUri="test123", text="a"
        try {
            (new Document("test123")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_158() throws Exception {
        // Combination: receiver__baseUri="test123", text="test123"
        try {
            (new Document("test123")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_159() throws Exception {
        // Combination: receiver__baseUri="test123", text="!@#"
        try {
            (new Document("test123")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_160() throws Exception {
        // Combination: receiver__baseUri="test123", text="0"
        try {
            (new Document("test123")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_161() throws Exception {
        // Combination: receiver__baseUri="test123", text="-1"
        try {
            (new Document("test123")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_162() throws Exception {
        // Combination: receiver__baseUri="test123", text="1.5"
        try {
            (new Document("test123")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_163() throws Exception {
        // Combination: receiver__baseUri="test123", text="9223372036854775807"
        try {
            (new Document("test123")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_164() throws Exception {
        // Combination: receiver__baseUri="test123", text="9223372036854775808"
        try {
            (new Document("test123")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_165() throws Exception {
        // Combination: receiver__baseUri="test123", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("test123")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_166() throws Exception {
        // Combination: receiver__baseUri="!@#", text=""
        try {
            (new Document("!@#")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_167() throws Exception {
        // Combination: receiver__baseUri="!@#", text=" "
        try {
            (new Document("!@#")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_168() throws Exception {
        // Combination: receiver__baseUri="!@#", text="a"
        try {
            (new Document("!@#")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_169() throws Exception {
        // Combination: receiver__baseUri="!@#", text="test123"
        try {
            (new Document("!@#")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_170() throws Exception {
        // Combination: receiver__baseUri="!@#", text="!@#"
        try {
            (new Document("!@#")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_171() throws Exception {
        // Combination: receiver__baseUri="!@#", text="0"
        try {
            (new Document("!@#")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_172() throws Exception {
        // Combination: receiver__baseUri="!@#", text="-1"
        try {
            (new Document("!@#")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_173() throws Exception {
        // Combination: receiver__baseUri="!@#", text="1.5"
        try {
            (new Document("!@#")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_174() throws Exception {
        // Combination: receiver__baseUri="!@#", text="9223372036854775807"
        try {
            (new Document("!@#")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_175() throws Exception {
        // Combination: receiver__baseUri="!@#", text="9223372036854775808"
        try {
            (new Document("!@#")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_176() throws Exception {
        // Combination: receiver__baseUri="!@#", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("!@#")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_177() throws Exception {
        // Combination: receiver__baseUri="0", text=""
        try {
            (new Document("0")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_178() throws Exception {
        // Combination: receiver__baseUri="0", text=" "
        try {
            (new Document("0")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_179() throws Exception {
        // Combination: receiver__baseUri="0", text="a"
        try {
            (new Document("0")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_180() throws Exception {
        // Combination: receiver__baseUri="0", text="test123"
        try {
            (new Document("0")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_181() throws Exception {
        // Combination: receiver__baseUri="0", text="!@#"
        try {
            (new Document("0")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_182() throws Exception {
        // Combination: receiver__baseUri="0", text="0"
        try {
            (new Document("0")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_183() throws Exception {
        // Combination: receiver__baseUri="0", text="-1"
        try {
            (new Document("0")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_184() throws Exception {
        // Combination: receiver__baseUri="0", text="1.5"
        try {
            (new Document("0")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_185() throws Exception {
        // Combination: receiver__baseUri="0", text="9223372036854775807"
        try {
            (new Document("0")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_186() throws Exception {
        // Combination: receiver__baseUri="0", text="9223372036854775808"
        try {
            (new Document("0")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_187() throws Exception {
        // Combination: receiver__baseUri="0", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("0")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_188() throws Exception {
        // Combination: receiver__baseUri="-1", text=""
        try {
            (new Document("-1")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_189() throws Exception {
        // Combination: receiver__baseUri="-1", text=" "
        try {
            (new Document("-1")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_190() throws Exception {
        // Combination: receiver__baseUri="-1", text="a"
        try {
            (new Document("-1")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_191() throws Exception {
        // Combination: receiver__baseUri="-1", text="test123"
        try {
            (new Document("-1")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_192() throws Exception {
        // Combination: receiver__baseUri="-1", text="!@#"
        try {
            (new Document("-1")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_193() throws Exception {
        // Combination: receiver__baseUri="-1", text="0"
        try {
            (new Document("-1")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_194() throws Exception {
        // Combination: receiver__baseUri="-1", text="-1"
        try {
            (new Document("-1")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_195() throws Exception {
        // Combination: receiver__baseUri="-1", text="1.5"
        try {
            (new Document("-1")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_196() throws Exception {
        // Combination: receiver__baseUri="-1", text="9223372036854775807"
        try {
            (new Document("-1")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_197() throws Exception {
        // Combination: receiver__baseUri="-1", text="9223372036854775808"
        try {
            (new Document("-1")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_198() throws Exception {
        // Combination: receiver__baseUri="-1", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("-1")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_199() throws Exception {
        // Combination: receiver__baseUri="1.5", text=""
        try {
            (new Document("1.5")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_200() throws Exception {
        // Combination: receiver__baseUri="1.5", text=" "
        try {
            (new Document("1.5")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_201() throws Exception {
        // Combination: receiver__baseUri="1.5", text="a"
        try {
            (new Document("1.5")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_202() throws Exception {
        // Combination: receiver__baseUri="1.5", text="test123"
        try {
            (new Document("1.5")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_203() throws Exception {
        // Combination: receiver__baseUri="1.5", text="!@#"
        try {
            (new Document("1.5")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_204() throws Exception {
        // Combination: receiver__baseUri="1.5", text="0"
        try {
            (new Document("1.5")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_205() throws Exception {
        // Combination: receiver__baseUri="1.5", text="-1"
        try {
            (new Document("1.5")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_206() throws Exception {
        // Combination: receiver__baseUri="1.5", text="1.5"
        try {
            (new Document("1.5")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_207() throws Exception {
        // Combination: receiver__baseUri="1.5", text="9223372036854775807"
        try {
            (new Document("1.5")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_208() throws Exception {
        // Combination: receiver__baseUri="1.5", text="9223372036854775808"
        try {
            (new Document("1.5")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_209() throws Exception {
        // Combination: receiver__baseUri="1.5", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("1.5")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_210() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text=""
        try {
            (new Document("9223372036854775807")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_211() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text=" "
        try {
            (new Document("9223372036854775807")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_212() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="a"
        try {
            (new Document("9223372036854775807")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_213() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="test123"
        try {
            (new Document("9223372036854775807")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_214() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="!@#"
        try {
            (new Document("9223372036854775807")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_215() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="0"
        try {
            (new Document("9223372036854775807")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_216() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="-1"
        try {
            (new Document("9223372036854775807")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_217() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="1.5"
        try {
            (new Document("9223372036854775807")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_218() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="9223372036854775807"
        try {
            (new Document("9223372036854775807")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_219() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="9223372036854775808"
        try {
            (new Document("9223372036854775807")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_220() throws Exception {
        // Combination: receiver__baseUri="9223372036854775807", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("9223372036854775807")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_221() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text=""
        try {
            (new Document("9223372036854775808")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_222() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text=" "
        try {
            (new Document("9223372036854775808")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_223() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="a"
        try {
            (new Document("9223372036854775808")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_224() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="test123"
        try {
            (new Document("9223372036854775808")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_225() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="!@#"
        try {
            (new Document("9223372036854775808")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_226() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="0"
        try {
            (new Document("9223372036854775808")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_227() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="-1"
        try {
            (new Document("9223372036854775808")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_228() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="1.5"
        try {
            (new Document("9223372036854775808")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_229() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="9223372036854775807"
        try {
            (new Document("9223372036854775808")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_230() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="9223372036854775808"
        try {
            (new Document("9223372036854775808")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_231() throws Exception {
        // Combination: receiver__baseUri="9223372036854775808", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("9223372036854775808")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_232() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text=""
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_233() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text=" "
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text(" ");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_234() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="a"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("a");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_235() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="test123"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("test123");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_236() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="!@#"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("!@#");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_237() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="0"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("0");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_238() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="-1"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("-1");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_239() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="1.5"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("1.5");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_240() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="9223372036854775807"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("9223372036854775807");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_241() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="9223372036854775808"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("9223372036854775808");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_text_pairwise_242() throws Exception {
        // Combination: receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Document("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).text("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
