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
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: FromStringDeserializer & FromStringDeserializer.Std
 *
 * 1. Factory & Registry Branches:
 *    - types(): all 13 supported target classes.
 *    - findDeserializer(Class<?>): 13 positive branches (STD_FILE ... STD_STRING_BUILDER), 1 negative (null).
 *
 * 2. Token & Input Conditioning Branches:
 *    - text != null:
 *        - length == 0 or text.trim().length() == 0 -> _deserializeFromEmptyString()
 *            - STD_URI -> URI.create("")
 *            - STD_LOCALE -> Locale.ROOT
 *            - STD_STRING_BUILDER -> empty StringBuilder
 *            - Others -> null
 *        - Non-empty text:
 *            - Successful _deserialize -> returns value
 *            - IllegalArgumentException / MalformedURLException caught -> weird string exception handling
 *    - text == null:
 *        - START_ARRAY -> _deserializeFromArray (supported if UNWRAP_SINGLE_VALUE_ARRAYS enabled, else error)
 *        - VALUE_EMBEDDED_OBJECT:
 *            - ob == null -> returns null
 *            - _valueClass.isAssignableFrom(ob.getClass()) -> return ob
 *            - else -> _deserializeEmbedded -> reports mapping exception
 *        - Other tokens -> ctxt.handleUnexpectedToken -> throws JsonMappingException
 *
 * 3. Std Deserialization Switch Branches:
 *    - STD_FILE, STD_URL, STD_URI, STD_CLASS, STD_JAVA_TYPE, STD_CURRENCY, STD_PATTERN,
 *      STD_CHARSET, STD_TIME_ZONE, STD_INET_ADDRESS, STD_STRING_BUILDER.
 *    - STD_LOCALE: single token ("en"), two tokens ("en_US" / "en-US"), three tokens ("en_US_WIN" / "en-US-WIN").
 *    - STD_INET_SOCKET_ADDRESS:
 *        - Bracketed IPv6: with port ("[::1]:8080"), without port ("[::1]"), missing closing bracket ("[::1" -> error).
 *        - Host:port IPv4/name ("localhost:8080").
 *        - Host without port / unbracketed IPv6 ("::1", "localhost").
 *    - Default branch: VersionUtil.throwInternal() -> IllegalStateException.
 *
 * 4. Defect-Targeted Branch Zone (Defects4J Ground Truth):
 *    - com.fasterxml.jackson.databind.filter.ProblemHandlerTest::testWeirdStringHandling:
 *      When invalid string representations (e.g., malformed UUID, URL, Currency) are encountered,
 *      the deserializer must invoke DeserializationProblemHandler.handleWeirdStringValue(...)
 *      rather than directly aborting with an unhandled weirdStringException.
 */
public class FromStringDeserializerGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions (All Supported Types)
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypesRegistry() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(13, types.length);

        for (Class<?> type : types) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(type);
            assertNotNull("Deserializer should be found for " + type.getName(), deser);
        }

        assertNull(FromStringDeserializer.findDeserializer(String.class));
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
        assertNull(FromStringDeserializer.findDeserializer(Integer.class));
    }

    @Test(timeout = 4000)
    public void testDeserializeCoreTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);

        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);

        URI uri = mapper.readValue("\"urn:isbn:0451450523\"", URI.class);
        assertEquals(URI.create("urn:isbn:0451450523"), uri);

        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);

        JavaType javaType = mapper.readValue("\"java.util.List<java.lang.Integer>\"", JavaType.class);
        assertNotNull(javaType);
        assertEquals(java.util.List.class, javaType.getRawClass());
        assertEquals(Integer.class, javaType.containedType(0).getRawClass());

        Currency currency = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);

        Pattern pattern = mapper.readValue("\"^test[0-9]+$\"", Pattern.class);
        assertNotNull(pattern);
        assertEquals("^test[0-9]+$", pattern.pattern());

        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);

        TimeZone timeZone = mapper.readValue("\"GMT+2\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("GMT+2"), timeZone);

        InetAddress inetAddress = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), inetAddress);

        StringBuilder sb = mapper.readValue("\"gemini-text\"", StringBuilder.class);
        assertNotNull(sb);
        assertEquals("gemini-text", sb.toString());
    }

    @Test(timeout = 4000)
    public void testLocaleParsingVariations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1 segment
        Locale l1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), l1);

        // 2 segments (underscore & hyphen)
        Locale l2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), l2);

        Locale l3 = mapper.readValue("\"en-GB\"", Locale.class);
        assertEquals(new Locale("en", "GB"), l3);

        // 3 segments (underscore & hyphen combinations)
        Locale l4 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), l4);

        Locale l5 = mapper.readValue("\"en-US-POSIX\"", Locale.class);
        assertEquals(new Locale("en", "US", "POSIX"), l5);

        Locale l6 = mapper.readValue("\"en-US_VARIANT\"", Locale.class);
        assertEquals(new Locale("en", "US", "VARIANT"), l6);
    }

    @Test(timeout = 4000)
    public void testInetSocketAddressParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Host and port
        InetSocketAddress addr1 = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals("localhost", addr1.getHostString());
        assertEquals(8080, addr1.getPort());

        // Host without port
        InetSocketAddress addr2 = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", addr2.getHostString());
        assertEquals(0, addr2.getPort());

        // Unbracketed IPv6 without port
        InetSocketAddress addr3 = mapper.readValue("\"::1\"", InetSocketAddress.class);
        assertEquals("::1", addr3.getHostString());
        assertEquals(0, addr3.getPort());

        // Bracketed IPv6 with port
        InetSocketAddress addr4 = mapper.readValue("\"[::1]:9090\"", InetSocketAddress.class);
        assertEquals("[::1]", addr4.getHostString());
        assertEquals(9090, addr4.getPort());

        // Bracketed IPv6 without port
        InetSocketAddress addr5 = mapper.readValue("\"[::1]\"", InetSocketAddress.class);
        assertEquals("[::1]", addr5.getHostString());
        assertEquals(0, addr5.getPort());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceStringHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // URI empty -> URI.create("")
        URI uri1 = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), uri1);
        URI uri2 = mapper.readValue("\"   \"", URI.class);
        assertEquals(URI.create(""), uri2);

        // Locale empty -> Locale.ROOT
        Locale loc1 = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, loc1);
        Locale loc2 = mapper.readValue("\"   \"", Locale.class);
        assertEquals(Locale.ROOT, loc2);

        // StringBuilder empty -> empty StringBuilder
        StringBuilder sb1 = mapper.readValue("\"\"", StringBuilder.class);
        assertNotNull(sb1);
        assertEquals("", sb1.toString());
        StringBuilder sb2 = mapper.readValue("\"   \"", StringBuilder.class);
        assertNotNull(sb2);
        assertEquals("", sb2.toString());

        // Other types default empty -> null
        assertNull(mapper.readValue("\"\"", File.class));
        assertNull(mapper.readValue("\"   \"", File.class));
        assertNull(mapper.readValue("\"\"", URL.class));
        assertNull(mapper.readValue("\"\"", Currency.class));
        assertNull(mapper.readValue("\"\"", Pattern.class));
        assertNull(mapper.readValue("\"\"", Charset.class));
        assertNull(mapper.readValue("\"\"", TimeZone.class));
        assertNull(mapper.readValue("\"\"", InetAddress.class));
        assertNull(mapper.readValue("\"\"", InetSocketAddress.class));
    }

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Currency currency = mapper.readValue("[\"USD\"]", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Embedded null -> null
        TokenBuffer tbNull = new TokenBuffer(mapper, false);
        tbNull.writeEmbeddedObject(null);
        Currency c1 = mapper.readValue(tbNull.asParser(), Currency.class);
        assertNull(c1);

        // 2. Embedded instance of target type -> returned as is
        TokenBuffer tbMatch = new TokenBuffer(mapper, false);
        Currency expectedCurrency = Currency.getInstance("EUR");
        tbMatch.writeEmbeddedObject(expectedCurrency);
        Currency c2 = mapper.readValue(tbMatch.asParser(), Currency.class);
        assertSame(expectedCurrency, c2);

        // 3. Embedded object of incompatible type -> reports mapping exception
        TokenBuffer tbMismatch = new TokenBuffer(mapper, false);
        tbMismatch.writeEmbeddedObject(Integer.valueOf(42));
        try {
            mapper.readValue(tbMismatch.asParser(), Currency.class);
            fail("Expected JsonMappingException for incompatible embedded object");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("Don't know how to convert embedded Object"));
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Directly tests the Defects4J regression from ProblemHandlerTest::testWeirdStringHandling:
     * When deserializing an invalid string (here: UUID), FromStringDeserializer must delegate
     * to ctxt.handleWeirdStringValue(...) allowing registered DeserializationProblemHandler
     * extensions to intercept the error and return a recovery value.
     */
    @Test(timeout = 4000)
    public void testDefectWeirdStringHandlingUUID() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final UUID fallback = UUID.fromString("00000000-0000-0000-0000-000000000000");

        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt,
                    Class<?> targetType, String valueToConvert,
                    String failureMsg) throws IOException {
                if (targetType == UUID.class) {
                    return fallback;
                }
                return DeserializationProblemHandler.NOT_HANDLED;
            }
        });

        UUID result = mapper.readValue("\"not a uuid!\"", UUID.class);
        assertNotNull("ProblemHandler should have intercepted the invalid string", result);
        assertEquals(fallback, result);
    }

    @Test(timeout = 4000)
    public void testDefectWeirdStringHandlingURL() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final URL fallback = new URL("http://recovery.internal");

        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt,
                    Class<?> targetType, String valueToConvert,
                    String failureMsg) throws IOException {
                if (targetType == URL.class) {
                    return fallback;
                }
                return DeserializationProblemHandler.NOT_HANDLED;
            }
        });

        URL result = mapper.readValue("\"invalid://malformed url with spaces\"", URL.class);
        assertNotNull("ProblemHandler should have recovered malformed URL", result);
        assertEquals(fallback, result);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedUrlThrowsWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"invalid-url\"", URL.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedUriThrowsWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"http://bad uri with spaces\"", URI.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedCurrencyThrowsWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"INVALID_CURRENCY_CODE\"", Currency.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedPatternThrowsWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"[unclosed-pattern\"", Pattern.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedCharsetThrowsWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"UNKNOWN-CHARSET-12345\"", Charset.class);
    }

    @Test(timeout = 4000)
    public void testMalformedBracketedIpv6SocketAddress() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("\"[::1\"", InetSocketAddress.class);
            fail("Expected InvalidFormatException for unclosed bracket");
        } catch (InvalidFormatException ife) {
            assertTrue(ife.getMessage().contains("Bracketed IPv6 address must contain closing bracket"));
        }
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMalformedSocketAddressInvalidPort() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"localhost:invalid_port\"", InetSocketAddress.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testClassNotFoundThrowsInstantiationProblem() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"com.nonexistent.Class12345\"", Class.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testUnexpectedTokenThrowsMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Object token {} is unexpected for string scalar
        mapper.readValue("{}", Currency.class);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testUnknownKindThrowsInternalInStd() throws Exception {
        FromStringDeserializer.Std unknownKindDeser = new FromStringDeserializer.Std(String.class, 9999);
        unknownKindDeser._deserialize("test", null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Subclass Contract Integrity
    // =========================================================================

    static class CustomNullReturningDeserializer extends FromStringDeserializer<String> {
        private static final long serialVersionUID = 1L;

        public CustomNullReturningDeserializer() {
            super(String.class);
        }

        @Override
        protected String _deserialize(String value, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        protected String _deserializeFromEmptyString() throws IOException {
            return "empty-custom";
        }
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerNullFallthroughAndEmptyString() throws Exception {
        CustomNullReturningDeserializer deser = new CustomNullReturningDeserializer();
        assertEquals("empty-custom", deser._deserializeFromEmptyString());

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = mapper.getFactory().createParser("\"valid-string\"");
        p.nextToken();

        try {
            deser.deserialize(p, ctxt);
            fail("Expected JsonMappingException when _deserialize returns null");
        } catch (JsonMappingException jme) {
            assertTrue(jme.getMessage().contains("not a valid textual representation"));
        }
    }
}