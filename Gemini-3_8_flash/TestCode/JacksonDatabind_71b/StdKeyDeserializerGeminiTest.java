package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.EnumResolver;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT TARGETING:
 *    - CharSequence Support: Issue Jackson-databind where `StdKeyDeserializer.forType(CharSequence.class)`
 *      returned null, causing Map deserialization failure when key is `CharSequence`.
 *      Target: `testDefectCharSequenceKeyDeserializerSupported()`.
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - StdKeyDeserializer.forType(Class<?>):
 *      - String.class / Object.class -> StringKD
 *      - UUID, Integer, Long, Date, Calendar, Boolean, Byte, Character, Short, Float, Double, URI, URL, Class
 *      - Locale, Currency (with FromStringDeserializer)
 *      - Unknown/unregistered type -> returns null
 *    - deserializeKey(String, DeserializationContext):
 *      - key == null -> returns null
 *      - valid parsed value -> returns value
 *      - parse exception -> throws weirdKeyException
 *      - result == null with enum & READ_UNKNOWN_ENUM_VALUES_AS_NULL -> returns null
 *      - result == null default -> throws weirdKeyException
 *    - _parse(String, DeserializationContext):
 *      - TYPE_BOOLEAN: "true" -> true, "false" -> false, invalid -> weirdKeyException
 *      - TYPE_BYTE: range min <= val <= 255. Overflows (>255, <-128) -> weirdKeyException
 *      - TYPE_SHORT: Short.MIN_VALUE to Short.MAX_VALUE. Overflows -> weirdKeyException
 *      - TYPE_CHAR: len == 1 -> char, len != 1 -> weirdKeyException
 *      - TYPE_INT / TYPE_LONG: valid parse vs NumberFormatException
 *      - TYPE_FLOAT / TYPE_DOUBLE: valid parse vs NumberFormatException
 *      - TYPE_LOCALE / TYPE_CURRENCY: valid parse vs IOException from deser
 *      - TYPE_DATE: parsed via ctxt.parseDate
 *      - TYPE_CALENDAR: null date vs constructed Calendar
 *      - TYPE_UUID / TYPE_URI / TYPE_URL: valid parse vs parse errors
 *      - TYPE_CLASS: found class vs ClassNotFoundException/weirdKeyException
 *    - StringKD:
 *      - String.class, Object.class cached instances; arbitrary class instance; returns key verbatim.
 *    - DelegatingKD:
 *      - null key handling; delegate returning non-null; delegate throwing exception; delegate returning null.
 *    - EnumKD:
 *      - with factory method vs byNameResolver vs byToStringResolver
 *      - READ_ENUMS_USING_TO_STRING enabled vs disabled
 *      - READ_UNKNOWN_ENUM_VALUES_AS_NULL enabled vs disabled
 *    - StringCtorKeyDeserializer & StringFactoryKeyDeserializer:
 *      - Instantiation via 1-arg reflection constructor and static 1-arg factory method.
 */
public class StdKeyDeserializerGeminiTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;

    public enum SampleEnum {
        ALPHA,
        BETA;

        @Override
        public String toString() {
            return "custom_" + name().toLowerCase(Locale.ROOT);
        }
    }

    public static class CustomCtorKey {
        public final String value;
        public CustomCtorKey(String v) {
            this.value = v;
        }
    }

    public static class CustomFactoryKey {
        public final String value;
        private CustomFactoryKey(String v) {
            this.value = v;
        }
        public static CustomFactoryKey valueOf(String v) {
            return new CustomFactoryKey(v);
        }
    }

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        JsonParser parser = _mapper.getFactory().createParser("{}");
        _context = _mapper.getDeserializationContext();
        if (_context instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            _context = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) _context)
                    .createInstance(_mapper.getDeserializationConfig(), parser, _mapper.getInjectableValues());
        }
    }

    private DeserializationContext createContext(DeserializationFeature feature, boolean state) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(feature, state);
        JsonParser parser = mapper.getFactory().createParser("{}");
        com.fasterxml.jackson.databind.deser.DefaultDeserializationContext dc =
                (com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) mapper.getDeserializationContext();
        return dc.createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J bug where CharSequence was not recognized as a supported key type
     * by StdKeyDeserializer.forType(CharSequence.class).
     */
    @Test(timeout = 4000)
    public void testDefectCharSequenceKeyDeserializerSupported() {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(CharSequence.class);
        assertNotNull("StdKeyDeserializer.forType(CharSequence.class) must return a key deserializer", kd);
        assertEquals(CharSequence.class, kd.getKeyClass());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Factory & Standard Types)
    // =========================================================================

    @Test(timeout = 4000)
    public void testForTypeFactoryMappings() {
        assertNotNull(StdKeyDeserializer.forType(UUID.class));
        assertNotNull(StdKeyDeserializer.forType(Integer.class));
        assertNotNull(StdKeyDeserializer.forType(Long.class));
        assertNotNull(StdKeyDeserializer.forType(Date.class));
        assertNotNull(StdKeyDeserializer.forType(Calendar.class));
        assertNotNull(StdKeyDeserializer.forType(Boolean.class));
        assertNotNull(StdKeyDeserializer.forType(Byte.class));
        assertNotNull(StdKeyDeserializer.forType(Character.class));
        assertNotNull(StdKeyDeserializer.forType(Short.class));
        assertNotNull(StdKeyDeserializer.forType(Float.class));
        assertNotNull(StdKeyDeserializer.forType(Double.class));
        assertNotNull(StdKeyDeserializer.forType(URI.class));
        assertNotNull(StdKeyDeserializer.forType(URL.class));
        assertNotNull(StdKeyDeserializer.forType(Class.class));
        assertNotNull(StdKeyDeserializer.forType(Locale.class));
        assertNotNull(StdKeyDeserializer.forType(Currency.class));
        assertNull(StdKeyDeserializer.forType(java.awt.Point.class));
    }

    @Test(timeout = 4000)
    public void testBooleanDeserialization() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        assertEquals(Boolean.TRUE, kd.deserializeKey("true", _context));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", _context));
    }

    @Test(timeout = 4000)
    public void testByteDeserializationValid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        assertEquals(Byte.valueOf((byte) 0), kd.deserializeKey("0", _context));
        assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", _context));
        assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", _context));
        // JACKSON-804: values up to 255 permitted
        assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", _context));
    }

    @Test(timeout = 4000)
    public void testShortDeserializationValid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        assertEquals(Short.valueOf((short) 0), kd.deserializeKey("0", _context));
        assertEquals(Short.valueOf(Short.MAX_VALUE), kd.deserializeKey("32767", _context));
        assertEquals(Short.valueOf(Short.MIN_VALUE), kd.deserializeKey("-32768", _context));
    }

    @Test(timeout = 4000)
    public void testCharacterDeserializationValid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        assertEquals(Character.valueOf('A'), kd.deserializeKey("A", _context));
        assertEquals(Character.valueOf(' '), kd.deserializeKey(" ", _context));
    }

    @Test(timeout = 4000)
    public void testIntAndLongDeserialization() throws Exception {
        StdKeyDeserializer intKd = StdKeyDeserializer.forType(Integer.class);
        assertEquals(Integer.valueOf(12345), intKd.deserializeKey("12345", _context));
        assertEquals(Integer.valueOf(-999), intKd.deserializeKey("-999", _context));

        StdKeyDeserializer longKd = StdKeyDeserializer.forType(Long.class);
        assertEquals(Long.valueOf(123456789012345L), longKd.deserializeKey("123456789012345", _context));
    }

    @Test(timeout = 4000)
    public void testFloatAndDoubleDeserialization() throws Exception {
        StdKeyDeserializer floatKd = StdKeyDeserializer.forType(Float.class);
        assertEquals(Float.valueOf(3.14f), floatKd.deserializeKey("3.14", _context));

        StdKeyDeserializer doubleKd = StdKeyDeserializer.forType(Double.class);
        assertEquals(Double.valueOf(2.718281828459), doubleKd.deserializeKey("2.718281828459", _context));
    }

    @Test(timeout = 4000)
    public void testLocaleAndCurrencyDeserialization() throws Exception {
        StdKeyDeserializer locKd = StdKeyDeserializer.forType(Locale.class);
        assertEquals(Locale.US, locKd.deserializeKey("en_US", _context));

        StdKeyDeserializer currKd = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.getInstance("USD"), currKd.deserializeKey("USD", _context));
    }

    @Test(timeout = 4000)
    public void testDateAndCalendarDeserialization() throws Exception {
        StdKeyDeserializer dateKd = StdKeyDeserializer.forType(Date.class);
        Object dateObj = dateKd.deserializeKey("2020-01-01T00:00:00.000+0000", _context);
        assertTrue(dateObj instanceof Date);

        StdKeyDeserializer calKd = StdKeyDeserializer.forType(Calendar.class);
        Object calObj = calKd.deserializeKey("2020-01-01T00:00:00.000+0000", _context);
        assertTrue(calObj instanceof Calendar);
    }

    @Test(timeout = 4000)
    public void testUUIDAndUriAndUrlDeserialization() throws Exception {
        StdKeyDeserializer uuidKd = StdKeyDeserializer.forType(UUID.class);
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, uuidKd.deserializeKey(uuid.toString(), _context));

        StdKeyDeserializer uriKd = StdKeyDeserializer.forType(URI.class);
        assertEquals(URI.create("http://localhost:8080/path"), uriKd.deserializeKey("http://localhost:8080/path", _context));

        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        assertEquals(new URL("http://localhost:8080/path"), urlKd.deserializeKey("http://localhost:8080/path", _context));
    }

    @Test(timeout = 4000)
    public void testClassDeserialization() throws Exception {
        StdKeyDeserializer classKd = StdKeyDeserializer.forType(Class.class);
        assertEquals(String.class, classKd.deserializeKey("java.lang.String", _context));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullKeyReturnsNull() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        assertNull(kd.deserializeKey(null, _context));
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testByteOverflowPositive() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("256", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testByteOverflowNegative() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("-129", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testShortOverflowPositive() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("32768", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testShortOverflowNegative() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("-32769", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testCharEmptyStringThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testCharMultipleCharactersThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("AB", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testBooleanInvalidStringThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        kd.deserializeKey("True", _context);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testIntNonNumericThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        kd.deserializeKey("abc", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testLongNonNumericThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        kd.deserializeKey("not_a_long", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDoubleInvalidThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Double.class);
        kd.deserializeKey("invalid_double", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testURLInvalidThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URL.class);
        kd.deserializeKey("not_a_valid_protocol://test", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testClassNonExistentThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        kd.deserializeKey("com.nonexistent.Class12345", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testCurrencyInvalidThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Currency.class);
        kd.deserializeKey("INVALID_CURR", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testUUIDInvalidThrows() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        kd.deserializeKey("invalid-uuid-format", _context);
    }

    // =========================================================================
    // Partition E: Subclasses & Specialized Deserializers
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringKDVariants() throws Exception {
        StdKeyDeserializer.StringKD kdStr = StdKeyDeserializer.StringKD.forType(String.class);
        assertEquals("testKey", kdStr.deserializeKey("testKey", _context));
        assertEquals(String.class, kdStr.getKeyClass());

        StdKeyDeserializer.StringKD kdObj = StdKeyDeserializer.StringKD.forType(Object.class);
        assertEquals("testKey2", kdObj.deserializeKey("testKey2", _context));
        assertEquals(Object.class, kdObj.getKeyClass());

        StdKeyDeserializer.StringKD kdCustom = StdKeyDeserializer.StringKD.forType(StringBuilder.class);
        assertEquals("testKey3", kdCustom.deserializeKey("testKey3", _context));
        assertEquals(StringBuilder.class, kdCustom.getKeyClass());
    }

    @Test(timeout = 4000)
    public void testDelegatingKDNormalAndNull() throws Exception {
        JsonDeserializer<String> mockDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                return "delegatedValue";
            }
        };

        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(String.class, mockDeser);
        assertEquals(String.class, delegatingKD.getKeyClass());
        assertNull(delegatingKD.deserializeKey(null, _context));
        assertEquals("delegatedValue", delegatingKD.deserializeKey("anyKey", _context));
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDelegatingKDReturningNullThrows() throws Exception {
        JsonDeserializer<String> nullDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(String.class, nullDeser);
        delegatingKD.deserializeKey("key", _context);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDelegatingKDExceptionThrows() throws Exception {
        JsonDeserializer<String> errorDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                throw new RuntimeException("simulated failure");
            }
        };
        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(String.class, errorDeser);
        delegatingKD.deserializeKey("key", _context);
    }

    @Test(timeout = 4000)
    public void testEnumKDSuccessAndFailures() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(SampleEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKD = new StdKeyDeserializer.EnumKD(er, null);
        assertEquals(SampleEnum.class, enumKD.getKeyClass());

        // Standard name lookup
        assertEquals(SampleEnum.ALPHA, enumKD.deserializeKey("ALPHA", _context));
        assertEquals(SampleEnum.BETA, enumKD.deserializeKey("BETA", _context));

        // Unknown enum with READ_UNKNOWN_ENUM_VALUES_AS_NULL disabled -> throws
        try {
            enumKD.deserializeKey("UNKNOWN", _context);
            fail("Expected JsonMappingException for unknown enum");
        } catch (JsonMappingException expected) {
            // Success
        }

        // Unknown enum with READ_UNKNOWN_ENUM_VALUES_AS_NULL enabled -> returns null
        DeserializationContext ctxtWithNullEnum = createContext(
                DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        assertNull(enumKD.deserializeKey("UNKNOWN", ctxtWithNullEnum));
    }

    @Test(timeout = 4000)
    public void testEnumKDWithToStringResolver() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(SampleEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKD = new StdKeyDeserializer.EnumKD(er, null);

        DeserializationContext ctxtToString = createContext(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        assertEquals(SampleEnum.ALPHA, enumKD.deserializeKey("custom_alpha", ctxtToString));
        assertEquals(SampleEnum.BETA, enumKD.deserializeKey("custom_beta", ctxtToString));
    }

    @Test(timeout = 4000)
    public void testEnumKDWithAnnotatedFactory() throws Exception {
        Method factoryMethod = SampleEnum.class.getMethod("valueOf", String.class);
        AnnotatedMethod annotatedFactory = new AnnotatedMethod(null, factoryMethod, null, null);
        EnumResolver er = EnumResolver.constructUnsafe(SampleEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKD = new StdKeyDeserializer.EnumKD(er, annotatedFactory);

        assertEquals(SampleEnum.ALPHA, enumKD.deserializeKey("ALPHA", _context));
        try {
            enumKD.deserializeKey("NON_EXISTING", _context);
            fail("Expected JsonMappingException for invalid enum via factory");
        } catch (JsonMappingException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<?> ctor = CustomCtorKey.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd =
                new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        assertEquals(CustomCtorKey.class, kd.getKeyClass());

        Object result = kd.deserializeKey("helloCtor", _context);
        assertNotNull(result);
        assertTrue(result instanceof CustomCtorKey);
        assertEquals("helloCtor", ((CustomCtorKey) result).value);
    }

    @Test(timeout = 4000)
    public void testStringFactoryKeyDeserializer() throws Exception {
        Method factory = CustomFactoryKey.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd =
                new StdKeyDeserializer.StringFactoryKeyDeserializer(factory);
        assertEquals(CustomFactoryKey.class, kd.getKeyClass());

        Object result = kd.deserializeKey("helloFactory", _context);
        assertNotNull(result);
        assertTrue(result instanceof CustomFactoryKey);
        assertEquals("helloFactory", ((CustomFactoryKey) result).value);
    }

    @Test(timeout = 4000)
    public void testHelperProtectedMethodsDirectly() {
        StdKeyDeserializer kd = new StdKeyDeserializer(StdKeyDeserializer.TYPE_INT, Integer.class);
        assertEquals(42, kd._parseInt("42"));
        assertEquals(9999999999L, kd._parseLong("9999999999"));
        assertEquals(12.34, kd._parseDouble("12.34"), 0.0001);
    }
}