package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: BaseNCodecInputStream
 * 
 * Decision branches in read(byte[], int, int):
 *   - b == null -> NullPointerException
 *   - offset < 0 || len < 0 -> IndexOutOfBoundsException
 *   - offset > b.length || offset+len > b.length -> IndexOutOfBoundsException
 *   - len == 0 -> return 0
 *   - else: while(readLen == 0) loop:
 *       - if(!baseNCodec.hasData()) -> read from underlying stream and encode/decode
 *       - readLen = baseNCodec.readResults(b, offset, len)
 *   - return readLen
 * 
 * Decision branches in read():
 *   - calls read(singleByte,0,1) in a while(r==0) loop
 *   - if r>0: return byte value (unsigned)
 *   - else: return EOF (-1)
 * 
 * Known defects (from Defects4J):
 *   - skip() returns wrong values (e.g., -1 vs 183, 3 vs 8)
 *   - available() returns 8 instead of 1
 *   - skip() does not throw IllegalArgumentException for negative length
 *   - decode corruption (testCodec130)
 * 
 * Boundary conditions:
 *   - null byte array
 *   - negative offset/len
 *   - offset+len > array length
 *   - len == 0
 *   - empty input stream
 *   - large skip values
 *   - negative skip values
 *   - EOF after skip
 *   - available before/after read
 *   - encoding vs decoding mode
 *   - Base32 and Base64 codecs
 */
public class BaseNCodecInputStreamDeepseekTest {

    // Helper to create a BaseNCodecInputStream in decode mode with a given codec and input bytes
    private BaseNCodecInputStream createDecodeStream(BaseNCodec codec, byte[] input) {
        InputStream in = new ByteArrayInputStream(input);
        return new BaseNCodecInputStream(in, codec, false);
    }

    // Helper to create a BaseNCodecInputStream in encode mode
    private BaseNCodecInputStream createEncodeStream(BaseNCodec codec, byte[] input) {
        InputStream in = new ByteArrayInputStream(input);
        return new BaseNCodecInputStream(in, codec, true);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testReadSingleByteDecodeBase64() throws IOException {
        // Base64 encode "Hello World" -> "SGVsbG8gV29ybGQ="
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        byte[] expected = "Hello World".getBytes();
        for (int i = 0; i < expected.length; i++) {
            int b = stream.read();
            assertEquals("byte " + i, expected[i] & 0xFF, b);
        }
        assertEquals(-1, stream.read()); // EOF
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteDecodeBase32() throws IOException {
        // Base32 encode "Hello World" -> "JBSWY3DPEB3W64TMMQ======"
        byte[] encoded = "JBSWY3DPEB3W64TMMQ======".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base32(), encoded);
        byte[] expected = "Hello World".getBytes();
        for (int i = 0; i < expected.length; i++) {
            int b = stream.read();
            assertEquals("byte " + i, expected[i] & 0xFF, b);
        }
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadArrayDecodeBase64() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        byte[] buffer = new byte[100];
        int len = stream.read(buffer, 0, 100);
        assertEquals("Hello World".length(), len);
        assertEquals("Hello World", new String(buffer, 0, len));
        assertEquals(-1, stream.read(buffer, 0, 1));
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadArrayDecodeBase32() throws IOException {
        byte[] encoded = "JBSWY3DPEB3W64TMMQ======".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base32(), encoded);
        byte[] buffer = new byte[100];
        int len = stream.read(buffer, 0, 100);
        assertEquals("Hello World".length(), len);
        assertEquals("Hello World", new String(buffer, 0, len));
        assertEquals(-1, stream.read(buffer, 0, 1));
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadArrayEncodeBase64() throws IOException {
        byte[] plain = "Hello World".getBytes();
        BaseNCodecInputStream stream = createEncodeStream(new Base64(), plain);
        byte[] buffer = new byte[100];
        int len = stream.read(buffer, 0, 100);
        String encoded = new String(buffer, 0, len);
        assertEquals("SGVsbG8gV29ybGQ=", encoded);
        stream.close();
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        stream.read(null, 0, 1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        stream.read(new byte[10], -1, 5);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLen() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        stream.read(new byte[10], 0, -1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetExceedsLength() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        stream.read(new byte[10], 11, 1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLenExceedsLength() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        stream.read(new byte[10], 5, 6);
    }

    @Test(timeout = 4000)
    public void testReadZeroLen() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), "SGVsbG8=".getBytes());
        assertEquals(0, stream.read(new byte[10], 0, 0));
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadEmptyStream() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        assertEquals(-1, stream.read());
        assertEquals(-1, stream.read(new byte[10], 0, 10));
        stream.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteNegativeValue() throws IOException {
        // Encode a byte with value 0x80 (negative as signed)
        byte[] plain = {(byte)0x80};
        BaseNCodecInputStream stream = createEncodeStream(new Base64(), plain);
        // Read encoded bytes (should be positive)
        int b = stream.read();
        assertTrue("encoded byte should be positive", b >= 0);
        stream.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // Defect: skip() should throw IllegalArgumentException for negative length
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeArgument() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), "SGVsbG8=".getBytes());
        stream.skip(-1);
    }

    // Defect: skip() to end should return number of bytes skipped and subsequent read returns -1
    @Test(timeout = 4000)
    public void testSkipToEnd() throws IOException {
        // Base64 decode "SGVsbG8gV29ybGQ=" -> 11 bytes
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        // Skip a huge number (should skip all available)
        long skipped = stream.skip(1000);
        assertEquals("Should skip exactly 11 bytes", 11, skipped);
        assertEquals("Subsequent read should return -1", -1, stream.read());
        stream.close();
    }

    // Defect: skip() past end should return actual number skipped (not exceed available)
    @Test(timeout = 4000)
    public void testSkipPastEnd() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes(); // decodes to 5 bytes
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        long skipped = stream.skip(10);
        assertEquals("Should skip exactly 5 bytes", 5, skipped);
        assertEquals(-1, stream.read());
        stream.close();
    }

    // Defect: skip() big but within bounds should return correct count
    @Test(timeout = 4000)
    public void testSkipBig() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes(); // 11 bytes decoded
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        long skipped = stream.skip(5);
        assertEquals(5, skipped);
        // Read remaining
        byte[] buffer = new byte[10];
        int len = stream.read(buffer, 0, 10);
        assertEquals(6, len);
        assertEquals(" World", new String(buffer, 0, len));
        stream.close();
    }

    // Defect: available() should return 1 if not at EOF, 0 if at EOF
    @Test(timeout = 4000)
    public void testAvailable() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes(); // 5 bytes decoded
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        int avail = stream.available();
        assertEquals("available should be 1 before reading", 1, avail);
        stream.read();
        avail = stream.available();
        assertEquals("available should be 1 after reading one byte", 1, avail);
        // Read all remaining
        byte[] buffer = new byte[10];
        stream.read(buffer, 0, 10);
        avail = stream.available();
        assertEquals("available should be 0 at EOF", 0, avail);
        stream.close();
    }

    // Defect: testCodec130 - decoding corruption
    @Test(timeout = 4000)
    public void testCodec130Base64() throws IOException {
        // Input that caused corruption: "ello World" encoded? Actually defect shows expected "[ello World]" but got garbage.
        // We'll test a known case: Base64 decode of "SGVsbG8gV29ybGQ=" should yield "Hello World"
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), encoded);
        byte[] buffer = new byte[100];
        int len = stream.read(buffer, 0, 100);
        String result = new String(buffer, 0, len);
        assertEquals("Hello World", result);
        stream.close();
    }

    @Test(timeout = 4000)
    public void testCodec130Base32() throws IOException {
        byte[] encoded = "JBSWY3DPEB3W64TMMQ======".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base32(), encoded);
        byte[] buffer = new byte[100];
        int len = stream.read(buffer, 0, 100);
        String result = new String(buffer, 0, len);
        assertEquals("Hello World", result);
        stream.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testMarkSupported() {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), new byte[0]);
        assertFalse("markSupported should return false", stream.markSupported());
    }

    // Test that read() handles the while(r==0) loop (rare case where readResults returns 0)
    // This requires a stream that causes the codec to return 0 initially.
    // We can simulate by providing a stream with non-base characters that are ignored.
    @Test(timeout = 4000)
    public void testReadWhileLoopZero() throws IOException {
        // Base64 decode of "!!SGVsbG8=" (with two non-base chars at start) should still decode correctly
        byte[] input = "!!SGVsbG8=".getBytes();
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), input);
        byte[] buffer = new byte[10];
        int len = stream.read(buffer, 0, 10);
        assertEquals(5, len);
        assertEquals("Hello", new String(buffer, 0, len));
        stream.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        BaseNCodecInputStream stream = createDecodeStream(new Base64(), "SGVsbG8=".getBytes());
        stream.close();
        // After close, read should throw IOException? Actually FilterInputStream.close() does not invalidate the stream.
        // But we can test that underlying stream is closed.
        // No specific contract to test here.
    }

    // Additional test for encoding mode with skip
    @Test(timeout = 4000)
    public void testSkipEncodeMode() throws IOException {
        byte[] plain = "Hello World".getBytes();
        BaseNCodecInputStream stream = createEncodeStream(new Base64(), plain);
        long skipped = stream.skip(5);
        // Encoding produces 16 bytes (including padding). Skip 5 encoded bytes.
        assertEquals(5, skipped);
        byte[] buffer = new byte[20];
        int len = stream.read(buffer, 0, 20);
        String remaining = new String(buffer, 0, len);
        // Original "Hello World" encoded: "SGVsbG8gV29ybGQ="
        // After skipping first 5 chars: "bG8gV29ybGQ="
        assertEquals("bG8gV29ybGQ=", remaining);
        stream.close();
    }
}