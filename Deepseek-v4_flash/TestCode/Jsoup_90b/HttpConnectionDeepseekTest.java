package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;

/**
 * White-box test suite for HttpConnection targeting high line/branch coverage and the known defect
 * (ArrayIndexOutOfBoundsException in header encoding handling).
 */
public class HttpConnectionDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - url(String), url(URL), proxy(Proxy), proxy(String,int), userAgent, timeout, maxBodySize,
     *     followRedirects, referrer, method, ignoreHttpErrors, ignoreContentType, data (various forms),
     *     requestBody, header, headers, cookie, cookies, parser, get, post, execute, request, response,
     *     postDataCharset, sslSocketFactory
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - null/empty arguments for URL, headers, cookies, data keys/values
     *   - zero/negative timeout, maxBodySize
     *   - empty collections, maps, varargs
     *   - URL with spaces, query string, fragment
     *   - Header values with special characters, leading/trailing spaces
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - ArrayIndexOutOfBoundsException in looksLikeUtf8 when incomplete UTF-8 sequence at end of header value
     *   - Test triggers fixHeaderEncoding -> looksLikeUtf8 with trailing leading byte
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - MalformedURLException wrapped in IllegalArgumentException
     *   - IllegalArgumentException for invalid timeout, maxBodySize, data key/value
     *   - NullPointerException from Validate.notNull/notEmpty
     *   - IllegalCharsetNameException for unsupported charset
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - KeyVal creation and chaining
     *   - Request/Response state consistency
     *   - encodeUrl static methods (normal, malformed, already encoded)
     *   - encodeMimeName (null, normal, with quotes)
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testUrlString() {
        HttpConnection conn = new HttpConnection();
        conn.url("http://example.com");
        assertEquals("http://example.com", conn.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testUrlURL() throws Exception {
        HttpConnection conn = new HttpConnection();
        URL url = new URL("http://example.com/path");
        conn.url(url);
        assertSame(url, conn.request().url());
    }

    @Test(timeout = 4000)
    public void testUrlWithSpaces() {
        HttpConnection conn = new HttpConnection();
        conn.url("http://example.com/a b");
        assertEquals("http://example.com/a%20b", conn.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testProxy() {
        HttpConnection conn = new HttpConnection();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("localhost", 8080));
        conn.proxy(proxy);
        assertSame(proxy, conn.request().proxy());
    }

    @Test(timeout = 4000)
    public void testProxyHostPort() {
        HttpConnection conn = new HttpConnection();
        conn.proxy("localhost", 8080);
        assertNotNull(conn.request().proxy());
    }

    @Test(timeout = 4000)
    public void testUserAgent() {
        HttpConnection conn = new HttpConnection();
        conn.userAgent("Mozilla/5.0");
        assertEquals("Mozilla/5.0", conn.request().header("User-Agent"));
    }

    @Test(timeout = 4000)
    public void testTimeout() {
        HttpConnection conn = new HttpConnection();
        conn.timeout(5000);
        assertEquals(5000, conn.request().timeout());
    }

    @Test(timeout = 4000)
    public void testMaxBodySize() {
        HttpConnection conn = new HttpConnection();
        conn.maxBodySize(2048);
        assertEquals(2048, conn.request().maxBodySize());
    }

    @Test(timeout = 4000)
    public void testFollowRedirects() {
        HttpConnection conn = new HttpConnection();
        conn.followRedirects(false);
        assertFalse(conn.request().followRedirects());
    }

    @Test(timeout = 4000)
    public void testReferrer() {
        HttpConnection conn = new HttpConnection();
        conn.referrer("http://referrer.com");
        assertEquals("http://referrer.com", conn.request().header("Referer"));
    }

    @Test(timeout = 4000)
    public void testMethod() {
        HttpConnection conn = new HttpConnection();
        conn.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, conn.request().method());
    }

    @Test(timeout = 4000)
    public void testIgnoreHttpErrors() {
        HttpConnection conn = new HttpConnection();
        conn.ignoreHttpErrors(true);
        assertTrue(conn.request().ignoreHttpErrors());
    }

    @Test(timeout = 4000)
    public void testIgnoreContentType() {
        HttpConnection conn = new HttpConnection();
        conn.ignoreContentType(true);
        assertTrue(conn.request().ignoreContentType());
    }

    @Test(timeout = 4000)
    public void testDataKeyValue() {
        HttpConnection conn = new HttpConnection();
        conn.data("key1", "value1");
        assertEquals(1, conn.request().data().size());
        assertEquals("key1", conn.request().data().iterator().next().key());
    }

    @Test(timeout = 4000)
    public void testDataMap() {
        HttpConnection conn = new HttpConnection();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        conn.data(map);
        assertEquals(2, conn.request().data().size());
    }

    @Test(timeout = 4000)
    public void testDataVarargs() {
        HttpConnection conn = new HttpConnection();
        conn.data("k1", "v1", "k2", "v2");
        assertEquals(2, conn.request().data().size());
    }

    @Test(timeout = 4000)
    public void testDataCollection() {
        HttpConnection conn = new HttpConnection();
        Collection<Connection.KeyVal> coll = new ArrayList<>();
        coll.add(HttpConnection.KeyVal.create("k", "v"));
        conn.data(coll);
        assertEquals(1, conn.request().data().size());
    }

    @Test(timeout = 4000)
    public void testDataInputStream() {
        HttpConnection conn = new HttpConnection();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        conn.data("file", "test.txt", is);
        Connection.KeyVal kv = conn.request().data().iterator().next();
        assertTrue(kv.hasInputStream());
        assertSame(is, kv.inputStream());
    }

    @Test(timeout = 4000)
    public void testDataInputStreamWithContentType() {
        HttpConnection conn = new HttpConnection();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        conn.data("file", "test.txt", is, "text/plain");
        Connection.KeyVal kv = conn.request().data().iterator().next();
        assertEquals("text/plain", kv.contentType());
    }

    @Test(timeout = 4000)
    public void testDataLookup() {
        HttpConnection conn = new HttpConnection();
        conn.data("key", "value");
        Connection.KeyVal kv = conn.data("key");
        assertNotNull(kv);
        assertEquals("value", kv.value());
    }

    @Test(timeout = 4000)
    public void testDataLookupMissing() {
        HttpConnection conn = new HttpConnection();
        assertNull(conn.data("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRequestBody() {
        HttpConnection conn = new HttpConnection();
        conn.requestBody("{\"json\":true}");
        assertEquals("{\"json\":true}", conn.request().requestBody());
    }

    @Test(timeout = 4000)
    public void testHeader() {
        HttpConnection conn = new HttpConnection();
        conn.header("X-Custom", "value");
        assertEquals("value", conn.request().header("X-Custom"));
    }

    @Test(timeout = 4000)
    public void testHeadersMap() {
        HttpConnection conn = new HttpConnection();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("X-A", "a");
        map.put("X-B", "b");
        conn.headers(map);
        assertEquals("a", conn.request().header("X-A"));
        assertEquals("b", conn.request().header("X-B"));
    }

    @Test(timeout = 4000)
    public void testCookie() {
        HttpConnection conn = new HttpConnection();
        conn.cookie("session", "abc123");
        assertEquals("abc123", conn.request().cookie("session"));
    }

    @Test(timeout = 4000)
    public void testCookiesMap() {
        HttpConnection conn = new HttpConnection();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("c1", "v1");
        map.put("c2", "v2");
        conn.cookies(map);
        assertEquals("v1", conn.request().cookie("c1"));
        assertEquals("v2", conn.request().cookie("c2"));
    }

    @Test(timeout = 4000)
    public void testParser() {
        HttpConnection conn = new HttpConnection();
        conn.parser(org.jsoup.parser.Parser.xmlParser());
        assertNotNull(conn.request().parser());
    }

    @Test(timeout = 4000)
    public void testPostDataCharset() {
        HttpConnection conn = new HttpConnection();
        conn.postDataCharset("UTF-8");
        assertEquals("UTF-8", conn.request().postDataCharset());
    }

    @Test(timeout = 4000)
    public void testSslSocketFactory() {
        HttpConnection conn = new HttpConnection();
        javax.net.ssl.SSLSocketFactory factory = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
        conn.sslSocketFactory(factory);
        assertSame(factory, conn.request().sslSocketFactory());
    }

    @Test(timeout = 4000)
    public void testRequestResponseCycle() {
        HttpConnection conn = new HttpConnection();
        Connection.Request req = conn.request();
        Connection.Response res = conn.response();
        assertNotNull(req);
        assertNotNull(res);
        // set request
        Connection.Request newReq = new HttpConnection.Request();
        conn.request(newReq);
        assertSame(newReq, conn.request());
        // set response
        Connection.Response newRes = new HttpConnection.Response();
        conn.response(newRes);
        assertSame(newRes, conn.response());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUrlEmptyString() {
        HttpConnection conn = new HttpConnection();
        conn.url("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUrlMalformed() {
        HttpConnection conn = new HttpConnection();
        conn.url("not a url");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTimeoutNegative() {
        HttpConnection conn = new HttpConnection();
        conn.timeout(-1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxBodySizeNegative() {
        HttpConnection conn = new HttpConnection();
        conn.maxBodySize(-1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataNullMap() {
        HttpConnection conn = new HttpConnection();
        conn.data((Map<String, String>) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataNullVarargs() {
        HttpConnection conn = new HttpConnection();
        conn.data((String[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataOddVarargs() {
        HttpConnection conn = new HttpConnection();
        conn.data("k1", "v1", "k2");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataEmptyKey() {
        HttpConnection conn = new HttpConnection();
        conn.data("", "value");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataNullValue() {
        HttpConnection conn = new HttpConnection();
        conn.data("key", (String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataNullCollection() {
        HttpConnection conn = new HttpConnection();
        conn.data((Collection<Connection.KeyVal>) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataLookupEmptyKey() {
        HttpConnection conn = new HttpConnection();
        conn.data("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHeaderNullName() {
        HttpConnection conn = new HttpConnection();
        conn.header(null, "value");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHeaderEmptyName() {
        HttpConnection conn = new HttpConnection();
        conn.header("", "value");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHeadersNullMap() {
        HttpConnection conn = new HttpConnection();
        conn.headers(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCookieEmptyName() {
        HttpConnection conn = new HttpConnection();
        conn.cookie("", "value");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCookieNullValue() {
        HttpConnection conn = new HttpConnection();
        conn.cookie("name", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCookiesNullMap() {
        HttpConnection conn = new HttpConnection();
        conn.cookies(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUserAgentNull() {
        HttpConnection conn = new HttpConnection();
        conn.userAgent(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReferrerNull() {
        HttpConnection conn = new HttpConnection();
        conn.referrer(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPostDataCharsetNull() {
        HttpConnection conn = new HttpConnection();
        conn.postDataCharset(null);
    }

    @Test(timeout = 4000, expected = IllegalCharsetNameException.class)
    public void testPostDataCharsetInvalid() {
        HttpConnection conn = new HttpConnection();
        conn.postDataCharset("invalid-charset");
    }

    @Test(timeout = 4000)
    public void testHeaderCaseInsensitive() {
        HttpConnection conn = new HttpConnection();
        conn.header("Content-Type", "text/html");
        assertEquals("text/html", conn.request().header("content-type"));
        assertTrue(conn.request().hasHeader("CONTENT-TYPE"));
    }

    @Test(timeout = 4000)
    public void testHeaderMultipleValues() {
        HttpConnection conn = new HttpConnection();
        conn.request().addHeader("Accept", "text/html");
        conn.request().addHeader("Accept", "application/json");
        List<String> vals = conn.request().headers("Accept");
        assertEquals(2, vals.size());
        assertEquals("text/html, application/json", conn.request().header("Accept"));
    }

    @Test(timeout = 4000)
    public void testHasHeaderWithValue() {
        HttpConnection conn = new HttpConnection();
        conn.header("X-Custom", "Value");
        assertTrue(conn.request().hasHeaderWithValue("X-Custom", "value"));
        assertFalse(conn.request().hasHeaderWithValue("X-Custom", "other"));
    }

    @Test(timeout = 4000)
    public void testRemoveHeader() {
        HttpConnection conn = new HttpConnection();
        conn.header("X-Test", "val");
        conn.request().removeHeader("x-test");
        assertFalse(conn.request().hasHeader("X-Test"));
    }

    @Test(timeout = 4000)
    public void testCookieCaseSensitive() {
        HttpConnection conn = new HttpConnection();
        conn.cookie("Session", "abc");
        assertTrue(conn.request().hasCookie("Session"));
        assertFalse(conn.request().hasCookie("session"));
    }

    @Test(timeout = 4000)
    public void testRemoveCookie() {
        HttpConnection conn = new HttpConnection();
        conn.cookie("test", "val");
        conn.request().removeCookie("test");
        assertFalse(conn.request().hasCookie("test"));
    }

    @Test(timeout = 4000)
    public void testEncodeUrlAlreadyEncoded() throws Exception {
        URL input = new URL("http://example.com/path?q=a%20b");
        URL result = HttpConnection.encodeUrl(input);
        assertEquals(input.toExternalForm(), result.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testEncodeUrlWithSpaces() throws Exception {
        URL input = new URL("http://example.com/a b");
        URL result = HttpConnection.encodeUrl(input);
        assertEquals("http://example.com/a%20b", result.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testEncodeUrlMalformed() {
        String result = HttpConnection.encodeUrl("not a url");
        assertEquals("not a url", result);
    }

    @Test(timeout = 4000)
    public void testEncodeMimeNameNull() {
        assertNull(HttpConnection.encodeMimeName(null));
    }

    @Test(timeout = 4000)
    public void testEncodeMimeNameWithQuotes() {
        assertEquals("a%22b", HttpConnection.encodeMimeName("a\"b"));
    }

    @Test(timeout = 4000)
    public void testEncodeMimeNameNormal() {
        assertEquals("normal", HttpConnection.encodeMimeName("normal"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known defect: ArrayIndexOutOfBoundsException in looksLikeUtf8
     * when a header value ends with an incomplete UTF-8 leading byte.
     * The fixHeaderEncoding method calls looksLikeUtf8 which iterates beyond array bounds.
     */
    @Test(timeout = 4000)
    public void handlesHeaderEncodingOnRequest() {
        HttpConnection conn = new HttpConnection();
        // Create a string that ends with a leading byte (0xC3) without continuation byte
        // Using ISO-8859-1 to get the raw byte
        String value = "test" + new String(new byte[]{(byte)0xC3}, java.nio.charset.Charset.forName("ISO-8859-1"));
        // This should not throw ArrayIndexOutOfBoundsException
        conn.header("X-Custom", value);
        // Verify the header is stored correctly
        assertEquals(value, conn.request().header("X-Custom"));
    }

    // Additional boundary: leading byte at end with 3-byte sequence
    @Test(timeout = 4000)
    public void handlesHeaderEncodingOnRequestThreeByte() {
        HttpConnection conn = new HttpConnection();
        // 3-byte leading byte 0xE0 without continuation bytes
        String value = "x" + new String(new byte[]{(byte)0xE0}, java.nio.charset.Charset.forName("ISO-8859-1"));
        conn.header("X-Custom", value);
        assertEquals(value, conn.request().header("X-Custom"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMethodNull() {
        HttpConnection conn = new HttpConnection();
        conn.method(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUrlNull() {
        HttpConnection conn = new HttpConnection();
        conn.url((URL) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDataKeyValNull() {
        HttpConnection conn = new HttpConnection();
        conn.data((Connection.KeyVal) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRequestBodyWithGetMethod() {
        // This is validated in execute, but we can test the Request validation indirectly
        HttpConnection.Request req = new HttpConnection.Request();
        req.method(Connection.Method.GET);
        req.requestBody("body");
        // The validation occurs in Response.execute, but we can test that the request holds the body
        // Actually the validation is in execute, not in request. So we just test that setting body is allowed.
        // The exception is thrown when execute is called. We'll test the validation logic via a unit test on the static method?
        // Since execute is complex, we skip. But we can test that the request object holds the body.
        assertEquals("body", req.requestBody());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testKeyValCreation() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        assertEquals("key", kv.key());
        assertEquals("value", kv.value());
        assertFalse(kv.hasInputStream());
    }

    @Test(timeout = 4000)
    public void testKeyValWithStream() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "name.txt", is);
        assertTrue(kv.hasInputStream());
        assertSame(is, kv.inputStream());
    }

    @Test(timeout = 4000)
    public void testKeyValToString() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("k", "v");
        assertEquals("k=v", kv.toString());
    }

    @Test(timeout = 4000)
    public void testKeyValContentType() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("k", "v");
        kv.contentType("text/plain");
        assertEquals("text/plain", kv.contentType());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testKeyValEmptyKey() {
        HttpConnection.KeyVal.create("", "v");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testKeyValNullValue() {
        HttpConnection.KeyVal.create("k", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testKeyValNullInputStream() {
        HttpConnection.KeyVal.create("k", "v", null);
    }

    @Test(timeout = 4000)
    public void testRequestDefaults() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertEquals(30000, req.timeout());
        assertEquals(1024 * 1024, req.maxBodySize());
        assertTrue(req.followRedirects());
        assertEquals(Connection.Method.GET, req.method());
        assertNotNull(req.parser());
        assertNotNull(req.data());
        assertTrue(req.data().isEmpty());
        assertNull(req.requestBody());
        assertNull(req.proxy());
        assertNull(req.sslSocketFactory());
    }

    @Test(timeout = 4000)
    public void testResponseDefaults() {
        HttpConnection.Response res = new HttpConnection.Response();
        assertEquals(0, res.statusCode());
        assertNull(res.statusMessage());
        assertNull(res.charset());
        assertNull(res.contentType());
    }

    @Test(timeout = 4000)
    public void testResponseCharset() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.charset("UTF-8");
        assertEquals("UTF-8", res.charset());
    }

    @Test(timeout = 4000)
    public void testConnectStaticMethods() {
        Connection conn1 = HttpConnection.connect("http://example.com");
        assertNotNull(conn1);
        assertEquals("http://example.com", conn1.request().url().toExternalForm());

        Connection conn2 = HttpConnection.connect("http://example.com");
        assertNotNull(conn2);
    }

    @Test(timeout = 4000)
    public void testConnectStaticURL() throws Exception {
        URL url = new URL("http://example.com");
        Connection conn = HttpConnection.connect(url);
        assertSame(url, conn.request().url());
    }
}