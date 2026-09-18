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
package org.apache.commons.compress.compressors;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: CompressorStreamFactory
 *
 * Methods Covered:
 *  - CompressorStreamFactory()
 *  - CompressorStreamFactory(boolean decompressUntilEOF)
 *  - setDecompressConcatenated(boolean decompressConcatenated)
 *  - getDecompressConcatenated()
 *  - createCompressorInputStream(InputStream in) [Auto-detection]
 *  - createCompressorInputStream(String name, InputStream in) [Named InputStream]
 *  - createCompressorOutputStream(String name, OutputStream out) [Named OutputStream]
 *
 * Decision / Branch Vectors:
 *  1. Constructors & Concatenated Flag:
 *     - Default ctor -> decompressUntilEOF is null, decompressConcatenated defaults to false.
 *     - Param ctor(true) -> decompressUntilEOF = true, decompressConcatenated = true.
 *     - Param ctor(false) -> decompressUntilEOF = false, decompressConcatenated = false.
 *     - setDecompressConcatenated when decompressUntilEOF != null -> throws IllegalStateException.
 *     - setDecompressConcatenated when decompressUntilEOF == null -> mutates decompressConcatenated.
 *  2. Autodetection (createCompressorInputStream(InputStream)):
 *     - in == null -> IllegalArgumentException
 *     - in.markSupported() == false -> IllegalArgumentException
 *     - stream throws IOException during readFully / reset -> CompressorException wrapped
 *     - Matching checks:
 *       * BZip2: BZip2CompressorInputStream.matches
 *       * Gzip: GzipCompressorInputStream.matches
 *       * Pack200: Pack200CompressorInputStream.matches
 *       * FramedSnappy: FramedSnappyCompressorInputStream.matches
 *       * Z: ZCompressorInputStream.matches
 *       * XZ: XZUtils.matches && XZUtils.isXZCompressionAvailable
 *       * LZMA: LZMAUtils.matches && LZMAUtils.isLZMACompressionAvailable (known boundary / signature defect)
 *       * Unknown signature -> CompressorException ("No Compressor found for the stream signature.")
 *  3. Named Streams (createCompressorInputStream & createCompressorOutputStream):
 *     - null name or stream -> IllegalArgumentException
 *     - Case-insensitive string matching:
 *       * GZIP, BZIP2, XZ, LZMA, PACK200, SNAPPY_RAW, SNAPPY_FRAMED, Z, DEFLATE
 *     - Unsupported output algorithms (LZMA, SNAPPY_RAW, SNAPPY_FRAMED, Z) -> CompressorException
 *     - Unknown name -> CompressorException
 *     - Corrupted/Invalid stream payload on factory instantiation -> CompressorException
 */
public class CompressorStreamFactoryGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorState() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        assertFalse("Default constructor must initialize decompressConcatenated to false",
                factory.getDecompressConcatenated());

        factory.setDecompressConcatenated(true);
        assertTrue("setDecompressConcatenated should update state when default constructor used",
                factory.getDecompressConcatenated());

        factory.setDecompressConcatenated(false);
        assertFalse("setDecompressConcatenated should allow toggling back to false",
                factory.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testBooleanConstructorStateTrue() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        assertTrue("Parameterized constructor(true) must set decompressConcatenated to true",
                factory.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testBooleanConstructorStateFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory(false);
        assertFalse("Parameterized constructor(false) must set decompressConcatenated to false",
                factory.getDecompressConcatenated());
    }

    @Test(timeout = 4000)
    public void testCreateCompressorOutputStreamAllSupported() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();

        // GZIP
        ByteArrayOutputStream gzOut = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, gzOut)) {
            assertTrue(cos instanceof GzipCompressorOutputStream);
        }

        // BZIP2
        ByteArrayOutputStream bzOut = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.BZIP2, bzOut)) {
            assertTrue(cos instanceof BZip2CompressorOutputStream);
        }

        // DEFLATE
        ByteArrayOutputStream defOut = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.DEFLATE, defOut)) {
            assertTrue(cos instanceof DeflateCompressorOutputStream);
        }

        // PACK200
        ByteArrayOutputStream packOut = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.PACK200, packOut)) {
            assertTrue(cos instanceof Pack200CompressorOutputStream);
        }

        // XZ (if available in environment)
        if (XZUtils.isXZCompressionAvailable()) {
            ByteArrayOutputStream xzOut = new ByteArrayOutputStream();
            try (CompressorOutputStream cos = factory.createCompressorOutputStream(CompressorStreamFactory.XZ, xzOut)) {
                assertTrue(cos instanceof XZCompressorOutputStream);
            }
        }
    }

    @Test(timeout = 4000)
    public void testCreateCompressorInputStreamByNameAllTypes() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();

        // GZIP
        byte[] gzData = createSampleGzipData();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.GZIP, new ByteArrayInputStream(gzData))) {
            assertTrue(in instanceof GzipCompressorInputStream);
        }

        // BZIP2
        byte[] bzData = createSampleBzip2Data();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.BZIP2, new ByteArrayInputStream(bzData))) {
            assertTrue(in instanceof BZip2CompressorInputStream);
        }

        // DEFLATE
        byte[] defData = createSampleDeflateData();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.DEFLATE, new ByteArrayInputStream(defData))) {
            assertTrue(in instanceof DeflateCompressorInputStream);
        }

        // SNAPPY_RAW
        byte[] snappyRaw = new byte[]{1, 4, 't', 'e', 's', 't'};
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.SNAPPY_RAW, new ByteArrayInputStream(snappyRaw))) {
            assertTrue(in instanceof SnappyCompressorInputStream);
        }

        // SNAPPY_FRAMED
        byte[] snappyFramed = getFramedSnappyHeader();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED, new ByteArrayInputStream(snappyFramed))) {
            assertTrue(in instanceof FramedSnappyCompressorInputStream);
        }

        // PACK200
        byte[] pack200 = getPack200Header();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.PACK200, new ByteArrayInputStream(pack200))) {
            assertTrue(in instanceof Pack200CompressorInputStream);
        }

        // Z
        byte[] zData = getZHeader();
        try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.Z, new ByteArrayInputStream(zData))) {
            assertTrue(in instanceof ZCompressorInputStream);
        }

        // XZ
        if (XZUtils.isXZCompressionAvailable()) {
            byte[] xzData = createSampleXzData();
            try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.XZ, new ByteArrayInputStream(xzData))) {
                assertTrue(in instanceof XZCompressorInputStream);
            }
        }

        // LZMA
        if (LZMAUtils.isLZMACompressionAvailable()) {
            byte[] lzmaData = getLzmaHeader();
            try (CompressorInputStream in = factory.createCompressorInputStream(CompressorStreamFactory.LZMA, new ByteArrayInputStream(lzmaData))) {
                assertTrue(in instanceof LZMACompressorInputStream);
            }
        }
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveNameMatching() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] gzData = createSampleGzipData();

        try (CompressorInputStream in = factory.createCompressorInputStream("Gz", new ByteArrayInputStream(gzData))) {
            assertTrue(in instanceof GzipCompressorInputStream);
        }
        try (CompressorInputStream in = factory.createCompressorInputStream("GZIP", new ByteArrayInputStream(gzData))) {
            assertTrue(in instanceof GzipCompressorInputStream);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CompressorOutputStream cos = factory.createCompressorOutputStream("bZiP2", out)) {
            assertTrue(cos instanceof BZip2CompressorOutputStream);
        }
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorInputStreamNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorInputStreamNonMarkSupportedStream() throws Exception {
        InputStream nonMarkStream = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CompressorStreamFactory().createCompressorInputStream(nonMarkStream);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorInputStreamNamedNullName() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorInputStreamNamedNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamNullName() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamNullStream() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(timeout = 4000)
    public void testCreateCompressorInputStreamUnknownName() {
        try {
            new CompressorStreamFactory().createCompressorInputStream("unknown-format", new ByteArrayInputStream(new byte[4]));
            fail("Expected CompressorException for unknown compressor name");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("unknown-format"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateCompressorOutputStreamUnknownName() {
        try {
            new CompressorStreamFactory().createCompressorOutputStream("unknown-format", new ByteArrayOutputStream());
            fail("Expected CompressorException for unknown output compressor name");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("unknown-format"));
        }
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamUnsupportedFormats() throws Exception {
        // LZMA is not supported as an output stream type
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.LZMA, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamSnappyRawUnsupported() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.SNAPPY_RAW, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamSnappyFramedUnsupported() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.SNAPPY_FRAMED, new ByteArrayOutputStream());
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testCreateCompressorOutputStreamZUnsupported() throws Exception {
        new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.Z, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSetDecompressConcatenatedThrowsWhenConstructorUsedTrue() {
        CompressorStreamFactory factory = new CompressorStreamFactory(true);
        factory.setDecompressConcatenated(false);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSetDecompressConcatenatedThrowsWhenConstructorUsedFalse() {
        CompressorStreamFactory factory = new CompressorStreamFactory(false);
        factory.setDecompressConcatenated(true);
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testAutoDetectEmptyStreamThrowsCompressorException() throws Exception {
        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        new CompressorStreamFactory().createCompressorInputStream(empty);
    }

    @Test(expected = CompressorException.class, timeout = 4000)
    public void testAutoDetectRandomBytesThrowsCompressorException() throws Exception {
        byte[] junk = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C};
        new CompressorStreamFactory().createCompressorInputStream(new ByteArrayInputStream(junk));
    }

    @Test(timeout = 4000)
    public void testAutoDetectIOExceptionHandling() {
        InputStream throwingStream = new BufferedInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read failure");
            }
        });
        try {
            new CompressorStreamFactory().createCompressorInputStream(throwingStream);
            fail("Expected CompressorException on IOException");
        } catch (CompressorException e) {
            assertTrue("Message should convey detection failure",
                    e.getMessage().contains("Failed to detect Compressor"));
            assertNotNull(e.getCause());
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (DetectCompressor Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J DetectCompressorTestCase::testDetection failure condition.
     * Ensures auto-detection correctly routes and recognizes valid signatures for
     * each compressor format supported by createCompressorInputStream(InputStream).
     */
    @Test(timeout = 4000)
    public void testAutodetectionAllFormats() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();

        // 1. Detect BZIP2
        byte[] bz2Data = createSampleBzip2Data();
        try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(bz2Data))) {
            assertTrue("Expected BZip2CompressorInputStream", in instanceof BZip2CompressorInputStream);
        }

        // 2. Detect GZIP
        byte[] gzData = createSampleGzipData();
        try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(gzData))) {
            assertTrue("Expected GzipCompressorInputStream", in instanceof GzipCompressorInputStream);
        }

        // 3. Detect PACK200
        byte[] packData = getPack200Header();
        try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(packData))) {
            assertTrue("Expected Pack200CompressorInputStream", in instanceof Pack200CompressorInputStream);
        }

        // 4. Detect Framed Snappy
        byte[] snappyFramedData = getFramedSnappyHeader();
        try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(snappyFramedData))) {
            assertTrue("Expected FramedSnappyCompressorInputStream", in instanceof FramedSnappyCompressorInputStream);
        }

        // 5. Detect Unix Z
        byte[] zData = getZHeader();
        try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(zData))) {
            assertTrue("Expected ZCompressorInputStream", in instanceof ZCompressorInputStream);
        }

        // 6. Detect XZ
        if (XZUtils.isXZCompressionAvailable()) {
            byte[] xzData = createSampleXzData();
            try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(xzData))) {
                assertTrue("Expected XZCompressorInputStream", in instanceof XZCompressorInputStream);
            }
        }

        // 7. Detect LZMA (Defect focus: stream detection boundary for LZMA)
        if (LZMAUtils.isLZMACompressionAvailable()) {
            byte[] lzmaData = getLzmaHeader();
            try (CompressorInputStream in = factory.createCompressorInputStream(new ByteArrayInputStream(lzmaData))) {
                assertTrue("Expected LZMACompressorInputStream", in instanceof LZMACompressorInputStream);
            }
        }
    }

    @Test(timeout = 4000)
    public void testAutoDetectStreamPositionResetAfterCheck() throws Exception {
        // Verify that auto-detection resets the stream back to the beginning
        byte[] gzData = createSampleGzipData();
        ByteArrayInputStream bais = new ByteArrayInputStream(gzData);

        CompressorStreamFactory factory = new CompressorStreamFactory();
        try (CompressorInputStream cis = factory.createCompressorInputStream(bais)) {
            byte[] buf = new byte[32];
            int read = cis.read(buf);
            assertTrue("Decompressed data must be readable", read > 0);
            assertEquals("Hello world", new String(buf, 0, read, "UTF-8"));
        }
    }

    @Test(timeout = 4000)
    public void testNamedStreamCreationInvalidDataThrowsCompressorException() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] invalidData = new byte[]{0x00, 0x01, 0x02};

        try {
            factory.createCompressorInputStream(CompressorStreamFactory.GZIP, new ByteArrayInputStream(invalidData));
            fail("Expected CompressorException wrapping IOException for corrupt GZIP");
        } catch (CompressorException e) {
            assertTrue(e.getMessage().contains("Could not create CompressorInputStream"));
            assertNotNull(e.getCause());
        }
    }

    // =========================================================================
    // PARTITION D: Helper Methods for Synthesizing Stream Signatures & Payloads
    // =========================================================================

    private static byte[] createSampleGzipData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write("Hello world".getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    private static byte[] createSampleBzip2Data() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(baos)) {
            bzos.write("Hello world".getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    private static byte[] createSampleDeflateData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write("Hello world".getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    private static byte[] createSampleXzData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XZCompressorOutputStream xzos = new XZCompressorOutputStream(baos)) {
            xzos.write("Hello world".getBytes("UTF-8"));
        }
        return baos.toByteArray();
    }

    private static byte[] getPack200Header() {
        // Pack200 magic: 0xCA, 0xFE, 0xD0, 0x0D followed by minor/major versions
        return new byte[]{
                (byte) 0xCA, (byte) 0xFE, (byte) 0xD0, (byte) 0x0D,
                0x00, 0x07, 0x00, 0x96,
                0x00, 0x00, 0x00, 0x00
        };
    }

    private static byte[] getFramedSnappyHeader() {
        // Snappy framing format identifier chunk: 0xff 0x06 0x00 0x00 's' 'N' 'a' 'P' 'p' 'Y'
        return new byte[]{
                (byte) 0xff, 0x06, 0x00, 0x00,
                0x73, 0x4e, 0x61, 0x50, 0x70, 0x59,
                0x00, 0x00
        };
    }

    private static byte[] getZHeader() {
        // Unix compress (.Z) magic header: 0x1f 0x9d followed by max bits mode (e.g., 0x90)
        return new byte[]{
                0x1f, (byte) 0x9d, (byte) 0x90, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
    }

    private static byte[] getLzmaHeader() {
        // Standard .lzma format:
        // Byte 0: properties byte (typically 0x5d = 93 = 5*9 + 0*9 + 3 for pb=2, lp=0, lc=3)
        // Bytes 1-4: dictionary size (little endian, e.g., 0x00 0x00 0x10 0x00)
        // Bytes 5-12: uncompressed 64-bit size (-1 if unknown, 0xFF repeated 8 times)
        return new byte[]{
                0x5d,
                0x00, 0x00, 0x10, 0x00,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
        };
    }
}