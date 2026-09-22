package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TokenQueue.
 */
public class TokenQueue_IPOTest {
    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_001() throws Exception {
        // Combination: receiver__data="", open='\0', close='\0'
        Object actual = (new TokenQueue("")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_002() throws Exception {
        // Combination: receiver__data=" ", open='a', close='\0'
        Object actual = (new TokenQueue(" ")).chompBalanced('a', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_003() throws Exception {
        // Combination: receiver__data="a", open='0', close='\0'
        Object actual = (new TokenQueue("a")).chompBalanced('0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_004() throws Exception {
        // Combination: receiver__data="test123", open=Character.MIN_VALUE, close='\0'
        Object actual = (new TokenQueue("test123")).chompBalanced(Character.MIN_VALUE, '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_005() throws Exception {
        // Combination: receiver__data="!@#", open=Character.MAX_VALUE, close='\0'
        Object actual = (new TokenQueue("!@#")).chompBalanced(Character.MAX_VALUE, '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_006() throws Exception {
        // Combination: receiver__data=" ", open='\0', close='a'
        Object actual = (new TokenQueue(" ")).chompBalanced('\0', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_007() throws Exception {
        // Combination: receiver__data="", open='a', close='a'
        Object actual = (new TokenQueue("")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_008() throws Exception {
        // Combination: receiver__data="test123", open='0', close='a'
        Object actual = (new TokenQueue("test123")).chompBalanced('0', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_009() throws Exception {
        // Combination: receiver__data="a", open=Character.MIN_VALUE, close='a'
        Object actual = (new TokenQueue("a")).chompBalanced(Character.MIN_VALUE, 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_010() throws Exception {
        // Combination: receiver__data="0", open=Character.MAX_VALUE, close='a'
        Object actual = (new TokenQueue("0")).chompBalanced(Character.MAX_VALUE, 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_011() throws Exception {
        // Combination: receiver__data="a", open='\0', close='0'
        Object actual = (new TokenQueue("a")).chompBalanced('\0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_012() throws Exception {
        // Combination: receiver__data="test123", open='a', close='0'
        Object actual = (new TokenQueue("test123")).chompBalanced('a', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_013() throws Exception {
        // Combination: receiver__data="", open='0', close='0'
        Object actual = (new TokenQueue("")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_014() throws Exception {
        // Combination: receiver__data=" ", open=Character.MIN_VALUE, close='0'
        Object actual = (new TokenQueue(" ")).chompBalanced(Character.MIN_VALUE, '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_015() throws Exception {
        // Combination: receiver__data="-1", open=Character.MAX_VALUE, close='0'
        Object actual = (new TokenQueue("-1")).chompBalanced(Character.MAX_VALUE, '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_016() throws Exception {
        // Combination: receiver__data="test123", open='\0', close=Character.MIN_VALUE
        Object actual = (new TokenQueue("test123")).chompBalanced('\0', Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_017() throws Exception {
        // Combination: receiver__data="a", open='a', close=Character.MIN_VALUE
        Object actual = (new TokenQueue("a")).chompBalanced('a', Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_018() throws Exception {
        // Combination: receiver__data=" ", open='0', close=Character.MIN_VALUE
        Object actual = (new TokenQueue(" ")).chompBalanced('0', Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_019() throws Exception {
        // Combination: receiver__data="", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_020() throws Exception {
        // Combination: receiver__data="1.5", open=Character.MAX_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("1.5")).chompBalanced(Character.MAX_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_021() throws Exception {
        // Combination: receiver__data="!@#", open='\0', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("!@#")).chompBalanced('\0', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_022() throws Exception {
        // Combination: receiver__data="0", open='a', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("0")).chompBalanced('a', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_023() throws Exception {
        // Combination: receiver__data="-1", open='0', close=Character.MAX_VALUE
        Object actual = (new TokenQueue("-1")).chompBalanced('0', Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_024() throws Exception {
        // Combination: receiver__data="1.5", open=Character.MIN_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("1.5")).chompBalanced(Character.MIN_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_025() throws Exception {
        // Combination: receiver__data="", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_026() throws Exception {
        // Combination: receiver__data=" ", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue(" ")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_027() throws Exception {
        // Combination: receiver__data="a", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("a")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_028() throws Exception {
        // Combination: receiver__data="test123", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("test123")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_029() throws Exception {
        // Combination: receiver__data="!@#", open='a', close='a'
        Object actual = (new TokenQueue("!@#")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_030() throws Exception {
        // Combination: receiver__data="!@#", open='0', close='0'
        Object actual = (new TokenQueue("!@#")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_031() throws Exception {
        // Combination: receiver__data="!@#", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("!@#")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_032() throws Exception {
        // Combination: receiver__data="0", open='\0', close='\0'
        Object actual = (new TokenQueue("0")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_033() throws Exception {
        // Combination: receiver__data="0", open='0', close='0'
        Object actual = (new TokenQueue("0")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_034() throws Exception {
        // Combination: receiver__data="0", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("0")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_035() throws Exception {
        // Combination: receiver__data="-1", open='\0', close='\0'
        Object actual = (new TokenQueue("-1")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_036() throws Exception {
        // Combination: receiver__data="-1", open='a', close='a'
        Object actual = (new TokenQueue("-1")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_037() throws Exception {
        // Combination: receiver__data="-1", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("-1")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_038() throws Exception {
        // Combination: receiver__data="1.5", open='\0', close='\0'
        Object actual = (new TokenQueue("1.5")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_039() throws Exception {
        // Combination: receiver__data="1.5", open='a', close='a'
        Object actual = (new TokenQueue("1.5")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_040() throws Exception {
        // Combination: receiver__data="1.5", open='0', close='0'
        Object actual = (new TokenQueue("1.5")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_041() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='\0', close='\0'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_042() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='a', close='a'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_043() throws Exception {
        // Combination: receiver__data="9223372036854775807", open='0', close='0'
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_044() throws Exception {
        // Combination: receiver__data="9223372036854775807", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_045() throws Exception {
        // Combination: receiver__data="9223372036854775807", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("9223372036854775807")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_046() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='\0', close='\0'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_047() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='a', close='a'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_048() throws Exception {
        // Combination: receiver__data="9223372036854775808", open='0', close='0'
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_049() throws Exception {
        // Combination: receiver__data="9223372036854775808", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_050() throws Exception {
        // Combination: receiver__data="9223372036854775808", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("9223372036854775808")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_051() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='\0', close='\0'
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('\0', '\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_052() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='a', close='a'
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('a', 'a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_053() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open='0', close='0'
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced('0', '0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_054() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open=Character.MIN_VALUE, close=Character.MIN_VALUE
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced(Character.MIN_VALUE, Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_chompBalanced_pairwise_055() throws Exception {
        // Combination: receiver__data="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", open=Character.MAX_VALUE, close=Character.MAX_VALUE
        Object actual = (new TokenQueue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).chompBalanced(Character.MAX_VALUE, Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

}
