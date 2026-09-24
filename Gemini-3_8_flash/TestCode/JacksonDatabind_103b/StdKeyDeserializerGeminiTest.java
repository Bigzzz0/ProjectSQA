package com.fasterxml.jackson.databind.deser.std;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: StdKeyDeserializer (and nested KeyDeserializers)
 *
 * 1. Factory Dispatch Branches (forType):
 *    - Core string/object types: String.class, Object.class, CharSequence.class -> StringKD
 *    - Standard primitives & wrappers: Boolean, Byte, Character, Short, Integer, Long, Float, Double
 *    - Date & Calendar: Date, Calendar
 *    - Identification & Net: UUID, URI, URL, Class
 *    - Internationalization & Base types: Locale, Currency, byte[]
 *    - Unrecognized/Unsupported raw classes: returns null
 *
 * 2. Key Deserialization & Core Parsing (_parse & deserializeKey):
 *    - null key input -> returns null immediately
 *    - TYPE_BOOLEAN: "true" -> Boolean.TRUE, "false" -> Boolean.FALSE, other -> handleWeirdKey
 *    - TYPE_BYTE: valid in [-128, 255] (unsigned byte support), < -128 or > 255 -> overflow handleWeirdKey, invalid int -> weirdKey
 *    - TYPE_SHORT: valid in [-32768, 32767], < -32768 or > 32767 -> overflow handleWeirdKey, invalid int -> weirdKey
 *    - TYPE_CHAR: key.length() == 1 -> char, length != 1 -> handleWeirdKey
 *    - TYPE_INT / TYPE_LONG / TYPE_FLOAT / TYPE_DOUBLE: standard parsing & parse failure handling
 *    - TYPE_LOCALE / TYPE_CURRENCY: helper _deser invocation; IllegalArgumentException caught -> _weirdKey
 *    - TYPE_DATE / TYPE_CALENDAR: ctxt.parseDate & ctxt.constructCalendar invocation
 *    - TYPE_UUID / TYPE_URI / TYPE_URL: parsing + MalformedURLException/Exception caught -> _weirdKey
 *    - TYPE_CLASS: ctxt.findClass; Exception caught -> handleWeirdKey
 *    - TYPE_BYTE_ARRAY: Base64 decode; IllegalArgumentException caught -> _weirdKey
 *    - Default (invalid kind): throws IllegalStateException
 *    - Fallback branch: result == null with isEnum() and READ_UNKNOWN_ENUM_VALUES_AS_NULL -> returns null
 *    - Fallback branch: result == null without enum feature -> handleWeirdKey
 *
 * 3. Inner KeyDeserializers:
 *    - StringKD: caching verification (sString, sObject), nominalType passthrough
 *    - DelegatingKD: null key, delegation to custom JsonDeserializer, handling null result, handling thrown exception
 *    - EnumKD:
 *        * factory method path (with success & exception rethrow via ClassUtil.unwrapAndThrowAsIAE)
 *        * standard by-name resolver lookup
 *        * READ_ENUMS_USING_TO_STRING resolver lookup
 *        * READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE fallback
 *        * READ_UNKNOWN_ENUM_VALUES_AS_NULL fallback
 *        * Failure path calling handleWeirdKey
 *    - StringCtorKeyDeserializer: single String arg constructor invocation
 *    - StringFactoryKeyDeserializer: static factory method invocation
 *
 * 4. Ground Truth Defect Targeted (Defects4J):
 *    - BasicExceptionTest::testLocationAddition failure:
 *      When an invalid key (e.g., Enum key) is encountered, `_parse` triggers `handleWeirdKey` which throws
 *      JsonMappingException / InvalidFormatException. `deserializeKey` catches `Exception re` and calls
 *      `handleWeirdKey` a second time, resulting in double-wrapped messages containing:
 *      "problem: (com.fasterxml.jackson.databind.exc.InvalidFormatException)" and duplicate location markers ('at [').
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.EnumResolver;

public class StdKeyDeserializerGeminiTest {

    public enum TestABC {
        A, B, C
    }

    public enum DefaultedEnum {
        VAL1,
        VAL2,
        @JsonEnumDefaultValue
        UNKNOWN_DEFAULT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum FactoryEnum {
        ONE, TWO;

        @JsonCreator
        public static FactoryEnum fromKey(String key) {
            if ("first".equals(key)) return ONE;
            if ("second".equals(key)) return TWO;
            throw new IllegalArgumentException("Unknown key: " + key);
        }
    }

    public static class StringCtorTarget {
        final String value;
        public StringCtorTarget(String value) {
            this.value = value;
        }
    }

    public static class StringFactoryTarget {
        final String value;
        private StringFactoryTarget(String value) {
            this.value = value;
        }
        public static StringFactoryTarget create(String value) {
            return new StringFactoryTarget(value);
        }
    }

    private DeserializationContext createCtxt(ObjectMapper mapper) throws Exception {
        JsonParser parser = mapper.getFactory().createParser("{}");
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), parser, null);
    }

    /*
    /**********************************************************
    /* Partition A: Factory Method (forType) & Structural Dispatch
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testForTypeAllSupportedClasses() {
        assertTrue(StdKeyDeserializer.forType(String.class) instanceof StdKeyDeserializer.StringKD);
        assertTrue(StdKeyDeserializer.forType(Object.class) instanceof StdKeyDeserializer.StringKD);
        assertTrue(StdKeyDeserializer.forType(CharSequence.class) instanceof StdKeyDeserializer.StringKD);

        assertEquals(StdKeyDeserializer.TYPE_UUID, StdKeyDeserializer.forType(UUID.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_INT, StdKeyDeserializer.forType(Integer.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_LONG, StdKeyDeserializer.forType(Long.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_DATE, StdKeyDeserializer.forType(Date.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_CALENDAR, StdKeyDeserializer.forType(Calendar.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_BOOLEAN, StdKeyDeserializer.forType(Boolean.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_BYTE, StdKeyDeserializer.forType(Byte.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_CHAR, StdKeyDeserializer.forType(Character.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_SHORT, StdKeyDeserializer.forType(Short.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_FLOAT, StdKeyDeserializer.forType(Float.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_DOUBLE, StdKeyDeserializer.forType(Double.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_URI, StdKeyDeserializer.forType(URI.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_URL, StdKeyDeserializer.forType(URL.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_CLASS, StdKeyDeserializer.forType(Class.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_LOCALE, StdKeyDeserializer.forType(Locale.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_CURRENCY, StdKeyDeserializer.forType(Currency.class)._kind);
        assertEquals(StdKeyDeserializer.TYPE_BYTE_ARRAY, StdKeyDeserializer.forType(byte[].class)._kind);

        assertNull(StdKeyDeserializer.forType(Void.class));
        assertNull(StdKeyDeserializer.forType(List.class));
        assertNull(StdKeyDeserializer.forType(TestABC.class));
    }

    @Test(timeout = 4000)
    public void testStringKDInstances() throws Exception {
        StdKeyDeserializer.StringKD kdString1 = StdKeyDeserializer.StringKD.forType(String.class);
        StdKeyDeserializer.StringKD kdString2 = StdKeyDeserializer.StringKD.forType(String.class);
        assertSame(kdString1, kdString2);

        StdKeyDeserializer.StringKD kdObj1 = StdKeyDeserializer.StringKD.forType(Object.class);
        StdKeyDeserializer.StringKD kdObj2 = StdKeyDeserializer.StringKD.forType(Object.class);
        assertSame(kdObj1, kdObj2);

        StdKeyDeserializer.StringKD kdSeq = StdKeyDeserializer.StringKD.forType(CharSequence.class);
        assertNotSame(kdString1, kdSeq);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);
        assertEquals("sampleKey", kdString1.deserializeKey("sampleKey", ctxt));
        assertNull(kdString1.deserializeKey(null, ctxt));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Primitives
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testBooleanKeyDeserialization() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertEquals(Boolean.TRUE, kd.deserializeKey("true", ctxt));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", ctxt));

        try {
            kd.deserializeKey("TRUE", ctxt);
            fail("Should fail for case-sensitive boolean");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("value not 'true' or 'false'"));
        }
    }

    @Test(timeout = 4000)
    public void testByteKeyDeserializationBoundaries() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertEquals((byte) 0, kd.deserializeKey("0", ctxt));
        assertEquals((byte) 127, kd.deserializeKey("127", ctxt));
        assertEquals((byte) -128, kd.deserializeKey("-128", ctxt));
        assertEquals((byte) -1, kd.deserializeKey("255", ctxt)); // unsigned 255 support

        try {
            kd.deserializeKey("256", ctxt);
            fail("Expected overflow error for 256");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }

        try {
            kd.deserializeKey("-129", ctxt);
            fail("Expected overflow error for -129");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testShortKeyDeserializationBoundaries() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertEquals((short) 32767, kd.deserializeKey("32767", ctxt));
        assertEquals((short) -32768, kd.deserializeKey("-32768", ctxt));

        try {
            kd.deserializeKey("32768", ctxt);
            fail("Expected overflow for 32768");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }

        try {
            kd.deserializeKey("-32769", ctxt);
            fail("Expected overflow for -32769");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testCharacterKeyDeserialization() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertEquals('Z', kd.deserializeKey("Z", ctxt));

        try {
            kd.deserializeKey("", ctxt);
            fail("Expected error for 0-char string");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("can only convert 1-character Strings"));
        }

        try {
            kd.deserializeKey("AB", ctxt);
            fail("Expected error for multi-char string");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("can only convert 1-character Strings"));
        }
    }

    @Test(timeout = 4000)
    public void testNumericPrimitivesAndFloatingPoints() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        StdKeyDeserializer intKd = StdKeyDeserializer.forType(Integer.class);
        assertEquals(12345, intKd.deserializeKey("12345", ctxt));

        StdKeyDeserializer longKd = StdKeyDeserializer.forType(Long.class);
        assertEquals(9876543210123L, longKd.deserializeKey("9876543210123", ctxt));

        StdKeyDeserializer floatKd = StdKeyDeserializer.forType(Float.class);
        assertEquals(Float.valueOf(3.14f), floatKd.deserializeKey("3.14", ctxt));

        StdKeyDeserializer doubleKd = StdKeyDeserializer.forType(Double.class);
        assertEquals(Double.valueOf(2.71828), doubleKd.deserializeKey("2.71828", ctxt));
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDefectLocationAdditionAndNoDoubleWrappingOnWeirdKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"XYZ\": 123}", new TypeReference<Map<TestABC, Integer>>() {});
            fail("Expected failure on unknown enum map key");
        } catch (JsonProcessingException e) {
            String msg = e.getMessage();
            assertNotNull(msg);

            // Defect Assertion: Verify no nested double-wrapping of JsonProcessingException
            assertFalse("Exception message must not double-wrap with 'problem: (com.fasterxml.jackson...)'",
                    msg.contains("problem: (com.fasterxml.jackson"));

            // Verify that Jackson error location was not appended multiple times in the exception message
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf("at [", idx)) != -1) {
                count++;
                idx += 4;
            }
            assertEquals("Should only get one 'at [' location marker, got: " + count + " in: " + msg, 1, count);
        }
    }

    @Test(timeout = 4000)
    public void testEnumKDDirectWeirdKeyDoubleWrapCheck() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);
        EnumResolver er = EnumResolver.constructUnsafe(TestABC.class, mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(er, null);

        try {
            enumKd.deserializeKey("UNKNOWN_VAL", ctxt);
            fail("Expected exception for unknown enum value");
        } catch (JsonProcessingException e) {
            String msg = e.getMessage();
            assertFalse("Direct enum deserializeKey must not double-wrap exception",
                    msg.contains("problem: (com.fasterxml.jackson"));
            assertTrue(msg.contains("not one of values"));
        }
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullKeyHandling() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertNull(kd.deserializeKey(null, ctxt));
    }

    @Test(timeout = 4000)
    public void testClassParsing() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertEquals(String.class, kd.deserializeKey("java.lang.String", ctxt));

        try {
            kd.deserializeKey("com.nonexistent.FakeClassXYZ", ctxt);
            fail("Expected failure for non-existent class");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("unable to parse key as Class"));
        }
    }

    @Test(timeout = 4000)
    public void testUriUrlUuidParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        StdKeyDeserializer uriKd = StdKeyDeserializer.forType(URI.class);
        assertEquals(URI.create("https://example.com"), uriKd.deserializeKey("https://example.com", ctxt));
        try {
            uriKd.deserializeKey("http:// invalid uri", ctxt);
            fail("Expected failure on invalid URI");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }

        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        assertEquals(new URL("https://example.com"), urlKd.deserializeKey("https://example.com", ctxt));
        try {
            urlKd.deserializeKey("not-a-valid-url-no-protocol", ctxt);
            fail("Expected failure on invalid URL");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }

        StdKeyDeserializer uuidKd = StdKeyDeserializer.forType(UUID.class);
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, uuidKd.deserializeKey(uuid.toString(), ctxt));
        try {
            uuidKd.deserializeKey("not-a-uuid", ctxt);
            fail("Expected failure on invalid UUID");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }
    }

    @Test(timeout = 4000)
    public void testByteArrayAndDateParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        StdKeyDeserializer bytesKd = StdKeyDeserializer.forType(byte[].class);
        byte[] decoded = (byte[]) bytesKd.deserializeKey("AQID", ctxt);
        assertArrayEquals(new byte[]{1, 2, 3}, decoded);

        try {
            bytesKd.deserializeKey("!!!not_base_64!!!", ctxt);
            fail("Expected failure for malformed base64");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }

        StdKeyDeserializer dateKd = StdKeyDeserializer.forType(Date.class);
        Object parsedDate = dateKd.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        assertTrue(parsedDate instanceof Date);

        StdKeyDeserializer calKd = StdKeyDeserializer.forType(Calendar.class);
        Object parsedCal = calKd.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        assertTrue(parsedCal instanceof Calendar);
    }

    @Test(timeout = 4000)
    public void testLocaleAndCurrencyParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        StdKeyDeserializer localeKd = StdKeyDeserializer.forType(Locale.class);
        assertEquals(Locale.US, localeKd.deserializeKey("en_US", ctxt));

        StdKeyDeserializer currKd = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.getInstance("USD"), currKd.deserializeKey("USD", ctxt));

        try {
            currKd.deserializeKey("INVALID_CURR", ctxt);
            fail("Expected failure for invalid currency");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownKindThrowsIllegalStateException() throws Exception {
        StdKeyDeserializer unknownKd = new StdKeyDeserializer(999, Integer.class);
        assertEquals(Integer.class, unknownKd.getKeyClass());
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        try {
            unknownKd._parse("123", ctxt);
            fail("Expected IllegalStateException for kind 999");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Internal error: unknown key type"));
        }
    }

    @Test(timeout = 4000)
    public void testEnumNullReturnHandlingWithFeature() throws Exception {
        StdKeyDeserializer nullReturningEnumKd = new StdKeyDeserializer(1, TestABC.class) {
            private static final long serialVersionUID = 1L;
            @Override
            protected Object _parse(String key, DeserializationContext ctxt) {
                return null;
            }
        };

        ObjectMapper mapperEnabled = new ObjectMapper();
        mapperEnabled.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        DeserializationContext ctxtEnabled = createCtxt(mapperEnabled);
        assertNull(nullReturningEnumKd.deserializeKey("any", ctxtEnabled));

        ObjectMapper mapperDisabled = new ObjectMapper();
        mapperDisabled.disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        DeserializationContext ctxtDisabled = createCtxt(mapperDisabled);
        try {
            nullReturningEnumKd.deserializeKey("any", ctxtDisabled);
            fail("Expected failure when READ_UNKNOWN_ENUM_VALUES_AS_NULL is disabled");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Specialized Subclasses
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDelegatingKDWorkflow() throws Exception {
        JsonDeserializer<String> customDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if ("null_val".equals(text)) return null;
                if ("throw_val".equals(text)) throw new RuntimeException("delegating error");
                return "TRANSFORMED:" + text;
            }
        };

        StdKeyDeserializer.DelegatingKD delegatingKD = new StdKeyDeserializer.DelegatingKD(String.class, customDeser);
        assertEquals(String.class, delegatingKD.getKeyClass());

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        assertNull(delegatingKD.deserializeKey(null, ctxt));
        assertEquals("TRANSFORMED:valid", delegatingKD.deserializeKey("valid", ctxt));

        try {
            delegatingKD.deserializeKey("null_val", ctxt);
            fail("Expected handleWeirdKey when delegate returns null");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }

        try {
            delegatingKD.deserializeKey("throw_val", ctxt);
            fail("Expected handleWeirdKey when delegate throws exception");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("delegating error"));
        }
    }

    @Test(timeout = 4000)
    public void testEnumKDFeaturesAndFallbacks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Feature: READ_ENUMS_USING_TO_STRING
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        Map<DefaultedEnum, Integer> map1 = mapper.readValue(
                "{\"val1\": 10}", new TypeReference<Map<DefaultedEnum, Integer>>() {});
        assertEquals(Integer.valueOf(10), map1.get(DefaultedEnum.VAL1));

        // 2. Feature: READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
        mapper.disable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        Map<DefaultedEnum, Integer> map2 = mapper.readValue(
                "{\"UNKNOWN_XYZ\": 20}", new TypeReference<Map<DefaultedEnum, Integer>>() {});
        assertEquals(Integer.valueOf(20), map2.get(DefaultedEnum.UNKNOWN_DEFAULT));

        // 3. Feature: READ_UNKNOWN_ENUM_VALUES_AS_NULL
        mapper.disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        Map<TestABC, Integer> map3 = mapper.readValue(
                "{\"UNKNOWN_KEY\": 30}", new TypeReference<Map<TestABC, Integer>>() {});
        assertTrue(map3.containsKey(null));
        assertEquals(Integer.valueOf(30), map3.get(null));
    }

    @Test(timeout = 4000)
    public void testEnumKDWithFactoryMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<FactoryEnum, Integer> mapSuccess = mapper.readValue(
                "{\"first\": 1, \"second\": 2}", new TypeReference<Map<FactoryEnum, Integer>>() {});
        assertEquals(Integer.valueOf(1), mapSuccess.get(FactoryEnum.ONE));
        assertEquals(Integer.valueOf(2), mapSuccess.get(FactoryEnum.TWO));

        try {
            mapper.readValue("{\"invalid\": 3}", new TypeReference<Map<FactoryEnum, Integer>>() {});
            fail("Expected error on invalid creator key");
        } catch (JsonProcessingException expected) {
            assertTrue(expected.getMessage().contains("Unknown key: invalid"));
        }
    }

    @Test(timeout = 4000)
    public void testStringCtorAndStringFactoryKeyDeserializers() throws Exception {
        Constructor<?> ctor = StringCtorTarget.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer ctorKd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createCtxt(mapper);

        Object ctorResult = ctorKd._parse("ctorVal", ctxt);
        assertTrue(ctorResult instanceof StringCtorTarget);
        assertEquals("ctorVal", ((StringCtorTarget) ctorResult).value);

        Method factoryMethod = StringFactoryTarget.class.getMethod("create", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer factoryKd = new StdKeyDeserializer.StringFactoryKeyDeserializer(factoryMethod);

        Object factoryResult = factoryKd._parse("factoryVal", ctxt);
        assertTrue(factoryResult instanceof StringFactoryTarget);
        assertEquals("factoryVal", ((StringFactoryTarget) factoryResult).value);
    }

    @Test(timeout = 4000)
    public void testProtectedHelperMethodsDirect() {
        StdKeyDeserializer kd = new StdKeyDeserializer(StdKeyDeserializer.TYPE_INT, Integer.class);
        assertEquals(101, kd._parseInt("101"));
        assertEquals(202L, kd._parseLong("202"));
        assertEquals(303.5, kd._parseDouble("303.5"), 0.0001);
    }
}