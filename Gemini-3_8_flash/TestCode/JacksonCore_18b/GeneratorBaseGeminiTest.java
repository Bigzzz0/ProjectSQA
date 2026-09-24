/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.fasterxml.jackson.core.base.GeneratorBase
 *
 * 1. Defect-Targeted Branches (Defects4J Ground Truth: testTooBigBigDecimal):
 *    - Branch: _asString(BigDecimal) when JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN is enabled.
 *      Buggy logic unconditionally returns value.toString() without verifying if scale exceeds
 *      the safety boundary (MAX_BIG_DECIMAL_SCALE = 9999), failing to prevent asymmetric DoS attacks.
 *      Test targets: new BigDecimal("1E+10000"), new BigDecimal("1E-10000").
 *      Expected Behavior: Throw JsonGenerationException / IOException indicating illegal scale.
 *
 * 2. Feature Configuration & State Management:
 *    - enable(Feature) / disable(Feature):
 *      - WRITE_NUMBERS_AS_STRINGS: verifies _cfgNumbersAsStrings toggles true/false.
 *      - ESCAPE_NON_ASCII: verifies highestNonEscapedChar transitions between 127 and 0.
 *      - STRICT_DUPLICATE_DETECTION: verifies DupDetector is attached to JsonWriteContext or set to null.
 *      - Non-derived features (e.g., AUTO_CLOSE_TARGET): ensures mask updates without affecting derived state.
 *    - setFeatureMask(int) / overrideStdFeatures(int, int):
 *      - Bitwise delta changed == 0 (no-op bypass).
 *      - (changedFeatures & DERIVED_FEATURES_MASK) == 0 (early exit branch).
 *      - Full state transitions when derived features are enabled vs disabled simultaneously.
 *
 * 3. Write Pipeline & Value Verification:
 *    - writeRawValue(...) variants (String, substring, char[], SerializableString):
 *      ensures _verifyValueWrite("write raw value") is dispatched before writeRaw.
 *    - writeFieldName(SerializableString), writeString(SerializableString):
 *      verifies delegation to String-based equivalents.
 *    - writeBinary(Base64Variant, InputStream, int):
 *      verifies unsupported operation contract.
 *
 * 4. POJO & Tree Delegation:
 *    - writeObject(Object): null value branch -> writeNull(); non-null with Codec -> writeValue();
 *      non-null without Codec -> _writeSimpleObject().
 *    - writeTree(TreeNode): null node -> writeNull(); non-null with Codec -> writeValue();
 *      non-null without Codec -> IllegalStateException("No ObjectCodec defined").
 *
 * 5. Pretty Printer & Context Handling:
 *    - useDefaultPrettyPrinter(): already present vs null branches.
 *    - getCurrentValue() / setCurrentValue(): output context delegation.
 *
 * 6. UTF-8 / Surrogate Decoding:
 *    - _decodeSurrogate(surr1, surr2):
 *      - Valid bounds: [SURR1_FIRST (0xD800), SURR1_LAST (0xDBFF)] with [SURR2_FIRST (0xDC00), SURR2_LAST (0xDFFF)].
 *      - Invalid surrogate 2 (< 0xDC00 or > 0xDFFF) -> reports error.
 */

package com.fasterxml.jackson.core.base;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class GeneratorBaseGeminiTest {

    // =========================================================================
    // Test Stub & Helper Fixtures
    // =========================================================================

    private static class TestGeneratorBase extends GeneratorBase {
        public boolean releaseBuffersCalled = false;
        public String lastVerifyMsg = null;
        public String lastWrittenString = null;
        public String lastWrittenRaw = null;
        public String lastFieldName = null;
        public boolean writeNullCalled = false;
        public int highestNonEscapedChar = 0;

        public TestGeneratorBase(int features, ObjectCodec codec) {
            super(features, codec);
        }

        public TestGeneratorBase(int features, ObjectCodec codec, JsonWriteContext ctxt) {
            super(features, codec, ctxt);
        }

        @Override
        public JsonGenerator setHighestNonEscapedChar(int charCode) {
            this.highestNonEscapedChar = charCode;
            return this;
        }

        @Override
        public int getHighestNonEscapedChar() {
            return this.highestNonEscapedChar;
        }

        @Override
        public void flush() throws IOException {}

        @Override
        protected void _releaseBuffers() {
            this.releaseBuffersCalled = true;
        }

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            this.lastVerifyMsg = typeMsg;
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
        public void writeFieldName(String name) throws IOException {
            this.lastFieldName = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            this.lastWrittenString = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            this.lastWrittenString = new String(text, offset, len);
        }

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeRaw(String text) throws IOException {
            this.lastWrittenRaw = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            this.lastWrittenRaw = text.substring(offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            this.lastWrittenRaw = new String(text, offset, len);
        }

        @Override
        public void writeRaw(char c) throws IOException {
            this.lastWrittenRaw = String.valueOf(c);
        }

        @Override
        public void writeRaw(SerializableString raw) throws IOException {
            this.lastWrittenRaw = raw.getValue();
        }

        @Override
        public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {}

        @Override
        public void writeNumber(int v) throws IOException {}

        @Override
        public void writeNumber(long v) throws IOException {}

        @Override
        public void writeNumber(BigInteger v) throws IOException {}

        @Override
        public void writeNumber(double v) throws IOException {}

        @Override
        public void writeNumber(float v) throws IOException {}

        @Override
        public void writeNumber(BigDecimal v) throws IOException {}

        @Override
        public void writeNumber(String encodedValue) throws IOException {}

        @Override
        public void writeBoolean(boolean state) throws IOException {}

        @Override
        public void writeNull() throws IOException {
            this.writeNullCalled = true;
        }

        // Expose protected methods for direct verification
        @Override
        public String _asString(BigDecimal value) throws IOException {
            return super._asString(value);
        }

        @Override
        public int _decodeSurrogate(int surr1, int surr2) throws IOException {
            return super._decodeSurrogate(surr1, surr2);
        }

        @Override
        public PrettyPrinter _constructDefaultPrettyPrinter() {
            return super._constructDefaultPrettyPrinter();
        }

        public boolean isNumbersAsStringsConfigured() {
            return this._cfgNumbersAsStrings;
        }
    }

    private static class StubCodec extends ObjectCodec {
        public Object lastWrittenValue = null;

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public <T> T readValue(JsonParser p, Class<T> valueType) {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser p, ResolvedType valueType) {
            return null;
        }

        @Override
        public <T extends TreeNode> T readTree(JsonParser p) {
            return null;
        }

        @Override
        public <T> Iterator<T> readValues(JsonParser p, Class<T> valueType) {
            return null;
        }

        @Override
        public <T> Iterator<T> readValues(JsonParser p, TypeReference<?> valueTypeRef) {
            return null;
        }

        @Override
        public <T> Iterator<T> readValues(JsonParser p, ResolvedType valueType) {
            return null;
        }

        @Override
        public void writeValue(JsonGenerator gen, Object value) {
            this.lastWrittenValue = value;
        }

        @Override
        public void writeTree(JsonGenerator gen, TreeNode tree) {
            this.lastWrittenValue = tree;
        }

        @Override
        public TreeNode createObjectNode() {
            return null;
        }

        @Override
        public TreeNode createArrayNode() {
            return null;
        }

        @Override
        public JsonParser treeAsTokens(TreeNode n) {
            return null;
        }

        @Override
        public <T> T treeToValue(TreeNode n, Class<T> valueType) {
            return null;
        }
    }

    private static class DummyTreeNode implements TreeNode {
        @Override
        public JsonToken asToken() {
            return JsonToken.NOT_AVAILABLE;
        }

        @Override
        public JsonParser.NumberType numberType() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isValueNode() {
            return false;
        }

        @Override
        public boolean isContainerNode() {
            return false;
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isObject() {
            return false;
        }

        @Override
        public TreeNode get(String fieldName) {
            return null;
        }

        @Override
        public TreeNode get(int index) {
            return null;
        }

        @Override
        public TreeNode path(String fieldName) {
            return null;
        }

        @Override
        public TreeNode path(int index) {
            return null;
        }

        @Override
        public Iterator<String> fieldNames() {
            return null;
        }

        @Override
        public TreeNode at(JsonPointer ptr) {
            return null;
        }

        @Override
        public TreeNode at(String jsonPtrExpr) {
            return null;
        }

        @Override
        public JsonParser traverse() {
            return null;
        }

        @Override
        public JsonParser traverse(ObjectCodec codec) {
            return null;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEnableAndDisableStandardDerivedFeatures() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(gen.isNumbersAsStringsConfigured());

        // 1. WRITE_NUMBERS_AS_STRINGS
        gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(gen.isNumbersAsStringsConfigured());

        gen.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertFalse(gen.isNumbersAsStringsConfigured());

        // 2. ESCAPE_NON_ASCII
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        assertEquals(127, gen.getHighestNonEscapedChar());

        gen.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        assertEquals(0, gen.getHighestNonEscapedChar());

        // 3. STRICT_DUPLICATE_DETECTION
        assertNull(gen.getOutputContext().getDupDetector());
        gen.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(gen.getOutputContext().getDupDetector());

        // Enable again when already active (idempotence)
        gen.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertNotNull(gen.getOutputContext().getDupDetector());

        gen.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test(timeout = 4000)
    public void testEnableAndDisableNonDerivedFeature() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        gen.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    @Test(timeout = 4000)
    public void testCurrentValueDelegation() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertNull(gen.getCurrentValue());

        Object dummy = new Object();
        gen.setCurrentValue(dummy);
        assertSame(dummy, gen.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testLifecycleAndClose() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.isClosed());

        gen.close();
        assertTrue(gen.isClosed());
    }

    @Test(timeout = 4000)
    public void testSetAndGetCodec() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertNull(gen.getCodec());

        StubCodec codec = new StubCodec();
        assertSame(gen, gen.setCodec(codec));
        assertSame(codec, gen.getCodec());
    }

    @Test(timeout = 4000)
    public void testVersionExtraction() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        Version v = gen.version();
        assertNotNull(v);
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterHandling() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertNull(gen.getPrettyPrinter());

        // First call assigns default pretty printer
        gen.useDefaultPrettyPrinter();
        assertNotNull(gen.getPrettyPrinter());
        PrettyPrinter pp = gen.getPrettyPrinter();

        // Second call retains existing instance
        gen.useDefaultPrettyPrinter();
        assertSame(pp, gen.getPrettyPrinter());

        assertNotNull(gen._constructDefaultPrettyPrinter());
        assertTrue(gen._constructDefaultPrettyPrinter() instanceof DefaultPrettyPrinter);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetFeatureMaskBranches() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertEquals(0, gen.getFeatureMask());

        // Identical mask (changed == 0 branch)
        assertSame(gen, gen.setFeatureMask(0));
        assertEquals(0, gen.getFeatureMask());

        // Change with non-derived features only (DERIVED_FEATURES_MASK == 0 branch)
        int nonDerived = JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask();
        gen.setFeatureMask(nonDerived);
        assertEquals(nonDerived, gen.getFeatureMask());

        // Change involving derived features (ENABLE)
        int allDerived = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()
                | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        gen.setFeatureMask(allDerived);
        assertTrue(gen.isNumbersAsStringsConfigured());
        assertEquals(127, gen.getHighestNonEscapedChar());
        assertNotNull(gen.getOutputContext().getDupDetector());

        // Change involving derived features (DISABLE)
        gen.setFeatureMask(0);
        assertFalse(gen.isNumbersAsStringsConfigured());
        assertEquals(0, gen.getHighestNonEscapedChar());
        assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test(timeout = 4000)
    public void testOverrideStdFeaturesBranches() {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);

        // No change branch: mask == 0
        gen.overrideStdFeatures(0, 0);
        assertEquals(0, gen.getFeatureMask());

        // Change derived features: enable ESCAPE_NON_ASCII and WRITE_NUMBERS_AS_STRINGS
        int maskToSet = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        gen.overrideStdFeatures(maskToSet, maskToSet);
        assertEquals(127, gen.getHighestNonEscapedChar());
        assertTrue(gen.isNumbersAsStringsConfigured());

        // Disable ESCAPE_NON_ASCII only
        int maskToClear = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        gen.overrideStdFeatures(0, maskToClear);
        assertEquals(0, gen.getHighestNonEscapedChar());
        assertTrue(gen.isNumbersAsStringsConfigured());
    }

    @Test(timeout = 4000)
    public void testDecodeSurrogateValidBoundaries() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);

        // Minimum boundary surrogate pair: U+10000
        int minCodePoint = gen._decodeSurrogate(GeneratorBase.SURR1_FIRST, GeneratorBase.SURR2_FIRST);
        assertEquals(0x10000, minCodePoint);

        // Maximum boundary surrogate pair: U+10FFFF
        int maxCodePoint = gen._decodeSurrogate(GeneratorBase.SURR1_LAST, GeneratorBase.SURR2_LAST);
        assertEquals(0x10FFFF, maxCodePoint);
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testDecodeSurrogateInvalidSecondCharBelowMin() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        gen._decodeSurrogate(GeneratorBase.SURR1_FIRST, GeneratorBase.SURR2_FIRST - 1);
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testDecodeSurrogateInvalidSecondCharAboveMax() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        gen._decodeSurrogate(GeneratorBase.SURR1_FIRST, GeneratorBase.SURR2_LAST + 1);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT: Jackson-core issue [core#315] / Defects4J testTooBigBigDecimal.
     * When Feature.WRITE_BIGDECIMAL_AS_PLAIN is enabled, attempting to serialize a BigDecimal
     * with an excessively large or small scale (e.g. 1E+10000 or 1E-10000) MUST fail with
     * an exception (JsonGenerationException or IOException) to prevent asymmetric CPU/memory DoS.
     * On defective GeneratorBase, _asString(BigDecimal) does not guard against excessive scale,
     * leading to assertion failure.
     */
    @Test(timeout = 4000)
    public void testTooBigBigDecimalThrowsExceptionWhenPlainEnabled() throws Exception {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        BigDecimal huge = new BigDecimal("1E+10000");

        try {
            gen._asString(huge);
            fail("Should not have written without exception: 1E+10000");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Attempt to write plain `java.math.BigDecimal`"));
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Attempt to write plain `java.math.BigDecimal`")
                    || expected.getMessage().contains("scale"));
        }
    }

    @Test(timeout = 4000)
    public void testTooSmallBigDecimalScaleThrowsExceptionWhenPlainEnabled() throws Exception {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        BigDecimal tiny = new BigDecimal("1E-10000");

        try {
            gen._asString(tiny);
            fail("Should not have written without exception: 1E-10000");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Attempt to write plain `java.math.BigDecimal`"));
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Attempt to write plain `java.math.BigDecimal`")
                    || expected.getMessage().contains("scale"));
        }
    }

    @Test(timeout = 4000)
    public void testBigDecimalAsStringWhenPlainDisabled() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        BigDecimal huge = new BigDecimal("1E+10000");
        assertEquals("1E+10000", gen._asString(huge));
    }

    @Test(timeout = 4000)
    public void testSafeBigDecimalWhenPlainEnabled() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        BigDecimal normal = new BigDecimal("123.456");
        assertEquals("123.456", gen._asString(normal));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryWithInputStreamUnsupported() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        gen.writeBinary(Base64Variants.MIME, in, 3);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWriteTreeWithoutCodecThrowsIllegalState() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        DummyTreeNode node = new DummyTreeNode();
        gen.writeTree(node);
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithCodecSucceeds() throws IOException {
        StubCodec codec = new StubCodec();
        TestGeneratorBase gen = new TestGeneratorBase(0, codec);
        DummyTreeNode node = new DummyTreeNode();

        gen.writeTree(node);
        assertSame(node, codec.lastWrittenValue);
    }

    @Test(timeout = 4000)
    public void testWriteTreeNullWritesNull() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.writeNullCalled);

        gen.writeTree(null);
        assertTrue(gen.writeNullCalled);
    }

    @Test(timeout = 4000)
    public void testWriteObjectNullWritesNull() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        assertFalse(gen.writeNullCalled);

        gen.writeObject(null);
        assertTrue(gen.writeNullCalled);
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodecDelegates() throws IOException {
        StubCodec codec = new StubCodec();
        TestGeneratorBase gen = new TestGeneratorBase(0, codec);
        Object custom = new Object();

        gen.writeObject(custom);
        assertSame(custom, codec.lastWrittenValue);
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithoutCodecDispatchesSimpleObject() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);

        gen.writeObject("simple-text");
        assertEquals("simple-text", gen.lastWrittenString);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Textual & Raw Output Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        SerializedString name = new SerializedString("testField");

        gen.writeFieldName(name);
        assertEquals("testField", gen.lastFieldName);
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);
        SerializedString val = new SerializedString("testValue");

        gen.writeString(val);
        assertEquals("testValue", gen.lastWrittenString);
    }

    @Test(timeout = 4000)
    public void testWriteRawValueVariantsVerifyAndWrite() throws IOException {
        TestGeneratorBase gen = new TestGeneratorBase(0, null);

        // 1. writeRawValue(String)
        gen.writeRawValue("raw1");
        assertEquals("write raw value", gen.lastVerifyMsg);
        assertEquals("raw1", gen.lastWrittenRaw);

        // 2. writeRawValue(String, int, int)
        gen.writeRawValue("prefix_raw2_suffix", 7, 4);
        assertEquals("write raw value", gen.lastVerifyMsg);
        assertEquals("raw2", gen.lastWrittenRaw);

        // 3. writeRawValue(char[], int, int)
        char[] chars = "prefix_raw3_suffix".toCharArray();
        gen.writeRawValue(chars, 7, 4);
        assertEquals("write raw value", gen.lastVerifyMsg);
        assertEquals("raw3", gen.lastWrittenRaw);

        // 4. writeRawValue(SerializableString)
        SerializedString sstr = new SerializedString("raw4");
        gen.writeRawValue(sstr);
        assertEquals("write raw value", gen.lastVerifyMsg);
        assertEquals("raw4", gen.lastWrittenRaw);
    }

    @Test(timeout = 4000)
    public void testConstructorsWithFeaturesAndContext() {
        int initialFeatures = JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        TestGeneratorBase gen = new TestGeneratorBase(initialFeatures, null);

        assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        assertTrue(gen.isNumbersAsStringsConfigured());
        assertNotNull(gen.getOutputContext().getDupDetector());

        // Secondary constructor
        JsonWriteContext customCtxt = JsonWriteContext.createRootContext(null);
        TestGeneratorBase gen2 = new TestGeneratorBase(0, null, customCtxt);
        assertSame(customCtxt, gen2.getOutputContext());
        assertFalse(gen2.isNumbersAsStringsConfigured());
    }
}