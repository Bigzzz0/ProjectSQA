/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.json.JsonGeneratorImpl
 *
 * Decision / Branch Matrix:
 * 1. Constructor JsonGeneratorImpl(IOContext, int, ObjectCodec)
 *    - Branch: Feature.ESCAPE_NON_ASCII enabled  -> _maximumNonEscapedChar = 127
 *    - Branch: Feature.ESCAPE_NON_ASCII disabled -> _maximumNonEscapedChar = 0
 *    - Branch: Feature.QUOTE_FIELD_NAMES enabled -> _cfgUnqNames = false
 *    - Branch: Feature.QUOTE_FIELD_NAMES disabled -> _cfgUnqNames = true
 * 2. enable(Feature f)
 *    - Branch: f == Feature.QUOTE_FIELD_NAMES -> _cfgUnqNames = false
 *    - Branch: f != Feature.QUOTE_FIELD_NAMES -> super.enable(f), _cfgUnqNames untouched
 * 3. _checkStdFeatureChanges(int, int)
 *    - Branch: Feature.QUOTE_FIELD_NAMES in newFeatureFlags -> _cfgUnqNames = false
 *    - Branch: Feature.QUOTE_FIELD_NAMES NOT in newFeatureFlags -> _cfgUnqNames = true
 * 4. setHighestNonEscapedChar(int charCode)
 *    - Branch: charCode < 0  -> _maximumNonEscapedChar = 0
 *    - Branch: charCode >= 0 -> _maximumNonEscapedChar = charCode
 * 5. setCharacterEscapes(CharacterEscapes esc)
 *    - Branch: esc == null -> _outputEscapes = sOutputEscapes
 *    - Branch: esc != null -> _outputEscapes = esc.getEscapeCodesForAscii()
 * 6. Defect Target:
 *    - Defects4J ground truth: TestJsonGeneratorFeatures::testFieldNameQuotingEnabled / disable()
 *      JsonGeneratorImpl overrides enable() to set _cfgUnqNames = false, but fails to override
 *      disable(Feature.QUOTE_FIELD_NAMES) or ensure _cfgUnqNames is set to true when disabled via
 *      generator.disable(Feature.QUOTE_FIELD_NAMES).
 */

package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonGeneratorImplGeminiTest {

    private static class DummyCharacterEscapes extends CharacterEscapes {
        private final int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();

        @Override
        public int[] getEscapeCodesForAscii() {
            return asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    }

    private static class TestJsonGeneratorImpl extends JsonGeneratorImpl {
        public TestJsonGeneratorImpl(IOContext ctxt, int features) {
            super(ctxt, features, null);
        }

        @Override
        public void writeStartArray() throws IOException {}
        @Override
        public void writeEndArray() throws IOException {}
        @Override
        public void writeStartObject() throws IOException {}
        @Override
        public void writeEndObject() throws IOException {}
        @Override
        public void writeFieldName(String name) throws IOException {}
        @Override
        public void writeFieldName(SerializableString name) throws IOException {}
        @Override
        public void writeString(String text) throws IOException {}
        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
        @Override
        public void writeRaw(String text) throws IOException {}
        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override
        public void writeRaw(char c) throws IOException {}
        @Override
        public void writeBinary(com.fasterxml.jackson.core.Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
        @Override
        public void writeNumber(int v) throws IOException {}
        @Override
        public void writeNumber(long v) throws IOException {}
        @Override
        public void writeNumber(java.math.BigInteger v) throws IOException {}
        @Override
        public void writeNumber(double v) throws IOException {}
        @Override
        public void writeNumber(float v) throws IOException {}
        @Override
        public void writeNumber(java.math.BigDecimal v) throws IOException {}
        @Override
        public void writeNumber(String encodedValue) throws IOException {}
        @Override
        public void writeBoolean(boolean state) throws IOException {}
        @Override
        public void writeNull() throws IOException {}
        @Override
        public void flush() throws IOException {}
        @Override
        protected void _releaseBuffers() {}
        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {}
    }

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), "source", false);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorFeatureCombinations() {
        IOContext ctxt = createIOContext();

        // 1. ESCAPE_NON_ASCII enabled, QUOTE_FIELD_NAMES enabled
        int feat1 = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()
                  | JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        TestJsonGeneratorImpl gen1 = new TestJsonGeneratorImpl(ctxt, feat1);
        assertEquals(127, gen1.getHighestEscapedChar());
        assertFalse(gen1._cfgUnqNames);

        // 2. ESCAPE_NON_ASCII disabled, QUOTE_FIELD_NAMES disabled
        int feat2 = 0;
        TestJsonGeneratorImpl gen2 = new TestJsonGeneratorImpl(ctxt, feat2);
        assertEquals(0, gen2.getHighestEscapedChar());
        assertTrue(gen2._cfgUnqNames);
    }

    @Test(timeout = 4000)
    public void testEnableQuoteFieldNames() {
        IOContext ctxt = createIOContext();
        // Start with QUOTE_FIELD_NAMES disabled -> _cfgUnqNames is true
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);
        assertTrue(gen._cfgUnqNames);

        // Enable another feature first; _cfgUnqNames should remain true
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(gen._cfgUnqNames);

        // Enable QUOTE_FIELD_NAMES -> _cfgUnqNames must transition to false
        JsonGenerator returned = gen.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertSame(gen, returned);
        assertFalse(gen._cfgUnqNames);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test(timeout = 4000)
    public void testCheckStdFeatureChanges() {
        IOContext ctxt = createIOContext();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);
        assertTrue(gen._cfgUnqNames);

        // Simulate feature change with QUOTE_FIELD_NAMES enabled
        int quoteMask = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        gen._checkStdFeatureChanges(quoteMask, quoteMask);
        assertFalse(gen._cfgUnqNames);

        // Simulate feature change with QUOTE_FIELD_NAMES disabled
        gen._checkStdFeatureChanges(0, quoteMask);
        assertTrue(gen._cfgUnqNames);
    }

    @Test(timeout = 4000)
    public void testSetAndGetCharacterEscapes() {
        IOContext ctxt = createIOContext();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);

        assertNull(gen.getCharacterEscapes());
        assertSame(JsonGeneratorImpl.sOutputEscapes, gen._outputEscapes);

        DummyCharacterEscapes customEscapes = new DummyCharacterEscapes();
        JsonGenerator returned = gen.setCharacterEscapes(customEscapes);
        assertSame(gen, returned);
        assertSame(customEscapes, gen.getCharacterEscapes());
        assertSame(customEscapes.getEscapeCodesForAscii(), gen._outputEscapes);

        // Reset to null
        gen.setCharacterEscapes(null);
        assertNull(gen.getCharacterEscapes());
        assertSame(JsonGeneratorImpl.sOutputEscapes, gen._outputEscapes);
    }

    @Test(timeout = 4000)
    public void testSetRootValueSeparator() {
        IOContext ctxt = createIOContext();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);
        assertSame(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, gen._rootValueSeparator);

        SerializableString customSep = new SerializedString("/");
        JsonGenerator returned = gen.setRootValueSeparator(customSep);
        assertSame(gen, returned);
        assertSame(customSep, gen._rootValueSeparator);

        gen.setRootValueSeparator(null);
        assertNull(gen._rootValueSeparator);
    }

    @Test(timeout = 4000)
    public void testVersion() {
        IOContext ctxt = createIOContext();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);
        Version v = gen.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());
    }

    @Test(timeout = 4000)
    public void testWriteStringFieldDelegation() throws IOException {
        final StringBuilder log = new StringBuilder();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(createIOContext(), 0) {
            @Override
            public void writeFieldName(String name) {
                log.append("name:").append(name).append(";");
            }
            @Override
            public void writeString(String text) {
                log.append("val:").append(text).append(";");
            }
        };

        gen.writeStringField("myField", "myValue");
        assertEquals("name:myField;val:myValue;", log.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetHighestNonEscapedCharBoundaries() {
        IOContext ctxt = createIOContext();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, 0);

        // Boundary: negative value must clamp to 0
        gen.setHighestNonEscapedChar(-1);
        assertEquals(0, gen.getHighestEscapedChar());

        gen.setHighestNonEscapedChar(Integer.MIN_VALUE);
        assertEquals(0, gen.getHighestEscapedChar());

        // Boundary: exact 0
        gen.setHighestNonEscapedChar(0);
        assertEquals(0, gen.getHighestEscapedChar());

        // Boundary: 127
        gen.setHighestNonEscapedChar(127);
        assertEquals(127, gen.getHighestEscapedChar());

        // Boundary: 128
        gen.setHighestNonEscapedChar(128);
        assertEquals(128, gen.getHighestEscapedChar());

        // Boundary: 65535
        gen.setHighestNonEscapedChar(65535);
        assertEquals(65535, gen.getHighestEscapedChar());

        // Boundary: Integer.MAX_VALUE
        gen.setHighestNonEscapedChar(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, gen.getHighestEscapedChar());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // Target: testFieldNameQuotingEnabled / disabling QUOTE_FIELD_NAMES via disable()
    // =========================================================================

    @Test(timeout = 4000)
    public void testFieldNameQuotingDisabledViaDisableWriter() throws IOException {
        JsonFactory f = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = f.createGenerator(sw);

        // QUOTE_FIELD_NAMES is enabled by default in JsonGenerator
        assertTrue(gen.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        // Disable quoting via generator.disable()
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        // When QUOTE_FIELD_NAMES is disabled, output must be {foo:1}, NOT {"foo":1}
        gen.writeStartObject();
        gen.writeNumberField("foo", 1);
        gen.writeEndObject();
        gen.close();

        assertEquals("{foo:1}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFieldNameQuotingDisabledViaDisableBytes() throws IOException {
        JsonFactory f = new JsonFactory();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = f.createGenerator(bytes);

        assertTrue(gen.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        // Disable quoting via generator.disable()
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        // When QUOTE_FIELD_NAMES is disabled, output must be {foo:1}, NOT {"foo":1}
        gen.writeStartObject();
        gen.writeNumberField("foo", 1);
        gen.writeEndObject();
        gen.close();

        assertEquals("{foo:1}", bytes.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testDirectCfgUnqNamesFlagUpdatedOnDisable() {
        IOContext ctxt = createIOContext();
        int initialFeatures = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        TestJsonGeneratorImpl gen = new TestJsonGeneratorImpl(ctxt, initialFeatures);

        assertFalse("Expected _cfgUnqNames to be false when QUOTE_FIELD_NAMES is enabled", gen._cfgUnqNames);

        // Disabling QUOTE_FIELD_NAMES should set _cfgUnqNames to true
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        assertTrue("Expected _cfgUnqNames to be true after disabling QUOTE_FIELD_NAMES", gen._cfgUnqNames);
    }

    // =========================================================================
    // Partition D: End-to-End Formatting & Escaping Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeNonAsciiInWriterGenerator() throws IOException {
        JsonFactory f = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = f.createGenerator(sw);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        gen.writeStartObject();
        gen.writeStringField("text", "\u00A9"); // copyright sign
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"text\":\"\\u00A9\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeNonAsciiInByteGenerator() throws IOException {
        JsonFactory f = new JsonFactory();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = f.createGenerator(bytes);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        gen.writeStartObject();
        gen.writeStringField("text", "\u00A9");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"text\":\"\\u00A9\"}", bytes.toString("UTF-8"));
    }
}