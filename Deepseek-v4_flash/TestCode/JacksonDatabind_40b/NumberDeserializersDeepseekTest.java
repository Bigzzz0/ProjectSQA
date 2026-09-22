package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DeserializationContexts;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

/**
 * White-box test suite for NumberDeserializers, targeting full branch coverage
 * and the known defect [databind#1095] where empty string coercion to null
 * for primitive types was incorrectly allowed.
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: find() method (primitive, wrapper, Number, BigDecimal, BigInteger, unrecognized)
 * - Partition B: getNullValue() in PrimitiveOrWrapperDeserializer (primitive vs wrapper, FAIL_ON_NULL_FOR_PRIMITIVES)
 * - Partition C: deserialize() for each concrete deserializer:
 *     * IntegerDeserializer: VALUE_NUMBER_INT, VALUE_STRING, START_ARRAY (unwrap)
 *     * LongDeserializer: similar
 *     * BooleanDeserializer: VALUE_TRUE/FALSE, VALUE_STRING ("true"/"false"/"")
 *     * DoubleDeserializer: VALUE_NUMBER_FLOAT, VALUE_STRING
 *     * FloatDeserializer: similar
 *     * ByteDeserializer: VALUE_NUMBER_INT, VALUE_STRING
 *     * ShortDeserializer: similar
 *     * CharacterDeserializer: VALUE_NUMBER_INT, VALUE_STRING (single char, empty)
 *     * NumberDeserializer: all token types, including inf/nan/empty string
 *     * BigDecimalDeserializer: VALUE_NUMBER_INT/FLOAT, VALUE_STRING
 *     * BigIntegerDeserializer: VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT (if ACCEPT_FLOAT_AS_INT), VALUE_STRING
 * - Partition D: deserializeWithType() on Integer, Long, Boolean, Double (natural types)
 * - Partition E: Defect targeted: empty string "" for primitive types (int, boolean, long, double)
 *     should throw JsonMappingException; the bug erroneously allows coercion to default value.
 * - Partition F: UNWRAP_SINGLE_VALUE_ARRAYS for all types
 * - Partition G: Boundary values: Integer.MAX_VALUE, Long.MAX_VALUE, Double.NaN, etc.
 */
public class NumberDeserializersDeepseekTest {

    // ---- Partition A: find() method ----
    @Test(timeout = 4000)
    public void testFindPrimitiveTypes() {
        assertNotNull("int primitive", NumberDeserializers.find(int.class, "int"));
        assertNotNull("boolean primitive", NumberDeserializers.find(boolean.class, "boolean"));
        assertNotNull("long primitive", NumberDeserializers.find(long.class, "long"));
        assertNotNull("double primitive", NumberDeserializers.find(double.class, "double"));
        assertNotNull("char primitive", NumberDeserializers.find(char.class, "char"));
        assertNotNull("byte primitive", NumberDeserializers.find(byte.class, "byte"));
        assertNotNull("short primitive", NumberDeserializers.find(short.class, "short"));
        assertNotNull("float primitive", NumberDeserializers.find(float.class, "float"));
    }

    @Test(timeout = 4000)
    public void testFindWrapperTypes() {
        assertNotNull("Integer wrapper", NumberDeserializers.find(Integer.class, "java.lang.Integer"));
        assertNotNull("Boolean wrapper", NumberDeserializers.find(Boolean.class, "java.lang.Boolean"));
        assertNotNull("Long wrapper", NumberDeserializers.find(Long.class, "java.lang.Long"));
        assertNotNull("Double wrapper", NumberDeserializers.find(Double.class, "java.lang.Double"));
        assertNotNull("Character wrapper", NumberDeserializers.find(Character.class, "java.lang.Character"));
        assertNotNull("Byte wrapper", NumberDeserializers.find(Byte.class, "java.lang.Byte"));
        assertNotNull("Short wrapper", NumberDeserializers.find(Short.class, "java.lang.Short"));
        assertNotNull("Float wrapper", NumberDeserializers.find(Float.class, "java.lang.Float"));
        assertNotNull("Number", NumberDeserializers.find(Number.class, "java.lang.Number"));
        assertNotNull("BigDecimal", NumberDeserializers.find(BigDecimal.class, "java.math.BigDecimal"));
        assertNotNull("BigInteger", NumberDeserializers.find(BigInteger.class, "java.math.BigInteger"));
    }

    @Test(timeout = 4000)
    public void testFindUnrecognizedReturnsNull() {
        assertNull("Unknown class", NumberDeserializers.find(String.class, "java.lang.String"));
    }

    // ---- Partition B: getNullValue() ----
    @Test(timeout = 4000)
    public void testGetNullValuePrimitiveWithFailOnNull() throws Exception {
        // Test that for primitive int, getNullValue throws when FAIL_ON_NULL_FOR_PRIMITIVES is enabled
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        // Need to call getNullValue with context (non-deprecated)
        try {
            deser.getNullValue(ctxt);
            fail("Expected JsonMappingException for primitive int with FAIL_ON_NULL_FOR_PRIMITIVES");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetNullValueWrapper() throws Exception {
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.wrapperInstance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertNull("Wrapper getNullValue returns null", deser.getNullValue(ctxt));
    }

    // ---- Partition C: deserialize() for each type ----
    // Use ObjectMapper to exercise deserializers via public API

    // IntegerDeserializer
    @Test(timeout = 4000)
    public void testIntegerDeserializerNumberInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Integer result = mapper.readValue("42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test(timeout = 4000)
    public void testIntegerDeserializerString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Integer result = mapper.readValue("\"123\"", Integer.class);
        assertEquals(Integer.valueOf(123), result);
    }

    @Test(timeout = 4000)
    public void testIntegerDeserializerUnwrapArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Integer result = mapper.readValue("[99]", Integer.class);
        assertEquals(Integer.valueOf(99), result);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testIntegerDeserializerInvalidArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        mapper.readValue("[1,2]", Integer.class);
    }

    // LongDeserializer
    @Test(timeout = 4000)
    public void testLongDeserializerNumberLong() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Long result = mapper.readValue("1234567890123", Long.class);
        assertEquals(Long.valueOf(1234567890123L), result);
    }

    // BooleanDeserializer
    @Test(timeout = 4000)
    public void testBooleanDeserializerTrue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Boolean.TRUE, mapper.readValue("true", Boolean.class));
        assertEquals(Boolean.FALSE, mapper.readValue("false", Boolean.class));
    }

    @Test(timeout = 4000)
    public void testBooleanDeserializerString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Boolean.TRUE, mapper.readValue("\"true\"", Boolean.class));
        assertEquals(Boolean.FALSE, mapper.readValue("\"false\"", Boolean.class));
    }

    // CharacterDeserializer
    @Test(timeout = 4000)
    public void testCharacterDeserializerNumberInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Character result = mapper.readValue("65", Character.class);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializerString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Character result = mapper.readValue("\"A\"", Character.class);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testCharacterDeserializerEmptyString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"\"", Character.class);
    }

    // NumberDeserializer
    @Test(timeout = 4000)
    public void testNumberDeserializerInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Object result = mapper.readValue("42", Number.class);
        assertTrue("Should be Integer", result instanceof Integer);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerFloat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Object result = mapper.readValue("3.14", Number.class);
        assertTrue("Should be Double", result instanceof Double);
        assertEquals(3.14, (Double) result, 0.0001);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerBigDecimal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Object result = mapper.readValue("3.14", Number.class);
        assertTrue("Should be BigDecimal", result instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14"), result);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerBigInteger() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Object result = mapper.readValue("12345678901234567890", Number.class);
        assertTrue("Should be BigInteger", result instanceof BigInteger);
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerInfinity() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Object result = mapper.readValue("\"Infinity\"", Number.class);
        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerNaN() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Object result = mapper.readValue("\"NaN\"", Number.class);
        assertEquals(Double.NaN, result);
    }

    // BigDecimalDeserializer
    @Test(timeout = 4000)
    public void testBigDecimalDeserializerFloat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal result = mapper.readValue("123.456", BigDecimal.class);
        assertEquals(new BigDecimal("123.456"), result);
    }

    // BigIntegerDeserializer
    @Test(timeout = 4000)
    public void testBigIntegerDeserializerInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigInteger result = mapper.readValue("12345678901234567890", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerFloatWithAccept() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        BigInteger result = mapper.readValue("1.0", BigInteger.class);
        assertEquals(BigInteger.ONE, result);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testBigIntegerDeserializerFloatWithoutAccept() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        mapper.readValue("1.5", BigInteger.class);
    }

    // ---- Partition D: deserializeWithType() for natural types ----
    @Test(timeout = 4000)
    public void testIntegerDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // For Integer, deserializeWithType should behave same as deserialize
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.wrapperInstance;
        JsonParser parser = mapper.getFactory().createParser("42");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Integer result = deser.deserializeWithType(parser, ctxt, null);
        assertEquals(Integer.valueOf(42), result);
    }

    // ---- Partition E: Defect targeted - empty string for primitive types ----
    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testEmptyToNullCoercionForPrimitiveInt() throws Exception {
        // Empty string should NOT be accepted for primitive int; should throw exception
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"\"", int.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testEmptyToNullCoercionForPrimitiveBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"\"", boolean.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testEmptyToNullCoercionForPrimitiveLong() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"\"", long.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testEmptyToNullCoercionForPrimitiveDouble() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"\"", double.class);
    }

    // Also test that wrapper types accept empty string and return null
    @Test(timeout = 4000)
    public void testEmptyStringCoercionForWrapperInteger() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // By default, empty string for wrapper returns null (unless configured otherwise)
        // But the defect is about primitives; wrapper should return null
        Integer result = mapper.readValue("\"\"", Integer.class);
        assertNull("Wrapper Integer should coerce empty string to null", result);
    }

    @Test(timeout = 4000)
    public void testEmptyStringCoercionForWrapperBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Boolean result = mapper.readValue("\"\"", Boolean.class);
        assertNull("Wrapper Boolean should coerce empty string to null", result);
    }

    // ---- Partition F: UNWRAP_SINGLE_VALUE_ARRAYS for all types ----
    @Test(timeout = 4000)
    public void testUnwrapArrayInteger() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertEquals(Integer.valueOf(5), mapper.readValue("[5]", Integer.class));
    }

    @Test(timeout = 4000)
    public void testUnwrapArrayBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertEquals(Boolean.TRUE, mapper.readValue("[true]", Boolean.class));
    }

    // ---- Partition G: Boundary values ----
    @Test(timeout = 4000)
    public void testIntegerMinMax() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Integer.MAX_VALUE, mapper.readValue("2147483647", Integer.class).intValue());
        assertEquals(Integer.MIN_VALUE, mapper.readValue("-2147483648", Integer.class).intValue());
    }

    @Test(timeout = 4000)
    public void testLongMinMax() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Long.MAX_VALUE, mapper.readValue("9223372036854775807", Long.class).longValue());
        assertEquals(Long.MIN_VALUE, mapper.readValue("-9223372036854775808", Long.class).longValue());
    }

    @Test(timeout = 4000)
    public void testDoubleSpecialValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(Double.isNaN(mapper.readValue("\"NaN\"", Double.class)));
        assertEquals(Double.POSITIVE_INFINITY, mapper.readValue("\"Infinity\"", Double.class), 0);
        assertEquals(Double.NEGATIVE_INFINITY, mapper.readValue("\"-Infinity\"", Double.class), 0);
        assertEquals(0.0, mapper.readValue("0.0", Double.class), 0);
    }

    @Test(timeout = 4000)
    public void testFloatSpecialValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(Float.isNaN(mapper.readValue("\"NaN\"", Float.class)));
        assertEquals(Float.POSITIVE_INFINITY, mapper.readValue("\"Infinity\"", Float.class), 0);
        assertEquals(Float.NEGATIVE_INFINITY, mapper.readValue("\"-Infinity\"", Float.class), 0);
    }

    @Test(timeout = 4000)
    public void testByteBoundary() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Byte.MAX_VALUE, mapper.readValue("127", Byte.class).byteValue());
        assertEquals(Byte.MIN_VALUE, mapper.readValue("-128", Byte.class).byteValue());
    }

    @Test(timeout = 4000)
    public void testShortBoundary() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(Short.MAX_VALUE, mapper.readValue("32767", Short.class).shortValue());
        assertEquals(Short.MIN_VALUE, mapper.readValue("-32768", Short.class).shortValue());
    }

    // Ensure no regression for valid float strings
    @Test(timeout = 4000)
    public void testFloatStringParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(3.14f, mapper.readValue("\"3.14\"", Float.class), 0.0001f);
    }

    // Test getEmptyValue for primitive and wrapper (Character case)
    @Test(timeout = 4000)
    public void testCharacterEmptyStringHandling() throws Exception {
        // For Character wrapper, empty string should return null (via getEmptyValue)
        ObjectMapper mapper = new ObjectMapper();
        assertNull("Empty string for Character wrapper", mapper.readValue("\"\"", Character.class));
    }
}