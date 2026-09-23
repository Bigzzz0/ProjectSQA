package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CharacterReader.
 */
public class CharacterReader_IPOTest {
    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_001() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_002() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_003() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='\0'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('\0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_004() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_005() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_006() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='a'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('a');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_007() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_008() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_009() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c='0'
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo('0');
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_010() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_011() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_012() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c=Character.MIN_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo(Character.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_013() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_014() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeTo_pairwise_015() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), c=Character.MAX_VALUE
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeTo(Character.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_016() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_017() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_018() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), chars=new char[] {}
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeToAny(new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_019() throws Exception {
        // Combination: receiver__input=new java.io.StringReader(""), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader(""))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_020() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a"), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader("a"))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_consumeToAny_pairwise_021() throws Exception {
        // Combination: receiver__input=new java.io.StringReader("a\nb"), chars=new char[] {1}
        Object actual = (new CharacterReader(new java.io.StringReader("a\nb"))).consumeToAny(new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a\nb", String.valueOf(actual));
    }

}
