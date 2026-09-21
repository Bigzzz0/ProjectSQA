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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang3.StringUtils
 * Known Defect: Defects4J Lang testReplace_StringStringArrayStringArray -> NullPointerException
 * Root Cause in replaceEach:
 *   When calculating buffer size adjustment in replaceEach(String, String[], String[], boolean, int):
 *   The loop:
 *       for (int i = 0; i < searchList.length; i++) {
 *           int greater = replacementList[i].length() - searchList[i].length();
 *           ...
 *       }
 *   fails to guard against null elements in searchList or replacementList when a prior match exists,
 *   causing a NullPointerException even though null search/replacement entries are specified to be ignored.
 *
 * Test Partitions:
 *   - Partition A: Core Functional Logic & State Transitions (isEmpty, isBlank, trim, strip, split, join, case, pad)
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes (null, empty, negative index, max bounds)
 *   - Partition C: Defect-Targeted Branch Zone (replaceEach with null entries triggering NPE in buffer estimation)
 *   - Partition D: Exception & Defensive Guard Paths (abbreviate bounds, invalid array sizes in replaceEach)
 *   - Partition E: Object Lifecycle & Contract Integrity (public constructor instantiation)
 */
public class StringUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Targets Defects4J bug in replaceEach:
     * When searchList or replacementList contains null elements, but another non-null
     * element matches the source string, replaceEach attempts to calculate
     * replacementList[i].length() - searchList[i].length() without checking for null,
     * triggering a java.lang.NullPointerException.
     */
    @Test(timeout = 4000)
    public void testDefectReplaceEachWithNullReplacementElements() {
        // searchList has matches ("a"), but replacementList contains null elements
        // According to StringUtils specification, null replacements should be ignored as no-op.
        String result = StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"z", null});
        assertEquals("zba", result);
    }

    @Test(timeout = 4000)
    public void testDefectReplaceEachWithNullSearchElements() {
        // searchList contains null at index 0 and a match at index 1
        String result = StringUtils.replaceEach("aba", new String[]{null, "b"}, new String[]{"z", "w"});
        assertEquals("awa", result);
    }

    @Test(timeout = 4000)
    public void testDefectReplaceEachRepeatedlyWithNullElements() {
        String result = StringUtils.replaceEachRepeatedly("aba", new String[]{null, "b"}, new String[]{"z", "w"});
        assertEquals("awa", result);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STRING OPERATIONS
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
        assertFalse(StringUtils.isBlank("abc"));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("  a  "));
    }

    @Test(timeout = 4000)
    public void testTrimAndStripOperations() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("    "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("    "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("    "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("yxabcba", "xyz"));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("  abc "));

        assertEquals("abc  ", StringUtils.stripStart("yxabc  ", "xyz"));
        assertEquals("  abc", StringUtils.stripEnd("  abcyx", "xyz"));

        String[] strippedAll = StringUtils.stripAll(new String[]{"  a  ", "  b", null}, null);
        assertNotNull(strippedAll);
        assertEquals("a", strippedAll[0]);
        assertEquals("b", strippedAll[1]);
        assertNull(strippedAll[2]);

        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
    }

    @Test(timeout = 4000)
    public void testEqualsAndCompare() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "def"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndContains() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 10));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(8, StringUtils.indexOf("aabaabaa", "", 9));

        assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "b", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", -1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 2));

        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 10));

        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));

        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
        assertFalse(StringUtils.contains(null, 'a'));

        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));
        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));

        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
        assertFalse(StringUtils.containsIgnoreCase(null, "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyAndContainsAny() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[]{'b', 'y'}));
        assertEquals(-1, StringUtils.indexOfAny("aba", new char[]{'z'}));

        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", ""));

        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertFalse(StringUtils.containsAny("aba", new char[]{'z'}));
        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("aba", (char[]) null));

        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny("aba", "z"));
        assertFalse(StringUtils.containsAny("aba", (String) null));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", new char[]{'a', 'b'}));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", (char[]) null));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", (String) null));

        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b', 'c'}));
        assertFalse(StringUtils.containsOnly("ab1", new char[]{'a', 'b', 'c'}));
        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("a", (char[]) null));
        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("a", new char[0]));

        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly("ab1", "abc"));
        assertFalse(StringUtils.containsOnly("abab", (String) null));

        assertTrue(StringUtils.containsNone("abab", new char[]{'x', 'y', 'z'}));
        assertFalse(StringUtils.containsNone("abz", new char[]{'x', 'y', 'z'}));
        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("a", (char[]) null));

        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
        assertTrue(StringUtils.containsNone("abz", (String) null));

        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"mn", "op"}));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String[]) null));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new String[]{null, ""}));

        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn", "op"}));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", (String[]) null));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{null, ""}));
    }

    @Test(timeout = 4000)
    public void testSubstringOperations() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 2));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("", StringUtils.substring("abc", 2, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));

        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 5, 2));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 4));
        assertEquals("b", StringUtils.mid("abc", 1, 1));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAndAfter() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "z"));
        assertEquals("", StringUtils.substringBefore("abc", ""));

        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "z"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));

        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("a", StringUtils.substringBeforeLast("a", null));
        assertEquals("a", StringUtils.substringBeforeLast("a", ""));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("a", StringUtils.substringBeforeLast("a", "z"));

        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("a", "z"));
    }

    @Test(timeout = 4000)
    public void testSubstringBetween() {
        assertNull(StringUtils.substringBetween(null, "tag"));
        assertNull(StringUtils.substringBetween("tagabctag", null));
        assertEquals("", StringUtils.substringBetween("", ""));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));
        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertNull(StringUtils.substringBetween("yabcz", "y", "x"));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("[a]", null, "]"));
        assertNull(StringUtils.substringsBetween("[a]", "[", null));
        assertNull(StringUtils.substringsBetween("[a]", "", "]"));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplitting() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("  abc   def  "));

        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));

        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("ab:cd:ef", ":", 0));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 3));

        assertNull(StringUtils.splitByWholeSeparator(null, ":"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab   de fg", null));

        assertArrayEquals(new String[]{"ab", "", "cd", "ef"}, 
                StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!--!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "-!-cd-!-ef"}, 
                StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!--!-cd-!-ef", "-!-", 2));

        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"", "a", "b", "c", ""}, StringUtils.splitPreserveAllTokens(" a b c ", ' '));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "", "cd:ef"}, StringUtils.splitPreserveAllTokens("ab::cd:ef", ":", 3));

        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
    }

    @Test(timeout = 4000)
    public void testJoining() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("", StringUtils.join(new Object[]{null}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals(";;a", StringUtils.join(new Object[]{null, "", "a"}, ';'));
        assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals("", StringUtils.join(new Object[]{"a", "b", "c"}, "--", 2, 2));

        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a;b;c", StringUtils.join(list.iterator(), ';'));
        assertEquals("a--b--c", StringUtils.join(list.iterator(), "--"));
        assertEquals("a;b;c", StringUtils.join((Iterable<?>) list, ';'));
        assertEquals("a--b--c", StringUtils.join((Iterable<?>) list, "--"));

        assertNull(StringUtils.join((Iterator<?>) null, ';'));
        assertNull(StringUtils.join((Iterator<?>) null, "-"));
        assertNull(StringUtils.join((Iterable<?>) null, ';'));
        assertNull(StringUtils.join((Iterable<?>) null, "-"));

        assertEquals("", StringUtils.join(Collections.emptyIterator(), ';'));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), "-"));
        assertEquals("single", StringUtils.join(Collections.singletonList("single").iterator(), ';'));
        assertEquals("single", StringUtils.join(Collections.singletonList("single").iterator(), "-"));
    }

    @Test(timeout = 4000)
    public void testDeletionAndRemoval() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("abc"));
        assertEquals("abc", StringUtils.deleteWhitespace("   a  b   c   "));

        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));

        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("www.domain.com", "WWW."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));

        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("", StringUtils.removeEnd("", "a"));
        assertEquals("abc", StringUtils.removeEnd("abc", null));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", ".org"));

        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.com", ".COM"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));

        assertNull(StringUtils.remove(null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("abc", StringUtils.remove("abc", null));
        assertEquals("abc", StringUtils.remove("abc", ""));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("queued", StringUtils.remove("queued", "zz"));

        assertNull(StringUtils.remove(null, 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("queued", StringUtils.remove("queued", 'z'));
    }

    @Test(timeout = 4000)
    public void testReplaceOperations() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("", StringUtils.replaceOnce("", "a", "b"));
        assertEquals("any", StringUtils.replaceOnce("any", null, "b"));
        assertEquals("any", StringUtils.replaceOnce("any", "a", null));
        assertEquals("any", StringUtils.replaceOnce("any", "", "b"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));

        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("aba", StringUtils.replace("aba", "a", "z", 0));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z", 2));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z", -1));
        assertEquals("aba", StringUtils.replace("aba", "x", "z"));

        assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        assertNull(StringUtils.replaceChars(null, "a", "b"));
        assertEquals("", StringUtils.replaceChars("", "a", "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", "", "b"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));

        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
    }

    @Test(timeout = 4000)
    public void testChompAndChop() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("", StringUtils.chomp("\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc\r\n", StringUtils.chomp("abc\r\n\r\n"));
        assertEquals("abc\n", StringUtils.chomp("abc\n\r"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("a", StringUtils.chomp("a"));

        assertNull(StringUtils.chomp(null, "foo"));
        assertEquals("", StringUtils.chomp("", "foo"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));
        assertEquals("foo", StringUtils.chomp("foo", null));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("abc", StringUtils.chop("abc\n"));
        assertEquals("abc", StringUtils.chop("abc\r"));
    }

    @Test(timeout = 4000)
    public void testPaddingAndCentering() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("", 2));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("", StringUtils.repeat("a", -1));
        assertEquals("a", StringUtils.repeat("a", 1));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));

        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
        assertNull(StringUtils.repeat(null, ", ", 3));
        assertEquals("aaa", StringUtils.repeat("a", null, 3));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 2));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat", StringUtils.leftPad("bat", 2));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));

        assertEquals(0, StringUtils.length(null));
        assertEquals(3, StringUtils.length("abc"));

        assertNull(StringUtils.center(null, 5));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" a  ", StringUtils.center("a", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
    }

    @Test(timeout = 4000)
    public void testCaseConversions() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertEquals("ABC", StringUtils.upperCase("aBc", Locale.ENGLISH));
        assertNull(StringUtils.upperCase(null, Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
        assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));
        assertNull(StringUtils.lowerCase(null, Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAt", StringUtils.capitalize("cAt"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    @Test(timeout = 4000)
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("", "a"));
        assertEquals(0, StringUtils.countMatches("abba", null));
        assertEquals(0, StringUtils.countMatches("abba", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));
        assertEquals(0, StringUtils.countMatches("abba", "xxx"));
    }

    @Test(timeout = 4000)
    public void testCharacterTypeChecks() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertFalse(StringUtils.isAlpha("  "));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));

        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("  "));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertFalse(StringUtils.isAlphanumeric("  "));
        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("ab 2 c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-c"));

        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable(" !~"));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12 3"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12-3"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace(" \t \r\n "));
        assertFalse(StringUtils.isWhitespace("  a  "));

        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test(timeout = 4000)
    public void testDefaultAndReverse() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("bat", StringUtils.defaultString("bat"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("bat", StringUtils.defaultString("bat", "default"));

        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("bat", StringUtils.defaultIfEmpty("bat", "default"));

        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("tab", StringUtils.reverse("bat"));

        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("", StringUtils.reverseDelimited("", '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
    }

    @Test(timeout = 4000)
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 8));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        assertNull(StringUtils.abbreviate(null, 0, 4));
        assertEquals("", StringUtils.abbreviate("", 0, 4));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 4, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ghij...", StringUtils.abbreviate("abcdefghijklmno", 6, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 8, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));
    }

    @Test(timeout = 4000)
    public void testDifferenceAndPrefix() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("abc", StringUtils.difference("", "abc"));
        assertEquals("", StringUtils.difference("abc", ""));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("xyz", StringUtils.difference("ab", "abxyz"));
        assertEquals("xyz", StringUtils.difference("abcde", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(-1, StringUtils.indexOfDifference("", ""));
        assertEquals(0, StringUtils.indexOfDifference("", "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));

        assertEquals(-1, StringUtils.indexOfDifference((String[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[0]));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc", "abc"}));
        assertEquals(1, StringUtils.indexOfDifference(new String[]{"abc", "a"}));
        assertEquals(7, StringUtils.indexOfDifference(new String[]{"i am a machine", "i am a robot"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"abc", null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{null, null}));

        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc"}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc", "abc"}));
        assertEquals("a", StringUtils.getCommonPrefix(new String[]{"abc", "a"}));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", "xyz"}));
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
        assertEquals(8, StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz"));
        assertEquals(1, StringUtils.getLevenshteinDistance("hello", "hallo"));
    }

    @Test(timeout = 4000)
    public void testStartsWithAndEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", "xyz"));

        assertFalse(StringUtils.startsWithAny(null, null));
        assertFalse(StringUtils.startsWithAny("abcxyz", null));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{""}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"abc"}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{null, "xyz", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"def", "ghi"}));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", "xyz"));
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testPadLimitBoundaries() {
        // Exceed PAD_LIMIT (8192) in rightPad and leftPad
        String str = "a";
        String rightPadded = StringUtils.rightPad(str, 8195, 'x');
        assertEquals(8195, rightPadded.length());
        assertTrue(rightPadded.startsWith("a"));
        assertTrue(rightPadded.endsWith("x"));

        String leftPadded = StringUtils.leftPad(str, 8195, 'y');
        assertEquals(8195, leftPadded.length());
        assertTrue(leftPadded.startsWith("y"));
        assertTrue(leftPadded.endsWith("a"));

        String rightPaddedStr = StringUtils.rightPad(str, 8195, "xy");
        assertEquals(8195, rightPaddedStr.length());
        assertTrue(rightPaddedStr.startsWith("a"));

        String leftPaddedStr = StringUtils.leftPad(str, 8195, "xy");
        assertEquals(8195, leftPaddedStr.length());
        assertTrue(leftPaddedStr.endsWith("a"));
    }

    @Test(timeout = 4000)
    public void testRepeatBoundaries() {
        assertEquals("abababab", StringUtils.repeat("ab", 4));
        assertEquals("aaaa", StringUtils.repeat("a", 4));
        // Single char length beyond PAD_LIMIT
        String repeatedLarge = StringUtils.repeat("a", 8200);
        assertEquals(8200, repeatedLarge.length());
    }

    @Test(timeout = 4000)
    public void testReplaceEachBoundaryValues() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[0]));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{""}, new String[]{"z"}));

        // Replacement resulting in length increase
        String enlarged = StringUtils.replaceEach("a", new String[]{"a"}, new String[]{"longerString"});
        assertEquals("longerString", enlarged);

        // Circular replacements without repeat vs with repeat
        assertEquals("dcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
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
    public void testGetLevenshteinDistanceNullFirst() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLevenshteinDistanceNullSecond() {
        StringUtils.getLevenshteinDistance("abc", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachMismatchedLengths() {
        StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"z"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachCircularEndlessLoop() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        // StringUtils constructor is explicitly public for JavaBean tools
        StringUtils instance = new StringUtils();
        assertNotNull(instance);
        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }
}