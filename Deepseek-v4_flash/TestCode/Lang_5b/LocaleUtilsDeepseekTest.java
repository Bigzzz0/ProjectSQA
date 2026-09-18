package org.apache.commons.lang3;

import org.junit.Test;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - toLocale: valid 2-char, 5-char, 7+ char, variant with underscore
 *   - localeLookupList: null, non-null with variant/country/language only, defaultLocale not in list
 *   - availableLocaleList/Set: returns unmodifiable, contains known locales
 *   - isAvailableLocale: true for known, false for unknown
 *   - languagesByCountry: null, existing country, non-existing country, cache hit
 *   - countriesByLanguage: null, existing language, non-existing language, cache hit
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - toLocale: null, empty string, length 1, length 3, length 4, length 6, invalid separators, invalid case
 *   - localeLookupList: null locale, null defaultLocale (via single-arg)
 *   - languagesByCountry: null, empty string, non-existent code
 *   - countriesByLanguage: null, empty string, non-existent code
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - toLocale("_GB") should return Locale("", "GB") but currently throws IllegalArgumentException (Defects4J testLang865)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - toLocale: invalid formats (too short, wrong separators, wrong case)
 *   - localeLookupList: null locale returns empty list
 *   - languagesByCountry: null returns empty list
 *   - countriesByLanguage: null returns empty list
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - availableLocaleList/Set: unmodifiable, consistent
 *   - Cache behavior: second call returns same list (identity check)
 */
public class LocaleUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testToLocale_Valid2Char() {
        Locale result = LocaleUtils.toLocale("en");
        assertEquals("en", result.getLanguage());
        assertEquals("", result.getCountry());
        assertEquals("", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocale_Valid5Char() {
        Locale result = LocaleUtils.toLocale("en_GB");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocale_Valid7PlusChar() {
        Locale result = LocaleUtils.toLocale("en_GB_xxx");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("xxx", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocale_VariantWithUnderscore() {
        // ch3 == '_' case: e.g., "en__xxx"
        Locale result = LocaleUtils.toLocale("en__xxx");
        assertEquals("en", result.getLanguage());
        assertEquals("", result.getCountry());
        assertEquals("xxx", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_Null() {
        List<Locale> list = LocaleUtils.localeLookupList(null);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_WithVariant() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(locale, list.get(3)); // defaultLocale = locale
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_WithCountryNoVariant() {
        Locale locale = new Locale("de", "DE");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("de", ""), list.get(1));
        assertEquals(locale, list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_LanguageOnly() {
        Locale locale = new Locale("en");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(2, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(locale, list.get(1));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_DefaultLocaleNotInList() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = new Locale("en");
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
        assertEquals(defaultLocale, list.get(2)); // defaultLocale added at end
        // Note: order: locale, then country stripped, then defaultLocale (since list doesn't contain it)
        // Actually the code adds defaultLocale only if not already present. Since locale is "fr_CA", list contains "fr_CA" and "fr", but not "en". So defaultLocale is added.
        // But the code adds defaultLocale after the language-only locale? Let's check: after adding language-only, it checks contains(defaultLocale) and adds. So defaultLocale becomes last.
        assertEquals(defaultLocale, list.get(2));
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleList_NotEmpty() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
        assertTrue(list.contains(Locale.US));
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleSet_NotEmpty() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertTrue(set.size() > 0);
        assertTrue(set.contains(Locale.US));
    }

    @Test(timeout = 4000)
    public void testIsAvailableLocale_True() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
    }

    @Test(timeout = 4000)
    public void testIsAvailableLocale_False() {
        Locale fake = new Locale("xx", "XX");
        assertFalse(LocaleUtils.isAvailableLocale(fake));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_Existing() {
        List<Locale> langs = LocaleUtils.languagesByCountry("US");
        assertNotNull(langs);
        assertTrue(langs.contains(Locale.ENGLISH)); // "en" is a language for US
        // Verify no variant locales
        for (Locale l : langs) {
            assertEquals("US", l.getCountry());
            assertTrue(l.getVariant().isEmpty());
        }
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_NonExisting() {
        List<Locale> langs = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_CacheHit() {
        // First call populates cache
        List<Locale> first = LocaleUtils.languagesByCountry("US");
        // Second call should return same list (identity)
        List<Locale> second = LocaleUtils.languagesByCountry("US");
        assertSame(first, second);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_Existing() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        assertTrue(countries.contains(Locale.US));
        // Verify no variant locales and country not empty
        for (Locale l : countries) {
            assertEquals("en", l.getLanguage());
            assertFalse(l.getCountry().isEmpty());
            assertTrue(l.getVariant().isEmpty());
        }
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_NonExisting() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("zz");
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_CacheHit() {
        List<Locale> first = LocaleUtils.countriesByLanguage("en");
        List<Locale> second = LocaleUtils.countriesByLanguage("en");
        assertSame(first, second);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testToLocale_Null() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_EmptyString() {
        LocaleUtils.toLocale("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_Length1() {
        LocaleUtils.toLocale("a");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_Length3() {
        LocaleUtils.toLocale("en_");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_Length4() {
        LocaleUtils.toLocale("en_G");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_Length6() {
        LocaleUtils.toLocale("en_GB_");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidSeparator() {
        LocaleUtils.toLocale("en-GB");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_UpperCaseLanguage() {
        LocaleUtils.toLocale("EN");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_LowerCaseCountry() {
        LocaleUtils.toLocale("en_gb");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidVariantSeparator() {
        LocaleUtils.toLocale("en_GBX");
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_NullDefaultLocale() {
        // Single-arg version uses locale as default, so null locale returns empty list
        List<Locale> list = LocaleUtils.localeLookupList(null);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_Null() {
        List<Locale> langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_EmptyString() {
        List<Locale> langs = LocaleUtils.languagesByCountry("");
        // Empty country code may match some locales? Actually country code "" is valid for language-only locales.
        // But the method filters by country.equals(countryCode). So it will return locales with empty country.
        // We just check it's not null.
        assertNotNull(langs);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_Null() {
        List<Locale> countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_EmptyString() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("");
        assertNotNull(countries);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: toLocale("_GB") should return Locale("", "GB") but currently throws IllegalArgumentException.
     * This test reveals the bug on the defective version.
     */
    @Test(timeout = 4000)
    public void testLang865() {
        // Expected: Locale with empty language and country "GB"
        Locale result = LocaleUtils.toLocale("_GB");
        assertEquals("", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("", result.getVariant());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidFormat_TooShort() {
        LocaleUtils.toLocale("a");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidFormat_NoUnderscore() {
        LocaleUtils.toLocale("enGB");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidFormat_WrongCaseCountry() {
        LocaleUtils.toLocale("en_gb");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidFormat_Length5ButWrongCase() {
        LocaleUtils.toLocale("en_Gb");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocale_InvalidFormat_Length7ButNoUnderscore() {
        LocaleUtils.toLocale("en_GBx");
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_NullLocaleReturnsEmpty() {
        List<Locale> list = LocaleUtils.localeLookupList(null, Locale.US);
        assertTrue(list.isEmpty());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testAvailableLocaleList_Unmodifiable() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        try {
            list.add(Locale.US);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleSet_Unmodifiable() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        try {
            set.add(Locale.US);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountry_Unmodifiable() {
        List<Locale> langs = LocaleUtils.languagesByCountry("US");
        try {
            langs.add(Locale.US);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguage_Unmodifiable() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        try {
            countries.add(Locale.US);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLocaleLookupList_Unmodifiable() {
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US);
        try {
            list.add(Locale.US);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}