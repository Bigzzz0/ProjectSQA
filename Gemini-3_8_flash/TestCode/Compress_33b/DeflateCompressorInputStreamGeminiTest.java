/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor 1: DeflateCompressorInputStream(InputStream) -> delegates to (InputStream, new DeflateParameters()).
 * 2. Constructor 2: DeflateCompressorInputStream(InputStream, DeflateParameters)
 *    - Branch: parameters.withZlibHeader() == true  -> Inflater(false) (expects zlib wrapper)
 *    - Branch: parameters.withZlibHeader() == false -> Inflater(true)  (raw deflate, nowrap)
 * 3. read():
 *    - Branch: ret == -1 -> count(0)
 *    - Branch: ret != -1 -> count(1)
 *    - Returns byte value (0..255) or -1 on EOF.
 * 4. read(byte[] b, int off, int len):
 *    - Delegates to in.read(b, off, len)
 *    - Calls count(ret)
 *    - Edge cases: len = 0, EOF (-1), partial read.
 * 5. skip(long n):
 *    - Delegates to in.skip(n), boundary n = 0, n > 0.
 * 6. available():
 *    - Delegates to in.available(), returns 0 or 1.
 * 7. close():
 *    - Delegates to in.close(), ensures underlying resources released.
 * 8. getBytesRead() / getCount():
 *    - Inherited from CompressorInputStream; must accurately track decompressed bytes.
 *
 * Known Defect (Defects4J / COMPRESS-297):
 * - DetectCompressorTestCase::testDetection -> CompressorException: No Compressor found for the stream signature.
 * - The Javadoc for `matches(byte[] signature, int length)` is present, but the static method `matches`
 *   was missing from DeflateCompressorInputStream, preventing auto-detection of zlib/deflate streams.
 * - Targeted via reflection-based assertion to ensure zero compilation error on the buggy revision
 *   while cleanly exposing the missing method defect.
 */
package org.apache.commons.compress.compressors.deflate;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.junit.Test;

public class DeflateCompressorInputStreamGeminiTest {

    private byte[] compress(byte[] data, boolean withZlibHeader) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, !withZlibHeader);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        } finally {
            deflater.end();
        }
        return baos.toByteArray();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadSingleByteWithZlibHeader() throws IOException {
        byte[] original = "Hello Deflate World!".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            for (byte expectedByte : original) {
                int b = in.read();
                assertTrue("Stream returned premature EOF", b != -1);
                assertEquals("Byte content mismatch", expectedByte & 0xFF, b);
            }
            assertEquals("Expected EOF on next read()", -1, in.read());
            assertEquals("Bytes read count mismatch", (long) original.length, in.getBytesRead());
        }
    }

    @Test(timeout = 4000)
    public void testReadByteArrayWithZlibHeader() throws IOException {
        byte[] original = "Testing Deflate byte array bulk reading logic.".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[original.length];
            int totalRead = 0;
            int readBytes;
            while ((readBytes = in.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
                totalRead += readBytes;
            }
            assertEquals(original.length, totalRead);
            assertArrayEquals(original, buffer);
            assertEquals(-1, in.read(buffer, 0, 1));
            assertEquals((long) original.length, in.getBytesRead());
        }
    }

    @Test(timeout = 4000)
    public void testRawDeflateWithoutZlibHeader() throws IOException {
        byte[] original = "Deflate raw without header payload.".getBytes("UTF-8");
        byte[] compressed = compress(original, false);

        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);
        assertFalse("Parameter should have withZlibHeader = false", params.withZlibHeader());

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed), params)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            assertArrayEquals(original, out.toByteArray());
            assertEquals((long) original.length, in.getBytesRead());
        }
    }

    @Test(timeout = 4000)
    public void testSkipOperation() throws IOException {
        byte[] original = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            long skipped = in.skip(10);
            assertEquals(10L, skipped);

            byte[] remaining = new byte[original.length - 10];
            int readCount = 0;
            while (readCount < remaining.length) {
                int r = in.read(remaining, readCount, remaining.length - readCount);
                if (r == -1) {
                    break;
                }
                readCount += r;
            }
            assertEquals(remaining.length, readCount);

            byte[] expected = new byte[original.length - 10];
            System.arraycopy(original, 10, expected, 0, expected.length);
            assertArrayEquals(expected, remaining);
        }
    }

    @Test(timeout = 4000)
    public void testAvailableAndClose() throws IOException {
        byte[] original = "Availability test payload".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed));
        int initialAvailable = in.available();
        assertTrue("Available should be >= 0", initialAvailable >= 0);

        in.close();
        try {
            in.read();
            fail("Expected IOException when reading from closed DeflateCompressorInputStream");
        } catch (IOException expected) {
            // Success: stream is closed
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStreamDecompression() throws IOException {
        byte[] original = new byte[0];
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            assertEquals(-1, in.read());
            assertEquals(0L, in.getBytesRead());
        }
    }

    @Test(timeout = 4000)
    public void testReadZeroBytesBuffer() throws IOException {
        byte[] original = "Zero bytes buffer test".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buf = new byte[10];
            int readCount = in.read(buf, 0, 0);
            assertEquals("Reading 0 bytes should return 0", 0, readCount);
            assertEquals("No bytes should have been counted", 0L, in.getBytesRead());
        }
    }

    @Test(timeout = 4000)
    public void testSkipZeroBytes() throws IOException {
        byte[] original = "Skip 0 bytes test".getBytes("UTF-8");
        byte[] compressed = compress(original, true);

        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(compressed))) {
            long skipped = in.skip(0);
            assertEquals(0L, skipped);
            int firstByte = in.read();
            assertEquals(original[0] & 0xFF, firstByte);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Ground Truth Bug: Missing matches() method)
    // =========================================================================

    /**
     * Targets the defect where DeflateCompressorInputStream lacked the static `matches(byte[], int)`
     * method required by CompressorStreamFactory for auto-detecting DEFLATE streams.
     * Uses reflection to verify method presence and signature contract without compilation failure.
     */
    @Test(timeout = 4000)
    public void testMatchesSignatureDefect() {
        Method matchesMethod;
        try {
            matchesMethod = DeflateCompressorInputStream.class.getMethod("matches", byte[].class, int.class);
        } catch (NoSuchMethodException e) {
            fail("Defect COMPRESS-297 detected: DeflateCompressorInputStream is missing required "
                    + "'public static boolean matches(byte[] signature, int length)' method for stream detection.");
            return;
        }

        assertTrue("matches method must be public", Modifier.isPublic(matchesMethod.getModifiers()));
        assertTrue("matches method must be static", Modifier.isStatic(matchesMethod.getModifiers()));
        assertEquals("matches method must return boolean", boolean.class, matchesMethod.getReturnType());

        try {
            // Standard zlib header magic bytes:
            // CMF: 0x78 (deflate, 32k window)
            // FLG: check bits such that (CMF * 256 + FLG) % 31 == 0
            // 0x78 0x01 (No compression) -> (0x7801 = 30721 % 31 == 0)
            // 0x78 0x9C (Default compression) -> (0x789C = 30876 % 31 == 0)
            // 0x78 0xDA (Best compression) -> (0x78DA = 30938 % 31 == 0)
            byte[] zlibDefault = new byte[] { (byte) 0x78, (byte) 0x9C };
            Boolean matched = (Boolean) matchesMethod.invoke(null, zlibDefault, zlibDefault.length);
            assertTrue("Expected matches() to return true for standard zlib header 0x78 0x9C", matched);

            byte[] zlibNoComp = new byte[] { (byte) 0x78, (byte) 0x01 };
            Boolean matchedNoComp = (Boolean) matchesMethod.invoke(null, zlibNoComp, zlibNoComp.length);
            assertTrue("Expected matches() to return true for zlib header 0x78 0x01", matchedNoComp);

            byte[] invalidSig = new byte[] { 0x00, 0x00 };
            Boolean matchedInvalid = (Boolean) matchesMethod.invoke(null, invalidSig, invalidSig.length);
            assertFalse("Expected matches() to return false for invalid header 0x00 0x00", matchedInvalid);

            byte[] tooShort = new byte[] { (byte) 0x78 };
            Boolean matchedTooShort = (Boolean) matchesMethod.invoke(null, tooShort, 1);
            assertFalse("Expected matches() to return false when length < 2", matchedTooShort);
        } catch (Exception e) {
            fail("Invocation of matches() threw unexpected exception: " + e);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCorruptedStreamThrowsIOException() {
        byte[] corruptedData = new byte[] { 0x12, 0x34, 0x56, 0x78 };
        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(corruptedData))) {
            in.read();
            fail("Expected IOException due to corrupted compressed stream");
        } catch (IOException expected) {
            // Successfully caught corrupted stream exception
        }
    }

    @Test(timeout = 4000)
    public void testMismatchedHeaderExpectationThrowsIOException() throws IOException {
        byte[] rawCompressed = compress("Test payload".getBytes("UTF-8"), false);
        // Stream has raw deflate data, but default constructor expects zlib header
        try (DeflateCompressorInputStream in = new DeflateCompressorInputStream(new ByteArrayInputStream(rawCompressed))) {
            in.read();
            fail("Expected IOException when zlib header is expected but missing");
        } catch (IOException expected) {
            // Successfully caught header mismatch
        }
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullInputStreamThrowsException() {
        new DeflateCompressorInputStream((InputStream) null);
    }
}