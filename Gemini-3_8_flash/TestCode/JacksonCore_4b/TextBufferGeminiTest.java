package com.fasterxml.jackson.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.core.util.TextBuffer
 * Targeted Defect: Defects4J / Jackson-core issue where expandCurrentSegment() fails when the
 * current segment length is already at or beyond MAX_SEGMENT_LEN (262144). Specifically, when
 * len == MAX_SEGMENT_LEN (262144), it grows to 262145 (MAX_SEGMENT_LEN + 1). However, on a subsequent
 * call when len == 262145, newLen evaluates to Math.min(MAX_SEGMENT_LEN, len + (len >> 1)), which
 * shrinks back to 262144 rather than expanding further, triggering AssertionFailedError.
 *
 * Core Branch & Logic Coverage Areas:
 * 1. Expand Defect Zone:
 *    - expandCurrentSegment() repeatedly past MAX_SEGMENT_LEN (262144 -> 262145 -> >262145).
 *    - expandCurrentSegment(minSize): minSize <= curr.length vs minSize > curr.length.
 * 2. Shared Buffer Mode & Lifecycle:
 *    - resetWithShared: len=0, len>0, start=0, start>0, unshare via append(char), append(char[]),
 *      append(String), ensureNotShared(), and getCurrentSegment().
 * 3. Segment Aggregation & Growth:
 *    - Single segment vs multi-segment concatenation across finishCurrentSegment().
 *    - append(char[], int, int) and append(String, int, int) partial fitting and multi-segment loop.
 *    - contentsAsString(), contentsAsArray(), size(), getTextOffset(), hasTextAsCharacters(), getTextBuffer().
 * 4. Numeric Conversions:
 *    - contentsAsDecimal() across pre-cut resultArray, shared buffer, single segment, and aggregated segments.
 *    - contentsAsDouble().
 *    - NumberFormatException validation.
 * 5. Memory Management & Recycling:
 *    - releaseBuffers() with BufferRecycler (null and non-null).
 *    - resetWithCopy(), resetWithString(), emptyAndGetCurrentSegment().
 */
public class TextBufferGeminiTest {

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    /**********************************************************
     */

    /**
     * Targets the defect where TextBuffer fails to expand when segment length reaches MAX_SEGMENT_LEN + 1 (262145).
     * On the buggy version, expanding from 262145 results in shrinking back to 262144.
     */
    @Test(timeout = 4000)
    public void testExpandDefectAtMaxSegmentBoundary() {
        TextBuffer tb = new TextBuffer(null);
        char[] buf = tb.getCurrentSegment();
        assertNotNull(buf);

        // Step 1: Expand until reaching MAX_SEGMENT_LEN (262144)
        while (buf.length < TextBuffer.MAX_SEGMENT_LEN) {
            buf = tb.expandCurrentSegment();
        }
        assertEquals(TextBuffer.MAX_SEGMENT_LEN, buf.length);

        // Step 2: First expansion at MAX_SEGMENT_LEN grows to MAX_SEGMENT_LEN + 1 (262145)
        buf = tb.expandCurrentSegment();
        int len262145 = buf.length;
        assertEquals(TextBuffer.MAX_SEGMENT_LEN + 1, len262145);

        // Step 3: Second expansion must expand beyond 262145, NOT shrink back to 262144
        buf = tb.expandCurrentSegment();
        assertTrue("Expected buffer of " + len262145 + " to expand, did not, length now " + buf.length,
                buf.length > len262145);
    }

    @Test(timeout = 4000)
    public void testExpandCurrentSegmentWithMinSize() {
        TextBuffer tb = new TextBuffer(null);
        char[] initial = tb.getCurrentSegment();
        int initialLen = initial.length;

        // If minSize is smaller or equal, return same array
        char[] same = tb.expandCurrentSegment(initialLen - 10);
        assertSame(initial, same);

        same = tb.expandCurrentSegment(initialLen);
        assertSame(initial, same);

        // If minSize is larger, return expanded copy
        char[] expanded = tb.expandCurrentSegment(initialLen + 500);
        assertNotSame(initial, expanded);
        assertTrue(expanded.length >= initialLen + 500);
        assertEquals(expanded.length, tb.getCurrentSegment().length);
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testInitialStateAndEmptyBuffer() {
        TextBuffer tb = new TextBuffer(null);
        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(new char[0], tb.contentsAsArray());
        assertEquals("", tb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSingleChars() {
        TextBuffer tb = new TextBuffer(null);
        tb.append('a');
        tb.append('b');
        tb.append('c');

        assertEquals(3, tb.size());
        assertEquals("abc", tb.contentsAsString());
        assertArrayEquals(new char[]{'a', 'b', 'c'}, tb.contentsAsArray());
        assertEquals(3, tb.size());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayAndString() {
        TextBuffer tb = new TextBuffer(null);
        tb.append(new char[]{'H', 'e', 'l', 'l', 'o', ' '}, 0, 6);
        tb.append("World!", 0, 6);

        assertEquals(12, tb.size());
        assertEquals("Hello World!", tb.contentsAsString());
        assertEquals("Hello World!", tb.toString());
        assertArrayEquals("Hello World!".toCharArray(), tb.contentsAsArray());
    }

    @Test(timeout = 4000)
    public void testSharedBufferTransitionAndUnsharing() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "Shared Content Here".toCharArray();
        tb.resetWithShared(source, 7, 7); // "Content"

        assertEquals(7, tb.size());
        assertEquals(7, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(source, tb.getTextBuffer());
        assertEquals("Content", tb.contentsAsString());

        // Unshare by appending a char
        tb.append('!');
        assertEquals(8, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("Content!", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testUnshareViaEnsureNotShared() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "ABCDEF".toCharArray();
        tb.resetWithShared(source, 2, 3); // "CDE"
        assertEquals(2, tb.getTextOffset());

        tb.ensureNotShared();
        assertEquals(0, tb.getTextOffset());
        assertEquals(3, tb.size());
        assertEquals("CDE", tb.contentsAsString());

        // Redundant ensureNotShared call
        tb.ensureNotShared();
        assertEquals("CDE", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testUnshareViaAppendCharArrayAndString() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "BaseString".toCharArray();

        tb.resetWithShared(source, 0, 4); // "Base"
        tb.append(new char[]{'b', 'a', 'l', 'l'}, 0, 4);
        assertEquals("Baseball", tb.contentsAsString());

        tb.resetWithShared(source, 4, 6); // "String"
        tb.append("Theory", 0, 6);
        assertEquals("StringTheory", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testUnshareViaGetCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "DirectSegment".toCharArray();
        tb.resetWithShared(source, 0, 6); // "Direct"

        char[] curr = tb.getCurrentSegment();
        assertNotNull(curr);
        assertEquals(0, tb.getTextOffset());
        assertEquals(6, tb.getCurrentSegmentSize());
        assertEquals("Direct", tb.contentsAsString());
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testResetWithEmptyAndSegmentsClearing() {
        TextBuffer tb = new TextBuffer(null);
        tb.getCurrentSegment();
        tb.setCurrentLength(100);
        tb.finishCurrentSegment();
        tb.append('x');

        assertTrue(tb.size() > 0);
        tb.resetWithEmpty();

        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(new char[0], tb.contentsAsArray());
    }

    @Test(timeout = 4000)
    public void testResetWithSharedEmpty() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = new char[10];
        tb.resetWithShared(source, 0, 0);

        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(new char[0], tb.contentsAsArray());
    }

    @Test(timeout = 4000)
    public void testResetWithSharedOffsetZero() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "PrefixAndSuffix".toCharArray();
        tb.resetWithShared(source, 0, 6); // "Prefix"

        assertEquals(6, tb.size());
        assertEquals(0, tb.getTextOffset());
        char[] arr = tb.contentsAsArray();
        assertArrayEquals("Prefix".toCharArray(), arr);
    }

    @Test(timeout = 4000)
    public void testResetWithCopy() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "CopySourceString".toCharArray();

        // 1. Reset with copy when currentSegment is null
        tb.resetWithCopy(source, 4, 6); // "Source"
        assertEquals(6, tb.size());
        assertEquals("Source", tb.contentsAsString());

        // 2. Reset with copy after finishing segments
        tb.finishCurrentSegment();
        tb.resetWithCopy(source, 0, 4); // "Copy"
        assertEquals(4, tb.size());
        assertEquals("Copy", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("DirectValue");

        assertEquals(11, tb.size());
        assertFalse(tb.hasTextAsCharacters());
        assertEquals("DirectValue", tb.contentsAsString());
        assertArrayEquals("DirectValue".toCharArray(), tb.getTextBuffer());
        assertTrue(tb.hasTextAsCharacters()); // Now cached in resultArray

        // Reset with string when segments exist
        tb.finishCurrentSegment();
        tb.resetWithString("AnotherValue");
        assertEquals(12, tb.size());
        assertEquals("AnotherValue", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testMultiSegmentAggregationAndFinishing() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg1 = tb.getCurrentSegment();
        Arrays.fill(seg1, 'A');
        tb.setCurrentLength(seg1.length);

        char[] seg2 = tb.finishCurrentSegment();
        assertNotSame(seg1, seg2);
        Arrays.fill(seg2, 'B');
        tb.setCurrentLength(seg2.length);

        char[] seg3 = tb.finishCurrentSegment();
        assertNotSame(seg2, seg3);
        seg3[0] = 'C';
        tb.setCurrentLength(1);

        int expectedSize = seg1.length + seg2.length + 1;
        assertEquals(expectedSize, tb.size());

        String str = tb.contentsAsString();
        assertEquals(expectedSize, str.length());
        assertEquals('A', str.charAt(0));
        assertEquals('B', str.charAt(seg1.length));
        assertEquals('C', str.charAt(expectedSize - 1));

        char[] combined = tb.contentsAsArray();
        assertEquals(expectedSize, combined.length);
        assertEquals('A', combined[0]);
        assertEquals('B', combined[seg1.length]);
        assertEquals('C', combined[expectedSize - 1]);
    }

    @Test(timeout = 4000)
    public void testHugeAppendExceedingSingleSegment() {
        TextBuffer tb = new TextBuffer(null);
        // Create an array substantially larger than MIN_SEGMENT_LEN (1000)
        int size = 5000;
        char[] large = new char[size];
        for (int i = 0; i < size; ++i) {
            large[i] = (char) ('0' + (i % 10));
        }

        // Test huge append using char array
        tb.append(large, 0, size);
        assertEquals(size, tb.size());
        assertEquals(new String(large), tb.contentsAsString());

        // Test huge append using String
        tb.resetWithEmpty();
        String largeStr = new String(large);
        tb.append(largeStr, 0, size);
        assertEquals(size, tb.size());
        assertEquals(largeStr, tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testEmptyAndGetCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("SomeContent", 0, 11);
        tb.finishCurrentSegment();

        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertEquals(0, tb.size());
        assertEquals(0, tb.getCurrentSegmentSize());

        // Calling again when already reset
        char[] seg2 = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg2);
        assertEquals(0, tb.size());
    }

    /*
    /**********************************************************
    /* Partition D: Numeric Parsing & Conversion Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testContentsAsDecimalFromSingleSegment() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("12345.67", 0, 8);
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("12345.67"), bd);
    }

    @Test(timeout = 4000)
    public void testContentsAsDecimalFromSharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        char[] chars = "Val: 9876.54321 Extra".toCharArray();
        tb.resetWithShared(chars, 5, 10);
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("9876.54321"), bd);
    }

    @Test(timeout = 4000)
    public void testContentsAsDecimalFromResultArray() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("100.5", 0, 5);
        tb.contentsAsArray(); // caches resultArray
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("100.5"), bd);
    }

    @Test(timeout = 4000)
    public void testContentsAsDecimalFromAggregatedSegments() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        seg[0] = '1';
        tb.setCurrentLength(1);
        tb.finishCurrentSegment();

        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = '9';
        tb.setCurrentLength(1);

        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("19"), bd);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testContentsAsDecimalInvalidFormat() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("not-a-number", 0, 12);
        tb.contentsAsDecimal();
    }

    @Test(timeout = 4000)
    public void testContentsAsDouble() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("123.456", 0, 7);
        assertEquals(123.456, tb.contentsAsDouble(), 0.000001);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testContentsAsDoubleInvalidFormat() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("NaN_Invalid", 0, 11);
        tb.contentsAsDouble();
    }

    /*
    /**********************************************************
    /* Partition E: Buffer Recycling & Lifecycle Management
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testReleaseBuffersWithNullAllocator() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("Testing", 0, 7);
        tb.releaseBuffers();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffersWithAllocator() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);

        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
        tb.append("Sample text", 0, 11);

        tb.releaseBuffers();
        assertEquals(0, tb.size());

        // Subsequent buffer acquisition should reuse recycled buffer from recycler
        TextBuffer tb2 = new TextBuffer(recycler);
        char[] seg2 = tb2.getCurrentSegment();
        assertNotNull(seg2);
        assertSame(seg, seg2);
    }

    @Test(timeout = 4000)
    public void testReleaseBuffersWhenCurrentSegmentNull() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);
        // release before any segment allocation
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    @Test(timeout = 4000)
    public void testGetTextBufferBranches() {
        // 1. Shared input buffer branch
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "abcdef".toCharArray();
        tb.resetWithShared(shared, 1, 4);
        assertSame(shared, tb.getTextBuffer());

        // 2. Pre-computed resultArray branch
        tb.resetWithEmpty();
        tb.append("abc", 0, 3);
        char[] arr = tb.contentsAsArray();
        assertSame(arr, tb.getTextBuffer());

        // 3. String value branch
        tb.resetWithString("FromStr");
        char[] fromStr = tb.getTextBuffer();
        assertArrayEquals("FromStr".toCharArray(), fromStr);

        // 4. Single non-shared segment
        tb.resetWithEmpty();
        tb.append("Single", 0, 6);
        assertNotNull(tb.getTextBuffer());
        assertEquals("Single", tb.contentsAsString());

        // 5. Multi-segmented array
        tb.resetWithEmpty();
        tb.getCurrentSegment();
        tb.setCurrentLength(5);
        tb.finishCurrentSegment();
        tb.getCurrentSegment();
        tb.setCurrentLength(5);
        char[] multi = tb.getTextBuffer();
        assertEquals(10, multi.length);
    }
}