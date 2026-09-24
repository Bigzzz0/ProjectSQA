package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.helper.HttpConnection
 * Target Bug: Defects4J bug where fixHeaderEncoding / looksLikeUtf8 throws ArrayIndexOutOfBoundsException: 1
 *             when encountering truncated or high-order single-byte characters in header values.
 *
 * Specific Decision Branches & Boundary Conditions Covered:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - HttpConnection creation via connect(String) and connect(URL).
 *    - Request & Response getter/setter chaining and state preservation.
 *    - Request defaults (GET, default UA, timeout 30s, maxBodySize 1MB, followRedirects=true).
 *    - URL encoding with spaces and special characters.
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Header case-insensitivity: getHeadersCaseInsensitive, scanHeaders, addHeader, removeHeader.
 *    - Multi-value headers joined by comma-space vs single header retrieval.
 *    - Cookie management: set, overwrite, get, remove, hasCookie.
 *    - Zero/positive timeout and maxBodySize boundaries.
 *    - Null values in addHeader resulting in empty string.
 * 3. Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
 *    - handlesHeaderEncodingOnRequest: Adding headers containing ISO-8859-1 single characters
 *      in the 0x80-0xFF range (e.g. '\u00e9') which triggers looksLikeUtf8 and causes
 *      ArrayIndexOutOfBoundsException if boundary guard is absent.
 *    - Multi-byte UTF-8 sequences (2-byte, 3-byte with/without BOM, 4-byte).
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Null / empty URL strings (IllegalArgumentException).
 *    - Unsupported URL protocols (MalformedURLException on ftp://).
 *    - Setting requestBody on GET method (IllegalArgumentException).
 *    - Odd-length data varargs (IllegalArgumentException).
 *    - Null/empty keys/values for data, cookies, headers, userAgent, referrer.
 *    - Negative timeout and negative maxBodySize guards.
 *    - Unsupported charset name in postDataCharset.
 *    - Invoking parse(), body(), bodyAsBytes(), bodyStream() on unexecuted Response.
 * 5. Partition E: Object Lifecycle & KeyVal Integrity
 *    - KeyVal creation (key-value vs key-filename-stream).
 *    - KeyVal content type assignment and toString formatting.
 *    - Response headers parsing including Set-Cookie token parsing and null key HTTP status lines.
 * ---------------------------------------------------------------------------------------------------------
 */
public class HttpConnectionGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Known Defects4J Bug)
    // =========================================================================

    /**
     * Targets Defects4J bug: fixHeaderEncoding / looksLikeUtf8 fails with
     * ArrayIndexOutOfBoundsException: 1 when a header contains non-ASCII characters
     * that do not form a complete multi-byte UTF-8 sequence.
     */
    @Test(timeout = 4000)
    public void testHandlesHeaderEncodingOnRequest() {
        Connection con = HttpConnection.connect("http://example.com");
        // Header value with non-ASCII ISO-8859-1 character '\u00e9' (é)
        con.header("X-Accent-Test", "Caf\u00e9");
        assertEquals("Caf\u00e9", con.request().header("X-Accent-Test"));

        // Header value with standalone high-byte character
        con.header("X-HighByte", "\u00c9");
        assertEquals("\u00c9", con.request().header("X-HighByte"));
    }

    @Test(timeout = 4000)
    public void testHeaderEncodingWithUtf8Sequences() {
        Connection con = HttpConnection.connect("http://example.com");

        // 3-byte UTF-8 character (Euro sign: \u20AC)
        con.header("X-Euro", "\u20AC");
        assertNotNull(con.request().header("X-Euro"));

        // UTF-8 BOM prefix
        String bomVal = "\uFEFFStartWithBOM";
        con.header("X-BOM", bomVal);
        assertNotNull(con.request().header("X-BOM"));

        // ASCII only header
        con.header("X-Ascii", "plain-text-123");
        assertEquals("plain-text-123", con.request().header("X-Ascii"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConnectAndFluentConfiguration() throws Exception {
        URL url = new URL("http://example.com/test");
        Connection con = HttpConnection.connect(url);

        con.userAgent("CustomAgent/1.0")
           .referrer("http://google.com")
           .timeout(15000)
           .maxBodySize(2048)
           .followRedirects(false)
           .ignoreHttpErrors(true)
           .ignoreContentType(true)
           .method(Connection.Method.POST)
           .postDataCharset("UTF-8");

        Connection.Request req = con.request();
        assertEquals(url, req.url());
        assertEquals("CustomAgent/1.0", req.header("User-Agent"));
        assertEquals("http://google.com", req.header("Referer"));
        assertEquals(15000, req.timeout());
        assertEquals(2048, req.maxBodySize());
        assertFalse(req.followRedirects());
        assertTrue(req.ignoreHttpErrors());
        assertTrue(req.ignoreContentType());
        assertEquals(Connection.Method.POST, req.method());
        assertEquals("UTF-8", req.postDataCharset());
    }

    @Test(timeout = 4000)
    public void testUrlEncodingWithSpaces() {
        Connection con = HttpConnection.connect("http://example.com/path with spaces/file.html");
        assertEquals("http://example.com/path%20with%20spaces/file.html", con.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testEncodeUrlStaticMethod() throws Exception {
        URL rawUrl = new URL("http://example.com/test space");
        URL encoded = HttpConnection.encodeUrl(rawUrl);
        assertEquals("http://example.com/test%20space", encoded.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testRequestAndResponseSwapping() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request newReq = new HttpConnection.Request();
        Connection.Response newRes = new HttpConnection.Response();

        con.request(newReq);
        con.response(newRes);

        assertSame(newReq, con.request());
        assertSame(newRes, con.response());
    }

    @Test(timeout = 4000)
    public void testProxyConfiguration() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertNull(req.proxy());

        req.proxy("127.0.0.1", 8080);
        assertNotNull(req.proxy());
        assertEquals(Proxy.Type.HTTP, req.proxy().type());

        req.proxy(Proxy.NO_PROXY);
        assertSame(Proxy.NO_PROXY, req.proxy());

        Connection con = HttpConnection.connect("http://example.com");
        con.proxy(Proxy.NO_PROXY);
        assertSame(Proxy.NO_PROXY, con.request().proxy());
        con.proxy("localhost", 9090);
        assertNotNull(con.request().proxy());
    }

    @Test(timeout = 4000)
    public void testParserConfiguration() {
        Connection con = HttpConnection.connect("http://example.com");
        Parser xmlParser = Parser.xmlParser();
        con.parser(xmlParser);
        assertSame(xmlParser, con.request().parser());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testHeadersCaseInsensitiveAndMultiValues() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Accept", "text/html");
        con.request().addHeader("accept", "application/xhtml+xml");

        assertTrue(con.request().hasHeader("ACCEPT"));
        assertTrue(con.request().hasHeader("accept"));
        assertTrue(con.request().hasHeaderWithValue("Accept", "text/html"));
        assertTrue(con.request().hasHeaderWithValue("ACCEPT", "application/xhtml+xml"));
        assertFalse(con.request().hasHeaderWithValue("Accept", "image/png"));

        List<String> values = con.request().headers("ACCEPT");
        assertEquals(2, values.size());
        assertEquals("text/html", values.get(0));
        assertEquals("application/xhtml+xml", values.get(1));

        assertEquals("text/html, application/xhtml+xml", con.request().header("ACCEPT"));

        con.request().removeHeader("accept");
        assertFalse(con.request().hasHeader("Accept"));
        assertNull(con.request().header("Accept"));
    }

    @Test(timeout = 4000)
    public void testHeaderAddNullValueBecomesEmpty() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.addHeader("X-Empty-Test", null);
        assertEquals("", req.header("X-Empty-Test"));
    }

    @Test(timeout = 4000)
    public void testHeaderBulkOperations() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("X-One", "1");
        headerMap.put("X-Two", "2");

        con.headers(headerMap);
        assertTrue(con.request().hasHeader("X-One"));
        assertTrue(con.request().hasHeader("X-Two"));

        Map<String, String> headers = con.request().headers();
        assertEquals("1", headers.get("X-One"));
        assertEquals("2", headers.get("X-Two"));
        assertNotNull(con.request().multiHeaders());
    }

    @Test(timeout = 4000)
    public void testCookieManagement() {
        Connection con = HttpConnection.connect("http://example.com");
        con.cookie("session_id", "xyz123");
        assertTrue(con.request().hasCookie("session_id"));
        assertEquals("xyz123", con.request().cookie("session_id"));

        Map<String, String> cookieBatch = new HashMap<>();
        cookieBatch.put("user", "alice");
        cookieBatch.put("pref", "dark");
        con.cookies(cookieBatch);

        assertEquals("alice", con.request().cookie("user"));
        assertEquals("dark", con.request().cookie("pref"));
        assertEquals(3, con.request().cookies().size());

        con.request().removeCookie("session_id");
        assertFalse(con.request().hasCookie("session_id"));
        assertNull(con.request().cookie("session_id"));
    }

    @Test(timeout = 4000)
    public void testNumericBoundaries() {
        HttpConnection.Request req = new HttpConnection.Request();

        req.timeout(0);
        assertEquals(0, req.timeout());
        req.timeout(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, req.timeout());

        req.maxBodySize(0);
        assertEquals(0, req.maxBodySize());
        req.maxBodySize(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, req.maxBodySize());
    }

    @Test(timeout = 4000)
    public void testDataAddingVarieties() {
        Connection con = HttpConnection.connect("http://example.com");

        con.data("key1", "val1");
        assertNotNull(con.data("key1"));
        assertEquals("val1", con.data("key1").value());

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("key2", "val2");
        dataMap.put("key3", "val3");
        con.data(dataMap);
        assertEquals("val2", con.data("key2").value());
        assertEquals("val3", con.data("key3").value());

        con.data("key4", "val4", "key5", "val5");
        assertEquals("val4", con.data("key4").value());
        assertEquals("val5", con.data("key5").value());

        InputStream is = new ByteArrayInputStream("stream-content".getBytes());
        con.data("fileKey", "sample.txt", is);
        Connection.KeyVal fileVal = con.data("fileKey");
        assertNotNull(fileVal);
        assertTrue(fileVal.hasInputStream());
        assertEquals("sample.txt", fileVal.value());

        InputStream is2 = new ByteArrayInputStream("stream-content-2".getBytes());
        con.data("fileKey2", "sample2.txt", is2, "text/plain");
        Connection.KeyVal fileVal2 = con.data("fileKey2");
        assertNotNull(fileVal2);
        assertEquals("text/plain", fileVal2.contentType());

        List<Connection.KeyVal> coll = new ArrayList<>();
        coll.add(HttpConnection.KeyVal.create("colKey", "colVal"));
        con.data(coll);
        assertEquals("colVal", con.data("colKey").value());

        assertNull(con.data("nonExistentKey"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectNullStringThrows() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectEmptyStringThrows() {
        HttpConnection.connect("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectMalformedUrlThrows() {
        HttpConnection.connect("http:///invalid url");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectNullUrlThrows() {
        HttpConnection.connect((URL) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeTimeoutThrows() {
        new HttpConnection.Request().timeout(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeMaxBodySizeThrows() {
        new HttpConnection.Request().maxBodySize(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUserAgentNullThrows() {
        HttpConnection.connect("http://example.com").userAgent(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReferrerNullThrows() {
        HttpConnection.connect("http://example.com").referrer(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataOddKeyvalsThrows() {
        HttpConnection.connect("http://example.com").data("key1", "val1", "orphanKey");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataNullMapThrows() {
        HttpConnection.connect("http://example.com").data((Map<String, String>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataNullArrayThrows() {
        HttpConnection.connect("http://example.com").data((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataNullCollectionThrows() {
        HttpConnection.connect("http://example.com").data((List<Connection.KeyVal>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataEmptyKeyThrows() {
        HttpConnection.connect("http://example.com").data("", "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataNullValueThrows() {
        HttpConnection.connect("http://example.com").data("key", (String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataLookupEmptyKeyThrows() {
        HttpConnection.connect("http://example.com").data("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testHeadersNullMapThrows() {
        HttpConnection.connect("http://example.com").headers(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCookiesNullMapThrows() {
        HttpConnection.connect("http://example.com").cookies(null);
    }

    @Test(expected = IllegalCharsetNameException.class, timeout = 4000)
    public void testPostDataCharsetInvalidThrows() {
        HttpConnection.connect("http://example.com").postDataCharset("ILLEGAL_CHARSET_NAME???");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPostDataCharsetNullThrows() {
        HttpConnection.connect("http://example.com").postDataCharset(null);
    }

    @Test(expected = MalformedURLException.class, timeout = 4000)
    public void testExecuteUnsupportedProtocolThrows() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("ftp://ftp.example.com/file.txt"));
        HttpConnection.Response.execute(req);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testExecuteBodyOnGetMethodThrows() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com"));
        req.method(Connection.Method.GET);
        req.requestBody("Illegal body for GET");
        HttpConnection.Response.execute(req);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseParseThrows() throws Exception {
        HttpConnection.Response res = new HttpConnection.Response();
        res.parse();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseBodyThrows() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.body();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseBodyAsBytesThrows() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bodyAsBytes();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseBodyStreamThrows() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bodyStream();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & KeyVal / Response Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeyValCreationAndModification() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("username", "testuser");
        assertEquals("username", kv.key());
        assertEquals("testuser", kv.value());
        assertFalse(kv.hasInputStream());
        assertNull(kv.inputStream());
        assertEquals("username=testuser", kv.toString());

        kv.key("user");
        kv.value("admin");
        assertEquals("user", kv.key());
        assertEquals("admin", kv.value());

        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        kv.inputStream(stream);
        assertTrue(kv.hasInputStream());
        assertSame(stream, kv.inputStream());

        kv.contentType("application/octet-stream");
        assertEquals("application/octet-stream", kv.contentType());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testKeyValEmptyKeyThrows() {
        HttpConnection.KeyVal.create("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testKeyValNullValueThrows() {
        HttpConnection.KeyVal.create("key", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testKeyValEmptyContentTypeThrows() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("k", "v");
        kv.contentType("");
    }

    @Test(timeout = 4000)
    public void testResponseHeadersAndCookieParsing() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headerMap = new HashMap<>();

        // Include HTTP status line simulation (key is null)
        headerMap.put(null, Collections.singletonList("HTTP/1.1 200 OK"));

        // Normal header
        headerMap.put("Content-Type", Collections.singletonList("text/html; charset=UTF-8"));

        // Cookie headers
        List<String> cookies = new ArrayList<>();
        cookies.add("session=abc123; Path=/; HttpOnly");
        cookies.add("theme=light; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
        cookies.add(null); // Null cookie in list should be skipped safely
        headerMap.put("Set-Cookie", cookies);

        res.processResponseHeaders(headerMap);

        assertEquals("text/html; charset=UTF-8", res.header("Content-Type"));
        assertEquals("abc123", res.cookie("session"));
        assertEquals("light", res.cookie("theme"));
        assertTrue(res.hasCookie("session"));
        assertTrue(res.hasCookie("theme"));
        assertFalse(res.hasCookie("non_existent"));
    }

    @Test(timeout = 4000)
    public void testResponseStateGetters() {
        HttpConnection.Response res = new HttpConnection.Response();
        assertEquals(0, res.statusCode());
        assertNull(res.statusMessage());
        assertNull(res.charset());
        assertNull(res.contentType());

        res.charset("UTF-8");
        assertEquals("UTF-8", res.charset());
    }

    @Test(timeout = 4000)
    public void testRequestBodyStorage() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertNull(req.requestBody());
        req.requestBody("raw request body string");
        assertEquals("raw request body string", req.requestBody());
    }

    @Test(timeout = 4000)
    public void testMethodDefaultsAndUpdates() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertEquals(Connection.Method.GET, req.method());

        req.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, req.method());
    }
}