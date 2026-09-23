package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for LocaleUtils.
 */
public class LocaleUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_001() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_002() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en, ]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_003() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.ROOT
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja, ]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_004() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[, en_US]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_005() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_006() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.US
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja, en_US]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_007() throws Exception {
        // Combination: locale=java.util.Locale.ROOT, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.ROOT, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[, ja_JP]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_008() throws Exception {
        // Combination: locale=java.util.Locale.US, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.US, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[en_US, en, ja_JP]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_localeLookupList_pairwise_009() throws Exception {
        // Combination: locale=java.util.Locale.JAPAN, defaultLocale=java.util.Locale.JAPAN
        Object actual = LocaleUtils.localeLookupList(java.util.Locale.JAPAN, java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[ja_JP, ja]", String.valueOf(actual));
    }

}
