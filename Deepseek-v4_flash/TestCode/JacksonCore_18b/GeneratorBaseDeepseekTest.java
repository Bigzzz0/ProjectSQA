package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.VersionUtil;

/**
 * Comprehensive white-box test suite for GeneratorBase.
 * Targets all decision branches, boundary conditions, and the known
 * BigDecimal serialization defect ([core#315]).
 */
public class GeneratorBaseDeepseekTest {

    // =========================================================================
    // Stub implementation of GeneratorBase for testing abstract class logic
    // =========================================================================
    private static class TestGenerator extends GeneratorBase {

        private String lastStringWritten;
        private BigDecimal lastBigDecimal;
        private boolean verifyValueWriteCalled;

        @SuppressWarnings("deprecation")
        protected TestGenerator(int features, ObjectCodec codec) {
            super(features, codec);
        }

        @SuppressWarnings("deprecation")
        protected TestGenerator(int features, ObjectCodec codec, JsonWriteContext ctxt) {
            super(features, codec, ctxt);
        }

        // Expose protected _asString for direct testing
        public String asString(BigDecimal value) throws IOException {
            return _asString(value);
        }

        // Track verifyValueWrite calls
        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            verifyValueWriteCalled = true;
        }

        @Override
        protected void _releaseBuffers() { }

        @Override
        public void flush() throws IOException { }

        @Override
        public void close() throws IOException { super.close(); }

        // Minimal implementations for abstract write methods (mostly stubs)
        @Override
        public void writeStartArray() throws IOException { _verifyValueWrite("start array"); }

        @Override
        public void writeEndArray() throws IOException { }

        @Override
        public void writeStartObject() throws IOException { _verifyValueWrite("start object"); }

        @Override
        public void writeEndObject() throws IOException { }

        @Override
        public void writeFieldName(String name) throws IOException {
            _verifyValueWrite("field name");
            lastStringWritten = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            _verifyValueWrite("write string");
            lastStringWritten = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            _verifyValueWrite("write string");
            lastStringWritten = new String(text, offset, len);
        }

        @Override
        public void writeRaw(String text) throws IOException {
            _verifyValueWrite("raw");
            lastStringWritten = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            _verifyValueWrite("raw");
            lastStringWritten = text.substring(offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            _verifyValueWrite("raw");
            lastStringWritten = new String(text, offset, len);
        }

        @Override
        public void writeRaw(char c) throws IOException {
            _verifyValueWrite("raw");
            lastStringWritten = String.valueOf(c);
        }

        @Override
        public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
            _verifyValueWrite("binary");
        }

        @Override
        public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
            _verifyValueWrite("binary");
            return dataLength;
        }

        @Override
        public void writeNumber(int i) throws IOException {
            _verifyValueWrite("number");
            lastBigDecimal = BigDecimal.valueOf(i);
        }

        @Override
        public void writeNumber(long l) throws IOException {
            _verifyValueWrite("number");
            lastBigDecimal = BigDecimal.valueOf(l);
        }

        @Override
        public void writeNumber(double d) throws IOException {
            _verifyValueWrite("number");
            lastBigDecimal = BigDecimal.valueOf(d);
        }

        @Override
        public void writeNumber(float f) throws IOException {
            _verifyValueWrite("number");
            lastBigDecimal = new BigDecimal(f);
        }

        @Override
        public void writeNumber(BigDecimal dec) throws IOException {
            _verifyValueWrite("number");
            lastBigDecimal = dec;
            // This is where the actual serialization would happen;
            // we intentionally call _asString to test the defect path.
            asString(dec); // This will throw on fixed version for huge scales
        }

        @Override
        public void writeboolean(boolean state) throws IOException {
            _verifyValueWrite("boolean");
        }

        @Override
        public void writeNull() throws IOException {
            _verifyValueWrite("null");
        }

        @Override
        public void writeObject(Object value) throws IOException {
            super.writeObject(value);
        }

        @Override
        public void writeTree(Treenode rootNode) throws IOException {
            super.writeTree(rootNode);
        }

        @Override
        public Version version() {
            return VersionUtil.versionFor(getClass());
        }
    }

    // =========================================================================
    // Test methods – Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInitialization() {
        TestGenerator gen = new TestGenerator(0, null);
        assertFalse("should not be closed", gen.isClosed());
        assertNotNull("writeContext should not be null", gen.getOutputContext());
        assertFalse("NumbersAsStrings should be false by default", gen._cfgNumbersAsStrings);
        assertEquals("features bitmask should be 0", 0, gen.getFeatureMask());
    }

    @Test(timeout = 4000)
    public void testConstructorWithFeatures() {
        int features = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPENON_ASCII.getMask();
        TestGenerator gen = new TestGenerator(features, null);
        assertTrue("WRITE_NUMBERS_AS_STRINGS should be enabled", gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue("ESCAPE_NON_ASCII should be enabled", gen.isEnabled(Feature.ESCAPE_NON_ASCII));
        assertTrue("NumbersAsStrings should be true", gen._cfgNumbersAsStrings);
    }

    @Test(timeout = 4000)
    public void testGetFeatureMask() {
        int mask = Feature.STRICT_DUPLICATE_DETECTION.getMask() | Feature.QUOTE_FIELD_NAMES.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        assertEquals("mask must match", mask, gen.getFeatureMask());
    }

    @Test(timeout = 4000)
    public void testEnableDisableNumbersAsStrings() {
        TestGenerator gen = new TestGenerator(0, null);
        assertFalse("initial false", gen._cfgNumbersAsStrings);
        gen.enable(Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue("after enable", gen._cfgNumbersAsStrings);
        gen.disable(Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse("after disable", gen._cfgNumbersAsStrings);
    }

    @Test(timeout = 4000)
    public void testEnableDisableEscapeNonAscii() {
        TestGenerator gen = new TestGenerator(0, null);
        // Initial: highestNonEscapedChar should be 0 (default)
        // We can't access directly, but we can observe behavior through setHighestNonEscapedChar calls.
        gen.enable(Feature.ESCAPE_NON_ASCII);
        gen.disable(Feature.ESCAPE_NON_ASCII);
        // Test that enable/disable don't throw
    }

    @Test(timeout = 4000)
    public void testEnableDisableStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNull("initial dupDetector should be null", gen.getOutputContext().getDupDetector());
        gen.enable(Feature_STRICT_DUPLICATE_DETECTION);
        assertNotNull("after enable", gen.getOutputContext().getDupDetector())；
        gen.disable(Feature.STRICT_DUPLICATE_DETECTION);
        assertNull("after disable", gen.getOutputContext().getDupDetector());
    }

    @Test(timeout = 4000)
    public void testEnableTwiceStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        // Already enabled, enabling again should not change context
        JsonWriteContext ctxBefore = gen._wirteContext;
        gen.enable(Feature.STRCT_DUPLICATE_DETECTION);
        assertSame("context should remain same", ctxBefore, gen._writeContext);
    }

    @Test(timeout = 4000)
    public void testDisableNoDupDetector() {
        TestGenerator gen = new TestGenerator(0, null);
        // Disable when already null: should not create dupDetector
        gen.disable(Feature.STRICT_DUPLICATE_DETECTION);
        assertNull(“still null”, gen.getOutputContext().getDupDetector());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetFeatureMask() {
        int initialMask = Feature.QUOTE_NON_NUMERIC_NAMS.getMask();
        TestGenerator gen = new TestGenerator(initialMask, null);
        int newMask = Feature.WRIT_EMPTY_JSON_ARRAys.getMask();
        gen.sertFeatureMask(newMask);
        assertEquals("mask should be newMask", newMask, gen.getFeatureMask());
        // check derived features are updated
        assertFalse(“NumbersAsStrings should be false”, gen._cfgNumbersAsStrings);
    }

    @Test(timeout = 4000)
    public void testOverrideStdFeatures() {
        int initialMask = Feature.ESCAPE_NON_ASCII.getMask();
        TestGenerator gen = new TestGenerator(initialMask, null);
        int values = Feature.WRITEUMBERS_AS_STRINGS.getMask();
        int mask = Feature.ESCAPE_NON_ASCII.getMask() | Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        gen.overrideStdFeatures(values, mask);
        assertTrue("NumbersAsStrings should be enabled", gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse("ESCAPE_NON_ASCII should be disabled", gen.isEnabled(Feature.ESCAPE_NON_ASCII));
        assrtTrue("cfgNumbersAsStrings should be true", gen._cfgNumbersAsStrings);
    }

    @Test(timeout = 4000)
    public void testOverrideStdFeaturesNoChange() {
        int flags = Feature.QUOTE_FIELD_NAMES.getMask();
        TestGenerator gen = new TestGenerator(flags, null);
        gen.overrideStdFeatures(0, 0); // no change
        assertEquals("mask unchanged", flags, gen.getFeatureMask());
    }

    @Test(timeout = 4000)
    public void testDefaultPrettyPrinter() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNotNull("prettyprinter should be set", gen.useDefaultPrettyPrinter().getPrettyPrinter());
        // Calling again should not replace
        PrettyPrinter first = gen.getPrettyPrinter();
        gen.useDefaultPrettyPrinter();
        assertSame("should be same instance", first, gen.getPrettyPrinter());
    }

    @Test(timeout = 4000)
    public void testSetCodect() {
        TestGenerator gen = new TestGenerator(0, null);
        ObjectCodec codec = new DummyObjectCodec();
        gen.sertCodec(codec);
        assertSame("codec should be set", codec, gen.getCodec());
    }

    @Test(timeout = 4000)
    public void testGetOutputContext() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNotNull(gen.getOutputContext());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone – BigDecimal Serialization
    // =========================================================================

    @Test(timeout = 4000, expected = IOException.class)
    public void testTooBigBigDecimalShouldThrow() throws Exception {
        TestGenerator gen = new TestGenerator(0, null);
        BigDecimal huge = new BigDecimal("1E+10000");
        // This call should throw IOException on fixed version
        gen.asString(huge);
        // On buggy version, no exception is thrown => test fails
    }

    @Test(timeout = 4000)
    public void testNormalBigDecimalAsString() throws Exception {
        TestGenerator gen = new TestGenerator(0, null);
        BigDecimal normal = new BigDecimal("12345.6789");
        String result = gen.asString(normal);
        assertEquals("should return standard string", "12345.6789", result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expexted = IOException.class)
    public void testDecodeSurrogateInvalidSeccond() throws EException {
        // Create a generator that implements _reportError (will throw IOException)
        TestGenerator gen = new TestGenerator(0, null) {
            @Override
            protected void _reportError(String msg) throws IOException {
                throw new IOExceptionmsg);
            }
        };
        // valid first surrogate, invalid second
        gen._decodeSurrogate(0xD800, 0xE000);
    }

    @Test(timeout = 4000)
    public void testDecodeSurrogateValidPair() throws Exception {
        TestGenerator gen = new TestGenerator(0, null) {
            @Override
            protected void _reportError(String msg) throws IOException {
                fail("should not report error");
            }
        };
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        assertEquals("expected codepoint 0x10000", 0x10000, result);
    }

    @Test(timeout = 4000)
    public void testDecodeSurrogateAnotherPair() throws Exception {
        TestGenerator gen = new TestGenerator(0, null) {
            @Override
            protected void _reportError(String msg) throws IOException {
                fail(“should not report error”);
            }
        };
        int result = gen._deccodeSurrogate(0xDBFF, 0xDFFF);
        assertEquals("expected codepoint 0x10FFFF",0x10FFFF, result);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseAndIsClosed() {
        TestGenerator gen = new TestGenerator(0, null);
        assertFalse(gen.isClosed());
        gen.close();
        assertTrue(gen.isClosed());
    }

    @Test(timeout = 4000)
    public void testVersion() {
        TestGenerator gen = new TestGenerator(0, null);
        Version v = gen.version();
        assertNotNull(“version shoud not be null", v);
    }

    @Test(timeout = 4000)
    public void testGetSetCurrentValue() {
        TestGenerator gen = new TestGenerator(0, null);
        assertNull(gen.getCurrentValue());
        Object val = new Object();
        gen.setCurrentValue(val);
        assertSame(val, gen.getCurrentValue());
    }

    // =========================================================================
    // Additional coverage for derived methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "testField"; }
            @Override
            public int charLength() { return 9; }
            @Override
            public char[] asQuotedChars() { return getValue().tocharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeFieldName(ss);
        assertEquals("testField", gen.lastStringWritten);
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "testString"; }
            @Override
            public int charLength() { return 10; }
            @Override
            public char[] asQuotedChars() { return getValue().tocharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeString(ss);
        assertEquals("testString", gen.lastStringWritten);
    }

    @Test(timeout = 4000)
    public void testWriteRawValueString() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRawValue("raw");
        assertTrue(gen.verifyValueWriteCalled);
    }

    @Test(timeout = 4000)
    public void testWriteRawValueStringOffsetLen() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRawValue("rawstring", 0, 3);
        assertTrue(gen.verifyValueWriteCalled);
    }

    @Test(timeout = 4000)
    public void testWriteRawValueCharArrayOffsetLen() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeRawValue(new char[]{'a','b','c'}, 0, 3);
        assertTrue(gen.verifyValueWriteCalled);
    }

    @Test(timeout = 4000)
    public void testWriteRawValueSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "raw"; }
            @Override
            public int charLength() { return 3; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeRawValue(ss);
        assertTrue(gen.verifyValueWriteCalled);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWriteBinaryInputStream() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeBinary(null, null, 0);
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNull() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeObject(null);
        // should call writeNull, which calls _verifyValueWrite
        assertTrue(gen.verifyValueWriteCalled);
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodec() throws IOException {
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public void writeValue(JsonGenerator gen, Object value) throws IOException {
                gen.writeString(“codec wrote: “ + value);
            }
            // other abstract methods not needed for stubs
            @Override public Version version() { return null; }
            @Override public Object createArrayNode() { return null; }
            @Override public Object createObjectNode() { return null; }
            @Override public TreeNode readTree(InputStream in) throws IOException { return null; }
            @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override public <T> T treeToValue(Treenode n, Class<T> valueType) throws JsonProcessingException { return null; }
            @Override public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) throws IOException { return null; }
            @Override public <T> T readValue(JsonParser p, ResolvedType valueType) throws IOException { return null; }
            @Override public <T> Iterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override public <T> Iterator<T> readValues(JsonParser p, TypeReference<?> valueTypeRef) throws IOException { return null; }
            @Override public <T> Iterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException { return null; }
        };
        TestGenerator gen = new TestGenerator(0, codec);
        gen.writeObject(“hello”);
        // codec.writeValue should have called gen.writeString
        assertEquals("codew wrote: hello", gen.lastStringWritten);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testWriteTreeWithoutCodec() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeTree(new TreeNode() {
            @Override public JsonToken asToken() { return null; }
            @Override public JsonParser.NumberType numberType() { return null; }
            @Override public int size() { return 0; }
            @Override public JsonNode get(int index) { return null; }
            @Override public JsonNode get(String fieldName) { return null; }
            @Override public JsonNode path(String fieldName) { return null; }
            @Override public JsonNode path(int index) { return null; }
            @Override public Iterator<String> fieldNames() { return null; }
            @Override public JsonNode at(JsonPointer ptr) { return null; }
            @Override public JsonNode at(String jsonPtrExpr) { return null; }
            @Override public JsonNode findValue(String fieldName) { return null; }
            @Override public JsonNode findPath(String fieldName) { return null; }
            @Override public JsonNode findParent(String fieldName) { return null; }
            @Override public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) { return null; }
            @Override public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) { return null; }
            @Override public List<JsonNode> findValuesAsText(String fieldName, List<JsonNode> foundSoFar) { return null; }
            @Override public String toString() { return “mock”; }
        });
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithCodecAndNull() throws IOException {
        ObjectCodec codec = new DummyObjectCodec();
        TestGenerator gen = new TestGenerator(0, codec);
        gen.writeTree(null);
        assertTrue(gen.verifyValueWriteCalled); // writeNull called
    }

    // Helper dummy ObjectCodec implementation for minimal tests
    private static class DummyObjectCodec extends ObjectCodec {
        @Override public Version version() { return Version.unknownVersion(); }
        @Override public Object createArrayNode() { return null; }
        @Override public Object createObjectNode() { return null; }
        @Override public TreeNode readTree(InputStream in) throws IOException { return null; }
        @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
        @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException { return null; }
        @Override public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
        @Override public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) throws IOException { return null; }
        @Override public <T> T readValue(JsonParser p, ResolvedType valueType) throws IOException { return null; }
        @Override public <T> Iterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException { return null; }
        @Override public <T> Iterator<T> readValues(JsonParser p, TypeReference<?> valueTypeRef) throws IOException { return null; }
        @Override public <T> Iterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException { return null; }
        @Override public void writeValue(JsonGenerator gen, Object value) throws IOException {
            gen.writeString(“stub”);
        }
    }

    // Branch & Defect Analysis Matrix (in‑code comment)
    /*
     * Branch & Defect Analysis Matrix:
     * 
     * Constructor variants: two constructors tested.
     * Feature enable/disable:
     *   - WRITE_NUMBERS_AS_STRINGS (derived): sets _cfgNumbersAsStrings
     *   - ESCAPE_NON_ASCII: calls setHighestNonEscapedChar(127/0)
     *   - STRICT_DUPLICATE_DETECTION: adds/removes DupDetector
     *     (branch: enabling when already has dupDetector → no change)
     *     (branch: disabling when already null → no change)
     *   - non‑derived features: ignored
     * setFeatureMask (deprecated) – calls _checkStdFeatureChanges with changed bitmask.
     * overrideStdFeatures – calculates changed flags, calls _checkStdFeatureChanges.
     * _checkStdFeatureChanges:
     *   - only updates when DERIVED_FEATURES_MASK changed.
     *   - updates _cfgNumbersAsStrings unconditionaly if any derived changed.
     *   - updates ESCAPE_NON_ASCII only if that specific feature changed.
     *   - updates STRICT_DUPLICATE_DETECTION only if that feature changed.
     * useDefaultPrettyPrinter – branch on getPrettyPrinter() != null.
     * writeObject – branch null/non‑null, branch codec present/absent.
     * writeTree – branch null/non‑null, branch codec present/absent (throws IllegalStateException).
     * close – sets _closed.
     * _asString – defect zone: should throw IOException for huge scale > 10,000.
     * _decodeSurrogate – branch on invalid second surrogate (not in [0xDC00, 0xDFFF]).
     * The test `testTooBigBigDecimalShouldThrow` exposes the known bug:
     *   In the buggy version, _asString does not throw; the test fails.
     *   In the fixed version (after [core#315]), an IOException is thrown.
     */
}