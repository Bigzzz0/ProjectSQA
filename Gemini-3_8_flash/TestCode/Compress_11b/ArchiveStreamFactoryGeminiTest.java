/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.archivers.ArchiveStreamFactory
 *
 * 1. Method: createArchiveInputStream(archiverName, in)
 *    - archiverName == null -> IllegalArgumentException
 *    - in == null -> IllegalArgumentException
 *    - archiverName equalsIgnoreCase (AR, ZIP, TAR, JAR, CPIO, DUMP) -> Returns concrete ArchiveInputStream
 *    - Unknown archiverName -> ArchiveException
 *
 * 2. Method: createArchiveOutputStream(archiverName, out)
 *    - archiverName == null -> IllegalArgumentException
 *    - out == null -> IllegalArgumentException
 *    - archiverName equalsIgnoreCase (AR, ZIP, TAR, JAR, CPIO) -> Returns concrete ArchiveOutputStream
 *    - archiverName == DUMP or unknown -> ArchiveException (DUMP output stream not supported)
 *
 * 3. Method: createArchiveInputStream(in) [Auto-detection]
 *    - in == null -> IllegalArgumentException
 *    - !in.markSupported() -> IllegalArgumentException
 *    - ZIP signature match -> ZipArchiveInputStream
 *    - JAR signature match -> JarArchiveInputStream
 *    - AR signature match -> ArArchiveInputStream
 *    - CPIO signature match -> CpioArchiveInputStream
 *    - DUMP signature match -> DumpArchiveInputStream
 *    - TAR signature match (standard 512b ustar) -> TarArchiveInputStream
 *    - COMPRESS-117 TAR fallback: reading first block with TarArchiveInputStream
 *    - Unrecognized / EOF / IOException during mark-reset -> ArchiveException
 *
 * 4. Ground Truth Defect (shortTextFilesAreNoTARs):
 *    - Passing a non-archive short text file to createArchiveInputStream(in) must fail with ArchiveException
 *      rather than misdetecting as TAR due to non-exceptional null entry parsing in fallback.
 */
public class ArchiveStreamFactoryGeminiTest {

    private final ArchiveStreamFactory factory = new ArchiveStreamFactory();

    // =========================================================================
    // PARTITION A: Core Functional Logic & Explicit Stream Creation
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamByNameAllFormats() throws Exception {
        byte[] dummy = new byte[0];

        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.AR, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected ArArchiveInputStream", in instanceof ArArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected ZipArchiveInputStream", in instanceof ZipArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.TAR, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected TarArchiveInputStream", in instanceof TarArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.JAR, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected JarArchiveInputStream", in instanceof JarArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.CPIO, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected CpioArchiveInputStream", in instanceof CpioArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream(ArchiveStreamFactory.DUMP, new ByteArrayInputStream(dummy))) {
            assertTrue("Expected DumpArchiveInputStream", in instanceof DumpArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveInputStreamCaseInsensitive() throws Exception {
        byte[] dummy = new byte[0];
        try (ArchiveInputStream in = factory.createArchiveInputStream("zIp", new ByteArrayInputStream(dummy))) {
            assertTrue("Expected ZipArchiveInputStream", in instanceof ZipArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream("TaR", new ByteArrayInputStream(dummy))) {
            assertTrue("Expected TarArchiveInputStream", in instanceof TarArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream("CpIo", new ByteArrayInputStream(dummy))) {
            assertTrue("Expected CpioArchiveInputStream", in instanceof CpioArchiveInputStream);
        }
        try (ArchiveInputStream in = factory.createArchiveInputStream("DuMp", new ByteArrayInputStream(dummy))) {
            assertTrue("Expected DumpArchiveInputStream", in instanceof DumpArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamByNameAllSupportedFormats() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ArchiveOutputStream out = factory.createArchiveOutputStream(ArchiveStreamFactory.AR, baos)) {
            assertTrue("Expected ArArchiveOutputStream", out instanceof ArArchiveOutputStream);
        }
        try (ArchiveOutputStream out = factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, baos)) {
            assertTrue("Expected ZipArchiveOutputStream", out instanceof ZipArchiveOutputStream);
        }
        try (ArchiveOutputStream out = factory.createArchiveOutputStream(ArchiveStreamFactory.TAR, baos)) {
            assertTrue("Expected TarArchiveOutputStream", out instanceof TarArchiveOutputStream);
        }
        try (ArchiveOutputStream out = factory.createArchiveOutputStream(ArchiveStreamFactory.JAR, baos)) {
            assertTrue("Expected JarArchiveOutputStream", out instanceof JarArchiveOutputStream);
        }
        try (ArchiveOutputStream out = factory.createArchiveOutputStream(ArchiveStreamFactory.CPIO, baos)) {
            assertTrue("Expected CpioArchiveOutputStream", out instanceof CpioArchiveOutputStream);
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveOutputStreamCaseInsensitive() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ArchiveOutputStream out = factory.createArchiveOutputStream("Ar", baos)) {
            assertTrue("Expected ArArchiveOutputStream", out instanceof ArArchiveOutputStream);
        }
        try (ArchiveOutputStream out = factory.createArchiveOutputStream("JaR", baos)) {
            assertTrue("Expected JarArchiveOutputStream", out instanceof JarArchiveOutputStream);
        }
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Auto-Detection Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAutoDetectZipSignature() throws Exception {
        byte[] zipHeader = new byte[] { 0x50, 0x4b, 0x03, 0x04, 0, 0, 0, 0, 0, 0, 0, 0 };
        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(zipHeader))) {
            assertTrue("Expected ZipArchiveInputStream for ZIP magic", in instanceof ZipArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectArSignature() throws Exception {
        byte[] arHeader = new byte[] { '!', '<', 'a', 'r', 'c', 'h', '>', '\n', 0, 0, 0, 0 };
        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(arHeader))) {
            assertTrue("Expected ArArchiveInputStream for AR magic", in instanceof ArArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectCpioOldAsciiSignature() throws Exception {
        byte[] cpioHeader = new byte[] { '0', '7', '0', '7', '0', '7', 0, 0, 0, 0, 0, 0 };
        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioHeader))) {
            assertTrue("Expected CpioArchiveInputStream for CPIO magic", in instanceof CpioArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectCpioNewAsciiSignature() throws Exception {
        byte[] cpioHeader = new byte[] { '0', '7', '0', '7', '0', '1', 0, 0, 0, 0, 0, 0 };
        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioHeader))) {
            assertTrue("Expected CpioArchiveInputStream for CPIO magic", in instanceof CpioArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectCpioBinarySignature() throws Exception {
        byte[] cpioHeader = new byte[] { (byte) 0xc7, 0x71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(cpioHeader))) {
            assertTrue("Expected CpioArchiveInputStream for CPIO binary magic", in instanceof CpioArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectTarStandardSignature() throws Exception {
        byte[] tarHeader = new byte[512];
        // POSIX ustar magic at offset 257: "ustar\0"
        byte[] magic = new byte[] { 'u', 's', 't', 'a', 'r', 0 };
        System.arraycopy(magic, 0, tarHeader, 257, magic.length);

        try (ArchiveInputStream in = factory.createArchiveInputStream(new ByteArrayInputStream(tarHeader))) {
            assertTrue("Expected TarArchiveInputStream for TAR magic", in instanceof TarArchiveInputStream);
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectEmptyStreamThrowsArchiveException() {
        try {
            factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
            fail("Expected ArchiveException for empty stream");
        } catch (ArchiveException e) {
            assertTrue("Expected message to mention signature",
                    e.getMessage().contains("No Archiver found for the stream signature"));
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (COMPRESS-117 / short text files)
    // =========================================================================

    /**
     * Ground Truth Defect: shortTextFilesAreNoTARs
     * When auto-detecting format from an input stream, a short text file must not be
     * incorrectly recognized as a TAR archive just because TarArchiveInputStream fallback
     * does not raise an exception. It must throw ArchiveException.
     */
    @Test(timeout = 4000)
    public void testShortTextFilesAreNoTARs() {
        byte[] shortText = "Hello, world!\n".getBytes();
        try {
            factory.createArchiveInputStream(new ByteArrayInputStream(shortText));
            fail("created an input stream for a non-archive");
        } catch (ArchiveException ae) {
            assertTrue("Exception message should indicate no archiver found",
                    ae.getMessage().startsWith("No Archiver found"));
        }
    }

    @Test(timeout = 4000)
    public void testArbitraryBinaryNonArchiveThrowsArchiveException() {
        byte[] randomBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        try {
            factory.createArchiveInputStream(new ByteArrayInputStream(randomBytes));
            fail("Arbitrary binary non-archive data should not be detected as an archive");
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateInputStreamNullName() throws ArchiveException {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateInputStreamNullStream() throws ArchiveException {
        factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateOutputStreamNullName() throws ArchiveException {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateOutputStreamNullStream() throws ArchiveException {
        factory.createArchiveOutputStream(ArchiveStreamFactory.ZIP, null);
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateInputStreamUnknownNameThrows() throws ArchiveException {
        factory.createArchiveInputStream("unknown_format", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateOutputStreamUnknownNameThrows() throws ArchiveException {
        factory.createArchiveOutputStream("unknown_format", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCreateOutputStreamDumpThrowsArchiveException() throws ArchiveException {
        // Dump format is read-only in Commons Compress; output stream must throw ArchiveException
        factory.createArchiveOutputStream(ArchiveStreamFactory.DUMP, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAutoDetectNullStreamThrows() throws ArchiveException {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAutoDetectMarkUnsupportedThrows() throws ArchiveException {
        InputStream unmarkableStream = new FilterInputStream(new ByteArrayInputStream(new byte[16])) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        factory.createArchiveInputStream(unmarkableStream);
    }

    @Test(timeout = 4000)
    public void testAutoDetectIOExceptionOnResetThrowsArchiveException() {
        InputStream faultyStream = new BufferedInputStream(new ByteArrayInputStream(new byte[64])) {
            @Override
            public synchronized void reset() throws IOException {
                throw new IOException("Simulated reset failure");
            }
        };
        try {
            factory.createArchiveInputStream(faultyStream);
            fail("Expected ArchiveException when reset fails with IOException");
        } catch (ArchiveException ae) {
            assertTrue("Expected message indicating reset/mark issue",
                    ae.getMessage().contains("Could not use reset and mark operations"));
            assertNotNull(ae.getCause());
            assertTrue(ae.getCause() instanceof IOException);
        }
    }

    // =========================================================================
    // PARTITION E: Contract Integrity & Constants
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantValues() {
        assertEquals("ar", ArchiveStreamFactory.AR);
        assertEquals("cpio", ArchiveStreamFactory.CPIO);
        assertEquals("dump", ArchiveStreamFactory.DUMP);
        assertEquals("jar", ArchiveStreamFactory.JAR);
        assertEquals("tar", ArchiveStreamFactory.TAR);
        assertEquals("zip", ArchiveStreamFactory.ZIP);
    }

    @Test(timeout = 4000)
    public void testFactoryInstantiation() {
        ArchiveStreamFactory f = new ArchiveStreamFactory();
        assertNotNull(f);
    }
}