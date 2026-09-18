package org.apache.commons.compress.compressors;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

/**
 * White-box JUnit 4 test suite targeting CompressorStreamFactory.
 * 
 * Branch & Defect Analysis Matrix:
 * - A: Constructors, getDecompressConcatenated(), setDecompressConcatenated() 
 *      -> branch: decompressUntilEOF null vs non-null.
 * - B: createCompressorInputStream(InputStream):
 *      null check, markSupport check, readFully+reset, 8 format matches (BZIP2, GZIP, PACK200, SNAPPY_FRAMED, Z, XZ, LZMA), IOException wrapper, "No Compressor found" throw.
 * - C: createCompressorInputStream(String, InputStream):
 *      null checks, case-insensitive matches (9 formats), IOException wrapper, "not found" throw.
 * - D: createCompressorOutputStream(String, OutputStream):
 *      null checks, case-insensitive matches (5 formats), IOException wrapper, "not found" throw.
 * - E: Defect-targeted: auto-detection fails to match valid gzip signature (see testDetectionRevealsBug).
 * - F: Boundary: empty name, unsupported output formats (LZMA, Z, SNAPPY_RAW, SNAPPY_FRAMED).
 * - G: Deprecated setter throws IllegalStateException when constructor already set.
 */
public class CompressorStreamFactoryDeepseekTest {

    // ========== Partition A: Constructors and state ==========

    @Test(timeout = 4000)
    public void testDefaultConstructorDecompressesConcatenatedFalse() {
        CompressorStreamFactory f = new CompressorStreamFactory();
        assertFalse("Default factory should have decompressConcatenated=false",
                f.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBoolTrue() {
        CompressorStreamFactory f = new CompressorStreamFactory(true);
        assertTrue("Factory(true) should have decompressConcatenated=true",
                f.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBoolFalse() {
        CompressorStreamFactory f = new CompressorStreamFactory(false);
        assertFalse("Factory(false) should have decompressConcatenated=false",
                f.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testDeprecatedSetterWorksOnDefaultConstructed() {
        CompressorStreamFactory f = new CompressorStreamFactory(); // decompressUntilEOF = null
        assertFalse("Initial state false", f.getDecompressConcatenated());
        f.setDecompressConcatenated(true);
        assertTrue("After setter true", f.getDecompressConcatenated());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testDeprecatedSetterThrowsWhenConstructorUsed() {
        CompressorStreamFactory f = new CompressorStreamFactory(true); // decompressUntilEOF != null
        f.setDecompressConcatenated(false);
    }

    // ========== Partition B: Boundary / null / mark ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateInputStreamFromStreamNull() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream((InputStream) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateInputStreamFromStreamNoMark() throws Exception {
        InputStream noMark = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CompressorStreamFactory().createCompressorInputStream(noMark);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateInputStreamFromStringNullName() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateInputStreamFromStringNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream("gz", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateOutputStreamNullName() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(null, new OutputStream() {
            @Override
            public void write(int b) throws IOException { }
        });
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateOutputStreamNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream("bzip2", null);
    }

    // ========== Partition C: Input stream auto-detection ==========

    // Helper to create a ByteArrayInputStream that supports mark
    private ByteArrayInputStream streamOf(byte... data) {
        return new ByteArrayInputStream(data);
    }

    @Test(timeout = 4000)
    public void testAutoDetectUnknownSignature() {
        CompressorStreamFactory f = new CompressorStreamFactory();
        byte[] unknown = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        try {
            f.createCompressorInputStream(streamOf(unknown));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue("Should contain 'No Compressor found'",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectGzipSignature() {
        // Magic bytes for gzip: 0x1f, 0x8b
        byte[] gzipMagic = {0x1f, (byte)0x8b, 0x08, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(gzipMagic));
            // If detection succeeds, construction may fail due to incomplete stream,
            // but that should NOT throw "No Compressor found".
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            String msg = e.getMessage();
            assertFalse("Defect: factory should not throw 'No Compressor found' for gzip signature",
                    msg.contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectBzip2Signature() {
        byte[] bzip2Magic = {'B', 'Z', 'h', '9', 0x31, 0x41, 0x59, 0x26, 0x53, 0x59, 0x3b, 0x7d};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(bzip2Magic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for bzip2 signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectPack200Signature() {
        // Pack200 magic: 0xca, 0xfe, 0xd0, 0x0d
        byte[] pack200Magic = {(byte)0xca, (byte)0xfe, (byte)0xd0, 0x0d, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(pack200Magic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for pack200 signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectSnappyFramedSignature() {
        // Framed Snappy magic: 0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59
        byte[] snappyMagic = {(byte)0xff, 0x06, 0x00, 0x00, 0x73, 0x4e, 0x61, 0x50, 0x70, 0x59, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(snappyMagic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for snappy-framed signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectZSignature() {
        // Z magic: 0x1f, 0x9d
        byte[] zMagic = {0x1f, (byte)0x9d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(zMagic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for Z signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectXZSignature() {
        // XZ magic: 0xfd, 0x37, 0x7a, 0x58, 0x5a, 0x00
        byte[] xzMagic = {(byte)0xfd, 0x37, 0x7a, 0x58, 0x5a, 0x00, 0, 0, 0, 0, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(xzMagic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for xz signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectLZMASignature() {
        // LZMA magic: 0x5d, 0x00, 0x00
        byte[] lzmaMagic = {0x5d, 0x00, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            new CompressorStreamFactory().createCompressorInputStream(streamOf(lzmaMagic));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertFalse("Defect: factory should not throw 'No Compressor found' for lzma signature",
                    e.getMessage().contains("No Compressor found"));
        }
    }

    // ========== Partition D: Named input streams ==========

    @Test(timeout = 4000)
    public void testNamedInputStreamGzip() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("gz", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue("Should contain 'Could not create' for valid name",
                    e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamBzip2() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("bzip2", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamXz() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("xz", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamLzma() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("lzma", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamPack200() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("pack200", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamSnappyRaw() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("snappy-raw", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamSnappyFramed() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("snappy-framed", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamZ() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("z", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamDeflate() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("deflate", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamUnknownName() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("unknown", streamOf(new byte[10]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue("Should contain 'not found'",
                    e.getMessage().contains("not found"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedInputStreamCaseInsensitivity() {
        // "GZ" should work as a synonym for "gz"
        try {
            new CompressorStreamFactory().createCompressorInputStream("GZ", streamOf(new byte[10]));
            fail("Expected CompressorException (construction failure)");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    // ========== Partition E: Output streams ==========

    // Helper to get a dummy OutputStream
    private OutputStream dummyOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException { }
        };
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamGzip() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("gz", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamBzip2() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("bzip2", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamXz() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("xz", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamPack200() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("pack200", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamDeflate() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("deflate", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamUnsupportedFormat() {
        // LZMA, Z, SNAPPY_RAW, SNAPPY_FRAMED are not supported for output
        try {
            new CompressorStreamFactory().createCompressorOutputStream("lzma", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue("Should contain 'not found' for unsupported output format",
                    e.getMessage().contains("not found"));
        }

        try {
            new CompressorStreamFactory().createCompressorOutputStream("z", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("not found"));
        }

        try {
            new CompressorStreamFactory().createCompressorOutputStream("snappy-raw", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("not found"));
        }

        try {
            new CompressorStreamFactory().createCompressorOutputStream("snappy-framed", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedOutputStreamUnknownName() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("unknown", dummyOutputStream());
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertTrue("Should contain 'not found'",
                    e.getMessage().contains("not found"));
        }
    }

    // ========== Partition F: Defect-revealing test ==========

    /**
     * This test targets the known defect: auto-detection fails to match a valid
     * gzip stream signature and throws "No Compressor found".  The test provides
     * a stream that begins with the gzip magic bytes, and asserts that the
     * factory does NOT throw that specific message.
     *
     * On the fixed version, detection will succeed and either return a stream
     * or throw a different CompressorException (e.g., "Failed to detect Compressor ...")
     * which is acceptable.
     */
    @Test(timeout = 4000)
    public void testDetectionRevealsBug() throws Exception {
        // Minimal gzip magic: first two bytes are 0x1f, 0x8b.
        byte[] gzipHeader = {0x1f, (byte)0x8b, 0x08, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        CompressorStreamFactory factory = new CompressorStreamFactory();
        try {
            factory.createCompressorInputStream(streamOf(gzipHeader));
            // If the factory returns without exception (unlikely with incomplete data)
            // we consider the test passed.
        } catch (CompressorException e) {
            String msg = e.getMessage();
            // The bug manifests as "No Compressor found for the stream signature."
            assertFalse("Defect: factory should not fail to detect the stream signature",
                    msg.contains("No Compressor found"));
        }
    }
}