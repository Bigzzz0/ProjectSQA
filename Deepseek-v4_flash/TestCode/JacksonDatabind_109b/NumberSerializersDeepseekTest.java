package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.Base;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.DoubleSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.FloatSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntLikeSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntegerSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.LongSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.ShortSerializer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target defect: BigDecimalPlain2230Test::testBigIntegerAsPlainTest
 * Expected: {"value":"0.0000000005"} but got: {"value":"5E-10"}
 * Root cause: BigInteger serialization uses scientific notation instead of plain string.
 * 
 * Branches targeted:
 * 1. Base constructor: _isInt = (INT || LONG || BIG_INTEGER) - all three true/false paths
 * 2. getSchema: returns createSchemaNode with correct type
 * 3. acceptJsonFormatVisitor: _isInt true/false branches
 * 4. createContextual: format != null && shape==STRING -> ToStringSerializer, else this
 * 5. serialize methods: each concrete serializer's cast and writeNumber call
 * 6. serializeWithType: delegates to serialize (no type info)
 * 7. addAll: registers all 12 type mappings
 * 
 * Boundary values:
 * - Integer: MIN_VALUE, MAX_VALUE, 0, 1, -1
 * - Long: MIN_VALUE, MAX_VALUE, 0
 * - Short: MIN_VALUE, MAX_VALUE, 0
 * - Float: 0.0f, -0.0f, Float.MAX_VALUE, Float.MIN_VALUE
 * - Double: 0.0, -0.0, Double.MAX_VALUE, Double.MIN_VALUE
 * - BigInteger: 0, 1, -1, MAX_VALUE, MIN_VALUE, 5E-10 (defect trigger)
 * - BigDecimal: 0.0000000005 (defect trigger)
 * 
 * Exception paths: null values, wrong types (ClassCastException)
 */
public class NumberSerializersDeepseekTest {

    /* ==================== Partition A: Core Functional Logic ==================== */

    @Test(timeout = 4000)
    public void testAddAllRegistersAllTypes() {
        Map<String, JsonSerializer<?>> map = new HashMap<>();
        NumberSerializers.addAll(map);
        
        assertEquals(12, map.size());
        assertTrue(map.containsKey(Integer.class.getName()));
        assertTrue(map.containsKey(Integer.TYPE.getName()));
        assertTrue(map.containsKey(Long.class.getName()));
        assertTrue(map.containsKey(Long.TYPE.getName()));
        assertTrue(map.containsKey(Byte.class.getName()));
        assertTrue(map.containsKey(Byte.TYPE.getName()));
        assertTrue(map.containsKey(Short.class.getName()));
        assertTrue(map.containsKey(Short.TYPE.getName()));
        assertTrue(map.containsKey(Double.class.getName()));
        assertTrue(map.containsKey(Double.TYPE.getName()));
        assertTrue(map.containsKey(Float.class.getName()));
        assertTrue(map.containsKey(Float.TYPE.getName()));
        
        assertTrue(map.get(Integer.class.getName()) instanceof IntegerSerializer);
        assertTrue(map.get(Long.class.getName()) instanceof LongSerializer);
        assertTrue(map.get(Byte.class.getName()) instanceof IntLikeSerializer);
        assertTrue(map.get(Short.class.getName()) instanceof ShortSerializer);
        assertTrue(map.get(Double.class.getName()) instanceof DoubleSerializer);
        assertTrue(map.get(Float.class.getName()) instanceof FloatSerializer);
    }

    @Test(timeout = 4000)
    public void testBaseConstructorIsIntFlag() {
        // INT type
        Base<Object> intBase = new Base<Object>(Integer.class, JsonParser.NumberType.INT, "integer") {};
        assertTrue(intBase._isInt);
        
        // LONG type
        Base<Object> longBase = new Base<Object>(Long.class, JsonParser.NumberType.LONG, "number") {};
        assertTrue(longBase._isInt);
        
        // BIG_INTEGER type
        Base<Object> bigIntBase = new Base<Object>(BigInteger.class, JsonParser.NumberType.BIG_INTEGER, "number") {};
        assertTrue(bigIntBase._isInt);
        
        // FLOAT type
        Base<Object> floatBase = new Base<Object>(Float.class, JsonParser.NumberType.FLOAT, "number") {};
        assertFalse(floatBase._isInt);
        
        // DOUBLE type
        Base<Object> doubleBase = new Base<Object>(Double.class, JsonParser.NumberType.DOUBLE, "number") {};
        assertFalse(doubleBase._isInt);
        
        // Unknown type
        Base<Object> unknownBase = new Base<Object>(String.class, null, "string") {};
        assertFalse(unknownBase._isInt);
    }

    @Test(timeout = 4000)
    public void testGetSchema() {
        IntegerSerializer intSer = new IntegerSerializer(Integer.class);
        JsonNode schema = intSer.getSchema(null, null);
        assertEquals("integer", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
        
        DoubleSerializer doubleSer = new DoubleSerializer(Double.class);
        schema = doubleSer.getSchema(null, null);
        assertEquals("number", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
        
        ShortSerializer shortSer = new ShortSerializer();
        schema = shortSer.getSchema(null, null);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorInt() throws Exception {
        IntegerSerializer ser = new IntegerSerializer(Integer.class);
        final boolean[] visitedInt = {false};
        final boolean[] visitedFloat = {false};
        
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper() {
            @Override
            public JsonSerializer<?> getSerializerProvider() { return null; }
            @Override
            public void expectStringFormat(JavaType type) { }
            @Override
            public void expectNumberFormat(JavaType type) { }
            @Override
            public void expectIntegerFormat(JavaType type) { visitedInt[0] = true; }
            @Override
            public void expectBooleanFormat(JavaType type) { }
            @Override
            public void expectArrayFormat(JavaType type) { }
            @Override
            public void expectObjectFormat(JavaType type) { }
            @Override
            public void expectNullFormat(JavaType type) { }
            @Override
            public void expectAnyFormat(JavaType type) { }
        };
        
        ser.acceptJsonFormatVisitor(visitor, null);
        assertTrue(visitedInt[0]);
        assertFalse(visitedFloat[0]);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorFloat() throws Exception {
        DoubleSerializer ser = new DoubleSerializer(Double.class);
        final boolean[] visitedInt = {false};
        final boolean[] visitedFloat = {false};
        
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper() {
            @Override
            public JsonSerializer<?> getSerializerProvider() { return null; }
            @Override
            public void expectStringFormat(JavaType type) { }
            @Override
            public void expectNumberFormat(JavaType type) { }
            @Override
            public void expectIntegerFormat(JavaType type) { visitedInt[0] = true; }
            @Override
            public void expectBooleanFormat(JavaType type) { }
            @Override
            public void expectArrayFormat(JavaType type) { }
            @Override
            public void expectObjectFormat(JavaType type) { }
            @Override
            public void expectNullFormat(JavaType type) { }
            @Override
            public void expectAnyFormat(JavaType type) { }
        };
        
        ser.acceptJsonFormatVisitor(visitor, null);
        assertFalse(visitedInt[0]);
        assertTrue(visitedFloat[0]);
    }

    /* ==================== Partition B: Boundary Value Analysis ==================== */

    @Test(timeout = 4000)
    public void testIntegerSerializerBoundaries() throws Exception {
        IntegerSerializer ser = new IntegerSerializer(Integer.class);
        
        // Test with mock generator
        final StringBuilder sb = new StringBuilder();
        JsonGenerator gen = new JsonGenerator() {
            @Override public void writeNumber(int v) { sb.append("int:").append(v); }
            @Override public void writeNumber(long v) { sb.append("long:").append(v); }
            @Override public void writeNumber(double v) { sb.append("double:").append(v); }
            @Override public void writeNumber(float v) { sb.append("float:").append(v); }
            @Override public void writeNumber(BigDecimal v) { sb.append("bigdec:").append(v); }
            @Override public void writeNumber(BigInteger v) { sb.append("bigint:").append(v); }
            @Override public void writeNumber(String v) { sb.append("str:").append(v); }
            @Override public void writeNumber(char[] v, int s, int l) { }
            @Override public void writeNumber(short v) { sb.append("short:").append(v); }
            @Override public void writeString(String v) { sb.append("str:").append(v); }
            @Override public void writeString(char[] v, int s, int l) { }
            @Override public void writeString(char v) { }
            @Override public void writeRaw(String v) { }
            @Override public void writeRaw(String v, int s, int l) { }
            @Override public void writeRaw(char[] v, int s, int l) { }
            @Override public void writeRaw(char v) { }
            @Override public void writeRawValue(String v) { }
            @Override public void writeRawValue(String v, int s, int l) { }
            @Override public void writeRawValue(char[] v, int s, int l) { }
            @Override public void writeRawValue(char v) { }
            @Override public void writeBinary(Base64Variant b, byte[] d, int s, int l) { }
            @Override public void writeBinary(byte[] d, int s, int l) { }
            @Override public void writeBoolean(boolean v) { }
            @Override public void writeNull() { }
            @Override public void writeStartArray() { }
            @Override public void writeEndArray() { }
            @Override public void writeStartObject() { }
            @Override public void writeEndObject() { }
            @Override public void writeFieldName(String v) { }
            @Override public void writeFieldName(SerializableString v) { }
            @Override public void writeString(SerializableString v) { }
            @Override public void writeObject(Object v) { }
            @Override public void writeTree(TreeNode v) { }
            @Override public JsonParser getOutputTarget() { return null; }
            @Override public void flush() { }
            @Override public boolean isClosed() { return false; }
            @Override public JsonGenerator setCodec(ObjectCodec c) { return this; }
            @Override public ObjectCodec getCodec() { return null; }
            @Override public JsonStreamContext getOutputContext() { return null; }
            @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
            @Override public JsonGenerator setSchema(FormatSchema s) { return this; }
            @Override public FormatSchema getSchema() { return null; }
            @Override public Version version() { return null; }
            @Override public Object getFeatureMask() { return null; }
            @Override public JsonGenerator setFeatureMask(long m) { return this; }
            @Override public JsonGenerator enable(Feature f) { return this; }
            @Override public JsonGenerator disable(Feature f) { return this; }
            @Override public boolean isEnabled(Feature f) { return false; }
            @Override public int getFormatFeatures() { return 0; }
            @Override public JsonGenerator setFormatFeatures(int m) { return this; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public PrettyPrinter getPrettyPrinter() { return null; }
            @Override public JsonGenerator setHighestEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public CharacterEscapes getCharacterEscapes() { return null; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter p) { return this; }
            @Override public JsonGenerator setHighestNonEscapedCharCode(int c) { return this; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes e) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString s) { return this; }
            @Override public JsonGenerator setSerializationFeature(SerializationFeature f) { return this; }
            @Override public JsonGenerator setSerializationFeatures(SerializationFeature... f) { return this; }
            @Override public JsonGenerator disable(SerializationFeature f) { return this; }
            @Override public JsonGenerator enable(SerializationFeature f) { return this; }
            @Override public boolean isEnabled(SerializationFeature f) { return false; }
            @Override public int getSerializationFeatures() { return 0;