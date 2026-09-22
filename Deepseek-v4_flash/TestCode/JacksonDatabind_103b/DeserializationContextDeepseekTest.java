package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.*;
import com.fasterxml.jackson.databind.exc.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for {@link DeserializationContext}.
 * Targets the known defect where {@link #handleWeirdKey} produces duplicate
 * "at [" markers in the exception message.
 */
public class DeserializationContextDeepseekTest {

    // Minimal concrete subclass for testing
    private static class TestDeserializationContext extends DeserializationContext {
        public TestDeserializationContext(DeserializerFactory df) {
            super(df);
        }

        public TestDeserializationContext(DeserializationContext src,
                                          DeserializationConfig config, JsonParser p,
                                          InjectableValues injectableValues) {
            super(src, config, p, injectableValues);
        }

        @Override
        public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> generator,
                                             ObjectIdResolver resolver) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkUnresolvedObjectId() throws UnresolvedForwardReference {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonDeserializer<Object> deserializerInstance(Annotated annotated,
                                                             Object deserDef) throws JsonMappingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(Annotated annotated,
                                                       Object deserDef) throws JsonMappingException {
            throw new UnsupportedOperationException();
        }
    }

    enum ABC { A, B, C }

    // Helper to create a minimal DeserializationContext for testing
    private DeserializationContext createTestContext(DeserializationConfig config, JsonParser parser) {
        // Blueprint with factory and cache
        BasicDeserializerFactory factory = new BasicDeserializerFactory(new BaseSettings());
        DeserializationContext blueprint = new TestDeserializationContext(factory);
        InjectableValues injectable = null;
        return new TestDeserializationContext(blueprint, config, parser, injectable);
    }

    /* ===========================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================== */

    @Test(timeout = 4000)
    public void testGetConfig() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertSame(config, ctxt.getConfig());
    }

    @Test(timeout = 4000)
    public void testGetActiveView() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNull(ctxt.getActiveView()); // default no view
    }

    @Test(timeout = 4000)
    public void testIsEnabled() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        // By default FAIL_ON_UNKNOWN_PROPERTIES is true
        assertTrue(ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        // but some features are false
        assertFalse(ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @Test(timeout = 4000)
    public void testGetDeserializationFeatures() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        int features = ctxt.getDeserializationFeatures();
        assertTrue((features & DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()) != 0);
    }

    @Test(timeout = 4000)
    public void testHasDeserializationFeatures() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask();
        assertTrue(ctxt.hasDeserializationFeatures(mask));
        assertFalse(ctxt.hasDeserializationFeatures(
                DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS.getMask()));
    }

    @Test(timeout = 4000)
    public void testHasSomeOfFeatures() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()
                 | DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS.getMask();
        assertTrue(ctxt.hasSomeOfFeatures(mask));
    }

    @Test(timeout = 4000)
    public void testGetParser() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TokenBuffer buf = new TokenBuffer(mapper.getFactory().getCodec());
        JsonParser parser = buf.asParserOnFirstToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertSame(parser, ctxt.getParser());
    }

    @Test(timeout = 4000)
    public void testGetFactory() {
        BasicDeserializerFactory factory = new BasicDeserializerFactory(new BaseSettings());
        DeserializationContext ctxt = new TestDeserializationContext(factory);
        assertSame(factory, ctxt.getFactory());
    }

    @Test(timeout = 4000)
    public void testGetBase64Variant() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNotNull(ctxt.getBase64Variant());
    }

    @Test(timeout = 4000)
    public void testGetNodeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNotNull(ctxt.getNodeFactory());
    }

    @Test(timeout = 4000)
    public void testGetSetAttribute() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        Object key = "testKey";
        assertNull(ctxt.getAttribute(key));
        ctxt.setAttribute(key, "value");
        assertEquals("value", ctxt.getAttribute(key));
    }

    @Test(timeout = 4000)
    public void testGetContextualType() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNull(ctxt.getContextualType());
        // We can't easily test non-null without calling handleContextualization
    }

    /* ===========================================================
     * Partition B: Boundary Value Analysis & Null Handling
     * =========================================================== */

    @Test(timeout = 4000)
    public void testFindInjectableValueNoInjectables() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.findInjectableValue("id", null, null);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No 'injectableValues' configured"));
        }
    }

    @Test(timeout = 4000)
    public void testLeaseReturnObjectBuffer() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        ObjectBuffer buf1 = ctxt.leaseObjectBuffer();
        assertNotNull(buf1);
        ctxt.returnObjectBuffer(buf1);
        ObjectBuffer buf2 = ctxt.leaseObjectBuffer();
        // Should reuse same buffer (since same initial capacity)
        assertSame(buf1, buf2);
        ctxt.returnObjectBuffer(buf2);
    }

    @Test(timeout = 4000)
    public void testGetArrayBuilders() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        ArrayBuilders builders = ctxt.getArrayBuilders();
        assertNotNull(builders);
        // Calling twice should return same instance
        assertSame(builders, ctxt.getArrayBuilders());
    }

    /* ===========================================================
     * Partition C: Defect-Targeted Test (duplicate "at [" markers)
     * =========================================================== */

    @Test(timeout = 4000)
    public void testHandleWeirdKeyNoHandlerSingleAtMarker() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser("{}"); // a short valid JSON
        parser.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createTestContext(config, parser);

        try {
            ctxt.handleWeirdKey(ABC.class, "value",
                    "not a valid representation");
            fail("expected InvalidFormatException");
        } catch (InvalidFormatException e) {
            String msg = e.getMessage();
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf("at [", idx)) != -1) {
                count++;
                idx += 4;
            }
            assertEquals("Should only get one 'at [' marker", 1, count);
        }
    }

    /* ===========================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================== */

    @Test(timeout = 4000)
    public void testHandleWeirdStringValueNoHandler() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("\"bad\"");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.handleWeirdStringValue(Integer.class, "bad",
                    "cannot parse");
            fail("expected InvalidFormatException");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("Cannot deserialize value of type"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleWeirdNumberValueNoHandler() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("123");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.handleWeirdNumberValue(ABC.class, 999,
                    "not an enum");
            fail("expected InvalidFormatException");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("Cannot deserialize value of type"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleWeirdNativeValueNoHandler() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("\"dummy\"");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.handleWeirdNativeValue(
                    TypeFactory.defaultInstance().constructType(ABC.class),
                    "bad", parser);
            fail("expected InvalidFormatException");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("incompatible types"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleMissingInstantiatorNoHandler() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.handleMissingInstantiator(String.class, null,
                    parser, "no default constructor");
            fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot construct instance"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnexpectedTokenNoHandler() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("\"text\"");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.handleUnexpectedToken(Integer.class, parser);
            fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot deserialize instance"));
        }
    }

    @Test(timeout = 4000)
    public void testReportWrongTokenException() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("\"text\"");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.reportWrongTokenException(String.class,
                    JsonToken.START_ARRAY, "expected array");
            fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unexpected token"));
        }
    }

    @Test(timeout = 4000)
    public void testReportInputMismatch() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = factory.createParser("123");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.reportInputMismatch(String.class, "wrong type");
            fail("expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            assertTrue(e.getMessage().contains("wrong type"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadMergeEnabled() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig()
                .with(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        JsonParser parser = factory.createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        // When feature enabled, should return null instead of throwing
        assertNull(ctxt.reportBadMerge(new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
            @Override
            public Class<?> handledType() { return Object.class; }
        }));
    }

    @Test(timeout = 4000)
    public void testReportBadMergeDisabled() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig()
                .without(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE);
        JsonParser parser = factory.createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = createTestContext(config, parser);
        try {
            ctxt.reportBadMerge(new JsonDeserializer<Object>() {
                @Override
                public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                    return null;
                }
                @Override
                public Class<?> handledType() { return Object.class; }
            });
            fail("expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("cannot be merged"));
        }
    }

    /* ===========================================================
     * Miscellaneous helper tests (not exhaustive but increase coverage)
     * =========================================================== */

    @Test(timeout = 4000)
    public void testConstructType() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        JavaType type = ctxt.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertNull(ctxt.constructType(null));
    }

    @Test(timeout = 4000)
    public void testFindClass() throws ClassNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertSame(String.class, ctxt.findClass("java.lang.String"));
    }

    @Test(timeout = 4000)
    public void testParseDate() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        Date d = ctxt.parseDate("2020-01-01");
        assertNotNull(d);
        try {
            ctxt.parseDate("not-a-date");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to parse Date"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructCalendar() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        Date d = new Date();
        Calendar cal = ctxt.constructCalendar(d);
        assertEquals(d.getTime(), cal.getTimeInMillis());
    }

    @Test(timeout = 4000)
    public void testCanOverrideAccessModifiers() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertTrue(ctxt.canOverrideAccessModifiers());
    }

    @Test(timeout = 4000)
    public void testGetDefaultPropertyFormat() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        JsonFormat.Value fmt = ctxt.getDefaultPropertyFormat(String.class);
        assertNotNull(fmt);
    }

    @Test(timeout = 4000)
    public void testGetAnnotationIntrospector() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNotNull(ctxt.getAnnotationIntrospector());
    }

    @Test(timeout = 4000)
    public void testGetLocale() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNotNull(ctxt.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetTimeZone() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser parser = TokenBuffer.create();
        DeserializationContext ctxt = createTestContext(config, parser);
        assertNotNull(ctxt.getTimeZone());
    }

    // To compile, provide a static helper for TokenBuffer
    private static final JsonFactory factory = new JsonFactory();

    // Inner helper to avoid exposing TokenBuffer import issues
    private static final class TokenBuffer {
        static JsonParser create() {
            return factory.createParser(new byte[0]);
        }
    }
}