package org.apache.commons.lang3;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.lang3.LocaleUtils
 *
 * Decision / Condition Coverage Targets:
 * 1. toLocale(String str):
 *    - Branch: str == null -> returns null
 *    - Branch: len < 2 -> IllegalArgumentException
 *    - Branch: !isLowerCase(ch0) || !isLowerCase(ch1) -> IllegalArgumentException
 *    - Branch: len == 2 -> Locale(str)
 *    - Branch: len < 5 -> IllegalArgumentException
 *    - Branch: charAt(2) != '_' -> IllegalArgumentException
 *    - Branch: ch3 == '_' -> Locale(lang, "", variant)
 *    - Branch: !isUpperCase(ch3) || !isUpperCase(ch4) -> IllegalArgumentException
 *    - Branch: len == 5 -> Locale(lang, country)
 *    - Branch: len < 7 -> IllegalArgumentException
 *    - Branch: charAt(5) != '_' -> IllegalArgumentException
 *    - Branch: len >= 7 -> Locale(lang, country, variant)
 *
 * 2. Defect Zone (LANG-865):
 *    - Defect: LocaleUtils.toLocale("_GB") fails because ch0 is '_' and not lower case.
 *    - Expected: parsing locales with empty language and valid country/variant (e.g. "_GB", "_GB_traditional").
 *
 * 3. localeLookupList(Locale locale, Locale defaultLocale):
 *    - Branch: locale == null -> empty list
 *    - Branch: locale.getVariant().length() > 0 -> add Locale(lang, country)
 *    - Branch: locale.getCountry().length() > 0 -> add Locale(lang, "")
 *    - Branch: list.contains(defaultLocale) == false / true -> add defaultLocale if absent
 *    - Immutability check on result
 *
 * 4. availableLocaleList(), availableLocaleSet(), isAvailableLocale(Locale):
 *    - Wrapper check on JDK Locale.getAvailableLocales()
 *    - Immutability checks on returned collection
 *    - True/false branches for isAvailableLocale
 *
 * 5. languagesByCountry(String countryCode) & countriesByLanguage(String languageCode):
 *    - Branch: code == null -> empty list
 *    - Branch: cache miss (langs/countries == null) vs cache hit
 *    - Filter conditions: country matches, variant empty, language matches, country not empty
 *    - Immutability checks
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocaleUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguages() {
        Locale l = LocaleUtils.toLocale("us");
        assertNotNull(l);
        assertEquals("us", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());

        l = LocaleUtils.toLocale("fr");
        assertNotNull(l);
        assertEquals("fr", l.getLanguage());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageAndCountry() {
        Locale l = LocaleUtils.toLocale("us_EN");
        assertNotNull(l);
        assertEquals("us", l.getLanguage());
        assertEquals("EN", l.getCountry());
        assertEquals("", l.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageCountryAndVariant() {
        Locale l = LocaleUtils.toLocale("us_EN_A");
        assertNotNull(l);
        assertEquals("us", l.getLanguage());
        assertEquals("EN", l.getCountry());
        assertEquals("A", l.getVariant());

        l = LocaleUtils.toLocale("us_EN_POSIX");
        assertNotNull(l);
        assertEquals("us", l.getLanguage());
        assertEquals("EN", l.getCountry());
        assertEquals("POSIX", l.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageWithEmptyCountryAndVariant() {
        Locale l = LocaleUtils.toLocale("us__POSIX");
        assertNotNull(l);
        assertEquals("us", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("POSIX", l.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListBasic() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListWithDefault() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = new Locale("en", "US");
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);

        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(defaultLocale, list.get(3));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListDefaultAlreadyPresent() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = new Locale("fr", "");
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new Locale("fr", "CA"), list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListLanguageOnly() {
        Locale locale = new Locale("fr");
        List<Locale> list = LocaleUtils.localeLookupList(locale);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(new Locale("fr"), list.get(0));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListVariantWithoutCountry() {
        Locale locale = new Locale("fr", "", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new Locale("fr", "", "xxx"), list.get(0));
        assertEquals(new Locale("fr"), list.get(1));
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleListAndSet() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        Set<Locale> set = LocaleUtils.availableLocaleSet();

        assertNotNull(list);
        assertNotNull(set);
        assertFalse(list.isEmpty());
        assertFalse(set.isEmpty());
        assertEquals(list.size(), set.size());
        assertTrue(set.containsAll(list));
    }

    @Test(timeout = 4000)
    public void testIsAvailableLocale() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        for (Locale l : set) {
            assertTrue(LocaleUtils.isAvailableLocale(l));
        }
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("qq", "ZZ", "DontExist")));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryCacheAndResults() {
        List<Locale> langsUS = LocaleUtils.languagesByCountry("US");
        assertNotNull(langsUS);
        assertFalse(langsUS.isEmpty());
        for (Locale l : langsUS) {
            assertEquals("US", l.getCountry());
            assertEquals("", l.getVariant());
        }

        // Hit cache branch
        List<Locale> langsUSCached = LocaleUtils.languagesByCountry("US");
        assertSame(langsUS, langsUSCached);

        // Unknown country code
        List<Locale> langsUnknown = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(langsUnknown);
        assertTrue(langsUnknown.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageCacheAndResults() {
        List<Locale> countriesEN = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countriesEN);
        assertFalse(countriesEN.isEmpty());
        for (Locale l : countriesEN) {
            assertEquals("en", l.getLanguage());
            assertFalse(l.getCountry().isEmpty());
            assertEquals("", l.getVariant());
        }

        // Hit cache branch
        List<Locale> countriesENCached = LocaleUtils.countriesByLanguage("en");
        assertSame(countriesEN, countriesENCached);

        // Unknown language code
        List<Locale> countriesUnknown = LocaleUtils.countriesByLanguage("zz");
        assertNotNull(countriesUnknown);
        assertTrue(countriesUnknown.isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testToLocaleNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListNull() {
        List<Locale> list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());

        List<Locale> listWithDefault = LocaleUtils.localeLookupList(null, Locale.ENGLISH);
        assertNotNull(listWithDefault);
        assertTrue(listWithDefault.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryNull() {
        List<Locale> list = LocaleUtils.languagesByCountry(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageNull() {
        List<Locale> list = LocaleUtils.countriesByLanguage(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-865)
    // =========================================================================

    /**
     * Target Defects4J Defect: LANG-865.
     * LocaleUtils.toLocale("_GB") throws IllegalArgumentException instead of parsing country-only locale.
     */
    @Test(timeout = 4000)
    public void testLang865() {
        Locale l1 = LocaleUtils.toLocale("_GB");
        assertNotNull(l1);
        assertEquals("", l1.getLanguage());
        assertEquals("GB", l1.getCountry());

        Locale l2 = LocaleUtils.toLocale("_GB_traditional");
        assertNotNull(l2);
        assertEquals("", l2.getLanguage());
        assertEquals("GB", l2.getCountry());
        assertEquals("traditional", l2.getVariant());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleEmptyString() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLengthOne() {
        LocaleUtils.toLocale("a");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleFirstCharNotLower() {
        LocaleUtils.toLocale("Us");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleSecondCharNotLower() {
        LocaleUtils.toLocale("uS");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleNumericLanguage() {
        LocaleUtils.toLocale("12");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLengthThree() {
        LocaleUtils.toLocale("en_");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLengthFour() {
        LocaleUtils.toLocale("en_G");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSeparatorAtTwo() {
        LocaleUtils.toLocale("en-GB");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleCountryFirstNotUpper() {
        LocaleUtils.toLocale("en_gB");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleCountrySecondNotUpper() {
        LocaleUtils.toLocale("en_Gb");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLengthSix() {
        LocaleUtils.toLocale("en_GB_");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSeparatorAtFive() {
        LocaleUtils.toLocale("en_GB-P");
    }

    // =========================================================================
    // Partition E: Immutability & Contract Integrity
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleListUnmodifiable() {
        LocaleUtils.availableLocaleList().add(Locale.ENGLISH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleSetUnmodifiable() {
        LocaleUtils.availableLocaleSet().add(Locale.ENGLISH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLocaleLookupListUnmodifiable() {
        List<Locale> list = LocaleUtils.localeLookupList(Locale.ENGLISH);
        list.add(Locale.GERMAN);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLanguagesByCountryUnmodifiable() {
        List<Locale> list = LocaleUtils.languagesByCountry("US");
        list.add(Locale.GERMAN);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCountriesByLanguageUnmodifiable() {
        List<Locale> list = LocaleUtils.countriesByLanguage("en");
        list.add(Locale.GERMAN);
    }
}