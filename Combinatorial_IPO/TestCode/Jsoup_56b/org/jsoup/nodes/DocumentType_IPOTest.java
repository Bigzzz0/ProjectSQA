package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DocumentType.
 */
public class DocumentType_IPOTest {
    @Test(timeout = 4000)
    public void test_nodeName_pairwise_001() throws Exception {
        // Combination: receiver__name="", receiver__publicId="", receiver__systemId="", receiver__baseUri=""
        Object actual = (new DocumentType("", "", "", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_002() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId=" ", receiver__systemId=" ", receiver__baseUri=""
        Object actual = (new DocumentType(" ", " ", " ", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_003() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="a", receiver__systemId="a", receiver__baseUri=""
        Object actual = (new DocumentType("a", "a", "a", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_004() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="test123", receiver__systemId="test123", receiver__baseUri=""
        Object actual = (new DocumentType("test123", "test123", "test123", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_005() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="!@#", receiver__systemId="!@#", receiver__baseUri=""
        Object actual = (new DocumentType("!@#", "!@#", "!@#", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_006() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="0", receiver__systemId="0", receiver__baseUri=""
        Object actual = (new DocumentType("0", "0", "0", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_007() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="-1", receiver__systemId="-1", receiver__baseUri=""
        Object actual = (new DocumentType("-1", "-1", "-1", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_008() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="1.5", receiver__systemId="1.5", receiver__baseUri=""
        Object actual = (new DocumentType("1.5", "1.5", "1.5", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_009() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="9223372036854775807", receiver__systemId="9223372036854775807", receiver__baseUri=""
        Object actual = (new DocumentType("9223372036854775807", "9223372036854775807", "9223372036854775807", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_010() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="9223372036854775808", receiver__systemId="9223372036854775808", receiver__baseUri=""
        Object actual = (new DocumentType("9223372036854775808", "9223372036854775808", "9223372036854775808", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_011() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=""
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_012() throws Exception {
        // Combination: receiver__name="", receiver__publicId=" ", receiver__systemId="a", receiver__baseUri=" "
        Object actual = (new DocumentType("", " ", "a", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_013() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="", receiver__systemId="test123", receiver__baseUri=" "
        Object actual = (new DocumentType(" ", "", "test123", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_014() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="test123", receiver__systemId="", receiver__baseUri=" "
        Object actual = (new DocumentType("a", "test123", "", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_015() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="a", receiver__systemId=" ", receiver__baseUri=" "
        Object actual = (new DocumentType("test123", "a", " ", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_016() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="0", receiver__systemId="-1", receiver__baseUri=" "
        Object actual = (new DocumentType("!@#", "0", "-1", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_017() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="!@#", receiver__systemId="1.5", receiver__baseUri=" "
        Object actual = (new DocumentType("0", "!@#", "1.5", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_018() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="1.5", receiver__systemId="!@#", receiver__baseUri=" "
        Object actual = (new DocumentType("-1", "1.5", "!@#", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_019() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="-1", receiver__systemId="0", receiver__baseUri=" "
        Object actual = (new DocumentType("1.5", "-1", "0", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_020() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="9223372036854775808", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri=" "
        Object actual = (new DocumentType("9223372036854775807", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_021() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="9223372036854775807", receiver__systemId="", receiver__baseUri=" "
        Object actual = (new DocumentType("9223372036854775808", "9223372036854775807", "", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_022() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="", receiver__systemId="9223372036854775807", receiver__baseUri=" "
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", "9223372036854775807", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_023() throws Exception {
        // Combination: receiver__name="", receiver__publicId="a", receiver__systemId="test123", receiver__baseUri="a"
        Object actual = (new DocumentType("", "a", "test123", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_024() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="test123", receiver__systemId="a", receiver__baseUri="a"
        Object actual = (new DocumentType(" ", "test123", "a", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_025() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="", receiver__systemId=" ", receiver__baseUri="a"
        Object actual = (new DocumentType("a", "", " ", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_026() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId=" ", receiver__systemId="", receiver__baseUri="a"
        Object actual = (new DocumentType("test123", " ", "", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_027() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="-1", receiver__systemId="1.5", receiver__baseUri="a"
        Object actual = (new DocumentType("!@#", "-1", "1.5", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_028() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="1.5", receiver__systemId="-1", receiver__baseUri="a"
        Object actual = (new DocumentType("0", "1.5", "-1", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_029() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="!@#", receiver__systemId="0", receiver__baseUri="a"
        Object actual = (new DocumentType("-1", "!@#", "0", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_030() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="0", receiver__systemId="!@#", receiver__baseUri="a"
        Object actual = (new DocumentType("1.5", "0", "!@#", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_031() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="9223372036854775808", receiver__baseUri="a"
        Object actual = (new DocumentType("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_032() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="a"
        Object actual = (new DocumentType("9223372036854775808", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_033() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="9223372036854775807", receiver__systemId=" ", receiver__baseUri="a"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", " ", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_034() throws Exception {
        // Combination: receiver__name="", receiver__publicId="test123", receiver__systemId=" ", receiver__baseUri="test123"
        Object actual = (new DocumentType("", "test123", " ", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_035() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="a", receiver__systemId="", receiver__baseUri="test123"
        Object actual = (new DocumentType(" ", "a", "", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_036() throws Exception {
        // Combination: receiver__name="a", receiver__publicId=" ", receiver__systemId="test123", receiver__baseUri="test123"
        Object actual = (new DocumentType("a", " ", "test123", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_037() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="", receiver__systemId="a", receiver__baseUri="test123"
        Object actual = (new DocumentType("test123", "", "a", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_038() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="1.5", receiver__systemId="0", receiver__baseUri="test123"
        Object actual = (new DocumentType("!@#", "1.5", "0", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_039() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="-1", receiver__systemId="!@#", receiver__baseUri="test123"
        Object actual = (new DocumentType("0", "-1", "!@#", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_040() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="0", receiver__systemId="1.5", receiver__baseUri="test123"
        Object actual = (new DocumentType("-1", "0", "1.5", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_041() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="!@#", receiver__systemId="-1", receiver__baseUri="test123"
        Object actual = (new DocumentType("1.5", "!@#", "-1", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_042() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="", receiver__systemId="!@#", receiver__baseUri="test123"
        Object actual = (new DocumentType("9223372036854775807", "", "!@#", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_043() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="9223372036854775807", receiver__baseUri="test123"
        Object actual = (new DocumentType("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_044() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="9223372036854775808", receiver__systemId="", receiver__baseUri="test123"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", "", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_045() throws Exception {
        // Combination: receiver__name="", receiver__publicId="!@#", receiver__systemId="9223372036854775807", receiver__baseUri="!@#"
        Object actual = (new DocumentType("", "!@#", "9223372036854775807", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_046() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="0", receiver__systemId="9223372036854775808", receiver__baseUri="!@#"
        Object actual = (new DocumentType(" ", "0", "9223372036854775808", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_047() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="-1", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="!@#"
        Object actual = (new DocumentType("a", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_048() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="1.5", receiver__systemId="", receiver__baseUri="!@#"
        Object actual = (new DocumentType("test123", "1.5", "", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_049() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="", receiver__systemId=" ", receiver__baseUri="!@#"
        Object actual = (new DocumentType("!@#", "", " ", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_050() throws Exception {
        // Combination: receiver__name="0", receiver__publicId=" ", receiver__systemId="a", receiver__baseUri="!@#"
        Object actual = (new DocumentType("0", " ", "a", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_051() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="a", receiver__systemId="test123", receiver__baseUri="!@#"
        Object actual = (new DocumentType("-1", "a", "test123", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_052() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="test123", receiver__systemId="!@#", receiver__baseUri="!@#"
        Object actual = (new DocumentType("1.5", "test123", "!@#", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_053() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId=" ", receiver__systemId="0", receiver__baseUri="!@#"
        Object actual = (new DocumentType("9223372036854775807", " ", "0", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_054() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId=" ", receiver__systemId="-1", receiver__baseUri="!@#"
        Object actual = (new DocumentType("9223372036854775808", " ", "-1", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_055() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId=" ", receiver__systemId="1.5", receiver__baseUri="!@#"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "1.5", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_056() throws Exception {
        // Combination: receiver__name="", receiver__publicId="0", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="0"
        Object actual = (new DocumentType("", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_057() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="!@#", receiver__systemId="", receiver__baseUri="0"
        Object actual = (new DocumentType(" ", "!@#", "", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_058() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="1.5", receiver__systemId="9223372036854775807", receiver__baseUri="0"
        Object actual = (new DocumentType("a", "1.5", "9223372036854775807", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_059() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="-1", receiver__systemId="9223372036854775808", receiver__baseUri="0"
        Object actual = (new DocumentType("test123", "-1", "9223372036854775808", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_060() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId=" ", receiver__systemId="a", receiver__baseUri="0"
        Object actual = (new DocumentType("!@#", " ", "a", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_061() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="", receiver__systemId=" ", receiver__baseUri="0"
        Object actual = (new DocumentType("0", "", " ", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_062() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="test123", receiver__systemId="0", receiver__baseUri="0"
        Object actual = (new DocumentType("-1", "test123", "0", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_063() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="a", receiver__systemId="test123", receiver__baseUri="0"
        Object actual = (new DocumentType("1.5", "a", "test123", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_064() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="a", receiver__systemId="-1", receiver__baseUri="0"
        Object actual = (new DocumentType("9223372036854775807", "a", "-1", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_065() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="a", receiver__systemId="!@#", receiver__baseUri="0"
        Object actual = (new DocumentType("9223372036854775808", "a", "!@#", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_066() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="a", receiver__systemId="0", receiver__baseUri="0"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "0", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_067() throws Exception {
        // Combination: receiver__name="", receiver__publicId="-1", receiver__systemId="", receiver__baseUri="-1"
        Object actual = (new DocumentType("", "-1", "", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_068() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="1.5", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="-1"
        Object actual = (new DocumentType(" ", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_069() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="!@#", receiver__systemId="9223372036854775808", receiver__baseUri="-1"
        Object actual = (new DocumentType("a", "!@#", "9223372036854775808", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_070() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="0", receiver__systemId="9223372036854775807", receiver__baseUri="-1"
        Object actual = (new DocumentType("test123", "0", "9223372036854775807", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_071() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="a", receiver__systemId="test123", receiver__baseUri="-1"
        Object actual = (new DocumentType("!@#", "a", "test123", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_072() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="test123", receiver__systemId="-1", receiver__baseUri="-1"
        Object actual = (new DocumentType("0", "test123", "-1", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_073() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="", receiver__systemId=" ", receiver__baseUri="-1"
        Object actual = (new DocumentType("-1", "", " ", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_074() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId=" ", receiver__systemId="a", receiver__baseUri="-1"
        Object actual = (new DocumentType("1.5", " ", "a", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_075() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="test123", receiver__systemId="1.5", receiver__baseUri="-1"
        Object actual = (new DocumentType("9223372036854775807", "test123", "1.5", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_076() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="test123", receiver__systemId="0", receiver__baseUri="-1"
        Object actual = (new DocumentType("9223372036854775808", "test123", "0", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_077() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="test123", receiver__systemId="!@#", receiver__baseUri="-1"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "!@#", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_078() throws Exception {
        // Combination: receiver__name="", receiver__publicId="1.5", receiver__systemId="9223372036854775808", receiver__baseUri="1.5"
        Object actual = (new DocumentType("", "1.5", "9223372036854775808", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_079() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="-1", receiver__systemId="9223372036854775807", receiver__baseUri="1.5"
        Object actual = (new DocumentType(" ", "-1", "9223372036854775807", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_080() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="0", receiver__systemId="", receiver__baseUri="1.5"
        Object actual = (new DocumentType("a", "0", "", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_081() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="!@#", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="1.5"
        Object actual = (new DocumentType("test123", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_082() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="test123", receiver__systemId="9223372036854775807", receiver__baseUri="1.5"
        Object actual = (new DocumentType("!@#", "test123", "9223372036854775807", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_083() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="a", receiver__systemId="test123", receiver__baseUri="1.5"
        Object actual = (new DocumentType("0", "a", "test123", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_084() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId=" ", receiver__systemId="a", receiver__baseUri="1.5"
        Object actual = (new DocumentType("-1", " ", "a", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_085() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="", receiver__systemId=" ", receiver__baseUri="1.5"
        Object actual = (new DocumentType("1.5", "", " ", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_086() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="!@#", receiver__systemId=" ", receiver__baseUri="1.5"
        Object actual = (new DocumentType("9223372036854775807", "!@#", " ", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_087() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="!@#", receiver__systemId="a", receiver__baseUri="1.5"
        Object actual = (new DocumentType("9223372036854775808", "!@#", "a", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_088() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="!@#", receiver__systemId="test123", receiver__baseUri="1.5"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "test123", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_089() throws Exception {
        // Combination: receiver__name="", receiver__publicId="9223372036854775807", receiver__systemId="!@#", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("", "9223372036854775807", "!@#", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_090() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="9223372036854775808", receiver__systemId="0", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType(" ", "9223372036854775808", "0", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_091() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="-1", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_092() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="", receiver__systemId="1.5", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("test123", "", "1.5", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_093() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId=" ", receiver__systemId="9223372036854775808", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("!@#", " ", "9223372036854775808", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_094() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="a", receiver__systemId="9223372036854775807", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("0", "a", "9223372036854775807", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_095() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="test123", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("-1", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_096() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="!@#", receiver__systemId="", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("1.5", "!@#", "", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_097() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="0", receiver__systemId="a", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("9223372036854775807", "0", "a", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_098() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="-1", receiver__systemId=" ", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("9223372036854775808", "-1", " ", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_099() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="1.5", receiver__systemId="a", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "a", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_100() throws Exception {
        // Combination: receiver__name="", receiver__publicId="9223372036854775808", receiver__systemId="-1", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("", "9223372036854775808", "-1", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_101() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="9223372036854775807", receiver__systemId="1.5", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType(" ", "9223372036854775807", "1.5", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_102() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="", receiver__systemId="0", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("a", "", "0", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_103() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="!@#", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_104() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId=" ", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("!@#", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_105() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="a", receiver__systemId="9223372036854775808", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("0", "a", "9223372036854775808", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_106() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="test123", receiver__systemId="", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("-1", "test123", "", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_107() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="!@#", receiver__systemId="9223372036854775807", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("1.5", "!@#", "9223372036854775807", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_108() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="-1", receiver__systemId="test123", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("9223372036854775807", "-1", "test123", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_109() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="0", receiver__systemId=" ", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("9223372036854775808", "0", " ", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_110() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="0", receiver__systemId="a", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "a", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_111() throws Exception {
        // Combination: receiver__name="", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="0", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_112() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="", receiver__systemId="-1", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType(" ", "", "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_113() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="9223372036854775807", receiver__systemId="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("a", "9223372036854775807", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_114() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="9223372036854775808", receiver__systemId=" ", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("test123", "9223372036854775808", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_115() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId=" ", receiver__systemId="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("!@#", " ", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_116() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="a", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("0", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_117() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="test123", receiver__systemId="9223372036854775808", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("-1", "test123", "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_118() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="!@#", receiver__systemId="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("1.5", "!@#", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_119() throws Exception {
        // Combination: receiver__name="9223372036854775807", receiver__publicId="1.5", receiver__systemId="", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("9223372036854775807", "1.5", "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_120() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="0", receiver__systemId="test123", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("9223372036854775808", "0", "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_121() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="-1", receiver__systemId="a", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_122() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="1.5", receiver__systemId=" ", receiver__baseUri="9223372036854775808"
        Object actual = (new DocumentType("9223372036854775808", "1.5", " ", "9223372036854775808")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_123() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="9223372036854775807", receiver__systemId="0", receiver__baseUri="test123"
        Object actual = (new DocumentType("test123", "9223372036854775807", "0", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_124() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="9223372036854775807", receiver__systemId="test123", receiver__baseUri="!@#"
        Object actual = (new DocumentType("!@#", "9223372036854775807", "test123", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_125() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="9223372036854775807", receiver__systemId="", receiver__baseUri="0"
        Object actual = (new DocumentType("0", "9223372036854775807", "", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_126() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="9223372036854775807", receiver__systemId="-1", receiver__baseUri="-1"
        Object actual = (new DocumentType("-1", "9223372036854775807", "-1", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_127() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="9223372036854775807", receiver__systemId="9223372036854775808", receiver__baseUri="1.5"
        Object actual = (new DocumentType("1.5", "9223372036854775807", "9223372036854775808", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_128() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="9223372036854775808", receiver__systemId="!@#", receiver__baseUri="a"
        Object actual = (new DocumentType("a", "9223372036854775808", "!@#", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_129() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="9223372036854775808", receiver__systemId="a", receiver__baseUri="!@#"
        Object actual = (new DocumentType("!@#", "9223372036854775808", "a", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_130() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="9223372036854775808", receiver__systemId="1.5", receiver__baseUri="0"
        Object actual = (new DocumentType("0", "9223372036854775808", "1.5", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_131() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="9223372036854775808", receiver__systemId="9223372036854775807", receiver__baseUri="-1"
        Object actual = (new DocumentType("-1", "9223372036854775808", "9223372036854775807", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_132() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="9223372036854775808", receiver__systemId="test123", receiver__baseUri="1.5"
        Object actual = (new DocumentType("1.5", "9223372036854775808", "test123", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_133() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="", receiver__baseUri=" "
        Object actual = (new DocumentType(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_134() throws Exception {
        // Combination: receiver__name="!@#", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId=" ", receiver__baseUri="!@#"
        Object actual = (new DocumentType("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", "!@#")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_135() throws Exception {
        // Combination: receiver__name="0", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="a", receiver__baseUri="0"
        Object actual = (new DocumentType("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", "0")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_136() throws Exception {
        // Combination: receiver__name="-1", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="test123", receiver__baseUri="-1"
        Object actual = (new DocumentType("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", "-1")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_137() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__systemId="1.5", receiver__baseUri="1.5"
        Object actual = (new DocumentType("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_138() throws Exception {
        // Combination: receiver__name="", receiver__publicId="1.5", receiver__systemId="test123", receiver__baseUri="9223372036854775807"
        Object actual = (new DocumentType("", "1.5", "test123", "9223372036854775807")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_139() throws Exception {
        // Combination: receiver__name=" ", receiver__publicId=" ", receiver__systemId="!@#", receiver__baseUri="1.5"
        Object actual = (new DocumentType(" ", " ", "!@#", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_140() throws Exception {
        // Combination: receiver__name="", receiver__publicId="", receiver__systemId="!@#", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("", "", "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_141() throws Exception {
        // Combination: receiver__name="", receiver__publicId="", receiver__systemId="0", receiver__baseUri="1.5"
        Object actual = (new DocumentType("", "", "0", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_142() throws Exception {
        // Combination: receiver__name="test123", receiver__publicId="", receiver__systemId="-1", receiver__baseUri="1.5"
        Object actual = (new DocumentType("test123", "", "-1", "1.5")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_143() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="", receiver__systemId="-1", receiver__baseUri=""
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", "-1", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_144() throws Exception {
        // Combination: receiver__name="", receiver__publicId="a", receiver__systemId="1.5", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("", "a", "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_145() throws Exception {
        // Combination: receiver__name="a", receiver__publicId="", receiver__systemId="1.5", receiver__baseUri=""
        Object actual = (new DocumentType("a", "", "1.5", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_146() throws Exception {
        // Combination: receiver__name="9223372036854775808", receiver__publicId="", receiver__systemId="1.5", receiver__baseUri=""
        Object actual = (new DocumentType("9223372036854775808", "", "1.5", "")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_147() throws Exception {
        // Combination: receiver__name="", receiver__publicId=" ", receiver__systemId="9223372036854775807", receiver__baseUri="a"
        Object actual = (new DocumentType("", " ", "9223372036854775807", "a")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_148() throws Exception {
        // Combination: receiver__name="", receiver__publicId="", receiver__systemId="9223372036854775807", receiver__baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DocumentType("", "", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_149() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__publicId="", receiver__systemId="9223372036854775808", receiver__baseUri=" "
        Object actual = (new DocumentType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", "9223372036854775808", " ")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_150() throws Exception {
        // Combination: receiver__name="", receiver__publicId="", receiver__systemId="9223372036854775808", receiver__baseUri="test123"
        Object actual = (new DocumentType("", "", "9223372036854775808", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nodeName_pairwise_151() throws Exception {
        // Combination: receiver__name="1.5", receiver__publicId="9223372036854775807", receiver__systemId="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__baseUri="test123"
        Object actual = (new DocumentType("1.5", "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).nodeName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("#doctype", String.valueOf(actual));
    }

}
