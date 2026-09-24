package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.AccessPattern;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * 1. Constructor and Type Introspection:
 *    - Class<?> constructor, JavaType constructor (null check fallback to Object.class), copy constructor.
 *    - handledType(), getValueClass(), getValueType(), isDefaultDeserializer(), isDefaultKeyDeserializer().
 *
 * 2. Primitive Parsing & Overflows:
 *    - _parseBooleanPrimitive: TRUE, FALSE, NULL (with/without FAIL_ON_NULL_FOR_PRIMITIVES),
 *      INT ("0" -> false, non-zero -> true), STRING ("true", "True", "false", "False", empty/null, weird string),
 *      UNWRAP_SINGLE_VALUE_ARRAYS (single vs multiple elements error), unexpected token.
 *    - _parseBytePrimitive / _byteOverflow: signed & unsigned ranges (-128 to 255), overflow handling.
 *    - _parseShortPrimitive / _shortOverflow: -32768 to 32767, overflow handling.
 *    - _parseIntPrimitive / _intOverflow: int token, string (<= 9 chars, > 9 chars with overflow or valid),
 *      float token (ACCEPT_FLOAT_AS_INT check), null token, array unwrap, invalid string format.
 *    - _parseLongPrimitive: int/long token, string (valid / invalid), float token, null, array unwrap.
 *    - _parseFloatPrimitive: float token, int token, string (pos inf, neg inf, NaN, normal, invalid),
 *      null, array unwrap.
 *    - _parseDoublePrimitive: parseDouble special case (NumberInput.NASTY_SMALL_DOUBLE -> Double.MIN_NORMAL),
 *      inf, NaN, normal, invalid, null, array unwrap.
 *
 * 3. Date Parsing:
 *    - _parseDate: String ("" / "null" -> null, valid ISO/epoch, invalid format),
 *      INT (valid long, overflow/weird), NULL, START_ARRAY (ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
 *      UNWRAP_SINGLE_VALUE_ARRAYS, single item vs missing end array, unexpected).
 *
 * 4. Coercions & Feature Verification:
 *    - _coerceIntegral (USE_BIG_INTEGER_FOR_INTS, USE_LONG_FOR_INTS).
 *    - _coerceNullToken, _coerceTextualNull, _coerceEmptyString (ALLOW_COERCION_OF_SCALARS,
 *      FAIL_ON_NULL_FOR_PRIMITIVES).
 *    - _verifyNullForPrimitive, _verifyNullForPrimitiveCoercion, _verifyNullForScalarCoercion,
 *      _verifyStringForScalarCoercion, _verifyNumberForScalarCoercion.
 *    - _coercedTypeDesc (structured: array, Collection, Map, JavaType vs scalar).
 *
 * 5. Array Unwrapping & Empty Object Handling:
 *    - _deserializeFromArray: empty array, single-wrapped value, multi-element missing end array,
 *      nested array disallowed check (_deserializeWrappedValue).
 *    - _deserializeFromEmpty: START_ARRAY, VALUE_STRING, unexpected tokens.
 *
 * 6. Null Providers & Format Overrides:
 *    - _findNullProvider / findContentNullProvider: Nulls.FAIL, Nulls.AS_EMPTY (ALWAYS_NULL, CONSTANT, dynamic),
 *      Nulls.SKIP, null default.
 *    - findFormatOverrides, findFormatFeature.
 *
 * 7. Unknown Properties & Utilities:
 *    - handleUnknownProperty, _neitherNull, _isIntNumber, _nonNullNumber.
 *    - Defect targeting: Exception formatting and location handling when parsing invalid representation.
 */
public class StdDeserializerGeminiTest {

    // Concrete mockable subclass for white-box testing of protected methods
    private static class ConcreteStdDeserializer<T> extends StdDeserializer<T> {
        private static final long serialVersionUID = 1L;

        protected ConcreteStdDeserializer(Class<?> vc) {
            super(vc);
        }

        protected ConcreteStdDeserializer(JavaType vt) {
            super(vt);
        }

        protected ConcreteStdDeserializer(StdDeserializer<?> src) {
            super(src);
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    @JacksonStdImpl
    private static class StdImplDeserializer extends ConcreteStdDeserializer<Object> {
        private static final long serialVersionUID = 1L;
        public StdImplDeserializer() {
            super(Object.class);
        }
    }

    private static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }
    }

    @JacksonStdImpl
    private static class StdImplKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() {
        ConcreteStdDeserializer<String> deserClass = new ConcreteStdDeserializer<String>(String.class);
        assertEquals(String.class, deserClass.handledType());
        @SuppressWarnings("deprecation")
        Class<?> valClass = deserClass.getValueClass();
        assertEquals(String.class, valClass);
        assertNull(deserClass.getValueType());

        // JavaType constructor
        JavaType vt = TypeFactory.defaultInstance().constructType(Integer.class);
        ConcreteStdDeserializer<Integer> deserJavaType = new ConcreteStdDeserializer<Integer>(vt);
        assertEquals(Integer.class, deserJavaType.handledType());

        // JavaType constructor with null
        ConcreteStdDeserializer<Object> deserNullType = new ConcreteStdDeserializer<Object>((JavaType) null);
        assertEquals(Object.class, deserNullType.handledType());

        // Copy constructor
        ConcreteStdDeserializer<String> copyDeser = new ConcreteStdDeserializer<String>(deserClass);
        assertEquals(String.class, copyDeser.handledType());
    }

    @Test(timeout = 4000)
    public void testIsDefaultDeserializerAndKeyDeserializer() {
        ConcreteStdDeserializer<Object> custom = new ConcreteStdDeserializer<Object>(Object.class);
        StdImplDeserializer std = new StdImplDeserializer();

        assertFalse(custom.isDefaultDeserializer(custom));
        assertTrue(custom.isDefaultDeserializer(std));

        CustomKeyDeserializer customKey = new CustomKeyDeserializer();
        StdImplKeyDeserializer stdKey = new StdImplKeyDeserializer();

        assertFalse(custom.isDefaultKeyDeserializer(customKey));
        assertTrue(custom.isDefaultKeyDeserializer(stdKey));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);
        String json = "{\"@type\":\"dummy\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return "@type"; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return Object.class; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return "fromObj"; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return "fromAny"; }
        };

        Object res = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals("fromAny", res);
        p.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Primitive Parsers
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseBooleanPrimitive() throws Exception {
        ConcreteStdDeserializer<Boolean> deser = new ConcreteStdDeserializer<Boolean>(boolean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // TRUE and FALSE tokens
        JsonParser pTrue = mapper.getFactory().createParser("true");
        pTrue.nextToken();
        assertTrue(deser._parseBooleanPrimitive(pTrue, ctxt));
        pTrue.close();

        JsonParser pFalse = mapper.getFactory().createParser("false");
        pFalse.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pFalse, ctxt));
        pFalse.close();

        // Int token 0 and 1
        JsonParser pInt0 = mapper.getFactory().createParser("0");
        pInt0.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pInt0, ctxt));
        pInt0.close();

        JsonParser pInt1 = mapper.getFactory().createParser("123");
        pInt1.nextToken();
        assertTrue(deser._parseBooleanPrimitive(pInt1, ctxt));
        pInt1.close();

        // String tokens: "true", "True", "false", "False", empty, null
        for (String validTrue : new String[]{"\"true\"", "\"True\""}) {
            JsonParser p = mapper.getFactory().createParser(validTrue);
            p.nextToken();
            assertTrue(deser._parseBooleanPrimitive(p, ctxt));
            p.close();
        }
        for (String validFalse : new String[]{"\"false\"", "\"False\""}) {
            JsonParser p = mapper.getFactory().createParser(validFalse);
            p.nextToken();
            assertFalse(deser._parseBooleanPrimitive(p, ctxt));
            p.close();
        }

        JsonParser pEmptyStr = mapper.getFactory().createParser("\"\"");
        pEmptyStr.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pEmptyStr, ctxt));
        pEmptyStr.close();

        // Null token
        JsonParser pNull = mapper.getFactory().createParser("null");
        pNull.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pNull, ctxt));
        pNull.close();
    }

    @Test(timeout = 4000)
    public void testParseBytePrimitive() throws Exception {
        ConcreteStdDeserializer<Byte> deser = new ConcreteStdDeserializer<Byte>(byte.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Valid ranges: Byte.MIN_VALUE (-128) to 255 (unsigned byte support)
        JsonParser p1 = mapper.getFactory().createParser("-128");
        p1.nextToken();
        assertEquals((byte) -128, deser._parseBytePrimitive(p1, ctxt));
        p1.close();

        JsonParser p2 = mapper.getFactory().createParser("255");
        p2.nextToken();
        assertEquals((byte) 255, deser._parseBytePrimitive(p2, ctxt));
        p2.close();

        // Overflow check helpers
        assertTrue(deser._byteOverflow(-129));
        assertTrue(deser._byteOverflow(256));
        assertFalse(deser._byteOverflow(0));
        assertFalse(deser._byteOverflow(255));
    }

    @Test(timeout = 4000)
    public void testParseShortPrimitive() throws Exception {
        ConcreteStdDeserializer<Short> deser = new ConcreteStdDeserializer<Short>(short.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser p1 = mapper.getFactory().createParser("-32768");
        p1.nextToken();
        assertEquals((short) -32768, deser._parseShortPrimitive(p1, ctxt));
        p1.close();

        JsonParser p2 = mapper.getFactory().createParser("32767");
        p2.nextToken();
        assertEquals((short) 32767, deser._parseShortPrimitive(p2, ctxt));
        p2.close();

        assertTrue(deser._shortOverflow(-32769));
        assertTrue(deser._shortOverflow(32768));
        assertFalse(deser._shortOverflow(0));
    }

    @Test(timeout = 4000)
    public void testParseIntPrimitive() throws Exception {
        ConcreteStdDeserializer<Integer> deser = new ConcreteStdDeserializer<Integer>(int.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Int token
        JsonParser pInt = mapper.getFactory().createParser("42");
        pInt.nextToken();
        assertEquals(42, deser._parseIntPrimitive(pInt, ctxt));
        pInt.close();

        // String short length (<= 9)
        JsonParser pStr = mapper.getFactory().createParser("\"12345678\"");
        pStr.nextToken();
        assertEquals(12345678, deser._parseIntPrimitive(pStr, ctxt));
        pStr.close();

        // String long length (> 9) within int range
        JsonParser pStr9 = mapper.getFactory().createParser("\"2147483647\"");
        pStr9.nextToken();
        assertEquals(Integer.MAX_VALUE, deser._parseIntPrimitive(pStr9, ctxt));
        pStr9.close();

        // Null token
        JsonParser pNull = mapper.getFactory().createParser("null");
        pNull.nextToken();
        assertEquals(0, deser._parseIntPrimitive(pNull, ctxt));
        pNull.close();

        // Float token with ACCEPT_FLOAT_AS_INT enabled
        ObjectMapper mapperFloat = new ObjectMapper().enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        JsonParser pFloat = mapperFloat.getFactory().createParser("12.34");
        pFloat.nextToken();
        assertEquals(12, deser._parseIntPrimitive(pFloat, mapperFloat.getDeserializationContext()));
        pFloat.close();

        // Overflow logic
        assertTrue(deser._intOverflow(Long.MAX_VALUE));
        assertTrue(deser._intOverflow(Long.MIN_VALUE));
        assertFalse(deser._intOverflow(100L));
    }

    @Test(timeout = 4000)
    public void testParseLongPrimitive() throws Exception {
        ConcreteStdDeserializer<Long> deser = new ConcreteStdDeserializer<Long>(long.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser p1 = mapper.getFactory().createParser("9223372036854775807");
        p1.nextToken();
        assertEquals(Long.MAX_VALUE, deser._parseLongPrimitive(p1, ctxt));
        p1.close();

        JsonParser p2 = mapper.getFactory().createParser("\"-9223372036854775808\"");
        p2.nextToken();
        assertEquals(Long.MIN_VALUE, deser._parseLongPrimitive(p2, ctxt));
        p2.close();

        JsonParser pNull = mapper.getFactory().createParser("null");
        pNull.nextToken();
        assertEquals(0L, deser._parseLongPrimitive(pNull, ctxt));
        pNull.close();
    }

    @Test(timeout = 4000)
    public void testParseFloatAndDoublePrimitive() throws Exception {
        ConcreteStdDeserializer<Double> deser = new ConcreteStdDeserializer<Double>(double.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Special floats: NaN, Infinity, -Infinity
        assertEquals(Float.POSITIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "Infinity"), 0.0001f);
        assertEquals(Float.POSITIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "INF"), 0.0001f);
        assertEquals(Float.NEGATIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "-Infinity"), 0.0001f);
        assertEquals(Float.NEGATIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "-INF"), 0.0001f);
        assertTrue(Float.isNaN(deser._parseFloatPrimitive(ctxt, "NaN")));

        // Double specials
        assertEquals(Double.POSITIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "Infinity"), 0.0001);
        assertEquals(Double.POSITIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "INF"), 0.0001);
        assertEquals(Double.NEGATIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "-Infinity"), 0.0001);
        assertEquals(Double.NEGATIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "-INF"), 0.0001);
        assertTrue(Double.isNaN(deser._parseDoublePrimitive(ctxt, "NaN")));

        // parseDouble NASTY_SMALL_DOUBLE test
        assertEquals(Double.MIN_NORMAL, ConcreteStdDeserializer.parseDouble("2.2250738585072012e-308"), 0.0);
    }

    @Test(timeout = 4000)
    public void testParseString() throws Exception {
        ConcreteStdDeserializer<String> deser = new ConcreteStdDeserializer<String>(String.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser p = mapper.getFactory().createParser("\"simple text\"");
        p.nextToken();
        assertEquals("simple text", deser._parseString(p, ctxt));
        p.close();

        JsonParser pInt = mapper.getFactory().createParser("12345");
        pInt.nextToken();
        assertEquals("12345", deser._parseString(pInt, ctxt));
        pInt.close();
    }

    @Test(timeout = 4000)
    public void testParseDate() throws Exception {
        ConcreteStdDeserializer<Date> deser = new ConcreteStdDeserializer<Date>(Date.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Date from timestamp int
        JsonParser pInt = mapper.getFactory().createParser("1500000000000");
        pInt.nextToken();
        Date d1 = deser._parseDate(pInt, ctxt);
        assertEquals(1500000000000L, d1.getTime());
        pInt.close();

        // Date from empty string or textual null
        assertNull(deser._parseDate("", ctxt));
        assertNull(deser._parseDate("null", ctxt));

        // Date from null token
        JsonParser pNull = mapper.getFactory().createParser("null");
        pNull.nextToken();
        assertNull(deser._parseDate(pNull, ctxt));
        pNull.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone & Formatting/Coercion Errors
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectLocationAdditionAndErrorHandling() {
        // Targets known defect where deserialization failure formats exception message
        // with proper representation and without duplicated location markers or garbled nested messages.
        ConcreteStdDeserializer<Date> deser = new ConcreteStdDeserializer<Date>(Date.class);
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();

        try {
            deser._parseDate("invalid-date-string", ctxt);
            fail("Expected parse exception for invalid date representation");
        } catch (IOException e) {
            assertTrue(e instanceof InvalidFormatException || e instanceof MismatchedInputException);
            String msg = e.getMessage();
            assertTrue(msg.contains("not a valid representation"));
            // Ensure no duplicate location tags
            int firstIdx = msg.indexOf("at [");
            if (firstIdx != -1) {
                int secondIdx = msg.indexOf("at [", firstIdx + 1);
                assertEquals("Should not have duplicated 'at [' markers", -1, secondIdx);
            }
        }
    }

    @Test(timeout = 4000)
    public void testCoerceTypeDesc() {
        ConcreteStdDeserializer<String> deserScalar = new ConcreteStdDeserializer<String>(String.class);
        assertEquals("for type `java.lang.String`", deserScalar._coercedTypeDesc());

        ConcreteStdDeserializer<List> deserColl = new ConcreteStdDeserializer<List>(List.class);
        assertEquals("as content of type `java.util.List`", deserColl._coercedTypeDesc());

        ConcreteStdDeserializer<Map> deserMap = new ConcreteStdDeserializer<Map>(Map.class);
        assertEquals("as content of type `java.util.Map`", deserMap._coercedTypeDesc());

        ConcreteStdDeserializer<int[]> deserArr = new ConcreteStdDeserializer<int[]>(int[].class);
        assertEquals("as content of type `[I`", deserArr._coercedTypeDesc());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFailDoubleToIntCoercion() throws Exception {
        ConcreteStdDeserializer<Integer> deser = new ConcreteStdDeserializer<Integer>(int.class);
        ObjectMapper strictMapper = new ObjectMapper().disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        JsonParser p = strictMapper.getFactory().createParser("12.34");
        p.nextToken();
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        try {
            deser._parseIntPrimitive(p, ctxt);
            fail("Should fail double to int coercion when ACCEPT_FLOAT_AS_INT is disabled");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce a floating-point value"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNullForPrimitiveThrows() throws Exception {
        ConcreteStdDeserializer<Integer> deser = new ConcreteStdDeserializer<Integer>(int.class);
        ObjectMapper strictMapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        JsonParser p = strictMapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        try {
            deser._parseIntPrimitive(p, ctxt);
            fail("Expected exception when FAIL_ON_NULL_FOR_PRIMITIVES is enabled");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce `null`"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testCoerceEmptyStringDisallowed() throws Exception {
        ConcreteStdDeserializer<String> deser = new ConcreteStdDeserializer<String>(String.class);
        ObjectMapper strictMapper = new ObjectMapper().disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        try {
            deser._coerceEmptyString(ctxt, false);
            fail("Expected coercion failure for empty String");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce empty String (\"\") to Null value"));
        }
    }

    @Test(timeout = 4000)
    public void testCoerceTextualNullDisallowed() throws Exception {
        ConcreteStdDeserializer<String> deser = new ConcreteStdDeserializer<String>(String.class);
        ObjectMapper strictMapper = new ObjectMapper().disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        try {
            deser._coerceTextualNull(ctxt, false);
            fail("Expected coercion failure for textual null");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce String \"null\" to Null value"));
        }
    }

    @Test(timeout = 4000)
    public void testVerifyScalarCoercionGuards() throws Exception {
        ConcreteStdDeserializer<String> deser = new ConcreteStdDeserializer<String>(String.class);
        ObjectMapper strictMapper = new ObjectMapper().disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        try {
            deser._verifyStringForScalarCoercion(ctxt, "test");
            fail("Expected exception for String scalar coercion disabled");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce String \"test\""));
        }

        JsonParser p = strictMapper.getFactory().createParser("123");
        p.nextToken();
        try {
            deser._verifyNumberForScalarCoercion(ctxt, p);
            fail("Expected exception for Number scalar coercion disabled");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Cannot coerce Number (123)"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArrayMissingEndArray() throws Exception {
        ConcreteStdDeserializer<Integer> deser = new ConcreteStdDeserializer<Integer>(int.class);
        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        JsonParser p = unwrapMapper.getFactory().createParser("[1, 2]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = unwrapMapper.getDeserializationContext();

        try {
            deser._parseIntPrimitive(p, ctxt);
            fail("Expected exception when array contains more than one value");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Attempted to unwrap"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWrappedValueDisallowsNestedArray() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);
        JsonParser p = mapper.getFactory().createParser("[[]]");
        p.nextToken(); // [
        p.nextToken(); // [
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser._deserializeWrappedValue(p, ctxt);
            fail("Expected nested arrays error");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("nested Arrays not allowed"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeFromEmptyHandling() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);
        ObjectMapper emptyMapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                                                     .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        DeserializationContext ctxt = emptyMapper.getDeserializationContext();

        // Empty array
        JsonParser pArr = emptyMapper.getFactory().createParser("[]");
        pArr.nextToken();
        assertNull(deser._deserializeFromEmpty(pArr, ctxt));
        pArr.close();

        // Empty string
        JsonParser pStr = emptyMapper.getFactory().createParser("\"\"");
        pStr.nextToken();
        assertNull(deser._deserializeFromEmpty(pStr, ctxt));
        pStr.close();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Utility Helpers
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindNullProviderVariants() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Nulls.FAIL
        NullValueProvider failProvider = deser._findNullProvider(ctxt, null, Nulls.FAIL, deser);
        assertTrue(failProvider instanceof NullsFailProvider);

        // Nulls.SKIP
        NullValueProvider skipProvider = deser._findNullProvider(ctxt, null, Nulls.SKIP, deser);
        assertTrue(skipProvider instanceof NullsConstantProvider);

        // Nulls.AS_EMPTY with constant access
        ConcreteStdDeserializer<String> strDeser = new ConcreteStdDeserializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public AccessPattern getEmptyAccessPattern() {
                return AccessPattern.CONSTANT;
            }
            @Override
            public Object getEmptyValue(DeserializationContext ctxt) {
                return "";
            }
        };
        NullValueProvider emptyProvider = deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, strDeser);
        assertTrue(emptyProvider instanceof NullsConstantProvider);
        assertEquals("", emptyProvider.getNullValue(ctxt));

        // Nulls.AS_EMPTY with ALWAYS_NULL access
        ConcreteStdDeserializer<String> nullAccessDeser = new ConcreteStdDeserializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public AccessPattern getEmptyAccessPattern() {
                return AccessPattern.ALWAYS_NULL;
            }
        };
        NullValueProvider nullerProvider = deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, nullAccessDeser);
        assertTrue(nullerProvider instanceof NullsConstantProvider);
        assertNull(nullerProvider.getNullValue(ctxt));

        // Nulls.AS_EMPTY with dynamic access
        ConcreteStdDeserializer<String> dynamicDeser = new ConcreteStdDeserializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public AccessPattern getEmptyAccessPattern() {
                return AccessPattern.DYNAMIC;
            }
        };
        NullValueProvider dynamicProvider = deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, dynamicDeser);
        assertTrue(dynamicProvider instanceof NullsAsEmptyProvider);

        // Nulls.AS_EMPTY with null valueDeser
        assertNull(deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, null));
    }

    @Test(timeout = 4000)
    public void testHelperUtilities() {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);

        // _neitherNull
        assertTrue(ConcreteStdDeserializer._neitherNull("a", "b"));
        assertFalse(ConcreteStdDeserializer._neitherNull(null, "b"));
        assertFalse(ConcreteStdDeserializer._neitherNull("a", null));
        assertFalse(ConcreteStdDeserializer._neitherNull(null, null));

        // _nonNullNumber
        assertEquals(10, deser._nonNullNumber(10));
        assertEquals(0, deser._nonNullNumber(null));

        // _isIntNumber
        assertTrue(deser._isIntNumber("12345"));
        assertTrue(deser._isIntNumber("-12345"));
        assertTrue(deser._isIntNumber("+12345"));
        assertFalse(deser._isIntNumber(""));
        assertFalse(deser._isIntNumber("12a45"));
        assertFalse(deser._isIntNumber("-"));

        // _hasTextualNull and _isEmptyOrTextualNull
        assertTrue(deser._hasTextualNull("null"));
        assertFalse(deser._hasTextualNull("other"));
        assertTrue(deser._isEmptyOrTextualNull(""));
        assertTrue(deser._isEmptyOrTextualNull("null"));
        assertFalse(deser._isEmptyOrTextualNull("not-null"));

        // _isNaN, _isPosInf, _isNegInf
        assertTrue(deser._isNaN("NaN"));
        assertFalse(deser._isNaN("nan"));
        assertTrue(deser._isPosInf("Infinity"));
        assertTrue(deser._isPosInf("INF"));
        assertFalse(deser._isPosInf("inf"));
        assertTrue(deser._isNegInf("-Infinity"));
        assertTrue(deser._isNegInf("-INF"));
        assertFalse(deser._isNegInf("-inf"));
    }

    @Test(timeout = 4000)
    public void testCoerceIntegral() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);

        // Default coercion without features
        JsonParser p1 = mapper.getFactory().createParser("123");
        p1.nextToken();
        Object res1 = deser._coerceIntegral(p1, mapper.getDeserializationContext());
        assertEquals(java.math.BigInteger.valueOf(123), res1);
        p1.close();

        // Coercion to Long
        ObjectMapper longMapper = new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS);
        JsonParser p2 = longMapper.getFactory().createParser("456");
        p2.nextToken();
        Object res2 = deser._coerceIntegral(p2, longMapper.getDeserializationContext());
        assertEquals(456L, res2);
        p2.close();

        // Coercion to BigInteger
        ObjectMapper bigIntMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonParser p3 = bigIntMapper.getFactory().createParser("789");
        p3.nextToken();
        Object res3 = deser._coerceIntegral(p3, bigIntMapper.getDeserializationContext());
        assertEquals(java.math.BigInteger.valueOf(789), res3);
        p3.close();
    }

    @Test(timeout = 4000)
    public void testUnknownPropertyHandling() throws Exception {
        ConcreteStdDeserializer<Object> deser = new ConcreteStdDeserializer<Object>(Object.class);
        JsonParser p = mapper.getFactory().createParser("{\"unknown\": 123, \"next\": 456}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME ("unknown")
        p.nextToken(); // VALUE_NUMBER_INT (123)

        DeserializationContext ctxt = mapper.getDeserializationContext();
        // Default behavior skips children and returns cleanly
        deser.handleUnknownProperty(p, ctxt, null, "unknown");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.currentToken());
        p.close();
    }
}