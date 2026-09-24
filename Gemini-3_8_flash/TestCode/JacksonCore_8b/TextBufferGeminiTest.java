package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.fasterxml.jackson.core.util.TextBuffer
 * Tested In: Defects4J / Java 8 / JUnit 4
 *
 * Targeted Defects & Decision Branches:
 * 1. DEFECT FIXATION (Defects4J ground truth: TestTextBuffer::testEmpty -> NPE):
 *    - Uninitialized or empty TextBuffer calling getTextBuffer() or contentsAsString() / size() / getTextOffset().
 *    - Default state where _inputStart == 0, _inputBuffer == null, or _currentSegment == null.
 * 2. Partition A: Core Functional Lifecycle & Transitions
 *    - releaseBuffers() with null allocator vs. active BufferRecycler (with and without active segment).
 *    - resetWithEmpty(), resetWithShared(), resetWithCopy(), resetWithString().
 *    - Transition from shared mode to unshared mode via ensureNotShared(), append(), and getCurrentSegment().
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Empty string/array appends (len = 0).
 *    - Offset variations (start = 0 vs. start > 0) in shared buffer and array copy.
 *    - Single-char, small array, and massive array/string appends requiring segment growth and multi-segment chaining.
 *    - Clamping logic in finishCurrentSegment: < MIN_SEGMENT_LEN (1000) and > MAX_SEGMENT_LEN (262144).
 *    - Clamping / growth logic in expandCurrentSegment() and expandCurrentSegment(minSize).
 * 4. Partition C: Defect-Targeted & Complex Conversion Branches
 *    - contentsAsDecimal() across all 4 internal states: _resultArray != null, shared buffer, single segment, aggregated.
 *    - contentsAsDouble() for valid floating-point values and invalid format exceptions.
 *    - contentsAsString() and contentsAsArray() with cached results, empty buffers, shared zero-length, and multi-segments.
 *    - setCurrentAndReturn() with 0 size, positive size on single segment, and positive size with pre-existing segments.
 * 5. Partition D: Defensive Guards & State Resets
 *    - Multiple consecutive releaseBuffers() calls (idempotence).
 *    - emptyAndGetCurrentSegment() on clean vs. segmented buffers.
 *    - NumberFormatException on malformed decimal/double parsing.
 * ====================================================================================================
 */
public class TextBufferGeminiTest {

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************
     */

    /**
     * Directly targets the known defect:
     * com.fasterxml.jackson.core.util.TestTextBuffer::testEmpty -> java.lang.NullPointerException
     * A newly instantiated TextBuffer must not return null or trigger NPE when queried while empty.
     */
    @Test(timeout = 4000)
    public void testEmpty() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);

        // Verification of empty buffer accessors
        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("", tb.contentsAsString());
        assertEquals("", tb.toString());

        char[] textBuf = tb.getTextBuffer();
        assertNotNull("TextBuffer.getTextBuffer() must not return null for empty buffer", textBuf);
        assertEquals(0, textBuf.length);

        char[] arr = tb.contentsAsArray();
        assertNotNull("contentsAsArray() must not return null for empty buffer", arr);
        assertEquals(0, arr.length);
    }

    /**
     * Defect variant: resetWithEmpty() explicitly invoked on fresh TextBuffer without allocator.
     */
    @Test(timeout = 4000)
    public void testEmptyWithoutAllocatorAfterReset() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithEmpty();

        assertEquals(0, tb.size());
        assertEquals(0, tb.getTextOffset());
        assertEquals("", tb.contentsAsString());

        char[] buf = tb.getTextBuffer();
        if (buf != null) {
            assertEquals(0, buf.length);
        }
        char[] arr = tb.contentsAsArray();
        assertNotNull(arr);
        assertEquals(0, arr.length);
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testResetWithSharedBuffer() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "HelloWorld".toCharArray();
        tb.resetWithShared(src, 5, 5);

        assertTrue(tb.hasTextAsCharacters());
        assertEquals(5, tb.getTextOffset());
        assertEquals(5, tb.size());
        assertSame(src, tb.getTextBuffer());
        assertEquals("World", tb.contentsAsString());

        // Array extraction when start > 0
        char[] extracted = tb.contentsAsArray();
        assertEquals("World", new String(extracted));
        assertNotSame(src, extracted);
    }

    @Test(timeout = 4000)
    public void testResetWithSharedBufferAtStartZero() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "DirectCopy".toCharArray();
        tb.resetWithShared(src, 0, 6);

        assertEquals(0, tb.getTextOffset());
        assertEquals(6, tb.size());
        assertEquals("Direct", tb.contentsAsString());

        // Array extraction when start == 0
        char[] extracted = tb.contentsAsArray();
        assertEquals("Direct", new String(extracted));
    }

    @Test(timeout = 4000)
    public void testResetWithSharedBufferZeroLength() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "Empty".toCharArray();
        tb.resetWithShared(src, 0, 0);

        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
        char[] arr = tb.contentsAsArray();
        assertNotNull(arr);
        assertEquals(0, arr.length);
    }

    @Test(timeout = 4000)
    public void testResetWithCopy() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "AlphabetSoup".toCharArray();
        tb.resetWithCopy(src, 8, 4);

        assertEquals(0, tb.getTextOffset());
        assertEquals(4, tb.size());
        assertEquals("Soup", tb.contentsAsString());
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test(timeout = 4000)
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(null);
        tb.resetWithString("ConstantString");

        assertFalse(tb.hasTextAsCharacters());
        assertEquals(14, tb.size());
        assertEquals("ConstantString", tb.contentsAsString());

        // Calling getTextBuffer caches result array and flips hasTextAsCharacters to true
        char[] chars = tb.getTextBuffer();
        assertEquals("ConstantString", new String(chars));
        assertTrue(tb.hasTextAsCharacters());
    }

    @Test(timeout = 4000)
    public void testAppendSingleCharsWithGrowth() {
        TextBuffer tb = new TextBuffer(null);
        // Fill initial segment and force expand
        for (int i = 0; i < 1500; i++) {
            tb.append((char) ('a' + (i % 26)));
        }
        assertEquals(1500, tb.size());
        String str = tb.contentsAsString();
        assertEquals(1500, str.length());
        assertEquals('a', str.charAt(0));
        assertEquals('b', str.charAt(1));
    }

    @Test(timeout = 4000)
    public void testEnsureNotSharedAndAppendChar() {
        TextBuffer tb = new TextBuffer(null);
        char[] src = "ReadOnly".toCharArray();
        tb.resetWithShared(src, 0, 4); // "Read"

        tb.ensureNotShared();
        assertEquals(4, tb.size());

        tb.append('!');
        assertEquals(5, tb.size());
        assertEquals("Read!", tb.contentsAsString());
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testAppendCharArrayExceedingCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        // Force initial segment allocation
        tb.getCurrentSegment();
        int initialCapacity = tb.getCurrentSegment().length;

        // Fill partially
        char[] part1 = new char[initialCapacity - 10];
        Arrays.fill(part1, 'x');
        tb.append(part1, 0, part1.length);
        assertEquals(initialCapacity - 10, tb.getCurrentSegmentSize());

        // Append more than remaining space (10)
        char[] part2 = new char[50];
        Arrays.fill(part2, 'y');
        tb.append(part2, 0, part2.length);

        assertEquals(initialCapacity + 40, tb.size());
        String res = tb.contentsAsString();
        assertEquals(initialCapacity + 40, res.length());
        assertTrue(res.startsWith("xx"));
        assertTrue(res.endsWith("yy"));
    }

    @Test(timeout = 4000)
    public void testHugeCharArrayAppendRequiringMultipleExpansions() {
        TextBuffer tb = new TextBuffer(null);
        // Large data that exceeds single MAX_SEGMENT_LEN or triggers multi-step loop
        int largeSize = 300000;
        char[] massive = new char[largeSize];
        Arrays.fill(massive, 'z');

        tb.append(massive, 0, massive.length);
        assertEquals(largeSize, tb.size());

        char[] combined = tb.contentsAsArray();
        assertEquals(largeSize, combined.length);
        assertEquals('z', combined[0]);
        assertEquals('z', combined[largeSize - 1]);
    }

    @Test(timeout = 4000)
    public void testAppendStringExceedingCurrentSegmentAndHugeString() {
        TextBuffer tb = new TextBuffer(null);
        char[] initial = tb.getCurrentSegment();
        int room = initial.length;

        // Partially fill
        char[] buf = new char[room - 5];
        Arrays.fill(buf, 'A');
        tb.append(new String(buf), 0, buf.length);

        // String append spanning segment boundary
        tb.append("1234567890EXTRA", 0, 15);
        assertEquals(room + 10, tb.size());

        // Huge string append triggering do-while loop in append(String, int, int)
        StringBuilder hugeSb = new StringBuilder(300000);
        for (int i = 0; i < 300000; i++) {
            hugeSb.append((char) ('0' + (i % 10)));
        }
        String hugeStr = hugeSb.toString();
        tb.append(hugeStr, 0, hugeStr.length());

        assertEquals(room + 10 + 300000, tb.size());
    }

    @Test(timeout = 4000)
    public void testFinishCurrentSegmentAndClamping() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] seg1 = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg1);

        // Segment 1 finished -> segments list instantiated
        char[] seg2 = tb.finishCurrentSegment();
        assertNotNull(seg2);
        assertTrue(seg2.length >= TextBuffer.MIN_SEGMENT_LEN);

        // Advance to cover MAX_SEGMENT_LEN branch in finishCurrentSegment()
        tb.expandCurrentSegment(TextBuffer.MAX_SEGMENT_LEN - 10);
        char[] segMax = tb.finishCurrentSegment();
        assertEquals(TextBuffer.MAX_SEGMENT_LEN, segMax.length);
    }

    @Test(timeout = 4000)
    public void testExpandCurrentSegmentDefaultAndAboveMax() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        int originalLen = seg.length;

        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > originalLen);
        assertEquals(originalLen + (originalLen >> 1), expanded.length);

        // Force segment length above MAX_SEGMENT_LEN boundary to hit 25% growth path
        tb.expandCurrentSegment(TextBuffer.MAX_SEGMENT_LEN);
        int bigLen = tb.getCurrentSegment().length;
        char[] maxExpanded = tb.expandCurrentSegment();
        assertEquals(bigLen + (bigLen >> 2), maxExpanded.length);
    }

    @Test(timeout = 4000)
    public void testExpandCurrentSegmentWithMinSize() {
        TextBuffer tb = new TextBuffer(null);
        char[] curr = tb.getCurrentSegment();
        int len = curr.length;

        // If minSize <= current length, returns same segment
        char[] same = tb.expandCurrentSegment(len - 1);
        assertSame(curr, same);

        // If minSize > current length, expands to exactly minSize
        char[] bigger = tb.expandCurrentSegment(len + 250);
        assertEquals(len + 250, bigger.length);
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted & Complex Conversion Branches
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testContentsAsDecimalAllBranches() {
        // Branch 1: Pre-cut _resultArray
        TextBuffer tb1 = new TextBuffer(null);
        tb1.append("1234.56", 0, 7);
        tb1.contentsAsArray(); // caches _resultArray
        BigDecimal d1 = tb1.contentsAsDecimal();
        assertEquals(new BigDecimal("1234.56"), d1);

        // Branch 2: Shared input buffer
        TextBuffer tb2 = new TextBuffer(null);
        char[] raw = "PREFIX999.88SUFFIX".toCharArray();
        tb2.resetWithShared(raw, 6, 6);
        BigDecimal d2 = tb2.contentsAsDecimal();
        assertEquals(new BigDecimal("999.88"), d2);

        // Branch 3: Single segment buffer (_segmentSize == 0 && _currentSegment != null)
        TextBuffer tb3 = new TextBuffer(null);
        tb3.append("42.0", 0, 4);
        BigDecimal d3 = tb3.contentsAsDecimal();
        assertEquals(new BigDecimal("42.0"), d3);

        // Branch 4: Multi-segment buffer
        TextBuffer tb4 = new TextBuffer(null);
        tb4.getCurrentSegment();
        tb4.append("100", 0, 3);
        tb4.finishCurrentSegment();
        tb4.append(".25", 0, 3);
        BigDecimal d4 = tb4.contentsAsDecimal();
        assertEquals(new BigDecimal("100.25"), d4);
    }

    @Test(timeout = 4000)
    public void testContentsAsDouble() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("123.5", 0, 5);
        assertEquals(123.5, tb.contentsAsDouble(), 0.00001);
    }

    @Test(timeout = 4000)
    public void testContentsAsStringCachedAndPrecut() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("CacheTest", 0, 9);
        // Generate and cache resultArray first
        char[] arr = tb.contentsAsArray();
        assertEquals("CacheTest", new String(arr));

        // Now contentsAsString should take the (_resultArray != null) branch
        String str = tb.contentsAsString();
        assertEquals("CacheTest", str);

        // Second call hits (_resultString != null) branch
        assertSame(str, tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testSetCurrentAndReturn() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        seg[0] = 'H';
        seg[1] = 'i';

        // Single segment, len = 2
        String s1 = tb.setCurrentAndReturn(2);
        assertEquals("Hi", s1);
        assertEquals(2, tb.getCurrentSegmentSize());

        // Single segment, len = 0
        String s2 = tb.setCurrentAndReturn(0);
        assertEquals("", s2);

        // Multi-segment path in setCurrentAndReturn
        tb.append("Part1", 0, 5);
        tb.finishCurrentSegment();
        char[] seg2 = tb.getCurrentSegment();
        seg2[0] = 'B';
        String s3 = tb.setCurrentAndReturn(1);
        assertEquals("Part1B", s3);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testContentsAsDecimalInvalidFormat() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("NotANumber", 0, 10);
        tb.contentsAsDecimal();
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testContentsAsDoubleInvalidFormat() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("NaN_Invalid", 0, 11);
        tb.contentsAsDouble();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffersIdempotenceAndNullAllocator() {
        // Without allocator
        TextBuffer tbNoAlloc = new TextBuffer(null);
        tbNoAlloc.append('A');
        tbNoAlloc.releaseBuffers();
        assertEquals(0, tbNoAlloc.size());

        // With allocator
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tbAlloc = new TextBuffer(recycler);

        // Release when nothing allocated
        tbAlloc.releaseBuffers();
        assertNull(tbAlloc.getTextBuffer());

        // Release when segment was allocated
        tbAlloc.getCurrentSegment();
        tbAlloc.releaseBuffers();
        assertEquals(0, tbAlloc.size());

        // Multiple calls to releaseBuffers must be benign
        tbAlloc.releaseBuffers();
    }

    @Test(timeout = 4000)
    public void testEmptyAndGetCurrentSegmentTransitions() {
        TextBuffer tb = new TextBuffer(null);
        // Build segments
        tb.append("Segment1Content", 0, 15);
        tb.finishCurrentSegment();
        tb.append("Segment2Content", 0, 15);
        assertTrue(tb.size() > 0);

        // Reset and clear segments via emptyAndGetCurrentSegment
        char[] freshSeg = tb.emptyAndGetCurrentSegment();
        assertNotNull(freshSeg);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testResetWithCopyAndResetWithStringClearsExistingSegments() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("InitialData", 0, 11);
        tb.finishCurrentSegment();

        // resetWithCopy should clear old segments
        char[] copySrc = "Overwritten".toCharArray();
        tb.resetWithCopy(copySrc, 0, copySrc.length);
        assertEquals("Overwritten", tb.contentsAsString());

        // Fill again and finish segment
        tb.finishCurrentSegment();
        tb.append("MoreData", 0, 8);

        // resetWithString should also clear old segments
        tb.resetWithString("NewString");
        assertEquals("NewString", tb.contentsAsString());
    }

    @Test(timeout = 4000)
    public void testSharedBufferTransitionViaGetCurrentSegment() {
        TextBuffer tb = new TextBuffer(null);
        char[] shared = "UnshareMe".toCharArray();
        tb.resetWithShared(shared, 0, shared.length);

        // Invoking getCurrentSegment on shared buffer unshares it
        char[] curr = tb.getCurrentSegment();
        assertNotNull(curr);
        assertEquals(9, tb.getCurrentSegmentSize());
        assertEquals("UnshareMe", tb.contentsAsString());
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testToStringContract() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("ContractValidation", 0, 18);
        assertEquals("ContractValidation", tb.toString());
        assertEquals(tb.contentsAsString(), tb.toString());
    }

    @Test(timeout = 4000)
    public void testSetCurrentLengthContract() {
        TextBuffer tb = new TextBuffer(null);
        char[] seg = tb.getCurrentSegment();
        seg[0] = 'X';
        seg[1] = 'Y';
        seg[2] = 'Z';
        tb.setCurrentLength(3);

        assertEquals(3, tb.getCurrentSegmentSize());
        assertEquals(3, tb.size());
        assertEquals("XYZ", tb.contentsAsString());

        tb.setCurrentLength(1);
        assertEquals(1, tb.getCurrentSegmentSize());
        tb.contentsAsArray(); // forces array rebuild
        assertEquals("X", tb.contentsAsString());
    }
}