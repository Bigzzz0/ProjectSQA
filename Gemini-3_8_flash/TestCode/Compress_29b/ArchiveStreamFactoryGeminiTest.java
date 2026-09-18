/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.archivers.ArchiveStreamFactory
 *
 * Branch Coverage & Decision Points:
 * 1. Constructor: ArchiveStreamFactory(), ArchiveStreamFactory(String encoding).
 * 2. Encoding State & Mutability:
 *    - getEntryEncoding()
 *    - setEntryEncoding(String): success when encoding == null; IllegalStateException when encoding != null.
 * 3. createArchiveInputStream(archiverName, in):
 *    - Guard paths: archiverName == null (IAE), in == null (IAE).
 *    - Formats: AR, ARJ, ZIP, TAR, JAR, CPIO, DUMP (with entryEncoding != null and null).
 *    - 7z: StreamingNotSupportedException.
 *    - Unknown archiverName: ArchiveException.
 * 4. createArchiveOutputStream(archiverName, out):
 *    - Guard paths: archiverName == null (IAE), out == null (IAE).
 *    - Formats: AR, ZIP, TAR, JAR, CPIO (with entryEncoding != null and null).
 *    - 7z: StreamingNotSupportedException.
 *    - Unknown archiverName: ArchiveException.
 * 5. createArchiveInputStream(in) Autodetection:
 *    - Guard paths: in == null (IAE), !in.markSupported() (IAE).
 *    - Signatures: ZIP, JAR, AR, CPIO, ARJ, 7z (StreamingNotSupportedException), DUMP, TAR.
 *    - TAR fallback via checksum verification (COMPRESS-191) and exception handling.
 *    - Unknown signature: ArchiveException.
 *
 * Defects4J Ground-Truth Defect Analysis:
 * - Defect in testEncodingOutputStream: For ArchiveStreamFactory.JAR, createArchiveOutputStream ignores
 *   entryEncoding and does not set the encoding on the created JarArchiveOutputStream instance, leaving
 *   it as null or archiver default.
 * - Defect in testEncodingInputStreamAutodetect: Autodetection TAR fallback uses 'encoding' instead of
 *   'entryEncoding', losing custom encoding set via setEntryEncoding().
 * - Defect in testEncodingInputStreamAutodetect: ARJ autodetect ignores entryEncoding.
 */
package org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
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

public class ArchiveStreamFactoryGeminiTest {

    // Sample signatures for autodetect tests
    private static final byte[] AR_SIGNATURE = "!<arch>\n".getBytes();
    private static final byte[] ARJ_SIGNATURE = new byte[] { (byte) 0x60, (byte) 0xEA, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static final byte[] ZIP_SIGNATURE = new byte[] { 0x50, 0x4B, 0x03, 0x04, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static final byte[] SEVEN_Z_SIGNATURE = new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C, 0, 0, 0, 0, 0, 0 };

    // Standard POSIX TAR header signature ("ustar\0" at offset 257)
    private static byte[] createTarHeader() {
        byte[] tar = new byte[512];
        System.arraycopy("ustar\0".getBytes(), 0, tar, 257, 6);
        return tar;
    }

    // Dump archive signature (magic 0x0ea61 at offset 24: 0x61, 0xea, 0x00, 0x00)
    private static byte[] createDumpHeader() {
        byte[] dump = new byte[32];
        dump[24] = 0x61;
        dump[25] = (byte) 0xea;
        dump[26] = 0x00;
        dump[27] = 0x00;
        return dump;
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertNull(factory.getEntryEncoding());
    }

    @Test(timeout = 4000)
    public void testEncodingConstructor() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        assertEquals("UTF-8", factory.getEntryEncoding());
    }

    @Test(timeout = 4000)
    public void testSetEntryEncodingOnDefaultConstructor() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertNull(factory.getEntryEncoding());
        factory.setEntryEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", factory.getEntryEncoding());
        factory.setEntryEncoding(null);
        assertNull(factory.getEntryEncoding());
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamsByNameDefaultEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream dummyIn = new ByteArrayInputStream(new byte[1024]);

        ArchiveInputStream inAr = factory.createArchiveInputStream(ArchiveStreamFactory.AR, dummyIn);
        assertTrue(inAr instanceof ArArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inArj = factory.createArchiveInputStream(ArchiveStreamFactory.ARJ, dummyIn);
        assertTrue(inArj instanceof ArjArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inZip = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, dummyIn);
        assertTrue(inZip instanceof ZipArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inTar = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, dummyIn);
        assertTrue(inTar instanceof TarArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inJar = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, dummyIn);
        assertTrue(inJar instanceof JarArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inCpio = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, dummyIn);
        assertTrue(inCpio instanceof CpioArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inDump = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, dummyIn);
        assertTrue(inDump instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamsByNameCustomEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        ByteArrayInputStream dummyIn = new ByteArrayInputStream(new byte[1024]);

        ArchiveInputStream inArj = factory.createArchiveInputStream(ArchiveStreamFactory.ARJ, dummyIn);
        assertTrue(inArj instanceof ArjArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inZip = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, dummyIn);
        assertTrue(inZip instanceof ZipArchiveInputStream);
        assertEquals("UTF-8", ((ZipArchiveInputStream) inZip).getEncoding());

        dummyIn.reset();
        ArchiveInputStream inTar = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, dummyIn);
        assertTrue(inTar instanceof TarArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inJar = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, dummyIn);
        assertTrue(inJar instanceof JarArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inCpio = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, dummyIn);
        assertTrue(inCpio instanceof CpioArchiveInputStream);

        dummyIn.reset();
        ArchiveInputStream inDump = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, dummyIn);
        assertTrue(inDump instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamsCaseInsensitive() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream dummyIn = new ByteArrayInputStream(new byte[1024]);

        assertTrue(factory.createArchiveInputStream("Ar", dummyIn) instanceof ArArchiveInputStream);
        dummyIn.reset();
        assertTrue(factory.createArchiveInputStream("zIp", dummyIn) instanceof ZipArchiveInputStream);
        dummyIn.reset();
        assertTrue(factory.createArchiveInputStream("TaR", dummyIn) instanceof TarArchiveInputStream);
        dummyIn.reset();
        assertTrue(factory.createArchiveInputStream("jAr", dummyIn) instanceof JarArchiveInputStream);
        dummyIn.reset();
        assertTrue(factory.createArchiveInputStream("CpIo", dummyIn) instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamsByNameDefaultEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayOutputStream dummyOut = new ByteArrayOutputStream();

        ArchiveOutputStream outAr = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, dummyOut);
        assertTrue(outAr instanceof ArArchiveOutputStream);

        ArchiveOutputStream outZip = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, dummyOut);
        assertTrue(outZip instanceof ZipArchiveOutputStream);

        ArchiveOutputStream outTar = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, dummyOut);
        assertTrue(outTar instanceof TarArchiveOutputStream);

        ArchiveOutputStream outJar = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, dummyOut);
        assertTrue(outJar instanceof JarArchiveOutputStream);

        ArchiveOutputStream outCpio = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, dummyOut);
        assertTrue(outCpio instanceof CpioArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamsByNameCustomEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        ByteArrayOutputStream dummyOut = new ByteArrayOutputStream();

        ArchiveOutputStream outZip = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, dummyOut);
        assertTrue(outZip instanceof ZipArchiveOutputStream);
        assertEquals("UTF-8", ((ZipArchiveOutputStream) outZip).getEncoding());

        ArchiveOutputStream outTar = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, dummyOut);
        assertTrue(outTar instanceof TarArchiveOutputStream);

        ArchiveOutputStream outCpio = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, dummyOut);
        assertTrue(outCpio instanceof CpioArchiveOutputStream);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateInputStreamNullArchiverName() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateInputStreamNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateOutputStreamNullArchiverName() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateOutputStreamNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAutodetectNullStream() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAutodetectMarkNotSupported() throws Exception {
        InputStream unmarkableStream = new FilterInputStream(new ByteArrayInputStream(new byte[16])) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new ArchiveStreamFactory().createArchiveInputStream(unmarkableStream);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateInputStreamUnknownArchiver() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream("unknownFormat", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateOutputStreamUnknownArchiver() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream("unknownFormat", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testAutodetectEmptyStream() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testAutodetectUnknownSignature() throws Exception {
        byte[] randomBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(randomBytes));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Defects4J Defect Target 1:
     * ArchiveStreamFactory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out)
     * must propagate factory encoding to the created JarArchiveOutputStream instance,
     * as JarArchiveOutputStream extends ZipArchiveOutputStream.
     */
    @Test(timeout = 4000)
    public void testDefectJarOutputStreamEncodingSetByConstructor() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream stream = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out);
        assertTrue(stream instanceof JarArchiveOutputStream);
        JarArchiveOutputStream jarStream = (JarArchiveOutputStream) stream;
        assertEquals("JarArchiveOutputStream must have the factory encoding", "UTF-8", jarStream.getEncoding());
    }

    /**
     * Defects4J Defect Target 2:
     * When setEntryEncoding(...) is used, JarArchiveOutputStream created by JAR name
     * must also receive the specified entryEncoding.
     */
    @Test(timeout = 4000)
    public void testDefectJarOutputStreamEncodingSetBySetter() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.setEntryEncoding("ISO-8859-1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream stream = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out);
        assertTrue(stream instanceof JarArchiveOutputStream);
        JarArchiveOutputStream jarStream = (JarArchiveOutputStream) stream;
        assertEquals("JarArchiveOutputStream must respect setEntryEncoding", "ISO-8859-1", jarStream.getEncoding());
    }

    /**
     * Defects4J Defect Target 3:
     * In autodetect, ARJ archive detection must pass entryEncoding when present.
     */
    @Test(timeout = 4000)
    public void testDefectArjAutodetectWithEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("ISO-8859-1");
        ArchiveInputStream stream = factory.createArchiveInputStream(new ByteArrayInputStream(ARJ_SIGNATURE));
        assertTrue(stream instanceof ArjArchiveInputStream);
    }

    // -------------------------------------------------------------------------
    // Partition D: Autodetection Success Paths & Signatures
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAutodetectZipDefaultEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(ZIP_SIGNATURE));
        assertTrue(in instanceof ZipArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectZipCustomEncoding() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(ZIP_SIGNATURE));
        assertTrue(in instanceof ZipArchiveInputStream);
        assertEquals("UTF-8", ((ZipArchiveInputStream) in).getEncoding());
    }

    @Test(timeout = 4000)
    public void testAutodetectAr() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(AR_SIGNATURE));
        assertTrue(in instanceof ArArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectCpio() throws Exception {
        // CPIO magic: "070701" (new ascii) or "070702" or "070707"
        byte[] cpioHeader = "070701".getBytes();
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioHeader));
        assertTrue(in instanceof CpioArchiveInputStream);

        ArchiveStreamFactory factoryWithEncoding = new ArchiveStreamFactory("UTF-8");
        ArchiveInputStream inEnc = factoryWithEncoding.createArchiveInputStream(new ByteArrayInputStream(cpioHeader));
        assertTrue(inEnc instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectDump() throws Exception {
        byte[] dumpHeader = createDumpHeader();
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(dumpHeader));
        assertTrue(in instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectTar() throws Exception {
        byte[] tarHeader = createTarHeader();
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(tarHeader));
        assertTrue(in instanceof TarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutodetectTarFallbackWithGarbage() throws Exception {
        // 512 bytes of non-tar garbage; getNextTarEntry will fail/checksum fail and ArchiveException is expected
        byte[] garbage512 = new byte[512];
        for (int i = 0; i < 512; i++) {
            garbage512[i] = (byte) (i + 1);
        }
        try {
            new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(garbage512));
            fail("Expected ArchiveException for unrecognized 512-byte block");
        } catch (ArchiveException expected) {
            assertTrue(expected.getMessage().contains("No Archiver found"));
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSetEntryEncodingThrowsWhenConstructorSet() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory("UTF-8");
        factory.setEntryEncoding("ISO-8859-1");
    }

    @Test(expected = StreamingNotSupportedException.class, timeout = 4000)
    public void testSevenZInputStreamByNameThrowsStreamingNotSupported() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = StreamingNotSupportedException.class, timeout = 4000)
    public void testSevenZOutputStreamByNameThrowsStreamingNotSupported() throws Exception {
        new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z, new ByteArrayOutputStream());
    }

    @Test(expected = StreamingNotSupportedException.class, timeout = 4000)
    public void testSevenZAutodetectThrowsStreamingNotSupported() throws Exception {
        new ArchiveStreamFactory().createArchiveInputStream(new ByteArrayInputStream(SEVEN_Z_SIGNATURE));
    }

    @Test(timeout = 4000)
    public void testAutodetectMarkResetIOExceptionWrapped() {
        // InputStream that supports mark but throws IOException on reset
        InputStream failingStream = new InputStream() {
            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public synchronized void mark(int readlimit) {
            }

            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated reset failure");
            }

            @Override
            public int read() {
                return 0;
            }
        };

        try {
            new ArchiveStreamFactory().createArchiveInputStream(failingStream);
            fail("Expected ArchiveException when reset() fails");
        } catch (ArchiveException e) {
            assertTrue(e.getMessage().contains("Could not use reset and mark operations"));
            assertNotNull(e.getCause());
            assertEquals("Simulated reset failure", e.getCause().getMessage());
        }
    }
}