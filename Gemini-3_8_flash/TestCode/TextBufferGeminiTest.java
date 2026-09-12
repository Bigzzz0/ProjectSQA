package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * 1. Defect-Targeted Zone (JacksonCore-1 / Issue 98):
 *    - `contentsAsDecimal()` with non-numeric tokens like "NaN", "Infinity", "-Infinity".
 *    - Target fault: NumberFormatException thrown MUST contain descriptive text
 *      "can not be represented as BigDecimal" rather than a null / generic message.
 *
 * 2. Partition A (Buffer Lifecycle & Reset):
 *    - TextBuffer(BufferRecycler): null recycler vs valid recycler.
 *    - releaseBuffers(): null allocator vs non-null allocator; _currentSegment null vs populated.
 *    - resetWithEmpty(): clears input buffer, shared flags, segments, and cached strings/arrays.
 *    - resetWithShared(buf, start, len): pointer wrapping, zero-length vs non-zero, clearSegments.
 *    - resetWithCopy(buf, start, len): deep copy, lazy buffer allocation via findBuffer().
 *    - resetWithString(String): string wrapping, segments clearing.
 *
 * 3. Partition B (Append Operations & Segment Growth):
 *    - append(char): shared unshare transition, current segment boundary expansion.
 *    - append(char[], int, int): fits entirely in segment; partially fits then expands;
 *      multi-segment spanning with large arrays exceeding single segment capacity.
 *    - append(String, int, int): fits entirely; partial copy + expand loop; huge string append.
 *    - getCurrentSegment(): unshare if shared, allocate if null, expand if full.
 *    - emptyAndGetCurrentSegment(): reset state and return current/allocated buffer.
 *    - finishCurrentSegment(): append to _segments, 50% capacity growth capped at MAX_SEGMENT_LEN.
 *    - expandCurrentSegment(): contiguous resize, capped at MAX_SEGMENT_LEN and +1 overflow branch.
 *
 * 4. Partition C (Content Conversion & Cache State Machine):
 *    - contentsAsString(): cached _resultString, cached _resultArray shortcut, shared input buffer
 *      (empty len < 1 vs non-empty), single segment, multi-segment concatenation.
 *    - contentsAsArray(): cached _resultArray, shared buffer (len < 1, start == 0, start > 0),
 *      empty buffer (size < 1 -> NO_CHARS), multi-segment array builder.
 *    - contentsAsDecimal(): branches for pre-cut _resultArray, shared buffer, single segment,
 *      and aggregated multi-segment buffer.
 *    - contentsAsDouble(): conversion through NumberInput.parseDouble(contentsAsString()).
 *
 * 5. Partition D (Boundary Conditions & Invariants):
 *    - size(), getTextOffset(), hasTextAsCharacters(): state verification across all modes.
 *    - ensureNotShared(): no-op when unshared, unshare when shared.
 * =========================================================================
 */
public class TextBufferGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonCore-1 / Issue 98)
    // =========================================================================

    @Test(timeout = 4000)
    public void testContentsAsDecimal_NaN_DescriptiveMessage_Issue98() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithCopy("NaN".toCharArray(), 0, 3);
        try {
            tb.contentsAsDecimal();
            fail("Expected NumberFormatException for 'NaN'");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Expected message to contain 'can not be represented as BigDecimal', but got: " + e.getMessage(),
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    @Test(timeout = 4000)
    public void testContentsAsDecimal_Infinity_DescriptiveMessage() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithShared("Infinity".toCharArray(), 0, 8);
        try {
            tb.contentsAsDecimal();
            fail("Expected NumberFormatException for 'Infinity'");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Expected message to contain 'can not be represented as BigDecimal', but got: " + e.getMessage(),
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    @Test(timeout = 4000)
    public void testContentsAsDecimal_NegativeInfinity_DescriptiveMessage() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("-Infinity", 0, 9);
        try {
            tb.contentsAsDecimal();
            fail("Expected NumberFormatException for '-Infinity'");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Expected message to contain 'can not be represented as BigDecimal', but got: " + e.getMessage(),
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    // =========================================================================
    // Partition A: Buffer Initialization, Reset & Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitializationAndEmptyResetWithoutRecycler() {
        TextBuffer tb = new TextBuffer(null);
        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());

        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    @Test(timeout = 4000)
    public void testInitializationAndReleaseWithRecycler() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);

        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertTrue(seg.length >= TextBuffer.MIN_SEGMENT_LEN);

        tb.append('A');
        assertEquals(1, tb.size());

        tb.releaseBuffers();
        assertEquals(0, tb.size());

        // Call release again when _currentSegment is null
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    @Test(timeout = 4000)
    public void testResetWithSharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "Hello World".toCharArray();
        tb.resetWithShared(shared, 6, 5);

        assertEquals(5, tb.size());
        assertEquals(6, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertSame(shared, tb.getTextBuffer());
        assertEquals("World", tb.contentsAsString());
        assertArrayEquals("World".toCharArray(), tb.contentsAsArray());

        // Reset with empty shared
        tb.resetWithShared(shared, 0, 0);
        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("", tb.contentsAsString());
        assertArrayEquals(TextBuffer.NO_CHARS, tb.contentsAsArray());
    }

    @Test(timeout = 4000)
    public void testResetWithSharedBufferOffsetZero() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "Jackson".toCharArray();
        tb.resetWithShared(shared, 0, 7);

        assertEquals(7, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertArrayEquals("Jackson".toCharArray(), tb.contentsAsArray());
        assertEquals("Jackson", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testResetWithCopy() {
        TextBuffer tb = new TextBuffer(null);
        char[] source = "CopyTargetData".toCharArray();
        tb.resetWithCopy(source, 4, 6); // "Target"

        assertEquals(6, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("Target", tb.contentsAsString());
        assertArrayEquals("Target".toCharArray(), tb.contentsAsArray());

        // Subsequent resetWithCopy should clear previous and copy new
        tb.resetWithCopy(source, 0, 4); // "Copy"
        assertEquals(4, tb.size());
        assertEquals("Copy", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("ConstantString");

        assertEquals(14, tb.size());
        assertFalse(tb.hasTextAsCharacters());
        assertEquals("ConstantString", tb.contentsAsString());
        assertEquals("ConstantString", tb.toString());

        char[] textBuf = tb.getTextBuffer();
        assertArrayEquals("ConstantString".toCharArray(), textBuf);
        // After text buffer is generated, resultArray exists
        assertTrue(tb.hasTextAsCharacters());
    }

    // =========================================================================
    // Partition B: Appends, Segment Transitions & Buffer Expansions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendSingleCharsAndEnsureNotShared() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "Share".toCharArray();
        tb.resetWithShared(shared, 0, 5);

        tb.ensureNotShared();
        assertEquals(5, tb.size());
        assertEquals("Share", tb.contentsAsString());

        tb.append('!');
        assertEquals(6, tb.size());
        assertEquals("Share!", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testAppendCharUnsharesDirectly() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "Init".toCharArray();
        tb.resetWithShared(shared, 1, 3); // "nit"

        tb.append('+');
        assertEquals("nit+", tb.contentsAsString());
        assertEquals(4, tb.size());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayFittingAndSpanningSegments() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();

        char[] chunk = new char[500];
        Arrays.fill(chunk, 'a');
        tb.append(chunk, 0, 500);
        assertEquals(500, tb.size());

        // Add chunk that exceeds the default segment length (1000)
        char[] chunk2 = new char[800];
        Arrays.fill(chunk2, 'b');
        tb.append(chunk2, 0, 800);
        assertEquals(1300, tb.size());

        String result = tb.contentsAsString();
        assertEquals(1300, result.length());
        assertEquals('a', result.charAt(0));
        assertEquals('a', result.charAt(499));
        assertEquals('b', result.charAt(500));
        assertEquals('b', result.charAt(1299));
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayHugeExceedingSingleNewSegment() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();

        int hugeLen = 150000;
        char[] huge = new char[hugeLen];
        for (int i = 0; i < hugeLen; i++) {
            huge[i] = (char) ('0' + (i % 10));
        }

        tb.append(huge, 0, hugeLen);
        assertEquals(hugeLen, tb.size());

        char[] combined = tb.contentsAsArray();
        assertArrayEquals(huge, combined);
        assertEquals(new String(huge), tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testAppendStringFittingAndSpanningSegments() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "prefix:".toCharArray();
        tb.resetWithShared(shared, 0, 7);

        String part1 = "first part ";
        tb.append(part1, 0, part1.length());
        assertEquals("prefix:first part ", tb.contentsAsString());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        String large = sb.toString();
        tb.append(large, 0, large.length());

        assertEquals(7 + part1.length() + large.length(), tb.size());
        assertTrue(tb.contentsAsString().startsWith("prefix:first part A"));
    }

    @Test(timeout = 4000)
    public void testAppendStringHuge() {
        TextBuffer tb = new TextBuffer(null);
        tb.emptyAndGetCurrentSegment();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120000; i++) {
            sb.append('Z');
        }
        String hugeStr = sb.toString();
        tb.append(hugeStr, 0, hugeStr.length());

        assertEquals(120000, tb.size());
        assertEquals(hugeStr, tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testManualSegmentManipulation() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
        assertEquals(0, tb.getCurrentSegmentSize());

        seg[0] = 'X';
        seg[1] = 'Y';
        tb.setCurrentLength(2);
        assertEquals(2, tb.size());
        assertEquals("XY", tb.contentsAsString());

        char[] nextSeg = tb.finishCurrentSegment();
        assertNotNull(nextSeg);
        assertNotSame(seg, nextSeg);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertEquals(2, tb.size());

        nextSeg[0] = 'Z';
        tb.setCurrentLength(1);
        assertEquals(3, tb.size());
        assertEquals("XYZ", tb.contentsAsString());

        // Check expandCurrentSegment
        int oldLen = nextSeg.length;
        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > oldLen);
        assertSame(expanded, tb.getCurrentSegment());
    }

    @Test(timeout = 4000)
    public void testGetCurrentSegmentSharedBranch() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "UnshareTest".toCharArray();
        tb.resetWithShared(shared, 0, shared.length);

        char[] curr = tb.getCurrentSegment();
        assertNotNull(curr);
        assertTrue(curr.length >= shared.length);
        assertEquals("UnshareTest", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testGetCurrentSegmentExpandWhenFull() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        tb.setCurrentLength(seg.length);

        char[] expanded = tb.getCurrentSegment();
        assertNotNull(expanded);
        assertTrue(tb.getCurrentSegmentSize() < expanded.length);
    }

    // =========================================================================
    // Partition D: Content Extraction & Conversion (Double, Decimal, Array)
    // =========================================================================

    @Test(timeout = 4000)
    public void testContentsAsDecimal_AllBranches() {
        // Branch 1: Single segment
        TextBuffer tb1 = new TextBuffer(null);
        tb1.append("123.45", 0, 6);
        assertEquals(new BigDecimal("123.45"), tb1.contentsAsDecimal());

        // Branch 2: Shared input buffer
        TextBuffer tb2 = new TextBuffer(null);
        char[] chars = "prefix987.654suffix".toCharArray();
        tb2.resetWithShared(chars, 6, 7); // "987.654"
        assertEquals(new BigDecimal("987.654"), tb2.contentsAsDecimal());

        // Branch 3: Cached _resultArray
        TextBuffer tb3 = new TextBuffer(null);
        tb3.append("42.0", 0, 4);
        tb3.contentsAsArray(); // caches _resultArray
        assertEquals(new BigDecimal("42.0"), tb3.contentsAsDecimal());

        // Branch 4: Multi-segment aggregation
        TextBuffer tb4 = new TextBuffer(null);
        char[] seg = tb4.emptyAndGetCurrentSegment();
        Arrays.fill(seg, '1');
        tb4.setCurrentLength(seg.length);
        tb4.finishCurrentSegment();
        tb4.append(".5", 0, 2);

        BigDecimal bd = tb4.contentsAsDecimal();
        assertNotNull(bd);
        assertTrue(bd.toString().startsWith("1111"));
        assertTrue(bd.toString().endsWith(".5"));
    }

    @Test(timeout = 4000)
    public void testContentsAsDouble() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("123.75", 0, 6);
        assertEquals(123.75, tb.contentsAsDouble(), 0.0001);

        tb.resetWithString("-0.005");
        assertEquals(-0.005, tb.contentsAsDouble(), 0.000001);
    }

    @Test(timeout = 4000)
    public void testContentsAsStringCachedPaths() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("TestString", 0, 10);

        // First call generates and caches _resultString
        String s1 = tb.contentsAsString();
        assertEquals("TestString", s1);
        String s2 = tb.contentsAsString();
        assertSame(s1, s2);

        // Pre-cached _resultArray path
        TextBuffer tb2 = new TextBuffer(null);
        tb2.append("ArrayFirst", 0, 10);
        tb2.contentsAsArray();
        assertEquals("ArrayFirst", tb2.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testContentsAsArrayCachedAndEmpty() {
        TextBuffer tb = new TextBuffer(null);
        char[] emptyArray = tb.contentsAsArray();
        assertArrayEquals(TextBuffer.NO_CHARS, emptyArray);

        // Shortcut via existing _resultString
        tb.resetWithString("FromStr");
        char[] fromStr = tb.contentsAsArray();
        assertArrayEquals("FromStr".toCharArray(), fromStr);

        // Subsequent call returns cached _resultArray
        assertSame(fromStr, tb.contentsAsArray());
    }

    @Test(timeout = 4000)
    public void testGetTextBufferBranches() {
        // Branch 1: Shared buffer
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "ShareMe".toCharArray();
        tb.resetWithShared(shared, 1, 5);
        assertSame(shared, tb.getTextBuffer());

        // Branch 2: Multi-segment fallback to contentsAsArray()
        tb.resetWithEmpty();
        char[] seg = tb.emptyAndGetCurrentSegment();
        tb.setCurrentLength(seg.length);
        tb.finishCurrentSegment();
        tb.append('X');
        char[] combined = tb.getTextBuffer();
        assertEquals(seg.length + 1, combined.length);
        assertEquals('X', combined[seg.length]);
    }

    @Test(timeout = 4000)
    public void testClearSegmentsOnReset() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.emptyAndGetCurrentSegment();
        tb.setCurrentLength(seg.length);
        tb.finishCurrentSegment();
        tb.append('Z');
        assertEquals(seg.length + 1, tb.size());

        // Reset with empty clears segments
        tb.resetWithEmpty();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());

        // Build segments again and resetWithShared
        tb.emptyAndGetCurrentSegment();
        tb.setCurrentLength(10);
        tb.finishCurrentSegment();
        char[] dummy = "Dummy".toCharArray();
        tb.resetWithShared(dummy, 0, 5);
        assertEquals(5, tb.size());

        // Build segments again and resetWithCopy
        tb.finishCurrentSegment();
        tb.resetWithCopy(dummy, 0, 5);
        assertEquals(5, tb.size());
        assertEquals("Dummy", tb.contentsAsString());

        // Build segments again and resetWithString
        tb.finishCurrentSegment();
        tb.resetWithString("Str");
        assertEquals(3, tb.size());
        assertEquals("Str", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testExpandCurrentSegmentAtMaxSegmentLength() {
        TextBuffer tb = new TextBuffer(null);
        // Emulate currentSegment having MAX_SEGMENT_LEN (262144)
        char[] maxSeg = new char[TextBuffer.MAX_SEGMENT_LEN];
        tb.resetWithCopy(maxSeg, 0, maxSeg.length);

        char[] expanded = tb.expandCurrentSegment();
        assertEquals(TextBuffer.MAX_SEGMENT_LEN + 1, expanded.length);
    }
}