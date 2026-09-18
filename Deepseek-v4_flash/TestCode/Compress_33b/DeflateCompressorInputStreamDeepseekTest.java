package org.apache.commons.compress.compressors.deflate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * White-box JUnit 4 test suite for DeflateCompressorInputStream.
 * Targets line/branch coverage and the known detection defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructor: two overloads, with/without DeflateParameters.
 * - read(): single byte, count logic (ret == -1 ? 0 : 1).
 * - read(byte[],int,int): delegates to InflaterInputStream, counts ret.
 * - skip(): delegates.
 * - available(): delegates.
 * - close(): delegates.
 * - Defect: CompressorStreamFactory fails to detect deflate stream due to missing/incorrect matches().
 *   Test: create a valid deflate stream and verify detection returns DeflateCompressorInputStream.
 */
public class DeflateCompressorInputStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testReadSingleByteWithZlibHeader() throws IOException {
        byte[] compressed = compressWithZlibHeader("Hello".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertEquals('H', in.read());
        assertEquals('e', in.read());
        assertEquals('l', in.read());
        assertEquals('l', in.read());
        assertEquals('o', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteWithoutZlibHeader() throws IOException {
        byte[] compressed = compressWithoutZlibHeader("World".getBytes("UTF-8"));
        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed), params);
        assertEquals('W', in.read());
        assertEquals('o', in.read());
        assertEquals('r', in.read());
        assertEquals('l', in.read());
        assertEquals('d', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadBufferWithZlibHeader() throws IOException {
        byte[] compressed = compressWithZlibHeader("Apache Commons".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        byte[] buf = new byte[14];
        int len = in.read(buf, 0, buf.length);
        assertEquals(14, len);
        assertEquals("Apache Commons", new String(buf, "UTF-8"));
        assertEquals(-1, in.read(buf, 0, 1));
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadBufferWithoutZlibHeader() throws IOException {
        byte[] compressed = compressWithoutZlibHeader("Compress".getBytes("UTF-8"));
        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed), params);
        byte[] buf = new byte[8];
        int len = in.read(buf, 0, buf.length);
        assertEquals(8, len);
        assertEquals("Compress", new String(buf, "UTF-8"));
        assertEquals(-1, in.read(buf, 0, 1));
        in.close();
    }

    @Test(timeout = 4000)
    public void testSkip() throws IOException {
        byte[] compressed = compressWithZlibHeader("SkipMe".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertEquals(4, in.skip(4));
        assertEquals('M', in.read());
        assertEquals('e', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testAvailable() throws IOException {
        byte[] compressed = compressWithZlibHeader("Avail".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertTrue(in.available() > 0);
        in.read();
        assertTrue(in.available() > 0);
        in.close();
        assertEquals(0, in.available());
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        byte[] compressed = compressWithZlibHeader("Close".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        in.close();
        // After close, read should throw IOException
        try {
            in.read();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullInputStream() {
        new DeflateCompressorInputStream(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullInputStreamWithParams() {
        new DeflateCompressorInputStream(null, new DeflateParameters());
    }

    @Test(timeout = 4000)
    public void testReadBufferWithZeroLength() throws IOException {
        byte[] compressed = compressWithZlibHeader("Data".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        byte[] buf = new byte[0];
        assertEquals(0, in.read(buf, 0, 0));
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadBufferWithNegativeOffset() throws IOException {
        byte[] compressed = compressWithZlibHeader("Data".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        byte[] buf = new byte[10];
        try {
            in.read(buf, -1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipZero() throws IOException {
        byte[] compressed = compressWithZlibHeader("Skip".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertEquals(0, in.skip(0));
        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipNegative() throws IOException {
        byte[] compressed = compressWithZlibHeader("Skip".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertEquals(0, in.skip(-1));
        in.close();
    }

    // ==================== Partition C: Defect-Targeted Detection Test ====================

    @Test(timeout = 4000)
    public void testDetectionOfDeflateStream() throws Exception {
        // Create a valid deflate compressed stream with zlib header
        byte[] compressed = compressWithZlibHeader("DetectMe".getBytes("UTF-8"));
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);

        // Use CompressorStreamFactory to detect the compressor
        CompressorStreamFactory factory = new CompressorStreamFactory();
        CompressorInputStream cis = factory.createCompressorInputStream(bais);

        // The detected stream should be an instance of DeflateCompressorInputStream
        assertTrue("Expected DeflateCompressorInputStream, but got " + cis.getClass().getName(),
                cis instanceof DeflateCompressorInputStream);

        // Verify we can read the decompressed data
        byte[] buf = new byte[8];
        int len = cis.read(buf, 0, buf.length);
        assertEquals(8, len);
        assertEquals("DetectMe", new String(buf, "UTF-8"));
        cis.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        byte[] compressed = compressWithZlibHeader("".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        in.close();
        in.read(); // should throw IOException
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadBufferAfterClose() throws IOException {
        byte[] compressed = compressWithZlibHeader("".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        in.close();
        in.read(new byte[1], 0, 1);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testSkipAfterClose() throws IOException {
        byte[] compressed = compressWithZlibHeader("".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        in.close();
        in.skip(1);
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================
    // No equals/hashCode/clone to test; focus on constructor variants.

    @Test(timeout = 4000)
    public void testConstructorWithDefaultParameters() throws IOException {
        byte[] compressed = compressWithZlibHeader("Default".getBytes("UTF-8"));
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed));
        assertNotNull(in);
        in.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithExplicitZlibHeaderTrue() throws IOException {
        byte[] compressed = compressWithZlibHeader("ZlibTrue".getBytes("UTF-8"));
        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(true);
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed), params);
        assertEquals('Z', in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithExplicitZlibHeaderFalse() throws IOException {
        byte[] compressed = compressWithoutZlibHeader("Raw".getBytes("UTF-8"));
        DeflateParameters params = new DeflateParameters();
        params.setWithZlibHeader(false);
        DeflateCompressorInputStream in = new DeflateCompressorInputStream(
                new ByteArrayInputStream(compressed), params);
        assertEquals('R', in.read());
        in.close();
    }

    // ==================== Helper Methods ====================

    private byte[] compressWithZlibHeader(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, false); // nowrap=false => zlib header
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] compressWithoutZlibHeader(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true); // nowrap=true => raw deflate
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            dos.write(data);
        }
        return baos.toByteArray();
    }
}