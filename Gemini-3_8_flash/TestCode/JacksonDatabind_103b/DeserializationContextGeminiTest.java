package com.fasterxml.jackson.databind;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.DeserializationContext
 *
 * Decision Branches & Partitions Analyzed:
 * 1. Construction & Blueprint Lifecycle:
 *    - DeserializationContext(DeserializerFactory df, DeserializerCache cache) null/non-null handling
 *    - Copy constructors and per-call reconfigurations
 * 2. Feature / Mask Queries:
 *    - isEnabled(DeserializationFeature), getDeserializationFeatures(), hasDeserializationFeatures(), hasSomeOfFeatures()
 *    - Config delegation: canOverrideAccessModifiers, isEnabled(MapperFeature), getDefaultPropertyFormat, getLocale, getTimeZone
 * 3. Buffer Lifecycle & Recycling:
 *    - leaseObjectBuffer(), returnObjectBuffer(): null buffer, replacement based on capacity
 *    - getArrayBuilders() lazy instantiation
 * 4. Exception Factory Methods & Problem Reporting:
 *    - weirdKeyException, weirdStringException, weirdNumberException, weirdNativeValueException
 *    - instantiationException (with Throwable / String), invalidTypeIdException, missingTypeIdException
 *    - wrongTokenException, reportInputMismatch, reportBadTypeDefinition, reportBadPropertyDefinition, reportBadMerge
 *    - _isCompatible: null, raw class instance, primitive vs wrapper resolution
 * 5. Problem Handlers Delegation & Recovery Paths:
 *    - handleUnknownProperty, handleWeirdKey, handleWeirdStringValue, handleWeirdNumberValue,
 *      handleWeirdNativeValue, handleMissingInstantiator, handleInstantiationProblem,
 *      handleUnexpectedToken, handleUnknownTypeId, handleMissingTypeId
 * 6. Defect-Targeted Zone:
 *    - BasicExceptionTest::testLocationAddition failure condition:
 *      Triggered when deserializing an invalid Map key (e.g. enum key) where chained exception message
 *      propagation leads to multiple "at [" location markers.
 */

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

import org.junit.Test;
import static org.junit.Assert.*;

public class DeserializationContextGeminiTest {

    private enum TestEnumKey {
        ALPHA, BETA, GAMMA
    }

    private static class ConcreteTestContext extends DefaultDeserializationContext {
        private static final long serialVersionUID = 1L;

        public ConcreteTestContext(DeserializerFactory df) {
            super(df, null);
        }

        public ConcreteTestContext(ConcreteTestContext src, DeserializationConfig config,
                                    JsonParser p, InjectableValues injectableValues) {
            super(src, config, p, injectableValues);
        }

        public ConcreteTestContext(ConcreteTestContext src, DeserializerFactory factory) {
            super(src, factory);
        }

        public ConcreteTestContext(ConcreteTestContext src) {
            super(src);
        }

        @Override
        public DefaultDeserializationContext createContext(DeserializationConfig config,
                JsonParser p, InjectableValues injectableValues) {
            return new ConcreteTestContext(this, config, p, injectableValues);
        }

        @Override
        public DefaultDeserializationContext with(DeserializerFactory factory) {
            return new ConcreteTestContext(this, factory);
        }

        @Override
        public DefaultDeserializationContext copy() {
            return new ConcreteTestContext(this);
        }
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testLocationAdditionForInvalidKeyFormatException() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, TestEnumKey.class, String.class);
        String json = "{\"INVALID_KEY\": \"some_val\"}";

        try {
            mapper.readValue(json, mapType);
            fail("Expected InvalidFormatException for unmapped enum key");
        } catch (InvalidFormatException e) {
            String msg = e.getMessage();
            assertNotNull(msg);

            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf("at [", idx)) >= 0) {
                count++;
                idx += 4;
            }
            assertEquals("Should only get one 'at [' marker, got " + count + ", source: " + msg, 1, count);
            assertSame(TestEnumKey.class, e.getTargetType());
            assertEquals("INVALID_KEY", e.getValue());
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testLifecycleAndConfigurationDelegates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = mapper.getFactory().createParser("123");
        DefaultDeserializationContext src = (DefaultDeserializationContext) mapper.getDeserializationContext();
        DefaultDeserializationContext ctxt = src.createInstance(config, parser, null);

        assertSame(config, ctxt.getConfig());
        assertSame(parser, ctxt.getParser());
        assertEquals(config.canOverrideAccessModifiers(), ctxt.canOverrideAccessModifiers());
        assertEquals(config.getAnnotationIntrospector(), ctxt.getAnnotationIntrospector());
        assertEquals(config.getTypeFactory(), ctxt.getTypeFactory());
        assertEquals(config.getLocale(), ctxt.getLocale());
        assertEquals(config.getTimeZone(), ctxt.getTimeZone());
        assertEquals(config.getBase64Variant(), ctxt.getBase64Variant());
        assertSame(config.getNodeFactory(), ctxt.getNodeFactory());

        Class<?> view = ctxt.getActiveView();
        assertNull(view);

        assertNotNull(ctxt.getFactory());
        assertNull(ctxt.getContextualType());

        ctxt.setAttribute("myKey", "myValue");
        assertEquals("myValue", ctxt.getAttribute("myKey"));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testFeatureFlagsQueries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig()
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, parser, null);

        assertTrue(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        int flags = ctxt.getDeserializationFeatures();
        assertEquals(config.getDeserializationFeatures(), flags);

        int mask = DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.getMask();
        assertTrue(ctxt.hasDeserializationFeatures(mask));
        assertTrue(ctxt.hasSomeOfFeatures(mask));

        int unknownMask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask();
        assertFalse(ctxt.hasDeserializationFeatures(unknownMask));
        assertFalse(ctxt.hasDeserializationFeatures(mask | unknownMask));
        assertTrue(ctxt.hasSomeOfFeatures(mask | unknownMask));

        assertTrue(ctxt.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testObjectBufferAndArrayBuildersRecycling() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();

        assertNotNull(ctxt.getArrayBuilders());
        assertSame(ctxt.getArrayBuilders(), ctxt.getArrayBuilders());

        ObjectBuffer buf1 = ctxt.leaseObjectBuffer();
        assertNotNull(buf1);
        ObjectBuffer buf2 = ctxt.leaseObjectBuffer();
        assertNotNull(buf2);
        assertNotSame(buf1, buf2);

        ctxt.returnObjectBuffer(buf1);
        ObjectBuffer reused = ctxt.leaseObjectBuffer();
        assertSame(buf1, reused);
    }

    @Test(timeout = 4000)
    public void testTypeConstructionAndResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();

        assertNull(ctxt.constructType(null));
        JavaType strType = ctxt.constructType(String.class);
        assertNotNull(strType);
        assertEquals(String.class, strType.getRawClass());

        Class<?> foundClass = ctxt.findClass("java.lang.Integer");
        assertEquals(Integer.class, foundClass);
    }

    @Test(timeout = 4000)
    public void testCalendarAndDateParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"2020-01-01T00:00:00.000+0000\"");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        Date d = ctxt.parseDate("2020-01-01T00:00:00.000+0000");
        assertNotNull(d);

        Calendar cal = ctxt.constructCalendar(d);
        assertNotNull(cal);
        assertEquals(d.getTime(), cal.getTimeInMillis());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCompatibilityCheck() {
        ConcreteTestContext ctxt = new ConcreteTestContext(BeanDeserializerFactory.instance);
        assertTrue(ctxt._isCompatible(Integer.class, 123));
        assertTrue(ctxt._isCompatible(int.class, 123));
        assertTrue(ctxt._isCompatible(Object.class, "str"));
        assertTrue(ctxt._isCompatible(String.class, null));
        assertFalse(ctxt._isCompatible(Integer.class, "not an int"));
        assertFalse(ctxt._isCompatible(int.class, "not an int"));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullDeserializerFactoryThrowsException() {
        try {
            new ConcreteTestContext((DeserializerFactory) null);
            fail("Expected IllegalArgumentException for null factory");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cannot pass null DeserializerFactory"));
        }
    }

    @Test(timeout = 4000)
    public void testHasValueDeserializerForHandling() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();
        JavaType stringType = mapper.constructType(String.class);

        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        boolean found = ctxt.hasValueDeserializerFor(stringType, cause);
        assertTrue(found);
        assertNull(cause.get());

        JavaType invalidType = mapper.getTypeFactory().constructType(Void.class);
        boolean voidFound = ctxt.hasValueDeserializerFor(invalidType, cause);
        assertFalse(voidFound);
    }

    @Test(timeout = 4000)
    public void testParseDateInvalidStringThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();
        try {
            ctxt.parseDate("not-a-valid-date-str");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to parse Date value 'not-a-valid-date-str'"));
        }
    }

    @Test(timeout = 4000)
    public void testReturnObjectBufferCapacityHandling() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();

        ObjectBuffer smallBuf = ctxt.leaseObjectBuffer();
        ObjectBuffer largeBuf = ctxt.leaseObjectBuffer();
        largeBuf.resetAndStart();

        ctxt.returnObjectBuffer(smallBuf);
        ctxt.returnObjectBuffer(largeBuf);

        ObjectBuffer leased = ctxt.leaseObjectBuffer();
        assertSame(largeBuf, leased);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testFindInjectableValueWithoutConfiguredInjectableValues() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) mapper.getDeserializationContext();

        try {
            ctxt.findInjectableValue("missingId", null, null);
            fail("Expected InvalidDefinitionException for missing InjectableValues");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No 'injectableValues' configured"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadMergeThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig()
                .without(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        JsonParser p = mapper.getFactory().createParser("123");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, p, null);

        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.constructType(Integer.class));
        try {
            ctxt.reportBadMerge(deser);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("values of type [simple type, class java.lang.Integer] cannot be merged"));
        }

        DeserializationConfig ignoreConfig = mapper.getDeserializationConfig()
                .with(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        DefaultDeserializationContext ignoreCtxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(ignoreConfig, p, null);
        assertNull(ignoreCtxt.reportBadMerge(deser));
        p.close();
    }

    @Test(timeout = 4000)
    public void testSemanticExceptionBuilders() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{\"k\": 1}");
        p.nextToken();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        JsonMappingException e1 = ctxt.weirdKeyException(String.class, "testKey", "bad format");
        assertTrue(e1 instanceof InvalidFormatException);
        assertEquals("testKey", ((InvalidFormatException) e1).getValue());
        assertEquals(String.class, ((InvalidFormatException) e1).getTargetType());

        JsonMappingException e2 = ctxt.weirdStringException("xyz", Integer.class, "cannot convert");
        assertTrue(e2 instanceof InvalidFormatException);
        assertEquals("xyz", ((InvalidFormatException) e2).getValue());
        assertEquals(Integer.class, ((InvalidFormatException) e2).getTargetType());

        JsonMappingException e3 = ctxt.weirdNumberException(10.5, Long.class, "no decimals allowed");
        assertTrue(e3 instanceof InvalidFormatException);
        assertEquals(10.5, ((InvalidFormatException) e3).getValue());

        JsonMappingException e4 = ctxt.weirdNativeValueException("nativeObj", Double.class);
        assertTrue(e4 instanceof InvalidFormatException);
        assertEquals("nativeObj", ((InvalidFormatException) e4).getValue());

        JsonMappingException e5 = ctxt.instantiationException(List.class, new RuntimeException("inner error"));
        assertTrue(e5 instanceof InvalidDefinitionException);
        assertTrue(e5.getMessage().contains("Cannot construct instance of java.util.List"));

        JsonMappingException e6 = ctxt.instantiationException(List.class, "no default ctor");
        assertTrue(e6 instanceof InvalidDefinitionException);

        JsonMappingException e7 = ctxt.invalidTypeIdException(ctxt.constructType(Number.class), "invalidId", "details");
        assertTrue(e7 instanceof InvalidTypeIdException);
        assertEquals("invalidId", ((InvalidTypeIdException) e7).getTypeId());

        JsonMappingException e8 = ctxt.missingTypeIdException(ctxt.constructType(Number.class), "need type");
        assertTrue(e8 instanceof InvalidTypeIdException);
        assertNull(((InvalidTypeIdException) e8).getTypeId());

        JsonMappingException e9 = ctxt.wrongTokenException(p, Integer.class, JsonToken.VALUE_NUMBER_INT, "wanted int");
        assertTrue(e9 instanceof MismatchedInputException);

        p.close();
    }

    @Test(timeout = 4000)
    public void testReportInputMismatchMethods() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"data\"");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        try {
            ctxt.reportInputMismatch(String.class, "Mismatch on class %s", "String");
            fail("Expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Mismatch on class String"));
        }

        try {
            ctxt.reportInputMismatch(ctxt.constructType(Long.class), "Mismatch on type");
            fail("Expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Mismatch on type"));
        }

        try {
            ctxt.reportTrailingTokens(String.class, p, JsonToken.END_OBJECT);
            fail("Expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("Trailing token"));
        }
        p.close();
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Handlers Recovery
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testProblemHandlerUnknownPropertyHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final AtomicReference<String> handledProp = new AtomicReference<String>();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
                    JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
                handledProp.set(propertyName);
                return true;
            }
        });

        JsonParser p = mapper.getFactory().createParser("{\"unknownField\": 42}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        boolean handled = ctxt.handleUnknownProperty(p, null, Object.class, "unknownField");
        assertTrue(handled);
        assertEquals("unknownField", handledProp.get());
        p.close();
    }

    @Test(timeout = 4000)
    public void testProblemHandlerUnknownPropertyUnhandledThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonParser p = mapper.getFactory().createParser("{\"skipped\": 42}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        assertTrue(ctxt.handleUnknownProperty(p, null, Object.class, "skipped"));
        p.close();

        ObjectMapper failMapper = new ObjectMapper();
        failMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonParser pFail = failMapper.getFactory().createParser("{\"failField\": 1}");
        DefaultDeserializationContext failCtxt = ((DefaultDeserializationContext) failMapper.getDeserializationContext())
                .createInstance(failMapper.getDeserializationConfig(), pFail, null);

        try {
            failCtxt.handleUnknownProperty(pFail, null, Object.class, "failField");
            fail("Expected UnrecognizedPropertyException");
        } catch (UnrecognizedPropertyException e) {
            assertEquals("failField", e.getPropertyName());
        }
        pFail.close();
    }

    @Test(timeout = 4000)
    public void testProblemHandlerWeirdValueRecovery() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdKey(DeserializationContext ctxt, Class<?> keyClass, String keyValue, String msg) {
                if ("recoveredKey".equals(keyValue)) {
                    return "fixedKey";
                }
                return DeserializationProblemHandler.NOT_HANDLED;
            }

            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetClass, String value, String msg) {
                if (targetClass == Integer.class && "NaN".equals(value)) {
                    return 0;
                }
                return DeserializationProblemHandler.NOT_HANDLED;
            }

            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetClass, Number value, String msg) {
                if (targetClass == String.class) {
                    return "num:" + value;
                }
                return DeserializationProblemHandler.NOT_HANDLED;
            }
        });

        JsonParser p = mapper.getFactory().createParser("{}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        Object keyResult = ctxt.handleWeirdKey(String.class, "recoveredKey", "test");
        assertEquals("fixedKey", keyResult);

        Object strResult = ctxt.handleWeirdStringValue(Integer.class, "NaN", "test");
        assertEquals(0, strResult);

        Object numResult = ctxt.handleWeirdNumberValue(String.class, 42, "test");
        assertEquals("num:42", numResult);
        p.close();
    }

    @Test(timeout = 4000)
    public void testReadValueAndReadPropertyValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"Hello World\"");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        String result = ctxt.readValue(p, String.class);
        assertEquals("Hello World", result);
        p.close();
    }
}