package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * White-box test suite for LocaleUtils targeting maximum coverage and the known defect
 * (testLang328: toLocale("fr__POSIX") incorrectly throws IllegalArgumentException).
 *
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * Partition A: Core Functional Logic & State Transitions
 *   - toLocale: null, length 2, length 5, length >=7 with valid/invalid separators
 *   - localeLookupList: null locale, null default, variant/country present, duplicates
 *   - availableLocaleList/Set: caching, immutability
 *   - isAvailableLocale: true/false
 *   - languagesByCountry: null, empty, cached, non-empty
 *   - countriesByLanguage: null, empty, cached, non-empty
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - toLocale: length 2 (lowercase), length 5 (uppercase country), length 7+ with variant
 *   - localeLookupList: locale with variant, with country, with both, default same/different
 *   - languagesByCountry: countryCode null, empty string, valid 2-letter, invalid length
 *   - countriesByLanguage: languageCode null, empty string, valid 2-letter, invalid length
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - toLocale("fr__POSIX") must succeed and produce Locale("fr", "", "POSIX")
 *   - toLocale("en__US") must succeed (empty country, variant)
 *   - toLocale("de__") must succeed (empty variant? Actually variant empty, but format "de__" -> language "de", country "", variant ""? That's ambiguous; but the code should handle it)
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - toLocale: invalid length (1,3,4,6), invalid language chars, missing underscore, invalid country chars, invalid variant separator
 *   - localeLookupList: null locale returns empty list (not null)
 *   - languagesByCountry: null returns empty list, not null
 *   - countriesByLanguage: null returns empty list, not null
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - availableLocaleSet: lazy initialization, unmodifiable
 *   - availableLocaleList: unmodifiable
 *   - cLanguagesByCountry / cCountriesByLanguage: synchronized map caching
 */
public class LocaleUtilsDeepseekTest {

    // ==================== Partition A & B: Core Logic & Boundaries ====================

    @Test(timeout = 4000)
    public void testToLocaleNull() {
        assertNull("toLocale(null) should return null", LocaleUtils.toLocale(null));
    }

    @Test(timeout = 4000)
    public void testToLocaleLength2() {
        Locale result = LocaleUtils.toLocale("en");
        assertEquals("en", result.getLanguage());
        assertEquals("", result.getCountry());
        assertEquals("", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleLength5() {
        Locale result = LocaleUtils.toLocale("en_GB");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleLength7Plus() {
        Locale result = LocaleUtils.toLocale("en_GB_xxx");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("xxx", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidLength1() {
        try {
            LocaleUtils.toLocale("a");
            fail("Expected IllegalArgumentException for length 1");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidLength3() {
        try {
            LocaleUtils.toLocale("abc");
            fail("Expected IllegalArgumentException for length 3");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidLength4() {
        try {
            LocaleUtils.toLocale("abcd");
            fail("Expected IllegalArgumentException for length 4");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidLength6() {
        try {
            LocaleUtils.toLocale("ab_cde");
            fail("Expected IllegalArgumentException for length 6");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidLanguageChars() {
        try {
            LocaleUtils.toLocale("A2");
            fail("Expected IllegalArgumentException for uppercase language");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            LocaleUtils.toLocale("1b");
            fail("Expected IllegalArgumentException for digit in language");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleMissingUnderscore() {
        try {
            LocaleUtils.toLocale("enGB");
            fail("Expected IllegalArgumentException for missing underscore at position 2");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidCountryChars() {
        try {
            LocaleUtils.toLocale("en_gb");
            fail("Expected IllegalArgumentException for lowercase country");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            LocaleUtils.toLocale("en_G1");
            fail("Expected IllegalArgumentException for digit in country");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToLocaleInvalidVariantSeparator() {
        try {
            LocaleUtils.toLocale("en_GB_");
            fail("Expected IllegalArgumentException for missing variant after underscore");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Actually length 7: "en_GB_" -> len=7, passes first check, then charAt(5) is '_'? Wait: "en_GB_" indices: 0=e,1=n,2=_,3=G,4=B,5=_,6=? Actually length 7: characters: e n _ G B _ ? The string "en_GB_" has 7 chars: e,n,_,G,B,_,? No, it's e n _ G B _ -> that's 6? Let's count: "en_GB_" = e(0), n(1), _(2), G(3), B(4), _(5) -> length 6. So to get length 7 with variant separator but no variant, we need something like "en_GB__"? That would be 7? Actually "en_GB__" = e n _ G B _ _ -> length 7. Then charAt(5) is '_', charAt(6) is '_'? The code checks if len == 5? No, len=7, so goes to else after checking charAt(2) and charAt(3,4). Then it checks if len == 5? No, so goes to else: if (str.charAt(5) != '_') throw... So for "en_GB__", charAt(5) is '_', so it passes, then returns new Locale("en","GB","_")? That would be variant "_". That's probably not intended but the code allows it. The known defect is about empty country, not empty variant. We'll test the known defect separately.
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Defect testLang328: toLocale("fr__POSIX") must succeed.
     * The current code incorrectly throws IllegalArgumentException because it requires
     * country code to be two uppercase letters, but when country is empty, the format
     * is language + "__" + variant.
     */
    @Test(timeout = 4000)
    public void testToLocaleEmptyCountryWithVariant() {
        Locale result = LocaleUtils.toLocale("fr__POSIX");
        assertEquals("fr", result.getLanguage());
        assertEquals("", result.getCountry());
        assertEquals("POSIX", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleEmptyCountryWithVariant2() {
        Locale result = LocaleUtils.toLocale("en__US");
        assertEquals("en", result.getLanguage());
        assertEquals("", result.getCountry());
        assertEquals("US", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleEmptyCountryEmptyVariant() {
        // "de__" -> language "de", country "", variant ""? Actually Locale("de","","") is valid.
        // The format "de__" has length 4? "de__" = d e _ _ -> length 4, which fails the first check (len !=2, !=5, len<7? 4<7 true, so throws). So this is invalid according to current code. But the spec says length must be 2,5, or >=7. So "de__" is length 4, invalid. So we expect exception.
        try {
            LocaleUtils.toLocale("de__");
            fail("Expected IllegalArgumentException for length 4");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testLocaleLookupListNullLocale() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull("list should not be null", list);
        assertTrue("list should be empty", list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListNullDefault() {
        Locale locale = Locale.US;
        List list = LocaleUtils.localeLookupList(locale, null);
        assertNotNull(list);
        // Should contain locale and its fallbacks, but default is null, so it's not added.
        // The method adds defaultLocale only if not already in list. null is not a Locale, so it will be added? Actually the code: if (list.contains(defaultLocale) == false) { list.add(defaultLocale); } where defaultLocale is null. list.contains(null) returns false, so it adds null. That would cause a NullPointerException later? But the method returns unmodifiable list, and the list contains null. That's a potential bug but not our target. We'll just verify it doesn't throw.
        assertTrue(list.contains(locale));
        // The list may contain null; we don't assert that.
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListWithVariant() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(locale, list.get(3)); // default is same as locale, so it's added again? Actually the code adds defaultLocale if not already in list. Since defaultLocale == locale, it is already in list (index 0), so it should not be added again. Wait: the code checks list.contains(defaultLocale) == false. Since locale is already in list, it should not add. So list size should be 3? Let's trace: list = [locale]; variant length >0 -> add new Locale(lang, country) -> list = [locale, fr_CA]; country length >0 -> add new Locale(lang, "") -> list = [locale, fr_CA, fr]; then check contains(defaultLocale) -> defaultLocale is locale, which is already in list, so not added. So size = 3. But the javadoc says the result includes the default locale at the end. Actually the javadoc example: localeLookupList(Locale("fr","CA","xxx")) = [Locale("fr","CA","xxx"), Locale("fr","CA"), Locale("fr")]. That's 3 elements. So size should be 3. Our test expects 4? Let's correct.
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListWithDefaultDifferent() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = Locale.ENGLISH;
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(defaultLocale, list.get(3));
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleList() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
        // Verify it's unmodifiable
        try {
            list.add(Locale.US);
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleSet() {
        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertTrue(set.size() > 0);
        // Verify it's unmodifiable
        try {
            set.add(Locale.US);
            fail("Set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // Verify caching: second call returns same set
        Set set2 = LocaleUtils.availableLocaleSet();
        assertSame(set, set2);
    }

    @Test(timeout = 4000)
    public void testIsAvailableLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "XX")));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryNull() {
        List langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryEmptyString() {
        List langs = LocaleUtils.languagesByCountry("");
        // Should return empty list because no locale has empty country? Actually some locales have empty country? The method filters by countryCode.equals(locale.getCountry()) and variant length 0. For empty country, it will match locales with empty country (like "en"). So it may return a non-empty list. We'll just check it's not null.
        assertNotNull(langs);
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryValid() {
        List langs = LocaleUtils.languagesByCountry("US");
        assertNotNull(langs);
        assertTrue(langs.size() > 0);
        for (Object o : langs) {
            Locale l = (Locale) o;
            assertEquals("US", l.getCountry());
            assertEquals(0, l.getVariant().length());
        }
        // Test caching
        List langs2 = LocaleUtils.languagesByCountry("US");
        assertSame(langs, langs2);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageNull() {
        List countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageEmptyString() {
        List countries = LocaleUtils.countriesByLanguage("");
        assertNotNull(countries);
        // Should return empty list because no locale has empty language? Actually language cannot be empty in a valid locale, so it's empty.
        assertTrue(countries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageValid() {
        List countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        assertTrue(countries.size() > 0);
        for (Object o : countries) {
            Locale l = (Locale) o;
            assertEquals("en", l.getLanguage());
            assertTrue(l.getCountry().length() > 0);
            assertEquals(0, l.getVariant().length());
        }
        // Test caching
        List countries2 = LocaleUtils.countriesByLanguage("en");
        assertSame(countries, countries2);
    }

    // ==================== Additional Edge Cases ====================

    @Test(timeout = 4000)
    public void testToLocaleVariantWithUnderscores() {
        // Variant may contain underscores? The Locale constructor accepts variant with underscores.
        // Our method splits at first underscore after country? Actually it takes substring(6) which includes everything after the second underscore. So "en_GB_xxx_yyy" would produce variant "xxx_yyy". That's valid.
        Locale result = LocaleUtils.toLocale("en_GB_xxx_yyy");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("xxx_yyy", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testToLocaleLength7WithEmptyVariant() {
        // "en_GB_" is length 6? Actually "en_GB_" is 6 chars, invalid. "en_GB__" is 7 chars, variant "_". That's allowed by current code but produces variant "_". We'll test that it works.
        Locale result = LocaleUtils.toLocale("en_GB__");
        assertEquals("en", result.getLanguage());
        assertEquals("GB", result.getCountry());
        assertEquals("_", result.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListNoVariantNoCountry() {
        Locale locale = new Locale("en");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(1, list.size()); // only locale itself, no variant, no country, default same so not added
        assertEquals(locale, list.get(0));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListOnlyCountry() {
        Locale locale = new Locale("en", "US");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(2, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("en", ""), list.get(1));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryCachingWithNull() {
        // First call with null caches empty list
        List langs1 = LocaleUtils.languagesByCountry(null);
        List langs2 = LocaleUtils.languagesByCountry(null);
        assertSame(langs1, langs2);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageCachingWithNull() {
        List countries1 = LocaleUtils.countriesByLanguage(null);
        List countries2 = LocaleUtils.countriesByLanguage(null);
        assertSame(countries1, countries2);
    }
}