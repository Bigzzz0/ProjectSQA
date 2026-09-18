package org.apache.commons.compress.archivers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * White-box test suite for ArchiveStreamFactory targeting maximum coverage
 * and the known defect where AIFF files are incorrectly identified as TAR archives.
 *
 * [Branch & Defect Analysis Matrix]
 * - createArchiveInputStream(String, InputStream): null checks, 6 format branches, unknown format throws ArchiveException.
 * - createArchiveOutputStream(String, OutputStream): null checks, 5 format branches, unknown format throws ArchiveException.
 * - createArchiveInputStream(InputStream): null check, markSupported check, signature matching for ZIP, JAR, AR, CPIO, DUMP, TAR,
 *   fallback TAR header checksum verification (COMPRESS-117/191), IOException handling, final "No Archiver found" exception.
 * - Defect: AIFF files (FORM+AIFF) should not be recognized as TAR; the code's TAR detection via getNextEntry may succeed incorrectly.
 *   We must assert that an AIFF-like stream throws ArchiveException.
 */
public class ArchiveStreamFactoryDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByArName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.AR, in);
        assertNotNull(ais);
        assertTrue(ais instanceof ArArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByZipName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByTarName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, in);
        assertNotNull(ais);
        assertTrue(ais instanceof TarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByJarName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, in);
        assertNotNull(ais);
        assertTrue(ais instanceof JarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByCpioName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, in);
        assertNotNull(ais);
        assertTrue(ais instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByDumpName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, in);
        assertNotNull(ais);
        assertTrue(ais instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByArName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof ArArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByZipName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
        assertNotNull(aos);
        assertTrue(aos instanceof ZipArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByTarName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof TarArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByJarName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof JarArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByCpioName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, out);
        assertNotNull(aos);
        assertTrue(aos instanceof CpioArchiveOutputStream);
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamNullInputStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamNullOutputStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateArchiveInputStreamUnknownArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamUnknownArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamAutoNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamAutoNoMarkSupport() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream noMark = new InputStream() {
            @Override public int read() throws IOException { return 0; }
            @Override public boolean markSupported() { return false; }
        };
        factory.createArchiveInputStream(noMark);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone (AIFF misdetection) ----------

    /**
     * Defect-specific test: AIFF files (FORM+AIFF) should NOT be recognized as TAR.
     * The auto-detection in createArchiveInputStream(InputStream) may incorrectly
     * treat them as TAR archives. This test verifies that an AIFF-like stream
     * throws ArchiveException.
     */
    @Test(timeout = 4000)
    public void testAiffFileIsNotRecognizedAsTar() {
        // AIFF file header: "FORM" (4 bytes) + size (4 bytes) + "AIFF" (4 bytes)
        // We'll create a minimal valid AIFF header (12 bytes) plus padding to exceed 512 bytes
        // to trigger the TAR fallback path.
        byte[] aiffHeader = new byte[600];
        // "FORM"
        aiffHeader[0] = 'F'; aiffHeader[1] = 'O'; aiffHeader[2] = 'R'; aiffHeader[3] = 'M';
        // size (big-endian) = 592 (0x250) -> bytes 4-7
        aiffHeader[4] = 0x00; aiffHeader[5] = 0x00; aiffHeader[6] = 0x02; aiffHeader[7] = 0x50;
        // "AIFF"
        aiffHeader[8] = 'A'; aiffHeader[9] = 'I'; aiffHeader[10] = 'F'; aiffHeader[11] = 'F';
        // Fill rest with zeros (not a valid TAR header)
        for (int i = 12; i < 600; i++) {
            aiffHeader[i] = 0;
        }

        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(aiffHeader);
        try {
            ArchiveInputStream ais = factory.createArchiveInputStream(in);
            // If we reach here, the bug is present (should have thrown ArchiveException)
            fail("Expected ArchiveException for AIFF stream, but got: " + ais.getClass().getName());
        } catch (ArchiveException e) {
            // Expected: "No Archiver found for the stream signature"
            assertTrue(e.getMessage().contains("No Archiver found"));
        }
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamAutoIOException() throws Exception {
        // Simulate an InputStream that throws IOException on read/reset
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream faulty = new InputStream() {
            private boolean first = true;
            @Override public int read() throws IOException {
                if (first) { first = false; return 0; }
                throw new IOException("Simulated failure");
            }
            @Override public boolean markSupported() { return true; }
            @Override public void reset() throws IOException { throw new IOException("Reset failed"); }
        };
        try {
            factory.createArchiveInputStream(faulty);
            fail("Expected ArchiveException wrapping IOException");
        } catch (ArchiveException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamAutoEmptyStream() throws Exception {
        // Empty stream should throw ArchiveException
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        try {
            factory.createArchiveInputStream(empty);
            fail("Expected ArchiveException for empty stream");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("No Archiver found"));
        }
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testArchiveStreamFactoryIsImmutable() {
        // The class is annotated @Immutable, but we can verify that constants are accessible
        assertNotNull(ArchiveStreamFactory.AR);
        assertNotNull(ArchiveStreamFactory.CPIO);
        assertNotNull(ArchiveStreamFactory.DUMP);
        assertNotNull(ArchiveStreamFactory.JAR);
        assertNotNull(ArchiveStreamFactory.TAR);
        assertNotNull(ArchiveStreamFactory.ZIP);
    }

    // Helper inner class for OutputStream testing (since ByteArrayOutputStream is not imported)
    private static class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {
        // just reuse
    }
}