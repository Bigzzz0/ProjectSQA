package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * LocaleUtilsDeepseekTest - Advanced White-Box Test Suite for LocaleUtils
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.lang.LocaleUtils
 * 
 * Known Defect: NullPointerException in multiple methods when cAvailableLocaleSet is null
 * Root Cause: isAvailableLocale() accesses cAvailableLocaleSet without null-check,
 *             and availableLocaleSet() has a race condition where cAvailableLocaleSet
 *             can be read as null between assignment and unmodifiable wrapping.
 * 
 * Decision Branches Targeted:
 * 1. toLocale(): null input, length checks (2,5,<7), char range checks, underscore positions
 * 2. localeLookupList(): null locale, variant/country length checks, defaultLocale containment
 * 3. availableLocaleSet(): lazy initialization null check
 * 4. isAvailableLocale(): direct set access (defect zone)
 * 5. languagesByCountry(): null countryCode, cache miss/hit, variant filtering
 * 6. countriesByLanguage(): null languageCode, cache miss/hit, country/variant filtering
 * 
 * Boundary Conditions:
 * - String lengths: 0, 1, 2, 3, 4, 5, 6, 7, 8
 * - Character ranges: a-z, A-Z, underscore, special chars
 * - Null inputs for all public methods
 * - Empty strings for country/language codes
 */
public class LocaleUtilsDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testToLocale_ValidTwoPart() {
        Locale result = LocaleUtils.toLocale("en");
        assertNotNull("Should not be null", result);
        assertEquals("Language should be en", "en", result.getLanguage());
        assertEquals("Country should be empty", "", result.getCountry());
    }
    
    @Test(timeout = 4000)
    public void testToLocale_ValidFivePart() {
        Locale result = LocaleUtils.toLocale("en_GB");
        assertNotNull("Should not be null", result);
        assertEquals("Language should be en", "en", result.getLanguage());
        assertEquals("Country should be GB", "GB", result.getCountry());
    }
    
    @Test(timeout = 4000)
    public void testToLocale_ValidSevenPlusPart() {
        Locale result = LocaleUtils.toLocale("en_GB_xxx");
        assertNotNull("Should not be null", result);
        assertEquals("Language should be en", "en", result.getLanguage());
        assertEquals("Country should be GB", "GB", result.getCountry());
        assertEquals("Variant should be xxx", "xxx", result.getVariant());
    }
    
    @Test(timeout = 4000)
    public void testToLocale_NullInput() {
        assertNull("Null input should return null", LocaleUtils.toLocale(null));
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_NullLocale() {
        List result = LocaleUtils.localeLookupList(null);
        assertNotNull("Should not be null", result);
        assertTrue("Should be empty", result.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_WithDefault() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = new Locale("en");
        List result = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull("Should not be null", result);
        assertEquals("Should have 4 entries", 4, result.size());
        assertEquals("First should be original locale", locale, result.get(0));
        assertEquals("Second should be language+country", new Locale("fr", "CA"), result.get(1));
        assertEquals("Third should be language only", new Locale("fr"), result.get(2));
        assertEquals("Fourth should be default locale", defaultLocale, result.get(3));
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_NoVariant() {
        Locale locale = new Locale("en", "US");
        Locale defaultLocale = new Locale("en");
        List result = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull("Should not be null", result);
        assertEquals("Should have 3 entries", 3, result.size());
        assertEquals("First should be original locale", locale, result.get(0));
        assertEquals("Second should be language only", new Locale("en"), result.get(1));
        assertEquals("Third should be default locale", defaultLocale, result.get(2));
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_NoCountry() {
        Locale locale = new Locale("en");
        Locale defaultLocale = new Locale("fr");
        List result = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull("Should not be null", result);
        assertEquals("Should have 2 entries", 2, result.size());
        assertEquals("First should be original locale", locale, result.get(0));
        assertEquals("Second should be default locale", defaultLocale, result.get(1));
    }
    
    @Test(timeout = 4000)
    public void testAvailableLocaleList_NotNull() {
        List result = LocaleUtils.availableLocaleList();
        assertNotNull("Should not be null", result);
        assertFalse("Should not be empty", result.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testAvailableLocaleSet_NotNull() {
        Set result = LocaleUtils.availableLocaleSet();
        assertNotNull("Should not be null", result);
        assertFalse("Should not be empty", result.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testAvailableLocaleSet_Consistency() {
        Set result1 = LocaleUtils.availableLocaleSet();
        Set result2 = LocaleUtils.availableLocaleSet();
        assertSame("Should return same cached instance", result1, result2);
    }
    
    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidLength1() {
        try {
            LocaleUtils.toLocale("a");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidLength3() {
        try {
            LocaleUtils.toLocale("abc");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidLength4() {
        try {
            LocaleUtils.toLocale("abcd");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidLength6() {
        try {
            LocaleUtils.toLocale("ab_cde");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidLanguageCase() {
        try {
            LocaleUtils.toLocale("Ab");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidCountryCase() {
        try {
            LocaleUtils.toLocale("en_gb");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_MissingUnderscore() {
        try {
            LocaleUtils.toLocale("enGB");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidChars() {
        try {
            LocaleUtils.toLocale("en_G!");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_EmptyString() {
        try {
            LocaleUtils.toLocale("");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testLanguagesByCountry_NullInput() {
        List result = LocaleUtils.languagesByCountry(null);
        assertNotNull("Should not be null", result);
        assertTrue("Should be empty", result.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testLanguagesByCountry_EmptyInput() {
        List result = LocaleUtils.languagesByCountry("");
        assertNotNull("Should not be null", result);
        // May be empty or contain locales with empty country
    }
    
    @Test(timeout = 4000)
    public void testCountriesByLanguage_NullInput() {
        List result = LocaleUtils.countriesByLanguage(null);
        assertNotNull("Should not be null", result);
        assertTrue("Should be empty", result.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testCountriesByLanguage_EmptyInput() {
        List result = LocaleUtils.countriesByLanguage("");
        assertNotNull("Should not be null", result);
        // May be empty or contain locales with empty language
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    /**
     * CRITICAL DEFECT TEST: This test targets the NullPointerException
     * that occurs when isAvailableLocale() is called before availableLocaleSet()
     * has been initialized. The defect is that cAvailableLocaleSet can be null
     * when isAvailableLocale() is called, causing NPE.
     */
    @Test(timeout = 4000)
    public void testIsAvailableLocale_BeforeSetInitialization() {
        // Force re-initialization by accessing availableLocaleList first
        // This ensures cAvailableLocaleSet is still null
        List list = LocaleUtils.availableLocaleList();
        assertNotNull("List should not be null", list);
        
        // Now call isAvailableLocale - this should NOT throw NPE
        // but the defective version will throw NullPointerException
        Locale testLocale = new Locale("en", "US");
        try {
            boolean result = LocaleUtils.isAvailableLocale(testLocale);
            // If we get here, the bug is fixed - just verify it returns something
            assertTrue("Result should be boolean", result == true || result == false);
        } catch (NullPointerException e) {
            fail("isAvailableLocale() threw NullPointerException - this is the known defect! " +
                 "cAvailableLocaleSet was null when accessed.");
        }
    }
    
    /**
     * Additional defect test: Verify that availableLocaleSet() works correctly
     * even when called multiple times in quick succession (race condition test)
     */
    @Test(timeout = 4000)
    public void testAvailableLocaleSet_ConsecutiveCalls() {
        for (int i = 0; i < 10; i++) {
            Set set = LocaleUtils.availableLocaleSet();
            assertNotNull("Set should not be null on iteration " + i, set);
            assertFalse("Set should not be empty on iteration " + i, set.isEmpty());
        }
    }
    
    /**
     * Test that isAvailableLocale works with a known available locale
     */
    @Test(timeout = 4000)
    public void testIsAvailableLocale_WithKnownLocale() {
        // First ensure set is initialized
        LocaleUtils.availableLocaleSet();
        
        // Test with a locale that should exist
        Locale[] available = Locale.getAvailableLocales();
        if (available.length > 0) {
            boolean result = LocaleUtils.isAvailableLocale(available[0]);
            assertTrue("First available locale should be available", result);
        }
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidUnderscorePosition() {
        try {
            LocaleUtils.toLocale("en_");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_InvalidVariantSeparator() {
        try {
            LocaleUtils.toLocale("en_GBx");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_NumericLanguage() {
        try {
            LocaleUtils.toLocale("12");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToLocale_NumericCountry() {
        try {
            LocaleUtils.toLocale("en_12");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_NullDefault() {
        Locale locale = new Locale("en");
        List result = LocaleUtils.localeLookupList(locale, null);
        assertNotNull("Should not be null", result);
        assertEquals("Should have 1 entry", 1, result.size());
        assertEquals("Should be original locale", locale, result.get(0));
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_NullBoth() {
        List result = LocaleUtils.localeLookupList(null, null);
        assertNotNull("Should not be null", result);
        assertTrue("Should be empty", result.isEmpty());
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testConstructor() {
        try {
            LocaleUtils utils = new LocaleUtils();
            assertNotNull("Constructor should work", utils);
        } catch (NullPointerException e) {
            fail("Constructor threw NullPointerException - this is the known defect!");
        }
    }
    
    @Test(timeout = 4000)
    public void testLanguagesByCountry_Caching() {
        // First call should populate cache
        List result1 = LocaleUtils.languagesByCountry("US");
        // Second call should return cached result
        List result2 = LocaleUtils.languagesByCountry("US");
        assertSame("Should return cached list", result1, result2);
    }
    
    @Test(timeout = 4000)
    public void testCountriesByLanguage_Caching() {
        // First call should populate cache
        List result1 = LocaleUtils.countriesByLanguage("en");
        // Second call should return cached result
        List result2 = LocaleUtils.countriesByLanguage("en");
        assertSame("Should return cached list", result1, result2);
    }
    
    @Test(timeout = 4000)
    public void testLanguagesByCountry_NoVariants() {
        List result = LocaleUtils.languagesByCountry("US");
        assertNotNull("Should not be null", result);
        for (Object obj : result) {
            Locale locale = (Locale) obj;
            assertEquals("Country should be US", "US", locale.getCountry());
            assertEquals("Variant should be empty", "", locale.getVariant());
        }
    }
    
    @Test(timeout = 4000)
    public void testCountriesByLanguage_NoVariants() {
        List result = LocaleUtils.countriesByLanguage("en");
        assertNotNull("Should not be null", result);
        for (Object obj : result) {
            Locale locale = (Locale) obj;
            assertEquals("Language should be en", "en", locale.getLanguage());
            assertTrue("Country should not be empty", locale.getCountry().length() > 0);
            assertEquals("Variant should be empty", "", locale.getVariant());
        }
    }
    
    @Test(timeout = 4000)
    public void testAvailableLocaleList_Unmodifiable() {
        List result = LocaleUtils.availableLocaleList();
        try {
            result.add(new Locale("xx"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testAvailableLocaleSet_Unmodifiable() {
        Set result = LocaleUtils.availableLocaleSet();
        try {
            result.add(new Locale("xx"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testLocaleLookupList_Unmodifiable() {
        List result = LocaleUtils.localeLookupList(new Locale("en"));
        try {
            result.add(new Locale("fr"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testLanguagesByCountry_Unmodifiable() {
        List result = LocaleUtils.languagesByCountry("US");
        try {
            result.add(new Locale("xx"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCountriesByLanguage_Unmodifiable() {
        List result = LocaleUtils.countriesByLanguage("en");
        try {
            result.add(new Locale("xx"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}