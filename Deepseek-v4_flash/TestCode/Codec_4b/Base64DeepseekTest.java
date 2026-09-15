package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

public class Base64DeepseekTest {

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Test(timeout = 4000)
    public void testDefaultEncoderDoesNotWriteChunkSeparator() {
        Base64 codec = new Base64();
        assertArrayEquals(utf8("AQID"), codec.encode(new byte[]{1, 2, 3}));
        assertEquals("AQID", codec.encodeToString(new byte[]{1, 2, 3}));
        assertEquals("Zm8=", new Base64().encodeToString(utf8("fo")));
    }

    @Test(timeout = 4000)
    public void testBinaryEncoderDefaultContract() {
        Base64 codec = new Base64();
        Object encoded = codec.encode((Object) new byte[]{1, 2, 3});
        assertTrue(encoded instanceof byte[]);
        assertArrayEquals(utf8("AQID"), (byte[]) encoded);
    }

    @Test(timeout = 4000)
    public void testEncodeInstanceExact() {
        Base64 codec = new Base64(0);
        assertArrayEquals(new byte[0], codec.encode(new byte[0]));
        assertArrayEquals(utf8("Zg=="), codec.encode(utf8("f")));
        assertArrayEquals(utf8("Zm8="), codec.encode(utf8("fo")));
        assertArrayEquals(utf8("Zm9v"), codec.encode(utf8("foo")));
        assertArrayEquals(utf8("Zm9vYg=="), codec.encode(utf8("foob")));
        assertArrayEquals(utf8("Zm9vYmE="), codec.encode(utf8("fooba")));
        assertArrayEquals(utf8("Zm9vYmFy"), codec.encode(utf8("foobar")));
    }

    @Test(timeout = 4000)
    public void testDecodeInstanceExact() {
        Base64 codec = new Base64(0);
        assertArrayEquals(utf8("f"), codec.decode("Zg=="));
        assertArrayEquals(utf8("fo"), codec.decode("Zm8="));
        assertArrayEquals(utf8("foo"), codec.decode("Zm9v"));
        assertArrayEquals(utf8("foobar"), codec.decode("Zm9vYmFy"));
        assertArrayEquals(utf8("f"), codec.decode("Zg"));
        assertEquals(0, codec.decode("").length);
        assertArrayEquals(new byte[0], codec.decode(new byte[]{0}));
        assertArrayEquals(new byte[0], codec.decode(new byte[]{(byte) 0x80}));
    }

    @Test(timeout = 4000)
    public void testDecodeIgnoresWhitespaceAndLineBreaks() {
        Base64 codec = new Base64();
        assertArrayEquals(utf8("foo"), codec.decode("Zm9v\r\n"));
        assertArrayEquals(utf8("foo"), codec.decode("Zm 9v"));
        assertArrayEquals(utf8("foo"), codec.decode("Zm9v\t"));
    }

    @Test(timeout = 4000)
    public void testStaticEncodeNoChunk() {
        assertArrayEquals(utf8("AQI="), Base64.encodeBase64(new byte[]{1, 2}));
        assertArrayEquals(utf8("AQI="), Base64.encodeBase64(new byte[]{1, 2}, false));
        assertArrayEquals(utf8("AQI="), Base64.encodeBase64(new byte[]{1, 2}, false, false));
        assertArrayEquals(utf8("AQI="), Base64.encodeBase64(new byte[]{1, 2}, false, false, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testStaticUrlSafeEncode() {
        assertArrayEquals(utf8("AQI"), Base64.encodeBase64URLSafe(new byte[]{1, 2}));
        assertEquals("AQI", Base64.encodeBase64URLSafeString(new byte[]{1, 2}));
        assertArrayEquals(utf8("AQI"), Base64.encodeBase64(new byte[]{1, 2}, false, true));
        assertArrayEquals(utf8("AQI"), Base64.encodeBase64(new byte[]{1, 2}, false, true, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testStaticChunkedEncode() {
        byte[] expected = utf8("AQI=\r\n");
        assertArrayEquals(expected, Base64.encodeBase64Chunked(new byte[]{1, 2}));
        assertArrayEquals(expected, Base64.encodeBase64(new byte[]{1, 2}, true));
        assertArrayEquals(expected, Base64.encodeBase64(new byte[]{1, 2}, true, false));
        assertArrayEquals(expected, Base64.encodeBase64(new byte[]{1, 2}, true, false, 6));
        assertEquals("AQI=\r\n", Base64.encodeBase64String(new byte[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testExplicitChunkingDefaultSeparator() {
        Base64 codec = new Base64(76);
        assertArrayEquals(utf8("Zm9v\r\n"), codec.encode(utf8("foo")));
    }

    @Test(timeout = 4000)
    public void testExplicitChunkingCustomSeparator() {
        Base64 codec = new Base64(4, new byte[]{'\n'});
        assertArrayEquals(utf8("Zm9v\n"), codec.encode(utf8("foo")));
    }

    @Test(timeout = 4000)
    public void testNullLineSeparatorDisablesChunking() {
        Base64 codec = new Base64(76, null);
        assertArrayEquals(utf8("Zm9v"), codec.encode(utf8("foo")));
    }

    @Test(timeout = 4000)
    public void testNegativeLineLengthNoChunk() {
        assertArrayEquals(utf8("Zg=="), new Base64(-1).encode(utf8("f")));
    }

    @Test(timeout = 4000)
    public void testMultipleEncodeCallsResetState() {
        Base64 codec = new Base64(0);
        assertArrayEquals(utf8("Zg=="), codec.encode(utf8("f")));
        assertArrayEquals(utf8("Zg=="), codec.encode(utf8("f")));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeBoundaryBytes() {
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) {
            all[i] = (byte) i;
        }

        Base64 standard = new Base64(0);
        byte[] encoded = standard.encode(all);
        assertArrayEquals(all, standard.decode(encoded));

        Base64 urlSafe = new Base64(0, CHUNK_SEPARATOR, true);
        byte[] urlEncoded = urlSafe.encode(all);
        assertArrayEquals(all, urlSafe.decode(urlEncoded));
    }

    @Test(timeout = 4000)
    public void testEofMakesEncoderAndDecoderInert() {
        Base64 encoder = new Base64(0);
        encoder.encode(new byte[]{1}, 0, 1);
        encoder.encode(new byte[]{1}, 0, -1);
        encoder.encode(new byte[]{9}, 0, 1);
        byte[] out = new byte[4];
        int n = encoder.readResults(out, 0, out.length);
        assertEquals(4, n);
        assertArrayEquals(utf8("AQ=="), out);

        Base64 decoder = new Base64(0);
        decoder.decode(utf8("Zg"), 0, 2);
        decoder.decode(utf8("Zg"), 0, -1);
        decoder.decode(utf8("Zm8="), 0, 4);
        byte[] dec = new byte[4];
        int m = decoder.readResults(dec, 0, dec.length);
        assertEquals(1, m);
        assertEquals((byte) 'f', dec[0]);
    }

    @Test(timeout = 4000)
    public void testStreamingStateAndReadResults() {
        Base64 codec = new Base64(0);
        assertFalse(codec.hasData());
        assertEquals(0, codec.avail());
        assertEquals(0, codec.readResults(new byte[1], 0, 1));

        codec.encode(new byte[]{1}, 0, 1);
        assertTrue(codec.hasData());

        codec.encode(new byte[]{1}, 0, -1);
        assertTrue(codec.hasData());
        assertEquals(4, codec.avail());

        byte[] out = new byte[4];
        assertEquals(4, codec.readResults(out, 0, out.length));
        assertArrayEquals(utf8("AQ=="), out);
        assertFalse(codec.hasData());
        assertEquals(0, codec.avail());
        assertEquals(-1, codec.readResults(new byte[1], 0, 1));
    }

    @Test(timeout = 4000)
    public void testSetInitialBufferWritesDirectly() {
        byte[] out = new byte[8];
        Base64 codec = new Base64(0);
        codec.setInitialBuffer(out, 0, out.length);
        assertTrue(codec.hasData());
        assertEquals(0, codec.avail());

        codec.encode(new byte[]{1}, 0, 1);
        codec.encode(new byte[]{1}, 0, -1);
        assertEquals(4, codec.avail());
        assertEquals((byte) 'A', out[0]);
        assertEquals((byte) 'Q', out[1]);
        assertEquals((byte) '=', out[2]);
        assertEquals((byte) '=', out[3]);

        assertEquals(4, codec.readResults(out, 0, out.length));
        assertFalse(codec.hasData());
    }

    @Test(timeout = 4000)
    public void testIsUrlSafe() {
        assertFalse(new Base64().isUrlSafe());
        assertFalse(new Base64(0).isUrlSafe());
        assertTrue(new Base64(true).isUrlSafe());
        assertTrue(new Base64(0, CHUNK_SEPARATOR, true).isUrlSafe());
        assertFalse(new Base64(0, CHUNK_SEPARATOR, false).isUrlSafe());
    }

    @Test(timeout = 4000)
    public void testConstructorRejectsBase64ByteInLineSeparator() {
        try {
            new Base64(76, new byte[]{'A'});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("base64"));
        }
    }

    @Test(timeout = 4000)
    public void testDecodeObjectAcceptsStringAndByteArray() throws DecoderException {
        Base64 codec = new Base64(0);
        assertArrayEquals(utf8("foo"), (byte[]) codec.decode((Object) "Zm9v"));
        assertArrayEquals(utf8("foo"), (byte[]) codec.decode((Object) utf8("Zm9v")));
    }

    @Test(timeout = 4000)
    public void testEncodeObjectAcceptsByteArrayOnly() throws EncoderException {
        Base64 codec = new Base64(0);
        assertArrayEquals(utf8("Zm9v"), (byte[]) codec.encode((Object) utf8("foo")));
    }

    @Test(timeout = 4000)
    public void testDecodeObjectRejectsInvalidType() {
        Base64 codec = new Base64(0);
        try {
            codec.decode((Object) Integer.valueOf(1));
            fail("Expected DecoderException");
        } catch (DecoderException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testEncodeObjectRejectsInvalidType() {
        Base64 codec = new Base64(0);
        try {
            codec.encode((Object) "foo");
            fail("Expected EncoderException");
        } catch (EncoderException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testEncodeObjectRejectsNull() {
        Base64 codec = new Base64(0);
        try {
            codec.encode((Object) null);
            fail("Expected EncoderException");
        } catch (EncoderException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testDecodeObjectRejectsNull() {
        Base64 codec = new Base64(0);
        try {
            codec.decode((Object) null);
            fail("Expected DecoderException");
        } catch (DecoderException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '!'));
        assertFalse(Base64.isBase64((byte) 0));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertTrue(Base64.isArrayByteBase64(utf8("AQID")));
        assertTrue(Base64.isArrayByteBase64(utf8("AQID\r\n")));
        assertFalse(Base64.isArrayByteBase64(utf8("AQID!")));
    }

    @Test(timeout = 4000)
    public void testDiscardWhitespace() {
        byte[] input = new byte[]{' ', 'A', '\n', 'B', '\r', '\t', 'C'};
        assertArrayEquals(utf8("ABC"), Base64.discardWhitespace(input));
        assertArrayEquals(utf8("ABC"), Base64.discardWhitespace(utf8("ABC")));
        assertArrayEquals(new byte[0], Base64.discardWhitespace(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeInteger() {
        BigInteger value = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(value);
        assertEquals(value, Base64.decodeInteger(encoded));
    }

    @Test(timeout = 4000)
    public void testEncodeIntegerNullThrows() {
        try {
            Base64.encodeInteger(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testToIntegerBytesVariants() {
        assertArrayEquals(new byte[]{1}, Base64.toIntegerBytes(BigInteger.ONE));
        assertArrayEquals(new byte[]{127}, Base64.toIntegerBytes(BigInteger.valueOf(127)));
        assertArrayEquals(new byte[]{(byte) 128}, Base64.toIntegerBytes(BigInteger.valueOf(128)));
        assertArrayEquals(new byte[]{1, 0}, Base64.toIntegerBytes(BigInteger.valueOf(256)));
    }

    @Test(timeout = 4000)
    public void testMaxResultSizeZeroThrows() {
        try {
            Base64.encodeBase64(new byte[]{1}, false, false, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testNullAndEmptyStaticInputs() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.encodeBase64(null, false, false, Integer.MAX_VALUE));
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0], false, false, Integer.MAX_VALUE));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullInstanceInputs() {
        Base64 codec = new Base64(0);
        assertNull(codec.encode(null));
        assertArrayEquals(new byte[0], codec.encode(new byte[0]));
        assertNull(codec.decode((byte[]) null));
        assertArrayEquals(new byte[0], codec.decode(new byte[0]));
        assertEquals(0, codec.decode("").length);
    }
}