/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer
 * Nested Classes Tested:
 *   - StdKeyDeserializer (core dispatcher & primitive/standard type handlers)
 *   - StdKeyDeserializer.StringKD
 *   - StdKeyDeserializer.DelegatingKD
 *   - StdKeyDeserializer.EnumKD
 *   - StdKeyDeserializer.StringCtorKeyDeserializer
 *   - StdKeyDeserializer.StringFactoryKeyDeserializer
 *
 * Decision Branches & Partitions:
 * - Partition A (Factory & Core Types):
 *     forType() branches: String, Object, UUID, Integer, Long, Date, Calendar,
 *     Boolean, Byte, Character, Short, Float, Double, URI, URL, Class, Locale, Currency, unknown (null).
 * - Partition B (Boundary & Overflow Handling):
 *     TYPE_BYTE: values < -128, [-128..255] unsigned byte support, > 255.
 *     TYPE_SHORT: values < -32768, [-32768..32767], > 32767.
 *     TYPE_CHAR: key.length() == 1 vs != 1.
 *     TYPE_BOOLEAN: "true", "false", invalid representation.
 *     null key handling -> returns null immediately across all deserializers.
 * - Partition C (Defect-Targeted Zone):
 *     Target: Defects4J / Jackson-databind Custom Enum Key Deserialization issue
 *     (TestCustomEnumKeyDeserializer::testCustomEnumKeySerializerWithPolymorphic).
 *     Triggers DelegatingKD exception mapping ("not a valid representation: %s") when delegate
 *     parser state or enum resolution fails.
 *     EnumKD: _byNameResolver vs _byToStringResolver (READ_ENUMS_USING_TO_STRING),
 *     READ_UNKNOWN_ENUM_VALUES_AS_NULL enabled vs disabled, custom @JsonCreator AnnotatedMethod factory.
 * - Partition D (Exception & Defensive Paths):
 *     Malformed URLs, invalid URIs, invalid UUIDs, unparseable Class names,
 *     StringCtorKeyDeserializer / StringFactoryKeyDeserializer invocation errors.
 * - Partition E (Object Lifecycle & Subtype Coverage):
 *     Reflection helper methods: _parseInt, _parseLong, _parseDouble, getKeyClass().
 */

package com.fasterxml.jackson.databind.deser.std;

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
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.EnumResolver;

public class StdKeyDeserializerGeminiTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    // Test Enums for EnumKD and Defect Reproduction
    public enum SimpleTestEnum {
        ALPHA,
        BETA;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum SuperTypeEnum {
        FOO,
        BAR
    }

    // Class for String constructor reflection test
    public static class KeyWithCtor {
        final String val;
        public KeyWithCtor(String v) { this.val = v; }
        public String getVal() { return val; }
    }

    // Class for String factory reflection test
    public static class KeyWithFactory {
        final String val;
        private KeyWithFactory(String v) { this.val = v; }
        public static KeyWithFactory valueOf(String v) {
            if ("INVALID".equals(v)) {
                throw new IllegalArgumentException("Invalid key value");
            }
            return new KeyWithFactory(v);
        }
        public String getVal() { return val; }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Type Factory Resolution
    // =========================================================================

    @Test(timeout = 4000)
    public void testForTypeStandardMapping() {
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
        assertNotNull(StdKeyDeserializer.forType(String.class));
        assertNotNull(StdKeyDeserializer.forType(Object.class));

        // Unsupported raw type should yield null
        assertNull(StdKeyDeserializer.forType(Void.class));
        assertNull(StdKeyDeserializer.forType(StdKeyDeserializerGeminiTest.class));
    }

    @Test(timeout = 4000)
    public void testBooleanDeserialization() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        assertEquals(Boolean.TRUE, kd.deserializeKey("true", ctxt));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", ctxt));
    }

    @Test(timeout = 4000)
    public void testIntAndLongDeserialization() throws Exception {
        StdKeyDeserializer kdInt = StdKeyDeserializer.forType(Integer.class);
        assertEquals(12345, kdInt.deserializeKey("12345", ctxt));
        assertEquals(-678, kdInt.deserializeKey("-678", ctxt));

        StdKeyDeserializer kdLong = StdKeyDeserializer.forType(Long.class);
        assertEquals(9876543210L, kdLong.deserializeKey("9876543210", ctxt));
    }

    @Test(timeout = 4000)
    public void testFloatAndDoubleDeserialization() throws Exception {
        StdKeyDeserializer kdFloat = StdKeyDeserializer.forType(Float.class);
        assertEquals(Float.valueOf(3.14f), kdFloat.deserializeKey("3.14", ctxt));

        StdKeyDeserializer kdDouble = StdKeyDeserializer.forType(Double.class);
        assertEquals(Double.valueOf(2.71828), kdDouble.deserializeKey("2.71828", ctxt));
    }

    @Test(timeout = 4000)
    public void testUUIDAndUriAndUrlDeserialization() throws Exception {
        StdKeyDeserializer kdUuid = StdKeyDeserializer.forType(UUID.class);
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, kdUuid.deserializeKey(uuid.toString(), ctxt));

        StdKeyDeserializer kdUri = StdKeyDeserializer.forType(URI.class);
        assertEquals(URI.create("https://example.com/test"), kdUri.deserializeKey("https://example.com/test", ctxt));

        StdKeyDeserializer kdUrl = StdKeyDeserializer.forType(URL.class);
        assertEquals(new URL("https://example.com"), kdUrl.deserializeKey("https://example.com", ctxt));
    }

    @Test(timeout = 4000)
    public void testLocaleAndCurrencyDeserialization() throws Exception {
        StdKeyDeserializer kdLocale = StdKeyDeserializer.forType(Locale.class);
        assertEquals(Locale.CANADA, kdLocale.deserializeKey(Locale.CANADA.toString(), ctxt));

        StdKeyDeserializer kdCurrency = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.getInstance("USD"), kdCurrency.deserializeKey("USD", ctxt));
    }

    @Test(timeout = 4000)
    public void testClassDeserialization() throws Exception {
        StdKeyDeserializer kdClass = StdKeyDeserializer.forType(Class.class);
        assertEquals(String.class, kdClass.deserializeKey(String.class.getName(), ctxt));
    }

    @Test(timeout = 4000)
    public void testDateAndCalendarDeserialization() throws Exception {
        StdKeyDeserializer kdDate = StdKeyDeserializer.forType(Date.class);
        Object dateObj = kdDate.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        assertNotNull(dateObj);
        assertTrue(dateObj instanceof Date);

        StdKeyDeserializer kdCal = StdKeyDeserializer.forType(Calendar.class);
        Object calObj = kdCal.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        assertNotNull(calObj);
        assertTrue(calObj instanceof Calendar);
    }

    @Test(timeout = 4000)
    public void testStringKDVariants() throws Exception {
        StdKeyDeserializer kdStr = StdKeyDeserializer.forType(String.class);
        assertEquals("sampleKey", kdStr.deserializeKey("sampleKey", ctxt));
        assertNull(kdStr.deserializeKey(null, ctxt));

        StdKeyDeserializer kdObj = StdKeyDeserializer.forType(Object.class);
        assertEquals("sampleKey2", kdObj.deserializeKey("sampleKey2", ctxt));

        StdKeyDeserializer customStrKD = StdKeyDeserializer.StringKD.forType(CharSequence.class);
        assertEquals("customCharSeq", customStrKD.deserializeKey("customCharSeq", ctxt));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullKeyYieldsNull() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        assertNull(kd.deserializeKey(null, ctxt));

        StdKeyDeserializer kdDelegating = new StdKeyDeserializer.DelegatingKD(String.class, null);
        assertNull(kdDelegating.deserializeKey(null, ctxt));
    }

    @Test(timeout = 4000)
    public void testByteBoundariesAndOverflow() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", ctxt));
        assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", ctxt));
        // Jackson allows unsigned byte values up to 255:
        assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", ctxt));

        // Beyond 255 should trigger handleWeirdKey
        try {
            kd.deserializeKey("256", ctxt);
            fail("Expected exception for byte overflow > 255");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }

        // Below -128 should trigger handleWeirdKey
        try {
            kd.deserializeKey("-129", ctxt);
            fail("Expected exception for byte overflow < -128");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testShortBoundariesAndOverflow() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        assertEquals(Short.valueOf((short) -32768), kd.deserializeKey("-32768", ctxt));
        assertEquals(Short.valueOf((short) 32767), kd.deserializeKey("32767", ctxt));

        try {
            kd.deserializeKey("32768", ctxt);
            fail("Expected exception for short overflow > 32767");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }

        try {
            kd.deserializeKey("-32769", ctxt);
            fail("Expected exception for short underflow < -32768");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testCharacterKeyBoundary() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        assertEquals(Character.valueOf('A'), kd.deserializeKey("A", ctxt));
        assertEquals(Character.valueOf(' '), kd.deserializeKey(" ", ctxt));

        try {
            kd.deserializeKey("ABC", ctxt);
            fail("Expected exception for multi-character string");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("can only convert 1-character"));
        }

        try {
            kd.deserializeKey("", ctxt);
            fail("Expected exception for empty string key with char deserializer");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("can only convert 1-character"));
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Custom Enum & DelegatingKD Defect)
    // =========================================================================

    /**
     * Targets Defects4J ground truth:
     * com.fasterxml.jackson.databind.module.TestCustomEnumKeyDeserializer::testCustomEnumKeySerializerWithPolymorphic
     * Verifies that DelegatingKD properly delegates to the underlying JsonDeserializer and propagates
     * formatted error descriptions when deserialization fails on invalid enum inputs.
     */
    @Test(timeout = 4000)
    public void testDelegatingKDDefectTargetOnEnumFailure() throws Exception {
        JsonDeserializer<SuperTypeEnum> failingEnumDeser = new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                // Emulate standard Enum deserialization error when enum constant is missing
                throw new IllegalArgumentException("No enum constant " + SuperTypeEnum.class.getName() + ".}");
            }
        };

        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(SuperTypeEnum.class, failingEnumDeser);

        assertEquals(SuperTypeEnum.class, delegatingKD.getKeyClass());

        try {
            delegatingKD.deserializeKey("FOO", ctxt);
            fail("Expected handleWeirdKey exception wrapping the failure");
        } catch (JsonMappingException expected) {
            String msg = expected.getMessage();
            assertTrue("Message should report 'not a valid representation'", msg.contains("not a valid representation"));
            assertTrue("Message should retain original error cause", msg.contains("No enum constant"));
        }
    }

    @Test(timeout = 4000)
    public void testDelegatingKDSuccessPath() throws Exception {
        JsonDeserializer<SuperTypeEnum> successfulDeser = new JsonDeserializer<SuperTypeEnum>() {
            @Override
            public SuperTypeEnum deserialize(JsonParser p, DeserializationContext ctxt) {
                return SuperTypeEnum.FOO;
            }
        };

        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(SuperTypeEnum.class, successfulDeser);

        Object result = delegatingKD.deserializeKey("FOO", ctxt);
        assertSame(SuperTypeEnum.FOO, result);
    }

    @Test(timeout = 4000)
    public void testDelegatingKDReturningNullTriggersWeirdKey() throws Exception {
        JsonDeserializer<String> nullDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        StdKeyDeserializer.DelegatingKD delegatingKD =
                new StdKeyDeserializer.DelegatingKD(String.class, nullDeser);

        try {
            delegatingKD.deserializeKey("someKey", ctxt);
            fail("Expected handleWeirdKey exception when delegate produces null");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    @Test(timeout = 4000)
    public void testEnumKDByNameAndToString() throws Exception {
        EnumResolver nameResolver = EnumResolver.constructUnsafe(SimpleTestEnum.class, ctxt.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKD = new StdKeyDeserializer.EnumKD(nameResolver, null);

        // 1. By-Name resolution
        assertEquals(SimpleTestEnum.ALPHA, enumKD.deserializeKey("ALPHA", ctxt));

        // 2. By-ToString resolution via DeserializationFeature toggle
        ObjectMapper toStringMapper = new ObjectMapper();
        toStringMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        DeserializationContext toStringCtxt = toStringMapper.getDeserializationContext();

        assertEquals(SimpleTestEnum.BETA, enumKD.deserializeKey("beta", toStringCtxt));

        // 3. Unknown enum value with READ_UNKNOWN_ENUM_VALUES_AS_NULL enabled
        ObjectMapper nullMapper = new ObjectMapper();
        nullMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        DeserializationContext nullCtxt = nullMapper.getDeserializationContext();

        assertNull(enumKD.deserializeKey("NON_EXISTENT", nullCtxt));
    }

    @Test(timeout = 4000)
    public void testEnumKDUnknownValueThrowsWhenNotIgnored() throws Exception {
        EnumResolver nameResolver = EnumResolver.constructUnsafe(SimpleTestEnum.class, ctxt.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKD = new StdKeyDeserializer.EnumKD(nameResolver, null);

        try {
            enumKD.deserializeKey("NON_EXISTENT", ctxt);
            fail("Expected handleWeirdKey exception for unknown enum value");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not one of values excepted for Enum class"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidBooleanKey() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        try {
            kd.deserializeKey("yes", ctxt);
            fail("Expected handleWeirdKey for invalid boolean");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("value not 'true' or 'false'"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidNumberFormats() throws Exception {
        StdKeyDeserializer kdInt = StdKeyDeserializer.forType(Integer.class);
        try {
            kdInt.deserializeKey("not-an-int", ctxt);
            fail("Expected NumberFormatException mapped to JsonMappingException");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }

        StdKeyDeserializer kdLong = StdKeyDeserializer.forType(Long.class);
        try {
            kdLong.deserializeKey("not-a-long", ctxt);
            fail("Expected NumberFormatException mapped to JsonMappingException");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }

        StdKeyDeserializer kdDouble = StdKeyDeserializer.forType(Double.class);
        try {
            kdDouble.deserializeKey("not-a-double", ctxt);
            fail("Expected NumberFormatException mapped to JsonMappingException");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidUrlAndUuid() throws Exception {
        StdKeyDeserializer kdUrl = StdKeyDeserializer.forType(URL.class);
        try {
            kdUrl.deserializeKey("relative/path/no/protocol", ctxt);
            fail("Expected MalformedURLException handled by ctxt");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }

        StdKeyDeserializer kdUuid = StdKeyDeserializer.forType(UUID.class);
        try {
            kdUuid.deserializeKey("invalid-uuid-string", ctxt);
            fail("Expected IllegalArgumentException handled by ctxt");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("problem:"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidClassKey() throws Exception {
        StdKeyDeserializer kdClass = StdKeyDeserializer.forType(Class.class);
        try {
            kdClass.deserializeKey("com.non.existent.PackageAndClassName", ctxt);
            fail("Expected ClassNotFound handled by ctxt");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("unable to parse key as Class"));
        }
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testUnknownInternalKindThrowsIllegalState() throws Exception {
        StdKeyDeserializer brokenKD = new StdKeyDeserializer(999, Object.class);
        brokenKD._parse("test", ctxt);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Reflection Deserializers & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<KeyWithCtor> ctor = KeyWithCtor.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer ctorKD = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);

        assertEquals(KeyWithCtor.class, ctorKD.getKeyClass());
        Object result = ctorKD.deserializeKey("helloCtor", ctxt);
        assertNotNull(result);
        assertTrue(result instanceof KeyWithCtor);
        assertEquals("helloCtor", ((KeyWithCtor) result).getVal());
    }

    @Test(timeout = 4000)
    public void testStringFactoryKeyDeserializerSuccess() throws Exception {
        Method factoryMethod = KeyWithFactory.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer factoryKD =
                new StdKeyDeserializer.StringFactoryKeyDeserializer(factoryMethod);

        assertEquals(KeyWithFactory.class, factoryKD.getKeyClass());
        Object result = factoryKD.deserializeKey("helloFactory", ctxt);
        assertNotNull(result);
        assertTrue(result instanceof KeyWithFactory);
        assertEquals("helloFactory", ((KeyWithFactory) result).getVal());
    }

    @Test(timeout = 4000)
    public void testStringFactoryKeyDeserializerErrorHandling() throws Exception {
        Method factoryMethod = KeyWithFactory.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer factoryKD =
                new StdKeyDeserializer.StringFactoryKeyDeserializer(factoryMethod);

        try {
            factoryKD.deserializeKey("INVALID", ctxt);
            fail("Expected failure invocation on factory method");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("not a valid representation"));
        }
    }

    @Test(timeout = 4000)
    public void testProtectedHelperMethods() {
        StdKeyDeserializer kd = new StdKeyDeserializer(StdKeyDeserializer.TYPE_INT, Integer.class);
        assertEquals(Integer.class, kd.getKeyClass());

        assertEquals(42, kd._parseInt("42"));
        assertEquals(9999999999L, kd._parseLong("9999999999"));
        assertEquals(123.456, kd._parseDouble("123.456"), 0.00001);
    }
}