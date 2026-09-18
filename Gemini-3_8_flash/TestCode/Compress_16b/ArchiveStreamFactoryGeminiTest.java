package org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target: org.apache.commons.compress.archivers.ArchiveStreamFactory
 *
 * Decision / Condition Coverage Targets:
 * 1. createArchiveInputStream(archiverName, in):
 *    - archiverName == null -> IllegalArgumentException
 *    - in == null -> IllegalArgumentException
 *    - archiverName equalsIgnoreCase: AR, ZIP, TAR, JAR, CPIO, DUMP -> returns matching type
 *    - archiverName unknown / unsupported -> ArchiveException
 *
 * 2. createArchiveOutputStream(archiverName, out):
 *    - archiverName == null -> IllegalArgumentException
 *    - out == null -> IllegalArgumentException
 *    - archiverName equalsIgnoreCase: AR, ZIP, TAR, JAR, CPIO -> returns matching type
 *    - archiverName DUMP (output not supported) / unknown -> ArchiveException
 *
 * 3. createArchiveInputStream(in) [Auto-detection]:
 *    - in == null -> IllegalArgumentException
 *    - !in.markSupported() -> IllegalArgumentException
 *    - ZipArchiveInputStream.matches(sig, len) -> ZipArchiveInputStream
 *    - JarArchiveInputStream.matches(sig, len) -> JarArchiveInputStream
 *    - ArArchiveInputStream.matches(sig, len) -> ArArchiveInputStream
 *    - CpioArchiveInputStream.matches(sig, len) -> CpioArchiveInputStream
 *    - DumpArchiveInputStream.matches(dumpsig, len) -> DumpArchiveInputStream
 *    - TarArchiveInputStream.matches(tarheader, len) -> TarArchiveInputStream
 *    - COMPRESS-117 / COMPRESS-191 TAR verification:
 *        signatureLength >= 512, parses header, checks next entry.
 *        If valid TAR entry -> returns TarArchiveInputStream.
 *        If invalid TAR entry -> caught Exception, continues to throw ArchiveException.
 *    - Reset/mark IOException handling -> throws ArchiveException("Could not use reset and mark operations.")
 *    - Fallthrough -> ArchiveException("No Archiver found for the stream signature")
 *
 * Defects4J Bug Pattern:
 * - COMPRESS-191 / ArchiveStreamFactoryTest::aiffFilesAreNoTARs:
 *   AIFF files (starting with "FORM....AIFF") or random 512-byte blocks can fool TarArchiveInputStream.matches()
 *   or pass loose header checking if getNextEntry() does not strictly validate the tar checksum.
 *   When an AIFF-like header is passed, auto-detection must NOT classify it as TAR; it must throw ArchiveException.
 */
public class ArchiveStreamFactoryGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Explicit Stream Creation
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameAllSupported() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream dummyIn = new ByteArrayInputStream(new byte[0]);

        ArchiveInputStream arIn = factory.createArchiveInputStream(ArchiveStreamFactory.AR, dummyIn);
        assertTrue(arIn instanceof ArArchiveInputStream);

        ArchiveInputStream zipIn = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, dummyIn);
        assertTrue(zipIn instanceof ZipArchiveInputStream);

        ArchiveInputStream tarIn = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, dummyIn);
        assertTrue(tarIn instanceof TarArchiveInputStream);

        ArchiveInputStream jarIn = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, dummyIn);
        assertTrue(jarIn instanceof JarArchiveInputStream);

        ArchiveInputStream cpioIn = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, dummyIn);
        assertTrue(cpioIn instanceof CpioArchiveInputStream);

        ArchiveInputStream dumpIn = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, dummyIn);
        assertTrue(dumpIn instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamCaseInsensitive() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream dummyIn = new ByteArrayInputStream(new byte[0]);

        assertTrue(factory.createArchiveInputStream("Ar", dummyIn) instanceof ArArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("ZiP", dummyIn) instanceof ZipArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("TaR", dummyIn) instanceof TarArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("jAr", dummyIn) instanceof JarArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("CpIo", dummyIn) instanceof CpioArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("dUmP", dummyIn) instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameAllSupported() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayOutputStream dummyOut = new ByteArrayOutputStream();

        ArchiveOutputStream arOut = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, dummyOut);
        assertTrue(arOut instanceof ArArchiveOutputStream);

        ArchiveOutputStream zipOut = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, dummyOut);
        assertTrue(zipOut instanceof ZipArchiveOutputStream);

        ArchiveOutputStream tarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, dummyOut);
        assertTrue(tarOut instanceof TarArchiveOutputStream);

        ArchiveOutputStream jarOut = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, dummyOut);
        assertTrue(jarOut instanceof JarArchiveOutputStream);

        ArchiveOutputStream cpioOut = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, dummyOut);
        assertTrue(cpioOut instanceof CpioArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamCaseInsensitive() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayOutputStream dummyOut = new ByteArrayOutputStream();

        assertTrue(factory.createArchiveOutputStream("Ar", dummyOut) instanceof ArArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream("zIP", dummyOut) instanceof ZipArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream("tAr", dummyOut) instanceof TarArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream("JaR", dummyOut) instanceof JarArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream("CpIo", dummyOut) instanceof CpioArchiveOutputStream);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Defensive Guards
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
            fail("Expected IllegalArgumentException for null archiverName");
        } catch (IllegalArgumentException e) {
            assertEquals("Archivername must not be null.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
            fail("Expected IllegalArgumentException for null inputStream");
        } catch (IllegalArgumentException e) {
            assertEquals("InputStream must not be null.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
            fail("Expected IllegalArgumentException for null archiverName");
        } catch (IllegalArgumentException e) {
            assertEquals("Archivername must not be null.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
            fail("Expected IllegalArgumentException for null outputStream");
        } catch (IllegalArgumentException e) {
            assertEquals("OutputStream must not be null.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamUnknownArchiver() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveInputStream("unsupportedFormat", new ByteArrayInputStream(new byte[0]));
            fail("Expected ArchiveException for unsupported format");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("Archiver: unsupportedFormat not found."));
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamUnknownArchiver() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveOutputStream("unsupportedFormat", new ByteArrayOutputStream());
            fail("Expected ArchiveException for unsupported format");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("Archiver: unsupportedFormat not found."));
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamDumpNotSupported() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            // DUMP format has no output stream implementation
            factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
            fail("Expected ArchiveException because DUMP has no output stream");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("Archiver: dump not found."));
        }
    }

    @Test(timeout = 4000)
    public void testAutodetectNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        try {
            factory.createArchiveInputStream((InputStream) null);
            fail("Expected IllegalArgumentException for null stream");
        } catch (IllegalArgumentException e) {
            assertEquals("Stream must not be null.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAutodetectMarkNotSupportedStream() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream unmarkableStream = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };

        try {
            factory.createArchiveInputStream(unmarkableStream);
            fail("Expected IllegalArgumentException when mark is not supported");
        } catch (IllegalArgumentException e) {
            assertEquals("Mark is not supported.", e.getMessage());
        } catch (ArchiveException e) {
            fail("Expected IllegalArgumentException, not ArchiveException");
        }
    }

    @Test(timeout = 4000)
    public void testAutodetectEmptyStreamThrowsArchiveException() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream emptyIn = new ByteArrayInputStream(new byte[0]);
        try {
            factory.createArchiveInputStream(emptyIn);
            fail("Expected ArchiveException for empty stream");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("No Archiver found for the stream signature"));
        }
    }

    @Test(timeout = 4000)
    public void testAutodetectRandomShortBytesThrowsArchiveException() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] randomBytes = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        ByteArrayInputStream in = new ByteArrayInputStream(randomBytes);
        try {
            factory.createArchiveInputStream(in);
            fail("Expected ArchiveException for random bytes");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("No Archiver found for the stream signature"));
        }
    }

    @Test(timeout = 4000)
    public void testAutodetectIOExceptionOnReset() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream faultyStream = new InputStream() {
            private int count = 0;

            @Override
            public int read() {
                count++;
                return 0x20;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                Arrays.fill(b, off, off + len, (byte) 0x20);
                count += len;
                return len;
            }

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public synchronized void mark(int readlimit) {
                // do nothing
            }

            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated reset failure");
            }
        };

        try {
            factory.createArchiveInputStream(faultyStream);
            fail("Expected ArchiveException wrapping IOException on reset failure");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("Could not use reset and mark operations."));
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Tests (COMPRESS-191 / aiffFilesAreNoTARs)
    // =========================================================================

    /**
     * TARGET DEFECT: Defects4J bug aiffFilesAreNoTARs.
     * AIFF files begin with "FORM" and have length indicators that can be mistakenly
     * recognized as a TAR entry if header checksum is not verified during auto-detection.
     * The auto-detection MUST reject this and throw ArchiveException.
     */
    @Test(timeout = 4000)
    public void testAiffFilesAreNoTARs() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Construct a synthetic 512-byte block simulating an AIFF file header:
        // FORM (4 bytes) + chunk size (4 bytes) + AIFF (4 bytes) + COMM chunk header, etc.
        byte[] aiffHeader = new byte[512];
        aiffHeader[0] = 'F';
        aiffHeader[1] = 'O';
        aiffHeader[2] = 'R';
        aiffHeader[3] = 'M';
        aiffHeader[4] = 0;
        aiffHeader[5] = 0;
        aiffHeader[6] = 1;
        aiffHeader[7] = 0;
        aiffHeader[8] = 'A';
        aiffHeader[9] = 'I';
        aiffHeader[10] = 'F';
        aiffHeader[11] = 'F';

        ByteArrayInputStream in = new ByteArrayInputStream(aiffHeader);
        try {
            ArchiveInputStream ais = factory.createArchiveInputStream(in);
            fail("Created an input stream for a non-archive (AIFF file): " + ais.getClass().getName());
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("No Archiver found for the stream signature"));
        }
    }

    /**
     * Additional test targeting non-TAR 512-byte block filled with non-zero arbitrary data.
     * TAR auto-detection must verify the header checksum rather than assuming all >= 512
     * byte buffers with non-zero fields are valid TAR archives.
     */
    @Test(timeout = 4000)
    public void testArbitrary512BytesIsNoTAR() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] arbitraryData = new byte[512];
        for (int i = 0; i < arbitraryData.length; i++) {
            arbitraryData[i] = (byte) (i % 127);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(arbitraryData);
        try {
            ArchiveInputStream ais = factory.createArchiveInputStream(in);
            fail("Created an input stream for arbitrary non-tar bytes: " + ais.getClass().getName());
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("No Archiver found for the stream signature"));
        }
    }

    // =========================================================================
    // Partition D: Autodetection Success Paths for All Archive Formats
    // =========================================================================

    @Test(timeout = 4000)
    public void testAutodetectZip() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Standard ZIP local file header signature: PK\003\004
        byte[] zipHeader = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00};
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(zipHeader));
        assertNotNull(in);
        assertTrue("Expected ZipArchiveInputStream, got " + in.getClass(), in instanceof ZipArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectAr() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // AR archive signature: "!<arch>\n" (8 bytes)
        byte[] arHeader = new byte[]{'!', '<', 'a', 'r', 'c', 'h', '>', '\n', 0, 0, 0, 0};
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(arHeader));
        assertNotNull(in);
        assertTrue("Expected ArArchiveInputStream, got " + in.getClass(), in instanceof ArArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectCpio() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // CPIO new ASCII magic: "070701"
        byte[] cpioHeader = new byte[]{'0', '7', '0', '7', '0', '1', 0, 0, 0, 0, 0, 0};
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioHeader));
        assertNotNull(in);
        assertTrue("Expected CpioArchiveInputStream, got " + in.getClass(), in instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectDump() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // DumpArchiveInputStream magic: NFS_MAGIC = 60011 (0x0000ea6b) or FS_UFS2_MAGIC
        // Little-endian NFS_MAGIC is 0x6b, 0xea, 0x00, 0x00 at offset 24 of 32-byte header
        byte[] dumpHeader = new byte[32];
        // Offset 24: NFS_MAGIC little-endian
        dumpHeader[24] = (byte) 0x6b;
        dumpHeader[25] = (byte) 0xea;
        dumpHeader[26] = 0x00;
        dumpHeader[27] = 0x00;

        ArchiveInputStream in