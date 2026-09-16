/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.codec.binary.Base64
 *
 * 1. Constructor Branches:
 *    - Base64(): defaults lineLength=0, urlSafe=false, lineSeparator=CRLF.
 *    - Base64(boolean urlSafe): chunk size=76, separator=CRLF, URL-safe table.
 *    - Base64(int lineLength): separator=CRLF, urlSafe=false.
 *    - Base64(int, byte[]): checks lineSeparator containing base64 bytes (throws IAE).
 *    - Base64(int, byte[], boolean):
 *        * lineSeparator == null -> lineLength forced to 0, default separator.
 *        * lineLength <= 0 vs lineLength > 0 (chunk size rounded down to multiple of 4).
 *        * containsBase64Byte(lineSeparator) == true -> IllegalArgumentException.
 *        * urlSafe == true (URL_SAFE_ENCODE_TABLE) vs false (STANDARD_ENCODE_TABLE).
 *
 * 2. Streaming Buffer Mechanics (Package-Private State Management):
 *    - setInitialBuffer(): out == null vs out.length == outAvail vs out.length != outAvail.
 *    - hasData(): buffer == null vs buffer != null.
 *    - avail(): buffer == null (0) vs pos - readPos.
 *    - resizeBuffer(): initial allocation (DEFAULT_BUFFER_SIZE=8192) vs doubling existing buffer.
 *    - readResults():
 *        * buffer == null (returns -1 when eof is true, 0 when eof is false).
 *        * buffer != null:
 *            - buffer != b: copies min(avail, bAvail), advances readPos; nulls buffer if readPos >= pos.
 *            - buffer == b: re-use original array path, nulls buffer immediately.
 *
 * 3. Encode Branches:
 *    - encode(byte[], int, int):
 *        * eof == true guard (early return).
 *        * inAvail < 0 (EOF signal):
 *            - modulus == 0: no trailing pad/modulus handling.
 *            - modulus == 1: 2 base64 chars + 2 PAD bytes (STANDARD) or 0 PAD (URL-safe).
 *            - modulus == 2: 3 base64 chars + 1 PAD byte (STANDARD) or 0 PAD (URL-safe).
 *            - lineLength > 0 && pos > 0: appends lineSeparator.
 *        * inAvail >= 0 (Data loop):
 *            - buffer growth check (buffer.length - pos < encodeSize).
 *            - byte sign handling (b < 0 -> b + 256).
 *            - modulus 3 cycle: emits 4 chars when modulus rolls to 0.
 *            - lineLength > 0 && lineLength <= currentLinePos: lineSeparator insertion, resets currentLinePos.
 *    - encode(byte[]): null or empty returns original input; precomputes encode length; buffer re-read; urlSafe trim.
 *    - encode(Object): non-byte[] throws EncoderException; byte[] succeeds.
 *    - encodeBase64(byte[], boolean, boolean, int maxResultSize):
 *        * len > maxResultSize throws IllegalArgumentException.
 *
 * 4. Decode Branches:
 *    - decode(byte[], int, int):
 *        * eof == true guard.
 *        * inAvail < 0 (EOF signal, sets eof = true).
 *        * Data loop:
 *            - b == PAD ('='): marks eof = true, breaks loop.
 *            - b >= 0 && b < DECODE_TABLE.length: DECODE_TABLE[b] lookup >= 0 vs -1 (ignores whitespace/invalid).
 *            - modulus 4 cycle: emits 3 bytes when modulus rolls to 0.
 *        * Trailing EOF check (eof && modulus != 0):
 *            - modulus == 2: extracts 1 byte ((x >> 16) & 0xff).
 *            - modulus == 3: extracts 2 bytes ((x >> 16) & 0xff, (x >> 8) & 0xff).
 *    - decode(byte[]): null or empty returns original input; initial buffer optimization; reads pos bytes.
 *    - decode(Object): String vs byte[] vs other (throws DecoderException).
 *
 * 5. Static Utility & Integer Conversion Branches:
 *    - isBase64(byte): PAD, valid chars in table, invalid chars (-1), negative octets, > length octets.
 *    - isArrayByteBase64(byte[]): validates characters or whitespaces (' ', '\t', '\r', '\n'); non-base64 returns false.
 *    - discardWhitespace(byte[]): ignores spaces/tabs/CR/LF, preserves everything else.
 *    - encodeInteger(BigInteger): null check (NPE), positive BigIntegers with varying bit lengths (bitLength % 8 == 0,
 *      bitLength % 8 != 0, sign bit stripping, leading null padding).
 *    - decodeInteger(byte[]): constructs positive BigInteger.
 *
 * 6. Defects4J Known Defect Context (CODEC-98):
 *    - Null/boundary checks when consumers interface with streaming buffers and readResults/setInitialBuffer.
 */

package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class Base64GeminiTest {

    // =================================================================