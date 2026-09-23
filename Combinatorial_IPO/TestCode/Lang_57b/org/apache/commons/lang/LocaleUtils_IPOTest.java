package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for LocaleUtils.
 */
public class LocaleUtils_IPOTest {
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
    public void test_localeLookupList_pairwise_001() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_002() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en, ]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_003() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja, ]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_004() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[, en_US]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_005() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_006() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja, en_US]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_007() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[, ja_JP]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_008() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en, ja_JP]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_009() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja]", formatValue(actual));
    }

}
