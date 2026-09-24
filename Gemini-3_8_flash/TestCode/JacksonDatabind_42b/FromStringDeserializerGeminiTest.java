/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
 * Defect ID: JacksonDatabind (TestJdkTypes::testLocale failure)
 *
 * Decision / Branch Matrix Analyzed:
 * 1. UNWRAP_SINGLE_VALUE_ARRAYS:
 *    - Token == START_ARRAY & feature enabled:
 *      - exactly one element -> unwraps and deserializes (Pass)
 *      - > 1 element -> throws wrongTokenException on JsonToken.END_ARRAY
 *    - Token != START_ARRAY -> continues to standard string coercion
 * 2. String representation & empty strings:
 *    - text == null -> bypasses string logic, checks embedded object
 *    - text.length() == 0 || trim().length() == 0 -> calls _deserializeFromEmptyString()
 *      - STD_URI -> returns URI.create("")
 *      - STD_LOCALE -> DEFECT TARGET: ground truth requires Locale.ROOT, but buggy impl returns null
 *      - default -> returns null
 * 3. _deserialize(String, DeserializationContext) dispatch (Std kinds 1..12):
 *    - STD_FILE (1): new File(value)
 *    - STD_URL (2): new URL(value), throws MalformedURLException -> handled as IAE / weirdStringException
 *    - STD_URI (3): URI.create(value), throws IAE -> weirdStringException
 *    - STD_CLASS (4): ctxt.findClass(value), failure wraps in instantiationException
 *    - STD_JAVA_TYPE (5): constructFromCanonical(value)
 *    - STD_CURRENCY (6): Currency.getInstance(value), invalid -> throws IAE
 *    - STD_PATTERN (7): Pattern.compile(value), malformed -> throws PatternSyntaxException (IAE subclass)
 *    - STD_LOCALE (8):
 *      - 0 underscores -> single-arg Locale(first)
 *      - 1 underscore -> two-arg Locale(first, second)
 *      - >= 2 underscores -> three-arg Locale(first, second, third)
 *    - STD_CHARSET (9): Charset.forName(value), invalid -> throws IAE
 *    - STD_TIME_ZONE (10): TimeZone.getTimeZone(value)
 *    - STD_INET_ADDRESS (11): InetAddress.getByName(value)
 *    - STD_INET_SOCKET_ADDRESS (12):
 *      - Starts with '[':
 *        - missing ']' -> throws InvalidFormatException
 *        - contains ':' after ']' -> parsed port
 *        - no ':' after ']' -> port = 0
 *      - Does not start with '[':
 *        - single ':' -> host:port
 *        - no ':' or multiple ':' (unbracketed IPv6) -> host, port = 0
 * 4. VALUE_EMBEDDED_OBJECT handling:
 *    - embedded object == null -> returns null
 *    - embedded object is assignable to _valueClass -> cast & return
 *    - embedded object is non-assignable -> calls _deserializeEmbedded -> throws mappingException
 * 5. Other tokens:
 *    - Non-string, non-array, non-embedded token -> throws mappingException(_valueClass)
 * 6. Factory methods:
 *    - types(): array of 12 supported classes
 *    - findDeserializer(): matches each class to its respective kind, returns null for unsupported class
 */

package com.fasterxml.jackson.databind.deser.std;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Test;
import static org.junit.Assert.*;

public class FromStringDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT TEST:
     * Known bug: com.fasterxml.jackson.databind.deser.TestJdkTypes::testLocale
     * Deserializing an empty string (or whitespace string) for Locale must return Locale.ROOT (""),
     * but the defective implementation delegates to super._deserializeFromEmptyString(), returning null.
     */
    @Test(timeout = 4000)
    public void testDefectEmptyStringToLocaleRootDirect() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Locale.class);
        assertNotNull(deser);
        Object result = deser._deserializeFromEmptyString();
        // The defect causes result to be null instead of Locale.ROOT
        assertSame("Empty String for Locale must resolve to Locale.ROOT (<>)", Locale.ROOT, result);
    }

    @Test(timeout = 4000)
    public void testDefectEmptyStringToLocaleViaMapper() throws Exception {
        Locale result = mapper.readValue("\"\"", Locale.class);
        assertSame("Empty string JSON should deserialize to Locale.ROOT (<>)", Locale.ROOT, result);

        Locale resultWhitespace = mapper.readValue("\"   \"", Locale.class);
        assertSame("Whitespace string JSON should deserialize to Locale.ROOT (<>)", Locale.ROOT, resultWhitespace);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Factory & Standard Types)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryTypesCompleteness() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(12, types.length);

        for (Class<?> cls : types) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(cls);
            assertNotNull("Deserializer should be found for " + cls.getName(), deser);
            assertEquals(cls, deser.getValueClass());
        }

        assertNull("Unsupported type must return null deserializer",
                FromStringDeserializer.findDeserializer(StringBuilder.class));
    }

    @Test(timeout = 4000)
    public void testStandardTypesDeserialization() throws Exception {
        // STD_FILE
        File file = mapper.readValue("\"/tmp/testFile.txt\"", File.class);
        assertEquals(new File("/tmp/testFile.txt"), file);

        // STD_URL
        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);

        // STD_URI (normal & empty)
        URI uri = mapper.readValue("\"http://example.com/api\"", URI.class);
        assertEquals(URI.create("http://example.com/api"), uri);
        URI emptyUri = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), emptyUri);

        // STD_CLASS
        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);

        // STD_JAVA_TYPE
        JavaType jt = mapper.readValue("\"java.util.List<java.lang.Integer>\"", JavaType.class);
        assertNotNull(jt);
        assertEquals(java.util.List.class, jt.getRawClass());
        assertEquals(Integer.class, jt.containedType(0).getRawClass());

        // STD_CURRENCY
        Currency cur = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), cur);

        // STD_PATTERN
        Pattern pat = mapper.readValue("\"^test[0-9]+$\"", Pattern.class);
        assertEquals("^test[0-9]+$", pat.pattern());

        // STD_CHARSET
        Charset cs = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), cs);

        // STD_TIME_ZONE
        TimeZone tz = mapper.readValue("\"GMT+2\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("GMT+2"), tz);

        // STD_INET_ADDRESS
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    @Test(timeout = 4000)
    public void testLocaleParsingVariations() throws Exception {
        // 1 part: language only
        Locale l1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), l1);

        // 2 parts: language and country
        Locale l2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), l2);

        // 3 parts: language, country, variant
        Locale l3 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), l3);

        // Multi-segment with extra underscores
        Locale l4 = mapper.readValue("\"en_US_WIN_SPECIAL\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN_SPECIAL"), l4);
    }

    @Test(timeout = 4000)
    public void testInetSocketAddressParsing() throws Exception {
        // Bracketed IPv6 with explicit port
        InetSocketAddress addr1 = mapper.readValue("\"[2001:db8::1]:8080\"", InetSocketAddress.class);
        assertEquals("[2001:db8::1]", addr1.getHostString());
        assertEquals(8080, addr1.getPort());

        // Bracketed IPv6 without port
        InetSocketAddress addr2 = mapper.readValue("\"[2001:db8::1]\"", InetSocketAddress.class);
        assertEquals("[2001:db8::1]", addr2.getHostString());
        assertEquals(0, addr2.getPort());

        // Host with port
        InetSocketAddress addr3 = mapper.readValue("\"localhost:9090\"", InetSocketAddress.class);
        assertEquals("localhost", addr3.getHostString());
        assertEquals(9090, addr3.getPort());

        // Host without port
        InetSocketAddress addr4 = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", addr4.getHostString());
        assertEquals(0, addr4.getPort());

        // Unbracketed IPv6 without port (multiple colons)
        InetSocketAddress addr5 = mapper.readValue("\"2001:db8::1\"", InetSocketAddress.class);
        assertEquals("2001:db8::1", addr5.getHostString());
        assertEquals(0, addr5.getPort());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Array Unwrapping
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArraySuccess() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Currency cur = unwrapMapper.readValue("[\"EUR\"]", Currency.class);
        assertEquals(Currency.getInstance("EUR"), cur);

        URI uri = unwrapMapper.readValue("[\"http://example.org\"]", URI.class);
        assertEquals(URI.create("http://example.org"), uri);
    }

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArrayWithTooManyElementsFails() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        try {
            unwrapMapper.readValue("[\"EUR\", \"USD\"]", Currency.class);
            fail("Expected JsonMappingException for multi-element array unwrap");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("more than a single value in the array"));
        }
    }

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArrayDisabledFailsOnArray() throws Exception {
        try {
            mapper.readValue("[\"EUR\"]", Currency.class);
            fail("Expected JsonMappingException when UNWRAP_SINGLE_VALUE_ARRAYS is disabled");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not deserialize instance of"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceStringsReturningNullByDefault() throws Exception {
        // Types other than URI (and Locale in fixed version) should return null on empty string
        assertNull(mapper.readValue("\"\"", Currency.class));
        assertNull(mapper.readValue("\"   \"", Currency.class));
        assertNull(mapper.readValue("\"\"", Pattern.class));
        assertNull(mapper.readValue("\"   \"", Charset.class));
        assertNull(mapper.readValue("\"\"", TimeZone.class));
        assertNull(mapper.readValue("\"   \"", InetAddress.class));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMalformedInetSocketAddressUnclosedBracket() throws Exception {
        try {
            mapper.readValue("\"[2001:db8::1:8080\"", InetSocketAddress.class);
            fail("Expected InvalidFormatException for unclosed bracket IPv6");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("must contain closing bracket"));
        }
    }

    @Test(timeout = 4000)
    public void testMalformedUrlThrowsWeirdStringException() throws Exception {
        try {
            mapper.readValue("\"invalid-url-scheme\"", URL.class);
            fail("Expected JsonMappingException for malformed URL");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test(timeout = 4000)
    public void testMalformedCurrencyThrowsWeirdStringException() throws Exception {
        try {
            mapper.readValue("\"INVALID_CURR_CODE\"", Currency.class);
            fail("Expected JsonMappingException for invalid currency code");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
            assertNotNull(e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testMalformedPatternThrowsWeirdStringException() throws Exception {
        try {
            mapper.readValue("\"[unclosed-regex-bracket\"", Pattern.class);
            fail("Expected JsonMappingException for invalid regex pattern");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test(timeout = 4000)
    public void testMalformedCharsetThrowsWeirdStringException() throws Exception {
        try {
            mapper.readValue("\"NON_EXISTENT_CHARSET_123\"", Charset.class);
            fail("Expected JsonMappingException for non-existent charset");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownClassThrowsInstantiationException() throws Exception {
        try {
            mapper.readValue("\"com.nonexistent.NoSuchClass123\"", Class.class);
            fail("Expected JsonMappingException for unknown class");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("com.nonexistent.NoSuchClass123") ||
                       e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidTokenThrowsMappingException() throws Exception {
        try {
            mapper.readValue("{\"key\": \"val\"}", Currency.class);
            fail("Expected JsonMappingException for JSON Object token");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not deserialize instance of"));
        }

        try {
            mapper.readValue("12345", Currency.class);
            fail("Expected JsonMappingException for int token when cannot coerce");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not deserialize instance of"));
        }
    }

    // =========================================================================
    // Partition E: Embedded Object Handling & Custom Subclass Execution
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmbeddedObjectAssignable() throws Exception {
        File originalFile = new File("/path/to/embed.txt");
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeEmbeddedObject(originalFile);

        JsonParser parser = tb.asParser();
        assertNotNull(parser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.getCurrentToken());

        File result = mapper.readValue(parser, File.class);
        assertSame(originalFile, result);
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectNull() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeEmbeddedObject(null);

        JsonParser parser = tb.asParser();
        assertNotNull(parser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.getCurrentToken());

        File result = mapper.readValue(parser, File.class);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectIncompatibleThrowsMappingException() throws Exception {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeEmbeddedObject(Integer.valueOf(42));

        JsonParser parser = tb.asParser();
        assertNotNull(parser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.getCurrentToken());

        try {
            mapper.readValue(parser, File.class);
            fail("Expected JsonMappingException for incompatible embedded object");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Don't know how to convert embedded Object"));
        }
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerNullResultThrowsWeirdStringException() throws Exception {
        // Test custom subclass returning null from _deserialize
        FromStringDeserializer<String> customDeser = new FromStringDeserializer<String>(String.class) {
            private static final long serialVersionUID = 1L;

            @Override
            protected String _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return null; // triggers the result == null path without throwing IAE
            }
        };

        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeString("some-text");
        JsonParser p = tb.asParser();
        p.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            customDeser.deserialize(p, ctxt);
            fail("Expected JsonMappingException when _deserialize returns null");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test(timeout = 4000)
    public void testStdInvalidKindThrowsIllegalArgumentException() {
        // Construct a Std instance with an invalid kind index to hit default branch
        FromStringDeserializer.Std invalidKindDeser = new FromStringDeserializer.Std(String.class, 9999);
        try {
            invalidKindDeser._deserialize("test", mapper.getDeserializationContext());
            fail("Expected IllegalArgumentException for unknown kind");
        } catch (IllegalArgumentException | IOException expected) {
            // Success: hits throw new IllegalArgumentException();
        }
    }
}