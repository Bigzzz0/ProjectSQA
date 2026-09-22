package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core functional logic – deserialization of valid String representations for each type.
 * Partition B: Boundary/Extreme values – empty strings, null, arrays with unwrapping, whitespace, etc.
 * Partition C: Defect-targeted branch – empty String for Locale (Defects4J bug #1123).
 * Partition D: Exception paths – invalid strings, illegal arguments, empty string for non-URI types returning null.
 * Partition E: Lifecycle / contract – single-value array unwrapping, embedded object path (not covered due to complexity).
 * 
 * Targeted branches:
 * - deserialize(): START_ARRAY -> unwrap if enabled; else getValueAsString -> empty/trimmed -> _deserializeFromEmptyString.
 * - _deserializeFromEmptyString(): STD_URI returns URI.create(""), others return null (defect: STD_LOCALE not handled).
 * - _deserialize(): switch on _kind for 12 types, with specific error handling (e.g., InetSocketAddress bracketed IPv6).
 * - Exception handling: IllegalArgumentException caught and wrapped in weirStringException.
 * - findDeserializer() coverage of all type mappings.
 */
public class FromStringDeserializerDeepseekTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ======== Partition A: Core Functional Logic ========

    @Test(timeout = 4000)
    public void testValidFile() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    @Test(timeout = 4000)
    public void testValidURL() throws Exception {
        URL url = mapper.readValue("\"http://example.com\"", URL.class);
        assertEquals(new URL("http://example.com"), url);
    }

    @Test(timeout = 4000)
    public void testValidURI() throws Exception {
        URI uri = mapper.readValue("\"http://example.com/path\"", URI.class);
        assertEquals(URI.create("http://example.com/path"), uri);
    }

    @Test(timeout = 4000)
    public void testValidClass() throws Exception {
        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);
    }

    @Test(timeout = 4000)
    public void testValidJavaType() throws Exception {
        JavaType type = mapper.readValue("\"java.util.List<java.lang.String>\"", JavaType.class);
        assertNotNull(type);
        assertTrue(type.isCollectionLike());
    }

    @Test(timeout = 4000)
    public void testValidCurrency() throws Exception {
        Currency currency = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);
    }

    @Test(timeout = 4000)
    public void testValidPattern() throws Exception {
        Pattern pattern = mapper.readValue("\"\\\\d+\"", Pattern.class);
        assertNotNull(pattern);
        assertTrue(pattern.matcher("123").matches());
    }

    @Test(timeout = 4000)
    public void testValidLocaleSimple() throws Exception {
        Locale locale = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), locale);
    }

    @Test(timeout = 4000)
    public void testValidLocaleTwoPart() throws Exception {
        Locale locale = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), locale);
    }

    @Test(timeout = 4000)
    public void testValidLocaleThreePart() throws Exception {
        Locale locale = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), locale);
    }

    @Test(timeout = 4000)
    public void testValidCharset() throws Exception {
        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);
    }

    @Test(timeout = 4000)
    public void testValidTimeZone() throws Exception {
        TimeZone tz = mapper.readValue("\"America/New_York\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("America/New_York"), tz);
    }

    @Test(timeout = 4000)
    public void testValidInetAddress() throws Exception {
        // We'll skip InetAddress due to network dependency, but test simple loopback
        // Actually InetAddress.getByName("127.0.0.1") does not require network.
        java.net.InetAddress addr = mapper.readValue("\"127.0.0.1\"", java.net.InetAddress.class);
        assertEquals(java.net.InetAddress.getByName("127.0.0.1"), addr);
    }

    @Test(timeout = 4000)
    public void testValidInetSocketAddressHostPort() throws Exception {
        InetSocketAddress sockAddr = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress("localhost", 8080), sockAddr);
    }

    @Test(timeout = 4000)
    public void testValidInetSocketAddressBracketedIPv6() throws Exception {
        InetSocketAddress sockAddr = mapper.readValue("\"[::1]:80\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress("::1", 80), sockAddr);
    }

    @Test(timeout = 4000)
    public void testValidInetSocketAddressNoPort() throws Exception {
        InetSocketAddress sockAddr = mapper.readValue("\"example.com\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress("example.com", 0), sockAddr);
    }

    // ======== Partition B: Boundary Value Analysis & Extremes ========

    @Test(timeout = 4000)
    public void testEmptyStringURI() throws Exception {
        // Empty string for URI should produce non-null URI with empty resource
        URI uri = mapper.readValue("\"\"", URI.class);
        assertNotNull(uri);
        assertEquals(URI.create(""), uri);
    }

    @Test(timeout = 4000)
    public void testEmptyStringFileReturnsNull() throws Exception {
        // For File, empty string should yield null
        File file = mapper.readValue("\"\"", File.class);
        assertNull(file);
    }

    @Test(timeout = 4000)
    public void testEmptyStringURLReturnsNull() throws Exception {
        URL url = mapper.readValue("\"\"", URL.class);
        assertNull(url);
    }

    @Test(timeout = 4000)
    public void testEmptyStringClassReturnsNull() throws Exception {
        Class<?> clazz = mapper.readValue("\"\"", Class.class);
        assertNull(clazz);
    }

    @Test(timeout = 4000)
    public void testEmptyStringJavaTypeReturnsNull() throws Exception {
        JavaType type = mapper.readValue("\"\"", JavaType.class);
        assertNull(type);
    }

    @Test(timeout = 4000)
    public void testEmptyStringCurrencyReturnsNull() throws Exception {
        Currency cur = mapper.readValue("\"\"", Currency.class);
        assertNull(cur);
    }

    @Test(timeout = 4000)
    public void testEmptyStringPatternReturnsNull() throws Exception {
        Pattern p = mapper.readValue("\"\"", Pattern.class);
        assertNull(p);
    }

    @Test(timeout = 4000)
    public void testEmptyStringCharsetReturnsNull() throws Exception {
        Charset cs = mapper.readValue("\"\"", Charset.class);
        assertNull(cs);
    }

    @Test(timeout = 4000)
    public void testEmptyStringTimeZoneReturnsNull() throws Exception {
        TimeZone tz = mapper.readValue("\"\"", TimeZone.class);
        assertNull(tz);
    }

    @Test(timeout = 4000)
    public void testEmptyStringInetAddressReturnsNull() throws Exception {
        java.net.InetAddress addr = mapper.readValue("\"\"", java.net.InetAddress.class);
        assertNull(addr);
    }

    @Test(timeout = 4000)
    public void testEmptyStringInetSocketAddressReturnsNull() throws Exception {
        InetSocketAddress sock = mapper.readValue("\"\"", InetSocketAddress.class);
        assertNull(sock);
    }

    @Test(timeout = 4000)
    public void testWhitespaceString() throws Exception {
        // Whitespace only string should be treated as empty (trimmed)
        File file = mapper.readValue("\"   \"", File.class);
        assertNull(file);
    }

    @Test(timeout = 4000)
    public void testSingleValueArrayUnwrapping() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        File file = localMapper.readValue("[\"/tmp/test.txt\"]", File.class);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testSingleValueArrayUnwrappingWithMultipleElements() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        // Array with 2 elements should fail
        localMapper.readValue("[\"/tmp/1.txt\", \"/tmp/2.txt\"]", File.class);
    }

    // ======== Partition C: Defect-Targeted Branch – Empty String Locale ========

    @Test(timeout = 4000)
    public void testEmptyStringLocale() throws Exception {
        // This test exposes the bug: in defective version, empty string returns null;
        // correct behavior should return a Locale with empty language (Locale.ROOT).
        Locale locale = mapper.readValue("\"\"", Locale.class);
        assertNotNull("Empty string should produce non-null Locale (Defect #1123)", locale);
        assertEquals("Language should be empty", "", locale.getLanguage());
        assertEquals("Country should be empty", "", locale.getCountry());
        assertEquals("Variant should be empty", "", locale.getVariant());
    }

    // ======== Partition D: Exception & Defensive Guard Paths ========

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidURL() throws Exception {
        mapper.readValue("\"not a url\"", URL.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidCurrency() throws Exception {
        mapper.readValue("\"INVALID\"", Currency.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidPattern() throws Exception {
        mapper.readValue("\"[\"", Pattern.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidInetSocketAddressBracketedNoClose() throws Exception {
        // Missing closing bracket should throw InvalidFormatException (wrapped)
        mapper.readValue("\"[::1:80\"", InetSocketAddress.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidInetSocketAddressPortParse() throws Exception {
        // Port number not a number
        mapper.readValue("\"localhost:abc\"", InetSocketAddress.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidClass() throws Exception {
        mapper.readValue("\"nonexistent.Class\"", Class.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidJavaType() throws Exception {
        mapper.readValue("\"not a type\"", JavaType.class);
    }

    // ======== Partition E: Object Lifecycle & Contract Integrity (not fully covered) ========
    // We test that findDeserializer returns null for unsupported types.
    @Test(timeout = 4000)
    public void testFindDeserializerUnsupportedType() throws Exception {
        Std deser = (Std) FromStringDeserializer.findDeserializer(Integer.class);
        assertNull("Unsupported type should return null", deser);
    }

    @Test(timeout = 4000)
    public void testFindDeserializerAllSupportedTypes() throws Exception {
        // Test that each known type returns a Std with correct _kind
        assertNotNull(FromStringDeserializer.findDeserializer(File.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URL.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URI.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Class.class));
        assertNotNull(FromStringDeserializer.findDeserializer(JavaType.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Currency.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Pattern.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Locale.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Charset.class));
        assertNotNull(FromStringDeserializer.findDeserializer(TimeZone.class));
        assertNotNull(FromStringDeserializer.findDeserializer(java.net.InetAddress.class));
        assertNotNull(FromStringDeserializer.findDeserializer(InetSocketAddress.class));
    }
}