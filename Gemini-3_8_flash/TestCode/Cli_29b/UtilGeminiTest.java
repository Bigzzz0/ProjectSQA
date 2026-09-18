/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Method: stripLeadingHyphens(String str)
 * - Branch 1: str == null -> returns null
 * - Branch 2: str.startsWith("--") == true -> returns substring(2)
 * - Branch 3: str.startsWith("-") == true -> returns substring(1)
 * - Branch 4: default fallthrough -> returns unchanged str
 * - Boundaries: null, "", "-", "--", "---", alphanumeric, mixed hyphens
 *
 * Method: stripLeadingAndTrailingQuotes(String str)
 * - Branch 1: str.startsWith("\"") == true vs false
 * - Branch 2: str.endsWith("\"") == true vs false
 * - Known Defect (Defects4J / CLI): The method removes a trailing quote unconditionally
 *   even when the string did NOT start with a quote, e.g., 'foo "bar"' becomes 'foo "bar'.
 *   Quotes should only be stripped if they enclose the entire token as matching pairs.
 * - Boundaries: "", "\"", "\"\"", "\"foo\"", "foo \"bar\"", "\"foo", "foo\""
 */
public class UtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStripLeadingHyphensSingleHyphen() {
        String input = "-option";
        String expected = "option";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleHyphen() {
        String input = "--option";
        String expected = "option";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensNone() {
        String input = "option";
        String expected = "option";
        String actual = Util.stripLeadingHyphens(input);
        assertSame(input, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesPaired() {
        String input = "\"hello\"";
        String expected = "hello";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesWithSpaces() {
        String input = "\"one two\"";
        String expected = "one two";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesWithoutQuotes() {
        String input = "simpleString";
        String expected = "simpleString";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStripLeadingHyphensNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensEmpty() {
        String actual = Util.stripLeadingHyphens("");
        assertEquals("", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensOnlySingleHyphen() {
        String actual = Util.stripLeadingHyphens("-");
        assertEquals("", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensOnlyDoubleHyphen() {
        String actual = Util.stripLeadingHyphens("--");
        assertEquals("", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensTripleHyphen() {
        String actual = Util.stripLeadingHyphens("---opt");
        assertEquals("-opt", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensEmbeddedHyphen() {
        String actual = Util.stripLeadingHyphens("foo-bar");
        assertEquals("foo-bar", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleLeadingAndEmbedded() {
        String actual = Util.stripLeadingHyphens("--foo-bar-baz");
        assertEquals("foo-bar-baz", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesEmpty() {
        String actual = Util.stripLeadingAndTrailingQuotes("");
        assertEquals("", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesTwoQuotesOnly() {
        String actual = Util.stripLeadingAndTrailingQuotes("\"\"");
        assertEquals("", actual);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * - org.apache.commons.cli.UtilTest::testStripLeadingAndTrailingQuotes
     *   junit.framework.ComparisonFailure: expected:<foo "bar["]> but was:<foo "bar[]>
     *
     * A string that does not start with a quote, but contains internal or trailing quotes,
     * must NOT have its trailing quote stripped.
     */
    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesTrailingOnlyExposingDefect() {
        String input = "foo \"bar\"";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("foo \"bar\"", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesLeadingOnly() {
        String input = "\"foo bar";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("\"foo bar", actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesSingleQuoteOnly() {
        String input = "\"";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("\"", actual);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testStripLeadingAndTrailingQuotesNullThrowsNpe() {
        Util.stripLeadingAndTrailingQuotes(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testUtilInstantiation() {
        Util util = new Util();
        assertNotNull(util);
    }
}