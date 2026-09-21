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
package org.apache.commons.lang3;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Under Test:
 * - StringUtilsEqualsIndexOfTest::testEquals
 *   StringUtils.equals(CharSequence, CharSequence) previously invoked cs1.equals(cs2) directly without
 *   checking if the CharSequence implementations (e.g., StringBuilder, StringBuffer, CharBuffer, String)
 *   support cross-type or content-based equals. Because StringBuilder does not override equals(),
 *   two non-identical StringBuilder instances with identical character sequences returned false.
 *   Furthermore, String.equals(StringBuilder) returned false.
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. StringUtils.equals: cs1 == cs2 (both null, identity), either null, heterogeneous CharSequences,
 *    identical vs differing lengths, identical vs differing character sequences.
 * 2. StringUtils.equalsIgnoreCase: null checks, length differences, case-insensitive match paths.
 * 3. Empty & Blank checks: null, empty (""), pure whitespace, control characters, mixed sequences.
 * 4. Trimming & Stripping: null, empty, all-whitespace, leading/trailing targets, surrogate characters.
 * 5. IndexOf & Search variants: standard, ordinal, case-insensitive, surrogate pairs, negative start positions.
 * 6. Set Matching (Any, None, Only): null arrays/strings, BMP characters vs high/low surrogate pairs.
 * 7. Substrings & Delimiters: negative offsets, inverted bounds, overlapping separators, empty tags.
 * 8. Split & Tokenize: null delimiters (whitespace fallback), preserveAllTokens flags, max limits,
 *    whole-separator matching, character type transitions (lower, upper, title, digits, camelCase).
 * 9. Join & Pad: Iterable/Iterator/Array boundaries, null elements, pad limits (> 8192 threshold).
 * 10. Replace & Mutate: single vs multiple replacement, max replacements, circular reference detection
 *     in replaceEachRepeatedly (IllegalStateException on timeToLive < 0).
 * 11. Levenshtein Distance: unbounded vs thresholded, swap optimizations (n > m), stripe boundaries.
 */
public class StringUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndBlankChecks() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   \t\r\n"));
        assertFalse(StringUtils.isBlank("  a  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("  a  "));
    }

    @Test(timeout = 4000)
    public void testTrimAndStripOperations() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("   abc   "));
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));

        assertEquals("abc", StringUtils.strip("yxabcxy", "xyz"));
        assertEquals("abcxy", StringUtils.stripStart("yxabcxy", "xyz"));
        assertEquals("yxabc", StringUtils.stripEnd("yxabcxy", "xyz"));

        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertEquals("abc", StringUtils.stripStart("abc", ""));
        assertEquals("abc", StringUtils.stripEnd("abc", ""));

        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(" a ", "  b", null));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"-a-", "+b+"}, "-+"));
        assertNull(StringUtils.stripAll((String[]) null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
    }

    @Test(timeout = 4000)
    public void testStripAccents() {
        assertNull(StringUtils.stripAccents(null));
        assertEquals("", StringUtils.stripAccents(""));
        assertEquals("eclair", StringUtils.stripAccents("éclair"));
        assertEquals("Bernoullie", StringUtils.stripAccents("Bernoullié"));
    }

    @Test(timeout = 4000)
    public void testSubstringsAndBoundaries() {
        assertNull(StringUtils.substring(null, 1));
        assertEquals("", StringUtils.substring("", 1));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("abc", StringUtils.substring("abc", -5));
        assertEquals("", StringUtils.substring("abc", 5));

        assertNull(StringUtils.substring(null, 1, 2));
        assertEquals("", StringUtils.substring("abc", 2, 1));
        assertEquals("b", StringUtils.substring("abc", 1, 2));
        assertEquals("bc", StringUtils.substring("abc", -2, 3));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
        assertEquals("c", StringUtils.substring("abc", 2, 8));
        assertEquals("", StringUtils.substring("abc", -2, -3));

        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));
        assertEquals("", StringUtils.left("abc", -1));
        assertNull(StringUtils.left(null, 2));

        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));
        assertEquals("", StringUtils.right("abc", -1));
        assertNull(StringUtils.right(null, 2));

        assertEquals("b", StringUtils.mid("abc", 1, 1));
        assertEquals("bc", StringUtils.mid("abc", 1, 5));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
        assertEquals("", StringUtils.mid("abc", 5, 2));
        assertEquals("", StringUtils.mid("abc", 1, -1));
        assertNull(StringUtils.mid(null, 1, 1));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAfterAndBetween() {
        assertEquals("a", StringUtils.substringBefore("a.b.c", "."));
        assertEquals("a.b.c", StringUtils.substringBefore("a.b.c", "x"));
        assertEquals("", StringUtils.substringBefore("a.b.c", ""));
        assertEquals("a.b.c", StringUtils.substringBefore("a.b.c", null));
        assertNull(StringUtils.substringBefore(null, "."));

        assertEquals("b.c", StringUtils.substringAfter("a.b.c", "."));
        assertEquals("", StringUtils.substringAfter("a.b.c", "x"));
        assertEquals("a.b.c", StringUtils.substringAfter("a.b.c", ""));
        assertEquals("", StringUtils.substringAfter("a.b.c", null));
        assertNull(StringUtils.substringAfter(null, "."));

        assertEquals("a.b", StringUtils.substringBeforeLast("a.b.c", "."));
        assertEquals("a.b.c", StringUtils.substringBeforeLast("a.b.c", "x"));
        assertEquals("a.b.c", StringUtils.substringBeforeLast("a.b.c", ""));
        assertNull(StringUtils.substringBeforeLast(null, "."));

        assertEquals("c", StringUtils.substringAfterLast("a.b.c", "."));
        assertEquals("", StringUtils.substringAfterLast("a.b.c", "x"));
        assertEquals("", StringUtils.substringAfterLast("a.b.c", ""));
        assertNull(StringUtils.substringAfterLast(null, "."));

        assertEquals("b", StringUtils.substringBetween("[b]", "[", "]"));
        assertNull(StringUtils.substringBetween("[b]", "[", "}"));
        assertEquals("b", StringUtils.substringBetween("tagbtag", "tag"));
        assertNull(StringUtils.substringBetween(null, "[", "]"));

        assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a][b]", "[", "]"));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertNull(StringUtils.substringsBetween("[a]", "", "]"));
        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("no match here", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplittingMethods() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b  c"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a::b::c", ":"));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b;c", ",;"));
        assertNull(StringUtils.split(null));

        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"", "a", ""}, StringUtils.splitPreserveAllTokens(":a:", ':'));
        assertArrayEquals(new String[]{"a", "b", ""}, StringUtils.splitPreserveAllTokens("a  b ", null));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.splitPreserveAllTokens("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"a", "", "b:c"}, StringUtils.splitPreserveAllTokens("a::b:c", ":", 3));
        assertNull(StringUtils.splitPreserveAllTokens(null));

        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.splitByWholeSeparator("a  b", null));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.splitByWholeSeparator("a  b", ""));
        assertNull(StringUtils.splitByWholeSeparator(null, "-"));

        assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("a--b", "-"));
        assertArrayEquals(new String[]{"a", "-b"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("a--b", "-", 2));
        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, "-"));

        assertArrayEquals(new String[]{"foo", "Bar", "200"}, StringUtils.splitByCharacterTypeCamelCase("fooBar200"));
        assertArrayEquals(new String[]{"FOO", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("FOOBar"));
        assertArrayEquals(new String[]{"foo", "BAR"}, StringUtils.splitByCharacterType("fooBAR"));
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
    }

    @Test(timeout = 4000)
    public void testJoinMethods() {
        assertEquals("a;b;c", StringUtils.join(new String[]{"a", "b", "c"}, ';'));
        assertEquals("abc", StringUtils.join("a", "b", "c"));
        assertEquals("a--b--c", StringUtils.join(new String[]{"a", "b", "c"}, "--"));
        assertEquals("a,b", StringUtils.join(new String[]{"x", "a", "b", "y"}, ",", 1, 3));
        assertEquals("", StringUtils.join(new String[]{"a"}, ",", 1, 1));
        assertNull(StringUtils.join((Object[]) null, ','));
        assertNull(StringUtils.join((Object[]) null, ","));

        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtils.join(list.iterator(), ','));
        assertEquals("a--b--c", StringUtils.join(list.iterator(), "--"));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ','));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), "-"));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), ','));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), "-"));
        assertNull(StringUtils.join((Iterator<?>) null, ','));
        assertNull(StringUtils.join((Iterator<?>) null, "-"));

        assertEquals("a,b,c", StringUtils.join((Iterable<?>) list, ','));
        assertEquals("a--b--c", StringUtils.join((Iterable<?>) list, "--"));
        assertNull(StringUtils.join((Iterable<?>) null, ','));
        assertNull(StringUtils.join((Iterable<?>) null, ","));
    }

    @Test(timeout = 4000)
    public void testReplaceAndRemoveOperations() {
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("aya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
        assertEquals("abcba", StringUtils.replaceChars("abcba", "z", "y"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "y"));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
        assertEquals("aba", StringUtils.replace("aba", "a", "z", 0));
        assertEquals("aba", StringUtils.replace("aba", "", "z", 2));

        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertEquals("abcde", StringUtils.replaceEach("abcde", new String[0], new String[0]));
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));

        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("queued", StringUtils.remove("queued", 'z'));

        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertNull(StringUtils.overlay(null, "zzzz", 0, 1));
    }

    @Test(timeout = 4000)
    public void testPaddingAndCentering() {
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("ab, ab, ab", StringUtils.repeat("ab", ", ", 3));
        assertEquals("eee", StringUtils.repeat('e', 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("", StringUtils.repeat("ab", 0));
        assertEquals("", StringUtils.repeat("ab", -2));

        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertEquals("bat", StringUtils.rightPad("bat", 2));

        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", 2));

        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("abcd", StringUtils.center("abcd", 2));
    }

    @Test(timeout = 4000)
    public void testChopAndChomp() {
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc\n", StringUtils.chomp("abc\n\r"));
        assertEquals("", StringUtils.chomp("\r\n"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("a", StringUtils.chomp("a"));
        assertNull(StringUtils.chomp(null));

        @SuppressWarnings("deprecation")
        String chompResult = StringUtils.chomp("foobar", "bar");
        assertEquals("foo", chompResult);

        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("", StringUtils.chop(""));
        assertNull(StringUtils.chop(null));
    }

    @Test(timeout = 4000)
    public void testCaseConversionsAndCapitalization() {
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));
        assertNull(StringUtils.upperCase(null));

        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));
        assertNull(StringUtils.lowerCase(null));

        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAt", StringUtils.capitalize("cAt"));
        assertEquals("", StringUtils.capitalize(""));
        assertNull(StringUtils.capitalize(null));

        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));
        assertEquals("", StringUtils.uncapitalize(""));
        assertNull(StringUtils.uncapitalize(null));

        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
        assertNull(StringUtils.swapCase(null));
    }

    @Test(timeout = 4000)
    public void testAbbreviationAndDifferences() {
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("...ghij...", StringUtils.abbreviate("abcdefghijklmno", 6, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));
        assertNull(StringUtils.abbreviate(null, 5));

        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));

        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("abc", StringUtils.difference(null, "abc"));
        assertEquals("abc", StringUtils.difference("abc", null));

        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));

        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot", "i am a mutant"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfDifference("abc", "abc", "abc"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfDifference(new String[]{}));
        assertEquals(0, StringUtils.indexOfDifference("abc", null));

        assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
        assertEquals("abc", StringUtils.getCommonPrefix("abc", "abc"));
        assertEquals("", StringUtils.getCommonPrefix("abc", "xyz"));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{}));
        assertEquals("", StringUtils.getCommonPrefix(null, "abc"));
    }

    @Test(timeout = 4000)
    public void testLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));

        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 8));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "", 6));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo", 6));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("hippo", "elephant", 6));
    }

    @Test(timeout = 4000)
    public void testPrefixAndSuffixMatching() {
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abc", null));
        assertFalse(StringUtils.startsWith("abc", "abcdef"));

        assertTrue(StringUtils.startsWithAny("abcxyz", "def", "abc"));
        assertFalse(StringUtils.startsWithAny("abcxyz", "def", "xyz"));
        assertFalse(StringUtils.startsWithAny("", "def"));
        assertFalse(StringUtils.startsWithAny("abc", (String[]) null));

        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("def", null));
        assertFalse(StringUtils.endsWith("def", "abcdef"));

        assertTrue(StringUtils.endsWithAny("abcxyz", "def", "xyz"));
        assertFalse(StringUtils.endsWithAny("abcxyz", "def", "abc"));
        assertFalse(StringUtils.endsWithAny("", "def"));
        assertFalse(StringUtils.endsWithAny("abc", (String[]) null));
    }

    @Test(timeout = 4000)
    public void testCharacterTestsAndDefaults() {
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));
        assertFalse(StringUtils.isAlpha(""));
        assertFalse(StringUtils.isAlpha(null));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));

        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));
        assertFalse(StringUtils.isAlphanumeric(""));

        assertTrue(StringUtils.isAlphanumericSpace("ab 2c"));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertFalse(StringUtils.isAlphanumericSpace("ab-c"));

        assertTrue(StringUtils.isAsciiPrintable(" !~"));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));
        assertFalse(StringUtils.isAsciiPrintable(null));

        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertFalse(StringUtils.isNumeric(""));

        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertTrue(StringUtils.isNumericSpace(""));
        assertFalse(StringUtils.isNumericSpace("12.3"));

        assertTrue(StringUtils.isWhitespace("  \t"));
        assertTrue(StringUtils.isWhitespace(""));
        assertFalse(StringUtils.isWhitespace(" a "));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));
        assertFalse(StringUtils.isAllLowerCase(""));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
        assertFalse(StringUtils.isAllUpperCase(""));

        assertEquals("", StringUtils.defaultString(null));
        assertEquals("bat", StringUtils.defaultString("bat"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("bat", StringUtils.defaultString("bat", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("  ", "NULL"));
        assertEquals("bat", StringUtils.defaultIfBlank("bat", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals(" ", StringUtils.defaultIfEmpty(" ", "NULL"));

        assertEquals("tab", StringUtils.reverse("bat"));
        assertNull(StringUtils.reverse(null));

        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
        assertNull(StringUtils.reverseDelimited(null, '.'));

        assertEquals("abc", StringUtils.deleteWhitespace(" a b  c "));
        assertEquals("a b c", StringUtils.normalizeSpace(" \t  a \n b   c\r  "));
        assertNull(StringUtils.normalizeSpace(null));
        assertEquals(3, StringUtils.length("abc"));
        assertEquals(0, StringUtils.length(null));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(0, StringUtils.countMatches("abba", ""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIndexOfAndContainsBoundaries() {
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOf(null, 'a'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));

        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOf(null, "a"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));

        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.ordinalIndexOf("aabaabaa", "a", 10));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.ordinalIndexOf("aabaabaa", "a", 0));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 2));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 10));

        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfIgnoreCase(null, "A"));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOf("", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOf("aabaabaa", 'b', -1));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOf("aabaabaa", "b", -1));

        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));

        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
        assertFalse(StringUtils.contains(null, 'a'));
        assertTrue(StringUtils.contains("abc", "a"));
        assertTrue(StringUtils.contains("abc", ""));
        assertFalse(StringUtils.contains(null, "a"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
        assertTrue(StringUtils.containsWhitespace("a b"));
        assertFalse(StringUtils.containsWhitespace("abc"));
        assertFalse(StringUtils.containsWhitespace(null));
    }

    @Test(timeout = 4000)
    public void testSetMatchingSurrogatesAndBoundaries() {
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny("aba", 'z'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny(null, 'z'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny("", 'z'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny("aba", (char[]) null));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertFalse(StringUtils.containsAny("aba", 'z'));
        assertFalse(StringUtils.containsAny(null, 'z'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny("aba", (String) null));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAnyBut("aba", 'a', 'b'));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAnyBut("aba", "ab"));

        assertTrue(StringUtils.containsOnly("abab", 'a', 'b'));
        assertFalse(StringUtils.containsOnly("ab1", 'a', 'b'));
        assertTrue(StringUtils.containsOnly("", 'a', 'b'));
        assertFalse(StringUtils.containsOnly("ab", (char[]) null));
        assertTrue(StringUtils.containsOnly("abab", "ab"));

        assertTrue(StringUtils.containsNone("abab", 'x', 'y'));
        assertFalse(StringUtils.containsNone("abz", 'x', 'z'));
        assertTrue(StringUtils.containsNone("", 'x'));
        assertTrue(StringUtils.containsNone(null, 'x'));
        assertTrue(StringUtils.containsNone("abab", "xyz"));

        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", ""));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny("zzabyycdxx", "mn", "op"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAny(null, "ab"));

        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", ""));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", "op"));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.lastIndexOfAny(null, "ab"));

        // Supplementary / Surrogate pair testing
        String highLow = "\uD83D\uDE00"; // UTF-16 surrogate pair
        char high = highLow.charAt(0);
        char low = highLow.charAt(1);

        assertTrue(StringUtils.containsAny(highLow, high, low));
        assertTrue(StringUtils.containsAny(highLow, high));
        assertEquals(0, StringUtils.indexOfAny(highLow, high, low));
        assertTrue(StringUtils.containsNone(highLow, 'a', 'b'));
        assertFalse(StringUtils.containsNone(highLow, high, low));
        assertEquals(StringUtils.INDEX_NOT_FOUND, StringUtils.indexOfAnyBut(highLow, high, low));
        assertEquals(0, StringUtils.indexOfAnyBut(highLow, 'a', 'b'));
    }

    @Test(timeout = 4000)
    public void testPadLimitExceeded() {
        String padded = StringUtils.leftPad("a", 8200, 'b');
        assertEquals(8200, padded.length());
        assertTrue(padded.startsWith("bbb"));
        assertTrue(padded.endsWith("a"));

        String rightPadded = StringUtils.rightPad("a", 8200, 'b');
        assertEquals(8200, rightPadded.length());
        assertTrue(rightPadded.startsWith("a"));
        assertTrue(rightPadded.endsWith("bbb"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where StringUtils.equals(CharSequence, CharSequence) used cs1.equals(cs2),
     * which fails when comparing a StringBuilder to another StringBuilder or to a String,
     * despite both containing identical character sequences.
     */
    @Test(timeout = 4000)
    public void testEqualsDefectWithHeterogeneousCharSequences() {
        CharSequence cs1 = new StringBuilder("testEqualsCharSequence");
        CharSequence cs2 = new StringBuilder("testEqualsCharSequence");
        CharSequence cs3 = "testEqualsCharSequence";
        CharSequence cs4 = CharBuffer.wrap("testEqualsCharSequence");

        assertTrue("Two distinct StringBuilder instances with identical sequences must be equal",
                StringUtils.equals(cs1, cs2));
        assertTrue("String and StringBuilder with identical sequences must be equal",
                StringUtils.equals(cs3, cs1));
        assertTrue("StringBuilder and String with identical sequences must be equal",
                StringUtils.equals(cs1, cs3));
        assertTrue("CharBuffer and StringBuilder with identical sequences must be equal",
                StringUtils.equals(cs4, cs1));

        CharSequence diff1 = new StringBuilder("diffA");
        CharSequence diff2 = new StringBuilder("diffB");
        assertFalse("Different sequences must return false", StringUtils.equals(diff1, diff2));
        assertFalse("Different length sequences must return false",
                StringUtils.equals(diff1, new StringBuilder("diffLonger")));
    }

    @Test(timeout = 4000)
    public void testEqualsNullAndSameReferences() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals("abc", null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "ABCD"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateWidthTooSmall() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetWidthTooSmall() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullInputFirst() {
        StringUtils.getLevenshteinDistance(null, "valid");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullInputSecond() {
        StringUtils.getLevenshteinDistance("valid", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNegativeThreshold() {
        StringUtils.getLevenshteinDistance("valid", "valid", -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachMismatchedArrays() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"z"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachRepeatedlyCircularLoop() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    @Test(expected = UnsupportedEncodingException.class, timeout = 4000)
    public void testToStringUnsupportedEncoding() throws UnsupportedEncodingException {
        StringUtils.toString("hello".getBytes(), "NON_EXISTENT_CHARSET_NAME_12345");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToStringNullBytes() throws UnsupportedEncodingException {
        StringUtils.toString(null, "UTF-8");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        StringUtils instance = new StringUtils();
        assertNotNull("JavaBean constructor should instantiate successfully", instance);
        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }

    @Test(timeout = 4000)
    public void testToStringValidEncoding() throws UnsupportedEncodingException {
        byte[] bytes = "Test StringUtils.toString()".getBytes("UTF-8");
        assertEquals("Test StringUtils.toString()", StringUtils.toString(bytes, "UTF-8"));
        assertEquals("Test StringUtils.toString()", StringUtils.toString(bytes, null));
    }
}