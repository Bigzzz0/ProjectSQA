package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CSVFormat.
 */
public class CSVFormat_IPOTest {
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
    public void test_equals_pairwise_001() throws Exception {
        // Combination: receiver__format="", obj=new Object()
        try {
            (CSVFormat.valueOf("")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: receiver__format=" ", obj=new Object()
        try {
            (CSVFormat.valueOf(" ")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: receiver__format="a", obj=new Object()
        try {
            (CSVFormat.valueOf("a")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: receiver__format="test123", obj=new Object()
        try {
            (CSVFormat.valueOf("test123")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: receiver__format="!@#", obj=new Object()
        try {
            (CSVFormat.valueOf("!@#")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: receiver__format="0", obj=new Object()
        try {
            (CSVFormat.valueOf("0")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: receiver__format="-1", obj=new Object()
        try {
            (CSVFormat.valueOf("-1")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: receiver__format="1.5", obj=new Object()
        try {
            (CSVFormat.valueOf("1.5")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: receiver__format="9223372036854775807", obj=new Object()
        try {
            (CSVFormat.valueOf("9223372036854775807")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: receiver__format="9223372036854775808", obj=new Object()
        try {
            (CSVFormat.valueOf("9223372036854775808")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", obj=new Object()
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__format="", obj="sample_str"
        try {
            (CSVFormat.valueOf("")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__format=" ", obj="sample_str"
        try {
            (CSVFormat.valueOf(" ")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__format="a", obj="sample_str"
        try {
            (CSVFormat.valueOf("a")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__format="test123", obj="sample_str"
        try {
            (CSVFormat.valueOf("test123")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: receiver__format="!@#", obj="sample_str"
        try {
            (CSVFormat.valueOf("!@#")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_017() throws Exception {
        // Combination: receiver__format="0", obj="sample_str"
        try {
            (CSVFormat.valueOf("0")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_018() throws Exception {
        // Combination: receiver__format="-1", obj="sample_str"
        try {
            (CSVFormat.valueOf("-1")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_019() throws Exception {
        // Combination: receiver__format="1.5", obj="sample_str"
        try {
            (CSVFormat.valueOf("1.5")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_020() throws Exception {
        // Combination: receiver__format="9223372036854775807", obj="sample_str"
        try {
            (CSVFormat.valueOf("9223372036854775807")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_021() throws Exception {
        // Combination: receiver__format="9223372036854775808", obj="sample_str"
        try {
            (CSVFormat.valueOf("9223372036854775808")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_022() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", obj="sample_str"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_023() throws Exception {
        // Combination: receiver__format="", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_024() throws Exception {
        // Combination: receiver__format=" ", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf(" ")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_025() throws Exception {
        // Combination: receiver__format="a", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("a")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_026() throws Exception {
        // Combination: receiver__format="test123", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("test123")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_027() throws Exception {
        // Combination: receiver__format="!@#", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("!@#")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_028() throws Exception {
        // Combination: receiver__format="0", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("0")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_029() throws Exception {
        // Combination: receiver__format="-1", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("-1")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_030() throws Exception {
        // Combination: receiver__format="1.5", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("1.5")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_031() throws Exception {
        // Combination: receiver__format="9223372036854775807", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("9223372036854775807")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_032() throws Exception {
        // Combination: receiver__format="9223372036854775808", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("9223372036854775808")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_033() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", obj=Integer.valueOf(1)
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_034() throws Exception {
        // Combination: receiver__format="", values=new Object[] {}
        try {
            (CSVFormat.valueOf("")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_035() throws Exception {
        // Combination: receiver__format="", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_036() throws Exception {
        // Combination: receiver__format=" ", values=new Object[] {}
        try {
            (CSVFormat.valueOf(" ")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_037() throws Exception {
        // Combination: receiver__format=" ", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf(" ")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_038() throws Exception {
        // Combination: receiver__format="a", values=new Object[] {}
        try {
            (CSVFormat.valueOf("a")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_039() throws Exception {
        // Combination: receiver__format="a", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("a")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_040() throws Exception {
        // Combination: receiver__format="test123", values=new Object[] {}
        try {
            (CSVFormat.valueOf("test123")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_041() throws Exception {
        // Combination: receiver__format="test123", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("test123")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_042() throws Exception {
        // Combination: receiver__format="!@#", values=new Object[] {}
        try {
            (CSVFormat.valueOf("!@#")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_043() throws Exception {
        // Combination: receiver__format="!@#", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("!@#")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_044() throws Exception {
        // Combination: receiver__format="0", values=new Object[] {}
        try {
            (CSVFormat.valueOf("0")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_045() throws Exception {
        // Combination: receiver__format="0", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("0")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_046() throws Exception {
        // Combination: receiver__format="-1", values=new Object[] {}
        try {
            (CSVFormat.valueOf("-1")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_047() throws Exception {
        // Combination: receiver__format="-1", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("-1")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_048() throws Exception {
        // Combination: receiver__format="1.5", values=new Object[] {}
        try {
            (CSVFormat.valueOf("1.5")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_049() throws Exception {
        // Combination: receiver__format="1.5", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("1.5")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_050() throws Exception {
        // Combination: receiver__format="9223372036854775807", values=new Object[] {}
        try {
            (CSVFormat.valueOf("9223372036854775807")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_051() throws Exception {
        // Combination: receiver__format="9223372036854775807", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("9223372036854775807")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_052() throws Exception {
        // Combination: receiver__format="9223372036854775808", values=new Object[] {}
        try {
            (CSVFormat.valueOf("9223372036854775808")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_053() throws Exception {
        // Combination: receiver__format="9223372036854775808", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("9223372036854775808")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_054() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", values=new Object[] {}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).format(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_055() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", values=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).format(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_056() throws Exception {
        // Combination: receiver__format="", commentMarker='\0'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_057() throws Exception {
        // Combination: receiver__format=" ", commentMarker='\0'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_058() throws Exception {
        // Combination: receiver__format="a", commentMarker='\0'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_059() throws Exception {
        // Combination: receiver__format="test123", commentMarker='\0'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_060() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='\0'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_061() throws Exception {
        // Combination: receiver__format="0", commentMarker='\0'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_062() throws Exception {
        // Combination: receiver__format="-1", commentMarker='\0'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_063() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='\0'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_064() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_065() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_066() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_067() throws Exception {
        // Combination: receiver__format="", commentMarker='a'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_068() throws Exception {
        // Combination: receiver__format=" ", commentMarker='a'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_069() throws Exception {
        // Combination: receiver__format="a", commentMarker='a'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_070() throws Exception {
        // Combination: receiver__format="test123", commentMarker='a'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_071() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='a'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_072() throws Exception {
        // Combination: receiver__format="0", commentMarker='a'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_073() throws Exception {
        // Combination: receiver__format="-1", commentMarker='a'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_074() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='a'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_075() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_076() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_077() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_078() throws Exception {
        // Combination: receiver__format="", commentMarker='0'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_079() throws Exception {
        // Combination: receiver__format=" ", commentMarker='0'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_080() throws Exception {
        // Combination: receiver__format="a", commentMarker='0'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_081() throws Exception {
        // Combination: receiver__format="test123", commentMarker='0'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_082() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='0'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_083() throws Exception {
        // Combination: receiver__format="0", commentMarker='0'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_084() throws Exception {
        // Combination: receiver__format="-1", commentMarker='0'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_085() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='0'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_086() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_087() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_088() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_089() throws Exception {
        // Combination: receiver__format="", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_090() throws Exception {
        // Combination: receiver__format=" ", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_091() throws Exception {
        // Combination: receiver__format="a", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("a")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_092() throws Exception {
        // Combination: receiver__format="test123", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_093() throws Exception {
        // Combination: receiver__format="!@#", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_094() throws Exception {
        // Combination: receiver__format="0", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("0")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_095() throws Exception {
        // Combination: receiver__format="-1", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_096() throws Exception {
        // Combination: receiver__format="1.5", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_097() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_098() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_099() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_100() throws Exception {
        // Combination: receiver__format="", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_101() throws Exception {
        // Combination: receiver__format=" ", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_102() throws Exception {
        // Combination: receiver__format="a", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_103() throws Exception {
        // Combination: receiver__format="test123", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_104() throws Exception {
        // Combination: receiver__format="!@#", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_105() throws Exception {
        // Combination: receiver__format="0", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_106() throws Exception {
        // Combination: receiver__format="-1", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_107() throws Exception {
        // Combination: receiver__format="1.5", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_108() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_109() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_110() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_111() throws Exception {
        // Combination: receiver__format="", commentMarker='\0'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_112() throws Exception {
        // Combination: receiver__format=" ", commentMarker='\0'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_113() throws Exception {
        // Combination: receiver__format="a", commentMarker='\0'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_114() throws Exception {
        // Combination: receiver__format="test123", commentMarker='\0'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_115() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='\0'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_116() throws Exception {
        // Combination: receiver__format="0", commentMarker='\0'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_117() throws Exception {
        // Combination: receiver__format="-1", commentMarker='\0'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_118() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='\0'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_119() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_120() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_121() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_122() throws Exception {
        // Combination: receiver__format="", commentMarker='a'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_123() throws Exception {
        // Combination: receiver__format=" ", commentMarker='a'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_124() throws Exception {
        // Combination: receiver__format="a", commentMarker='a'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_125() throws Exception {
        // Combination: receiver__format="test123", commentMarker='a'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_126() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='a'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_127() throws Exception {
        // Combination: receiver__format="0", commentMarker='a'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_128() throws Exception {
        // Combination: receiver__format="-1", commentMarker='a'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_129() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='a'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_130() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_131() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_132() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_133() throws Exception {
        // Combination: receiver__format="", commentMarker='0'
        try {
            (CSVFormat.valueOf("")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_134() throws Exception {
        // Combination: receiver__format=" ", commentMarker='0'
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_135() throws Exception {
        // Combination: receiver__format="a", commentMarker='0'
        try {
            (CSVFormat.valueOf("a")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_136() throws Exception {
        // Combination: receiver__format="test123", commentMarker='0'
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_137() throws Exception {
        // Combination: receiver__format="!@#", commentMarker='0'
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_138() throws Exception {
        // Combination: receiver__format="0", commentMarker='0'
        try {
            (CSVFormat.valueOf("0")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_139() throws Exception {
        // Combination: receiver__format="-1", commentMarker='0'
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_140() throws Exception {
        // Combination: receiver__format="1.5", commentMarker='0'
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_141() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_142() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_143() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_144() throws Exception {
        // Combination: receiver__format="", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_145() throws Exception {
        // Combination: receiver__format=" ", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_146() throws Exception {
        // Combination: receiver__format="a", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_147() throws Exception {
        // Combination: receiver__format="test123", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_148() throws Exception {
        // Combination: receiver__format="!@#", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_149() throws Exception {
        // Combination: receiver__format="0", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_150() throws Exception {
        // Combination: receiver__format="-1", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_151() throws Exception {
        // Combination: receiver__format="1.5", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_152() throws Exception {
        // Combination: receiver__format="9223372036854775807", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_153() throws Exception {
        // Combination: receiver__format="9223372036854775808", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withCommentMarker_pairwise_154() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", commentMarker=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withCommentMarker(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_155() throws Exception {
        // Combination: receiver__format="", delimiter='\0'
        try {
            (CSVFormat.valueOf("")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_156() throws Exception {
        // Combination: receiver__format=" ", delimiter='\0'
        try {
            (CSVFormat.valueOf(" ")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_157() throws Exception {
        // Combination: receiver__format="a", delimiter='\0'
        try {
            (CSVFormat.valueOf("a")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_158() throws Exception {
        // Combination: receiver__format="test123", delimiter='\0'
        try {
            (CSVFormat.valueOf("test123")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_159() throws Exception {
        // Combination: receiver__format="!@#", delimiter='\0'
        try {
            (CSVFormat.valueOf("!@#")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_160() throws Exception {
        // Combination: receiver__format="0", delimiter='\0'
        try {
            (CSVFormat.valueOf("0")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_161() throws Exception {
        // Combination: receiver__format="-1", delimiter='\0'
        try {
            (CSVFormat.valueOf("-1")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_162() throws Exception {
        // Combination: receiver__format="1.5", delimiter='\0'
        try {
            (CSVFormat.valueOf("1.5")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_163() throws Exception {
        // Combination: receiver__format="9223372036854775807", delimiter='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_164() throws Exception {
        // Combination: receiver__format="9223372036854775808", delimiter='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_165() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiter='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withDelimiter('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_166() throws Exception {
        // Combination: receiver__format="", delimiter='a'
        try {
            (CSVFormat.valueOf("")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_167() throws Exception {
        // Combination: receiver__format=" ", delimiter='a'
        try {
            (CSVFormat.valueOf(" ")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_168() throws Exception {
        // Combination: receiver__format="a", delimiter='a'
        try {
            (CSVFormat.valueOf("a")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_169() throws Exception {
        // Combination: receiver__format="test123", delimiter='a'
        try {
            (CSVFormat.valueOf("test123")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_170() throws Exception {
        // Combination: receiver__format="!@#", delimiter='a'
        try {
            (CSVFormat.valueOf("!@#")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_171() throws Exception {
        // Combination: receiver__format="0", delimiter='a'
        try {
            (CSVFormat.valueOf("0")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_172() throws Exception {
        // Combination: receiver__format="-1", delimiter='a'
        try {
            (CSVFormat.valueOf("-1")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_173() throws Exception {
        // Combination: receiver__format="1.5", delimiter='a'
        try {
            (CSVFormat.valueOf("1.5")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_174() throws Exception {
        // Combination: receiver__format="9223372036854775807", delimiter='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_175() throws Exception {
        // Combination: receiver__format="9223372036854775808", delimiter='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_176() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiter='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withDelimiter('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_177() throws Exception {
        // Combination: receiver__format="", delimiter='0'
        try {
            (CSVFormat.valueOf("")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_178() throws Exception {
        // Combination: receiver__format=" ", delimiter='0'
        try {
            (CSVFormat.valueOf(" ")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_179() throws Exception {
        // Combination: receiver__format="a", delimiter='0'
        try {
            (CSVFormat.valueOf("a")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_180() throws Exception {
        // Combination: receiver__format="test123", delimiter='0'
        try {
            (CSVFormat.valueOf("test123")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_181() throws Exception {
        // Combination: receiver__format="!@#", delimiter='0'
        try {
            (CSVFormat.valueOf("!@#")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_182() throws Exception {
        // Combination: receiver__format="0", delimiter='0'
        try {
            (CSVFormat.valueOf("0")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_183() throws Exception {
        // Combination: receiver__format="-1", delimiter='0'
        try {
            (CSVFormat.valueOf("-1")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_184() throws Exception {
        // Combination: receiver__format="1.5", delimiter='0'
        try {
            (CSVFormat.valueOf("1.5")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_185() throws Exception {
        // Combination: receiver__format="9223372036854775807", delimiter='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_186() throws Exception {
        // Combination: receiver__format="9223372036854775808", delimiter='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_187() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiter='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withDelimiter('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_188() throws Exception {
        // Combination: receiver__format="", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_189() throws Exception {
        // Combination: receiver__format=" ", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf(" ")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_190() throws Exception {
        // Combination: receiver__format="a", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("a")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_191() throws Exception {
        // Combination: receiver__format="test123", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("test123")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_192() throws Exception {
        // Combination: receiver__format="!@#", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_193() throws Exception {
        // Combination: receiver__format="0", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("0")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_194() throws Exception {
        // Combination: receiver__format="-1", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("-1")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_195() throws Exception {
        // Combination: receiver__format="1.5", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_196() throws Exception {
        // Combination: receiver__format="9223372036854775807", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_197() throws Exception {
        // Combination: receiver__format="9223372036854775808", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_198() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiter=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withDelimiter(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_199() throws Exception {
        // Combination: receiver__format="", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_200() throws Exception {
        // Combination: receiver__format=" ", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_201() throws Exception {
        // Combination: receiver__format="a", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_202() throws Exception {
        // Combination: receiver__format="test123", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_203() throws Exception {
        // Combination: receiver__format="!@#", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_204() throws Exception {
        // Combination: receiver__format="0", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_205() throws Exception {
        // Combination: receiver__format="-1", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_206() throws Exception {
        // Combination: receiver__format="1.5", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_207() throws Exception {
        // Combination: receiver__format="9223372036854775807", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_208() throws Exception {
        // Combination: receiver__format="9223372036854775808", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withDelimiter_pairwise_209() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", delimiter=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withDelimiter(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_210() throws Exception {
        // Combination: receiver__format="", escape='\0'
        try {
            (CSVFormat.valueOf("")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_211() throws Exception {
        // Combination: receiver__format=" ", escape='\0'
        try {
            (CSVFormat.valueOf(" ")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_212() throws Exception {
        // Combination: receiver__format="a", escape='\0'
        try {
            (CSVFormat.valueOf("a")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_213() throws Exception {
        // Combination: receiver__format="test123", escape='\0'
        try {
            (CSVFormat.valueOf("test123")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_214() throws Exception {
        // Combination: receiver__format="!@#", escape='\0'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_215() throws Exception {
        // Combination: receiver__format="0", escape='\0'
        try {
            (CSVFormat.valueOf("0")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_216() throws Exception {
        // Combination: receiver__format="-1", escape='\0'
        try {
            (CSVFormat.valueOf("-1")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_217() throws Exception {
        // Combination: receiver__format="1.5", escape='\0'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_218() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_219() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_220() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_221() throws Exception {
        // Combination: receiver__format="", escape='a'
        try {
            (CSVFormat.valueOf("")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_222() throws Exception {
        // Combination: receiver__format=" ", escape='a'
        try {
            (CSVFormat.valueOf(" ")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_223() throws Exception {
        // Combination: receiver__format="a", escape='a'
        try {
            (CSVFormat.valueOf("a")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_224() throws Exception {
        // Combination: receiver__format="test123", escape='a'
        try {
            (CSVFormat.valueOf("test123")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_225() throws Exception {
        // Combination: receiver__format="!@#", escape='a'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_226() throws Exception {
        // Combination: receiver__format="0", escape='a'
        try {
            (CSVFormat.valueOf("0")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_227() throws Exception {
        // Combination: receiver__format="-1", escape='a'
        try {
            (CSVFormat.valueOf("-1")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_228() throws Exception {
        // Combination: receiver__format="1.5", escape='a'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_229() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_230() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_231() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_232() throws Exception {
        // Combination: receiver__format="", escape='0'
        try {
            (CSVFormat.valueOf("")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_233() throws Exception {
        // Combination: receiver__format=" ", escape='0'
        try {
            (CSVFormat.valueOf(" ")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_234() throws Exception {
        // Combination: receiver__format="a", escape='0'
        try {
            (CSVFormat.valueOf("a")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_235() throws Exception {
        // Combination: receiver__format="test123", escape='0'
        try {
            (CSVFormat.valueOf("test123")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_236() throws Exception {
        // Combination: receiver__format="!@#", escape='0'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_237() throws Exception {
        // Combination: receiver__format="0", escape='0'
        try {
            (CSVFormat.valueOf("0")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_238() throws Exception {
        // Combination: receiver__format="-1", escape='0'
        try {
            (CSVFormat.valueOf("-1")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_239() throws Exception {
        // Combination: receiver__format="1.5", escape='0'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_240() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_241() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_242() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_243() throws Exception {
        // Combination: receiver__format="", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_244() throws Exception {
        // Combination: receiver__format=" ", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf(" ")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_245() throws Exception {
        // Combination: receiver__format="a", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("a")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_246() throws Exception {
        // Combination: receiver__format="test123", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("test123")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_247() throws Exception {
        // Combination: receiver__format="!@#", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_248() throws Exception {
        // Combination: receiver__format="0", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("0")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_249() throws Exception {
        // Combination: receiver__format="-1", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("-1")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_250() throws Exception {
        // Combination: receiver__format="1.5", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_251() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_252() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_253() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_254() throws Exception {
        // Combination: receiver__format="", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_255() throws Exception {
        // Combination: receiver__format=" ", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_256() throws Exception {
        // Combination: receiver__format="a", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_257() throws Exception {
        // Combination: receiver__format="test123", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_258() throws Exception {
        // Combination: receiver__format="!@#", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_259() throws Exception {
        // Combination: receiver__format="0", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_260() throws Exception {
        // Combination: receiver__format="-1", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_261() throws Exception {
        // Combination: receiver__format="1.5", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_262() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_263() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_264() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_265() throws Exception {
        // Combination: receiver__format="", escape='\0'
        try {
            (CSVFormat.valueOf("")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_266() throws Exception {
        // Combination: receiver__format=" ", escape='\0'
        try {
            (CSVFormat.valueOf(" ")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_267() throws Exception {
        // Combination: receiver__format="a", escape='\0'
        try {
            (CSVFormat.valueOf("a")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_268() throws Exception {
        // Combination: receiver__format="test123", escape='\0'
        try {
            (CSVFormat.valueOf("test123")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_269() throws Exception {
        // Combination: receiver__format="!@#", escape='\0'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_270() throws Exception {
        // Combination: receiver__format="0", escape='\0'
        try {
            (CSVFormat.valueOf("0")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_271() throws Exception {
        // Combination: receiver__format="-1", escape='\0'
        try {
            (CSVFormat.valueOf("-1")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_272() throws Exception {
        // Combination: receiver__format="1.5", escape='\0'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_273() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_274() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_275() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_276() throws Exception {
        // Combination: receiver__format="", escape='a'
        try {
            (CSVFormat.valueOf("")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_277() throws Exception {
        // Combination: receiver__format=" ", escape='a'
        try {
            (CSVFormat.valueOf(" ")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_278() throws Exception {
        // Combination: receiver__format="a", escape='a'
        try {
            (CSVFormat.valueOf("a")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_279() throws Exception {
        // Combination: receiver__format="test123", escape='a'
        try {
            (CSVFormat.valueOf("test123")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_280() throws Exception {
        // Combination: receiver__format="!@#", escape='a'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_281() throws Exception {
        // Combination: receiver__format="0", escape='a'
        try {
            (CSVFormat.valueOf("0")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_282() throws Exception {
        // Combination: receiver__format="-1", escape='a'
        try {
            (CSVFormat.valueOf("-1")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_283() throws Exception {
        // Combination: receiver__format="1.5", escape='a'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_284() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_285() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_286() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_287() throws Exception {
        // Combination: receiver__format="", escape='0'
        try {
            (CSVFormat.valueOf("")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_288() throws Exception {
        // Combination: receiver__format=" ", escape='0'
        try {
            (CSVFormat.valueOf(" ")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_289() throws Exception {
        // Combination: receiver__format="a", escape='0'
        try {
            (CSVFormat.valueOf("a")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_290() throws Exception {
        // Combination: receiver__format="test123", escape='0'
        try {
            (CSVFormat.valueOf("test123")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_291() throws Exception {
        // Combination: receiver__format="!@#", escape='0'
        try {
            (CSVFormat.valueOf("!@#")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_292() throws Exception {
        // Combination: receiver__format="0", escape='0'
        try {
            (CSVFormat.valueOf("0")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_293() throws Exception {
        // Combination: receiver__format="-1", escape='0'
        try {
            (CSVFormat.valueOf("-1")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_294() throws Exception {
        // Combination: receiver__format="1.5", escape='0'
        try {
            (CSVFormat.valueOf("1.5")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_295() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_296() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_297() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_298() throws Exception {
        // Combination: receiver__format="", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_299() throws Exception {
        // Combination: receiver__format=" ", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_300() throws Exception {
        // Combination: receiver__format="a", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_301() throws Exception {
        // Combination: receiver__format="test123", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_302() throws Exception {
        // Combination: receiver__format="!@#", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_303() throws Exception {
        // Combination: receiver__format="0", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_304() throws Exception {
        // Combination: receiver__format="-1", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_305() throws Exception {
        // Combination: receiver__format="1.5", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_306() throws Exception {
        // Combination: receiver__format="9223372036854775807", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_307() throws Exception {
        // Combination: receiver__format="9223372036854775808", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withEscape_pairwise_308() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", escape=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withEscape(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_309() throws Exception {
        // Combination: receiver__format="", header=new String[] {}
        try {
            (CSVFormat.valueOf("")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_310() throws Exception {
        // Combination: receiver__format=" ", header=new String[] {}
        try {
            (CSVFormat.valueOf(" ")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_311() throws Exception {
        // Combination: receiver__format="a", header=new String[] {}
        try {
            (CSVFormat.valueOf("a")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_312() throws Exception {
        // Combination: receiver__format="test123", header=new String[] {}
        try {
            (CSVFormat.valueOf("test123")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_313() throws Exception {
        // Combination: receiver__format="!@#", header=new String[] {}
        try {
            (CSVFormat.valueOf("!@#")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_314() throws Exception {
        // Combination: receiver__format="0", header=new String[] {}
        try {
            (CSVFormat.valueOf("0")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_315() throws Exception {
        // Combination: receiver__format="-1", header=new String[] {}
        try {
            (CSVFormat.valueOf("-1")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_316() throws Exception {
        // Combination: receiver__format="1.5", header=new String[] {}
        try {
            (CSVFormat.valueOf("1.5")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_317() throws Exception {
        // Combination: receiver__format="9223372036854775807", header=new String[] {}
        try {
            (CSVFormat.valueOf("9223372036854775807")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_318() throws Exception {
        // Combination: receiver__format="9223372036854775808", header=new String[] {}
        try {
            (CSVFormat.valueOf("9223372036854775808")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_319() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", header=new String[] {}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withHeader(new String[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_320() throws Exception {
        // Combination: receiver__format="", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_321() throws Exception {
        // Combination: receiver__format=" ", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf(" ")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_322() throws Exception {
        // Combination: receiver__format="a", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("a")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_323() throws Exception {
        // Combination: receiver__format="test123", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("test123")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_324() throws Exception {
        // Combination: receiver__format="!@#", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("!@#")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_325() throws Exception {
        // Combination: receiver__format="0", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("0")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_326() throws Exception {
        // Combination: receiver__format="-1", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("-1")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_327() throws Exception {
        // Combination: receiver__format="1.5", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("1.5")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_328() throws Exception {
        // Combination: receiver__format="9223372036854775807", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("9223372036854775807")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_329() throws Exception {
        // Combination: receiver__format="9223372036854775808", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("9223372036854775808")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeader_pairwise_330() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", header=new String[] {"value"}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withHeader(new String[] {"value"});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_331() throws Exception {
        // Combination: receiver__format="", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_332() throws Exception {
        // Combination: receiver__format=" ", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf(" ")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_333() throws Exception {
        // Combination: receiver__format="a", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("a")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_334() throws Exception {
        // Combination: receiver__format="test123", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("test123")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_335() throws Exception {
        // Combination: receiver__format="!@#", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("!@#")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_336() throws Exception {
        // Combination: receiver__format="0", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("0")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_337() throws Exception {
        // Combination: receiver__format="-1", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("-1")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_338() throws Exception {
        // Combination: receiver__format="1.5", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("1.5")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_339() throws Exception {
        // Combination: receiver__format="9223372036854775807", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("9223372036854775807")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_340() throws Exception {
        // Combination: receiver__format="9223372036854775808", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("9223372036854775808")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_341() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", headerComments=new Object[] {}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withHeaderComments(new Object[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_342() throws Exception {
        // Combination: receiver__format="", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_343() throws Exception {
        // Combination: receiver__format=" ", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf(" ")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_344() throws Exception {
        // Combination: receiver__format="a", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("a")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_345() throws Exception {
        // Combination: receiver__format="test123", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("test123")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_346() throws Exception {
        // Combination: receiver__format="!@#", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("!@#")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_347() throws Exception {
        // Combination: receiver__format="0", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("0")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_348() throws Exception {
        // Combination: receiver__format="-1", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("-1")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_349() throws Exception {
        // Combination: receiver__format="1.5", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("1.5")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_350() throws Exception {
        // Combination: receiver__format="9223372036854775807", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("9223372036854775807")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_351() throws Exception {
        // Combination: receiver__format="9223372036854775808", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("9223372036854775808")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withHeaderComments_pairwise_352() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", headerComments=new Object[] {"a", Integer.valueOf(1)}
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withHeaderComments(new Object[] {"a", Integer.valueOf(1)});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_353() throws Exception {
        // Combination: receiver__format="", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_354() throws Exception {
        // Combination: receiver__format=" ", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf(" ")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_355() throws Exception {
        // Combination: receiver__format="a", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("a")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_356() throws Exception {
        // Combination: receiver__format="test123", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("test123")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_357() throws Exception {
        // Combination: receiver__format="!@#", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("!@#")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_358() throws Exception {
        // Combination: receiver__format="0", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("0")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_359() throws Exception {
        // Combination: receiver__format="-1", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("-1")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_360() throws Exception {
        // Combination: receiver__format="1.5", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("1.5")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_361() throws Exception {
        // Combination: receiver__format="9223372036854775807", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("9223372036854775807")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_362() throws Exception {
        // Combination: receiver__format="9223372036854775808", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("9223372036854775808")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_363() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", allowMissingColumnNames=true
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withAllowMissingColumnNames(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_364() throws Exception {
        // Combination: receiver__format="", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_365() throws Exception {
        // Combination: receiver__format=" ", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf(" ")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_366() throws Exception {
        // Combination: receiver__format="a", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("a")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_367() throws Exception {
        // Combination: receiver__format="test123", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("test123")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_368() throws Exception {
        // Combination: receiver__format="!@#", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("!@#")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_369() throws Exception {
        // Combination: receiver__format="0", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("0")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_370() throws Exception {
        // Combination: receiver__format="-1", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("-1")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_371() throws Exception {
        // Combination: receiver__format="1.5", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("1.5")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_372() throws Exception {
        // Combination: receiver__format="9223372036854775807", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("9223372036854775807")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_373() throws Exception {
        // Combination: receiver__format="9223372036854775808", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("9223372036854775808")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withAllowMissingColumnNames_pairwise_374() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", allowMissingColumnNames=false
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withAllowMissingColumnNames(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_375() throws Exception {
        // Combination: receiver__format="", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_376() throws Exception {
        // Combination: receiver__format=" ", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf(" ")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_377() throws Exception {
        // Combination: receiver__format="a", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("a")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_378() throws Exception {
        // Combination: receiver__format="test123", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("test123")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_379() throws Exception {
        // Combination: receiver__format="!@#", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_380() throws Exception {
        // Combination: receiver__format="0", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("0")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_381() throws Exception {
        // Combination: receiver__format="-1", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("-1")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_382() throws Exception {
        // Combination: receiver__format="1.5", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_383() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_384() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_385() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreEmptyLines=true
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreEmptyLines(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_386() throws Exception {
        // Combination: receiver__format="", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_387() throws Exception {
        // Combination: receiver__format=" ", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf(" ")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_388() throws Exception {
        // Combination: receiver__format="a", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("a")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_389() throws Exception {
        // Combination: receiver__format="test123", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("test123")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_390() throws Exception {
        // Combination: receiver__format="!@#", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_391() throws Exception {
        // Combination: receiver__format="0", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("0")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_392() throws Exception {
        // Combination: receiver__format="-1", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("-1")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_393() throws Exception {
        // Combination: receiver__format="1.5", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_394() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_395() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreEmptyLines_pairwise_396() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreEmptyLines=false
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreEmptyLines(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_397() throws Exception {
        // Combination: receiver__format="", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_398() throws Exception {
        // Combination: receiver__format=" ", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf(" ")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_399() throws Exception {
        // Combination: receiver__format="a", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("a")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_400() throws Exception {
        // Combination: receiver__format="test123", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("test123")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_401() throws Exception {
        // Combination: receiver__format="!@#", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_402() throws Exception {
        // Combination: receiver__format="0", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("0")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_403() throws Exception {
        // Combination: receiver__format="-1", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("-1")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_404() throws Exception {
        // Combination: receiver__format="1.5", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_405() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_406() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_407() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreSurroundingSpaces=true
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreSurroundingSpaces(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_408() throws Exception {
        // Combination: receiver__format="", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_409() throws Exception {
        // Combination: receiver__format=" ", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf(" ")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_410() throws Exception {
        // Combination: receiver__format="a", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("a")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_411() throws Exception {
        // Combination: receiver__format="test123", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("test123")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_412() throws Exception {
        // Combination: receiver__format="!@#", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_413() throws Exception {
        // Combination: receiver__format="0", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("0")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_414() throws Exception {
        // Combination: receiver__format="-1", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("-1")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_415() throws Exception {
        // Combination: receiver__format="1.5", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_416() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_417() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreSurroundingSpaces_pairwise_418() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreSurroundingSpaces=false
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreSurroundingSpaces(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_419() throws Exception {
        // Combination: receiver__format="", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_420() throws Exception {
        // Combination: receiver__format=" ", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf(" ")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_421() throws Exception {
        // Combination: receiver__format="a", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("a")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_422() throws Exception {
        // Combination: receiver__format="test123", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("test123")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_423() throws Exception {
        // Combination: receiver__format="!@#", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_424() throws Exception {
        // Combination: receiver__format="0", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("0")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_425() throws Exception {
        // Combination: receiver__format="-1", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("-1")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_426() throws Exception {
        // Combination: receiver__format="1.5", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_427() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_428() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_429() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreHeaderCase=true
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreHeaderCase(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_430() throws Exception {
        // Combination: receiver__format="", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_431() throws Exception {
        // Combination: receiver__format=" ", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf(" ")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_432() throws Exception {
        // Combination: receiver__format="a", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("a")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_433() throws Exception {
        // Combination: receiver__format="test123", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("test123")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_434() throws Exception {
        // Combination: receiver__format="!@#", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("!@#")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_435() throws Exception {
        // Combination: receiver__format="0", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("0")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_436() throws Exception {
        // Combination: receiver__format="-1", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("-1")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_437() throws Exception {
        // Combination: receiver__format="1.5", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("1.5")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_438() throws Exception {
        // Combination: receiver__format="9223372036854775807", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("9223372036854775807")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_439() throws Exception {
        // Combination: receiver__format="9223372036854775808", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("9223372036854775808")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withIgnoreHeaderCase_pairwise_440() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", ignoreHeaderCase=false
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withIgnoreHeaderCase(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_441() throws Exception {
        // Combination: receiver__format="", nullString=""
        try {
            (CSVFormat.valueOf("")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_442() throws Exception {
        // Combination: receiver__format=" ", nullString=""
        try {
            (CSVFormat.valueOf(" ")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_443() throws Exception {
        // Combination: receiver__format="a", nullString=""
        try {
            (CSVFormat.valueOf("a")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_444() throws Exception {
        // Combination: receiver__format="test123", nullString=""
        try {
            (CSVFormat.valueOf("test123")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_445() throws Exception {
        // Combination: receiver__format="!@#", nullString=""
        try {
            (CSVFormat.valueOf("!@#")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_446() throws Exception {
        // Combination: receiver__format="0", nullString=""
        try {
            (CSVFormat.valueOf("0")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_447() throws Exception {
        // Combination: receiver__format="-1", nullString=""
        try {
            (CSVFormat.valueOf("-1")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_448() throws Exception {
        // Combination: receiver__format="1.5", nullString=""
        try {
            (CSVFormat.valueOf("1.5")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_449() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString=""
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_450() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString=""
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_451() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=""
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_452() throws Exception {
        // Combination: receiver__format="", nullString=" "
        try {
            (CSVFormat.valueOf("")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_453() throws Exception {
        // Combination: receiver__format=" ", nullString=" "
        try {
            (CSVFormat.valueOf(" ")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_454() throws Exception {
        // Combination: receiver__format="a", nullString=" "
        try {
            (CSVFormat.valueOf("a")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_455() throws Exception {
        // Combination: receiver__format="test123", nullString=" "
        try {
            (CSVFormat.valueOf("test123")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_456() throws Exception {
        // Combination: receiver__format="!@#", nullString=" "
        try {
            (CSVFormat.valueOf("!@#")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_457() throws Exception {
        // Combination: receiver__format="0", nullString=" "
        try {
            (CSVFormat.valueOf("0")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_458() throws Exception {
        // Combination: receiver__format="-1", nullString=" "
        try {
            (CSVFormat.valueOf("-1")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_459() throws Exception {
        // Combination: receiver__format="1.5", nullString=" "
        try {
            (CSVFormat.valueOf("1.5")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_460() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString=" "
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_461() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString=" "
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_462() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString=" "
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_463() throws Exception {
        // Combination: receiver__format="", nullString="a"
        try {
            (CSVFormat.valueOf("")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_464() throws Exception {
        // Combination: receiver__format=" ", nullString="a"
        try {
            (CSVFormat.valueOf(" ")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_465() throws Exception {
        // Combination: receiver__format="a", nullString="a"
        try {
            (CSVFormat.valueOf("a")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_466() throws Exception {
        // Combination: receiver__format="test123", nullString="a"
        try {
            (CSVFormat.valueOf("test123")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_467() throws Exception {
        // Combination: receiver__format="!@#", nullString="a"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_468() throws Exception {
        // Combination: receiver__format="0", nullString="a"
        try {
            (CSVFormat.valueOf("0")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_469() throws Exception {
        // Combination: receiver__format="-1", nullString="a"
        try {
            (CSVFormat.valueOf("-1")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_470() throws Exception {
        // Combination: receiver__format="1.5", nullString="a"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_471() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="a"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_472() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="a"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_473() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="a"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_474() throws Exception {
        // Combination: receiver__format="", nullString="test123"
        try {
            (CSVFormat.valueOf("")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_475() throws Exception {
        // Combination: receiver__format=" ", nullString="test123"
        try {
            (CSVFormat.valueOf(" ")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_476() throws Exception {
        // Combination: receiver__format="a", nullString="test123"
        try {
            (CSVFormat.valueOf("a")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_477() throws Exception {
        // Combination: receiver__format="test123", nullString="test123"
        try {
            (CSVFormat.valueOf("test123")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_478() throws Exception {
        // Combination: receiver__format="!@#", nullString="test123"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_479() throws Exception {
        // Combination: receiver__format="0", nullString="test123"
        try {
            (CSVFormat.valueOf("0")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_480() throws Exception {
        // Combination: receiver__format="-1", nullString="test123"
        try {
            (CSVFormat.valueOf("-1")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_481() throws Exception {
        // Combination: receiver__format="1.5", nullString="test123"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_482() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="test123"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_483() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="test123"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_484() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="test123"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_485() throws Exception {
        // Combination: receiver__format="", nullString="!@#"
        try {
            (CSVFormat.valueOf("")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_486() throws Exception {
        // Combination: receiver__format=" ", nullString="!@#"
        try {
            (CSVFormat.valueOf(" ")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_487() throws Exception {
        // Combination: receiver__format="a", nullString="!@#"
        try {
            (CSVFormat.valueOf("a")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_488() throws Exception {
        // Combination: receiver__format="test123", nullString="!@#"
        try {
            (CSVFormat.valueOf("test123")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_489() throws Exception {
        // Combination: receiver__format="!@#", nullString="!@#"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_490() throws Exception {
        // Combination: receiver__format="0", nullString="!@#"
        try {
            (CSVFormat.valueOf("0")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_491() throws Exception {
        // Combination: receiver__format="-1", nullString="!@#"
        try {
            (CSVFormat.valueOf("-1")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_492() throws Exception {
        // Combination: receiver__format="1.5", nullString="!@#"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_493() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="!@#"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_494() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="!@#"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_495() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="!@#"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_496() throws Exception {
        // Combination: receiver__format="", nullString="0"
        try {
            (CSVFormat.valueOf("")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_497() throws Exception {
        // Combination: receiver__format=" ", nullString="0"
        try {
            (CSVFormat.valueOf(" ")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_498() throws Exception {
        // Combination: receiver__format="a", nullString="0"
        try {
            (CSVFormat.valueOf("a")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_499() throws Exception {
        // Combination: receiver__format="test123", nullString="0"
        try {
            (CSVFormat.valueOf("test123")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_500() throws Exception {
        // Combination: receiver__format="!@#", nullString="0"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_501() throws Exception {
        // Combination: receiver__format="0", nullString="0"
        try {
            (CSVFormat.valueOf("0")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_502() throws Exception {
        // Combination: receiver__format="-1", nullString="0"
        try {
            (CSVFormat.valueOf("-1")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_503() throws Exception {
        // Combination: receiver__format="1.5", nullString="0"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_504() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="0"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_505() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="0"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_506() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="0"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_507() throws Exception {
        // Combination: receiver__format="", nullString="-1"
        try {
            (CSVFormat.valueOf("")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_508() throws Exception {
        // Combination: receiver__format=" ", nullString="-1"
        try {
            (CSVFormat.valueOf(" ")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_509() throws Exception {
        // Combination: receiver__format="a", nullString="-1"
        try {
            (CSVFormat.valueOf("a")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_510() throws Exception {
        // Combination: receiver__format="test123", nullString="-1"
        try {
            (CSVFormat.valueOf("test123")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_511() throws Exception {
        // Combination: receiver__format="!@#", nullString="-1"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_512() throws Exception {
        // Combination: receiver__format="0", nullString="-1"
        try {
            (CSVFormat.valueOf("0")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_513() throws Exception {
        // Combination: receiver__format="-1", nullString="-1"
        try {
            (CSVFormat.valueOf("-1")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_514() throws Exception {
        // Combination: receiver__format="1.5", nullString="-1"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_515() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="-1"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_516() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="-1"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_517() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="-1"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_518() throws Exception {
        // Combination: receiver__format="", nullString="1.5"
        try {
            (CSVFormat.valueOf("")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_519() throws Exception {
        // Combination: receiver__format=" ", nullString="1.5"
        try {
            (CSVFormat.valueOf(" ")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_520() throws Exception {
        // Combination: receiver__format="a", nullString="1.5"
        try {
            (CSVFormat.valueOf("a")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_521() throws Exception {
        // Combination: receiver__format="test123", nullString="1.5"
        try {
            (CSVFormat.valueOf("test123")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_522() throws Exception {
        // Combination: receiver__format="!@#", nullString="1.5"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_523() throws Exception {
        // Combination: receiver__format="0", nullString="1.5"
        try {
            (CSVFormat.valueOf("0")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_524() throws Exception {
        // Combination: receiver__format="-1", nullString="1.5"
        try {
            (CSVFormat.valueOf("-1")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_525() throws Exception {
        // Combination: receiver__format="1.5", nullString="1.5"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_526() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="1.5"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_527() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="1.5"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_528() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="1.5"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_529() throws Exception {
        // Combination: receiver__format="", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_530() throws Exception {
        // Combination: receiver__format=" ", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf(" ")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_531() throws Exception {
        // Combination: receiver__format="a", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("a")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_532() throws Exception {
        // Combination: receiver__format="test123", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("test123")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_533() throws Exception {
        // Combination: receiver__format="!@#", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_534() throws Exception {
        // Combination: receiver__format="0", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("0")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_535() throws Exception {
        // Combination: receiver__format="-1", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("-1")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_536() throws Exception {
        // Combination: receiver__format="1.5", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_537() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_538() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_539() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775807"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_540() throws Exception {
        // Combination: receiver__format="", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_541() throws Exception {
        // Combination: receiver__format=" ", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf(" ")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_542() throws Exception {
        // Combination: receiver__format="a", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("a")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_543() throws Exception {
        // Combination: receiver__format="test123", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("test123")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_544() throws Exception {
        // Combination: receiver__format="!@#", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_545() throws Exception {
        // Combination: receiver__format="0", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("0")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_546() throws Exception {
        // Combination: receiver__format="-1", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("-1")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_547() throws Exception {
        // Combination: receiver__format="1.5", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_548() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_549() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_550() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="9223372036854775808"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_551() throws Exception {
        // Combination: receiver__format="", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_552() throws Exception {
        // Combination: receiver__format=" ", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf(" ")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_553() throws Exception {
        // Combination: receiver__format="a", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("a")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_554() throws Exception {
        // Combination: receiver__format="test123", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("test123")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_555() throws Exception {
        // Combination: receiver__format="!@#", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("!@#")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_556() throws Exception {
        // Combination: receiver__format="0", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("0")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_557() throws Exception {
        // Combination: receiver__format="-1", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("-1")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_558() throws Exception {
        // Combination: receiver__format="1.5", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("1.5")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_559() throws Exception {
        // Combination: receiver__format="9223372036854775807", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_560() throws Exception {
        // Combination: receiver__format="9223372036854775808", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withNullString_pairwise_561() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", nullString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withNullString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_562() throws Exception {
        // Combination: receiver__format="", quoteChar='\0'
        try {
            (CSVFormat.valueOf("")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_563() throws Exception {
        // Combination: receiver__format=" ", quoteChar='\0'
        try {
            (CSVFormat.valueOf(" ")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_564() throws Exception {
        // Combination: receiver__format="a", quoteChar='\0'
        try {
            (CSVFormat.valueOf("a")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_565() throws Exception {
        // Combination: receiver__format="test123", quoteChar='\0'
        try {
            (CSVFormat.valueOf("test123")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_566() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='\0'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_567() throws Exception {
        // Combination: receiver__format="0", quoteChar='\0'
        try {
            (CSVFormat.valueOf("0")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_568() throws Exception {
        // Combination: receiver__format="-1", quoteChar='\0'
        try {
            (CSVFormat.valueOf("-1")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_569() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='\0'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_570() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_571() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_572() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_573() throws Exception {
        // Combination: receiver__format="", quoteChar='a'
        try {
            (CSVFormat.valueOf("")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_574() throws Exception {
        // Combination: receiver__format=" ", quoteChar='a'
        try {
            (CSVFormat.valueOf(" ")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_575() throws Exception {
        // Combination: receiver__format="a", quoteChar='a'
        try {
            (CSVFormat.valueOf("a")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_576() throws Exception {
        // Combination: receiver__format="test123", quoteChar='a'
        try {
            (CSVFormat.valueOf("test123")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_577() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='a'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_578() throws Exception {
        // Combination: receiver__format="0", quoteChar='a'
        try {
            (CSVFormat.valueOf("0")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_579() throws Exception {
        // Combination: receiver__format="-1", quoteChar='a'
        try {
            (CSVFormat.valueOf("-1")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_580() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='a'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_581() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_582() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_583() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_584() throws Exception {
        // Combination: receiver__format="", quoteChar='0'
        try {
            (CSVFormat.valueOf("")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_585() throws Exception {
        // Combination: receiver__format=" ", quoteChar='0'
        try {
            (CSVFormat.valueOf(" ")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_586() throws Exception {
        // Combination: receiver__format="a", quoteChar='0'
        try {
            (CSVFormat.valueOf("a")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_587() throws Exception {
        // Combination: receiver__format="test123", quoteChar='0'
        try {
            (CSVFormat.valueOf("test123")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_588() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='0'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_589() throws Exception {
        // Combination: receiver__format="0", quoteChar='0'
        try {
            (CSVFormat.valueOf("0")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_590() throws Exception {
        // Combination: receiver__format="-1", quoteChar='0'
        try {
            (CSVFormat.valueOf("-1")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_591() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='0'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_592() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_593() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_594() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_595() throws Exception {
        // Combination: receiver__format="", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_596() throws Exception {
        // Combination: receiver__format=" ", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf(" ")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_597() throws Exception {
        // Combination: receiver__format="a", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("a")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_598() throws Exception {
        // Combination: receiver__format="test123", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("test123")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_599() throws Exception {
        // Combination: receiver__format="!@#", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_600() throws Exception {
        // Combination: receiver__format="0", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("0")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_601() throws Exception {
        // Combination: receiver__format="-1", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("-1")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_602() throws Exception {
        // Combination: receiver__format="1.5", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_603() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_604() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_605() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_606() throws Exception {
        // Combination: receiver__format="", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_607() throws Exception {
        // Combination: receiver__format=" ", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_608() throws Exception {
        // Combination: receiver__format="a", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_609() throws Exception {
        // Combination: receiver__format="test123", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_610() throws Exception {
        // Combination: receiver__format="!@#", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_611() throws Exception {
        // Combination: receiver__format="0", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_612() throws Exception {
        // Combination: receiver__format="-1", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_613() throws Exception {
        // Combination: receiver__format="1.5", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_614() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_615() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_616() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_617() throws Exception {
        // Combination: receiver__format="", quoteChar='\0'
        try {
            (CSVFormat.valueOf("")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_618() throws Exception {
        // Combination: receiver__format=" ", quoteChar='\0'
        try {
            (CSVFormat.valueOf(" ")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_619() throws Exception {
        // Combination: receiver__format="a", quoteChar='\0'
        try {
            (CSVFormat.valueOf("a")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_620() throws Exception {
        // Combination: receiver__format="test123", quoteChar='\0'
        try {
            (CSVFormat.valueOf("test123")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_621() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='\0'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_622() throws Exception {
        // Combination: receiver__format="0", quoteChar='\0'
        try {
            (CSVFormat.valueOf("0")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_623() throws Exception {
        // Combination: receiver__format="-1", quoteChar='\0'
        try {
            (CSVFormat.valueOf("-1")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_624() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='\0'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_625() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_626() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_627() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_628() throws Exception {
        // Combination: receiver__format="", quoteChar='a'
        try {
            (CSVFormat.valueOf("")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_629() throws Exception {
        // Combination: receiver__format=" ", quoteChar='a'
        try {
            (CSVFormat.valueOf(" ")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_630() throws Exception {
        // Combination: receiver__format="a", quoteChar='a'
        try {
            (CSVFormat.valueOf("a")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_631() throws Exception {
        // Combination: receiver__format="test123", quoteChar='a'
        try {
            (CSVFormat.valueOf("test123")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_632() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='a'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_633() throws Exception {
        // Combination: receiver__format="0", quoteChar='a'
        try {
            (CSVFormat.valueOf("0")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_634() throws Exception {
        // Combination: receiver__format="-1", quoteChar='a'
        try {
            (CSVFormat.valueOf("-1")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_635() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='a'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_636() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_637() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_638() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_639() throws Exception {
        // Combination: receiver__format="", quoteChar='0'
        try {
            (CSVFormat.valueOf("")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_640() throws Exception {
        // Combination: receiver__format=" ", quoteChar='0'
        try {
            (CSVFormat.valueOf(" ")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_641() throws Exception {
        // Combination: receiver__format="a", quoteChar='0'
        try {
            (CSVFormat.valueOf("a")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_642() throws Exception {
        // Combination: receiver__format="test123", quoteChar='0'
        try {
            (CSVFormat.valueOf("test123")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_643() throws Exception {
        // Combination: receiver__format="!@#", quoteChar='0'
        try {
            (CSVFormat.valueOf("!@#")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_644() throws Exception {
        // Combination: receiver__format="0", quoteChar='0'
        try {
            (CSVFormat.valueOf("0")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_645() throws Exception {
        // Combination: receiver__format="-1", quoteChar='0'
        try {
            (CSVFormat.valueOf("-1")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_646() throws Exception {
        // Combination: receiver__format="1.5", quoteChar='0'
        try {
            (CSVFormat.valueOf("1.5")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_647() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_648() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_649() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_650() throws Exception {
        // Combination: receiver__format="", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_651() throws Exception {
        // Combination: receiver__format=" ", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_652() throws Exception {
        // Combination: receiver__format="a", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_653() throws Exception {
        // Combination: receiver__format="test123", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_654() throws Exception {
        // Combination: receiver__format="!@#", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_655() throws Exception {
        // Combination: receiver__format="0", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_656() throws Exception {
        // Combination: receiver__format="-1", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_657() throws Exception {
        // Combination: receiver__format="1.5", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_658() throws Exception {
        // Combination: receiver__format="9223372036854775807", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_659() throws Exception {
        // Combination: receiver__format="9223372036854775808", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withQuote_pairwise_660() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", quoteChar=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withQuote(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_661() throws Exception {
        // Combination: receiver__format="", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_662() throws Exception {
        // Combination: receiver__format="", recordSeparator='a'
        try {
            (CSVFormat.valueOf("")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_663() throws Exception {
        // Combination: receiver__format="", recordSeparator='0'
        try {
            (CSVFormat.valueOf("")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_664() throws Exception {
        // Combination: receiver__format="", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_665() throws Exception {
        // Combination: receiver__format="", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_666() throws Exception {
        // Combination: receiver__format=" ", recordSeparator='\0'
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_667() throws Exception {
        // Combination: receiver__format=" ", recordSeparator='a'
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_668() throws Exception {
        // Combination: receiver__format=" ", recordSeparator='0'
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_669() throws Exception {
        // Combination: receiver__format=" ", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_670() throws Exception {
        // Combination: receiver__format=" ", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_671() throws Exception {
        // Combination: receiver__format="a", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_672() throws Exception {
        // Combination: receiver__format="a", recordSeparator='a'
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_673() throws Exception {
        // Combination: receiver__format="a", recordSeparator='0'
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_674() throws Exception {
        // Combination: receiver__format="a", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_675() throws Exception {
        // Combination: receiver__format="a", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_676() throws Exception {
        // Combination: receiver__format="test123", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_677() throws Exception {
        // Combination: receiver__format="test123", recordSeparator='a'
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_678() throws Exception {
        // Combination: receiver__format="test123", recordSeparator='0'
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_679() throws Exception {
        // Combination: receiver__format="test123", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_680() throws Exception {
        // Combination: receiver__format="test123", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_681() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_682() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator='a'
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_683() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator='0'
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_684() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_685() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_686() throws Exception {
        // Combination: receiver__format="0", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_687() throws Exception {
        // Combination: receiver__format="0", recordSeparator='a'
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_688() throws Exception {
        // Combination: receiver__format="0", recordSeparator='0'
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_689() throws Exception {
        // Combination: receiver__format="0", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_690() throws Exception {
        // Combination: receiver__format="0", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_691() throws Exception {
        // Combination: receiver__format="-1", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_692() throws Exception {
        // Combination: receiver__format="-1", recordSeparator='a'
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_693() throws Exception {
        // Combination: receiver__format="-1", recordSeparator='0'
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_694() throws Exception {
        // Combination: receiver__format="-1", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_695() throws Exception {
        // Combination: receiver__format="-1", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_696() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_697() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator='a'
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_698() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator='0'
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_699() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_700() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_701() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_702() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator='a'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_703() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator='0'
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_704() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_705() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_706() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_707() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator='a'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_708() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator='0'
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_709() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_710() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_711() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator='\0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator('\0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_712() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator='a'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator('a');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_713() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator='0'
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator('0');
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_714() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator=Character.MIN_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator(Character.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_715() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator=Character.MAX_VALUE
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator(Character.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_716() throws Exception {
        // Combination: receiver__format="", recordSeparator=""
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_717() throws Exception {
        // Combination: receiver__format="", recordSeparator=" "
        try {
            (CSVFormat.valueOf("")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_718() throws Exception {
        // Combination: receiver__format="", recordSeparator="a"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_719() throws Exception {
        // Combination: receiver__format="", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_720() throws Exception {
        // Combination: receiver__format="", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_721() throws Exception {
        // Combination: receiver__format="", recordSeparator="0"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_722() throws Exception {
        // Combination: receiver__format="", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_723() throws Exception {
        // Combination: receiver__format="", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_724() throws Exception {
        // Combination: receiver__format="", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_725() throws Exception {
        // Combination: receiver__format="", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_726() throws Exception {
        // Combination: receiver__format="", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_727() throws Exception {
        // Combination: receiver__format=" ", recordSeparator=""
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_728() throws Exception {
        // Combination: receiver__format=" ", recordSeparator=" "
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_729() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="a"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_730() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="test123"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_731() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_732() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="0"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_733() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="-1"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_734() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_735() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_736() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_737() throws Exception {
        // Combination: receiver__format=" ", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf(" ")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_738() throws Exception {
        // Combination: receiver__format="a", recordSeparator=""
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_739() throws Exception {
        // Combination: receiver__format="a", recordSeparator=" "
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_740() throws Exception {
        // Combination: receiver__format="a", recordSeparator="a"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_741() throws Exception {
        // Combination: receiver__format="a", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_742() throws Exception {
        // Combination: receiver__format="a", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_743() throws Exception {
        // Combination: receiver__format="a", recordSeparator="0"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_744() throws Exception {
        // Combination: receiver__format="a", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_745() throws Exception {
        // Combination: receiver__format="a", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_746() throws Exception {
        // Combination: receiver__format="a", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_747() throws Exception {
        // Combination: receiver__format="a", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_748() throws Exception {
        // Combination: receiver__format="a", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("a")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_749() throws Exception {
        // Combination: receiver__format="test123", recordSeparator=""
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_750() throws Exception {
        // Combination: receiver__format="test123", recordSeparator=" "
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_751() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="a"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_752() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_753() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_754() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="0"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_755() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_756() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_757() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_758() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_759() throws Exception {
        // Combination: receiver__format="test123", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("test123")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_760() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator=""
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_761() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator=" "
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_762() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="a"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_763() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_764() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_765() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="0"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_766() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_767() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_768() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_769() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_770() throws Exception {
        // Combination: receiver__format="!@#", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("!@#")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_771() throws Exception {
        // Combination: receiver__format="0", recordSeparator=""
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_772() throws Exception {
        // Combination: receiver__format="0", recordSeparator=" "
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_773() throws Exception {
        // Combination: receiver__format="0", recordSeparator="a"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_774() throws Exception {
        // Combination: receiver__format="0", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_775() throws Exception {
        // Combination: receiver__format="0", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_776() throws Exception {
        // Combination: receiver__format="0", recordSeparator="0"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_777() throws Exception {
        // Combination: receiver__format="0", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_778() throws Exception {
        // Combination: receiver__format="0", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_779() throws Exception {
        // Combination: receiver__format="0", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_780() throws Exception {
        // Combination: receiver__format="0", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_781() throws Exception {
        // Combination: receiver__format="0", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("0")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_782() throws Exception {
        // Combination: receiver__format="-1", recordSeparator=""
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_783() throws Exception {
        // Combination: receiver__format="-1", recordSeparator=" "
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_784() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="a"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_785() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_786() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_787() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="0"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_788() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_789() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_790() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_791() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_792() throws Exception {
        // Combination: receiver__format="-1", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("-1")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_793() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator=""
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_794() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator=" "
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_795() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="a"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_796() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_797() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_798() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="0"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_799() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_800() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_801() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_802() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_803() throws Exception {
        // Combination: receiver__format="1.5", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("1.5")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_804() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator=""
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_805() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator=" "
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_806() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="a"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_807() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_808() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_809() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="0"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_810() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_811() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_812() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_813() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_814() throws Exception {
        // Combination: receiver__format="9223372036854775807", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("9223372036854775807")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_815() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator=""
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_816() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator=" "
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_817() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="a"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_818() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_819() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_820() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="0"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_821() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_822() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_823() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_824() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_825() throws Exception {
        // Combination: receiver__format="9223372036854775808", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("9223372036854775808")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_826() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator=""
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_827() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator=" "
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_828() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="a"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_829() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="test123"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_830() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="!@#"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_831() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="0"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_832() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="-1"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_833() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="1.5"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_834() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="9223372036854775807"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_835() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="9223372036854775808"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withRecordSeparator_pairwise_836() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", recordSeparator="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withRecordSeparator("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_837() throws Exception {
        // Combination: receiver__format="", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_838() throws Exception {
        // Combination: receiver__format="", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_839() throws Exception {
        // Combination: receiver__format=" ", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf(" ")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_840() throws Exception {
        // Combination: receiver__format=" ", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf(" ")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_841() throws Exception {
        // Combination: receiver__format="a", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("a")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_842() throws Exception {
        // Combination: receiver__format="a", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("a")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_843() throws Exception {
        // Combination: receiver__format="test123", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("test123")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_844() throws Exception {
        // Combination: receiver__format="test123", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("test123")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_845() throws Exception {
        // Combination: receiver__format="!@#", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("!@#")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_846() throws Exception {
        // Combination: receiver__format="!@#", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("!@#")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_847() throws Exception {
        // Combination: receiver__format="0", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("0")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_848() throws Exception {
        // Combination: receiver__format="0", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("0")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_849() throws Exception {
        // Combination: receiver__format="-1", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("-1")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_850() throws Exception {
        // Combination: receiver__format="-1", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("-1")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_851() throws Exception {
        // Combination: receiver__format="1.5", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("1.5")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_852() throws Exception {
        // Combination: receiver__format="1.5", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("1.5")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_853() throws Exception {
        // Combination: receiver__format="9223372036854775807", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("9223372036854775807")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_854() throws Exception {
        // Combination: receiver__format="9223372036854775807", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("9223372036854775807")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_855() throws Exception {
        // Combination: receiver__format="9223372036854775808", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("9223372036854775808")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_856() throws Exception {
        // Combination: receiver__format="9223372036854775808", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("9223372036854775808")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_857() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", skipHeaderRecord=true
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withSkipHeaderRecord(true);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withSkipHeaderRecord_pairwise_858() throws Exception {
        // Combination: receiver__format="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", skipHeaderRecord=false
        try {
            (CSVFormat.valueOf("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).withSkipHeaderRecord(false);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
