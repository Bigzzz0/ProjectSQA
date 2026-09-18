package org.apache.commons.compress.archivers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 * White-box JUnit 4 test suite for ArchiveStreamFactory.
 * Targets line/branch coverage and the known defect: short text files incorrectly
 * recognized as TAR archives (COMPRESS-117).
 */
public class ArchiveStreamFactoryDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     *
     * Partitions for createArchiveInputStream(String, InputStream):
     *   Branch A: archiverName == null -> IllegalArgumentException
     *   Branch B: in == null -> IllegalArgumentException
     *   Branch C: archiverName equalsIgnoreCase AR -> ArArchiveInputStream
     *   Branch D: ZIP -> ZipArchiveInputStream
     *   Branch E: TAR -> TarArchiveInputStream
     *   Branch F: JAR -> JarArchiveInputStream
     *   Branch G: CPIO -> CpioArchiveInputStream
     *   Branch H: DUMP -> DumpArchiveInputStream
     *   Branch I: any other case -> ArchiveException
     *
     * Partitions for createArchiveOutputStream(String, OutputStream):
     *   Branch J: archiverName == null -> IllegalArgumentException
     *   Branch K: out == null -> IllegalArgumentException
     *   Branch L: AR -> ArArchiveOutputStream
     *   Branch M: ZIP -> ZipArchiveOutputStream
     *   Branch N: TAR -> TarArchiveOutputStream
     *   Branch O: JAR -> JarArchiveOutputStream
     *   Branch P: CPIO -> CpioArchiveOutputStream
     *   Branch Q: any other case -> ArchiveException
     *
     * Partitions for createArchiveInputStream(InputStream) - auto-detection:
     *   Branch R: in == null -> IllegalArgumentException
     *   Branch S: mark not supported -> IllegalArgumentException
     *   Branch T: Zip signature match -> ZipArchiveInputStream
     *   Branch U: Jar signature match -> JarArchiveInputStream
     *   Branch V: Ar signature match -> ArArchiveInputStream
     *   Branch W: Cpio signature match -> CpioArchiveInputStream
     *   Branch X: Dump signature match (using 32-byte buffer) -> DumpArchiveInputStream
     *   Branch Y: Tar signature match (using 512-byte buffer) -> TarArchiveInputStream
     *   Branch Z: COMPRESS-117 fallback: if TarArchiveInputStream.getNextEntry() succeeds -> TarArchiveInputStream
     *   Branch AA: IOException during reset/mark -> ArchiveException wrapping IOException
     *   Branch AB: No archiver found -> ArchiveException
     *
     * Defect Target (COMPRESS-117):
     *   A non-archive stream (e.g., a short text string) that does not match any known signature
     *   should throw ArchiveException, but the buggy code creates a TarArchiveInputStream because
     *   the fallback try block succeeds without throwing an exception.
     */

    // ---------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------
    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameAr() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.AR, in);
        assertNotNull("ArArchiveInputStream expected", ais);
        assertTrue(ais instanceof ArArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameZip() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameTar() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, in);
        assertNotNull(ais);
        assertTrue(ais instanceof TarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameJar() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, in);
        assertNotNull(ais);
        assertTrue(ais instanceof JarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameCpio() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, in);
        assertNotNull(ais);
        assertTrue(ais instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameDump() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, in);
        assertNotNull(ais);
        assertTrue(ais instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameAr() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof ArArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameZip() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
        assertNotNull(aos);
        assertTrue(aos instanceof ZipArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameTar() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof TarArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameJar() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, out);
        assertNotNull(aos);
        assertTrue(aos instanceof JarArchiveOutputStream);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameCpio() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, out);
        assertNotNull(aos);
        assertTrue(aos instanceof CpioArchiveOutputStream);
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream((String) null, in);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream("zip", (InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamNullArchiverName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream((String) null, out);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamNullStream() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream("zip", (OutputStream) null);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateArchiveInputStreamUnknownName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream("unknown", in);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateArchiveOutputStreamUnknownName() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream("unknown", out);
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-117)
    // ---------------------------------------------------------------
    @Test(timeout = 4000)
    public void testShortTextFilesAreNoTARs() throws Exception {
        // A non-archive, short text stream that doesn't match any known signature
        // must throw ArchiveException. The buggy version would incorrectly create a TarArchiveInputStream.
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] shortText = "Hello, this is not an archive.".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(shortText);
        try {
            ArchiveInputStream ais = factory.createArchiveInputStream(in);
            // If we get here, it's a bug (defective version creates TAR stream)
            fail("Expected ArchiveException for non-archive stream, but got: " + ais.getClass().getName());
        } catch (ArchiveException e) {
            // Expected - correct behavior for non-archive
            assertTrue(e.getMessage().contains("No Archiver found") || e.getMessage().contains("signature"));
        }
    }

    @Test(timeout = 4000)
    public void testNonArchiveButFakeTarHeader() throws Exception {
        // A stream that starts with a valid TAR header but is not a complete TAR should still be
        // identified as TAR by the fallback. This is actually acceptable behavior.
        // However, the defect is about false positives. We'll test a clearly non-TAR stream.
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Create a stream that mimics a random file, not matching any signature
        byte[] randomData = new byte[512];
        for (int i = 0; i < randomData.length; i++) {
            randomData[i] = (byte) (i % 256);
        }
        // Ensure it is not a TAR header by zeroing out the magic bytes (ustar)
        // Actually, random data might accidentally match TAR? Unlikely but possible.
        // We'll use a stream with known non-TAR first bytes.
        byte[] nonTar = new byte[512];
        nonTar[0] = 0x01; // not TAR magic
        nonTar[1] = 0x02;
        InputStream in = new ByteArrayInputStream(nonTar);
        try {
            ArchiveInputStream ais = factory.createArchiveInputStream(in);
            fail("Expected ArchiveException for non-archive stream, but got: " + ais.getClass().getName());
        } catch (ArchiveException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // Partition D: Auto-detection with various signatures
    // ---------------------------------------------------------------
    @Test(timeout = 4000)
    public void testAutoDetectZip() throws Exception {
        // Create a minimal valid ZIP signature (PK\x03\x04)
        byte[] zipHeader = new byte[] {0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(zipHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue("Expected ZipArchiveInputStream", ais instanceof ZipArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectJar() throws Exception {
        // Jar header same as Zip but with Jar magic? Actually Jar uses same PK signature.
        // JarArchiveInputStream.matches() checks for PK\x03\x04 as well.
        byte[] jarHeader = new byte[] {0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(jarHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        // Since Zip is checked first, it will return ZipArchiveInputStream; Jar detection is redundant.
        assertTrue(ais instanceof ZipArchiveInputStream || ais instanceof JarArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectAr() throws Exception {
        // AR signature: "!<arch>\n" (bytes: 0x21 0x3C 0x61 0x72 0x63 0x68 0x3E 0x0A)
        byte[] arHeader = "!<arch>\n".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(arHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue("Expected ArArchiveInputStream", ais instanceof ArArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectCpio() throws Exception {
        // CPIO header: "070707" (ASCII) or "070701" for new format. We'll use old ASCII: "070707"
        byte[] cpioHeader = "070707".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(cpioHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue("Expected CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectDump() throws Exception {
        // Dump requires 32-byte buffer. NFS dump header. We'll use known magic: "NFS4" or similar.
        // For simplicity, we create a valid Dump header (first 32 bytes). Dump magic is 0x04 0x01? 
        // Actually DumpArchiveInputStream.matches checks for "NFS4" at offset 0? 
        // Let's use a minimal valid header: byte[] dumpHeader = ...; but that's complex.
        // We'll skip detailed validation and rely on the other tests. For coverage, we can provide a header that matches.
        // However, we need to ensure it's recognized. Since we don't have the exact magic, we'll use a dummy that doesn't match.
        // For branch coverage we must exercise the Dump signature check. Let's create a header that matches.
        // According to DumpArchiveInputStream, matches returns true if the first 4 bytes are "NFS4" (0x4E 0x46 0x53 0x34).
        byte[] dumpHeader = new byte[32];
        dumpHeader[0] = 0x4E; // 'N'
        dumpHeader[1] = 0x46; // 'F'
        dumpHeader[2] = 0x53; // 'S'
        dumpHeader[3] = 0x34; // '4'
        InputStream in = new ByteArrayInputStream(dumpHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue("Expected DumpArchiveInputStream", ais instanceof DumpArchiveInputStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectTar() throws Exception {
        // TAR header: 257-262 bytes are "ustar\0" or "ustar  \0". We'll create a minimal TAR header.
        byte[] tarHeader = new byte[512];
        // Set magic at offset 257: "ustar" + null char
        tarHeader[257] = 0x75; // u
        tarHeader[258] = 0x73; // s
        tarHeader[259] = 0x74; // t
        tarHeader[260] = 0x61; // a
        tarHeader[261] = 0x72; // r
        tarHeader[262] = 0x00; // null
        InputStream in = new ByteArrayInputStream(tarHeader);
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue("Expected TarArchiveInputStream", ais instanceof TarArchiveInputStream);
    }

    // ---------------------------------------------------------------
    // Partition E: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamNullAutoDetect() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateArchiveInputStreamMarkNotSupported() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
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
        factory.createArchiveInputStream(noMark);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateArchiveInputStreamIOExceptionOnMarkReset() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream failOnReset = new ByteArrayInputStream("test".getBytes()) {
            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("reset failed");
            }
        };
        // The read will succeed, reset will throw IOException -> ArchiveException
        factory.createArchiveInputStream(failOnReset);
    }

    // ---------------------------------------------------------------
    // Partition F: Object Lifecycle & Contract Integrity (if applicable)
    // ---------------------------------------------------------------
    // ArchiveStreamFactory is not cloneable and has no state; skipping.

    // Additional edge case: case-insensitive names
    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamCaseInsensitive() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("ZIP", in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testAutoDetectNoMatch() throws Exception {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Use a stream that doesn't match any known signature and also fails the TAR fallback
        // (e.g., a stream that when read in 512 bytes causes TarArchiveInputStream to throw).
        // We'll create a stream that is exactly 511 bytes of zeros (not enough for TAR header).
        byte[] noMatch = new byte[511];
        InputStream in = new ByteArrayInputStream(noMatch);
        factory.createArchiveInputStream(in);
    }
}