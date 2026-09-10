package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberUtils.
 */
public class NumberUtils_createNumber__String_IPOTest {
    @Test(timeout = 4000)
    public void test_createNumber_pairwise_001() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_002() throws Exception {
        // Combination: str="1f"
        Object actual = NumberUtils.createNumber("1f");
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_003() throws Exception {
        // Combination: str="1.2e3F"
        Object actual = NumberUtils.createNumber("1.2e3F");
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1200.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_004() throws Exception {
        // Combination: str="FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFd"
        try {
            NumberUtils.createNumber("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFd");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_005() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_006() throws Exception {
        // Combination: str="+123f"
        Object actual = NumberUtils.createNumber("+123f");
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("123.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_007() throws Exception {
        // Combination: str="+99999999999999999999999999999999999.1234"
        Object actual = NumberUtils.createNumber("+99999999999999999999999999999999999.1234");
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0E35", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_008() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_009() throws Exception {
        // Combination: str="+AF"
        try {
            NumberUtils.createNumber("+AF");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_010() throws Exception {
        // Combination: str="+xl"
        try {
            NumberUtils.createNumber("+xl");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_011() throws Exception {
        // Combination: str="-9999999999999999999999999999999999999999F"
        Object actual = NumberUtils.createNumber("-9999999999999999999999999999999999999999F");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0E40", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_012() throws Exception {
        // Combination: str="-12.34d"
        Object actual = NumberUtils.createNumber("-12.34d");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-12.34", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_013() throws Exception {
        // Combination: str="-1"
        Object actual = NumberUtils.createNumber("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_014() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_015() throws Exception {
        // Combination: str="-12x34L"
        try {
            NumberUtils.createNumber("-12x34L");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_016() throws Exception {
        // Combination: str="0x1d"
        Object actual = NumberUtils.createNumber("0x1d");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("29", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_017() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_018() throws Exception {
        // Combination: str="0x99999999999999999999999999999999e9999f"
        Object actual = NumberUtils.createNumber("0x99999999999999999999999999999999e9999f");
        assertNotNull(actual);
        assertEquals("java.math.BigInteger", actual.getClass().getName());
        assertEquals("3425394462494303714539886326678788327323834783", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_019() throws Exception {
        // Combination: str="0x80000000"
        Object actual = NumberUtils.createNumber("0x80000000");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_020() throws Exception {
        // Combination: str="0xabc123abc123abc123abc123abc123abc123abc123abc123Z"
        try {
            NumberUtils.createNumber("0xabc123abc123abc123abc123abc123abc123abc123abc123Z");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_021() throws Exception {
        // Combination: str="-0x1D"
        Object actual = NumberUtils.createNumber("-0x1D");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-29", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_022() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_023() throws Exception {
        // Combination: str="-0x99999999999999999999999999999999e9999L"
        try {
            NumberUtils.createNumber("-0x99999999999999999999999999999999e9999L");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_024() throws Exception {
        // Combination: str="-0x80000000Z"
        try {
            NumberUtils.createNumber("-0x80000000Z");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_025() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_026() throws Exception {
        // Combination: str="#123l"
        try {
            NumberUtils.createNumber("#123l");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_027() throws Exception {
        // Combination: str="#99999999999999999999999999999999999.1234D"
        try {
            NumberUtils.createNumber("#99999999999999999999999999999999999.1234D");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_028() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_029() throws Exception {
        // Combination: str="#AL"
        try {
            NumberUtils.createNumber("#AL");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_030() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_031() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_032() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_033() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_034() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_035() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_036() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_037() throws Exception {
        // Combination: str="+1.2e3D"
        Object actual = NumberUtils.createNumber("+1.2e3D");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1200.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_038() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_039() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_040() throws Exception {
        // Combination: str="99999999999999999999999999999999e9999l"
        try {
            NumberUtils.createNumber("99999999999999999999999999999999e9999l");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_041() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_042() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_043() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_044() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_045() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_046() throws Exception {
        // Combination: str="1Z"
        try {
            NumberUtils.createNumber("1Z");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_047() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_createNumber_pairwise_048() throws Exception {
        // Combination: str=""
        try {
            NumberUtils.createNumber("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
