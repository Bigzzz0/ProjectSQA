/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Branch / Condition                                        | Target Test Method
 * ---------------------------------------------------------------------------------------------------
 * Defect: Locale with BCP 47 hyphen format (e.g. "en-US")   | testDefectHyphenatedLocaleDeserialization
 * Types catalogue verification (types())                    | testTypesCatalog
 * Factory method resolution (findDeserializer for all 12)   | testFindDeserializerAllSupportedTypes
 * Factory method unsupported class returns null             | testFindDeserializerUnsupportedType
 * Empty / blank string handling for URI (URI.create(""))    | testEmptyAndBlankStringForURI
 * Empty / blank string handling for Locale (Locale.ROOT)    | testEmptyAndBlankStringForLocale
 * Empty / blank string handling for other types (null)      | testEmptyAndBlankStringForOtherTypes
 * Array unwrapping: UNWRAP_SINGLE_VALUE_ARRAYS enabled (ok) | testArrayUnwrappingSingleValueSuccess
 * Array unwrapping: UNWRAP_SINGLE_VALUE_ARRAYS (>1 element) | testArrayUnwrappingMultipleValuesFails
 * Array unwrapping: UNWRAP_SINGLE_VALUE_ARRAYS disabled     | testArrayUnwrappingDisabledFails
 * Embedded object: null embedded object returns null        | testEmbeddedObjectNull
 * Embedded object: exact or assignable type returns as-is   | testEmbeddedObjectMatchingType
 * Embedded object: unconvertible object triggers exception  | testEmbeddedObjectIncompatibleType
 * Custom deserializer: _deserializeEmbedded override hook   | testCustomDeserializerEmbeddedHook
 * Custom deserializer: null returned from _deserialize      | testCustomDeserializerReturningNullFails
 * Custom deserializer: IAE without message (cause.msg==null)| testCustomDeserializerExceptionWithoutMessage
 * Non-string, non-array, non-embedded token (START_OBJECT)  | testInvalidTokenTypeStartObject
 * File deserialization                                      | testStdFileDeserialization
 * URL deserialization (valid and invalid)                   | testStdURLDeserialization, testStdURLInvalid
 * URI deserialization (valid and invalid)                   | testStdURIDeserialization, testStdURIInvalid
 * Class deserialization (valid and invalid)                 | testStdClassDeserialization, testStdClassInvalid
 * JavaType deserialization (valid and invalid)              | testStdJavaTypeDeserialization, testStdJavaTypeInvalid
 * Currency deserialization (valid and invalid)              | testStdCurrencyDeserialization, testStdCurrencyInvalid
 * Pattern deserialization (valid and invalid)               | testStdPatternDeserialization, testStdPatternInvalid
 * Locale deserialization (1, 2, and 3 parts with '_')       | testStdLocaleVariants
 * Charset deserialization (valid and invalid)               | testStdCharsetDeserialization, testStdCharsetInvalid
 * TimeZone deserialization                                  | testStdTimeZoneDeserialization
 * InetAddress deserialization                               | testStdInetAddressDeserialization
 * InetSocketAddress: host only                              | testStdInetSocketAddressHostOnly
 * InetSocketAddress: host and port                          | testStdInetSocketAddressHostPort
 * InetSocketAddress: bracketed IPv6 with port               | testStdInetSocketAddressIPv6BracketedWithPort
 * InetSocketAddress: bracketed IPv6 without port            | testStdInetSocketAddressIPv6BracketedWithoutPort
 * InetSocketAddress: bracketed IPv6 missing closing bracket | testStdInetSocketAddressIPv6MissingBracket
 * InetSocketAddress: unbracketed IPv6 without port          | testStdInetSocketAddressIPv6Unbracketed
 * Std unknown kind fallback exception                       | testStdUnknownKindThrowsIllegalArgumentException
 * ---------------------------------------------------------------------------------------------------
 */

package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class FromStringDeserializerGeminiTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J failure:
     * com.fasterxml.jackson.databind.deser.TestJdkTypes::testLocale
     * junit.framework.AssertionFailedError: expected:<en_US> but was:<en-us>
     *
     * Tests deserializing a Locale string formatted using BCP 47 hyphen (e.g. "en-US"),
     * expecting it to parse properly into Locale.US (language "en", country "US").
     */
    @Test(timeout = 4000)
    public void testDefectHyphenatedLocaleDeserialization() throws Exception {
        Locale loc = MAPPER.readValue("\"en-US\"", Locale.class);
        assertNotNull("Deserialized Locale should not be null", loc);
        assertEquals("en_US", loc.toString());
        assertEquals(Locale.US, loc);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypesCatalog() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(12, types.length);

        Class<?>[] expected = new Class<?>[] {
            File.class, URL.class, URI.class, Class.class,
            JavaType.class, Currency.class, Pattern.class, Locale.class,
            Charset.class, TimeZone.class, InetAddress.class, InetSocketAddress.class
        };
        assertArrayEquals(expected, types);
    }

    @Test(timeout = 4000)
    public void testFindDeserializerAllSupportedTypes() {
        for (Class<?> type : FromStringDeserializer.types()) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(type);
            assertNotNull("Deserializer should be found for " + type.getName(), deser);
            assertEquals(type, deser.getValueClass());
        }
    }

    @Test(timeout = 4000)
    public void testFindDeserializerUnsupportedType() {
        assertNull(FromStringDeserializer.findDeserializer(String.class));
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
        assertNull(FromStringDeserializer.findDeserializer(Integer.class));
    }

    @Test(timeout = 4000)
    public void testStdFileDeserialization() throws Exception {
        File file = MAPPER.readValue("\"/tmp/test_jackson.txt\"", File.class);
        assertNotNull(file);
        assertEquals(new File("/tmp/test_jackson.txt"), file);
    }

    @Test(timeout = 4000)
    public void testStdURLDeserialization() throws Exception {
        URL url = MAPPER.readValue("\"http://localhost:8080/test\"", URL.class);
        assertNotNull(url);
        assertEquals("http", url.getProtocol());
        assertEquals(8080, url.getPort());
        assertEquals("/test", url.getPath());
    }

    @Test(timeout = 4000)
    public void testStdURIDeserialization() throws Exception {
        URI uri = MAPPER.readValue("\"http://example.com/api?v=1\"", URI.class);
        assertNotNull(uri);
        assertEquals("example.com", uri.getHost());
        assertEquals("/api", uri.getPath());
        assertEquals("v=1", uri.getQuery());
    }

    @Test(timeout = 4000)
    public void testStdClassDeserialization() throws Exception {
        Class<?> clazz = MAPPER.readValue("\"java.lang.Integer\"", Class.class);
        assertEquals(Integer.class, clazz);
    }

    @Test(timeout = 4000)
    public void testStdJavaTypeDeserialization() throws Exception {
        JavaType javaType = MAPPER.readValue("\"java.util.Map<java.lang.String,java.lang.Integer>\"", JavaType.class);
        assertNotNull(javaType);
        assertTrue(javaType.isMapLikeType());
        assertEquals(String.class, javaType.getKeyType().getRawClass());
        assertEquals(Integer.class, javaType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testStdCurrencyDeserialization() throws Exception {
        Currency curr = MAPPER.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), curr);
    }

    @Test(timeout = 4000)
    public void testStdPatternDeserialization() throws Exception {
        Pattern pat = MAPPER.readValue("\"^[0-9]+$\"", Pattern.class);
        assertNotNull(pat);
        assertTrue(pat.matcher("12345").matches());
        assertFalse(pat.matcher("abc").matches());
    }

    @Test(timeout = 4000)
    public void testStdLocaleVariants() throws Exception {
        // 1 piece: "en"
        Locale loc1 = MAPPER.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), loc1);

        // 2 pieces: "en_GB"
        Locale loc2 = MAPPER.readValue("\"en_GB\"", Locale.class);
        assertEquals(new Locale("en", "GB"), loc2);

        // 3 pieces: "en_US_WIN"
        Locale loc3 = MAPPER.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), loc3);
    }

    @Test(timeout = 4000)
    public void testStdCharsetDeserialization() throws Exception {
        Charset cs = MAPPER.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), cs);
    }

    @Test(timeout = 4000)
    public void testStdTimeZoneDeserialization() throws Exception {
        TimeZone tz = MAPPER.readValue("\"PST\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("PST"), tz);
    }

    @Test(timeout = 4000)
    public void testStdInetAddressDeserialization() throws Exception {
        InetAddress addr = MAPPER.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressHostOnly() throws Exception {
        InetSocketAddress addr = MAPPER.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostString());
        assertEquals(0, addr.getPort());
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressHostPort() throws Exception {
        InetSocketAddress addr = MAPPER.readValue("\"localhost:9090\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostString());
        assertEquals(9090, addr.getPort());
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressIPv6BracketedWithPort() throws Exception {
        InetSocketAddress addr = MAPPER.readValue("\"[2001:db8::1]:8443\"", InetSocketAddress.class);
        assertEquals("[2001:db8::1]", addr.getHostString());
        assertEquals(8443, addr.getPort());
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressIPv6BracketedWithoutPort() throws Exception {
        InetSocketAddress addr = MAPPER.readValue("\"[2001:db8::1]\"", InetSocketAddress.class);
        assertEquals("[2001:db8::1]", addr.getHostString());
        assertEquals(0, addr.getPort());
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressIPv6Unbracketed() throws Exception {
        InetSocketAddress addr = MAPPER.readValue("\"2001:db8::1\"", InetSocketAddress.class);
        assertEquals(0, addr.getPort());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndBlankStringForURI() throws Exception {
        URI emptyUri = MAPPER.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), emptyUri);

        URI blankUri = MAPPER.readValue("\"   \"", URI.class);
        assertEquals(URI.create(""), blankUri);
    }

    @Test(timeout = 4000)
    public void testEmptyAndBlankStringForLocale() throws Exception {
        Locale emptyLoc = MAPPER.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, emptyLoc);

        Locale blankLoc = MAPPER.readValue("\"   \"", Locale.class);
        assertEquals(Locale.ROOT, blankLoc);
    }

    @Test(timeout = 4000)
    public void testEmptyAndBlankStringForOtherTypes() throws Exception {
        assertNull(MAPPER.readValue("\"\"", File.class));
        assertNull(MAPPER.readValue("\"   \"", File.class));
        assertNull(MAPPER.readValue("\"\"", Currency.class));
        assertNull(MAPPER.readValue("\"   \"", Currency.class));
    }

    @Test(timeout = 4000)
    public void testArrayUnwrappingSingleValueSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        File f = mapper.readValue("[\"/tmp/unwrap.txt\"]", File.class);
        assertEquals(new File("/tmp/unwrap.txt"), f);
    }

    @Test(timeout = 4000)
    public void testArrayUnwrappingMultipleValuesFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[\"/tmp/1.txt\", \"/tmp/2.txt\"]", File.class);
            fail("Expected JsonMappingException for multi-value array");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Attempted to unwrap single value array"));
        }
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testArrayUnwrappingDisabledFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        mapper.readValue("[\"/tmp/1.txt\"]", File.class);
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectNull() throws Exception {
        TokenBuffer tb = new TokenBuffer(MAPPER, false);
        tb.writeEmbeddedObject(null);
        File result = MAPPER.readValue(tb.asParser(), File.class);
        assertNull(result);
        tb.close();
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectMatchingType() throws Exception {
        File expected = new File("/tmp/embedded.txt");
        TokenBuffer tb = new TokenBuffer(MAPPER, false);
        tb.writeEmbeddedObject(expected);
        File result = MAPPER.readValue(tb.asParser(), File.class);
        assertSame(expected, result);
        tb.close();
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectIncompatibleType() throws Exception {
        TokenBuffer tb = new TokenBuffer(MAPPER, false);
        tb.writeEmbeddedObject(Integer.valueOf(12345));
        try {
            MAPPER.readValue(tb.asParser(), File.class);
            fail("Expected JsonMappingException for incompatible embedded object");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Don't know how to convert embedded Object of type"));
        } finally {
            tb.close();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testInvalidTokenTypeStartObject() throws Exception {
        MAPPER.readValue("{\"key\":\"val\"}", File.class);
    }

    @Test(timeout = 4000)
    public void testStdInetSocketAddressIPv6MissingBracket() throws Exception {
        try {
            MAPPER.readValue("\"[2001:db8::1\"", InetSocketAddress.class);
            fail("Expected InvalidFormatException for unclosed bracket");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("Bracketed IPv6 address must contain closing bracket"));
        }
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdURLInvalid() throws Exception {
        MAPPER.readValue("\"invalid_url_protocol://\"", URL.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdURIInvalid() throws Exception {
        MAPPER.readValue("\"http://example .com\"", URI.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdClassInvalid() throws Exception {
        MAPPER.readValue("\"com.nonexistent.BogusClass123\"", Class.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdJavaTypeInvalid() throws Exception {
        MAPPER.readValue("\"java.util.Map<Malformed\"", JavaType.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdCurrencyInvalid() throws Exception {
        MAPPER.readValue("\"INVALID_CURRENCY_XYZ\"", Currency.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdPatternInvalid() throws Exception {
        MAPPER.readValue("\"(?i(\"", Pattern.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testStdCharsetInvalid() throws Exception {
        MAPPER.readValue("\"NO_SUCH_CHARSET_NAME_123\"", Charset.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testStdUnknownKindThrowsIllegalArgumentException() throws Exception {
        FromStringDeserializer.Std deser = new FromStringDeserializer.Std(String.class, 9999);
        deser._deserialize("any", null);
    }

    // =========================================================================
    // Partition E: Custom Subclass Hooks & Advanced Contract Integrity
    // =========================================================================

    static class CustomObject {
        final String val;
        CustomObject(String val) { this.val = val; }
    }

    static class CustomEmbeddedDeser extends FromStringDeserializer<CustomObject> {
        private static final long serialVersionUID = 1L;

        public CustomEmbeddedDeser() {
            super(CustomObject.class);
        }

        @Override
        protected CustomObject _deserialize(String value, DeserializationContext ctxt) {
            return new CustomObject(value);
        }

        @Override
        protected CustomObject _deserializeEmbedded(Object ob, DeserializationContext ctxt) throws IOException {
            if (ob instanceof Number) {
                return new CustomObject("NUM:" + ob);
            }
            return super._deserializeEmbedded(ob, ctxt);
        }

        @Override
        protected CustomObject _deserializeFromEmptyString() {
            return new CustomObject("EMPTY");
        }
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerEmbeddedHook() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomObject.class, new CustomEmbeddedDeser());
        mapper.registerModule(module);

        // Handled embedded object
        TokenBuffer tb1 = new TokenBuffer(mapper, false);
        tb1.writeEmbeddedObject(42);
        CustomObject obj1 = mapper.readValue(tb1.asParser(), CustomObject.class);
        assertEquals("NUM:42", obj1.val);
        tb1.close();

        // Empty string override
        CustomObject obj2 = mapper.readValue("\"\"", CustomObject.class);
        assertEquals("EMPTY", obj2.val);

        // Fallback to super._deserializeEmbedded throws mapping exception
        TokenBuffer tb3 = new TokenBuffer(mapper, false);
        tb3.writeEmbeddedObject(Boolean.TRUE);
        try {
            mapper.readValue(tb3.asParser(), CustomObject.class);
            fail("Expected JsonMappingException for unhandled embedded object type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Don't know how to convert embedded Object of type"));
        } finally {
            tb3.close();
        }
    }

    static class CustomNullResultDeser extends FromStringDeserializer<CustomObject> {
        private static final long serialVersionUID = 1L;

        public CustomNullResultDeser() {
            super(CustomObject.class);
        }

        @Override
        protected CustomObject _deserialize(String value, DeserializationContext ctxt) {
            return null; // explicitly returns null for non-empty string
        }
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerReturningNullFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomObject.class, new CustomNullResultDeser());
        mapper.registerModule(module);

        try {
            mapper.readValue("\"some_string\"", CustomObject.class);
            fail("Expected JsonMappingException when _deserialize returns null");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    static class CustomNullMessageExDeser extends FromStringDeserializer<CustomObject> {
        private static final long serialVersionUID = 1L;

        public CustomNullMessageExDeser() {
            super(CustomObject.class);
        }

        @Override
        protected CustomObject _deserialize(String value, DeserializationContext ctxt) {
            throw new IllegalArgumentException((String) null);
        }
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerExceptionWithoutMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomObject.class, new CustomNullMessageExDeser());
        mapper.registerModule(module);

        try {
            mapper.readValue("\"input\"", CustomObject.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
            assertFalse(e.getMessage().contains("problem:"));
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }
}