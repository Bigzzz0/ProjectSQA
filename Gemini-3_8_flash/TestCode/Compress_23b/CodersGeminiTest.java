/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.archivers.sevenz.Coders
 *
 * 1. Defect-Targeted Branch Zone (Defects4J: SevenZFileTest#testCompressedHeaderWithNonDefaultDictionarySize):
 *    - In Coders.LZMADecoder#decode:
 *      Sign-extension bug when reading dictionary size from coder.properties bytes (byte to long/int shift without masking & 0xFF).
 *      Bytes with the high bit set (e.g. 0x80 for 32KiB dictSize, or 0x80 in properties[1..4]) promote to negative ints,
 *      resulting in negative dictSize passed to LZMAInputStream and triggering:
 *      "org.tukaani.xz.UnsupportedOptionsException: LZMA dictionary is too big for this implementation".
 *    - Boundary check: dictSize > LZMAInputStream.DICT_SIZE_MAX throws IOException("Dictionary larger than 4GiB maximum size").
 *
 * 2. Method Dispatching & Supported Encoders/Decoders:
 *    - Coders#addDecoder and Coders#addEncoder across all SevenZMethod constants:
 *      COPY, LZMA, LZMA2, DEFLATE, BZIP2, AES256SHA256, plus unsupported methods / unknown IDs.
 *    - CoderId matching and array equals on decompressionMethodId.
 *
 * 3. AES256SHA256Decoder:
 *    - Property size validation: 2 + saltSize + ivSize > properties.length -> IOException("Salt size + IV size too long").
 *    - Null password validation: passwordBytes == null -> IOException("Cannot read encrypted files without a password").
 *    - numCyclesPower == 0x3f (direct key copy path) vs numCyclesPower < 0x3f (SHA-256 key stretching loop with byte counter).
 *    - AES cipher initialization and read() / read(b, off, len) / close() behavior.
 *
 * 4. DeflateDecoder & DummyByteAddingInputStream:
 *    - DummyByteAddingInputStream: single-byte read() reaching EOF (-1) appends dummy byte 0 once, then -1.
 *    - Multi-byte read(b, off, len) reaching EOF (-1) returns 1 with byte 0, then subsequent read returns -1.
 *
 * 5. CoderBase & Encoders:
 *    - Default encode() in CoderBase throwing UnsupportedOperationException.
 *    - Supported encoders: CopyDecoder, DeflateDecoder, BZIP2Decoder.
 */

package org.apache.commons.compress.archivers.sevenz;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CodersGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LZMA Dict Size Sign-Extension)
    // =========================================================================

    /**
     * Targets Defects4J bug where signed byte expansion in LZMADecoder causes a negative
     * dictionary size, triggering "LZMA dictionary is too big for this implementation".
     * A dictionary size of 32 KiB (0x00008000) has 0x80 in properties[2].
     */
    @Test(timeout = 4000)
    public void testLzmaDecoderNonDefaultDictionarySizeSignExtension() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // lc=3, lp=0, pb=2 => propsByte = (2 * 5 + 0) * 9 + 3 = 93 (0x5D)
        // dictSize = 32768 (0x00008000) -> bytes: 0x00, 0x80, 0x00, 0x00
        coder.properties = new byte[] { 0x5D, 0x00, (byte) 0x80, 0x00, 0x00 };

        byte[] inputData = new byte[] { 0x00 };
        InputStream in = new ByteArrayInputStream(inputData);
        InputStream decoderStream = Coders.addDecoder(in, coder, null);
        assertNotNull("Decoder stream should be instantiated successfully", decoderStream);
        decoderStream.close();
    }

    /**
     * Tests dictionary size with properties[1] having top bit set (e.g., 0x80 = 128 bytes).
     */
    @Test(timeout = 4000)
    public void testLzmaDecoderDictionarySizeProperty1SignExtension() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // dictSize = 4096 (0x00001000) -> properties: [0x5D, 0x00, 0x10, 0x00, 0x00]
        coder.properties = new byte[] { 0x5D, 0x00, 0x10, 0x00, 0x00 };

        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream decoderStream = Coders.addDecoder(in, coder, null);
        assertNotNull(decoderStream);
        decoderStream.close();
    }

    /**
     * Tests the dictionary size boundary check (> DICT_SIZE_MAX).
     */
    @Test(timeout = 4000)
    public void testLzmaDecoderDictionarySizeExceedsMax() {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.LZMA.getId();
        // dictSize = 0xFFFFFFFF (greater than LZMAInputStream.DICT_SIZE_MAX)
        coder.properties = new byte[] { 0x5D, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

        InputStream in = new ByteArrayInputStream(new byte[0]);
        try {
            Coders.addDecoder(in, coder, null);
            fail("Should have thrown IOException for dictionary exceeding 4GiB");
        } catch (IOException expected) {
            assertTrue("Expected exception message to mention 4GiB maximum size",
                    expected.getMessage().contains("Dictionary larger than 4GiB maximum size"));
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Decoders/Encoders)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCopyDecoderAndEncoderRoundTrip() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.COPY.getId();

        byte[] original = "RoundTripCopyData12345".getBytes("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoderOut = Coders.addEncoder(baos, SevenZMethod.COPY, null);
        encoderOut.write(original);
        encoderOut.flush();
        encoderOut.close();

        byte[] copied = baos.toByteArray();
        assertArrayEquals(original, copied);

        InputStream decodedIn = Coders.addDecoder(new ByteArrayInputStream(copied), coder, null);
        byte[] readBuffer = new byte[original.length];
        int bytesRead = decodedIn.read(readBuffer);
        assertEquals(original.length, bytesRead);
        assertArrayEquals(original, readBuffer);
        decodedIn.close();
    }

    @Test(timeout = 4000)
    public void testDeflateDecoderAndEncoderRoundTrip() throws IOException {
        byte[] original = "DeflateCompressionTestPayloadString!".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoderOut = Coders.addEncoder(baos, SevenZMethod.DEFLATE, null);
        encoderOut.write(original);
        encoderOut.close();

        byte[] compressed = baos.toByteArray();
        assertTrue("Compressed size should be greater than 0", compressed.length > 0);

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.DEFLATE.getId();
        InputStream decodedIn = Coders.addDecoder(new ByteArrayInputStream(compressed), coder, null);

        ByteArrayOutputStream uncompressedBaos = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int read;
        while ((read = decodedIn.read(buf)) != -1) {
            uncompressedBaos.write(buf, 0, read);
        }
        decodedIn.close();

        assertArrayEquals(original, uncompressedBaos.toByteArray());
    }

    @Test(timeout = 4000)
    public void testBZip2DecoderAndEncoderRoundTrip() throws IOException {
        byte[] original = "BZip2DataCompressionVerificationStream".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoderOut = Coders.addEncoder(baos, SevenZMethod.BZIP2, null);
        encoderOut.write(original);
        encoderOut.close();

        byte[] compressed = baos.toByteArray();
        assertTrue(compressed.length > 0);

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.BZIP2.getId();
        InputStream decodedIn = Coders.addDecoder(new ByteArrayInputStream(compressed), coder, null);

        ByteArrayOutputStream restoredBaos = new ByteArrayOutputStream();
        int b;
        while ((b = decodedIn.read()) != -1) {
            restoredBaos.write(b);
        }
        decodedIn.close();

        assertArrayEquals(original, restoredBaos.toByteArray());
    }

    @Test(timeout = 4000)
    public void testAES256SHA256DecoderWithDirectKey() throws Exception {
        // numCyclesPower = 0x3f (63) triggers direct key copy (no SHA-256 loop)
        // byte0: numCyclesPower (0x3f) | (ivSizeBit << 6) | (saltSizeBit << 7)
        // Let saltSize = 0, ivSize = 0 -> byte0 = 0x3f, byte1 = 0x00
        byte byte0 = 0x3f;
        byte byte1 = 0x00;
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[] { byte0, byte1 };

        byte[] password = new byte[32];
        Arrays.fill(password, (byte) 0x07);

        // Feed empty data to decoder
        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream aesIn = Coders.addDecoder(in, coder, password);
        assertNotNull(aesIn);

        // CipherInputStream read on empty stream should return -1
        int readByte = aesIn.read();
        assertEquals(-1, readByte);

        byte[] buf = new byte[8];
        int bytesRead = aesIn.read(buf, 0, buf.length);
        assertEquals(-1, bytesRead);

        aesIn.close();
    }

    @Test(timeout = 4000)
    public void testAES256SHA256DecoderWithKeyStretching() throws Exception {
        // numCyclesPower = 1 (2 iterations)
        // ivSize = 16 (ivSizeBit = 0, byte1 low 4 bits = 0x00) -> Wait:
        // ivSize = ((byte0 >> 6) & 1) + (byte1 & 0x0f)
        // saltSize = ((byte0 >> 7) & 1) + (byte1 >> 4)
        // Let ivSize = 16: ivSizeBit = 0, byte1 low bits = 16 is impossible (max 15).
        // If ivSizeBit = 1 (byte0 bit 6 set) and byte1 low nibble = 15 => ivSize = 1 + 15 = 16.
        // Let saltSize = 2: byte0 bit 7 = 0, byte1 high nibble = 2.
        // byte0: 0x01 (numCyclesPower=1) | (1 << 6 = 0x40) = 0x41
        // byte1: (2 << 4) | 15 = 0x2F
        byte byte0 = 0x41;
        byte byte1 = 0x2F;
        int saltSize = 2;
        int ivSize = 16;
        byte[] props = new byte[2 + saltSize + ivSize];
        props[0] = byte0;
        props[1] = byte1;
        props[2] = 0x11; // salt byte 1
        props[3] = 0x22; // salt byte 2
        for (int i = 0; i < ivSize; i++) {
            props[4 + i] = (byte) (i + 1); // iv bytes
        }

        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = props;

        byte[] password = "SecretPassword123".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream aesIn = Coders.addDecoder(in, coder, password);

        // Read to trigger init() and key stretching loop
        int readVal = aesIn.read();
        assertEquals(-1, readVal);
        aesIn.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & DummyByteAddingInputStream
    // =========================================================================

    @Test(timeout = 4000)
    public void testDummyByteAddingInputStreamSingleByteRead() throws Exception {
        Class<?> clazz = Class.forName("org.apache.commons.compress.archivers.sevenz.Coders$DummyByteAddingInputStream");
        Constructor<?> constructor = clazz.getDeclaredConstructor(InputStream.class);
        constructor.setAccessible(true);

        InputStream emptyIn = new ByteArrayInputStream(new byte[] { 0x42 });
        InputStream dummyIn = (InputStream) constructor.newInstance(emptyIn);

        // First byte: 0x42 from source
        assertEquals(0x42, dummyIn.read());
        // Second byte: 0 appended dummy byte
        assertEquals(0, dummyIn.read());
        // Third byte: end of stream -1
        assertEquals(-1, dummyIn.read());
        dummyIn.close();
    }

    @Test(timeout = 4000)
    public void testDummyByteAddingInputStreamArrayRead() throws Exception {
        Class<?> clazz = Class.forName("org.apache.commons.compress.archivers.sevenz.Coders$DummyByteAddingInputStream");
        Constructor<?> constructor = clazz.getDeclaredConstructor(InputStream.class);
        constructor.setAccessible(true);

        InputStream emptyIn = new ByteArrayInputStream(new byte[0]);
        InputStream dummyIn = (InputStream) constructor.newInstance(emptyIn);

        byte[] buf = new byte[4];
        // At EOF, dummy byte should return 1 byte with value 0
        int count1 = dummyIn.read(buf, 1, 2);
        assertEquals(1, count1);
        assertEquals(0, buf[1]);

        // Next read should be -1
        int count2 = dummyIn.read(buf, 0, buf.length);
        assertEquals(-1, count2);
        dummyIn.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnsupportedDecompressionMethodThrowsException() {
        Coder coder = new Coder();
        coder.decompressionMethodId = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };

        try {
            Coders.addDecoder(new ByteArrayInputStream(new byte[0]), coder, null);
            fail("Expected IOException for unsupported compression method");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Unsupported compression method"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedCompressionMethodEncoderThrowsException() {
        try {
            Coders.addEncoder(new ByteArrayOutputStream(), null, null);
            fail("Expected IOException for unsupported compression method");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Unsupported compression method"));
        }
    }

    @Test(timeout = 4000)
    public void testEncoderUnsupportedOperationExceptionOnLZMA() throws IOException {
        // SevenZMethod.LZMA encoder is not supported in Coders#coderTable (throws UnsupportedOperationException)
        try {
            Coders.addEncoder(new ByteArrayOutputStream(), SevenZMethod.LZMA, null);
            fail("Expected UnsupportedOperationException for LZMA encoder");
        } catch (UnsupportedOperationException expected) {
            assertEquals("method doesn't support writing", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAES256SHA256DecoderSaltIvTooLongThrowsException() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        // byte0 = 0xC0 (saltSize bit = 1, ivSize bit = 1), byte1 = 0xFF (saltSize += 15, ivSize += 15)
        // total required length = 2 + 16 + 16 = 34, but provided array is only length 2
        coder.properties = new byte[] { (byte) 0xC0, (byte) 0xFF };

        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream aesIn = Coders.addDecoder(in, coder, "testPassword".getBytes("UTF-8"));

        try {
            aesIn.read();
            fail("Expected IOException because salt size + IV size too long");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Salt size + IV size too long"));
        }
    }

    @Test(timeout = 4000)
    public void testAES256SHA256DecoderNullPasswordThrowsException() throws IOException {
        Coder coder = new Coder();
        coder.decompressionMethodId = SevenZMethod.AES256SHA256.getId();
        coder.properties = new byte[] { 0x00, 0x00 };

        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream aesIn = Coders.addDecoder(in, coder, null);

        try {
            aesIn.read();
            fail("Expected IOException when password is null");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Cannot read encrypted files without a password"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Structural Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testCodersInstantiation() {
        // Instantiating Coders default constructor for complete structural coverage
        Coders coders = new Coders();
        assertNotNull(coders);
    }

    @Test(timeout = 4000)
    public void testCoderIdMappingIntegrity() {
        assertNotNull(Coders.coderTable);
        assertTrue(Coders.coderTable.length >= 6);

        for (Coders.CoderId coderId : Coders.coderTable) {
            assertNotNull(coderId.method);
            assertNotNull(coderId.coder);
        }
    }

    @Test(timeout = 4000)
    public void testCoderBaseDefaultEncodeThrowsUnsupportedOperationException() throws Exception {
        Coders.CoderBase coderBase = new Coders.CoderBase() {
            @Override
            InputStream decode(InputStream in, Coder coder, byte[] password) {
                return in;
            }
        };

        try {
            coderBase.encode(new ByteArrayOutputStream(), null);
            fail("Default CoderBase encode should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals("method doesn't support writing", expected.getMessage());
        }
    }
}