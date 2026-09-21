package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: org.apache.commons.lang.LocaleUtils
 *
 * Target Defect (LANG-328):
 * - LocaleUtils.toLocale() fails on valid language-plus-variant strings without country code
 *   (e.g., "fr__POSIX").
 * - Target branch in toLocale: length >= 7, str.charAt(2) == '_', str.charAt(3) == '_' (empty country),
 *   followed by variant identifier.
 *
 * Partition A: Core Functional Logic & State Transitions
 * - toLocale(String) with valid 2-letter language ("en")
 * - toLocale(String) with valid 5-letter language + country ("en_US")
 * - toLocale(String) with valid 7+ character language + country + variant ("en_US_WIN", "fr_CA_xxx")
 * - localeLookupList(Locale) 1-param delegate
 * - localeLookupList(Locale, Locale) with language only, language + country, language + country + variant
 * - localeLookupList(Locale, Locale) default locale duplication check (default already present vs omitted)
 * - availableLocaleList() & availableLocaleSet() querying and caching idempotency
 * - isAvailableLocale(Locale) checks for known vs synthetic locales
 * - languagesByCountry(String) search, result validation, and internal map caching
 * - countriesByLanguage(String) search, result validation, and internal map caching
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - toLocale(null) returns null
 * - localeLookupList(null), localeLookupList(null, defaultLocale)
 * - languagesByCountry(null) returns Collections.EMPTY_LIST
 * - countriesByLanguage(null) returns Collections.EMPTY_LIST
 * - Invalid lengths for toLocale: 0, 1, 3, 4, 6
 * - Invalid character boundaries:
 *     char 0 not in ['a'..'z'] (e.g., 'A', '`', '{')
 *     char 1 not in ['a'..'z']
 *     char 2 not '_' (e.g., '-')
 *     char 3 not in ['A'..'Z'] (e.g., 'a', '@', '[')
 *     char 4 not in ['A'..'Z']
 *     char 5 not '_'
 *
 * Partition C: Defect-Targeted Branch Zone (LANG-328)
 * - toLocale("fr__POSIX") -> Locale("fr", "", "POSIX")
 * - toLocale("de__POSIX") -> Locale("de", "", "POSIX")
 * - toLocale("en__POSIX_test") -> Locale("en", "", "POSIX_test")
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Throws IllegalArgumentException on invalid formats
 * - Immutability checks: modifying availableLocaleList(), availableLocaleSet(),
 *   languagesByCountry(), countriesByLanguage(), localeLookupList() throws UnsupportedOperationException
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - Public constructor instantiation for JavaBean compliance
 */
public class LocaleUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Normal Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageOnly() {
        Locale loc = LocaleUtils.toLocale("en");
        assertNotNull("Locale should not be null", loc);
        assertEquals("en", loc.getLanguage());
        assertEquals("", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageAndCountry() {
        Locale loc = LocaleUtils.toLocale("en_US");
        assertNotNull("Locale should not be null", loc);
        assertEquals("en", loc.getLanguage());
        assertEquals("US", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageCountryAndVariant() {
        Locale loc = LocaleUtils.toLocale("en_US_POSIX");
        assertNotNull("Locale should not be null", loc);
        assertEquals("en", loc.getLanguage());
        assertEquals("US", loc.getCountry());
        assertEquals("POSIX", loc.getVariant());

        Locale locLong = LocaleUtils.toLocale("fr_CA_custom_variant");
        assertNotNull(locLong);
        assertEquals("fr", locLong.getLanguage());
        assertEquals("CA", locLong.getCountry());
        assertEquals("custom_variant", locLong.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListSingleParam() {
        Locale locale = new Locale("en", "US", "POSIX");
        List list = LocaleUtils.localeLookupList(locale);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(new Locale("en", "US", "POSIX"), list.get(0));
        assertEquals(new Locale("en", "US"), list.get(1));
        assertEquals(new Locale("en", ""), list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListTwoParamsDefaultNotPresent() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLoc = new Locale("en", "US");
        List list = LocaleUtils.localeLookupList(locale, defaultLoc);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA"), list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
        assertEquals(defaultLoc, list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListTwoParamsDefaultAlreadyPresent() {
        Locale locale = new Locale("en", "US");
        Locale defaultLoc = new Locale("en", "");
        List list = LocaleUtils.localeLookupList(locale, defaultLoc);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new Locale("en", "US"), list.get(0));
        assertEquals(defaultLoc, list.get(1));
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleListAndSetConsistency() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertFalse(list.isEmpty());

        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertEquals(list.size(), set.size());

        // Call again to test caching logic branch (cAvailableLocaleSet != null)
        Set set2 = LocaleUtils.availableLocaleSet();
        assertSame("Cached set instance should be identical", set, set2);
        assertTrue(set.containsAll(list));
    }

    @Test(timeout = 4000)
    public void testIsAvailableLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));

        Locale fakeLocale = new Locale("xx", "YY", "ZZZZ");
        assertFalse(LocaleUtils.isAvailableLocale(fakeLocale));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryCacheAndContent() {
        List langsUS = LocaleUtils.languagesByCountry("US");
        assertNotNull(langsUS);
        assertFalse(langsUS.isEmpty());
        for (Iterator it = langsUS.iterator(); it.hasNext(); ) {
            Locale l = (Locale) it.next();
            assertEquals("US", l.getCountry());
            assertEquals(0, l.getVariant().length());
        }

        // Subsequent call triggers cache lookup branch
        List langsUSCached = LocaleUtils.languagesByCountry("US");
        assertSame(langsUS, langsUSCached);

        List langsUnknown = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(langsUnknown);
        assertTrue(langsUnknown.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageCacheAndContent() {
        List countriesEn = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countriesEn);
        assertFalse(countriesEn.isEmpty());
        for (Iterator it = countriesEn.iterator(); it.hasNext(); ) {
            Locale l = (Locale) it.next();
            assertEquals("en", l.getLanguage());
            assertTrue(l.getCountry().length() > 0);
            assertEquals(0, l.getVariant().length());
        }

        // Subsequent call triggers cache lookup branch
        List countriesEnCached = LocaleUtils.countriesByLanguage("en");
        assertSame(countriesEn, countriesEnCached);

        List countriesUnknown = LocaleUtils.countriesByLanguage("zz");
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
    public void testLocaleLookupListNullLocale() {
        List list1 = LocaleUtils.localeLookupList(null);
        assertNotNull(list1);
        assertTrue(list1.isEmpty());

        List list2 = LocaleUtils.localeLookupList(null, Locale.ENGLISH);
        assertNotNull(list2);
        assertTrue(list2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryNull() {
        List langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());

        // Call twice for cache branch verification with null key
        List langs2 = LocaleUtils.languagesByCountry(null);
        assertSame(langs, langs2);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageNull() {
        List countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        // Call twice for cache branch verification with null key
        List countries2 = LocaleUtils.countriesByLanguage(null);
        assertSame(countries, countries2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLength0() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLength1() {
        LocaleUtils.toLocale("e");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLength3() {
        LocaleUtils.toLocale("eng");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLength4() {
        LocaleUtils.toLocale("en_U");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleLength6() {
        LocaleUtils.toLocale("en_US_");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidFirstCharLower() {
        LocaleUtils.toLocale("`n");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidFirstCharUpper() {
        LocaleUtils.toLocale("{n");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSecondCharLower() {
        LocaleUtils.toLocale("e`");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSecondCharUpper() {
        LocaleUtils.toLocale("e{");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleFirstCharCapital() {
        LocaleUtils.toLocale("En");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleSecondCharCapital() {
        LocaleUtils.toLocale("eN");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleNoUnderscoreAtPos2() {
        LocaleUtils.toLocale("en-US");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidThirdChar() {
        LocaleUtils.toLocale("en_aS");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidFourthChar() {
        LocaleUtils.toLocale("en_Ua");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleNoUnderscoreAtPos5() {
        LocaleUtils.toLocale("en_US-POSIX");
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J LANG-328)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang328() {
        // Valid ISO Locale containing a language and variant but no country code
        Locale locale = LocaleUtils.toLocale("fr__POSIX");
        assertNotNull("Locale should be successfully created", locale);
        assertEquals("fr", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("POSIX", locale.getVariant());
    }

    @Test(timeout = 4000)
    public void testLang328AdditionalVariants() {
        Locale dePosix = LocaleUtils.toLocale("de__POSIX");
        assertNotNull(dePosix);
        assertEquals("de", dePosix.getLanguage());
        assertEquals("", dePosix.getCountry());
        assertEquals("POSIX", dePosix.getVariant());

        Locale longVariant = LocaleUtils.toLocale("en__POSIX_TEST");
        assertNotNull(longVariant);
        assertEquals("en", longVariant.getLanguage());
        assertEquals("", longVariant.getCountry());
        assertEquals("POSIX_TEST", longVariant.getVariant());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths (Immutability Enforcements)
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleListImmutability() {
        List list = LocaleUtils.availableLocaleList();
        list.add(Locale.CANADA);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleSetImmutability() {
        Set set = LocaleUtils.availableLocaleSet();
        set.add(Locale.CANADA);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLocaleLookupListImmutability() {
        List list = LocaleUtils.localeLookupList(Locale.ENGLISH);
        list.add(Locale.CANADA);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLanguagesByCountryImmutability() {
        List list = LocaleUtils.languagesByCountry("US");
        list.add(Locale.CANADA);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCountriesByLanguageImmutability() {
        List list = LocaleUtils.countriesByLanguage("en");
        list.add(Locale.CANADA);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        LocaleUtils utils = new LocaleUtils();
        assertNotNull("Public constructor for JavaBean compliance must succeed", utils);
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListLanguageOnly() {
        Locale locale = new Locale("en");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(1, list.size());
        assertEquals(locale, list.get(0));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListLanguageAndCountryOnly() {
        Locale locale = new Locale("en", "US");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(2, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("en", ""), list.get(1));
    }
}