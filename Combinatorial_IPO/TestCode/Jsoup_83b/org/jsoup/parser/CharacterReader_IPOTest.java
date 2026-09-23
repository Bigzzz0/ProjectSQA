package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CharacterReader.
 */
public class CharacterReader_IPOTest {
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
    public void test_consumeTo_pairwise_001() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_002() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_003() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_004() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_005() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_006() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_007() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_008() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_009() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_010() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_011() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_012() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_013() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_014() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_015() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_016() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_017() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_018() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_019() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_020() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_021() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", formatValue(actual));
    }

}
