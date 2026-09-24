package com.fasterxml.jackson.databind.ser.std;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: com.fasterxml.jackson.databind.ser.std.NumberSerializer
 *
 * Branches & Logic Targeted:
 * 1. Constructor:
 *    - rawType == BigInteger.class -> _isInt = true
 *    - rawType != BigInteger.class (BigDecimal, Number, Long, etc.) -> _isInt = false
 * 2. createContextual(SerializerProvider, BeanProperty):
 *    - format == null -> returns this
 *    - format != null, shape != STRING -> returns this
 *    - format != null, shape == STRING:
 *      * For BigDecimal: Defect target (Jackson issue #2264 / PR #2230) where plain
 *        string representation ("0.0000000005") vs scientific notation ("5E-10") is tested.
 *      * For other Number types (e.g., BigInteger) -> returns ToStringSerializer.
 * 3. serialize(Number, JsonGenerator, SerializerProvider):
 *    - value instanceof BigDecimal -> g.writeNumber(BigDecimal)
 *    - value instanceof BigInteger -> g.writeNumber(BigInteger)
 *    - value instanceof Long -> g.writeNumber(long)
 *    - value instanceof Double -> g.writeNumber(double)
 *    - value instanceof Float -> g.writeNumber(float)
 *    - value instanceof Integer -> g.writeNumber(int)
 *    - value instanceof Byte -> g.writeNumber(int)
 *    - value instanceof Short -> g.writeNumber(int)
 *    - fallback (e.g., custom Number, AtomicInteger, AtomicLong) -> g.writeNumber(value.toString())
 * 4. getSchema(SerializerProvider, Type):
 *    - _isInt == true -> createSchemaNode("integer", true)
 *    - _isInt == false -> createSchemaNode("number", true)
 * 5. acceptJsonFormatVisitor(JsonFormatVisitorWrapper, JavaType):
 *    - _isInt == true -> visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER)
 *    - _isInt == false && handledType == BigDecimal.class -> visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL)
 *    - _isInt == false && handledType != BigDecimal.class -> visitor.expectNumberFormat(typeHint)
 */
public class NumberSerializerGeminiTest {

    private final JsonFactory jsonFactory = new JsonFactory();

    // Custom test POJO to trigger defect in BigDecimal formatting with Shape.STRING
    static class BigDecimalPlainWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal value;

        public BigDecimalPlainWrapper() { }

        public BigDecimalPlainWrapper(BigDecimal value) {
            this.value = value;
        }
    }

    // Custom Number subclass to exercise fallback untyped branch
    static class CustomNumber extends Number {
        private static final long serialVersionUID = 1L;
        private final String representation;

        public CustomNumber(String rep) {
            this.representation = rep;
        }

        @Override
        public int intValue() { return 42; }

        @Override
        public long longValue() { return 42L; }

        @Override
        public float floatValue() { return 42.0f; }

        @Override
        public double doubleValue() { return 42.0; }

        @Override
        public String toString() {
            return representation;
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson #2264 / Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Ground Truth Test:
     * com.fasterxml.jackson.databind.ser.jdk.BigDecimalPlain2230Test::testBigIntegerAsPlainTest
     * Verifies that a BigDecimal with small magnitude serialized as STRING outputs the plain string
     * representation ("0.0000000005") instead of scientific notation ("5E-10").
     */
    @Test(timeout = 4000)
    public void testBigDecimalAsStringPlainFormatDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal smallNumber = new BigDecimal("0.0000000005");
        BigDecimalPlainWrapper wrapper = new BigDecimalPlainWrapper(smallNumber);

        String json = mapper.writeValueAsString(wrapper);
        assertEquals("{\"value\":\"0.0000000005\"}", json);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (serialize branches)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeBigDecimal() throws IOException {
        NumberSerializer ser = new NumberSerializer(BigDecimal.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        BigDecimal bd = new BigDecimal("12345.6789");
        ser.serialize(bd, gen, null);
        gen.flush();

        assertEquals("12345.6789", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeBigInteger() throws IOException {
        NumberSerializer ser = new NumberSerializer(BigInteger.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        BigInteger bi = new BigInteger("9876543210987654321");
        ser.serialize(bi, gen, null);
        gen.flush();

        assertEquals("9876543210987654321", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeLong() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        ser.serialize(123456789012345L, gen, null);
        gen.flush();

        assertEquals("123456789012345", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeDouble() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        ser.serialize(3.14159265, gen, null);
        gen.flush();

        assertEquals("3.14159265", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeFloat() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        ser.serialize(1.5f, gen, null);
        gen.flush();

        assertEquals("1.5", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeIntegerShortByte() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;

        // Integer
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = jsonFactory.createGenerator(sw1);
        ser.serialize(Integer.valueOf(100), gen1, null);
        gen1.flush();
        assertEquals("100", sw1.toString());

        // Short
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = jsonFactory.createGenerator(sw2);
        ser.serialize(Short.valueOf((short) 50), gen2, null);
        gen2.flush();
        assertEquals("50", sw2.toString());

        // Byte
        StringWriter sw3 = new StringWriter();
        JsonGenerator gen3 = jsonFactory.createGenerator(sw3);
        ser.serialize(Byte.valueOf((byte) 12), gen3, null);
        gen3.flush();
        assertEquals("12", sw3.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeCustomNumberFallback() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        CustomNumber customNum = new CustomNumber("9999999999999999999999999999");
        ser.serialize(customNum, gen, null);
        gen.flush();

        assertEquals("9999999999999999999999999999", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAtomicIntegerAndLong() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;

        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = jsonFactory.createGenerator(sw1);
        ser.serialize(new AtomicInteger(1234), gen1, null);
        gen1.flush();
        assertEquals("1234", sw1.toString());

        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = jsonFactory.createGenerator(sw2);
        ser.serialize(new AtomicLong(5678L), gen2, null);
        gen2.flush();
        assertEquals("5678", sw2.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeBoundaryValues() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;

        // Long Min / Max
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize(Long.MIN_VALUE, gen, null);
        ser.serialize(Long.MAX_VALUE, gen, null);
        // Integer Min / Max
        ser.serialize(Integer.MIN_VALUE, gen, null);
        ser.serialize(Integer.MAX_VALUE, gen, null);
        // Zero
        ser.serialize(0, gen, null);
        ser.serialize(0.0, gen, null);
        gen.flush();

        String expected = Long.MIN_VALUE + "" + Long.MAX_VALUE + ""
                + Integer.MIN_VALUE + "" + Integer.MAX_VALUE + "0" + "0.0";
        assertEquals(expected, sw.toString());
    }

    // =========================================================================
    // Partition D: Schema & Visitor Introspection
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSchemaForBigInteger() {
        NumberSerializer ser = new NumberSerializer(BigInteger.class);
        JsonNode schema = ser.getSchema(null, null);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaForNonInteger() {
        NumberSerializer serBigDecimal = new NumberSerializer(BigDecimal.class);
        JsonNode schemaBD = serBigDecimal.getSchema(null, null);
        assertNotNull(schemaBD);
        assertEquals("number", schemaBD.get("type").asText());

        NumberSerializer serDefault = NumberSerializer.instance;
        JsonNode schemaDef = serDefault.getSchema(null, null);
        assertNotNull(schemaDef);
        assertEquals("number", schemaDef.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorBigInteger() throws JsonMappingException {
        NumberSerializer ser = new NumberSerializer(BigInteger.class);
        final boolean[] visitedInt = new boolean[1];
        final JsonParser.NumberType[] numberTypeHolder = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visitedInt[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        numberTypeHolder[0] = type;
                    }
                };
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(BigInteger.class);
        ser.acceptJsonFormatVisitor(visitor, type);

        assertTrue("expectIntegerFormat should have been called", visitedInt[0]);
        assertEquals(JsonParser.NumberType.BIG_INTEGER, numberTypeHolder[0]);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorBigDecimal() throws JsonMappingException {
        NumberSerializer ser = new NumberSerializer(BigDecimal.class);
        final boolean[] visitedNumber = new boolean[1];
        final JsonParser.NumberType[] numberTypeHolder = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visitedNumber[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        numberTypeHolder[0] = type;
                    }
                };
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(BigDecimal.class);
        ser.acceptJsonFormatVisitor(visitor, type);

        assertTrue("expectNumberFormat should have been called for BigDecimal", visitedNumber[0]);
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, numberTypeHolder[0]);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorGenericNumber() throws JsonMappingException {
        NumberSerializer ser = NumberSerializer.instance; // handledType is Number.class
        final boolean[] visitedNumber = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visitedNumber[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(Number.class);
        ser.acceptJsonFormatVisitor(visitor, type);

        assertTrue("expectNumberFormat fallback should have been called for generic Number", visitedNumber[0]);
    }

    // =========================================================================
    // Partition E: Contextual Resolution & Shape Configuration
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateContextualWithoutFormatOverridesReturnsThis() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        NumberSerializer ser = new NumberSerializer(BigInteger.class);

        JsonSerializer<?> contextual = ser.createContextual(prov, null);
        assertSame("Without property or format overrides, createContextual should return this", ser, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualShapeNumberReturnsThis() throws Exception {
        class PojoWithNumberShape {
            @JsonFormat(shape = JsonFormat.Shape.NUMBER)
            public BigInteger num = BigInteger.TEN;
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PojoWithNumberShape());
        assertEquals("{\"num\":10}", json);
    }

    @Test(timeout = 4000)
    public void testCreateContextualShapeStringForBigInteger() throws Exception {
        class PojoWithStringBigInteger {
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            public BigInteger bigInt = new BigInteger("12345678901234567890");
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PojoWithStringBigInteger());
        assertEquals("{\"bigInt\":\"12345678901234567890\"}", json);
    }

    @Test(timeout = 4000)
    public void testInstanceSingletonNotNull() {
        assertNotNull(NumberSerializer.instance);
        assertEquals(Number.class, NumberSerializer.instance.handledType());
    }
}