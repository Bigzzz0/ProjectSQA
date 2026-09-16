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

package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.apache.commons.codec.Charsets;
import org.junit.Test;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.codec.binary.StringUtils
 *
 * Branch & Path Coverage Map:
 * - equals(CharSequence, CharSequence):
 *   * Branch 1: cs1 == cs2 (identical refs, both null) -> true
 *   * Branch 2: cs1 == null || cs2 == null (one null, one non-null) -> false
 *   * Branch 3: cs1 instanceof String && cs2 instanceof String (String fast path) -> cs1.equals(cs2)
 *   * Branch 4: Non-String CharSequences of equal length, equal contents -> true
 *   * Branch 5: Non-String CharSequences of equal length, differing contents -> false
 *   * Branch 6: Defect-targeted path: CharSequences of different lengths (e.g. String vs StringBuilder)
 *               Defects4J flaw: Math.max(cs1.length(), cs2.length()) causes StringIndexOutOfBoundsException
 *               on the shorter sequence.
 * - getByteBufferUtf8(String):
 *   * Branch 1: string == null -> null
 *   * Branch 2: string != null -> wrapped ByteBuffer with UTF-8 payload
 * - getBytesIso8859_1, getBytesUsAscii, getBytesUtf16, getBytesUtf16Be, getBytesUtf16Le, getBytesUtf8:
 *   * Branch 1: string == null -> null
 *   * Branch 2: string != null -> valid byte array
 * - getBytesUnchecked(String, String):
 *   * Branch 1: string == null -> null
 *   * Branch 2: valid charsetName -> encoded bytes
 *   * Branch 3: invalid/unsupported charsetName -> throws IllegalStateException with nested cause
 * - newString(byte[], String):
 *   * Branch 1: bytes == null -> null
 *   * Branch 2: valid charsetName -> decoded String
 *   * Branch 3: invalid/unsupported charsetName -> throws IllegalStateException with nested cause
 * - newStringIso8859_1, newStringUsAscii, newStringUtf16, newStringUtf16Be, newStringUtf16Le, newStringUtf8:
 *   * Branch 1: bytes == null -> null
 *   * Branch 2: bytes != null -> decoded String
 * - Constructor:
 *   * StringUtils() default constructor invocation
 */
public class StringUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsBothStringsEqualAndUnequal() {
        assertTrue(StringUtils.equals("testString", "testString"));
        assertFalse(StringUtils.equals("testString", "differentString"));
        assertFalse(StringUtils.equals("testString", "TestString")); // Case-sensitive
    }

    @Test(timeout = 4000)
    public void testEqualsNonStringCharSequencesEqual() {
        StringBuilder sb1 = new StringBuilder("commonValue");
        StringBuilder sb2 = new StringBuilder("commonValue");
        assertTrue(StringUtils.equals(sb1, sb2));

        StringBuffer buf = new StringBuffer("commonValue");
        assertTrue(StringUtils.equals(sb1, buf));
        assertTrue(StringUtils.equals("commonValue", sb1));
        assertTrue(StringUtils.equals(sb1, "commonValue"));
    }

    @Test(timeout = 4000)
    public void testEqualsNonStringCharSequencesSameLengthDifferentContent() {
        StringBuilder sb1 = new StringBuilder("valueA");
        StringBuilder sb2 = new StringBuilder("valueB");
        assertFalse(StringUtils.equals(sb1, sb2));
        assertFalse(StringUtils.equals("valueA", sb2));
        assertFalse(StringUtils.equals(sb1, "valueB"));
    }

    @Test(timeout = 4000)
    public void testByteBufferUtf8() {
        assertNull(StringUtils.getByteBufferUtf8(null));
        ByteBuffer buffer = StringUtils.getByteBufferUtf8("Hello World");
        assertNotNull(buffer);
        assertEquals("Hello World", new String(buffer.array(), Charsets.UTF_8));
    }

    @Test(timeout = 4000)
    public void testStandardCharsetsRoundTrip() {
        String testText = "Quick brown fox jumps over the lazy dog 12345!@#$%^&*()_+";

        // ISO-8859-1
        byte[] isoBytes = StringUtils.getBytesIso8859_1(testText);
        assertEquals(testText, StringUtils.newStringIso8859_1(isoBytes));

        // US-ASCII
        byte[] asciiBytes = StringUtils.getBytesUsAscii(testText);
        assertEquals(testText, StringUtils.newStringUsAscii(asciiBytes));

        // UTF-8
        byte[] utf8Bytes = StringUtils.getBytesUtf8(testText);
        assertEquals(testText, StringUtils.newStringUtf8(utf8Bytes));

        // UTF-16
        byte[] utf16Bytes = StringUtils.getBytesUtf16(testText);
        assertEquals(testText, StringUtils.newStringUtf16(utf16Bytes));

        // UTF-16BE
        byte[] utf16BeBytes = StringUtils.getBytesUtf16Be(testText);
        assertEquals(testText, StringUtils.newStringUtf16Be(utf16BeBytes));

        // UTF-16LE
        byte[] utf16LeBytes = StringUtils.getBytesUtf16Le(testText);
        assertEquals(testText, StringUtils.newStringUtf16Le(utf16LeBytes));
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedAndNewStringValid() {
        byte[] bytes = StringUtils.getBytesUnchecked("sample", "UTF-8");
        assertNotNull(bytes);
        String result = StringUtils.newString(bytes, "UTF-8");
        assertEquals("sample", result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsNullBoundaries() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertFalse(StringUtils.equals(null, new StringBuilder("abc")));
        assertFalse(StringUtils.equals(new StringBuilder("abc"), null));
    }

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        String str = "sameRef";
        assertTrue(StringUtils.equals(str, str));

        StringBuilder sb = new StringBuilder("sameRefSb");
        assertTrue(StringUtils.equals(sb, sb));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptySequences() {
        assertTrue(StringUtils.equals("", ""));
        assertTrue(StringUtils.equals("", new StringBuilder("")));
        assertTrue(StringUtils.equals(new StringBuilder(""), ""));
        assertTrue(StringUtils.equals(new StringBuilder(""), new StringBuffer("")));
    }

    @Test(timeout = 4000)
    public void testGetBytesNullInputs() {
        assertNull(StringUtils.getBytesIso8859_1(null));
        assertNull(StringUtils.getBytesUsAscii(null));
        assertNull(StringUtils.getBytesUtf8(null));
        assertNull(StringUtils.getBytesUtf16(null));
        assertNull(StringUtils.getBytesUtf16Be(null));
        assertNull(StringUtils.getBytesUtf16Le(null));
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNewStringNullInputs() {
        assertNull(StringUtils.newStringIso8859_1(null));
        assertNull(StringUtils.newStringUsAscii(null));
        assertNull(StringUtils.newStringUtf8(null));
        assertNull(StringUtils.newStringUtf16(null));
        assertNull(StringUtils.newStringUtf16Be(null));
        assertNull(StringUtils.newStringUtf16Le(null));
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testEmptyByteArrayConversions() {
        byte[] empty = new byte[0];
        assertEquals("", StringUtils.newStringIso8859_1(empty));
        assertEquals("", StringUtils.newStringUsAscii(empty));
        assertEquals("", StringUtils.newStringUtf8(empty));
        assertEquals("", StringUtils.newStringUtf16(empty));
        assertEquals("", StringUtils.newStringUtf16Be(empty));
        assertEquals("", StringUtils.newStringUtf16Le(empty));
        assertEquals("", StringUtils.newString(empty, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testEmptyStringConversions() {
        assertEquals(0, StringUtils.getBytesIso8859_1("").length);
        assertEquals(0, StringUtils.getBytesUsAscii("").length);
        assertEquals(0, StringUtils.getBytesUtf8("").length);
        assertEquals(0, StringUtils.getBytesUtf16("").length);
        assertEquals(0, StringUtils.getBytesUtf16Be("").length);
        assertEquals(0, StringUtils.getBytesUtf16Le("").length);
        assertEquals(0, StringUtils.getBytesUnchecked("", "UTF-8").length);
        assertEquals(0, StringUtils.getByteBufferUtf8("").remaining());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J issue where comparing CharSequences of unequal lengths
     * causes a StringIndexOutOfBoundsException when cs1 is shorter than cs2.
     */
    @Test(timeout = 4000)
    public void testEqualsCS1_DefectTarget() {
        CharSequence cs1 = "abc";
        CharSequence cs2 = new StringBuilder("abcd");
        assertFalse(StringUtils.equals(cs1, cs2));
    }

    /**
     * Targets Defects4J issue where comparing CharSequences of unequal lengths
     * causes a StringIndexOutOfBoundsException when cs2 is shorter than cs1.
     */
    @Test(timeout = 4000)
    public void testEqualsCS2_DefectTarget() {
        CharSequence cs1 = new StringBuilder("abc");
        CharSequence cs2 = "abcd";
        assertFalse(StringUtils.equals(cs1, cs2));
    }

    /**
     * Targets unequal length comparison where neither CharSequence is a java.lang.String.
     */
    @Test(timeout = 4000)
    public void testEqualsBothNonStringDifferentLengths() {
        CharSequence cs1 = new StringBuilder("123");
        CharSequence cs2 = new StringBuffer("1234");
        assertFalse(StringUtils.equals(cs1, cs2));
        assertFalse(StringUtils.equals(cs2, cs1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetBytesUncheckedUnsupportedEncodingThrowsIllegalStateException() {
        final String invalidCharset = "INVALID_CHARSET_NAME_XYZ_12345";
        try {
            StringUtils.getBytesUnchecked("anyString", invalidCharset);
            fail("Expected IllegalStateException due to unsupported charset");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(invalidCharset));
            assertNotNull(e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testNewStringUnsupportedEncodingThrowsIllegalStateException() {
        final String invalidCharset = "INVALID_CHARSET_NAME_XYZ_12345";
        try {
            StringUtils.newString(new byte[] { 0x41, 0x42 }, invalidCharset);
            fail("Expected IllegalStateException due to unsupported charset");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(invalidCharset));
            assertNotNull(e.getCause());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        // Explicitly instantiate public default constructor for 100% line coverage
        StringUtils instance = new StringUtils();
        assertNotNull(instance);
    }
}