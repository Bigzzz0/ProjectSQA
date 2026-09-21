/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: org.apache.commons.lang.LocaleUtils
 *
 * Target Defect (Defects4J):
 * - NPE in isAvailableLocale(Locale): cAvailableLocaleSet is lazily initialized in availableLocaleSet().
 *   Direct invocation of isAvailableLocale(Locale) when availableLocaleSet() has not been previously
 *   called accesses null.contains(locale), causing a NullPointerException.
 *
 * Branch & Boundary Coverage Matrix:
 * 1. toLocale(String):
 *    - str == null (returns null)
 *    - len == 2 (pure language): char bounds [a-z]
 *    - len == 5 (language + country): separator '_', country char bounds [A-Z]
 *    - len >= 7 (language + country + variant): separators at index 2 and index 5
 *    - Invalid lengths: 0, 1, 3, 4, 6
 *    - Invalid chars: language ch0/ch1 < 'a', > 'z'; country ch3/ch4 < 'A', > 'Z'
 *    - Invalid separators: index 2 != '_', index 5 != '_'
 * 2. localeLookupList(Locale, Locale):
 *    - locale == null (returns empty unmodifiable list)
 *    - defaultLocale == null or not matching
 *    - locale with variant (3 elements), locale with country only (2 elements), locale with language only (1 element)
 *    - locale with variant but empty country (e.g., new Locale("en", "", "POSIX"))
 *    - defaultLocale deduplication check (already present vs newly appended)
 * 3. availableLocaleList() / availableLocaleSet():
 *    - List & Set immutability validation (UnsupportedOperationException)
 *    - Cache verification on repeated calls
 * 4. isAvailableLocale(Locale):
 *    - Direct invocation without calling availableLocaleSet() first
 *    - Known installed locales vs non-existent artificial locales
 * 5. languagesByCountry(String):
 *    - null countryCode (returns empty list, cached)
 *    - Valid countryCode (filters out variants, cached result)
 *    - Non-existent countryCode
 * 6. countriesByLanguage(String):
 *    - null languageCode (returns empty list, cached)
 *    - Valid languageCode (filters out variants and empty countries, cached result)
 *    - Non-existent languageCode
 */
public class LocaleUtilsGeminiTest {

    // ==========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ==========================================================================

    @Test(timeout = 4000)
    public void testToLocaleValidTwoLetterLanguage() {
        Locale locale = LocaleUtils.toLocale("en");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());

        locale = LocaleUtils.toLocale("fr");
        assertEquals("fr", locale.getLanguage());
        assertEquals("", locale.getCountry());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageAndCountry() {
        Locale locale = LocaleUtils.toLocale("en_US");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("", locale.getVariant());

        locale = LocaleUtils.toLocale("fr_CA");
        assertEquals("fr", locale.getLanguage());
        assertEquals("CA", locale.getCountry());
    }

    @Test(timeout = 4000)
    public void testToLocaleValidLanguageCountryAndVariant() {
        Locale locale = LocaleUtils.toLocale("en_US_WIN");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("WIN", locale.getVariant());

        locale = LocaleUtils.toLocale("en_US_traditional_custom");
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("traditional_custom", locale.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListDefaultSingleArg() {
        Locale locale = new Locale("en", "US", "POSIX");
        List list = LocaleUtils.localeLookupList(locale);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("en", "US"), list.get(1));
        assertEquals(new Locale("en", ""), list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListWithDefaultLocale() {
        Locale locale = new Locale("en", "GB");
        Locale defaultLocale = Locale.FRANCE;
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(new Locale("en", "GB"), list.get(0));
        assertEquals(new Locale("en", ""), list.get(1));
        assertEquals(defaultLocale, list.get(2));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListWithDefaultLocaleAlreadyPresent() {
        Locale locale = new Locale("en", "GB");
        Locale defaultLocale = new Locale("en");
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new Locale("en", "GB"), list.get(0));
        assertEquals(new Locale("en"), list.get(1));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListVariantWithoutCountry() {
        Locale locale = new Locale("en", "", "POSIX");
        List list = LocaleUtils.localeLookupList(locale);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(new Locale("en", "", "POSIX"), list.get(0));
        assertEquals(new Locale("en", ""), list.get(1));
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryNormalAndCached() {
        List langs1 = LocaleUtils.languagesByCountry("US");
        assertNotNull(langs1);
        assertFalse(langs1.isEmpty());
        for (int i = 0; i < langs1.size(); i++) {
            Locale loc = (Locale) langs1.get(i);
            assertEquals("US", loc.getCountry());
            assertEquals("", loc.getVariant());
        }

        List langs2 = LocaleUtils.languagesByCountry("US");
        assertSame("Subsequent calls should return cached list instance", langs1, langs2);
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageNormalAndCached() {
        List countries1 = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries1);
        assertFalse(countries1.isEmpty());
        for (int i = 0; i < countries1.size(); i++) {
            Locale loc = (Locale) countries1.get(i);
            assertEquals("en", loc.getLanguage());
            assertTrue(loc.getCountry().length() > 0);
            assertEquals("", loc.getVariant());
        }

        List countries2 = LocaleUtils.countriesByLanguage("en");
        assertSame("Subsequent calls should return cached list instance", countries1, countries2);
    }

    // ==========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ==========================================================================

    @Test(timeout = 4000)
    public void testToLocaleNullArgument() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test(timeout = 4000)
    public void testLocaleLookupListNullLocale() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());

        list = LocaleUtils.localeLookupList(null, Locale.ENGLISH);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLanguagesByCountryNullOrEmpty() {
        List langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());

        List cachedLangs = LocaleUtils.languagesByCountry(null);
        assertSame(langs, cachedLangs);

        List unknownLangs = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(unknownLangs);
        assertTrue(unknownLangs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCountriesByLanguageNullOrEmpty() {
        List countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());

        List cachedCountries = LocaleUtils.countriesByLanguage(null);
        assertSame(countries, cachedCountries);

        List unknownCountries = LocaleUtils.countriesByLanguage("zz");
        assertNotNull(unknownCountries);
        assertTrue(unknownCountries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAvailableLocaleListAndSetCompleteness() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertFalse(list.isEmpty());

        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertEquals(list.size(), set.size());
        assertTrue(set.containsAll(list));

        Set cachedSet = LocaleUtils.availableLocaleSet();
        assertSame(set, cachedSet);
    }

    // ==========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ==========================================================================

    /**
     * Targets Defects4J bug where isAvailableLocale accesses cAvailableLocaleSet
     * directly without triggering availableLocaleSet() lazy initialization first.
     */
    @Test(timeout = 4000)
    public void testIsAvailableLocaleDirectly() {
        Locale testLocale = Locale.US;
        boolean available = LocaleUtils.isAvailableLocale(testLocale);
        List availableList = Arrays.asList(Locale.getAvailableLocales());
        assertEquals(availableList.contains(testLocale), available);

        Locale fakeLocale = new Locale("xx", "YY", "ZZ");
        assertFalse(LocaleUtils.isAvailableLocale(fakeLocale));
    }

    // ==========================================================================
    // Partition D: Exception & Defensive Guard Paths (toLocale Parsing)
    // ==========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLengthEmpty() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLength1() {
        LocaleUtils.toLocale("e");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLength3() {
        LocaleUtils.toLocale("en_");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLength4() {
        LocaleUtils.toLocale("en_U");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLength6() {
        LocaleUtils.toLocale("en_US_");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar0Low() {
        LocaleUtils.toLocale("`n");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar0High() {
        LocaleUtils.toLocale("{n");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar0Upper() {
        LocaleUtils.toLocale("En");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar1Low() {
        LocaleUtils.toLocale("e`");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar1High() {
        LocaleUtils.toLocale("e{");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidLanguageChar1Upper() {
        LocaleUtils.toLocale("eN");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSeparatorAt2ForLength5() {
        LocaleUtils.toLocale("en-US");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar3Low() {
        LocaleUtils.toLocale("en_@S");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar3High() {
        LocaleUtils.toLocale("en_[S");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar3Lower() {
        LocaleUtils.toLocale("en_uS");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar4Low() {
        LocaleUtils.toLocale("en_U@");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar4High() {
        LocaleUtils.toLocale("en_U[");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidCountryChar4Lower() {
        LocaleUtils.toLocale("en_Us");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSeparatorAt2ForLength7() {
        LocaleUtils.toLocale("en#US_WIN");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToLocaleInvalidSeparatorAt5ForLength7() {
        LocaleUtils.toLocale("en_US#WIN");
    }

    // ==========================================================================
    // Partition E: Object Lifecycle & Immutability Contract Integrity
    // ==========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleListIsUnmodifiable() {
        List list = LocaleUtils.availableLocaleList();
        list.add(Locale.ENGLISH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAvailableLocaleSetIsUnmodifiable() {
        Set set = LocaleUtils.availableLocaleSet();
        set.add(Locale.ENGLISH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLocaleLookupListIsUnmodifiable() {
        List list = LocaleUtils.localeLookupList(Locale.ENGLISH);
        list.add(Locale.FRENCH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testLanguagesByCountryIsUnmodifiable() {
        List list = LocaleUtils.languagesByCountry("US");
        list.add(Locale.ENGLISH);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCountriesByLanguageIsUnmodifiable() {
        List list = LocaleUtils.countriesByLanguage("en");
        list.add(Locale.ENGLISH);
    }
}