package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.std.NumberDeserializers
 *
 * Key Branch Zones:
 * 1. NumberDeserializers.find(Class<?>, String):
 *    - All 8 primitive types (int, boolean, long, double, char, byte, short, float)
 *    - Unsupported primitive type (e.g. Void.TYPE) -> throws IllegalArgumentException
 *    - Wrapper types and Big types (Integer, Boolean, Long, Double, Character, Byte, Short, Float, Number, BigDecimal, BigInteger)
 *    - Unrecognized class / non-number class -> returns null
 *
 * 2. PrimitiveOrWrapperDeserializer:
 *    - getNullValue(ctxt): _primitive && FAIL_ON_NULL_FOR_PRIMITIVES -> throws JsonMappingException
 *    - getNullValue(ctxt): wrapper or disabled feature -> returns _nullValue
 *    - getNullValue(): deprecated zero-arg getter returns _nullValue
 *
 * 3. CharacterDeserializer:
 *    - ID_NUMBER_INT: within [0, 0xFFFF] returns char; outside bounds -> throws JsonMappingException
 *    - ID_STRING: len == 1 returns char; len == 0 returns empty value; len > 1 -> throws JsonMappingException
 *    - ID_START_ARRAY: UNWRAP_SINGLE_VALUE_ARRAYS enabled with 1 element -> char; with >1 element -> throws JsonMappingException
 *    - Other token types -> throws JsonMappingException
 *
 * 4. IntegerDeserializer / LongDeserializer / DoubleDeserializer:
 *    - isCachable() == true
 *    - deserialize() with VALUE_NUMBER_INT fast path vs scalar coercion
 *    - deserializeWithType() delegation
 *
 * 5. NumberDeserializers.NumberDeserializer:
 *    - ID_NUMBER_INT with and without F_MASK_INT_COERCIONS
 *    - ID_NUMBER_FLOAT with and without USE_BIG_DECIMAL_FOR_FLOATS
 *    - ID_STRING: empty string, textual null ("null"), PosInf, NegInf, NaN
 *    - ID_STRING: int numbers within Integer range vs Long range vs BigInteger
 *    - ID_STRING: non-int numbers as Double vs BigDecimal
 *    - ID_STRING: illegal number format -> throws JsonMappingException
 *    - ID_START_ARRAY: unwrap single value array (single vs multi element)
 *    - deserializeWithType() with scalar token vs typed scalar
 *
 * 6. BigIntegerDeserializer & BigDecimalDeserializer:
 *    - ID_NUMBER_FLOAT: BigInteger with ACCEPT_FLOAT_AS_INT enabled vs disabled
 *    - ID_STRING: empty string returns null; valid big representation; invalid representation
 *    - ID_START_ARRAY: unwrap single vs multiple elements
 *
 * Defect Zone (Defects4J / databind#1095):
 * - Coercion of empty string "" into primitive types when FAIL_ON_NULL_FOR_PRIMITIVES is enabled
 *   must be rejected with JsonMappingException and not silently coerced into 0/default primitive.
 */
public class NumberDeserializersGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // PARTITION A: Core Factory & Registry Lookup (find)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindAllPrimitiveDeserializers() {
        assertNotNull(NumberDeserializers.find(Integer.TYPE, Integer.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Boolean.TYPE, Boolean.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Long.TYPE, Long.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Double.TYPE, Double.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Character.TYPE, Character.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Byte.TYPE, Byte.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Short.TYPE, Short.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Float.TYPE, Float.TYPE.getName()));
    }

    @Test(timeout = 4000)
    public void testFindAllWrapperAndBigDeserializers() {
        assertNotNull(NumberDeserializers.find(Integer.class, Integer.class.getName()));
        assertNotNull(NumberDeserializers.find(Boolean.class, Boolean.class.getName()));
        assertNotNull(NumberDeserializers.find(Long.class, Long.class.getName()));
        assertNotNull(NumberDeserializers.find(Double.class, Double.class.getName()));
        assertNotNull(NumberDeserializers.find(Character.class, Character.class.getName()));
        assertNotNull(NumberDeserializers.find(Byte.class, Byte.class.getName()));
        assertNotNull(NumberDeserializers.find(Short.class, Short.class.getName()));
        assertNotNull(NumberDeserializers.find(Float.class, Float.class.getName()));
        assertNotNull(NumberDeserializers.find(Number.class, Number.class.getName()));
        assertNotNull(NumberDeserializers.find(BigDecimal.class, BigDecimal.class.getName()));
        assertNotNull(NumberDeserializers.find(BigInteger.class, BigInteger.class.getName()));
    }

    @Test(timeout = 4000)
    public void testFindUnrecognizedTypeReturnsNull() {
        assertNull(NumberDeserializers.find(String.class, String.class.getName()));
        assertNull(NumberDeserializers.find(Object.class, Object.class.getName()));
    }

    @Test(timeout = 4000)
    public void testFindUnsupportedPrimitiveThrowsInternalError() {
        try {
            NumberDeserializers.find(Void.TYPE, Void.TYPE.getName());
            fail("Expected IllegalArgumentException for unsupported primitive type void");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Internal error: can't find deserializer for void"));
        }
    }

    @Test(timeout = 4000)
    public void testNumberDeserializersConstructor() {
        NumberDeserializers nd = new NumberDeserializers();
        assertNotNull(nd);
    }

    // =========================================================================
    // PARTITION B: PrimitiveOrWrapper Null Handling & Features
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveOrWrapperNullValueBasic() {
        NumberDeserializers.IntegerDeserializer intDeser = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        assertTrue(intDeser.isCachable());
        assertEquals(Integer.valueOf(0), intDeser.getNullValue());

        NumberDeserializers.IntegerDeserializer wrapperDeser = NumberDeserializers.IntegerDeserializer.wrapperInstance;
        assertNull(wrapperDeser.getNullValue());

        NumberDeserializers.LongDeserializer longDeser = NumberDeserializers.LongDeserializer.primitiveInstance;
        assertTrue(longDeser.isCachable());
        assertEquals(Long.valueOf(0L), longDeser.getNullValue());
    }

    @Test(timeout = 4000)
    public void testFailOnNullForPrimitivesEnabled() throws Exception {
        ObjectMapper failMapper = new ObjectMapper();
        failMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        try {
            failMapper.readValue("null", int.class);
            fail("Expected JsonMappingException on null for primitive int");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("FAIL_ON_NULL_FOR_PRIMITIVES"));
        }

        try {
            failMapper.readValue("null", boolean.class);
            fail("Expected JsonMappingException on null for primitive boolean");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("FAIL_ON_NULL_FOR_PRIMITIVES"));
        }
    }

    @Test(timeout = 4000)
    public void testFailOnNullForPrimitivesDisabled() throws Exception {
        ObjectMapper okMapper = new ObjectMapper();
        okMapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        int intVal = okMapper.readValue("null", int.class);
        assertEquals(0, intVal);

        boolean boolVal = okMapper.readValue("null", boolean.class);
        assertFalse(boolVal);

        double doubleVal = okMapper.readValue("null", double.class);
        assertEquals(0.0, doubleVal, 0.0001);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (databind#1095 / Defects4J)
    // =========================================================================

    /**
     * TARGETED DEFECT:
     * com.fasterxml.jackson.databind.deser.TestSimpleTypes::testEmptyToNullCoercionForPrimitives
     * When FAIL_ON_NULL_FOR_PRIMITIVES is true, empty String coercion into primitive
     * should not be allowed and MUST throw JsonMappingException!
     */
    @Test(timeout = 4000)
    public void testDefectEmptyStringToPrimitiveWithFailOnNull() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        try {
            strictMapper.readValue("\"\"", int.class);
            fail("Should not have passed: empty string to primitive int with FAIL_ON_NULL_FOR_PRIMITIVES enabled must throw");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            strictMapper.readValue("\"\"", boolean.class);
            fail("Should not have passed: empty string to primitive boolean with FAIL_ON_NULL_FOR_PRIMITIVES enabled must throw");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            strictMapper.readValue("\"\"", double.class);
            fail("Should not have passed: empty string to primitive double with FAIL_ON_NULL_FOR_PRIMITIVES enabled must throw");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // PARTITION D: CharacterDeserializer Complete Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testCharacterDeserializerValidTokens() throws Exception {
        Character c1 = mapper.readValue("\"a\"", Character.class);
        assertEquals(Character.valueOf('a'), c1);

        char c2 = mapper.readValue("\"Z\"", char.class);
        assertEquals('Z', c2);

        Character c3 = mapper.readValue("65", Character.class);
        assertEquals(Character.valueOf('A'), c3);

        Character cEmpty = mapper.readValue("\"\"", Character.class);
        assertNull(cEmpty);
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializerInvalidStringLength() throws Exception {
        try {
            mapper.readValue("\"abc\"", Character.class);
            fail("Expected JsonMappingException for string length > 1");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializerOutOfRangeInt() throws Exception {
        try {
            mapper.readValue("-1", Character.class);
            fail("Expected JsonMappingException for negative char code");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            mapper.readValue("65536", Character.class);
            fail("Expected JsonMappingException for char code > 0xFFFF");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializerUnwrapSingleArray() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Character c = unwrapMapper.readValue("[\"k\"]", Character.class);
        assertEquals(Character.valueOf('k'), c);

        try {
            unwrapMapper.readValue("[\"k\", \"m\"]", Character.class);
            fail("Expected JsonMappingException for array with more than 1 value");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("there was more than a single value"));
        }
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializerInvalidToken() throws Exception {
        try {
            mapper.readValue("true", Character.class);
            fail("Expected JsonMappingException for boolean token to Character");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // PARTITION E: NumberDeserializer (Generic Number.class) Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumberDeserializerTokens() throws Exception {
        Object intNum = mapper.readValue("42", Number.class);
        assertTrue(intNum instanceof Integer || intNum instanceof Long);
        assertEquals(42, ((Number) intNum).intValue());

        Object floatNum = mapper.readValue("3.14159", Number.class);
        assertTrue(floatNum instanceof Double);
        assertEquals(3.14159, ((Double) floatNum).doubleValue(), 0.00001);

        ObjectMapper bigDecimalMapper = new ObjectMapper();
        bigDecimalMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Object bigDec = bigDecimalMapper.readValue("3.14159", Number.class);
        assertTrue(bigDec instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14159"), bigDec);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerStrings() throws Exception {
        assertNull(mapper.readValue("\"\"", Number.class));
        assertNull(mapper.readValue("\"   \"", Number.class));
        assertNull(mapper.readValue("\"null\"", Number.class));

        assertEquals(Double.POSITIVE_INFINITY, mapper.readValue("\"Infinity\"", Number.class));
        assertEquals(Double.POSITIVE_INFINITY, mapper.readValue("\"+Infinity\"", Number.class));
        assertEquals(Double.NEGATIVE_INFINITY, mapper.readValue("\"-Infinity\"", Number.class));
        assertEquals(Double.NaN, mapper.readValue("\"NaN\"", Number.class));

        Object smallInt = mapper.readValue("\"1234\"", Number.class);
        assertTrue(smallInt instanceof Integer);
        assertEquals(1234, ((Integer) smallInt).intValue());

        Object bigLong = mapper.readValue("\"3000000000\"", Number.class);
        assertTrue(bigLong instanceof Long);
        assertEquals(3000000000L, ((Long) bigLong).longValue());

        Object doubleFromStr = mapper.readValue("\"12.34\"", Number.class);
        assertTrue(doubleFromStr instanceof Double);
        assertEquals(12.34, (Double) doubleFromStr, 0.0001);

        ObjectMapper bigIntMapper = new ObjectMapper();
        bigIntMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Object bigIntegerObj = bigIntMapper.readValue("\"999\"", Number.class);
        assertTrue(bigIntegerObj instanceof BigInteger);
        assertEquals(new BigInteger("999"), bigIntegerObj);

        ObjectMapper useLongMapper = new ObjectMapper();
        useLongMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        Object longObj = useLongMapper.readValue("\"100\"", Number.class);
        assertTrue(longObj instanceof Long);
        assertEquals(100L, ((Long) longObj).longValue());

        ObjectMapper bigDecFloatMapper = new ObjectMapper();
        bigDecFloatMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Object bigDecObj = bigDecFloatMapper.readValue("\"45.67\"", Number.class);
        assertTrue(bigDecObj instanceof BigDecimal);
        assertEquals(new BigDecimal("45.67"), bigDecObj);
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerInvalidString() throws Exception {
        try {
            mapper.readValue("\"not_a_number\"", Number.class);
            fail("Expected JsonMappingException for invalid number string");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid number"));
        }
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerArrayUnwrapping() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Object val = unwrapMapper.readValue("[ 55 ]", Number.class);
        assertEquals(55, ((Number) val).intValue());

        try {
            unwrapMapper.readValue("[ 55, 66 ]", Number.class);
            fail("Expected JsonMappingException when multiple elements found in array");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("there was more than a single value"));
        }
    }

    @Test(timeout = 4000)
    public void testNumberDeserializerUnexpectedToken() throws Exception {
        try {
            mapper.readValue("true", Number.class);
            fail("Expected JsonMappingException for boolean token mapped to Number");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // PARTITION F: BigIntegerDeserializer & BigDecimalDeserializer
    // =========================================================================

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerTokens() throws Exception {
        BigInteger b1 = mapper.readValue("12345678901234567890", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), b1);

        BigInteger b2 = mapper.readValue("\"9876543210\"", BigInteger.class);
        assertEquals(new BigInteger("9876543210"), b2);

        BigInteger bEmpty = mapper.readValue("\"\"", BigInteger.class);
        assertNull(bEmpty);

        BigInteger bFromFloat = mapper.readValue("12.99", BigInteger.class);
        assertEquals(BigInteger.valueOf(12), bFromFloat);
    }

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerFloatDisabled() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

        try {
            strictMapper.readValue("12.34", BigInteger.class);
            fail("Expected JsonMappingException when ACCEPT_FLOAT_AS_INT is false");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerInvalidString() throws Exception {
        try {
            mapper.readValue("\"invalid-bigint\"", BigInteger.class);
            fail("Expected JsonMappingException for invalid BigInteger string");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerArrayUnwrapping() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        BigInteger b = unwrapMapper.readValue("[ \"100\" ]", BigInteger.class);
        assertEquals(BigInteger.valueOf(100), b);

        try {
            unwrapMapper.readValue("[ \"100\", \"200\" ]", BigInteger.class);
            fail("Expected JsonMappingException for multi-element array");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("there was more than a single value"));
        }
    }

    @Test(timeout = 4000)
    public void testBigIntegerDeserializerUnexpectedToken() throws Exception {
        try {
            mapper.readValue("true", BigInteger.class);
            fail("Expected JsonMappingException for boolean token");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBigDecimalDeserializerTokens() throws Exception {
        BigDecimal bd1 = mapper.readValue("123.456", BigDecimal.class);
        assertEquals(new BigDecimal("123.456"), bd1);

        BigDecimal bd2 = mapper.readValue("100", BigDecimal.class);
        assertEquals(new BigDecimal("100"), bd2);

        BigDecimal bd3 = mapper.readValue("\"789.01\"", BigDecimal.class);
        assertEquals(new BigDecimal("789.01"), bd3);

        BigDecimal bdEmpty = mapper.readValue("\"\"", BigDecimal.class);
        assertNull(bdEmpty);
    }

    @Test(timeout = 4000)
    public void testBigDecimalDeserializerInvalidString() throws Exception {
        try {
            mapper.readValue("\"invalid-decimal\"", BigDecimal.class);
            fail("Expected JsonMappingException for invalid BigDecimal representation");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    @Test(timeout = 4000)
    public void testBigDecimalDeserializerArrayUnwrapping() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        BigDecimal bd = unwrapMapper.readValue("[ \"1.23\" ]", BigDecimal.class);
        assertEquals(new BigDecimal("1.23"), bd);

        try {
            unwrapMapper.readValue("[ \"1.23\", \"4.56\" ]", BigDecimal.class);
            fail("Expected JsonMappingException for multi-element array");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("there was more than a single value"));
        }
    }

    @Test(timeout = 4000)
    public void testBigDecimalDeserializerUnexpectedToken() throws Exception {
        try {
            mapper.readValue("false", BigDecimal.class);
            fail("Expected JsonMappingException for boolean token to BigDecimal");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // PARTITION G: Byte, Short, Float, Double, Boolean Deserializers
    // =========================================================================

    @Test(timeout = 4000)
    public void testByteAndShortDeserializers() throws Exception {
        byte bVal = mapper.readValue("12", byte.class);
        assertEquals((byte) 12, bVal);

        Byte bObj = mapper.readValue("\"-5\"", Byte.class);
        assertEquals(Byte.valueOf((byte) -5), bObj);

        short sVal = mapper.readValue("32000", short.class);
        assertEquals((short) 32000, sVal);

        Short sObj = mapper.readValue("\"-100\"", Short.class);
        assertEquals(Short.valueOf((short) -100), sObj);
    }

    @Test(timeout = 4000)
    public void testFloatAndDoubleDeserializers() throws Exception {
        float fVal = mapper.readValue("1.25", float.class);
        assertEquals(1.25f, fVal, 0.001f);

        Float fObj = mapper.readValue("\"3.5\"", Float.class);
        assertEquals(Float.valueOf(3.5f), fObj);

        double dVal = mapper.readValue("2.71828", double.class);
        assertEquals(2.71828, dVal, 0.00001);

        Double dObj = mapper.readValue("\"-0.05\"", Double.class);
        assertEquals(Double.valueOf(-0.05), dObj);
    }

    @Test(timeout = 4000)
    public void testBooleanDeserializer() throws Exception {
        boolean boolVal = mapper.readValue("true", boolean.class);
        assertTrue(boolVal);

        Boolean boolObj = mapper.readValue("\"false\"", Boolean.class);
        assertEquals(Boolean.FALSE, boolObj);

        Boolean boolFromInt = mapper.readValue("1", Boolean.class);
        assertEquals(Boolean.TRUE, boolFromInt);
    }

    // =========================================================================
    // PARTITION H: Direct Method Invocations (deserializeWithType & Coercion)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeWithTypeDirectDelegation() throws IOException {
        NumberDeserializers.BooleanDeserializer boolDeser = NumberDeserializers.BooleanDeserializer.wrapperInstance;
        JsonParser pBool = mapper.getFactory().createParser("true");
        pBool.nextToken();
        DeserializationContext ctxtBool = mapper.getDeserializationContext();
        Boolean bRes = boolDeser.deserializeWithType(pBool, ctxtBool, null);
        assertTrue(bRes);

        NumberDeserializers.IntegerDeserializer intDeser = NumberDeserializers.IntegerDeserializer.wrapperInstance;
        JsonParser pInt = mapper.getFactory().createParser("123");
        pInt.nextToken();
        Integer iRes = intDeser.deserializeWithType(pInt, ctxtBool, null);
        assertEquals(Integer.valueOf(123), iRes);

        NumberDeserializers.DoubleDeserializer doubleDeser = NumberDeserializers.DoubleDeserializer.wrapperInstance;
        JsonParser pDouble = mapper.getFactory().createParser("4.56");
        pDouble.nextToken();
        Double dRes = doubleDeser.deserializeWithType(pDouble, ctxtBool, null);
        assertEquals(4.56, dRes, 0.0001);

        NumberDeserializers.NumberDeserializer numDeser = NumberDeserializers.NumberDeserializer.instance;
        JsonParser pNum = mapper.getFactory().createParser("789");
        pNum.nextToken();
        Object nRes = numDeser.deserializeWithType(pNum, ctxtBool, null);
        assertEquals(789, ((Number) nRes).intValue());
    }
}