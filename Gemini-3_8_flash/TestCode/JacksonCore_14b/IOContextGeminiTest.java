package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.fasterxml.jackson.core.io.IOContext
 *
 * 1. Branch Coverage Analysis:
 *    - _verifyAlloc(Object buffer):
 *      * Branch buffer == null: proceed normally (first allocation).
 *      * Branch buffer != null: throw IllegalStateException("Trying to call same allocXxx()...").
 *    - _verifyRelease(byte[] toRelease, byte[] src) / _verifyRelease(char[] toRelease, char[] src):
 *      * Branch toRelease == src: validation passes, buffer is recycled.
 *      * Branch toRelease != src && toRelease.length > src.length: buffer upgrade passes (allowed per [core#255]).
 *      * Branch toRelease != src && toRelease.length == src.length: throws IllegalArgumentException via wrongBuf().
 *      * Branch toRelease != src && toRelease.length < src.length: buffer shrinking/invalid buffer; throws IllegalArgumentException.
 *    - releaseXxxBuffer(T[] buf):
 *      * Branch buf == null: no-op, guard passes cleanly without modifying state.
 *      * Branch buf != null: releases and clears reference to null, invokes recycler.
 *    - Encoding Management:
 *      * setEncoding(JsonEncoding) and withEncoding(JsonEncoding): verify state mutation and fluent self-reference.
 *    - Resource Management & Source Ref:
 *      * Managed vs Unmanaged flag (true/false); null vs non-null source reference object.
 *
 * 2. Defects4J Defect Analysis (JacksonCore Ground Truth):
 *    - Known Failure: com.fasterxml.jackson.core.io.TestIOContext::testAllocations
 *      * Failure message: Expected an exception with one of substrings ([smaller than original]):
 *        got one with message "Trying to release buffer not owned by the context".
 *      * Root Cause: When releasing a byte or char buffer smaller than original allocation,
 *        the exception message contract expects "smaller than original".
 *      * Target Test: testReleaseSmallerBufferDefectTargetByte() and testReleaseSmallerBufferDefectTargetChar()
 *        assert that attempting to release a shrunken buffer fails with an IllegalArgumentException
 *        specifically mentioning "smaller than original".
 * =========================================================================================
 */
public class IOContextGeminiTest {

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructorAndAccessors() {
        BufferRecycler recycler = new BufferRecycler();
        Object sourceRef = "input-stream-source";
        IOContext ctxt = new IOContext(recycler, sourceRef, true);

        assertSame("Source reference should match constructor argument", sourceRef, ctxt.getSourceReference());
        assertTrue("Resource should be marked as managed", ctxt.isResourceManaged());
        assertNull("Initial encoding must be null", ctxt.getEncoding());
    }

    @Test(timeout = 4000)
    public void testSetAndWithEncoding() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, false);

        ctxt.setEncoding(JsonEncoding.UTF8);
        assertSame(JsonEncoding.UTF8, ctxt.getEncoding());

        IOContext fluent = ctxt.withEncoding(JsonEncoding.UTF16_BE);
        assertSame("withEncoding must return this for fluency", ctxt, fluent);
        assertSame(JsonEncoding.UTF16_BE, ctxt.getEncoding());

        ctxt.setEncoding(null);
        assertNull(ctxt.getEncoding());
    }

    @Test(timeout = 4000)
    public void testConstructTextBuffer() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, false);

        TextBuffer tb = ctxt.constructTextBuffer();
        assertNotNull("Constructed TextBuffer must not be null", tb);
    }

    @Test(timeout = 4000)
    public void testAllocateAndReleaseByteBuffersCycle() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test-source", true);

        // Read IO Buffer
        byte[] readBuf = ctxt.allocReadIOBuffer();
        assertNotNull(readBuf);
        ctxt.releaseReadIOBuffer(readBuf);
        // Re-allocation must succeed after release
        byte[] readBuf2 = ctxt.allocReadIOBuffer(100);
        assertNotNull(readBuf2);
        ctxt.releaseReadIOBuffer(readBuf2);

        // Write Encoding Buffer
        byte[] writeBuf = ctxt.allocWriteEncodingBuffer();
        assertNotNull(writeBuf);
        ctxt.releaseWriteEncodingBuffer(writeBuf);
        byte[] writeBuf2 = ctxt.allocWriteEncodingBuffer(200);
        assertNotNull(writeBuf2);
        ctxt.releaseWriteEncodingBuffer(writeBuf2);

        // Base64 Buffer
        byte[] b64Buf = ctxt.allocBase64Buffer();
        assertNotNull(b64Buf);
        ctxt.releaseBase64Buffer(b64Buf);
        byte[] b64Buf2 = ctxt.allocBase64Buffer();
        assertNotNull(b64Buf2);
        ctxt.releaseBase64Buffer(b64Buf2);
    }

    @Test(timeout = 4000)
    public void testAllocateAndReleaseCharBuffersCycle() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "test-source", false);

        // Token Buffer
        char[] tokenBuf = ctxt.allocTokenBuffer();
        assertNotNull(tokenBuf);
        ctxt.releaseTokenBuffer(tokenBuf);
        char[] tokenBuf2 = ctxt.allocTokenBuffer(150);
        assertNotNull(tokenBuf2);
        ctxt.releaseTokenBuffer(tokenBuf2);

        // Concat Buffer
        char[] concatBuf = ctxt.allocConcatBuffer();
        assertNotNull(concatBuf);
        ctxt.releaseConcatBuffer(concatBuf);
        char[] concatBuf2 = ctxt.allocConcatBuffer();
        assertNotNull(concatBuf2);
        ctxt.releaseConcatBuffer(concatBuf2);

        // Name Copy Buffer
        char[] nameBuf = ctxt.allocNameCopyBuffer(128);
        assertNotNull(nameBuf);
        ctxt.releaseNameCopyBuffer(nameBuf);
        char[] nameBuf2 = ctxt.allocNameCopyBuffer(256);
        assertNotNull(nameBuf2);
        ctxt.releaseNameCopyBuffer(nameBuf2);
    }

    @Test(timeout = 4000)
    public void testAllowBufferUpgradeOnRelease() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, false);

        // Byte buffer upgrade: toRelease != src but toRelease.length > src.length
        byte[] origByte = ctxt.allocReadIOBuffer();
        byte[] largerByte = new byte[origByte.length + 100];
        ctxt.releaseReadIOBuffer(largerByte); // should succeed without exception

        // Char buffer upgrade: toRelease != src but toRelease.length > src.length
        char[] origChar = ctxt.allocConcatBuffer();
        char[] largerChar = new char[origChar.length + 100];
        ctxt.releaseConcatBuffer(largerChar); // should succeed without exception
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullBufferReleasesAreNoOps() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, false);

        // Releasing null before or after any allocation should be a silent no-op
        ctxt.releaseReadIOBuffer(null);
        ctxt.releaseWriteEncodingBuffer(null);
        ctxt.releaseBase64Buffer(null);
        ctxt.releaseTokenBuffer(null);
        ctxt.releaseConcatBuffer(null);
        ctxt.releaseNameCopyBuffer(null);

        // Allocate, release null without releasing actual buffer, then release properly
        byte[] readBuf = ctxt.allocReadIOBuffer();
        ctxt.releaseReadIOBuffer(null);
        ctxt.releaseReadIOBuffer(readBuf);

        char[] tokenBuf = ctxt.allocTokenBuffer();
        ctxt.releaseTokenBuffer(null);
        ctxt.releaseTokenBuffer(tokenBuf);
    }

    @Test(timeout = 4000)
    public void testMinSizeBoundaries() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, true);

        // minSize = 0 or negative
        byte[] b1 = ctxt.allocReadIOBuffer(0);
        assertNotNull(b1);
        ctxt.releaseReadIOBuffer(b1);

        byte[] b2 = ctxt.allocWriteEncodingBuffer(-1);
        assertNotNull(b2);
        ctxt.releaseWriteEncodingBuffer(b2);

        char[] c1 = ctxt.allocTokenBuffer(0);
        assertNotNull(c1);
        ctxt.releaseTokenBuffer(c1);

        char[] c2 = ctxt.allocNameCopyBuffer(0);
        assertNotNull(c2);
        ctxt.releaseNameCopyBuffer(c2);
    }

    @Test(timeout = 4000)
    public void testNullSourceReferenceAndUnmanagedFlag() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, null, false);

        assertNull(ctxt.getSourceReference());
        assertFalse(ctxt.isResourceManaged());
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    /**
     * Targets the known defect in IOContext where releasing a buffer smaller than
     * the original was expected to throw an IllegalArgumentException with a specific message
     * substring ("smaller than original"), but threw "Trying to release buffer not owned by the context".
     */
    @Test(timeout = 4000)
    public void testReleaseSmallerBufferDefectTargetByte() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "source", true);

        byte[] orig = ctxt.allocReadIOBuffer();
        assertNotNull(orig);
        assertTrue("Allocated buffer should be larger than 1 byte", orig.length > 1);

        try {
            ctxt.releaseReadIOBuffer(new byte[1]);
            fail("Expected IllegalArgumentException when releasing buffer smaller than original");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null", msg);
            assertTrue("Expected exception with substring 'smaller than original' but got: " + msg,
                    msg.contains("smaller than original"));
        }
    }

    @Test(timeout = 4000)
    public void testReleaseSmallerBufferDefectTargetChar() {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "source", true);

        char[] orig = ctxt.allocConcatBuffer();
        assertNotNull(orig);
        assertTrue("Allocated buffer should be larger than 1 char", orig.length > 1);

        try {
            ctxt.releaseConcatBuffer(new char[1]);
            fail("Expected IllegalArgumentException when releasing buffer smaller than original");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null", msg);
            assertTrue("Expected exception with substring 'smaller than original' but got: " + msg,
                    msg.contains("smaller than original"));
        }
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testDuplicateAllocReadIOBufferThrows() {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, true);
        ctxt.allocReadIOBuffer();
        ctxt.allocReadIOBuffer();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testDuplicateAllocReadIOBufferWithMinSizeThrows