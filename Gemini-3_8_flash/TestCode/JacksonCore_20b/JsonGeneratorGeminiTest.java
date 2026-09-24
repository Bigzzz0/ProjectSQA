/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.JsonGenerator
 *
 * Targeted Defects & Branches:
 * 1. Defect [core#318]:
 *    - In defective version, writeEmbeddedObject(Object) unconditionally throws JsonGenerationException.
 *    - Fixed behavior delegates byte[] to writeBinary(byte[]) and null to writeNull().
 *    - Targeted in: testWriteEmbeddedObjectBinaryTargetingDefect(), testWriteEmbeddedObjectNullTargetingDefect().
 *
 * 2. Feature enumeration & masks:
 *    - collectDefaults(), enabledByDefault(), enabledIn(int), getMask().
 *    - configure(Feature, boolean), overrideStdFeatures(int, int), overrideFormatFeatures(int, int).
 *
 * 3. Array scalar writing (int[], long[], double[]):
 *    - Valid writes, offset/length boundary checking, null handling, negative offset/length exception branches.
 *
 * 4. Context & CurrentValue:
 *    - getCurrentValue() and setCurrentValue(Object) with both null and non-null JsonStreamContext.
 *    - writeStartObject(Object forValue).
 *
 * 5. Default capability introspection:
 *    - canUseSchema, canWriteObjectId, canWriteTypeId, canWriteBinaryNatively, canOmitFields, canWriteFormattedNumbers.
 *
 * 6. Copy events and structure (copyCurrentEvent, copyCurrentStructure):
 *    - All JsonToken types (START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY, FIELD_NAME, STRING with/without textChars,
 *      INT [INT, BIG_INTEGER, LONG], FLOAT [BIG_DECIMAL, FLOAT, DOUBLE], TRUE, FALSE, NULL, EMBEDDED_OBJECT, NOT_AVAILABLE/null).
 *    - Nested structures copying in copyCurrentStructure.
 *
 * 7. Protected helper _writeSimpleObject:
 *    - null, String, Number sub-types (Integer, Long, Double, Float, Short, Byte, BigInteger, BigDecimal, AtomicInteger, AtomicLong),
 *      byte[], Boolean, AtomicBoolean, unsupported object type.
 *
 * 8. Convenience field writes:
 *    - writeStringField, writeBooleanField, writeNullField, writeNumberField, writeBinaryField, writeArrayFieldStart,
 *      writeObjectFieldStart, writeObjectField, writeOmittedField, writeFieldId.
 */
public class JsonGeneratorGeminiTest {

    // =========================================================================
    // Test Stub Implementation of JsonGenerator
    // =========================================================================

    private static class MockJsonGenerator extends JsonGenerator {
        final List<String> recordedEvents = new ArrayList<String>();
        private int _features = Feature.collectDefaults();
        private JsonStreamContext _context;
        private ObjectCodec _codec;
        private boolean _closed = false;

        public MockJsonGenerator() {
            super();
        }

        public void setOutputContext(JsonStreamContext ctxt) {
            this._context = ctxt;
        }

        @Override
        public JsonGenerator setCodec(ObjectCodec oc) {
            this._codec = oc;
            return this;
        }

        @Override
        public ObjectCodec getCodec() {
            return _codec;
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonGenerator enable(Feature f) {
            _features |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            _features &= ~f.getMask();
            return this;
        }

        @Override
        public boolean isEnabled(Feature f) {
            return (_features & f.getMask()) != 0;
        }

        @Override
        public int getFeatureMask() {
            return _features;
        }

        @Override
        public JsonGenerator setFeatureMask(int values) {
            _features = values;
            return this;
        }

        @Override
        public JsonGenerator useDefaultPrettyPrinter() {
            recordedEvents.add("useDefaultPrettyPrinter");
            return this;
        }

        @Override
        public void writeStartArray() throws IOException {
            recordedEvents.add("startArray");
        }

        @Override
        public void writeEndArray() throws IOException {
            recordedEvents.add("endArray");
        }

        @Override
        public void writeStartObject() throws IOException {
            recordedEvents.add("startObject");
        }

        @Override
        public void writeEndObject() throws IOException {
            recordedEvents.add("endObject");
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            recordedEvents.add("field:" + name);
        }

        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            recordedEvents.add("field:" + name.getValue());
        }

        @Override
        public void writeString(String text) throws IOException {
            recordedEvents.add("string:" + text);
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            recordedEvents.add("stringChars:" + new String(text, offset, len));
        }

        @Override
        public void writeString(SerializableString text) throws IOException {
            recordedEvents.add("stringSerializable:" + text.getValue());
        }

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
            recordedEvents.add("rawUTF8:" + new String(text, offset, length, "UTF-8"));
        }

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
            recordedEvents.add("utf8:" + new String(text, offset, length, "UTF-8"));
        }

        @Override
        public void writeRaw(String text) throws IOException {
            recordedEvents.add("raw:" + text);
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            recordedEvents.add("rawSubstring:" + text.substring(offset, offset + len));
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            recordedEvents.add("rawCharArray:" + new String(text, offset, len));
        }

        @Override
        public void writeRaw(char c) throws IOException {
            recordedEvents.add("rawChar:" + c);
        }

        @Override
        public void writeRawValue(String text) throws IOException {
            recordedEvents.add("rawValue:" + text);
        }

        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException {
            recordedEvents.add("rawValueSubstring:" + text.substring(offset, offset + len));
        }

        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException {
            recordedEvents.add("rawValueCharArray:" + new String(text, offset, len));
        }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
            recordedEvents.add("binary:" + len + "bytes");
        }

        @Override
        public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
            recordedEvents.add("binaryStream:" + dataLength);
            return dataLength;
        }

        @Override
        public void writeNumber(int v) throws IOException {
            recordedEvents.add("number:int:" + v);
        }

        @Override
        public void writeNumber(long v) throws IOException {
            recordedEvents.add("number:long:" + v);
        }

        @Override
        public void writeNumber(BigInteger v) throws IOException {
            recordedEvents.add("number:bigint:" + v);
        }

        @Override
        public void writeNumber(double v) throws IOException {
            recordedEvents.add("number:double:" + v);
        }

        @Override
        public void writeNumber(float v) throws IOException {
            recordedEvents.add("number:float:" + v);
        }

        @Override
        public void writeNumber(BigDecimal v) throws IOException {
            recordedEvents.add("number:bigdec:" + v);
        }

        @Override
        public void writeNumber(String encodedValue) throws IOException {
            recordedEvents.add("number:encoded:" + encodedValue);
        }

        @Override
        public void writeBoolean(boolean state) throws IOException {
            recordedEvents.add("boolean:" + state);
        }

        @Override
        public void writeNull() throws IOException {
            recordedEvents.add("null");
        }

        @Override
        public void writeObject(Object pojo) throws IOException {
            recordedEvents.add("object:" + pojo);
        }

        @Override
        public void writeTree(TreeNode rootNode) throws IOException {
            recordedEvents.add("tree:" + rootNode);
        }

        @Override
        public JsonStreamContext getOutputContext() {
            return _context;
        }

        @Override
        public void flush() throws IOException {
            recordedEvents.add("flush");
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        @Override
        public void close() throws IOException {
            _closed = true;
            recordedEvents.add("close");
        }

        public void publicWriteSimpleObject(Object val) throws IOException {
            _writeSimpleObject(val);
        }

        public void publicReportError(String msg) throws JsonGenerationException {
            _reportError(msg);
        }

        public void publicReportUnsupportedOperation() {
            _reportUnsupportedOperation();
        }
    }

    private static class MockJsonStreamContext extends JsonStreamContext {
        private Object _currVal;

        public MockJsonStreamContext() {
            super();
            _type = TYPE_ROOT;
            _index = 0;
        }

        @Override
        public String getCurrentName() {
            return null;
        }

        @Override
        public JsonStreamContext getParent() {
            return null;
        }

        @Override
        public Object getCurrentValue() {
            return _currVal;
        }

        @Override
        public void setCurrentValue(Object v) {
            _currVal = v;
        }
    }

    private static class StubJsonParser extends JsonParser {
        private List<JsonToken> tokens = new ArrayList<JsonToken>();
        private int index = -1;
        private String currentName;
        private String text;
        private char[] textChars;
        private int textOffset;
        private int textLen;
        private NumberType numberType;
        private int intVal;
        private long longVal;
        private BigInteger bigIntVal;
        private float floatVal;
        private double doubleVal;
        private BigDecimal bigDecVal;
        private Object embeddedObj;

        public void addToken(JsonToken t) {
            tokens.add(t);
        }

        @Override
        public JsonToken nextToken() {
            index++;
            if (index < tokens.size()) {
                _currToken = tokens.get(index);
                return _currToken;
            }
            _currToken = null;
            return null;
        }

        @Override
        public String getCurrentName() {
            return currentName;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public boolean hasTextCharacters() {
            return textChars != null;
        }

        @Override
        public char[] getTextCharacters() {
            return textChars;
        }

        @Override
        public int getTextOffset() {
            return textOffset;
        }

        @Override
        public int getTextLength() {
            return textLen;
        }

        @Override
        public NumberType getNumberType() {
            return numberType;
        }

        @Override
        public int getIntValue() {
            return intVal;
        }

        @Override
        public long getLongValue() {
            return longVal;
        }

        @Override
        public BigInteger getBigIntegerValue() {
            return bigIntVal;
        }

        @Override
        public float getFloatValue() {
            return floatVal;
        }

        @Override
        public double getDoubleValue() {
            return doubleVal;
        }

        @Override
        public BigDecimal getDecimalValue() {
            return bigDecVal;
        }

        @Override
        public Object getEmbeddedObject() {
            return embeddedObj;
        }

        @Override
        public ObjectCodec getCodec() { return null; }
        @Override
        public void setCodec(ObjectCodec c) {}
        @Override
        public Version version() { return Version.unknownVersion(); }
        @Override
        public void close() {}
        @Override
        public boolean isClosed() { return false; }
        @Override
        public JsonStreamContext getParsingContext() { return null; }
        @Override
        public JsonLocation getTokenLocation() { return JsonLocation.NA; }
        @Override
        public JsonLocation getCurrentLocation() { return JsonLocation.NA; }
        @Override
        public void overrideCurrentName(String name) { this.currentName = name; }
        @Override
        public byte[] getBinaryValue(Base64Variant bv) { return new byte[0]; }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known issue: writeEmbeddedObject(byte[]) should write binary data
     * instead of throwing JsonGenerationException("No native support for writing embedded objects").
     */
    @Test(timeout = 4000)
    public void testWriteEmbeddedObjectBinaryTargetingDefect() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        byte[] rawBytes = new byte[] { 1, 2, 3, 4 };
        gen.writeEmbeddedObject(rawBytes);
        assertEquals(1, gen.recordedEvents.size());
        assertEquals("binary:4bytes", gen.recordedEvents.get(0));
    }

    /**
     * Targets Defects4J known issue: writeEmbeddedObject(null) should write null
     * instead of throwing JsonGenerationException("No native support for writing embedded objects").
     */
    @Test(timeout = 4000)
    public void testWriteEmbeddedObjectNullTargetingDefect() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        gen.writeEmbeddedObject(null);
        assertEquals(1, gen.recordedEvents.size());
        assertEquals("null", gen.recordedEvents.get(0));
    }

    /**
     * Non-null and non-byte[] object should still throw JsonGenerationException.
     */
    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteEmbeddedObjectUnsupportedTargetingDefect() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        gen.writeEmbeddedObject(new Object());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFeatureDefaultsAndBitmask() {
        int defaults = JsonGenerator.Feature.collectDefaults();
        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_TARGET.enabledByDefault());
        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.enabledByDefault());
        assertTrue(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.enabledByDefault());
        assertTrue(JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledByDefault());
        assertTrue(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledByDefault());

        assertFalse(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.enabledByDefault());
        assertFalse(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledByDefault());
        assertFalse(JsonGenerator.Feature.ESCAPE_NON_ASCII.enabledByDefault());
        assertFalse(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.enabledByDefault());
        assertFalse(JsonGenerator.Feature.IGNORE_UNKNOWN.enabledByDefault());

        assertTrue(JsonGenerator.Feature.AUTO_CLOSE_TARGET.enabledIn(defaults));
        assertFalse(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(defaults));
        assertEquals(1 << JsonGenerator.Feature.AUTO_CLOSE_TARGET.ordinal(),
                JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask());
    }

    @Test(timeout = 4000)
    public void testConfigureAndOverrideStdFeatures() {
        MockJsonGenerator gen = new MockJsonGenerator();
        assertTrue(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        gen.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        gen.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        gen.overrideStdFeatures(mask, mask);
        assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));

        gen.overrideStdFeatures(0, mask);
        assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test(timeout = 4000)
    public void testCurrentValueHandling() {
        MockJsonGenerator gen = new MockJsonGenerator();
        assertNull(gen.getCurrentValue());
        gen.setCurrentValue("orphan"); // ctxt is null, should be no-op

        MockJsonStreamContext ctxt = new MockJsonStreamContext();
        gen.setOutputContext(ctxt);
        assertNull(gen.getCurrentValue());

        gen.setCurrentValue("myValue");
        assertEquals("myValue", gen.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testWriteStartObjectWithForValue() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        MockJsonStreamContext ctxt = new MockJsonStreamContext();
        gen.setOutputContext(ctxt);

        gen.writeStartObject("contextPojo");
        assertEquals("startObject", gen.recordedEvents.get(0));
        assertEquals("contextPojo", gen.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testScalarArrays() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();

        int[] ints = new int[] { 10, 20, 30 };
        gen.writeArray(ints, 1, 2);
        assertEquals("startArray", gen.recordedEvents.get(0));
        assertEquals("number:int:20", gen.recordedEvents.get(1));
        assertEquals("number:int:30", gen.recordedEvents.get(2));
        assertEquals("endArray", gen.recordedEvents.get(3));

        gen.recordedEvents.clear();
        long[] longs = new long[] { 100L, 200L };
        gen.writeArray(longs, 0, 2);
        assertEquals("startArray", gen.recordedEvents.get(0));
        assertEquals("number:long:100", gen.recordedEvents.get(1));
        assertEquals("number:long:200", gen.recordedEvents.get(2));
        assertEquals("endArray", gen.recordedEvents.get(3));

        gen.recordedEvents.clear();
        double[] doubles = new double[] { 1.5, 2.5 };
        gen.writeArray(doubles, 0, 1);
        assertEquals("startArray", gen.recordedEvents.get(0));
        assertEquals("number:double:1.5", gen.recordedEvents.get(1));
        assertEquals("endArray", gen.recordedEvents.get(2));
    }

    @Test(timeout = 4000)
    public void testConvenienceFieldMethods() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();

        gen.writeStringField("fString", "val");
        assertEquals("field:fString", gen.recordedEvents.get(0));
        assertEquals("string:val", gen.recordedEvents.get(1));

        gen.writeBooleanField("fBool", true);
        assertEquals("field:fBool", gen.recordedEvents.get(2));
        assertEquals("boolean:true", gen.recordedEvents.get(3));

        gen.writeNullField("fNull");
        assertEquals("field:fNull", gen.recordedEvents.get(4));
        assertEquals("null", gen.recordedEvents.get(5));

        gen.writeNumberField("fInt", 1);
        assertEquals("field:fInt", gen.recordedEvents.get(6));
        assertEquals("number:int:1", gen.recordedEvents.get(7));

        gen.writeNumberField("fLong", 2L);
        assertEquals("field:fLong", gen.recordedEvents.get(8));
        assertEquals("number:long:2", gen.recordedEvents.get(9));

        gen.writeNumberField("fDouble", 3.0);
        assertEquals("field:fDouble", gen.recordedEvents.get(10));
        assertEquals("number:double:3.0", gen.recordedEvents.get(11));

        gen.writeNumberField("fFloat", 4.0f);
        assertEquals("field:fFloat", gen.recordedEvents.get(12));
        assertEquals("number:float:4.0", gen.recordedEvents.get(13));

        gen.writeNumberField("fBigDec", BigDecimal.TEN);
        assertEquals("field:fBigDec", gen.recordedEvents.get(14));
        assertEquals("number:bigdec:10", gen.recordedEvents.get(15));

        gen.writeBinaryField("fBin", new byte[] { 0x0A });
        assertEquals("field:fBin", gen.recordedEvents.get(16));
        assertEquals("binary:1bytes", gen.recordedEvents.get(17));

        gen.writeArrayFieldStart("fArray");
        assertEquals("field:fArray", gen.recordedEvents.get(18));
        assertEquals("startArray", gen.recordedEvents.get(19));

        gen.writeObjectFieldStart("fObj");
        assertEquals("field:fObj", gen.recordedEvents.get(20));
        assertEquals("startObject", gen.recordedEvents.get(21));

        gen.writeObjectField("fPojo", "pojoVal");
        assertEquals("field:fPojo", gen.recordedEvents.get(22));
        assertEquals("object:pojoVal", gen.recordedEvents.get(23));

        gen.writeFieldId(12345L);
        assertEquals("field:12345", gen.recordedEvents.get(24));

        gen.writeOmittedField("skipped"); // default does nothing
        assertEquals(25, gen.recordedEvents.size());
    }

    @Test(timeout = 4000)
    public void testDelegatedWritesAndDefaults() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();

        gen.writeStartArray(5);
        assertEquals("startArray", gen.recordedEvents.get(0));

        gen.writeNumber((short) 42);
        assertEquals("number:int:42", gen.recordedEvents.get(1));

        gen.writeRaw(new SerializedString("rawSerializable"));
        assertEquals("raw:rawSerializable", gen.recordedEvents.get(2));

        gen.writeRawValue(new SerializedString("rawValueSerializable"));
        assertEquals("rawValue:rawValueSerializable", gen.recordedEvents.get(3));

        gen.writeBinary(new byte[] { 1, 2, 3 });
        assertEquals("binary:3bytes", gen.recordedEvents.get(4));

        gen.writeBinary(new byte[] { 1, 2, 3 }, 0, 2);
        assertEquals("binary:2bytes", gen.recordedEvents.get(5));

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 1 });
        gen.writeBinary(in, 1);
        assertEquals("binaryStream:1", gen.recordedEvents.get(6));

        assertNull(gen.getOutputTarget());
        assertEquals(-1, gen.getOutputBuffered());
        assertFalse(gen.canUseSchema(null));
        assertFalse(gen.canWriteObjectId());
        assertFalse(gen.canWriteTypeId());
        assertFalse(gen.canWriteBinaryNatively());
        assertTrue(gen.canOmitFields());
        assertFalse(gen.canWriteFormattedNumbers());
        assertEquals(0, gen.getFormatFeatures());
        assertNull(gen.getSchema());
        assertNull(gen.getCharacterEscapes());
        assertEquals(gen, gen.setCharacterEscapes(null));
        assertEquals(0, gen.getHighestEscapedChar());
        assertEquals(gen, gen.setHighestNonEscapedChar(127));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterConfiguration() {
        MockJsonGenerator gen = new MockJsonGenerator();
        assertNull(gen.getPrettyPrinter());
        PrettyPrinter pp = new PrettyPrinter() {
            @Override
            public void writeRootValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeStartObject(JsonGenerator gen) throws IOException {}
            @Override
            public void writeEndObject(JsonGenerator gen, int nrOfValues) throws IOException {}
            @Override
            public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeStartArray(JsonGenerator gen) throws IOException {}
            @Override
            public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {}
            @Override
            public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void beforeArrayValues(JsonGenerator gen) throws IOException {}
            @Override
            public void beforeObjectEntries(JsonGenerator gen) throws IOException {}
        };
        gen.setPrettyPrinter(pp);
        assertSame(pp, gen.getPrettyPrinter());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayIntNull() throws Exception {
        new MockJsonGenerator().writeArray((int[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayLongNull() throws Exception {
        new MockJsonGenerator().writeArray((long[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayDoubleNull() throws Exception {
        new MockJsonGenerator().writeArray((double[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayIntNegativeOffset() throws Exception {
        new MockJsonGenerator().writeArray(new int[5], -1, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayIntOverflowLength() throws Exception {
        new MockJsonGenerator().writeArray(new int[5], 2, 4);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayLongNegativeOffset() throws Exception {
        new MockJsonGenerator().writeArray(new long[5], -1, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayLongOverflowLength() throws Exception {
        new MockJsonGenerator().writeArray(new long[5], 3, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayDoubleNegativeOffset() throws Exception {
        new MockJsonGenerator().writeArray(new double[5], -1, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWriteArrayDoubleOverflowLength() throws Exception {
        new MockJsonGenerator().writeArray(new double[5], 4, 2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOverrideFormatFeaturesThrows() {
        new MockJsonGenerator().overrideFormatFeatures(1, 1);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetSchemaThrows() {
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "TEST_SCHEMA";
            }
        };
        new MockJsonGenerator().setSchema(schema);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetRootValueSeparatorThrows() {
        new MockJsonGenerator().setRootValueSeparator(new SerializedString("/"));
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteObjectIdThrows() throws Exception {
        new MockJsonGenerator().writeObjectId("id-123");
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteObjectRefThrows() throws Exception {
        new MockJsonGenerator().writeObjectRef("id-123");
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteTypeIdThrows() throws Exception {
        new MockJsonGenerator().writeTypeId("type-123");
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testReportError() throws Exception {
        new MockJsonGenerator().publicReportError("Boom");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testReportUnsupportedOperation() {
        new MockJsonGenerator().publicReportUnsupportedOperation();
    }

    // =========================================================================
    // Partition E: Protected _writeSimpleObject Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteSimpleObjectComprehensive() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();

        gen.publicWriteSimpleObject(null);
        assertEquals("null", gen.recordedEvents.get(0));

        gen.publicWriteSimpleObject("hello");
        assertEquals("string:hello", gen.recordedEvents.get(1));

        gen.publicWriteSimpleObject(Integer.valueOf(1));
        assertEquals("number:int:1", gen.recordedEvents.get(2));

        gen.publicWriteSimpleObject(Long.valueOf(2L));
        assertEquals("number:long:2", gen.recordedEvents.get(3));

        gen.publicWriteSimpleObject(Double.valueOf(3.5));
        assertEquals("number:double:3.5", gen.recordedEvents.get(4));

        gen.publicWriteSimpleObject(Float.valueOf(4.5f));
        assertEquals("number:float:4.5", gen.recordedEvents.get(5));

        gen.publicWriteSimpleObject(Short.valueOf((short) 5));
        assertEquals("number:int:5", gen.recordedEvents.get(6));

        gen.publicWriteSimpleObject(Byte.valueOf((byte) 6));
        assertEquals("number:int:6", gen.recordedEvents.get(7));

        gen.publicWriteSimpleObject(BigInteger.valueOf(7L));
        assertEquals("number:bigint:7", gen.recordedEvents.get(8));

        gen.publicWriteSimpleObject(BigDecimal.valueOf(8.5));
        assertEquals("number:bigdec:8.5", gen.recordedEvents.get(9));

        gen.publicWriteSimpleObject(new AtomicInteger(9));
        assertEquals("number:int:9", gen.recordedEvents.get(10));

        gen.publicWriteSimpleObject(new AtomicLong(10L));
        assertEquals("number:long:10", gen.recordedEvents.get(11));

        gen.publicWriteSimpleObject(new byte[] { 1, 2 });
        assertEquals("binary:2bytes", gen.recordedEvents.get(12));

        gen.publicWriteSimpleObject(Boolean.TRUE);
        assertEquals("boolean:true", gen.recordedEvents.get(13));

        gen.publicWriteSimpleObject(new AtomicBoolean(false));
        assertEquals("boolean:false", gen.recordedEvents.get(14));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWriteSimpleObjectUnsupportedType() throws Exception {
        new MockJsonGenerator().publicWriteSimpleObject(new Thread());
    }

    // =========================================================================
    // Partition F: copyCurrentEvent & copyCurrentStructure
    // =========================================================================

    @Test(timeout = 4000)
    public void testCopyCurrentEventAllTokens() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        StubJsonParser p = new StubJsonParser();

        // 1. START_OBJECT
        p.addToken(JsonToken.START_OBJECT);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("startObject", gen.recordedEvents.get(0));

        // 2. END_OBJECT
        p.addToken(JsonToken.END_OBJECT);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("endObject", gen.recordedEvents.get(1));

        // 3. START_ARRAY
        p.addToken(JsonToken.START_ARRAY);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("startArray", gen.recordedEvents.get(2));

        // 4. END_ARRAY
        p.addToken(JsonToken.END_ARRAY);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("endArray", gen.recordedEvents.get(3));

        // 5. FIELD_NAME
        p.addToken(JsonToken.FIELD_NAME);
        p.nextToken();
        p.overrideCurrentName("customField");
        gen.copyCurrentEvent(p);
        assertEquals("field:customField", gen.recordedEvents.get(4));

        // 6. STRING with text characters
        p.addToken(JsonToken.VALUE_STRING);
        p.nextToken();
        p.textChars = "customText".toCharArray();
        p.textOffset = 0;
        p.textLen = 10;
        gen.copyCurrentEvent(p);
        assertEquals("stringChars:customText", gen.recordedEvents.get(5));

        // 7. STRING without text characters
        p.addToken(JsonToken.VALUE_STRING);
        p.nextToken();
        p.textChars = null;
        p.text = "plainText";
        gen.copyCurrentEvent(p);
        assertEquals("string:plainText", gen.recordedEvents.get(6));

        // 8. NUMBER INT (INT, BIG_INTEGER, LONG)
        p.addToken(JsonToken.VALUE_NUMBER_INT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.INT;
        p.intVal = 42;
        gen.copyCurrentEvent(p);
        assertEquals("number:int:42", gen.recordedEvents.get(7));

        p.addToken(JsonToken.VALUE_NUMBER_INT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.BIG_INTEGER;
        p.bigIntVal = BigInteger.valueOf(999);
        gen.copyCurrentEvent(p);
        assertEquals("number:bigint:999", gen.recordedEvents.get(8));

        p.addToken(JsonToken.VALUE_NUMBER_INT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.LONG;
        p.longVal = 8888L;
        gen.copyCurrentEvent(p);
        assertEquals("number:long:8888", gen.recordedEvents.get(9));

        // 9. NUMBER FLOAT (BIG_DECIMAL, FLOAT, DOUBLE)
        p.addToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.BIG_DECIMAL;
        p.bigDecVal = BigDecimal.valueOf(1.23);
        gen.copyCurrentEvent(p);
        assertEquals("number:bigdec:1.23", gen.recordedEvents.get(10));

        p.addToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.FLOAT;
        p.floatVal = 2.5f;
        gen.copyCurrentEvent(p);
        assertEquals("number:float:2.5", gen.recordedEvents.get(11));

        p.addToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.nextToken();
        p.numberType = JsonParser.NumberType.DOUBLE;
        p.doubleVal = 3.1415;
        gen.copyCurrentEvent(p);
        assertEquals("number:double:3.1415", gen.recordedEvents.get(12));

        // 10. BOOLEAN & NULL
        p.addToken(JsonToken.VALUE_TRUE);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("boolean:true", gen.recordedEvents.get(13));

        p.addToken(JsonToken.VALUE_FALSE);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("boolean:false", gen.recordedEvents.get(14));

        p.addToken(JsonToken.VALUE_NULL);
        p.nextToken();
        gen.copyCurrentEvent(p);
        assertEquals("null", gen.recordedEvents.get(15));

        // 11. EMBEDDED_OBJECT
        p.addToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.nextToken();
        p.embeddedObj = "embeddedData";
        gen.copyCurrentEvent(p);
        assertEquals("object:embeddedData", gen.recordedEvents.get(16));
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testCopyCurrentEventNullThrows() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        StubJsonParser p = new StubJsonParser();
        gen.copyCurrentEvent(p);
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testCopyCurrentEventNotAvailableThrows() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        StubJsonParser p = new StubJsonParser();
        p.addToken(JsonToken.NOT_AVAILABLE);
        p.nextToken();
        gen.copyCurrentEvent(p);
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructureNested() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        StubJsonParser p = new StubJsonParser();

        p.addToken(JsonToken.START_OBJECT);
        p.addToken(JsonToken.FIELD_NAME);
        p.addToken(JsonToken.START_ARRAY);
        p.addToken(JsonToken.VALUE_TRUE);
        p.addToken(JsonToken.END_ARRAY);
        p.addToken(JsonToken.END_OBJECT);

        p.nextToken(); // points to START_OBJECT
        p.overrideCurrentName("arrField");

        gen.copyCurrentStructure(p);

        assertEquals("startObject", gen.recordedEvents.get(0));
        assertEquals("field:arrField", gen.recordedEvents.get(1));
        assertEquals("startArray", gen.recordedEvents.get(2));
        assertEquals("boolean:true", gen.recordedEvents.get(3));
        assertEquals("endArray", gen.recordedEvents.get(4));
        assertEquals("endObject", gen.recordedEvents.get(5));
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructureArrayRoot() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        StubJsonParser p = new StubJsonParser();

        p.addToken(JsonToken.START_ARRAY);
        p.addToken(JsonToken.VALUE_FALSE);
        p.addToken(JsonToken.END_ARRAY);

        p.nextToken(); // points to START_ARRAY
        gen.copyCurrentStructure(p);

        assertEquals("startArray", gen.recordedEvents.get(0));
        assertEquals("boolean:false", gen.recordedEvents.get(1));
        assertEquals("endArray", gen.recordedEvents.get(2));
    }

    @Test(timeout = 4000)
    public void testLifecycleAndFlushClose() throws Exception {
        MockJsonGenerator gen = new MockJsonGenerator();
        assertFalse(gen.isClosed());
        gen.flush();
        assertTrue(gen.recordedEvents.contains("flush"));

        gen.close();
        assertTrue(gen.isClosed());
        assertTrue(gen.recordedEvents.contains("close"));
    }
}